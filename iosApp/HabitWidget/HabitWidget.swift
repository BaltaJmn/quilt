import AppIntents
import SwiftUI
import WidgetKit

private let surface = Color(UIColor { $0.userInterfaceStyle == .dark
    ? UIColor(red: 0.125, green: 0.114, blue: 0.086, alpha: 1)
    : UIColor(red: 0.984, green: 0.973, blue: 0.953, alpha: 1) })
private let onSurface = Color(UIColor { $0.userInterfaceStyle == .dark
    ? UIColor(red: 0.925, green: 0.898, blue: 0.851, alpha: 1)
    : UIColor(red: 0.224, green: 0.208, blue: 0.180, alpha: 1) })
private let muted = Color(UIColor { $0.userInterfaceStyle == .dark
    ? UIColor(red: 0.612, green: 0.580, blue: 0.525, alpha: 1)
    : UIColor(red: 0.545, green: 0.518, blue: 0.475, alpha: 1) })
private let emptySlot = Color(UIColor { $0.userInterfaceStyle == .dark
    ? UIColor(red: 0.173, green: 0.157, blue: 0.125, alpha: 1)
    : UIColor(red: 0.941, green: 0.922, blue: 0.886, alpha: 1) })
/// Pastel accents stay light in both themes, so the tick keeps a fixed dark ink.
private let ink = Color(red: 0.180, green: 0.165, blue: 0.141)
/// The palette's green, reserved for "everything done today".
private let allDone = Color(red: 0.714, green: 0.839, blue: 0.671)

private func accent(_ argb: Int64) -> Color {
    Color(
        red: Double((argb >> 16) & 0xFF) / 255,
        green: Double((argb >> 8) & 0xFF) / 255,
        blue: Double(argb & 0xFF) / 255
    )
}

// MARK: - Timeline

/// One habit with everything the view needs already computed. Streaks walk the whole history, so
/// they are worked out once per timeline entry rather than on every redraw.
struct HabitRow: Identifiable {
    let habit: Habit
    let streak: Int
    let week: [DayState]
    var id: String { habit.id }
}

struct HabitEntry: TimelineEntry {
    let date: Date
    let rows: [HabitRow]
    let today: String

    var doneCount: Int { rows.filter { $0.habit.isDone(on: today) }.count }
    var isAllDone: Bool { !rows.isEmpty && doneCount == rows.count }
}

struct HabitProvider: TimelineProvider {
    private func entry() -> HabitEntry {
        let now = Date()
        let week = HabitFile.lastDays(7)
        let rows = HabitFile.todaysHabits().map { habit in
            HabitRow(
                habit: habit,
                streak: habit.streak(today: now),
                week: week.map { habit.state(on: $0) }
            )
        }
        return HabitEntry(date: now, rows: rows, today: HabitFile.dayKey(now))
    }

    /// The widget gallery renders this before the user has picked anything, and at that point the
    /// real store is usually empty, so the entry that sells the widget is the "nothing scheduled"
    /// one. Sample rows instead, only ever on a preview.
    private func sampleEntry() -> HabitEntry {
        let today = HabitFile.todayKey
        let emojis = ["\u{1F4A7}", "\u{1F4D6}", "\u{1F6B6}"]
        let colors: [Int64] = [0xFF9CD3C7, 0xFFB4B8EC, 0xFFB6D6AB]
        let weeks: [[DayState]] = [
            [.done, .done, .done, .skipped, .done, .done, .done],
            [.done, .pending, .done, .done, .off, .done, .pending],
            [.done, .done, .skipped, .done, .done, .done, .done],
        ]
        let rows = L.sampleHabits.enumerated().map { index, name in
            HabitRow(
                habit: Habit(
                    id: "sample-\(index)",
                    name: name,
                    emoji: emojis[index],
                    colorArgb: colors[index],
                    target: 1,
                    scheduleDays: [1, 2, 3, 4, 5, 6, 7],
                    reminderMinute: nil,
                    createdAt: "2000-01-01",
                    archived: false,
                    log: index == 1 ? [:] : [today: 1],
                    skipped: nil
                ),
                streak: [12, 3, 7][index],
                week: weeks[index]
            )
        }
        return HabitEntry(date: Date(), rows: rows, today: today)
    }

    func placeholder(in context: Context) -> HabitEntry { sampleEntry() }

