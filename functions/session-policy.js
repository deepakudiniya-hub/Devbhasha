'use strict';
function eligibleProvider(p) {
  return !!p && p.status === 'APPROVED' && p.isVerified === true && p.isOnline === true
    && p.isAvailable !== false && !p.blocked && !p.disabled && !p.deletionPending;
}
function settlement(s, now) {
  const ms = v => v?.toMillis?.() || Number(v) || 0;
  const started = ms(s.startedAt);
  const elapsed = started ? Math.max(0, Math.min(now, ms(s.endsAt)) - started) : 0;
  const used = s.providerAccepted === true && started > 0;
  const perMinute = ['CHAT_PER_MIN','CALL_PER_MIN'].includes(s.type);
  const minutes = used ? Math.max(1, Math.ceil(elapsed / 60000)) : 0;
  const held = Number.isSafeInteger(s.reservedPaise) ? s.reservedPaise : 0;
  const charged = !used || s.isFreeTrial ? 0 : Math.min(held, perMinute ? minutes * s.pricePerMinPaise : s.amountPaise);
  // Free trial subsidy requires evidence of two-way participation and a minimum minute.
  const subsidy = s.isFreeTrial && used && elapsed >= 60000 && s.seekerParticipated && s.providerParticipated ? 5000 : 0;
  const share = Number.isInteger(s.sharePercent) && s.sharePercent >= 0 && s.sharePercent <= 100 ? s.sharePercent : 60;
  return {charged, refund: held - charged, minutes, payout: subsidy || Math.floor(charged * share / 100)};
}
module.exports = {eligibleProvider, settlement};
