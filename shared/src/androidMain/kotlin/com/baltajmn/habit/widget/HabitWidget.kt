package com.baltajmn.habit.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.baltajmn.habit.data.AndroidContext
import com.baltajmn.habit.data.HabitRepository
import com.baltajmn.habit.data.today
import com.baltajmn.habit.i18n.S
import com.baltajmn.habit.model.Habit
import com.baltajmn.habit.shared.R
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

// Day/night through Android colour resources: Glance 1.1 has no two-tone ColorProvider.
internal val Surface = ColorProvider(R.color.widget_surface)
internal val OnSurface = ColorProvider(R.color.widget_on_surface)
internal val Muted = ColorProvider(R.color.widget_muted)
internal val Empty = ColorProvider(R.color.widget_empty)
internal val Skipped = ColorProvider(R.color.widget_skipped)
internal val Off = ColorProvider(R.color.widget_off)

/** The pastel accents stay light in both themes, so the tick keeps a fixed dark ink. */
private val Ink = ColorProvider(Color(0xFF2E2A24))

/** The palette's green, reserved for "everything done today". */
private val AllDone = ColorProvider(Color(0xFFB6D6AB))

/**
 * Same three shapes as the iOS widget, so both stores show the same product. The widths are
 * narrower than iOS's families because an Android home screen cell is smaller: three columns on a
 * phone is about 210dp, and that is already wide enough for the list.
 */
private val Small = DpSize(140.dp, 140.dp)
private val Medium = DpSize(210.dp, 140.dp)
private val Large = DpSize(210.dp, 260.dp)

private const val WEEK = 7

/** Today's habits, one tap each to mark them done. */
class HabitWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(Small, Medium, Large))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        AndroidContext.init(context)
        HabitRepository.load()
        provideContent { WidgetBody() }
    }
}

class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitWidget()
}

/** What one day looks like in the history strip. */
internal enum class DayState { Done, Skipped, Pending, Off }

internal fun Habit.stateOn(date: LocalDate): DayState = when {
    date < created -> DayState.Off
    isSkippedOn(date) -> DayState.Skipped
    !isScheduledOn(date) -> DayState.Off
    isDoneOn(date) -> DayState.Done
    else -> DayState.Pending
}

@Composable
private fun WidgetBody() {
    val today = today()
    val habits = HabitRepository.activeHabits.filter { it.countsOn(today) }
    val done = habits.count { it.isDoneOn(today) }
    val size = LocalSize.current
    val small = size.width < Medium.width
    val large = !small && size.height >= Large.height

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(Surface)
            .cornerRadius(20.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Header(done, habits.size)
        Spacer(GlanceModifier.height(if (small) 10.dp else 6.dp))
        when {
            habits.isEmpty() -> Text(
                text = S.widgetEmpty,
                style = TextStyle(color = Muted, fontSize = 12.sp),
            )
            small -> SmallGrid(habits, today)
            else -> HabitList(habits, today, large)
        }
    }
}

