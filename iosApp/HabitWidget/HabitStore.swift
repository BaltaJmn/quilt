import Foundation

/// Mirrors the Kotlin `Habit` / `Store` written by the app.
/// The JSON file is the contract between the two: keep both sides in step.
struct Habit: Codable, Identifiable {
    let id: String
    var name: String
    var emoji: String
    var colorArgb: Int64
    var target: Int
    var scheduleDays: Set<Int>
    /// Days per week that count as success, or nil for the fixed `scheduleDays` schedule.
    /// Optional for the same reason as `skipped`, and mandatory here: this file re-encodes the
    /// whole store on every tap, so a field it does not declare is erased from the user's data.
    var weeklyTarget: Int?
    var reminderMinute: Int?
    var createdAt: String
    var archived: Bool
    var log: [String: Int]
    /// Optional on purpose: files written before skipped days existed have no such key, and an
    /// Optional is the one shape whose synthesised coder both tolerates a missing key and omits
    /// it again rather than writing `null`, which Kotlin's non-nullable `Set<String>` would reject.
    var skipped: Set<String>?

    func count(on day: String) -> Int { log[day] ?? 0 }

    func isDone(on day: String) -> Bool { count(on: day) >= target }

    func isScheduled(on isoWeekday: Int) -> Bool { scheduleDays.contains(isoWeekday) }

    func isSkipped(on day: String) -> Bool { skipped?.contains(day) ?? false }
}

/// What one day looks like in the widget's history strip.
enum DayState {
    /// Done, or excused, or still owed today, or nothing was ever asked.
    case done, skipped, pending, off
}

extension Habit {
    /// A day that actually asks something: scheduled, not excused, and after the habit existed.
    private func counts(on date: Date) -> Bool {
        let key = HabitFile.dayKey(date)
        return key >= createdAt
            && isScheduled(on: HabitFile.isoWeekday(date))
            && !isSkipped(on: key)
    }

    func state(on date: Date) -> DayState {
        let key = HabitFile.dayKey(date)
        if key < createdAt { return .off }
        if isSkipped(on: key) { return .skipped }
        if !isScheduled(on: HabitFile.isoWeekday(date)) { return .off }
        return isDone(on: key) ? .done : .pending
    }

    /// Monday of the ISO week [date] falls in.
    private func weekStart(_ date: Date) -> Date {
        let calendar = Calendar.current
        let start = calendar.startOfDay(for: date)
        return calendar.date(byAdding: .day, value: -(HabitFile.isoWeekday(start) - 1), to: start) ?? start
    }

    /// Days completed in the week [date] belongs to.
    private func doneInWeek(_ date: Date) -> Int {
        let calendar = Calendar.current
        let monday = weekStart(date)
        return (0..<7).reduce(0) { total, offset in
            guard let day = calendar.date(byAdding: .day, value: offset, to: monday) else { return total }
            return total + (isDone(on: HabitFile.dayKey(day)) ? 1 : 0)
        }
    }

    /// Mirrors Kotlin `Habit.weeklyStreak`: consecutive weeks that met the quota, counting back
    /// from the week [today] is in. The week in progress gets the grace today gets in the daily
    /// streak: falling short does not break a run that is not over yet.
    private func weeklyStreak(target: Int, today: Date) -> Int {
        let calendar = Calendar.current
        var monday = weekStart(today)
        var streak = 0
        var isCurrentWeek = true
        // Stop once the whole week predates the habit. Capped anyway so a corrupt date cannot spin.
        for _ in 0..<3_000 {
            guard let sunday = calendar.date(byAdding: .day, value: 6, to: monday),
                  HabitFile.dayKey(sunday) >= createdAt else { break }
            if doneInWeek(monday) >= target {
                streak += 1
            } else if !isCurrentWeek {
                break
            }
            isCurrentWeek = false
            guard let previous = calendar.date(byAdding: .day, value: -7, to: monday) else { break }
            monday = previous
        }
        return streak
    }

