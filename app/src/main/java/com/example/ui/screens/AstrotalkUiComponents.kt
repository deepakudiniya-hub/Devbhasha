package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DevLogoIcon
import com.example.ui.components.DevWatermarkLogo
import com.example.ui.models.SadhakItem
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*

// =============================================================================
// 1. CELESTIAL VECTOR ILLUSTRATIONS (Horoscope Wheel, Sun Mandala, Yantra)
// Matches Astrotalk Onboarding 1, 2, 3
// =============================================================================

/**
 * Onboarding Slide 1: Zodiac Wheel Mandala
 */
@Composable
fun AstrotalkZodiacWheelIllustration(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFFF97316),
    accentColor: Color = Color(0xFFEAB308)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wheel_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wheel_angle"
    )

    Box(
        modifier = modifier
            .size(240.dp)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val outerRadius = size.width * 0.46f
            val innerRadius = size.width * 0.32f
            val coreRadius = size.width * 0.16f

            // Outer rings
            drawCircle(
                color = primaryColor.copy(alpha = 0.25f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 1.8f)
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.35f),
                radius = outerRadius - 10f,
                center = center,
                style = Stroke(width = 1.2f)
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = innerRadius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // 12 spokes for 12 Zodiac Houses
            for (i in 0 until 12) {
                val angleRad = (i * 30.0 * PI / 180.0).toFloat()
                val startX = center.x + innerRadius * cos(angleRad)
                val startY = center.y + innerRadius * sin(angleRad)
                val endX = center.x + (outerRadius - 10f) * cos(angleRad)
                val endY = center.y + (outerRadius - 10f) * sin(angleRad)
                drawLine(
                    color = accentColor.copy(alpha = 0.45f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.2f
                )

                // Marker dot in house
                val midAngle = ((i * 30.0 + 15.0) * PI / 180.0).toFloat()
                val dotX = center.x + (innerRadius + (outerRadius - innerRadius) * 0.5f) * cos(midAngle)
                val dotY = center.y + (innerRadius + (outerRadius - innerRadius) * 0.5f) * sin(midAngle)
                drawCircle(
                    color = primaryColor,
                    radius = 3.5f,
                    center = Offset(dotX, dotY)
                )
            }

            // Core sun ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.15f),
                radius = coreRadius,
                center = center
            )
            drawCircle(
                color = primaryColor,
                radius = coreRadius,
                center = center,
                style = Stroke(width = 2f)
            )
        }

        // Center sacred glyph
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFF7ED),
            border = BorderStroke(1.5.dp, primaryColor),
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "♈",
                    fontSize = 24.sp,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Onboarding Slide 2: Celestial Sun Mandala
 */
@Composable
fun AstrotalkSunMandalaIllustration(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFFF97316),
    accentColor: Color = Color(0xFFEAB308)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sun_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .size(240.dp)
            .scale(pulse),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.width * 0.28f

            // 16 Solar Rays
            for (i in 0 until 16) {
                val angleRad = (i * 22.5 * PI / 180.0).toFloat()
                val rayLen = if (i % 2 == 0) size.width * 0.44f else size.width * 0.38f
                val endX = center.x + rayLen * cos(angleRad)
                val endY = center.y + rayLen * sin(angleRad)

                val perpAngle1 = (angleRad + PI / 2).toFloat()
                val perpAngle2 = (angleRad - PI / 2).toFloat()
                val baseWidth = 8f
                val p1 = Offset(center.x + baseWidth * cos(perpAngle1), center.y + baseWidth * sin(perpAngle1))
                val p2 = Offset(center.x + baseWidth * cos(perpAngle2), center.y + baseWidth * sin(perpAngle2))

                val rayPath = Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(endX, endY)
                    lineTo(p2.x, p2.y)
                    close()
                }
                drawPath(
                    path = rayPath,
                    color = if (i % 2 == 0) primaryColor.copy(alpha = 0.35f) else accentColor.copy(alpha = 0.25f)
                )
            }

            // Sun disk
            drawCircle(
                color = accentColor.copy(alpha = 0.2f),
                radius = baseRadius + 8f,
                center = center
            )
            drawCircle(
                color = primaryColor,
                radius = baseRadius,
                center = center,
                style = Stroke(width = 2.5f)
            )
        }

        // Radiant Sun Face Icon
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFF7ED),
            border = BorderStroke(2.dp, primaryColor),
            modifier = Modifier.size(68.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "☀️",
                    fontSize = 32.sp
                )
            }
        }
    }
}

