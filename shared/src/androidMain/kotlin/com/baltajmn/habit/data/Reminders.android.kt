package com.baltajmn.habit.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.baltajmn.habit.model.Habit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import com.baltajmn.habit.i18n.S

internal const val REMINDER_CHANNEL = "habit_reminders"
internal const val EXTRA_HABIT_ID = "habitId"

@OptIn(ExperimentalTime::class)
actual object Reminders {

    /**
     * Set by the Android entry point. Called when a reminder exists but POST_NOTIFICATIONS
     * has not been granted, so the permission is asked for at the moment it starts to matter.
     */
    var onNeedsPermission: (() -> Unit)? = null

    actual fun sync(habits: List<Habit>) {
        val context = AndroidContext.value
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        var needsPermission = false

        habits.forEach { habit ->
            alarms.cancel(pendingIntent(context, habit.id))
            if (habit.archived) return@forEach
            val next = habit.nextReminderAt(now) ?: return@forEach
            needsPermission = true
            // Inexact on purpose: an exact alarm would need SCHEDULE_EXACT_ALARM, and a habit
            // nudge a few minutes late is fine. allowWhileIdle still gets it out of doze.
            alarms.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                pendingIntent(context, habit.id),
            )
        }

        if (needsPermission && !hasNotificationPermission(context)) onNeedsPermission?.invoke()
    }

    actual fun cancel(habitId: String) {
        val context = AndroidContext.value
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarms.cancel(pendingIntent(context, habitId))
    }

    private fun pendingIntent(context: Context, habitId: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_HABIT_ID, habitId)
        return PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun hasNotificationPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) true
        else context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    internal fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(REMINDER_CHANNEL) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                REMINDER_CHANNEL,
                S.reminderChannel,
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        )
    }
}
