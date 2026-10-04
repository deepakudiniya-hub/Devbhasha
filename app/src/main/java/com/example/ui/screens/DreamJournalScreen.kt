package com.example.ui.screens

import com.example.ui.theme.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.DreamJournalEntry
import com.example.ui.theme.TextDark
import com.example.utils.UserSession
import java.text.SimpleDateFormat
import java.util.*

private val SaffronOrange = Saffron
private val SoftGreyBorder = BorderLight
private val CardBackground = SurfaceWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamJournalScreen(
    onBackClick: () -> Unit,
    userId: String = "",
    userName: String = "साधक",
    onLogDream: ((String) -> Unit)? = null,
    onChatWithSadhak: () -> Unit = {},
    dreamHistory: List<DreamJournalEntry> = emptyList(),
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en"
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hi", "hindi") -> "hi"
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        else -> "en"
    }
    val context = LocalContext.current
    val userSession = remember { UserSession(context) }
    val effectiveUserId = userId.ifBlank { userSession.getUserId() }

    var dreamInput by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("🌿 शुभ (Shubh)") }
    var selectedTags by remember { mutableStateOf(setOf("#lucid", "#recurring")) }
    var activeCategoryFilterTag by remember { mutableStateOf<String?>(null) }
    var showCustomTagDialog by remember { mutableStateOf(false) }
    var customTagInput by remember { mutableStateOf("") }
    var isMicActive by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var selectedMeaningId by remember { mutableStateOf<String?>(null) }
    var dreamToDelete by remember { mutableStateOf<DreamJournalEntry?>(null) }

    val defaultSuggestedTags = listOf(
        "#lucid", "#recurring", "#spiritual", "#prophetic", "#nightmare", "#symbolic", "#peaceful", "#deities"
    )

    var savedDreams by remember { mutableStateOf(dreamHistory.ifEmpty {
        listOf(
            DreamJournalEntry(
                id = "dream_local_1",
                title = "पवित्र शिवलिंग एवं गंगाजल दर्शन",
                datePhase = "आज",
                description = "प्रातःकाल सपने में प्राचीन शिव मंदिर में शिवलिंग पर निर्मल गंगाजल और बेलपत्र अर्पित करते देखा।",
                tag = "🌿 शुभ (Shubh)",
                tags = listOf("#shiva", "#sacred", "#peace"),
                timestamp = System.currentTimeMillis() - 7200000L,
                meaning = "अत्यंत मंगलकारी स्वप्न। मानसिक शांति, रोगमुक्ति तथा रुके हुए कार्यों में सफलता का संकेत है।",
                answeredBy = "आचार्य देव शर्मा"
            )
        )
    }) }
    var isLoadingDreams by remember { mutableStateOf(false) }

    // Determine entries to display: live local saved dreams or passed in/fallback
    val activeDreams = when {
        savedDreams.isNotEmpty() -> savedDreams
        dreamHistory.isNotEmpty() -> dreamHistory
        else -> emptyList()
    }

    val allDistinctTags = remember(activeDreams) {
        val tagsSet = linkedSetOf<String>()
        activeDreams.forEach { dream ->
            tagsSet.addAll(dream.getAllTags())
        }
        tagsSet.toList()
    }

    val displayedDreams = remember(activeDreams, activeCategoryFilterTag) {
        if (activeCategoryFilterTag == null) {
            activeDreams
        } else {
            activeDreams.filter { dream ->
                dream.getAllTags().any { it.equals(activeCategoryFilterTag, ignoreCase = true) }
            }
        }
    }

    // Function to generate Vedic meaning based on dream text
    fun generateVedicMeaning(text: String): String {
        return when {
            text.contains("सांप", ignoreCase = true) || text.contains("नाग", ignoreCase = true) || text.contains("snake", ignoreCase = true) ->
                "सपने में नाग देवता के दर्शन कुण्डलिनी जागरण, आध्यात्मिक उन्नति और पूर्वजों के आशीर्वाद का अत्यंत शुभ संकेत है।"
            text.contains("शिव", ignoreCase = true) || text.contains("मंदिर", ignoreCase = true) || text.contains("temple", ignoreCase = true) ->
                "भगवान शिव व मंदिर का दृश्य जीवन से समस्त नकारात्मकताओं का नाश और मनोकामना पूर्ति का दिव्य संकेत है।"
            text.contains("नदी", ignoreCase = true) || text.contains("जल", ignoreCase = true) || text.contains("water", ignoreCase = true) ->
                "पावन नदी का प्रवाह मन की शुद्धि, मानसिक शांति और आने वाले समय में आर्थिक समृद्धि का द्योतक है।"
            text.contains("उड़ना", ignoreCase = true) || text.contains("fly", ignoreCase = true) || text.contains("आकाश", ignoreCase = true) ->
                "आकाश में उड़ान भरना बंधनों से मुक्ति, उच्च लक्ष्यों की प्राप्ति और आंतरिक आत्म-विश्वास में वृद्धि को दर्शाता है।"
            text.contains("गिरना", ignoreCase = true) || text.contains("डर", ignoreCase = true) || text.contains("fall", ignoreCase = true) ->
                "ऊंचाई से गिरना या भय का अनुभव जीवन में सावधानी बरतने और आत्म-संयम बनाए रखने का दैवीय संदेश है।"
            else ->
                "यह स्वप्न आपके अंतर्मन की चेतना का प्रकटीकरण है, जो भविष्य में सकारात्मक बदलाव और नवीन अवसरों के आगमन की सूचना देता है।"
        }
    }

    // Function to save dream locally
    fun saveDreamLocally() {
        if (dreamInput.isBlank()) return
        val text = dreamInput.trim()
        val meaning = generateVedicMeaning(text)
        val tagsToSave = (selectedTags.toList() + selectedTag).distinct()
        val newEntry = DreamJournalEntry(
            id = "dream_local_" + System.currentTimeMillis(),
            title = text.take(40),
            datePhase = "आज",
            description = text,
            tag = selectedTag,
            tags = tagsToSave,
            timestamp = System.currentTimeMillis(),
            meaning = meaning,
            answeredBy = "वैदिक स्वप्न मीमांसा"
        )
        savedDreams = listOf(newEntry) + savedDreams
        dreamInput = ""
        isMicActive = false
        onLogDream?.invoke(text)
        Toast.makeText(context, "सपना सुरक्षित हुआ · स्वप्न फल तैयार ✨", Toast.LENGTH_SHORT).show()
    }

    // Function to delete dream entry
    fun performDeleteDream(dream: DreamJournalEntry) {
        savedDreams = savedDreams.filter { it.id != dream.id }
        Toast.makeText(context, "सपना हटाया गया", Toast.LENGTH_SHORT).show()
    }

    // Delete confirmation dialog
    if (dreamToDelete != null) {
        AlertDialog(
            onDismissRequest = { dreamToDelete = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "सपना हटाएं? (Delete Entry)",
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "क्या आप इस सपने को अपनी स्वप्न डायरी से हटाना चाहते हैं?",
                    color = Neutral500,
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toDelete = dreamToDelete
                        if (toDelete != null) {
                            performDeleteDream(toDelete)
                        }
                        dreamToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRedDeep),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("हाँ, हटाएं", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { dreamToDelete = null }) {
                    Text("रद्द करें", color = Neutral500)
                }
            }
        )
    }

    // Add Custom Tag Dialog
    if (showCustomTagDialog) {
        AlertDialog(
            onDismissRequest = {
                showCustomTagDialog = false
                customTagInput = ""
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = if (currentLangCode == "hi") "नया टैग जोड़ें" else "Add Custom Tag",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextDark
                )
            },
            text = {
                Column {
                    Text(
                        text = if (currentLangCode == "hi") "टैग का नाम दर्ज करें (जैसे #lucid, #recurring, #prophetic):" else "Enter tag (e.g. #lucid, #recurring, #prophetic):",
                        color = Neutral500,
                        fontSize = 12.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customTagInput,
                        onValueChange = { customTagInput = it },
                        placeholder = { Text("#lucid", fontSize = 13.sp, color = Neutral400) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val raw = customTagInput.trim()
                        if (raw.isNotBlank()) {
                            val formatted = if (raw.startsWith("#")) raw else "#$raw"
                            selectedTags = selectedTags + formatted
                        }
                        showCustomTagDialog = false
                        customTagInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("जोड़ें", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCustomTagDialog = false
                    customTagInput = ""
                }) {
                    Text("रद्द करें", color = Neutral500)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Neutral50)
                    .border(1.dp, SoftGreyBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDark
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "स्वप्न डायरी · Dream Journal"
                        "hgl" -> "Swapna Diary · Dream Journal"
                        else -> "Dream Journal"
                    },
                    fontFamily = FontFamily.Default,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "सपनों का आध्यात्मिक रहस्य व फल जानें"
                        "hgl" -> "Sapnon ka spiritual rahasya aur phal jaanein"
                        else -> "Decode spiritual meanings and Vedic insights"
                    },
                    fontSize = 11.5.sp,
                    color = Neutral500
                )
            }

            if (isLoadingDreams) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = SaffronOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 2. Dream Input Section Card (Rounded, Soft Grey Border, Mic Icon, Saffron '+' Add Button)
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, SoftGreyBorder),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = SaffronOrange.copy(alpha = 0.12f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "🌙", fontSize = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = when (currentLangCode) {
                                        "hi" -> "आज क्या सपना देखा?"
                                        "hgl" -> "Aaj kya sapna dekha?"
                                        else -> "What did you dream today?"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    fontSize = 16.sp
                                )
                            }

                            if (isMicActive) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SaffronOrange.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = when (currentLangCode) {
                                            "hi" -> "🎙️ सुन रहा है..."
                                            "hgl" -> "🎙️ Sun raha hai..."
                                            else -> "🎙️ Listening..."
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronOrange,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Outlined Text Field: Rounded Corners (16dp), Soft Grey Borders, Mic Icon inside
                        OutlinedTextField(
                            value = dreamInput,
                            onValueChange = { dreamInput = it },
                            placeholder = {
                                Text(
                                    text = when (currentLangCode) {
                                        "hi" -> "आपने सपने में क्या देखा? यहाँ लिखें या माइक दबाएं..."
                                        "hgl" -> "Aapne sapne mein kya dekha? Yahan likhein ya mic dabayein..."
                                        else -> "What did you see in your dream? Type here or tap mic..."
                                    },
                                    fontSize = 13.5.sp,
                                    color = Neutral400
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        isMicActive = !isMicActive
                                        if (isMicActive) {
                                            if (dreamInput.isBlank()) {
                                                dreamInput = if (currentLangCode == "hi") {
                                                    "सपने में पावन गंगा तट और सुनहरी आरती के दर्शन हुए..."
                                                } else if (currentLangCode == "hgl") {
                                                    "Sapne mein paawan Ganga tat aur sunhari aarti ke darshan hue..."
                                                } else {
                                                    "I saw holy river Ganges and golden evening aarti in my dream..."
                                                }
                                            }
                                            val micMsg = when (currentLangCode) {
                                                "hi" -> "माइक चालू: सपना रिकॉर्ड हुआ ✨"
                                                "hgl" -> "Mic on: Sapna record hua ✨"
                                                else -> "Mic active: Dream recorded ✨"
                                            }
                                            Toast.makeText(context, micMsg, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Mic,
                                        contentDescription = "Voice Input Mic",
                                        tint = if (isMicActive) SaffronOrange else Neutral500,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SaffronOrange,
                                unfocusedBorderColor = SoftGreyBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Neutral50
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Mood / Type Selector Chips
                        val moodTags = when (currentLangCode) {
                            "hi" -> listOf("🌿 शुभ (Shubh)", "⚡ डरावना (Scary)", "🕊️ मुक्ति (Freedom)", "✨ रहस्यमय (Mystic)")
                            "hgl" -> listOf("🌿 Shubh (Auspicious)", "⚡ Scary (Darawna)", "🕊️ Mukti (Freedom)", "✨ Rahasyamay (Mystic)")
                            else -> listOf("🌿 Auspicious", "⚡ Scary", "🕊️ Freedom", "✨ Mystical")
                        }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(moodTags) { tag ->
                                val isSelected = selectedTag == tag
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) SaffronOrange.copy(alpha = 0.12f) else Neutral100,
                                    border = if (isSelected) BorderStroke(1.dp, SaffronOrange) else null,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { selectedTag = tag }
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) SaffronOrange else Neutral600,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Hashtags Multi-Selector (#lucid, #recurring, etc.)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "टैग्स जोड़ें (Tags):"
                                    "hgl" -> "Tags chunein (Category):"
                                    else -> "Categorize with Tags:"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Neutral500
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(defaultSuggestedTags + selectedTags.filter { !defaultSuggestedTags.contains(it) }) { tag ->
                                val isTagSelected = selectedTags.contains(tag)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isTagSelected) AccentVioletSoft else Neutral50,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isTagSelected) Color(0xFF9333EA) else Neutral200
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedTags = if (isTagSelected) {
                                                selectedTags - tag
                                            } else {
                                                selectedTags + tag
                                            }
                                        }
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        fontWeight = if (isTagSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isTagSelected) AccentViolet else Neutral500,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            item {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AccentBlueSoft,
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showCustomTagDialog = true }
                                ) {
                                    Text(
                                        text = "+ कस्टम टैग",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AccentBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Saffron Add Button with '+' Icon
                        Button(
                            onClick = { saveDreamLocally() },
                            enabled = dreamInput.isNotBlank() && !isSaving,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SaffronOrange,
                                disabledContainerColor = SaffronOrange.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (currentLangCode) {
                                        "hi" -> "सपना सुरक्षित हो रहा है..."
                                        "hgl" -> "Sapna save ho raha hai..."
                                        else -> "Saving dream to journal..."
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add Dream",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (currentLangCode) {
                                        "hi" -> "सपना दर्ज करें (Save to Journal)"
                                        "hgl" -> "Sapna darj karein (Save to Journal)"
                                        else -> "Save to Journal"
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 3. Section Header: Purane Sapne + Category Filter Row
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when (currentLangCode) {
                                "hi" -> "पुराने सपने (Saved Dreams)"
                                "hgl" -> "Purane Sapne (Saved Dreams)"
                                else -> "Saved Dreams"
                            },
                            fontFamily = FontFamily.Default,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Neutral100
                        ) {
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "${activeDreams.size} सपने"
                                    "hgl" -> "${activeDreams.size} sapne"
                                    else -> "${activeDreams.size} dreams"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Neutral600,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Dynamic Tag Categorization Filter Row
                    if (allDistinctTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                val isAllSelected = activeCategoryFilterTag == null
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isAllSelected) SaffronOrange else Neutral100,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { activeCategoryFilterTag = null }
                                ) {
                                    Text(
                                        text = if (currentLangCode == "hi") "सभी (All)" else "All",
                                        color = if (isAllSelected) Color.White else Neutral600,
                                        fontSize = 11.sp,
                                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            items(allDistinctTags) { filterTag ->
                                val isTagSelected = activeCategoryFilterTag == filterTag
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isTagSelected) AccentViolet else Color(0xFFFAF5FF),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isTagSelected) AccentViolet else Color(0xFFE9D5FF)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            activeCategoryFilterTag = if (isTagSelected) null else filterTag
                                        }
                                ) {
                                    Text(
                                        text = filterTag,
                                        color = if (isTagSelected) Color.White else AccentViolet,
                                        fontSize = 11.sp,
                                        fontWeight = if (isTagSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (activeDreams.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (currentLangCode) {
                                "hi" -> "💡 हटाने के लिए किसी भी सपने को बाएँ या दाएँ स्वाइप करें (Swipe to delete)"
                                "hgl" -> "💡 Hatane ke liye kisi bhi sapne ko swipe karein (Swipe to delete)"
                                else -> "💡 Swipe any dream left or right to delete"
                            },
                            fontSize = 11.sp,
                            color = Neutral400
                        )
                    }
                }
            }

            // 4. Saved Dreams List with SwipeToDismissBox or Beautiful Empty State
            if (activeDreams.isEmpty() && !isLoadingDreams) {
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, SoftGreyBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 36.dp, horizontal = 20.dp)
                        ) {
                            // Spiritual Moon, Stars & Cloud Illustration
                            Box(
                                modifier = Modifier.size(86.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SaffronOrange.copy(alpha = 0.12f),
                                    modifier = Modifier.size(76.dp)
                                ) {}
                                Text(text = "🌙", fontSize = 40.sp)
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "अभी कोई पुराना सपना नहीं है"
                                    "hgl" -> "Purane sapne abhi nahi hain"
                                    else -> "No saved dreams yet"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "रात में देखा गया कोई भी सपना ऊपर दर्ज करें और उसका आध्यात्मिक अर्थ जानें। यह हमेशा के लिए सुरक्षित रहेगा।"
                                    "hgl" -> "Raat mein dekha gaya koi bhi sapna upar darj karein aur uska spiritual arth jaanein."
                                    else -> "Log your dreams above to reveal their spiritual Vedic meaning and reflections."
                                },
                                fontSize = 12.5.sp,
                                color = Neutral500,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                items(displayedDreams, key = { it.id }) { dream ->
                    SwipeableDreamItemCard(
                        dream = dream,
                        isMeaningExpanded = selectedMeaningId == dream.id,
                        language = currentLangCode,
                        onToggleMeaning = {
                            selectedMeaningId = if (selectedMeaningId == dream.id) null else dream.id
                            if (selectedMeaningId == dream.id) {
                                val phalMsg = when (currentLangCode) {
                                    "hi" -> "स्वप्न फल लोड हो गया ✨"
                                    "hgl" -> "Swapna phal load ho gaya ✨"
                                    else -> "Vedic meaning unlocked ✨"
                                }
                                Toast.makeText(context, phalMsg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDeleteClick = { dreamToDelete = dream },
                        onDismissed = { performDeleteDream(dream) },
                        fallbackMeaningGenerator = { text -> generateVedicMeaning(text) }
                    )
                }
            }
        }
    }
}

/**
 * Swipe-to-delete enabled Dream Item Card with smooth animated background and action states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableDreamItemCard(
    dream: DreamJournalEntry,
    isMeaningExpanded: Boolean,
    onToggleMeaning: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismissed: () -> Unit,
    fallbackMeaningGenerator: (String) -> String,
    language: String = "en",
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hi", "hindi") -> "hi"
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        else -> "en"
    }
    val context = LocalContext.current
    val currentDismissed by rememberUpdatedState(onDismissed)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                currentDismissed()
                true
            } else {
                false
            }
        }
    )

    val moodTag = when {
        dream.tag.isNotBlank() -> dream.tag
        dream.description.contains("सांप", ignoreCase = true) || dream.description.contains("मंदिर", ignoreCase = true) || dream.description.contains("शिव", ignoreCase = true) -> "🌿 शुभ (Shubh)"
        dream.description.contains("डर", ignoreCase = true) || dream.description.contains("गिरना", ignoreCase = true) -> "⚡ डरावना (Scary)"
        else -> "✨ रहस्यमय (Mystic)"
    }

    val moodColor = when {
        moodTag.contains("शुभ") || moodTag.contains("Shubh") || moodTag.contains("Auspicious") -> GreenBase
        moodTag.contains("Scary") || moodTag.contains("डरावना") || moodTag.contains("Darawna") -> DangerRedDeep
        else -> SaffronOrange
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.CenterEnd
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DangerRed)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> "हटाएं (Delete)"
                            "hgl" -> "Hatayein (Delete)"
                            else -> "Delete"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, SoftGreyBorder),
            shadowElevation = 1.5.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Date/Day, Time badge, Mood Tag, and Delete button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "Date",
                            tint = SaffronOrange,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (dream.timestamp > 0L) {
                                SimpleDateFormat("d MMM yyyy (EEE)", Locale.getDefault()).format(Date(dream.timestamp))
                            } else {
                                dream.datePhase.ifBlank { if (currentLangCode == "hi") "आज" else if (currentLangCode == "hgl") "Aaj" else "Today" }
                            },
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        if (dream.timestamp > 0L) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Neutral100
                            ) {
                                Text(
                                    text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(dream.timestamp)),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Neutral500,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = moodColor.copy(alpha = 0.12f),
                            border = BorderStroke(0.5.dp, moodColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = moodTag,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = moodColor,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Delete",
                                tint = Neutral400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = dream.description,
                    fontSize = 14.5.sp,
                    color = TextDark,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal
                )

                // Display Category Tags Chips (#lucid, #recurring, etc.)
                val entryTags = dream.getAllTags()
                if (entryTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        entryTags.take(4).forEach { t ->
                            val isHashtag = t.startsWith("#")
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isHashtag) AccentVioletSoft else SaffronOrange.copy(alpha = 0.08f),
                                border = BorderStroke(0.8.dp, if (isHashtag) Color(0xFFDDD6FE) else SaffronOrange.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = t,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isHashtag) AccentViolet else SaffronOrange,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Swapna Phal (Find Meaning) Button or Expanded Meaning Card
                if (isMeaningExpanded || !dream.meaning.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SaffronOrange.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, SaffronOrange.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = "Meaning",
                                    tint = SaffronOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (currentLangCode) {
                                        "hi" -> "स्वप्न फल (वैदिक मीमांसा)"
                                        "hgl" -> "Swapna Phal (Vedic Mimansa)"
                                        else -> "Dream Meaning (Vedic Insight)"
                                    },
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronOrange
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "\u201C${dream.meaning ?: fallbackMeaningGenerator(dream.description)}\u201D",
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                color = TextDark,
                                lineHeight = 19.sp
                            )
                            if (!dream.answeredBy.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "— ${dream.answeredBy}",
                                    fontSize = 10.5.sp,
                                    color = Neutral500,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }

                // Bottom Actions: Copy, Share, and Swapna Phal Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Copy Button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Dream", "${dream.title}\n${dream.description}"))
                                    Toast.makeText(context, "सपना कॉपी किया गया ✓", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy",
                                tint = Neutral500,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "कॉपी",
                                color = Neutral500,
                                fontSize = 11.5.sp
                            )
                        }

                        // Share Button (Export via Intent)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    shareDreamEntry(context, dream)
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = SaffronOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "शेयर"
                                    "hgl" -> "Share"
                                    else -> "Share"
                                },
                                color = SaffronOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    if (!isMeaningExpanded && dream.meaning.isNullOrBlank()) {
                        Button(
                            onClick = onToggleMeaning,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SaffronOrange.copy(alpha = 0.12f),
                                contentColor = SaffronOrange
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Swapna Phal",
                                tint = SaffronOrange,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "स्वप्न फल"
                                    "hgl" -> "Swapna Phal"
                                    else -> "Meaning"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronOrange
                            )
                        }
                    }
                }
            }
        }
    }
}
