# Devbhasha & Devsadhak Shared Backend & Data Contract

This document defines the shared architecture, Firebase configuration, collection schemas, and backend settlement contracts between **Devbhasha** (Seeker client / backend owner) and **Devsadhak** (Provider / Sadhak companion client).

---

## 1. Firebase Project Configuration

- **Shared Firebase Project ID:** `devbhasha-d9e22`
- **Project Number:** `1059256538774`
- **Firestore Database ID:** `devbhasha-d9e22`
- **Client App Packages (`google-services.json`):**
  - **Devbhasha (Seeker App):** `com.aistudio.provider.xkpwd` / `com.aistudio.devbhasha.kxmpzq`
  - **Devsadhak (Provider App):** `com.aistudio.provider.xkpwd`

---

## 2. Universal Financial & Money Standard

- **Currency Base Unit:** Indian Rupees (INR)
- **Denomination Standard:** **Integer Paise** (1 INR = 100 paise; e.g. ₹50.00 = `5000` paise).
- **Floating Point Prohibition:** No currency values may be stored as floating-point numbers. All balances, deductions, rates, and earnings are strictly non-negative integers (`Number.isInteger`).
- **Server Authority:** Clients are untrusted. All wallet modifications, transaction records, and settlements are strictly executed server-side via Cloud Functions (Admin SDK).

---

## 3. Firestore Collections & Document Schemas

### A. `/users/{uid}` (Seeker Profile & Wallet)
- **Role:** Seeker identity, wallet balance, and referral tracking.
- **Security:** `isSelf(uid)` read; server-only write (`allow write: if false;`).
- **Fields:**
  - `uid` (string): Seeker authentication UID.
  - `name` (string): Full name.
  - `phone` (string): Contact number.
  - `email` (string): User email address.
  - `walletBalance` (integer paise): Server-authoritative available balance.
  - `referralCode` (string): Unique code assigned to the user.
  - `referredBy` (string, optional): Referrer UID or referral code.
  - `referralBonusClaimed` (boolean): Flag for signup/referral bonus state.
  - `createdAt` (timestamp): Account creation timestamp.
  - `updatedAt` (timestamp): Last profile update.

### B. `/sadhaks/{sadhakId}` (Sadhak Directory & Profile)
- **Role:** Sadhak public listings and consultation metadata.
- **Security:** Signed-in read (`allow read: if isSignedIn();`). Sadhak may update only `name`, `bio`, `photo`, `rate`, and `availability` of their own document (`auth.uid == sadhakId`). Never `verified`, `rating`, or `earnings` fields.
- **Fields:**
  - `sadhakId` (string): Provider UID.
  - `name` (string): Sadhak display name.
  - `bio` (string): Profile bio and spiritual specialization.
  - `photo` (string): Profile photo URL.
  - `rate` (integer paise/min): Call consultation rate per minute.
  - `availability` (boolean): Current online/available status.
  - `verified` (boolean): Verification badge (ops/server-only write).
  - `rating` (number): Overall rating score (server-only write).
  - `earnings` / `totalEarnings` (integer paise): Accumulated lifetime earnings (server-only write).

### C. `/questions/{questionId}` (Seeker Consultation Questions)
- **Role:** Text-based questions asked by seekers and answered by sadhaks.
- **Security:**
  - Read: Signed-in users (`allow read: if isSignedIn();`).
  - Create: Seeker creates own question (`request.resource.data.userId == request.auth.uid`), cannot write `earningAmount`.
  - Update: Assigned provider (`providerId == request.auth.uid`) may update only `answer`, `status`, and `answeredAt`. `earningAmount` is server-only.
  - Delete: Denied (`allow delete: if false;`).
- **Schema:**
  - `questionText` (string): The query submitted by the seeker.
  - `userId` (string): Seeker's authentication UID.
  - `userName` (string): Seeker's display name.
  - `seekerLocation` (string): Seeker's city/place of origin.
  - `category` (string): Topic (e.g., 'Career', 'Kundali', 'Dream', 'Remedy').
  - `providerId` (string): Assigned sadhak UID.
  - `sadhakName` (string): Assigned sadhak's display name.
  - `status` (string): `'pending'` | `'assigned'` | `'answered'`.
  - `answer` (string): Provider's guidance/response.
  - `earningAmount` (integer paise): Fee credited to provider / debited from seeker (server-only).
  - `createdAt` (timestamp): Timestamp of question submission.
  - `answeredAt` (timestamp): Timestamp when answer was posted.

### D. `/active_calls/{callId}` (Real-Time Audio / Video Calls)
- **Role:** WebRTC/Agora live calling session control.
- **Security:**
  - Read: Only `callerId` or `providerId` (`allow read: if resource.data.callerId == request.auth.uid || resource.data.providerId == request.auth.uid;`).
  - Create: Caller creates with status `'ringing'`, cannot write `earnedAmount`.
  - Update: Either party (`callerId` or `providerId`) may update `status` and `durationSeconds`. `earnedAmount` is server-only.
  - Delete: Denied (`allow delete: if false;`).
