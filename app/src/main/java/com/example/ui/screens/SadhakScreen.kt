package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.models.SadhakItem
import com.example.ui.theme.*

/**
 * Devbhasha Sadhak Tab Screen
 * Displays verified astrologers and sadhaks with online indicators,
 * search bar, category & status filter chips.
 */
@Composable
fun SadhakScreen(
    sadhakList: List<SadhakItem>,
    onChatClick: (SadhakItem) -> Unit,
    onCallClick: (SadhakItem) -> Unit,
    onSessionClick: (SadhakItem) -> Unit,
    onSadhakClick: (SadhakItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    val filters = listOf(
        "all" to "All Experts",
        "online" to "Online",
        "top" to "Top Rated",
        "vedic" to "Vedic Astrology",
        "vastu" to "Vastu",
        "tarot" to "Tarot"
    )

    val filteredSadhaks = remember(selectedFilter, searchQuery, sadhakList) {
        sadhakList.filter { sadhak ->
            val matchesSearch = searchQuery.isBlank() ||
                    sadhak.nameEn.contains(searchQuery, ignoreCase = true) ||
                    sadhak.titleEn.contains(searchQuery, ignoreCase = true) ||
                    sadhak.nameHi.contains(searchQuery, ignoreCase = true) ||
                    sadhak.titleHi.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "online" -> sadhak.isOnline
                "top" -> (sadhak.rating.toDoubleOrNull() ?: 0.0) >= 4.8
                "vedic" -> sadhak.titleEn.contains("Vedic", ignoreCase = true) || sadhak.titleHi.contains("वैदिक", ignoreCase = true)
                "vastu" -> sadhak.titleEn.contains("Vastu", ignoreCase = true) || sadhak.titleHi.contains("वास्तु", ignoreCase = true)
                "tarot" -> sadhak.titleEn.contains("Tarot", ignoreCase = true) || sadhak.titleHi.contains("टैरो", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8F2))
            .padding(horizontal = DevSpacing.space16),
        contentPadding = PaddingValues(top = DevSpacing.space12, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(DevSpacing.space12)
    ) {

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.search_hint), fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = SaffronPrimary) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaffronPrimary,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(DevSpacing.space8),
                modifier = Modifier.padding(vertical = DevSpacing.space4)
            ) {
                items(filters) { (filterKey, label) ->
                    val isSelected = selectedFilter == filterKey
                    Surface(
                        shape = DevbhashaChipShape,
                        color = if (isSelected) SaffronPrimary else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) SaffronPrimary else Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .clip(DevbhashaChipShape)
                            .clickable { selectedFilter = filterKey }
                    ) {
                        Text(
                            text = label,
                            fontFamily = NotoSansDevanagari,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF1E293B),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        if (filteredSadhaks.isEmpty()) {
            item {
                Surface(
                    shape = DevbhashaCardShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(DevSpacing.space24),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.avatar_01),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(2.dp, SaffronPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.height(DevSpacing.space12))
                        Text(
                            text = stringResource(R.string.sadhak_empty_title),
                            fontFamily = NotoSansDevanagari,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(DevSpacing.space16))
                        Button(
                            onClick = {
                                searchQuery = ""
                                selectedFilter = "all"
                                Toast.makeText(context, "Filters reset", Toast.LENGTH_SHORT).show()
                            },
                            shape = DevbhashaButtonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SaffronPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(DevSpacing.buttonHeight)
                        ) {
                            Text(
                                text = "View All Experts",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        } else {
            // Sadhak Cards
            items(filteredSadhaks) { sadhak ->
                Surface(
                    shape = DevbhashaCardShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(DevbhashaCardShape)
                        .clickable { onSadhakClick(sadhak) }
                ) {
                    Column(modifier = Modifier.padding(DevSpacing.space16)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar with Online indicator
                            Box(modifier = Modifier.size(54.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = SaffronSurface,
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Image(
                                        painter = painterResource(id = sadhak.getAvatarRes()),
                                        contentDescription = sadhak.nameHi,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                                if (sadhak.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E8B57))
                                            .align(Alignment.BottomEnd)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(DevSpacing.space12))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = sadhak.nameEn.ifBlank { sadhak.nameHi },
                                        fontSize = 17.sp,
                                        lineHeight = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.width(DevSpacing.space8))
                                    Surface(
                                        shape = DevbhashaChipShape,
                                        color = Color(0xFFFEF3C7)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = sadhak.rating.ifBlank { "4.9" },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF92400E)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(DevSpacing.space2))

                                Text(
                                    text = sadhak.titleEn.ifBlank { sadhak.titleHi },
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )

                                val expText = sadhak.experienceEn.ifBlank { sadhak.experienceHi }
                                val rateSummaries = listOfNotNull(
                                    if (sadhak.chatPricePerMin > 0) "Chat ₹${sadhak.chatPricePerMin}/min" else null,
                                    if (sadhak.callPricePerMin > 0) "Call ₹${sadhak.callPricePerMin}/min" else null,
                                    if (sadhak.sessionPrice30Min > 0) "30-min Session ₹${sadhak.sessionPrice30Min}" else null
                                ).joinToString(" · ")
                                val subText = if (rateSummaries.isNotBlank()) "Experience: $expText · $rateSummaries" else "Experience: $expText"

                                Text(
                                    text = subText,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DevSpacing.space12))

                        // Actions Row: Chat, Call, 30-min Session (Hide option if price is 0)
                        val hasAnyOption = sadhak.chatPricePerMin > 0 || sadhak.callPricePerMin > 0 || sadhak.sessionPrice30Min > 0
                        if (hasAnyOption) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(DevSpacing.space8)
                            ) {
                                val isOnline = sadhak.isOnline

                                // Chat Button
                                if (sadhak.chatPricePerMin > 0) {
                                    Button(
                                        onClick = { onChatClick(sadhak) },
                                        enabled = isOnline,
                                        shape = DevbhashaButtonShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SaffronPrimary,
                                            contentColor = Color.White,
                                            disabledContainerColor = Color(0xFFE2E8F0),
                                            disabledContentColor = Color(0xFF94A3B8)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    ) {
                                        Text(
                                            text = "Chat\n₹${sadhak.chatPricePerMin}/min",
                                            fontFamily = NotoSansDevanagari,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Call Button
                                if (sadhak.callPricePerMin > 0) {
                                    Button(
                                        onClick = { onCallClick(sadhak) },
                                        enabled = isOnline,
                                        shape = DevbhashaButtonShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SaffronPrimary,
                                            contentColor = Color.White,
                                            disabledContainerColor = Color(0xFFE2E8F0),
                                            disabledContentColor = Color(0xFF94A3B8)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    ) {
                                        Text(
                                            text = "Call\n₹${sadhak.callPricePerMin}/min",
                                            fontFamily = NotoSansDevanagari,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Session Button
                                if (sadhak.sessionPrice30Min > 0) {
                                    Button(
                                        onClick = { onSessionClick(sadhak) },
                                        enabled = isOnline,
                                        shape = DevbhashaButtonShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SaffronPrimary,
                                            contentColor = Color.White,
                                            disabledContainerColor = Color(0xFFE2E8F0),
                                            disabledContentColor = Color(0xFF94A3B8)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    ) {
                                        Text(
                                            text = "30-min Session\n₹${sadhak.sessionPrice30Min}",
                                            fontFamily = NotoSansDevanagari,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
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
