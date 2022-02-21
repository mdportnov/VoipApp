import SwiftUI
import shared


enum Tab {
    case caller, catalog, profile, settings, test
}

class ViewRouter: ObservableObject {
    @Published var activeTab: Tab = .catalog

    func open(_ tab: Tab) {
        activeTab = tab
    }
}

class AppState: ObservableObject {
    @Published var inputLine = ""
    @Published var isNumPadVisible = false
}


struct MainRouter: View {
    var api: KtorApiService
    @StateObject private var viewRouter = ViewRouter()
    @StateObject private var appState = AppState()
    var callerViewModel = CallerViewModel()
    var catalogViewModel = CatalogVM()
    var userSettings: UserSettings
    @EnvironmentObject var callManager: CallManager

    init(api: KtorApiService) {
        self.api = api
        userSettings = UserSettings(callerVM: callerViewModel, catalogVM: catalogViewModel)
    }

    var body: some View {
        TabView(selection: $viewRouter.activeTab) {
            CallerScreen(viewModel: callerViewModel)
                    .tabItem {
                        Image(systemName: "phone")
                        Text("Звонки")
                    }
                    .tag(Tab.caller)
                    .environmentObject(callManager)

            CatalogScreen(vm: catalogViewModel)
                    .tabItem {
                        Image(systemName: "house.fill")
                        Text("Каталог")
                    }
                    .tag(Tab.catalog)

            ProfileScreen(viewModel: .init(api: api, userSettings: userSettings))
                    .tabItem {
                        Image(systemName: "person.crop.circle")
                        Text("Профиль")
                    }
                    .tag(Tab.profile)

            DialView().tabItem {
                        Image(systemName: "testtube.2")
                        Text("Тест")
                    }
                    .tag(Tab.test)
        }
                .accentColor(.orange)
                .environmentObject(viewRouter)
                .environmentObject(appState)
    }
}
