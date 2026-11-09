# Deployment and integration checklist

## Shared ownership

- Project: `devbhasha-d9e22`.
- Database: `ai-studio-android-devbhash-c2b59e88-80f5-4209-81ce-859c3cb917d9`.
- Region: `us-central1`.
- Main backend: `devbhasha/functions`, codebase `default`. Owns payments, sessions, payout, agreement, deletion and admin content/approval.
- Companion backend: `devsadhak/functions-security`, codebase `devsadhak-security`. Owns reviews and KYC notification trigger only.
- Deploy Firestore/indexes/Storage from Devbhasha only. The copies in DevSadhak are for review and must remain byte-identical.

## Required configuration

- Node 22, Java 21 for test tooling, Android SDK 36 and the included Gradle wrappers.
- Set the standard Firebase secrets `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, `AGORA_APP_CERTIFICATE` and the NEW `RAZORPAY_WEBHOOK_SECRET`.
- Set `AGORA_APP_ID`. If setting a database env value, use `FIRESTORE_DB_ID` with the exact named database above. A different non-emulator ID fails startup.
- Register BOTH Android apps with Firebase App Check / Play Integrity and the correct release certificate fingerprints. Use registered debug tokens only in debug builds.
- Register the admin web app with App Check / reCAPTCHA Enterprise. Set `VITE_RECAPTCHA_ENTERPRISE_SITE_KEY` before `npm run build`; an unconfigured production panel fails closed.
- Configure Storage CORS for the actual admin HTTPS origin (`storage.cors.example.json`). Authenticated `getBlob` previews need it. Do not use a public bucket or make KYC objects publicly readable to bypass CORS.
- Configure Razorpay webhook URL for the deployed `razorpayWebhook` function, subscribe to `payment.captured` / `order.paid`, and set the same webhook secret. Test signature failure and duplicate delivery in staging.

## Safe rollout order

1. Back up the database and deployed configuration using your existing operational process. Enable maintenance while switching the session protocol.
2. Finish/settle existing active sessions. The main backend preserves a legacy settlement path for pre-reservation session documents, but new sessions use the new protocol. Do not delete locks manually for unsettled sessions.
3. Inspect deployed function ownership. If `secureRequestAccountDeletion` / `secureProcessAccountDeletion` belong to `devsadhak-security`, finish existing deletion jobs and migrate those endpoints to `default`; remove only the obsolete duplicate implementation during the maintenance window. The updated companion source no longer exports them. Never run both old and new deletion workers together.
4. Run the existing provider contact/flag migration scripts after reviewing their dry-run/options. Review legacy approval flags with real KYC evidence; do not bulk approve providers. Review old `documents` / `admin_notifications` queues if populated; new submissions use canonical collections. Preserve old records until reconciled.
5. Deploy the updated main and companion functions, named-database indexes, rules and Storage rules. Verify the configured project/database before each deploy.
6. Install the new Android builds and admin panel in staging. Old clients that omit a start-session idempotency key or do not send App Check tokens are intentionally incompatible with the tightened backend. Enforce a minimum app version for the coordinated rollout.
7. Run end-to-end tests and then reopen service. Monitor pending sessions, settlement errors, webhook failures, rejected App Check requests and deletion jobs.

## Test commands

From Devbhasha root:

```sh
npm ci
npm test
npm run test:rules
cd functions
npm ci
npm run check
npm audit --omit=dev
```

From admin panel:

```sh
npm ci
npm run lint
npm run build
npm audit --omit=dev
```

From DevSadhak:

```sh
python checks/test_no_auth_bypass.py
python checks/test_login_agreement_ui_contract.py
cd functions-security
npm ci
npm test
npm run check
```

Android: use `gradlew.bat :app:assembleDebug` on Windows or `./gradlew :app:assembleDebug` elsewhere, then the signed release/R8 build with your own release keystore. No release credentials are included.

## Session and deletion details

`startSession` requires `{type,providerId,idempotencyKey,questionText?}` and returns the canonical session ID. A 120-second pending deadline reserves funds; one participant can have only one pending/active session. Chat acceptance activates billing. Calls pass through `accepted`; both RTC peers call `confirmSessionConnection` before billing starts. Settlement credits earnings and releases unused funds atomically. A one-minute sweeper settles abandoned sessions.

Wallet `walletBalance` is available balance; `reservedBalance` is held balance. Start/release ledger entries net to the final charge. Quoted provider rates are integer rupees; ledger/balance amounts are integer paise.

Deletion returns `{accepted:true,status:"queued"}`. Sign out immediately. `accountDeletionJobs/{uid}` is the durable progress record; failed workers retry. Archives under `deleted_providers` preserve financial history and carry `retainUntil`; this timestamp is not an automatic purge guarantee. The worker retains shared consultation records and removes direct profile identifiers.
