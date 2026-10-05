package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.*
import com.example.ui.theme.*
import com.example.utils.DreamSubmitter
import com.example.utils.DreamSubmitResult
import com.example.utils.HapticFeedbackHelper
import com.example.utils.PaymentResultEvent
import com.example.utils.RazorpayPaymentManager
import com.example.utils.UserManager
import com.example.utils.UserSession
import com.example.utils.WalletRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    userName: String = "साधक",
    userId: String = "",
    currentUserId: String = userId,
    onLogoutClick: () -> Unit = {},
    onSwitchToProvider: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSession = remember { UserSession(context) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val targetUserDocId = userId.ifBlank { userSession.getUserId() }

    // Dynamic user state
    var userDisplayName by remember {
        mutableStateOf(
            userName.ifBlank { userSession.getUserName() }
        )
    }
    var userEmail by remember { mutableStateOf("") }
    // Server-authoritative wallet — this is a display mirror only.
    var walletBalance by remember { mutableDoubleStateOf(userSession.getCachedWalletBalance()) }
    DisposableEffect(targetUserDocId) {
        val reg = WalletRepository.observeBalance(targetUserDocId) {
            walletBalance = it
            userSession.setCachedWalletBalance(it)
        }
        onDispose { reg.remove() }
    }
    var freeMins by remember { mutableIntStateOf(0) }
    var freeDreamUsed by remember { mutableStateOf(false) }
    var isOfferClaimed by remember { mutableStateOf(false) }
    var isHindi by remember { mutableStateOf(userSession.isHindi()) } // Keep for logic compatibility
    var language by remember { mutableStateOf(userSession.getLanguage()) }

    // Navigation View: "today", "sadhak", "remedy", "dreams", "wallet"
    var currentView by remember { mutableStateOf("today") }
    var searchQuery by remember { mutableStateOf("") }
    var activeJournalFilter by remember { mutableStateOf(JournalFilterType.ALL) }
    var customFilterDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedEntryForDetail by remember { mutableStateOf<DreamJournalEntry?>(null) }

    // Sheets & Dialogs
    var showProfileSheet by remember { mutableStateOf(false) }
    var showRechargeSheet by remember { mutableStateOf(false) }
    var showBookingSheet by remember { mutableStateOf(false) }
    var showLogDreamDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showPoochhoSheet by remember { mutableStateOf(false) }
    var showDreamsModalSheet by remember { mutableStateOf(false) }
    var editedNameInput by remember { mutableStateOf("") }
    var newDreamText by remember { mutableStateOf("") }

    // Live Consultations
    var activeChatSadhak by remember { mutableStateOf<SadhakItem?>(null) }
    var activeCallingSadhak by remember { mutableStateOf<SadhakItem?>(null) }

    // Toast Notification Banner State
    var toastMessage by remember { mutableStateOf<String?>(null) }

    fun showToast(msg: String) {
        toastMessage = msg
        coroutineScope.launch {
            delay(2200)
            if (toastMessage == msg) {
                toastMessage = null
            }
        }
    }

    val initialSadhaks = remember {
        listOf(
            SadhakItem(
                id = "sadhak_1",
                nameHi = "आचार्य देव शर्मा",
                nameEn = "Acharya Dev Sharma",
                titleHi = "वैदिक ज्योतिष एवं कुण्डली",
                titleEn = "Vedic Astrologer & Kundli",
                experienceHi = "18 वर्ष अनुभव",
                experienceEn = "18 Years Exp",
                rating = "4.9",
                isOnline = true,
                bio = "Expert in Vedic astrology, Kundli matching and spiritual remedies.",
                phone = "",
                initialHi = "आ",
                initialEn = "AD"
            ),
            SadhakItem(
                id = "sadhak_2",
                nameHi = "पं. रामानंद शास्त्री",
                nameEn = "Pt. Ramanand Shastri",
                titleHi = "कर्मकांड एवं पूजा अनुष्ठान",
                titleEn = "Karma Kand & Vedic Rituals",
                experienceHi = "22 वर्ष अनुभव",
                experienceEn = "22 Years Exp",
                rating = "5.0",
                isOnline = true,
                bio = "Specialist in Vedic rituals, Mantras and Vastu Shastra.",
                phone = "",
                initialHi = "पं",
                initialEn = "RS"
            ),
            SadhakItem(
                id = "sadhak_3",
                nameHi = "योगी आनंद नाथ",
                nameEn = "Yogi Ananda Nath",
                titleHi = "ध्यान एवं स्वप्न विशेषज्ञ",
                titleEn = "Meditation & Dream Guide",
                experienceHi = "15 वर्ष अनुभव",
                experienceEn = "15 Years Exp",
                rating = "4.8",
                isOnline = true,
                bio = "Guiding seekers through dream symbolism, meditation and inner peace.",
                phone = "",
                initialHi = "यो",
                initialEn = "YA"
            ),
            SadhakItem(
                id = "sadhak_4",
                nameHi = "डॉ. राधिका वशिष्ठ",
                nameEn = "Dr. Radhika Vashishta",
                titleHi = "टैरो कार्ड एवं अंक ज्योतिष",
                titleEn = "Tarot & Numerology",
                experienceHi = "12 वर्ष अनुभव",
                experienceEn = "12 Years Exp",
                rating = "4.9",
                isOnline = true,
                bio = "Intuitive tarot reader, numerologist and spiritual life consultant.",
                phone = "",
                initialHi = "डॉ",
                initialEn = "RV"
            ),
            SadhakItem(
                id = "sadhak_5",
                nameHi = "स्वामी प्रज्ञानंद",
                nameEn = "Swami Pragyanand",
                titleHi = "हस्तरेखा एवं वास्तु शास्त्र",
                titleEn = "Palmistry & Sacred Vastu",
                experienceHi = "25 वर्ष अनुभव",
                experienceEn = "25 Years Exp",
                rating = "4.9",
                isOnline = false,
                bio = "Master palmist, ancient Vastu architect and mantra practitioner.",
                phone = "",
                initialHi = "स्वा",
                initialEn = "SP"
            )
        )
    }

    val initialDreams = remember {
        listOf(
            DreamJournalEntry(
                id = "dream_1",
                title = "पवित्र शिवलिंग एवं गंगाजल दर्शन",
                datePhase = "आज",
                description = "प्रातःकाल सपने में प्राचीन शिव मंदिर में शिवलिंग पर निर्मल गंगाजल और बेलपत्र अर्पित करते देखा।",
                tag = "🌿 शुभ (Shubh)",
                tags = listOf("#shiva", "#sacred", "#peace"),
                timestamp = System.currentTimeMillis() - 7200000L,
                meaning = "अत्यंत मंगलकारी स्वप्न। मानसिक शांति, रोगमुक्ति तथा रुके हुए कार्यों में सफलता का संकेत है।",
                answeredBy = "आचार्य देव शर्मा"
            ),
            DreamJournalEntry(
                id = "dream_2",
                title = "नदी किनारे जलता हुआ दीपक",
                datePhase = "कल",
                description = "संध्या समय किसी शांत नदी के तट पर घी का अखण्ड दीपक जलते हुए देखा।",
                tag = "✨ आत्म-बोध (Awakening)",
                tags = listOf("#deepak", "#river", "#spiritual"),
                timestamp = System.currentTimeMillis() - 86400000L,
                meaning = "ज्ञान, मार्गदर्शन और जीवन में नए प्रकाश का प्रतीक है।",
                answeredBy = "योगी आनंद नाथ"
            )
        )
    }

    var saadhakList by remember { mutableStateOf(initialSadhaks) }
    var dreamList by remember { mutableStateOf(initialDreams) }

    // Listen to Razorpay
    LaunchedEffect(Unit) {
        RazorpayPaymentManager.paymentEvents.collect { event ->
            when (event) {
                is PaymentResultEvent.Success -> {
                    if (RazorpayPaymentManager.pendingPurpose == "dream_matlab") {
                        val text = RazorpayPaymentManager.pendingDreamText.trim()
                        if (text.isNotBlank()) {
                            val submitRes = DreamSubmitter.submitDreamToRandomSadhak(
                                userId = targetUserDocId,
                                userName = userDisplayName,
                                dreamText = text,
                                paid = true,
                                amount = 99.0,
                                paymentMode = "razorpay",
                                paymentId = event.transactionId
                            )
                            // Charge server-side (idempotent by payment id).
                            WalletRepository.spend(
                                amountRupees = 99.0,
                                purpose = "dream_matlab",
                                ref = "dream_${event.transactionId}"
                            ) { }
                            when (submitRes) {
                                is DreamSubmitResult.Success -> {
                                    showToast("पेमेंट सफल! सपना साधक '${submitRes.sadhakName}' को भेज दिया गया ✨")
                                }
                                is DreamSubmitResult.NoVerifiedSadhak -> {
                                    showToast("पेमेंट सफल! सपना दर्ज हो गया है, शीघ्र ही सत्यापित साधक को सौंपा जाएगा।")
                                }
                                is DreamSubmitResult.Error -> {
                                    showToast("पेमेंट सफल! सपना सुरक्षित कर लिया गया।")
                                }
                            }
                        }
                    } else {
                        showToast("Payment Successful: ${event.transactionId}")
                    }
                }
                is PaymentResultEvent.Error -> {
                    showToast("Payment Error: ${event.errorMessage}")
                }
            }
        }
    }

    // Call / Chat Session Starter (₹20 deduction + session request)
    val handleStartConsultation: (SadhakItem, String) -> Unit = { sadhak, type ->
        HapticFeedbackHelper.playClick(haptic)
        if (walletBalance < 20.0 && freeMins <= 0) {
            showRechargeSheet = true
            showToast("Low balance. Please recharge ₹100")
        } else {
            if (freeMins > 0) {
                freeMins -= 1
                showToast("Used 1 free minute with ${sadhak.nameEn}")
            } else {
                // Charge server-side; the mirror updates via observeBalance.
                WalletRepository.spend(
                    amountRupees = 20.0,
                    purpose = "consultation",
                    ref = "consult_${sadhak.id}_${System.currentTimeMillis()}"
                ) { }
            }

            if (type == "chat") {
                activeChatSadhak = sadhak
            } else {
                activeCallingSadhak = sadhak
            }
        }
    }

    fun handleClaimTicket() {
        HapticFeedbackHelper.playClick(haptic)
        if (!isOfferClaimed) {
            isOfferClaimed = true
            freeMins = 15
            showToast("Offer claimed — 15 free minutes added")
        }
    }

    // Log Dream Dialog
    if (showLogDreamDialog) {
        AlertDialog(
            onDismissRequest = { showLogDreamDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "नया सपना दर्ज करें · Log Dream",
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "सपने में देखे गए दृश्य, प्रतीक व भावनाएँ यहाँ लिखें:",
                        fontSize = 12.5.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newDreamText,
                        onValueChange = { newDreamText = it },
                        placeholder = { Text("सपने का विवरण लिखें...", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (newDreamText.isBlank()) {
                                        newDreamText = "सपने में बहती हुई पावन नदी और कमल का पुष्प दिखाई दिया..."
                                    }
                                    Toast.makeText(context, "माइक द्वारा सपना रिकॉर्ड हुआ ✨", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Mic,
                                    contentDescription = "Mic",
                                    tint = SaffronPrimary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(115.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val text = newDreamText.trim()
                        if (text.isNotBlank()) {
                            val newEntry = DreamJournalEntry(
                                id = "dream_" + System.currentTimeMillis(),
                                title = text.take(42),
                                datePhase = "आज",
                                description = text,
                                tag = "🌿 शुभ (Shubh)",
                                tags = listOf("#lucid", "#spiritual"),
                                timestamp = System.currentTimeMillis()
                            )
                            dreamList = listOf(newEntry) + dreamList
                            newDreamText = ""
                            showLogDreamDialog = false
                            showToast("सपना सुरक्षित कर लिया गया ✨")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Save",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("सपना सुरक्षित करें", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogDreamDialog = false }) {
                    Text("रद्द करें (Cancel)", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = PaperBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Edit Profile Name",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = Ink
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your sacred name or preferred name for consultations:",
                        fontSize = 12.sp,
                        color = InkSoft
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editedNameInput,
                        onValueChange = { editedNameInput = it },
                        placeholder = { Text("e.g. Rahul Sharma", fontSize = 13.sp, color = InkFaint) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Terra,
                            unfocusedBorderColor = EditorialLineStrong,
                            focusedContainerColor = PaperCard,
                            unfocusedContainerColor = PaperCard
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = editedNameInput.trim()
                        if (trimmed.isNotBlank()) {
                            userDisplayName = trimmed
                            UserSession(context).saveUserSession(
                                userName = trimmed,
                                isLoggedIn = true,
                                userId = targetUserDocId,
                                phoneNumber = ""
                            )
                            showToast("Name updated to $trimmed ✨")
                            showEditNameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Terra),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Name")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = InkSoft)
                }
            }
        )
    }

    // Full Screen Live Chat Room Overlay
    if (activeChatSadhak != null) {
        LiveChatRoomScreen(
            sadhak = activeChatSadhak!!,
            isHindi = isHindi,
            userId = targetUserDocId,
            userName = userDisplayName,
            walletBalance = walletBalance,
            onCloseChat = { activeChatSadhak = null },
            onStartCall = {
                val s = activeChatSadhak!!
                activeChatSadhak = null
                activeCallingSadhak = s
            },
            onLowBalance = { showRechargeSheet = true },
            onAddBalance = { showRechargeSheet = true }
        )
        return
    }

    // Full Screen Live Audio Call Overlay
    if (activeCallingSadhak != null) {
        LiveAudioCallScreen(
            sadhak = activeCallingSadhak!!,
            isHindi = isHindi,
            userId = targetUserDocId,
            userName = userDisplayName,
            walletBalance = walletBalance,
            onEndCall = { activeCallingSadhak = null }
        )
        return
    }

    BackHandler(enabled = currentView != "today") {
        currentView = "today"
    }

    // Main Scaffold in Bento Editorial Design
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFAFAFA), // Unified background
        topBar = {
            if (currentView == "today") {
                Surface(
                    color = Color(0xFFFAFAFA), // Matches Scaffold
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Column {
                        BentoEditorialMasthead(
                            balanceAmount = walletBalance,
                            language = language,
                            isHindi = isHindi,
                            onLanguageSelect = { newLang ->
                                HapticFeedbackHelper.playClick(haptic)
                                language = newLang
                                isHindi = (newLang == "hi")
                                userSession.setLanguage(newLang)
                                UserManager.updateLanguagePreference(targetUserDocId, newLang)
                                val toastText = when (newLang) {
                                    "hi" -> "भाषा: हिंदी (Hindi) सक्रिय ✨"
                                    "hgl" -> "Bhasha: Hinglish active ✨"
                                    else -> "Language: English active ✨"
                                }
                                showToast(toastText)
                            },
                            onToggleLanguage = {
                                HapticFeedbackHelper.playClick(haptic)
                                val nextLang = when (language) {
                                    "en" -> "hi"
                                    "hi" -> "hgl"
                                    else -> "en"
                                }
                                language = nextLang
                                isHindi = (nextLang == "hi")
                                userSession.setLanguage(nextLang)
                                UserManager.updateLanguagePreference(targetUserDocId, nextLang)
                                val toastText = when (nextLang) {
                                    "hi" -> "भाषा: हिंदी (Hindi) सक्रिय ✨"
                                    "hgl" -> "Bhasha: Hinglish active ✨"
                                    else -> "Language: English active ✨"
                                }
                                showToast(toastText)
                            },
                            onBalanceClick = { currentView = "wallet" }
                        )
                        // Personalized Recommendation Panel
                        BentoEditorialRecommendationPanel(
                            userName = userDisplayName,
                            onClick = { showToast("Top ritual selected ✨") }
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (currentView in listOf("today", "sadhak", "remedy")) {
                BentoEditorialDock(
                    currentTab = currentView,
                    onTabSelect = { tab ->
                        HapticFeedbackHelper.playClick(haptic)
                        if (tab == "profile") {
                            showProfileSheet = true
                        } else {
                            currentView = tab
                        }
                    },
                    hasUnreadSadhak = !isOfferClaimed || freeMins == 0,
                    isHindi = isHindi,
                    language = language
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                // ... (existing content)
                targetState = currentView,
                transitionSpec = {
                    (fadeIn() + slideInVertically(initialOffsetY = { 50 }))
                        .togetherWith(fadeOut() + slideOutVertically(targetOffsetY = { -50 }))
                },
                label = "bento_view_animation",
                modifier = Modifier.fillMaxSize()
            ) { view ->
                when (view) {
                    "today" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 24.dp)
                        ) {
                            // 1. Journal Search Bar at the top of Home Screen (Keyword & Date filtering)
                            val isJournalSearchActive = searchQuery.isNotBlank() || customFilterDateMillis != null || activeJournalFilter != JournalFilterType.ALL
                            val filteredJournalEntries = remember(dreamList, searchQuery, activeJournalFilter, customFilterDateMillis) {
                                JournalSearchEngine.filterEntries(
                                    entries = dreamList,
                                    query = searchQuery,
                                    filterType = activeJournalFilter,
                                    customDateMillis = customFilterDateMillis
                                )
                            }

                            HomeJournalSearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                activeFilter = activeJournalFilter,
                                onFilterSelect = { filter ->
                                    activeJournalFilter = filter
                                    if (filter != JournalFilterType.ALL) {
                                        customFilterDateMillis = null
                                    }
                                },
                                customDateMillis = customFilterDateMillis,
                                onCustomDateSelect = { dateMillis ->
                                    customFilterDateMillis = dateMillis
                                    if (dateMillis != null) {
                                        activeJournalFilter = JournalFilterType.ALL
                                    }
                                },
                                onSearchSubmit = { q ->
                                    if (q.isNotBlank()) {
                                        showToast("Searching journal for \"$q\"")
                                    }
                                },
                                onOpenJournalClick = {
                                    currentView = "dreams"
                                },
                                isHindi = isHindi,
                                language = language
                            )

                            // 2. Real-time Filtered Search Results (displays when searching or date filtering)
                            if (isJournalSearchActive) {
                                HomeJournalSearchResultsSection(
                                    searchResults = filteredJournalEntries,
                                    searchQuery = searchQuery,
                                    activeFilter = activeJournalFilter,
                                    customDateMillis = customFilterDateMillis,
                                    onClearAllFilters = {
                                        searchQuery = ""
                                        activeJournalFilter = JournalFilterType.ALL
                                        customFilterDateMillis = null
                                    },
                                    onEntryClick = { entry ->
                                        selectedEntryForDetail = entry
                                    },
                                    onAskSadhakClick = { entry ->
                                        val firstSadhak = saadhakList.firstOrNull()
                                        if (firstSadhak != null) {
                                            handleStartConsultation(firstSadhak, "chat")
                                        } else {
                                            showBookingSheet = true
                                        }
                                    },
                                    onAddNewDreamClick = {
                                        showLogDreamDialog = true
                                    },
                                    isHindi = isHindi,
                                    language = language
                                )
                            }

                            // 3. Promo / Coupon Code Banner (DEV100 / Free 15 Mins)
                            Spacer(modifier = Modifier.height(10.dp))
                            BentoEditorialPromoBanner(
                                isOfferClaimed = isOfferClaimed,
                                onClaimOffer = { handleClaimTicket() },
                                isHindi = isHindi,
                                language = language
                            )

                            // 4. Grouped Tools Grid (Poochho, Dreams, and Circular Quick Tools)
                            Spacer(modifier = Modifier.height(14.dp))
                            BentoEditorialToolsGrid(
                                currentView = currentView,
                                onPoochhoClick = { showPoochhoSheet = true },
                                onDreamsClick = { showDreamsModalSheet = true },
                                onKundliClick = { currentView = "kundli" },
                                onTarotClick = { currentView = "tarot" },
                                onMatchClick = { currentView = "match" },
                                onHabitsClick = { currentView = "habits" },
                                onTimerClick = { currentView = "timer" },
                                isHindi = isHindi,
                                language = language
                            )

                            // 5. Experts on Call (Featured card + Expert rows)
                            Spacer(modifier = Modifier.height(14.dp))
                            BentoEditorialExpertsSection(
                                sadhaks = saadhakList,
                                onChatClick = { sadhak ->
                                    handleStartConsultation(sadhak, "chat")
                                },
                                onCallClick = { sadhak ->
                                    handleStartConsultation(sadhak, "call")
                                },
                                onSeeAllClick = {
                                    showBookingSheet = true
                                },
                                isHindi = isHindi,
                                language = language
                            )
                        }
                    }

                    "sadhak" -> {
                        SadhakDirectoryScreen(
                            sadhaks = saadhakList,
                            isHindi = isHindi,
                            onConsultSadhak = { sadhak ->
                                handleStartConsultation(sadhak, "chat")
                            },
                            onCallClick = { sadhak ->
                                handleStartConsultation(sadhak, "call")
                            },
                            onChatClick = { sadhak ->
                                handleStartConsultation(sadhak, "chat")
                            }
                        )
                    }

                    "remedy" -> {
                        BentoEditorialRemedyScreen()
                    }

                    "dreams" -> {
                        DreamJournalScreen(
                            userId = targetUserDocId,
                            userName = userDisplayName,
                            onBackClick = { currentView = "today" },
                            onLogDream = {
                                val savedMsg = when (language) {
                                    "hi" -> "सपना सुरक्षित कर लिया गया ✨"
                                    "hgl" -> "Sapna save kar liya gaya ✨"
                                    else -> "Dream saved to journal ✨"
                                }
                                showToast(savedMsg)
                            },
                            onChatWithSadhak = { currentView = "sadhak" },
                            dreamHistory = dreamList,
                            isHindi = isHindi,
                            language = language
                        )
                    }

                    "wallet" -> {
                        BentoEditorialWalletScreen(
                            balanceAmount = walletBalance,
                            onBackClick = { currentView = "today" },
                            onAddMoneyClick = { showRechargeSheet = true },
                            onGiftClick = { showToast("Gifting a sadhak — coming soon") },
                            isHindi = isHindi,
                            language = language
                        )
                    }

                    "kundli", "family" -> {
                        BentoFamilyProblemsScreen(
                            onBackClick = { currentView = "today" },
                            onStartChat = { sadhak -> handleStartConsultation(sadhak, "chat") },
                            onStartCall = { sadhak -> handleStartConsultation(sadhak, "call") },
                            sadhaks = saadhakList,
                            isHindi = isHindi,
                            language = language
                        )
                    }

                    "tarot", "health", "health_issues" -> {
                        BentoHealthIssuesScreen(
                            onBackClick = { currentView = "today" },
                            onStartChat = { sadhak -> handleStartConsultation(sadhak, "chat") },
                            onStartCall = { sadhak -> handleStartConsultation(sadhak, "call") },
                            sadhaks = saadhakList,
                            isHindi = isHindi,
                            language = language
                        )
                    }

                    "match", "money", "money_problem" -> {
                        BentoMoneyProblemScreen(
                            onBackClick = { currentView = "today" },
                            onStartChat = { sadhak -> handleStartConsultation(sadhak, "chat") },
                            onStartCall = { sadhak -> handleStartConsultation(sadhak, "call") },
                            sadhaks = saadhakList,
                            isHindi = isHindi,
                            language = language
                        )
                    }

                    "pooja" -> {
                        BentoDailyPoojaScreen(onBackClick = { currentView = "today" })
                    }

                    "habits", "negativity" -> {
                        BentoNegativityScreen(
                            onBackClick = { currentView = "today" },
                            onStartChat = { sadhak -> handleStartConsultation(sadhak, "chat") },
                            onStartCall = { sadhak -> handleStartConsultation(sadhak, "call") },
                            sadhaks = saadhakList,
                            isHindi = isHindi,
                            language = language
                        )
                    }

                    "timer", "pitr", "pitr_dosh" -> {
                        BentoPitrDoshScreen(
                            onBackClick = { currentView = "today" },
                            onStartChat = { sadhak -> handleStartConsultation(sadhak, "chat") },
                            onStartCall = { sadhak -> handleStartConsultation(sadhak, "call") },
                            sadhaks = saadhakList,
                            isHindi = isHindi,
                            language = language
                        )
                    }
                }
            }

            // Floating Toast Message
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { 20 }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Ink,
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = toastMessage ?: "",
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp)
                    )
                }
            }
        }
    }

    // Bottom Sheet: Recharge Sheet
    if (showRechargeSheet) {
        BentoEditorialRechargeSheet(
            currentBalance = walletBalance,
            onDismiss = { showRechargeSheet = false },
            onConfirmRecharge = { addedAmt ->
                val activity = context as? android.app.Activity
                if (activity != null) {
                    RazorpayPaymentManager.startRechargePayment(
                        activity = activity,
                        amount = addedAmt.toDouble(),
                        userId = targetUserDocId,
                        userName = userDisplayName,
                        userEmail = userEmail
                    )
                } else {
                    showToast("Payment system error")
                }
                showRechargeSheet = false
            }
        )
    }

    // Bottom Sheet: Poochho Problem & Consult Sheet (Tile 01)
    if (showPoochhoSheet) {
        PoochhoProblemConsultSheet(
            sadhaks = saadhakList,
            onStartChat = { sadhak, problemText ->
                handleStartConsultation(sadhak, "chat")
                if (problemText.isNotBlank()) {
                    showToast("परामर्श शुरू: $problemText ✨")
                }
            },
            onStartCall = { sadhak, problemText ->
                handleStartConsultation(sadhak, "call")
                if (problemText.isNotBlank()) {
                    showToast("कॉल शुरू: $problemText ✨")
                }
            },
            onDismiss = { showPoochhoSheet = false },
            isHindi = isHindi,
            language = language
        )
    }

    // Bottom Sheet: Dream Quick Recorder & Interpret Sheet (Tile 02)
    if (showDreamsModalSheet) {
        DreamQuickRecorderSheet(
            userId = targetUserDocId,
            userName = userDisplayName,
            onSaveDream = { text, tag ->
                showToast("सपना सुरक्षित हो गया ✨")
            },
            onAskSadhak = { text ->
                val firstSadhak = saadhakList.firstOrNull()
                if (firstSadhak != null) {
                    handleStartConsultation(firstSadhak, "chat")
                } else {
                    currentView = "sadhak"
                }
                showToast("सपना साधक को व्याख्या हेतु भेजा गया ✨")
            },
            onOpenJournal = {
                currentView = "dreams"
            },
            onDismiss = { showDreamsModalSheet = false },
            isHindi = isHindi,
            language = language
        )
    }

    // Bottom Sheet: Profile Sheet
    if (showProfileSheet) {
        BentoEditorialProfileSheet(
            userName = userDisplayName,
            userEmail = userEmail,
            balanceAmount = walletBalance,
            freeMins = freeMins,
            language = language,
            onDismiss = { showProfileSheet = false },
            onEditProfile = {
                editedNameInput = userDisplayName
                showProfileSheet = false
                showEditNameDialog = true
            },
            onOrdersClick = {
                val ordersMsg = when(language) {
                    "hi" -> "मेरे ऑर्डर — शीघ्र आ रहे हैं"
                    "hinglish" -> "Mere orders — jaldi aa rahe hain"
                    else -> "My orders — opening soon"
                }
                showToast(ordersMsg)
                showProfileSheet = false
            },
            onLanguageChange = { newLang ->
                language = newLang
                isHindi = (newLang == "hi")
                userSession.setLanguage(newLang)
                UserManager.updateLanguagePreference(targetUserDocId, newLang)
                val langStr = when(newLang) {
                    "hi" -> "हिंदी (Hindi)"
                    "hinglish" -> "Hinglish"
                    else -> "English"
                }
                showToast("Language changed to $langStr")
                showProfileSheet = false
            },
            onHelpClick = {
                val helpMsg = when(language) {
                    "hi" -> "सहायता एवं समर्थन — शीघ्र चालू होगा"
                    "hinglish" -> "Help & support — jald hi shuru hoga"
                    else -> "Help & support — opening soon"
                }
                showToast(helpMsg)
                showProfileSheet = false
            },
            onSwitchToProvider = {
                showProfileSheet = false
                onSwitchToProvider()
            },
            onLogoutClick = {
                showProfileSheet = false
                onLogoutClick()
            }
        )
    }

    // Bottom Sheet: Book a Pro Sheet
    if (showBookingSheet) {
        BentoEditorialBookingSheet(
            sadhaks = saadhakList,
            onDismiss = { showBookingSheet = false },
            onRequestBooking = { sadhak ->
                showBookingSheet = false
                showToast("Request sent to ${sadhak.nameEn} — they'll confirm shortly")
            }
        )
    }

    // Dialog: Full Detail of Selected Search Result Entry
    if (selectedEntryForDetail != null) {
        DreamEntryDetailDialog(
            entry = selectedEntryForDetail!!,
            onDismiss = { selectedEntryForDetail = null },
            onAskSadhak = {
                selectedEntryForDetail = null
                val firstSadhak = saadhakList.firstOrNull()
                if (firstSadhak != null) {
                    handleStartConsultation(firstSadhak, "chat")
                } else {
                    showBookingSheet = true
                }
            },
            isHindi = isHindi,
            language = language
        )
    }
}
