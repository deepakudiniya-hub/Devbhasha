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
    onSadhakClick: (SadhakItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    val filters = listOf(
        "all" to "सभी विशेषज्ञ",
        "online" to "उपलब्ध Online",
        "top" to "शीर्ष Top Rated",
        "vedic" to "वैदिक Vedic",
        "vastu" to "वास्तु Vastu",
        "tarot" to "टैरो Tarot"
    )

    val filteredSadhaks = remember(selectedFilter, searchQuery, sadhakList) {
        sadhakList.filter { sadhak ->
            val matchesSearch = searchQuery.isBlank() ||
                    sadhak.nameEn.contains(searchQuery, ignoreCase = true) ||
                    sadhak.nameHi.contains(searchQuery, ignoreCase = true) ||
                    sadhak.titleHi.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "online" -> sadhak.isOnline
                "top" -> (sadhak.rating.toDoubleOrNull() ?: 0.0) >= 4.8
                "vedic" -> sadhak.titleHi.contains("वैदिक", ignoreCase = true) || sadhak.titleEn.contains("Vedic", ignoreCase = true)
                "vastu" -> sadhak.titleHi.contains("वास्तु", ignoreCase = true) || sadhak.titleEn.contains("Vastu", ignoreCase = true)
                "tarot" -> sadhak.titleHi.contains("टैरो", ignoreCase = true) || sadhak.titleEn.contains("Tarot", ignoreCase = true)
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
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = Color(0xFF0F5C5C)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0F5C5C),
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
                        color = if (isSelected) Color(0xFF0F5C5C) else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF0F5C5C) else Color(0xFFCBD5E1)),
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
                                .border(2.dp, Color(0xFF0F5C5C), CircleShape)
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
                                Toast.makeText(context, "फ़िल्टर रीसेट किया गया", Toast.LENGTH_SHORT).show()
                            },
                            shape = DevbhashaButtonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0F5C5C),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(DevSpacing.buttonHeight)
                        ) {
                            Text(
                                text = "सभी विशेषज्ञ देखें",
                                fontFamily = NotoSansDevanagari,
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
                                    color = Color(0xFFE6F4F1),
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
                                        text = sadhak.nameHi.ifBlank { "वैदिक Sadhak" },
                                        fontFamily = TiroDevanagariHindi,
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
                                                fontFamily = NotoSansDevanagari,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF92400E)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(DevSpacing.space2))

                                Text(
                                    text = sadhak.titleHi.ifBlank { "वैदिक ज्योतिष एवं मार्गदर्शन" },
                                    fontFamily = NotoSansDevanagari,
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )

                                Text(
                                    text = "अनुभव: ${sadhak.experienceHi} · ${sadhak.consultationFee}",
                                    fontFamily = NotoSansDevanagari,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DevSpacing.space12))

                        // Actions Row: Chat and Call
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DevSpacing.space12)
                        ) {
                            Button(
                                onClick = { onChatClick(sadhak) },
                                shape = DevbhashaButtonShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0F5C5C),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(DevSpacing.space4))
                                Text(
                                    text = "चैट परामर्श",
                                    fontFamily = NotoSansDevanagari,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { onCallClick(sadhak) },
                                shape = DevbhashaButtonShape,
                                border = BorderStroke(1.5.dp, Color(0xFF0F5C5C)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF0F5C5C)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(DevSpacing.space4))
                                Text(
                                    text = "कॉल परामर्श",
                                    fontFamily = NotoSansDevanagari,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