    /// Mirrors Kotlin `Habit.streak`: consecutive scheduled days completed counting back from
    /// [today]; today still pending does not break it, skipped days are stepped over.
    func streak(today: Date) -> Int {
        if let weeklyTarget { return weeklyStreak(target: weeklyTarget, today: today) }
        let calendar = Calendar.current
        var date = calendar.startOfDay(for: today)
        var streak = 0
        var isFirstScheduledDay = true
        // The `createdAt` guard inside `counts` stops the walk from paying off; cap it anyway so a
        // corrupt date cannot spin forever.
        for _ in 0..<20_000 {
            if HabitFile.dayKey(date) < createdAt { break }
            if counts(on: date) {
                if isDone(on: HabitFile.dayKey(date)) {
                    streak += 1
                } else if !isFirstScheduledDay {
                    break
                }
                isFirstScheduledDay = false
            }
            guard let previous = calendar.date(byAdding: .day, value: -1, to: date) else { break }
            date = previous
        }
        return streak
    }
}

struct HabitStore: Codable {
    var version: Int
    var habits: [Habit]
    var isPro: Bool
}

enum HabitFile {
    static let appGroup = "group.com.baltajmn.habit"

    private static var url: URL? {
        FileManager.default
            .containerURL(forSecurityApplicationGroupIdentifier: appGroup)?
            .appendingPathComponent("habits.json")
    }

    /// Kept in step with `Storage.ios.kt`: a widget write must not be the mutation that drops the
    /// recoverable copy of a user's history.
    private static var backupURL: URL? {
        url?.appendingPathExtension("bak")
    }

    /// yyyy-MM-dd in the device's own time zone, matching kotlinx-datetime's `LocalDate.toString()`.
    private static let isoFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

    static func dayKey(_ date: Date) -> String { isoFormatter.string(from: date) }

    static var todayKey: String { dayKey(Date()) }

    /// ISO weekday, 1 = Monday .. 7 = Sunday. `Calendar` counts Sunday as 1.
    static func isoWeekday(_ date: Date) -> Int {
        let weekday = Calendar.current.component(.weekday, from: date)
        return weekday == 1 ? 7 : weekday - 1
    }

    static var todayIsoWeekday: Int { isoWeekday(Date()) }

    /// [count] days ending today, oldest first.
    static func lastDays(_ count: Int) -> [Date] {
        let calendar = Calendar.current
        let today = calendar.startOfDay(for: Date())
        return (0..<count).reversed().compactMap {
            calendar.date(byAdding: .day, value: -$0, to: today)
        }
    }

    static func read() -> HabitStore? {
        for file in [url, backupURL].compactMap({ $0 }) {
            if let store = decodedStore(at: file) { return store }
        }
        return nil
    }

    static func write(_ store: HabitStore) {
        guard let url, let data = try? JSONEncoder().encode(store) else { return }
        // Do not replace a good backup with a corrupt primary file. The Kotlin repository makes
        // the same distinction while repairing a damaged store.
        // Write the bytes rather than remove-then-copy: a crash between the two left no backup
        // at all, which is the exact case the backup exists for.
        if let backupURL, decodedStore(at: url) != nil,
           let current = try? Data(contentsOf: url) {
            try? current.write(to: backupURL, options: .atomic)
        }
        try? data.write(to: url, options: .atomic)
    }

    private static func decodedStore(at url: URL) -> HabitStore? {
        guard let data = try? Data(contentsOf: url) else { return nil }
        return try? JSONDecoder().decode(HabitStore.self, from: data)
    }

    /// Habits that actually ask something today, in the order the app shows them.
    static func todaysHabits() -> [Habit] {
        let weekday = todayIsoWeekday
        let day = todayKey
        return (read()?.habits ?? []).filter {
            !$0.archived && $0.isScheduled(on: weekday) && !$0.isSkipped(on: day)
        }
    }

    /// Same rule as the app: 0 -> 1 -> .. -> target -> 0.
    static func cycle(habitId: String) {
        guard var store = read(),
              let index = store.habits.firstIndex(where: { $0.id == habitId }) else { return }
        let day = todayKey
        let habit = store.habits[index]
        let next = habit.count(on: day) + 1
        store.habits[index].log[day] = next <= habit.target ? next : nil
        // A day cannot be both done and excused; the app enforces the same invariant.
        store.habits[index].skipped?.remove(day)
        write(store)
    }
}
