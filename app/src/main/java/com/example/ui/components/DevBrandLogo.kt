package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SaffronDeep
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.SaffronSoftBg

/**
 * Premium, spiritually refined Original Brand Logo component.
 * Uses the official #F97316 orange rounded squircle with white bold 'देव'.
 */
@Composable
fun DevLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    showGlow: Boolean = false,
    elevation: Dp = 2.dp,
    useDevanagariChar: Boolean = true
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.dev_logo),
            contentDescription = "लोगो - देव भाषा",
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Brand Lockup for App Bar & Top Headers (36x36 Icon + "देव भाषा" Title + Tagline)
 */
@Composable
fun DevBrandLockup(
    modifier: Modifier = Modifier,
    isHindi: Boolean = true,
    iconSize: Dp = 36.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DevLogoIcon(
            size = iconSize,
            elevation = 2.dp,
            showGlow = false
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = if (isHindi) "देव भाषा" else "Dev Bhasha",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2B2B2B),
                letterSpacing = 0.3.sp
            )
            Text(
                text = if (isHindi) "वैदिक पंचांग • स्वप्न विचार • परामर्श" else "Vedic Almanac & Guidance",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6E6E6E)
            )
        }
    }
}

/**
 * Small Watermark Logo for Share Cards (rashifal, muhurat, shlok)
 * Size: 24dp, 80% opacity in bottom corner
 */
@Composable
fun DevWatermarkLogo(
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.dev_logo),
            contentDescription = "देव भाषा",
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(5.dp))
        )
        Text(
            text = "देव भाषा",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B2B2B).copy(alpha = 0.80f)
        )
    }
}

/**
 * About / Heritage Brand Banner used in Profile & Settings screen
 */
@Composable
fun DevBrandBanner(
    isHindi: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DevLogoIcon(
                size = 64.dp,
                elevation = 8.dp,
                showGlow = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isHindi) "देव भाषा" else "Dev Bhasha",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isHindi) "सनातन ज्ञान, कर्मकांड एवं विश्वसनीय साधक सेवा"
                else "Sanatan Wisdom, Vedic Remedies & Verified Sadhak Guidance",
                fontSize = 12.5.sp,
                color = Color(0xFF64748B),
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SaffronSoftBg)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = SaffronPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (isHindi) "100% प्रमाणित वैदिक पद्धति" else "100% Verified Vedic Tradition",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SaffronDeep
                )
            }
        }
    }
}

/**
 * Compact animated 'देव' Logo Loading Indicator (for pull-to-refresh, cards, and transitions)
 */
@Composable
fun DevLogoLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dev_pulse")
    val scale = infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale.value),
        contentAlignment = Alignment.Center
    ) {
        DevLogoIcon(
            size = size,
            elevation = 2.dp,
            showGlow = false
        )
    }
}
