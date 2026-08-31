package com.baltajmn.habit.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.baltajmn.habit.data.AndroidContext
import com.baltajmn.habit.data.HabitRepository
import com.baltajmn.habit.data.today
import com.baltajmn.habit.i18n.S
import com.baltajmn.habit.model.Habit
import com.baltajmn.habit.shared.R
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlin.math.ceil
import kotlin.math.min

/** Which habit this instance shows. Written by the configuration screen, one value per widget. */
val YearHabitIdKey = stringPreferencesKey("habitId")

/** One habit, the whole year. The same grid the app draws, on the home screen. */
class YearWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        AndroidContext.init(context)
        HabitRepository.load()
        provideContent { YearBody() }
    }
}

class YearWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = YearWidget()
}

@Composable
private fun YearBody() {
    val context = LocalContext.current
    val habitId = currentState(YearHabitIdKey)
    // Falling back to the first habit keeps a widget added without configuring it from being blank.
    val habit = HabitRepository.activeHabits.firstOrNull { it.id == habitId }
        ?: HabitRepository.activeHabits.firstOrNull()

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(Surface)
            .cornerRadius(20.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        if (habit == null) {
            Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = S.widgetEmpty,
                    style = TextStyle(color = Muted, fontSize = 12.sp),
                )
            }
            return@Column
        }

        val year = today().year
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${habit.emoji} ${habit.name}",
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(color = OnSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium),
            )
            Text(
                text = "${habit.doneDaysIn(year)}",
                style = TextStyle(color = Muted, fontSize = 12.sp),
            )
        }
        Spacer(modifier = GlanceModifier.height(6.dp))
        // Glance has no canvas, and 365 Boxes is far past the element budget of a RemoteViews
        // tree, so the grid is drawn once into a bitmap and sent as a single image.
        Image(
            provider = ImageProvider(yearBitmap(context, habit, LocalSize.current, year)),
            contentDescription = habit.name,
            modifier = GlanceModifier.fillMaxSize(),
        )
    }
}

private fun Habit.doneDaysIn(year: Int): Int {
    val first = LocalDate(year, 1, 1)
    return (0 until daysInYear(year)).count { isDoneOn(first.plus(DatePeriod(days = it))) }
}

private fun daysInYear(year: Int): Int = LocalDate(year, 12, 31).dayOfYear

private fun yearBitmap(context: Context, habit: Habit, size: DpSize, year: Int): Bitmap {
    val density = context.resources.displayMetrics.density
    // The widget's own padding and the header row are outside the image.
    val width = ((size.width.value - 24) * density).toInt().coerceAtLeast(1)
    val height = ((size.height.value - 46) * density).toInt().coerceAtLeast(1)

    val first = LocalDate(year, 1, 1)
    val days = daysInYear(year)
    val firstRow = first.dayOfWeek.isoDayNumber - 1
    val columns = ceil((firstRow + days) / 7f).toInt()
    val cell = min(width / columns.toFloat(), height / 7f)
    val gap = (cell * 0.16f).coerceAtLeast(0.5f)
    val side = cell - gap
    val radius = side * 0.3f

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val done = habit.colorArgb.toInt()
    val empty = context.getColor(R.color.widget_empty)
    val skipped = context.getColor(R.color.widget_skipped)
    val off = context.getColor(R.color.widget_off)

    repeat(days) { index ->
        val slot = index + firstRow
        val left = (slot / 7) * cell
        val top = (slot % 7) * cell
        paint.color = when (habit.stateOn(first.plus(DatePeriod(days = index)))) {
            DayState.Done -> done
            DayState.Skipped -> skipped
            DayState.Pending -> empty
            DayState.Off -> off
        }
        canvas.drawRoundRect(left, top, left + side, top + side, radius, radius, paint)
    }
    return bitmap
}
