package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.DEFAULT_RASHIS
import com.example.ui.models.RashiHoroscope
import com.example.ui.components.DevWatermarkLogo
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 1. Radiant Hero Promo Banner Inspired by Top Astrology Apps
 * (Golden/Saffron gradient, Diya/Sun rays, ₹499 / 20 min consultation)
 */
@Composable
fun HeroAstroPromoBanner(
    isHindi: Boolean,
    onConsultClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFF8A00), // Saffron Gradient Start
            Color(0xFFFF6B00)  // Strict Saffron #FF6B00
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(6.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .drawBehind {
                    // Subtle decorative golden sunburst lines
                    for (i in 0 until 8) {
                        val angle = Math.toRadians(i * 45.0)
                        val start = Offset(size.width * 0.85f, size.height * 0.35f)
                        val end = Offset(
                            (start.x + Math.cos(angle) * 120).toFloat(),
                            (start.y + Math.sin(angle) * 120).toFloat()
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = start,
                            end = end,
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Tag with Shimmer Star
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✨",
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isHindi) "प्रथम परामर्श विशेष दक्षिणा" else "First Consultation Offer",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFFEF08A),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isHindi) "₹499 / 20 मिनट" else "₹499 / 20 min",
                            color = Color(0xFF854D0E),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Headline
                Text(
                    text = if (isHindi) "सत्यापित वैदिक साधकों से मार्गदर्शन" else "Consult Verified Vedic Astrologers",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isHindi) "कुंडली, विवाह, करियर एवं गृह शांति पर तुरंत समाधान प्राप्त करें"
                    else "Get instant guidance on Kundli, career, marriage & peace",
                    fontSize = 12.5.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Trust Badges & Action CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔒 100% गोपनीय",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "⭐ 4.9 रेटिंग",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onConsultClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = SaffronDeep
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = if (isHindi) "अभी बात करें →" else "Consult Now →",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. Today's Panchang & Auspicious Muhurat Card
 */
@Composable
fun TodayPanchangCard(
    isHindi: Boolean,
    onViewFullPanchang: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayFormatted = remember {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("hi", "IN"))
        sdf.format(Date())
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFAFAFA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🪔", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (isHindi) "आज का पंचांग एवं शुभ मुहूर्त" else "Today's Panchang & Muhurat",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                        Text(
                            text = todayFormatted,
                            fontSize = 10.sp,
                            color = Color(0xFF737373)
                        )
                    }
                }

                TextButton(
                    onClick = onViewFullPanchang,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (isHindi) "विस्तार" else "Details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDeep
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3 Mini Compact Panchang Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PanchangPill(
                    label = if (isHindi) "तिथि" else "Tithi",
                    value = if (isHindi) "शुक्ल पक्ष नवमी" else "Shukla Navami",
                    modifier = Modifier.weight(1f)
                )
                PanchangPill(
                    label = if (isHindi) "नक्षत्र" else "Nakshatra",
                    value = if (isHindi) "रोहिणी" else "Rohini",
                    modifier = Modifier.weight(1f)
                )
                PanchangPill(
                    label = if (isHindi) "अभिजीत" else "Abhijit",
                    value = "11:45 AM",
                    isGood = true,
                    modifier = Modifier.weight(1.1f)
                )
            }
        }
    }
}

@Composable
private fun PanchangPill(
    label: String,
    value: String,
    isGood: Boolean? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(
            1.dp,
            if (isGood == true) Color(0xFFA7F3D0) else BorderLight
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGood == true) Color(0xFF047857) else Color(0xFF1E293B),
                maxLines = 1
            )
        }
    }
}

