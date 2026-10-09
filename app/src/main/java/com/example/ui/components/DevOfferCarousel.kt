package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Auto-rotating promotional offer banner for the Home screen.
 * Swipeable (HorizontalPager) + auto-advances every few seconds, with page dots.
 */
private data class OfferSlide(
    val headline: String,
    val sub: String,
    val cta: String,
    val start: Color,
    val end: Color
)

private fun offerSlides(isHindi: Boolean): List<OfferSlide> = if (isHindi) listOf(
    OfferSlide(
        "पहले रिचार्ज पर 100% कैशबैक",
        "वॉलेट में जोड़ें, तुरंत पाएं",
        "रिचार्ज करें",
        Color(0xFFE46228), Color(0xFFB83A0E)
    ),
    OfferSlide(
        "पहली सलाह पर 50% छूट",
        "सत्यापित साधक से अभी जुड़ें",
        "अभी जुड़ें",
        Color(0xFF4F46E5), Color(0xFF7C3AED)
    ),
    OfferSlide(
        "रोज़ का पंचांग — मुफ़्त",
        "आज का शुभ मुहूर्त देखें",
        "देखें",
        Color(0xFF0F766E), Color(0xFF15803D)
    )
) else listOf(
    OfferSlide(
        "100% cashback on first recharge",
        "Add to wallet, get it instantly",
        "Recharge",
        Color(0xFFE46228), Color(0xFFB83A0E)
    ),
    OfferSlide(
        "50% off your first consultation",
        "Connect with a verified sadhak",
        "Connect",
        Color(0xFF4F46E5), Color(0xFF7C3AED)
    ),
    OfferSlide(
        "Daily Panchang — free",
        "See today's shubh muhurat",
        "View",
        Color(0xFF0F766E), Color(0xFF15803D)
    )
)

@Composable
fun DevOfferCarousel(
    isHindi: Boolean = true,
    onCtaClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val slides = remember(isHindi) { offerSlides(isHindi) }
    val pagerState = rememberPagerState(pageCount = { slides.size })

    LaunchedEffect(pagerState, slides.size) {
        while (true) {
            delay(4000L)
            if (slides.isNotEmpty()) {
                val next = (pagerState.currentPage + 1) % slides.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = 20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) { page ->
            OfferBannerCard(
                slide = slides[page],
                onCtaClick = { onCtaClick(page) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(slides.size) { index ->
                val active = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(if (active) Color(0xFFE46228) else Color(0xFFD9D9D9))
                )
            }
        }
    }
}

@Composable
private fun OfferBannerCard(
    slide: OfferSlide,
    onCtaClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(slide.start, slide.end)))
    ) {
        // Decorative translucent coins / glow
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 34.dp, y = (-12).dp)
                .size(132.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 12.dp, y = 42.dp)
                .size(96.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
        Icon(
            imageVector = Icons.Filled.CurrencyRupee,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.26f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(18.dp)
                .size(46.dp)
        )
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.55f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 22.dp)
                .size(26.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.74f)
                .padding(start = 20.dp, end = 8.dp)
        ) {
            Text(
                text = slide.headline,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = slide.sub,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White)
                    .clickable { onCtaClick() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = slide.cta + "  \u2192",
                    color = slide.end,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
