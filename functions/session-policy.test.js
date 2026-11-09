const {test}=require('node:test');const assert=require('node:assert/strict');
const {settlement,eligibleProvider}=require('./session-policy');
const base={type:'CHAT_PER_MIN',reservedPaise:5000,pricePerMinPaise:1000,amountPaise:0,providerAccepted:true,startedAt:100000,endsAt:400000};
test('no-show returns every reserved paise and earns nothing',()=>assert.deepEqual(settlement({...base,providerAccepted:false,startedAt:null},200000),{charged:0,refund:5000,minutes:0,payout:0}));
test('per-minute billing rounds up and refunds unused balance',()=>assert.deepEqual(settlement(base,161000),{charged:2000,refund:3000,minutes:2,payout:1200}));
test('late settlement cannot charge past purchased duration',()=>assert.equal(settlement(base,999999).charged,5000));
test('free trial requires two-way interaction and duration',()=>{
 const trial={...base,isFreeTrial:true,reservedPaise:0};
 assert.equal(settlement(trial,200000).payout,0);
 assert.equal(settlement({...trial,seekerParticipated:true,providerParticipated:true},150000).payout,0);
 assert.equal(settlement({...trial,seekerParticipated:true,providerParticipated:true},200000).payout,5000);
});
test('pending call connection has no charge even after provider accepts',()=>assert.equal(settlement({...base,startedAt:null},200000).charged,0));
test('provider must be approved verified online and not deleting',()=>{
 const p={status:'APPROVED',isVerified:true,isOnline:true};assert.equal(eligibleProvider(p),true);
 for(const change of [{status:'PENDING'},{isVerified:false},{isOnline:false},{isAvailable:false},{blocked:true},{disabled:true},{deletionPending:true}])assert.equal(eligibleProvider({...p,...change}),false);
});
