package com.baltajmn.habit.data

/**
 * WidgetKit has no Kotlin/Native bindings, so the Swift entry point plugs
 * `WidgetCenter.shared.reloadAllTimelines()` in here at startup.
 */
var onRefreshWidgets: (() -> Unit)? = null

actual fun refreshWidgets() {
    onRefreshWidgets?.invoke()
}
