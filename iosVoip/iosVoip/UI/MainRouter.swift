import SwiftUI
import shared


enum Tab {
    case caller, catalog, profile, settings
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

    init(api: KtorApiService) {
        self.api = api
    }

    var body: some View {
        TabView(selection: $viewRouter.activeTab) {
            CallerScreen(viewModel: callerViewModel)
                    .tabItem {
                        Image(systemName: "phone")
                        Text("Звонки")
                    }
                    .tag(Tab.caller)

            CatalogScreen(vm: catalogViewModel)
                    .tabItem {
                        Image(systemName: "house.fill")
                        Text("Каталог")
                    }
                    .tag(Tab.catalog)

            ProfileScreen(viewModel: .init(api: api, userSettings:
            UserSettings(callerVM: callerViewModel, catalogVM: catalogViewModel)))
                    .tabItem {
                        Image(systemName: "person.crop.circle")
                        Text("Профиль")
                    }
                    .tag(Tab.profile)
        }
                .accentColor(.orange)
                .environmentObject(viewRouter)
                .environmentObject(appState)
    }
}
