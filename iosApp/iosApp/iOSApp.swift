import SwiftUI
import Shared
import WidgetKit

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase

    init() {
        // WidgetKit has no Kotlin bindings, so the shared code calls back in here.
        Widgets_iosKt.onRefreshWidgets = { WidgetCenter.shared.reloadAllTimelines() }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onChange(of: scenePhase) { _, phase in
                    // The widget writes to the same file from its own process.
                    if phase == .active { HabitRepository.shared.reload() }
                }
        }
    }
}
