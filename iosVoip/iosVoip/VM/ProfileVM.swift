import Foundation
import shared
import CloudKit
import SwiftUI
import Combine

class ProfileViewModel : ObservableObject {
    var clientBuilder = KtorClientBuilder()
    var api: KtorApiService
    var photoUrl: String
    
    @Published var username = ""
    @Published var password = ""
    @Published var isValid = false
    private var cancellableSet: Set<AnyCancellable> = []
    
    @State var sipStatus = AccountStatus.loading
    
    var sipNumber = "09024"
    @Published var sipNameItem = LoadableNameItem.loading
    
    init(api: KtorApiService) {
        self.api = api
        self.photoUrl = clientBuilder.PHOTO_REQUEST_URL_BY_PHONE + sipNumber
        self.getNameByPhone(phone: sipNumber)
        
        $username.debounce(for: 0.8, scheduler: RunLoop.main)
            .removeDuplicates()
            .map{
                input in return input.count>=4
            }
            .assign(to: \.isValid, on: self)
            .store(in: &cancellableSet)
    }
    
    func getNameByPhone(phone: String) {
        self.sipNameItem = .loading
        
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

