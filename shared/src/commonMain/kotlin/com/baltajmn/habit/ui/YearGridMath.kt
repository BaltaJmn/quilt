package com.baltajmn.habit.ui

import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber

// The year grid's arithmetic, with no Compose in it, so a test can run it. The tap and the paint
// have to stay exact inverses of each other, and the same numbers are written out again in
// YearWidget.kt, ShareCard.kt and HabitYearWidget.swift.

/** 365, or 366 in a leap year. */
internal fun daysInYear(year: Int): Int = LocalDate(year, 12, 31).dayOfYear

/** The row January 1st sits on. 0 is Monday, 6 is Sunday. */
internal fun firstOffset(year: Int): Int = LocalDate(year, 1, 1).dayOfWeek.isoDayNumber - 1

/** Seven-day columns the year takes up. The last one is almost always short. */
internal fun yearColumns(year: Int): Int = (firstOffset(year) + daysInYear(year) + 6) / 7

/** The cell a day is drawn in: column is slot / 7, row is slot % 7. */
internal fun slotOf(year: Int, index: Int): Int = firstOffset(year) + index

/**
 * The day under a tap, as an index into the year, or null outside it. Exact inverse of [slotOf].
 * The row is bounded because a tap in the strip below the last row divides to 7, and column * 7 + 7
 * is the same index as the top of the next column: a sliver of the canvas marked the wrong day.
 */
internal fun indexAt(year: Int, column: Int, row: Int): Int? {
    if (row !in 0..6) return null
    val index = column * 7 + row - firstOffset(year)
    return index.takeIf { it in 0 until daysInYear(year) }
}
