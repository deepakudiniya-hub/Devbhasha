const {test,beforeEach,after}=require('node:test');const assert=require('node:assert/strict');
if(!process.env.FIRESTORE_EMULATOR_HOST)throw Error('Emulator required');
const sdk=require('firebase-admin');const app=sdk.initializeApp({projectId:'demo-deletion'},'deletion-test');const db=app.firestore();
class E extends Error{constructor(code,message){super(message);this.code=code;}}
let deletedAuth=0;
const admin={auth:()=>({updateUser:async()=>{},revokeRefreshTokens:async()=>{},deleteUser:async()=>{deletedAuth++;}}),storage:()=>({bucket:()=>({deleteFiles:async()=>{}})})};
const service=require('./deletion')({db,admin,FieldValue:sdk.firestore.FieldValue,Timestamp:sdk.firestore.Timestamp,HttpsError:E,requireUid:r=>r.auth.uid});
const request=()=>({auth:{uid:'p',token:{auth_time:Date.now()/1000}},data:{confirm:'DELETE'}});
beforeEach(async()=>{deletedAuth=0;for(const name of ['providers','users','sadhaks','sessions','sessionLocks','accountDeletionJobs','deleted_providers','account_deletions'])await db.recursiveDelete(db.collection(name));await db.doc('providers/p').set({status:'APPROVED'});});
after(()=>app.delete());
test('deletion refuses stale auth, active sessions and nonzero balances',async()=>{
 const old=request();old.auth.token.auth_time-=1000;await assert.rejects(service.request(old),{code:'failed-precondition'});
 await db.doc('sessionLocks/p').set({sessionId:'s'});await assert.rejects(service.request(request()),{code:'failed-precondition'});
 await db.doc('sessionLocks/p').delete();await db.doc('providers/p/wallet/summary').set({balance:500});await assert.rejects(service.request(request()),{code:'failed-precondition'});
});
test('durable deletion archives financial records, scrubs PII and is retry safe',async()=>{
 await db.doc('providers/p/wallet/summary').set({balance:0,pendingPayout:0,totalEarnings:9000});
 await db.doc('providers/p/transactions/t').set({amountPaise:9000,phone:'private',accountNumber:'secret'});
 const result=await service.request(request());assert.equal(result.status,'queued');assert.equal((await db.doc('providers/p').get()).get('deletionPending'),true);
 await service.process({params:{uid:'p'}});await service.process({params:{uid:'p'}});
 assert.equal(deletedAuth,1);assert.equal((await db.doc('providers/p').get()).exists,false);
 const archived=(await db.doc('deleted_providers/p/transactions/t').get()).data();assert.deepEqual(archived,{amountPaise:9000});
 assert.equal((await db.doc('deleted_providers/p/wallet/summary').get()).get('totalEarnings'),9000);
 assert.equal((await db.doc('accountDeletionJobs/p').get()).get('status'),'completed');
});
