import Foundation
import Combine
import Toaster
import shared

class Account: Identifiable, Codable {
    var login: String = ""
    var password: String = ""
    var isActive: Bool = false

    init(login: String, password: String, isActive: Bool) {
        self.login = login
        self.password = password
        self.isActive = isActive
    }
}

class UserSettings: ObservableObject {
    let savedAccountsKey = "SavedAccounts"
    let activeAccountKey = "ActiveAccount"

    let searchDB = SearchDB()
    var callerViewModel: CallerViewModel
    var catalogViewModel: CatalogVM

    init(callerVM: CallerViewModel, catalogVM: CatalogVM) {
        callerViewModel = callerVM
        catalogViewModel = catalogVM

        activeAccount = UserDefaults.standard.object(forKey: activeAccountKey) as? Account ?? Account(login: "", password: "", isActive: false)
        isBackground = UserDefaults.standard.object(forKey: "isBackground") as? Bool ?? true

        accounts = []
        accounts.append(Account(login: "09024", password: "wT8WzGjoUUptxkcu", isActive: true))

        if let data = UserDefaults.standard.data(forKey: savedAccountsKey) {
            if let decoded = try? JSONDecoder().decode([Account].self, from: data) {
                accounts = decoded
                return
            }
        }
    }

    @Published var accounts: [Account]

    @Published var activeAccount: Account {
        didSet {
            if let encoded = try? JSONEncoder().encode(activeAccount) {
                UserDefaults.standard.set(encoded, forKey: activeAccountKey)
            }
            changeActiveAccount(activeAccount.login)
        }
    }

    @Published var isBackground: Bool {
        didSet {
            UserDefaults.standard.set(isBackground, forKey: "isBackground")
        }
    }

    func changeActiveAccount(_ login: String) {
        accounts.forEach { account in
            if account.login == login {
                account.isActive = true
            } else {
                account.isActive = false
            }
        }
        Toast(text: "Активный аккаунт: \(activeAccount.login)", duration: Delay.short).show()
        save()
    }

    func save() {
        if let encoded = try? JSONEncoder().encode(accounts) {
            UserDefaults.standard.set(encoded, forKey: savedAccountsKey)
        }
    }

    func saveAccount(username: String, password: String) {
        accounts.forEach { account in
            account.isActive = false
        }
        accounts.append(Account(login: username, password: password, isActive: true))
        save()
    }

    func deleteSearchHistory() {
        searchDB.deleteAll()
    }

    func deleteCallsHistory() {
        callerViewModel.deleteAllRecords()
    }

    func deleteCatalogCache() {
        catalogViewModel.deleteCache()
    }
}
