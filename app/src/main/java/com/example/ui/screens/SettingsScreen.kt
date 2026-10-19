package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.models.ALL_AVATAR_OPTIONS
import com.example.ui.models.getAvatarById
import com.example.ui.theme.*
import com.example.ui.components.DevLogoIcon
import com.example.ui.components.DevWatermarkLogo
import com.example.utils.HapticFeedbackHelper
import com.example.utils.UserSession
import com.example.utils.WalletRepository

data class VedicMantraItem(
    val id: String,
    val titleHi: String,
    val deityHi: String,
    val shloka: String,
    val meaningHi: String,
    val benefitHi: String,
    val symbol: String = "🕉️",
    val defaultSaved: Boolean = false
)

val DEFAULT_VEDIC_MANTRAS = listOf(
    VedicMantraItem(
        id = "mantra_mahamrityunjaya",
        titleHi = "महामृत्युंजय मंत्र",
        deityHi = "भगवान शिव",
        shloka = "ॐ त्र्यम्बकं यजामहे सुगन्धिं पुष्टिवर्धनम्।\nउर्वारुकमिव बन्धनान्मृत्योर्मुक्षीय मामृतात्॥",
        meaningHi = "हम त्रिनेत्रधारी सुगंधित व पुष्टि करने वाले भगवान शिव की वंदना करते हैं। जिस प्रकार पका हुआ खरबूजा बेल से मुक्त होता है, वैसे ही हम मृत्यु व भय से मुक्त होकर अमरता प्राप्त करें।",
        benefitHi = "आरोग्य, अकाल मृत्यु से सुरक्षा, भय मुक्ति एवं मानसिक शांति।",
        symbol = "🔱",
        defaultSaved = true
    ),
    VedicMantraItem(
        id = "mantra_gayatri",
        titleHi = "गायत्री महामंत्र",
        deityHi = "सविता देव (सूर्य)",
        shloka = "ॐ भूर्भुवः स्वः तत्सवितुर्वरेण्यं\nभर्गो देवस्य धीमहि धियो यो नः प्रचोदयात्॥",
        meaningHi = "उस प्राणस्वरूप, दुःखनिवारक, सुखदाता, श्रेष्ठ, तेजस्वी परमपिता परमात्मा के तेज को हम अपनी बुद्धि में धारण करें, जो हमारी बुद्धि को सत्कर्मों की ओर प्रेरित करे।",
        benefitHi = "बुद्धि, तेज, सकारात्मक ऊर्जा, एकाग्रता एवं पाप निवारण।",
        symbol = "☀️",
        defaultSaved = true
    ),
    VedicMantraItem(
        id = "mantra_ganesh",
        titleHi = "श्री गणेश संकट नाशन मंत्र",
        deityHi = "प्रथम पूज्य श्री गणेश",
        shloka = "ॐ गं गणपतये नमः।\nवक्रतुण्ड महाकाय सूर्यकोटि समप्रभ।\nनिर्विघ्नं कुरु मे देव सर्वकार्येषु सर्वदा॥",
        meaningHi = "हे विशाल शरीर वाले, करोड़ सूर्यों के समान तेजस्वी महाप्रतापी भगवान गणेश! आप मेरे सभी कार्यों को बिना किसी विघ्न के सदैव पूर्ण करें।",
        benefitHi = "समस्त विघ्न-बाधाओं का शमन, व्यापार व अध्ययन में सफलता।",
        symbol = "🪔",
        defaultSaved = false
    ),
    VedicMantraItem(
        id = "mantra_lakshmi",
        titleHi = "महालक्ष्मी समृद्धि मंत्र",
        deityHi = "माता महालक्ष्मी",
        shloka = "ॐ श्रीं ह्रीं क्लीं श्रीं सिद्ध लक्ष्म्यै नमः॥\nॐ हिरण्यवर्णां हरिणीं सुवर्णरजतस्रजाम्।",
        meaningHi = "हे ऐश्वर्य, सौभाग्य एवं समृद्धि की अधिष्ठात्री माँ महालक्ष्मी! हमारे जीवन में धन, सात्विक संपदा व शांति का संचार करें।",
        benefitHi = "दरिद्रता का नाश, धन-धान्य वृद्धि, गृह शांति व ऐश्वर्य प्राप्ति।",
        symbol = "🪷",
        defaultSaved = false
    )
)

