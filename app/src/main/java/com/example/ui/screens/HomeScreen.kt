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
import com.example.ui.components.PreConsultationDisclaimerBottomSheet
import com.example.ui.components.VedicAnimatedSearchBar
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
import com.example.utils.PriceLabels
import com.example.utils.SessionType
import com.example.utils.SessionBilling
import com.example.utils.SessionBillingException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.layout.ContentScale

import com.google.firebase.firestore.Query
import com.example.ui.models.AppConfig
import com.example.ui.models.Banner

@Composable
fun HomeScreen(
    userName: String = "Devotee",
    userId: String = "",
    currentUserId: String = userId,
    appConfig: AppConfig? = null,
    onLogoutClick: () -> Unit = {},
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
    var walletBalance by remember { mutableDoubleStateOf(userSession.getCachedWalletBalance() ?: 0.0) }
    DisposableEffect(targetUserDocId) {
        val reg = WalletRepository.observeBalance(targetUserDocId) {
            walletBalance = it
            userSession.setCachedWalletBalance(it)
        }
        onDispose { reg.remove() }
    }
    val freeMins = 0 // free time is server-owned; never tracked or granted on device
    var freeDreamUsed by remember { mutableStateOf(false) }
    var isOfferClaimed by remember { mutableStateOf(false) }
    var isHindi by remember { mutableStateOf(false) } // Fixed to English on app open
    var language by remember { mutableStateOf("en") }

    // Navigation View: "today", "sadhak", "remedy", "dreams", "wallet"
    var currentView by remember { mutableStateOf("today") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }

    BackHandler(enabled = isSearchFocused || searchQuery.isNotEmpty()) {
        isSearchFocused = false
        searchQuery = ""
    }
    var recentSearches by remember { mutableStateOf(userSession.getRecentSearches()) }
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
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedCardId by remember { mutableStateOf<String?>(null) }
    var editedNameInput by remember { mutableStateOf("") }
    var newDreamText by remember { mutableStateOf("") }
    var showHelpSupportScreen by remember { mutableStateOf(false) }

    // Live Consultations
    var activeChatSadhak by remember { mutableStateOf<SadhakItem?>(null) }
    // Text pre-filled (not sent) in the chat box when a chat is opened from a topic FAQ question.
    var chatPrefill by remember { mutableStateOf("") }
    var activeCallingSadhak by remember { mutableStateOf<SadhakItem?>(null) }
    var showPreConsultationWarningSheet by remember { mutableStateOf(false) }
    var pendingConsultationSadhak by remember { mutableStateOf<SadhakItem?>(null) }
    var pendingConsultationType by remember { mutableStateOf("chat") }

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

    val initialDreams = emptyList<DreamJournalEntry>()

    var saadhakList by remember { mutableStateOf<List<SadhakItem>>(emptyList()) }
    var dreamList by remember { mutableStateOf(initialDreams) }

    // Live listener for online approved sadhaks with accepted agreement
    DisposableEffect(context) {
        val listener = com.example.utils.SadhakRepository.observeOnlineSadhaks(
            context = context,
            onUpdate = { onlineList ->
                saadhakList = onlineList
            },
            onError = { err ->
                android.util.Log.e("HomeScreen", "Error observing online sadhaks", err)
            }
        )
        onDispose { listener.remove() }
    }

    // Listen to Razorpay
    LaunchedEffect(Unit) {
        RazorpayPaymentManager.paymentEvents.collect { event ->
            when (event) {
                is PaymentResultEvent.Success -> {
                    if (RazorpayPaymentManager.pendingPurpose == "dream_matlab") {
                        val text = RazorpayPaymentManager.pendingDreamText.trim()
                        if (text.isNotBlank()) {
                            // Wallet was credited by the verified payment; the server
                            // now bills the dream session itself (no client amount).
                            val submitRes = DreamSubmitter.submitDream(
                                userName = userDisplayName,
                                dreamText = text,
                                type = SessionType.DREAM_CHAT
                            )
                            when (submitRes) {
                                is DreamSubmitResult.Success -> {
                                    showToast("Payment successful! Dream sent to Sadhak '${submitRes.sadhakName}'.")
                                }
                                is DreamSubmitResult.NoVerifiedSadhak -> {
                                    showToast("Payment successful! Funds added to wallet. No Sadhak is currently online.")
                                }
                                is DreamSubmitResult.InsufficientBalance -> {
                                    showToast("Payment successful! Funds added to wallet. Please resend dream.")
                                }
                                is DreamSubmitResult.Error -> {
                                    showToast("Payment successful! Funds added to wallet. Error submitting dream: ${submitRes.message}")
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

    // Call / Chat Session Starter. Price, balance check and debit happen on the
    // server in startSession (see SessionBilling); the client sends no amounts.
    val handleStartConsultation: (SadhakItem, String) -> Unit = { sadhak, type ->
        HapticFeedbackHelper.playClick(haptic)
        if (userSession.isConsultationDisclaimerDismissed()) {
            when (type) {
                "chat" -> activeChatSadhak = sadhak
                "call" -> activeCallingSadhak = sadhak
                "session" -> {
                    coroutineScope.launch {
                        val result = SessionBilling.startSession(SessionType.SESSION_30, sadhak.id)
                        if (result.isSuccess) {
                            // Successfully started session
                            showBookingSheet = false 
                        } else {
                            val err = result.exceptionOrNull()
                            if (err is SessionBillingException && err.isInsufficientBalance) {
                                showRechargeSheet = true
                            } else {
                                showToast("Error starting session: ${err?.message}")
                            }
                        }
                    }
                }
            }
        } else {
            pendingConsultationSadhak = sadhak
            pendingConsultationType = type
            showPreConsultationWarningSheet = true
        }
    }

    fun handleClaimTicket() {
        HapticFeedbackHelper.playClick(haptic)
        // Free trial (first dream chat, 5 min, once per phone number) is granted
        // only by the server; nothing is credited on the device.
        isOfferClaimed = true
        showToast("${PriceLabels.FREE_TRIAL_BADGE} — पात्रता सत्र शुरू होने पर जाँची जाएगी")
    }

    // Log Dream Dialog
    if (showLogDreamDialog) {
        AlertDialog(
            onDismissRequest = { showLogDreamDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Log Dream",
                    fontFamily = com.example.ui.theme.ManropeHindFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "Describe the scenes, symbols, and emotions in your dream:",
                        fontSize = 12.5.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newDreamText,
                        onValueChange = { newDreamText = it },
                        placeholder = { Text("Write your dream details...", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (newDreamText.isBlank()) {
                                        newDreamText = "I saw a serene sacred river and a blooming lotus in the temple..."
                                    }
                                    Toast.makeText(context, "Voice input recorded", Toast.LENGTH_SHORT).show()
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
                                datePhase = "Today",
                                description = text,
                                tag = "Auspicious",
                                tags = listOf("#lucid", "#spiritual"),
                                timestamp = System.currentTimeMillis()
                            )
                            dreamList = listOf(newEntry) + dreamList
                            newDreamText = ""
                            showLogDreamDialog = false
                            showToast("Dream saved successfully")
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
                    Text("Save Dream", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogDreamDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
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
                    fontFamily = AppFontFamily,
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
                            showToast("Name updated to $trimmed")
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

    // Full Screen Help & Support Overlay
    if (showHelpSupportScreen) {
        HelpSupportScreen(
            onBack = { showHelpSupportScreen = false },
            appConfig = appConfig
        )
        return
    }

    // Full Screen Live Chat Room Overlay
    if (activeChatSadhak != null) {
        LiveChatRoomScreen(
            sadhak = activeChatSadhak!!,
            isHindi = isHindi,
            userId = targetUserDocId,
            userName = userDisplayName,
            walletBalance = walletBalance,
            onCloseChat = {
                activeChatSadhak = null
                chatPrefill = ""
            },
            onStartCall = {
                val s = activeChatSadhak!!
                activeChatSadhak = null
                chatPrefill = ""
                activeCallingSadhak = s
            },
            onLowBalance = { showRechargeSheet = true },
            onAddBalance = { showRechargeSheet = true },
            initialMessage = chatPrefill
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

    // Edge-to-edge white background across all views: use dark status bar icons.
    val hostView = androidx.compose.ui.platform.LocalView.current
    if (!hostView.isInEditMode) {
        DisposableEffect(currentView) {
            val window = (hostView.context as? Activity)?.window
            val controller = window?.let { androidx.core.view.WindowCompat.getInsetsController(it, hostView) }
            val previous = controller?.isAppearanceLightStatusBars
            controller?.isAppearanceLightStatusBars = true
            onDispose {
                if (controller != null && previous != null) controller.isAppearanceLightStatusBars = previous
            }
        }
    }

    // Main Scaffold in Bento Editorial Design
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background, // White background
        // Top bar removed from secondary tabs per user request
        bottomBar = {
            if (currentView in listOf("today", "sadhak", "dreams", "remedy")) {
                BentoEditorialDock(
                    currentTab = currentView,
                    onTabSelect = { tab ->
                        HapticFeedbackHelper.playClick(haptic)
                        currentView = tab
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
                .then(
                    if (currentView == "today") {
                        // Saffron header extends behind the status bar; it pads itself.
                        Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    } else {
                        Modifier.padding(innerPadding)
                    }
                )
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
                                .padding(bottom = 28.dp)
                        ) {
                            // 1 + 2. Saffron header: brand, wallet, avatar, greeting, search bar, today card banner
                            HomeV2Header(
                                userName = userDisplayName,
                                balanceAmount = walletBalance,
                                language = language,
                                isHindi = isHindi,
                                onBalanceClick = { currentView = "wallet" },
                                onAddMoneyClick = {
                                    HapticFeedbackHelper.playClick(haptic)
                                    showRechargeSheet = true
                                },
                                onProfileClick = {
                                    HapticFeedbackHelper.playClick(haptic)
                                    showProfileSheet = true
                                },
                                onTodayClick = {
                                    HapticFeedbackHelper.playClick(haptic)
                                    currentView = "pooja"
                                },
                                searchBar = {
                                    // Search Bar with animated expansion and smooth fade-in results (Placed directly ABOVE the Today's Panchang banner)
                                    VedicAnimatedSearchBar(
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { searchQuery = it },
                                        isFocused = isSearchFocused,
                                        onFocusChange = { 
                                            isSearchFocused = it
                                            if (it) { recentSearches = userSession.getRecentSearches() }
                                        },
                                        sadhakList = saadhakList,
                                        onSadhakClick = { sadhak ->
                                            isSearchFocused = false
                                            userSession.addRecentSearch(searchQuery)
                                            handleStartConsultation(sadhak, "chat")
                                        },
                                        onCategoryClick = { target ->
                                            isSearchFocused = false
                                            userSession.addRecentSearch(target)
                                            currentView = target
                                        },
                                        isHindi = isHindi
                                    )
                                    
                                    if (isSearchFocused && searchQuery.isEmpty() && recentSearches.isNotEmpty()) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                                Text("Recent Searches", style = MaterialTheme.typography.labelMedium, color = InkSoft)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                recentSearches.forEach { query ->
                                                    TextButton(
                                                        onClick = { searchQuery = query },
                                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(query, color = Ink)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            )

                            // 2. "What about?" (किस बारे में?) Categories - Placed directly below the search bar
                            Spacer(modifier = Modifier.height(20.dp))
                            HomeV2CategoryRow(
                                onCategoryClick = { category ->
                                    HapticFeedbackHelper.playClick(haptic)
                                    selectedCardId = category
                                    // Topic cards open their own FAQ screen (10 FAQ + ask box).
                                    // Unknown ids keep the old card detail screen.
                                    currentView = if (category in com.example.utils.TopicFaqContent.topicIds) "topic_faq" else "firestore_card"
                                },
                                language = language,
                                isHindi = isHindi
                            )

                            // 3. Banners Carousel (Below Search Bar and Categories)
                            val banners = remember { mutableStateListOf<Banner>() }
                            LaunchedEffect(Unit) {
                                val db = com.example.utils.FirestoreProvider.get(context)
                                db.collection("banners")
                                    .whereEqualTo("active", true)
                                    .orderBy("order", Query.Direction.ASCENDING)
                                    .addSnapshotListener { snapshot, error ->
                                        if (error != null) {
                                            android.util.Log.w("HomeScreen", "Banners listen error: ${error.message}")
                                            return@addSnapshotListener
                                        }
                                        snapshot?.let { snp ->
                                            val now = com.google.firebase.Timestamp.now()
                                            val list = snp.documents.mapNotNull { doc ->
                                                val b = doc.toObject(Banner::class.java)?.copy(id = doc.id)
                                                if (b != null) {
                                                    val start = b.startAt ?: now
                                                    val end = b.endAt ?: now
                                                    if (now >= start && now <= end) b else null
                                                } else null
                                            }
                                            banners.clear()
                                            banners.addAll(list)
                                        }
                                    }
                            }
                            
                            if (banners.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                BannersCarousel(banners, context)
                            }
                            
                            // Config-based Announcement Strip
                            appConfig?.let { config ->
                                if (config.announcement.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    AnnouncementStrip(config.announcement)
                                }
                            }


                            // Online sadhaks
                            Spacer(modifier = Modifier.height(26.dp))
                            HomeV2OnlineSadhaks(
                                sadhaks = saadhakList,
                                onChatClick = { sadhak -> handleStartConsultation(sadhak, "chat") },
                                onCallClick = { sadhak -> handleStartConsultation(sadhak, "call") },
                                onSeeAllClick = { showBookingSheet = true },
                                language = language,
                                isHindi = isHindi
                            )


                        }
                    }

                    "sadhak" -> {
                        Column(modifier = Modifier.statusBarsPadding()) {
                            SadhakScreen(
                                sadhakList = saadhakList,
                                onChatClick = { sadhak -> handleStartConsultation(sadhak, "chat") },
                                onCallClick = { sadhak -> handleStartConsultation(sadhak, "call") },
                                onSessionClick = { sadhak -> handleStartConsultation(sadhak, "session") },
                                onSadhakClick = { sadhak -> handleStartConsultation(sadhak, "chat") }
                            )
                        }
                    }

                    "remedy" -> {
                        BentoEditorialRemedyScreen(
                            isHindi = isHindi,
                            language = language,
                            modifier = Modifier.statusBarsPadding()
                        )
                    }

                    "dreams" -> {
                        DreamFeedScreen(
                            userId = targetUserDocId,
                            userName = userDisplayName,
                            onBackClick = { currentView = "today" },
                            onAskSadhakPrivate = { dreamText ->
                                val firstSadhak = saadhakList.firstOrNull()
                                if (firstSadhak != null) {
                                    handleStartConsultation(firstSadhak, "chat")
                                } else {
                                    currentView = "sadhak"
                                }
                                showToast("Dream sent to sadhak for interpretation")
                            },
                            showTopAppBar = false,
                            modifier = Modifier.statusBarsPadding()
                        )
                    }

                    "wallet" -> {
                        BentoEditorialWalletScreen(
                            balanceAmount = walletBalance,
                            onBackClick = { currentView = "today" },
                            onAddMoneyClick = { showRechargeSheet = true },
                            onGiftClick = { showToast(if (isHindi) "साधक दक्षिणा — शीघ्र उपलब्ध" else "Gifting a sadhak — coming soon") },
                            isHindi = isHindi,
                            language = language,
                            onRechargeWithAmount = { addedAmt ->
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
                                    showRechargeSheet = true
                                }
                            }
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

                    "japa" -> {
                        BentoJapaTimerScreen(onBackClick = { currentView = "today" })
                    }

                    "panchang" -> {
                        RemedyScreen(isHindi = isHindi)
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

                    "topic_faq" -> {
                        TopicFaqScreen(
                            topicId = selectedCardId ?: "family",
                            userId = targetUserDocId,
                            onBackClick = { currentView = "today" },
                            // Guest (guest_explorer, no Firebase auth): same exit as logout -> Login screen.
                            onRequestLogin = onLogoutClick,
                            onChatWithSadhak = { sadhak, prefill ->
                                chatPrefill = prefill
                                handleStartConsultation(sadhak, "chat")
                            }
                        )
                    }

                    "firestore_card" -> {
                        FirestoreCardDetailScreen(
                            cardId = selectedCardId ?: "family",
                            onBackClick = { currentView = "today" },
                            onStartChat = { sadhak -> handleStartConsultation(sadhak, "chat") },
                            onStartCall = { sadhak -> handleStartConsultation(sadhak, "call") },
                            sadhaks = saadhakList,
                            isHindi = isHindi,
                            language = language
                        )
                    }

                    "profile" -> {
                        UserProfileScreen(
                            userId = targetUserDocId,
                            userNameInitial = userDisplayName,
                            onBackClick = { currentView = "today" },
                            onLogoutClick = onLogoutClick
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

    // Pre-Consultation Disclaimer Bottom Sheet
    if (showPreConsultationWarningSheet && pendingConsultationSadhak != null) {
        PreConsultationDisclaimerBottomSheet(
            isHindi = isHindi,
            onConfirm = { dontShowAgain ->
                if (dontShowAgain) {
                    userSession.setConsultationDisclaimerDismissed(true)
                }
                val s = pendingConsultationSadhak
                val t = pendingConsultationType
                showPreConsultationWarningSheet = false
                pendingConsultationSadhak = null
                if (s != null) {
                    if (t == "chat") {
                        activeChatSadhak = s
                    } else {
                        activeCallingSadhak = s
                    }
                }
            },
            onDismiss = {
                showPreConsultationWarningSheet = false
                pendingConsultationSadhak = null
                chatPrefill = ""
            }
        )
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
        val categoryMap = mapOf(
            "family" to "Family Harmony",
            "career" to "Career & Business",
            "marriage" to "Marriage & Relationships",
            "health" to "Health & Peace"
        )
        PoochhoProblemConsultSheet(
            sadhaks = saadhakList,
            onStartChat = { sadhak, problemText ->
                handleStartConsultation(sadhak, "chat")
                if (problemText.isNotBlank()) {
                    showToast("Consultation started: $problemText")
                }
            },
            onStartCall = { sadhak, problemText ->
                handleStartConsultation(sadhak, "call")
                if (problemText.isNotBlank()) {
                    showToast("Call started: $problemText")
                }
            },
            onDismiss = {
                showPoochhoSheet = false
                selectedCategoryId = null
            },
            initialCategory = selectedCategoryId?.let { categoryMap[it] },
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
                showToast("Dream saved successfully")
            },
            onAskSadhak = { text ->
                val firstSadhak = saadhakList.firstOrNull()
                if (firstSadhak != null) {
                    handleStartConsultation(firstSadhak, "chat")
                } else {
                    currentView = "sadhak"
                }
                showToast("Dream sent to sadhak for interpretation")
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
                showProfileSheet = false
                currentView = "profile"
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
                showHelpSupportScreen = true
                showProfileSheet = false
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

@Composable
fun AnnouncementStrip(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFEF3C7)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Campaign,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF92400E)
            )
        }
    }
}

@Composable
fun BannersCarousel(banners: List<Banner>, context: android.content.Context) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(banners) { banner ->
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .height(140.dp)
                    .clickable {
                        if (banner.link.isNotBlank()) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(banner.link))
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
