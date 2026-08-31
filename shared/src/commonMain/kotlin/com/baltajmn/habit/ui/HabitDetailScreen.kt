package com.baltajmn.habit.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.habit.data.HabitRepository
import com.baltajmn.habit.data.today
import com.baltajmn.habit.model.Habit
import kotlin.math.roundToInt
import com.baltajmn.habit.i18n.S


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(habitId: String, onBack: () -> Unit) {
    val habit = HabitRepository.habits.firstOrNull { it.id == habitId } ?: return
    val today = remember { today() }
    var year by remember { mutableStateOf(today.year) }
    var showEdit by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val accent = Color(habit.colorArgb)
    val firstYear = habit.created.year

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Header(habit = habit, accent = accent, onBack = onBack, onEdit = { showEdit = true })

        YearSwitcher(
            year = year,
            canGoBack = year > firstYear,
            canGoForward = year < today.year,
            onChange = { year = it },
        )

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(16.dp)) {
                YearGrid(
                    year = year,
                    habit = habit,
                    today = today,
                    onDayClick = { date -> HabitRepository.cycle(habit.id, date) },
                    onDayLongClick = { date -> HabitRepository.toggleSkip(habit.id, date) },
                )
                Text(
                    S.skipHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }

        Stats(habit = habit, year = year, today = today)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionRow(
                label = if (habit.archived) S.unarchive else S.archive,
                hint = S.archiveHint,
                onClick = { HabitRepository.update(habit.copy(archived = !habit.archived)) },
            )
            ActionRow(
                label = S.deleteHabit,
                hint = S.deleteHint,
                destructive = true,
                onClick = { confirmDelete = true },
            )
        }
    }

    if (showEdit) {
        ModalBottomSheet(
            onDismissRequest = { showEdit = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            HabitForm(
                initial = habit,
                onSubmit = { name, emoji, color, target, days, weekly, reminder ->
                    HabitRepository.update(
                        habit.copy(
                            name = name.trim(),
                            emoji = emoji,
                            colorArgb = color,
                            target = target,
                            scheduleDays = days,
                            weeklyTarget = weekly,
                            reminderMinute = reminder,
                        )
                    )
                    showEdit = false
                },
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            title = { Text(S.deleteTitle(habit.name)) },
            text = { Text(S.deleteBody(habit.totalDone())) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    HabitRepository.delete(habit.id)
                    onBack()
                }) {
                    Text(S.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(S.cancel) } },
        )
    }
}

@Composable
private fun Header(habit: Habit, accent: Color, onBack: () -> Unit, onEdit: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconCircle("‹", onClick = onBack)
            Spacer(Modifier.weight(1f))
            IconCircle("✎", onClick = onEdit)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).background(accent.copy(alpha = 0.28f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text(habit.emoji.ifBlank { "•" }, fontSize = 24.sp) }
            Spacer(Modifier.size(14.dp))
            Column {
                Text(habit.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    scheduleSummary(habit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun scheduleSummary(habit: Habit): String {
    val days = when {
        habit.weeklyTarget != null -> S.perWeek(habit.weeklyTarget)
        habit.scheduleDays.size == 7 -> S.everyDay
        habit.scheduleDays == setOf(1, 2, 3, 4, 5) -> S.weekdays
        habit.scheduleDays == setOf(6, 7) -> S.weekends
        else -> habit.scheduleDays.sorted().joinToString(" ") { S.dayInitials[it - 1] }
    }
    return listOfNotNull(
        days,
        habit.target.takeIf { it > 1 }?.let { S.timesPerDay(it) },
        habit.reminderMinute?.let { "🔔 ${S.time(it)}" },
    ).joinToString(" · ")
}

@Composable
private fun IconCircle(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(label, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable
private fun YearSwitcher(year: Int, canGoBack: Boolean, canGoForward: Boolean, onChange: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Arrow("‹", enabled = canGoBack) { onChange(year - 1) }
        Text(
            "$year",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Arrow("›", enabled = canGoForward) { onChange(year + 1) }
    }
}

@Composable
private fun Arrow(label: String, enabled: Boolean, onClick: () -> Unit) {
    Text(
        label,
        fontSize = 22.sp,
        color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier
            .size(32.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(top = 2.dp),
    )
}

@Composable
private fun Stats(habit: Habit, year: Int, today: kotlinx.datetime.LocalDate) {
    val rate = (habit.completionRate(year, today) * 100).roundToInt()
    val streak = habit.streak(today)
    val best = habit.bestStreak(today)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(S.currentStreak, "$streak", S.days(streak, habit.isWeekly), Modifier.weight(1f))
            StatTile(S.bestStreak, "$best", S.days(best, habit.isWeekly), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(S.totalDays, "${habit.totalDone()}", S.sinceStart, Modifier.weight(1f))
            StatTile(S.completion, "$rate%", S.inYear(year), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Light)
            Text(
                unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ActionRow(
    label: String,
    hint: String,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = onClick,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
