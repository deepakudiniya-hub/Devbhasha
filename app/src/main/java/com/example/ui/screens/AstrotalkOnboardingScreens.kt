package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DevLogoIcon
import com.example.ui.theme.*

// =============================================================================
// SCREEN 1, 2, 3: ONBOARDING WALKTHROUGH WITH CELESTIAL MANDALAS
// =============================================================================

@Composable
fun AstrotalkOnboardingWalkthrough(
    currentStep: Int,
    onNextStep: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(0xFF0D656C)
    val accentGold = Color(0xFFEAB308)
    val textColor = Color(0xFF2B2B2B)
    val textSub = Color(0xFF6E6E6E)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF6EA))
            .padding(horizontal = 24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar: Skip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onSkip) {
                Text(
                    text = "Skip",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textSub
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Titles
        Crossfade(targetState = currentStep, label = "walkthrough_text") { step ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (step) {
                        0 -> "सत्यापित वैदिक साधक से\nसीधा मार्गदर्शन"
                        1 -> "सटीक स्वप्न विचार\nएवं अंतर्दृष्टि"
                        else -> "गोपनीय एवं सुरक्षित\nलाइव परामर्श"
                    },
                    fontFamily = AppFontFamily,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = when (step) {
                        0 -> "विवाह, करियर, स्वास्थ्य या व्यापार — अपनी समस्याओं का सात्विक समाधान पाएं।"
                        1 -> "सपने में देखे गए प्रतीकों का अर्थ जानें और अपने जीवन में सही मार्गदर्शन पाएं।"
                        else -> "अनुभवी आचार्यों व साधकों से तुरंत लाइव चैट एवं ऑडियो कॉल द्वारा बात करें।"
                    },
                    fontFamily = AppFontFamily,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = textSub,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step Indicator Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val isActive = index == currentStep
                Box(
                    modifier = Modifier
                        .size(if (isActive) 22.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isActive) primaryColor else Color(0xFFE2E8F0))
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Celestial Mandala Graphic
        Box(
            modifier = Modifier
                .size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            when (currentStep) {
                0 -> AstrotalkZodiacWheelIllustration(primaryColor = primaryColor, accentColor = accentGold)
                1 -> AstrotalkSunMandalaIllustration(primaryColor = primaryColor, accentColor = accentGold)
                else -> AstrotalkYantraMandalaIllustration(primaryColor = primaryColor, accentColor = accentGold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Circular Action Button (Astrotalk style)
        Surface(
            shape = CircleShape,
            color = primaryColor,
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .clickable { onNextStep() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

// =============================================================================
// SCREEN 4: CHOOSE YOUR PREFERABLE LANGUAGE
// =============================================================================

@Composable
fun AstrotalkLanguageSelectionScreen(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(0xFF0D656C)
    val textColor = Color(0xFF2B2B2B)
    val textSub = Color(0xFF6E6E6E)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF6EA))
            .padding(horizontal = 22.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Row: Skip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onSkip) {
                Text("Skip", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textSub)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Choose your preferable\nlanguage",
            fontFamily = AppFontFamily,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Choose your preferred language for convenience",
            fontFamily = AppFontFamily,
            fontSize = 13.sp,
            color = textSub
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 3-Column Grid of Language Pills (matching Astrotalk)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(ASTROTALK_LANGUAGES) { lang ->
                val isSelected = selectedLanguage.equals(lang.code, ignoreCase = true)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFFFAF6EA) else Color.White,
                    shadowElevation = if (isSelected) 2.dp else 1.dp,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) primaryColor else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .aspectRatio(1.1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onLanguageSelected(lang.code) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = lang.nativeScript,
                            fontFamily = AppFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) primaryColor else textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = lang.nameEn,
                            fontSize = 11.5.sp,
                            color = if (isSelected) primaryColor.copy(alpha = 0.85f) else textSub,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Bottom Continue Button
        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}

// =============================================================================
// SCREEN 5: CHOOSE YOUR ZODIAC SIGN
// =============================================================================

@Composable
fun AstrotalkZodiacSelectionScreen(
    selectedZodiac: String,
    onZodiacSelected: (String) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(0xFF0D656C)
    val textColor = Color(0xFF2B2B2B)
    val textSub = Color(0xFF6E6E6E)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF6EA))
            .padding(horizontal = 20.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Row: Skip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onSkip) {
                Text("Skip", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textSub)
            }
        }

        Text(
            text = "Choose your Zodiac\nSign",
            fontFamily = AppFontFamily,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Selecting your zodiac sign will help you get daily horoscope",
            fontFamily = AppFontFamily,
            fontSize = 13.sp,
            color = textSub
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3-Column Grid of 12 Zodiac signs
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(ASTROTALK_ZODIAC_SIGNS) { sign ->
                val isSelected = selectedZodiac.equals(sign.id, ignoreCase = true)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFFFAF6EA) else Color.White,
                    shadowElevation = if (isSelected) 2.dp else 1.dp,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) primaryColor else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .aspectRatio(0.98f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onZodiacSelected(sign.id) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) primaryColor.copy(alpha = 0.15f) else Color(0xFFF8FAFC),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = sign.symbol,
                                    fontSize = 22.sp,
                                    color = if (isSelected) primaryColor else textColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = sign.nameHi,
                            fontFamily = AppFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) primaryColor else textColor,
                            maxLines = 1
                        )

                        Text(
                            text = sign.nameEn,
                            fontSize = 10.5.sp,
                            color = if (isSelected) primaryColor else textSub,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Bottom Continue Button
        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}

// =============================================================================
// SCREEN 6: FILL YOUR SOME PERSONNEL DETAILS (KUNDLI SETUP)
// =============================================================================

@Composable
fun AstrotalkPersonalDetailsScreen(
    initialName: String,
    onSaveDetails: (name: String, gender: String, dob: String, tob: String, pob: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(0xFF0D656C)
    val textColor = Color(0xFF2B2B2B)
    val textSub = Color(0xFF6E6E6E)

    var gender by remember { mutableStateOf("male") }
    var name by remember { mutableStateOf(initialName.ifBlank { "दीपक" }) }
    var dob by remember { mutableStateOf("15/08/1998") }
    var tob by remember { mutableStateOf("08:30 AM") }
    var pob by remember { mutableStateOf("वाराणसी, उत्तर प्रदेश") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF6EA))
            .padding(horizontal = 22.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Back
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
            }
        }

        Text(
            text = "Fill your some\npersonnel details",
            fontFamily = AppFontFamily,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Please enter your credentials to proceed for accurate kundli & horoscope",
            fontFamily = AppFontFamily,
            fontSize = 13.sp,
            color = textSub
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Gender Selection (Male / Female avatar pills)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Male
            val isMale = gender == "male"
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isMale) Color(0xFFFAF6EA) else Color.White,
                border = BorderStroke(if (isMale) 2.dp else 1.dp, if (isMale) primaryColor else Color(0xFFE2E8F0)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { gender = "male" }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "👨", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "पुरुष (Male)",
                        fontSize = 13.sp,
                        fontWeight = if (isMale) FontWeight.Bold else FontWeight.Medium,
                        color = if (isMale) primaryColor else textColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Female
            val isFemale = gender == "female"
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isFemale) Color(0xFFFAF6EA) else Color.White,
                border = BorderStroke(if (isFemale) 2.dp else 1.dp, if (isFemale) primaryColor else Color(0xFFE2E8F0)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { gender = "female" }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "👩", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "महिला (Female)",
                        fontSize = 13.sp,
                        fontWeight = if (isFemale) FontWeight.Bold else FontWeight.Medium,
                        color = if (isFemale) primaryColor else textColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Full Name
        Text("पूरा नाम (Full Name)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = textColor)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Enter your full name", color = Color(0xFF94A3B8)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Date of Birth & Time of Birth (2 Columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("जन्म तिथि (DOB)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    placeholder = { Text("DD/MM/YYYY", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("जन्म समय (Time)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = tob,
                    onValueChange = { tob = it },
                    placeholder = { Text("HH:MM AM", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Place of Birth
        Text("जन्म स्थान (Place of Birth)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = textColor)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = pob,
            onValueChange = { pob = it },
            placeholder = { Text("City, State, Country", color = Color(0xFF94A3B8)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Finish Button
        Button(
            onClick = { onSaveDetails(name, gender, dob, tob, pob) },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Finish",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// =============================================================================
// SCREEN 7: "YOU'RE ALL SET!" CONFIRMATION
// =============================================================================

@Composable
fun AstrotalkAllSetScreen(
    onLetsGo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(0xFF0D656C)
    val textColor = Color(0xFF2B2B2B)
    val textSub = Color(0xFF6E6E6E)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF6EA))
            .padding(horizontal = 26.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Big Checkmark Circle Icon (✓) matching Astrotalk
        Surface(
            shape = CircleShape,
            color = Color(0xFFDCFCE7),
            border = BorderStroke(3.dp, Color(0xFF16A34A)),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "You're All Set!",
            fontFamily = AppFontFamily,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "सत्यापित वैदिक साधकों से परामर्श एवं स्वप्न विचार प्रारंभ करें।",
            fontFamily = AppFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = textSub,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLetsGo,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Let's Go!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