@Composable
fun SettingsScreen(
    userId: String,
    userNameInitial: String = "Sadhak",
    onBackClick: (() -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null,
    onOpenHelpSupport: (() -> Unit)? = null,
    isDarkTheme: Boolean = false,
    onThemeToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSession = remember { UserSession(context) }
    val effectiveUserId = userId.ifBlank { userSession.getUserId().ifBlank { "user_live" } }

    val initialSanitizedName = if (userNameInitial.isNotBlank() && userNameInitial != "Sadhak") userNameInitial else userSession.getUserName()
    val initialSanitizedPhone = userSession.getPhoneNumber()

    val haptic = LocalHapticFeedback.current

    // User State
    var userName by remember { mutableStateOf(initialSanitizedName.ifBlank { "राधिका शर्मा" }) }
    var phone by remember { mutableStateOf(initialSanitizedPhone) }
    var email by remember { mutableStateOf("support@devbhasha.com") }
    var dob by remember { mutableStateOf(userSession.getBirthDate().ifBlank { "14 अगस्त 1996" }) }
    var birthTime by remember { mutableStateOf(userSession.getBirthTime().ifBlank { "06:42 AM" }) }
    var birthPlace by remember { mutableStateOf(userSession.getBirthPlace().ifBlank { "वाराणसी, उत्तर प्रदेश" }) }
    var gender by remember { mutableStateOf("स्त्री") }
    var gotra by remember { mutableStateOf(userSession.getGotra().ifBlank { "कश्यप" }) }
    var avatarId by remember { mutableStateOf(userSession.getAvatarId()) }
    var savedMantraIds by remember { mutableStateOf(setOf("mantra_mahamrityunjaya", "mantra_gayatri")) }

    var isVerifiedSadhak by remember { mutableStateOf(true) }

    DisposableEffect(effectiveUserId) {
        val db = com.example.utils.FirestoreProvider.get(context)
        val userReg = db.collection("users").document(effectiveUserId)
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    val data = snap.data ?: emptyMap()
                    data["userName"]?.toString()?.takeIf { it.isNotBlank() }?.let { userName = it }
                    data["birthDate"]?.toString()?.takeIf { it.isNotBlank() }?.let { dob = it }
                    data["birthTime"]?.toString()?.takeIf { it.isNotBlank() }?.let { birthTime = it }
                    data["birthPlace"]?.toString()?.takeIf { it.isNotBlank() }?.let { birthPlace = it }
                    data["gotra"]?.toString()?.takeIf { it.isNotBlank() }?.let { gotra = it }
                    data["gender"]?.toString()?.takeIf { it.isNotBlank() }?.let { gender = it }
                }
            }

        val sadhakReg = db.collection("sadhaks").document(effectiveUserId)
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    val verified = snap.getBoolean("isVerified") ?: true
                    val status = snap.getString("status") ?: "APPROVED"
                    isVerifiedSadhak = verified && status == "APPROVED"
                } else {
                    isVerifiedSadhak = true
                }
            }

        onDispose {
            userReg.remove()
            sadhakReg.remove()
        }
    }

    // Consultations state
    val initialQuestions = remember {
        listOf<Map<String, Any>>(
            mapOf(
                "questionText" to "सपने में शिवलिंग पर जल चढ़ाना",
                "sadhakName" to "आचार्य देव शर्मा",
                "status" to "Answered",
                "providerAnswer" to "यह अत्यंत शुभ स्वप्न है। आपके रुके कार्य पूर्ण होंगे।",
                "type" to "dream"
            )
        )
    }
    var userQuestions by remember { mutableStateOf<List<Map<String, Any>>>(initialQuestions) }
    var isLoadingQuestions by remember { mutableStateOf(false) }

    // UI Dialog States
    var showAvatarPickerDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // DPDP Act Dialog States
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showMeraDataDialog by remember { mutableStateOf(false) }
    var showWithdrawConsentDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirm1Dialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirm2Dialog by remember { mutableStateOf(false) }
    var showReauthRequiredDialog by remember { mutableStateOf(false) }

    // Settings States
    var dailyNotificationsEnabled by remember { mutableStateOf(true) }

    val currentAvatar = remember(avatarId) { getAvatarById(avatarId) }
    val sadhakAvatarsList = remember { ALL_AVATAR_OPTIONS.filter { it.packageId == "sadhaks" } }

    val isProfileIncomplete = phone.isBlank()

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBackClick != null) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SaffronDeep
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "सेटिंग्स एवं नियंत्रण (Settings)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }
        },
        containerColor = Color(0xFFFAF8F5)
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
        ) {
            // 1. Profile Incomplete Banner if needed
            if (isProfileIncomplete) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_details_banner"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF6EA)),
                        border = BorderStroke(1.2.dp, Color(0xFFE9DB97))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFAF6EA)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "✍️", fontSize = 20.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "प्रोफ़ाइल विवरण पूर्ण करें",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF125157)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "सटीक कुंडली विचार, वैदिक परामर्श कॉल व अनुष्ठान हेतु अपना नाम व फ़ोन नंबर दर्ज करें।",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF125157),
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { showEditProfileDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF125157)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "जोड़ें (+)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 2. Consultation History Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("consultation_history_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFFAF6EA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFAF6EA)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "📜", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "परामर्श इतिहास (Consultation History)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "पूछे गए प्रश्न, समाधान व मार्गदर्शन",
                                        fontSize = 11.5.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            if (userQuestions.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SaffronSoftBg
                                ) {
                                    Text(
                                        text = "${userQuestions.size} प्रश्न",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronDeep,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFF8FAFC))
                        Spacer(modifier = Modifier.height(12.dp))

                        if (isLoadingQuestions) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = SaffronPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        } else if (userQuestions.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .padding(vertical = 20.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "🪔", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "अभी कोई परामर्श इतिहास उपलब्ध नहीं है",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "कुंडली, गृह दोष, विवाह या करियर संबंधी प्रश्नों के लिए हमारे वैदिक Sadhakों से संपर्क करें।",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                userQuestions.take(5).forEachIndexed { idx, q ->
                                    val questionText = q["questionText"] as? String ?: ""
                                    val status = q["status"] as? String ?: "Pending"
                                    val answer = q["providerAnswer"] as? String ?: ""
                                    val isAnswered = status.equals("Answered", ignoreCase = true) || answer.isNotBlank()
                                    var isExpanded by remember { mutableStateOf(false) }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isAnswered) Color(0xFFF0FDF4) else Color(0xFFFAF6EA),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isAnswered) Color(0xFFBBF7D0) else Color(0xFFE9DB97)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isExpanded = !isExpanded }
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "#${idx + 1}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isAnswered) GreenPrimary else Color(0xFF125157)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = questionText.ifBlank { "वैदिक परामर्श प्रश्न" },
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = TextDark,
                                                        maxLines = if (isExpanded) 10 else 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isAnswered) GreenPrimary.copy(alpha = 0.15f) else Color(0xFFD0BF75).copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = if (isAnswered) "समाधान प्राप्त ✓" else "प्रतीक्षारत ⏳",
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isAnswered) GreenPrimary else Color(0xFF125157),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            if (isExpanded && answer.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                HorizontalDivider(color = Color(0xFFDCFCE7))
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Sadhak का समाधान:",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GreenPrimary
                                                )
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = answer,
                                                    fontSize = 12.5.sp,
                                                    color = Color(0xFF166534),
                                                    lineHeight = 17.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Saved Mantras Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("saved_mantras_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFFAF6EA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFAF6EA)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "📿", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "संग्रहीत मंत्र एवं नित्य पाठ (Saved Mantras)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "नित्य जाप व साधना हेतु पावन वैदिक मंत्र",
                                        fontSize = 11.5.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFF8FAFC))
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DEFAULT_VEDIC_MANTRAS.forEach { mantra ->
                                val isSaved = savedMantraIds.contains(mantra.id)
                                var isExpanded by remember { mutableStateOf(false) }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSaved) Color(0xFFFFFDF8) else Color(0xFFFAFAFA),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSaved) Color(0xFFFAF6EA) else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isExpanded = !isExpanded }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = mantra.symbol, fontSize = 20.sp)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = mantra.titleHi,
                                                        fontSize = 13.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSaved) SaffronDeep else TextDark
                                                    )
                                                    Text(
                                                        text = mantra.deityHi,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    val newSet = if (isSaved) {
                                                        savedMantraIds - mantra.id
                                                    } else {
                                                        savedMantraIds + mantra.id
                                                    }
                                                    savedMantraIds = newSet
                                                    Toast.makeText(
                                                        context,
                                                        if (newSet.contains(mantra.id)) "${mantra.titleHi} संग्रह में जोड़ा गया" else "${mantra.titleHi} हटाया गया",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                                    contentDescription = "Save Mantra",
                                                    tint = if (isSaved) SaffronPrimary else Color(0xFF94A3B8),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        AnimatedVisibility(
                                            visible = isExpanded,
                                            enter = fadeIn() + expandVertically(),
                                            exit = fadeOut() + shrinkVertically()
                                        ) {
                                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFFAF6EA))
                                                        .padding(10.dp)
                                                ) {
                                                    Text(
                                                        text = mantra.shloka,
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SaffronDeep,
                                                        lineHeight = 18.sp,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "सरल अर्थ: ${mantra.meaningHi}",
                                                    fontSize = 11.5.sp,
                                                    color = Color(0xFF475569),
                                                    lineHeight = 16.sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "साधना लाभ: ${mantra.benefitHi}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = GreenPrimary,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    DevWatermarkLogo()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Settings & Support Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_support_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFFAF6EA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DevLogoIcon(
                                size = 36.dp,
                                elevation = 2.dp,
                                showGlow = false
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "सेटिंग्स एवं सहायता (Settings & Support)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "ऐप प्राथमिकताएं, गोपनीयता एवं संपर्क",
                                    fontSize = 11.5.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFF8FAFC))
                        Spacer(modifier = Modifier.height(6.dp))

                        // Theme Mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Filled.DarkMode else Icons.Outlined.LightMode,
                                    contentDescription = null,
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "डार्क मोड / लाइट मोड (Theme Mode)",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                    Text(
                                        text = if (isDarkTheme) "वर्तमान में डार्क मोड सक्रिय है" else "वर्तमान में लाइट मोड सक्रिय है",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = {
                                    onThemeToggle(it)
                                    Toast.makeText(
                                        context,
                                        if (it) "डार्क मोड सक्रिय किया गया" else "लाइट मोड सक्रिय किया गया",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SaffronPrimary
                                )
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Daily Notifications
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "दैनिक पंचांग व शुभ मुहूर्त सूचनाएं",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "प्रातःकाल शुभ चौघड़िया व राहुकाल अलर्ट",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                            Switch(
                                checked = dailyNotificationsEnabled,
                                onCheckedChange = {
                                    dailyNotificationsEnabled = it
                                    Toast.makeText(
                                        context,
                                        if (it) "दैनिक सूचनाएं सक्रिय की गईं" else "दैनिक सूचनाएं बंद की गईं",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SaffronPrimary
                                )
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Help Support
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSupportDialog = true }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = null,
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Sadhak सहायता एवं समाधान केंद्र",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "कॉल / चैट सहायता एवं प्रश्न निवारण",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Terms & Privacy
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTermsDialog = true }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Security,
                                    contentDescription = null,
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "नियम, शर्तें एवं गोपनीयता नीति",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "Sadhak गोपनीयता एवं सात्विक सेवा नियम",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Help FAQ
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenHelpSupport?.invoke() }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Help,
                                    contentDescription = null,
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "सहायता, समर्थन और डेटा FAQ",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "support@devbhasha.com और सुरक्षा FAQ",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 5. DPDP Data & Privacy Rights Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dpdp_privacy_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFFAF6EA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = null,
                                tint = SaffronPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "डेटा अधिकार एवं गोपनीयता (DPDP Act)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "आपकी सहमति, संग्रहीत डेटा व गोपनीयता अधिकार",
                                    fontSize = 11.5.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(4.dp))

                        // Privacy Policy
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPrivacyPolicyDialog = true }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🛡️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "गोपनीयता नीति (Privacy Policy)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Mera Data
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showMeraDataDialog = true }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📑", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "मेरा डेटा (My Personal Data)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Withdraw Consent
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showWithdrawConsentDialog = true }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("↩️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "सहमति वापस लें (Withdraw Consent)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFB3261E)
                                )
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Account Delete
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDeleteAccountConfirm1Dialog = true }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🗑️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "खाता हटाएं (Delete Account)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // 6. Logout Button
            if (onLogoutClick != null) {
                item {
                    OutlinedButton(
                        onClick = { showLogoutConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("logout_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, Color(0xFFEF4444)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "लॉगआउट करें (Logout)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
