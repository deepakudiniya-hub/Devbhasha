package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.SadhakItem
import com.example.ui.theme.*

// =============================================================================
// ASTROTALK CONSULTATION SCREEN (Chat with Astrologer tab)
// Matches rightmost screen in user reference image
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstrotalkConsultationScreen(
    sadhaks: List<SadhakItem>,
    balanceAmount: Double,
    isHindi: Boolean,
    onSadhakClick: (SadhakItem) -> Unit,
    onChatClick: (SadhakItem) -> Unit,
    onCallClick: (SadhakItem) -> Unit,
    onWalletClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(0xFFF97316)
    val textColor = Color(0xFF2B2B2B)
    val textSub = Color(0xFF6E6E6E)

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }

    val categories = listOf(
        "all" to (if (isHindi) "सभी" else "All"),
        "vedic" to (if (isHindi) "वैदिक ज्योतिष" else "Vedic"),
        "tarot" to (if (isHindi) "टैरो कार्ड" else "Tarot"),
        "kundli" to (if (isHindi) "कुंडली" else "Kundli"),
        "love" to (if (isHindi) "प्रेम व संबंध" else "Love"),
        "career" to (if (isHindi) "करियर व व्यवसाय" else "Career"),
        "marriage" to (if (isHindi) "विवाह मिलान" else "Marriage")
    )

    val filteredList = remember(sadhaks, searchQuery, selectedCategory) {
        sadhaks.filter { item ->
            val matchQuery = searchQuery.isBlank() ||
                    item.nameHi.contains(searchQuery, true) ||
                    item.nameEn.contains(searchQuery, true) ||
                    item.titleHi.contains(searchQuery, true)
            val matchCategory = when (selectedCategory) {
                "all" -> true
                "vedic" -> item.titleHi.contains("ज्योतिष", true) || item.titleEn.contains("Vedic", true)
                "tarot" -> item.titleHi.contains("टैरो", true) || item.titleEn.contains("Tarot", true)
                "kundli" -> item.titleHi.contains("कुंडली", true) || item.titleEn.contains("Kundli", true)
                "love" -> item.titleHi.contains("संबंध", true) || item.titleEn.contains("Love", true)
                "career" -> item.titleHi.contains("करियर", true) || item.titleEn.contains("Career", true)
                "marriage" -> item.titleHi.contains("विवाह", true) || item.titleEn.contains("Marriage", true)
                else -> true
            }
            matchQuery && matchCategory
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF9))
    ) {
        // Top App Bar matching Astrotalk
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isHindi) "ज्योतिषी से चैट करें" else "Chat with Astrologer",
                fontFamily = AppFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            // Wallet Chip with "+" icon
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = primaryColor,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onWalletClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "₹${balanceAmount.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "+",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        // Search Bar with Filter Icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = textSub,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                if (isHindi) "नाम या विशेषज्ञता खोजें..." else "Search by name or skill...",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { (key, label) ->
                val isSelected = selectedCategory == key
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSelected) primaryColor else Color.White,
                    border = BorderStroke(1.dp, if (isSelected) primaryColor else Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { selectedCategory = key }
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else textColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Astrologers List (matching Astrotalk design)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList) { sadhak ->
                AstrotalkAstrologerCard(
                    sadhak = sadhak,
                    isHindi = isHindi,
                    onChatClick = { onChatClick(sadhak) },
                    onCallClick = { onCallClick(sadhak) }
                )
            }
        }
    }
}
