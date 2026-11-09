/**
 * backfillChatExpiry.cjs
 *
 * Sets expireAt = (createdAt / timestamp / settledAt + 365 days) on:
 * - existing messages in chats/{chatId}/messages
 * - existing chats in chats/{chatId}
 * - existing sessions in sessions/{sessionId}
 *
 * Usage: node functions/scripts/backfillChatExpiry.cjs [--dry-run | --apply]
 */

const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

let dbId = process.env.FIRESTORE_DB_ID || "ai-studio-android-devbhash-c2b59e88-80f5-4209-81ce-859c3cb917d9";
try {
  const envPath = path.join(__dirname, "..", ".env");
  if (fs.existsSync(envPath)) {
    const env = fs.readFileSync(envPath, "utf8");
    const match = env.match(/FIRESTORE_DB_ID=(.+)/);
    if (match) dbId = match[1].trim();
  }
} catch (e) {
  // Use default
}

console.log(`Connecting to Firestore database: ${dbId}`);
if (!admin.apps.length) admin.initializeApp();
const db = !dbId || dbId === "(default)" ? admin.firestore() : admin.firestore(dbId);

const args = process.argv.slice(2);
const isDryRun = args.includes("--dry-run");
const isApply = args.includes("--apply");

if (!isDryRun && !isApply) {
  console.error("Please specify either --dry-run or --apply");
  process.exit(1);
}

const ONE_YEAR_MS = 365 * 24 * 60 * 60 * 1000;

function getMillis(val) {
  if (!val) return null;
  if (typeof val === "number") return val;
  if (typeof val.toMillis === "function") return val.toMillis();
  if (typeof val.toDate === "function") return val.toDate().getTime();
  if (val instanceof Date) return val.getTime();
  return null;
}

async function backfillChatsAndMessages() {
  console.log("\n--- Processing chats and messages ---");
  const chatsSnap = await db.collection("chats").get();
  console.log(`Found ${chatsSnap.size} chat documents.`);

  let updatedChats = 0;
  let updatedMessages = 0;

  for (const chatDoc of chatsSnap.docs) {
    const chatData = chatDoc.data() || {};
    const chatCreatedMs = getMillis(chatData.createdAt) || getMillis(chatData.updatedAt) || Date.now();
    const chatExpireAt = admin.firestore.Timestamp.fromMillis(chatCreatedMs + ONE_YEAR_MS);

    if (!chatData.expireAt) {
      updatedChats++;
      if (isApply) {
        await chatDoc.ref.set({ expireAt: chatExpireAt }, { merge: true });
      }
    }

    const messagesSnap = await chatDoc.ref.collection("messages").get();
    for (const msgDoc of messagesSnap.docs) {
      const msgData = msgDoc.data() || {};
      if (!msgData.expireAt) {
        const msgTimeMs = getMillis(msgData.timestamp) || getMillis(msgData.createdAt) || chatCreatedMs;
        const msgExpireAt = admin.firestore.Timestamp.fromMillis(msgTimeMs + ONE_YEAR_MS);
        updatedMessages++;
        if (isApply) {
          await msgDoc.ref.set({ expireAt: msgExpireAt }, { merge: true });
        }
      }
    }
  }

  console.log(`Chats needing expireAt: ${updatedChats}`);
  console.log(`Messages needing expireAt: ${updatedMessages}`);
}

async function backfillSessions() {
  console.log("\n--- Processing sessions ---");
  const sessionsSnap = await db.collection("sessions").get();
  console.log(`Found ${sessionsSnap.size} session documents.`);

  let updatedSessions = 0;

  for (const sessionDoc of sessionsSnap.docs) {
    const sessionData = sessionDoc.data() || {};
    if (!sessionData.expireAt) {
      const baseMs = getMillis(sessionData.settledAt) ||
                     getMillis(sessionData.endedAt) ||
                     getMillis(sessionData.createdAt) ||
                     Date.now();
      const sessionExpireAt = admin.firestore.Timestamp.fromMillis(baseMs + ONE_YEAR_MS);
      updatedSessions++;
      if (isApply) {
        await sessionDoc.ref.set({ expireAt: sessionExpireAt }, { merge: true });
      }
    }
  }

  console.log(`Sessions needing expireAt: ${updatedSessions}`);
}

async function main() {
  try {
    await backfillChatsAndMessages();
    await backfillSessions();
    console.log(isApply ? "\nBackfill applied successfully." : "\nDry run completed. Run with --apply to commit changes.");
  } catch (err) {
    console.error("Backfill failed:", err);
    process.exit(1);
  }
}

main();
