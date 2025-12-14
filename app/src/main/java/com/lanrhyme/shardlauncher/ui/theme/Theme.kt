package com.lanrhyme.shardlauncher.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

enum class ThemeColor(val title: String) {
    Green("草碎影"),
    Blue("蓝璃梦"),
    Purple("紫晶泪"),
    Golden("黄粱残"),
    Dynamic("动态（Android 12+）"),
    Custom("自定义"),
}

data class ColorSet(
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme
)

// Helper function to linearly interpolate between two colors
private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    val r = start.red + fraction * (stop.red - start.red)
    val g = start.green + fraction * (stop.green - start.green)
    val b = start.blue + fraction * (stop.blue - start.blue)
    val a = start.alpha + fraction * (stop.alpha - start.alpha)
    return Color(red = r, green = g, blue = b, alpha = a)
}

/**
 * Generates a light and dark color scheme from a given primary color.
 * This is a simplified approach. For more advanced theming, you might use a
 * library that can generate full tonal palettes.
 */
fun generateColorSetFromPrimary(primaryColor: Color): ColorSet {
    // Light Theme Colors
    val lightSecondary = lerp(primaryColor, Color(0xFF888888), 0.2f) // Desaturate for secondary
    val lightTertiary = lerp(primaryColor, Color(0xFF888888), 0.4f)  // Desaturate more for tertiary

    // Dark Theme Colors (generally lighter and less saturated than their light theme counterparts)
    val darkPrimary = lerp(primaryColor, Color.White, 0.3f)
    val darkSecondary = lerp(darkPrimary, Color.White, 0.2f)
    val darkTertiary = lerp(darkPrimary, Color.White, 0.4f)


    val lightScheme = lightColorScheme(
        primary = primaryColor,
        secondary = lightSecondary,
        tertiary = lightTertiary,
        background = blue_backgroundLight,
        surface = blue_surfaceLight,
        error = blue_errorLight,
        errorContainer = blue_errorContainerLight,
        onErrorContainer = blue_onErrorContainerLight,
        surfaceVariant = blue_surfaceVariantLight
    )

    val darkScheme = darkColorScheme(
        primary = darkPrimary,
        secondary = darkSecondary,
        tertiary = darkTertiary,
        background = blue_backgroundDark,
        surface = blue_surfaceDark,
        error = blue_errorDark,
        errorContainer = blue_errorContainerDark,
        onErrorContainer = blue_onErrorContainerDark,
        surfaceVariant = blue_surfaceVariantDark
    )

    return ColorSet(lightScheme, darkScheme)
}


