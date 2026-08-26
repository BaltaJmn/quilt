package com.baltajmn.habit.data

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.baltajmn.habit.shared.R
import com.baltajmn.habit.i18n.S

/** Fires at a habit's reminder time: notifies, then schedules that habit's next occurrence. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        AndroidContext.init(context)
        HabitRepository.load()

        val habitId = intent.getStringExtra(EXTRA_HABIT_ID)
        val habit = HabitRepository.habits.firstOrNull { it.id == habitId }
        val today = today()

        // Nothing to nag about if the habit is done, excused, or today is a rest day.
        if (habit != null && !habit.archived && habit.countsOn(today) && !habit.isDoneOn(today)) {
            Reminders.ensureChannel(context)
            val open = context.packageManager.getLaunchIntentForPackage(context.packageName)
            val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("${habit.emoji} ${habit.name}".trim())
                .setContentText(S.reminderBody)
                .setAutoCancel(true)
            if (open != null) {
                builder.setContentIntent(
                    PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_IMMUTABLE)
                )
            }
            NotificationManagerCompat.from(context).run {
                if (areNotificationsEnabled()) notify(habit.id.hashCode(), builder.build())
            }
        }

        Reminders.sync(HabitRepository.habits)
    }
}

/** Alarms do not survive a reboot, so reschedule them all once the device is back up. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        AndroidContext.init(context)
        HabitRepository.load()
        Reminders.sync(HabitRepository.habits)
    }
}
