/**
 * Devbhasha — server-authoritative wallet, pricing and sessions.
 *
 * The app is untrusted. It never supplies a price, rate, duration or amount
 * for anything it is charged for. Every price lives in PRICE_TABLE below
 * (integer paise) and every wallet debit/credit happens inside a Firestore
 * transaction in this file, with a balance check before any debit.
 *
 * Secrets / params (never hardcoded, never shipped in the app):
 *   firebase functions:secrets:set RAZORPAY_KEY_ID
 *   firebase functions:secrets:set RAZORPAY_KEY_SECRET
 *   firebase functions:secrets:set AGORA_APP_CERTIFICATE
 *   AGORA_APP_ID  -> string param; put it in functions/.env (AGORA_APP_ID=...)
 *                    or answer the prompt at deploy time.
 */

const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { defineSecret, defineString } = require("firebase-functions/params");
const admin = require("firebase-admin");
const crypto = require("crypto");
const Razorpay = require("razorpay");
const { RtcTokenBuilder, RtcRole } = require("agora-token");

admin.initializeApp();
// Firestore database id comes from functions/.env (FIRESTORE_DB_ID); "(default)" if unset.
const { getFirestore } = require("firebase-admin/firestore");
const FIRESTORE_DB_ID = process.env.FIRESTORE_DB_ID || "(default)";
const db = FIRESTORE_DB_ID === "(default)" ? getFirestore() : getFirestore(FIRESTORE_DB_ID);
const FieldValue = admin.firestore.FieldValue;
const Timestamp = admin.firestore.Timestamp;

const RAZORPAY_KEY_ID = defineSecret("RAZORPAY_KEY_ID");
const RAZORPAY_KEY_SECRET = defineSecret("RAZORPAY_KEY_SECRET");
const AGORA_APP_CERTIFICATE = defineSecret("AGORA_APP_CERTIFICATE");
const AGORA_APP_ID = defineString("AGORA_APP_ID");
const RAZORPAY_SECRETS = [RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET];

// Firestore triggers must point at the named database.
const FIRESTORE_TRIGGER_OPTS = { database: FIRESTORE_DB_ID };

// ---------------------------------------------------------------------------
// Single price table — integer paise, server-only. Change prices HERE only.
// ---------------------------------------------------------------------------
const PRICE_TABLE = Object.freeze({
  SESSION: Object.freeze({ amountPaise: 49900, durationMinutes: 20 }),   // ₹499 / 20 min
  EXTENSION: Object.freeze({ amountPaise: 9900, durationMinutes: 5 }),   // ₹99 / +5 min
  DREAM_CHAT: Object.freeze({ amountPaise: 9900, durationMinutes: 7 }),  // ₹99 / 7 min
  DREAM_CALL: Object.freeze({ amountPaise: 19900, durationMinutes: 10 }), // ₹199 / 10 min
});
// First dream chat is free for 5 minutes, once per verified phone number.
const FREE_TRIAL = Object.freeze({
  type: "DREAM_CHAT",
  amountPaise: 0,
  durationMinutes: 5,
  sadhakPayoutPaise: 3000, // ₹30 fixed payout to the sadhak, paid by Devbhasha
});
const SADHAK_SHARE_PERCENT = 65; // Devbhasha keeps the remaining 35%
// Answered text questions (legacy flow): fixed server price, same as the
// previous default in this file (₹51). The client can no longer set it.
const QUESTION_PRICE_PAISE = 5100;

const SESSION_TYPES = ["SESSION", "DREAM_CHAT", "DREAM_CALL"];
const CALL_TYPES = ["SESSION", "DREAM_CALL"]; // types that may get an Agora token
const MAX_RECHARGE_PAISE = 500000; // ₹5,000 cap per recharge
const REFERRAL_BONUS_PAISE = 10000; // ₹100 referral bonus for referee and referrer (on first paid recharge)
const SETTLE_GRACE_MS = 2 * 60 * 1000; // sweeper settles sessions 2 min after they expire

