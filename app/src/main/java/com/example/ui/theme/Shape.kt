package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ==========================================
// DEVBHASHA SHAPES & SPACING SYSTEM
// ==========================================

val DevbhashaCardShape = RoundedCornerShape(20.dp)
val DevbhashaButtonShape = RoundedCornerShape(14.dp)
val DevbhashaTextFieldShape = RoundedCornerShape(14.dp)
val DevbhashaChipShape = CircleShape // Fully rounded
val DevbhashaDialogShape = RoundedCornerShape(24.dp)

val DevbhashaShapes = Shapes(
    small = DevbhashaChipShape,
    medium = DevbhashaButtonShape,
    large = DevbhashaCardShape,
    extraLarge = DevbhashaDialogShape
)

// Legacy aliases
val ShapeCard = DevbhashaCardShape
val ShapeDialog = DevbhashaDialogShape
val ShapePill = CircleShape
val ShapeChip = DevbhashaChipShape

// Standard spacing tokens on a 4dp grid
object DevSpacing {
    val space2 = 2.dp
    val space4 = 4.dp
    val space8 = 8.dp
    val space12 = 12.dp // Spacing between cards
    val space16 = 16.dp // Screen padding
    val space20 = 20.dp
    val space24 = 24.dp
    val space32 = 32.dp
    val space40 = 40.dp
    val space48 = 48.dp
    val buttonHeight = 52.dp // Primary button height
}
