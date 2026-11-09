'use strict';
const {eligibleProvider, settlement} = require('./session-policy');
// Dependency injection keeps transaction behavior testable without production credentials.
module.exports = function sessions({db, FieldValue:F, Timestamp:T, HttpsError:E, config, id, uid, channel}) {
  const stamp = () => F.serverTimestamp();
  const ms = v => v?.toMillis?.() || Number(v) || 0;
  const lock = u => db.doc(`sessionLocks/${u}`);
  const ledger = (u,s,k) => db.doc(`walletLedger/${u}_session_${s}_${k}`);
  const types = ['DREAM_CHAT','DREAM_CALL','CHAT_PER_MIN','CALL_PER_MIN','SESSION_30'];
  async function start(request) {
    const seekerId = uid(request), d = request.data || {}, type = d.type;
    if (!types.includes(type)) throw new E('invalid-argument','Invalid session type.');
    const providerId = id(d.providerId,'providerId');
    if (providerId === seekerId) throw new E('invalid-argument','Cannot consult yourself.');
    const key = id(d.idempotencyKey,'idempotencyKey');
    const sessionId = require('crypto').createHash('sha256').update(`${seekerId}:${key}`).digest('hex');
    const ref = db.doc(`sessions/${sessionId}`), userRef = db.doc(`users/${seekerId}`);
    const phone = request.auth.token.phone_number;
    const trial = typeof phone === 'string' && phone ? db.doc(`freeTrialClaims/${phone}`) : null;
    const c = await config();
    return db.runTransaction(async tx => {
      const [existing, user, provider, a, b, trialSnap] = await Promise.all([
        tx.get(ref), tx.get(userRef), tx.get(db.doc(`providers/${providerId}`)),
        tx.get(lock(seekerId)), tx.get(lock(providerId)), trial ? tx.get(trial) : null
      ]);
      if (existing.exists) {
        const s=existing.data();
        if (s.providerId !== providerId || s.type !== type) throw new E('invalid-argument','Request key was already used.');
        return response(s);
      }
      const p=provider.data(), u=user.data() || {};
      if (!eligibleProvider(p)) throw new E('failed-precondition','Provider is not approved and available.');
      if (p.acceptedAgreementVersion !== (c.agreementVersion || '2.0')) throw new E('failed-precondition','Provider agreement is pending.');
      if (u.blocked || u.disabled || u.deletionPending) throw new E('permission-denied','Account unavailable.');
      if (a.exists || b.exists) throw new E('failed-precondition','A participant already has a pending or active session.');
      const balance = Number.isSafeInteger(u.walletBalance) ? u.walletBalance : 0;
      const isFreeTrial = type === 'DREAM_CHAT' && !!trial && !trialSnap.exists;
      let amountPaise=0, pricePerMinPaise=0, durationMinutes=5;
      if (!isFreeTrial) {
        if (type === 'DREAM_CHAT') amountPaise=19900;
        else if (type === 'DREAM_CALL') amountPaise=29900;
        else if (type === 'SESSION_30') {
          if (!Number.isInteger(p.sessionPrice30Min) || p.sessionPrice30Min<199 || p.sessionPrice30Min>2999) throw new E('failed-precondition','Invalid session rate.');
          amountPaise=p.sessionPrice30Min*100; durationMinutes=30;
        } else {
          const rate=type === 'CHAT_PER_MIN' ? p.chatPricePerMin : p.callPricePerMin;
          if (!Number.isInteger(rate) || rate<5 || rate>100) throw new E('failed-precondition','Invalid minute rate.');
          pricePerMinPaise=rate*100;
          durationMinutes=Math.min(120,Math.floor(balance/pricePerMinPaise));
          if (durationMinutes<1) throw new E('failed-precondition','Insufficient balance.');
        }
      }
      const reservedPaise=pricePerMinPaise ? durationMinutes*pricePerMinPaise : amountPaise;
      if (balance<reservedPaise) throw new E('failed-precondition','Insufficient balance.');
      const now=Date.now(), acceptBy=T.fromMillis(now+120000);
      const questionText=typeof d.questionText==='string' ? d.questionText.trim().slice(0,4000) : '';
      const s={sessionId,seekerId,providerId,participants:[seekerId,providerId],type,status:'pending',
        isFreeTrial,amountPaise,pricePerMinPaise,reservedPaise,durationMinutes,sharePercent:Number.isInteger(c.sadhakSharePercent) && c.sadhakSharePercent>=0 && c.sadhakSharePercent<=100 ? c.sadhakSharePercent : 60,
        createdAt:stamp(),acceptBy,endsAt:acceptBy,settleAfter:acceptBy,settled:false,
        providerAccepted:false,channel:channel(sessionId),chatId:sessionId,
        seekerName: String(u.name || 'Devotee').slice(0,100), providerName:String(p.name || p.displayName || 'Provider').slice(0,100), questionText};
      if (isFreeTrial) {s.trialClaimPath=trial.path;tx.create(trial,{uid:seekerId,sessionId,providerId,claimedAt:stamp()});}
      tx.set(ref,s);
      for (const participant of [seekerId,providerId]) tx.create(lock(participant),{sessionId,createdAt:stamp()});
      if (reservedPaise>0) {
        tx.set(userRef,{walletBalance:F.increment(-reservedPaise),reservedBalance:F.increment(reservedPaise)},{merge:true});
        tx.create(ledger(seekerId,sessionId,'hold'),{uid:seekerId,sessionId,amountPaise:-reservedPaise,purpose:'session_hold',createdAt:stamp()});
      }
      tx.create(db.doc(`chats/${sessionId}`),{sessionId,chatId:sessionId,seekerId,providerId,participants:s.participants,
        seekerName:s.seekerName,type,status:'pending',createdAt:stamp()});
      if (['DREAM_CALL','CALL_PER_MIN','SESSION_30'].includes(type)) tx.create(db.doc(`active_calls/${sessionId}`),{
        sessionId,callId:sessionId,callerId:seekerId,callerName:s.seekerName,providerId,status:'ringing',
        consultationType:type,channelName:s.channel,createdAt:stamp(),timestamp:stamp()});
      if (questionText) tx.create(db.doc(`questions/${sessionId}`),{sessionId,userId:seekerId,userName:s.seekerName,
        providerId,questionText,status:'pending',type:'dream',category:'Dream',createdAt:stamp()});
      return response(s);
    });
  }
  function response(s) {return {sessionId:s.sessionId,status:s.status,endsAt:ms(s.endsAt),amountPaise:s.amountPaise,
    pricePerMinPaise:s.pricePerMinPaise,isFreeTrial:s.isFreeTrial,type:s.type};}
  async function accept(request) {
    const providerId=uid(request), sessionId=id(request.data?.sessionId,'sessionId'), ref=db.doc(`sessions/${sessionId}`);
    const c=await config();
    return db.runTransaction(async tx=>{
      const [snap,provider]=await Promise.all([tx.get(ref),tx.get(db.doc(`providers/${providerId}`))]);
      if (!snap.exists) throw new E('not-found','Session not found.');
      const s=snap.data(),p=provider.data();
      if (s.providerId!==providerId) throw new E('permission-denied','Only the assigned provider may accept.');
      if (!eligibleProvider(p) || p.acceptedAgreementVersion!==(c.agreementVersion||'2.0')) throw new E('failed-precondition','Provider unavailable or agreement pending.');
      if (s.settled || ms(s.endsAt)<=Date.now()) throw new E('failed-precondition','Session expired or settled.');
      if (['active','accepted'].includes(s.status) && s.providerAccepted) return response(s);
      if (s.status!=='pending' || ms(s.acceptBy)<=Date.now()) throw new E('failed-precondition','Session is no longer awaiting acceptance.');
      if(['DREAM_CALL','CALL_PER_MIN','SESSION_30'].includes(s.type)) {
        tx.update(ref,{status:'accepted',providerAccepted:true,acceptedAt:stamp()});
        return {...response(s),status:'accepted'};
      }
      const endsAt=T.fromMillis(Date.now()+s.durationMinutes*60000);
      tx.update(ref,{status:'active',providerAccepted:true,acceptedAt:stamp(),startedAt:stamp(),endsAt,settleAfter:endsAt});
      tx.update(db.doc(`chats/${sessionId}`),{status:'active'});
      return {...response(s),status:'active',endsAt:ms(endsAt)};
    });
  }
  async function settle(sessionId,endedBy) {
    const ref=db.doc(`sessions/${sessionId}`);
    return db.runTransaction(async tx=>{
      const snap=await tx.get(ref);
      if (!snap.exists) return {skipped:true};
      const s=snap.data();
      if(s.settled) return {alreadySettled:true};
      // Old sessions are settled by the preserved migration-compatible implementation.
      if(!Number.isSafeInteger(s.reservedPaise)) throw new E('failed-precondition','Legacy session requires legacy settlement.');
      const aRef=lock(s.seekerId),bRef=lock(s.providerId),pTx=db.doc(`providers/${s.providerId}/transactions/session_${sessionId}`);
      const [a,b,prior,question,call,claim]=await Promise.all([tx.get(aRef),tx.get(bRef),tx.get(pTx),tx.get(db.doc(`questions/${sessionId}`)),
        tx.get(db.doc(`active_calls/${sessionId}`)),s.trialClaimPath?tx.get(db.doc(s.trialClaimPath)):null]);
      const r=settlement(s,Date.now());
      if (s.reservedPaise>0) tx.set(db.doc(`users/${s.seekerId}`),{walletBalance:F.increment(r.refund),reservedBalance:F.increment(-s.reservedPaise)},{merge:true});
      if(r.refund>0) tx.create(ledger(s.seekerId,sessionId,'release'),{uid:s.seekerId,sessionId,amountPaise:r.refund,purpose:'session_hold_release',createdAt:stamp()});
      if(!prior.exists && r.payout>0) {
        tx.create(pTx,{id:pTx.id,type:'session_earning',sessionId,seekerId:s.seekerId,amountPaise:r.payout,grossPaise:r.charged,status:'completed',timestamp:stamp()});
        tx.set(db.doc(`providers/${s.providerId}/wallet/summary`),{balance:F.increment(r.payout),totalEarnings:F.increment(r.payout),updatedAt:stamp()},{merge:true});
      }
      if(a.get('sessionId')===sessionId)tx.delete(aRef);
      if(b.get('sessionId')===sessionId)tx.delete(bRef);
      if(!s.providerAccepted && claim?.get('sessionId')===sessionId)tx.delete(claim.ref);
      const status=s.providerAccepted?'ended':'cancelled_noshow';
      tx.update(ref,{status,settled:true,settledAt:stamp(),endedAt:stamp(),endedBy:endedBy||'system',endsAt:T.now(),
        settleAfter:F.delete(),amountPaise:r.charged,refundedPaise:r.refund,sadhakPayoutPaise:r.payout,platformSharePaise:r.charged-r.payout,
        durationMinutes:r.minutes,expireAt:T.fromMillis(Date.now()+365*86400000)});
      tx.set(db.doc(`chats/${sessionId}`),{status,expireAt:T.fromMillis(Date.now()+365*86400000)},{merge:true});
      if(question.exists)tx.update(question.ref,{settled:true,settlementStatus:'paid_via_session',...(s.providerAccepted?{}:{status:'rejected',rejectReason:'no_show'})});
      if(call.exists)tx.update(call.ref,{status:'ENDED',endedAt:stamp()});
      return {settled:true,amountPaise:r.charged,sadhakPayoutPaise:r.payout,refundedPaise:r.refund};
    });
  }
  async function confirm(request) {
    const participant=uid(request),sessionId=id(request.data?.sessionId,'sessionId'),ref=db.doc(`sessions/${sessionId}`);
    return db.runTransaction(async tx=>{
      const snap=await tx.get(ref),s=snap.data();
      if(!s || !s.participants.includes(participant))throw new E('permission-denied','Not a participant.');
      if(s.settled || !['pending','accepted','active'].includes(s.status) || ms(s.endsAt)<=Date.now())throw new E('failed-precondition','Session is closed.');
      const field=participant===s.providerId?'providerJoined':'seekerJoined';
      const changes={[field]:true};
      if(s.providerAccepted && (s.providerJoined || field==='providerJoined') && (s.seekerJoined || field==='seekerJoined') && !s.startedAt) {
        const endsAt=T.fromMillis(Date.now()+s.durationMinutes*60000);
        Object.assign(changes,{status:'active',startedAt:stamp(),endsAt,settleAfter:endsAt});
        tx.update(db.doc(`chats/${sessionId}`),{status:'active'});
      }
      tx.update(ref,changes);return {success:true};
    });
  }
  return {start,accept,settle,confirm};
};