/**
 * 3. Daily Horoscope BottomSheet (दैनिक राशिफल - 12 राशियां)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyHoroscopeBottomSheet(
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onConsultSadhak: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedRashi by remember { mutableStateOf(DEFAULT_RASHIS.first()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF08A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌟", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isHindi) "आज का दैनिक राशिफल" else "Today's Horoscope",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isHindi) "अपनी राशि चुनें और भविष्यफल जानें" else "Select your Zodiac sign",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 12 Rashi Horizontal Scrollable Picker
            Text(
                text = if (isHindi) "राशि चयन करें:" else "Select Sign:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DEFAULT_RASHIS.forEach { rashi ->
                    val isSelected = rashi.id == selectedRashi.id
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedRashi = rashi },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF000000) else Color(0xFFFAFAFA),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF000000) else BorderLight
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(text = rashi.symbol, fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (isHindi) rashi.nameHi else rashi.nameEn,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF000000)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Rashi Detailed Prediction Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = selectedRashi.symbol, fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isHindi) "${selectedRashi.nameHi} राशि (${selectedRashi.nameEn})"
                                    else "${selectedRashi.nameEn} (${selectedRashi.nameHi})",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF000000)
                                )
                                Text(
                                    text = selectedRashi.dates,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF737373)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFAFAFA),
                            border = BorderStroke(1.dp, BorderLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Saffron,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = selectedRashi.rating,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF000000)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Lucky details pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LuckyAttrPill(
                            label = if (isHindi) "स्वामी ग्रह" else "Lord",
                            value = selectedRashi.rulingPlanet,
                            modifier = Modifier.weight(1f)
                        )
                        LuckyAttrPill(
                            label = if (isHindi) "शुभ रंग" else "Color",
                            value = selectedRashi.luckyColor,
                            modifier = Modifier.weight(1.3f)
                        )
                        LuckyAttrPill(
                            label = if (isHindi) "शुभ अंक" else "Number",
                            value = "${selectedRashi.luckyNumber}",
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isHindi) "आज का भविष्यफल:" else "Today's Prediction:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDeep
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) selectedRashi.predictionHi else selectedRashi.predictionEn,
                        fontSize = 13.5.sp,
                        color = Color(0xFF334155),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        DevWatermarkLogo()
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onDismiss()
                            onConsultSadhak(selectedRashi.nameHi)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "इस राशि के विशेष उपाय हेतु साधक से बात करें"
                            else "Consult Sadhak for Rashi Remedy",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LuckyAttrPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                maxLines = 1
            )
        }
    }
}

/**
 * 4. Free Kundli Generator BottomSheet (मुफ्त जन्म कुंडली)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeKundliBottomSheet(
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onConsultSadhak: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var nameInput by remember { mutableStateOf("") }
    var dobInput by remember { mutableStateOf("15/08/1995") }
    var timeInput by remember { mutableStateOf("10:30 AM") }
    var placeInput by remember { mutableStateOf("नई दिल्ली / New Delhi") }
    var hasGenerated by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFAFAFA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📜", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isHindi) "मुफ्त जन्म कुंडली विश्लेषण" else "Free Kundli Analysis",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                        Text(
                            text = if (isHindi) "वैदिक ज्योतिष गणना अनुसार लग्न व ग्रह स्थिति" else "Vedic planetary calculation",
                            fontSize = 12.sp,
                            color = Color(0xFF737373)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!hasGenerated) {
                // Input form
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(if (isHindi) "जातक का नाम" else "Full Name") },
                    placeholder = { Text("उदा. दीपक शर्मा") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dobInput,
                        onValueChange = { dobInput = it },
                        label = { Text(if (isHindi) "जन्म तिथि" else "DOB") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        label = { Text(if (isHindi) "जन्म समय" else "Time") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = placeInput,
                    onValueChange = { placeInput = it },
                    label = { Text(if (isHindi) "जन्म स्थान" else "Birth Place") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        hasGenerated = true
                        Toast.makeText(context, "कुंडली का विश्लेषण सफल!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isHindi) "कुंडली तैयार करें (Free Kundli)" else "Generate Kundli",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else {
                // Generated Kundli Result View
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "कुंडली चक्र तैयार है" else "Kundli Chart Ready",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF000000)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Grid of key findings
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KundliInfoBox("लग्न (Ascendant)", "मेष (Aries)", Modifier.weight(1f))
                            KundliInfoBox("चंद्र राशि (Moon Sign)", "सिंह (Leo)", Modifier.weight(1f))
                            KundliInfoBox("नक्षत्र (Nakshatra)", "मघा (Magha)", Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isHindi) "ज्योतिषीय अवलोकन:" else "Astrological Overview:",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Saffron
                        )
                        Text(
                            text = if (isHindi)
                                "आपकी कुंडली में सूर्य पंचम भाव में स्वगृही होकर ज्ञान और मान-सम्मान के प्रबल योग बना रहे हैं। बृहस्पति का शुभ दृष्टि संबंध आर्थिक स्थिरता प्रदान करेगा।"
                            else
                                "Sun in 5th house forms strong Raja Yoga for recognition. Jupiter aspect brings sustained financial prosperity.",
                            fontSize = 12.5.sp,
                            color = Color(0xFF334155),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { hasGenerated = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isHindi) "पुनः गणना" else "Recalculate")
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onConsultSadhak()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text(if (isHindi) "साधक से परामर्श (₹499)" else "Consult Sadhak (₹499)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun KundliInfoBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 10.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Saffron, textAlign = TextAlign.Center)
        }
    }
}

/**
 * 5. Kundli Matching (कुंडली मिलान / 36 गुण मिलान)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KundliMatchingBottomSheet(
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onConsultSadhak: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isMatched by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💍", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isHindi) "कुंडली मिलान (36 गुण)" else "Kundli Matching",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isHindi) "वर-कन्या का अष्टकूट गुण मिलान" else "Ashtakoot Guna Matching",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!isMatched) {
                OutlinedTextField(
                    value = "वर (Boy): राहुल शर्मा • 12/04/1994",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isHindi) "वर विवरण (Boy's Details)" else "Boy's Details") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = "कन्या (Girl): प्रिया वर्मा • 08/09/1996",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isHindi) "कन्या विवरण (Girl's Details)" else "Girl's Details") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { isMatched = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isHindi) "गुण मिलान देखें (Match Guna)" else "Check Match",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFFF1F2),
                    border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "28 / 36",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFE11D48)
                        )
                        Text(
                            text = if (isHindi) "उत्तम मिलान (Auspicious Match)" else "Very Auspicious Match",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9F1239)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isHindi) "नाड़ी दोष: नहीं • मांगलिक दोष: सौम्य • भकूट: शुभ"
                            else "Nadi Dosha: None • Manglik: Mild • Bhakoot: Auspicious",
                            fontSize = 12.sp,
                            color = Color(0xFF4C0519)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onConsultSadhak()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isHindi) "साधक से विस्तृत मिलान परामर्श लें" else "Detailed Consultation with Sadhak")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * 6. Astro Quick Feature Cards Grid (दैनिक राशिफल, जन्म कुंडली, कुंडली मिलान, पंचांग)
 * Placed directly below the Search Bar & Categories, and above the Promotional Banner.
 */
