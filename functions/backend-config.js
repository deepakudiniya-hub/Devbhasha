// Shared named database. A typo must never silently select another database.
const expected='ai-studio-android-devbhash-c2b59e88-80f5-4209-81ce-859c3cb917d9';
const configured=process.env.FIRESTORE_DB_ID || process.env.FIRESTORE_DATABASE_ID || expected;
if(configured!==expected && !process.env.FIRESTORE_EMULATOR_HOST) throw Error('Unexpected Firestore database ID');
module.exports={database:configured};
