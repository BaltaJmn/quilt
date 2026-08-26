package com.baltajmn.habit.data

import com.baltajmn.habit.model.Habit
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import com.baltajmn.habit.i18n.S

/**
 * One repeating calendar trigger per (habit, weekday). iOS keeps them alive on its own, so
 * there is no receiver and no rescheduling to do.
 * iOS caps pending requests at 64, so a habit scheduled every day gets a single daily trigger
 * instead of seven weekly ones. Only habits on a partial week cost one request per day.
 */
actual object Reminders {

    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    actual fun sync(habits: List<Habit>) {
        center.removeAllPendingNotificationRequests()
        val scheduled = habits.filter { !it.archived && it.reminderMinute != null }
        if (scheduled.isEmpty()) return
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound
        ) { granted, _ ->
            if (granted) scheduled.forEach { schedule(it) }
        }
    }

    actual fun cancel(habitId: String) {
        // 0 is the daily trigger, 1..7 the per-weekday ones.
        center.removePendingNotificationRequestsWithIdentifiers((0..7).map { "$habitId-$it" })
    }

    private fun schedule(habit: Habit) {
        val minutes = habit.reminderMinute ?: return
        val content = UNMutableNotificationContent().apply {
            setTitle("${habit.emoji} ${habit.name}".trim())
            setBody(S.reminderBody)
            setSound(UNNotificationSound.defaultSound())
        }
        // A null day means "every day", which is one request rather than seven.
        val days: List<Int?> =
            if (habit.scheduleDays == Habit.ALL_DAYS) listOf(null) else habit.scheduleDays.sorted()
        days.forEach { isoDay ->
            val components = NSDateComponents().apply {
                // ISO counts Monday as 1; Apple counts Sunday as 1.
                if (isoDay != null) weekday = if (isoDay == 7) 1L else (isoDay + 1).toLong()
                hour = (minutes / 60).toLong()
                minute = (minutes % 60).toLong()
            }
            center.addNotificationRequest(
                UNNotificationRequest.requestWithIdentifier(
                    identifier = "${habit.id}-${isoDay ?: 0}",
                    content = content,
                    trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                        dateComponents = components,
                        repeats = true,
                    ),
                ),
                withCompletionHandler = null,
            )
        }
    }
}