    func getSnapshot(in context: Context, completion: @escaping (HabitEntry) -> Void) {
        completion(context.isPreview ? sampleEntry() : entry())
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<HabitEntry>) -> Void) {
        // Nothing changes on its own until the day rolls over. Added through the calendar rather
        // than 86_400 seconds: a DST day is 23 or 25 hours long, and the fixed arithmetic either
        // refreshes an hour early or sits an hour past the rollover showing yesterday.
        let calendar = Calendar.current
        let midnight = calendar.date(byAdding: .day, value: 1, to: calendar.startOfDay(for: Date()))
            ?? Date().addingTimeInterval(86_400)
        completion(Timeline(entries: [entry()], policy: .after(midnight)))
    }
}

struct ToggleHabitIntent: AppIntent {
    static var title: LocalizedStringResource = "Mark habit"
    static var isDiscoverable: Bool = false

    @Parameter(title: "Habit") var habitId: String

    init() {}
    init(habitId: String) { self.habitId = habitId }

    func perform() async throws -> some IntentResult {
        HabitFile.cycle(habitId: habitId)
        WidgetCenter.shared.reloadAllTimelines()
        return .result()
    }
}

// MARK: - Pieces

/// The tappable square. Filled with the habit's colour once done, part-filled from the bottom
/// while a multi-step habit is under way, so a glance shows progress and not just done/not done.
private struct MarkSquare: View {
    let habit: Habit
    let today: String
    let side: CGFloat

    var body: some View {
        let count = habit.count(on: today)
        let done = count >= habit.target
        let fraction = habit.target > 0 ? min(1, Double(count) / Double(habit.target)) : 0
        let shape = RoundedRectangle(cornerRadius: side * 0.28, style: .continuous)

        ZStack(alignment: .bottom) {
            Rectangle().fill(emptySlot)
            Rectangle().fill(accent(habit.colorArgb)).frame(height: side * fraction)
        }
        .frame(width: side, height: side)
        .clipShape(shape)
        .overlay {
            Text(done ? "✓" : (count > 0 ? "\(count)" : habit.emoji))
                .font(.system(size: side * (done ? 0.5 : 0.42), weight: .bold))
                .foregroundStyle(count > 0 ? ink : onSurface)
                .minimumScaleFactor(0.6)
        }
    }
}

/// The last seven days, oldest on the left. Same square language as the app's year grid, so the
/// widget reads as a zoomed-in slice of it: momentum, not just today.
private struct WeekStrip: View {
    let habit: Habit
    let week: [DayState]
    let box: CGFloat

    private func fill(_ state: DayState) -> Color {
        switch state {
        case .done: return accent(habit.colorArgb)
        case .pending: return emptySlot
        case .skipped: return muted.opacity(0.35)
        case .off: return emptySlot.opacity(0.45)
        }
    }

    var body: some View {
        HStack(spacing: box * 0.35) {
            ForEach(Array(week.enumerated()), id: \.offset) { _, state in
                RoundedRectangle(cornerRadius: box * 0.3, style: .continuous)
                    .fill(fill(state))
                    .frame(width: box, height: box)
            }
        }
    }
}

private struct StreakBadge: View {
    let streak: Int
    let color: Color
    let size: CGFloat

    var body: some View {
        // A one-day streak is just "done today"; showing it would be noise on every row.
        if streak >= 2 {
            HStack(spacing: 1) {
                Image(systemName: "flame.fill").font(.system(size: size * 0.85))
                Text("\(streak)").font(.system(size: size, weight: .semibold))
            }
            .foregroundStyle(color)
        }
    }
}

private struct Header: View {
    let entry: HabitEntry

    var body: some View {
        VStack(spacing: 4) {
            HStack(alignment: .firstTextBaseline) {
                Text(L.today)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundStyle(muted)
                Spacer(minLength: 4)
                Text("\(entry.doneCount)/\(entry.rows.count)")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(entry.isAllDone ? allDone : onSurface)
            }
            GeometryReader { geometry in
                let fraction = entry.rows.isEmpty
                    ? 0
                    : Double(entry.doneCount) / Double(entry.rows.count)
                ZStack(alignment: .leading) {
                    Capsule().fill(emptySlot)
                    Capsule()
                        .fill(entry.isAllDone ? allDone : onSurface.opacity(0.55))
                        .frame(width: geometry.size.width * fraction)
                }
            }
            .frame(height: 3)
        }
    }
}

// MARK: - Layouts

