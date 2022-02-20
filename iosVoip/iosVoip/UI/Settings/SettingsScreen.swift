import SwiftUI
import Toaster

struct SettingsScreen: View {
    @ObservedObject var userSettings: UserSettings
    @State private var showingAlertDeleteHistory = false
    @State private var showingAlertDeleteCache = false
    @State private var showingAlertDeleteCalls = false
    @State private var showingAlertDeleteAccount = false
    @State private var showingAlertActiveAccount = false
    @State private var selectedAccount = Account(login: "", password: "", isActive: false)

    init(userSettings: UserSettings) {
        self.userSettings = userSettings
    }

    @State private var refreshingID = UUID()

    var body: some View {
        VStack {
            List {
                Section(header: Text("SIP")) {
                    NavigationLink(destination: {
                        VStack {
                            ForEach(userSettings.accounts, id: \.login) { account in
                                HStack {
                                    Spacer()
                                    Button(action: {
                                        selectedAccount = account
                                        if selectedAccount.isActive != true {
                                            showingAlertActiveAccount.toggle()
                                        }
                                    }) {
                                        Image(systemName: account.isActive ? "checkmark.circle.fill" : "circle").foregroundColor(.green)
                                                .alert(isPresented: $showingAlertActiveAccount) {
                                                    Alert(
                                                            title: Text("Вы действительно хотите сделать аккаунт \(selectedAccount.login) активным?"),
                                                            message: Text(selectedAccount.isActive ? "Сейчас он активен" : ""),
                                                            primaryButton: .default(Text("Да")) {
                                                                userSettings.changeActiveAccount(selectedAccount.login)
                                                            },
                                                            secondaryButton: .destructive(Text("Нет"))
                                                    )
                                                }
                                        Text(account.login)
                                    }

                                    Button(action: {
                                        selectedAccount = account
                                        showingAlertDeleteAccount.toggle()
                                    }) {
                                        Image(systemName: "xmark.bin").foregroundColor(.red)
                                    }
                                            .alert(isPresented: $showingAlertDeleteAccount) {
                                                Alert(
                                                        title: Text("Вы действительно хотите удалить аккаунт: \(selectedAccount.login)?"),
                                                        message: Text(selectedAccount.isActive ? "Сейчас он активен" : ""),
                                                        primaryButton: .destructive(Text("Да")) {
                                                            userSettings.deleteAccount(accountToDelete: selectedAccount)
                                                        },
                                                        secondaryButton: .cancel(Text("Нет"))
                                                )
                                            }
                                    Spacer()
                                }
                                        .frame(width: .infinity, height: 70)
                            }
                            Spacer()
                        }
                                .navigationBarTitle(Text("Мои аккаунты"))
                    }, label: {
                        SettingRowView(title: "Мои аккаунты", systemImageName: "person.2.fill")
                    })

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
                            .alert(isPresented: $showingAlertDeleteHistory) {
                                Alert(
                                        title: Text("Вы действительно хотите очистить историю запросов?"),
                                        message: Text("Вы их вводили при поиске в каталоге"),
                                        primaryButton: .destructive(Text("Да")) {
                                            userSettings.deleteSearchHistory()
                                        },
                                        secondaryButton: .cancel(Text("Нет"))
                                )
                            }
                    SettingRowView(title: "Удалить кэш каталога",
                            systemImageName: "lineweight")
                            .onTapGesture {
                                showingAlertDeleteCache.toggle()
                            }
                            .alert(isPresented: $showingAlertDeleteCache) {
                                Alert(
                                        title: Text("Вы действительно хотите очистить кэш каталога?"),
                                        message: Text("Загруженные данные для офлайн доступа к каталогу"),
                                        primaryButton: .destructive(Text("Да")) {
                                            userSettings.deleteCatalogCache()
                                        },
                                        secondaryButton: .cancel(Text("Нет"))
                                )
                            }
                    SettingRowView(title: "Удалить записи звонков",
                            systemImageName: "phone.arrow.right")
                            .onTapGesture {
                                showingAlertDeleteCalls.toggle()
                            }
                            .alert(isPresented: $showingAlertDeleteCalls) {
                                Alert(
                                        title: Text("Вы действительно хотите удалить записи звонков?"),
                                        primaryButton: .destructive(Text("Да")) {
                                            userSettings.deleteCallsHistory()
                                        },
                                        secondaryButton: .cancel(Text("Нет"))
                                )
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
                .navigationBarTitle(Text("Настройки"))
    }
}

struct SettingsScreen_Previews: PreviewProvider {
    static var previews: some View {
        SettingsScreen(userSettings: UserSettings(callerVM: .init(), catalogVM: .init()))
    }
}