/**
 * Onboarding Slide 3: Vedic Kundli Yantra Mandala
 */
@Composable
fun AstrotalkYantraMandalaIllustration(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFFF97316),
    accentColor: Color = Color(0xFFEAB308)
) {
    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width * 0.42f

            // Outer Vedic square enclosure (Bhupura)
            drawRect(
                color = primaryColor.copy(alpha = 0.3f),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 1.8f)
            )

            // Rotated inner diamond
            val diamondPath = Path().apply {
                moveTo(center.x, center.y - radius)
                lineTo(center.x + radius, center.y)
                lineTo(center.x, center.y + radius)
                lineTo(center.x - radius, center.y)
                close()
            }
            drawPath(
                path = diamondPath,
                color = accentColor.copy(alpha = 0.35f),
                style = Stroke(width = 1.8f)
            )

            // Inner Star of David / Shatkona (interlocking triangles)
            val triRadius = radius * 0.72f
            val tri1 = Path().apply {
                moveTo(center.x, center.y - triRadius)
                lineTo(center.x + triRadius * 0.866f, center.y + triRadius * 0.5f)
                lineTo(center.x - triRadius * 0.866f, center.y + triRadius * 0.5f)
                close()
            }
            val tri2 = Path().apply {
                moveTo(center.x, center.y + triRadius)
                lineTo(center.x + triRadius * 0.866f, center.y - triRadius * 0.5f)
                lineTo(center.x - triRadius * 0.866f, center.y - triRadius * 0.5f)
                close()
            }
            drawPath(path = tri1, color = primaryColor, style = Stroke(width = 2f))
            drawPath(path = tri2, color = primaryColor, style = Stroke(width = 2f))

            // Central circle
            drawCircle(
                color = accentColor.copy(alpha = 0.25f),
                radius = radius * 0.30f,
                center = center
            )
        }

        // Center Bindu symbol
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFF7ED),
            border = BorderStroke(2.dp, primaryColor),
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "🕉️",
                    fontSize = 24.sp,
                    color = primaryColor
                )
            }
        }
    }
}

// =============================================================================
// 2. DATA MODELS FOR ASTROTALK ONBOARDING & SETUP
// =============================================================================

data class LanguageOption(
    val code: String,
    val nameEn: String,
    val nativeScript: String
)

val ASTROTALK_LANGUAGES = listOf(
    LanguageOption("en", "English", "English"),
    LanguageOption("hi", "Hindi", "हिन्दी"),
    LanguageOption("te", "Telugu", "తెలుగు"),
    LanguageOption("ta", "Tamil", "தமிழ்"),
    LanguageOption("kn", "Kannada", "ಕನ್ನಡ"),
    LanguageOption("mr", "Marathi", "मराठी"),
    LanguageOption("bn", "Bengali", "বাংলা"),
    LanguageOption("gu", "Gujarati", "ગુજરાતી"),
    LanguageOption("pa", "Punjabi", "ਪੰਜਾਬੀ")
)

data class ZodiacOption(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val symbol: String,
    val dates: String
)

val ASTROTALK_ZODIAC_SIGNS = listOf(
    ZodiacOption("aries", "Aries", "मेष", "♈", "Mar 21 - Apr 19"),
    ZodiacOption("taurus", "Taurus", "वृषभ", "♉", "Apr 20 - May 20"),
    ZodiacOption("gemini", "Gemini", "मिथुन", "♊", "May 21 - Jun 20"),
    ZodiacOption("cancer", "Cancer", "कर्क", "♋", "Jun 21 - Jul 22"),
    ZodiacOption("leo", "Leo", "सिंह", "♌", "Jul 23 - Aug 22"),
    ZodiacOption("virgo", "Virgo", "कन्या", "♍", "Aug 23 - Sep 22"),
    ZodiacOption("libra", "Libra", "तुला", "♎", "Sep 23 - Oct 22"),
    ZodiacOption("scorpio", "Scorpio", "वृश्चिक", "♏", "Oct 23 - Nov 21"),
    ZodiacOption("sagittarius", "Sagittarius", "धनु", "♐", "Nov 22 - Dec 21"),
    ZodiacOption("capricorn", "Capricorn", "मकर", "♑", "Dec 22 - Jan 19"),
    ZodiacOption("aquarius", "Aquarius", "कुंभ", "♒", "Jan 20 - Feb 18"),
    ZodiacOption("pisces", "Pisces", "मीन", "♓", "Feb 19 - Mar 20")
)
