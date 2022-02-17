import SwiftUI

struct NewAccountView: View {
    @ObservedObject private(set) var viewModel: ProfileViewModel
    
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        VStack{
            Section{
                TextField("SIP USER ID", text: $viewModel.username)
                SecureField("SIP PASSWORD", text: $viewModel.password)
            }.padding()
            Section{
                Button(action: {
                    
                }){
                    Text("Добавить").padding()
                }.disabled(!self.viewModel.isValid)
            }.padding()
        }.background(colorScheme == .dark ? .black : .white)
    }
}

struct NewAccountView_Previews: PreviewProvider {
    static var previews: some View {
        NewAccountView(viewModel: .init(api: .init(), userSettings: UserSettings()))
    }
}
