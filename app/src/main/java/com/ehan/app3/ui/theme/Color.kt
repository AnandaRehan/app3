package com.ehan.app3.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppPalette(
    val key: String,
    val displayName: String,
    val previewPrimary: Color,
    val previewSecondary: Color,
    val previewTertiary: Color
) {
    INDIGO(
        key = "Indigo",
        displayName = "Aurora Violet",
        previewPrimary = Color(0xFF4F46E5),
        previewSecondary = Color(0xFFEC4899),
        previewTertiary = Color(0xFF06B6D4)
    ),
    EMERALD(
        key = "Emerald",
        displayName = "Emerald Cyber",
        previewPrimary = Color(0xFF059669),
        previewSecondary = Color(0xFF0EA5E9),
        previewTertiary = Color(0xFFF59E0B)
    ),
    OCEAN(
        key = "Ocean",
        displayName = "Oceanic Neon",
        previewPrimary = Color(0xFF0284C7),
        previewSecondary = Color(0xFF6366F1),
        previewTertiary = Color(0xFF14B8A6)
    ),
    SUNSET(
        key = "Sunset",
        displayName = "Sunset Blaze",
        previewPrimary = Color(0xFFE11D48),
        previewSecondary = Color(0xFFF97316),
        previewTertiary = Color(0xFF8B5CF6)
    ),
    CYBER(
        key = "Cyber",
        displayName = "Neon Magenta",
        previewPrimary = Color(0xFF9333EA),
        previewSecondary = Color(0xFFEC4899),
        previewTertiary = Color(0xFFEAB308)
    );

    companion object {
        fun fromKey(key: String): AppPalette {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: INDIGO
        }
    }
}

// 1. AURORA VIOLET (Indigo + Pink + Cyan)
val IndigoPrimaryLight = Color(0xFF4338CA)
val IndigoOnPrimaryLight = Color(0xFFFFFFFF)
val IndigoPrimaryContainerLight = Color(0xFFE0E7FF)
val IndigoOnPrimaryContainerLight = Color(0xFF1E1B4B)

val IndigoSecondaryLight = Color(0xFFDB2777)
val IndigoOnSecondaryLight = Color(0xFFFFFFFF)
val IndigoSecondaryContainerLight = Color(0xFFFCE7F3)
val IndigoOnSecondaryContainerLight = Color(0xFF500724)

val IndigoTertiaryLight = Color(0xFF0891B2)
val IndigoOnTertiaryLight = Color(0xFFFFFFFF)
val IndigoTertiaryContainerLight = Color(0xFFCFFAFE)
val IndigoOnTertiaryContainerLight = Color(0xFF083344)

val IndigoBackgroundLight = Color(0xFFF5F7FF)
val IndigoOnBackgroundLight = Color(0xFF0F172A)
val IndigoSurfaceLight = Color(0xFFFFFFFF)
val IndigoOnSurfaceLight = Color(0xFF0F172A)
val IndigoSurfaceVariantLight = Color(0xFFEEF2FF)
val IndigoOnSurfaceVariantLight = Color(0xFF334155)
val IndigoOutlineLight = Color(0xFF94A3B8)
val IndigoOutlineVariantLight = Color(0xFFCBD5E1)

val IndigoPrimaryDark = Color(0xFF818CF8)
val IndigoOnPrimaryDark = Color(0xFF1E1B4B)
val IndigoPrimaryContainerDark = Color(0xFF3730A3)
val IndigoOnPrimaryContainerDark = Color(0xFFE0E7FF)

val IndigoSecondaryDark = Color(0xFFF472B6)
val IndigoOnSecondaryDark = Color(0xFF500724)
val IndigoSecondaryContainerDark = Color(0xFF9D174D)
val IndigoOnSecondaryContainerDark = Color(0xFFFCE7F3)

val IndigoTertiaryDark = Color(0xFF22D3EE)
val IndigoOnTertiaryDark = Color(0xFF083344)
val IndigoTertiaryContainerDark = Color(0xFF155E75)
val IndigoOnTertiaryContainerDark = Color(0xFFCFFAFE)

val IndigoBackgroundDark = Color(0xFF0B0F19)
val IndigoOnBackgroundDark = Color(0xFFF8FAFC)
val IndigoSurfaceDark = Color(0xFF131B2E)
val IndigoOnSurfaceDark = Color(0xFFF8FAFC)
val IndigoSurfaceVariantDark = Color(0xFF1E293B)
val IndigoOnSurfaceVariantDark = Color(0xFFCBD5E1)
val IndigoOutlineDark = Color(0xFF64748B)
val IndigoOutlineVariantDark = Color(0xFF334155)

