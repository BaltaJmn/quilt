package com.baltajmn.habit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.tooling.preview.Preview
import com.baltajmn.habit.billing.Billing
import com.baltajmn.habit.data.HabitRepository
import com.baltajmn.habit.ui.HabitDetailScreen
import com.baltajmn.habit.ui.HomeScreen
import com.baltajmn.habit.ui.ShareScreen
import com.baltajmn.habit.ui.theme.HabitTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    remember {
        HabitRepository.load()
        Billing.configure()
    }
    // Catches a purchase made on another device, and a refund.
    LaunchedEffect(Unit) { Billing.refresh() }
    // Two screens do not justify a navigation library.
    var openHabitId by remember { mutableStateOf<String?>(null) }
    var sharing by remember { mutableStateOf(false) }

    HabitTheme {
        val id = openHabitId
        when {
            sharing -> {
                BackHandler { sharing = false }
                ShareScreen(onBack = { sharing = false })
            }
            id != null -> {
                BackHandler { openHabitId = null }
                HabitDetailScreen(habitId = id, onBack = { openHabitId = null })
            }
            else -> HomeScreen(
                onOpenHabit = { openHabitId = it },
                onShare = { sharing = true },
            )
        }
    }
}