function sadhakPayoutFor(paidPaise) {
  return Math.floor((paidPaise * SADHAK_SHARE_PERCENT) / 100);
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------
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

function requireAdmin(request) {
  requireUid(request);
  if (request.auth.token.admin !== true) {
    throw new HttpsError("permission-denied", "Admin only.");
  }
}

function requireRechargePaise(value) {
  const paise = Number(value);
  if (!Number.isInteger(paise) || paise <= 0 || paise > MAX_RECHARGE_PAISE) {
    throw new HttpsError("invalid-argument", "Invalid amount.");
  }
  return paise;
}

function requireId(value, name) {
  const s = typeof value === "string" ? value.trim() : "";
  if (!s || s.length > 128 || s.includes("/")) {
    throw new HttpsError("invalid-argument", `Invalid ${name}.`);
  }
  return s;
}

function balancePaiseOf(snapshot) {
  const v = snapshot.exists ? snapshot.get("walletBalance") : 0;
  return Number.isInteger(v) ? v : 0;
}

function millisOf(ts) {
  if (!ts) return 0;
  if (typeof ts.toMillis === "function") return ts.toMillis();
  return Number(ts) || 0;
}

/** Deterministic Agora uid (1..2^31-1) from a Firebase uid. */
function agoraUidFor(firebaseUid) {
  const h = crypto.createHash("sha256").update(String(firebaseUid)).digest();
  const n = h.readUInt32BE(0) & 0x7fffffff;
  return n === 0 ? 1 : n;
}

function channelFor(sessionId) {
  return `devbhasha_${sessionId}`;
}

// ---------------------------------------------------------------------------
// Razorpay recharge
// ---------------------------------------------------------------------------

/**
 * Create a Razorpay order server-side so checkout can be tied to an order id.
 * Recharge amount is chosen by the user (it is their own money going in);
 * it is capped and must be an integer number of paise.
 */
exports.createRazorpayOrder = onCall({ secrets: RAZORPAY_SECRETS }, async (request) => {
  const uid = requireUid(request);
  const amountPaise = requireRechargePaise(request.data && request.data.amountPaise);

  const order = await razorpayClient().orders.create({
    amount: amountPaise,
    currency: "INR",
    receipt: `rcpt_${uid.slice(0, 20)}_${Date.now()}`,
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
 * Credits the amount stored on our own paymentOrders doc, never a client value.
 */
exports.verifyRazorpayAndCredit = onCall({ secrets: RAZORPAY_SECRETS }, async (request) => {
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
    expectedBuf.length === givenBuf.length && crypto.timingSafeEqual(expectedBuf, givenBuf);
  if (!signatureOk) {
    throw new HttpsError("permission-denied", "Invalid payment signature.");
  }

  // 2. The order must exist and belong to this user.
  const orderRef = db.collection("paymentOrders").doc(String(orderId));
  const orderSnap = await orderRef.get();
  if (!orderSnap.exists) {
    throw new HttpsError("not-found", "Unknown order.");
  }
  const order = orderSnap.data();
  if (order.uid !== uid) {
    throw new HttpsError("permission-denied", "Order does not belong to this user.");
  }
  if (!Number.isInteger(order.amountPaise) || order.amountPaise <= 0) {
    throw new HttpsError("failed-precondition", "Corrupt order amount.");
  }

  // 3. Confirm with Razorpay that the payment is captured and the amount matches.
  //    "authorized" is NOT accepted: an authorized payment can still be voided.
  const payment = await razorpayClient().payments.fetch(String(paymentId));
  if (payment.order_id !== orderId) {
    throw new HttpsError("permission-denied", "Payment/order mismatch.");
  }
  if (payment.status !== "captured") {
    throw new HttpsError("failed-precondition", `Payment not captured (${payment.status}).`);
  }
  if (Number(payment.amount) !== Number(order.amountPaise) || payment.currency !== "INR") {
    throw new HttpsError("failed-precondition", "Amount mismatch.");
  }

  // 4. Credit idempotently — a payment id is credited at most once.
  const userRef = db.collection("users").doc(uid);
  const payRef = db.collection("payments").doc(String(paymentId));
  return db.runTransaction(async (tx) => {
    // ---- all reads first (Firestore transactions require reads before writes)
    const [paySnap, userSnap] = await Promise.all([tx.get(payRef), tx.get(userRef)]);
    if (paySnap.exists) {
      return { alreadyProcessed: true, balancePaise: balancePaiseOf(userSnap) };
    }
    const referral = await readReferral(tx, uid, userSnap);

    // ---- writes
    tx.set(payRef, {
      uid,
      orderId,
      paymentId,
      amountPaise: order.amountPaise,
      status: "credited",
      createdAt: FieldValue.serverTimestamp(),
    });
    tx.set(userRef, { walletBalance: FieldValue.increment(order.amountPaise) }, { merge: true });
    tx.set(db.collection("walletLedger").doc(`${uid}_recharge_${paymentId}`), {
      uid,
      amountPaise: order.amountPaise,
      purpose: "recharge",
      ref: `recharge_${paymentId}`,
      createdAt: FieldValue.serverTimestamp(),
    });
    tx.set(orderRef, { status: "paid", paymentId }, { merge: true });
    const referralBonus = applyReferral(tx, uid, referral);

    return {
      alreadyProcessed: false,
      balancePaise: balancePaiseOf(userSnap) + order.amountPaise + referralBonus,
    };
  });
});

/** Read-only half of the referral reward (must run before any tx writes). */
async function readReferral(tx, uid, userSnap) {
  const userData = userSnap.exists ? userSnap.data() || {} : {};
  if (userData.referralBonusClaimed || !userData.referredBy) {
    return { eligible: false };
  }
  const code = String(userData.referredBy);
  let referrerRef = null;
  const q = await tx.get(db.collection("users").where("referralCode", "==", code).limit(1));
  if (!q.empty) {
    referrerRef = q.docs[0].ref;
  } else if (!code.includes("/")) {
    const direct = await tx.get(db.collection("users").doc(code));
    if (direct.exists) referrerRef = direct.ref;
  }
  if (referrerRef && referrerRef.id === uid) referrerRef = null; // no self-referral
  return { eligible: true, referrerRef };
}

/** Write half of the referral reward. Returns paise credited to `uid`. */
function applyReferral(tx, uid, referral) {
  if (!referral.eligible) return 0;
  const refereeRef = db.collection("users").doc(uid);
  if (!referral.referrerRef) {
    tx.set(refereeRef, { referralBonusClaimed: true }, { merge: true });
    return 0;
  }
  const referrerId = referral.referrerRef.id;
  tx.set(refereeRef, {
    walletBalance: FieldValue.increment(REFERRAL_BONUS_PAISE),
    referralBonusClaimed: true,
  }, { merge: true });
  tx.set(db.collection("walletLedger").doc(`${uid}_referral_reward`), {
    uid,
    amountPaise: REFERRAL_BONUS_PAISE,
    purpose: "referral_welcome_bonus",
    ref: "referral_reward",
    createdAt: FieldValue.serverTimestamp(),
  });
  tx.set(referral.referrerRef, {
    walletBalance: FieldValue.increment(REFERRAL_BONUS_PAISE),
    totalReferralEarnings: FieldValue.increment(REFERRAL_BONUS_PAISE),
  }, { merge: true });
  tx.set(db.collection("walletLedger").doc(`${referrerId}_referral_earned_${uid}`), {
    uid: referrerId,
    amountPaise: REFERRAL_BONUS_PAISE,
    purpose: "referral_earned",
    ref: `referral_earned_${uid}`,
    createdAt: FieldValue.serverTimestamp(),
  });
  return REFERRAL_BONUS_PAISE;
}

/**
 * DEPRECATED — the ₹100 signup bonus has been removed.
 * Kept as a harmless no-op so already-installed app builds that still call it
 * do not crash. It credits nothing.
 */
exports.claimSignupBonus = onCall(async (request) => {
  const uid = requireUid(request);
  const userSnap = await db.collection("users").doc(uid).get();
  return { alreadyClaimed: true, bonusDisabled: true, balancePaise: balancePaiseOf(userSnap) };
});

// ---------------------------------------------------------------------------
// Sessions: startSession / extendSession / getAgoraToken / endSession
//
// sessions/{sessionId} (server-only writes):
//   seekerId, providerId, participants[2], type, status 'active'|'ended',
//   isFreeTrial, startedAt, endsAt (Timestamp), durationMinutes,
//   amountPaise (total charged incl. extensions), extensionCount,
//   channel, chatId (== sessionId), settled, settleAfter (only while unsettled)
// chats/{sessionId} is created alongside for participant-scoped chat.
// ---------------------------------------------------------------------------

async function loadProvider(tx, providerId) {
  const [p, s] = await Promise.all([
    tx.get(db.collection("providers").doc(providerId)),
    tx.get(db.collection("sadhaks").doc(providerId)),
  ]);
  const doc = p.exists ? p : s.exists ? s : null;
  if (!doc) return null;
  const d = doc.data() || {};
  if (d.blocked === true || d.disabled === true) return null;
  return d;
}

exports.startSession = onCall(async (request) => {
  const uid = requireUid(request);
  const data = request.data || {};
  const type = String(data.type || "");
  if (!SESSION_TYPES.includes(type)) {
    throw new HttpsError("invalid-argument", "type must be SESSION, DREAM_CHAT or DREAM_CALL.");
  }
  const providerId = requireId(data.providerId, "providerId");
  if (providerId === uid) {
    throw new HttpsError("invalid-argument", "Cannot start a session with yourself.");
  }
  // Verified phone number from the ID token (Firebase phone auth). Never from the client payload.
  const phone = typeof request.auth.token.phone_number === "string" ? request.auth.token.phone_number : "";

  const sessionRef = db.collection("sessions").doc();
  const sessionId = sessionRef.id;
  const userRef = db.collection("users").doc(uid);
  const trialRef = phone ? db.collection("freeTrialClaims").doc(phone) : null;

  return db.runTransaction(async (tx) => {
    // ---- reads
    const [userSnap, trialSnap, provider] = await Promise.all([
      tx.get(userRef),
      trialRef ? tx.get(trialRef) : Promise.resolve(null),
      loadProvider(tx, providerId),
    ]);
    if (!provider) {
      throw new HttpsError("not-found", "Sadhak not found or unavailable.");
    }

    const isFreeTrial = type === FREE_TRIAL.type && !!trialRef && !trialSnap.exists;
    const price = isFreeTrial
      ? { amountPaise: FREE_TRIAL.amountPaise, durationMinutes: FREE_TRIAL.durationMinutes }
      : PRICE_TABLE[type];
    const balance = balancePaiseOf(userSnap);
    if (!isFreeTrial && balance < price.amountPaise) {
      throw new HttpsError("failed-precondition", "Insufficient balance.", {
        requiredPaise: price.amountPaise,
        balancePaise: balance,
      });
    }

    const nowMs = Date.now();
    const endsAt = Timestamp.fromMillis(nowMs + price.durationMinutes * 60 * 1000);

    // ---- writes
    if (isFreeTrial) {
      tx.create(trialRef, {
        phoneNumber: phone,
        uid,
        sessionId,
        providerId,
        claimedAt: FieldValue.serverTimestamp(),
      });
    } else {
      tx.set(userRef, { walletBalance: FieldValue.increment(-price.amountPaise) }, { merge: true });
      tx.set(db.collection("walletLedger").doc(`${uid}_session_${sessionId}_start`), {
        uid,
        amountPaise: -price.amountPaise,
        purpose: `session_${type.toLowerCase()}`,
        ref: `session_${sessionId}_start`,
        sessionId,
        providerId,
        createdAt: FieldValue.serverTimestamp(),
      });
    }

    tx.set(sessionRef, {
      sessionId,
      seekerId: uid,
      providerId,
      participants: [uid, providerId],
      type,
      status: "active",
      isFreeTrial,
      startedAt: FieldValue.serverTimestamp(),
      endsAt,
      durationMinutes: price.durationMinutes,
      amountPaise: price.amountPaise,
      extensionCount: 0,
      channel: channelFor(sessionId),
      chatId: sessionId,
      settled: false,
      settleAfter: Timestamp.fromMillis(endsAt.toMillis() + SETTLE_GRACE_MS),
    });
    tx.set(db.collection("chats").doc(sessionId), {
      chatId: sessionId,
      sessionId,
      seekerId: uid,
      providerId,
      participants: [uid, providerId],
      type,
      createdAt: FieldValue.serverTimestamp(),
    });

    return {
      sessionId,
      endsAt: endsAt.toMillis(), // epoch millis
      amountPaise: price.amountPaise,
      isFreeTrial,
    };
  });
});

exports.extendSession = onCall(async (request) => {
  const uid = requireUid(request);
  const data = request.data || {};
  const sessionId = requireId(data.sessionId, "sessionId");
  // Optional client idempotency key so a network retry is not charged twice.
  const idemKey = data.idempotencyKey ? requireId(String(data.idempotencyKey), "idempotencyKey") : null;
  const price = PRICE_TABLE.EXTENSION;
  const sessionRef = db.collection("sessions").doc(sessionId);
  const userRef = db.collection("users").doc(uid);

  return db.runTransaction(async (tx) => {
    const sessionSnap = await tx.get(sessionRef);
    if (!sessionSnap.exists) throw new HttpsError("not-found", "Session not found.");
    const s = sessionSnap.data();
    if (s.seekerId !== uid) throw new HttpsError("permission-denied", "Only the seeker can extend.");

    const ledgerId = idemKey
      ? `${uid}_session_${sessionId}_ext_${idemKey}`
      : `${uid}_session_${sessionId}_ext_${(s.extensionCount || 0) + 1}`;
    const ledgerRef = db.collection("walletLedger").doc(ledgerId);
    const [userSnap, ledgerSnap] = await Promise.all([tx.get(userRef), tx.get(ledgerRef)]);
    if (idemKey && ledgerSnap.exists) {
      return { endsAt: millisOf(s.endsAt), amountPaise: price.amountPaise, alreadyProcessed: true };
    }

    const nowMs = Date.now();
    if (s.status !== "active" || millisOf(s.endsAt) <= nowMs) {
      throw new HttpsError("failed-precondition", "Session is not active.");
    }
    const balance = balancePaiseOf(userSnap);
    if (balance < price.amountPaise) {
      throw new HttpsError("failed-precondition", "Insufficient balance.", {
        requiredPaise: price.amountPaise,
        balancePaise: balance,
      });
    }

    const newEndsAt = Timestamp.fromMillis(millisOf(s.endsAt) + price.durationMinutes * 60 * 1000);
    tx.set(userRef, { walletBalance: FieldValue.increment(-price.amountPaise) }, { merge: true });
    tx.set(ledgerRef, {
      uid,
      amountPaise: -price.amountPaise,
      purpose: "session_extension",
      ref: ledgerId.slice(uid.length + 1),
      sessionId,
      providerId: s.providerId,
      createdAt: FieldValue.serverTimestamp(),
    });
    tx.update(sessionRef, {
      endsAt: newEndsAt,
      durationMinutes: FieldValue.increment(price.durationMinutes),
      amountPaise: FieldValue.increment(price.amountPaise),
      extensionCount: FieldValue.increment(1),
      settleAfter: Timestamp.fromMillis(newEndsAt.toMillis() + SETTLE_GRACE_MS),
    });

    return { endsAt: newEndsAt.toMillis(), amountPaise: price.amountPaise };
  });
});

exports.getAgoraToken = onCall({ secrets: [AGORA_APP_CERTIFICATE] }, async (request) => {
  const uid = requireUid(request);
  const sessionId = requireId((request.data || {}).sessionId, "sessionId");
  const snap = await db.collection("sessions").doc(sessionId).get();
  if (!snap.exists) throw new HttpsError("not-found", "Session not found.");
  const s = snap.data();
  if (s.seekerId !== uid && s.providerId !== uid) {
    throw new HttpsError("permission-denied", "Not a participant of this session.");
  }
  if (!CALL_TYPES.includes(s.type)) {
    throw new HttpsError("failed-precondition", "This session type has no call.");
  }
  const nowMs = Date.now();
  const endsAtMs = millisOf(s.endsAt);
  if (s.status !== "active" || endsAtMs <= nowMs) {
    throw new HttpsError("failed-precondition", "Session is not active.");
  }

  const appId = AGORA_APP_ID.value();
  const cert = AGORA_APP_CERTIFICATE.value();
  if (!appId || !cert) {
    throw new HttpsError("internal", "Calling is not configured.");
  }
  const channel = s.channel || channelFor(sessionId);
  const agoraUid = agoraUidFor(uid);
  // Token valid only until the paid time runs out (min 30 s). After an
  // extension the app should call getAgoraToken again and renew.
  const ttl = Math.max(30, Math.ceil((endsAtMs - nowMs) / 1000));
  const token = RtcTokenBuilder.buildTokenWithUid(
    appId, cert, channel, agoraUid, RtcRole.PUBLISHER, ttl, ttl
  );
  return { token, channel, uid: agoraUid, appId, expiresAt: nowMs + ttl * 1000 };
});

exports.endSession = onCall(async (request) => {
  const uid = requireUid(request);
  const sessionId = requireId((request.data || {}).sessionId, "sessionId");
  const ref = db.collection("sessions").doc(sessionId);
  const snap = await ref.get();
  if (!snap.exists) throw new HttpsError("not-found", "Session not found.");
  const s = snap.data();
  const isAdmin = request.auth.token.admin === true;
  if (s.seekerId !== uid && s.providerId !== uid && !isAdmin) {
    throw new HttpsError("permission-denied", "Not a participant of this session.");
  }
  const result = await settleSession(sessionId, uid);
  return { sessionId, status: "ended", ...result };
});

/**
 * End (if still active) and settle a session, idempotently, in one transaction.
 * Sadhak payout = floor(65% of amountPaise) for paid sessions, fixed 3000 paise
 * for the free trial. The seeker was already charged at start/extend.
 */
async function settleSession(sessionId, endedBy) {
  const sessionRef = db.collection("sessions").doc(sessionId);
  return db.runTransaction(async (tx) => {
    const snap = await tx.get(sessionRef);
    if (!snap.exists) return { skipped: true };
    const s = snap.data();
    if (s.settled) {
      return { settled: true, alreadySettled: true, sadhakPayoutPaise: s.sadhakPayoutPaise || 0 };
    }
    const providerId = s.providerId;
    const providerTxRef = db.collection("providers").doc(providerId)
      .collection("transactions").doc(`session_${sessionId}`);
    const providerWalletRef = db.collection("providers").doc(providerId)
      .collection("wallet").doc("summary");
    const providerTxSnap = await tx.get(providerTxRef);

    const amountPaise = Number.isInteger(s.amountPaise) ? s.amountPaise : 0;
    const sadhakPayoutPaise = s.isFreeTrial
      ? FREE_TRIAL.sadhakPayoutPaise + sadhakPayoutFor(amountPaise) // amountPaise>0 only if trial was extended
      : sadhakPayoutFor(amountPaise);
    const platformSharePaise = amountPaise - sadhakPayoutPaise; // negative for an unextended free trial

    const nowMs = Date.now();
    const update = {
      settled: true,
      settledAt: FieldValue.serverTimestamp(),
      settleAfter: FieldValue.delete(),
      sadhakPayoutPaise,
      platformSharePaise,
    };
    if (s.status !== "ended") {
      update.status = "ended";
      update.endedAt = FieldValue.serverTimestamp();
      update.endedBy = endedBy || "system";
      // If ended early, endsAt is pulled in so tokens/rules stop honouring it.
      if (millisOf(s.endsAt) > nowMs) update.endsAt = Timestamp.fromMillis(nowMs);
    }

    if (!providerTxSnap.exists && sadhakPayoutPaise > 0) {
      tx.set(providerTxRef, {
        id: `session_${sessionId}`,
        type: "session_earning",
        sessionType: s.type,
        isFreeTrial: !!s.isFreeTrial,
        amountPaise: sadhakPayoutPaise,
        grossPaise: amountPaise,
        sessionId,
        seekerId: s.seekerId,
        timestamp: FieldValue.serverTimestamp(),
        status: "completed",
      });
      tx.set(providerWalletRef, {
        balance: FieldValue.increment(sadhakPayoutPaise),
        totalEarnings: FieldValue.increment(sadhakPayoutPaise),
        updatedAt: FieldValue.serverTimestamp(),
      }, { merge: true });
    }
    tx.update(sessionRef, update);
    return { settled: true, sadhakPayoutPaise };
  });
}

/** Settles sessions whose time ran out without anyone calling endSession. */
exports.settleExpiredSessions = onSchedule("every 10 minutes", async () => {
  const due = await db.collection("sessions")
    .where("settleAfter", "<=", Timestamp.now())
    .orderBy("settleAfter")
    .limit(200)
    .get();
  for (const doc of due.docs) {
    try {
      await settleSession(doc.id, "system");
    } catch (e) {
      console.error(`[settleExpiredSessions] ${doc.id}`, e);
    }
  }
});

// ---------------------------------------------------------------------------
// Text questions (legacy flow) — server-priced, balance-checked
// ---------------------------------------------------------------------------
async function executeSettleQuestion(questionId) {
  const questionRef = db.collection("questions").doc(questionId);

  return db.runTransaction(async (tx) => {
    const questionSnap = await tx.get(questionRef);
    if (!questionSnap.exists) return { skipped: true, reason: "not_found" };
    const qData = questionSnap.data() || {};
    if (qData.settled) return { alreadySettled: true, earningAmount: qData.earningAmount };
    if (qData.status !== "answered") return { skipped: true, reason: "not_answered" };

    const userId = qData.userId;
    const providerId = qData.providerId;
    if (!userId || !providerId || userId === providerId) {
      return { skipped: true, reason: "missing_participants" };
    }

    const chargePaise = QUESTION_PRICE_PAISE; // never read from the document
    const payoutPaise = sadhakPayoutFor(chargePaise);

    const userRef = db.collection("users").doc(userId);
    const ledgerRef = db.collection("walletLedger").doc(`${userId}_question_${questionId}`);
    const providerTxRef = db.collection("providers").doc(providerId)
      .collection("transactions").doc(`question_${questionId}`);
    const providerWalletRef = db.collection("providers").doc(providerId)
      .collection("wallet").doc("summary");

    const [userSnap, ledgerSnap, providerTxSnap] = await Promise.all([
      tx.get(userRef), tx.get(ledgerRef), tx.get(providerTxRef),
    ]);
    if (ledgerSnap.exists || providerTxSnap.exists) {
      return { alreadySettled: true, earningAmount: payoutPaise };
    }
    if (balancePaiseOf(userSnap) < chargePaise) {
      tx.update(questionRef, { settlementStatus: "insufficient_balance" });
      return { skipped: true, reason: "insufficient_balance" };
    }

    tx.set(userRef, { walletBalance: FieldValue.increment(-chargePaise) }, { merge: true });
    tx.set(ledgerRef, {
      uid: userId,
      amountPaise: -chargePaise,
      purpose: "question_charge",
      ref: `question_${questionId}`,
      providerId,
      questionId,
      createdAt: FieldValue.serverTimestamp(),
    });
    tx.set(providerTxRef, {
      id: `question_${questionId}`,
      type: "question_earning",
      amountPaise: payoutPaise,
      grossPaise: chargePaise,
      questionId,
      userId,
      userName: qData.userName || "",
      timestamp: FieldValue.serverTimestamp(),
      status: "completed",
    });
    tx.set(providerWalletRef, {
      balance: FieldValue.increment(payoutPaise),
      totalEarnings: FieldValue.increment(payoutPaise),
      updatedAt: FieldValue.serverTimestamp(),
    }, { merge: true });
    tx.update(questionRef, {
      earningAmount: payoutPaise,
      chargedPaise: chargePaise,
      settled: true,
      settlementStatus: "settled",
      settledAt: FieldValue.serverTimestamp(),
    });

    return { success: true, questionId, earningAmount: payoutPaise, chargedPaise: chargePaise };
  });
}

exports.settleQuestion = onDocumentUpdated(
  { document: "questions/{questionId}", ...FIRESTORE_TRIGGER_OPTS },
  async (event) => {
    const before = event.data?.before?.data() || {};
    const after = event.data?.after?.data() || {};
    if (after.status !== "answered" || before.status === "answered") return null;
    return executeSettleQuestion(event.params.questionId);
  }
);

// Admin-only manual retry (was callable by anyone for any question).
exports.settleQuestionCallable = onCall(async (request) => {
  requireAdmin(request);
  const questionId = requireId((request.data || {}).questionId, "questionId");
  return executeSettleQuestion(questionId);
});

// Admin-only manual settle of a session (e.g. ops fixing a stuck one).
exports.adminSettleSession = onCall(async (request) => {
  requireAdmin(request);
  const sessionId = requireId((request.data || {}).sessionId, "sessionId");
  return settleSession(sessionId, `admin:${request.auth.uid}`);
});

// ---------------------------------------------------------------------------
// Website (devbhasha.com) <-> Dev Panel bridge
// ---------------------------------------------------------------------------
const { onRequest } = require("firebase-functions/v2/https");

const WEBSITE_ORIGINS = new Set([
  "https://devbhasha.com",
  "https://www.devbhasha.com",
]);

function applyCors(req, res) {
  const origin = req.get("origin") || "";
  if (WEBSITE_ORIGINS.has(origin) || /^https:\/\/[a-z0-9-]+\.vercel\.app$/.test(origin)) {
    res.set("Access-Control-Allow-Origin", origin);
    res.set("Vary", "Origin");
  }
  res.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  res.set("Access-Control-Allow-Headers", "Content-Type");
  res.set("Access-Control-Max-Age", "3600");
  if (req.method === "OPTIONS") {
    res.status(204).send("");
    return true;
  }
  return false;
}

function pickStr(d, keys, max = 300) {
  for (const k of keys) {
    const v = d[k];
    if (typeof v === "string" && v.trim()) return v.trim().slice(0, max);
    if (Array.isArray(v) && v.length) return v.filter((x) => typeof x === "string").join(", ").slice(0, max);
  }
  return "";
}

function isApprovedProvider(d) {
  if (d.blocked === true || d.disabled === true) return false;
  const st = String(d.verificationStatus || d.status || "").toLowerCase();
  return d.verified === true || d.approved === true || st === "approved" || st === "verified";
}

/**
 * GET https://<region>-devbhasha-d9e22.cloudfunctions.net/publicSadhaks
 * Public list of approved sadhaks (public profile fields only, never phone,
 * email, wallet, KYC or earnings) plus the server price table, for the website.
 */
exports.publicSadhaks = onRequest({ cors: false, maxInstances: 5, invoker: "public" }, async (req, res) => {
  if (applyCors(req, res)) return;
  if (req.method !== "GET") { res.status(405).json({ error: "method" }); return; }
  try {
    const snap = await db.collection("providers").limit(200).get();
    const sadhaks = [];
    snap.forEach((doc) => {
      const d = doc.data() || {};
      if (!isApprovedProvider(d)) return;
      const ratingSum = Number(d.ratingSum), ratingCount = Number(d.ratingCount);
      let rating = Number(d.rating);
      if (!Number.isFinite(rating) && ratingCount > 0) rating = ratingSum / ratingCount;
      sadhaks.push({
        id: doc.id,
        name: pickStr(d, ["displayName", "name", "fullName", "nameHi", "nameEn"], 80),
        title: pickStr(d, ["title", "titleHi", "expertise", "specialization", "skills", "category"], 120),
        experience: pickStr(d, ["experience", "experienceYears", "experienceHi"], 40) ||
          (Number.isFinite(Number(d.experienceYears)) ? `${Number(d.experienceYears)}+ वर्ष` : ""),
        languages: pickStr(d, ["languages", "language"], 80),
        bio: pickStr(d, ["bio", "about", "description"], 300),
        photoUrl: pickStr(d, ["photoUrl", "profileImage", "imageUrl", "avatarUrl", "photoURL"], 500),
        rating: Number.isFinite(rating) ? Math.round(rating * 10) / 10 : null,
        isOnline: d.isOnline === true || d.online === true,
      });
    });
    sadhaks.sort((a, b) => Number(b.isOnline) - Number(a.isOnline) || (b.rating || 0) - (a.rating || 0));
    res.set("Cache-Control", "public, max-age=60, s-maxage=120");
    res.json({
      sadhaks,
      prices: {
        session: { rupees: PRICE_TABLE.SESSION.amountPaise / 100, minutes: PRICE_TABLE.SESSION.durationMinutes },
        extension: { rupees: PRICE_TABLE.EXTENSION.amountPaise / 100, minutes: PRICE_TABLE.EXTENSION.durationMinutes },
        dreamChat: { rupees: PRICE_TABLE.DREAM_CHAT.amountPaise / 100, minutes: PRICE_TABLE.DREAM_CHAT.durationMinutes },
        dreamCall: { rupees: PRICE_TABLE.DREAM_CALL.amountPaise / 100, minutes: PRICE_TABLE.DREAM_CALL.durationMinutes },
      },
      updatedAt: new Date().toISOString(),
    });
  } catch (e) {
    console.error("publicSadhaks", e);
    res.status(500).json({ error: "internal" });
  }
});

/**
 * POST https://<region>-devbhasha-d9e22.cloudfunctions.net/websiteEnquiry
 * Body JSON: { name, phone, email?, message, sadhakId?, topic?, page?, website? (honeypot) }
 * Stores in /websiteEnquiries for the Dev Panel. Throttled per IP and phone.
 */
exports.websiteEnquiry = onRequest({ cors: false, maxInstances: 5, invoker: "public" }, async (req, res) => {
  if (applyCors(req, res)) return;
  if (req.method !== "POST") { res.status(405).json({ error: "method" }); return; }
  try {
    const b = typeof req.body === "object" && req.body ? req.body : {};
    if (typeof b.website === "string" && b.website.trim()) { res.json({ ok: true }); return; } // bot
    const s = (v, max) => (typeof v === "string" ? v.trim().slice(0, max) : "");
    const name = s(b.name, 80);
    const phoneDigits = s(b.phone, 20).replace(/\D/g, "");
    const phone = phoneDigits.length === 12 && phoneDigits.startsWith("91") ? phoneDigits.slice(2) : phoneDigits;
    const email = s(b.email, 120);
    const message = s(b.message, 1500);
    if (name.length < 2) { res.status(400).json({ error: "name" }); return; }
    if (!/^[6-9]\d{9}$/.test(phone)) { res.status(400).json({ error: "phone" }); return; }
    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { res.status(400).json({ error: "email" }); return; }
    if (message.length < 2) { res.status(400).json({ error: "message" }); return; }

    const ip = String(req.get("x-forwarded-for") || req.ip || "").split(",")[0].trim();
    const ipHash = crypto.createHash("sha256").update(ip).digest("hex").slice(0, 32);
    const sinceMs = Date.now() - 60 * 60 * 1000;
    const recent = await db.collection("websiteEnquiries").where("ipHash", "==", ipHash).limit(50).get();
    let lastHour = 0;
    recent.forEach((d) => { const t = d.get("createdAt"); if (t && t.toMillis && t.toMillis() >= sinceMs) lastHour++; });
    if (lastHour >= 5) { res.status(429).json({ error: "rate" }); return; }

    const ref = await db.collection("websiteEnquiries").add({
      name, phone, email, message,
      sadhakId: s(b.sadhakId, 128),
      sadhakName: s(b.sadhakName, 80),
      topic: s(b.topic, 60),
      page: s(b.page, 200),
      source: "website",
      status: "new",
      ipHash,
      userAgent: s(req.get("user-agent"), 200),
      createdAt: FieldValue.serverTimestamp(),
    });
    res.json({ ok: true, id: ref.id });
  } catch (e) {
    console.error("websiteEnquiry", e);
    res.status(500).json({ error: "internal" });
  }
});
