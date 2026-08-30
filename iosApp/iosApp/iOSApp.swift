import SwiftUI
import Shared
import UserNotifications
import WidgetKit

/// Without a delegate iOS swallows a notification whose app is already on screen, so a reminder
/// that fires while the user is looking at Quilt is never seen. Also the place the "you said no"
/// path lands: iOS shows its permission alert once per install and never again, so the only way
/// back is Settings and the app has to take the user there.
private final class Notifications: NSObject, UNUserNotificationCenterDelegate {
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .list, .sound])
    }
}

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase

    /// Held here because `UNUserNotificationCenter.delegate` is weak.
    private let notifications = Notifications()

    init() {
        // WidgetKit has no Kotlin bindings, so the shared code calls back in here.
        Widgets_iosKt.onRefreshWidgets = { WidgetCenter.shared.reloadAllTimelines() }
        UNUserNotificationCenter.current().delegate = notifications
        Reminders.shared.onNeedsPermission = {
            guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
            UIApplication.shared.open(url)
        }
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
