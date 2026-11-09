'use strict';
module.exports=({db,admin,FieldValue:F,Timestamp:T,HttpsError:E,requireUid})=>({
  async request(request) {
    const uid=requireUid(request),authTime=request.auth.token.auth_time;
    if(request.data?.confirm!=='DELETE')throw new E('invalid-argument','Type DELETE to confirm.');
    if(!Number.isFinite(authTime) || authTime>Date.now()/1000+30 || Date.now()/1000-authTime>300)throw new E('failed-precondition','Verify your phone again before deleting.');
    const job=db.doc(`accountDeletionJobs/${uid}`),provider=db.doc(`providers/${uid}`);
    await db.runTransaction(async tx=>{
      const [j,p,w,lock,user]=await Promise.all([tx.get(job),tx.get(provider),tx.get(db.doc(`providers/${uid}/wallet/summary`)),tx.get(db.doc(`sessionLocks/${uid}`)),tx.get(db.doc(`users/${uid}`))]);
      if(j.exists)return;
      if(!p.exists)throw new E('permission-denied','Provider profile required.');
      if(lock.exists)throw new E('failed-precondition','Finish your pending or active session first.');
      if(['balance','pendingPayout','pendingBalance'].some(k=>Number(w.get(k)||0)!==0) || ['walletBalance','reservedBalance'].some(k=>Number(user.get(k)||0)!==0))throw new E('failed-precondition','Settle wallet balances and payouts first.');
      const pending=await tx.get(provider.collection('payout_requests').where('status','==','pending').limit(1));
      const legacy=await tx.get(db.collection('sessions').where('providerId','==',uid).where('status','in',['pending','active','accepted']).limit(1));
      if(!pending.empty || !legacy.empty)throw new E('failed-precondition','Finish pending sessions and payouts.');
      tx.set(provider,{deletionPending:true,isOnline:false,isAvailable:false},{merge:true});
      if(user.exists)tx.update(user.ref,{deletionPending:true});
      tx.create(job,{status:'queued',requestedAt:F.serverTimestamp(),reason:String(request.data.reason||'').slice(0,500)});
    });
    return {accepted:true,status:'queued'};
  },
  async process(event) {
    const uid=event.params.uid,job=db.doc(`accountDeletionJobs/${uid}`);
    if((await job.get()).get('status')==='completed')return;
    await job.set({status:'processing',updatedAt:F.serverTimestamp()},{merge:true});
    try {await admin.auth().updateUser(uid,{disabled:true});await admin.auth().revokeRefreshTokens(uid);}catch(e){if(e.code!=='auth/user-not-found')throw e;}
    const provider=db.doc(`providers/${uid}`),archive=db.doc(`deleted_providers/${uid}`);
    await archive.set({deletedAt:F.serverTimestamp(),retainUntil:T.fromMillis(Date.now()+8*365*86400000)},{merge:true});
    // Archive before removing anything. Retrying never replaces the archive with an empty record.
    for(const collection of ['transactions','payout_requests','wallet']) {
      let cursor;
      while(true){
        let q=provider.collection(collection).orderBy('__name__').limit(200);
        if(cursor)q=q.startAfter(cursor);
        const page=await q.get();if(page.empty)break;
        const batch=db.batch();
        for(const doc of page.docs){
          const {name,phone,email,photo,upiId,accountNumber,ifsc,accountHolder,destination,...financial}=doc.data();
          batch.set(archive.collection(collection).doc(doc.id),financial);
        }
        await batch.commit();cursor=page.docs.at(-1);
      }
    }
    for(const coll of ['sessions','active_calls','questions','reviews','remedies','verification_requests','admin_alerts']) {
      let cursor;
      while(true){
        let q=db.collection(coll).where('providerId','==',uid).orderBy('__name__').limit(200);
        if(cursor)q=q.startAfter(cursor);
        const page=await q.get();if(page.empty)break;
        const batch=db.batch();
        for(const doc of page.docs){
          if(['verification_requests','admin_alerts'].includes(coll)){batch.delete(doc.ref);continue;}
          const changes={providerName:'Deleted account'};
          for(const k of ['providerPhone','providerEmail','phone','email'])changes[k]=F.delete();
          if(coll==='questions' && ['pending','assigned'].includes(doc.get('status')))Object.assign(changes,{status:'rejected',rejectReason:'provider_deleted'});
          batch.update(doc.ref,changes);
        }
        await batch.commit();cursor=page.docs.at(-1);
      }
    }
    await admin.storage().bucket().deleteFiles({prefix:`providers/${uid}/`});
    await db.recursiveDelete(provider);
    await db.recursiveDelete(db.doc(`sadhaks/${uid}`));
    await db.recursiveDelete(db.doc(`payout_destinations/${uid}`));
    // Preserve seeker financial history if this identity used both apps; scrub direct identifiers.
    const user=db.doc(`users/${uid}`);
    if((await user.get()).exists)await user.set({name:'Deleted account',phone:F.delete(),email:F.delete(),fcmToken:F.delete(),deletionPending:true},{merge:true});
    try{await admin.auth().deleteUser(uid);}catch(e){if(e.code!=='auth/user-not-found')throw e;}
    await job.set({status:'completed',completedAt:F.serverTimestamp()},{merge:true});
    await db.doc(`account_deletions/${uid}`).set({uid,status:'completed',deletedAt:F.serverTimestamp()});
  }
});
