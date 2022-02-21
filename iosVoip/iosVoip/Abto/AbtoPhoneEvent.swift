import Foundation

enum AbtoPhoneEvent {
    // REGISTER events
    case registerSuccess(Int)
    case registerFailed(Int, Int, String)
    case unregister(Int)
    case remoteAlerting(Int, Int)
    
    // call events
    case callConnected(Int, String)
    case callDisconnected(Int, Int, String)
    case callAlerting(Int, Int)
    case callIncoming(Int, String)
    case callOutgoing(Int, String)
    case callTransfering(Int, Int, String)
    case callHold(Int, Bool)
    case callDtmfTone(Int, Int)
    case callZrtpSas(Int, String, Bool)
    case callZrtpSecureState(Int, Bool)
    case callZrtpError(Int, Int, Int)

    // IM events
    case textMessageReceived(String, String, String)
    case textMessageStatus(String, String, Bool)

    // Network events
    case networkStateChanged(Bool, Bool)

    // MWI events
    case mwiInfo(Int, String, String)

    // events
    func isRegisterEvent() -> Bool {
        switch self {
        case .registerSuccess:
            fallthrough
        case .registerFailed:
            fallthrough
        case .unregister:
            fallthrough
        case .remoteAlerting:
            return true

        default:
            return false
        }
    }
    
    func isCallEvent() -> Bool {
        switch self {
        case .callConnected:
            fallthrough
        case .callDisconnected:
            fallthrough
        case .callAlerting:
            fallthrough
        case .callIncoming:
            fallthrough
        case .callOutgoing:
            fallthrough
        case .callTransfering:
            fallthrough
        case .callHold:
            fallthrough
        case .callDtmfTone:
            fallthrough
        case .callZrtpSas:
            fallthrough
        case .callZrtpSecureState:
            fallthrough
        case .callZrtpError:
            return true

        default:
            return false
        }
    }

}

extension Notification.Name {
    static let AbtoPhoneEvent = Notification.Name("AbtoPhoneEvent")
}
