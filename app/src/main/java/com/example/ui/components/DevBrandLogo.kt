package com.example.ui.components

import com.example.ui.theme.*
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
 * Uses the authentic satvik vector drawable and pristine Devanagari 'भा' / Sacred Jyoti design.
 */
@Composable
fun DevLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    showGlow: Boolean = false,
    elevation: Dp = 2.dp,
    useDevanagariChar: Boolean = true
) {
    val cornerRadius = size * 0.24f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Main squircle container with crisp golden rim (no distracting glow)
        Surface(
            modifier = Modifier
                .size(size)
                .shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(cornerRadius)
                ),
            shape = RoundedCornerShape(cornerRadius),
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        SaffronGold,
                        Saffron
                    )
                )
            ),
            color = Color.Transparent
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_devbhasha_logo),
                contentDescription = "लोगो - सनातन आध्यात्मिक मंच",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Brand Lockup for App Bar & Top Headers (Icon + "भाषा" Title + Tagline)
 */
@Composable
fun DevBrandLockup(
    modifier: Modifier = Modifier,
    isHindi: Boolean = true,
    iconSize: Dp = 38.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DevLogoIcon(
            size = iconSize,
            elevation = 3.dp,
            showGlow = false
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isHindi) "भाषा" else "Bhasha",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = InkPrimary,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = if (isHindi) "वैदिक आध्यात्मिक मार्गदर्शन" else "Spiritual Guidance & Solutions",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Neutral500
            )
        }
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
                text = if (isHindi) "भाषा" else "Bhasha",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral800
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isHindi) "सनातन ज्ञान, कर्मकांड एवं विश्वसनीय साधक सेवा"
                else "Sanatan Wisdom, Vedic Remedies & Verified Sadhak Guidance",
                fontSize = 12.5.sp,
                color = Neutral500,
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