// 2. EMERALD CYBER (Emerald + Sky + Amber)
val EmeraldPrimaryLight = Color(0xFF047857)
val EmeraldOnPrimaryLight = Color(0xFFFFFFFF)
val EmeraldPrimaryContainerLight = Color(0xFFD1FAE5)
val EmeraldOnPrimaryContainerLight = Color(0xFF022C22)

val EmeraldSecondaryLight = Color(0xFF0284C7)
val EmeraldOnSecondaryLight = Color(0xFFFFFFFF)
val EmeraldSecondaryContainerLight = Color(0xFFE0F2FE)
val EmeraldOnSecondaryContainerLight = Color(0xFF0C4A6E)

val EmeraldTertiaryLight = Color(0xFFD97706)
val EmeraldOnTertiaryLight = Color(0xFFFFFFFF)
val EmeraldTertiaryContainerLight = Color(0xFFFEF3C7)
val EmeraldOnTertiaryContainerLight = Color(0xFF451A03)

val EmeraldBackgroundLight = Color(0xFFF2FBF7)
val EmeraldSurfaceVariantLight = Color(0xFFE6F7EF)

val EmeraldPrimaryDark = Color(0xFF34D399)
val EmeraldOnPrimaryDark = Color(0xFF022C22)
val EmeraldPrimaryContainerDark = Color(0xFF065F46)
val EmeraldOnPrimaryContainerDark = Color(0xFFD1FAE5)

val EmeraldSecondaryDark = Color(0xFF38BDF8)
val EmeraldOnSecondaryDark = Color(0xFF082F49)
val EmeraldSecondaryContainerDark = Color(0xFF0369A1)
val EmeraldOnSecondaryContainerDark = Color(0xFFE0F2FE)

val EmeraldTertiaryDark = Color(0xFFFBBF24)
val EmeraldOnTertiaryDark = Color(0xFF451A03)
val EmeraldTertiaryContainerDark = Color(0xFF92400E)
val EmeraldOnTertiaryContainerDark = Color(0xFFFEF3C7)

val EmeraldBackgroundDark = Color(0xFF071712)
val EmeraldSurfaceDark = Color(0xFF0F241D)
val EmeraldSurfaceVariantDark = Color(0xFF16332A)

// 3. OCEANIC NEON (Blue + Indigo + Teal)
val OceanPrimaryLight = Color(0xFF0369A1)
val OceanOnPrimaryLight = Color(0xFFFFFFFF)
val OceanPrimaryContainerLight = Color(0xFFE0F2FE)
val OceanOnPrimaryContainerLight = Color(0xFF082F49)

val OceanSecondaryLight = Color(0xFF4F46E5)
val OceanOnSecondaryLight = Color(0xFFFFFFFF)
val OceanSecondaryContainerLight = Color(0xFFE0E7FF)
val OceanOnSecondaryContainerLight = Color(0xFF1E1B4B)

val OceanTertiaryLight = Color(0xFF0D9488)
val OceanOnTertiaryLight = Color(0xFFFFFFFF)
val OceanTertiaryContainerLight = Color(0xFFCCFBF1)
val OceanOnTertiaryContainerLight = Color(0xFF134E4A)

val OceanBackgroundLight = Color(0xFFF0F9FF)
val OceanSurfaceVariantLight = Color(0xFFE0F2FE)

val OceanPrimaryDark = Color(0xFF38BDF8)
val OceanOnPrimaryDark = Color(0xFF082F49)
val OceanPrimaryContainerDark = Color(0xFF075985)
val OceanOnPrimaryContainerDark = Color(0xFFE0F2FE)

val OceanSecondaryDark = Color(0xFF818CF8)
val OceanOnSecondaryDark = Color(0xFF1E1B4B)
val OceanSecondaryContainerDark = Color(0xFF3730A3)
val OceanOnSecondaryContainerDark = Color(0xFFE0E7FF)

val OceanTertiaryDark = Color(0xFF2DD4BF)
val OceanOnTertiaryDark = Color(0xFF042F2E)
val OceanTertiaryContainerDark = Color(0xFF115E59)
val OceanOnTertiaryContainerDark = Color(0xFFCCFBF1)

