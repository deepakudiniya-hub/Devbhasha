package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DevLogoIcon
import com.example.ui.models.SadhakItem
import com.example.ui.theme.*

/**
 * Tab 2: "साधक" (Sadhak Directory Screen)
 * Displays list of Vedic Sadhaks with Online status filter, search, bio, and quick Call (📞) & Chat (💬) buttons.
 */
@Composable
fun SadhakDirectoryScreen(
    sadhaks: List<SadhakItem>,
    isHindi: Boolean,
    onConsultSadhak: (SadhakItem) -> Unit,
    onCallClick: (SadhakItem) -> Unit = {},
    onChatClick: (SadhakItem) -> Unit = {},
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }

    val filteredList = remember(sadhaks, searchQuery, selectedFilter) {
        sadhaks.filter { sadhak ->
            val matchesSearch = searchQuery.isBlank() ||
                sadhak.nameHi.contains(searchQuery, ignoreCase = true) ||
                sadhak.nameEn.contains(searchQuery, ignoreCase = true) ||
                sadhak.titleHi.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "online" -> sadhak.isOnline
                "top" -> (sadhak.rating.toDoubleOrNull() ?: 0.0) >= 4.9
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Neutral200, RoundedCornerShape(20.dp)),
            placeholder = {
                Text(
                    text = if (isHindi) "साधक, विशेषज्ञता या नाम से खोजें..." else "Search sadhaks by name or expertise...",
                    fontSize = 13.5.sp,
                    color = Neutral400
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = SaffronPrimary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear",
                            tint = Neutral400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "all" to (if (isHindi) "सभी साधक" else "All Sadhaks"),
                "online" to (if (isHindi) "ऑनलाइन (🟢)" else "Online Now (🟢)"),
                "top" to (if (isHindi) "सर्वोच्च रेटिंग" else "Top Rated")
            )
            filters.forEach { (key, label) ->
                val isSelected = selectedFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = key },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SaffronPrimary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SaffronPrimary)
            }
        } else if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.PersonOff,
                        contentDescription = null,
                        tint = Neutral400,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isHindi) "कोई साधक नहीं मिला" else "No Sadhaks Found",
                        fontWeight = FontWeight.Medium,
                        color = Neutral500
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredList, key = { it.id }) { sadhak ->
                    SadhakDirectoryCard(
                        sadhak = sadhak,
                        isHindi = isHindi,
                        onConsultClick = { onConsultSadhak(sadhak) },
                        onCallClick = { onCallClick(sadhak) },
                        onChatClick = { onChatClick(sadhak) }
                    )
                }
            }
        }
    }
}

@Composable
fun SadhakDirectoryCard(
    sadhak: SadhakItem,
    isHindi: Boolean,
    onConsultClick: () -> Unit,
    onCallClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Neutral200),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with initial and online indicator
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SaffronPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isHindi) sadhak.initialHi else sadhak.initialEn,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (sadhak.isOnline) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(EmeraldBase)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isHindi) sadhak.nameHi else sadhak.nameEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Neutral800
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (sadhak.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(GreenPrimary)
                            )
                        }
                    }

                    Text(
                        text = if (isHindi) sadhak.titleHi else sadhak.titleEn,
                        color = SaffronDeep,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Saffron,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = sadhak.rating,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Neutral800
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${if (isHindi) sadhak.experienceHi else sadhak.experienceEn}",
                            fontSize = 12.sp,
                            color = Neutral500
                        )
                    }
                }

                // Action Buttons (Call 📞 / Chat 💬 / Consult)
                if (sadhak.isOnline) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onCallClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GreenPrimary.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = "Call",
                                tint = GreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onChatClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SaffronPrimary.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Chat,
                                contentDescription = "Chat",
                                tint = SaffronPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onConsultClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Neutral400),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isHindi) "विवरण" else "Details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Neutral100)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = sadhak.bio,
                fontSize = 12.5.sp,
                color = Neutral600,
                lineHeight = 17.sp
            )
        }
    }
}

