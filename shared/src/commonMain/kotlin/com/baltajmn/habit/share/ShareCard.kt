package com.baltajmn.habit.share

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.baltajmn.habit.model.Habit
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.roundToInt
import com.baltajmn.habit.i18n.S

/** The three ways a run of days can be drawn, from chunky to dense. */
enum class SharePeriod {
    WEEK, MONTH, YEAR;

    val label: String get() = when (this) {
        WEEK -> S.week
        MONTH -> S.month
        YEAR -> S.year
    }
}

private const val CARD_W = 1080f
private const val CARD_H = 1350f
private const val MARGIN = 80f
private const val MAX_HABITS = 5


private val Cream = Color(0xFFFBF8F3)
private val Ink = Color(0xFF39352E)
private val Muted = Color(0xFF8B8479)
private val Empty = Color(0xFFEDE7DC)

/**
 * Renders the shareable card. The same bitmap is what the preview shows and what gets exported,
 * so what you see is exactly what you post.
 */
fun renderShareCard(
    habits: List<Habit>,
    period: SharePeriod,
    today: LocalDate,
    measurer: TextMeasurer,
): ImageBitmap {
    val bitmap = ImageBitmap(CARD_W.roundToInt(), CARD_H.roundToInt())
    // Only five grids fit legibly, but the header counts every habit: a card that says
    // "5 hábitos · 99%" when you track eight is a lie told about your own year.
    val shown = habits.take(MAX_HABITS)
    CanvasDrawScope().draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = Canvas(bitmap),
        size = Size(CARD_W, CARD_H),
    ) {
        drawRect(Cream)
        drawHeader(measurer, habits, period, today)
        drawHabits(measurer, shown, period, today)
        drawFooter(measurer)
    }
    return bitmap
}

private fun DrawScope.drawHeader(
    measurer: TextMeasurer,
    habits: List<Habit>,
    period: SharePeriod,
    today: LocalDate,
) {
    val title = when (period) {
        SharePeriod.WEEK -> S.weekOf(startOfWeek(today).day, startOfWeek(today).month.ordinal)
        SharePeriod.MONTH -> "${S.months[today.month.ordinal].replaceFirstChar { it.uppercase() }} ${today.year}"
        SharePeriod.YEAR -> "${today.year}"
    }
    text(measurer, title, MARGIN, 90f, 64f, FontWeight.Light, Ink)

    val days = periodDays(period, today)
    var done = 0
    var scheduled = 0
    habits.forEach { habit ->
        days.forEach { day ->
            if (day <= today && day >= habit.created && habit.countsOn(day)) {
                scheduled++
                if (habit.isDoneOn(day)) done++
            }
        }
    }
    val rate = if (scheduled == 0) 0 else (done * 100f / scheduled).roundToInt()
    val summary = S.shareSummary(habits.size, done, scheduled, rate)
    text(measurer, summary, MARGIN, 178f, 34f, FontWeight.Medium, Muted)
}

private const val TITLE_ROOM = 62f
private const val BLOCK_GAP = 40f
private const val WEEK_LABEL_ROOM = 44f

private fun gridRows(period: SharePeriod, today: LocalDate): Int = when (period) {
    SharePeriod.WEEK -> 1
    SharePeriod.MONTH -> monthRows(today)
    SharePeriod.YEAR -> 7
}

private fun gridColumns(period: SharePeriod, today: LocalDate): Int = when (period) {
    SharePeriod.WEEK, SharePeriod.MONTH -> 7
    SharePeriod.YEAR -> yearColumns(today)
}

/**
 * Square cells sized so the blocks fit both the card's width and the height left over
 * once every habit has its share. Without the height half, a month grid overflows the card.
 */
private fun cellSize(period: SharePeriod, today: LocalDate, habitCount: Int, available: Float): Float {
    val byWidth = (CARD_W - 2 * MARGIN) / gridColumns(period, today)
    val extra = if (period == SharePeriod.WEEK) WEEK_LABEL_ROOM else 0f
    val perBlock = (available - (habitCount - 1) * BLOCK_GAP) / habitCount
    val byHeight = (perBlock - TITLE_ROOM - extra) / gridRows(period, today)
    return maxOf(4f, minOf(byWidth, byHeight))
}

private fun DrawScope.drawHabits(
    measurer: TextMeasurer,
    habits: List<Habit>,
    period: SharePeriod,
    today: LocalDate,
) {
    if (habits.isEmpty()) return
    val top = 270f
    val bottom = CARD_H - 150f
    val cell = cellSize(period, today, habits.size, bottom - top)
    val extra = if (period == SharePeriod.WEEK) WEEK_LABEL_ROOM else 0f
    val block = TITLE_ROOM + gridRows(period, today) * cell + extra
    val total = habits.size * block + (habits.size - 1) * BLOCK_GAP
    var y = top + maxOf(0f, (bottom - top - total) / 2f)

    // A height-limited grid (a month, mostly) is narrower than the card: centre it.
    val left = maxOf(MARGIN, (CARD_W - gridColumns(period, today) * cell) / 2f)

    habits.forEach { habit ->
        text(measurer, "${habit.emoji} ${habit.name}".trim(), left, y, 38f, FontWeight.SemiBold, Ink)
        val gridTop = y + TITLE_ROOM
        when (period) {
            SharePeriod.WEEK -> drawWeekRow(measurer, habit, today, gridTop, left, cell)
            SharePeriod.MONTH -> drawMonthGrid(habit, today, gridTop, left, cell)
            SharePeriod.YEAR -> drawYearGrid(habit, today, gridTop, left, cell)
        }
        y += block + BLOCK_GAP
    }
}

