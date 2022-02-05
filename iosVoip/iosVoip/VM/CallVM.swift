import Foundation
import shared

class CallViewModel : ObservableObject {
    var clientBuilder = KtorClientBuilder()
    var api: KtorApiService
    var callerPhotoUrl: String
    var callerNumber: String
    
    init(api: KtorApiService, callerNumber: String) {
        self.callerNumber = callerNumber
        self.api = api
        self.callerPhotoUrl = clientBuilder.PHOTO_REQUEST_URL_BY_PHONE + callerNumber
    }

}
