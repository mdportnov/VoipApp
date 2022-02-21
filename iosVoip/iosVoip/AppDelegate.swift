import Foundation
import shared

class AppDelegate: UIResponder, UIApplicationDelegate {
//    var providerDelegate: AbtoCallKitProvider?
    var phone: AbtoPhoneInterface?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        KoinKt.doInitKoin()

        return true
    }
}
