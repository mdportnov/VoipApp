import SwiftUI

struct ProfileScreen: View {
    @ObservedObject private(set) var viewModel: ProfileViewModel

    @State private var isSettingsVisible: Bool = false
    @State private var isProfileVisible: Bool = true

    var body: some View {
        NavigationView{
            ProfileView(viewModel: viewModel)
                .navigationBarTitle("Профиль")
                .toolbar {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button(action:  {
                            self.isSettingsVisible = true
                            self.isProfileVisible = false
                        }){
                            NavigationLink(destination: SettingsScreen(userSettings: viewModel.userSettings), isActive: $isSettingsVisible){
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
        ProfileScreen(viewModel: .init(api: .init(), userSettings: UserSettings(callerVM: .init(), catalogVM: .init())))
    }
}
