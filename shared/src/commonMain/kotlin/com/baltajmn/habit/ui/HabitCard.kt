package com.baltajmn.habit.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.habit.model.Habit
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.math.roundToInt
import com.baltajmn.habit.i18n.S

@Composable
fun HabitCard(
    habit: Habit,
    today: LocalDate,
    onToggleToday: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onDayLongClick: (LocalDate) -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = Color(habit.colorArgb)
    val streak = habit.streak(today)
    val rate = (habit.completionRate(today.year, today) * 100).roundToInt()
    val streakLabel = S.streak(streak, habit.isWeekly)
    val yesterday = today.minus(DatePeriod(days = 1))

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.clickable(onClick = onOpen).padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .background(accent.copy(alpha = 0.28f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(habit.emoji.ifBlank { "•" }, fontSize = 18.sp)
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "$streakLabel · ${S.rateThisYear(rate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                CheckButton(
                    accent = accent,
                    count = habit.countOn(today),
                    target = habit.target,
                    onClick = onToggleToday,
                )
            }
            // The streak breaks quietly overnight and the grid alone does not say it can be
            // mended, so the offer only appears on the day it is worth anything. It excuses,
            // it does not mark: a day off is not a lie about having done the thing.
            // [onDayLongClick] is the skip toggle the grid already uses, same action, same undo.
            if (habit.canExcuse(yesterday)) {
                Spacer(Modifier.size(10.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Text(
                        S.skipYesterday,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { onDayLongClick(yesterday) }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                    )
                }
            }
            Spacer(Modifier.size(16.dp))
            YearGrid(
                year = today.year,
                habit = habit,
                today = today,
                onDayClick = onDayClick,
                onDayLongClick = onDayLongClick,
            )
        }
    }
}

@Composable
private fun CheckButton(accent: Color, count: Int, target: Int, onClick: () -> Unit) {
    val done = count >= target
    val fill by animateColorAsState(
        if (done) accent else MaterialTheme.colorScheme.surfaceVariant,
        label = "check",
    )
    Box(
        Modifier
            .size(44.dp)
            .background(fill, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (done) "✓" else if (target > 1) "$count/$target" else "",
            fontSize = if (done) 20.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            // Pastel accents stay light in both themes, so the tick keeps a fixed dark ink.
            color = if (done) Color(0xFF2E2A24) else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
