import Foundation
import shared
import CloudKit
import SwiftUI
import Combine

class ProfileViewModel: ObservableObject {
    var clientBuilder = KtorClientBuilder()
    var api: KtorApiService

    @ObservedObject var userSettings: UserSettings

    @Published var username = ""
    @Published var password = ""
    var server = "pbx.mephi.ru"
    @Published var isValid = false
    private var cancellableSet: Set<AnyCancellable> = []

    @State var photoUrl: String
    @State var sipStatus = AccountStatus.loading
    @State var sipNumber = ""
    @Published var sipNameItem = LoadableNameItem.loading

    init(api: KtorApiService, userSettings: UserSettings) {
        self.api = api
        self.userSettings = userSettings
        sipNumber = userSettings.activeAccount.login
        photoUrl = clientBuilder.PHOTO_REQUEST_URL_BY_PHONE + userSettings.activeAccount.login
        getNameByPhone(phone: sipNumber)
//
//        $username.debounce(for: 0.8, scheduler: RunLoop.main)
//                .removeDuplicates()
//                .map {
//                    input in input.count >= 4
//                }
//                .assign(to: \.isValid, on: self)
//                .store(in: &cancellableSet)
    }

    func addNewAccount() {
        if username.count >= 4 && !password.isEmpty{
            userSettings.saveAccount(username: username, password: password)
        }
    }

    func getNameByPhone(phone: String) {
        sipNameItem = .loading

        api.getInfoByPhone(phone: phone, completionHandler: { resource, error in
            switch resource {
            case is ResourceErrorNetworkError<NSArray>: self.sipNameItem = .error("Проблемы с сетью")
            case is ResourceErrorEmptyError<NSArray>:
                self.sipNameItem = .error("Имя недоступно")
            case is ResourceSuccess<NSArray>:
                self.sipNameItem = .result(((resource?.data) as! NSArray)[0] as! NameItem)
            default:
                self.sipNameItem = .error("Что-то пошло не так")
            }

        })
    }
}

