package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.SadhakItem
import com.example.ui.theme.*
import com.example.utils.FaqQuestionRepository
import com.example.utils.FaqSubmitResult
import com.example.utils.PriceLabels
import com.example.utils.PublicFaqQuestion
import com.example.utils.SadhakRepository
import com.example.utils.TopicFaqContent
import kotlinx.coroutines.launch

/**
 * Home topic card screen (परिवार / धन / नकारात्मकता / पितृदोष / स्वास्थ्य).
 * Shows ONLY that topic: 10 local FAQ (accordion) + approved anonymous user
 * questions (faq_public) + "आपका सवाल यहाँ नहीं है?" ask box.
 * No remedies, no astrology, no sadhak list on the main screen.
 *
 * After submit: bottom sheet -> optional "साधक चुनें" list -> existing chat flow
 * (via [onChatWithSadhak]) with the question pre-filled, not auto-sent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicFaqScreen(
    topicId: String,
    userId: String,
    onBackClick: () -> Unit,
    onRequestLogin: () -> Unit,
    onChatWithSadhak: (sadhak: SadhakItem, prefill: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val faq = remember(topicId) { TopicFaqContent.get(topicId) }
    val topicTitle = faq?.titleHi ?: TopicFaqContent.titleHi(topicId)

    var expandedKey by remember(topicId) { mutableStateOf<String?>("faq_0") }
    var publicQuestions by remember(topicId) { mutableStateOf<List<PublicFaqQuestion>>(emptyList()) }

    var questionText by remember(topicId) { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showSubmittedSheet by remember { mutableStateOf(false) }
    var submittedText by remember { mutableStateOf("") }
    var showSadhakPicker by remember { mutableStateOf(false) }

    // Approved anonymous questions for this topic (public collection only).
    DisposableEffect(topicId) {
        val reg = FaqQuestionRepository.observePublic(context, topicId) { publicQuestions = it }
        onDispose { reg.remove() }
    }

    if (showSadhakPicker) {
        TopicFaqSadhakPicker(
            questionText = submittedText,
            onBack = { showSadhakPicker = false },
            onChat = { sadhak -> onChatWithSadhak(sadhak, submittedText) }
        )
        return
    }

    fun submit() {
        val text = questionText.trim()
        if (text.isEmpty() || isSubmitting) return
        if (!FaqQuestionRepository.isSignedIn(userId)) {
            showLoginDialog = true
            return
        }
        coroutineScope.launch {
            isSubmitting = true
            when (FaqQuestionRepository.submitQuestion(context, topicId, text)) {
                is FaqSubmitResult.Success -> {
                    submittedText = text
                    questionText = ""
                    showSubmittedSheet = true
                }
                is FaqSubmitResult.NotSignedIn -> showLoginDialog = true
                is FaqSubmitResult.Error -> Toast.makeText(
                    context,
                    "सवाल नहीं भेजा जा सका। कृपया दोबारा कोशिश करें।",
                    Toast.LENGTH_SHORT
                ).show()
            }
            isSubmitting = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFAF7F2),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = topicTitle,
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Ink
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "वापस", tint = Ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAF7F2))
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Section header
            item {
                Column {
                    if (faq != null && faq.subtitleHi.isNotBlank()) {
                        Text(
                            text = faq.subtitleHi,
                            fontSize = 12.5.sp,
                            color = InkSoft
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Text(
                        text = "अक्सर पूछे जाने वाले सवाल",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Ink
                    )
                    Text(
                        text = "${faq?.items?.size ?: 0} सवाल · किसी पर टैप करें",
                        fontSize = 11.5.sp,
                        color = InkSoft
                    )
                }
            }

            // 10 local FAQ (verbatim content)
            itemsIndexed(faq?.items ?: emptyList()) { index, item ->
                val key = "faq_$index"
                TopicFaqAccordionItem(
                    question = item.question,
                    answer = item.answer,
                    expanded = expandedKey == key,
                    onToggle = { expandedKey = if (expandedKey == key) null else key }
                )
            }

            // Approved user questions (anonymous: no name / avatar / identifier)
            if (publicQuestions.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "लोगों के पूछे सवाल",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Ink
                    )
                    Text(
                        text = "जाँच के बाद जोड़े गए · पूछने वाले की पहचान नहीं दिखाई जाती",
                        fontSize = 11.5.sp,
                        color = InkSoft
                    )
                }
                items(publicQuestions, key = { it.id }) { pq ->
                    val key = "pub_${pq.id}"
                    TopicFaqAccordionItem(
                        question = pq.question,
                        answer = pq.answer,
                        expanded = expandedKey == key,
                        onToggle = { expandedKey = if (expandedKey == key) null else key }
                    )
                }
            }

            // Ask box
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SaffronSoftBg,
                    border = BorderStroke(1.dp, Saffron.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "आपका सवाल यहाँ नहीं है?",
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Ink
                        )
                        Text(
                            text = "$topicTitle से जुड़ा अपना सवाल लिखें या पेस्ट करें",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = questionText,
                            onValueChange = { questionText = it.take(FaqQuestionRepository.MAX_LENGTH) },
                            placeholder = {
                                Text(
                                    text = "नाम या फ़ोन नंबर न लिखें",
                                    fontSize = 13.sp,
                                    color = InkFaint
                                )
                            },
                            supportingText = {
                                Text(
                                    text = "${questionText.length}/${FaqQuestionRepository.MAX_LENGTH}",
                                    fontSize = 11.sp,
                                    color = InkSoft,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Saffron,
                                unfocusedBorderColor = EditorialLineStrong,
                                focusedContainerColor = PaperCard,
                                unfocusedContainerColor = PaperCard
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { submit() },
                            enabled = questionText.isNotBlank() && !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text(text = "सवाल भेजें", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "नाम या फ़ोन नंबर न लिखें · आपका सवाल बिना पहचान के दिखेगा",
                            fontSize = 11.sp,
                            color = InkSoft,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Short disclaimer
            item {
                Text(
                    text = "यह जानकारी केवल समझ के लिए है, निदान नहीं।",
                    fontSize = 11.sp,
                    color = InkSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }
        }
    }

    // Guest -> login prompt instead of submitting
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "लॉगिन ज़रूरी है",
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Ink
                )
            },
            text = {
                Text(
                    text = "सवाल भेजने के लिए कृपया लॉगिन करें। आपका नाम या नंबर सवाल के साथ कहीं नहीं दिखेगा।",
                    fontSize = 13.sp,
                    color = InkSoft,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLoginDialog = false
                        onRequestLogin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("लॉगिन करें", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) {
                    Text("अभी नहीं", color = InkSoft)
                }
            }
        )
    }

    // After submit
    if (showSubmittedSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSubmittedSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "आपका सवाल मिल गया",
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Ink,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "जाँच के बाद इसका जवाब $topicTitle FAQ में जोड़ा जाएगा।",
                    fontSize = 13.sp,
                    color = InkSoft,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "क्या आप इस बारे में साधक से पूछना चाहते हैं?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = Ink,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        showSubmittedSheet = false
                        showSadhakPicker = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("हाँ, साधक चुनें", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showSubmittedSheet = false },
                    border = BorderStroke(1.2.dp, Saffron),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("नहीं, बस सवाल भेजना था", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SaffronDeep)
                }
            }
        }
    }
}

@Composable
private fun TopicFaqAccordionItem(
    question: String,
    answer: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (expanded) Saffron.copy(alpha = 0.4f) else BorderLight),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggle() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(SaffronContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SaffronDeep)
                }
                Text(
                    text = question,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                    lineHeight = 19.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Saffron
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = answer,
                    fontSize = 13.sp,
                    color = InkSoft,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

/**
 * "साधक चुनें": approved + verified sadhaks from the sadhaks directory.
 * Offline sadhaks are shown with a disabled "अभी उपलब्ध नहीं" button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopicFaqSadhakPicker(
    questionText: String,
    onBack: () -> Unit,
    onChat: (SadhakItem) -> Unit
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    var sadhaks by remember { mutableStateOf<List<SadhakItem>?>(null) }
    DisposableEffect(Unit) {
        val reg = SadhakRepository.observeApprovedSadhaks(
            context = context,
            onUpdate = { sadhaks = it },
            onError = { sadhaks = sadhaks ?: emptyList() }
        )
        onDispose { reg.remove() }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFAF7F2),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "साधक चुनें",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Ink
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "वापस", tint = Ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAF7F2))
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (questionText.isNotBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SaffronSoftBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "आपका सवाल chat में पहले से भरा होगा:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDeep
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "“$questionText”",
                                fontSize = 13.sp,
                                color = Ink,
                                lineHeight = 19.sp,
                                maxLines = 4,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            val list = sadhaks
            when {
                list == null -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Terra)
                    }
                }
                list.isEmpty() -> item {
                    Text(
                        text = "अभी कोई verified साधक उपलब्ध नहीं है। कृपया थोड़ी देर बाद देखें।",
                        fontSize = 13.sp,
                        color = InkSoft,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
                else -> items(list, key = { it.id }) { sadhak ->
                    TopicFaqSadhakCard(sadhak = sadhak, onChat = { onChat(sadhak) })
                }
            }

            item {
                Text(
                    text = "सिर्फ़ approved और verified साधक दिखाए जाते हैं",
                    fontSize = 11.sp,
                    color = InkSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun TopicFaqSadhakCard(
    sadhak: SadhakItem,
    onChat: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderLight),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SaffronContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sadhak.initialHi.ifBlank { sadhak.nameHi.take(1) },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SaffronDeep
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(if (sadhak.isOnline) OnlineGreen else Color(0xFF94A3B8))
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sadhak.nameHi.ifBlank { sadhak.nameEn },
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Ink,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    val expertise = sadhak.titleHi.ifBlank { sadhak.titleEn }
                    if (expertise.isNotBlank()) {
                        Text(
                            text = expertise,
                            fontSize = 12.sp,
                            color = InkSoft,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (sadhak.rating.isBlank()) "नए" else "★ ${sadhak.rating} रेटिंग",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SaffronDeep
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    val sadhakRate = if (sadhak.chatPricePerMin > 0) "₹${sadhak.chatPricePerMin}/मि" else if (sadhak.callPricePerMin > 0) "₹${sadhak.callPricePerMin}/मि" else if (sadhak.sessionPrice30Min > 0) "₹${sadhak.sessionPrice30Min}" else ""
                    if (sadhakRate.isNotEmpty()) {
                        Text(
                            text = sadhakRate,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                    }
                    Text(
                        text = if (sadhak.isOnline) "ऑनलाइन" else "ऑफ़लाइन",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (sadhak.isOnline) OnlineGreen else InkSoft
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onChat,
                enabled = sadhak.isOnline,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Saffron,
                    disabledContainerColor = Color(0xFFF1F5F9),
                    disabledContentColor = Color(0xFF94A3B8)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text(
                    text = if (sadhak.isOnline) "Chat करें" else "अभी उपलब्ध नहीं",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
