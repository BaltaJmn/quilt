package com.baltajmn.habit

import com.baltajmn.habit.data.HabitRepository
import com.baltajmn.habit.data.csvOf
import com.baltajmn.habit.data.cycled
import com.baltajmn.habit.data.skipToggled
import com.baltajmn.habit.i18n.normalizeLanguage
import com.baltajmn.habit.model.Habit
import com.baltajmn.habit.ui.daysInYear
import com.baltajmn.habit.ui.firstOffset
import com.baltajmn.habit.ui.indexAt
import com.baltajmn.habit.ui.slotOf
import com.baltajmn.habit.ui.yearColumns
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun habit(
    name: String = "test",
    target: Int = 1,
    archived: Boolean = false,
    log: Map<String, Int> = emptyMap(),
    skipped: Set<String> = emptySet(),
) = Habit(
    id = "t",
    name = name,
    colorArgb = 0xFFFFFFFF,
    target = target,
    createdAt = "2026-01-01",
    archived = archived,
    log = log,
    skipped = skipped,
)

private val march10 = LocalDate.parse("2026-03-10")

/** The two mutations a tap can make. They own the invariant that no day is both done and excused. */
class MutationTest {

    @Test
    fun cycling_wraps_round_to_zero_at_the_target() {
        var h = habit(target = 3)
        val counts = (1..4).map {
            h = cycled(h, march10)
            h.countOn(march10)
        }
        assertEquals(listOf(1, 2, 3, 0), counts)
    }

    @Test
    fun cycling_off_removes_the_day_instead_of_logging_a_zero() {
        // A zero left behind would ship in the backup and in the CSV as a day with a row.
        val h = cycled(cycled(habit(), march10), march10)
        assertEquals(emptyMap(), h.log)
    }

    @Test
    fun cycling_a_day_takes_its_excuse_away() {
        val h = cycled(habit(skipped = setOf("2026-03-10")), march10)
        assertEquals(1, h.countOn(march10))
        assertEquals(emptySet(), h.skipped)
    }

    @Test
    fun excusing_a_day_drops_what_was_logged_on_it() {
        val h = skipToggled(habit(log = mapOf("2026-03-10" to 1)), march10)
        assertEquals(0, h.countOn(march10))
        assertTrue(h.isSkippedOn(march10))
    }

    @Test
    fun taking_an_excuse_back_does_not_bring_the_tick_back() {
        val once = skipToggled(habit(log = mapOf("2026-03-10" to 1)), march10)
        val twice = skipToggled(once, march10)
        assertEquals(emptyMap(), twice.log)
        assertEquals(emptySet(), twice.skipped)
    }

    @Test
    fun no_sequence_of_taps_leaves_a_day_both_done_and_excused() {
        // The two states are drawn differently and counted differently, so a day in both is a bug
        // wherever it comes from.
        var h = habit(target = 2)
        for (step in 0 until 12) {
            h = if (step % 3 == 2) skipToggled(h, march10) else cycled(h, march10)
            assertTrue(h.countOn(march10) == 0 || !h.isSkippedOn(march10), "step $step")
        }
    }
}

class CsvTest {

    @Test
    fun rows_are_sorted_by_date_and_say_partial_from_done() {
        val h = habit(
            name = "Leer",
            target = 2,
            log = mapOf("2026-01-02" to 2, "2026-01-01" to 1),
            skipped = setOf("2026-01-03"),
        )
        assertEquals(
            """
            habit,date,count,target,status
            "Leer",2026-01-01,1,2,partial
            "Leer",2026-01-02,2,2,done
            "Leer",2026-01-03,0,2,skipped

            """.trimIndent(),
            csvOf(listOf(h)),
        )
    }

    @Test
    fun archived_habits_are_exported_too() {
        // The export is the user's data, not the app's current screen.
        val out = csvOf(listOf(habit(name = "Viejo", archived = true, log = mapOf("2026-01-02" to 1))))
        assertTrue(out.contains("\"Viejo\",2026-01-02,1,1,done"), out)
    }

    @Test
    fun a_habit_with_no_history_writes_only_the_header() {
        assertEquals("habit,date,count,target,status\n", csvOf(listOf(habit())))
    }
}

class BackupGuardTest {

    private fun backup(habit: String, isPro: Boolean = false): String =
        """{"version":1,"habits":[$habit],"isPro":$isPro}"""

    private val sane =
        """{"id":"a","name":"x","colorArgb":1,"target":1,"createdAt":"2026-01-01"}"""

    @Test
    fun a_sane_habit_is_restored() {
        assertNotNull(HabitRepository.parseBackup(backup(sane)))
    }

    @Test
    fun a_created_date_the_parser_cannot_read_is_rejected() {
        // `created` is re-parsed on every draw, so importing this would not fail once: it would
        // throw on every launch from then on, and the file is read again at each start.
        val broken = """{"id":"a","name":"x","colorArgb":1,"target":1,"createdAt":"ayer"}"""
        assertNull(HabitRepository.parseBackup(backup(broken)))
    }

    @Test
    fun a_target_below_one_is_rejected() {
        // With target 0 every day in the year is done by definition, including days off.
        val broken = """{"id":"a","name":"x","colorArgb":1,"target":0,"createdAt":"2026-01-01"}"""
        assertNull(HabitRepository.parseBackup(backup(broken)))
    }

    @Test
    fun a_weekly_quota_outside_one_to_seven_is_rejected() {
        // A quota of zero hands out a perfect rate and an endless streak over an empty log.
        for (quota in listOf(0, 8, -1)) {
            val broken =
                """{"id":"a","name":"x","colorArgb":1,"target":1,"createdAt":"2026-01-01","weeklyTarget":$quota}"""
            assertNull(HabitRepository.parseBackup(backup(broken)), "weeklyTarget $quota")
        }
    }

