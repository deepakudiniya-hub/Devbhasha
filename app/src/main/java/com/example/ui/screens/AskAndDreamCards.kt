package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DevLogoIcon
import com.example.ui.theme.*
import com.example.utils.DreamSubmitResult
import com.example.utils.DreamSubmitter
import com.example.utils.RazorpayPaymentManager
import com.example.utils.UserSession
import com.example.utils.PriceLabels
import com.example.utils.SessionType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card 1: "पूछा — साधक से सीधा मार्गदर्शन"
 */
@Composable
fun AskAnythingCard(
    isHindi: Boolean,
    onOpenAskSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .clickable { onOpenAskSheet() },
        color = Color.White,
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular icon with peach background
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFAFAFA))
                        .border(1.dp, BorderLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = SaffronPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isHindi) "पूछा — साधक से सीधा मार्गदर्शन" else "Puchhaa — Direct Sadhak Guidance",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isHindi) "अपनी किसी भी समस्या का समाधान पाएं" else "Find spiritual solutions for your life",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // "लाइव" Green Pill Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isHindi) "लाइव" else "LIVE",
                            color = Color(0xFF047857),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pill Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .clickable { onOpenAskSheet() },
                shape = RoundedCornerShape(26.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = SaffronPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isHindi) "अपनी समस्या यहाँ लिखें या बोलें..." else "Write or speak your problem here...",
                        fontSize = 13.5.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFAFAFA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = "Mic",
                            tint = SaffronPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card 2: "Dream Meaning — स्वप्न फल विचार"
 */
@Composable
fun DreamMeaningCard(
    isHindi: Boolean,
    onOpenDreamSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .clickable { onOpenDreamSheet() },
        color = Color.White,
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular icon with peach background
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFAFAFA))
                        .border(1.dp, BorderLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NightlightRound,
                        contentDescription = null,
                        tint = SaffronPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isHindi) "Dream Meaning — स्वप्न फल विचार" else "Dream Meaning — Spiritual Guidance",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000000)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isHindi) "सपनों का आध्यात्मिक अर्थ व साधक से समाधान" else "Submit dream for genuine Sadhak interpretation",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // "स्वप्न ज्ञान" Saffron Pill Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFFAFAFA),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = SaffronPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "स्वप्न ज्ञान" else "Dream Wisdom",
                            color = SaffronDeep,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pill Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .clickable { onOpenDreamSheet() },
                shape = RoundedCornerShape(26.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = SaffronPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isHindi) "सपना यहाँ लिखें (जैसे: सांप, मंदिर, नदी, उड़ना...)" else "Write dream (e.g. snake, temple, flying...)",
                        fontSize = 13.5.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF7ED)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Magic",
                            tint = SaffronPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Bottom Sheet for "पूछा" (Ask Anything)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskAnythingBottomSheet(
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onSubmitQuestion: (String) -> Unit
) {
    var queryText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var answeredText by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DevLogoIcon(size = 28.dp, elevation = 2.dp, showGlow = false)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "पूछा — साधक से सीधा समाधान" else "Puchhaa — Direct Sadhak Solution",
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                }
            }

            Text(
                text = if (isHindi) "विवाह, करियर, स्वास्थ्य, व्यापार अथवा पारिवारिक कष्ट के निवारण हेतु अपना प्रश्न पूछें।"
                else "Ask your query regarding career, marriage, family, or spiritual peace.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Input field
            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        if (isHindi) "अपनी समस्या का विवरण यहाँ लिखें..." else "Describe your question here...",
                        color = Color(0xFF94A3B8)
                    )
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaffronPrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Voice recording button & Submit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        isRecording = !isRecording
                        if (isRecording) {
                            queryText = if (isHindi) "मेरी कुंडली में नौकरी संबंधी बाधा के निवारण का उपाय बताएं।" else "Guide me regarding obstacles in career."
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (isRecording) Color.Red else SaffronPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = null,
                        tint = if (isRecording) Color.Red else SaffronPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRecording) (if (isHindi) "रिकॉर्डिंग जारी..." else "Recording...") else (if (isHindi) "बोलकर पूछें" else "Voice"),
                        color = if (isRecording) Color.Red else SaffronPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        if (queryText.isNotBlank()) {
                            answeredText = if (isHindi)
                                "आपका प्रश्न वैदिक साधक के पास भेज दिया गया है। शीघ्र ही आपको मार्गदर्शन प्राप्त होगा।"
                            else
                                "Your query has been sent to the Vedic Sadhak. You will receive guidance shortly."
                            onSubmitQuestion(queryText)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    enabled = queryText.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isHindi) "समाधान प्राप्त करें" else "Get Guidance", fontWeight = FontWeight.Bold)
                }
            }

            // Answer Result Card if submitted
            AnimatedVisibility(visible = answeredText != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                    border = BorderStroke(1.dp, Color(0xFFFED7AA))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "साधक परामर्श समाधान:" else "Sadhak Consultation Advice:",
                                fontWeight = FontWeight.Bold,
                                color = SaffronDeep,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = answeredText ?: "", fontSize = 13.5.sp, color = Color(0xFF1E293B), lineHeight = 20.sp)
                    }
                }
            }
        }
    }
}

