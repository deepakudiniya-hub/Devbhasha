/**
 * Devbhasha — server-authoritative wallet.
 *
 * The app must never be able to set its own balance. All balance changes go
 * through these callable functions, which run with the Admin SDK and are the
 * only writers of `users/{uid}.walletBalance`.
 *
 * Secrets (Razorpay key id/secret) come from the environment / Functions
 * secrets — never hardcoded, never shipped in the app.
 *
 *   firebase functions:secrets:set RAZORPAY_KEY_ID
 *   firebase functions:secrets:set RAZORPAY_KEY_SECRET
 */

const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { defineSecret } = require("firebase-functions/params");
const admin = require("firebase-admin");
const crypto = require("crypto");
const Razorpay = require("razorpay");

admin.initializeApp();
const db = admin.firestore();
const FieldValue = admin.firestore.FieldValue;

const RAZORPAY_KEY_ID = defineSecret("RAZORPAY_KEY_ID");
const RAZORPAY_KEY_SECRET = defineSecret("RAZORPAY_KEY_SECRET");

const MAX_RECHARGE_PAISE = 500000; // ₹5,000 cap per recharge
const SIGNUP_BONUS_PAISE = 10000;  // ₹100 signup bonus — owner-configurable
const REFERRAL_BONUS_PAISE = 10000; // ₹100 referral bonus for referee and referrer
const SECRETS = [RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET];

async function processReferralReward(tx, uid, userSnap) {
  const userData = userSnap.data() || {};
  if (userData.referralBonusClaimed || !userData.referredBy) {
    return;
  }

  const referrerCodeOrUid = userData.referredBy;
  let referrerQuery = db.collection("users").where("referralCode", "==", referrerCodeOrUid);
  let referrerSnap = await tx.get(referrerQuery);
  let referrerDocRef = null;

  if (!referrerSnap.empty) {
    referrerDocRef = referrerSnap.docs[0].ref;
  } else {
    const directRef = db.collection("users").doc(referrerCodeOrUid);
    const directSnap = await tx.get(directRef);
    if (directSnap.exists) {
      referrerDocRef = directRef;
    }
  }

  if (!referrerDocRef) {
    tx.set(db.collection("users").doc(uid), { referralBonusClaimed: true }, { merge: true });
    return;
  }

  const refereeRef = db.collection("users").doc(uid);
  const refereeLedgerRef = db.collection("walletLedger").doc(`${uid}_referral_reward`);
  const referrerLedgerRef = db.collection("walletLedger").doc(`${referrerDocRef.id}_referral_earned_${uid}`);

  // Credit referee (₹100)
  tx.set(refereeRef, {
    walletBalance: FieldValue.increment(REFERRAL_BONUS_PAISE),
    referralBonusClaimed: true,
  }, { merge: true });

  tx.set(refereeLedgerRef, {
    uid,
    amountPaise: REFERRAL_BONUS_PAISE,
    purpose: "referral_welcome_bonus",
    ref: "referral_reward",
    createdAt: FieldValue.serverTimestamp(),
  });

  // Credit referrer (₹100)
  tx.set(referrerDocRef, {
    walletBalance: FieldValue.increment(REFERRAL_BONUS_PAISE),
    totalReferralEarnings: FieldValue.increment(REFERRAL_BONUS_PAISE),
  }, { merge: true });

  tx.set(referrerLedgerRef, {
    uid: referrerDocRef.id,
    amountPaise: REFERRAL_BONUS_PAISE,
    purpose: "referral_earned",
    ref: `referral_earned_${uid}`,
    createdAt: FieldValue.serverTimestamp(),
  });
}

function razorpayClient() {
  return new Razorpay({
    key_id: RAZORPAY_KEY_ID.value(),
    key_secret: RAZORPAY_KEY_SECRET.value(),
  });
}

function requireUid(request) {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Sign-in required.");
  }
  return request.auth.uid;
}

function requirePositivePaise(value) {
  const paise = Number(value);
  if (!Number.isInteger(paise) || paise <= 0 || paise > MAX_RECHARGE_PAISE) {
    throw new HttpsError("invalid-argument", "Invalid amount.");
  }
  return paise;
}

function balancePaiseOf(snapshot) {
  const v = snapshot.get("walletBalance");
  return typeof v === "number" ? v : 0;
}

