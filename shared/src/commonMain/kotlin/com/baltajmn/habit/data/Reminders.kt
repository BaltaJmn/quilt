package com.baltajmn.habit.data

import com.baltajmn.habit.model.Habit
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus

/**
 * Local notifications. [sync] is a full re-sync: it cancels everything it knows about and
 * reschedules from scratch, so callers never track individual alarms.
 */
expect object Reminders {
    /** Pass every habit, archived ones included, so their old alarms get cancelled too. */
    fun sync(habits: List<Habit>)

    fun cancel(habitId: String)
}

/** Next moment this habit should fire, strictly after [now]. Null if it has no reminder. */
fun Habit.nextReminderAt(now: LocalDateTime): LocalDateTime? {
    val minutes = reminderMinute ?: return null
    val time = LocalTime(minutes / 60, minutes % 60)
    var date = now.date
    // A week plus today is enough to hit any weekly schedule.
    repeat(8) {
        if (isScheduledOn(date)) {
            val candidate = LocalDateTime(date, time)
            if (candidate > now) return candidate
        }
        date = date.plus(DatePeriod(days = 1))
    }
    return null
}
