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

const { onCall: firebaseOnCall, onRequest, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentUpdated, onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { defineSecret, defineString } = require("firebase-functions/params");
const admin = require("firebase-admin");
const crypto = require("crypto");
const Razorpay = require("razorpay");
const { RtcTokenBuilder, RtcRole } = require("agora-token");

admin.initializeApp();
const { getFirestore } = require("firebase-admin/firestore");
const { getStorage } = require("firebase-admin/storage");
const PDFDocument = require("pdfkit");
const FIRESTORE_DB_ID = require('./backend-config').database;
const db = getFirestore(FIRESTORE_DB_ID);
// App Check is mandatory on all callable endpoints. Webhooks authenticate separately.
function onCall(options, handler) {
  if (typeof options === 'function') { handler=options; options={}; }
  return firebaseOnCall({...options, enforceAppCheck:true}, async request => {
    const uid=requireUid(request);
    await db.runTransaction(async tx => {
      const ref=db.doc(`callableRateLimits/${uid}`), profile=db.doc(`users/${uid}`), provider=db.doc(`providers/${uid}`);
      const [snap,u,p]=await Promise.all([tx.get(ref),tx.get(profile),tx.get(provider)]);
      if ((u.get('deletionPending') || p.get('deletionPending')) && handler !== deletion.request) throw new HttpsError('failed-precondition','Account deletion is in progress.');
      const now=Date.now(), previous=snap.data()||{}, same=now-(previous.windowStart||0)<60000;
      const count=same?(previous.count||0)+1:1;
      if(count>90) throw new HttpsError('resource-exhausted','Too many requests. Retry shortly.');
      tx.set(ref,{count,windowStart:same?previous.windowStart:now,expireAt:admin.firestore.Timestamp.fromMillis(now+86400000)});
    });
    return handler(request);
  });
}
const FieldValue = admin.firestore.FieldValue;
const Timestamp = admin.firestore.Timestamp;

const RAZORPAY_KEY_ID = defineSecret("RAZORPAY_KEY_ID");
const RAZORPAY_KEY_SECRET = defineSecret("RAZORPAY_KEY_SECRET");
const AGORA_APP_CERTIFICATE = defineSecret("AGORA_APP_CERTIFICATE");
const AGORA_APP_ID = defineString("AGORA_APP_ID");
const RAZORPAY_WEBHOOK_SECRET = defineSecret("RAZORPAY_WEBHOOK_SECRET");
const RAZORPAY_SECRETS = [RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET];

// Firestore triggers must point at the named database.
const FIRESTORE_TRIGGER_OPTS = { database: FIRESTORE_DB_ID };

async function getServerConfig() {
  const [privSnap, settingsSnap] = await Promise.all([
    db.collection("app_config").doc("private").get(),
    db.collection("app_config").doc("settings").get(),
  ]);
  const priv = privSnap.exists ? privSnap.data() : {};
  const settings = settingsSnap.exists ? settingsSnap.data() : {};
  return {
    sadhakSharePercent: 60,
    videoCallEnabled: false,
    agreementVersion: "2.0",
    ...settings,
    ...priv,
  };
}

async function assertSadhakAgreement(uid) {
  const config = await getServerConfig();
  const requiredVersion = config.agreementVersion || "2.0";

  const [sadhakSnap, provSnap] = await Promise.all([
    db.collection("sadhaks").doc(uid).get(),
    db.collection("providers").doc(uid).get(),
  ]);

  const profile = sadhakSnap.exists ? sadhakSnap.data() : (provSnap.exists ? provSnap.data() : {});
  if (profile.acceptedAgreementVersion !== requiredVersion) {
    throw new HttpsError("failed-precondition", "AGREEMENT_NOT_ACCEPTED");
  }
  return true;
}

// ---------------------------------------------------------------------------
// Single price table — integer paise, server-only. Change prices HERE only.
// ---------------------------------------------------------------------------
const PRICE_TABLE = Object.freeze({
  DREAM_CHAT: Object.freeze({ amountPaise: 19900, durationMinutes: 5 }),  // ₹199
  DREAM_CALL: Object.freeze({ amountPaise: 29900, durationMinutes: 5 }), // ₹299
});
// First dream chat is free for 5 minutes, once per verified phone number.
const FREE_TRIAL = Object.freeze({
  type: "DREAM_CHAT",
  amountPaise: 0,
  durationMinutes: 5,
  sadhakPayoutPaise: 5000, // ₹50 fixed payout to the sadhak, paid by Devbhasha
});
const SADHAK_SHARE_PERCENT = 60; // Devbhasha keeps the remaining 40%
// Answered text questions (legacy flow): fixed server price, same as the
// previous default in this file (₹51). The client can no longer set it.
const QUESTION_PRICE_PAISE = 5100;

const SESSION_TYPES = ["DREAM_CHAT", "DREAM_CALL", "CHAT_PER_MIN", "CALL_PER_MIN", "SESSION_30"];
const CALL_TYPES = ["DREAM_CALL", "CALL_PER_MIN", "SESSION_30"]; // types that may get an Agora token
const MAX_RECHARGE_PAISE = 500000; // ₹5,000 cap per recharge
const REFERRAL_BONUS_PAISE = 10000; // ₹100 referral bonus for referee and referrer (on first paid recharge)
const SETTLE_GRACE_MS = 2 * 60 * 1000; // sweeper settles sessions 2 min after they expire

async function sadhakPayoutFor(paidPaise) {
  const config = await getServerConfig();
  const share=config.sadhakSharePercent ?? SADHAK_SHARE_PERCENT;
  if(!Number.isInteger(share) || share<0 || share>100) throw new HttpsError('failed-precondition','Invalid revenue share configuration.');
  return Math.floor(paidPaise*share/100);
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
  if (!request.auth || !request.auth.uid || request.auth.token.firebase?.sign_in_provider === "anonymous") {
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

  return creditCapturedPayment(uid, String(orderId), String(paymentId));
});

async function creditCapturedPayment(uid, orderId, paymentId) {
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
    const [paySnap, userSnap, currentOrder] = await Promise.all([tx.get(payRef), tx.get(userRef), tx.get(orderRef)]);
    if (paySnap.exists) {
      return { alreadyProcessed: true, balancePaise: balancePaiseOf(userSnap) };
    }
    if(currentOrder.get('status')==='paid') {
      if(currentOrder.get('paymentId')!==paymentId) throw new HttpsError('already-exists','Order already credited.');
      return {alreadyProcessed:true,balancePaise:balancePaiseOf(userSnap)};
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
}

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

const sessions = require('./sessions')({db,FieldValue,Timestamp,HttpsError,config:getServerConfig,id:requireId,uid:requireUid,channel:channelFor});
exports.startSession = onCall(sessions.start);
exports.confirmSessionConnection = onCall(sessions.confirm);

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
  if (!["pending", "accepted", "active"].includes(s.status) || s.settled || endsAtMs <= nowMs) {
    throw new HttpsError("failed-precondition", "Session is not active.");
  }

  const appId = AGORA_APP_ID.value();
  const cert = AGORA_APP_CERTIFICATE.value();
  if (!appId || !cert) {
    throw new HttpsError("internal", "Calling is not configured.");
  }
  const channel = s.channel || channelFor(sessionId);
  const agoraUid = agoraUidFor(uid);
  const ttl = Math.max(1, Math.min(120, Math.ceil((endsAtMs - nowMs) / 1000)));
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
 * Sadhak payout = floor(60% of what the user actually paid) for paid sessions,
 * fixed 5000 paise for the free trial.
 */
async function settleSession(sessionId, endedBy) {
  const snap=await db.doc(`sessions/${sessionId}`).get();
  if (Number.isSafeInteger(snap.get('reservedPaise'))) return sessions.settle(sessionId,endedBy);
  return settleLegacySession(sessionId,endedBy);
}
async function settleLegacySession(sessionId, endedBy) {
  const sessionRef = db.collection("sessions").doc(sessionId);
  return db.runTransaction(async (tx) => {
    const snap = await tx.get(sessionRef);
    if (!snap.exists) return { skipped: true };
    const s = snap.data();
    if (s.settled) {
      return { settled: true, alreadySettled: true, sadhakPayoutPaise: s.sadhakPayoutPaise || 0 };
    }
    const providerId = s.providerId;
    const seekerId = s.seekerId;
    const isPerMinute = s.type === "CHAT_PER_MIN" || s.type === "CALL_PER_MIN";

    const providerTxRef = db.collection("providers").doc(providerId)
      .collection("transactions").doc(`session_${sessionId}`);
    const providerWalletRef = db.collection("providers").doc(providerId)
      .collection("wallet").doc("summary");
    const userRef = db.collection("users").doc(seekerId);

    const [providerTxSnap, userSnap] = await Promise.all([
      tx.get(providerTxRef),
      isPerMinute ? tx.get(userRef) : Promise.resolve(null),
    ]);

    const nowMs = Date.now();
    let amountPaise = Number.isInteger(s.amountPaise) ? s.amountPaise : 0;
    let actualMinutes = s.durationMinutes || 0;

    if (isPerMinute) {
      const startMs = millisOf(s.startedAt) || (s.endsAt ? millisOf(s.endsAt) - (s.durationMinutes || 1) * 60 * 1000 : nowMs);
      const endsAtMs = millisOf(s.endsAt);
      const endLimitMs = endsAtMs > 0 ? Math.min(nowMs, endsAtMs) : nowMs;
      const elapsedMs = Math.max(0, endLimitMs - startMs);
      // Bill on actual minutes used, rounded up to the next full minute
      actualMinutes = Math.max(1, Math.ceil(elapsedMs / (60 * 1000)));

      const pricePerMin = Number.isInteger(s.pricePerMinPaise) ? s.pricePerMinPaise : 0;
      const calculatedCharge = actualMinutes * pricePerMin;
      const currentBalance = userSnap ? balancePaiseOf(userSnap) : 0;
      // Never deduct more than the user's wallet balance
      amountPaise = Math.min(calculatedCharge, Math.max(0, currentBalance));

      if (amountPaise > 0) {
        tx.set(userRef, { walletBalance: FieldValue.increment(-amountPaise) }, { merge: true });
        tx.set(db.collection("walletLedger").doc(`${seekerId}_session_${sessionId}_end`), {
          uid: seekerId,
          amountPaise: -amountPaise,
          purpose: `session_${s.type.toLowerCase()}`,
          ref: `session_${sessionId}_end`,
          sessionId,
          providerId,
          minutesBilled: actualMinutes,
          createdAt: FieldValue.serverTimestamp(),
        });
      }
    }

    const sadhakPayoutPaise = s.isFreeTrial
      ? FREE_TRIAL.sadhakPayoutPaise
      : await sadhakPayoutFor(amountPaise);
    const platformSharePaise = amountPaise - sadhakPayoutPaise;

    const expireAt = Timestamp.fromMillis(nowMs + 365 * 24 * 60 * 60 * 1000);
    const update = {
      settled: true,
      settledAt: FieldValue.serverTimestamp(),
      settleAfter: FieldValue.delete(),
      expireAt,
      amountPaise,
      durationMinutes: actualMinutes,
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
    // Also set expireAt on the corresponding chat document
    tx.set(db.collection("chats").doc(sessionId), { expireAt }, { merge: true });
    return { settled: true, sadhakPayoutPaise, amountPaise, minutesBilled: actualMinutes };
  });
}

/** Settles sessions whose time ran out without anyone calling endSession. */
exports.settleExpiredSessions = onSchedule("every 1 minutes", async () => {
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
async function resolveFcmTokens(uid, isProvider = true) {
  const tokens = new Set();
  const paths = isProvider ? [
    db.collection("providers").doc(uid).collection("private").doc("device"),
    db.collection("providers").doc(uid),
    db.collection("sadhaks").doc(uid)
  ] : [
    db.collection("users").doc(uid)
  ];

  for (const ref of paths) {
    try {
      const snap = await ref.get();
      if (snap.exists) {
        const d = snap.data() || {};
        const t = d.fcmToken || d.fcmTokens;
        if (Array.isArray(t)) {
          t.forEach(x => { if (typeof x === "string" && x.trim()) tokens.add(x.trim()); });
        } else if (typeof t === "string" && t.trim()) {
          tokens.add(t.trim());
        }
      }
    } catch (e) {
      // Ignore read errors
    }
  }
  return Array.from(tokens);
}

async function sendPayoutNotification(providerId, title, body) {
  try {
    const tokens = await resolveFcmTokens(providerId, true);
    if (tokens.length === 0) return;

    const response = await admin.messaging().sendEachForMulticast({
      tokens,
      notification: { title, body },
      data: { type: "payout" }
    });
    response.responses.forEach((resp, idx) => {
      if (!resp.success) {
        const err = resp.error;
        if (err && (err.code === "messaging/registration-token-not-registered" || err.code === "messaging/invalid-registration-token")) {
          const invalidToken = tokens[idx];
          const removePayload = { fcmToken: FieldValue.arrayRemove(invalidToken), fcmTokens: FieldValue.arrayRemove(invalidToken) };
          db.collection("providers").doc(providerId).collection("private").doc("device").update(removePayload).catch(() => {});
          db.collection("providers").doc(providerId).update(removePayload).catch(() => {});
          db.collection("sadhaks").doc(providerId).update(removePayload).catch(() => {});
        }
      }
    });
  } catch (e) {
    console.error("Failed to send payout notification", e);
  }
}

async function executeSettleQuestion(questionId) {
  const questionRef = db.collection("questions").doc(questionId);
  const sessionRef = db.collection("sessions").doc(questionId);

  return db.runTransaction(async (tx) => {
    const [questionSnap, sessionSnap] = await Promise.all([
      tx.get(questionRef),
      tx.get(sessionRef),
    ]);
    if (!questionSnap.exists) return { skipped: true, reason: "not_found" };
    const qData = questionSnap.data() || {};
    
    // Check for dream session
    if (qData.type === "dream" || qData.kind === "dream" || qData.sessionId || sessionSnap.exists) {
        if (!qData.settled) {
          tx.update(questionRef, {
            settled: true,
            settledAt: FieldValue.serverTimestamp(),
            settlementStatus: "paid_via_session"
          });
        }
        return { skipped: true, reason: "paid_via_session" };
    }

    if (qData.settled) return { alreadySettled: true, earningAmount: qData.earningAmount };
    if (qData.status !== "answered") return { skipped: true, reason: "not_answered" };

    const userId = qData.userId;
    const providerId = qData.providerId;
    if (!userId || !providerId || userId === providerId) {
      return { skipped: true, reason: "missing_participants" };
    }

    const chargePaise = QUESTION_PRICE_PAISE; // never read from the document
    const payoutPaise = await sadhakPayoutFor(chargePaise);

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

    return { success: true, questionId, earningAmount: payoutPaise, chargedPaise: chargePaise, providerId };
  });
}

exports.settleQuestion = onDocumentUpdated(
  { document: "questions/{questionId}", ...FIRESTORE_TRIGGER_OPTS },
  async (event) => {
    const before = event.data?.before?.data() || {};
    const after = event.data?.after?.data() || {};
    if (after.status !== "answered" || before.status === "answered") return null;
    const result = await executeSettleQuestion(event.params.questionId);
    if (result.success) {
        await sendEarningNotification(result.providerId, result.earningAmount);
    }
    return null;
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
  const result = await settleSession(sessionId, `admin:${request.auth.uid}`);
  if (result.settled && !result.alreadySettled && result.sadhakPayoutPaise > 0) {
      // Look up providerId for notification
      const s = await db.collection("sessions").doc(sessionId).get();
      if (s.exists) await sendEarningNotification(s.data().providerId, result.sadhakPayoutPaise);
  }
  return { sessionId, status: "ended", ...result };
});

exports.onQuestionRejected = onDocumentUpdated(
  { document: "questions/{questionId}", ...FIRESTORE_TRIGGER_OPTS },
  async (event) => {
    const before = event.data?.before?.data() || {};
    const after = event.data?.after?.data() || {};
    if (after.status === "rejected" && before.status !== "rejected") {
        if(after.sessionId) { await settleSession(requireId(after.sessionId,'sessionId'),'question_rejected'); return null; }

        if (after.chargedPaise > 0) {
            const questionId = event.params.questionId;
            const userId = after.userId;
            const refundRef = db.collection("walletLedger").doc(`${userId}_question_${questionId}_refund`);
            
            await db.runTransaction(async (tx) => {
                const refundSnap = await tx.get(refundRef);
                if (refundSnap.exists) return; // Idempotent

                tx.set(refundRef, {
                    uid: userId,
                    amountPaise: after.chargedPaise,
                    purpose: "question_refund",
                    ref: `question_${questionId}_refund`,
                    createdAt: FieldValue.serverTimestamp(),
                });
                tx.update(db.collection("users").doc(userId), {
                    walletBalance: FieldValue.increment(after.chargedPaise)
                });
            });
        }
    }
    return null;
  }
);

// ---------------------------------------------------------------------------
// Payout System
// ---------------------------------------------------------------------------
const MIN_PAYOUT_PAISE = 10000;

exports.setPayoutBankAccount = onCall(async (request) => {
  const uid = requireUid(request);
  const data = request.data || {};
  const accountNumber = typeof data.accountNumber === "string" ? data.accountNumber.trim() : "";
  const ifsc = typeof data.ifsc === "string" ? data.ifsc.trim().toUpperCase() : "";
  const accountHolder = typeof data.accountHolder === "string" ? data.accountHolder.trim() : "";

  if (!/^\d{9,18}$/.test(accountNumber)) {
    throw new HttpsError("invalid-argument", "Invalid account number (must be 9 to 18 digits).");
  }
  if (!/^[A-Z]{4}0[A-Z0-9]{6}$/.test(ifsc)) {
    throw new HttpsError("invalid-argument", "Invalid IFSC format.");
  }
  if (!accountHolder) {
    throw new HttpsError("invalid-argument", "Account holder name cannot be blank.");
  }

  const bankDocRef = db.collection("payout_destinations").doc(uid).collection("bank").doc("current");
  await db.runTransaction(async tx => {
    const providerRef=db.doc(`providers/${uid}`), provider=await tx.get(providerRef);
    if(!provider.exists || provider.get('deletionPending')) throw new HttpsError('permission-denied','Provider unavailable.');
    if(provider.get('status')==='APPROVED') throw new HttpsError('failed-precondition','Bank account is locked after approval.');
    tx.update(providerRef,{bankDetailsUpdatedAt:FieldValue.serverTimestamp()});
    tx.set(bankDocRef,{
    accountNumber,
    ifsc,
    accountHolder,
    updatedAt: FieldValue.serverTimestamp(),
  });

  });
  return { success: true };
});

exports.requestPayout = onCall(async (request) => {
  const uid = requireUid(request);
  await assertSadhakAgreement(uid);
  const data = request.data || {};
  const config = await getServerConfig();
  const minPayout = (typeof config.minPayoutPaise === "number" && config.minPayoutPaise > 0)
    ? config.minPayoutPaise
    : MIN_PAYOUT_PAISE;

  const amountPaise = Number(data.amountPaise);
  if (!Number.isInteger(amountPaise) || amountPaise < minPayout) {
    throw new HttpsError("invalid-argument", `Minimum payout is ₹${(minPayout / 100).toFixed(0)}.`);
  }

  if (data.method !== "BANK") {
    throw new HttpsError("invalid-argument", "Only BANK payout method is supported.");
  }

  const idempotencyKey = typeof data.idempotencyKey === "string" ? data.idempotencyKey.trim() : "";
  if (!/^[A-Za-z0-9_-]{8,64}$/.test(idempotencyKey)) {
    throw new HttpsError("invalid-argument", "Invalid idempotencyKey.");
  }

  const provSnap = await db.collection("providers").doc(uid).get();
  if (!provSnap.exists) {
    throw new HttpsError("permission-denied", "Only providers can request payouts.");
  }
  const provData = provSnap.data() || {};
  if (provData.status !== "APPROVED" || provData.isVerified !== true) {
    throw new HttpsError("failed-precondition", "Provider must be APPROVED and verified to request payouts.");
  }

  // Read bank account from payout_destinations/{uid}/bank/current
  const bankSnap = await db.collection("payout_destinations").doc(uid).collection("bank").doc("current").get();
  if (!bankSnap.exists) {
    throw new HttpsError("failed-precondition", "Bank details not verified.");
  }
  const bankData = bankSnap.data() || {};
  if (!bankData.accountNumber || !bankData.ifsc || !bankData.accountHolder) {
    throw new HttpsError("failed-precondition", "Bank details not verified.");
  }

  const requestRef = db.collection("providers").doc(uid).collection("payout_requests").doc(idempotencyKey);
  const existingReqSnap = await requestRef.get();
  if (existingReqSnap.exists) {
    return {
      requestId: requestRef.id,
      status: existingReqSnap.data().status,
      idempotent: true,
    };
  }

  const maskedDestination = `XXXX${bankData.accountNumber.slice(-4)} • ${bankData.ifsc.substring(0, 4)}`;
  const fullDestination = {
    method: "BANK",
    accountNumber: bankData.accountNumber,
    ifsc: bankData.ifsc,
    accountHolder: bankData.accountHolder,
    createdAt: FieldValue.serverTimestamp(),
  };

  const walletRef = db.collection("providers").doc(uid).collection("wallet").doc("summary");
  const txRef = db.collection("providers").doc(uid).collection("transactions").doc(`payout_${requestRef.id}`);
  const destRef = db.collection("payout_destinations").doc(uid).collection("requests").doc(requestRef.id);

  return db.runTransaction(async (tx) => {
    const txReqSnap = await tx.get(requestRef);
    if (txReqSnap.exists) {
      return { requestId: requestRef.id, status: txReqSnap.data().status, idempotent: true };
    }

    const currentProvider = await tx.get(db.doc(`providers/${uid}`));
    if(currentProvider.get('deletionPending') || currentProvider.get('status')!=='APPROVED' || currentProvider.get('isVerified')!==true) throw new HttpsError('failed-precondition','Provider is unavailable.');
    const walletSnap = await tx.get(walletRef);
    const balance = walletSnap.exists ? walletSnap.data().balance || 0 : 0;
    if (balance < amountPaise) throw new HttpsError("failed-precondition", "Insufficient balance.");

    const pendingRequests = await tx.get(db.collection("providers").doc(uid).collection("payout_requests").where("status", "==", "pending"));
    if (!pendingRequests.empty) throw new HttpsError("failed-precondition", "Already have a pending payout request.");

    tx.update(walletRef, {
      balance: FieldValue.increment(-amountPaise),
      pendingPayout: FieldValue.increment(amountPaise),
      updatedAt: FieldValue.serverTimestamp(),
    });

    tx.set(requestRef, {
      amountPaise,
      method: "BANK",
      destination: maskedDestination,
      status: "pending",
      idempotencyKey,
      createdAt: FieldValue.serverTimestamp(),
    });

    tx.set(txRef, {
      type: "payout",
      amountPaise: -amountPaise,
      status: "pending",
      requestId: requestRef.id,
      timestamp: FieldValue.serverTimestamp(),
    });

    tx.set(destRef, fullDestination);

    return { requestId: requestRef.id, status: "pending", newBalancePaise: balance - amountPaise };
  });
});

exports.adminGetPayoutDestination = onCall(async (request) => {
  requireAdmin(request);
  const { providerId, requestId } = request.data;
  const auditRef = db.collection("payout_access_log").doc();
  await auditRef.set({ adminUid: request.auth.uid, providerId, requestId, at: FieldValue.serverTimestamp() });
  const snap = await db.collection("payout_destinations").doc(providerId).collection("requests").doc(requestId).get();
  if (!snap.exists) throw new HttpsError("not-found", "Destination not found.");
  return snap.data();
});

exports.adminProcessPayout = onCall(async (request) => {
  requireAdmin(request);
  const data = request.data || {};
  const { providerId, requestId, action, utr, reason } = data;
  const requestRef = db.collection("providers").doc(providerId).collection("payout_requests").doc(requestId);
  const walletRef = db.collection("providers").doc(providerId).collection("wallet").doc("summary");
  const txRef = db.collection("providers").doc(providerId).collection("transactions").doc(`payout_${requestId}`);
  const destRef = db.collection("payout_destinations").doc(providerId).collection("requests").doc(requestId);

  return db.runTransaction(async (tx) => {
    const requestSnap = await tx.get(requestRef);
    if (!requestSnap.exists) throw new HttpsError("not-found", "Request not found.");
    const reqData = requestSnap.data();
    if (reqData.status !== "pending") return { alreadyProcessed: true };

    const amountPaise = reqData.amountPaise;
    const adminUid = request.auth.uid;
    const purgeAfter = Timestamp.fromMillis(Date.now() + 30 * 24 * 60 * 60 * 1000);

    if (action === "paid") {
      if (!utr) throw new HttpsError("invalid-argument", "UTR required.");
      const config = await getServerConfig();
      const tdsPercent = typeof config.tdsPercent === "number" ? config.tdsPercent : 0;
      const platformFeePercent = typeof config.platformFeePercent === "number" ? config.platformFeePercent : 0;
      if(!Number.isFinite(tdsPercent) || !Number.isFinite(platformFeePercent) || tdsPercent<0 || platformFeePercent<0 || tdsPercent+platformFeePercent>100) throw new HttpsError('failed-precondition','Invalid payout deductions.');
      const grossPaise = amountPaise;
      const tdsPaise = Math.round((grossPaise * tdsPercent) / 100);
      const feePaise = Math.round((grossPaise * platformFeePercent) / 100);
      const netPaise = grossPaise - tdsPaise - feePaise;

      tx.update(requestRef, {
        status: "paid",
        utr,
        paidAt: FieldValue.serverTimestamp(),
        processedBy: adminUid,
        grossPaise,
        tdsPaise,
        feePaise,
        netPaise,
      });
      tx.update(walletRef, { pendingPayout: FieldValue.increment(-amountPaise), totalPaidOut: FieldValue.increment(amountPaise) });
      tx.update(txRef, { status: "completed", utr, timestamp: FieldValue.serverTimestamp() });
      tx.update(destRef, { purgeAfter });
      sendPayoutNotification(providerId, "Payout paid", `Payout of ₹${(netPaise/100).toFixed(2)} paid. UTR: ${utr}`).catch(() => {});
    } else if (action === "rejected") {
      if (!reason) throw new HttpsError("invalid-argument", "Reason required.");
      tx.update(requestRef, { status: "rejected", reason, processedAt: FieldValue.serverTimestamp(), processedBy: adminUid });
      tx.update(walletRef, { balance: FieldValue.increment(amountPaise), pendingPayout: FieldValue.increment(-amountPaise) });
      tx.update(txRef, { status: "rejected", reason, timestamp: FieldValue.serverTimestamp() });
      tx.update(destRef, { purgeAfter });
      sendPayoutNotification(providerId, "Payout rejected", `Payout rejected: ${reason}. Amount returned to your wallet.`).catch(() => {});
    } else {
      throw new HttpsError("invalid-argument", "Invalid action.");
    }
    return { success: true };
  });
});

exports.purgePayoutDestinations = onSchedule("every 24 hours", async () => {
    const now = Timestamp.now();
    const snapshot = await db.collectionGroup("requests")
        .where("purgeAfter", "<=", now)
        .get();
    for (const doc of snapshot.docs) {
        await doc.ref.delete();
    }
});


const deletion=require('./deletion')({db,admin,FieldValue,Timestamp,HttpsError,requireUid});
exports.secureRequestAccountDeletion=onCall(deletion.request);
exports.deleteProviderAccount=exports.secureRequestAccountDeletion;
exports.secureProcessAccountDeletion=onDocumentCreated({document:'accountDeletionJobs/{uid}',database:FIRESTORE_DB_ID,
  region:'us-central1',retry:true,timeoutSeconds:540,memory:'512MiB'},deletion.process);

// ---------------------------------------------------------------------------
// Admin Control Suite & Audit Logs
// ---------------------------------------------------------------------------
async function logAdminAudit(adminUid, action, details = {}) {
  try {
    await db.collection("adminAuditLogs").add({
      adminUid,
      action,
      details,
      timestamp: FieldValue.serverTimestamp(),
      createdAt: FieldValue.serverTimestamp(),
    });
  } catch (err) {
    console.error("Failed to write admin audit log:", err);
  }
}

/**
 * Admin: Set account status for seeker or sadhak (active or suspended).
 */
exports.adminSetAccountStatus = onCall(async (request) => {
  requireAdmin(request);
  const data = request.data || {};
  const userId = requireId(data.userId, "userId");
  const role = typeof data.role === "string" ? data.role.trim().toLowerCase() : "";
  if (role !== "seeker" && role !== "sadhak") {
    throw new HttpsError("invalid-argument", "Role must be 'seeker' or 'sadhak'.");
  }
  const status = typeof data.status === "string" ? data.status.trim().toLowerCase() : "";
  if (status !== "active" && status !== "suspended") {
    throw new HttpsError("invalid-argument", "Status must be 'active' or 'suspended'.");
  }

  const isSuspended = status === "suspended";
  const now = FieldValue.serverTimestamp();

  if (role === "seeker") {
    const userRef = db.collection("users").doc(userId);
    const userSnap = await userRef.get();
    if (!userSnap.exists) {
      throw new HttpsError("not-found", "User not found.");
    }
    await userRef.set({
      status,
      suspended: isSuspended,
      isSuspended,
      statusUpdatedAt: now,
      updatedAt: now,
    }, { merge: true });
  } else {
    const provRef = db.collection("providers").doc(userId);
    const sadhakRef = db.collection("sadhaks").doc(userId);
    const provSnap = await provRef.get();
    if (!provSnap.exists) {
      throw new HttpsError("not-found", "Provider not found.");
    }
    await provRef.set({
      accountStatus: status,
      suspended: isSuspended,
      isSuspended,
      statusUpdatedAt: now,
      updatedAt: now,
    }, { merge: true });

    const sadhakSnap = await sadhakRef.get();
    if (sadhakSnap.exists) {
      await sadhakRef.set({
        accountStatus: status,
        isAvailable: !isSuspended,
        statusUpdatedAt: now,
        updatedAt: now,
      }, { merge: true });
    }
  }

  await logAdminAudit(request.auth.uid, "SET_ACCOUNT_STATUS", {
    userId,
    role,
    status,
  });

  return { success: true, userId, role, status };
});

/**
 * Admin: Set custom user claims for role management.
 * Allowed roles are exactly "super_admin", "admin", "developer", "operator", "support" and "none".
 * For any of the first five, set the custom claims { admin: true, role }.
 * For "none", remove both admin and role.
 * Reject any other value.
 * An admin cannot change their own role.
 */
exports.adminSetRole = onCall(async (request) => {
  requireAdmin(request);
  const data = request.data || {};
  const uid = requireId(data.uid, "uid");
  const role = typeof data.role === "string" ? data.role.trim().toLowerCase() : "";

  const ALLOWED_ADMIN_ROLES = ["super_admin", "admin", "developer", "operator", "support", "none"];
  if (!ALLOWED_ADMIN_ROLES.includes(role)) {
    throw new HttpsError(
      "invalid-argument",
      `Invalid role. Allowed roles are: ${ALLOWED_ADMIN_ROLES.join(", ")}`
    );
  }

  if (request.auth.uid === uid) {
    throw new HttpsError("failed-precondition", "An admin cannot change their own role.");
  }

  const userRecord = await admin.auth().getUser(uid);
  const existingClaims = { ...(userRecord.customClaims || {}) };

  let newClaims = { ...existingClaims };
  if (role === "none") {
    delete newClaims.admin;
    delete newClaims.role;
  } else {
    newClaims.admin = true;
    newClaims.role = role;
  }

  await admin.auth().setCustomUserClaims(uid, newClaims);

  await logAdminAudit(request.auth.uid, "SET_ROLE", {
    uid,
    role,
    claims: newClaims,
  });

  return { success: true, uid, role, claims: newClaims };
});

/**
 * Admin: Instantly publish a verse.
 */
exports.adminPublishVerseNow = onCall(async (request) => {
  requireAdmin(request);
  const data = request.data || {};
  const verseId = requireId(data.verseId, "verseId");

  const verseRef = db.collection("verses").doc(verseId);
  const verseSnap = await verseRef.get();
  let verseData = null;
  if (verseSnap.exists) {
    verseData = verseSnap.data();
    await verseRef.set({
      isPublished: true,
      publishedAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    }, { merge: true });
  }

  await db.collection("app_content").doc("daily_verse").set({
    activeVerseId: verseId,
    publishedAt: FieldValue.serverTimestamp(),
    ...(verseData ? { verse: verseData } : {}),
  }, { merge: true });

  await logAdminAudit(request.auth.uid, "PUBLISH_VERSE_NOW", {
    verseId,
  });

  return { success: true, verseId };
});

/**
 * Admin: Refund to user wallet in a transaction with idempotency key.
 */
exports.adminRefundToWallet = onCall(async (request) => {
  requireAdmin(request);
  const data = request.data || {};
  const userId = requireId(data.userId, "userId");
  const amountPaise = Number(data.amountPaise);
  if (!Number.isInteger(amountPaise) || amountPaise <= 0) {
    throw new HttpsError("invalid-argument", "amountPaise must be a positive integer.");
  }
  const referenceId = requireId(data.referenceId, "referenceId");
  const referenceType = typeof data.referenceType === "string" ? data.referenceType : "admin_refund";
  const reason = typeof data.reason === "string" ? data.reason : "Admin refund";

  const userRef = db.collection("users").doc(userId);
  const refundDocId = `refund_${referenceId}`;
  const ledgerRef = db.collection("walletLedger").doc(refundDocId);

  const result = await db.runTransaction(async (tx) => {
    const ledgerSnap = await tx.get(ledgerRef);
    if (ledgerSnap.exists) {
      return { success: true, alreadyProcessed: true, refundId: refundDocId };
    }
    const userSnap = await tx.get(userRef);
    if (!userSnap.exists) {
      throw new HttpsError("not-found", "User not found.");
    }

    tx.set(userRef, { walletBalance: FieldValue.increment(amountPaise) }, { merge: true });
    tx.set(ledgerRef, {
      userId,
      amountPaise,
      type: "admin_refund",
      referenceId,
      referenceType,
      reason,
      refundedBy: request.auth.uid,
      createdAt: FieldValue.serverTimestamp(),
    });

    return { success: true, refundId: refundDocId, creditedPaise: amountPaise };
  });

  await logAdminAudit(request.auth.uid, "REFUND_TO_WALLET", {
    userId,
    amountPaise,
    referenceId,
    referenceType,
    reason,
  });

  return result;
});

/**
 * Admin: Review Sadhak application (APPROVED or REJECTED).
 * On approval, updates public sadhaks/{id} doc with public fields only (no phone, email, KYC).
 * Does NOT write any made-up default values.
 * rating starts at 0 and totalReviews at 0 unless real review data exists.
 * If name, at least one price, or languages are missing, refuse approval with failed-precondition "Profile incomplete: <missing fields>".
 * Decision is case-insensitive, stored in uppercase.
 */
exports.adminReviewSadhak = onCall(async (request) => {
  requireAdmin(request);
  const data = request.data || {};
  const providerId = requireId(data.providerId, "providerId");
  const decisionUpper = typeof data.decision === "string" ? data.decision.trim().toUpperCase() : "";
  if (decisionUpper !== "APPROVED" && decisionUpper !== "REJECTED") {
    throw new HttpsError("invalid-argument", "decision must be 'APPROVED' or 'REJECTED'.");
  }
  const reason = typeof data.reason === "string" ? data.reason.trim() : "";
  if (decisionUpper === "REJECTED" && !reason) {
    throw new HttpsError("invalid-argument", "Rejection reason is required.");
  }

  const provRef = db.collection("providers").doc(providerId);
  const sadhakRef = db.collection("sadhaks").doc(providerId);

  await db.runTransaction(async tx => {
  const provSnap = await tx.get(provRef);
  const requestId = data.requestId ? requireId(data.requestId,'requestId') : null;
  const queue = requestId ? await tx.get(db.doc(`verification_requests/${requestId}`)) : null;
  const sadhakSnap = await tx.get(sadhakRef);
  if(queue && (!queue.exists || queue.get('providerId') !== providerId)) throw new HttpsError('invalid-argument','Verification request does not match provider.');
  if(queue && queue.get('status') !== 'PENDING') throw new HttpsError('failed-precondition','Verification already reviewed.');
  if (!provSnap.exists) {
    throw new HttpsError("not-found", "Provider not found.");
  }
  const provData = provSnap.data() || {};

  const isVerified = decisionUpper === "APPROVED";
  const rejectionReason = decisionUpper === "REJECTED" ? reason : null;

  if (decisionUpper === "APPROVED") {
    const missing = [];
    const hasName = (typeof provData.name === "string" && provData.name.trim().length > 0) ||
                    (typeof provData.displayName === "string" && provData.displayName.trim().length > 0);
    if (!hasName) {
      missing.push("name");
    }

    const hasChatPrice = Number.isInteger(provData.chatPricePerMin) && provData.chatPricePerMin > 0;
    const hasCallPrice = Number.isInteger(provData.callPricePerMin) && provData.callPricePerMin > 0;
    const hasSessionPrice = Number.isInteger(provData.sessionPrice30Min) && provData.sessionPrice30Min > 0;
    if (!hasChatPrice && !hasCallPrice && !hasSessionPrice) {
      missing.push("at least one price");
    }

    const hasLanguages = (Array.isArray(provData.languages) && provData.languages.length > 0) ||
                         (typeof provData.languages === "string" && provData.languages.trim().length > 0);
    if (!hasLanguages) {
      missing.push("languages");
    }

    if (missing.length > 0) {
      throw new HttpsError("failed-precondition", `Profile incomplete: ${missing.join(", ")}`);
    }

    // Copy a field only if it really exists on providers/{id}.
    // rating starts at 0 and totalReviews at 0 unless real review data exists.
    const publicData = {
      id: providerId,
      status: "APPROVED",
      isVerified: true,
      updatedAt: FieldValue.serverTimestamp(),
      rating: typeof provData.rating === "number" ? provData.rating : 0,
      totalReviews: Number.isInteger(provData.totalReviews)
        ? provData.totalReviews
        : (Number.isInteger(provData.reviewsCount) ? provData.reviewsCount : 0),
    };

    if (typeof provData.name === "string" && provData.name.trim().length > 0) {
      publicData.name = provData.name.trim();
    }
    if (typeof provData.displayName === "string" && provData.displayName.trim().length > 0) {
      publicData.displayName = provData.displayName.trim();
    }
    if (!publicData.name && publicData.displayName) {
      publicData.name = publicData.displayName;
    }
    if (!publicData.displayName && publicData.name) {
      publicData.displayName = publicData.name;
    }

    if (typeof provData.bio === "string" && provData.bio.trim().length > 0) {
      publicData.bio = provData.bio.trim();
    }
    if (typeof provData.photo === "string" && provData.photo.trim().length > 0) {
      publicData.photo = provData.photo.trim();
    }
    if (typeof provData.avatarUrl === "string" && provData.avatarUrl.trim().length > 0) {
      publicData.avatarUrl = provData.avatarUrl.trim();
    }
    if (Array.isArray(provData.languages) && provData.languages.length > 0) {
      publicData.languages = provData.languages;
    }
    if (Array.isArray(provData.specialities) && provData.specialities.length > 0) {
      publicData.specialities = provData.specialities;
    }
    if (Array.isArray(provData.categories) && provData.categories.length > 0) {
      publicData.categories = provData.categories;
    }
    if (provData.experienceYears != null) {
      publicData.experienceYears = provData.experienceYears;
    }
    if (provData.chatPricePerMin != null) {
      publicData.chatPricePerMin = provData.chatPricePerMin;
    }
    if (provData.callPricePerMin != null) {
      publicData.callPricePerMin = provData.callPricePerMin;
    }
    if (provData.sessionPrice30Min != null) {
      publicData.sessionPrice30Min = provData.sessionPrice30Min;
    }
    if (typeof provData.isAvailable === "boolean") {
      publicData.isAvailable = provData.isAvailable;
    } else {
      publicData.isAvailable = true;
    }
    if (typeof provData.isOnline === "boolean") {
      publicData.isOnline = provData.isOnline;
    }

    tx.set(provRef,{
      status: "APPROVED",
      isVerified: true,
      rejectionReason: null,
      reviewedBy: request.auth.uid,
      reviewedAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    }, { merge: true });

    tx.set(sadhakRef,publicData, { merge: true });
  } else if (decisionUpper === "REJECTED") {
    tx.set(provRef,{
      status: "REJECTED",
      isVerified: false,
      rejectionReason,
      reviewedBy: request.auth.uid,
      reviewedAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    }, { merge: true });

    if (sadhakSnap.exists) {
      tx.set(sadhakRef,{
        status: "REJECTED",
        isVerified: false,
        isAvailable: false,
        updatedAt: FieldValue.serverTimestamp(),
      }, { merge: true });
    }
  }

  if(queue) tx.update(queue.ref,{status:decisionUpper==='APPROVED'?'VERIFIED':'REJECTED',reviewedBy:request.auth.uid,
    reviewedAt:FieldValue.serverTimestamp(),rejectionReason:reason});
  });

  await logAdminAudit(request.auth.uid, "REVIEW_SADHAK", {
    providerId,
    decision: decisionUpper,
    reason: rejectionReason,
  });

  return { success: true, providerId, decision: decisionUpper, isVerified };
});

// ---------------------------------------------------------------------------
/**
 * Admin: Send a push notification to a topic or specific user/sadhak.
 * Rate limit: 10 broadcasts (topics) per hour.
 */
exports.adminSendNotification = onCall(async (request) => {
  requireAdmin(request);
  const data = request.data || {};
  const title = String(data.title || "").trim();
  const body = String(data.body || "").trim();
  const imageUrl = String(data.imageUrl || "").trim();
  const link = String(data.link || "").trim();
  const audience = String(data.audience || "");
  const targetId = String(data.targetId || "").trim();

  if (!title || title.length > 60) throw new HttpsError("invalid-argument", "Title required (max 60).");
  if (!body || body.length > 200) throw new HttpsError("invalid-argument", "Body required (max 200).");
  if (!["all_users", "all_sadhaks", "user", "sadhak"].includes(audience)) {
    throw new HttpsError("invalid-argument", "Invalid audience.");
  }

  // Rate limit for broadcasts
  if (audience === "all_users" || audience === "all_sadhaks") {
    const oneHourAgo = new Date(Date.now() - 60 * 60 * 1000);
    const recentBroadcasts = await db.collection("notification_log")
      .where("sentAt", ">", oneHourAgo)
      .where("audience", "in", ["all_users", "all_sadhaks"])
      .count().get();
    
    if (recentBroadcasts.data().count >= 10) {
      throw new HttpsError("resource-exhausted", "Broadcast rate limit reached (10/hr).");
    }
  }

  const message = {
    notification: { title, body },
    data: { title, body }
  };
  if (imageUrl) {
    message.notification.imageUrl = imageUrl;
    message.data.imageUrl = imageUrl;
  }
  if (link) message.data.link = link;

  let successCount = 0;
  let failureCount = 0;

  try {
    if (audience === "all_users" || audience === "all_sadhaks") {
      message.topic = audience;
      await admin.messaging().send(message);
      successCount = 1; // Topic sends are counted as 1 broadcast
    } else {
      let tokens = [];
      if (audience === "sadhak") {
        tokens = await resolveFcmTokens(targetId, true);
      } else {
        tokens = await resolveFcmTokens(targetId, false);
      }

      if (tokens.length === 0) {
        failureCount = 1;
      } else {
        message.tokens = tokens;
        delete message.token;
        try {
          const response = await admin.messaging().sendEachForMulticast(message);
          let sent = 0;
          response.responses.forEach((resp, idx) => {
            if (resp.success) {
              sent++;
            } else {
              const err = resp.error;
              if (err && (err.code === "messaging/registration-token-not-registered" || err.code === "messaging/invalid-registration-token")) {
                const invalidToken = tokens[idx];
                const removePayload = { fcmToken: FieldValue.arrayRemove(invalidToken), fcmTokens: FieldValue.arrayRemove(invalidToken) };
                if (audience === "sadhak") {
                  db.collection("providers").doc(targetId).collection("private").doc("device").update(removePayload).catch(() => {});
                  db.collection("providers").doc(targetId).update(removePayload).catch(() => {});
                  db.collection("sadhaks").doc(targetId).update(removePayload).catch(() => {});
                } else {
                  db.collection("users").doc(targetId).update(removePayload).catch(() => {});
                }
              }
            }
          });
          successCount = sent > 0 ? 1 : 0;
          failureCount = sent > 0 ? 0 : 1;
        } catch (e) {
          failureCount = 1;
        }
      }
    }
  } catch (e) {
    console.error("FCM Send Error", e);
    failureCount = (audience === "all_users" || audience === "all_sadhaks") ? 1 : failureCount;
  }

  await db.collection("notification_log").add({
    adminUid: request.auth.uid,
    audience,
    targetId: targetId || null,
    title,
    body,
    sentAt: FieldValue.serverTimestamp(),
    successCount,
    failureCount,
  });

  return { success: successCount > 0, successCount, failureCount };
});

exports.acceptSadhakAgreement = onCall(async (request) => {
  const uid = requireUid(request);
  const data = request.data || {};
  const config = await getServerConfig();
  const requiredVersion = config.agreementVersion || "2.0";
  const version = (typeof data.version === "string" && data.version.trim())
    ? data.version.trim()
    : requiredVersion;
  if(version!==requiredVersion) throw new HttpsError("failed-precondition","Agreement version is no longer current.");
  const deviceId = typeof data.deviceId === "string" ? data.deviceId.trim() : "";
  const appVersion = typeof data.appVersion === "string" ? data.appVersion.trim() : "";

  // Fetch sadhak profile details if available for query metadata
  const [sadhakSnap, provSnap] = await Promise.all([
    db.collection("sadhaks").doc(uid).get(),
    db.collection("providers").doc(uid).get(),
  ]);
  const profile = sadhakSnap.exists ? sadhakSnap.data() : (provSnap.exists ? provSnap.data() : {});
  const sadhakName = profile.name || profile.displayName || profile.nameHi || profile.nameEn || "";
  const sadhakPhone = profile.phone || profile.phoneNumber || "";

  // 1. Add new document in sadhakAgreementAcceptances (old documents preserved)
  const acceptanceRef = db.collection("sadhakAgreementAcceptances").doc();
  const batch = db.batch();
  batch.create(acceptanceRef, {
    uid,
    sadhakId: uid,
    version,
    deviceId,
    appVersion,
    sadhakName,
    sadhakPhone,
    acceptedAt: FieldValue.serverTimestamp(),
    createdAt: FieldValue.serverTimestamp(),
  });

  // 2. Update sadhak profile
  const profileUpdate = {
    acceptedAgreementVersion: version,
    agreementAcceptedAt: FieldValue.serverTimestamp(),
  };

  batch.set(db.collection("sadhaks").doc(uid), profileUpdate, { merge: true });
  batch.set(db.collection("providers").doc(uid), profileUpdate, { merge: true });
  await batch.commit();

  return {
    success: true,
    acceptanceId: acceptanceRef.id,
    version,
    uid,
  };
});

exports.goOnline = onCall(async (request) => {
  const uid = requireUid(request);
  await assertSadhakAgreement(uid);
  const p=await db.doc(`providers/${uid}`).get();
  if(p.get('status')!=='APPROVED' || p.get('isVerified')!==true) throw new HttpsError('permission-denied','Provider approval required.');
  const isOnline = request.data?.isOnline !== false;

  const update = {
    isOnline,
    availability: isOnline ? "available" : "unavailable",
    isAvailable: isOnline,
    lastActiveAt: FieldValue.serverTimestamp(),
    updatedAt: FieldValue.serverTimestamp(),
  };

  await Promise.all([
    db.collection("sadhaks").doc(uid).set(update, { merge: true }),
    db.collection("providers").doc(uid).set(update, { merge: true }),
  ]);

  return { success: true, isOnline, uid };
});
exports.sadhakGoOnline = exports.goOnline;
exports.setSadhakOnline = exports.goOnline;

exports.acceptSession = onCall(sessions.accept);
exports.sadhakAcceptSession = exports.acceptSession;
exports.acceptCall = onCall(async request => {
  const uid=requireUid(request), callId=requireId(request.data?.callId,'callId');
  const ref=db.doc(`active_calls/${callId}`), snap=await ref.get();
  if(!snap.exists) throw new HttpsError('not-found','Call not found.');
  if(snap.get('providerId')!==uid) throw new HttpsError('permission-denied','Not the assigned provider.');
  if(!['ringing','CALLING','WAITING','ACCEPTED'].includes(snap.get('status'))) throw new HttpsError('failed-precondition','Call is not awaiting acceptance.');
  const result=await sessions.accept({...request,data:{sessionId:snap.get('sessionId')}});
  await ref.update({status:'ACCEPTED',acceptedAt:FieldValue.serverTimestamp()});
  return result;
});

exports.withdrawRequest = exports.requestPayout;
exports.requestWithdrawal = exports.requestPayout;

exports.adminGetAgreementAcceptances = onCall(async (request) => {
  requireAdmin(request);
  const data = request.data || {};
  let query = db.collection("sadhakAgreementAcceptances");
  if (data.sadhakId || data.uid) {
    query = query.where("uid", "==", data.sadhakId || data.uid);
  }
  if (data.version) {
    query = query.where("version", "==", data.version);
  }
  query = query.orderBy("acceptedAt", "desc").limit(Math.min(Number(data.limit) || 100, 500));
  const snap = await query.get();
  return {
    count: snap.size,
    acceptances: snap.docs.map(doc => ({ id: doc.id, ...doc.data() })),
  };
});

exports.onSadhakStatusUpdated = onDocumentUpdated(
  { document: "sadhaks/{sadhakId}", ...FIRESTORE_TRIGGER_OPTS },
  async (event) => {
    const before = event.data?.before?.data() || {};
    const after = event.data?.after?.data() || {};
    const wentOnline = (after.isOnline === true || after.isAvailable === true || after.availability === true || after.availability === "available") &&
                       (before.isOnline !== true && before.isAvailable !== true && before.availability !== true && before.availability !== "available");
    if (!wentOnline) return null;

    const config = await getServerConfig();
    const requiredVersion = config.agreementVersion || "2.0";
    if (after.acceptedAgreementVersion !== requiredVersion) {
      console.warn(`[onSadhakStatusUpdated] Reverting online status for ${event.params.sadhakId}: agreement not accepted`);
      await event.data.after.ref.update({
        isOnline: false,
        isAvailable: false,
        availability: "unavailable",
        agreementPending: true,
      });
      await db.collection("providers").doc(event.params.sadhakId).update({
        isOnline: false,
        isAvailable: false,
        availability: "unavailable",
        agreementPending: true,
      }).catch(() => {});
    }
    return null;
  }
);


exports.adminClawbackFreeTrial = onCall(async (request) => {
  requireAdmin(request);
  const uid = requireId((request.data || {}).uid, "uid");
  const amount = Number(request.data.amountPaise);
  if (!amount || amount <= 0) throw new HttpsError("invalid-argument", "Invalid amount.");

  const walletRef = db.collection("providers").doc(uid).collection("wallet").doc("summary");
  const txRef = db.collection("providers").doc(uid).collection("transactions").doc(`clawback_${Date.now()}`);

  return db.runTransaction(async (tx) => {
    tx.set(walletRef, {
      balance: FieldValue.increment(-amount),
      totalEarnings: FieldValue.increment(-amount),
      updatedAt: FieldValue.serverTimestamp(),
    }, { merge: true });
    tx.set(txRef, {
      type: "clawback",
      amountPaise: -amount,
      reason: "free_trial_abuse_clawback",
      timestamp: FieldValue.serverTimestamp(),
    });
  });
});

// ---------------------------------------------------------------------------
// Chat & Conversation Retention: 1 Year Auto-Delete (Daily 03:00 Asia/Kolkata)
// ---------------------------------------------------------------------------
function getRecordMillis(val) {
  if (!val) return null;
  if (typeof val === "number") return val;
  if (typeof val.toMillis === "function") return val.toMillis();
  if (typeof val.toDate === "function") return val.toDate().getTime();
  if (val instanceof Date) return val.getTime();
  return null;
}

async function executeCleanupOldConversations() {
  const RETENTION_MS = 365 * 24 * 60 * 60 * 1000; // 365 days
  const cutoffMs = Date.now() - RETENTION_MS;
  const BATCH_LIMIT = 400;

  let deletedMessagesCount = 0;
  let deletedChatsCount = 0;
  let redactedQuestionsCount = 0;

  let batch = db.batch();
  let batchOps = 0;

  async function flushBatch() {
    if (batchOps > 0) {
      await batch.commit();
      batch = db.batch();
      batchOps = 0;
    }
  }

  // 1. Process chats/{chatId}/messages & chats/{chatId}
  const chatsSnap = await db.collection("chats").get();
  for (const chatDoc of chatsSnap.docs) {
    const chatData = chatDoc.data() || {};
    const messagesSnap = await chatDoc.ref.collection("messages").get();

    let allDeleted = true;
    for (const msgDoc of messagesSnap.docs) {
      const msgData = msgDoc.data() || {};
      const msgTime = getRecordMillis(msgData.timestamp) || getRecordMillis(msgData.createdAt);
      if (msgTime && msgTime <= cutoffMs) {
        batch.delete(msgDoc.ref);
        batchOps++;
        deletedMessagesCount++;
        if (batchOps >= BATCH_LIMIT) {
          await flushBatch();
        }
      } else {
        allDeleted = false;
      }
    }

    // If chat has no messages remaining and chat itself was created > 365 days ago
    const chatCreatedMs = getRecordMillis(chatData.createdAt);
    if ((messagesSnap.empty || allDeleted) && chatCreatedMs && chatCreatedMs <= cutoffMs) {
      batch.delete(chatDoc.ref);
      batchOps++;
      deletedChatsCount++;
      if (batchOps >= BATCH_LIMIT) {
        await flushBatch();
      }
    }
  }

  // 2. Process questions collection: answered or closed > 365 days old
  // Redact text & answer only; financial fields, settlement IDs, statuses and timestamps remain untouched.
  const questionsSnap = await db.collection("questions").get();
  for (const qDoc of questionsSnap.docs) {
    const qData = qDoc.data() || {};
    const status = (qData.status || "").toLowerCase();

    if (status === "answered" || status === "closed") {
      const qTime = getRecordMillis(qData.answeredAt) ||
                    getRecordMillis(qData.closedAt) ||
                    getRecordMillis(qData.settledAt) ||
                    getRecordMillis(qData.createdAt) ||
                    getRecordMillis(qData.updatedAt);

      if (qTime && qTime <= cutoffMs) {
        const redactionUpdates = {};
        if (qData.questionText !== undefined) redactionUpdates.questionText = FieldValue.delete();
        if (qData.text !== undefined) redactionUpdates.text = FieldValue.delete();
        if (qData.question !== undefined) redactionUpdates.question = FieldValue.delete();
        if (qData.providerAnswer !== undefined) redactionUpdates.providerAnswer = FieldValue.delete();
        if (qData.answer !== undefined) redactionUpdates.answer = FieldValue.delete();
        if (qData.answerText !== undefined) redactionUpdates.answerText = FieldValue.delete();

        if (Object.keys(redactionUpdates).length > 0) {
          batch.update(qDoc.ref, redactionUpdates);
          batchOps++;
          redactedQuestionsCount++;
          if (batchOps >= BATCH_LIMIT) {
            await flushBatch();
          }
        }
      }
    }
  }

  await flushBatch();

  console.log(`cleanupOldConversations complete: deleted ${deletedMessagesCount} messages, ${deletedChatsCount} chat docs, redacted text on ${redactedQuestionsCount} questions.`);
  return {
    success: true,
    deletedMessagesCount,
    deletedChatsCount,
    redactedQuestionsCount,
    executedAt: new Date().toISOString()
  };
}

exports.cleanupOldConversations = onSchedule({
  schedule: "0 3 * * *",
  timeZone: "Asia/Kolkata"
}, async () => {
  await executeCleanupOldConversations();
});

exports.adminCleanupOldConversationsNow = onCall(async (request) => {
  requireAdmin(request);
  return executeCleanupOldConversations();
});

// ---------------------------------------------------------------------------
// TTL Backup: Purge Expired Chats, Messages and Sessions (Every 24 hours)
// ---------------------------------------------------------------------------
async function executePurgeExpiredChats() {
  const now = Timestamp.now();
  const BATCH_LIMIT = 400;

  let deletedMessagesCount = 0;
  let deletedChatsCount = 0;
  let deletedSessionsCount = 0;

  let batch = db.batch();
  let batchOps = 0;

  async function flushBatch() {
    if (batchOps > 0) {
      await batch.commit();
      batch = db.batch();
      batchOps = 0;
    }
  }

  // 1. Expired messages across all chats
  try {
    const expiredMessagesSnap = await db.collectionGroup("messages")
      .where("expireAt", "<=", now)
      .limit(BATCH_LIMIT)
      .get();

    for (const doc of expiredMessagesSnap.docs) {
      batch.delete(doc.ref);
      batchOps++;
      deletedMessagesCount++;
      if (batchOps >= BATCH_LIMIT) await flushBatch();
    }
    await flushBatch();
  } catch (err) {
    console.warn("CollectionGroup messages query failed, falling back to chat subcollections:", err.message);
    const chatsSnap = await db.collection("chats").get();
    for (const chatDoc of chatsSnap.docs) {
      const msgs = await chatDoc.ref.collection("messages").where("expireAt", "<=", now).limit(BATCH_LIMIT).get();
      for (const m of msgs.docs) {
        batch.delete(m.ref);
        batchOps++;
        deletedMessagesCount++;
        if (batchOps >= BATCH_LIMIT) await flushBatch();
      }
    }
    await flushBatch();
  }

  // 2. Expired chats
  const expiredChatsSnap = await db.collection("chats")
    .where("expireAt", "<=", now)
    .limit(BATCH_LIMIT)
    .get();

  for (const doc of expiredChatsSnap.docs) {
    batch.delete(doc.ref);
    batchOps++;
    deletedChatsCount++;
    if (batchOps >= BATCH_LIMIT) await flushBatch();
  }
  await flushBatch();

  // 3. Expired sessions
  const expiredSessionsSnap = await db.collection("sessions")
    .where("expireAt", "<=", now)
    .limit(BATCH_LIMIT)
    .get();

  for (const doc of expiredSessionsSnap.docs) {
    batch.delete(doc.ref);
    batchOps++;
    deletedSessionsCount++;
    if (batchOps >= BATCH_LIMIT) await flushBatch();
  }
  await flushBatch();

  console.log(`purgeExpiredChats complete: deleted ${deletedMessagesCount} messages, ${deletedChatsCount} chats, ${deletedSessionsCount} sessions.`);
  return {
    success: true,
    deletedMessagesCount,
    deletedChatsCount,
    deletedSessionsCount,
    executedAt: new Date().toISOString()
  };
}

exports.purgeExpiredChats = onSchedule("every 24 hours", async () => {
  await executePurgeExpiredChats();
});

exports.adminPurgeExpiredChatsNow = onCall(async (request) => {
  requireAdmin(request);
  return executePurgeExpiredChats();
});

exports.postComment = onCall(async (request) => {
  const uid = requireUid(request);
  const data = request.data || {};
  const cardId = requireId(data.cardId, "cardId");
  const userName = String(data.userName || "साधक").trim().slice(0, 50);
  const commentText = String(data.commentText || "").trim().slice(0, 1000);
  const rating = Number.isInteger(Number(data.rating)) ? Number(data.rating) : 5;

  if (!commentText || !Number.isInteger(rating) || rating<1 || rating>5) {
    throw new HttpsError("invalid-argument", "Comment text cannot be empty.");
  }

  const commentRef = db.collection("card_comments").doc();
  const batch = db.batch();
  batch.set(commentRef, {
    cardId,
    userName,
    commentText,
    rating,
    timestamp: FieldValue.serverTimestamp(),
  });
  batch.set(commentRef.collection("private").doc("author"), {
    authorUid: uid,
    createdAt: FieldValue.serverTimestamp(),
  });

  await batch.commit();
  return { success: true, commentId: commentRef.id };
});


// ---------------------------------------------------------------------------
// Payout Slip PDF Generation & Scheduled No-Show Cancellations (A1 & A2)
// ---------------------------------------------------------------------------

async function generateAndUploadPayoutSlipPdf(providerId, requestId, reqData) {
  const [provSnap, sadhakSnap] = await Promise.all([
    db.collection("providers").doc(providerId).get(),
    db.collection("sadhaks").doc(providerId).get(),
  ]);
  const pData = provSnap.exists ? provSnap.data() : (sadhakSnap.exists ? sadhakSnap.data() : {});
  const providerName = pData.name || pData.displayName || pData.fullName || "Sadhak";

  const grossPaise = reqData.grossPaise !== undefined ? reqData.grossPaise : (reqData.amountPaise || 0);
  const tdsPaise = reqData.tdsPaise !== undefined ? reqData.tdsPaise : 0;
  const feePaise = reqData.feePaise !== undefined ? reqData.feePaise : 0;
  const netPaise = reqData.netPaise !== undefined ? reqData.netPaise : (grossPaise - tdsPaise - feePaise);

  const reqDate = reqData.createdAt?.toDate ? reqData.createdAt.toDate().toLocaleDateString("en-IN") : "N/A";
  const paidDate = reqData.paidAt?.toDate ? reqData.paidAt.toDate().toLocaleDateString("en-IN") : "N/A";
  const utr = reqData.utr || "N/A";

  // Bank account masked: "XXXX" + last 4 digits only
  let maskedAccount = "XXXX";
  if (typeof reqData.destination === "string") {
    const match = reqData.destination.match(/XXXX(\d{4})/i) || reqData.destination.match(/(\d{4})/);
    if (match) {
      maskedAccount = `XXXX${match[1]}`;
    }
  }

  const doc = new PDFDocument({ margin: 50, size: "A4" });
  const chunks = [];
  doc.on("data", (chunk) => chunks.push(chunk));

  doc.fontSize(22).font("Helvetica-Bold").text("Devbhasha - Payout Slip", { align: "center" });
  doc.moveDown(0.5);
  doc.fontSize(12).font("Helvetica").text("Official Payout Disbursement Statement", { align: "center" });
  doc.moveDown(1.5);

  doc.strokeColor("#E2E8F0").lineWidth(1).moveTo(50, doc.y).lineTo(545, doc.y).stroke();
  doc.moveDown(1);

  doc.font("Helvetica-Bold").fontSize(11).text("Slip Number (Request ID): ", { continued: true });
  doc.font("Helvetica").text(requestId);
  doc.moveDown(0.5);

  doc.font("Helvetica-Bold").text("Provider Name: ", { continued: true });
  doc.font("Helvetica").text(providerName);
  doc.moveDown(0.5);

  doc.font("Helvetica-Bold").text("Request Date: ", { continued: true });
  doc.font("Helvetica").text(reqDate);
  doc.moveDown(0.5);

  doc.font("Helvetica-Bold").text("Disbursement Date: ", { continued: true });
  doc.font("Helvetica").text(paidDate);
  doc.moveDown(0.5);

  doc.font("Helvetica-Bold").text("UTR / Reference: ", { continued: true });
  doc.font("Helvetica").text(utr);
  doc.moveDown(0.5);

  doc.font("Helvetica-Bold").text("Bank Account: ", { continued: true });
  doc.font("Helvetica").text(maskedAccount);
  doc.moveDown(1.5);

  doc.strokeColor("#E2E8F0").lineWidth(1).moveTo(50, doc.y).lineTo(545, doc.y).stroke();
  doc.moveDown(1);

  doc.fontSize(14).font("Helvetica-Bold").text("Payment Breakdown");
  doc.moveDown(0.8);

  const formatRs = (paise) => `₹ ${(paise / 100).toFixed(2)}`;

  doc.fontSize(11).font("Helvetica").text("Gross Amount:", 50);
  doc.text(formatRs(grossPaise), 400, doc.y - 14, { align: "right", width: 145 });
  doc.moveDown(0.5);

  doc.text("TDS Deducted:", 50);
  doc.text(`- ${formatRs(tdsPaise)}`, 400, doc.y - 14, { align: "right", width: 145 });
  doc.moveDown(0.5);

  doc.text("Platform Fee:", 50);
  doc.text(`- ${formatRs(feePaise)}`, 400, doc.y - 14, { align: "right", width: 145 });
  doc.moveDown(0.8);

  doc.strokeColor("#E2E8F0").lineWidth(1).moveTo(50, doc.y).lineTo(545, doc.y).stroke();
  doc.moveDown(0.8);

  doc.font("Helvetica-Bold").fontSize(13).text("Net Paid Amount:", 50);
  doc.text(formatRs(netPaise), 400, doc.y - 16, { align: "right", width: 145 });
  doc.moveDown(2);

  doc.fontSize(9).font("Helvetica").fillColor("#64748B")
    .text("This is a computer-generated payout slip and does not require a physical signature.", { align: "center" });

  doc.end();

  const buffer = await new Promise((resolve, reject) => {
    doc.on("end", () => resolve(Buffer.concat(chunks)));
    doc.on("error", reject);
  });

  const bucket = getStorage().bucket();
  const filePath = `payout_slips/${providerId}/${requestId}.pdf`;
  const file = bucket.file(filePath);

  await file.save(buffer, {
    metadata: {
      contentType: "application/pdf",
    },
  });

  const [signedUrl] = await file.getSignedUrl({
    action: "read",
    expires: Date.now() + 15 * 60 * 1000, // 15 minutes valid
  });

  return signedUrl;
}

exports.adminGeneratePayoutSlip = onCall(async (request) => {
  requireAdmin(request);
  const data = request.data || {};
  const providerId = requireId(data.providerId, "providerId");
  const requestId = requireId(data.requestId, "requestId");

  const reqDoc = await db.collection("providers").doc(providerId).collection("payout_requests").doc(requestId).get();
  if (!reqDoc.exists) throw new HttpsError("not-found", "Payout request not found.");
  const reqData = reqDoc.data();
  if (reqData.status !== "paid") {
    throw new HttpsError("failed-precondition", "Payout slip is only available for paid requests.");
  }

  const url = await generateAndUploadPayoutSlipPdf(providerId, requestId, reqData);
  return { url };
});

exports.getMyPayoutSlip = onCall(async (request) => {
  const uid = requireUid(request);
  const data = request.data || {};
  const requestId = requireId(data.requestId, "requestId");

  const reqDoc = await db.collection("providers").doc(uid).collection("payout_requests").doc(requestId).get();
  if (!reqDoc.exists) throw new HttpsError("not-found", "Payout request not found.");
  const reqData = reqDoc.data();
  if (reqData.status !== "paid") {
    throw new HttpsError("failed-precondition", "Payout slip is only available for paid requests.");
  }

  const url = await generateAndUploadPayoutSlipPdf(uid, requestId, reqData);
  return { url };
});

/**
 * Scheduled function (every 5 minutes):
 * Detects sessions/questions in "accepted" or "pending" status where sadhak did not start
 * within noShowMinutes (config, default 10), marks them "cancelled_noshow", and
 * idempotently refunds the seeker wallet logging reason "NO_SHOW_REFUND".
 */
exports.cancelNoShowSessionsAndQuestions = onSchedule("every 5 minutes", async () => {
  const config = await getServerConfig();
  const noShowMinutes = Number(config.noShowMinutes) || 10;
  const cutoffTime = new Date(Date.now() - noShowMinutes * 60 * 1000);

  // 1. Sessions in pending or accepted
  const pendingSessions = await db.collection("sessions")
    .where("status", "in", ["pending", "accepted"])
    .get();

  for (const doc of pendingSessions.docs) {
    const s = doc.data();
    if (Number.isSafeInteger(s.reservedPaise)) continue;
    const created = s.createdAt?.toDate ? s.createdAt.toDate() : (s.acceptedAt?.toDate ? s.acceptedAt.toDate() : null);
    if (!created || created >= cutoffTime) continue;

    const sessionId = doc.id;
    const seekerId = s.seekerId || s.userId;
    const amountPaise = Number(s.amountPaise) || Number(s.initialHoldPaise) || 0;
    const refundRefId = `refund_noshow_session_${sessionId}`;
    const ledgerRef = db.collection("walletLedger").doc(refundRefId);
    const sessionRef = doc.ref;

    try {
      await db.runTransaction(async (tx) => {
        const snap = await tx.get(sessionRef);
        if (!snap.exists) return;
        const data = snap.data();
        if (data.status !== "pending" && data.status !== "accepted") return;

        const ledgerSnap = await tx.get(ledgerRef);
        if (ledgerSnap.exists) {
          tx.update(sessionRef, { status: "cancelled_noshow", updatedAt: FieldValue.serverTimestamp() });
          return;
        }

        tx.update(sessionRef, {
          status: "cancelled_noshow",
          cancelledReason: "Sadhak did not start within allowed window",
          updatedAt: FieldValue.serverTimestamp(),
        });

        if (seekerId && amountPaise > 0) {
          const userRef = db.collection("users").doc(seekerId);
          tx.set(userRef, { walletBalance: FieldValue.increment(amountPaise) }, { merge: true });
          tx.set(ledgerRef, {
            uid: seekerId,
            userId: seekerId,
            amountPaise,
            type: "refund",
            purpose: "refund",
            reason: "NO_SHOW_REFUND",
            sessionId,
            referenceId: sessionId,
            referenceType: "session",
            createdAt: FieldValue.serverTimestamp(),
          });
        }
      });
    } catch (err) {
      console.error(`Failed to cancel no-show session ${sessionId}:`, err);
    }
  }

  // 2. Questions in pending or accepted
  const pendingQuestions = await db.collection("questions")
    .where("status", "in", ["pending", "accepted"])
    .get();

  for (const qDoc of pendingQuestions.docs) {
    const q = qDoc.data();
    if (q.sessionId) continue;
    const created = q.createdAt?.toDate ? q.createdAt.toDate() : null;
    if (!created || created >= cutoffTime) continue;

    const questionId = qDoc.id;
    const seekerId = q.seekerId || q.userId;
    const amountPaise = Number(q.amountPaise) || 0;
    const refundRefId = `refund_noshow_question_${questionId}`;
    const ledgerRef = db.collection("walletLedger").doc(refundRefId);
    const questionRef = qDoc.ref;

    try {
      await db.runTransaction(async (tx) => {
        const snap = await tx.get(questionRef);
        if (!snap.exists) return;
        const data = snap.data();
        if (data.status !== "pending" && data.status !== "accepted") return;

        const ledgerSnap = await tx.get(ledgerRef);
        if (ledgerSnap.exists) {
          tx.update(questionRef, { status: "cancelled_noshow", updatedAt: FieldValue.serverTimestamp() });
          return;
        }

        tx.update(questionRef, {
          status: "cancelled_noshow",
          cancelledReason: "No response within allowed time",
          updatedAt: FieldValue.serverTimestamp(),
        });

        if (seekerId && amountPaise > 0) {
          const userRef = db.collection("users").doc(seekerId);
          tx.set(userRef, { walletBalance: FieldValue.increment(amountPaise) }, { merge: true });
          tx.set(ledgerRef, {
            uid: seekerId,
            userId: seekerId,
            amountPaise,
            type: "refund",
            purpose: "refund",
            reason: "NO_SHOW_REFUND",
            questionId,
            referenceId: questionId,
            referenceType: "question",
            createdAt: FieldValue.serverTimestamp(),
          });
        }
      });
    } catch (err) {
      console.error(`Failed to cancel no-show question ${questionId}:`, err);
    }
  }
});

exports.adminUpdateAppContent=onCall(async request=>{
  requireAdmin(request);
  const pageId=requireId(request.data?.pageId,'pageId'), content=request.data?.content;
  if(!content || typeof content!=='object' || Array.isArray(content) || Buffer.byteLength(JSON.stringify(content))>200000) throw new HttpsError('invalid-argument','Invalid content.');
  const ref=db.doc(`app_content/${pageId}`), version=ref.collection('versions').doc();
  await db.runTransaction(async tx=>{
    const before=await tx.get(ref);
    tx.create(version,{previous:before.data()||null,content,createdAt:FieldValue.serverTimestamp(),adminUid:request.auth.uid});
    tx.set(ref,{...content,updatedAt:FieldValue.serverTimestamp(),updatedBy:request.auth.uid},{merge:true});
    tx.create(db.collection('adminAuditLogs').doc(),{adminUid:request.auth.uid,action:'UPDATE_CONTENT_PAGE',pageId,versionId:version.id,createdAt:FieldValue.serverTimestamp()});
  });
  return {success:true,versionId:version.id};
});
exports.onSessionMessage=onDocumentCreated({document:'chats/{sessionId}/messages/{messageId}',database:FIRESTORE_DB_ID},async event=>{
  const sender=event.data?.get('senderId'), ref=db.doc(`sessions/${event.params.sessionId}`);
  await db.runTransaction(async tx=>{
    const snap=await tx.get(ref);if(!snap.exists || snap.get('settled'))return;
    const field=sender===snap.get('seekerId')?'seekerParticipated':sender===snap.get('providerId')?'providerParticipated':null;
    if(field)tx.update(ref,{[field]:true});
  });
});

// Raw-body HMAC authentication; independent of the mobile callback and App Check.
exports.razorpayWebhook=onRequest({secrets:[...RAZORPAY_SECRETS,RAZORPAY_WEBHOOK_SECRET]},async(req,res)=>{
  if(req.method!=='POST'){res.status(405).send('Method not allowed');return;}
  const supplied=String(req.get('x-razorpay-signature')||'');
  const expected=crypto.createHmac('sha256',RAZORPAY_WEBHOOK_SECRET.value()).update(req.rawBody).digest('hex');
  if(!/^[a-f0-9]{64}$/i.test(supplied) || !crypto.timingSafeEqual(Buffer.from(expected,'hex'),Buffer.from(supplied,'hex'))){res.status(401).send('Invalid signature');return;}
  if(!['payment.captured','order.paid'].includes(req.body?.event)){res.status(200).send('Ignored');return;}
  const payment=req.body?.payload?.payment?.entity;
  if(!payment?.id || !payment?.order_id){res.status(400).send('Missing payment');return;}
  try {
    const order=await db.doc(`paymentOrders/${requireId(payment.order_id,'orderId')}`).get();
    if(!order.exists){res.status(200).send('Unrelated order');return;}
    await creditCapturedPayment(order.get('uid'),payment.order_id,payment.id);
    res.status(200).send('OK');
  }catch(e){console.error('Payment recovery failed',e.code);res.status(500).send('Retry later');}
});