@Composable
private fun Header(done: Int, total: Int) {
    val complete = total > 0 && done == total
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = S.widgetTitle,
            style = TextStyle(color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium),
        )
        Spacer(GlanceModifier.defaultWeight())
        Text(
            text = "$done/$total",
            style = TextStyle(
                color = if (complete) AllDone else OnSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
    Spacer(GlanceModifier.height(4.dp))
    LinearProgressIndicator(
        progress = if (total == 0) 0f else done.toFloat() / total,
        modifier = GlanceModifier.fillMaxWidth().height(3.dp),
        color = if (complete) AllDone else OnSurface,
        backgroundColor = Empty,
    )
}

/** Small drops the names and leans on the emoji: a 2x2 of squares is all that fits. */
@Composable
private fun SmallGrid(habits: List<Habit>, today: LocalDate) {
    val shown = habits.take(4)
    val side = if (shown.size == 1) 94.dp else 46.dp
    // Centred rather than top-aligned: a fixed 2x2 in a two-row cell would otherwise leave
    // the bottom half of the widget blank.
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        shown.chunked(2).forEachIndexed { row, pair ->
            if (row > 0) Spacer(GlanceModifier.height(8.dp))
            Row {
                pair.forEachIndexed { index, habit ->
                    if (index > 0) Spacer(GlanceModifier.width(8.dp))
                    // The square is the whole target here: small has no row to hang the tap on.
                    MarkSquare(
                        habit,
                        today,
                        side,
                        GlanceModifier.clickable(
                            actionRunCallback<ToggleHabitAction>(habitIdParameters(habit.id))
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitList(habits: List<Habit>, today: LocalDate, large: Boolean) {
    val maxRows = if (large) 7 else 3
    val shown = habits.take(maxRows)
    // Rows share the leftover height instead of leaving a void under a short list; the floor caps
    // how tall a single row can grow.
    val slots = maxOf(shown.size, if (large) 5 else 3)

    Column(modifier = GlanceModifier.fillMaxSize()) {
        shown.forEach { habit ->
            HabitRow(habit, today, large, GlanceModifier.defaultWeight())
        }
        repeat(slots - shown.size) { Spacer(GlanceModifier.defaultWeight()) }
        if (habits.size > maxRows) {
            // Overflow is worth naming: a widget that silently hides habits is one you stop trusting.
            Text(
                text = "+${habits.size - maxRows}",
                style = TextStyle(color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Medium),
            )
        }
    }
}

@Composable
private fun HabitRow(habit: Habit, today: LocalDate, large: Boolean, modifier: GlanceModifier) {
    val done = habit.isDoneOn(today)
    // An Android home cell is narrower than an iOS one, so the large row grows the square but
    // keeps the medium strip: a wider strip would eat the habit's name.
    val side = if (large) 32.dp else 28.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(actionRunCallback<ToggleHabitAction>(habitIdParameters(habit.id))),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MarkSquare(habit, today, side)
        Spacer(GlanceModifier.width(10.dp))
        Text(
            text = habit.name,
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = if (done) Muted else OnSurface,
                fontSize = 14.sp,
                fontWeight = if (done) FontWeight.Normal else FontWeight.Medium,
            ),
        )
        StreakBadge(habit.streak(today), if (done) Color(habit.colorArgb) else null)
        Spacer(GlanceModifier.width(8.dp))
        WeekStrip(habit, today, 8.dp)
    }
}

/**
 * The tappable square. Filled with the habit's colour once done, part-filled from the bottom while
 * a multi-step habit is under way, so a glance shows progress and not just done/not done.
 */
@Composable
private fun MarkSquare(
    habit: Habit,
    today: LocalDate,
    side: Dp,
    modifier: GlanceModifier = GlanceModifier,
) {
    val count = habit.countOn(today)
    val done = count >= habit.target
    val fraction = if (habit.target > 0) minOf(1f, count.toFloat() / habit.target) else 0f

    Box(
        modifier = modifier
            .size(side)
            .cornerRadius(side * 0.28f)
            .background(Empty),
        contentAlignment = Alignment.Center,
    ) {
        if (fraction > 0f) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(side * fraction)
                        .background(ColorProvider(Color(habit.colorArgb))),
                    content = {},
                )
            }
        }
        Text(
            text = if (done) "✓" else if (count > 0) "$count" else habit.emoji,
            style = TextStyle(
                color = if (count > 0) Ink else OnSurface,
                fontSize = (side.value * if (done) 0.5f else 0.42f).sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

/** A one-day streak is just "done today"; showing it would be noise on every row. */
@Composable
private fun StreakBadge(streak: Int, accent: Color?) {
    if (streak < 2) return
    val color = accent?.let { ColorProvider(it) } ?: Muted
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            provider = ImageProvider(R.drawable.ic_flame),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color),
            modifier = GlanceModifier.size(10.dp),
        )
        Spacer(GlanceModifier.width(1.dp))
        Text(
            text = "$streak",
            style = TextStyle(color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium),
        )
    }
}

/**
 * The last seven days, oldest on the left. Same square language as the app's year grid, so the
 * widget reads as a zoomed-in slice of it: momentum, not just today.
 */
@Composable
private fun WeekStrip(habit: Habit, today: LocalDate, box: Dp) {
    Row(
        modifier = GlanceModifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The gap is padding rather than a Spacer: a Glance Row takes at most ten children, and
        // seven squares plus six spacers is thirteen.
        repeat(WEEK) { index ->
            val date = today.minus(DatePeriod(days = WEEK - 1 - index))
            val fill = when (habit.stateOn(date)) {
                DayState.Done -> ColorProvider(Color(habit.colorArgb))
                DayState.Pending -> Empty
                DayState.Skipped -> Skipped
                DayState.Off -> Off
            }
            Box(
                modifier = GlanceModifier.padding(start = if (index == 0) 0.dp else box * 0.35f),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(box)
                        .cornerRadius(box * 0.3f)
                        .background(fill),
                    content = {},
                )
            }
        }
    }
}
