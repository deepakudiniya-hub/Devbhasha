const {test} = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');

// Runs the actual exported production handlers with an in-memory Firestore
// transaction boundary. No production services, credentials or real funds.
function harness() {
  const docs = new Map(); let next = 0; let lock = Promise.resolve();
  class Stamp { constructor(ms) { this.ms = ms; } toMillis() { return this.ms; } }
  const Timestamp = {fromMillis: ms => new Stamp(ms), now: () => new Stamp(Date.now())};
  const FieldValue = {increment: n => ({inc:n}), serverTimestamp: () => Timestamp.now(), delete: () => ({del:true})};
  const snapshot = ref => ({id: ref.id, ref, exists:docs.has(ref.path), data:()=>docs.get(ref.path), get:k=>docs.get(ref.path)?.[k]});
  function apply(ref, data, options) {
    const out = options?.merge ? {...docs.get(ref.path)} : {};
    for (const [k,v] of Object.entries(data)) {
      if (v?.del) delete out[k];
      else out[k] = v && typeof v === 'object' && 'inc' in v ? (out[k] || 0) + v.inc : v;
    }
    docs.set(ref.path, out);
  }
  function collection(p) {
    return {doc: id => ref(`${p}/${id || `id${++next}`}`), where: () => ({limit: () => ({query:true, prefix:p})})};
  }
  function ref(p) {
    return {path:p, id:p.split('/').at(-1), collection:k=>collection(`${p}/${k}`),
      get:async()=>snapshot(ref(p)), set:async(d,o)=>apply(ref(p),d,o)};
  }
  const db = {collection, runTransaction: fn => {
    const run = lock.then(async()=>{
      const writes=[];
      const tx = {get:async r=>{
        assert.equal(writes.length, 0, 'Firestore transaction reads must precede writes');
        if (r.query) return {empty:true,docs:[]};
        return snapshot(r);
      }, set:(...a)=>writes.push(a), update:(r,d)=>writes.push([r,d,{merge:true}]), create:(r,d)=>{
        assert.ok(!docs.has(r.path)); writes.push([r,d]);
      }};
      const result=await fn(tx); for(const args of writes) apply(...args); return result;
    });
    lock=run.catch(()=>{}); return run;
  }};
  class HttpsError extends Error {constructor(code,message,details){super(message);this.code=code;this.details=details;}}
  const admin={initializeApp(){},firestore:Object.assign(()=>db,{FieldValue,Timestamp})};
  const wrap=(...args)=>args.at(-1);
  const exports={};
  const customRequire = name => {
    if(name==='firebase-functions/v2/https') return {onCall:wrap,HttpsError};
    if(name==='firebase-functions/v2/firestore') return {onDocumentUpdated:wrap};
    if(name==='firebase-functions/v2/scheduler') return {onSchedule:wrap};
    if(name==='firebase-functions/params') return {defineSecret:()=>({value:()=> 'test-secret'}),defineString:()=>({value:()=> 'a'.repeat(32)})};
    if(name==='firebase-admin') return admin;
    if(name==='firebase-admin/firestore') return {getFirestore:()=>db};
    if(name==='razorpay') return class {};
    if(name==='agora-token') return {RtcRole:{PUBLISHER:1},RtcTokenBuilder:{buildTokenWithUid:(id,cert,ch,uid)=>`test:${ch}:${uid}`}};
    if(name==='crypto') return crypto;
    throw Error(`Unexpected dependency ${name}`);
  };
  vm.runInNewContext(fs.readFileSync(path.join(__dirname,'../index.js'),'utf8'),
    {require:customRequire,exports,process:{env:{}},console,Buffer,Date,fetch}, {filename:'index.js'});
  docs.set('providers/provider', {name:'Provider',status:'APPROVED',isVerified:true,isOnline:true});
  docs.set('users/seeker', {name:'Seeker',walletBalance:100000});
  const request=(uid,data,token={})=>({auth:uid?{uid,token}:null,data});
  const start=(mode='call',type='SESSION',extra={})=>exports.startSession(request('seeker',{providerId:'provider',type,mode,...extra}));
  return {docs,exports,request,start};
}

