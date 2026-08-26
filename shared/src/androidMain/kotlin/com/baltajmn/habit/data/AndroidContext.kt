package com.baltajmn.habit.data

import android.content.Context

/** Application context, set once from the Android entry point (or from a broadcast receiver). */
object AndroidContext {
    private var appContext: Context? = null

    val value: Context get() = requireNotNull(appContext) { "AndroidContext.init() was never called" }

    fun init(context: Context) {
        if (appContext == null) appContext = context.applicationContext
    }
}
