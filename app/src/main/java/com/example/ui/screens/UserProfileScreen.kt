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

// Preset Vedic Sacred Mantras for the \"Saved Mantras\" section
data class VedicMantraItem(
    val id: String,
    val titleHi: String,
    val deityHi: String,
    val shloka: String,
    val meaningHi: String,
    val benefitHi: String,
    val symbol: String = "\ud83d\udd49\ufe0f",
    val defaultSaved: Boolean = false
)

val DEFAULT_VEDIC_MANTRAS = listOf(
    VedicMantraItem(
        id = "mantra_mahamrityunjaya",
        titleHi = "\u092e\u0939\u093e\u092e\u0943\u0924\u094d\u092f\u0941\u0902\u091c\u092f \u092e\u0902\u0924\u094d\u0930",
        deityHi = "\u092d\u0917\u0935\u093e\u0928 \u0936\u093f\u0935",
        shloka = "\u0950 \u0924\u094d\u0930\u094d\u092f\u092e\u094d\u092c\u0915\u0902 \u092f\u091c\u093e\u092e\u0939\u0947 \u0938\u0941\u0917\u0928\u094d\u0927\u093f\u0902 \u092a\u0941\u0937\u094d\u091f\u093f\u0935\u0930\u094d\u0927\u0928\u092e\u094d\u0964\n\u0909\u0930\u094d\u0935\u093e\u0930\u0941\u0915\u092e\u093f\u0935 \u092c\u0928\u094d\u0927\u0928\u093e\u0928\u094d\u092e\u0943\u0924\u094d\u092f\u094b\u0930\u094d\u092e\u0941\u0915\u094d\u0937\u0940\u092f \u092e\u093e\u092e\u0943\u0924\u093e\u0924\u094d\u0965",
        meaningHi = "\u0939\u092e \u0924\u094d\u0930\u093f\u0928\u0947\u0924\u094d\u0930\u0927\u093e\u0930\u0940 \u0938\u0941\u0917\u0902\u0927\u093f\u0924 \u0935 \u092a\u0941\u0937\u094d\u091f\u093f \u0915\u0930\u0928\u0947 \u0935\u093e\u0932\u0947 \u092d\u0917\u0935\u093e\u0928 \u0936\u093f\u0935 \u0915\u0940 \u0935\u0902\u0926\u0928\u093e \u0915\u0930\u0924\u0947 \u0939\u0948\u0902\u0964 \u091c\u093f\u0938 \u092a\u094d\u0930\u0915\u093e\u0930 \u092a\u0915\u093e \u0939\u0941\u0906 \u0916\u0930\u092c\u0942\u091c\u093e \u092c\u0947\u0932 \u0938\u0947 \u092e\u0941\u0915\u094d\u0924 \u0939\u094b\u0924\u093e \u0939\u0948, \u0935\u0948\u0938\u0947 \u0939\u0940 \u0939\u092e \u092e\u0943\u0924\u094d\u092f\u0941 \u0935 \u092d\u092f \u0938\u0947 \u092e\u0941\u0915\u094d\u0924 \u0939\u094b\u0915\u0930 \u0905\u092e\u0930\u0924\u093e \u092a\u094d\u0930\u093e\u092a\u094d\u0924 \u0915\u0930\u0947\u0902\u0964",
        benefitHi = "\u0906\u0930\u094b\u0917\u094d\u092f, \u0905\u0915\u093e\u0932 \u092e\u0943\u0924\u094d\u092f\u0941 \u0938\u0947 \u0938\u0941\u0930\u0915\u094d\u0937\u093e, \u092d\u092f \u092e\u0941\u0915\u094d\u0924\u093f \u090f\u0935\u0902 \u092e\u093e\u0928\u0938\u093f\u0915 \u0936\u093e\u0902\u0924\u093f\u0964",
        symbol = "\ud83d\udd31",
        defaultSaved = true
    ),
    VedicMantraItem(
        id = "mantra_gayatri",
        titleHi = "\u0917\u093e\u092f\u0924\u094d\u0930\u0940 \u092e\u0939\u093e\u092e\u0902\u0924\u094d\u0930",
        deityHi = "\u0938\u0935\u093f\u0924\u093e \u0926\u0947\u0935 (\u0938\u0942\u0930\u094d\u092f)",
        shloka = "\u0950 \u092d\u0942\u0930\u094d\u092d\u0941\u0935\u0903 \u0938\u094d\u0935\u0903 \u0924\u0924\u094d\u0938\u0935\u093f\u0924\u0941\u0930\u094d\u0935\u0930\u0947\u0923\u094d\u092f\u0902\n\u092d\u0930\u094d\u0917\u094b \u0926\u0947\u0935\u0938\u094d\u092f \u0927\u0940\u092e\u0939\u093f \u0927\u093f\u092f\u094b \u092f\u094b \u0928\u0903 \u092a\u094d\u0930\u091a\u094b\u0926\u092f\u093e\u0924\u094d\u0965",
        meaningHi = "\u0909\u0938 \u092a\u094d\u0930\u093e\u0923\u0938\u094d\u0935\u0930\u0942\u092a, \u0926\u0941\u0903\u0916\u0928\u093f\u0935\u093e\u0930\u0915, \u0938\u0941\u0916\u0926\u093e\u0924\u093e, \u0936\u094d\u0930\u0947\u0937\u094d\u0920, \u0924\u0947\u091c\u0938\u094d\u0935\u0940 \u092a\u0930\u092e\u092a\u093f\u0924\u093e \u092a\u0930\u092e\u093e\u0924\u094d\u092e\u093e \u0915\u0947 \u0924\u0947\u091c \u0915\u094b \u0939\u092e \u0905\u092a\u0928\u0940 \u092c\u0941\u0926\u094d\u0927\u093f \u092e\u0947\u0902 \u0927\u093e\u0930\u0923 \u0915\u0930\u0947\u0902, \u091c\u094b \u0939\u092e\u093e\u0930\u0940 \u092c\u0941\u0926\u094d\u0927\u093f \u0915\u094b \u0938\u0924\u094d\u0915\u0930\u094d\u092e\u094b\u0902 \u0915\u0940 \u0913\u0930 \u092a\u094d\u0930\u0947\u0930\u093f\u0924 \u0915\u0930\u0947\u0964",
        benefitHi = "\u092c\u0941\u0926\u094d\u0927\u093f, \u0924\u0947\u091c, \u0938\u0915\u093e\u0930\u093e\u0924\u094d\u092e\u0915 \u090a\u0930\u094d\u091c\u093e, \u090f\u0915\u093e\u0917\u094d\u0930\u0924\u093e \u090f\u0935\u0902 \u092a\u093e\u092a \u0928\u093f\u0935\u093e\u0930\u0923\u0964",
        symbol = "\u2600\ufe0f",
        defaultSaved = true
    ),
    VedicMantraItem(
        id = "mantra_ganesh",
        titleHi = "\u0936\u094d\u0930\u0940 \u0917\u0923\u0947\u0936 \u0938\u0902\u0915\u091f \u0928\u093e\u0936\u0928 \u092e\u0902\u0924\u094d\u0930",
        deityHi = "\u092a\u094d\u0930\u0925\u092e \u092a\u0942\u091c\u094d\u092f \u0936\u094d\u0930\u0940 \u0917\u0923\u0947\u0936",
        shloka = "\u0950 \u0917\u0902 \u0917\u0923\u092a\u0924\u092f\u0947 \u0928\u092e\u0903\u0964\n\u0935\u0915\u094d\u0930\u0924\u0941\u0923\u094d\u0921 \u092e\u0939\u093e\u0915\u093e\u092f \u0938\u0942\u0930\u094d\u092f\u0915\u094b\u091f\u093f \u0938\u092e\u092a\u094d\u0930\u092d\u0964\n\u0928\u093f\u0930\u094d\u0935\u093f\u0918\u094d\u0928\u0902 \u0915\u0941\u0930\u0941 \u092e\u0947 \u0926\u0947\u0935 \u0938\u0930\u094d\u0935\u0915\u093e\u0930\u094d\u092f\u0947\u0937\u0941 \u0938\u0930\u094d\u0935\u0926\u093e\u0965",
        meaningHi = "\u0939\u0947 \u0935\u093f\u0936\u093e\u0932 \u0936\u0930\u0940\u0930 \u0935\u093e\u0932\u0947, \u0915\u0930\u094b\u0921\u093c \u0938\u0942\u0930\u094d\u092f\u094b\u0902 \u0915\u0947 \u0938\u092e\u093e\u0928 \u0924\u0947\u091c\u0938\u094d\u0935\u0940 \u092e\u0939\u093e\u092a\u094d\u0930\u0924\u093e\u092a\u0940 \u092d\u0917\u0935\u093e\u0928 \u0917\u0923\u0947\u0936! \u0906\u092a \u092e\u0947\u0930\u0947 \u0938\u092d\u0940 \u0915\u093e\u0930\u094d\u092f\u094b\u0902 \u0915\u094b \u092c\u093f\u0928\u093e \u0915\u093f\u0938\u0940 \u0935\u093f\u0918\u094d\u0928 \u0915\u0947 \u0938\u0926\u0948\u0935 \u092a\u0942\u0930\u094d\u0923 \u0915\u0930\u0947\u0902\u0964",
        benefitHi = "\u0938\u092e\u0938\u094d\u0924 \u0935\u093f\u0918\u094d\u0928-\u092c\u093e\u0927\u093e\u0913\u0902 \u0915\u093e \u0936\u092e\u0928, \u0935\u094d\u092f\u093e\u092a\u093e\u0930 \u0935 \u0905\u0927\u094d\u092f\u092f\u0928 \u092e\u0947\u0902 \u0938\u092b\u0932\u0924\u093e\u0964",
        symbol = "\ud83e\ude94",
        defaultSaved = false
    ),
    VedicMantraItem(
        id = "mantra_lakshmi",
        titleHi = "\u092e\u0939\u093e\u0932\u0915\u094d\u0937\u094d\u092e\u0940 \u0938\u092e\u0943\u0926\u094d\u0927\u093f \u092e\u0902\u0924\u094d\u0930",
        deityHi = "\u092e\u093e\u0924\u093e \u092e\u0939\u093e\u0932\u0915\u094d\u0937\u094d\u092e\u0940",
        shloka = "\u0950 \u0936\u094d\u0930\u0940\u0902 \u0939\u094d\u0930\u0940\u0902 \u0915\u094d\u0932\u0940\u0902 \u0936\u094d\u0930\u0940\u0902 \u0938\u093f\u0926\u094d\u0927 \u0932\u0915\u094d\u0937\u094d\u092e\u094d\u092f\u0948 \u0928\u092e\u0903\u0965\n\u0950 \u0939\u093f\u0930\u0923\u094d\u092f\u0935\u0930\u094d\u0923\u093e\u0902 \u0939\u0930\u093f\u0923\u0940\u0902 \u0938\u0941\u0935\u0930\u094d\u0923\u0930\u091c\u0924\u0938\u094d\u0930\u091c\u093e\u092e\u094d\u0964",
        meaningHi = "\u0939\u0947 \u0910\u0936\u094d\u0935\u0930\u094d\u092f, \u0938\u094c\u092d\u093e\u0917\u094d\u092f \u090f\u0935\u0902 \u0938\u092e\u0943\u0926\u094d\u0927\u093f \u0915\u0940 \u0905\u0927\u093f\u0937\u094d\u0920\u093e\u0924\u094d\u0930\u0940 \u092e\u093e\u0901 \u092e\u0939\u093e\u0932\u0915\u094d\u0937\u094d\u092e\u0940! \u0939\u092e\u093e\u0930\u0947 \u091c\u0940\u0935\u0928 \u092e\u0947\u0902 \u0927\u0928, \u0938\u093e\u0924\u094d\u0935\u093f\u0915 \u0938\u0902\u092a\u0926\u093e \u0935 \u0936\u093e\u0902\u0924\u093f \u0915\u093e \u0938\u0902\u091a\u093e\u0930 \u0915\u0930\u0947\u0902\u0964",
        benefitHi = "\u0926\u0930\u093f\u0926\u094d\u0930\u0924\u093e \u0915\u093e \u0928\u093e\u0936, \u0927\u0928-\u0927\u093e\u0928\u094d\u092f \u0935\u0943\u0926\u094d\u0927\u093f, \u0917\u0943\u0939 \u0936\u093e\u0902\u0924\u093f \u0935 \u0910\u0936\u094d\u0935\u0930\u094d\u092f \u092a\u094d\u0930\u093e\u092a\u094d\u0924\u093f\u0964",
        symbol = "\ud83e\udeb7",
        defaultSaved = false
    )
)

