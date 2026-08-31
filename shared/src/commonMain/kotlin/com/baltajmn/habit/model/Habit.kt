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
    /**
     * Days per week that count as success, or null for the fixed [scheduleDays] schedule.
     * With a target the week is the unit and no single day is owed: any day fills the quota, and
     * the streak and the completion rate are counted in weeks instead of days.
     */
    val weeklyTarget: Int? = null,
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

    /** True when the week, not the day, is the unit of success. */
    val isWeekly: Boolean get() = weeklyTarget != null

    /** Monday of the ISO week [date] falls in. */
    private fun weekStart(date: LocalDate): LocalDate =
        date.minus(DatePeriod(days = date.dayOfWeek.isoDayNumber - 1))

    /** First Monday on or after [from]: the first week judged in full. */
    private fun firstFullWeek(from: LocalDate): LocalDate {
        val monday = weekStart(from)
        return if (monday == from) monday else monday.plus(DatePeriod(days = 7))
    }

    /** Days completed in the week [date] belongs to. */
    fun doneInWeek(date: LocalDate): Int {
        val monday = weekStart(date)
        return (0..6).count { isDoneOn(monday.plus(DatePeriod(days = it))) }
    }

    /** True once the week [date] belongs to has met its quota. Always false without one. */
    fun weekMet(date: LocalDate): Boolean {
        val target = weeklyTarget ?: return false
        return doneInWeek(date) >= target
    }

    /**
     * Consecutive scheduled days completed, counting back from [today].
     * Today still pending does not break the streak; any earlier missed scheduled day does.
     * Skipped days are stepped over as if they were not scheduled at all.
     */
    fun streak(today: LocalDate): Int {
        if (weeklyTarget != null) return weeklyStreak(today)
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
        if (weeklyTarget != null) return weeklyBestStreak(today)
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
        if (weeklyTarget != null) return weeklyCompletionRate(year, until)
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

    /**
     * Consecutive weeks that met the quota, counting back from the week [today] is in. The week in
     * progress gets the same grace today gets in the daily streak: falling short does not break a
     * run that is not over yet.
     */
    private fun weeklyStreak(today: LocalDate): Int {
        val target = weeklyTarget ?: return 0
        val firstWeek = weekStart(created)
        var monday = weekStart(today)
        var streak = 0
        var isCurrentWeek = true
        while (monday >= firstWeek) {
            if (doneInWeek(monday) >= target) streak++ else if (!isCurrentWeek) break
            isCurrentWeek = false
            monday = monday.minus(DatePeriod(days = 7))
        }
        return streak
    }

    /** Longest run of weeks that met the quota. The week in progress cannot count as a failure. */
    private fun weeklyBestStreak(today: LocalDate): Int {
        val target = weeklyTarget ?: return 0
        val last = weekStart(today)
        var monday = weekStart(created)
        var best = 0
        var current = 0
        while (monday <= last) {
            if (doneInWeek(monday) >= target) {
                current++
                if (current > best) best = current
            } else if (monday < last) {
                current = 0
            }
            monday = monday.plus(DatePeriod(days = 7))
        }
        return best
    }

    /**
     * Weeks that met the quota vs weeks elapsed, in [year] up to [until]. A week belongs to the
     * year of its Monday, and the week the habit was created in is skipped when it started before
     * the habit did: a quota of three is not reachable in the two days that were left.
     */
    private fun weeklyCompletionRate(year: Int, until: LocalDate): Float {
        val target = weeklyTarget ?: return 0f
        var monday = firstFullWeek(maxOf(LocalDate(year, 1, 1), created))
        val end = weekStart(minOf(LocalDate(year, 12, 31), until))
        var weeks = 0
        var met = 0
        while (monday <= end) {
            weeks++
            if (doneInWeek(monday) >= target) met++
            monday = monday.plus(DatePeriod(days = 7))
        }
        return if (weeks == 0) 0f else met.toFloat() / weeks
    }

    companion object {
        val ALL_DAYS = setOf(1, 2, 3, 4, 5, 6, 7)
    }
}
