package com.baltajmn.habit.data

import com.baltajmn.habit.model.Habit

/**
 * Flat surface for the AppIntents that live in the Swift app target. Siri and Spotlight need ids
 * and names, not the Kotlin model, and an intent can run with the app long suspended, so the
 * mutating call re-reads the file first.
 */
object HabitShortcuts {

    /** Active habits, in the order the app lists them. */
    fun list(): List<HabitSummary> {
        HabitRepository.load()
        return HabitRepository.activeHabits.map { it.summary() }
    }

    /** Only what today actually asks for: what "mark my habit" is almost always about. */
    fun todays(): List<HabitSummary> {
        HabitRepository.load()
        val date = today()
        return HabitRepository.activeHabits.filter { it.countsOn(date) }.map { it.summary() }
    }

    /**
     * The same tap the widget does: 0 -> 1 -> .. -> target -> 0. True when the habit ends up done.
     * Reloads first because the widget writes to the same file from its own process.
     */
    fun mark(habitId: String): Boolean {
        HabitRepository.reload()
        val date = today()
        HabitRepository.cycle(habitId, date)
        return HabitRepository.habits.firstOrNull { it.id == habitId }?.isDoneOn(date) == true
    }

    private fun Habit.summary() = HabitSummary(id, name, emoji)
}

data class HabitSummary(val id: String, val name: String, val emoji: String)
