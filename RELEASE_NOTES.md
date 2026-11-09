# Devbhasha / DevSadhak / Admin fixes — 9 November 2026

This is a source-code repair release. It has NOT been deployed to Firebase or published as an APK. No production records were changed.

## Fixed in source

- One identical Firestore/Storage ruleset in both Android projects. Devbhasha owns deployment of those rules.
- Providers cannot self-approve, self-verify, forge consent, change protected financial fields, or overwrite approved KYC documents.
- KYC app and admin panel now use `verification_requests` and `admin_alerts`. Only the backend generates admin alerts and confirmation notifications. Admin approval updates provider, public profile and queue item in one transaction.
- Private `gs://` documents are loaded by the admin preview with authenticated Storage access. No public download token is created by the preview.
- One canonical server-created session/chat/call ID; removed duplicate client session and call creation.
- Idempotent starts, atomic participant locks and integer-paise wallet reservations. Concurrent sessions cannot reserve the same participant/balance.
- Provider must be approved, verified, available, online and have accepted the current agreement.
- Chat charging starts after provider acceptance. Call charging starts after BOTH participants confirm RTC connection. Pending/no-show sessions are refunded without provider earnings.
- Per-minute settlement releases unused reservation. Repeated/concurrent settlement cannot double-credit. New sessions are capped at 120 minutes for per-minute reservations.
- Free-trial subsidy requires two-way chat participation and at least one minute; an unaccepted no-show releases the trial claim.
- Only assigned provider can accept a call/session; expired/settled sessions cannot reopen.
- Short-lived RTC tokens with renewal limit post-session access to the token lifetime (up to 120 seconds). Immediate server-side Agora channel revocation is not implemented.
- Account deletion has ONE owner: main backend. Durable queue, active-session/payout/balance checks, immediate account write freeze, retry-safe worker, financial-record archive, PII cleanup and correct queued UI response.
- Razorpay raw-body signed webhook uses the same idempotent captured-payment credit transaction as the app callback. A second payment ID cannot credit an already paid order.
- App Check enforced on callable functions; Play Integrity added to seeker app; admin web App Check setup added. Main callable endpoints have a per-account request limit.
- Database configuration cannot silently select a different/default production database.
- Consent bypasses returning `true` removed, per-user agreement version restored, outdated consent versions rejected server-side.
- Dream submission no longer reports success after a billing failure or attempts unauthorized session writes.
- Fake online/verified providers and sample dream history removed from the real Home state.
- Admin content changes and version snapshots saved atomically by a callable with an audit record.
- Fixed admin JSX/type errors, missing CSV handler, unsafe missing-field assumptions in session lists, and mapped server field names/statuses.
- Fixed Kotlin imports before package declarations, selected leaked listeners, default-database usage, Agora token renewal, and a nonexistent custom debug-keystore dependency.
- Added Gradle wrappers (Devbhasha 9.3.1; DevSadhak 8.9). Seeker compile/target SDK aligned to installed/available API 36; the SDK repository did not expose API 37 during validation.
- Runtime dependency locks updated: main backend Firebase Admin 14.5.0 / Functions 7.4.0; admin panel grpc-js 1.14.7, re2js 2.9.1, esbuild 0.28.2. Panel lock synchronization was checked with `npm ci --dry-run --offline`.

## Validation actually completed

| Check | Result |
|---|---|
| Firestore emulator: rules + session transactions + deletion archive | 41 passed |
| Session/review policy tests | 10 passed |
| DevSadhak login/agreement UI source-contract checks | 13 passed |
| DevSadhak auth-bypass scan | Passed |
| Backend JavaScript syntax checks | Passed |
| Admin TypeScript check and Vite production build | Passed with the locally installed dependency set |
| Patched panel lockfile synchronization | Passed dry-run |
| Android Gradle compilation | Blocked while resolving external Gradle plugins |
| New dependency versions: fresh install and repeat security audit | Not completed; dependency downloads were blocked |
| Production Firebase/Storage/App Check/Webhook configuration | Not deployed or verified |
| Physical-device OTP, KYC, payments, call reconnect, release/R8 tests | Not run |

The build/tests ran with the dependencies available locally before the final security dependency-lock upgrades. The final upgraded dependency set still requires a clean install and retest. Do not treat the earlier dependency audit as a clean final audit. No claim is made that every possible defect or vulnerability is eliminated.

## Before release

1. Read `DEPLOYMENT_2026-11-09.md` and complete the staged rollout.
2. Use Node 22. Run clean installs, repeat tests, and audit production dependencies on a network-enabled machine.
3. Build both Android projects with their included wrappers in Android Studio; resolve any compiler/dependency compatibility errors before publishing. Neither APK is included or claimed to be build-verified.
4. Test both applications and panel together against staging: KYC approval/rejection, profile/prices, first paid recharge, lost payment callback, chat/call acceptance, failed connection, no-show, concurrent starts, duplicate settlement, payout and deletion.
5. Existing deployed rules/functions and already stored data are not changed by downloading these ZIPs. Follow the migration instructions; do not deploy the old duplicate deletion worker.