/**
 * Create a Razorpay order server-side so checkout can be tied to an order id.
 * Returns the order id and the public key id for the checkout sheet.
 */
exports.createRazorpayOrder = onCall({ secrets: SECRETS }, async (request) => {
  const uid = requireUid(request);
  const amountPaise = requirePositivePaise(request.data && request.data.amountPaise);

  const order = await razorpayClient().orders.create({
    amount: amountPaise,
    currency: "INR",
    receipt: `rcpt_${uid}_${Date.now()}`,
    notes: { uid },
  });

  await db.collection("paymentOrders").doc(order.id).set({
    uid,
    amountPaise,
    status: "created",
    createdAt: FieldValue.serverTimestamp(),
  });

  return { orderId: order.id, amountPaise, keyId: RAZORPAY_KEY_ID.value() };
});

/**
 * Verify a Razorpay payment server-side and credit the wallet exactly once.
 */
exports.verifyRazorpayAndCredit = onCall({ secrets: SECRETS }, async (request) => {
  const uid = requireUid(request);
  const data = request.data || {};
  const { orderId, paymentId, signature } = data;
  if (!orderId || !paymentId || !signature) {
    throw new HttpsError("invalid-argument", "Missing payment fields.");
  }

  // 1. Verify the signature: HMAC-SHA256("orderId|paymentId", key_secret).
  const expected = crypto
    .createHmac("sha256", RAZORPAY_KEY_SECRET.value())
    .update(`${orderId}|${paymentId}`)
    .digest("hex");
  const expectedBuf = Buffer.from(expected, "utf8");
  const givenBuf = Buffer.from(String(signature), "utf8");
  const signatureOk =
    expectedBuf.length === givenBuf.length &&
    crypto.timingSafeEqual(expectedBuf, givenBuf);
  if (!signatureOk) {
    throw new HttpsError("permission-denied", "Invalid payment signature.");
  }

  // 2. The order must exist and belong to this user.
  const orderRef = db.collection("paymentOrders").doc(orderId);
  const orderSnap = await orderRef.get();
  if (!orderSnap.exists) {
    throw new HttpsError("not-found", "Unknown order.");
  }
  const order = orderSnap.data();
  if (order.uid !== uid) {
    throw new HttpsError("permission-denied", "Order does not belong to this user.");
  }

  // 3. Confirm with Razorpay that the payment is captured and the amount matches.
  const payment = await razorpayClient().payments.fetch(paymentId);
  if (payment.order_id !== orderId) {
    throw new HttpsError("permission-denied", "Payment/order mismatch.");
  }
  if (payment.status !== "captured" && payment.status !== "authorized") {
    throw new HttpsError("failed-precondition", `Payment not captured (${payment.status}).`);
  }
  if (Number(payment.amount) !== Number(order.amountPaise)) {
    throw new HttpsError("failed-precondition", "Amount mismatch.");
  }

  // 4. Credit idempotently — a payment id is credited at most once.
  const userRef = db.collection("users").doc(uid);
  const result = await db.runTransaction(async (tx) => {
    const payRef = db.collection("payments").doc(paymentId);
    const [paySnap, userSnap] = await Promise.all([tx.get(payRef), tx.get(userRef)]);

    if (paySnap.exists) {
      return { alreadyProcessed: true, balancePaise: balancePaiseOf(userSnap) };
    }

    tx.set(payRef, {
      uid,
      orderId,
      paymentId,
      amountPaise: order.amountPaise,
      status: "credited",
      createdAt: FieldValue.serverTimestamp(),
    });
    tx.set(userRef, { walletBalance: FieldValue.increment(order.amountPaise) }, { merge: true });
    tx.set(orderRef, { status: "paid", paymentId }, { merge: true });

    // Process referral reward upon first successful transaction
    await processReferralReward(tx, uid, userSnap);

    return {
      alreadyProcessed: false,
      balancePaise: balancePaiseOf(userSnap) + order.amountPaise,
    };
  });

  return result;
});

/**
 * Debit the wallet server-side, atomically and idempotently (keyed by `ref`).
 */
