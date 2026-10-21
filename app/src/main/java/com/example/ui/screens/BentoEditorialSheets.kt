package com.example.ui.screens

import com.example.utils.PriceLabels
import com.example.utils.Pricing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.models.ALL_AVATAR_OPTIONS
import com.example.ui.models.AVATAR_PACKAGES
import com.example.ui.models.AvatarOption
import com.example.ui.models.SadhakItem
import com.example.ui.models.getAvatarById
import com.example.utils.HapticFeedbackHelper
import com.example.utils.UserSession
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 1. Bento Editorial Recharge Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoEditorialRechargeSheet(
    currentBalance: Double,
    language: String = "en",
    onDismiss: () -> Unit,
    onConfirmRecharge: (Int) -> Unit
) {
    var selectedAmount by remember { mutableIntStateOf(100) }
    val amounts = listOf(100, 200, 500)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PaperBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                shape = RoundedCornerShape(2.dp),
                color = EditorialLineStrong,
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recharge Wallet",
                    fontFamily = AppFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color(0xFFE9DB97))
                ) {
                    Text(
                        text = "🔒 Secure Razorpay",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF125157),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Current balance: ",
                    fontSize = 12.sp,
                    color = InkSoft
                )
                Text(
                    text = "₹${currentBalance.toInt()}",
                    fontFamily = AppFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Price list (display labels only — amounts are decided by the server)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF1F5F9),
                border = BorderStroke(1.2.dp, Color(0xFFD0BF75)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "परामर्श सत्र ${PriceLabels.SESSION} · समय बढ़ाएँ ${PriceLabels.EXTEND}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF125157)
                    )
                    Text(
                        text = "स्वप्न अर्थ चैट ${PriceLabels.DREAM_CHAT} · स्वप्न कॉल ${PriceLabels.DREAM_CALL}",
                        fontSize = 11.sp,
                        color = Color(0xFF125157)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                amounts.forEach { amt ->
                    val isSel = selectedAmount == amt
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) Ink else PaperCard,
                        border = BorderStroke(1.5.dp, if (isSel) Ink else EditorialLineStrong),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedAmount = amt }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "₹$amt",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Ink,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Calculation
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (language == "hi") "टॉप-अप राशि:" else "Recharge Amount:", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(text = "₹$selectedAmount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFE2E8F0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (language == "hi") "कुल वॉलेट क्रेडिट:" else "Total Credited in Wallet:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Text(text = "₹$selectedAmount", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF16A34A))
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Confirm Button
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Terra,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onConfirmRecharge(selectedAmount * 2) }
            ) {
                Text(
                    text = if (language == "hi") "भुगतान करें ₹$selectedAmount और पाएं ₹${selectedAmount * 2}" else "Pay ₹$selectedAmount & Get ₹${selectedAmount * 2}",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 13.dp)
                )
            }
        }
    }
}

