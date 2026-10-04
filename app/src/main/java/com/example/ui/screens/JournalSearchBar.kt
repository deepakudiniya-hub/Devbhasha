package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.DreamJournalEntry
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Filter Presets for Journal Search
 */
enum class JournalFilterType(val titleHi: String, val titleEn: String, val titleHgl: String, val icon: String) {
    ALL("सभी", "All", "Sabhi", "✨"),
    TODAY("आज", "Today", "Aaj", "📅"),
    YESTERDAY("कल", "Yesterday", "Kal", "📅"),
    THIS_WEEK("इस सप्ताह", "This Week", "Iss Hafte", "🗓️"),
    SHUBH("शुभ स्वप्न", "Auspicious", "Shubh Sapne", "🌿"),
    ALERT("चेतावनी", "Warnings", "Chetavni", "⚡"),
    ANSWERED("साधक उत्तर", "Answered", "Sadhak Answer", "👤")
}

/**
 * Core search and filtering engine for journal entries.
 * Evaluates keywords and multiple date formats (relative, ISO, formatted strings, timestamps).
 */
object JournalSearchEngine {

    fun filterEntries(
        entries: List<DreamJournalEntry>,
        query: String,
        filterType: JournalFilterType = JournalFilterType.ALL,
        customDateMillis: Long? = null
    ): List<DreamJournalEntry> {
        val cleanQuery = query.trim().lowercase()

        val calendarNow = Calendar.getInstance()
        val currentYear = calendarNow.get(Calendar.YEAR)
        val currentDayOfYear = calendarNow.get(Calendar.DAY_OF_YEAR)

        return entries.filter { entry ->
            // 1. Preset filter check
            val matchesFilterType = when (filterType) {
                JournalFilterType.ALL -> true
                JournalFilterType.TODAY -> isSameCalendarDay(entry.timestamp, System.currentTimeMillis()) || entry.datePhase.contains("आज", ignoreCase = true) || entry.datePhase.contains("today", ignoreCase = true)
                JournalFilterType.YESTERDAY -> {
                    val calEntry = Calendar.getInstance().apply { timeInMillis = entry.timestamp }
                    (calEntry.get(Calendar.YEAR) == currentYear && calEntry.get(Calendar.DAY_OF_YEAR) == currentDayOfYear - 1) ||
                            entry.datePhase.contains("कल", ignoreCase = true) || entry.datePhase.contains("yesterday", ignoreCase = true)
                }
                JournalFilterType.THIS_WEEK -> {
                    val diff = System.currentTimeMillis() - entry.timestamp
                    diff in 0..(7L * 24 * 60 * 60 * 1000)
                }
                JournalFilterType.SHUBH -> entry.tag.contains("शुभ", ignoreCase = true) || entry.description.contains("शुभ", ignoreCase = true)
                JournalFilterType.ALERT -> entry.tag.contains("चेतावनी", ignoreCase = true) || entry.tag.contains("अशुभ", ignoreCase = true) || entry.tag.contains("alert", ignoreCase = true)
                JournalFilterType.ANSWERED -> !entry.answeredBy.isNullOrBlank() || !entry.meaning.isNullOrBlank()
            }

            if (!matchesFilterType) return@filter false

            // 2. Custom selected date check
            if (customDateMillis != null && !isSameCalendarDay(entry.timestamp, customDateMillis)) {
                return@filter false
            }

            // 3. Keyword / Date query search
            if (cleanQuery.isBlank()) {
                return@filter true
            }

            // Match in text content
            val inTitle = entry.title.lowercase().contains(cleanQuery)
            val inDesc = entry.description.lowercase().contains(cleanQuery)
            val inTag = entry.tag.lowercase().contains(cleanQuery)
            val inTags = entry.tags.any { it.lowercase().contains(cleanQuery) }
            val inMeaning = (entry.meaning ?: "").lowercase().contains(cleanQuery)
            val inAnsweredBy = (entry.answeredBy ?: "").lowercase().contains(cleanQuery)
            val inDatePhase = entry.datePhase.lowercase().contains(cleanQuery)

            // Date-based keyword checks
            val inFormattedDate = if (entry.timestamp > 0) {
                val fullDate = SimpleDateFormat("dd MMMM yyyy EEEE dd/MM/yyyy d MMM d-MM-yyyy", Locale.getDefault()).format(Date(entry.timestamp)).lowercase()
                val hindiDate = SimpleDateFormat("dd MMMM yyyy EEEE", Locale("hi", "IN")).format(Date(entry.timestamp)).lowercase()
                fullDate.contains(cleanQuery) || hindiDate.contains(cleanQuery)
            } else false

            // Natural language date terms in query
            val naturalDateMatch = when {
                cleanQuery in listOf("आज", "today", "aaj") -> isSameCalendarDay(entry.timestamp, System.currentTimeMillis())
                cleanQuery in listOf("कल", "yesterday", "kal") -> {
                    val calEntry = Calendar.getInstance().apply { timeInMillis = entry.timestamp }
                    calEntry.get(Calendar.YEAR) == currentYear && calEntry.get(Calendar.DAY_OF_YEAR) == currentDayOfYear - 1
                }
                cleanQuery in listOf("इस सप्ताह", "this week", "week") -> {
                    val diff = System.currentTimeMillis() - entry.timestamp
                    diff in 0..(7L * 24 * 60 * 60 * 1000)
                }
                else -> false
            }

            inTitle || inDesc || inTag || inMeaning || inAnsweredBy || inDatePhase || inFormattedDate || naturalDateMatch
        }
    }

