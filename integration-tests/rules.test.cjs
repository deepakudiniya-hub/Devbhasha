const {test,before,after,beforeEach}=require('node:test');
const fs=require('node:fs');
const path=require('node:path');
const {initializeTestEnvironment,assertSucceeds,assertFails}=require('@firebase/rules-unit-testing');
const {doc,setDoc,updateDoc,getDoc,collection,getDocs,query,where,Timestamp}=require('firebase/firestore');
let env;
const db=uid=>env.authenticatedContext(uid).firestore();
before(async()=>{env=await initializeTestEnvironment({projectId:'demo-devbhasha',firestore:{rules:fs.readFileSync(path.join(__dirname,'../firestore.rules'),'utf8')}});});
after(async()=>{await env?.cleanup();});
beforeEach(async()=>{
 await env.clearFirestore();
 await env.withSecurityRulesDisabled(async ctx=>{
  const d=ctx.firestore();
  await Promise.all([
   setDoc(doc(d,'providers/provider'),{name:'Provider',status:'APPROVED',isVerified:true,isOnline:true}),
   setDoc(doc(d,'users/seeker'),{walletBalance:100000}),
   setDoc(doc(d,'sessions/session'),{seekerId:'seeker',providerId:'provider',participants:['seeker','provider'],status:'active',endsAt:Timestamp.fromMillis(Date.now()+600000)}),
   setDoc(doc(d,'chats/session'),{seekerId:'seeker',providerId:'provider',participants:['seeker','provider']}),
   setDoc(doc(d,'active_calls/session'),{callerId:'seeker',providerId:'provider',status:'Ringing'}),
   setDoc(doc(d,'questions/question'),{userId:'seeker',providerId:'provider',status:'pending',questionText:'Question'}),
   setDoc(doc(d,'providers/provider/wallet/summary'),{balancePaise:100000,balance:1000})
  ]);
 });
});
test('paired participants exchange the exact Android wire messages',async()=>{
 for(const uid of ['seeker','provider']) {
  await assertSucceeds(setDoc(doc(db(uid),`chats/session/messages/${uid}`),{id:uid,senderId:uid,senderName:uid,text:'Hello',isFromUser:uid==='seeker',timestamp:Date.now()}));
  await assertSucceeds(getDocs(collection(db(uid),'chats/session/messages')));
 }
 await assertFails(getDocs(collection(db('stranger'),'chats/session/messages')));
 await assertFails(setDoc(doc(db('seeker'),'chats/session/messages/spoof'),{senderId:'provider',text:'Forged'}));
});
test('provider discovers only its chat rooms; stranger cannot list everyone',async()=>{
 await assertSucceeds(getDocs(query(collection(db('provider'),'chats'),where('participants','array-contains','provider'))));
 await assertFails(getDocs(collection(db('stranger'),'chats')));
});
test('messages stop at server session expiry and cannot be altered',async()=>{
 const message=doc(db('seeker'),'chats/session/messages/one');
 await assertSucceeds(setDoc(message,{senderId:'seeker',text:'Hello',timestamp:Date.now()}));
 await assertFails(updateDoc(message,{text:'Changed'}));
 await env.withSecurityRulesDisabled(ctx=>updateDoc(doc(ctx.firestore(),'sessions/session'),{endsAt:Timestamp.fromMillis(Date.now()-1000)}));
 await assertFails(setDoc(doc(db('provider'),'chats/session/messages/late'),{senderId:'provider',text:'Late'}));
});
test('money, paid sessions and provider approval cannot be forged by either app',async()=>{
 await assertFails(updateDoc(doc(db('seeker'),'users/seeker'),{walletBalance:999999}));
 await assertFails(updateDoc(doc(db('provider'),'providers/provider/wallet/summary'),{balancePaise:999999}));
 await assertFails(updateDoc(doc(db('provider'),'providers/provider'),{isVerified:false,status:'Pending'}));
 await assertFails(setDoc(doc(db('seeker'),'sessions/free'),{participants:['seeker','provider'],status:'active'}));
 await assertFails(setDoc(doc(db('seeker'),'active_calls/fake'),{callerId:'seeker',providerId:'provider',status:'ringing'}));
 await assertFails(setDoc(doc(db('provider'),'providers/provider/payout_requests/fake'),{status:'pending',amountPaise:50000}));
});
test('provider can accept its call and answer only the assigned question',async()=>{
 await assertSucceeds(updateDoc(doc(db('provider'),'active_calls/session'),{status:'Connected',connectedAt:Date.now(),durationSeconds:0}));
 await assertFails(updateDoc(doc(db('stranger'),'active_calls/session'),{status:'Connected',connectedAt:Date.now()}));
 await assertFails(updateDoc(doc(db('seeker'),'active_calls/session'),{connectedAt:Date.now()+10}));
 await assertSucceeds(updateDoc(doc(db('provider'),'questions/question'),{answer:'Answer',status:'answered',answeredAt:Timestamp.now()}));
 await assertFails(updateDoc(doc(db('stranger'),'questions/question'),{answer:'Forged',status:'answered'}));
});
test('account-scoped history and directory updates satisfy the actual rules',async()=>{
 await assertSucceeds(getDocs(query(collection(db('provider'),'active_calls'),where('providerId','==','provider'))));
 await assertSucceeds(getDocs(query(collection(db('seeker'),'active_calls'),where('callerId','==','seeker'))));
 await assertFails(getDocs(collection(db('provider'),'active_calls')));
 await assertSucceeds(updateDoc(doc(db('provider'),'providers/provider'),{isOnline:false,availabilityStatus:'Offline'}));
 await assertFails(getDoc(doc(db('stranger'),'users/seeker')));
});

test('provider consent is writable only by its owner; private verification profile is not public',async()=>{
 const consent={uid:'provider',type:'data_processing',version:1,approved:true,approvedAt:Timestamp.now(),platform:'android',appVersion:'46.0'};
 await assertSucceeds(setDoc(doc(db('provider'),'users/provider/consents/data_processing'),consent));
 await assertFails(setDoc(doc(db('stranger'),'users/provider/consents/data_processing'),consent));
 await assertSucceeds(getDoc(doc(db('provider'),'providers/provider')));
 await assertFails(getDoc(doc(db('seeker'),'providers/provider')));
});

test('consultation requests cannot be forged or reassigned by clients',async()=>{
 const requestRef=doc(db('seeker'),'consultation_requests/request');
 await assertFails(setDoc(requestRef,{userId:'seeker',providerId:'provider',status:'accepted'}));
 await env.withSecurityRulesDisabled(ctx=>setDoc(doc(ctx.firestore(),'consultation_requests/request'),{
  userId:'seeker',providerId:'provider',status:'pending'
 }));
 await assertFails(updateDoc(requestRef,{providerId:'stranger',status:'accepted'}));
 await assertFails(updateDoc(doc(db('provider'),'consultation_requests/request'),{status:'completed'}));
 await assertFails(updateDoc(doc(db('stranger'),'consultation_requests/request'),{status:'accepted'}));
});
