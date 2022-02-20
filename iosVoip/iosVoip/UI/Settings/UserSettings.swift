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

        isBackground = UserDefaults.standard.object(forKey: "isBackground") as? Bool ?? true
        accounts = []
        activeAccount = UserDefaults.standard.object(forKey: activeAccountKey) as? Account ?? Account(login: "", password: "", isActive: false)

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
        }
    }

    @Published var isBackground: Bool {
        didSet {
            UserDefaults.standard.set(isBackground, forKey: "isBackground")
        }
    }

    func changeActiveAccount(_ login: String) {
        var newAccounts: [Account] = []

        accounts.forEach { account in
            if account.login == login {
                activeAccount = Account(login: account.login, password: account.password, isActive: true)
                newAccounts.append(activeAccount)
            } else {
                newAccounts.append(Account(login: account.login, password: account.password, isActive: false))
            }
        }

        accounts = newAccounts
        save()
        Toast(text: "Активный аккаунт: \(activeAccount.login)", duration: Delay.short).show()
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

    func deleteAccount(accountToDelete: Account) {
        accounts = accounts.filter { account in
            account.login != accountToDelete.login
        }
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
