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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.habit.data.HabitRepository
import com.baltajmn.habit.model.Habit
import com.baltajmn.habit.model.habitIcon
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
        weeklyTarget: Int?,
        reminderMinute: Int?,
    ) -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var emoji by remember { mutableStateOf(initial?.emoji ?: HABIT_EMOJIS.first()) }
    var color by remember { mutableStateOf(initial?.colorArgb ?: HabitPalette.first()) }
    var target by remember { mutableStateOf(initial?.target ?: 1) }
    var days by remember { mutableStateOf(initial?.scheduleDays ?: Habit.ALL_DAYS) }
    var weekly by remember { mutableStateOf(initial?.weeklyTarget) }
    var reminder by remember { mutableStateOf(initial?.reminderMinute) }
    var pickingTime by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }
    val valid = name.isNotBlank() && (weekly != null || days.isNotEmpty())

    // The sheet shrinks to whatever the keyboard leaves, and this form is taller than that: without
    // a scroll the schedule, the reminder and the label of the submit button are simply cut off
    // while the user is typing the name. Same failure as the share screen on a wide display.
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
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
            // The thirteenth slot is theirs. Twelve presets cover the habits everyone has and
            // none of the ones only you have, so the row ends in an empty circle that is itself
            // the text field: tapping it opens the emoji keyboard, which is a better picker than
            // any grid we could ship.
            OwnIcon(
                value = emoji.takeIf { it !in HABIT_EMOJIS }.orEmpty(),
                onValue = { emoji = habitIcon(it) },
            )
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

        // Fixed days and a weekly quota are alternatives, not layers: showing both would let a
        // user pick three days and then ask for four a week.
        if (weekly == null) PickerRow(label = S.daysLabel) {
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
            Text(S.daysAWeek, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Stepper(
                value = weekly ?: 0,
                onChange = { weekly = it.coerceIn(0, 7).takeIf { n -> n > 0 } },
                label = weekly?.toString() ?: S.none,
            )
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
            onClick = {
                // A weekly quota can be filled on any day, so the day picker stops applying.
                if (valid) {
                    onSubmit(
                        name, emoji, color, target,
                        if (weekly != null) Habit.ALL_DAYS else days,
                        weekly, reminder,
                    )
                }
            },
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
private fun OwnIcon(value: String, onValue: (String) -> Unit) {
    val focus = remember { FocusRequester() }
    val outline = MaterialTheme.colorScheme.outline
    val ring = MaterialTheme.colorScheme.onSurface
    val dashes = remember { PathEffect.dashPathEffect(floatArrayOf(6f, 6f)) }
    Box(
        Modifier
            .size(34.dp)
            .drawBehind {
                // Dashed while empty, so an unused slot reads as one to fill and not as a
                // thirteenth icon that failed to load.
                if (value.isEmpty()) {
                    drawCircle(outline, style = Stroke(width = 1.dp.toPx(), pathEffect = dashes))
                }
            }
            .then(
                if (value.isEmpty()) Modifier
                else Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .border(2.dp, ring, CircleShape)
            )
            .clickable { focus.requestFocus() },
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValue,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                color = LocalContentColor.current,
            ),
            // No caret: the field wears the shape of a swatch, and a blinking bar inside a 34dp
            // circle reads as a rendering fault. The keyboard coming up is the feedback.
            cursorBrush = SolidColor(Color.Transparent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth().focusRequester(focus),
        )
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
private fun Stepper(value: Int, onChange: (Int) -> Unit, label: String = "$value") {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
    ) {
        StepperButton("−") { onChange(value - 1) }
        Text(label, style = MaterialTheme.typography.titleSmall)
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