- **Schema:**
  - `callerId` (string): Seeker auth UID.
  - `callerName` (string): Seeker's display name.
  - `callerPhone` (string): Seeker's phone number.
  - `providerId` (string): Sadhak auth UID.
  - `channelName` (string): Agora channel identifier (format: `devsaadhak_<callId>`).
  - `agoraToken` (string): Authentication token for RTC channel.
  - `ratePerMinute` (integer paise): Consultation rate per minute.
  - `durationSeconds` (integer seconds): Total call duration in seconds.
  - `earnedAmount` (integer paise): Final settlement amount (server-only write).
  - `status` (string): `'ringing'` | `'connected'` | `'ended'`.
  - `timestamp` (timestamp): Call initiation timestamp.

### E. `/chats/{chatId}` and `/chats/{chatId}/messages/{messageId}`
- **Role:** Consultation direct messaging.
- **Security:** Participants read and write (`allow read, write: if isSignedIn();`).
- **Fields:**
  - `chatId` (string): Unique conversation ID.
  - `senderId` (string): Author UID.
  - `text` (string): Chat message body.
  - `timestamp` (timestamp): Message send time.

### F. `/providers/{providerId}` Subcollections
- **Jobs & Remedies:**
  - `/providers/{providerId}/jobs/{jobId}`: Owner read/write.
  - `/providers/{providerId}/remedies/{remedyId}`: Owner read/write.
  - `/providers/{providerId}/payout_requests/{requestId}`: Owner read/write.
- **Provider Ledger & Wallet:**
  - `/providers/{providerId}/wallet/summary`:
    - Read: Owner (`auth.uid == providerId`).
    - Write: Server-only (`allow write: if false;`).
    - Fields: `balance` (integer paise), `totalEarnings` (integer paise), `updatedAt` (timestamp).
  - `/providers/{providerId}/transactions/{transactionId}`:
    - Read: Owner (`auth.uid == providerId`).
    - Write: Server-only (`allow write: if false;`).
    - Fields: `id`, `type` (`'call_earning'` | `'question_earning'`), `amountPaise` (integer), `callId` / `questionId`, `timestamp`, `status`.

### G. Financial Audit Collections
- `/payments/{paymentId}`: Read by owner (`resource.data.uid == request.auth.uid`), server-only write.
- `/paymentOrders/{orderId}`: Read by owner (`resource.data.uid == request.auth.uid`), server-only write.
- `/walletLedger/{ledgerId}`: Read by owner (`resource.data.uid == request.auth.uid`), server-only write.

---

## 4. Cloud Functions Settlement Contract

### 1. `settleCall` (Trigger: `active_calls -> 'ended'`)
- **Trigger:** Cloud Firestore document update on `active_calls/{callId}` when `status` transitions from non-ended to `'ended'`. Also exposed via `settleCallCallable`.
- **Settlement Formula:**
  $$\text{earnedAmount} = \lceil \frac{\text{durationSeconds}}{60} \rceil \times \text{ratePerMinute} \quad (\text{all values in paise})$$
- **Atomic Operations (`db.runTransaction`):**
  1. Idempotency Check: Verifies `callData.settled != true` and `ledgerSnap.exists != true`.
  2. Debit Seeker: Atomically increments `/users/{callerId}.walletBalance` by `-earnedAmount`.
  3. Write Seeker Ledger: Creates `/walletLedger/{callerId}_call_{callId}` with negative paise debit.
  4. Credit Provider Transaction: Writes `/providers/{providerId}/transactions/call_{callId}` with `+earnedAmount`.
  5. Update Provider Wallet: Increments `/providers/{providerId}/wallet/summary` `balance` by `+earnedAmount` and `totalEarnings` by `+earnedAmount`.
  6. Finalize Call: Updates `/active_calls/{callId}` with `earnedAmount`, `settled: true`, and `settledAt`.

### 2. `settleQuestion` (Trigger: `questions -> 'answered'`)
- **Trigger:** Cloud Firestore document update on `questions/{questionId}` when `status` transitions to `'answered'`. Also exposed via `settleQuestionCallable`.
- **Atomic Operations (`db.runTransaction`):**
  1. Idempotency Check: Verifies `questionData.settled != true` and `ledgerSnap.exists != true`.
  2. Debit Seeker: Atomically increments `/users/{userId}.walletBalance` by `-earningAmount`.
  3. Write Seeker Ledger: Creates `/walletLedger/{userId}_question_{questionId}`.
  4. Credit Provider Transaction: Writes `/providers/{providerId}/transactions/question_{questionId}` with `+earningAmount`.
  5. Update Provider Wallet: Increments `/providers/{providerId}/wallet/summary` `balance` and `totalEarnings`.
  6. Finalize Question: Updates `/questions/{questionId}` with `earningAmount`, `settled: true`, and `settledAt`.
