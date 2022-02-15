import SwiftUI
import shared
import Kingfisher
import AbtoSipClientWrapper

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    let api = KtorApiService()
    
    var body: some Scene {
        WindowGroup {
            MainRouter(api: api)
        }
    }
}