object ColorPalettes {
    val Green = ColorSet(
        lightColorScheme = lightColorScheme(
            primary = green_primaryLight,
            onPrimary = green_onPrimaryLight,
            primaryContainer = green_primaryContainerLight,
            onPrimaryContainer = green_onPrimaryContainerLight,
            secondary = green_secondaryLight,
            onSecondary = green_onSecondaryLight,
            secondaryContainer = green_secondaryContainerLight,
            onSecondaryContainer = green_onSecondaryContainerLight,
            tertiary = green_tertiaryLight,
            onTertiary = green_onTertiaryLight,
            tertiaryContainer = green_tertiaryContainerLight,
            onTertiaryContainer = green_onTertiaryContainerLight,
            error = green_errorLight,
            onError = green_onErrorLight,
            errorContainer = green_errorContainerLight,
            onErrorContainer = green_onErrorContainerLight,
            background = green_backgroundLight,
            onBackground = green_onBackgroundLight,
            surface = green_surfaceLight,
            onSurface = green_onSurfaceLight,
            surfaceVariant = green_surfaceVariantLight,
            onSurfaceVariant = green_onSurfaceVariantLight,
            outline = green_outlineLight,
            outlineVariant = green_outlineVariantLight,
            scrim = green_scrimLight,
            inverseSurface = green_inverseSurfaceLight,
            inverseOnSurface = green_inverseOnSurfaceLight,
            inversePrimary = green_inversePrimaryLight,
            surfaceDim = green_surfaceDimLight,
            surfaceBright = green_surfaceBrightLight,
            surfaceContainerLowest = green_surfaceContainerLowestLight,
            surfaceContainerLow = green_surfaceContainerLowLight,
            surfaceContainer = green_surfaceContainerLight,
            surfaceContainerHigh = green_surfaceContainerHighLight,
            surfaceContainerHighest = green_surfaceContainerHighestLight,
        ),
        darkColorScheme = darkColorScheme(
            primary = green_primaryDark,
            onPrimary = green_onPrimaryDark,
            primaryContainer = green_primaryContainerDark,
            onPrimaryContainer = green_onPrimaryContainerDark,
            secondary = green_secondaryDark,
            onSecondary = green_onSecondaryDark,
            secondaryContainer = green_secondaryContainerDark,
            onSecondaryContainer = green_onSecondaryContainerDark,
            tertiary = green_tertiaryDark,
            onTertiary = green_onTertiaryDark,
            tertiaryContainer = green_tertiaryContainerDark,
            onTertiaryContainer = green_onTertiaryContainerDark,
            error = green_errorDark,
            onError = green_onErrorDark,
            errorContainer = green_errorContainerDark,
            onErrorContainer = green_onErrorContainerDark,
            background = green_backgroundDark,
            onBackground = green_onBackgroundDark,
            surface = green_surfaceDark,
            onSurface = green_onSurfaceDark,
            surfaceVariant = green_surfaceVariantDark,
            onSurfaceVariant = green_onSurfaceVariantDark,
            outline = green_outlineDark,
            outlineVariant = green_outlineVariantDark,
            scrim = green_scrimDark,
            inverseSurface = green_inverseSurfaceDark,
            inverseOnSurface = green_inverseOnSurfaceDark,
            inversePrimary = green_inversePrimaryDark,
            surfaceDim = green_surfaceDimDark,
            surfaceBright = green_surfaceBrightDark,
            surfaceContainerLowest = green_surfaceContainerLowestDark,
            surfaceContainerLow = green_surfaceContainerLowDark,
            surfaceContainer = green_surfaceContainerDark,
            surfaceContainerHigh = green_surfaceContainerHighDark,
            surfaceContainerHighest = green_surfaceContainerHighestDark,
        )
    )