test('both apps share exactly one paid call, signalling ID and channel',async()=>{
 const h=harness(),s=await h.start();
 const session=h.docs.get(`sessions/${s.sessionId}`),call=h.docs.get(`active_calls/${s.sessionId}`);
 assert.equal(session.seekerId,call.callerId);assert.equal(call.providerId,'provider');
 assert.equal(session.channel,call.channelName);assert.equal(call.status,'Ringing');
 assert.equal(h.docs.get('users/seeker').walletBalance,50100);
 const a=await h.exports.getAgoraToken(h.request('seeker',{sessionId:s.sessionId}));
 const b=await h.exports.getAgoraToken(h.request('provider',{sessionId:s.sessionId}));
 assert.equal(a.channel,b.channel);assert.notEqual(a.uid,b.uid);assert.notEqual(a.token,b.token);
});
test('chat uses paid session room, no orphan signal and no voice token',async()=>{
 const h=harness(),s=await h.start('chat');
 assert.ok(h.docs.has(`chats/${s.sessionId}`));assert.ok(!h.docs.has(`active_calls/${s.sessionId}`));
 await assert.rejects(h.exports.getAgoraToken(h.request('provider',{sessionId:s.sessionId})),{code:'failed-precondition'});
});
test('unanswered call refunds once and earns nothing',async()=>{
 const h=harness(),s=await h.start();
 const end=()=>h.exports.endSession(h.request('provider',{sessionId:s.sessionId}));
 await Promise.all([end(),end(),end()]);
 assert.equal(h.docs.get('users/seeker').walletBalance,100000);
 assert.equal(h.docs.get(`sessions/${s.sessionId}`).refundedPaise,49900);
 assert.ok(!h.docs.has('providers/provider/wallet/summary'));
 assert.equal(h.docs.get(`active_calls/${s.sessionId}`).status,'Ended');
});
test('connected call settles 65 percent exactly once and displays rupees',async()=>{
 const h=harness(),s=await h.start();h.docs.get(`active_calls/${s.sessionId}`).connectedAt=Date.now();
 await Promise.all(['seeker','provider','provider'].map(uid=>h.exports.endSession(h.request(uid,{sessionId:s.sessionId}))));
 const wallet=h.docs.get('providers/provider/wallet/summary');
 assert.equal(wallet.balancePaise,32435);assert.equal(wallet.balance,324.35);
 assert.equal(h.docs.get('users/seeker').walletBalance,50100);
 assert.equal(h.docs.get(`providers/provider/transactions/session_${s.sessionId}`).amountPaise,32435);
});
test('auth, ownership, expiry and insufficient funds fail closed',async()=>{
 const h=harness();await assert.rejects(h.exports.startSession(h.request(null,{})),{code:'unauthenticated'});
 h.docs.get('users/seeker').walletBalance=0;await assert.rejects(h.start(),{code:'failed-precondition'});
 assert.equal([...h.docs.keys()].filter(k=>k.startsWith('sessions/')).length,0);
 h.docs.get('users/seeker').walletBalance=100000;const s=await h.start();
 for(const fn of ['getAgoraToken','endSession']) await assert.rejects(h.exports[fn](h.request('stranger',{sessionId:s.sessionId})),{code:'permission-denied'});
 await h.exports.endSession(h.request('seeker',{sessionId:s.sessionId}));
 await assert.rejects(h.exports.getAgoraToken(h.request('provider',{sessionId:s.sessionId})),{code:'failed-precondition'});
});
test('unverified or offline providers cannot charge seekers',async()=>{
 for(const patch of [{isVerified:false},{status:'Pending'},{isOnline:false},{disabled:true}]) {
   const h=harness();Object.assign(h.docs.get('providers/provider'),patch);
   await assert.rejects(h.start(),{code:'not-found'});assert.equal(h.docs.get('users/seeker').walletBalance,100000);
 }
});
test('one free trial per phone across accounts and idempotent extension debit',async()=>{
 const h=harness();const req=uid=>h.request(uid,{type:'DREAM_CHAT',mode:'chat',providerId:'provider'},{phone_number:'+919876543210'});
 const first=await h.exports.startSession(req('seeker'));assert.equal(first.isFreeTrial,true);
 h.docs.set('users/seeker2',{walletBalance:100000});const second=await h.exports.startSession(req('seeker2'));
 assert.equal(second.isFreeTrial,false);assert.equal(second.amountPaise,19900);
 const extend=()=>h.exports.extendSession(h.request('seeker',{sessionId:first.sessionId,idempotencyKey:'retry1'}));
 await extend();await extend();assert.equal(h.docs.get('users/seeker').walletBalance,90100);
 await h.exports.endSession(h.request('provider',{sessionId:first.sessionId}));
 assert.equal(h.docs.get('providers/provider/wallet/summary').balancePaise,9435);
});
test('answered question charges server price and pays once; client price ignored',async()=>{
 const h=harness();h.docs.set('questions/q',{userId:'seeker',providerId:'provider',status:'answered',earningAmount:999999});
 const event={params:{questionId:'q'},data:{before:{data:()=>({status:'pending'})},after:{data:()=>({status:'answered'})}}};
 await h.exports.settleQuestion(event);await h.exports.settleQuestion(event);
 assert.equal(h.docs.get('users/seeker').walletBalance,94900);
 assert.equal(h.docs.get('providers/provider/wallet/summary').balancePaise,3315);
 assert.equal(h.docs.get('questions/q').earningAmount,33.15);
});