exports.walletSpend = onCall(async (request) => {
  const uid = requireUid(request);
  const data = request.data || {};
  const amountPaise = requirePositivePaise(data.amountPaise);
  const purpose = String(data.purpose || "spend");
  const ref = String(data.ref || "");
  if (!ref) {
    throw new HttpsError("invalid-argument", "Missing idempotency ref.");
  }

  const userRef = db.collection("users").doc(uid);
  const ledgerRef = db.collection("walletLedger").doc(`${uid}_${ref}`);

  return db.runTransaction(async (tx) => {
    const [userSnap, ledgerSnap] = await Promise.all([tx.get(userRef), tx.get(ledgerRef)]);
    const bal = balancePaiseOf(userSnap);

    if (ledgerSnap.exists) {
      return { alreadyProcessed: true, balancePaise: bal };
    }
    if (bal < amountPaise) {
      throw new HttpsError("failed-precondition", "Insufficient balance.");
    }

    tx.set(userRef, { walletBalance: FieldValue.increment(-amountPaise) }, { merge: true });
    tx.set(ledgerRef, {
      uid,
      amountPaise: -amountPaise,
      purpose,
      ref,
      createdAt: FieldValue.serverTimestamp(),
    });

    return { alreadyProcessed: false, balancePaise: bal - amountPaise };
  });
});

/**
 * Refund / credit the wallet server-side, atomically and idempotently.
 * Used when a paid action fails after a debit.
 */
exports.walletRefund = onCall(async (request) => {
  const uid = requireUid(request);
  const data = request.data || {};
  const amountPaise = requirePositivePaise(data.amountPaise);
  const purpose = String(data.purpose || "refund");
  const ref = String(data.ref || "");
  if (!ref) {
    throw new HttpsError("invalid-argument", "Missing idempotency ref.");
  }

  const userRef = db.collection("users").doc(uid);
  const ledgerRef = db.collection("walletLedger").doc(`${uid}_${ref}`);

  return db.runTransaction(async (tx) => {
    const [userSnap, ledgerSnap] = await Promise.all([tx.get(userRef), tx.get(ledgerRef)]);
    const bal = balancePaiseOf(userSnap);

    if (ledgerSnap.exists) {
      return { alreadyProcessed: true, balancePaise: bal };
    }

    tx.set(userRef, { walletBalance: FieldValue.increment(amountPaise) }, { merge: true });
    tx.set(ledgerRef, {
      uid,
      amountPaise,
      purpose,
      ref,
      createdAt: FieldValue.serverTimestamp(),
    });

    return { alreadyProcessed: false, balancePaise: bal + amountPaise };
  });
});

/**
 * Grant the one-time signup bonus.
 *
 * Idempotent: keyed by the `signup_bonus` ledger entry, so a user is credited
 * exactly once no matter how many times the app calls this. Safe to call on
 * every login.
 */
exports.claimSignupBonus = onCall(async (request) => {
  const uid = requireUid(request);
  const userRef = db.collection("users").doc(uid);
  const ledgerRef = db.collection("walletLedger").doc(`${uid}_signup_bonus`);

  return db.runTransaction(async (tx) => {
    const [userSnap, ledgerSnap] = await Promise.all([tx.get(userRef), tx.get(ledgerRef)]);
    const bal = balancePaiseOf(userSnap);

    if (ledgerSnap.exists) {
      return { alreadyClaimed: true, balancePaise: bal };
    }

    tx.set(userRef, { walletBalance: FieldValue.increment(SIGNUP_BONUS_PAISE) }, { merge: true });
    tx.set(ledgerRef, {
      uid,
      amountPaise: SIGNUP_BONUS_PAISE,
      purpose: "signup_bonus",
      ref: "signup_bonus",
      createdAt: FieldValue.serverTimestamp(),
    });

    return { alreadyClaimed: false, balancePaise: bal + SIGNUP_BONUS_PAISE };
  });
});

/**
 * Execute call settlement idempotently in a single Firestore transaction:
 * - active_calls -> 'ended': earnedAmount = ceil(durationSeconds/60)*ratePerMinute paise
 * - debit /users/{callerId}.walletBalance
 * - write /walletLedger
 * - credit /providers/{providerId}/transactions
 * - bump /providers/{providerId}/wallet/summary balance and totalEarnings
 * - update /active_calls/{callId} with earnedAmount and settled status
 */
