package com.baltajmn.habit.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.baltajmn.habit.model.Habit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
private data class Store(
    val version: Int = 1,
    val habits: List<Habit> = emptyList(),
    val isPro: Boolean = false,
)

/**
 * Single source of truth. The whole dataset is one JSON file rewritten on every mutation.
 * ponytail: synchronous whole-file write on the caller's thread; move to a coroutine + debounce
 * if the file ever grows past a few hundred KB (roughly 50 habits x 5 years).
 */
object HabitRepository {

    /** Deliberately generous: the free tier is the trial, since one-off purchases have none. */
    const val FREE_HABIT_LIMIT = 5

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val _habits = mutableStateListOf<Habit>()

    val habits: List<Habit> get() = _habits
    val activeHabits: List<Habit> get() = _habits.filter { !it.archived }

    var isPro by mutableStateOf(false)
        private set

    private var loaded = false
    private var repairPrimary = false

    fun load() {
        if (loaded) return
        loaded = true
        // An unreadable file is not an empty one: fall back before starting from scratch,
        // otherwise the next save would overwrite a year of history with nothing.
        val store = decode(Storage.read())
            ?: decode(Storage.readPrevious())?.also { repairPrimary = true }
            ?: return
        _habits.addAll(store.habits)
        isPro = store.isPro
        // Repair the damaged file now: waiting for the user's next tap leaves a window where
        // one more failure would take the backup with it.
        if (repairPrimary) save(syncReminders = false)
        Reminders.sync(_habits)
    }

    /** Re-read the file. The home screen widget writes to it from another process. */
    fun reload() {
        loaded = false
        _habits.clear()
        load()
    }

    fun canAddHabit(): Boolean = isPro || activeHabits.size < FREE_HABIT_LIMIT

    fun add(
        name: String,
        emoji: String,
        colorArgb: Long,
        target: Int = 1,
        scheduleDays: Set<Int> = Habit.ALL_DAYS,
        reminderMinute: Int? = null,
    ): Habit? {
        if (!canAddHabit()) return null
        val habit = Habit(
            id = newId(),
            name = name.trim(),
            emoji = emoji,
            colorArgb = colorArgb,
            target = target,
            scheduleDays = scheduleDays,
            reminderMinute = reminderMinute,
            createdAt = today().toString(),
        )
        _habits.add(habit)
        save()
        return habit
    }

    fun update(habit: Habit) {
        val index = _habits.indexOfFirst { it.id == habit.id }
        if (index < 0) return
        _habits[index] = habit
        save()
    }

    fun delete(id: String) {
        Reminders.cancel(id)
        _habits.removeAll { it.id == id }
        save()
    }

    /** Cycles the day's count 0 -> 1 -> .. -> target -> 0, so one tap both checks and un-checks. */
    fun cycle(habitId: String, date: LocalDate) {
        val index = _habits.indexOfFirst { it.id == habitId }
        if (index < 0) return
        val habit = _habits[index]
        val next = (habit.countOn(date) + 1).takeIf { it <= habit.target } ?: 0
        val log = habit.log.toMutableMap()
        if (next == 0) log.remove(date.toString()) else log[date.toString()] = next
        // Logging a day un-excuses it: a day cannot be both done and skipped.
        _habits[index] = habit.copy(log = log, skipped = habit.skipped - date.toString())
        // Ticking a box cannot change a reminder, so skip the reschedule.
        save(syncReminders = false)
    }

    /**
     * Excuses a day, or takes the excuse back. Holidays and illness should not read as failures,
     * which is the single most repeated complaint in this category's reviews.
     */
    fun toggleSkip(habitId: String, date: LocalDate) {
        val index = _habits.indexOfFirst { it.id == habitId }
        if (index < 0) return
        val habit = _habits[index]
        val key = date.toString()
        val skipped = habit.skipped.toMutableSet()
        val log = habit.log.toMutableMap()
        if (key in skipped) {
            skipped.remove(key)
        } else {
            skipped.add(key)
            log.remove(key)
        }
        _habits[index] = habit.copy(log = log, skipped = skipped)
        save(syncReminders = false)
    }

