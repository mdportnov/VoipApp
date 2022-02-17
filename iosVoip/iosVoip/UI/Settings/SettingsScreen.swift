import SwiftUI
import Toaster

struct SettingsScreen: View {
    @ObservedObject var userSettings: UserSettings
    @State private var showingAlertDeleteHistory = false
    @State private var showingAlertDeleteCache = false

    init(userSettings: UserSettings) {
        self.userSettings = userSettings
    }

    var body: some View {
        NavigationView {
            VStack {
                List {
                    Section(header: Text("SIP")) {
                        Picker(selection: $userSettings.activeAccount.login, label: SettingRowView(title: "Мои аккаунты", systemImageName: "person.2.fill")) {
                            ForEach(userSettings.accounts, id: \.login) { account in
                                HStack {
                                    if account.isActive {
                                        Image(systemName: "checkmark.circle.fill").foregroundColor(.green)
                                    }
                                    Text(account.login).onTapGesture {
                                        userSettings.changeActiveAccount(account.login)
                                    }
                                }
                            }
                        }


                        NavigationLink(destination: EmptyView(), label: {
                            SettingRowView(title: "SIP настройки",
                                    systemImageName: "phone")
                        })

                        Toggle(isOn: $userSettings.isBackground) {
                            SettingRowView(title: "Работать в фоновом режиме",
                                    systemImageName: "square.3.layers.3d.down.backward")
                        }
                    }

                    Section(header: Text("Данные")) {
                        SettingRowView(title: "Удалить историю запросов",
                                systemImageName: "xmark.bin")
                                .onTapGesture {
                                    showingAlertDeleteHistory.toggle()
                                }
                                .alert("Вы действительно хотите очистить историю запросов?", isPresented: $showingAlertDeleteHistory) {
                                    Button("Да", role: .cancel) {
                                    }
                                }

                        SettingRowView(title: "Удалить кэш каталога",
                                systemImageName: "lineweight")
                                .onTapGesture {
                                    showingAlertDeleteCache.toggle()
                                }
                                .alert("Вы действительно хотите очистить кэш каталога?", isPresented: $showingAlertDeleteCache) {
                                    Button("Да", role: .cancel) {
                                    }
                                }
                    }

                    Section(header: Text("О приложении")) {
                        SettingRowView(title: "Отправить фидбэк",
                                systemImageName: "paperplane", info: "Сообщить о технических вопросах или предложить новые функции")
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
        SettingsScreen(userSettings: UserSettings())
    }
}
