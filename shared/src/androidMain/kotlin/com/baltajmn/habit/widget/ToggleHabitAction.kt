package com.baltajmn.habit.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.baltajmn.habit.data.AndroidContext
import com.baltajmn.habit.data.HabitRepository
import com.baltajmn.habit.data.today

private val HabitIdKey = ActionParameters.Key<String>("habitId")

internal fun habitIdParameters(habitId: String) = actionParametersOf(HabitIdKey to habitId)

/** Tapping a row marks the habit for today (and un-marks it on the next tap). */
class ToggleHabitAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val habitId = parameters[HabitIdKey] ?: return
        AndroidContext.init(context)
        HabitRepository.load()
        HabitRepository.cycle(habitId, today())
        HabitWidget().updateAll(context)
    }
}