/** Chunky squares, one per weekday, with the day initial underneath. */
private fun DrawScope.drawWeekRow(
    measurer: TextMeasurer,
    habit: Habit,
    today: LocalDate,
    top: Float,
    left: Float,
    cell: Float,
) {
    val start = startOfWeek(today)
    val gap = cell * 0.16f
    val box = cell - gap

    repeat(7) { index ->
        val date = start.plus(DatePeriod(days = index))
        val x = left + index * cell
        drawRoundRect(
            color = cellColor(habit, date, today),
            topLeft = Offset(x, top),
            size = Size(box, box),
            cornerRadius = CornerRadius(box * 0.28f),
        )
        text(
            measurer,
            S.dayInitials[index],
            x + box / 2 - 8f,
            top + box + 10f,
            26f,
            FontWeight.Medium,
            if (date == today) Ink else Muted,
        )
    }
}

/** A real calendar month: weeks as rows, so the shape of a good month is readable. */
private fun DrawScope.drawMonthGrid(habit: Habit, today: LocalDate, top: Float, left: Float, cell: Float) {
    val first = LocalDate(today.year, today.month, 1)
    val daysInMonth = daysInMonth(today)
    val offset = first.dayOfWeek.isoDayNumber - 1

    val gap = cell * 0.18f
    val box = cell - gap

    repeat(daysInMonth) { index ->
        val date = first.plus(DatePeriod(days = index))
        val slot = offset + index
        drawRoundRect(
            color = cellColor(habit, date, today),
            topLeft = Offset(left + (slot % 7) * cell, top + (slot / 7) * cell),
            size = Size(box, box),
            cornerRadius = CornerRadius(box * 0.26f),
        )
    }
}

/** The full year, seven rows of weeks. */
private fun DrawScope.drawYearGrid(habit: Habit, today: LocalDate, top: Float, left: Float, cell: Float) {
    val jan1 = LocalDate(today.year, 1, 1)
    val daysInYear = LocalDate(today.year, 12, 31).dayOfYear
    val offset = jan1.dayOfWeek.isoDayNumber - 1
    val box = cell * 0.82f

    repeat(daysInYear) { index ->
        val date = jan1.plus(DatePeriod(days = index))
        val slot = offset + index
        drawRoundRect(
            color = cellColor(habit, date, today),
            topLeft = Offset(left + (slot / 7) * cell, top + (slot % 7) * cell),
            size = Size(box, box),
            cornerRadius = CornerRadius(box * 0.3f),
        )
    }
}

private fun cellColor(habit: Habit, date: LocalDate, today: LocalDate): Color {
    val accent = Color(habit.colorArgb)
    val count = habit.countOn(date)
    return when {
        count >= habit.target -> accent
        count > 0 -> accent.copy(alpha = 0.45f)
        date > today || date < habit.created -> Empty.copy(alpha = 0.4f)
        habit.isScheduledOn(date) -> Empty
        else -> Empty.copy(alpha = 0.55f)
    }
}

private fun DrawScope.drawFooter(measurer: TextMeasurer) {
    val y = CARD_H - 100f
    text(measurer, S.shareFooter, MARGIN, y, 30f, FontWeight.Medium, Muted)

    val dot = 26f
    val palette = listOf(0xFFF0AFBE, 0xFFF5C39B, 0xFFB6D6AB, 0xFF9CD3C7, 0xFFA2C3E9, 0xFFD9AFE6)
    palette.forEachIndexed { index, argb ->
        drawRoundRect(
            color = Color(argb),
            topLeft = Offset(CARD_W - MARGIN - (palette.size - index) * (dot + 10f), y + 4f),
            size = Size(dot, dot),
            cornerRadius = CornerRadius(dot * 0.35f),
        )
    }
}

private fun DrawScope.text(
    measurer: TextMeasurer,
    value: String,
    x: Float,
    y: Float,
    size: Float,
    weight: FontWeight,
    color: Color,
) {
    drawText(
        textMeasurer = measurer,
        text = value,
        style = TextStyle(color = color, fontSize = size.sp, fontWeight = weight),
        topLeft = Offset(x, y),
    )
}

private fun daysInMonth(date: LocalDate): Int =
    LocalDate(date.year, date.month, 1).plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1)).day

private fun monthRows(date: LocalDate): Int {
    val offset = LocalDate(date.year, date.month, 1).dayOfWeek.isoDayNumber - 1
    return (offset + daysInMonth(date) + 6) / 7
}

private fun yearColumns(date: LocalDate): Int {
    val offset = LocalDate(date.year, 1, 1).dayOfWeek.isoDayNumber - 1
    return (offset + LocalDate(date.year, 12, 31).dayOfYear + 6) / 7
}

private fun startOfWeek(date: LocalDate): LocalDate =
    date.minus(DatePeriod(days = date.dayOfWeek.isoDayNumber - 1))

private fun periodDays(period: SharePeriod, today: LocalDate): List<LocalDate> = when (period) {
    SharePeriod.WEEK -> (0 until 7).map { startOfWeek(today).plus(DatePeriod(days = it)) }
    SharePeriod.MONTH -> {
        val first = LocalDate(today.year, today.month, 1)
        (0 until daysInMonth(today)).map { first.plus(DatePeriod(days = it)) }
    }
    SharePeriod.YEAR -> {
        val jan1 = LocalDate(today.year, 1, 1)
        (0 until LocalDate(today.year, 12, 31).dayOfYear).map { jan1.plus(DatePeriod(days = it)) }
    }
}
