/**
 * backfillSadhakFlags.cjs
 * Sets isAvailable=false and isOnline=false on sadhaks and providers documents missing them.
 * Usage: node backfillSadhakFlags.cjs [--dry-run | --apply]
 */

const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

// Load DB ID from .env
let dbId = "(default)";
try {
  const envPath = path.join(__dirname, "..", ".env");
  if (fs.existsSync(envPath)) {
    const env = fs.readFileSync(envPath, "utf8");
    const match = env.match(/FIRESTORE_DB_ID=(.+)/);
    if (match) dbId = match[1].trim();
  }
} catch (e) {
  console.warn("Could not read .env, using (default)");
}

console.log(`Using database: ${dbId}`);

admin.initializeApp();
const db = dbId === "(default)" ? admin.firestore() : admin.firestore(dbId);

const args = process.argv.slice(2);
const isDryRun = args.includes("--dry-run");
const isApply = args.includes("--apply");

if (!isDryRun && !isApply) {
  console.error("Please specify --dry-run or --apply");
  process.exit(1);
}

async function backfillCollection(name) {
  console.log(`\nProcessing collection: ${name}`);
  const snap = await db.collection(name).get();
  let count = 0;
  let missing = 0;

  for (const doc of snap.docs) {
    const data = doc.data();
    const needsUpdate = !('isAvailable' in data) || !('isOnline' in data);

    if (needsUpdate) {
      missing++;
      if (isApply) {
        await doc.ref.update({
          isAvailable: data.isAvailable ?? false,
          isOnline: data.isOnline ?? false,
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
      }
    }
    count++;
  }

  console.log(`Total documents: ${count}`);
  console.log(`Documents missing flags: ${missing}`);
  if (isApply) {
    console.log(`Updated ${missing} documents.`);
  } else {
    console.log(`(Dry run) Would update ${missing} documents.`);
  }
}

async function main() {
  try {
    await backfillCollection("sadhaks");
    await backfillCollection("providers");
    console.log("\nFinished.");
  } catch (e) {
    console.error("Error during backfill:", e);
    process.exit(1);
  }
}

main();
