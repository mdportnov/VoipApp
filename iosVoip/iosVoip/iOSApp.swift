import SwiftUI
import shared
import Kingfisher

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    let api = KtorApiService()
//    let searchDao = SearchDB()
//    let catalogDao = CatalogDao()
//    let catalogRepository = CatalogRepository()
    
    var body: some Scene {
        WindowGroup {
            MainRouter(api: api)
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        KoinKt.doInitKoin()
        return true
    }
}