    val Blue = ColorSet(
        lightColorScheme = lightColorScheme(
            primary = blue_primaryLight,
            onPrimary = blue_onPrimaryLight,
            primaryContainer = blue_primaryContainerLight,
            onPrimaryContainer = blue_onPrimaryContainerLight,
            secondary = blue_secondaryLight,
            onSecondary = blue_onSecondaryLight,
            secondaryContainer = blue_secondaryContainerLight,
            onSecondaryContainer = blue_onSecondaryContainerLight,
            tertiary = blue_tertiaryLight,
            onTertiary = blue_onTertiaryLight,
            tertiaryContainer = blue_tertiaryContainerLight,
            onTertiaryContainer = blue_onTertiaryContainerLight,
            error = blue_errorLight,
            onError = blue_onErrorLight,
            errorContainer = blue_errorContainerLight,
            onErrorContainer = blue_onErrorContainerLight,
            background = blue_backgroundLight,
            onBackground = blue_onBackgroundLight,
            surface = blue_surfaceLight,
            onSurface = blue_onSurfaceLight,
            surfaceVariant = blue_surfaceVariantLight,
            onSurfaceVariant = blue_onSurfaceVariantLight,
            outline = blue_outlineLight,
            outlineVariant = blue_outlineVariantLight,
            scrim = blue_scrimLight,
            inverseSurface = blue_inverseSurfaceLight,
            inverseOnSurface = blue_inverseOnSurfaceLight,
            inversePrimary = blue_inversePrimaryLight,
            surfaceDim = blue_surfaceDimLight,
            surfaceBright = blue_surfaceBrightLight,
            surfaceContainerLowest = blue_surfaceContainerLowestLight,
            surfaceContainerLow = blue_surfaceContainerLowLight,
            surfaceContainer = blue_surfaceContainerLight,
            surfaceContainerHigh = blue_surfaceContainerHighLight,
            surfaceContainerHighest = blue_surfaceContainerHighestLight,
        ),
        darkColorScheme = darkColorScheme(
            primary = blue_primaryDark,
            onPrimary = blue_onPrimaryDark,
            primaryContainer = blue_primaryContainerDark,
            onPrimaryContainer = blue_onPrimaryContainerDark,
            secondary = blue_secondaryDark,
            onSecondary = blue_onSecondaryDark,
            secondaryContainer = blue_secondaryContainerDark,
            onSecondaryContainer = blue_onSecondaryContainerDark,
            tertiary = blue_tertiaryDark,
            onTertiary = blue_onTertiaryDark,
            tertiaryContainer = blue_tertiaryContainerDark,
            onTertiaryContainer = blue_onTertiaryContainerDark,
            error = blue_errorDark,
            onError = blue_onErrorDark,
            errorContainer = blue_errorContainerDark,
            onErrorContainer = blue_onErrorContainerDark,
            background = blue_backgroundDark,
            onBackground = blue_onBackgroundDark,
            surface = blue_surfaceDark,
            onSurface = blue_onSurfaceDark,
            surfaceVariant = blue_surfaceVariantDark,
            onSurfaceVariant = blue_onSurfaceVariantDark,
            outline = blue_outlineDark,
            outlineVariant = blue_outlineVariantDark,
            scrim = blue_scrimDark,
            inverseSurface = blue_inverseSurfaceDark,
            inverseOnSurface = blue_inverseOnSurfaceDark,
            inversePrimary = blue_inversePrimaryDark,
            surfaceDim = blue_surfaceDimDark,
            surfaceBright = blue_surfaceBrightDark,
            surfaceContainerLowest = blue_surfaceContainerLowestDark,
            surfaceContainerLow = blue_surfaceContainerLowDark,
            surfaceContainer = blue_surfaceContainerDark,
            surfaceContainerHigh = blue_surfaceContainerHighDark,
            surfaceContainerHighest = blue_surfaceContainerHighestDark,
        )
    )

