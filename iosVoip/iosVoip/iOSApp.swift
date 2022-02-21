import SwiftUI
import shared
import Kingfisher
import AbtoSipClientWrapper

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    let api = KtorApiService()
    @StateObject var callManager = CallManager()

    var providerDelegate: ProviderDelegate?

    init(){
        providerDelegate = ProviderDelegate(callManager: callManager)
    }

    var body: some Scene {
        WindowGroup {
            MainRouter(api: api)
                    .environmentObject(callManager)
        }
    }
}
