import SwiftUI
import CallKit
import shared
import Toaster

struct IncomingInterfaceView: View {
    @EnvironmentObject var callManager: CallManager

    @Binding var receiverID: String
    @Binding var hasActivateCall: Bool
    @Binding var callID: UUID?
    @State var providerDelegate: ProviderDelegate?

    let api: KtorApiService = KtorApiService()

    let acceptPublisher = NotificationCenter.default
            .publisher(for: Notification.Name.DidCallAccepted)

    var body: some View {
        HStack {
            Button(action: {
                receiveCall(from: receiverID)
            }) {
                Image(systemName: "phone.arrow.down.left")
            }
        }
                .onReceive(acceptPublisher) { _ in
                    self.hasActivateCall = true
                    providerDelegate?.connectedCall(with: callID!)
                }
    }

    @State var loadingResource: Resource<NameItem> = ResourceLoading<NameItem>(data: nil)

    func receiveCall(from callerPhone: String) {
        providerDelegate = ProviderDelegate(callManager: callManager)

        var nameItem: NameItem? = nil
        let uuid = UUID()
        self.callID = uuid
        let update = CXCallUpdate()

        api.getInfoByPhone(phone: callerPhone) { resource, error in
            switch resource {
            case is ResourceErrorNotFoundError<NSArray>:
                self.loadingResource = ResourceError(exception: nil, data: nil)
                Toast(text: resource?.message, duration: Delay.short).show()
            case is ResourceSuccess<NSArray>:
                nameItem = ((resource!.data as! NSArray))[0] as! NameItem

                print(nameItem?.description)

                if let name = nameItem?.display_name {
                    update.remoteHandle = CXHandle(type: .generic, value: name)
                    providerDelegate?.reportIncomingCall(with: uuid, remoteUserID: name) { error in
                        if let error = error {
                            print(error.localizedDescription)
                        } else {
                            print("Ring Ring...")
                        }
                    }
                } else {
                    update.remoteHandle = CXHandle(type: .generic, value: callerPhone)
                    providerDelegate?.reportIncomingCall(with: uuid, remoteUserID: callerPhone) { error in
                        if let error = error {
                            print(error.localizedDescription)
                        } else {
                            print("Ring Ring...")
                        }
                    }
                }
            default:
                Toast(text: resource?.message, duration: Delay.short).show()
            }
        }

//        if let name = nameItem?.display_name {
//            update.remoteHandle = CXHandle(type: .generic, value: name)
//            providerDelegate?.reportIncomingCall(with: uuid, remoteUserID: name) { error in
//                if let error = error {
//                    print(error.localizedDescription)
//                } else {
//                    print("Ring Ring...")
//                }
//            }
//        } else {
//            update.remoteHandle = CXHandle(type: .generic, value: callerPhone)
//            providerDelegate?.reportIncomingCall(with: uuid, remoteUserID: callerPhone) { error in
//                if let error = error {
//                    print(error.localizedDescription)
//                } else {
//                    print("Ring Ring...")
//                }
//            }
//        }
    }
}