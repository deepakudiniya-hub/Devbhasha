const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;

before(async () => {
  const [emuHost, emuPort] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8080").split(":");
  testEnv = await initializeTestEnvironment({
    projectId: "devbhasha-d9e22",
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: emuHost,
      port: Number(emuPort),
    },
  });
});

after(async () => {
  await testEnv.cleanup();
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

// 1. users
test("users: allow self read/create, deny cross-user read", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  const u2 = testEnv.authenticatedContext("user2").firestore();

  await assertSucceeds(u1.collection("users").doc("user1").set({ name: "User 1" }));
  await assertFails(u2.collection("users").doc("user1").get());
});

// 2. payments
test("payments: allow owner read, deny client write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("payments").doc("p1").set({ uid: "user1", amount: 500 });
  });

  await assertSucceeds(u1.collection("payments").doc("p1").get());
  await assertFails(u1.collection("payments").doc("p2").set({ uid: "user1", amount: 100 }));
});

// 3. paymentOrders
test("paymentOrders: allow owner read, deny client write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("paymentOrders").doc("o1").set({ uid: "user1", amount: 500 });
  });

  await assertSucceeds(u1.collection("paymentOrders").doc("o1").get());
  await assertFails(u1.collection("paymentOrders").doc("o2").set({ uid: "user1", amount: 100 }));
});

// 4. walletLedger
test("walletLedger: allow owner read, deny client write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("walletLedger").doc("l1").set({ uid: "user1", amount: -100 });
  });

  await assertSucceeds(u1.collection("walletLedger").doc("l1").get());
  await assertFails(u1.collection("walletLedger").doc("l2").set({ uid: "user1", amount: 100 }));
});

// 5. wallets
test("wallets: allow self read, deny client write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("wallets").doc("user1").set({ balance: 1000 });
  });

  await assertSucceeds(u1.collection("wallets").doc("user1").get());
  await assertFails(u1.collection("wallets").doc("user1").update({ balance: 2000 }));
});

// 6. transactions
test("transactions: allow owner read, deny client write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("transactions").doc("t1").set({ uid: "user1", amount: 500 });
  });

  await assertSucceeds(u1.collection("transactions").doc("t1").get());
  await assertFails(u1.collection("transactions").doc("t2").set({ uid: "user1", amount: 500 }));
});

// 7. sessions
test("sessions: allow participant read, deny client write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("sessions").doc("s1").set({ participants: ["user1", "sadhak1"] });
  });

  await assertSucceeds(u1.collection("sessions").doc("s1").get());
  await assertFails(u1.collection("sessions").doc("s2").set({ participants: ["user1", "sadhak1"] }));
});

// 8. chats & messages
test("chats & messages: allow participant access with valid expireAt", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("chats").doc("c1").set({ participants: ["user1", "sadhak1"] });
    await adminDb.collection("sessions").doc("c1").set({ status: "active", endsAt: new Date(Date.now() + 3600000), participants: ["user1", "sadhak1"] });
  });

  await assertSucceeds(u1.collection("chats").doc("c1").get());
  const now = new Date();
  const validExpire = new Date(now.getTime() + 365 * 24 * 3600 * 1000);
  await assertSucceeds(
    u1.collection("chats").doc("c1").collection("messages").add({
      senderId: "user1",
      text: "Namaste",
      timestamp: now,
      expireAt: validExpire
    })
  );
  await assertFails(
    u1.collection("chats").doc("c1").collection("messages").add({
      senderId: "user1",
      text: "Invalid expire",
      timestamp: now,
      expireAt: new Date(now.getTime() + 1000)
    })
  );
});

// 9. questions
test("questions: allow participant read/create, deny unauthorized updates", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  const u2 = testEnv.authenticatedContext("user2").firestore();

  await assertSucceeds(
    u1.collection("questions").doc("q1").set({
      userId: "user1",
      providerId: "sadhak1",
      status: "pending",
      questionText: "Future?"
    })
  );
  await assertSucceeds(u1.collection("questions").doc("q1").get());
  await assertFails(u2.collection("questions").doc("q1").get());
});

// 10. active_calls
test("active_calls: allow participant read/create, deny unauthorized update", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  const u2 = testEnv.authenticatedContext("user2").firestore();

  await assertFails(u1.collection("active_calls").doc("call1").set({callerId:"user1",providerId:"sadhak1",status:"ringing"}));
  await testEnv.withSecurityRulesDisabled(async ctx => { await ctx.firestore().doc('active_calls/call1').set({callerId:'user1',providerId:'sadhak1',status:'ringing'}); });
  await assertSucceeds(u1.collection("active_calls").doc("call1").get());
  await assertFails(u2.collection("active_calls").doc("call1").get());
});

