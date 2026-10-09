package com.sriramanappindi.openwire.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Source Serif for the masthead and story headlines gives Open Wire an
 * editorial, trustworthy feel (think a modern newspaper) instead of the
 * generic Material look most sideloaded RSS readers have — IBM Plex Sans
 * carries everything functional (meta rows, summaries, buttons) so the
 * serif stays reserved for things worth lingering on.
 */
val OpenWireTypography = Typography(
    headlineMedium = TextStyle(fontFamily = SourceSerif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = SourceSerif, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = SourceSerif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = PlexSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = PlexSans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = PlexSans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.3.sp),
    labelLarge = TextStyle(fontFamily = PlexSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
)
