package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Chat
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class ProviderQuestionItem(
    val id: String = "",
    val userName: String = "",
    val category: String = "कुंडली / ग्रह दोष",
    val questionText: String = "",
    val timestamp: String = "10 मिनट पहले",
    val sadhakName: String = "",
    var status: String = "Pending", // "Pending" or "Answered"
    var providerAnswer: String = ""
)

data class LiveConsultationRequest(
    val id: String,
    val userName: String,
    val type: String, // "Chat" or "Audio Call"
    val durationMins: Int = 15,
    val topic: String,
    val timeAgo: String = "अभी (Just now)",
    var status: String = "Incoming" // "Incoming", "In Progress", "Completed"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDashboardScreen(
    currentSadhakName: String = "आचार्य देव शर्मा",
    providerId: String = "provider_1",
    onSwitchToUserDashboard: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(true) }
    var consultationRate by remember { mutableIntStateOf(19) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Requests, 1: Questions, 2: Earnings & Reviews

    // Mock incoming live consultation requests
    var liveRequests by remember {
        mutableStateOf(
            listOf(
                LiveConsultationRequest(
                    id = "req_1",
                    userName = "रोहित कुमार (Rohit Kumar)",
                    type = "Audio Call",
                    topic = "करियर में आ रही बाधाएं एवं राहू महादशा उपाय",
                    durationMins = 15,
                    timeAgo = "1 मिनट पहले"
                ),
                LiveConsultationRequest(
                    id = "req_2",
                    userName = "प्रिया सिंह (Priya Singh)",
                    type = "Chat",
                    topic = "विवाह मिलान एवं मांगलिक दोष निवारण",
                    durationMins = 20,
                    timeAgo = "5 मिनट पहले"
                )
            )
        )
    }

    // Mock Questions from users
    var questionsList by remember {
        mutableStateOf(
            listOf(
                ProviderQuestionItem(
                    id = "q_1",
                    userName = "राजेश शर्मा",
                    category = "💼 व्यापार एवं धन",
                    questionText = "व्यवसाय में लगातार विघ्न आ रहे हैं, कोई उपयुक्त वैदिक उपाय व रुद्राक्ष धारण सुझाएं।",
                    timestamp = "12 मिनट पहले",
                    sadhakName = currentSadhakName,
                    status = "Pending"
                ),
                ProviderQuestionItem(
                    id = "q_2",
                    userName = "सुनीता वर्मा",
                    category = "🌙 स्वप्न फल विचार",
                    questionText = "प्रातःकाल सपने में प्राचीन शिव मंदिर में गंगाजल अर्पित करते देखा। इसका क्या शुभ संकेत है?",
                    timestamp = "25 मिनट पहले",
                    sadhakName = currentSadhakName,
                    status = "Pending"
                ),
                ProviderQuestionItem(
                    id = "q_3",
                    userName = "अमित शुक्ला",
                    category = "🔮 कुंडली दोष",
                    questionText = "शनि की साढ़ेसाती के तीसरे चरण में किस प्रकार की सावधानियां बरतनी चाहिए?",
                    timestamp = "1 घंटा पहले",
                    sadhakName = currentSadhakName,
                    status = "Answered",
                    providerAnswer = "शनिवार को पीपल के वृक्ष के नीचे तिल के तेल का दीपक प्रज्वलित करें एवं नित्य हनुमान चालीसा का पाठ करें।"
                )
            )
        )
    }

    var answerInputs by remember { mutableStateOf(mapOf<String, String>()) }
    var activeOngoingSession by remember { mutableStateOf<LiveConsultationRequest?>(null) }
    var showRateDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Header Bar: Provider Brand + Switch Mode & Logout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Saffron,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "आचार्य",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentSadhakName,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = "सत्यापित साधक ✓",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "वैदिक परामर्श प्रदाता डैशबोर्ड (Provider)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        // Switch & Logout Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Switch to Normal User Mode
                            FilledTonalButton(
                                onClick = onSwitchToUserDashboard,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFF3F4F6),
                                    contentColor = Color(0xFF1F2937)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "User App",
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("यूज़र मोड", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // Logout Button
                            IconButton(
                                onClick = onLogoutClick,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEE2E2))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = "Log out",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Online Status Toggle & Per Minute Rate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (isOnline) Color(0xFFDCFCE7) else Color(0xFFF3F4F6),
                            border = BorderStroke(1.dp, if (isOnline) Color(0xFF86EFAC) else Color(0xFFE5E7EB)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable {
                                    isOnline = !isOnline
                                    Toast.makeText(
                                        context,
                                        if (isOnline) "स्थिति: ऑनलाइन (परामर्श स्वीकार्य) 🟢" else "स्थिति: ऑफलाइन (कंसल्टेशन बंद) ⚪",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) Color(0xFF16A34A) else Color(0xFF9CA3AF))
                                )
                                Text(
                                    text = if (isOnline) "लाइव ऑनलाइन (कंसल्टेशन चालू)" else "ऑफलाइन (विश्राम)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOnline) Color(0xFF16A34A) else Color(0xFF6B7280)
                                )
                            }
                        }

                        // Per-minute rate chip
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xFFFFF7ED),
                            border = BorderStroke(1.dp, Saffron.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { showRateDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "दर: ₹$consultationRate/मिनट",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Saffron
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit rate",
                                    tint = Saffron,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // 1. Metric Overview Cards (Role Specific Data)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProviderMetricCard(
                        title = "आज की कमाई",
                        value = "₹3,450",
                        subtitle = "+₹420 कल से",
                        icon = Icons.Outlined.CurrencyRupee,
                        accentColor = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                    ProviderMetricCard(
                        title = "कुल परामर्श",
                        value = "14",
                        subtitle = "182 मिनट कॉल",
                        icon = Icons.Outlined.HeadsetMic,
                        accentColor = Saffron,
                        modifier = Modifier.weight(1f)
                    )
                    ProviderMetricCard(
                        title = "रेटिंग",
                        value = "4.95 ⭐",
                        subtitle = "128 समीक्षाएं",
                        icon = Icons.Outlined.Star,
                        accentColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Navigation Tab Selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("लाइव अनुरोध (${liveRequests.count { it.status == "Incoming" }})", "प्रश्न उत्तर", "कमाई एवं समीक्षाएं")
                    tabs.forEachIndexed { index, tabTitle ->
                        val isSelected = selectedTab == index
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color.White else Color.Transparent,
                            shadowElevation = if (isSelected) 1.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedTab = index }
                        ) {
                            Text(
                                text = tabTitle,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF1F2937) else Color(0xFF6B7280),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // 3. Tab Contents
            when (selectedTab) {
                0 -> {
                    // LIVE CONSULTATION REQUESTS
                    item {
                        Text(
                            text = "इनकमिंग कंसल्टेशन अनुरोध (Live Requests)",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                    }

                    if (activeOngoingSession != null) {
                        item {
                            ActiveSessionCard(
                                session = activeOngoingSession!!,
                                onEndSession = {
                                    val ended = activeOngoingSession!!
                                    liveRequests = liveRequests.filter { it.id != ended.id }
                                    activeOngoingSession = null
                                    Toast.makeText(context, "परामर्श संपन्न हुआ! ₹285 खाते में जोड़े गए ✓", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }

                    val incomingRequests = liveRequests.filter { it.status == "Incoming" }
                    if (incomingRequests.isEmpty() && activeOngoingSession == null) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🪔", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "वर्तमान में कोई नया लाइव अनुरोध लंबित नहीं है।",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = Color(0xFF1F2937)
                                    )
                                    Text(
                                        text = "जब साधक चैट या कॉल का अनुरोध करेंगे, वह तुरंत यहाँ प्रदर्शित होगा।",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF6B7280),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(incomingRequests) { request ->
                            LiveRequestCard(
                                request = request,
                                onAccept = {
                                    activeOngoingSession = request.copy(status = "In Progress")
                                    liveRequests = liveRequests.map {
                                        if (it.id == request.id) it.copy(status = "In Progress") else it
                                    }
                                    Toast.makeText(context, "${request.userName} के साथ ${request.type} सत्र प्रारंभ हुआ ✨", Toast.LENGTH_SHORT).show()
                                },
                                onDecline = {
                                    liveRequests = liveRequests.filter { it.id != request.id }
                                    Toast.makeText(context, "अनुरोध निरस्त किया गया", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                1 -> {
                    // QUESTIONS & DREAMS INBOX
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "साधक प्रश्न एवं स्वप्न डायरी उत्तर",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            val pendingCount = questionsList.count { it.status == "Pending" }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (pendingCount > 0) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = "$pendingCount उत्तर लंबित",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pendingCount > 0) Color(0xFFB45309) else Color(0xFF16A34A),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    items(questionsList) { item ->
                        val currentAnswer = answerInputs[item.id] ?: item.providerAnswer
                        ProviderQuestionCard(
                            question = item,
                            answerText = currentAnswer,
                            onAnswerChange = { newText ->
                                answerInputs = answerInputs.toMutableMap().apply { put(item.id, newText) }
                            },
                            onSendAnswer = {
                                if (currentAnswer.isNotBlank()) {
                                    questionsList = questionsList.map {
                                        if (it.id == item.id) it.copy(status = "Answered", providerAnswer = currentAnswer) else it
                                    }
                                    Toast.makeText(context, "${item.userName} को समाधान भेज दिया गया ✓", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                2 -> {
                    // EARNINGS & REVIEWS
                    item {
                        ProviderEarningsSection(
                            dailyEarnings = 3450,
                            totalConsults = 14,
                            onWithdrawClick = {
                                Toast.makeText(context, "₹3,450 की निकासी का अनुरोध दर्ज हुआ (Payout requested) ✓", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Rate Setting Dialog
    if (showRateDialog) {
        var tempRate by remember { mutableIntStateOf(consultationRate) }
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "परामर्श दर निर्धारित करें (Consultation Rate)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Column {
                    Text(
                        text = "साधकों के लिए प्रति मिनट परामर्श शुल्क चुनें:",
                        fontSize = 12.sp,
                        color = Color(0xFF4B5563)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(12, 19, 25, 35).forEach { r ->
                            val isSel = tempRate == r
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) Saffron else Color(0xFFF3F4F6),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { tempRate = r }
                            ) {
                                Text(
                                    text = "₹$r/m",
                                    color = if (isSel) Color.White else Color(0xFF1F2937),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        consultationRate = tempRate
                        showRateDialog = false
                        Toast.makeText(context, "परामर्श दर ₹$tempRate/मिनट सेट हो गई!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("सेव करें", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) {
                    Text("रद्द करें", color = Color(0xFF6B7280))
                }
            }
        )
    }
}

@Composable
private fun ProviderMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color(0xFF4B5563),
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.5.sp,
                color = accentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LiveRequestCard(
    request: LiveConsultationRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.2.dp, Saffron.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFF7ED),
                        border = BorderStroke(1.dp, Saffron),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (request.type == "Audio Call") Icons.Outlined.Phone else Icons.AutoMirrored.Outlined.Chat,
                                contentDescription = null,
                                tint = Saffron,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = request.userName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = "${request.type} परामर्श · ${request.durationMins} मिनट",
                            fontSize = 11.sp,
                            color = Saffron,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = request.timeAgo,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF9FAFB),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "विषय: ${request.topic}",
                    fontSize = 12.sp,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Accept & Decline Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B7280))
                ) {
                    Text("अस्वीकार (Decline)", fontSize = 12.sp)
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1.3f).height(44.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                ) {
                    Icon(
                        imageVector = if (request.type == "Audio Call") Icons.Filled.Call else Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("स्वीकारें (Accept)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ActiveSessionCard(
    session: LiveConsultationRequest,
    onEndSession: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF0FDF4),
        border = BorderStroke(1.5.dp, Color(0xFF86EFAC)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF16A34A)))
                    Text(
                        text = "लाइव सत्र जारी है (In Progress)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }
                Text(
                    text = "दर: ₹19/min",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${session.userName} · ${session.type}",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            Text(
                text = session.topic,
                fontSize = 12.sp,
                color = Color(0xFF4B5563)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onEndSession,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("परामर्श समाप्त करें (Complete & Collect ₹285)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProviderQuestionCard(
    question: ProviderQuestionItem,
    answerText: String,
    onAnswerChange: (String) -> Unit,
    onSendAnswer: () -> Unit
) {
    val isAnswered = question.status == "Answered"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (isAnswered) Color(0xFFE5E7EB) else Saffron.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = question.userName,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "· ${question.category}",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isAnswered) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (isAnswered) "समाधान दिया गया ✓" else "उत्तर लंबित",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAnswered) Color(0xFF16A34A) else Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = question.questionText,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (isAnswered) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "आपका वैदिक समाधान:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                        Text(
                            text = question.providerAnswer,
                            fontSize = 12.sp,
                            color = Color(0xFF1F2937),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = answerText,
                    onValueChange = onAnswerChange,
                    placeholder = { Text("वैदिक उपाय, मंत्र अथवा स्वप्न फल यहाँ लिखें...", fontSize = 12.sp, color = Color(0xFF9CA3AF)) },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Saffron,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onSendAnswer,
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("समाधान भेजें (Send Answer)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderEarningsSection(
    dailyEarnings: Int,
    totalConsults: Int,
    onWithdrawClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "कुल उपलब्ध शेष (Available Balance)",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "₹18,920",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Button(
                        onClick = onWithdrawClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.AccountBalance, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("निकासी करें (Withdraw)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFF3F4F6))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("आज का योग: ₹$dailyEarnings", fontSize = 11.5.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
                    Text("कमीशन दर: 15% प्लेटफ़ॉर्म", fontSize = 11.5.sp, color = Color(0xFF9CA3AF))
                }
            }
        }

        // Recent reviews
        Text(
            text = "नवीनतम साधक समीक्षाएं (Recent Reviews)",
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        val reviews = listOf(
            Triple("सुनील मेहता", "5.0 ⭐", "गुरुजी का मार्गदर्शन अत्यंत सटीक रहा। पारिवारिक शांति के उपाय से काफी लाभ हुआ।"),
            Triple("अंजली त्रिपाठी", "5.0 ⭐", "कुंडली विश्लेषण और उपाय बहुत सरल और प्रभावकारी थे। आभार।"),
            Triple("मनीष गोयल", "4.8 ⭐", "व्यवसाय में आ रही मंदी के लिए दिए गए मंत्र जप से सकारात्मक ऊर्जा मिली।")
        )

        reviews.forEach { (name, rating, comment) ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1F2937))
                        Text(rating, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFF59E0B))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(comment, fontSize = 12.sp, color = Color(0xFF4B5563), lineHeight = 16.sp)
                }
            }
        }
    }
}