/// Small has room for the squares and nothing else, so it drops the names and leans on the emoji.
private struct SmallView: View {
    let entry: HabitEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Header(entry: entry)
            if entry.rows.isEmpty {
                EmptyToday()
            } else {
                let side: CGFloat = entry.rows.count == 1 ? 94 : 46
                LazyVGrid(
                    columns: Array(
                        repeating: GridItem(.fixed(side), spacing: 8),
                        count: entry.rows.count == 1 ? 1 : 2
                    ),
                    spacing: 8
                ) {
                    ForEach(entry.rows.prefix(4)) { row in
                        Button(intent: ToggleHabitIntent(habitId: row.habit.id)) {
                            MarkSquare(habit: row.habit, today: entry.today, side: side)
                        }
                        .buttonStyle(.plain)
                    }
                }
                Spacer(minLength: 0)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

/// Medium and large share one row; only the count and the sizes differ.
private struct ListView: View {
    let entry: HabitEntry
    let maxRows: Int
    let side: CGFloat
    let box: CGFloat

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Header(entry: entry)
            if entry.rows.isEmpty {
                EmptyToday()
            } else {
                ForEach(entry.rows.prefix(maxRows)) { row in
                    Button(intent: ToggleHabitIntent(habitId: row.habit.id)) {
                        HStack(spacing: 10) {
                            MarkSquare(habit: row.habit, today: entry.today, side: side)
                            let done = row.habit.isDone(on: entry.today)
                            Text(row.habit.name)
                                .font(.system(size: 14, weight: done ? .regular : .medium))
                                .foregroundStyle(done ? muted : onSurface)
                                .lineLimit(1)
                            Spacer(minLength: 4)
                            StreakBadge(
                                streak: row.streak,
                                color: done ? accent(row.habit.colorArgb) : muted,
                                size: 11
                            )
                            WeekStrip(habit: row.habit, week: row.week, box: box)
                        }
                        // Rows share the leftover height instead of leaving a void under a short
                        // list, capped so two habits do not become two enormous bands.
                        .frame(maxWidth: .infinity, maxHeight: side * 1.7)
                    }
                    .buttonStyle(.plain)
                }
                // Overflow is worth naming: a widget that silently hides habits is a widget you
                // stop trusting.
                if entry.rows.count > maxRows {
                    Text("+\(entry.rows.count - maxRows)")
                        .font(.system(size: 11, weight: .medium))
                        .foregroundStyle(muted)
                }
                Spacer(minLength: 0)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct EmptyToday: View {
    var body: some View {
        VStack(spacing: 6) {
            Image(systemName: "moon.zzz").font(.system(size: 20)).foregroundStyle(muted)
            Text(L.nothingToday)
                .font(.system(size: 12))
                .foregroundStyle(muted)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct HabitWidgetView: View {
    @Environment(\.widgetFamily) private var family
    let entry: HabitEntry

    var body: some View {
        Group {
            switch family {
            case .systemSmall: SmallView(entry: entry)
            case .systemLarge: ListView(entry: entry, maxRows: 7, side: 34, box: 10)
            default: ListView(entry: entry, maxRows: 3, side: 28, box: 8)
            }
        }
        .containerBackground(surface, for: .widget)
    }
}

struct HabitTodayWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "HabitTodayWidget", provider: HabitProvider()) { entry in
            HabitWidgetView(entry: entry)
        }
        .configurationDisplayName(L.today)
        .description(L.widgetDescription)
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge])
    }
}

@main
struct HabitWidgetBundle: WidgetBundle {
    var body: some Widget { HabitTodayWidget() }
}

/// The widget's three strings. Same five languages and same fallback rule as the app's Kotlin table.
enum L {
    /// `Locale.current` resolves against the bundle's own .lproj list, and the extension ships
    /// none, so it always answers English. The user's actual preference is the app's rule too.
    private static let lang: String = {
        let code = String((Locale.preferredLanguages.first ?? "en").prefix(2)).lowercased()
        return ["en", "es", "pt", "de", "fr"].contains(code) ? code : "en"
    }()

    private static func t(_ en: String, _ es: String, _ pt: String, _ de: String, _ fr: String) -> String {
        switch lang {
        case "es": return es
        case "pt": return pt
        case "de": return de
        case "fr": return fr
        default: return en
        }
    }

    static let today = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui")
    static let nothingToday = t(
        "Nothing scheduled for today",
        "Nada programado para hoy",
        "Nada programado para hoje",
        "Für heute nichts geplant",
        "Rien de prévu aujourd'hui"
    )
    /// Only the gallery preview ever shows these.
    static let sampleHabits = [
        t("Water", "Agua", "\u{00C1}gua", "Wasser", "Eau"),
        t("Read", "Leer", "Ler", "Lesen", "Lire"),
        t("Walk", "Caminar", "Caminhar", "Spazieren", "Marcher"),
    ]
    static let widgetDescription = t(
        "Tick off today's habits",
        "Marca tus hábitos de hoy",
        "Marque seus hábitos de hoje",
        "Hake deine heutigen Gewohnheiten ab",
        "Cochez vos habitudes du jour"
    )
}