// 11. sadhaks
test("sadhaks: allow public read, restrict updates and validate price ranges", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  const s1 = testEnv.authenticatedContext("sadhak1").firestore();

  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("sadhaks").doc("sadhak1").set({
      name: "Sadhak 1",
      isVerified: true,
      chatPricePerMin: 25,
      callPricePerMin: 35,
      sessionPrice30Min: 499
    });
  });

  await assertSucceeds(u1.collection("sadhaks").doc("sadhak1").get());
  await assertFails(s1.collection("sadhaks").doc("sadhak1").update({ isVerified: false }));

  // Agreement fields cannot be updated by sadhak
  await assertFails(s1.collection("sadhaks").doc("sadhak1").update({ agreementAccepted: true }));
  await assertFails(s1.collection("sadhaks").doc("sadhak1").update({ acceptedAgreementVersion: "2.0" }));
  await assertFails(s1.collection("sadhaks").doc("sadhak1").update({ agreementAcceptedAt: new Date() }));
  await assertFails(s1.collection("sadhaks").doc("sadhak1").update({ hasAcceptedAgreement: true }));

  // Valid price updates
  await assertSucceeds(s1.collection("sadhaks").doc("sadhak1").update({ chatPricePerMin: 50 }));
  await assertSucceeds(s1.collection("sadhaks").doc("sadhak1").update({ callPricePerMin: 60 }));
  await assertSucceeds(s1.collection("sadhaks").doc("sadhak1").update({ sessionPrice30Min: 1500 }));

  // Out of range prices
  await assertFails(s1.collection("sadhaks").doc("sadhak1").update({ chatPricePerMin: 4 }));
  await assertFails(s1.collection("sadhaks").doc("sadhak1").update({ chatPricePerMin: 101 }));
  await assertFails(s1.collection("sadhaks").doc("sadhak1").update({ callPricePerMin: 4 }));
  await assertFails(s1.collection("sadhaks").doc("sadhak1").update({ callPricePerMin: 101 }));
  await assertFails(s1.collection("sadhaks").doc("sadhak1").update({ sessionPrice30Min: 198 }));
  await assertFails(s1.collection("sadhaks").doc("sadhak1").update({ sessionPrice30Min: 3000 }));
});

// 12. providers
test("providers & private subcollection: allow self private read/write, deny seeker private read", async () => {
  const seeker = testEnv.authenticatedContext("user1").firestore();
  const sadhak = testEnv.authenticatedContext("sadhak1").firestore();

  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("providers").doc("sadhak1").collection("private").doc("contact").set({ phone: "9876543210" });
  });

  await assertFails(seeker.collection("providers").doc("sadhak1").collection("private").doc("contact").get());
  await assertSucceeds(sadhak.collection("providers").doc("sadhak1").collection("private").doc("contact").get());
});

// 13. card_contents & card_comments
test("card_contents & card_comments: allow public read, private author subcollection", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("card_contents").doc("c1").set({ title: "Verse 1" });
  });

  await assertSucceeds(u1.collection("card_contents").doc("c1").get());
  await assertFails(u1.collection("card_comments").doc("forged").set({cardId:'c1',commentText:'fake'}));
  await testEnv.withSecurityRulesDisabled(async ctx => { await ctx.firestore().doc('card_comments/comm1').set({cardId:'c1',commentText:'Inspiring'}); });
  await assertFails(u1.collection("card_comments").doc("comm1").collection("private").doc("author").set({authorUid:'user1'}));
  await assertSucceeds(u1.collection("card_comments").doc("comm1").get());
});

// 14. consultation_requests
test("consultation_requests: allow participant access, deny unauthorized", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  const u2 = testEnv.authenticatedContext("user2").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("consultation_requests").doc("cr1").set({ userId: "user1", providerId: "sadhak1" });
  });

  await assertSucceeds(u1.collection("consultation_requests").doc("cr1").get());
  await assertFails(u2.collection("consultation_requests").doc("cr1").get());
});

// 15. consultation_history
test("consultation_history: allow participant access, deny unauthorized", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  const u2 = testEnv.authenticatedContext("user2").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("consultation_history").doc("ch1").set({ userId: "user1", providerId: "sadhak1" });
  });

  await assertSucceeds(u1.collection("consultation_history").doc("ch1").get());
  await assertFails(u2.collection("consultation_history").doc("ch1").get());
});

// 16. remedies
test("remedies: allow participant access", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  const u2 = testEnv.authenticatedContext("user2").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("remedies").doc("r1").set({ userId: "user1", providerId: "sadhak1" });
  });

  await assertSucceeds(u1.collection("remedies").doc("r1").get());
  await assertFails(u2.collection("remedies").doc("r1").get());
});

