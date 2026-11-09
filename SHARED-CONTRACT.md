# Shared contract — 2026-11-09

This file supersedes the old contract. See the deployment guide for rollout requirements.

- Project/database/region and function ownership: `DEPLOYMENT_2026-11-09.md`.
- Money: all balances, reservations, ledgers, earnings and payment amounts are integer paise. Provider `chatPricePerMin`/`callPricePerMin` are integer INR 5–100; `sessionPrice30Min` is integer INR 199–2999.
- Verification: `verification_requests/{requestId}`, status `PENDING`, backend-reviewed to `VERIFIED`/`REJECTED`; `admin_alerts/{id}` is backend-owned. Provider approval is `status:APPROVED,isVerified:true` and cannot be self-written.
- Public profiles: `providers/{uid}`, `sadhaks/{uid}`. Private contact, device and KYC: `providers/{uid}/private/*`; bank destination is function-only under `payout_destinations`.
- Sessions: server-created `sessions/{id}`, `chats/{id}` and for calls `active_calls/{id}` share one ID. States: `pending -> active` (chat), `pending -> accepted -> active` (call), then `ended` or `cancelled_noshow`. Calls require both connection confirmations. All session state/money mutations use backend functions.
- Call signalling may update participant-owned status/time fields; it cannot authoritatively activate billing or create a paid session.
- Chat text: 1–4000 characters, sender must equal auth UID, TTL is 365 days with a 10-minute clock allowance. Pending seekers can introduce themselves; provider first reply accepts the session. Subsidies require two-way participation and a minimum elapsed minute.
- `startSession` requires an idempotency key. Participant locks serialize concurrent starts. `settleSession` is idempotent and handles no-show refunds/reservation release.
- Dream question metadata is passed to `startSession`; clients cannot directly create linked session/questions or bypass billing with forged `sessionId/type/kind` fields.
- `secureRequestAccountDeletion` and its worker live in main backend only. Queued response signs the client out; archive/purge work is asynchronous.
- Main callable transport enforces App Check, nonanonymous auth and per-user throttling. Admin actions require the admin custom claim.
- Main backend versioned agreement defaults to `2.0`; provider UI submits that version. A new version requires matching text/client release and minimum-version rollout.
- Payment callback and signed Razorpay webhook share one idempotent captured-payment transaction.
- Rules are identical in both Android packages; only Devbhasha deploys them.
