package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.Constants
import com.example.utils.UserManager
import com.example.utils.UserSession

@Composable
fun ConsentScreen(
    userId: String,
    userSession: UserSession,
    onConsentAccepted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHindi by remember { mutableStateOf(true) }
    var isAge18Checked by remember { mutableStateOf(false) }
    var isConsentChecked by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // Header Bar with Language Toggle Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "डेटा सुरक्षा व सहमति" else "Data Consent Notice",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Hindi / English Toggle Switch
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = isHindi,
                            onClick = { isHindi = true },
                            label = { Text("हिंदी", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = null,
                            modifier = Modifier.height(30.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            selected = !isHindi,
                            onClick = { isHindi = false },
                            label = { Text("English", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = null,
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Intro Banner
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderBeige)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isHindi) "डिजिटल व्यक्तिगत डेटा संरक्षण अधिनियम (DPDP Act, 2023) के अंतर्गत सहमति"
                            else "Consent under Digital Personal Data Protection Act (DPDP Act, 2023)",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Saffron
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isHindi)
                                "देवभाषा (Devbhasha) ऐप आपकी गोपनीयता व डेटा सुरक्षा का सम्मान करता है। सेवाएं प्रदान करने हेतु आपके डेटा का उपयोग पारदर्शी रूप से किया जाता है।"
                            else
                                "Devbhasha respects your privacy & data protection. Your personal data is collected transparently to provide personalized services.",
                            fontSize = 12.5.sp,
                            color = TextSub,
                            lineHeight = 18.sp
                        )
                    }
                }

                // 1. Data Collected
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "हम कौन सा डेटा एकत्र करते हैं? (Data Collected)"
                                else "What Personal Data We Collect",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val items = if (isHindi) listOf(
                            "• नाम और संपर्क नंबर (Phone Number)",
                            "• जन्म तिथि, जन्म समय व जन्म स्थान (जन्मकुंडली हेतु)",
                            "• चैट व कॉल रिकॉर्ड्स (ज्योतिष सलाह परामर्श हेतु)",
                            "• भुगतान इतिहास व वॉलेट लेनदेन (Payment History)"
                        ) else listOf(
                            "• Name and Phone Number",
                            "• Date, Time, and Place of Birth (for horoscope)",
                            "• Chat & Call consultation logs",
                            "• Payment history & Wallet transactions"
                        )
                        items.forEach { item ->
                            Text(
                                text = item,
                                fontSize = 12.sp,
                                color = TextSub,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }

                // 2. Purpose
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "डेटा संग्रह का उद्देश्य (Purpose of Processing)"
                                else "Purpose of Collecting Data",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val purposes = if (isHindi) listOf(
                            "• सटीक ज्योतिष सलाह व पंचांग/कुंडली विश्लेषण",
                            "• Sadhak/आचार्य से संपर्क व परामर्श सुविधा",
                            "• सुरक्षित भुगतान प्रक्रिया व वॉलेट प्रबंधन",
                            "• ग्राहक सहायता एवं सेवा सुधार (Support)"
                        ) else listOf(
                            "• Accurate astrological readings & horoscope analysis",
                            "• Connecting with verified Sadhaks/Astrologers",
                            "• Secure payment processing & wallet balance management",
                            "• Customer support & service improvement"
                        )
                        purposes.forEach { purpose ->
                            Text(
                                text = purpose,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }

                // 3. Withdrawal & Grievance
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "सहमति वापस लेना व शिकायत निवारण (Rights & Grievances)"
                                else "Withdrawal Right & Grievance Redressal",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isHindi)
                                "• सहमति वापस लेने का तरीका: आप कभी भी प्रोफ़ाइल / सेटिंग्स अनुभाग में जाकर 'Consent वापस लें' विकल्प चुनकर अपनी सहमति वापस ले सकते हैं या खाता हटा सकते हैं।\n" +
                                        "• शिकायत निवारण अधिकारी: किसी भी प्रश्न या शिकायत हेतु संपर्क करें: ${Constants.GRIEVANCE_EMAIL}\n" +
                                        "• भारतीय डिजिटल व्यक्तिगत डेटा संरक्षण बोर्ड (DPBI): आप आवश्यकता पड़ने पर Data Protection Board of India के समक्ष भी शिकायत दर्ज करा सकते हैं।"
                            else
                                "• Right to Withdraw: You can withdraw your consent anytime under Profile / Settings by selecting 'Withdraw Consent' or deleting your account.\n" +
                                        "• Grievance Officer: For any privacy concerns or grievances, email us at: ${Constants.GRIEVANCE_EMAIL}\n" +
                                        "• Data Protection Board of India (DPBI): You also reserve the right to file a complaint with the Data Protection Board of India as per the DPDP Act.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    }
                }

                // Checkboxes section
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isHindi) "कृपया आगे बढ़ने के लिए दोनों बॉक्स टिक करें:"
                            else "Please check both boxes to proceed:",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Checkbox 1: Age
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = isAge18Checked,
                                onCheckedChange = { isAge18Checked = it },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isHindi) "Main 18 varsh ya usse adhik ka hoon (मैं 18 वर्ष या उससे अधिक आयु का हूँ)"
                                else "I am 18 years of age or older",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Checkbox 2: Consent
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = isConsentChecked,
                                onCheckedChange = { isConsentChecked = it },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isHindi) "Main sahmat hoon (मैं देवभाषा डेटा नियमों से पूर्णतः सहमत हूँ)"
                                else "I agree (Main sahmat hoon to data processing terms)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            Button(
                onClick = {
                    if (isAge18Checked && isConsentChecked && !isSubmitting) {
                        isSubmitting = true
                        userSession.setGivenConsent(userId, true)
                        UserManager.recordConsent(userId, Constants.CONSENT_VERSION) {
                            isSubmitting = false
                            onConsentAccepted()
                        }
                    }
                },
                enabled = isAge18Checked && isConsentChecked && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.outline
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isHindi) "सहमति दें व आगे बढ़ें (Accept & Continue)" else "Accept & Continue",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