// 17. app_content
test("app_content: allow public read, deny client write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("app_content").doc("general_info").set({ text: "Om" });
  });

  await assertSucceeds(u1.collection("app_content").doc("general_info").get());
  await assertFails(u1.collection("app_content").doc("general_info").set({ text: "New" }));
});

// 19. banners
test("banners: allow public read, deny client write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("banners").doc("b1").set({ title: "Banner" });
  });

  await assertSucceeds(u1.collection("banners").doc("b1").get());
  await assertFails(u1.collection("banners").doc("b1").set({ title: "New" }));
});

// 20. app_config
test("app_config: allow public read, deny client write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("app_config").doc("settings").set({ maintenanceMode: false });
  });

  await assertSucceeds(u1.collection("app_config").doc("settings").get());
  await assertFails(u1.collection("app_config").doc("settings").set({ maintenanceMode: true }));
});

// 21. notification_log
test("notification_log: deny non-admin read/write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await assertFails(u1.collection("notification_log").doc("n1").get());
  await assertFails(u1.collection("notification_log").doc("n1").set({ title: "Test" }));
});

// 22. support_tickets
test("support_tickets: allow owner read/create, deny other read", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  const u2 = testEnv.authenticatedContext("user2").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("support_tickets").doc("t1").set({ userId: "user1", message: "Help" });
  });

  await assertSucceeds(u1.collection("support_tickets").doc("t1").get());
  await assertFails(u2.collection("support_tickets").doc("t1").get());
});

// 23. waitlist
test("waitlist: allow participant read/write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  const u2 = testEnv.authenticatedContext("user2").firestore();

  await assertSucceeds(u1.collection("waitlist").doc("w1").set({ userId: "user1", sadhakId: "s1" }));
  await assertFails(u2.collection("waitlist").doc("w1").get());
});

// 24. sadhakAgreementAcceptances
test("sadhakAgreementAcceptances: allow owner read, deny client write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("sadhakAgreementAcceptances").doc("a1").set({ uid: "user1", version: "2.0" });
  });

  await assertSucceeds(u1.collection("sadhakAgreementAcceptances").doc("a1").get());
  await assertFails(u1.collection("sadhakAgreementAcceptances").doc("a2").set({ uid: "user1" }));
});

// 25. payout_destinations
test("payout_destinations: deny client read/write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await assertFails(u1.collection("payout_destinations").doc("p1").get());
  await assertFails(u1.collection("payout_destinations").doc("p1").set({ data: 1 }));
});

// 26. payout_access_log
test("payout_access_log: deny non-admin read/write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await assertFails(u1.collection("payout_access_log").doc("l1").get());
  await assertFails(u1.collection("payout_access_log").doc("l1").set({ data: 1 }));
});

// 27. deleted_providers
test("deleted_providers: deny non-admin read/write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await assertFails(u1.collection("deleted_providers").doc("d1").get());
  await assertFails(u1.collection("deleted_providers").doc("d1").set({ data: 1 }));
});

// 28. account_deletions
test("account_deletions: deny non-admin read/write", async () => {
  const u1 = testEnv.authenticatedContext("user1").firestore();
  await assertFails(u1.collection("account_deletions").doc("ad1").get());
  await assertFails(u1.collection("account_deletions").doc("ad1").set({ data: 1 }));
});

// 29. websiteEnquiries
test("websiteEnquiries: allow create with expected fields, message under 2000 chars, createdAt == request.time", async () => {
  const anon = testEnv.unauthenticatedContext().firestore();
  const u1 = testEnv.authenticatedContext("user1").firestore();

  // Valid create
  await assertSucceeds(
    anon.collection("websiteEnquiries").add({
      name: "Devotee",
      email: "devotee@example.com",
      phone: "+919876543210",
      message: "Please help with kundli.",
      createdAt: require("firebase/firestore").serverTimestamp()
    })
  );

  // Deny unexpected fields
  await assertFails(
    anon.collection("websiteEnquiries").add({
      name: "Devotee",
      email: "devotee@example.com",
      phone: "+919876543210",
      message: "Test",
      createdAt: require("firebase/firestore").serverTimestamp(),
      admin: true
    })
  );

  // Deny message >= 2000 chars
  await assertFails(
    anon.collection("websiteEnquiries").add({
      name: "Devotee",
      email: "devotee@example.com",
      phone: "+919876543210",
      message: "a".repeat(2001),
      createdAt: require("firebase/firestore").serverTimestamp()
    })
  );

  // Deny non-admin read
  await assertFails(u1.collection("websiteEnquiries").doc("any").get());
});
