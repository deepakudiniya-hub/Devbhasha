const admin=require("firebase-admin");const {getFirestore}=require("firebase-admin/firestore");
admin.initializeApp({projectId:"devbhasha-d9e22"});
async function scan(id){const db=id==="(default)"?getFirestore():getFirestore(id);const cs=await db.listCollections();const out=[];for(const c of cs){const n=(await c.count().get()).data().count;out.push(c.id+":"+n)}return id+" => "+(out.join(", ")||"EMPTY")}
(async()=>{for(const id of ["(default)","devbhasha-d9e22"]){try{console.log(await scan(id))}catch(e){console.log(id,"ERR",e.message)}}process.exit(0)})();
