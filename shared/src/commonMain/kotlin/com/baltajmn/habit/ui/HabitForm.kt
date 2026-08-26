package com.baltajmn.habit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.habit.data.HabitRepository
import com.baltajmn.habit.model.Habit
import com.baltajmn.habit.ui.theme.HabitPalette
import com.baltajmn.habit.i18n.S

val HABIT_EMOJIS = listOf("🏃", "💧", "📚", "🧘", "💤", "🥗", "🎸", "🧹", "✍️", "🦷", "💊", "☀️")

/** Fields of a habit. Same form for creating one and for editing an existing [initial]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitForm(
    initial: Habit? = null,
    onSubmit: (
        name: String,
        emoji: String,
        color: Long,
        target: Int,
        days: Set<Int>,
        reminderMinute: Int?,
    ) -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var emoji by remember { mutableStateOf(initial?.emoji ?: HABIT_EMOJIS.first()) }
    var color by remember { mutableStateOf(initial?.colorArgb ?: HabitPalette.first()) }
    var target by remember { mutableStateOf(initial?.target ?: 1) }
    var days by remember { mutableStateOf(initial?.scheduleDays ?: Habit.ALL_DAYS) }
    var reminder by remember { mutableStateOf(initial?.reminderMinute) }
    var pickingTime by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }
    val valid = name.isNotBlank() && days.isNotEmpty()

    Column(
        Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            if (initial == null) S.newHabit else S.editHabit,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text(S.namePlaceholder) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )

        PickerRow(label = S.icon) {
            HABIT_EMOJIS.forEach { option ->
                SelectableCircle(
                    selected = option == emoji,
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { emoji = option },
                ) { Text(option, fontSize = 17.sp) }
            }
        }

        PickerRow(label = S.color) {
            HabitPalette.forEachIndexed { index, option ->
                // A colour the habit already wears stays pickable without Pro: a refund or a
                // failed entitlement check must never leave an existing habit uneditable.
                val locked = !HabitRepository.isPro &&
                    index >= HabitRepository.FREE_COLOR_LIMIT &&
                    option != initial?.colorArgb
                SelectableCircle(
                    selected = option == color,
                    background = Color(option).copy(alpha = if (locked) 0.3f else 1f),
                    onClick = { if (locked) showPaywall = true else color = option },
                ) { if (locked) Text("\uD83D\uDD12", fontSize = 11.sp) }
            }
        }

        PickerRow(label = S.daysLabel) {
            S.dayInitials.forEachIndexed { index, label ->
                val iso = index + 1
                val on = iso in days
                SelectableCircle(
                    selected = on,
                    background = if (on) Color(color) else MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { days = if (on) days - iso else days + iso },
                ) {
                    Text(
                        label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (on) Color(0xFF2E2A24) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(S.timesADay, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Stepper(value = target, onChange = { target = it.coerceIn(1, 20) })
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(S.reminder, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = { pickingTime = true }) {
                Text(reminder?.let(S::time) ?: S.none)
            }
        }

        TextButton(
            onClick = { if (valid) onSubmit(name, emoji, color, target, days, reminder) },
            enabled = valid,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium),
        ) {
            Text(
                if (initial == null) S.createHabit else S.save,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }


    if (pickingTime) {
        val state = rememberTimePickerState(
            initialHour = (reminder ?: 9 * 60) / 60,
            initialMinute = (reminder ?: 0) % 60,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { pickingTime = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            title = { Text(S.reminder) },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    reminder = state.hour * 60 + state.minute
                    pickingTime = false
                }) { Text(S.save) }
            },
            dismissButton = {
                TextButton(onClick = { reminder = null; pickingTime = false }) { Text(S.remove) }
            },
        )
    }

    if (showPaywall) ProDialog(onDismiss = { showPaywall = false })
}

@Composable
private fun PickerRow(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) { content() }
    }
}

@Composable
private fun SelectableCircle(
    selected: Boolean,
    background: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier
            .size(34.dp)
            .background(background, CircleShape)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

@Composable
private fun Stepper(value: Int, onChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
    ) {
        StepperButton("−") { onChange(value - 1) }
        Text("$value", style = MaterialTheme.typography.titleSmall)
        StepperButton("+") { onChange(value + 1) }
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(34.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(label, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}
