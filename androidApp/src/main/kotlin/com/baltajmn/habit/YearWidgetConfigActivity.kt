package com.baltajmn.habit

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.baltajmn.habit.data.AndroidContext
import com.baltajmn.habit.data.HabitRepository
import com.baltajmn.habit.i18n.S
import com.baltajmn.habit.widget.YearHabitIdKey
import com.baltajmn.habit.widget.YearWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Which habit a year widget shows. The launcher opens it when the widget is dropped, and again
 * from the widget's own reconfigure action. It is never reachable from inside the app, so it is
 * not a screen the app grew.
 *
 * A system dialog rather than a Compose screen: the app module has no Compose foundation of its
 * own, and a one-shot list of names is exactly what a plain list dialog already is.
 */
class YearWidgetConfigActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidContext.init(applicationContext)
        val widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        // Backing out has to leave the widget unplaced, so cancelled is the result until a pick.
        setResult(RESULT_CANCELED)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        HabitRepository.load()
        val habits = HabitRepository.activeHabits
        if (habits.isEmpty()) {
            finish()
            return
        }
        AlertDialog.Builder(this)
            .setTitle(S.pickHabit)
            .setItems(habits.map { "${it.emoji} ${it.name}" }.toTypedArray()) { _, index ->
                choose(widgetId, habits[index].id)
            }
            .setOnCancelListener { finish() }
            .show()
    }

    private fun choose(widgetId: Int, habitId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(widgetId)
            updateAppWidgetState(applicationContext, glanceId) { it[YearHabitIdKey] = habitId }
            YearWidget().update(applicationContext, glanceId)
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
            finish()
        }
    }
}
