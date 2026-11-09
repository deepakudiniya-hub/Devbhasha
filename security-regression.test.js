const {initializeTestEnvironment,assertFails,assertSucceeds}=require('@firebase/rules-unit-testing');
const {test,before,after,beforeEach}=require('node:test');const fs=require('fs');let env;
before(async()=>{const [host,port]=process.env.FIRESTORE_EMULATOR_HOST.split(':');env=await initializeTestEnvironment({projectId:'demo-rules-regression',firestore:{host,port:Number(port),rules:fs.readFileSync('firestore.rules','utf8')}});});
after(()=>env.cleanup());beforeEach(()=>env.clearFirestore());
const auth=uid=>env.authenticatedContext(uid,{firebase:{sign_in_provider:'phone'}}).firestore();
test('self approval and forged consent blocked on both profile paths',async()=>{
 const db=auth('p');await assertSucceeds(db.doc('providers/p').set({name:'P',status:'PENDING'}));
 await assertFails(db.doc('providers/p').update({status:'APPROVED',isVerified:true}));
 await assertFails(db.doc('providers/p').update({acceptedAgreementVersion:'2.0'}));
 await assertFails(db.doc('sadhaks/p').set({status:'APPROVED',isVerified:true}));
});
test('canonical KYC batch succeeds but client admin alert injection is denied',async()=>{
 const db=auth('p'),batch=db.batch();batch.set(db.doc('providers/p'),{name:'P',status:'PENDING'});
 batch.set(db.doc('providers/p/private/verification'),{idProofUrl:'gs://bucket/providers/p/private/doc.jpg'});
 batch.set(db.doc('verification_requests/r'),{providerId:'p',providerName:'P',status:'PENDING',docType:'ID',confirmationId:'r',submittedAt:new Date()});
 await assertSucceeds(batch.commit());
 await assertFails(db.doc('admin_alerts/fake').set({providerId:'p'}));
 await assertFails(db.doc('providers/p/notifications/fake').set({isRead:false}));
});
test('approved KYC immutable, contact remains editable',async()=>{
 await env.withSecurityRulesDisabled(async c=>{await c.firestore().doc('providers/p').set({status:'APPROVED',isVerified:true});});
 const db=auth('p');await assertFails(db.doc('providers/p/private/verification').set({idProofUrl:'changed'}));
 await assertSucceeds(db.doc('providers/p/private/contact').set({phone:'123'}));
});
test('wallet holds, calls, content versions and linked questions are server owned',async()=>{
 const db=auth('u');await assertFails(db.doc('users/u').set({reservedBalance:999}));
 await assertFails(db.doc('active_calls/c').set({callerId:'u',providerId:'p',status:'ringing'}));
 await assertFails(db.doc('questions/q').set({userId:'u',providerId:'p',status:'pending',sessionId:'fake'}));
 const admin=env.authenticatedContext('admin',{admin:true,firebase:{sign_in_provider:'password'}}).firestore();
 await assertFails(admin.doc('app_content/home').set({text:'bypass'}));
 await assertFails(admin.doc('payout_destinations/p/bank/current').get());
});

test('admin can read operating queues and financial summaries, never raw payout destinations',async()=>{
 const admin=env.authenticatedContext('admin',{admin:true,firebase:{sign_in_provider:'password'}}).firestore();
 await env.withSecurityRulesDisabled(async c=>{const db=c.firestore();await db.doc('users/u').set({name:'U'});await db.doc('providers/p/wallet/summary').set({balance:100});});
 await assertSucceeds(admin.doc('users/u').get());await assertSucceeds(admin.doc('providers/p/wallet/summary').get());
 await assertSucceeds(admin.collection('verification_requests').get());await assertFails(admin.doc('payout_destinations/p/bank/current').get());
});
