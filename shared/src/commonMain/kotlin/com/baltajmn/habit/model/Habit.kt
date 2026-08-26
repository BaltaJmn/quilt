package com.baltajmn.habit.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable

/**
 * One habit plus its whole history. History is a sparse map of ISO-date -> times done that day,
 * so a full year of a daily habit is ~365 entries: small enough to keep in memory and to
 * serialize as a single JSON blob.
 */
@Serializable
data class Habit(
    val id: String,
    val name: String,
    val emoji: String = "",
    val colorArgb: Long,
    /** Times per day that count as "done". */
    val target: Int = 1,
    /** ISO day numbers (1 = Monday .. 7 = Sunday) the habit is scheduled on. */
    val scheduleDays: Set<Int> = ALL_DAYS,
    /** Minutes from midnight; null = no reminder. */
    val reminderMinute: Int? = null,
    val createdAt: String,
    val archived: Boolean = false,
    val log: Map<String, Int> = emptyMap(),
    /**
     * ISO dates deliberately excused: holidays, illness, a rest the user chose. A skipped day is
     * not a failure and not a success. It neither breaks the streak nor counts against the rate.
     */
    val skipped: Set<String> = emptySet(),
) {
    val created: LocalDate get() = LocalDate.parse(createdAt)

    fun countOn(date: LocalDate): Int = log[date.toString()] ?: 0

    fun isDoneOn(date: LocalDate): Boolean = countOn(date) >= target

    fun isScheduledOn(date: LocalDate): Boolean = date.dayOfWeek.isoDayNumber in scheduleDays

    fun isSkippedOn(date: LocalDate): Boolean = date.toString() in skipped

    /** A day that actually asks something of the user: scheduled, and not excused. */
    fun countsOn(date: LocalDate): Boolean = isScheduledOn(date) && !isSkippedOn(date)

    /**
     * Consecutive scheduled days completed, counting back from [today].
     * Today still pending does not break the streak; any earlier missed scheduled day does.
     * Skipped days are stepped over as if they were not scheduled at all.
     */
    fun streak(today: LocalDate): Int {
        val start = created
        var streak = 0
        var date = today
        var isFirstScheduledDay = true
        while (date >= start) {
            if (countsOn(date)) {
                if (isDoneOn(date)) streak++ else if (!isFirstScheduledDay) break
                isFirstScheduledDay = false
            }
            date = date.minus(DatePeriod(days = 1))
        }
        return streak
    }

    /** Longest run of completed scheduled days ever, up to [today]. Skipped days do not reset it. */
    fun bestStreak(today: LocalDate): Int {
        var best = 0
        var current = 0
        var date = created
        while (date <= today) {
            if (countsOn(date)) {
                if (isDoneOn(date)) {
                    current++
                    if (current > best) best = current
                } else {
                    current = 0
                }
            }
            date = date.plus(DatePeriod(days = 1))
        }
        return best
    }

    /** Days ever completed, across every year. */
    fun totalDone(): Int = log.count { it.value >= target }

    /** Completed vs days that asked something, in [year] up to [until], as 0f..1f. */
    fun completionRate(year: Int, until: LocalDate): Float {
        var done = 0
        var scheduled = 0
        var date = maxOf(LocalDate(year, 1, 1), created)
        val end = minOf(LocalDate(year, 12, 31), until)
        while (date <= end) {
            if (countsOn(date)) {
                scheduled++
                if (isDoneOn(date)) done++
            }
            date = date.plus(DatePeriod(days = 1))
        }
        return if (scheduled == 0) 0f else done.toFloat() / scheduled
    }

    companion object {
        val ALL_DAYS = setOf(1, 2, 3, 4, 5, 6, 7)
    }
}
