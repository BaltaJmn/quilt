package com.baltajmn.habit

import com.baltajmn.habit.data.HabitRepository
import com.baltajmn.habit.data.csvField
import com.baltajmn.habit.data.reorder
import com.baltajmn.habit.i18n.SUPPORTED
import com.baltajmn.habit.i18n.dayInitialsFor
import com.baltajmn.habit.i18n.formatTime
import com.baltajmn.habit.i18n.monthAbbreviations
import com.baltajmn.habit.i18n.monthNames
import com.baltajmn.habit.i18n.streakText
import com.baltajmn.habit.data.nextReminderAt
import com.baltajmn.habit.model.Habit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HabitTest {

    private fun habit(
        created: String = "2026-01-01",
        target: Int = 1,
        days: Set<Int> = Habit.ALL_DAYS,
        reminderMinute: Int? = null,
        vararg done: String,
    ) = Habit(
        id = "t",
        name = "test",
        colorArgb = 0xFFFFFFFF,
        target = target,
        scheduleDays = days,
        reminderMinute = reminderMinute,
        createdAt = created,
        log = done.associateWith { target },
    )

    @Test
    fun a_skipped_day_does_not_break_the_streak() {
        // Nothing logged on the 9th, but it was excused: the run carries across it.
        val h = habit(done = arrayOf("2026-03-08", "2026-03-10"))
            .copy(skipped = setOf("2026-03-09"))
        assertEquals(2, h.streak(LocalDate.parse("2026-03-10")))
    }

    @Test
    fun a_missed_day_still_breaks_the_streak_when_it_was_not_skipped() {
        val h = habit(done = arrayOf("2026-03-08", "2026-03-10"))
        assertEquals(1, h.streak(LocalDate.parse("2026-03-10")))
    }

    @Test
    fun best_streak_spans_a_skipped_day() {
        val h = habit(done = arrayOf("2026-03-06", "2026-03-07", "2026-03-09", "2026-03-10"))
            .copy(skipped = setOf("2026-03-08"))
        assertEquals(4, h.bestStreak(LocalDate.parse("2026-03-10")))
    }

    @Test
    fun completion_rate_leaves_skipped_days_out_of_the_denominator() {
        // Mon+Wed only. Scheduled up to Jan 14: Jan 5, 7, 12, 14. Excuse the 12th and the 14th.
        val h = habit(days = setOf(1, 3), done = arrayOf("2026-01-05", "2026-01-07"))
            .copy(skipped = setOf("2026-01-12", "2026-01-14"))
        // 2 done out of the 2 days that still asked something.
        assertEquals(1f, h.completionRate(2026, LocalDate.parse("2026-01-14")))
    }

    @Test
    fun a_skipped_day_is_not_a_done_day() {
        val h = habit().copy(skipped = setOf("2026-03-10"))
        assertEquals(0, h.totalDone())
        assertEquals(0, h.streak(LocalDate.parse("2026-03-10")))
    }

    @Test
    fun streak_counts_consecutive_days_back_from_today() {
        val h = habit(done = arrayOf("2026-03-08", "2026-03-09", "2026-03-10"))
        assertEquals(3, h.streak(LocalDate.parse("2026-03-10")))
    }

    @Test
    fun streak_survives_a_pending_today() {
        val h = habit(done = arrayOf("2026-03-08", "2026-03-09"))
        assertEquals(2, h.streak(LocalDate.parse("2026-03-10")))
    }

    @Test
    fun streak_breaks_on_a_missed_day() {
        val h = habit(done = arrayOf("2026-03-06", "2026-03-08", "2026-03-09"))
        assertEquals(2, h.streak(LocalDate.parse("2026-03-09")))
    }

    @Test
    fun unscheduled_days_do_not_break_the_streak() {
        // Mondays only. 2026-03-02, 03-09 and 03-16 are Mondays.
        val h = habit(days = setOf(1), done = arrayOf("2026-03-02", "2026-03-09", "2026-03-16"))
        assertEquals(3, h.streak(LocalDate.parse("2026-03-18")))
    }

    @Test
    fun partial_count_is_not_done() {
        val h = habit(target = 3).copy(log = mapOf("2026-03-10" to 2))
        assertEquals(0, h.streak(LocalDate.parse("2026-03-10")))
    }

    @Test
    fun best_streak_finds_the_longest_past_run() {
        val h = habit(
            done = arrayOf(
                "2026-01-02", "2026-01-03", "2026-01-04", "2026-01-05", // run of 4
                "2026-02-10", "2026-02-11",                             // run of 2
            ),
        )
        assertEquals(4, h.bestStreak(LocalDate.parse("2026-03-01")))
    }

    @Test
    fun best_streak_stops_at_today() {
        val h = habit(done = arrayOf("2026-01-02", "2026-01-03", "2026-01-04"))
        assertEquals(2, h.bestStreak(LocalDate.parse("2026-01-03")))
    }

    @Test
    fun total_done_ignores_partial_days() {
        val h = habit(target = 2).copy(log = mapOf("2026-01-02" to 2, "2026-01-03" to 1, "2026-01-04" to 3))
        assertEquals(2, h.totalDone())
    }

    @Test
    fun completion_rate_ignores_unscheduled_days() {
        // Mon+Wed only, first two weeks of January 2026 (Jan 1 = Thursday).
        val h = habit(days = setOf(1, 3), done = arrayOf("2026-01-05", "2026-01-07"))
        // Scheduled up to Jan 14: Jan 5, 7, 12, 14 -> 2 of 4.
        assertEquals(0.5f, h.completionRate(2026, LocalDate.parse("2026-01-14")))
    }

    // 2026-03-10 is a Tuesday.

    @Test
    fun next_reminder_is_today_while_the_time_has_not_passed() {
        val h = habit(reminderMinute = 9 * 60)
        assertEquals(
            LocalDateTime.parse("2026-03-10T09:00"),
            h.nextReminderAt(LocalDateTime.parse("2026-03-10T08:00")),
        )
    }

    @Test
    fun next_reminder_rolls_over_once_the_time_has_passed() {
        val h = habit(reminderMinute = 9 * 60)
        assertEquals(
            LocalDateTime.parse("2026-03-11T09:00"),
            h.nextReminderAt(LocalDateTime.parse("2026-03-10T09:00")),
        )
    }

    @Test
    fun next_reminder_skips_days_the_habit_is_not_scheduled_on() {
        val h = habit(days = setOf(1), reminderMinute = 9 * 60) // Mondays only
        assertEquals(
            LocalDateTime.parse("2026-03-16T09:00"),
            h.nextReminderAt(LocalDateTime.parse("2026-03-10T08:00")),
        )
    }

    @Test
    fun a_habit_without_a_reminder_has_no_next_time() {
        assertNull(habit().nextReminderAt(LocalDateTime.parse("2026-03-10T08:00")))
    }
}

