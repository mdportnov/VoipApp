import SwiftUI
import shared

struct MainRouter: View {
    var api: KtorApiService
//    var catalogRepository: CatalogRepository

    init(api: KtorApiService){
        self.api = api
    }
    
    var body: some View {
        TabView{
            CallerScreen(viewModel: .init())
                .tabItem{
                    Image(systemName: "phone")
                    Text("Звонки")
                }
            
            CatalogScreen(viewModel: .init())
                .tabItem{
                    Image(systemName: "house.fill")
                    Text("Каталог")
                }
            
            ProfileScreen(viewModel: .init(api: api))
                .tabItem{
                    Image(systemName: "person.crop.circle")
                    Text("Профиль")
                }
        }.accentColor(.orange)
    }
}