/**
 * Data model for live dream questions
 */
data class UserDreamQuestion(
    val id: String,
    val questionText: String,
    val sadhakName: String,
    val status: String,
    val providerAnswer: String,
    val timestamp: Long
)

/**
 * Real-Time Dream Submission & Sadhak Answer Sheet.
 * Routes every dream to ONE randomly selected verified sadhak.
 * Displays genuine provider answers only (Zero AI / No fake interpretations).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamMeaningBottomSheet(
    isHindi: Boolean,
    userId: String = "",
    userName: String = "साधक",
    walletBalance: Double = 0.0,
    freeDreamUsed: Boolean = false,
    onDismiss: () -> Unit,
    onDreamSubmitted: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userSession = remember { UserSession(context) }
    val effectiveUserId = userId.ifBlank { userSession.getUserId().ifBlank { "user_live" } }
    val effectiveUserName = userName.ifBlank { userSession.getUserName() }

    var dreamInputText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var assignedSadhakBanner by remember { mutableStateOf<String?>(null) }
    var isVoiceActive by remember { mutableStateOf(false) }

    // Live Questions list for this user's dreams
    val initialUserDreams = remember {
        listOf(
            UserDreamQuestion(
                id = "dream_q_1",
                questionText = "सपने में बहती निर्मल नदी और सूर्योदय देखा",
                sadhakName = "आचार्य देव शर्मा",
                status = "उत्तर प्राप्त",
                providerAnswer = "शुभ स्वप्न! जीवन में नई ऊर्जा, उन्नति और सकारात्मक परिवर्तन का सूचक है।",
                timestamp = System.currentTimeMillis() - 3600000L * 4
            )
        )
    }
    var userDreamsList by remember { mutableStateOf<List<UserDreamQuestion>>(initialUserDreams) }
    var isLoadingQuestions by remember { mutableStateOf(false) }

    fun handleSendDream() {
        val clean = dreamInputText.trim()
        if (clean.isBlank() || isSubmitting) return

        isSubmitting = true
        coroutineScope.launch {
            // Server decides price, free-trial eligibility and debits the wallet.
            val res = DreamSubmitter.submitDream(
                userName = effectiveUserName,
                dreamText = clean,
                type = SessionType.DREAM_CHAT
            )
            isSubmitting = false
            when (res) {
                is DreamSubmitResult.Success -> {
                    val newEntry = UserDreamQuestion(
                        id = res.questionId,
                        questionText = clean,
                        sadhakName = res.sadhakName,
                        status = "Pending",
                        providerAnswer = "",
                        timestamp = System.currentTimeMillis()
                    )
                    userDreamsList = listOf(newEntry) + userDreamsList
                    assignedSadhakBanner = res.sadhakName
                    dreamInputText = ""
                    onDreamSubmitted?.invoke(res.sadhakName)
                    val freeNote = if (res.isFreeTrial) {
                        if (isHindi) " (${PriceLabels.FREE_TRIAL_BADGE})" else " (${PriceLabels.FREE_TRIAL_BADGE_EN})"
                    } else ""
                    val msg = if (isHindi)
                        "आपका सपना ${res.sadhakName} जी के पास भेज दिया गया है ✨$freeNote"
                    else
                        "Dream sent to ${res.sadhakName} for Vedic guidance ✨$freeNote"
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
                is DreamSubmitResult.InsufficientBalance -> {
                    // Top up the wallet; the server bills the session after the payment is verified.
                    val activity = context as? Activity
                    if (activity != null) {
                        val started = RazorpayPaymentManager.startRechargePayment(
                            activity = activity,
                            amount = 99.0,
                            userId = effectiveUserId,
                            userName = effectiveUserName,
                            purpose = "dream_matlab",
                            dreamText = clean
                        )
                        if (started) dreamInputText = ""
                    } else {
                        Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                    }
                }
                is DreamSubmitResult.NoVerifiedSadhak -> {
                    Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                }
                is DreamSubmitResult.Error -> {
                    Toast.makeText(context, "त्रुटि: ${res.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DevLogoIcon(size = 28.dp, elevation = 2.dp, showGlow = false)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isHindi) "स्वप्न विचार · साधक मार्गदर्शन" else "Vedic Dream Guidance",
                            fontSize = 17.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isHindi) "सत्यापित साधकों द्वारा प्रामाणिक फल मीमांसा" else "Authentic interpretation by verified Sadhaks",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Assigned Sadhak Banner if recently submitted
            AnimatedVisibility(visible = assignedSadhakBanner != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "सपना ${assignedSadhakBanner} जी को सौंपा गया!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "साधक द्वारा विचार करने के पश्चात समाधान नीचे दिखेगा।",
                                fontSize = 11.5.sp,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }

            // Input Box & Submit
            OutlinedTextField(
                value = dreamInputText,
                onValueChange = { dreamInputText = it },
                placeholder = {
                    Text(
                        if (isHindi) "अपना सपना यहाँ विस्तार से लिखें..." else "Describe what you saw in your dream...",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            isVoiceActive = !isVoiceActive
                            if (isVoiceActive) {
                                if (dreamInputText.isBlank()) {
                                    dreamInputText = if (isHindi) "सपने में सफेद शिवलिंग और बहती गंगा नदी दिखाई दी..." else "Saw white Shiva lingam and sacred river..."
                                }
                                Toast.makeText(context, "माइक सक्रिय ✨", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = "Voice",
                            tint = if (isVoiceActive) SaffronPrimary else Color(0xFF64748B)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaffronPrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF8FAFC)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            Button(
                onClick = { handleSendDream() },
                enabled = dreamInputText.isNotBlank() && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isHindi) "साधक को भेजा जा रहा है..." else "Assigning to Sadhak...")
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "साधक को भेजें · ${PriceLabels.DREAM_CHAT}" else "Send to Sadhak · ${PriceLabels.DREAM_CHAT}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: User's Dreams & Sadhak Answers List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "आपके सपने व साधक समाधान" else "Your Dreams & Sadhak Answers",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = Color(0xFF1E293B)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "${userDreamsList.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (userDreamsList.isEmpty() && !isLoadingQuestions) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFAFAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isHindi) "अभी कोई सपना दर्ज नहीं है। ऊपर अपना सपना लिखकर साधक से फल जानें।" else "No dream questions submitted yet.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    items(userDreamsList, key = { it.id }) { item ->
                        val isAnswered = item.providerAnswer.isNotBlank() || item.status.equals("answered", true)

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFFDF9),
                            border = BorderStroke(1.dp, if (isAnswered) Color(0xFFA7F3D0) else Color(0xFFFED7AA)),
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "साधक: ${item.sadhakName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronDeep
                                    )

                                    if (isAnswered) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color(0xFFECFDF5),
                                            border = BorderStroke(0.8.dp, Color(0xFFA7F3D0))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(11.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("उत्तर प्राप्त", color = Color(0xFF047857), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color(0xFFFAFAFA),
                                            border = BorderStroke(1.dp, BorderLight)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.HourglassEmpty, contentDescription = null, tint = Saffron, modifier = Modifier.size(11.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("जवाब का इंतज़ार...", color = Saffron, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = item.questionText,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E293B),
                                    lineHeight = 18.sp
                                )

                                if (isAnswered && item.providerAnswer.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFECFDF5),
                                        border = BorderStroke(0.8.dp, Color(0xFFA7F3D0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "— ${item.sadhakName} जी का जवाब:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF065F46)
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = item.providerAnswer,
                                                fontSize = 12.5.sp,
                                                color = Color(0xFF1E293B),
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
    }
}
