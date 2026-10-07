package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DevLogoIcon
import com.example.ui.theme.*
import com.example.R
import kotlinx.coroutines.delay

/**
 * Splash Screen shown when the app launches ("app ke shuru hone pe").
 * Displays the iconic "देव" logo with smooth entrance animations.
 */
@Composable
fun SplashScreen(
    onTimeout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.72f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Smooth entrance animation
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650)
        )
        // Hold on screen, then transition smoothly into the app
        delay(1200L)
        onTimeout()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaperBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Animated Brand Logo (Dev Squircle)
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value),
                contentAlignment = Alignment.Center
            ) {
                // Official Sacred Logo (Dev Squircle)
                DevLogoIcon(
                    size = 96.dp,
                    elevation = 8.dp,
                    showGlow = false
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Name / Brand Title: देव भाषा
            Text(
                text = "देव भाषा",
                fontFamily = AppFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2B2B2B),
                letterSpacing = 1.sp,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle / Tagline
            Text(
                text = "वैदिक साधक • स्वप्न विचार • सीधा समाधान",
                fontFamily = AppFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6E6E6E),
                letterSpacing = 0.4.sp,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Vol. 01 · Est. MMXXIV",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = InkFaint,
                letterSpacing = 1.sp,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom subtle loading indicator
            CircularProgressIndicator(
                color = Terra,
                strokeWidth = 2.5.dp,
                modifier = Modifier
                    .size(24.dp)
                    .alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ESTD · 2024 · VERIFIED VEDIC SADHAK",
                fontSize = 11.sp,
                color = InkFaint,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .alpha(alpha.value)
            )
        }
    }
}
