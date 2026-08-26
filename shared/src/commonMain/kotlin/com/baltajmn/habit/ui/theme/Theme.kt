package com.baltajmn.habit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** The eight accents a habit can pick. Muted pastels so a full grid never shouts. */
val HabitPalette = listOf(
    0xFFF0AFBE, // rose
    0xFFF5C39B, // peach
    0xFFEDDC98, // butter
    0xFFB6D6AB, // sage
    0xFF9CD3C7, // mint
    0xFFA2C3E9, // sky
    0xFFB4B8EC, // periwinkle
    0xFFD9AFE6, // lilac
)

private val Light = lightColorScheme(
    primary = Color(0xFF6FAE9B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9EDE5),
    onPrimaryContainer = Color(0xFF23453B),
    secondary = Color(0xFFC2A8D4),
    background = Color(0xFFFBF8F3),
    onBackground = Color(0xFF39352E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF39352E),
    surfaceVariant = Color(0xFFF0EBE2),
    onSurfaceVariant = Color(0xFF8B8479),
    outline = Color(0xFFE3DCD1),
    outlineVariant = Color(0xFFEFE9DF),
    error = Color(0xFFD98C8C),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF8FC9B6),
    onPrimary = Color(0xFF12271F),
    primaryContainer = Color(0xFF2B4A40),
    onPrimaryContainer = Color(0xFFD9EDE5),
    secondary = Color(0xFFC7B2D8),
    background = Color(0xFF17150F),
    onBackground = Color(0xFFECE5D9),
    surface = Color(0xFF201D16),
    onSurface = Color(0xFFECE5D9),
    surfaceVariant = Color(0xFF2C2820),
    onSurfaceVariant = Color(0xFF9C9486),
    outline = Color(0xFF3A352B),
    outlineVariant = Color(0xFF2C2820),
    error = Color(0xFFE0A0A0),
)

private val SoftShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun HabitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) Dark else Light,
        shapes = SoftShapes,
        content = content,
    )
}
