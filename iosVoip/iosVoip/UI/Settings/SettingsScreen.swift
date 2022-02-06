import SwiftUI

struct SettingsScreen: View {
    @ObservedObject var userSettings = UserSettings()
    @State private var showingAlertDeleteHistory = false
    @State private var showingAlertDeleteCache = false
    
    var body: some View {
        NavigationView {
            VStack{
                List{
                    Section(header: Text("SIP")) {
                        Picker(selection: $userSettings.account, label: SettingRowView(title: "Мои аккаунты", systemImageName: "person.2.fill")) {
                            ForEach(userSettings.accounts, id: \.self) { account in
                                Text(account)
                            }
                        }
                        
                        NavigationLink(destination: EmptyView(), label: {
                            SettingRowView(title: "SIP настройки",
                                           systemImageName: "phone")
                        })
                        
                        Toggle(isOn: $userSettings.isBackgroung) {
                            SettingRowView(title: "Работать в фоновом режиме",
                                           systemImageName: "square.3.layers.3d.down.backward")
                        }
                    }
                    
                    Section(header: Text("Данные")) {
                        SettingRowView(title: "Удалить историю запросов",
                                       systemImageName:"xmark.bin")
                            .onTapGesture {
                                showingAlertDeleteHistory.toggle()
                            }.alert("Вы действительно хотите очистить историю запросов?", isPresented: $showingAlertDeleteHistory) {
                                Button("Да", role: .cancel) { }
                            }
                        
                        SettingRowView(title: "Удалить кэш каталога",
                                       systemImageName: "lineweight")
                            .onTapGesture {
                                showingAlertDeleteCache.toggle()
                            }.alert("Вы действительно хотите очистить кэш каталога?", isPresented: $showingAlertDeleteCache){
                                Button("Да", role: .cancel) { }
                            }
                    }
                    
                    Section(header: Text("О приложении")) {
                        SettingRowView(title: "Отправить фидбэк",
                                       systemImageName:"paperplane", info: "Сообщить о технических вопросах или предложить новые функции")
                            .onTapGesture {
                                if let url = URL(string: "mailto:voip@mephi.ru") {
                                    if #available(iOS 10.0, *) {
                                        UIApplication.shared.open(url)
                                    } else {
                                        UIApplication.shared.openURL(url)
                                    }
                                }
                            }
                        
                        SettingRowView(title: "Версия ",
                                       systemImageName: "info", info: "0.2.0")
                        
                        SettingRowView(title: "Разработано ",
                                       systemImageName: "hammer", info: "НИЯУ МИФИ, Управление информатизации")
                    }
                }
            }
            .navigationTitle(Text("Настройки"))
            Spacer()
        }
    }
}

struct SettingsScreen_Previews: PreviewProvider {
    static var previews: some View {
        SettingsScreen()
    }
}
