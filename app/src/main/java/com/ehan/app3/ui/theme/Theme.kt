package com.ehan.app3.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode(
    val label: String
) {
    SYSTEM("System default"),
    DARK("Dark"),
    LIGHT("Light")
}

private fun getLightScheme(palette: AppPalette): ColorScheme = when (palette) {
    AppPalette.INDIGO -> lightColorScheme(
        primary = IndigoPrimaryLight,
        onPrimary = IndigoOnPrimaryLight,
        primaryContainer = IndigoPrimaryContainerLight,
        onPrimaryContainer = IndigoOnPrimaryContainerLight,
        secondary = IndigoSecondaryLight,
        onSecondary = IndigoOnSecondaryLight,
        secondaryContainer = IndigoSecondaryContainerLight,
        onSecondaryContainer = IndigoOnSecondaryContainerLight,
        tertiary = IndigoTertiaryLight,
        onTertiary = IndigoOnTertiaryLight,
        tertiaryContainer = IndigoTertiaryContainerLight,
        onTertiaryContainer = IndigoOnTertiaryContainerLight,
        background = IndigoBackgroundLight,
        onBackground = IndigoOnBackgroundLight,
        surface = IndigoSurfaceLight,
        onSurface = IndigoOnSurfaceLight,
        surfaceVariant = IndigoSurfaceVariantLight,
        onSurfaceVariant = IndigoOnSurfaceVariantLight,
        outline = IndigoOutlineLight,
        outlineVariant = IndigoOutlineVariantLight
    )
    AppPalette.EMERALD -> lightColorScheme(
        primary = EmeraldPrimaryLight,
        onPrimary = EmeraldOnPrimaryLight,
        primaryContainer = EmeraldPrimaryContainerLight,
        onPrimaryContainer = EmeraldOnPrimaryContainerLight,
        secondary = EmeraldSecondaryLight,
        onSecondary = EmeraldOnSecondaryLight,
        secondaryContainer = EmeraldSecondaryContainerLight,
        onSecondaryContainer = EmeraldOnSecondaryContainerLight,
        tertiary = EmeraldTertiaryLight,
        onTertiary = EmeraldOnTertiaryLight,
        tertiaryContainer = EmeraldTertiaryContainerLight,
        onTertiaryContainer = EmeraldOnTertiaryContainerLight,
        background = EmeraldBackgroundLight,
        onBackground = IndigoOnBackgroundLight,
        surface = IndigoSurfaceLight,
        onSurface = IndigoOnSurfaceLight,
        surfaceVariant = EmeraldSurfaceVariantLight,
        onSurfaceVariant = IndigoOnSurfaceVariantLight,
        outline = IndigoOutlineLight,
        outlineVariant = IndigoOutlineVariantLight
    )
    AppPalette.OCEAN -> lightColorScheme(
        primary = OceanPrimaryLight,
        onPrimary = OceanOnPrimaryLight,
        primaryContainer = OceanPrimaryContainerLight,
        onPrimaryContainer = OceanOnPrimaryContainerLight,
        secondary = OceanSecondaryLight,
        onSecondary = OceanOnSecondaryLight,
        secondaryContainer = OceanSecondaryContainerLight,
        onSecondaryContainer = OceanOnSecondaryContainerLight,
        tertiary = OceanTertiaryLight,
        onTertiary = OceanOnTertiaryLight,
        tertiaryContainer = OceanTertiaryContainerLight,
        onTertiaryContainer = OceanOnTertiaryContainerLight,
        background = OceanBackgroundLight,
        onBackground = IndigoOnBackgroundLight,
        surface = IndigoSurfaceLight,
        onSurface = IndigoOnSurfaceLight,
        surfaceVariant = OceanSurfaceVariantLight,
        onSurfaceVariant = IndigoOnSurfaceVariantLight,
        outline = IndigoOutlineLight,
        outlineVariant = IndigoOutlineVariantLight
    )
    AppPalette.SUNSET -> lightColorScheme(
        primary = SunsetPrimaryLight,
        onPrimary = SunsetOnPrimaryLight,
        primaryContainer = SunsetPrimaryContainerLight,
        onPrimaryContainer = SunsetOnPrimaryContainerLight,
        secondary = SunsetSecondaryLight,
        onSecondary = SunsetOnSecondaryLight,
        secondaryContainer = SunsetSecondaryContainerLight,
        onSecondaryContainer = SunsetOnSecondaryContainerLight,
        tertiary = SunsetTertiaryLight,
        onTertiary = SunsetOnTertiaryLight,
        tertiaryContainer = SunsetTertiaryContainerLight,
        onTertiaryContainer = SunsetOnTertiaryContainerLight,
        background = SunsetBackgroundLight,
        onBackground = IndigoOnBackgroundLight,
        surface = IndigoSurfaceLight,
        onSurface = IndigoOnSurfaceLight,
        surfaceVariant = SunsetSurfaceVariantLight,
        onSurfaceVariant = IndigoOnSurfaceVariantLight,
        outline = IndigoOutlineLight,
        outlineVariant = IndigoOutlineVariantLight
    )
    AppPalette.CYBER -> lightColorScheme(
        primary = CyberPrimaryLight,
        onPrimary = CyberOnPrimaryLight,
        primaryContainer = CyberPrimaryContainerLight,
        onPrimaryContainer = CyberOnPrimaryContainerLight,
        secondary = CyberSecondaryLight,
        onSecondary = CyberOnSecondaryLight,
        secondaryContainer = CyberSecondaryContainerLight,
        onSecondaryContainer = CyberOnSecondaryContainerLight,
        tertiary = CyberTertiaryLight,
        onTertiary = CyberOnTertiaryLight,
        tertiaryContainer = CyberTertiaryContainerLight,
        onTertiaryContainer = CyberOnTertiaryContainerLight,
        background = CyberBackgroundLight,
        onBackground = IndigoOnBackgroundLight,
        surface = IndigoSurfaceLight,
        onSurface = IndigoOnSurfaceLight,
        surfaceVariant = CyberSurfaceVariantLight,
        onSurfaceVariant = IndigoOnSurfaceVariantLight,
        outline = IndigoOutlineLight,
        outlineVariant = IndigoOutlineVariantLight
    )
}

