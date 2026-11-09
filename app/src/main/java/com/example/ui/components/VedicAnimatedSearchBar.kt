package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.SadhakItem
import com.example.ui.theme.*



data class SpiritualCategoryItem(
    val id: String,
    val nameHi: String,
    val nameEn: String,
    val subtitleHi: String,
    val subtitleEn: String,
    val targetView: String,
    val iconType: String
)

val SPIRITUAL_CATEGORIES = listOf(
    SpiritualCategoryItem(
        id = "kundli",
        nameHi = "कुंडली विश्लेषण एवं मिलान",
        nameEn = "Kundli Matching & Analysis",
        subtitleHi = "ग्रह दोष, दशा एवं लग्न विचार",
        subtitleEn = "Planetary placements, Dasha & Muhurat",
        targetView = "kundli",
        iconType = "psychology"
    ),
    SpiritualCategoryItem(
        id = "dreams",
        nameHi = "स्वप्न विचार व फलादेश",
        nameEn = "Dream Interpretation Journal",
        subtitleHi = "स्वप्नों के आध्यात्मिक व मनोवैज्ञानिक संकेत",
        subtitleEn = "Decipher symbols & spiritual messages",
        targetView = "dreams",
        iconType = "spa"
    ),
    SpiritualCategoryItem(
        id = "panchang",
        nameHi = "दैनिक वैदिक पंचांग",
        nameEn = "Daily Vedic Panchang",
        subtitleHi = "तिथि, नक्षत्र, चौघड़िया व शुभ मुहूर्त",
        subtitleEn = "Tithi, Nakshatra, Rahu Kaal & Muhurat",
        targetView = "panchang",
        iconType = "book"
    ),
    SpiritualCategoryItem(
        id = "remedies",
        nameHi = "वैदिक उपाय एवं अनुष्ठान",
        nameEn = "Sacred Remedies & Rituals",
        subtitleHi = "जीवन की बाधाओं के शास्त्रसम्मत निवारण",
        subtitleEn = "Time-tested remedial practices & guidance",
        targetView = "remedy",
        iconType = "spa"
    )
)

/**
 * Animated Search Bar with smooth expansion on focus and smooth fade-in search results.
 */
