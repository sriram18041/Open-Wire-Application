package com.sriramanappindi.openwire.games

import java.util.TimeZone

/**
 * The local calendar day number, used to seed/select "today's" puzzle —
 * shared by Sudoku and the chess puzzle so both change at local midnight.
 * Avoids java.time.LocalDate, which needs API 26+ or core library
 * desugaring; this project's minSdk is 24 and nothing else in the codebase
 * pulls in java.time, so a plain millis-based calculation keeps things
 * consistent with how the rest of the app handles dates/times.
 */
fun localEpochDay(): Long {
    val now = System.currentTimeMillis()
    val offset = TimeZone.getDefault().getOffset(now)
    return (now + offset) / 86_400_000L
}
