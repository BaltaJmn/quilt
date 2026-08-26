package com.baltajmn.habit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.habit.data.HabitRepository
import com.baltajmn.habit.data.today
import com.baltajmn.habit.model.Habit
import com.baltajmn.habit.i18n.S

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onOpenHabit: (String) -> Unit, onShare: () -> Unit) {
    val today = remember { today() }
    var showSheet by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val habits = HabitRepository.activeHabits

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (HabitRepository.canAddHabit()) showSheet = true else showPaywall = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Text("+", fontSize = 26.sp) }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                // Only habits scheduled today can be done today: counting the rest in the
                // denominator makes "6 de 6" unreachable on any day you rest.
                val dueToday = habits.filter { it.countsOn(today) }
                YearHeader(
                    year = today.year,
                    habits = habits,
                    todayTotal = dueToday.size,
                    todayDone = dueToday.count { it.isDoneOn(today) },
                    onShare = onShare,
                    onSettings = { showSettings = true },
                )
            }
            items(habits, key = { it.id }) { habit ->
                HabitCard(
                    habit = habit,
                    today = today,
                    onToggleToday = { HabitRepository.cycle(habit.id, today) },
                    onDayClick = { date -> HabitRepository.cycle(habit.id, date) },
                    onDayLongClick = { date -> HabitRepository.toggleSkip(habit.id, date) },
                    onOpen = { onOpenHabit(habit.id) },
                )
            }
            if (habits.isEmpty()) item { EmptyState() }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            HabitForm(
                onSubmit = { name, emoji, color, target, days, reminder ->
                    HabitRepository.add(name, emoji, color, target, days, reminder)
                    showSheet = false
                },
            )
        }
    }

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            SettingsSheet(onOpenPro = { showSettings = false; showPaywall = true })
        }
    }

    if (showPaywall) ProDialog(onDismiss = { showPaywall = false })
}

@Composable
private fun YearHeader(
    year: Int,
    habits: List<Habit>,
    todayTotal: Int,
    todayDone: Int,
    onShare: () -> Unit,
    onSettings: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
        Column(Modifier.weight(1f)) {
            Text(
                "$year",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                if (habits.isEmpty()) S.startFirst else S.doneToday(todayDone, todayTotal),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (habits.isNotEmpty()) RoundIcon("↑", onShare)
            RoundIcon("⚙", onSettings)
        }
    }
}

@Composable
private fun RoundIcon(glyph: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(glyph, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier.fillMaxWidth().padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🌱", fontSize = 44.sp)
        Spacer(Modifier.size(12.dp))
        Text(
            S.emptyHint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
