package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.Constants
import com.example.utils.DisclaimerContent
import com.example.utils.UserManager
import com.example.utils.UserSession

/**
 * Unified DPDP (Data Protection & Privacy) Policy and Disclaimer Screen.
 * Combines Indian Digital Personal Data Protection Act 2023 compliance details,
 * user privacy rights, data storage details, and astrological/platform disclaimers
 * into a modern tabbed/scrollable UI with bilingual support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DpdpDisclaimerScreen(
    userId: String,
    userSession: UserSession,
    onDisclaimerAccepted: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isHindi by remember { mutableStateOf(userSession.isHindi()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: DPDP Privacy, 2: Disclaimer, 3: Grievance
    var isAcknowledgedChecked by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val saffronDark = SaffronDark
    val goldPrimary = Color(0xFFD4AF37)
    val goldLight = Color(0xFFFFF8E7)

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("dpdp_disclaimer_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Header Bar
            Surface(
                color = saffronDark,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onBackClick != null) {
                                IconButton(
                                    onClick = onBackClick,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("dpdp_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = goldPrimary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = goldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            Column {
                                Text(
                                    text = if (isHindi) "गोपनीयता, DPDP व अस्वीकरण" else "Privacy, DPDP & Disclaimer",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isHindi) "DPDP अधिनियम 2023 एवं मंच के नियम" else "Digital Data Protection & Platform Policy",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Language Switcher Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, goldPrimary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isHindi) goldPrimary else Color.Transparent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { isHindi = true }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "हिंदी",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isHindi) saffronDark else Color.White
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (!isHindi) goldPrimary else Color.Transparent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { isHindi = false }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "EN",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isHindi) saffronDark else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab Selector Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tabs = listOf(
                            if (isHindi) "सभी नियम" else "All Terms",
                            if (isHindi) "DPDP डेटा अधिकार" else "DPDP Rights",
                            if (isHindi) "सेवा अस्वीकरण" else "Disclaimer",
                            if (isHindi) "शिकायत अधिकारी" else "Grievance"
                        )
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) goldPrimary else Color.White.copy(alpha = 0.12f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) goldPrimary else Color.White.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { selectedTab = index }
                                    .testTag("dpdp_tab_$index")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) saffronDark else Color.White,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Scrollable Body Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Intro Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = goldLight),
                    border = BorderStroke(1.dp, goldPrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = goldPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PrivacyTip,
                                    contentDescription = null,
                                    tint = saffronDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "डिजिटल व्यक्तिगत डेटा संरक्षण अधिनियम 2023" else "Digital Personal Data Protection Act, 2023",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = saffronDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isHindi)
                                    "देवभाषा आपकी गोपनीयता, कुण्डली/साधना डेटा एवं व्यक्तिगत अधिकारों की पूर्ण सुरक्षा हेतु प्रतिबद्ध है।"
                                else
                                    "Devbhasha is fully committed to protecting your privacy, Kundli/Sadhana data, and user rights.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF4A5568),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 1: DPDP Act 2023 & User Data Rights
                if (selectedTab == 0 || selectedTab == 1) {
                    DpdpPrivacyRightsSection(isHindi = isHindi, saffronDark = saffronDark, goldPrimary = goldPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION 2: Astrological & Platform Service Disclaimer
                if (selectedTab == 0 || selectedTab == 2) {
                    AstrologicalDisclaimerSection(isHindi = isHindi, saffronDark = saffronDark, goldPrimary = goldPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION 3: Grievance Officer & Contact
                if (selectedTab == 0 || selectedTab == 3) {
                    GrievanceOfficerSection(
                        isHindi = isHindi,
                        context = context,
                        userId = userId,
                        saffronDark = saffronDark,
                        goldPrimary = goldPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom Action Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    if (onDisclaimerAccepted != null) {
                        // Checkbox agreement requirement for onboarding / mandatory consent
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = goldLight,
                            border = BorderStroke(1.dp, goldPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Checkbox(
                                    checked = isAcknowledgedChecked,
                                    onCheckedChange = { isAcknowledgedChecked = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = saffronDark,
                                        checkmarkColor = goldPrimary
                                    ),
                                    modifier = Modifier.testTag("dpdp_age_checkbox")
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isHindi)
                                        "मैंने DPDP नियम एवं अस्वीकरण पढ़ लिया है तथा मेरी आयु 18 वर्ष या उससे अधिक है।"
                                    else
                                        "I have read the DPDP policy & disclaimer and I am 18 years or older.",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 16.sp,
                                    color = saffronDark,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (isAcknowledgedChecked && !isSubmitting) {
                                    isSubmitting = true
                                    userSession.setAcceptedDisclaimer(userId, Constants.DISCLAIMER_VERSION, true)
                                    UserManager.recordDisclaimerAcceptance(userId, Constants.DISCLAIMER_VERSION) {
                                        isSubmitting = false
                                        onDisclaimerAccepted()
                                    }
                                }
                            },
                            enabled = isAcknowledgedChecked && !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("dpdp_agree_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = saffronDark,
                                disabledContainerColor = Color(0xFFCBD5E1)
                            )
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = goldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isHindi) "सहमत होकर आगे बढ़ें (Agree & Continue)" else "Agree & Accept Terms",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else if (onBackClick != null) {
                        Button(
                            onClick = onBackClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("dpdp_close_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = saffronDark)
                        ) {
                            Text(
                                text = if (isHindi) "समझ गया / बंद करें" else "Acknowledged / Close",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        // Standard display notice
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = saffronDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "DPDP Act 2023 अनुपालन प्रमाणीकृत" else "DPDP Act 2023 Compliant Platform",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = saffronDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DpdpPrivacyRightsSection(
    isHindi: Boolean,
    saffronDark: Color,
    goldPrimary: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dpdp_privacy_section_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = saffronDark.copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = saffronDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isHindi) "1. DPDP अधिनियम 2023 - उपयोगकर्ता अधिकार" else "1. DPDP Act 2023 - Your Privacy Rights",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = saffronDark
                    )
                    Text(
                        text = if (isHindi) "डिजिटल व्यक्तिगत डेटा सुरक्षा कानून के तहत आपके अधिकार" else "Legal data protection standards & user guarantees",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            val rights = if (isHindi) listOf(
                "सहमति आधारित प्रसंस्करण (Consent-Based Processing): आपका डेटा केवल आपकी स्पष्ट सहमति के बाद ही कुण्डली, दैनिक राशिफल एवं ज्योतिषीय परामर्श हेतु उपयोग किया जाता है।",
                "डेटा एक्सेस एवं देखने का अधिकार (Right to Access): आप अपने प्रोफाइल में संग्रहीत अपना नाम, जन्म तिथि, जन्म स्थान एवं साधना रिकॉर्ड किसी भी समय देख सकते हैं।",
                "डेटा सुधार एवं अद्यतन का अधिकार (Right to Correction): अपने प्रोफाइल विवरण में त्रुटि होने पर आप उसे अपडेट कर सकते हैं।",
                "डेटा मिटाने एवं सहमति वापसी का अधिकार (Right to Erasure / Right to Forget): आप 'सेटिंग्स > खाता हटाएं' में जाकर अपना संपूर्ण डेटा प्लेटफ़ॉर्म से स्थायी रूप से हटा सकते हैं।",
                "सुरक्षित क्लाउड इन्फ्रास्ट्रक्चर (AES-256 Encryption): आपका डेटा Firebase Cloud Server पर एंड-टू-एंड एन्क्रिप्शन के साथ पूर्णतः सुरक्षित रहता है।"
            ) else listOf(
                "Consent-Based Processing: Your personal & astrological data is processed only upon your explicit consent for generating Horoscopes and Sadhana services.",
                "Right to Access: You have full access to view your stored name, birth date, time, coordinates, and consultation history directly in app settings.",
                "Right to Correction: You can modify or correct any inaccurate personal information at any time.",
                "Right to Erasure (Right to be Forgotten): You can permanently purge your entire account and stored records via Settings > Delete Account.",
                "Secure Infrastructure: Stored on Firebase / GCP using AES-256 resting encryption and TLS 1.3 transit security."
            )

            rights.forEachIndexed { index, right ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = saffronDark.copy(alpha = 0.12f),
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = saffronDark
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = right,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}

@Composable
private fun AstrologicalDisclaimerSection(
    isHindi: Boolean,
    saffronDark: Color,
    goldPrimary: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dpdp_disclaimer_section_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = goldPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = saffronDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isHindi) "2. मंच एवं ज्योतिषीय सेवा अस्वीकरण" else "2. Platform & Consultation Disclaimer",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = saffronDark
                    )
                    Text(
                        text = if (isHindi) "आध्यात्मिक एवं ज्योतिषीय सेवाओं की महत्वपूर्ण जानकारी" else "Spiritual faith-based guidance guidelines",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isHindi) DisclaimerContent.HINDI_INTRO else DisclaimerContent.ENGLISH_INTRO,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val points = if (isHindi) DisclaimerContent.HINDI_POINTS else DisclaimerContent.ENGLISH_POINTS
            points.forEachIndexed { index, point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldPrimary
                    )
                    Text(
                        text = point,
                        fontSize = 12.sp,
                        lineHeight = 17.5.sp,
                        color = Color(0xFF475569)
                    )
                }
            }
        }
    }
}

@Composable
private fun GrievanceOfficerSection(
    isHindi: Boolean,
    context: android.content.Context,
    userId: String,
    saffronDark: Color,
    goldPrimary: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dpdp_grievance_section_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = saffronDark.copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = saffronDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isHindi) "3. शिकायत अधिकारी एवं डेटा सुरक्षा नोडल" else "3. Grievance Redressal & Nodal Officer",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = saffronDark
                    )
                    Text(
                        text = if (isHindi) "DPDP कानून के तहत शिकायत निवारण अधिकारी" else "Contact designated Grievance Officer under DPDP Act",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Designated Grievance Officer (DPDP Act, 2023):",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Name: ${DisclaimerContent.GRIEVANCE_OFFICER_NAME}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = saffronDark
                    )
                    Text(
                        text = "Email: ${DisclaimerContent.GRIEVANCE_EMAIL}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2563EB)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) "प्रतिक्रिया समय: 24 से 48 घंटे" else "Response SLA: Within 24-48 business hours",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${DisclaimerContent.GRIEVANCE_EMAIL}")
                                putExtra(Intent.EXTRA_SUBJECT, "Devbhasha DPDP Grievance / Data Protection Query")
                                putExtra(Intent.EXTRA_TEXT, "User ID: $userId\n\nIssue / Data Protection Query:\n")
                            }
                            try {
                                context.startActivity(emailIntent)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    if (isHindi) "ईमेल भेजने हेतु ${DisclaimerContent.GRIEVANCE_EMAIL} पर लिखें।"
                                    else "Please send an email to ${DisclaimerContent.GRIEVANCE_EMAIL}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = saffronDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("send_grievance_email_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = goldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "ईमेल भेजें (Send Email)" else "Send Email to Grievance Officer",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
