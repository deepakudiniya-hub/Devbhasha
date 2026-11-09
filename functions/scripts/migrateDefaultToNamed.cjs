/**
 * Migration Script: Default Database to Named Database
 * 
 * README:
 * 1. Run "node scripts/migrateDefaultToNamed.cjs" to dry-run and count documents.
 * 2. Run "node scripts/migrateDefaultToNamed.cjs --copy" to perform the copy for eligible collections.
 * 
 * NOTE: Ensure you have initialized your environment with GOOGLE_APPLICATION_CREDENTIALS or are logged into firebase-tools.
 */

const admin = require("firebase-admin");
const { getFirestore } = require("firebase-admin/firestore");

admin.initializeApp();

const defaultDb = getFirestore();
const namedDb = getFirestore("ai-studio-android-devbhash-c2b59e88-80f5-4209-81ce-859c3cb917d9");

const collections = ["providers", "sadhaks", "questions", "active_calls", "chats", "remedies"];
const copyCollections = ["providers", "sadhaks", "remedies"];

async function countDocs(db, coll) {
    const snap = await db.collection(coll).get();
    return snap.size;
}

async function migrate() {
    const shouldCopy = process.argv.includes("--copy");
    console.log(`Starting migration: Copy mode = ${shouldCopy}`);

    for (const coll of collections) {
        const defaultCount = await countDocs(defaultDb, coll);
        const namedCount = await countDocs(namedDb, coll);
        console.log(`Collection: ${coll} | Default: ${defaultCount} | Named: ${namedCount}`);

        if (shouldCopy && copyCollections.includes(coll)) {
            console.log(`  Copying eligible documents from ${coll}...`);
            const docs = await defaultDb.collection(coll).listDocuments();
            for (const docRef of docs) {
                const doc = await docRef.get();
                const namedDocRef = namedDb.collection(coll).doc(doc.id);
                const namedDoc = await namedDocRef.get();
                
                if (!namedDoc.exists) {
                    await namedDocRef.set(doc.data());
                    // Simplified: Assuming only top-level for now. 
                    // Subcollections would need recursion.
                    console.log(`    Copied ${doc.id}`);
                }
            }
        }
    }
}

migrate().catch(console.error);
