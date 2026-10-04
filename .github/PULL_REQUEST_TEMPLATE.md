## What changed

<!-- One or two sentences. Link the issue if there is one. -->

## Why

<!-- The problem this solves. For security work, name the finding. -->

## Type

- [ ] Feature
- [ ] Bug fix
- [ ] **Security** (touches auth, payments, wallet, secrets, or data access)
- [ ] Refactor / chore
- [ ] Docs

## Checklist

- [ ] I read the real code before changing it; one owner per change.
- [ ] `gradle assembleDebug` builds locally.
- [ ] Unit tests pass (`gradle testDebugUnitTest`).
- [ ] New behaviour has a test (money paths and auth are mandatory).
- [ ] No secrets in code, logs, or config — keys live in `.env` / Functions secrets.

### Security-specific (required if this PR is marked Security)

- [ ] No client-authoritative writes to money or verification fields.
- [ ] Firestore rules updated if the data model changed.
- [ ] Payment changes verify the Razorpay signature server-side.
- [ ] RTC changes use server-issued tokens (no empty-token joins).

## Screenshots / evidence

<!-- For UI changes, attach Roborazzi screenshots. For payment flows, note the test. -->
