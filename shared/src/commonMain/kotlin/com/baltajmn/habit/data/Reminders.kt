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

/** Each active reminder as far as asking for permission goes: which habit, at what time. */
internal fun reminderKeys(habits: List<Habit>): Set<String> =
    habits.filter { !it.archived && it.reminderMinute != null }
        .map { "${it.id}@${it.reminderMinute}" }
        .toSet()

/**
 * Whether a re-sync carries a reminder the user has just set or moved. Only that earns asking for
 * the permission: a launch, a return to the foreground and a tick all re-sync the same reminders,
 * and asking on each of them turns one "no" into a loop. On iOS the loop was literal, Settings
 * opened again every time the user came back from it. Null [previous] is the first sync of the
 * process, which sets the baseline and asks nothing.
 */
internal fun hasNewReminder(previous: Set<String>?, current: Set<String>): Boolean =
    previous != null && !previous.containsAll(current)