async function executeSettleCall(callId, currentCallData = null) {
  const callRef = db.collection("active_calls").doc(callId);

  return db.runTransaction(async (tx) => {
    const callSnap = await tx.get(callRef);
    if (!callSnap.exists) {
      console.warn(`[settleCall] active_calls/${callId} does not exist`);
      return { skipped: true, reason: "not_found" };
    }

    const callData = callSnap.data() || {};
    // Idempotency: skip if already settled or earnedAmount is recorded
    if (callData.settled || callData.earnedAmount != null) {
      return { alreadySettled: true, earnedAmount: callData.earnedAmount };
    }

    const callerId = callData.callerId;
    const providerId = callData.providerId;
    if (!callerId || !providerId) {
      console.warn(`[settleCall] missing callerId or providerId for ${callId}`);
      return { skipped: true, reason: "missing_participants" };
    }

    const durationSeconds = Math.max(0, Number(callData.durationSeconds || 0));
    const ratePerMinute = Math.max(0, Number(callData.ratePerMinute || 0));
    const billedMinutes = Math.ceil(durationSeconds / 60);
    const earnedAmount = Math.round(billedMinutes * ratePerMinute); // integer paise

    const userRef = db.collection("users").doc(callerId);
    const ledgerRef = db.collection("walletLedger").doc(`${callerId}_call_${callId}`);
    const providerTxRef = db.collection("providers").doc(providerId).collection("transactions").doc(`call_${callId}`);
    const providerWalletRef = db.collection("providers").doc(providerId).collection("wallet").doc("summary");

    const [userSnap, ledgerSnap, providerTxSnap, providerWalletSnap] = await Promise.all([
      tx.get(userRef),
      tx.get(ledgerRef),
      tx.get(providerTxRef),
      tx.get(providerWalletRef),
    ]);

    if (ledgerSnap.exists || providerTxSnap.exists) {
      return { alreadySettled: true, earnedAmount };
    }

    // 1. Debit caller wallet balance
    tx.set(userRef, {
      walletBalance: FieldValue.increment(-earnedAmount),
    }, { merge: true });

    // 2. Write /walletLedger
    tx.set(ledgerRef, {
      uid: callerId,
      amountPaise: -earnedAmount,
      purpose: "call_charge",
      ref: `call_${callId}`,
      providerId,
      callId,
      durationSeconds,
      ratePerMinute,
      createdAt: FieldValue.serverTimestamp(),
    });

    // 3. Credit /providers/{providerId}/transactions
    tx.set(providerTxRef, {
      id: `call_${callId}`,
      type: "call_earning",
      amountPaise: earnedAmount,
      callId,
      callerId,
      callerName: callData.callerName || "",
      durationSeconds,
      ratePerMinute,
      timestamp: FieldValue.serverTimestamp(),
      status: "completed",
    });

    // 4. Bump /providers/{providerId}/wallet/summary balance and totalEarnings
    tx.set(providerWalletRef, {
      balance: FieldValue.increment(earnedAmount),
      totalEarnings: FieldValue.increment(earnedAmount),
      updatedAt: FieldValue.serverTimestamp(),
    }, { merge: true });

    // 5. Update /active_calls/{callId}
    tx.set(callRef, {
      earnedAmount,
      settled: true,
      settledAt: FieldValue.serverTimestamp(),
    }, { merge: true });

    return {
      success: true,
      callId,
      earnedAmount,
      billedMinutes,
      callerId,
      providerId,
    };
  });
}

/**
 * Settle active call: triggers when active_calls document transitions to 'ended'.
 */
exports.settleCall = onDocumentUpdated("active_calls/{callId}", async (event) => {
  const callId = event.params.callId;
  const before = event.data?.before?.data() || {};
  const after = event.data?.after?.data() || {};

  if (after.status !== "ended") {
    return null;
  }
  if (before.status === "ended") {
    return null;
  }

  return executeSettleCall(callId, after);
});

// Callable endpoint for manual or client invocation
exports.settleCallCallable = onCall(async (request) => {
  const data = request.data || {};
  const callId = String(data.callId || "");
  if (!callId) {
    throw new HttpsError("invalid-argument", "Missing callId.");
  }
  return executeSettleCall(callId);
});

