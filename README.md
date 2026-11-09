> Current repair release: read [RELEASE_NOTES.md](RELEASE_NOTES.md) and [DEPLOYMENT_2026-11-09.md](DEPLOYMENT_2026-11-09.md). Historical status below may predate these changes.

# Devbhasha Backend & Operations Guide

## Firestore TTL Policies (1-Year Auto-Delete)

To enable automatic time-to-live (TTL) expiration on the named database `ai-studio-android-devbhash-c2b59e88-80f5-4209-81ce-859c3cb917d9`, run the following `gcloud` commands:

### 1. Enable TTL on `messages` Collection Group
```bash
gcloud firestore fields ttls update expireAt \
  --collection-group=messages \
  --database=ai-studio-android-devbhash-c2b59e88-80f5-4209-81ce-859c3cb917d9 \
  --enable-ttl
```

### 2. Enable TTL on `chats` Collection Group
```bash
gcloud firestore fields ttls update expireAt \
  --collection-group=chats \
  --database=ai-studio-android-devbhash-c2b59e88-80f5-4209-81ce-859c3cb917d9 \
  --enable-ttl
```

### 3. Enable TTL on `sessions` Collection Group
```bash
gcloud firestore fields ttls update expireAt \
  --collection-group=sessions \
  --database=ai-studio-android-devbhash-c2b59e88-80f5-4209-81ce-859c3cb917d9 \
  --enable-ttl
```

### 4. Verify Active TTL Policies
```bash
gcloud firestore fields ttls list \
  --database=ai-studio-android-devbhash-c2b59e88-80f5-4209-81ce-859c3cb917d9
```

---

## One-Time Maintenance & Backfill Scripts

### Backfill `expireAt` on Existing Messages, Chats, and Sessions
```bash
# Dry run:
node functions/scripts/backfillChatExpiry.cjs --dry-run

# Apply updates:
node functions/scripts/backfillChatExpiry.cjs --apply
```

### Migrate Provider Contact (Phone & Email) and FCM Tokens to Private Subcollections
```bash
# Dry run:
node functions/scripts/migrateProviderContact.cjs --dry-run

# Apply migration:
node functions/scripts/migrateProviderContact.cjs --apply
```

---

## Cloud Functions Retention Backup

- `purgeExpiredChats`: Cloud Function scheduled to run every 24 hours that sweeps and deletes any `messages`, `chats`, and `sessions` where `expireAt <= now`, in batches of 400.
- Financial audit records (`walletLedger`, `transactions`, `payments`, `payouts`, `tax`, `sadhakAgreementAcceptances`) are **retained for 8 years** and excluded from deletion.
