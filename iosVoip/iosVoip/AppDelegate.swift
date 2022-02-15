import Foundation
import shared

class AppDelegate: NSObject, UIApplicationDelegate {
    var providerDelegate: AbtoCallKitProvider?
    var phone: AbtoPhoneInterface?
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        KoinKt.doInitKoin()
        return true
    }
}
