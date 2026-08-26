package com.baltajmn.habit.i18n

import java.util.Locale

actual fun systemLanguage(): String = Locale.getDefault().language