/**
 * 2. Bento Editorial Profile Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoEditorialProfileSheet(
    userName: String,
    userEmail: String,
    balanceAmount: Double,
    freeMins: Int,
    language: String = "en",
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onHelpClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val isEn = language == "en"
    val isHi = language == "hi"
    val isHing = language == "hinglish"
    
    val langLabel = when(language) {
        "hi" -> "भाषा · हिंदी"
        "hinglish" -> "Language · Hinglish"
        else -> "Language · English"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PaperBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                shape = RoundedCornerShape(2.dp),
                color = EditorialLineStrong,
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
            ) {}
        }
    ) {
        val context = LocalContext.current
        val haptic = LocalHapticFeedback.current
        val userSession = remember { UserSession(context) }
        var currentAvatarId by remember { mutableStateOf(userSession.getAvatarId()) }
        val avatar = remember(currentAvatarId) { getAvatarById(currentAvatarId) }
        var showAvatarModalDialog by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Profile Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier.clickable {
                        HapticFeedbackHelper.playClick(haptic)
                        showAvatarModalDialog = true
                    }
                ) {
                    Surface(
                        shape = CircleShape,
                        border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.secondary),
                        shadowElevation = 3.dp,
                        modifier = Modifier.size(58.dp)
                    ) {
                        Image(
                            painter = painterResource(id = avatar.drawableRes),
                            contentDescription = avatar.nameHi,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // Small edit icon badge at the corner
                    Surface(
                        shape = CircleShape,
                        color = SaffronPrimary,
                        border = BorderStroke(1.5.dp, Color.White),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Edit avatar",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userName.ifBlank { "Sadhak" },
                            fontFamily = AppFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SaffronSoft
                        ) {
                            Text(
                                text = avatar.badgeHi,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDeep,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = avatar.nameHi + " • " + (if (userEmail.isNotBlank()) userEmail else "साधना पथ"),
                        fontSize = 11.5.sp,
                        color = InkSoft,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick 1-Tap Sadhak Avatar Strip
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, Color(0xFFE9DB97)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✨ Sadhak अवतार चुनें",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF125157)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color(0xFFD0BF75)
                            ) {
                                Text(
                                    text = "12+ उपलब्ध",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "सभी देखें ›",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF125157),
                            modifier = Modifier.clickable {
                                HapticFeedbackHelper.playClick(haptic)
                                showAvatarModalDialog = true
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val sadhakAvatars = ALL_AVATAR_OPTIONS.filter { it.packageId == "sadhaks" }
                        items(sadhakAvatars) { opt ->
                            val isSelected = (opt.id == currentAvatarId)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color.White else Color(0xFFF1F5F9),
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) Color(0xFFD0BF75) else Color(0xFFE9DB97)
                                ),
                                shadowElevation = if (isSelected) 2.dp else 0.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        HapticFeedbackHelper.playClick(haptic)
                                        currentAvatarId = opt.id
                                        userSession.setAvatarId(opt.id)
                                        Toast.makeText(context, "अवतार चुना गया: ${opt.nameHi} ✨", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = opt.bgColor,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = opt.symbol, fontSize = 13.sp)
                                        }
                                    }
                                    Text(
                                        text = opt.nameHi,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF125157) else Color(0xFF125157)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatBox(label = if (isHi) "बैलेंस" else "BALANCE", value = "₹${balanceAmount.toInt()}", modifier = Modifier.weight(1f))
                ProfileStatBox(label = if (isHi) "फ्री मिनट" else "FREE MINS", value = "$freeMins", modifier = Modifier.weight(1f))
                ProfileStatBox(label = if (isHi) "खाता स्थिति" else "MEMBERSHIP", value = if (isHi) "सक्रिय" else "Active", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Settings Rows
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PaperCard,
                border = BorderStroke(1.dp, EditorialLine),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Filled.AutoAwesome,
                        title = if (isHi) "Sadhak अवतार बदलें (12+ विकल्प)" else "Change Sadhak Avatar",
                        onClick = {
                            HapticFeedbackHelper.playClick(haptic)
                            showAvatarModalDialog = true
                        }
                    )
                    HorizontalDivider(color = EditorialLine)
                    ProfileMenuRow(
                        icon = Icons.Outlined.Edit,
                        title = if (isHi) "प्रोफ़ाइल एवं जन्म विवरण बदलें" else "Edit profile & details",
                        onClick = onEditProfile
                    )
                    HorizontalDivider(color = EditorialLine)
                    
                    // Language Selection
                    var showLanguageOptions by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { showLanguageOptions = !showLanguageOptions }
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape, // Circular tonal background
                            color = Color(0xFFF3F4F6),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Language,
                                    contentDescription = null,
                                    tint = Color(0xFF1F2937),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = langLabel,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1F2937),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (showLanguageOptions) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Toggle Buttons
                    AnimatedVisibility(visible = showLanguageOptions) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .padding(bottom = 11.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val languages = listOf("en" to "English", "hi" to "Hindi", "hinglish" to "Hinglish")
                            languages.forEach { (code, label) ->
                                val isSelected = language == code
                                OutlinedButton(
                                    onClick = { onLanguageChange(code) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) SaffronSoftBg else Color.Transparent,
                                        contentColor = if (isSelected) Terra else Ink
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) Terra else EditorialLine),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = EditorialLine)
                    ProfileMenuRow(icon = Icons.AutoMirrored.Outlined.HelpOutline, title = if (isHi) "सहायता एवं समर्थन" else "Help & support", onClick = onHelpClick)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Log Out Button (Distinct, full-width pill-shaped outlined button with soft red/orange border)
            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.5.dp, Color(0xFFF87171)), // Soft red border
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFEF4444)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHi) "लॉग आउट" else "Log out",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // =========================================================================
        // Full Sadhak Avatar Picker Dialog (12+ Spiritual & Devotional Avatars)
        // =========================================================================
        if (showAvatarModalDialog) {
            DevbhashaAvatarPickerSheet(
                currentAvatarId = currentAvatarId,
                onDismiss = { showAvatarModalDialog = false },
                onAvatarSelected = { newAvatarId ->
                    currentAvatarId = newAvatarId
                    userSession.setAvatarId(newAvatarId)
                    showAvatarModalDialog = false
                    Toast.makeText(context, "अवतार सफलतापूर्वक सहेजा गया ✓", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun ProfileStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val isActive = value == "Active" || value == "सक्रिय"
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF3F4F6), // Soft grey background
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isActive) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFDCFCE7), // Soft green background
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = value,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A), // Green text
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = value,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                color = Color(0xFF4B5563), // High contrast
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape, // Circular tonal background
            color = Color(0xFFF3F4F6),
            modifier = Modifier.size(30.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) Color(0xFFEF4444) else Color(0xFF1F2937),
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) Color(0xFFEF4444) else Color(0xFF1F2937),
            modifier = Modifier.weight(1f)
        )

        if (!isDestructive) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


/**
 * 3. Book a Pro Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoEditorialBookingSheet(
    sadhaks: List<SadhakItem>,
    onDismiss: () -> Unit,
    onRequestBooking: (SadhakItem) -> Unit
) {
    var selectedSadhak by remember { mutableStateOf(sadhaks.firstOrNull()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PaperBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                shape = RoundedCornerShape(2.dp),
                color = EditorialLineStrong,
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Book a pro",
                fontFamily = AppFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Ink
            )

            Spacer(modifier = Modifier.height(12.dp))

            val displayList = sadhaks

            if (displayList.isEmpty()) {
                Text(
                    text = "Koi sadhak available nahi hai abhi",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = InkSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                )
            } else {
                if (selectedSadhak == null && displayList.isNotEmpty()) {
                    selectedSadhak = displayList.first()
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    displayList.take(3).forEach { sadhak ->
                        val isSel = selectedSadhak?.id == sadhak.id
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSel) Color(0xFFFDF1EA) else PaperCard,
                            border = BorderStroke(1.dp, if (isSel) Terra else EditorialLine),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedSadhak = sadhak }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF4D8F7C),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = sadhak.initialEn.take(2),
                                            fontFamily = AppFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sadhak.nameEn,
                                        fontFamily = AppFontFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Ink
                                    )
                                    Text(
                                        text = sadhak.titleEn,
                                        fontSize = 10.5.sp,
                                        color = InkSoft
                                    )
                                }

                                Text(
                                    text = PriceLabels.SESSION,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink
                                )

                                // Tick
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSel) Terra else Color.Transparent,
                                    border = BorderStroke(1.5.dp, if (isSel) Terra else EditorialLineStrong),
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isSel) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Terra,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .clickable {
                        selectedSadhak?.let { onRequestBooking(it) }
                    }
            ) {
                Text(
                    text = "Request booking",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 13.dp)
                )
            }
        }
    }
}

/**
 * Poochho Consultation Sheet (Tile 01):
 * User can explain any kind of problem (typed or spoken) and consult with a Sadhak via Chat or Voice Call.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoochhoProblemConsultSheet(
    sadhaks: List<SadhakItem>,
    onStartChat: (SadhakItem, String) -> Unit,
    onStartCall: (SadhakItem, String) -> Unit,
    onDismiss: () -> Unit,
    initialCategory: String? = null,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en"
) {
    var problemText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory ?: "करियर / व्यापार") }
    var selectedSadhak by remember { mutableStateOf<SadhakItem?>(sadhaks.firstOrNull()) }
    var isListening by remember { mutableStateOf(false) }

    val quickProblems = listOf(
        "करियर / व्यापार",
        "विवाह / संबंध",
        "स्वास्थ्य / शांति",
        "कुंडली / ग्रह दोष",
        "पारिवारिक क्लेश",
        "साधना / मंत्र"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = BorderMedium,
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Header: "Poochho · 01"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isHindi) "पूछा (Poocha)" else "Poocha",
                            fontFamily = AppFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = SaffronLight
                        ) {
                            Text(
                                text = "01 · पूछा",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Saffron,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = "अपनी किसी भी समस्या का समाधान पाएं — Sadhak से चैट अथवा वॉइस कॉल द्वारा",
                        fontSize = 12.sp,
                        color = Color(0xFF737373)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFAFAFA),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.size(36.dp).clickable { onDismiss() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF000000),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickProblems) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (isSelected) Color(0xFF000000) else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF000000) else BorderLight),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable {
                                selectedCategory = cat
                                if (problemText.isBlank()) {
                                    problemText = "$cat से संबंधित समस्या का परामर्श चाहिए..."
                                }
                            }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF737373),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Problem Input Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFAFAFA),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = problemText,
                        onValueChange = { problemText = it },
                        placeholder = {
                            Text(
                                text = "अपनी समस्या या प्रश्न यहाँ लिखें या माइक दबाकर बोलें...",
                                fontSize = 13.5.sp,
                                color = Color(0xFFA8A8A8)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (isListening) SaffronLight else Color.White,
                            border = BorderStroke(1.dp, if (isListening) Saffron else BorderLight),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable {
                                    isListening = !isListening
                                    if (isListening && problemText.isBlank()) {
                                        problemText = "मेरे व्यापार में बार-बार बाधा आ रही है, उचित उपाय व मार्गदर्शन बताएं..."
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Mic",
                                    tint = if (isListening) Saffron else Color(0xFF000000),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (isListening) "बोलें (Recording...)" else "माइक से बोलें",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isListening) Saffron else Color(0xFF000000)
                                )
                            }
                        }

                        if (problemText.isNotBlank()) {
                            Text(
                                text = "मिटाएं",
                                fontSize = 11.sp,
                                color = Color(0xFF737373),
                                modifier = Modifier.clickable { problemText = "" }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sadhak Selection Row
            Text(
                text = "परामर्श हेतु Sadhak चुनें:",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sadhaks) { sadhak ->
                    val isSelected = selectedSadhak?.id == sadhak.id
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFFFAFAFA) else Color.White,
                        border = BorderStroke(1.5.dp, if (isSelected) Color(0xFF000000) else BorderLight),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedSadhak = sadhak }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF0F0F0),
                                    border = if (sadhak.isOnline) BorderStroke(2.dp, Saffron) else null,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = sadhak.initialEn.take(2).ifEmpty { "SA" },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF000000)
                                        )
                                    }
                                }
                                if (sadhak.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(Saffron)
                                            .border(1.5.dp, Color.White, CircleShape)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = sadhak.nameEn,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF000000)
                                )
                                Text(
                                    text = "${PriceLabels.SESSION} · ⭐ ${sadhak.rating}",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF737373)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dual CTA Buttons: Chat & Voice Call
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary: Chat (Solid Saffron)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Saffron,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val active = selectedSadhak ?: sadhaks.firstOrNull()
                            if (active != null) {
                                onStartChat(active, problemText)
                                onDismiss()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = "Chat",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "चैट (₹${Pricing.TEXT_QUESTION_PRICE_RUPEES})",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Secondary: Call (White + Black border)
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFF000000)),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable {
                            val active = selectedSadhak ?: sadhaks.firstOrNull()
                            if (active != null) {
                                onStartCall(active, problemText)
                                onDismiss()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = "Call",
                            tint = Color(0xFF000000),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "कॉल (₹${Pricing.CONSULTATION_PRICE_RUPEES})",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Dream Quick Recorder Sheet (Tile 02):
 * User can save dream (written or voice recorder) and ask a sadhak for interpretation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamQuickRecorderSheet(
    userId: String,
    userName: String,
    onSaveDream: (String, String) -> Unit,
    onAskSadhak: (String) -> Unit,
    onOpenJournal: () -> Unit,
    onDismiss: () -> Unit,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en"
) {
    var mode by remember { mutableStateOf("write") } // "write" or "voice"
    var dreamText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("🌿 शुभ (Shubh)") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var isSaving by remember { mutableStateOf(false) }
    var toastNote by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Voice record timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording && recordingSeconds < 120) {
                delay(1000L)
                recordingSeconds += 1
            }
        }
    }

    val moodTags = listOf(
        "शुभ (Shubh)",
        "चेतावनी (Warning)",
        "मुक्ति (Peace)",
        "रहस्यमय (Mystic)",
        "नदी / प्रकृति"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = BorderMedium,
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Header: "Dreams · 02"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Dreams",
                            fontFamily = AppFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = SaffronLight
                        ) {
                            Text(
                                text = "02 · Interpret messages",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Saffron,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = "सपना दर्ज करें और Sadhak से उसका आध्यात्मिक अर्थ व फल जानें 🌙✨",
                        fontSize = 12.sp,
                        color = Color(0xFF737373)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFAFAFA),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.size(36.dp).clickable { onDismiss() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF000000),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Toggle Pills: Write vs Voice Record
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (mode == "write") Color(0xFF000000) else Color.White,
                    border = BorderStroke(1.dp, if (mode == "write") Color(0xFF000000) else BorderLight),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { mode = "write" }
                ) {
                    Text(
                        text = "✍️ लिखना (Write Dream)",
                        fontSize = 12.sp,
                        fontWeight = if (mode == "write") FontWeight.Bold else FontWeight.Medium,
                        color = if (mode == "write") Color.White else Color(0xFF737373),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (mode == "voice") Color(0xFF000000) else Color.White,
                    border = BorderStroke(1.dp, if (mode == "voice") Color(0xFF000000) else BorderLight),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { mode = "voice" }
                ) {
                    Text(
                        text = "🎙️ वॉइस रिकॉर्डर (Voice Record)",
                        fontSize = 12.sp,
                        fontWeight = if (mode == "voice") FontWeight.Bold else FontWeight.Medium,
                        color = if (mode == "voice") Color.White else Color(0xFF737373),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (mode == "write") {
                // Write Input Field
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFAFAFA),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = dreamText,
                            onValueChange = { dreamText = it },
                            placeholder = {
                                Text(
                                    text = "सपने में क्या देखा? (उदा. पावन नदी, मंदिर के शिखर, सफेद अश्व अथवा उड़ते हुए दृश्य)...",
                                    fontSize = 13.5.sp,
                                    color = Color(0xFFA8A8A8)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${dreamText.length} अक्षर",
                                fontSize = 10.5.sp,
                                color = Color(0xFFA8A8A8)
                            )
                            if (dreamText.isNotBlank()) {
                                Text(
                                    text = "हटाएं (Clear)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF737373),
                                    modifier = Modifier.clickable { dreamText = "" }
                                )
                            }
                        }
                    }
                }
            } else {
                // Voice Recorder Mode UI
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFAFAFA),
                    border = BorderStroke(1.dp, if (isRecording) Saffron else BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pulsing Mic Button
                        Surface(
                            shape = CircleShape,
                            color = if (isRecording) SaffronLight else Color.White,
                            border = BorderStroke(2.dp, if (isRecording) Saffron else BorderLight),
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .clickable {
                                    isRecording = !isRecording
                                    if (isRecording) {
                                        toastNote = "वॉइस रिकॉर्डिंग शुरू..."
                                    } else {
                                        if (dreamText.isBlank()) {
                                            dreamText = "सपने में पावन गंगा तट और आरती के दर्शन हुए, मन में अपार शांति की अनुभूति हुई..."
                                        }
                                        toastNote = "ऑडियो रिकॉर्ड हो गया ✨"
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = "Record",
                                    tint = if (isRecording) Saffron else Color(0xFF000000),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val formattedSec = String.format("%02d:%02d", recordingSeconds / 60, recordingSeconds % 60)
                        Text(
                            text = if (isRecording) "रिकॉर्डिंग चालू • $formattedSec" else (if (dreamText.isNotBlank()) "ऑडियो तैयार • टैप कर पुनः रिकॉर्ड करें" else "टैप करें और अपना सपना बोलें"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRecording) Saffron else Color(0xFF000000)
                        )

                        if (dreamText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, BorderLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "ट्रांसक्रिप्शन: $dreamText",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF737373),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mood Tags
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(moodTags) { tag ->
                    val isSelected = selectedTag == tag
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (isSelected) Color(0xFF000000) else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF000000) else BorderLight),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { selectedTag = tag }
                    ) {
                        Text(
                            text = tag,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF737373),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Two Action Buttons:
            // 1. Save Dream (Secondary - White with Black border)
            // 2. Ask Sadhak for Meaning (Primary - Saffron Gradient)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secondary: Save Dream
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFF000000)),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable {
                            val textToSave = dreamText.trim().ifBlank { "शुभ स्वप्न" }
                            onSaveDream(textToSave, selectedTag)
                            onDismiss()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "💾 सपना सहेजें",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                    }
                }

                // Primary: Ask Sadhak (Solid Saffron)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Saffron,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val textToAsk = dreamText.trim().ifBlank { "सपने का अर्थ व समाधान बताएं" }
                            onAskSadhak(textToAsk)
                            onDismiss()
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sadhak से अर्थ पूछें",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Open Full Journal Shortcut
            TextButton(
                onClick = {
                    onOpenJournal()
                    onDismiss()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "📖 पूरी स्वप्न डायरी देखें (Open Journal) →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF000000)
                )
            }
        }
    }
}
