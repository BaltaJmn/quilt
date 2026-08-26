package com.baltajmn.habit.data

import androidx.glance.appwidget.updateAll
import com.baltajmn.habit.widget.HabitWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

actual fun refreshWidgets() {
    // No context yet means no UI is running, so nothing is on screen to refresh.
    val context = runCatching { AndroidContext.value }.getOrNull() ?: return
    CoroutineScope(Dispatchers.Default).launch {
        HabitWidget().updateAll(context)
    }
}
