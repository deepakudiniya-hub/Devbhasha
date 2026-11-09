/**
 * migrateProviderContact.cjs
 *
 * Moves existing phone and email from public providers/{uid} into providers/{uid}/private/contact.
 * Moves fcmToken into providers/{uid}/private/device.
 * Deletes phone, email, and fcmToken from the public providers and sadhaks docs.
 *
 * Usage: node functions/scripts/migrateProviderContact.cjs [--dry-run | --apply]
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

async function migrateProviders() {
  console.log("\n--- Scanning providers ---");
  const providersSnap = await db.collection("providers").get();
  console.log(`Found ${providersSnap.size} provider documents.`);

  let migratedContactCount = 0;
  let migratedDeviceCount = 0;

  for (const doc of providersSnap.docs) {
    const data = doc.data() || {};
    const uid = doc.id;
    const phone = data.phone || data.phoneNumber || null;
    const email = data.email || null;
    const fcmToken = data.fcmToken || null;

    const hasContact = !!(phone || email);
    const hasDevice = !!fcmToken;

    if (hasContact) {
      migratedContactCount++;
      if (isApply) {
        const contactRef = db.collection("providers").doc(uid).collection("private").doc("contact");
        await contactRef.set({
          phone: phone || "",
          email: email || "",
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        }, { merge: true });

        // Remove from public document
        const publicDeletes = {};
        if (data.phone !== undefined) publicDeletes.phone = admin.firestore.FieldValue.delete();
        if (data.phoneNumber !== undefined) publicDeletes.phoneNumber = admin.firestore.FieldValue.delete();
        if (data.email !== undefined) publicDeletes.email = admin.firestore.FieldValue.delete();
        if (Object.keys(publicDeletes).length > 0) {
          await doc.ref.update(publicDeletes);
        }
      }
    }

    if (hasDevice) {
      migratedDeviceCount++;
      if (isApply) {
        const deviceRef = db.collection("providers").doc(uid).collection("private").doc("device");
        await deviceRef.set({
          fcmToken,
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        }, { merge: true });

        await doc.ref.update({
          fcmToken: admin.firestore.FieldValue.delete()
        }).catch(() => {});
      }
    }

    // Also sanitize mirrored sadhaks document
    if (isApply) {
      const sadhakRef = db.collection("sadhaks").doc(uid);
      const sadhakSnap = await sadhakRef.get();
      if (sadhakSnap.exists) {
        const sData = sadhakSnap.data();
        const sDeletes = {};
        if (sData.phone !== undefined) sDeletes.phone = admin.firestore.FieldValue.delete();
        if (sData.phoneNumber !== undefined) sDeletes.phoneNumber = admin.firestore.FieldValue.delete();
        if (sData.email !== undefined) sDeletes.email = admin.firestore.FieldValue.delete();
        if (sData.fcmToken !== undefined) sDeletes.fcmToken = admin.firestore.FieldValue.delete();
        if (Object.keys(sDeletes).length > 0) {
          await sadhakRef.update(sDeletes).catch(() => {});
        }
      }
    }
  }

  console.log(`Providers with contact info migrated: ${migratedContactCount}`);
  console.log(`Providers with FCM tokens migrated: ${migratedDeviceCount}`);
}

async function main() {
  try {
    await migrateProviders();
    console.log(isApply ? "\nMigration applied successfully." : "\nDry run completed. Run with --apply to commit changes.");
  } catch (err) {
    console.error("Migration failed:", err);
    process.exit(1);
  }
}

main();