/**
 * Execute question settlement idempotently in a single Firestore transaction:
 * - status -> 'answered'
 * - earningAmount in integer paise
 * - debit /users/{userId}.walletBalance
 * - write /walletLedger
 * - credit /providers/{providerId}/transactions
 * - bump /providers/{providerId}/wallet/summary balance and totalEarnings
 * - update /questions/{questionId} with earningAmount and settled: true
 */
async function executeSettleQuestion(questionId, currentQuestionData = null) {
  const questionRef = db.collection("questions").doc(questionId);

  return db.runTransaction(async (tx) => {
    const questionSnap = await tx.get(questionRef);
    if (!questionSnap.exists) {
      console.warn(`[settleQuestion] questions/${questionId} does not exist`);
      return { skipped: true, reason: "not_found" };
    }

    const qData = questionSnap.data() || {};
    if (qData.settled) {
      return { alreadySettled: true, earningAmount: qData.earningAmount };
    }

    const userId = qData.userId;
    const providerId = qData.providerId;
    if (!userId || !providerId) {
      console.warn(`[settleQuestion] missing userId or providerId for ${questionId}`);
      return { skipped: true, reason: "missing_participants" };
    }

    const rawAmount = qData.earningAmount != null ? qData.earningAmount : (qData.amountPaise != null ? qData.amountPaise : 5100);
    const earningAmount = Math.round(Math.max(0, Number(rawAmount)));

    const userRef = db.collection("users").doc(userId);
    const ledgerRef = db.collection("walletLedger").doc(`${userId}_question_${questionId}`);
    const providerTxRef = db.collection("providers").doc(providerId).collection("transactions").doc(`question_${questionId}`);
    const providerWalletRef = db.collection("providers").doc(providerId).collection("wallet").doc("summary");

    const [userSnap, ledgerSnap, providerTxSnap, providerWalletSnap] = await Promise.all([
      tx.get(userRef),
      tx.get(ledgerRef),
      tx.get(providerTxRef),
      tx.get(providerWalletRef),
    ]);

    if (ledgerSnap.exists || providerTxSnap.exists) {
      return { alreadySettled: true, earningAmount };
    }

    // 1. Debit seeker wallet balance
    tx.set(userRef, {
      walletBalance: FieldValue.increment(-earningAmount),
    }, { merge: true });

    // 2. Write /walletLedger
    tx.set(ledgerRef, {
      uid: userId,
      amountPaise: -earningAmount,
      purpose: "question_charge",
      ref: `question_${questionId}`,
      providerId,
      questionId,
      createdAt: FieldValue.serverTimestamp(),
    });

    // 3. Credit /providers/{providerId}/transactions
    tx.set(providerTxRef, {
      id: `question_${questionId}`,
      type: "question_earning",
      amountPaise: earningAmount,
      questionId,
      userId,
      userName: qData.userName || "",
      timestamp: FieldValue.serverTimestamp(),
      status: "completed",
    });

    // 4. Bump /providers/{providerId}/wallet/summary balance and totalEarnings
    tx.set(providerWalletRef, {
      balance: FieldValue.increment(earningAmount),
      totalEarnings: FieldValue.increment(earningAmount),
      updatedAt: FieldValue.serverTimestamp(),
    }, { merge: true });

    // 5. Update /questions/{questionId}
    tx.set(questionRef, {
      earningAmount,
      settled: true,
      settledAt: FieldValue.serverTimestamp(),
    }, { merge: true });

    return {
      success: true,
      questionId,
      earningAmount,
      userId,
      providerId,
    };
  });
}

/**
 * Settle question: triggers when questions document transitions to 'answered'.
 */
exports.settleQuestion = onDocumentUpdated("questions/{questionId}", async (event) => {
  const questionId = event.params.questionId;
  const before = event.data?.before?.data() || {};
  const after = event.data?.after?.data() || {};

  if (after.status !== "answered") {
    return null;
  }
  if (before.status === "answered") {
    return null;
  }

  return executeSettleQuestion(questionId, after);
});

// Callable endpoint for manual or client invocation
exports.settleQuestionCallable = onCall(async (request) => {
  const data = request.data || {};
  const questionId = String(data.questionId || "");
  if (!questionId) {
    throw new HttpsError("invalid-argument", "Missing questionId.");
  }
  return executeSettleQuestion(questionId);
});

exports.executeSettleCall = executeSettleCall;
exports.executeSettleQuestion = executeSettleQuestion;

