import SwiftUI
//import Kingfisher

struct ProfileScreen: View {
    @ObservedObject private(set) var viewModel: ProfileViewModel
    
    @State private var isSettingsVisible: Bool = false
    @State private var isProfileVisible: Bool = true

    var body: some View {
        NavigationView{
            ProfileView(viewModel: viewModel)
                .navigationTitle("Профиль")
                .toolbar {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button(action:  {
                            self.isSettingsVisible = true
                            self.isProfileVisible = false
                        }){
                            NavigationLink(destination: SettingsScreen(), isActive: $isSettingsVisible){
                                Image(systemName: "gearshape")
                            }
                        }
                    }
                }
        }
    }
}

struct ProfileScreen_Previews: PreviewProvider {
    static var previews: some View {
        ProfileScreen(viewModel: .init(api: .init()))
    }
}