/**
 * Sanitizes any raw string to prevent awkward values like '\u0938\u093e\u0927\u0915 (Facebook)', 'null', or blank strings.
 * Gracefully returns '\u092a\u094d\u0930\u093f\u092f \u0938\u093e\u0927\u0915' when user's name is missing.
 */
private fun sanitizeDevoteeName(raw: String?): String {
    val trimmed = raw?.trim() ?: ""
    if (trimmed.isBlank() ||
        trimmed.equals("null", ignoreCase = true) ||
        trimmed.contains("Facebook", ignoreCase = true) ||
        trimmed == "\u0938\u093e\u0927\u0915" ||
        trimmed.startsWith("user_", ignoreCase = true)
    ) {
        return "\u0926\u0940\u092a\u0915 \u091c\u0940"
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
    userNameInitial: String = "\u0938\u093e\u0927\u0915",
    onLogoutClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSession = remember { UserSession(context) }
    val effectiveUserId = userId.ifBlank { userSession.getUserId().ifBlank { "user_live" } }

    val initialSanitizedName = sanitizeDevoteeName(
        if (userNameInitial.isNotBlank() && userNameInitial != "\u0938\u093e\u0927\u0915") userNameInitial else userSession.getUserName()
    )
    val initialSanitizedPhone = sanitizeDevoteePhone(userSession.getPhoneNumber())

    // User State
    var userName by remember { mutableStateOf(initialSanitizedName) }
    var phone by remember { mutableStateOf(initialSanitizedPhone) }
    var email by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var birthTime by remember { mutableStateOf("") }
    var birthPlace by remember { mutableStateOf("") }
    var gotra by remember { mutableStateOf("") }
    var avatarId by remember { mutableStateOf("om") }
    var walletBalance by remember { mutableDoubleStateOf(userSession.getWalletBalance()) }
    var savedMantraIds by remember { mutableStateOf(setOf("mantra_mahamrityunjaya", "mantra_gayatri")) }

    // Consultations state
    val initialQuestions = remember {
        listOf<Map<String, Any>>(
            mapOf(
                "questionText" to "\u0938\u092a\u0928\u0947 \u092e\u0947\u0902 \u0936\u093f\u0935\u0932\u093f\u0902\u0917 \u092a\u0930 \u091c\u0932 \u091a\u0922\u093c\u093e\u0928\u093e",
                "sadhakName" to "\u0906\u091a\u093e\u0930\u094d\u092f \u0926\u0947\u0935 \u0936\u0930\u094d\u092e\u093e",
                "status" to "Answered",
                "providerAnswer" to "\u092f\u0939 \u0905\u0924\u094d\u092f\u0902\u0924 \u0936\u0941\u092d \u0938\u094d\u0935\u092a\u094d\u0928 \u0939\u0948\u0964 \u0906\u092a\u0915\u0947 \u0930\u0941\u0915\u0947 \u0915\u093e\u0930\u094d\u092f \u092a\u0942\u0930\u094d\u0923 \u0939\u094b\u0902\u0917\u0947\u0964",
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
                                Text(text = "\ud83d\udd49\ufe0f", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "\u0935\u0948\u0926\u093f\u0915 \u0938\u093e\u0927\u0915 \u092a\u094d\u0930\u094b\u092b\u093c\u093e\u0907\u0932",
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
                                Toast.makeText(context, "\u0938\u093e\u0927\u0915 UID \u0915\u0949\u092a\u0940 \u0939\u0941\u0906!", Toast.LENGTH_SHORT).show()
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
                                        contentDescription = "\u0905\u0935\u0924\u093e\u0930 \u092c\u0926\u0932\u0947\u0902",
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
                                            text = "\u092b\u093c\u094b\u0928 \u0928\u0902\u092c\u0930 \u091c\u094b\u0921\u093c\u0947\u0902 (+)",
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
                                            if (dob.isNotBlank()) append("\u091c\u0928\u094d\u092e: $dob")
                                            if (dob.isNotBlank() && gotra.isNotBlank()) append(" \u2022 ")
                                            if (gotra.isNotBlank()) append("\u0917\u094b\u0924\u094d\u0930: $gotra")
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
                                    text = "\u0935\u0949\u0932\u0947\u091f \u0926\u0915\u094d\u0937\u093f\u0923\u093e \u0936\u0947\u0937",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9A3412),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "\u20b9${walletBalance.toInt()}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SaffronDeep
                                )
                            }
                        }

                        // Consultations summary count
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "\u092a\u0930\u093e\u092e\u0930\u094d\u0936 \u092a\u094d\u0930\u0936\u094d\u0928",
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
                        // "\u0935\u093f\u0935\u0930\u0923 \u091c\u094b\u0921\u093c\u0947\u0902 / \u0938\u0902\u092a\u093e\u0926\u093f\u0924 \u0915\u0930\u0947\u0902" Button
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
                                text = if (isProfileIncomplete) "\u0935\u093f\u0935\u0930\u0923 \u091c\u094b\u0921\u093c\u0947\u0902 (Add Details)" else "\u0935\u093f\u0935\u0930\u0923 \u092c\u0926\u0932\u0947\u0902 (Edit)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // "\u0905\u0935\u0924\u093e\u0930 \u092c\u0926\u0932\u0947\u0902" Button
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
                                text = "\u0905\u0935\u0924\u093e\u0930 \u091a\u0941\u0928\u0947\u0902",
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
                            Text(text = "\u270d\ufe0f", fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "\u092a\u094d\u0930\u094b\u092b\u093c\u093e\u0907\u0932 \u0935\u093f\u0935\u0930\u0923 \u092a\u0942\u0930\u094d\u0923 \u0915\u0930\u0947\u0902",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "\u0938\u091f\u0940\u0915 \u0915\u0941\u0902\u0921\u0932\u0940 \u0935\u093f\u091a\u093e\u0930, \u0935\u0948\u0926\u093f\u0915 \u092a\u0930\u093e\u092e\u0930\u094d\u0936 \u0915\u0949\u0932 \u0935 \u0905\u0928\u0941\u0937\u094d\u0920\u093e\u0928 \u0939\u0947\u0924\u0941 \u0905\u092a\u0928\u093e \u0928\u093e\u092e \u0935 \u092b\u093c\u094b\u0928 \u0928\u0902\u092c\u0930 \u0926\u0930\u094d\u091c \u0915\u0930\u0947\u0902\u0964",
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
                                text = "\u091c\u094b\u0921\u093c\u0947\u0902 (+)",
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
        // 2. Consultation History Card (\u092a\u0930\u093e\u092e\u0930\u094d\u0936 \u0907\u0924\u093f\u0939\u093e\u0938)
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
                                Text(text = "\ud83d\udcdc", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "\u092a\u0930\u093e\u092e\u0930\u094d\u0936 \u0907\u0924\u093f\u0939\u093e\u0938 (Consultation History)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "\u092a\u0942\u091b\u0947 \u0917\u090f \u092a\u094d\u0930\u0936\u094d\u0928, \u0938\u092e\u093e\u0927\u093e\u0928 \u0935 \u092e\u093e\u0930\u094d\u0917\u0926\u0930\u094d\u0936\u0928",
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
                                    text = "${userQuestions.size} \u092a\u094d\u0930\u0936\u094d\u0928",
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
                            Text(text = "\ud83e\ude94", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\u0905\u092d\u0940 \u0915\u094b\u0908 \u092a\u0930\u093e\u092e\u0930\u094d\u0936 \u0907\u0924\u093f\u0939\u093e\u0938 \u0909\u092a\u0932\u092c\u094d\u0927 \u0928\u0939\u0940\u0902 \u0939\u0948",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\u0915\u0941\u0902\u0921\u0932\u0940, \u0917\u0943\u0939 \u0926\u094b\u0937, \u0935\u093f\u0935\u093e\u0939 \u092f\u093e \u0915\u0930\u093f\u092f\u0930 \u0938\u0902\u092c\u0902\u0927\u0940 \u092a\u094d\u0930\u0936\u094d\u0928\u094b\u0902 \u0915\u0947 \u0932\u093f\u090f \u0939\u092e\u093e\u0930\u0947 \u0935\u0948\u0926\u093f\u0915 \u0938\u093e\u0927\u0915\u094b\u0902 \u0938\u0947 \u0938\u0902\u092a\u0930\u094d\u0915 \u0915\u0930\u0947\u0902\u0964",
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
                                                    text = questionText.ifBlank { "\u0935\u0948\u0926\u093f\u0915 \u092a\u0930\u093e\u092e\u0930\u094d\u0936 \u092a\u094d\u0930\u0936\u094d\u0928" },
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
                                                    text = if (isAnswered) "\u0938\u092e\u093e\u0927\u093e\u0928 \u092a\u094d\u0930\u093e\u092a\u094d\u0924 \u2713" else "\u092a\u094d\u0930\u0924\u0940\u0915\u094d\u0937\u093e\u0930\u0924 \u23f3",
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
                                                text = "\u0938\u093e\u0927\u0915 \u0915\u093e \u0938\u092e\u093e\u0927\u093e\u0928:",
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
        // 3. Saved Mantras & Daily Japa Card (\u0938\u0902\u0917\u094d\u0930\u0939\u0940\u0924 \u092e\u0902\u0924\u094d\u0930)
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
                                Text(text = "\ud83d\udcff", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "\u0938\u0902\u0917\u094d\u0930\u0939\u0940\u0924 \u092e\u0902\u0924\u094d\u0930 \u090f\u0935\u0902 \u0928\u093f\u0924\u094d\u092f \u092a\u093e\u0920 (Saved Mantras)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "\u0928\u093f\u0924\u094d\u092f \u091c\u093e\u092a \u0935 \u0938\u093e\u0927\u0928\u093e \u0939\u0947\u0924\u0941 \u092a\u093e\u0935\u0928 \u0935\u0948\u0926\u093f\u0915 \u092e\u0902\u0924\u094d\u0930",
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
                                                    if (newSet.contains(mantra.id)) "${mantra.titleHi} \u0938\u0902\u0917\u094d\u0930\u0939 \u092e\u0947\u0902 \u091c\u094b\u0921\u093c\u093e \u0917\u092f\u093e" else "${mantra.titleHi} \u0939\u091f\u093e\u092f\u093e \u0917\u092f\u093e",
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
                                                text = "\u0938\u0930\u0932 \u0905\u0930\u094d\u0925: ${mantra.meaningHi}",
                                                fontSize = 11.5.sp,
                                                color = Color(0xFF475569),
                                                lineHeight = 16.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "\u0938\u093e\u0927\u0928\u093e \u0932\u093e\u092d: ${mantra.benefitHi}",
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
        // 4. Settings & Support Card (\u0938\u0947\u091f\u093f\u0902\u0917\u094d\u0938 \u090f\u0935\u0902 \u0938\u0939\u093e\u092f\u0924\u093e)
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
                            Text(text = "\u2699\ufe0f", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "\u0938\u0947\u091f\u093f\u0902\u0917\u094d\u0938 \u090f\u0935\u0902 \u0938\u0939\u093e\u092f\u0924\u093e (Settings & Support)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "\u0910\u092a \u092a\u094d\u0930\u093e\u0925\u092e\u093f\u0915\u0924\u093e\u090f\u0902, \u0917\u094b\u092a\u0928\u0940\u092f\u0924\u093e \u090f\u0935\u0902 \u0938\u0902\u092a\u0930\u094d\u0915",
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
                                    text = "\u0926\u0948\u0928\u093f\u0915 \u092a\u0902\u091a\u093e\u0902\u0917 \u0935 \u0936\u0941\u092d \u092e\u0941\u0939\u0942\u0930\u094d\u0924 \u0938\u0942\u091a\u0928\u093e\u090f\u0902",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                                Text(
                                    text = "\u092a\u094d\u0930\u093e\u0924\u0903\u0915\u093e\u0932 \u0936\u0941\u092d \u091a\u094c\u0918\u0921\u093c\u093f\u092f\u093e \u0935 \u0930\u093e\u0939\u0941\u0915\u093e\u0932 \u0905\u0932\u0930\u094d\u091f",
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
                                    if (it) "\u0926\u0948\u0928\u093f\u0915 \u0938\u0942\u091a\u0928\u093e\u090f\u0902 \u0938\u0915\u094d\u0930\u093f\u092f \u0915\u0940 \u0917\u0908\u0902" else "\u0926\u0948\u0928\u093f\u0915 \u0938\u0942\u091a\u0928\u093e\u090f\u0902 \u092c\u0902\u0926 \u0915\u0940 \u0917\u0908\u0902",
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
                                    text = "\u0938\u093e\u0927\u0915 \u0938\u0939\u093e\u092f\u0924\u093e \u090f\u0935\u0902 \u0938\u092e\u093e\u0927\u093e\u0928 \u0915\u0947\u0902\u0926\u094d\u0930",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                                Text(
                                    text = "\u0915\u0949\u0932 / \u091a\u0948\u091f \u0938\u0939\u093e\u092f\u0924\u093e \u090f\u0935\u0902 \u092a\u094d\u0930\u0936\u094d\u0928 \u0928\u093f\u0935\u093e\u0930\u0923",
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
                                    text = "\u0928\u093f\u092f\u092e, \u0936\u0930\u094d\u0924\u0947\u0902 \u090f\u0935\u0902 \u0917\u094b\u092a\u0928\u0940\u092f\u0924\u093e \u0928\u0940\u0924\u093f",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark
                                )
                                Text(
                                    text = "\u0938\u093e\u0927\u0915 \u0917\u094b\u092a\u0928\u0940\u092f\u0924\u093e \u090f\u0935\u0902 \u0938\u093e\u0924\u094d\u0935\u093f\u0915 \u0938\u0947\u0935\u093e \u0928\u093f\u092f\u092e",
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
                        text = "\u0932\u0949\u0917\u0906\u0909\u091f \u0915\u0930\u0947\u0902 (Logout)",
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
        var editName by remember { mutableStateOf(if (userName == "\u092a\u094d\u0930\u093f\u092f \u0938\u093e\u0927\u0915") "" else userName) }
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
                    Text(text = "\ud83d\udd49\ufe0f", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "\u0938\u093e\u0927\u0915 \u0935\u094d\u092f\u0915\u094d\u0924\u093f\u0917\u0924 \u0935\u093f\u0935\u0930\u0923",
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
                        text = "\u0938\u091f\u0940\u0915 \u0915\u0941\u0902\u0921\u0932\u0940 \u0935\u093f\u091a\u093e\u0930, \u0935\u0948\u0926\u093f\u0915 \u092a\u0930\u093e\u092e\u0930\u094d\u0936 \u090f\u0935\u0902 \u092a\u0942\u091c\u093e \u0938\u0902\u0915\u0932\u094d\u092a \u0939\u0947\u0924\u0941 \u0905\u092a\u0928\u093e \u092a\u094d\u0930\u093e\u092e\u093e\u0923\u093f\u0915 \u0935\u093f\u0935\u0930\u0923 \u0926\u0930\u094d\u091c \u0915\u0930\u0947\u0902:",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )

                    // 1. Full Name
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("\u092a\u0942\u0930\u093e \u0928\u093e\u092e (Full Name) *") },
                        placeholder = { Text("\u0909\u0926\u093e. \u0905\u0928\u0941\u0930\u093e\u0917 \u0936\u0930\u094d\u092e\u093e") },
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
                        label = { Text("\u092b\u093c\u094b\u0928 \u0928\u0902\u092c\u0930 (Phone Number) *") },
                        placeholder = { Text("\u0909\u0926\u093e. 9876543210") },
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
                        label = { Text("\u0908\u092e\u0947\u0932 (Email - \u0935\u0948\u0915\u0932\u094d\u092a\u093f\u0915)") },
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
                        label = { Text("\u091c\u0928\u094d\u092e\u0924\u093f\u0925\u093f (Date of Birth)") },
                        placeholder = { Text("DD/MM/YYYY (\u0909\u0926\u093e. 15/08/1995)") },
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
                        label = { Text("\u091c\u0928\u094d\u092e \u0938\u094d\u0925\u093e\u0928 (\u0928\u0917\u0930 / \u0930\u093e\u091c\u094d\u092f)") },
                        placeholder = { Text("\u0909\u0926\u093e. \u0935\u093e\u0930\u093e\u0923\u0938\u0940, \u0909\u0924\u094d\u0924\u0930 \u092a\u094d\u0930\u0926\u0947\u0936") },
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
                        label = { Text("\u0917\u094b\u0924\u094d\u0930 (Gotra - \u092a\u0942\u091c\u093e \u0938\u0902\u0915\u0932\u094d\u092a \u0939\u0947\u0924\u0941)") },
                        placeholder = { Text("\u0909\u0926\u093e. \u0915\u0936\u094d\u092f\u092a / \u092d\u093e\u0930\u0926\u094d\u0935\u093e\u091c / \u0935\u0924\u094d\u0938") },
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
                            "\u0926\u0940\u092a\u0915 \u091c\u0940"
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
                        Toast.makeText(context, "\u0935\u093f\u0935\u0930\u0923 \u0938\u092b\u0932\u0924\u093e\u092a\u0942\u0930\u094d\u0935\u0915 \u0938\u0941\u0930\u0915\u094d\u0937\u093f\u0924 \u0939\u0941\u0906!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    enabled = !isSaving
                ) {
                    Text(
                        text = if (isSaving) "\u0938\u0941\u0930\u0915\u094d\u0937\u093f\u0924 \u0939\u094b \u0930\u0939\u093e \u0939\u0948..." else "\u0935\u093f\u0935\u0930\u0923 \u0938\u0939\u0947\u091c\u0947\u0902 (Save)",
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
                    Text("\u0930\u0926\u094d\u0926 \u0915\u0930\u0947\u0902", color = TextMuted)
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
                        Text(text = "\ud83d\udd49\ufe0f", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "\u092a\u0935\u093f\u0924\u094d\u0930 \u0906\u0927\u094d\u092f\u093e\u0924\u094d\u092e\u093f\u0915 \u0905\u0935\u0924\u093e\u0930",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextDark
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\u092a\u094d\u0930\u094b\u092b\u093c\u093e\u0907\u0932 \u092e\u0947\u0902 \u0905\u092a\u0928\u0940 \u0906\u0927\u094d\u092f\u093e\u0924\u094d\u092e\u093f\u0915 \u0938\u093e\u0927\u0928\u093e \u0915\u0947 \u0905\u0928\u0941\u0915\u0942\u0932 \u092a\u094d\u0930\u0924\u0940\u0915 \u092f\u093e \u0938\u093e\u0927\u0915 \u0905\u0935\u0924\u093e\u0930 \u091a\u0941\u0928\u0947\u0902\u0964",
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
                        Toast.makeText(context, "$chosenName \u0905\u0935\u0924\u093e\u0930 \u0938\u092b\u0932\u0924\u093e\u092a\u0942\u0930\u094d\u0935\u0915 \u0938\u0947\u091f \u0939\u0941\u0906!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("\u0905\u0935\u0924\u093e\u0930 \u0938\u0947\u091f \u0915\u0930\u0947\u0902", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAvatarPickerDialog = false }) {
                    Text("\u0930\u0926\u094d\u0926 \u0915\u0930\u0947\u0902", color = TextMuted)
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
                    Text(text = "\ud83d\udd49\ufe0f", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "\u0938\u093e\u0927\u0915 \u0938\u0939\u093e\u092f\u0924\u093e \u0915\u0947\u0902\u0926\u094d\u0930",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextDark
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "\u0926\u0947\u0935\u092d\u093e\u0937\u093e \u0935\u0948\u0926\u093f\u0915 \u0938\u0939\u093e\u092f\u0924\u093e \u0915\u0947\u0902\u0926\u094d\u0930 \u092e\u0947\u0902 \u0906\u092a\u0915\u093e \u0938\u094d\u0935\u093e\u0917\u0924 \u0939\u0948\u0964 \u0915\u093f\u0938\u0940 \u092d\u0940 \u0924\u0915\u0928\u0940\u0915\u0940 \u0938\u0939\u093e\u092f\u0924\u093e, \u0935\u0949\u0932\u0947\u091f \u0930\u093f\u091a\u093e\u0930\u094d\u091c \u092f\u093e \u092a\u0930\u093e\u092e\u0930\u094d\u0936 \u092a\u094d\u0930\u0936\u094d\u0928 \u0939\u0947\u0924\u0941 \u0938\u0902\u092a\u0930\u094d\u0915 \u0915\u0930\u0947\u0902:",
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
                                text = "\u0908\u092e\u0947\u0932 \u0938\u0939\u093e\u092f\u0924\u093e:",
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
                                text = "\u0909\u092a\u0932\u092c\u094d\u0927\u0924\u093e \u0938\u092e\u092f:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDeep
                            )
                            Text(
                                text = "\u0938\u094b\u092e\u0935\u093e\u0930 \u0938\u0947 \u0930\u0935\u093f\u0935\u093e\u0930: \u092a\u094d\u0930\u093e\u0924\u0903 8:00 \u0938\u0947 \u0930\u093e\u0924\u094d\u0930\u093f 9:00",
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
                    Text("\u0920\u0940\u0915 \u0939\u0948", color = Color.White)
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
                    Text(text = "\ud83d\udcdc", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "\u0928\u093f\u092f\u092e \u090f\u0935\u0902 \u0917\u094b\u092a\u0928\u0940\u092f\u0924\u093e \u0928\u0940\u0924\u093f",
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
                        text = "1. \u0938\u093e\u0924\u094d\u0935\u093f\u0915 \u090f\u0935\u0902 \u0935\u0948\u0926\u093f\u0915 \u092a\u0930\u0902\u092a\u0930\u093e: \u0926\u0947\u0935\u092d\u093e\u0937\u093e \u0910\u092a \u092a\u0930 \u0938\u092d\u0940 \u092a\u0930\u093e\u092e\u0930\u094d\u0936 \u092d\u093e\u0930\u0924\u0940\u092f \u0935\u0948\u0926\u093f\u0915 \u091c\u094d\u092f\u094b\u0924\u093f\u0937, \u0915\u0930\u094d\u092e\u0915\u093e\u0902\u0921 \u090f\u0935\u0902 \u0938\u0928\u093e\u0924\u0928 \u092a\u0930\u0902\u092a\u0930\u093e \u092a\u0930 \u0906\u0927\u093e\u0930\u093f\u0924 \u0939\u0948\u0902\u0964",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "2. \u0921\u0947\u091f\u093e \u0917\u094b\u092a\u0928\u0940\u092f\u0924\u093e: \u0906\u092a\u0915\u0947 \u091c\u0928\u094d\u092e \u0935\u093f\u0935\u0930\u0923 (\u091c\u0928\u094d\u092e \u0938\u092e\u092f, \u0938\u094d\u0925\u093e\u0928, \u091c\u0928\u094d\u092e\u0924\u093f\u0925\u093f) \u0915\u093e \u0909\u092a\u092f\u094b\u0917 \u0915\u0947\u0935\u0932 \u0915\u0941\u0902\u0921\u0932\u0940 \u090f\u0935\u0902 \u0917\u094d\u0930\u0939 \u0935\u093f\u091a\u093e\u0930 \u0939\u0947\u0924\u0941 \u092a\u0942\u0930\u094d\u0923\u0924\u0903 \u0938\u0941\u0930\u0915\u094d\u0937\u093f\u0924 \u0930\u0916\u093e \u091c\u093e\u0924\u093e \u0939\u0948\u0964",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "3. \u0935\u0949\u0932\u0947\u091f \u0926\u0915\u094d\u0937\u093f\u0923\u093e: \u0935\u0949\u0932\u0947\u091f \u092e\u0947\u0902 \u091c\u094b\u0921\u093c\u0940 \u0917\u0908 \u0930\u093e\u0936\u093f \u0915\u093e \u0909\u092a\u092f\u094b\u0917 \u0938\u093e\u0927\u0915\u094b\u0902 \u0938\u0947 \u092a\u0930\u093e\u092e\u0930\u094d\u0936 \u090f\u0935\u0902 \u0926\u0915\u094d\u0937\u093f\u0923\u093e \u0939\u0947\u0924\u0941 \u0915\u093f\u092f\u093e \u091c\u093e\u0924\u093e \u0939\u0948\u0964",
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
                    Text("\u0938\u094d\u0935\u0940\u0915\u093e\u0930 \u0939\u0948", color = Color.White)
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
                    text = "\u0932\u0949\u0917\u0906\u0909\u091f \u0915\u0940 \u092a\u0941\u0937\u094d\u091f\u093f",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = "\u0915\u094d\u092f\u093e \u0906\u092a \u0938\u091a\u092e\u0941\u091a \u0905\u092a\u0928\u0947 \u0938\u093e\u0927\u0915 \u0916\u093e\u0924\u0947 \u0938\u0947 \u092c\u093e\u0939\u0930 \u0928\u093f\u0915\u0932\u0928\u093e \u091a\u093e\u0939\u0924\u0947 \u0939\u0948\u0902?",
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
                    Text("\u0939\u093e\u0901, \u0932\u0949\u0917\u0906\u0909\u091f \u0915\u0930\u0947\u0902", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("\u0930\u0926\u094d\u0926 \u0915\u0930\u0947\u0902", color = TextMuted)
                }
            }
        )
    }
}
