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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.AVATAR_PACKAGES
import com.example.ui.models.ALL_AVATAR_OPTIONS
import com.example.ui.models.getAvatarById
import com.example.ui.theme.*
import com.example.utils.UserSession
import com.example.utils.WalletRepository

// Preset Vedic Sacred Mantras for the "Saved Mantras" section
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

/**
 * Sanitizes any raw string to prevent awkward values like 'साधक (Facebook)', 'null', or blank strings.
 * Gracefully returns 'प्रिय साधक' when user's name is missing.
 */
private fun sanitizeDevoteeName(raw: String?): String {
    val trimmed = raw?.trim() ?: ""
    if (trimmed.isBlank() ||
        trimmed.equals("null", ignoreCase = true) ||
        trimmed.contains("Facebook", ignoreCase = true) ||
        trimmed == "साधक" ||
        trimmed.startsWith("user_", ignoreCase = true)
    ) {
        return "दीपक जी"
    }
    return trimmed
}

/**
 * Sanitizes phone number string, returning blank if missing or dummy.
 */
private fun sanitizeDevoteePhone(raw: String?): String {
    val trimmed = raw?.trim() ?: ""
    if (trimmed.isBlank() ||
        trimmed.equals("null", ignoreCase = true) ||
        trimmed.startsWith("user_", ignoreCase = true) ||
        trimmed == "9876543210"
    ) {
        return ""
    }
    return trimmed
}

