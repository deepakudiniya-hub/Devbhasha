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
const SECRETS = [RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET];

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