class BackupTest {

    private val realBackup = """
        {"version":1,"habits":[
          {"id":"a","name":"Leer","emoji":"","colorArgb":4293963710,"target":1,
           "scheduleDays":[1,2,3,4,5,6,7],"reminderMinute":null,"createdAt":"2026-01-01",
           "archived":false,"log":{"2026-01-02":1}}
        ],"isPro":true}
    """.trimIndent()

    @Test
    fun readsARealBackup() {
        val habits = HabitRepository.parseBackup(realBackup)
        assertEquals(1, habits?.size)
        assertEquals("Leer", habits?.first()?.name)
        assertEquals(1, habits?.first()?.countOn(LocalDate.parse("2026-01-02")))
    }

    @Test
    fun rejectsJsonThatIsNotOurs() {
        // Each of these decodes fine with ignoreUnknownKeys, and each would wipe the year.
        assertNull(HabitRepository.parseBackup("{}"))
        assertNull(HabitRepository.parseBackup("""{"foo":1}"""))
        assertNull(HabitRepository.parseBackup("""{"habits":"nope"}"""))
        assertNull(HabitRepository.parseBackup("[]"))
        assertNull(HabitRepository.parseBackup("no es json"))
        assertNull(HabitRepository.parseBackup(""))
    }
}

class StringsTest {