    private fun isSameCalendarDay(ts1: Long, ts2: Long): Boolean {
        if (ts1 <= 0L || ts2 <= 0L) return false
        val cal1 = Calendar.getInstance().apply { timeInMillis = ts1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = ts2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}

/**
 * Top Journal Search Bar for the Home Screen.
 * Includes text search input, clear button, custom date picker trigger, and quick filter chips.
 */
@Composable
fun HomeJournalSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    activeFilter: JournalFilterType,
    onFilterSelect: (JournalFilterType) -> Unit,
    customDateMillis: Long?,
    onCustomDateSelect: (Long?) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onOpenJournalClick: () -> Unit,
    isHindi: Boolean,
    language: String,
    modifier: Modifier = Modifier
) {
    var showFilters by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    // Kept short so it fits the single-line search pill without ellipsis. The
    // previous long string ("...शब्द या तारीख (उदा. शिव, 28 Sep, कल)...") was
    // clipped mid-word on narrow screens.
    val placeholderText = when (currentLangCode) {
        "hi" -> "डायरी में खोजें…"
        "hgl" -> "Journal mein khojein…"
        else -> "Search journal…"
    }

    // Function to launch Android DatePickerDialog
    fun openDatePicker() {
        val calendar = Calendar.getInstance()
        if (customDateMillis != null) {
            calendar.timeInMillis = customDateMillis
        }
        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onCustomDateSelect(selectedCal.timeInMillis)
                val dateStr = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(selectedCal.time)
                Toast.makeText(context, "तारीख फ़िल्टर: $dateStr", Toast.LENGTH_SHORT).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Main Search Bar Pill (#FAFAFA fill, rounded-xl, black text, 1px #EFEFEF border)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFFAFAFA),
            border = BorderStroke(
                width = 1.dp,
                color = if (query.isNotBlank() || customDateMillis != null || activeFilter != JournalFilterType.ALL) Color(0xFF000000) else Color(0xFFEFEFEF)
            ),
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Search Icon
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF000000),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Text Field Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty() && customDateMillis == null) {
                        Text(
                            text = placeholderText,
                            color = InkFaint,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Ink,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Default
                        ),
                        cursorBrush = SolidColor(Terra),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            onSearchSubmit(query)
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Active Custom Date Badge (if any)
                if (customDateMillis != null) {
                    val dateLabel = SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(customDateMillis))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Terra,
                        modifier = Modifier.clickable { onCustomDateSelect(null) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "📅 $dateLabel",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear Date",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Clear Query Button ('✕')
                if (query.isNotEmpty()) {
                    // 48dp touch target (was 28dp — below the 48dp minimum).
                    IconButton(
                        onClick = {
                            onQueryChange("")
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear",
                            tint = InkSoft,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Filter Toggle Icon Button
                // 48dp touch target; the visual pill stays 32dp inside it.
                IconButton(
                    onClick = { showFilters = !showFilters },
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (showFilters || activeFilter != JournalFilterType.ALL) Terra.copy(alpha = 0.15f) else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Toggle Filters",
                            tint = if (showFilters || activeFilter != JournalFilterType.ALL) Terra else InkSoft,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Date Picker Calendar Button
                // 48dp touch target; the visual pill stays 32dp inside it.
                IconButton(
                    onClick = { openDatePicker() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (customDateMillis != null) Terra.copy(alpha = 0.15f) else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Pick Date",
                            tint = if (customDateMillis != null) Terra else InkSoft,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Voice / Journal Quick Shortcut — 48dp touch target (was 32dp);
                // the visual circle stays 32dp inside it.
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onOpenJournalClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Terra,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.AutoStories,
                                contentDescription = "Open Journal",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = showFilters || activeFilter != JournalFilterType.ALL) {
            Column {
                Spacer(modifier = Modifier.height(6.dp))
                // Horizontal Quick Filter Chips Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    JournalFilterType.values().forEach { filter ->
                        val isSelected = (activeFilter == filter && customDateMillis == null)
                        val label = when (currentLangCode) {
                            "hi" -> filter.titleHi
                            "hgl" -> filter.titleHgl
                            else -> filter.titleEn
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (isSelected) Color(0xFF000000) else Color.White,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF000000) else Color(0xFFEFEFEF)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { onFilterSelect(filter) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = filter.icon,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else Color(0xFF737373),
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Filtered Journal Search Results Section displayed directly on the Home Screen.
 */
@Composable
fun HomeJournalSearchResultsSection(
    searchResults: List<DreamJournalEntry>,
    searchQuery: String,
    activeFilter: JournalFilterType,
    customDateMillis: Long?,
    onClearAllFilters: () -> Unit,
    onEntryClick: (DreamJournalEntry) -> Unit,
    onAskSadhakClick: (DreamJournalEntry) -> Unit,
    onAddNewDreamClick: () -> Unit,
    isHindi: Boolean,
    language: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    val isFilterActive = searchQuery.isNotBlank() || customDateMillis != null || activeFilter != JournalFilterType.ALL

    if (!isFilterActive) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Summary Results Header Bar
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFFFF9F2),
            border = BorderStroke(1.dp, Color(0xFFFFDFC4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "📖",
                        fontSize = 15.sp
                    )
                    Column {
                        val countText = when (currentLangCode) {
                            "hi" -> "${searchResults.size} प्रविष्टियाँ मिलीं"
                            "hgl" -> "${searchResults.size} entries mili"
                            else -> "${searchResults.size} entries found"
                        }
                        Text(
                            text = countText,
                            color = Ink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        val subText = when {
                            searchQuery.isNotBlank() -> "\"$searchQuery\""
                            customDateMillis != null -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(customDateMillis))
                            else -> activeFilter.titleHi
                        }
                        Text(
                            text = "फ़िल्टर: $subText",
                            color = InkSoft,
                            fontSize = 11.sp
                        )
                    }
                }

                // Clear Filter Button
                TextButton(
                    onClick = onClearAllFilters,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> "फ़िल्टर हटाएं ✕"
                            "hgl" -> "Clear karein ✕"
                            else -> "Clear ✕"
                        },
                        color = Terra,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results List or Empty State
        if (searchResults.isEmpty()) {
            // Empty Search State
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFEFE6D8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🔍", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> "कोई डायरी प्रविष्टि नहीं मिली"
                            "hgl" -> "Koi journal entry nahi mili"
                            else -> "No matching journal entries"
                        },
                        color = Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> "कृपया अन्य शब्द, तारीख खोजें या नया सपना दर्ज करें।"
                            "hgl" -> "Doosra keyword ya date search karein ya new dream add karein."
                            else -> "Try searching another keyword, date, or log a new entry."
                        },
                        color = InkSoft,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onAddNewDreamClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Terra),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLangCode) {
                                "hi" -> "नया सपना दर्ज करें ✍️"
                                "hgl" -> "Naya Sapna Add Karein ✍️"
                                else -> "Log New Entry ✍️"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        } else {
            // Results Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                searchResults.forEach { entry ->
                    JournalSearchResultCard(
                        entry = entry,
                        searchHighlightQuery = searchQuery,
                        onClick = { onEntryClick(entry) },
                        onAskSadhakClick = { onAskSadhakClick(entry) },
                        isHindi = isHindi,
                        language = language
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

/**
 * Single Journal Entry Result Card with Highlighted Keywords and Interactive Actions
 */
@Composable
fun JournalSearchResultCard(
    entry: DreamJournalEntry,
    searchHighlightQuery: String,
    onClick: () -> Unit,
    onAskSadhakClick: () -> Unit,
    isHindi: Boolean,
    language: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    val formattedDate = if (entry.timestamp > 0) {
        SimpleDateFormat("d MMM yyyy • hh:mm a", Locale.getDefault()).format(Date(entry.timestamp))
    } else {
        entry.datePhase.ifBlank { "आज" }
    }

    val hasAnswer = !entry.answeredBy.isNullOrBlank() || !entry.meaning.isNullOrBlank()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEAE2D5)),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Date Pill & Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Date Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Date",
                        tint = Terra,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = formattedDate,
                        color = InkSoft,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Tag Pills / Tags list
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val allTags = entry.getAllTags()
                    if (allTags.isNotEmpty()) {
                        allTags.take(3).forEach { t ->
                            val isAlert = t.contains("चेतावनी") || t.contains("Scary")
                            val isLucidOrSpecial = t.startsWith("#")
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    isAlert -> Color(0xFFFFF1F0)
                                    isLucidOrSpecial -> Color(0xFFF3E8FF)
                                    else -> Color(0xFFF2F7EC)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isAlert -> Color(0xFFFFCCC7)
                                        isLucidOrSpecial -> Color(0xFFDDD6FE)
                                        else -> Color(0xFFD6E4C4)
                                    }
                                )
                            ) {
                                Text(
                                    text = t,
                                    color = when {
                                        isAlert -> Color(0xFFCF1322)
                                        isLucidOrSpecial -> Color(0xFF7C3AED)
                                        else -> Color(0xFF389E0D)
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF2F7EC),
                            border = BorderStroke(1.dp, Color(0xFFD6E4C4))
                        ) {
                            Text(
                                text = entry.tag,
                                color = Color(0xFF389E0D),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title with highlight
            Text(
                text = highlightSearchQuery(entry.title, searchHighlightQuery),
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                lineHeight = 19.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description / dream narrative snippet
            Text(
                text = highlightSearchQuery(entry.description, searchHighlightQuery),
                color = InkSoft,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Sadhak Verified Meaning if present
            if (hasAnswer) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF6F8F3),
                    border = BorderStroke(1.dp, Color(0xFFE0EAD8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "✨", fontSize = 12.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "साधक परामर्श / वैदिक फल:",
                                color = Color(0xFF2B5B2E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                            val answerSnippet = entry.meaning ?: "परामर्श उपलब्ध है"
                            Text(
                                text = answerSnippet,
                                color = Ink,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Copy & Share Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Copy Text
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Dream", "${entry.title}\n${entry.description}"))
                                Toast.makeText(context, "सपना कॉपी किया गया ✓", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy",
                            tint = InkSoft,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "कॉपी",
                            color = InkSoft,
                            fontSize = 11.sp
                        )
                    }

                    // Share Button (Export to other apps via Intent)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                shareDreamEntry(context, entry)
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = Terra,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = when (currentLangCode) {
                                "hi" -> "शेयर"
                                "hgl" -> "Share"
                                else -> "Share"
                            },
                            color = Terra,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Ask Sadhak Button or View Full
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldStampSoft,
                        border = BorderStroke(1.dp, Terra.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onAskSadhakClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Chat,
                                contentDescription = "Ask",
                                tint = Terra,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "साधक से फल जानें"
                                    "hgl" -> "Sadhak se poochein"
                                    else -> "Ask Sadhak"
                                },
                                color = Terra,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Terra,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onClick() }
                    ) {
                        Text(
                            text = "विवरण →",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shares a dream journal entry with formatted text via Android Intent
 */
fun shareDreamEntry(context: Context, entry: DreamJournalEntry) {
    val dateText = if (entry.timestamp > 0) {
        SimpleDateFormat("d MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(entry.timestamp))
    } else {
        entry.datePhase.ifBlank { "आज" }
    }

    val shareBody = buildString {
        appendLine("🌙 देवभाषा स्वप्न डायरी · Dream Journal Entry")
        val allTags = entry.getAllTags()
        val tagsStr = if (allTags.isNotEmpty()) allTags.joinToString(" ") else entry.tag
        appendLine("📅 $dateText | $tagsStr")
        appendLine()
        appendLine("✨ ${entry.title}")
        appendLine(entry.description)
        if (!entry.meaning.isNullOrBlank()) {
            appendLine()
            appendLine("📜 वैदिक फल / परामर्श: ${entry.meaning}")
        }
        if (!entry.answeredBy.isNullOrBlank()) {
            appendLine("— मार्गदर्शन: ${entry.answeredBy}")
        }
        if (entry.tags.isNotEmpty()) {
            appendLine()
            appendLine("🏷️ टैग्स: ${entry.tags.joinToString(" ")}")
        }
        appendLine()
        append("— देवभाषा (Devbhasha) ऐप से साझा किया गया")
    }

    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_SUBJECT, entry.title)
        putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "सपना साझा करें (Share Dream Entry)"))
}

/**
 * Highlights matches of the search query inside text using SpanStyle
 */
fun highlightSearchQuery(text: String, query: String): androidx.compose.ui.text.AnnotatedString {
    if (query.isBlank()) {
        return buildAnnotatedString { append(text) }
    }

    val lowerText = text.lowercase()
    val lowerQuery = query.trim().lowercase()

    return buildAnnotatedString {
        var startIndex = 0
        while (startIndex < text.length) {
            val index = lowerText.indexOf(lowerQuery, startIndex)
            if (index == -1) {
                append(text.substring(startIndex))
                break
            }
            if (index > startIndex) {
                append(text.substring(startIndex, index))
            }
            withStyle(
                SpanStyle(
                    background = Color(0xFFFFE58F),
                    color = Color(0xFF593800),
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text.substring(index, index + lowerQuery.length))
            }
            startIndex = index + lowerQuery.length
        }
    }
}

/**
 * Detailed Dream Journal Entry View Dialog
 */
@Composable
fun DreamEntryDetailDialog(
    entry: DreamJournalEntry,
    onDismiss: () -> Unit,
    onAskSadhak: () -> Unit,
    isHindi: Boolean,
    language: String
) {
    val context = LocalContext.current
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    val formattedDate = if (entry.timestamp > 0) {
        SimpleDateFormat("d MMMM yyyy (EEEE) • hh:mm a", Locale.getDefault()).format(Date(entry.timestamp))
    } else {
        entry.datePhase.ifBlank { "आज" }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onAskSadhak()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Terra),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = "Ask",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "साधक से फल जानें (₹99)"
                        "hgl" -> "Sadhak se poochein (₹99)"
                        else -> "Ask Sadhak (₹99)"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { shareDreamEntry(context, entry) }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Terra,
                        modifier = Modifier.size(18.dp)
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text("बंद करें (Close)", color = InkSoft)
                }
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🌙", fontSize = 20.sp)
                Text(
                    text = entry.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Ink
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Date & Tag Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 $formattedDate",
                        color = InkSoft,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (entry.tag.contains("चेतावनी")) Color(0xFFFFF1F0) else Color(0xFFF2F7EC),
                        border = BorderStroke(1.dp, if (entry.tag.contains("चेतावनी")) Color(0xFFFFCCC7) else Color(0xFFD6E4C4))
                    ) {
                        Text(
                            text = entry.tag,
                            color = if (entry.tag.contains("चेतावनी")) Color(0xFFCF1322) else Color(0xFF389E0D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFEFE6D8))

                // Full Dream Description
                Text(
                    text = "सपना / अनुभव:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = Ink
                )
                Text(
                    text = entry.description,
                    color = Ink,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                )

                // Meaning if available
                if (!entry.meaning.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF6F9F2),
                        border = BorderStroke(1.dp, Color(0xFFDCEAD4)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "✨ वैदिक विश्लेषण व फल:",
                                color = Color(0xFF235527),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = entry.meaning!!,
                                color = Ink,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}
