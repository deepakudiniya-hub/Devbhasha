package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class SearchableContentCard(
    val id: String,
    val titleHi: String,
    val titleEn: String,
    val keywords: List<String>,
    val emoji: String
)

@Composable
fun ContentCardSearchSection(
    onCardSelected: (String) -> Unit,
    isHindi: Boolean = true,
    language: String = "hi",
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val cards = remember {
        listOf(
            SearchableContentCard("family", "पारिवारिक शांति", "Family Peace", listOf("family", "ghar", "vivad", "परिवार", "क्लेश", "शांति", "घर"), "🏡"),
            SearchableContentCard("career", "करियर एवं व्यापार", "Career & Business", listOf("career", "job", "business", "naukri", "व्यापार", "नौकरी", "सफलता", "धन"), "💼"),
            SearchableContentCard("marriage", "विवाह एवं संबंध", "Marriage & Love", listOf("marriage", "vivah", "love", "relationship", "विवाह", "प्रेम", "संबंध", "शादी"), "❤️"),
            SearchableContentCard("health", "स्वास्थ्य एवं आरोग्य", "Health & Wellness", listOf("health", "disease", "swasthya", "swasth", "स्वास्थ्य", "रोग", "आरोग्य", "तनाव"), "🩺"),
            SearchableContentCard("money", "धन एवं समृद्धि", "Wealth & Prosperity", listOf("money", "dhan", "wealth", "prosperity", "कर्ज", "धन", "समृद्धि"), "🪙"),
            SearchableContentCard("negativity", "नकारात्मकता निवारण", "Negativity Removal", listOf("negativity", "bhooty", "evil", "buri", "नकारात्मकता", "नजर", "टोटका"), "🧿"),
            SearchableContentCard("pitr", "पितृ दोष निवारण", "Pitr Dosh Remedies", listOf("pitr", "pitradosh", "ancestors", "पितृ", "श्राप", "दोष"), "🪔")
        )
    }

    val filteredCards = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val q = searchQuery.trim().lowercase()
            cards.filter {
                it.titleHi.lowercase().contains(q) ||
                it.titleEn.lowercase().contains(q) ||
                it.keywords.any { kw -> kw.lowercase().contains(q) }
            }
        }
    }

    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = if (currentLangCode == "hi") "समस्या या कार्ड खोजें (उदा. करियर, परिवार, स्वास्थ्य)..." else "Search content cards (e.g. career, family, health)...",
                    fontSize = 13.sp,
                    color = InkFaint
                )
            },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = Saffron, modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Outlined.Clear, contentDescription = "Clear", tint = InkSoft, modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Saffron,
                unfocusedBorderColor = EditorialLineStrong,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Filtered Results Chips / Cards Row
        if (filteredCards.isNotEmpty() && searchQuery.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (currentLangCode == "hi") "खोज परिणाम (${filteredCards.size})" else "Search Results (${filteredCards.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = InkSoft,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredCards) { card ->
                    val title = if (currentLangCode == "hi") card.titleHi else card.titleEn
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.2.dp, Saffron.copy(alpha = 0.4f)),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onCardSelected(card.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = card.emoji, fontSize = 16.sp)
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        }
                    }
                }
            }
        } else if (searchQuery.isNotBlank() && filteredCards.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEF2F2),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (currentLangCode == "hi") "कोई कार्ड नहीं मिला। कृपया दूसरा कीवर्ड दर्ज करें।" else "No cards found. Try another keyword.",
                    fontSize = 12.sp,
                    color = Color(0xFF991B1B),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
