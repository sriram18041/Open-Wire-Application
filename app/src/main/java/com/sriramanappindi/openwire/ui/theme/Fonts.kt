package com.sriramanappindi.openwire.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.sriramanappindi.openwire.R

/**
 * Editorial serif for the masthead and headlines — Source Serif 4, an
 * open-source (SIL OFL) Google font, bundled as TTF resources so it
 * renders identically on every device instead of falling back to
 * whatever default serif the OEM skin ships.
 */
val SourceSerif = FontFamily(
    Font(R.font.source_serif_4_regular, FontWeight.Normal),
    Font(R.font.source_serif_4_semibold, FontWeight.SemiBold),
    Font(R.font.source_serif_4_bold, FontWeight.Bold)
)

/** Clean grotesque for body copy and UI chrome — IBM Plex Sans, also OFL. */
val PlexSans = FontFamily(
    Font(R.font.ibm_plex_sans_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_semibold, FontWeight.SemiBold)
)
