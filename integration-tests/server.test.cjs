const {test,before,after}=require('node:test');
const assert=require('node:assert/strict');
const vm=require('node:vm');
const fs=require('node:fs');
const path=require('node:path');
const {createRequire}=require('node:module');
const req=createRequire(path.join(__dirname,'../functions/index.js'));
const admin=req('firebase-admin');
const {getFirestore}=req('firebase-admin/firestore');
const {HttpsError}=req('firebase-functions/v2/https');
const app=admin.initializeApp({projectId:'demo-devbhasha'});
const db=getFirestore(app);
const handlers={};
const request=(uid,data)=>({auth:{uid,token:{}},data});
before(async()=>{
 const wrap=(...args)=>args.at(-1);
 const custom=name=>{
  if(name==='firebase-admin') return {...admin,initializeApp:()=>app};
  if(name==='firebase-functions/v2/https') return {onCall:wrap,HttpsError};
  if(name==='firebase-functions/v2/firestore') return {onDocumentUpdated:wrap};
  if(name==='firebase-functions/v2/scheduler') return {onSchedule:wrap};
  if(name==='firebase-functions/params') return {defineSecret:()=>({value:()=> 'emulator'}),defineString:()=>({value:()=> 'a'.repeat(32)})};
  return req(name);
 };
 vm.runInNewContext(fs.readFileSync(path.join(__dirname,'../functions/index.js'),'utf8'),
  {require:custom,exports:handlers,process:{env:{FIRESTORE_DB_ID:'(default)'}},console,Buffer,Date,fetch});
 await db.doc('providers/live-provider').set({status:'APPROVED',isVerified:true,isOnline:true,name:'Provider'});
 await db.doc('users/live-seeker').set({walletBalance:100000,userName:'Seeker'});
});
after(async()=>{await app.delete();});
test('real Firestore transaction: seeker start -> provider accept -> concurrent ends -> single credit',async()=>{
 const start=await handlers.startSession(request('live-seeker',{type:'SESSION',mode:'call',providerId:'live-provider'}));
 const id=start.sessionId;
 const call=await db.doc(`active_calls/${id}`).get();
 const room=await db.doc(`chats/${id}`).get();
 assert.equal(call.get('sessionId'),id);assert.equal(room.get('providerId'),'live-provider');
 await db.doc(`active_calls/${id}`).update({connectedAt:Date.now(),status:'Connected'});
 await Promise.all(['live-provider','live-seeker','live-provider'].map(uid=>handlers.endSession(request(uid,{sessionId:id}))));
 assert.equal((await db.doc('users/live-seeker').get()).get('walletBalance'),50100);
 const wallet=await db.doc('providers/live-provider/wallet/summary').get();
 assert.equal(wallet.get('balancePaise'),32435);assert.equal(wallet.get('balance'),324.35);
 assert.equal((await db.doc(`active_calls/${id}`).get()).get('status'),'Ended');
});
test('real Firestore refund and payout reservation reject overdrafts and replay safely',async()=>{
 await db.doc('users/refund-seeker').set({walletBalance:100000});
 const s=await handlers.startSession(request('refund-seeker',{type:'SESSION',mode:'call',providerId:'live-provider'}));
 await Promise.all([1,2].map(()=>handlers.endSession(request('live-provider',{sessionId:s.sessionId}))));
 assert.equal((await db.doc('users/refund-seeker').get()).get('walletBalance'),100000);
 await db.doc('providers/live-provider/wallet/summary').set({balancePaise:100000,balance:1000});
 const payload={amountPaise:50000,payoutType:'UPI',upiId:'provider@bank',idempotencyKey:'same-request'};
 const a=await handlers.requestProviderPayout(request('live-provider',payload));
 const b=await handlers.requestProviderPayout(request('live-provider',payload));
 assert.equal(a.requestId,b.requestId);
 assert.equal((await db.doc('providers/live-provider/wallet/summary').get()).get('balancePaise'),50000);
 await assert.rejects(handlers.requestProviderPayout(request('live-provider',{...payload,idempotencyKey:'overdraft',amountPaise:60000})),{code:'failed-precondition'});
});