@Composable
fun AstroQuickFeatureCards(
    isHindi: Boolean,
    onHoroscopeClick: () -> Unit,
    onKundliClick: () -> Unit,
    onKundliMilanClick: () -> Unit,
    onPanchangClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Daily Horoscope
            AstroMiniCard(
                iconEmoji = "🌟",
                title = if (isHindi) "दैनिक राशिफल" else "Daily Horoscope",
                subtitle = if (isHindi) "12 राशियों का भविष्य" else "12 Zodiac Signs",
                badgeText = if (isHindi) "दैनिक" else "Daily",
                bgColor = Color(0xFFFAFAFA),
                borderColor = BorderLight,
                badgeBg = Color(0xFF000000),
                badgeTextColor = Color.White,
                onClick = onHoroscopeClick,
                modifier = Modifier.weight(1f)
            )

            // Card 2: Family Problems
            AstroMiniCard(
                iconEmoji = "👨‍👩‍👧‍👦",
                title = if (isHindi) "पारिवारिक समस्या" else "Family Problems",
                subtitle = if (isHindi) "क्लेश निवारण व शांति" else "Peace & Harmony",
                badgeText = if (isHindi) "समाधान" else "Remedies",
                bgColor = Color(0xFFFAFAFA),
                borderColor = BorderLight,
                badgeBg = Saffron,
                badgeTextColor = Color.White,
                onClick = onKundliClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 3: Money Problem
            AstroMiniCard(
                iconEmoji = "💰",
                title = if (isHindi) "धन समस्या" else "Money Problem",
                subtitle = if (isHindi) "कर्ज मुक्ति व धन लाभ" else "Debt Relief & Wealth",
                badgeText = if (isHindi) "उपाय" else "Remedies",
                bgColor = Color(0xFFFAFAFA),
                borderColor = BorderLight,
                badgeBg = Color(0xFF000000),
                badgeTextColor = Color.White,
                onClick = onKundliMilanClick,
                modifier = Modifier.weight(1f)
            )

            // Card 4: Today's Panchang
            AstroMiniCard(
                iconEmoji = "📅",
                title = if (isHindi) "दैनिक पंचांग" else "Daily Panchang",
                subtitle = if (isHindi) "शुभ मुहूर्त व राहुकाल" else "Muhurat & Tithi",
                badgeText = if (isHindi) "आज" else "Today",
                bgColor = Color(0xFFFAFAFA),
                borderColor = BorderLight,
                badgeBg = Color(0xFF16A34A),
                badgeTextColor = Color.White,
                onClick = onPanchangClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AstroMiniCard(
    iconEmoji: String,
    title: String,
    subtitle: String,
    badgeText: String,
    bgColor: Color,
    borderColor: Color,
    badgeBg: Color,
    badgeTextColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = iconEmoji, fontSize = 17.sp)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = Color(0xFF64748B),
                maxLines = 1
            )
        }
    }
}

