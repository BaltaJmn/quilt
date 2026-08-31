import AppIntents
import SwiftUI
import WidgetKit

// MARK: - Habit picker

/// The habit chosen when the user long presses the widget. Separate from the app target's entity
/// on purpose: this one reads the shared file directly, so it works with the app never launched.
struct YearHabit: AppEntity {
    let id: String
    let name: String
    let emoji: String

    // AppIntents metadata is extracted at build time, so these three must be literals. The
    // extension ships no .lproj, so they stay English; everything the widget itself draws goes
    // through L like the rest.
    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Habit"

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(title: "\(emoji) \(name)")
    }

    static var defaultQuery = YearHabitQuery()
}

struct YearHabitQuery: EntityQuery {
    private func active() -> [YearHabit] {
        (HabitFile.read()?.habits ?? [])
            .filter { !$0.archived }
            .map { YearHabit(id: $0.id, name: $0.name, emoji: $0.emoji) }
    }

    func entities(for identifiers: [String]) async throws -> [YearHabit] {
        active().filter { identifiers.contains($0.id) }
    }

    func suggestedEntities() async throws -> [YearHabit] { active() }
}

struct SelectYearHabitIntent: WidgetConfigurationIntent {
    static var title: LocalizedStringResource = "Habit"

    @Parameter(title: "Habit")
    var habit: YearHabit?
}

// MARK: - Timeline

struct YearEntry: TimelineEntry {
    let date: Date
    let name: String
    let emoji: String
    let color: Color
    /// One state per day of the current year, starting at January 1st.
    let states: [DayState]
    /// Row January 1st sits on, 0 = Monday. The grid reads down a column, like the app's.
    let firstRow: Int

    var doneCount: Int { states.filter { $0 == .done }.count }
}

struct YearProvider: AppIntentTimelineProvider {
    private func days(_ today: Date) -> [Date] {
        let calendar = Calendar.current
        let year = calendar.component(.year, from: today)
        guard let first = calendar.date(from: DateComponents(year: year, month: 1, day: 1)) else { return [] }
        let count = calendar.range(of: .day, in: .year, for: first)?.count ?? 365
        return (0..<count).compactMap { calendar.date(byAdding: .day, value: $0, to: first) }
    }

    private func entry(for selection: YearHabit?, now: Date = Date()) -> YearEntry {
        let habits = (HabitFile.read()?.habits ?? []).filter { !$0.archived }
        // Nothing picked yet is the normal first state of a freshly dropped widget.
        let habit = habits.first { $0.id == selection?.id } ?? habits.first
        let dates = days(now)
        guard let habit, let first = dates.first else {
            return YearEntry(
                date: now, name: L.pickHabit, emoji: "", color: emptySlot,
                states: dates.map { _ in DayState.off }, firstRow: 0
            )
        }
        return YearEntry(
            date: now,
            name: habit.name,
            emoji: habit.emoji,
            color: accent(habit.colorArgb),
            states: dates.map { habit.state(on: $0) },
            firstRow: HabitFile.isoWeekday(first) - 1
        )
    }

    func placeholder(in context: Context) -> YearEntry { entry(for: nil) }

    func snapshot(for configuration: SelectYearHabitIntent, in context: Context) async -> YearEntry {
        entry(for: configuration.habit)
    }

    func timeline(for configuration: SelectYearHabitIntent, in context: Context) async -> Timeline<YearEntry> {
        let now = Date()
        // Through the calendar and not 86_400 seconds, as HabitWidget.swift already does: a DST
        // day is 23 or 25 hours long, and the fixed arithmetic either refreshes an hour early or
        // sits an hour past the rollover showing yesterday.
        let calendar = Calendar.current
        let midnight = calendar.date(byAdding: .day, value: 1, to: calendar.startOfDay(for: now))
            ?? now.addingTimeInterval(86_400)
        return Timeline(entries: [entry(for: configuration.habit, now: now)], policy: .after(midnight))
    }
}

// MARK: - View

private struct YearGrid: View {
    let entry: YearEntry

    private func fill(_ state: DayState) -> Color {
        switch state {
        case .done: return entry.color
        case .skipped: return muted.opacity(0.35)
        case .pending: return emptySlot
        case .off: return emptySlot.opacity(0.5)
        }
    }

    var body: some View {
        GeometryReader { geometry in
            let slots = entry.firstRow + entry.states.count
            let columns = max(1, Int(ceil(Double(slots) / 7)))
            let cell = min(geometry.size.width / CGFloat(columns), geometry.size.height / 7)
            let gap = max(cell * 0.16, 0.5)
            let side = cell - gap
            Canvas { context, _ in
                for (index, state) in entry.states.enumerated() {
                    let slot = index + entry.firstRow
                    let rect = CGRect(
                        x: CGFloat(slot / 7) * cell,
                        y: CGFloat(slot % 7) * cell,
                        width: side,
                        height: side
                    )
                    context.fill(
                        Path(roundedRect: rect, cornerRadius: side * 0.3),
                        with: .color(fill(state))
                    )
                }
            }
            .frame(width: CGFloat(columns) * cell, height: cell * 7)
        }
    }
}

struct HabitYearWidgetView: View {
    let entry: YearEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 4) {
                if !entry.emoji.isEmpty { Text(entry.emoji).font(.caption) }
                Text(entry.name)
                    .font(.caption).fontWeight(.semibold)
                    .foregroundStyle(onSurface)
                    .lineLimit(1)
                Spacer(minLength: 4)
                if !entry.emoji.isEmpty {
                    Text("\(entry.doneCount)")
                        .font(.caption2).monospacedDigit()
                        .foregroundStyle(muted)
                }
            }
            YearGrid(entry: entry)
        }
        .padding(12)
        .containerBackground(surface, for: .widget)
    }
}

struct HabitYearWidget: Widget {
    var body: some WidgetConfiguration {
        AppIntentConfiguration(
            kind: "HabitYearWidget",
            intent: SelectYearHabitIntent.self,
            provider: YearProvider()
        ) { entry in
            HabitYearWidgetView(entry: entry)
        }
        .configurationDisplayName(L.yearTitle)
        .description(L.yearDescription)
        .supportedFamilies([.systemMedium, .systemLarge])
    }
}
