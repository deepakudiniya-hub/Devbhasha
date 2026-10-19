package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.SadhakItem
import com.example.ui.theme.*
import com.example.utils.UserSession
import java.util.Calendar

/**
 * 1. Daily Horoscope Card for Home Screen
 */
@Composable
fun BentoEditorialHoroscopeCard(
    isHindi: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSession = remember { UserSession(context) }
    var selectedRashi by remember { mutableStateOf(userSession.getSelectedRashi().ifBlank { "mesh" }) }

    val rashis = listOf(
        Triple("mesh", "मेष", "Mesh"),
        Triple("vrishabh", "वृषभ", "Vrishabh"),
        Triple("mithun", "मिथुन", "Mithun"),
        Triple("kark", "कर्क", "Kark"),
        Triple("simha", "सिंह", "Simha"),
        Triple("kanya", "कन्या", "Kanya"),
        Triple("tula", "तुला", "Tula"),
        Triple("vrischika", "वृश्चिक", "Vrischika"),
        Triple("dhanu", "धनु", "Dhanu"),
        Triple("makar", "मकर", "Makar"),
        Triple("kumbh", "कुंभ", "Kumbh"),
        Triple("meen", "मीन", "Meen")
    )

    val horoscopeData = mapOf(
        "mesh" to Pair("आज का दिन ऊर्जावान रहेगा। कार्यक्षेत्र में सफलता और मान-सम्मान की प्राप्ति होगी। परिवार का सहयोग मिलेगा।", "Red / लाल • अंक: 9"),
        "vrishabh" to Pair("धन लाभ के योग बन रहे हैं। निवेश में सावधानी बरतें। स्वास्थ्य उत्तम रहेगा, यात्रा टालें।", "White / सफेद • अंक: 6"),
        "mithun" to Pair("विद्यार्थियों के लिए उत्तम समय है। नए संपर्क बनेंगे जो भविष्य में लाभ देंगे। मानसिक शांति बनाए रखें।", "Green / हरा • अंक: 5"),
        "kark" to Pair("भावनात्मक स्थिरता रखें। पारिवारिक मामलों में धैर्य से काम लें। व्यवसाय में गति आएगी।", "Yellow / पीला • अंक: 2"),
        "simha" to Pair("नेतृत्व क्षमता उभरेगी। उच्च अधिकारियों का सहयोग मिलेगा। आर्थिक स्थिति मजबूत होगी।", "Gold / सुनहरा • अंक: 1"),
        "kanya" to Pair("व्यापार में नए अवसर मिलेंगे। पुराने मित्रों से मुलाकात मन प्रसन्न करेगी। खानपान का ध्यान रखें।", "Blue / नीला • अंक: 3"),
        "tula" to Pair("दांपत्य जीवन में मधुरता आएगी। साझेदारी के कार्यों में लाभ होगा। रचनात्मक कार्यों में रुचि बढ़ेगी।", "Pink / गुलाबी • अंक: 7"),
        "vrischika" to Pair("अचानक धन लाभ या रुका हुआ पैसा मिलने के योग हैं। स्वास्थ्य के प्रति सचेत रहें।", "Maroon / गहरा लाल • अंक: 8"),
        "dhanu" to Pair("धार्मिक और आध्यात्मिक कार्यों में मन लगेगा। लंबी यात्रा के योग हैं। भाग्य का पूरा साथ मिलेगा।", "Saffron / केसरिया • अंक: 3"),
        "makar" to Pair("कड़ी मेहनत का फल मिलेगा। करियर में प्रमोशन या नई जिम्मेदारी मिल सकती है। धैर्य रखें।", "Grey / स्लेटी • अंक: 4"),
        "kumbh" to Pair("मित्रों का सहयोग प्राप्त होगा। रचनात्मक कार्यों से आय में वृद्धि होगी। स्वास्थ्य सामान्य रहेगा।", "Cyan / आसमानी • अंक: 11"),
        "meen" to Pair("मन में सकारात्मक विचार आएंगे। परिवार के साथ आनंददायक समय बीतेगा। निवेश लाभकारी रहेगा।", "Yellow / पीला • अंक: 3")
    )

    val currentInfo = horoscopeData[selectedRashi] ?: Pair("आज का दिन मंगलमय रहेगा।", "Orange • अंक: 1")

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialFactoryCardColor(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✨", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "दैनिक राशिफल (Daily Horoscope)" else "Daily Horoscope",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "आज",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rashi Selector Horizontal Scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rashis.forEach { (id, hiName, enName) ->
                    val isSelected = (selectedRashi == id)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedRashi = id
                            userSession.setSelectedRashi(id)
                        },
                        label = {
                            Text(text = if (isHindi) hiName else enName, fontSize = 13.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prediction Text Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = currentInfo.first,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "शुभ: ${currentInfo.second}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "पंचांग आधारित",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. First Call Free & Promo Code Banner
 */
@Composable
fun BentoEditorialPromoBanner(
    onApplyCode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var promoCodeInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF8B0000), Color(0xFFD4AF37))
                )
            )
            .clickable { showDialog = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "🎁 स्वागत ऑफर (Welcome Offer)",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "पहला परामर्श बिल्कुल मुफ़्त! (First Call Free)",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "कूपन कोड 'WELCOME100' या 'FIRSTFREE' का उपयोग करें",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.CardGiftcard,
                        contentDescription = "Claim Offer",
                        tint = Color(0xFF8B0000),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("प्रोमो कोड लागू करें (Apply Promo Code)") },
            text = {
                Column {
                    Text("अपना कूपन कोड दर्ज करें (जैसे: WELCOME100, FIRSTFREE):", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = promoCodeInput,
                        onValueChange = { promoCodeInput = it },
                        placeholder = { Text("WELCOME100") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (promoCodeInput.isNotBlank()) {
                            onApplyCode(promoCodeInput.trim())
                            Toast.makeText(context, "कूपन '${promoCodeInput.uppercase()}' सफलतापूर्वक लागू हुआ!", Toast.LENGTH_SHORT).show()
                            promoCodeInput = ""
                            showDialog = false
                        } else {
                            Toast.makeText(context, "कृपया वैध कूपन कोड दर्ज करें", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("लागू करें (Apply)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}

/**
 * 5. Refer & Earn Card
 */
@Composable
fun BentoEditorialReferAndEarn(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSession = remember { UserSession(context) }
    val referralCode = "DEV_${userSession.getUserId().takeLast(6).uppercase()}"

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialFactoryCardColor(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🤝", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "रेफर करें और ₹50 पाएं (Refer & Earn)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "मित्रों को ऐप पर आमंत्रित करें और प्रत्येक सफल जॉइनिंग पर ₹50 वॉलेट बोनस प्राप्त करें।",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = referralCode,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy code",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Referral Code", referralCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "रेफरल कोड कॉपी हो गया: $referralCode", Toast.LENGTH_SHORT).show()
                                },
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. Waitlist / Notify Me Dialog
 */
@Composable
fun WaitlistDialog(
    sadhak: SadhakItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("प्रतीक्षा सूची (Join Waitlist)") },
        text = {
            Column {
                Text("${sadhak.nameHi.ifBlank { sadhak.nameEn }} वर्तमान में व्यस्त (Busy/Offline) हैं।", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("जैसे ही साधक ऑनलाइन आएंगे, आपको तुरंत पुश नोटिफिकेशन (Push Notification) मिल जाएगी।", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("नोटिफ़ाई मी (Notify Me)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें")
            }
        }
    )
}

@Composable
fun MaterialFactoryCardColor(): Color {
    return MaterialTheme.colorScheme.surface
}
