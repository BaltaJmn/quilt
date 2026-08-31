import AppIntents
import Shared
import WidgetKit

/// A habit as Siri and Spotlight see it. The list comes from Kotlin, which owns the file.
struct HabitShortcutEntity: AppEntity {
    let id: String
    let name: String
    let emoji: String

    static var typeDisplayRepresentation: TypeDisplayRepresentation { "Habit" }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(title: "\(emoji) \(name)")
    }

    static var defaultQuery = HabitShortcutQuery()
}

struct HabitShortcutQuery: EntityQuery {
    func entities(for identifiers: [String]) async throws -> [HabitShortcutEntity] {
        HabitShortcuts.shared.list().filter { identifiers.contains($0.id) }.map(HabitShortcutEntity.init)
    }

    func suggestedEntities() async throws -> [HabitShortcutEntity] {
        // Only what today asks for: "mark my habit" is almost never about a habit off duty.
        HabitShortcuts.shared.todays().map(HabitShortcutEntity.init)
    }
}

private extension HabitShortcutEntity {
    init(_ summary: HabitSummary) {
        self.init(id: summary.id, name: summary.name, emoji: summary.emoji)
    }
}

/// The same tap the widget does, from Siri, Spotlight or the Shortcuts app.
struct MarkHabitIntent: AppIntent {
    static var title: LocalizedStringResource = "Mark habit"
    static var description = IntentDescription("Ticks a habit off for today.")
    static var openAppWhenRun = false

    @Parameter(title: "Habit")
    var habit: HabitShortcutEntity

    init() {}

    init(habit: HabitShortcutEntity) { self.habit = habit }

    func perform() async throws -> some IntentResult {
        _ = HabitShortcuts.shared.mark(habitId: habit.id)
        WidgetCenter.shared.reloadAllTimelines()
        // No dialog on purpose: Siri would speak the phrase in the app's language, not the one
        // the user is talking to it in.
        return .result()
    }
}

/// Apple requires this in the app target, not in the extension.
struct QuiltShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: MarkHabitIntent(),
            phrases: [
                "Mark a habit in \(.applicationName)",
                "Marcar un hábito en \(.applicationName)",
                "Marcar um hábito no \(.applicationName)",
                "Gewohnheit in \(.applicationName) abhaken",
                "Cocher une habitude dans \(.applicationName)",
            ],
            shortTitle: "Mark habit",
            systemImageName: "checkmark.square"
        )
    }
}
