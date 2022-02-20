import SwiftUI

struct SettingRowView: View {
    var title : String
    var systemImageName : String
    var info: String?
    
    var body: some View {
        HStack (spacing : 15) {
            Image(systemName: systemImageName).frame(width: 30)
            VStack(alignment: .leading){
                Text (title).font(.system(size: 13)).scaledToFill()
                if info != nil {
                    Text(info!).foregroundColor(.gray)
                        .font(.system(size: 10))
                }
            }
        }
    }
}

struct SettingRowView_Previews: PreviewProvider {
    static var previews: some View {
        SettingRowView(title: "Person", systemImageName: "person", info: "Мой аккаунт Мой аккаунт")
    }
}