    val Purple = ColorSet(
        lightColorScheme = lightColorScheme(
            primary = purple_primaryLight,
            onPrimary = purple_onPrimaryLight,
            primaryContainer = purple_primaryContainerLight,
            onPrimaryContainer = purple_onPrimaryContainerLight,
            secondary = purple_secondaryLight,
            onSecondary = purple_onSecondaryLight,
            secondaryContainer = purple_secondaryContainerLight,
            onSecondaryContainer = purple_onSecondaryContainerLight,
            tertiary = purple_tertiaryLight,
            onTertiary = purple_onTertiaryLight,
            tertiaryContainer = purple_tertiaryContainerLight,
            onTertiaryContainer = purple_onTertiaryContainerLight,
            error = purple_errorLight,
            onError = purple_onErrorLight,
            errorContainer = purple_errorContainerLight,
            onErrorContainer = purple_onErrorContainerLight,
            background = purple_backgroundLight,
            onBackground = purple_onBackgroundLight,
            surface = purple_surfaceLight,
            onSurface = purple_onSurfaceLight,
            surfaceVariant = purple_surfaceVariantLight,
            onSurfaceVariant = purple_onSurfaceVariantLight,
            outline = purple_outlineLight,
            outlineVariant = purple_outlineVariantLight,
            scrim = purple_scrimLight,
            inverseSurface = purple_inverseSurfaceLight,
            inverseOnSurface = purple_inverseOnSurfaceLight,
            inversePrimary = purple_inversePrimaryLight,
            surfaceDim = purple_surfaceDimLight,
            surfaceBright = purple_surfaceBrightLight,
            surfaceContainerLowest = purple_surfaceContainerLowestLight,
            surfaceContainerLow = purple_surfaceContainerLowLight,
            surfaceContainer = purple_surfaceContainerLight,
            surfaceContainerHigh = purple_surfaceContainerHighLight,
            surfaceContainerHighest = purple_surfaceContainerHighestLight,
        ),
        darkColorScheme = darkColorScheme(
            primary = purple_primaryDark,
            onPrimary = purple_onPrimaryDark,
            primaryContainer = purple_primaryContainerDark,
            onPrimaryContainer = purple_onPrimaryContainerDark,
            secondary = purple_secondaryDark,
            onSecondary = purple_onSecondaryDark,
            secondaryContainer = purple_secondaryContainerDark,
            onSecondaryContainer = purple_onSecondaryContainerDark,
            tertiary = purple_tertiaryDark,
            onTertiary = purple_onTertiaryDark,
            tertiaryContainer = purple_tertiaryContainerDark,
            onTertiaryContainer = purple_onTertiaryContainerDark,
            error = purple_errorDark,
            onError = purple_onErrorDark,
            errorContainer = purple_errorContainerDark,
            onErrorContainer = purple_onErrorContainerDark,
            background = purple_backgroundDark,
            onBackground = purple_onBackgroundDark,
            surface = purple_surfaceDark,
            onSurface = purple_onSurfaceDark,
            surfaceVariant = purple_surfaceVariantDark,
            onSurfaceVariant = purple_onSurfaceVariantDark,
            outline = purple_outlineDark,
            outlineVariant = purple_outlineVariantDark,
            scrim = purple_scrimDark,
            inverseSurface = purple_inverseSurfaceDark,
            inverseOnSurface = purple_inverseOnSurfaceDark,
            inversePrimary = purple_inversePrimaryDark,
            surfaceDim = purple_surfaceDimDark,
            surfaceBright = purple_surfaceBrightDark,
            surfaceContainerLowest = purple_surfaceContainerLowestDark,
            surfaceContainerLow = purple_surfaceContainerLowDark,
            surfaceContainer = purple_surfaceContainerDark,
            surfaceContainerHigh = purple_surfaceContainerHighDark,
            surfaceContainerHighest = purple_surfaceContainerHighestDark,
        )
    )