private fun getDarkScheme(palette: AppPalette): ColorScheme = when (palette) {
    AppPalette.INDIGO -> darkColorScheme(
        primary = IndigoPrimaryDark,
        onPrimary = IndigoOnPrimaryDark,
        primaryContainer = IndigoPrimaryContainerDark,
        onPrimaryContainer = IndigoOnPrimaryContainerDark,
        secondary = IndigoSecondaryDark,
        onSecondary = IndigoOnSecondaryDark,
        secondaryContainer = IndigoSecondaryContainerDark,
        onSecondaryContainer = IndigoOnSecondaryContainerDark,
        tertiary = IndigoTertiaryDark,
        onTertiary = IndigoOnTertiaryDark,
        tertiaryContainer = IndigoTertiaryContainerDark,
        onTertiaryContainer = IndigoOnTertiaryContainerDark,
        background = IndigoBackgroundDark,
        onBackground = IndigoOnBackgroundDark,
        surface = IndigoSurfaceDark,
        onSurface = IndigoOnSurfaceDark,
        surfaceVariant = IndigoSurfaceVariantDark,
        onSurfaceVariant = IndigoOnSurfaceVariantDark,
        outline = IndigoOutlineDark,
        outlineVariant = IndigoOutlineVariantDark
    )
    AppPalette.EMERALD -> darkColorScheme(
        primary = EmeraldPrimaryDark,
        onPrimary = EmeraldOnPrimaryDark,
        primaryContainer = EmeraldPrimaryContainerDark,
        onPrimaryContainer = EmeraldOnPrimaryContainerDark,
        secondary = EmeraldSecondaryDark,
        onSecondary = EmeraldOnSecondaryDark,
        secondaryContainer = EmeraldSecondaryContainerDark,
        onSecondaryContainer = EmeraldOnSecondaryContainerDark,
        tertiary = EmeraldTertiaryDark,
        onTertiary = EmeraldOnTertiaryDark,
        tertiaryContainer = EmeraldTertiaryContainerDark,
        onTertiaryContainer = EmeraldOnTertiaryContainerDark,
        background = EmeraldBackgroundDark,
        onBackground = IndigoOnBackgroundDark,
        surface = EmeraldSurfaceDark,
        onSurface = IndigoOnSurfaceDark,
        surfaceVariant = EmeraldSurfaceVariantDark,
        onSurfaceVariant = IndigoOnSurfaceVariantDark,
        outline = IndigoOutlineDark,
        outlineVariant = IndigoOutlineVariantDark
    )
    AppPalette.OCEAN -> darkColorScheme(
        primary = OceanPrimaryDark,
        onPrimary = OceanOnPrimaryDark,
        primaryContainer = OceanPrimaryContainerDark,
        onPrimaryContainer = OceanOnPrimaryContainerDark,
        secondary = OceanSecondaryDark,
        onSecondary = OceanOnSecondaryDark,
        secondaryContainer = OceanSecondaryContainerDark,
        onSecondaryContainer = OceanOnSecondaryContainerDark,
        tertiary = OceanTertiaryDark,
        onTertiary = OceanOnTertiaryDark,
        tertiaryContainer = OceanTertiaryContainerDark,
        onTertiaryContainer = OceanOnTertiaryContainerDark,
        background = OceanBackgroundDark,
        onBackground = IndigoOnBackgroundDark,
        surface = OceanSurfaceDark,
        onSurface = IndigoOnSurfaceDark,
        surfaceVariant = OceanSurfaceVariantDark,
        onSurfaceVariant = IndigoOnSurfaceVariantDark,
        outline = IndigoOutlineDark,
        outlineVariant = IndigoOutlineVariantDark
    )
    AppPalette.SUNSET -> darkColorScheme(
        primary = SunsetPrimaryDark,
        onPrimary = SunsetOnPrimaryDark,
        primaryContainer = SunsetPrimaryContainerDark,
        onPrimaryContainer = SunsetOnPrimaryContainerDark,
        secondary = SunsetSecondaryDark,
        onSecondary = SunsetOnSecondaryDark,
        secondaryContainer = SunsetSecondaryContainerDark,
        onSecondaryContainer = SunsetOnSecondaryContainerDark,
        tertiary = SunsetTertiaryDark,
        onTertiary = SunsetOnTertiaryDark,
        tertiaryContainer = SunsetTertiaryContainerDark,
        onTertiaryContainer = SunsetOnTertiaryContainerDark,
        background = SunsetBackgroundDark,
        onBackground = IndigoOnBackgroundDark,
        surface = SunsetSurfaceDark,
        onSurface = IndigoOnSurfaceDark,
        surfaceVariant = SunsetSurfaceVariantDark,
        onSurfaceVariant = IndigoOnSurfaceVariantDark,
        outline = IndigoOutlineDark,
        outlineVariant = IndigoOutlineVariantDark
    )
    AppPalette.CYBER -> darkColorScheme(
        primary = CyberPrimaryDark,
        onPrimary = CyberOnPrimaryDark,
        primaryContainer = CyberPrimaryContainerDark,
        onPrimaryContainer = CyberOnPrimaryContainerDark,
        secondary = CyberSecondaryDark,
        onSecondary = CyberOnSecondaryDark,
        secondaryContainer = CyberSecondaryContainerDark,
        onSecondaryContainer = CyberOnSecondaryContainerDark,
        tertiary = CyberTertiaryDark,
        onTertiary = CyberOnTertiaryDark,
        tertiaryContainer = CyberTertiaryContainerDark,
        onTertiaryContainer = CyberOnTertiaryContainerDark,
        background = CyberBackgroundDark,
        onBackground = IndigoOnBackgroundDark,
        surface = CyberSurfaceDark,
        onSurface = IndigoOnSurfaceDark,
        surfaceVariant = CyberSurfaceVariantDark,
        onSurfaceVariant = IndigoOnSurfaceVariantDark,
        outline = IndigoOutlineDark,
        outlineVariant = IndigoOutlineVariantDark
    )
}

@Composable
fun App3Theme(
    darkTheme: Boolean = false,
    accentColor: String = "Indigo",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val palette = AppPalette.fromKey(accentColor)
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> getDarkScheme(palette)
        else -> getLightScheme(palette)
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