    @Test
    fun streak_plural_matches_the_language() {
        assertEquals("1 day streak", streakText(1, "en"))
        assertEquals("4 day streak", streakText(4, "en"))
        assertEquals("1 día seguido", streakText(1, "es"))
        assertEquals("4 días seguidos", streakText(4, "es"))
        assertEquals("1 dia seguido", streakText(1, "pt"))
        assertEquals("4 dias seguidos", streakText(4, "pt"))
        assertEquals("1 Tag in Folge", streakText(1, "de"))
        assertEquals("4 Tage in Folge", streakText(4, "de"))
        assertEquals("1 jour d'affilée", streakText(1, "fr"))
        assertEquals("4 jours d'affilée", streakText(4, "fr"))
        // An unsupported language falls back to English rather than to an empty string.
        assertEquals("4 day streak", streakText(4, "ja"))
    }

    @Test
    fun time_is_12_hour_only_in_english() {
        assertEquals("7:05 AM", formatTime(7 * 60 + 5, "en"))
        assertEquals("7:30 PM", formatTime(19 * 60 + 30, "en"))
        // midnight and noon are where 12-hour clocks usually break
        assertEquals("12:00 AM", formatTime(0, "en"))
        assertEquals("12:00 PM", formatTime(12 * 60, "en"))
        for (lang in listOf("es", "pt", "de", "fr")) {
            assertEquals("07:05", formatTime(7 * 60 + 5, lang), lang)
            assertEquals("19:30", formatTime(19 * 60 + 30, lang), lang)
        }
    }

    @Test
    fun every_language_has_twelve_months_and_seven_day_initials() {
        // The lists are space-separated literals typed by hand. One missing space and the year
        // grid draws the wrong month label over the wrong column, in that language only.
        for (lang in SUPPORTED) {
            assertEquals(12, monthNames(lang).size, "months in $lang")
            assertEquals(12, monthAbbreviations(lang).size, "short months in $lang")
            assertEquals(7, dayInitialsFor(lang).size, "day initials in $lang")
            assertEquals(emptyList(), monthNames(lang).filter { it.isBlank() }, "blank month in $lang")
            assertEquals(
                emptyList(),
                monthAbbreviations(lang).filter { it.length != 3 },
                "short month names must all be three letters in $lang",
            )
        }
    }
}

class ReorderTest {

    private val all = listOf("a", "b", "c")

    @Test
    fun moves_one_slot_in_each_direction() {
        assertEquals(listOf("b", "a", "c"), reorder(all, emptySet(), "a", 1))
        assertEquals(listOf("a", "c", "b"), reorder(all, emptySet(), "c", -1))
    }

    @Test
    fun refuses_to_fall_off_either_end() {
        assertEquals(all, reorder(all, emptySet(), "a", -1))
        assertEquals(all, reorder(all, emptySet(), "c", 1))
        assertEquals(all, reorder(all, emptySet(), "missing", 1))
    }

    @Test
    fun steps_over_archived_habits() {
        // "x" is archived and invisible, so moving "a" down must put it past "b", not past "x".
        val withArchived = listOf("a", "x", "b")
        assertEquals(listOf("x", "b", "a"), reorder(withArchived, setOf("x"), "a", 1))
        assertEquals(listOf("b", "a", "x"), reorder(withArchived, setOf("x"), "b", -1))
    }

    @Test
    fun csv_fields_survive_a_comma_and_a_quote() {
        // A habit called 'Leer "de verdad", a diario' would otherwise shift every later column.
        assertEquals("\"Leer\"", csvField("Leer"))
        assertEquals("\"a, b\"", csvField("a, b"))
        assertEquals("\"say \"\"hi\"\"\"", csvField("say \"hi\""))
    }
}