@Composable
fun UserProfileScreen(
    userId: String,
    userNameInitial: String = "साधक",
    onLogoutClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSession = remember { UserSession(context) }
    val effectiveUserId = userId.ifBlank { userSession.getUserId().ifBlank { "user_live" } }

    val initialSanitizedName = sanitizeDevoteeName(
        if (userNameInitial.isNotBlank() && userNameInitial != "साधक") userNameInitial else userSession.getUserName()
    )
    val initialSanitizedPhone = sanitizeDevoteePhone(userSession.getPhoneNumber())

    // User State
    var userName by remember { mutableStateOf(initialSanitizedName) }
    var phone by remember { mutableStateOf(initialSanitizedPhone) }
    var email by remember { mutableStateOf("Deepakudiniya@gmail.com") }
    var dob by remember { mutableStateOf("") }
    var birthTime by remember { mutableStateOf("") }
    var birthPlace by remember { mutableStateOf("") }
    var gotra by remember { mutableStateOf("") }
    var avatarId by remember { mutableStateOf("om") }
    var walletBalance by remember { mutableDoubleStateOf(0.0) }
    DisposableEffect(effectiveUserId) {
        val reg = WalletRepository.observeBalance(effectiveUserId) { walletBalance = it }
        onDispose { reg.remove() }
    }
    var savedMantraIds by remember { mutableStateOf(setOf("mantra_mahamrityunjaya", "mantra_gayatri")) }

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

    // Settings States
    var dailyNotificationsEnabled by remember { mutableStateOf(true) }

    val currentAvatar = remember(avatarId) { getAvatarById(avatarId) }

    val isProfileIncomplete = phone.isBlank()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F5))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
    ) {
        // -------------------------------------------------------------
        // 1. Premium Vedic Profile Header Card
        // -------------------------------------------------------------
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, Color(0xFFFED7AA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFF9F0), Color.White)
                            )
                        )
                        .padding(18.dp)
                ) {
                    // Top Badge: Devotee Status & Saffron Om
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SaffronSoftBg,
                            border = BorderStroke(1.dp, Color(0xFFFDBA74))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🕉️", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "वैदिक साधक प्रोफाइल",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronDeep
                                )
                            }
                        }

                        // Tap to copy UID
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText("Devbhasha UID", effectiveUserId)
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(context, "साधक UID कॉपी हुआ!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "UID: ${effectiveUserId.take(6)}...",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy UID",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Avatar & Core Devotee Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Spiritual Avatar with halo border
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, Color(0xFFF59E0B), CircleShape)
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(currentAvatar.bgColor)
                                .clickable { showAvatarPickerDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentAvatar.symbol,
                                fontSize = 32.sp,
                                color = Color.White
                            )

                            // Small edit pencil badge
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(SaffronPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "अवतार बदलें",
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            // User Name
                            Text(
                                text = userName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Phone Row: clean handling of missing phone
                            if (phone.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Phone,
                                        contentDescription = null,
                                        tint = GreenPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = phone,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFEF3C7),
                                    border = BorderStroke(0.8.dp, Color(0xFFFDE68A)),
                                    modifier = Modifier.clickable { showEditProfileDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Phone,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "फ़ोन नंबर जोड़ें (+)",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }

                            // Email or Astrological Gotra / DOB if entered
                            if (dob.isNotBlank() || gotra.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = buildString {
                                            if (dob.isNotBlank()) append("जन्म: $dob")
                                            if (dob.isNotBlank() && gotra.isNotBlank()) append(" • ")
                                            if (gotra.isNotBlank()) append("गोत्र: $gotra")
                                        },
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else if (email.isNotBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Email,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = email,
                                        fontSize = 11.5.sp,
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dakshina & Sadhana Counter Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF7ED))
                            .border(1.dp, Color(0xFFFFEDD5), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "वॉलेट दक्षिणा शेष",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9A3412),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "₹${walletBalance.toInt()}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SaffronDeep
                                )
                            }
                        }

                        // Consultations summary count
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "परामर्श प्रश्न",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "${userQuestions.size}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons Row: Edit Profile & Change Avatar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // "विवरण जोड़ें / संपादित करें" Button
                        Button(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("edit_profile_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isProfileIncomplete) Color(0xFFEA580C) else SaffronPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (isProfileIncomplete) Icons.Filled.AddCircle else Icons.Filled.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isProfileIncomplete) "विवरण जोड़ें (Add Details)" else "विवरण बदलें (Edit)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // "अवतार बदलें" Button
                        OutlinedButton(
                            onClick = { showAvatarPickerDialog = true },
                            modifier = Modifier
                                .weight(0.9f)
                                .height(44.dp)
                                .testTag("change_avatar_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, SaffronPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SaffronPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = SaffronPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "अवतार चुनें",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 1.1 Graceful "Profile Incomplete" Alert Card if Name/Phone missing
        // -------------------------------------------------------------
        if (isProfileIncomplete) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_details_banner"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.2.dp, Color(0xFFFDE68A))
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
                                .background(Color(0xFFFEF3C7)),
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
                                color = Color(0xFF92400E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "सटीक कुंडली विचार, वैदिक परामर्श कॉल व अनुष्ठान हेतु अपना नाम व फ़ोन नंबर दर्ज करें।",
                                fontSize = 11.5.sp,
                                color = Color(0xFFB45309),
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { showEditProfileDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
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

        // -------------------------------------------------------------
        // 2. Consultation History Card (परामर्श इतिहास)
        // -------------------------------------------------------------
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("consultation_history_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
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
                                    .background(Color(0xFFFFF7ED)),
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
                        // Serene Empty State
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
                                text = "कुंडली, गृह दोष, विवाह या करियर संबंधी प्रश्नों के लिए हमारे वैदिक साधकों से संपर्क करें।",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    } else {
                        // List of recent questions
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            userQuestions.take(5).forEachIndexed { idx, q ->
                                val questionText = q["questionText"] as? String ?: ""
                                val status = q["status"] as? String ?: "Pending"
                                val answer = q["providerAnswer"] as? String ?: ""
                                val isAnswered = status.equals("Answered", ignoreCase = true) || answer.isNotBlank()
                                var isExpanded by remember { mutableStateOf(false) }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isAnswered) Color(0xFFF0FDF4) else Color(0xFFFFFBEB),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isAnswered) Color(0xFFBBF7D0) else Color(0xFFFDE68A)
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
                                                    color = if (isAnswered) GreenPrimary else Color(0xFFD97706)
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
                                                color = if (isAnswered) GreenPrimary.copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = if (isAnswered) "समाधान प्राप्त ✓" else "प्रतीक्षारत ⏳",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isAnswered) GreenPrimary else Color(0xFFB45309),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        if (isExpanded && answer.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = Color(0xFFDCFCE7))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "साधक का समाधान:",
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

        // -------------------------------------------------------------
        // 3. Saved Mantras & Daily Japa Card (संग्रहीत मंत्र)
        // -------------------------------------------------------------
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saved_mantras_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
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
                                    .background(Color(0xFFFFF7ED)),
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
                                    if (isSaved) Color(0xFFFED7AA) else Color(0xFFE2E8F0)
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

                                        // Bookmark icon to toggle save
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

                                    // Expanded Shloka & Meaning
                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        Column(modifier = Modifier.padding(top = 10.dp)) {
                                            // Shloka in divine box
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFFFF7ED))
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
                                            Text(
                                                text = "साधना लाभ: ${mantra.benefitHi}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = GreenPrimary
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

        // -------------------------------------------------------------
        // 4. Settings & Support Card (सेटिंग्स एवं सहायता)
        // -------------------------------------------------------------
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_support_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF7ED)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⚙️", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
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

                    // Row 1: Daily Panchang & Muhurat Notifications
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

                    // Row 2: Help & Astrological Support
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
                                    text = "साधक सहायता एवं समाधान केंद्र",
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

                    // Row 3: Terms & Privacy Policy
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
                                    text = "साधक गोपनीयता एवं सात्विक सेवा नियम",
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

        // -------------------------------------------------------------
        // 5. Logout Button
        // -------------------------------------------------------------
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

    // =========================================================================
    // Dialog 1: Comprehensive "Edit Profile" Dialog (Name, Phone, DOB, Gotra)
    // =========================================================================
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(if (userName == "प्रिय साधक") "" else userName) }
        var editPhone by remember { mutableStateOf(phone) }
        var editEmail by remember { mutableStateOf(email) }
        var editDob by remember { mutableStateOf(dob) }
        var editBirthTime by remember { mutableStateOf(birthTime) }
        var editBirthPlace by remember { mutableStateOf(birthPlace) }
        var editGotra by remember { mutableStateOf(gotra) }
        var isSaving by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSaving) showEditProfileDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🕉️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "साधक व्यक्तिगत विवरण",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextDark
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "सटीक कुंडली विचार, वैदिक परामर्श एवं पूजा संकल्प हेतु अपना प्रामाणिक विवरण दर्ज करें:",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )

                    // 1. Full Name
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("पूरा नाम (Full Name) *") },
                        placeholder = { Text("उदा. अनुराग शर्मा") },
                        leadingIcon = {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = SaffronPrimary)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_edit_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            focusedLabelColor = SaffronPrimary
                        )
                    )

                    // 2. Phone Number
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("फ़ोन नंबर (Phone Number) *") },
                        placeholder = { Text("उदा. 9876543210") },
                        leadingIcon = {
                            Icon(Icons.Filled.Phone, contentDescription = null, tint = SaffronPrimary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_edit_phone"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            focusedLabelColor = SaffronPrimary
                        )
                    )

                    // 3. Email (Optional)
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("ईमेल (Email - वैकल्पिक)") },
                        placeholder = { Text("name@example.com") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = SaffronPrimary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            focusedLabelColor = SaffronPrimary
                        )
                    )

                    // 4. Date of Birth
                    OutlinedTextField(
                        value = editDob,
                        onValueChange = { editDob = it },
                        label = { Text("जन्मतिथि (Date of Birth)") },
                        placeholder = { Text("DD/MM/YYYY (उदा. 15/08/1995)") },
                        leadingIcon = {
                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = SaffronPrimary)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_edit_dob"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            focusedLabelColor = SaffronPrimary
                        )
                    )

                    // 5. Birth Place (Optional for Kundali)
                    OutlinedTextField(
                        value = editBirthPlace,
                        onValueChange = { editBirthPlace = it },
                        label = { Text("जन्म स्थान (नगर / राज्य)") },
                        placeholder = { Text("उदा. वाराणसी, उत्तर प्रदेश") },
                        leadingIcon = {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = SaffronPrimary)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            focusedLabelColor = SaffronPrimary
                        )
                    )

                    // 6. Gotra (Spiritual param for Puja)
                    OutlinedTextField(
                        value = editGotra,
                        onValueChange = { editGotra = it },
                        label = { Text("गोत्र (Gotra - पूजा संकल्प हेतु)") },
                        placeholder = { Text("उदा. कश्यप / भारद्वाज / वत्स") },
                        leadingIcon = {
                            Icon(Icons.Outlined.SelfImprovement, contentDescription = null, tint = SaffronPrimary)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            focusedLabelColor = SaffronPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = editName.trim()
                        val finalCleanName = if (trimmedName.isBlank() || trimmedName == "null" || trimmedName.contains("Facebook", true)) {
                            "दीपक जी"
                        } else {
                            trimmedName
                        }
                        val trimmedPhone = sanitizeDevoteePhone(editPhone)

                        // Save to local UserSession
                        userSession.saveUserSession(
                            userName = finalCleanName,
                            isLoggedIn = true,
                            userId = effectiveUserId,
                            phoneNumber = trimmedPhone
                        )

                        // Update local Compose state
                        userName = finalCleanName
                        phone = trimmedPhone
                        email = editEmail.trim()
                        dob = editDob.trim()
                        birthTime = editBirthTime.trim()
                        birthPlace = editBirthPlace.trim()
                        gotra = editGotra.trim()

                        isSaving = false
                        showEditProfileDialog = false
                        Toast.makeText(context, "विवरण सफलतापूर्वक सुरक्षित हुआ!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    enabled = !isSaving
                ) {
                    Text(
                        text = if (isSaving) "सुरक्षित हो रहा है..." else "विवरण सहेजें (Save)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditProfileDialog = false },
                    enabled = !isSaving
                ) {
                    Text("रद्द करें", color = TextMuted)
                }
            }
        )
    }

    // =========================================================================
    // Dialog 2: Sacred Avatar Selector Dialog
    // =========================================================================
    if (showAvatarPickerDialog) {
        var selectedPackageId by remember { mutableStateOf("symbols") }
        var tempSelectedAvatarId by remember { mutableStateOf(avatarId) }

        val filteredAvatars = remember(selectedPackageId) {
            ALL_AVATAR_OPTIONS.filter { it.packageId == selectedPackageId }
        }

        AlertDialog(
            onDismissRequest = { showAvatarPickerDialog = false },
            title = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🕉️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "पवित्र आध्यात्मिक अवतार",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextDark
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "प्रोफ़ाइल में अपनी आध्यात्मिक साधना के अनुकूल प्रतीक या साधक अवतार चुनें।",
                        fontSize = 11.5.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Package Tabs (Chips)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AVATAR_PACKAGES.forEach { pkg ->
                            val isSelected = pkg.id == selectedPackageId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPackageId = pkg.id },
                                label = {
                                    Text(
                                        text = pkg.titleHi,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid of Avatar Options (3 columns)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        filteredAvatars.chunked(3).forEach { rowAvatars ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                rowAvatars.forEach { option ->
                                    val isSelected = option.id == tempSelectedAvatarId
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { tempSelectedAvatarId = option.id }
                                            .padding(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(option.bgColor)
                                                .border(
                                                    width = if (isSelected) 2.5.dp else 0.dp,
                                                    color = if (isSelected) Color(0xFFF59E0B) else Color.Transparent,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = option.symbol,
                                                fontSize = 26.sp,
                                                color = Color.White
                                            )

                                            // Selected Checkmark Badge
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .align(Alignment.TopEnd)
                                                        .clip(CircleShape)
                                                        .background(Color.White)
                                                        .padding(2.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(CircleShape)
                                                            .background(GreenPrimary),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Check,
                                                            contentDescription = "Selected",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = option.nameHi,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) SaffronDeep else TextDark,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        avatarId = tempSelectedAvatarId
                        showAvatarPickerDialog = false
                        val chosenName = getAvatarById(tempSelectedAvatarId).nameHi
                        Toast.makeText(context, "$chosenName अवतार सफलतापूर्वक सेट हुआ!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("अवतार सेट करें", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAvatarPickerDialog = false }) {
                    Text("रद्द करें", color = TextMuted)
                }
            }
        )
    }

    // =========================================================================
    // Dialog 3: Help & Support Dialog
    // =========================================================================
    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🕉️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "साधक सहायता केंद्र",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextDark
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "देवभाषा वैदिक सहायता केंद्र में आपका स्वागत है। किसी भी तकनीकी सहायता, वॉलेट रिचार्ज या परामर्श प्रश्न हेतु संपर्क करें:",
                        fontSize = 12.5.sp,
                        color = Color(0xFF475569),
                        lineHeight = 17.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF7ED),
                        border = BorderStroke(1.dp, Color(0xFFFED7AA))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ईमेल सहायता:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDeep
                            )
                            Text(
                                text = "support@devbhasha.org",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "उपलब्धता समय:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDeep
                            )
                            Text(
                                text = "सोमवार से रविवार: प्रातः 8:00 से रात्रि 9:00",
                                fontSize = 12.sp,
                                color = TextDark
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSupportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("ठीक है", color = Color.White)
                }
            }
        )
    }

    // =========================================================================
    // Dialog 4: Terms & Privacy Dialog
    // =========================================================================
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📜", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "नियम एवं गोपनीयता नीति",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextDark
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1. सात्विक एवं वैदिक परंपरा: देवभाषा ऐप पर सभी परामर्श भारतीय वैदिक ज्योतिष, कर्मकांड एवं सनातन परंपरा पर आधारित हैं।",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "2. डेटा गोपनीयता: आपके जन्म विवरण (जन्म समय, स्थान, जन्मतिथि) का उपयोग केवल कुंडली एवं ग्रह विचार हेतु पूर्णतः सुरक्षित रखा जाता है।",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "3. वॉलेट दक्षिणा: वॉलेट में जोड़ी गई राशि का उपयोग साधकों से परामर्श एवं दक्षिणा हेतु किया जाता है।",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("स्वीकार है", color = Color.White)
                }
            }
        )
    }

    // =========================================================================
    // Dialog 5: Logout Confirmation Dialog
    // =========================================================================
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = "लॉगआउट की पुष्टि",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = "क्या आप सचमुच अपने साधक खाते से बाहर निकलना चाहते हैं?",
                    fontSize = 13.5.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogoutClick?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("हाँ, लॉगआउट करें", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("रद्द करें", color = TextMuted)
                }
            }
        )
    }
}
