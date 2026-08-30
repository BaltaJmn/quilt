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
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import com.baltajmn.habit.i18n.S

/**
 * One repeating calendar trigger per (habit, weekday). iOS keeps them alive on its own, so
 * there is no receiver and no rescheduling to do.
 * iOS caps pending requests at 64, so a habit scheduled every day gets a single daily trigger
 * instead of seven weekly ones. Only habits on a partial week cost one request per day.
 */
actual object Reminders {

    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    /**
     * Same hook as the Android actual: the UI decides what to do when the OS will not deliver.
     * iOS only ever shows its alert once per install, so after a Don't Allow the only way back is
     * Settings and the app has to say so.
     */
    var onNeedsPermission: (() -> Unit)? = null

    actual fun sync(habits: List<Habit>) {
        center.removeAllPendingNotificationRequests()
        val scheduled = habits.filter { !it.archived && it.reminderMinute != null }
        if (scheduled.isEmpty()) return
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound
        ) { granted, _ ->
            // Both callbacks arrive off the main thread and the handler touches Compose state.
            dispatch_async(dispatch_get_main_queue()) {
                if (granted) scheduled.forEach { schedule(it) } else onNeedsPermission?.invoke()
            }
        }
    }

    actual fun cancel(habitId: String) {
        // 0 is the daily trigger, 1..7 the per-weekday ones.
        val ids = (0..7).map { "$habitId-$it" }
        center.removePendingNotificationRequestsWithIdentifiers(ids)
        // Pending and delivered are two different stores. Without this a reminder that already
        // fired keeps sitting in Notification Center naming a habit that no longer exists.
        center.removeDeliveredNotificationsWithIdentifiers(ids)
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