    val Golden = ColorSet(
        lightColorScheme = lightColorScheme(
            primary = yellow_primaryLight,
            onPrimary = yellow_onPrimaryLight,
            primaryContainer = yellow_primaryContainerLight,
            onPrimaryContainer = yellow_onPrimaryContainerLight,
            secondary = yellow_secondaryLight,
            onSecondary = yellow_onSecondaryLight,
            secondaryContainer = yellow_secondaryContainerLight,
            onSecondaryContainer = yellow_onSecondaryContainerLight,
            tertiary = yellow_tertiaryLight,
            onTertiary = yellow_onTertiaryLight,
            tertiaryContainer = yellow_tertiaryContainerLight,
            onTertiaryContainer = yellow_onTertiaryContainerLight,
            error = yellow_errorLight,
            onError = yellow_onErrorLight,
            errorContainer = yellow_errorContainerLight,
            onErrorContainer = yellow_onErrorContainerLight,
            background = yellow_backgroundLight,
            onBackground = yellow_onBackgroundLight,
            surface = yellow_surfaceLight,
            onSurface = yellow_onSurfaceLight,
            surfaceVariant = yellow_surfaceVariantLight,
            onSurfaceVariant = yellow_onSurfaceVariantLight,
            outline = yellow_outlineLight,
            outlineVariant = yellow_outlineVariantLight,
            scrim = yellow_scrimLight,
            inverseSurface = yellow_inverseSurfaceLight,
            inverseOnSurface = yellow_inverseOnSurfaceLight,
            inversePrimary = yellow_inversePrimaryLight,
            surfaceDim = yellow_surfaceDimLight,
            surfaceBright = yellow_surfaceBrightLight,
            surfaceContainerLowest = yellow_surfaceContainerLowestLight,
            surfaceContainerLow = yellow_surfaceContainerLowLight,
            surfaceContainer = yellow_surfaceContainerLight,
            surfaceContainerHigh = yellow_surfaceContainerHighLight,
            surfaceContainerHighest = yellow_surfaceContainerHighestLight,
        ),
        darkColorScheme = darkColorScheme(
            primary = yellow_primaryDark,
            onPrimary = yellow_onPrimaryDark,
            primaryContainer = yellow_primaryContainerDark,
            onPrimaryContainer = yellow_onPrimaryContainerDark,
            secondary = yellow_secondaryDark,
            onSecondary = yellow_onSecondaryDark,
            secondaryContainer = yellow_secondaryContainerDark,
            onSecondaryContainer = yellow_onSecondaryContainerDark,
            tertiary = yellow_tertiaryDark,
            onTertiary = yellow_onTertiaryDark,
            tertiaryContainer = yellow_tertiaryContainerDark,
            onTertiaryContainer = yellow_onTertiaryContainerDark,
            error = yellow_errorDark,
            onError = yellow_onErrorDark,
            errorContainer = yellow_errorContainerDark,
            onErrorContainer = yellow_onErrorContainerDark,
            background = yellow_backgroundDark,
            onBackground = yellow_onBackgroundDark,
            surface = yellow_surfaceDark,
            onSurface = yellow_onSurfaceDark,
            surfaceVariant = yellow_surfaceVariantDark,
            onSurfaceVariant = yellow_onSurfaceVariantDark,
            outline = yellow_outlineDark,
            outlineVariant = yellow_outlineVariantDark,
            scrim = yellow_scrimDark,
            inverseSurface = yellow_inverseSurfaceDark,
            inverseOnSurface = yellow_inverseOnSurfaceDark,
            inversePrimary = yellow_inversePrimaryDark,
            surfaceDim = yellow_surfaceDimDark,
            surfaceBright = yellow_surfaceBrightDark,
            surfaceContainerLowest = yellow_surfaceContainerLowestDark,
            surfaceContainerLow = yellow_surfaceContainerLowDark,
            surfaceContainer = yellow_surfaceContainerDark,
            surfaceContainerHigh = yellow_surfaceContainerHighDark,
            surfaceContainerHighest = yellow_surfaceContainerHighestDark,
        )
    )
}

@Composable
fun ShardLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: ThemeColor = ThemeColor.Green, // Default to Green
    lightColorScheme: ColorScheme? = null,
    darkColorScheme: ColorScheme? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        themeColor == ThemeColor.Dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context).copy(
                    surfaceVariant = blue_surfaceVariantDark,
                    background = blue_backgroundDark
                )
            } else {
                dynamicLightColorScheme(context).copy(
                    surface = blue_surfaceLight,
                    surfaceVariant = blue_surfaceVariantLight,
                    background = blue_backgroundLight
                )
            }
        }
        themeColor == ThemeColor.Custom && lightColorScheme != null && darkColorScheme != null -> {
            if (darkTheme) darkColorScheme else lightColorScheme
        }
        else -> {
            val palette = when (themeColor) {
                ThemeColor.Blue -> ColorPalettes.Blue
                ThemeColor.Purple -> ColorPalettes.Purple
                ThemeColor.Golden -> ColorPalettes.Golden
                else -> ColorPalettes.Green // Green, Dynamic fallback, Custom fallback
            }
            if(darkTheme) palette.darkColorScheme else palette.lightColorScheme
        }
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
