import Foundation
import CallKit

class CallManager: NSObject, ObservableObject {
    static let shared = CallManager()

    let callController = CXCallController()

    private(set) var callIDs: [UUID] = []

    func startCall(with uuid: UUID, receiverID: String, completionHandler: ErrorHandler? = nil) {
        let handle = CXHandle(type: .generic, value: receiverID)
        let startCallAction = CXStartCallAction(call: uuid, handle: handle)

        let transaction = CXTransaction(action: startCallAction)
        requestTransaction(transaction, completionHandler: completionHandler)
    }

    func endCall(with uuid: UUID, completionHandler: ErrorHandler? = nil) {
        let endCallAction = CXEndCallAction(call: uuid)

        let transaction = CXTransaction(action: endCallAction)
        requestTransaction(transaction, completionHandler: completionHandler)
    }

    func setHeldCall(with uuid: UUID, onHold: Bool, completionHandler: ErrorHandler?) {
        let setHeldCallAction = CXSetHeldCallAction(call: uuid, onHold: onHold)

        let transaction = CXTransaction(action: setHeldCallAction)
        requestTransaction(transaction, completionHandler: completionHandler)
    }

    func muteCall(with uuid: UUID, muted: Bool, completionHandler: ErrorHandler?) {
        let muteCallAction = CXSetMutedCallAction(call: uuid, muted: muted)

        let transaction = CXTransaction(action: muteCallAction)
        requestTransaction(transaction, completionHandler: completionHandler)
    }

    func requestTransaction(_ transaction: CXTransaction, completionHandler: ErrorHandler?) {
        callController.request(transaction) { error in
            guard error == nil else {
                completionHandler?(error as NSError?)
                return
            }
            print("Requested transaction successfully")
            completionHandler?(nil)
        }
    }

    // MARK: Call Management
    func addCall(uuid: UUID) {
        callIDs.append(uuid)
    }

    func removeCall(uuid: UUID) {
        callIDs.removeAll {
            $0 == uuid
        }
    }

    func removeAllCalls() {
        callIDs.removeAll()
    }
}