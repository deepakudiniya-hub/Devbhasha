package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Standardized Spacing Scale for DevBhasha
 */
object DevSpacing {
    val micro = 4.dp
    val small = 8.dp
    val compact = 12.dp
    val standard = 16.dp
    val comfortable = 20.dp
    val section = 24.dp
    val majorSection = 32.dp
}

/**
 * Standardized Shapes & Radii for DevBhasha (Instagram-minimal: 24px card radius, 999px pill radius)
 */
object DevRadius {
    val micro = 4.dp
    val compact = 8.dp
    val card = 24.dp
    val cardLarge = 24.dp
    val sheet = 24.dp
    val pill = 999.dp
}

/**
 * Editorial Section Header with optional action link (e.g. "सभी देखें →")
 */
@Composable
fun DevSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = DevSpacing.compact),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = title,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = InkSoft,
                    lineHeight = 16.sp
                )
            }
        }

        if (!actionText.isNullOrBlank() && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = DevSpacing.compact, vertical = DevSpacing.micro)
            ) {
                Text(
                    text = actionText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Terra
                )
            }
        }
    }
}

/**
 * Primary Brand Button in Saffron Gradient with touch target >= 48dp, white text, and shadow
 */
@Composable
fun DevPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    containerColor: Color = Saffron,
    contentColor: Color = Color.White
) {
    val saffronBrush = Brush.horizontalGradient(
        listOf(SaffronGradientStart, SaffronGradientEnd)
    )
    Surface(
        shape = RoundedCornerShape(DevRadius.pill),
        color = if (enabled && !isLoading) Color.Transparent else containerColor.copy(alpha = 0.45f),
        shadowElevation = if (enabled && !isLoading) 4.dp else 0.dp,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(DevRadius.pill))
            .then(
                if (enabled && !isLoading) Modifier.background(saffronBrush)
                else Modifier.background(containerColor.copy(alpha = 0.45f))
            )
            .clickable(enabled = enabled && !isLoading) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = DevSpacing.comfortable, vertical = DevSpacing.compact + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = contentColor
                )
                Spacer(modifier = Modifier.width(DevSpacing.small))
            } else if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(DevSpacing.small))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

/**
 * Secondary Button: pure white bg + black border (Instagram Minimal)
 */
@Composable
fun DevSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    borderColor: Color = Color(0xFF000000)
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(DevRadius.pill),
        border = BorderStroke(1.dp, if (enabled) borderColor else BorderLight),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF000000),
            disabledContainerColor = Color.White.copy(alpha = 0.5f),
            disabledContentColor = TextTertiary
        ),
        contentPadding = PaddingValues(horizontal = DevSpacing.comfortable, vertical = DevSpacing.compact),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (enabled) Color(0xFF000000) else TextTertiary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(DevSpacing.small))
        }
        Text(
            text = text,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) Color(0xFF000000) else TextTertiary
        )
    }
}

/**
 * Standard Surface Card for DevBhasha: pure white #FFFFFF with 1px border #EFEFEF and 24dp radius
 */
@Composable
fun DevCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = RoundedCornerShape(DevRadius.card),
    backgroundColor: Color = Color.White,
    borderColor: Color = BorderLight,
    elevation: Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = elevation,
        modifier = if (onClick != null) {
            modifier
                .clip(shape)
                .clickable { onClick() }
        } else {
            modifier
        }
    ) {
        Column(
            modifier = Modifier.padding(DevSpacing.comfortable),
            content = content
        )
    }
}

/**
 * Chip / Pill for Filters: Inactive = white bg + #EFEFEF border + #737373 text, Active = black bg + white text
 */
@Composable
fun DevPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    accentColor: Color = Color(0xFF000000)
) {
    Surface(
        shape = RoundedCornerShape(DevRadius.pill),
        color = if (isSelected) Color(0xFF000000) else Color.White,
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF000000) else BorderLight),
        shadowElevation = if (isSelected) 1.dp else 0.dp,
        modifier = modifier
            .clip(RoundedCornerShape(DevRadius.pill))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = DevSpacing.compact + 2.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DevSpacing.micro)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = text,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextSecondary
            )
        }
    }
}

/**
 * Icon Badge with a soft circular container
 */
@Composable
fun DevIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = Terra,
    background: Color = GoldStampSoft,
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp
) {
    Surface(
        shape = CircleShape,
        color = background,
        modifier = modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Subtle hairline divider adhering to editorial theme
 */
@Composable
fun DevDivider(
    modifier: Modifier = Modifier,
    color: Color = EditorialLine,
    thickness: Dp = 1.dp
) {
    HorizontalDivider(
        modifier = modifier,
        color = color,
        thickness = thickness
    )
}

/**
 * Standardized Empty State for lists, dreams, transactions, consultations
 */
@Composable
fun DevEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    symbol: String = "🪔",
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(DevRadius.cardLarge),
        color = PaperCard,
        border = BorderStroke(1.dp, EditorialLine),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = DevSpacing.compact)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = DevSpacing.section, vertical = DevSpacing.majorSection)
        ) {
            Surface(
                shape = CircleShape,
                color = GoldStampSoft,
                border = BorderStroke(1.dp, EditorialLine),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = symbol, fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.height(DevSpacing.standard))

            Text(
                text = title,
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(DevSpacing.micro + 2.dp))

            Text(
                text = description,
                fontSize = 12.5.sp,
                color = InkSoft,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            if (!actionText.isNullOrBlank() && onActionClick != null) {
                Spacer(modifier = Modifier.height(DevSpacing.standard))
                DevPrimaryButton(
                    text = actionText,
                    onClick = onActionClick,
                    modifier = Modifier.height(44.dp)
                )
            }
        }
    }
}

/**
 * Standardized Loading State
 */
@Composable
fun DevLoadingState(
    modifier: Modifier = Modifier,
    message: String = "कृपा प्रतीक्षा करें..."
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DevSpacing.section),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Terra,
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(DevSpacing.compact))
        Text(
            text = message,
            fontSize = 12.5.sp,
            color = InkSoft,
            fontWeight = FontWeight.Medium
        )
    }
}