@Composable
fun VedicAnimatedSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    sadhakList: List<SadhakItem>,
    onSadhakClick: (SadhakItem) -> Unit,
    onCategoryClick: (String) -> Unit,
    isHindi: Boolean,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Expansion Animations when receiving focus
    val animatedHorizontalPadding by animateDpAsState(
        targetValue = 0.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "search_horiz_padding"
    )
    val animatedHeight by animateDpAsState(
        targetValue = if (isFocused) 52.dp else 48.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "search_height"
    )
    val animatedCornerRadius by animateDpAsState(
        targetValue = 20.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "search_corner_radius"
    )
    val animatedElevation by animateDpAsState(
        targetValue = if (isFocused) 6.dp else 2.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "search_elevation"
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isFocused) SaffronPrimary else Color(0xFFE2E8F0),
        animationSpec = tween(durationMillis = 300),
        label = "search_border_color"
    )
    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isFocused) 1.5.dp else 1.dp,
        animationSpec = tween(durationMillis = 300),
        label = "search_border_width"
    )
    val animatedBgColor by animateColorAsState(
        targetValue = Color.White,
        animationSpec = tween(durationMillis = 300),
        label = "search_bg_color"
    )
    val animatedIconTint by animateColorAsState(
        targetValue = if (isFocused) SaffronPrimary else Color(0xFF64748B),
        animationSpec = tween(durationMillis = 300),
        label = "search_icon_tint"
    )

    // Filtered results
    val cleanQuery = searchQuery.trim().lowercase()

    val filteredSadhaks = remember(cleanQuery, sadhakList) {
        if (cleanQuery.isBlank()) emptyList()
        else sadhakList.filter { sadhak ->
            sadhak.nameEn.lowercase().contains(cleanQuery) ||
            sadhak.nameHi.contains(cleanQuery) ||
            sadhak.titleEn.lowercase().contains(cleanQuery) ||
            sadhak.titleHi.contains(cleanQuery) ||
            sadhak.bio.lowercase().contains(cleanQuery)
        }
    }

    val filteredCategories = remember(cleanQuery) {
        if (cleanQuery.isBlank()) emptyList()
        else SPIRITUAL_CATEGORIES.filter { cat ->
            cat.nameEn.lowercase().contains(cleanQuery) ||
            cat.nameHi.contains(cleanQuery) ||
            cat.subtitleEn.lowercase().contains(cleanQuery) ||
            cat.subtitleHi.contains(cleanQuery)
        }
    }

    val totalMatchesCount = filteredSadhaks.size + filteredCategories.size

    Column(modifier = modifier.fillMaxWidth()) {
        // Expandable Search Bar container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = animatedHorizontalPadding)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(animatedHeight)
                    .shadow(animatedElevation, RoundedCornerShape(animatedCornerRadius)),
                shape = RoundedCornerShape(animatedCornerRadius),
                color = animatedBgColor,
                border = BorderStroke(animatedBorderWidth, animatedBorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = animatedIconTint,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = if (isHindi) "साधक या विषय खोजें..." else "Search sadhaks or topics...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    onFocusChange(focusState.isFocused)
                                },
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(SaffronPrimary)
                        )
                    }

                    // Trailing Clear or Close Action with smooth scale/fade animation
                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty() || isFocused,
                        enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                        exit = fadeOut(tween(150)) + scaleOut(tween(150))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            if (isFocused) {
                                TextButton(
                                    onClick = {
                                        onSearchQueryChange("")
                                        onFocusChange(false)
                                        focusManager.clearFocus()
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = if (isHindi) "रद्द" else "Done",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = SaffronPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Smooth Fade-In Search Results dropdown panel
        AnimatedVisibility(
            visible = isFocused,
            enter = fadeIn(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)) +
                    expandVertically(
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                        expandFrom = Alignment.Top
                    ),
            exit = fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)) +
                   shrinkVertically(
                       animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                       shrinkTowards = Alignment.Top
                   )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = animatedHorizontalPadding)
                    .padding(top = 10.dp)
                    .shadow(8.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    if (searchQuery.isBlank()) {
                        // Trending / Recommended Vedic Queries
                        Text(
                            text = if (isHindi) "लोकप्रिय आध्यात्मिक खोज" else "Trending Vedic Inquiries",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val suggestions = listOf(
                            "ज्योतिष परामर्श",
                            "स्वप्न विचार",
                            "कुंडली मिलान",
                            "पारिवारिक समस्या",
                            "स्वास्थ्य"
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(suggestions) { item ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = SaffronContainer.copy(alpha = 0.45f),
                                    border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.2f)),
                                    modifier = Modifier.clickable {
                                        onSearchQueryChange(item)
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = SaffronPrimary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = item,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium,
                                                color = SaffronPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                    } else {
                        // Display search results
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isHindi) "खोज परिणाम ($totalMatchesCount)" else "Search Results ($totalMatchesCount)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Matching Sadhaks
                        if (filteredSadhaks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isHindi) "साधक विशेषज्ञ (${filteredSadhaks.size})" else "Spiritual Sadhaks (${filteredSadhaks.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SlateLight,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            filteredSadhaks.take(3).forEach { sadhak ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable { onSadhakClick(sadhak) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(SaffronContainer, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isHindi) sadhak.initialHi else sadhak.initialEn,
                                                fontWeight = FontWeight.Bold,
                                                color = SaffronPrimary,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isHindi) sadhak.nameHi else sadhak.nameEn,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp
                                                )
                                            )
                                            Text(
                                                text = if (isHindi) sadhak.titleHi else sadhak.titleEn,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.5.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Rating badge
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = SaffronPrimary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = sadhak.rating,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }



                        // Matching Spiritual Categories
                        if (filteredCategories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isHindi) "संबंधित विषय (${filteredCategories.size})" else "Spiritual Tools (${filteredCategories.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SlateLight,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            filteredCategories.forEach { cat ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable { onCategoryClick(cat.targetView) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(SaffronContainer.copy(alpha = 0.7f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (cat.iconType) {
                                                    "psychology" -> Icons.Outlined.Psychology
                                                    "book" -> Icons.Outlined.Book
                                                    else -> Icons.Outlined.Spa
                                                },
                                                contentDescription = null,
                                                tint = SaffronPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isHindi) cat.nameHi else cat.nameEn,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            )
                                            Text(
                                                text = if (isHindi) cat.subtitleHi else cat.subtitleEn,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = SlateLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // No results fallback
                        if (totalMatchesCount == 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isHindi) "कोई परिणाम नहीं मिला" else "No matching results found",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
