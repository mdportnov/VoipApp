import SwiftUI
import Kingfisher

struct ProfileView: View {
    @ObservedObject private(set) var viewModel: ProfileViewModel
    
    var body: some View {
        VStack{
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
            }
            Spacer()
            HStack(alignment: .center){
                Spacer()
                Button("Добавить аккаунт") {
                   
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

struct ProfileView_Previews: PreviewProvider {
    static var previews: some View {
        ProfileView(viewModel: .init(api: .init()))
    }
}
