package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PaperBg
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Ultra-Modern Interactive 3D Gift & Scratch Coupon Card (Cred & GPay Inspired).
 * Features live interactive "Tap to Scratch / Reveal" mode, 3D Celebration Particles,
 * Shimmering Holographic Gold Foil, Instant Copy Pill, and Claim Animation.
 */
@Composable
fun FestivalPromoCouponCard(
    isHindi: Boolean,
    modifier: Modifier = Modifier,
    onClaimOffer: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRevealed by remember { mutableStateOf(true) } // default revealed or can be scratched
    var isCopied by remember { mutableStateOf(false) }
    var isClaimed by remember { mutableStateOf(false) }
    val couponCode = "DEV100"

    // Continuous Shimmer Glow Animation
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    fun copyCoupon() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Devbhasha Coupon", couponCode)
        clipboard?.setPrimaryClip(clip)
        isCopied = true
        val msg = if (isHindi) "✨ कूपन कोड '$couponCode' कॉपी हो गया!" else "✨ Coupon code '$couponCode' copied!"
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        coroutineScope.launch {
            delay(2200)
            isCopied = false
        }
    }

    // Radiant Solar Sunset & Saffron Gradient
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF5E36), // Vibrant Coral Saffron
            Color(0xFFFFA000), // Rich Golden Amber
            Color(0xFFFFD54F), // Sunburst Yellow
            Color(0xFFFF6D00)  // Deep Sacred Orange
        ),
        start = Offset(0f, 0f),
        end = Offset(800f, 600f)
    )

    // Holographic Foil Border
    val foilBorder = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFF9C4),
            Color(0xFFFFD54F),
            Color(0xFFFFAB00),
            Color(0xFFFFF59D)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(14.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFFFF6D00))
            .background(cardGradient, RoundedCornerShape(24.dp))
            .border(BorderStroke(1.5.dp, foilBorder), RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .drawBehind {
                // Moving Holographic Shimmer Beam
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        start = Offset(shimmerOffset, 0f),
                        end = Offset(shimmerOffset + 200f, size.height)
                    ),
                    start = Offset(shimmerOffset, 0f),
                    end = Offset(shimmerOffset + 200f, size.height),
                    strokeWidth = 140f
                )

                // Background Sacred Geometry Watermark
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.width * 0.45f,
                    center = Offset(size.width * 0.90f, size.height * 0.20f)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Row: Animated Gift Ribbon & Live Availability Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shimmering Golden Badge
                Surface(
                    color = Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "🎁",
                            fontSize = 13.sp,
                            modifier = Modifier.scale(pulseScale)
                        )
                        Text(
                            text = if (isHindi) "प्रथम परामर्श विशेष उपहार" else "WELCOME GIFT • 100% OFF",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp
                        )
                    }
                }

                // Status Pill
                Surface(
                    color = if (isClaimed) Color(0xFF1B5E20) else Color.Black.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(0.8.dp, if (isClaimed) Color(0xFF81C784) else Color(0xFFFFD54F).copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (isClaimed) Color(0xFF69F0AE) else Color(0xFFFFD54F), CircleShape)
                        )
                        Text(
                            text = "⚡ 5 MIN FREE*",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Hero Row: 3D Hologram Badge + Value Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shiny 3D Gift Disc
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFFF8F00))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🪔",
                            fontSize = 28.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Headline & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "100% MUFT",
                            color = Color(0xFFFFF8E1),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF7A1B0C)
                        ) {
                            Text(
                                text = "FREE",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isHindi) "पहली स्वप्न अर्थ चैट 5 मिनट निःशुल्क (प्रति फ़ोन नंबर एक बार)" else "First dream chat free for 5 min (once per phone number)",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 17.sp
                    )
                    Text(
                        text = if (isHindi) "कॉल या चैट पर बिना किसी शुल्क के तुरंत मार्गदर्शन" else "Talk to verified astrologers • No credit card needed",
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dashed Divider Line with Semicircle Cutouts
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
            ) {
                val midY = size.height / 2f
                val notchRadius = 7.dp.toPx()

                drawLine(
                    color = Color.White.copy(alpha = 0.45f),
                    start = Offset(notchRadius + 4f, midY),
                    end = Offset(size.width - notchRadius - 4f, midY),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Cutout notches
                drawCircle(color = PaperBg, radius = notchRadius, center = Offset(0f, midY))
                drawCircle(color = PaperBg, radius = notchRadius, center = Offset(size.width, midY))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row: Copyable Code Box & Large Gleaming Claim CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Copyable Code Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.28f),
                    border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { copyCoupon() }
                        .defaultMinSize(minHeight = 46.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isHindi) "कूपन कोड" else "COUPON",
                                color = Color(0xFFFFF9C4),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = couponCode,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.5.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Icon(
                            imageVector = if (isCopied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                            contentDescription = "Copy code",
                            tint = if (isCopied) Color(0xFF69F0AE) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Gleaming White & Gold Claim Button
                Button(
                    onClick = {
                        isClaimed = true
                        onClaimOffer()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isClaimed) Color(0xFF1B5E20) else Color.White,
                        contentColor = if (isClaimed) Color.White else Color(0xFFD84315)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 46.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isClaimed) Icons.Default.CheckCircle else Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (isClaimed) Color(0xFF81C784) else Color(0xFFFF6D00),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = when {
                                isClaimed -> if (isHindi) "पात्रता सत्र में जाँची जाएगी ✓" else "Eligibility checked at session start ✓"
                                isHindi -> "तुरंत क्लेम करें ⚡"
                                else -> "Claim Offer ⚡"
                            },
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
