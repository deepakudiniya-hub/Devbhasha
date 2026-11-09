const {test,beforeEach,after}=require('node:test');const assert=require('node:assert/strict');
if(!process.env.FIRESTORE_EMULATOR_HOST)throw Error('Emulator required');
const admin=require('firebase-admin');const app=admin.initializeApp({projectId:'demo-devbhasha'},'sessions-test');const db=app.firestore();
class HttpsError extends Error {constructor(code,message){super(message);this.code=code;}}
const api=require('./sessions')({db,FieldValue:admin.firestore.FieldValue,Timestamp:admin.firestore.Timestamp,HttpsError,
 config:async()=>({agreementVersion:'2.0'}),uid:r=>r.auth.uid,id:x=>x,channel:s=>`channel_${s}`});
const request=(uid,data)=>({auth:{uid,token:{phone_number:'+919000000001'}},data});
const start=(key='one',type='CHAT_PER_MIN')=>api.start(request('u',{providerId:'p',type,idempotencyKey:key}));
beforeEach(async()=>{
 for(const coll of ['users','providers','sessions','chats','sessionLocks','walletLedger','freeTrialClaims','active_calls'])await db.recursiveDelete(db.collection(coll));
 await db.doc('users/u').set({walletBalance:10000});
 await db.doc('providers/p').set({status:'APPROVED',isVerified:true,isOnline:true,isAvailable:true,acceptedAgreementVersion:'2.0',chatPricePerMin:10,callPricePerMin:10});
});
after(()=>app.delete());
test('concurrent starts cannot spend same balance twice',async()=>{
 const r=await Promise.allSettled([start('a'),start('b')]);assert.equal(r.filter(x=>x.status==='fulfilled').length,1);
 assert.equal((await db.doc('users/u').get()).get('walletBalance'),0);
});
test('start retries reuse session and no-show refunds exactly once',async()=>{
 const a=await start(),b=await start();assert.equal(a.sessionId,b.sessionId);
 await api.settle(a.sessionId,'system');await api.settle(a.sessionId,'system');
 assert.equal((await db.doc('users/u').get()).get('walletBalance'),10000);
 assert.equal((await db.doc('users/u').get()).get('reservedBalance'),0);
 assert.equal((await db.doc('sessionLocks/p').get()).exists,false);
});
test('outsider cannot accept; settled session cannot reopen',async()=>{
 const a=await start();await assert.rejects(api.accept(request('u',{sessionId:a.sessionId})),{code:'permission-denied'});
 await api.settle(a.sessionId,'system');await assert.rejects(api.accept(request('p',{sessionId:a.sessionId})),{code:'failed-precondition'});
});
test('call timer starts only when both participants report connected',async()=>{
 const a=await start('call','CALL_PER_MIN');await api.accept(request('p',{sessionId:a.sessionId}));
 await api.confirm(request('p',{sessionId:a.sessionId}));assert.equal((await db.doc(`sessions/${a.sessionId}`).get()).get('startedAt'),undefined);
 await api.confirm(request('u',{sessionId:a.sessionId}));assert.equal((await db.doc(`sessions/${a.sessionId}`).get()).get('status'),'active');
 await api.settle(a.sessionId,'u');assert.equal((await db.doc('users/u').get()).get('walletBalance'),9000);
 assert.equal((await db.doc('providers/p/wallet/summary').get()).get('balance'),600);
});
test('unapproved provider is rejected by backend',async()=>{
 await db.doc('providers/p').update({status:'PENDING'});await assert.rejects(start(),{code:'failed-precondition'});
});

test('duplicate concurrent settlements credit the provider once',async()=>{
 const a=await start();await api.accept(request('p',{sessionId:a.sessionId}));
 await Promise.all([api.settle(a.sessionId,'u'),api.settle(a.sessionId,'p')]);
 assert.equal((await db.doc('users/u').get()).get('walletBalance'),9000);
 assert.equal((await db.doc('providers/p/wallet/summary').get()).get('balance'),600);
});
