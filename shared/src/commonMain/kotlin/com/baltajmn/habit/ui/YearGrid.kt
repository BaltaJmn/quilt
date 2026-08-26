package com.baltajmn.habit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.habit.model.Habit
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import com.baltajmn.habit.i18n.S

/**
 * The whole year as a 7-row heatmap: one column per ISO week, one cell per day.
 * Drawn on a single Canvas because 366 composables per habit is not worth it.
 */
@Composable
fun YearGrid(
    year: Int,
    habit: Habit,
    today: LocalDate,
    modifier: Modifier = Modifier,
    onDayClick: (LocalDate) -> Unit = {},
    onDayLongClick: (LocalDate) -> Unit = {},
) {
    val jan1 = LocalDate(year, 1, 1)
    val daysInYear = LocalDate(year, 12, 31).dayOfYear
    val firstOffset = jan1.dayOfWeek.isoDayNumber - 1
    val columns = (firstOffset + daysInYear + 6) / 7

    val accent = Color(habit.colorArgb)
    val empty = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val todayRing = MaterialTheme.colorScheme.onSurface
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 8.sp, letterSpacing = 0.5.sp, color = labelColor)
    val labelHeight = 13.dp

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val unit = maxWidth / columns
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(labelHeight + unit * 7)
                .pointerInput(habit.id, year) {
                    fun dateAt(offset: Offset): LocalDate? {
                        val cell = size.width / columns.toFloat()
                        val top = labelHeight.toPx()
                        if (offset.y < top) return null
                        val column = (offset.x / cell).toInt()
                        val row = ((offset.y - top) / cell).toInt()
                        val index = column * 7 + row - firstOffset
                        if (index !in 0 until daysInYear) return null
                        return jan1.plus(DatePeriod(days = index)).takeIf { it <= today }
                    }
                    detectTapGestures(
                        onTap = { dateAt(it)?.let(onDayClick) },
                        onLongPress = { dateAt(it)?.let(onDayLongClick) },
                    )
                }
        ) {
            val cell = size.width / columns.toFloat()
            val box = cell * 0.80f
            val top = labelHeight.toPx()
            val radius = CornerRadius(box * 0.32f)

            for (index in 0 until daysInYear) {
                val date = jan1.plus(DatePeriod(days = index))
                val slot = firstOffset + index
                val x = (slot / 7) * cell
                val y = top + (slot % 7) * cell
                val count = habit.countOn(date)
                val scheduled = habit.isScheduledOn(date)
                val skipped = habit.isSkippedOn(date)

                val color = when {
                    count >= habit.target -> accent
                    count > 0 -> accent.copy(alpha = 0.45f)
                    skipped -> empty.copy(alpha = 0.5f)
                    date > today || date < habit.created -> empty.copy(alpha = 0.35f)
                    scheduled -> empty
                    else -> empty.copy(alpha = 0.5f)
                }
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(box, box),
                    cornerRadius = radius,
                )
                if (skipped && count == 0) {
                    // A dash reads as "paused" at any size. A ring or a dot disappears at the
                    // three-pixel cells the year grid ends up with on a phone.
                    val bar = box * 0.30f
                    drawRoundRect(
                        color = labelColor.copy(alpha = 0.6f),
                        topLeft = Offset(x + box * 0.18f, y + (box - bar) / 2f),
                        size = Size(box * 0.64f, bar),
                        cornerRadius = CornerRadius(bar / 2f),
                    )
                }
                if (date == today) {
                    drawRoundRect(
                        color = todayRing,
                        topLeft = Offset(x, y),
                        size = Size(box, box),
                        cornerRadius = radius,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = box * 0.16f),
                    )
                }
            }

            for (month in 1..12) {
                val slot = firstOffset + LocalDate(year, month, 1).dayOfYear - 1
                val x = (slot / 7) * cell
                drawText(
                    textMeasurer = measurer,
                    text = S.monthsShort[month - 1],
                    style = labelStyle,
                    topLeft = Offset(x, 0f),
                )
            }
        }
    }
}
