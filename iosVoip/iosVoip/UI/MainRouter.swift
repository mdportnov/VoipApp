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

class AppState: ObservableObject{
    @Published var inputLine = ""
    @Published var isNumPadVisible = false
}


struct MainRouter: View {
    var api: KtorApiService
    @StateObject private var viewRouter = ViewRouter()
    @StateObject private var appState = AppState()

    init(api: KtorApiService){
        self.api = api
    }
    
    var body: some View {
        TabView(selection: $viewRouter.activeTab){
            CallerScreen(viewModel: .init())
                .tabItem{
                    Image(systemName: "phone")
                    Text("Звонки")
                }.tag(Tab.caller)
            
            CatalogScreen(viewModel: .init())
                .tabItem{
                    Image(systemName: "house.fill")
                    Text("Каталог")
                }.tag(Tab.catalog)
            
            ProfileScreen(viewModel: .init(api: api, userSettings: UserSettings()))
                .tabItem{
                    Image(systemName: "person.crop.circle")
                    Text("Профиль")
                }.tag(Tab.profile)
        }.accentColor(.orange)
            .environmentObject(viewRouter)
            .environmentObject(appState)
    }
}