    @Test
    fun one_bad_habit_rejects_the_whole_file() {
        // Half an import is worse than none: the file it would replace is already gone.
        val broken = """{"id":"b","name":"y","colorArgb":1,"target":0,"createdAt":"2026-01-01"}"""
        assertNull(HabitRepository.parseBackup(backup("$sane,$broken")))
    }

    @Test
    fun a_created_date_already_in_the_file_degrades_instead_of_crashing() {
        // The guard above keeps this out of new files, but one that got in before the guard existed
        // is read again at every start, so the model itself cannot be the thing that throws.
        val broken = habit(log = mapOf("2026-02-05" to 1)).copy(createdAt = "ayer")
        assertEquals(LocalDate.parse("2026-02-05"), broken.created)
        assertEquals(1, broken.streak(LocalDate.parse("2026-02-05")))
        assertEquals(1, broken.bestStreak(LocalDate.parse("2026-02-06")))
        assertEquals(0.5f, broken.completionRate(2026, LocalDate.parse("2026-02-06")))
    }

    @Test
    fun a_broken_date_with_no_history_at_all_still_answers() {
        val empty = habit().copy(createdAt = "")
        assertEquals(0, empty.streak(LocalDate.parse("2026-02-05")))
        assertEquals(0, empty.bestStreak(LocalDate.parse("2026-02-05")))
    }

    @Test
    fun a_backup_cannot_grant_pro() {
        val before = HabitRepository.isPro
        assertNotNull(HabitRepository.parseBackup(backup(sane, isPro = true)))
        assertEquals(before, HabitRepository.isPro)
    }

    @Test
    fun the_json_keys_of_a_habit_are_the_ones_the_ios_widget_decodes() {
        // HabitStore.swift decodes and re-encodes this same file by hand. A field renamed here and
        // not there disappears from the user's history the next time the widget writes.
        val text = Json { encodeDefaults = true }.encodeToString(habit())
        assertEquals(
            setOf(
                "id", "name", "emoji", "colorArgb", "target", "scheduleDays", "weeklyTarget",
                "reminderMinute", "createdAt", "archived", "log", "skipped",
            ),
            Json.parseToJsonElement(text).jsonObject.keys,
        )
    }
}

/** The tap and the paint have to stay exact inverses, in every year. */
class YearGridMathTest {

    private val years = 2020..2040

    @Test
    fun every_day_of_every_year_survives_the_round_trip() {
        for (year in years) {
            for (index in 0 until daysInYear(year)) {
                val slot = slotOf(year, index)
                assertEquals(index, indexAt(year, slot / 7, slot % 7), "$year day $index")
            }
        }
    }

    @Test
    fun a_leap_year_has_one_more_day_than_its_neighbours() {
        assertEquals(365, daysInYear(2026))
        assertEquals(366, daysInYear(2028))
        assertEquals(365, daysInYear(2100)) // divisible by 100, not by 400
    }

    @Test
    fun a_year_that_starts_on_monday_opens_the_first_column_at_the_top() {
        assertEquals(0, firstOffset(2024)) // 2024-01-01 was a Monday
        assertEquals(0, slotOf(2024, 0))
        assertEquals(0, indexAt(2024, 0, 0))
    }

    @Test
    fun a_year_that_starts_on_sunday_leaves_six_dead_cells_above_january_first() {
        assertEquals(6, firstOffset(2023)) // 2023-01-01 was a Sunday
        for (row in 0..5) assertNull(indexAt(2023, 0, row), "row $row")
        assertEquals(0, indexAt(2023, 0, 6))
    }

    @Test
    fun a_tap_past_december_thirty_first_is_not_a_day() {
        val last = daysInYear(2026) - 1
        val slot = slotOf(2026, last)
        for (row in (slot % 7 + 1)..6) assertNull(indexAt(2026, slot / 7, row), "row $row")
    }

    @Test
    fun a_tap_below_the_last_row_is_not_a_day_a_week_later() {
        // column * 7 + 7 is the same index as the top of the next column, so an unclamped row
        // turned the sliver under the grid into next week's day.
        assertNull(indexAt(2026, 5, 7))
        assertNull(indexAt(2026, 5, -1))
    }

    @Test
    fun the_grid_is_exactly_wide_enough_for_every_year() {
        for (year in years) {
            val slots = firstOffset(year) + daysInYear(year)
            val columns = yearColumns(year)
            assertTrue(columns * 7 >= slots, "$year is $columns columns short")
            assertTrue((columns - 1) * 7 < slots, "$year wastes a column")
        }
    }

    @Test
    fun month_labels_march_left_to_right_from_column_zero() {
        for (year in years) {
            assertEquals(0, slotOf(year, 0) / 7, "January of $year")
            var previous = -1
            for (month in 1..12) {
                val column = slotOf(year, LocalDate(year, month, 1).dayOfYear - 1) / 7
                assertTrue(column > previous, "month $month of $year lands on column $column")
                previous = column
            }
        }
    }
}

class LanguageTest {

    @Test
    fun the_region_is_dropped_and_the_case_ignored() {
        assertEquals("es", normalizeLanguage("es"))
        assertEquals("es", normalizeLanguage("es-419"))
        assertEquals("es", normalizeLanguage("ES_es"))
        assertEquals("pt", normalizeLanguage("pt_BR"))
        assertEquals("fr", normalizeLanguage("fr-CA"))
    }

    @Test
    fun anything_the_app_does_not_ship_lands_in_english() {
        for (tag in listOf("ja", "zh-Hans", "", "x", "ca")) {
            assertEquals("en", normalizeLanguage(tag), tag)
        }
    }
}
