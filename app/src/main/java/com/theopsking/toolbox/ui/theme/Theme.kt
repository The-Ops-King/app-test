package com.theopsking.toolbox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.theopsking.toolbox.R

/** Colors from jtylerray.com (the default "blueprint" style). The app is dark only, like the site. */
object Brand {
    val Ink = Color(0xFF0C0B0A)
    val Raised = Color(0xFF141312)
    val Raised2 = Color(0xFF1C1B19)
    val Bone = Color(0xFFF5F3EE)
    val GreyMid = Color(0xFFA4A098)
    val Accent = Color(0xFF3F7D5C)
    val AccentHover = Color(0xFF4A8E6A)
    val AccentText = Color(0xFF5E9E78)
    val AccentSoft = Color(0x293F7D5C)
    /** rgba(138, 133, 124, .12): the site's hairline borders. */
    val Hair = Color(0x1F8A857C)
    val HairStrong = Color(0x3D8A857C)
}

private fun inter(weight: Int) = Font(
    R.font.inter,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun mono(weight: Int) = Font(
    R.font.jetbrains_mono,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Inter = FontFamily(inter(400), inter(500), inter(600), inter(700))
val Mono = FontFamily(mono(400), mono(500), mono(700))

/** Small uppercase mono label, like the site's eyebrows ("SALES OPS | FRACTIONAL COO/CTO"). */
val Eyebrow = TextStyle(fontFamily = Mono, fontSize = 12.sp, letterSpacing = 0.14.em, fontWeight = FontWeight(500))

private val base = Typography()
private val typography = Typography(
    displaySmall = base.displaySmall.copy(fontFamily = Inter, fontWeight = FontWeight(500), letterSpacing = (-0.032).em),
    headlineLarge = base.headlineLarge.copy(fontFamily = Inter, fontWeight = FontWeight(500), letterSpacing = (-0.032).em),
    headlineMedium = base.headlineMedium.copy(fontFamily = Inter, fontWeight = FontWeight(500), letterSpacing = (-0.02).em),
    headlineSmall = base.headlineSmall.copy(fontFamily = Inter, fontWeight = FontWeight(500), letterSpacing = (-0.02).em),
    titleLarge = base.titleLarge.copy(fontFamily = Inter, fontWeight = FontWeight(500)),
    titleMedium = base.titleMedium.copy(fontFamily = Inter, fontWeight = FontWeight(500)),
    titleSmall = base.titleSmall.copy(fontFamily = Inter),
    bodyLarge = base.bodyLarge.copy(fontFamily = Inter),
    bodyMedium = base.bodyMedium.copy(fontFamily = Inter),
    bodySmall = base.bodySmall.copy(fontFamily = Inter),
    labelLarge = base.labelLarge.copy(fontFamily = Inter, fontWeight = FontWeight(500)),
    labelMedium = base.labelMedium.copy(fontFamily = Mono),
    labelSmall = base.labelSmall.copy(fontFamily = Mono),
)

private val colors = darkColorScheme(
    primary = Brand.Accent,
    onPrimary = Brand.Bone,
    primaryContainer = Brand.AccentSoft,
    onPrimaryContainer = Brand.Bone,
    secondary = Brand.AccentText,
    onSecondary = Brand.Ink,
    secondaryContainer = Brand.Raised2,
    onSecondaryContainer = Brand.Bone,
    tertiary = Brand.AccentText,
    background = Brand.Ink,
    onBackground = Brand.Bone,
    surface = Brand.Ink,
    onSurface = Brand.Bone,
    surfaceVariant = Brand.Raised,
    onSurfaceVariant = Brand.GreyMid,
    surfaceContainerLowest = Brand.Ink,
    surfaceContainerLow = Brand.Raised,
    surfaceContainer = Brand.Raised,
    surfaceContainerHigh = Brand.Raised2,
    surfaceContainerHighest = Brand.Raised2,
    outline = Brand.HairStrong,
    outlineVariant = Brand.Hair,
    error = Color(0xFFE07A6B),
)

@Composable
fun ToolboxTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
