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
    
    var sipNumber = "8877"
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
        api.getInfoByPhone(phone: phone, completionHandler: { nameItem, error in
            if let nameItem = nameItem {
                self.sipNameItem = .result(nameItem)
            } else {
                self.sipNameItem = .error(error?.localizedDescription ?? "error")
            }
        })
    }
}