val OceanBackgroundDark = Color(0xFF07131F)
val OceanSurfaceDark = Color(0xFF0E1F33)
val OceanSurfaceVariantDark = Color(0xFF172E47)

// 4. SUNSET BLAZE (Rose + Orange + Violet)
val SunsetPrimaryLight = Color(0xFFBE123C)
val SunsetOnPrimaryLight = Color(0xFFFFFFFF)
val SunsetPrimaryContainerLight = Color(0xFFFFE4E6)
val SunsetOnPrimaryContainerLight = Color(0xFF4C0519)

val SunsetSecondaryLight = Color(0xFFEA580C)
val SunsetOnSecondaryLight = Color(0xFFFFFFFF)
val SunsetSecondaryContainerLight = Color(0xFFFFEDD5)
val SunsetOnSecondaryContainerLight = Color(0xFF431407)

val SunsetTertiaryLight = Color(0xFF7C3AED)
val SunsetOnTertiaryLight = Color(0xFFFFFFFF)
val SunsetTertiaryContainerLight = Color(0xFFEDE9FE)
val SunsetOnTertiaryContainerLight = Color(0xFF2E1065)

val SunsetBackgroundLight = Color(0xFFFFF5F6)
val SunsetSurfaceVariantLight = Color(0xFFFFE4E6)

val SunsetPrimaryDark = Color(0xFFFB7185)
val SunsetOnPrimaryDark = Color(0xFF4C0519)
val SunsetPrimaryContainerDark = Color(0xFF9F1239)
val SunsetOnPrimaryContainerDark = Color(0xFFFFE4E6)

val SunsetSecondaryDark = Color(0xFFFB923C)
val SunsetOnSecondaryDark = Color(0xFF431407)
val SunsetSecondaryContainerDark = Color(0xFF9A3412)
val SunsetOnSecondaryContainerDark = Color(0xFFFFEDD5)

val SunsetTertiaryDark = Color(0xFFA78BFA)
val SunsetOnTertiaryDark = Color(0xFF2E1065)
val SunsetTertiaryContainerDark = Color(0xFF5B21B6)
val SunsetOnTertiaryContainerDark = Color(0xFFEDE9FE)

val SunsetBackgroundDark = Color(0xFF180A10)
val SunsetSurfaceDark = Color(0xFF26121B)
val SunsetSurfaceVariantDark = Color(0xFF381A28)

// 5. NEON MAGENTA (Purple + Pink + Gold)
val CyberPrimaryLight = Color(0xFF7E22CE)
val CyberOnPrimaryLight = Color(0xFFFFFFFF)
val CyberPrimaryContainerLight = Color(0xFFF3E8FF)
val CyberOnPrimaryContainerLight = Color(0xFF3B0764)

val CyberSecondaryLight = Color(0xFFDB2777)
val CyberOnSecondaryLight = Color(0xFFFFFFFF)
val CyberSecondaryContainerLight = Color(0xFFFCE7F3)
val CyberOnSecondaryContainerLight = Color(0xFF500724)

val CyberTertiaryLight = Color(0xFFCA8A04)
val CyberOnTertiaryLight = Color(0xFFFFFFFF)
val CyberTertiaryContainerLight = Color(0xFFFEF9C3)
val CyberOnTertiaryContainerLight = Color(0xFF422006)

val CyberBackgroundLight = Color(0xFFFAF5FF)
val CyberSurfaceVariantLight = Color(0xFFF3E8FF)

val CyberPrimaryDark = Color(0xFFC084FC)
val CyberOnPrimaryDark = Color(0xFF3B0764)
val CyberPrimaryContainerDark = Color(0xFF6B21A8)
val CyberOnPrimaryContainerDark = Color(0xFFF3E8FF)

val CyberSecondaryDark = Color(0xFFF472B6)
val CyberOnSecondaryDark = Color(0xFF500724)
val CyberSecondaryContainerDark = Color(0xFF9D174D)
val CyberOnSecondaryContainerDark = Color(0xFFFCE7F3)

val CyberTertiaryDark = Color(0xFFFACC15)
val CyberOnTertiaryDark = Color(0xFF422006)
val CyberTertiaryContainerDark = Color(0xFF854D0E)
val CyberOnTertiaryContainerDark = Color(0xFFFEF9C3)

val CyberBackgroundDark = Color(0xFF13091F)
val CyberSurfaceDark = Color(0xFF1E1130)
val CyberSurfaceVariantDark = Color(0xFF2E1B47)
