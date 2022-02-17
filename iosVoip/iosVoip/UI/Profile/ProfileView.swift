import SwiftUI
import Kingfisher

let lightGreyColor = Color(red: 239.0/255.0, green: 243.0/255.0, blue: 244.0/255.0, opacity: 1.0)

struct ProfileView: View {
    @ObservedObject private(set) var viewModel: ProfileViewModel
    @State var isLoginControlVisible = false
    @State var buttonText = "Добавить аккаунт"
    
    init(viewModel: ProfileViewModel){
        self.viewModel = viewModel
        buttonText = isLoginControlVisible ? "Добавить" : "Добавить аккаунт"
    }
    
    var body: some View {
        VStack(alignment: .center){
            HStack{
                Image("logo_voip").shadow(color: .black.opacity(0.3), radius: 3)
                ZStack(alignment: .bottomTrailing){
                    KFImage(URL(string: viewModel.photoUrl)!)
                        .resizable()
                        .placeholder{
                            Image(systemName: "person").foregroundColor(.gray)
                        }
                        .fade(duration: 0.4)
                        .cacheOriginalImage()
                        .scaledToFit()
                        .shadow(color: .black.opacity(0.3), radius: 3)
                        .frame(width: 70)
                        .clipShape(Circle())
                    
                    ZStack{
                        Circle().fill(.white)
                            .frame(width: 30, height: 30, alignment: .bottomTrailing)
                            .shadow(color: .black.opacity(0.3), radius: 3)
                            .padding(3)
                        Image(systemName: "checkmark.circle").foregroundColor(.green)
                    }
                }
            }
            VStack(alignment: .leading){
                HStack{
                    Text("Имя: ").foregroundColor(.orange).bold()
                    nameText().bold()
                }
                HStack{
                    Text("Номер SIP: ").foregroundColor(.orange).bold()
                    Text(viewModel.sipNumber).bold()
                }
                HStack{
                    Text("Статус: ").foregroundColor(.orange).bold()
                    Text(viewModel.sipStatus.status).bold()
                }
                if isLoginControlVisible {
                        TextField("SIP USER ID", text: $viewModel.username)
                            .keyboardType(.numberPad)
                            .padding(10.0)
                            .background(lightGreyColor)
                            .cornerRadius(5.0)
                        SecureField("SIP PASSWORD", text: $viewModel.password)
                            .padding(10.0)
                            .background(lightGreyColor)
                            .cornerRadius(5.0)
                }
            }.padding(10.0)
            Spacer()
            HStack(alignment: .center){
                Spacer()
                Button(buttonText) {
                    UIApplication.shared.endEditing()
                    isLoginControlVisible.toggle()
                    self.buttonText = isLoginControlVisible ? "Добавить" : "Добавить аккаунт"
                    viewModel.addNewAccount()
                }.buttonStyle(GrowingButton())
            }.padding()
        }
    }
    
    private func nameText() -> Text {
        switch viewModel.sipNameItem {
        case .loading: return Text("...")
        case .error(let description):
            return Text(description)
        case .result(let nameItem):
            return Text(nameItem.display_name)
        }
    }
}

extension UIApplication {
    func endEditing() {
        sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
    }
}

//struct ProfileView_Previews: PreviewProvider {
//    static var previews: some View {
//        ProfileView(viewModel: .init(api: .init()))
//    }
//}