    /** Mirrors the store's entitlement. Cached in the file so a launch offline still shows Pro. */
    fun updatePro(value: Boolean) {
        if (isPro == value) return
        isPro = value
        save(syncReminders = false)
    }

    private fun save(syncReminders: Boolean = true) {
        Storage.write(
            json.encodeToString(Store(habits = _habits.toList(), isPro = isPro)),
            rotateBackup = !repairPrimary,
        )
        repairPrimary = false
        refreshWidgets()
        if (syncReminders) Reminders.sync(_habits)
    }

    /**
     * Moves a habit one slot up ([delta] -1) or down (+1) among the ones the user can see.
     * Archived habits sit in the same list but are stepped over, so the visible order is what moves.
     */
    fun moveBy(habitId: String, delta: Int) {
        val ids = _habits.map { it.id }
        val next = reorder(ids, _habits.filter { it.archived }.map { it.id }.toSet(), habitId, delta)
        if (next == ids) return
        val byId = _habits.associateBy { it.id }
        _habits.clear()
        _habits.addAll(next.map { byId.getValue(it) })
        save(syncReminders = false)
    }

    /**
     * One row per day that has something to say, archived habits included: an export that quietly
     * dropped them would not be the user's data.
     */
    fun exportCsv(): String {
        val out = StringBuilder("habit,date,count,target,status\n")
        for (habit in _habits) {
            for (date in (habit.log.keys + habit.skipped).sorted()) {
                val count = habit.log[date] ?: 0
                val status = when {
                    date in habit.skipped -> "skipped"
                    count >= habit.target -> "done"
                    else -> "partial"
                }
                out.append(csvField(habit.name)).append(',')
                    .append(date).append(',')
                    .append(count).append(',')
                    .append(habit.target).append(',')
                    .append(status).append('\n')
            }
        }
        return out.toString()
    }

    fun exportJson(): String = json.encodeToString(Store(habits = _habits.toList(), isPro = isPro))

    /**
     * Reads a backup file, rejecting anything that is merely valid JSON. Without the shape check,
     * `ignoreUnknownKeys` would turn any object at all into an empty store and silently wipe the
     * year.
     *
     * Returns only the habits on purpose: the type makes it impossible to restore `isPro`, which
     * would otherwise be a paywall bypass anyone could do in a text editor.
     */
    internal fun parseBackup(text: String): List<Habit>? {
        val root = runCatching { json.parseToJsonElement(text) }.getOrNull() as? JsonObject
            ?: return null
        if (root["habits"] !is JsonArray) return null
        return decode(text)?.habits
    }

    /**
     * Replaces every habit with the contents of a backup. Returns false and changes nothing when
     * the file is not one of ours. What it replaced survives in the backup copy.
     */
    fun importJson(text: String): Boolean {
        val imported = parseBackup(text) ?: return false
        _habits.clear()
        _habits.addAll(imported)
        save()
        return true
    }

    private fun decode(text: String?): Store? =
        text?.let { runCatching { json.decodeFromString<Store>(it) }.getOrNull() }

    private fun newId(): String = Random.nextLong().toULong().toString(16)
}

@OptIn(ExperimentalTime::class)
fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

/**
 * A habit name with a comma, a quote or a newline would otherwise shift every column after it.
 * RFC 4180: wrap in quotes, and double any quote inside.
 */
internal fun csvField(value: String): String = "\"" + value.replace("\"", "\"\"") + "\""

/**
 * [ids] after moving [id] one slot up ([delta] -1) or down (+1) among the entries the user can see.
 * Archived habits stay where they are and are stepped over, so what moves is the visible order.
 * Returns [ids] unchanged when the move would fall off either end.
 */
internal fun reorder(ids: List<String>, archived: Set<String>, id: String, delta: Int): List<String> {
    val visible = ids.filter { it !in archived }
    val position = visible.indexOf(id)
    val target = position + delta
    if (position < 0 || target !in visible.indices) return ids
    val from = ids.indexOf(id)
    val to = ids.indexOf(visible[target])
    val out = ids.toMutableList()
    out.add(to, out.removeAt(from))
    return out
}
