import Foundation
import Combine

class UserSettings: ObservableObject {
    public var accounts = ["09024", "09025", "8877"]
    
    @Published var account: String {
           didSet {
               UserDefaults.standard.set(account, forKey: "account")
           }
       }
    
   
    @Published var isBackgroung: Bool {
        didSet {
            UserDefaults.standard.set(isBackgroung, forKey: "isBackgroung")
        }
    }
    
    init() {
        self.account = UserDefaults.standard.object(forKey: "account") as? String ?? ""
        self.isBackgroung = UserDefaults.standard.object(forKey: "isBackgroung") as? Bool ?? true
    }
}