/**
 * Tab 3: "उपाय" (Remedy Screen with Panchang & Mala Counter)
 */
@Composable
fun RemedyScreen(
    isHindi: Boolean,
    modifier: Modifier = Modifier
) {
    var malaCount by remember { mutableIntStateOf(0) }
    var malaLaps by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Today's Panchang Hero Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, BorderLight),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceAlt),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🕉️", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isHindi) "आज का पंचांग व शुभ मुहूर्त" else "Today's Panchang & Muhurat",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = InkPrimary
                            )
                            Text(
                                text = "विक्रम संवत् 2083 • आश्विन मास",
                                fontSize = 12.sp,
                                color = Saffron
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Neutral50)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PanchangDetailItem(
                        label = if (isHindi) "अभिजित मुहूर्त" else "Abhijit Muhurat",
                        value = "11:45 AM - 12:35 PM",
                        isHighlight = true
                    )
                    PanchangDetailItem(
                        label = if (isHindi) "राहुकाल" else "Rahu Kaal",
                        value = "04:30 PM - 06:00 PM",
                        isHighlight = false
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PanchangDetailItem(
                        label = if (isHindi) "तिथि" else "Tithi",
                        value = "शुक्ल पक्ष एकादशी",
                        isHighlight = false
                    )
                    PanchangDetailItem(
                        label = if (isHindi) "दिशा शूल" else "Disha Shool",
                        value = "उत्तर दिशा",
                        isHighlight = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Digital Japa Mala Counter Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, BorderLight),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📿", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isHindi) "डिजिटल मंत्र जाप माला" else "Digital Japa Mala",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = InkPrimary
                        )
                        Text(
                            text = if (isHindi) "108 मनकों का सात्विक चक्र" else "108 Beads sacred cycle",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Circular Counter Button
                Surface(
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            if (malaCount < 107) {
                                malaCount++
                            } else {
                                malaCount = 0
                                malaLaps++
                            }
                        },
                    color = Color.White,
                    border = BorderStroke(3.dp, Saffron)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$malaCount",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = InkPrimary
                        )
                        Text(
                            text = "/ 108",
                            fontSize = 13.sp,
                            color = Neutral400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isHindi) "पूर्ण माला चक्र (Laps): $malaLaps" else "Completed Malas: $malaLaps",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SaffronDeep
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            malaCount = 0
                            malaLaps = 0
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Neutral300)
                    ) {
                        Text(
                            text = if (isHindi) "रीसेट करें" else "Reset",
                            color = Neutral500,
                            fontSize = 12.5.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (malaCount < 107) {
                                malaCount++
                            } else {
                                malaCount = 0
                                malaLaps++
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isHindi) "जाप करें (+1)" else "Count (+1)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PanchangDetailItem(
    label: String,
    value: String,
    isHighlight: Boolean
) {
    Column {
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = Neutral500
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) SaffronDeep else Neutral800
        )
    }
}

/**
 * Tab 4: "प्रोफाइल" (User Profile Screen)
 */
@Composable
fun UserProfileScreen(
    userId: String,
    userNameInitial: String,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SaffronPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userNameInitial.take(1).ifEmpty { "द" },
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = userNameInitial,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral800
        )

        Text(
            text = "ID: $userId",
            fontSize = 12.5.sp,
            color = Neutral500
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Settings / Options List
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Neutral200),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                ProfileOptionRow(
                    icon = Icons.Outlined.History,
                    title = "पूछा व परामर्श इतिहास",
                    onClick = {}
                )
                HorizontalDivider(color = Neutral100)
                ProfileOptionRow(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = "सहेजे गए मंत्र व उपाय",
                    onClick = {}
                )
                HorizontalDivider(color = Neutral100)
                ProfileOptionRow(
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    title = "सहायता एवं समर्थन",
                    onClick = {}
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "लॉगआउट करें",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SaffronPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Neutral800,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Neutral400,
            modifier = Modifier.size(20.dp)
        )
    }
}
