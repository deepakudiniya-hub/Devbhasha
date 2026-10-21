package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.ui.components.DynamicPromoBanner
import com.example.ui.components.PromoBannerData
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.ProviderViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
import com.example.utils.FirestoreProvider
import com.example.utils.SadhakRepository
import com.example.utils.PriceLabels
import com.example.utils.SessionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    userName: String = "Sadhak",
    userId: String = "",
    currentUserId: String = userId,
    onLogoutClick: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onThemeToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSession = remember { UserSession(context) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val targetUserDocId = userId.ifBlank { userSession.getUserId() }

    // Request notification permission for Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ -> }
        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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
    val freeMins = 0 // free time is server-owned; never tracked or granted on device
    var freeDreamUsed by remember { mutableStateOf(false) }
    var isOfferClaimed by remember { mutableStateOf(false) }
    var isHindi by remember { mutableStateOf(userSession.isHindi()) } // Keep for logic compatibility
    var language by remember { mutableStateOf(userSession.getLanguage()) }

    val providerViewModel: ProviderViewModel = viewModel()
    val saadhakList by providerViewModel.sadhaks.collectAsStateWithLifecycle()
    val filteredSaadhaks by providerViewModel.filteredSadhaks.collectAsStateWithLifecycle()
    val isSadhaksLoading by providerViewModel.isLoading.collectAsStateWithLifecycle()
    val activeChatSadhak by providerViewModel.activeChatSadhak.collectAsStateWithLifecycle()
    val activeChatSessionId by providerViewModel.activeChatSessionId.collectAsStateWithLifecycle()
    val activeCallingSadhak by providerViewModel.activeCallingSadhak.collectAsStateWithLifecycle()
    val activeCallingCallId by providerViewModel.activeCallingCallId.collectAsStateWithLifecycle()

    // Navigation View: "today", "sadhak", "remedy", "dreams", "wallet"
    var currentView by remember { mutableStateOf("today") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedEntryForDetail by remember { mutableStateOf<DreamJournalEntry?>(null) }

    // Sheets & Dialogs
    var currentAvatarId by remember { mutableStateOf(userSession.getAvatarId()) }
    var showAvatarPickerSheet by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }
    var showRechargeSheet by remember { mutableStateOf(false) }
    var showBookingSheet by remember { mutableStateOf(false) }
    var showPoochhoSheet by remember { mutableStateOf(false) }
    var showDreamsModalSheet by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedCardId by remember { mutableStateOf<String?>(null) }
    var showLangDropdown by remember { mutableStateOf(false) }

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
                tag = "आत्म-बोध (Awakening)",
                tags = listOf("#deepak", "#river", "#spiritual"),
                timestamp = System.currentTimeMillis() - 86400000L,
                meaning = "ज्ञान, मार्गदर्शन और जीवन में नए प्रकाश का प्रतीक है।",
                answeredBy = "योगी आनंद नाथ"
            )
        )
    }

    var dreamList by remember { mutableStateOf(initialDreams) }

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
                                    showToast("पेमेंट सफल! सपना Sadhak '${submitRes.sadhakName}' को भेज दिया गया।")
                                }
                                is DreamSubmitResult.NoVerifiedSadhak -> {
                                    showToast("पेमेंट सफल! राशि वॉलेट में जुड़ गई। अभी कोई Sadhak उपलब्ध नहीं है।")
                                }
                                is DreamSubmitResult.InsufficientBalance -> {
                                    showToast("पेमेंट सफल! राशि वॉलेट में जुड़ गई। कृपया दोबारा सपना भेजें।")
                                }
                                is DreamSubmitResult.Error -> {
                                    showToast("पेमेंट सफल! राशि वॉलेट में जुड़ गई। सपना भेजने में Error: ${submitRes.message}")
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

    // Call / Chat Session Starter via ProviderViewModel
    val handleStartConsultation: (SadhakItem, String) -> Unit = { sadhak, type ->
        HapticFeedbackHelper.playClick(haptic)
        providerViewModel.startConsultation(
            sadhak = sadhak,
            type = type,
            userId = targetUserDocId,
            onStarted = { id ->
                showToast(if (type == "chat") "चैट सत्र शुरू हो रहा है..." else "कॉल कनेक्ट हो रही है...")
            }
        )
    }

    fun handleClaimTicket() {
        HapticFeedbackHelper.playClick(haptic)
        // Free trial (first dream chat, 5 min, once per phone number) is granted
        // only by the server; nothing is credited on the device.
        isOfferClaimed = true
        showToast("${PriceLabels.FREE_TRIAL_BADGE} — पात्रता सत्र शुरू होने पर जाँची जाएगी")
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
                providerViewModel.endActiveChat()
            },
            onStartCall = {
                val s = activeChatSadhak!!
                providerViewModel.endActiveChat()
                providerViewModel.startConsultation(s, "call", targetUserDocId, onStarted = {})
            },
            onLowBalance = { showRechargeSheet = true },
            onAddBalance = { showRechargeSheet = true },
            sessionId = activeChatSessionId
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
            onEndCall = {
                providerViewModel.endActiveCall()
            },
            callDocId = activeCallingCallId
        )
        return
    }

    // Fixed BackHandler: Returns to 'today' tab when in secondary view, enables system back when already on 'today'
    BackHandler(enabled = currentView != "today") {
        currentView = "today"
    }

    // Main Scaffold in Bento Editorial Design
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background, // Theme background
        topBar = {
            if (currentView in listOf("today", "sadhak", "dreams", "remedy")) {
                Surface(
                    color = Color.White, // Pure white background
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.statusBarsPadding()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (userDisplayName.isBlank()) "नमस्ते, Sadhak 🙏" else "नमस्ते, $userDisplayName 🙏",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E2A2A) // Main text
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Language toggle removed from here in favor of Profile Sheet

                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFDCCFB4)), // Outline
                                    modifier = Modifier.clickable { currentView = "wallet" }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "₹${walletBalance.toInt()}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E2A2A)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Balance",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFF0F5C5C)
                                        )
                                    }
                                }
                                Surface(
                                    shape = CircleShape,
                                    border = BorderStroke(1.5.dp, Color(0xFF0F5C5C)), // Gold
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable { currentView = "profile" }
                                ) {
                                    Image(
                                        painter = painterResource(id = getAvatarDrawableRes(currentAvatarId)),
                                        contentDescription = "प्रोफ़ाइल",
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
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
                        var isVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            isVisible = true
                        }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF8F9FA)),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            item {
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(animationSpec = tween(400, delayMillis = 0)) +
                                            slideInVertically(animationSpec = tween(400, delayMillis = 0), initialOffsetY = { 60 })
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = { Text(if (isHindi) "खोजें..." else "Search...") },
                                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                            item {
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(animationSpec = tween(400, delayMillis = 0)) +
                                            slideInVertically(animationSpec = tween(400, delayMillis = 0), initialOffsetY = { 60 })
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    DynamicPromoBanner(
                                        dataList = listOf(
                                            PromoBannerData(
                                                title = if (isHindi) "आज का आध्यात्मिक उत्सव" else "Today's Spiritual Event",
                                                description = if (isHindi) "अभी भाग लें और विशेष आशीष प्राप्त करें।" else "Join now and receive special blessings.",
                                                buttonText = if (isHindi) "भाग लें" else "Join Now"
                                            ),
                                            PromoBannerData(
                                                title = if (isHindi) "वैदिक ज्योतिष परामर्श" else "Vedic Astrology Consult",
                                                description = if (isHindi) "अपने भविष्य के बारे में जानें।" else "Gain insights into your future.",
                                                buttonText = if (isHindi) "अभी देखें" else "Check Now",
                                                gradientColors = listOf(Color(0xFF8B4513), Color(0xFFD2691E))
                                            )
                                        ),
                                        onButtonClick = { _ -> showToast("उत्सव में आपका स्वागत है! 🙏") }
                                    )
                                    }
                                }
                            }
                            item {
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(animationSpec = tween(400, delayMillis = 0)) +
                                            slideInVertically(animationSpec = tween(400, delayMillis = 0), initialOffsetY = { 60 })
                                ) {
                                    BentoEditorialPoochhaHeroCard(
                                        onAskClick = { showPoochhoSheet = true },
                                        isHindi = isHindi,
                                        language = language
                                    )
                                }
                            }
                            item {
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(animationSpec = tween(400, delayMillis = 25)) +
                                            slideInVertically(animationSpec = tween(400, delayMillis = 25), initialOffsetY = { 60 })
                                ) {
                                    DailyAffirmationCard(isHindi = isHindi)
                                }
                            }
                            item {
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(animationSpec = tween(400, delayMillis = 50)) +
                                            slideInVertically(animationSpec = tween(400, delayMillis = 50), initialOffsetY = { 60 })
                                ) {
                                    BentoEditorialAskBar(
                                        query = searchQuery,
                                        onQueryChange = { searchQuery = it },
                                        onGoClick = { currentView = "ai_sahayak" },
                                        isHindi = isHindi,
                                        language = language
                                    )
                                }
                            }
                            item {
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(animationSpec = tween(400, delayMillis = 100)) +
                                            slideInVertically(animationSpec = tween(400, delayMillis = 100), initialOffsetY = { 60 })
                                ) {
                                    BentoEditorialPanchangHeadline(
                                        isHindi = isHindi,
                                        language = language
                                    )
                                }
                            }
                            item {
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(animationSpec = tween(400, delayMillis = 150)) +
                                            slideInVertically(animationSpec = tween(400, delayMillis = 150), initialOffsetY = { 60 })
                                ) {
                                    BentoEditorialToolsGrid(
                                        currentView = currentView,
                                        onPoochhoClick = { showPoochhoSheet = true },
                                        onDreamsClick = { currentView = "dreams" },
                                        onKundliClick = { currentView = "kundli" },
                                        onTarotClick = { currentView = "tarot" },
                                        onMatchClick = { currentView = "match" },
                                        onHabitsClick = { currentView = "remedy" },
                                        onTimerClick = { currentView = "remedy" },
                                        isHindi = isHindi,
                                        language = language
                                    )
                                }
                            }
                            item {
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(animationSpec = tween(400, delayMillis = 200)) +
                                            slideInVertically(animationSpec = tween(400, delayMillis = 200), initialOffsetY = { 60 })
                                ) {
                                    BentoEditorialOnlineSadhaksCarousel(
                                        sadhaks = filteredSaadhaks,
                                        onChatClick = { sadhak -> handleStartConsultation(sadhak, "chat") },
                                        onCallClick = { sadhak -> handleStartConsultation(sadhak, "call") },
                                        onSeeAllClick = { currentView = "sadhak" },
                                        isHindi = isHindi,
                                        language = language
                                    )
                                }
                            }
                            item {
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(animationSpec = tween(400, delayMillis = 220)) +
                                            slideInVertically(animationSpec = tween(400, delayMillis = 220), initialOffsetY = { 60 })
                                ) {
                                    BentoEditorialHoroscopeCard(
                                        isHindi = isHindi
                                    )
                                }
                            }

                            item {
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(animationSpec = tween(400, delayMillis = 280)) +
                                            slideInVertically(animationSpec = tween(400, delayMillis = 280), initialOffsetY = { 60 })
                                ) {
                                    BentoEditorialReferAndEarn()
                                }
                            }
                            item {
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(animationSpec = tween(400, delayMillis = 320)) +
                                            slideInVertically(animationSpec = tween(400, delayMillis = 320), initialOffsetY = { 60 })
                                ) {
                                    BentoEditorialExpertsSection(
                                        sadhaks = filteredSaadhaks,
                                        onChatClick = { sadhak -> handleStartConsultation(sadhak, "chat") },
                                        onCallClick = { sadhak -> handleStartConsultation(sadhak, "call") },
                                        onSeeAllClick = { currentView = "sadhak" },
                                        isHindi = isHindi,
                                        language = language
                                    )
                                }
                            }
                        }
                    }

                    "sadhak" -> {
                        SadhakScreen(
                            sadhakList = saadhakList,
                            onChatClick = { sadhak -> handleStartConsultation(sadhak, "chat") },
                            onCallClick = { sadhak -> handleStartConsultation(sadhak, "call") },
                            onSadhakClick = { sadhak -> handleStartConsultation(sadhak, "chat") }
                        )
                    }

                    "ai_sahayak" -> {
                        AiSahayakChatScreen(
                            onBackClick = { currentView = "today" },
                            isHindi = isHindi,
                            language = language
                        )
                    }

                    "remedy" -> {
                        UpayScreen(isHindi = isHindi, language = language)
                    }

                    "dreams" -> {
                        SvapnaScreen(
                            userId = targetUserDocId,
                            userName = userDisplayName,
                            dreamHistory = dreamList,
                            onChatWithSadhak = { currentView = "sadhak" }
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
                            onLogoutClick = onLogoutClick,
                            onOpenHelpSupport = { currentView = "help_support" },
                            onOpenSettings = { currentView = "settings" },
                            onOpenConsultationHistory = { currentView = "consultation_history" },
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = onThemeToggle
                        )
                    }

                    "consultation_history" -> {
                        ConsultationHistoryScreen(
                            userId = targetUserDocId,
                            onBackClick = { currentView = "profile" },
                            sadhaks = saadhakList,
                            isHindi = isHindi
                        )
                    }

                    "settings" -> {
                        SettingsScreen(
                            userId = targetUserDocId,
                            userNameInitial = userDisplayName,
                            onBackClick = { currentView = "profile" },
                            onLogoutClick = onLogoutClick,
                            onOpenHelpSupport = { currentView = "help_support" },
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = onThemeToggle
                        )
                    }

                    "help_support" -> {
                        HelpSupportScreen(
                            onBack = { currentView = "profile" }
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
            language = language,
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
            "family" to "पारिवारिक क्लेश",
            "career" to "करियर / व्यापार",
            "marriage" to "विवाह / संबंध",
            "health" to "स्वास्थ्य / शांति"
        )
        PoochhoProblemConsultSheet(
            sadhaks = saadhakList,
            onStartChat = { sadhak, problemText ->
                handleStartConsultation(sadhak, "chat")
                if (problemText.isNotBlank()) {
                    showToast("परामर्श शुरू: $problemText")
                }
            },
            onStartCall = { sadhak, problemText ->
                handleStartConsultation(sadhak, "call")
                if (problemText.isNotBlank()) {
                    showToast("कॉल शुरू: $problemText")
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
                showToast("सपना सुरक्षित हो गया")
            },
            onAskSadhak = { text ->
                val firstSadhak = saadhakList.firstOrNull()
                if (firstSadhak != null) {
                    handleStartConsultation(firstSadhak, "chat")
                } else {
                    currentView = "sadhak"
                }
                showToast("सपना Sadhak को व्याख्या हेतु भेजा गया")
            },
            onOpenJournal = {
                currentView = "dreams"
            },
            onDismiss = { showDreamsModalSheet = false },
            isHindi = isHindi,
            language = language
        )
    }

    // Bottom Sheet: Devbhasha Avatar Picker Sheet
    if (showAvatarPickerSheet) {
        DevbhashaAvatarPickerSheet(
            currentAvatarId = currentAvatarId,
            onDismiss = { showAvatarPickerSheet = false },
            onAvatarSelected = { newAvatarId ->
                currentAvatarId = newAvatarId
                userSession.setAvatarId(newAvatarId)
                UserManager.updateAvatarPreference(targetUserDocId, newAvatarId)
                showToast("अवतार सफलतापूर्वक सहेजा गया ✓")
            }
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
                val helpMsg = when(language) {
                    "hi" -> "सहायता एवं समर्थन — शीघ्र चालू होगा"
                    "hinglish" -> "Help & support — jald hi shuru hoga"
                    else -> "Help & support — opening soon"
                }
                showToast(helpMsg)
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
