// one-off: node setAdmin.js  (run inside functions/ in Cloud Shell)
const admin = require('firebase-admin');
admin.initializeApp({ projectId: 'devbhasha-d9e22' });
admin.auth().getUserByEmail('deepakudiniya@gmail.com')
  .then(u => admin.auth().setCustomUserClaims(u.uid, { ...(u.customClaims||{}), admin: true }).then(() => console.log('admin claim set for', u.uid)))
  .catch(e => { console.error(e.message); process.exit(1); });
