//
//  AbtoCallKitPushKitProvider.swift
//
//  Copyright © 2021 ABTO Software. All rights reserved.
//

import UIKit
import AVFoundation
import CallKit
import AbtoSipClientWrapper

let kPushkitId = -999
let kInitialCallIndex = 100
let kMaxCallIndex  = 100 + 1000

protocol AbtoCallKitProviderDelegate {

    // MARK: Delegate to cover SIP parsing for CallKit
    
    // extract information from SIP INVITE message
    func sipCallMatchId(_ inviteRequest: String?, andRemoteContact remoteContact: String?) -> String?
    func sipCallerName(_ inviteRequest: String?, andRemoteContact remoteContact: String?) -> String?
    func sipCallerNumber(_ inviteRequest: String?, andRemoteContact remoteContact: String?) -> String?
    func sipCallerContactId(_ inviteRequest: String?, andRemoteContact remoteContact: String?) -> String?
    
    // Common information
    func anonymousNumber() -> String
}

public class AbtoCallKitProvider: NSObject {
    
    // MARK: Main class properties and methods

    let delegate: AbtoCallKitProviderDelegate
    let handleType: CXHandle.HandleType

    private(set) lazy var abtoPhone: AbtoPhoneInterface = {
        return AbtoPhoneInterface()
    }()

    private(set) var controller: CXCallController
    private(set) var provider: CXProvider
    private(set) var frontCamera = true

    var mwiStatus: [AnyHashable]?
    var registered = false
    var networkAvailable = false
    var audioSessionActive = false

    var currentCalls: [UUID : AbtoCall] = [:]
    var rejectCallList: [String] = []

    init(handleType cxHandleType: CXHandle.HandleType, delegate callKitDelegate: AbtoCallKitProviderDelegate) {
        delegate = callKitDelegate
        handleType = cxHandleType
        controller = CXCallController()
        provider = CXProvider(configuration: type(of: self).providerConfiguration(type: cxHandleType))

        super.init()
        
        // Setup CallKit
        provider.setDelegate(self, queue: nil)

        // Initiailze ABTO SIP SDK
        abtoPhone.initialize(self, withBackground: true)
    }
    
    func startCall(_ handle: String, isVideo video: Bool, displayName: String?) -> AbtoCall? {
        let currentCall = AbtoCall(phone: abtoPhone, callId: synthesizeCallId(), handle: handle, outgoing: true, video: video)
        currentCall.displayName = displayName ?? ""
        
        abtoPhone.logMsg("startCall notify CallKit with callId \(currentCall.sipId) and call '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")
        
        let callHandle = CXHandle(type: handleType, value: handle)
        let action = CXStartCallAction(call: currentCall.uuid, handle: callHandle)
        action.isVideo = video

        if !AbtoCallKitProvider.isInvalidIdValue(displayName) {
            action.contactIdentifier = displayName
        }

        currentCalls[currentCall.uuid] = currentCall

        let transaction = CXTransaction(action: action)
        controller.request(transaction) { [self] error in
            if let error = error {
                DispatchQueue.main.async(execute: { [self] in
                    abtoPhone.logMsg("requestTransaction: CXStartCallAction call '\(currentCall.uuid)' with error '\(error)'", withLevel: 5, forSender: "PushKitWrapper")
                    currentCalls.removeValue(forKey: currentCall.uuid)
                    injectSdkEventsForBrokenCall(currentCall);
//                    if (error as NSError).code == CXErrorCodeRequestTransactionError.Code.maximumCallGroupsReached.rawValue {
//                        _ = outgoingCall.endCall(486)
//                    }
                })
            }
        }
        
        return currentCall
    }

    func answerCall(_ currentCall: AbtoCall?) {
        guard let currentCall = currentCall, !currentCall.isOutgoing else {
            return
        }

        abtoPhone.logMsg("answerCall with callId \(currentCall.sipId) and call '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")

        let action = CXAnswerCallAction(call: currentCall.uuid)
        let transaction = CXTransaction(action: action)
        controller.request(transaction) { [self] error in
            if let error = error {
                DispatchQueue.main.async(execute: {
                    abtoPhone.logMsg("requestTransaction: CXAnswerCallAction call '\(currentCall.uuid)' with error '\(error)'", withLevel: 5, forSender: "PushKitWrapper")
                })
            }
        }
    }
    
    func endCall(_ currentCall: AbtoCall?) {
        guard let currentCall = currentCall else {
            return
        }

        abtoPhone.logMsg("endCall with callId \(currentCall.sipId) and call '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")

        let action = CXEndCallAction(call: currentCall.uuid)
        let transaction = CXTransaction(action: action)
        controller.request(transaction) { [self] error in
            if let error = error {
                DispatchQueue.main.async(execute: {
                    abtoPhone.logMsg("requestTransaction: CXEndCallAction call '\(currentCall.uuid)' with error '\(error)'", withLevel: 5, forSender: "PushKitWrapper")
                })
            }
        }
    }

    func holdResumeCall(_ currentCall: AbtoCall?) {
        guard let currentCall = currentCall else {
            return
        }

        abtoPhone.logMsg("holdResumeCall with callId \(currentCall.sipId) and call '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")

        let action = CXSetHeldCallAction(call: currentCall.uuid, onHold: !currentCall.isLocalHold)
        let transaction = CXTransaction(action: action)
        controller.request(transaction) { [self] error in
            if let error = error {
                DispatchQueue.main.async(execute: {
                    abtoPhone.logMsg("requestTransaction: CXSetHeldCallAction call '\(currentCall.uuid)' with error '\(error)'", withLevel: 5, forSender: "PushKitWrapper")
                })
            }
        }
    }
    
    func muteMicrophoneInCall(_ currentCall: AbtoCall?, state: Bool) {
        guard let currentCall = currentCall else {
            return
        }
        
        abtoPhone.logMsg("muteMicrophoneInCall with callId \(currentCall.sipId) and call '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")

        let action = CXSetMutedCallAction(call: currentCall.uuid, muted: state)
        let transaction = CXTransaction(action: action)
        controller.request(transaction) { [self] error in
            if let error = error {
                DispatchQueue.main.async(execute: {
                    abtoPhone.logMsg("requestTransaction: CXSetMutedCallAction call '\(currentCall.uuid)' with error '\(error)'", withLevel: 5, forSender: "PushKitWrapper")
                })
            }
        }
    }
    
    func sendDtmfInCall(_ currentCall: AbtoCall?, dtmf: String) {
        guard let currentCall = currentCall else {
            return
        }
        
        abtoPhone.logMsg("sendDtmfInCall with callId \(currentCall.sipId) and call '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")
        
        let action = CXPlayDTMFCallAction(call: currentCall.uuid, digits: dtmf, type: .singleTone)
        let transaction = CXTransaction(action: action)
        controller.request(transaction) { [self] error in
            if let error = error {
                DispatchQueue.main.async(execute: {
                    abtoPhone.logMsg("requestTransaction: CXPlayDTMFCallAction call '\(currentCall.uuid)' with error '\(error)'", withLevel: 5, forSender: "PushKitWrapper")
                })
            }
        }
    }
    
    func transferCall(_ currentCall: AbtoCall?, toContact: String) {
        guard let currentCall = currentCall else {
            return
        }
        
        abtoPhone.logMsg("transferCall with callId \(currentCall.sipId) and call '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")
        
        currentCall.transferCall(toContact)
    }
    
    func startRecordingCall(_ currentCall: AbtoCall?, filename: String) -> Bool {
        guard let currentCall = currentCall else {
            return false
        }
        
        abtoPhone.logMsg("startRecordCall with callId \(currentCall.sipId) and call '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")
        
        return currentCall.startRecordingCall(filename)
    }
    
    func stopRecordingCall(_ currentCall: AbtoCall?) {
        guard let currentCall = currentCall else {
            return
        }
        
        abtoPhone.logMsg("stopRecordCall with callId \(currentCall.sipId) and call '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")
        
        currentCall.stopRecordingCall()
    }

    func switchCamera(_ currentCall: AbtoCall?, toFront: Bool) {
        guard let currentCall = currentCall else {
            return
        }
        
        if currentCall.switchCamera(toFront: toFront) {
            frontCamera = toFront
        }
    }

    //    func call(inProgress callUUID: UUID?) -> Bool {
//    }
//
//    func isVideoCall(_ callUUI: UUID?) -> Bool {
//    }
//
//    func isConnected(_ callUUI: UUID?) -> Bool {
//    }
//
//    func isOutgoing(_ callUUI: UUID?) -> Bool {
//    }
//
//    func callHandle(_ callUUID: UUID?) -> String? {
//    }
//
//    func getRealSipId(_ NSInteger: Int) -> Int {
//    }
//
//    func isPushKitId(_ callId: Int) -> Bool {
//        return callId < kPushkitId;
//    }
//

    class func parseSipPacket(_ packet: String?, header searchHeader: String?) -> String? {
        guard let packet = packet as NSString?, packet.length > 0 else {
            return nil
        }
        
        var searchRange = NSRange(location: 0, length: packet.length)

        repeat {
            let lineSplitRange = packet.range(of: "\r\n", options: [], range: searchRange)

            guard lineSplitRange.location != NSNotFound, searchRange.location != lineSplitRange.location else {
                return nil
            }
            
            let headerRange = NSRange(location: searchRange.location, length: lineSplitRange.location - searchRange.location)

            searchRange.location = lineSplitRange.location + lineSplitRange.length
            searchRange.length = packet.length - searchRange.location
            
            let delimiterRange = packet.range(of: ":", options: [], range: headerRange)

            if delimiterRange.location != NSNotFound {
                let nameRange = NSRange(location: headerRange.location, length: delimiterRange.location - headerRange.location)
                let headerName = packet.substring(with: nameRange)

                if searchHeader == headerName {
                    let valueRange = NSRange(location: delimiterRange.location + delimiterRange.length, length: lineSplitRange.location - delimiterRange.location - delimiterRange.length)
                    let headerValue = packet.substring(with: valueRange)

                    //                return headerValue;
                    return headerValue.trimmingCharacters(in: CharacterSet.whitespaces)
                }
            }

        } while true
    }
}

extension AbtoCallKitProvider : CXProviderDelegate {

    // MARK: CallKit delegate

    public func providerDidReset(_ provider: CXProvider) {
        abtoPhone.logMsg("CallKit: Did Reset Provider", withLevel: 5, forSender: "PushKitWrapper")
    }

    public func providerDidBegin(_ provider: CXProvider) {
        abtoPhone.logMsg("CallKit: Did Begin Provider", withLevel: 5, forSender: "PushKitWrapper")
    }

    public func provider(_ provider: CXProvider, execute transaction: CXTransaction) -> Bool {
        return false
    }

    public func provider(_ provider: CXProvider, perform action: CXStartCallAction) {
        abtoPhone.logMsg("CallKit: Perform Start Call Action with call \(action.callUUID)", withLevel: 5, forSender: "PushKitWrapper")

        guard let currentCall = currentCalls[action.callUUID] else {
            // check if we need to generate 2 events to inform about broken call start and it's end
            action.fail()
            return
        }

        if !audioSessionActive {
            abtoPhone.deactivateAudio()
        }
        
        currentCall.isKnownByCallKit = true

        guard currentCall.startCall() else {
            abtoPhone.logMsg("CallKit: Finish ougoing call rejected by SDK with call \(action.callUUID)", withLevel: 5, forSender: "PushKitWrapper")
            abtoPhone.logMsg("startCall rejected by SDK", withLevel: 5, forSender: "PushKitWrapper")

            DispatchQueue.main.async(execute: { [self] in
                // Mark as failed
                currentCall.hangupAction = nil
                currentCall.isErrorCall = true
                
                finishedCall(currentCall)
                
                action.fail()
            })
            return
        }

        let remoteContact = "sip:\(action.handle)@dummyhost"
        
        let event = AbtoPhoneEvent.callOutgoing(currentCall.sipId, remoteContact)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)

        self.provider.reportOutgoingCall(with: currentCall.uuid, startedConnectingAt: Date())
        action.fulfill()
        if #available(iOS 14.0, *) {
            // audio will be set in
            // func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession)
        } else {
            setupOsAudioSession()
        }
    }

    public func provider(_ provider: CXProvider, perform action: CXAnswerCallAction) {
        abtoPhone.logMsg("CallKit: Perform Answer Call Action \(action.callUUID)", withLevel: 5, forSender: "PushKitWrapper")

        guard let currentCall = currentCalls[action.callUUID] else {
            action.fail()
            return
        }

        currentCall.callInjectDate = nil

        if currentCall.isPushKit {
            // PushKit call

            if currentCall.sipId == kInvalidCallId {
                abtoPhone.logMsg("CallKit: PushKit call \(action.callUUID) delayed answer", withLevel: 5, forSender: "PushKitWrapper")
                // Save answer action for later use
                currentCall.answerAction = action
                return
            }
            
            // We already have real SIP callId, proceed with answer
        }
        
        abtoPhone.logMsg("CallKit: Answer call \(action.callUUID) with real callId \(currentCall.sipId)", withLevel: 5, forSender: "PushKitWrapper")

        // prepare audio session
        if #available(iOS 14.0, *) {
            // audio will be set in
            // func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession)
        } else {
            setupOsAudioSession()
        }
        
        // answer call
        if currentCall.answerCall(200) {
            currentCall.answerAction = action
        } else {
            action.fail()
        }
    }

    public func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
        abtoPhone.logMsg("CallKit: Perform End Call Action \(action.callUUID)", withLevel: 5, forSender: "PushKitWrapper")

        guard let currentCall = currentCalls[action.callUUID] else {
            action.fail()
            return
        }

        if currentCall.isPushKit {
            // PushKit call

            if currentCall.sipId == kInvalidCallId {
                abtoPhone.logMsg("CallKit: Finish PushKit call \(action.callUUID) without real callId", withLevel: 5, forSender: "PushKitWrapper")

                // Save hangup action for later use
                currentCall.hangupAction = action

                // finish call immediately, as no real SIP call available
                finishedCall(currentCall)

                // Add call to reject list
                if let callID = currentCall.callID {
                    rejectCallList.append(callID)
                }

                // call end callback
                currentCall.finishBlock?(currentCall, 486, "Busy Here")

                return
            }

            // We already have real SIP callId, proceed with hangup
        }
        
        abtoPhone.logMsg("CallKit: Finish call \(action.callUUID) with real callId \(currentCall.sipId)", withLevel: 5, forSender: "PushKitWrapper")

        currentCall.hangupAction = action

        if !currentCall.endCall(486) {
            currentCall.hangupAction = nil
            action.fail()
        }
    }
    
    public func provider(_ provider: CXProvider, perform action: CXSetHeldCallAction) {
        abtoPhone.logMsg("CallKit: Perform Set Held Call Action \(action.callUUID)", withLevel: 5, forSender: "PushKitWrapper")

        guard let currentCall = currentCalls[action.callUUID] else {
            action.fail()
            return
        }

        if action.isOnHold != currentCall.isLocalHold {
            _ = currentCall.holdToggleCall()
        }

        action.fulfill()
    }
    
    public func provider(_ provider: CXProvider, perform action: CXSetMutedCallAction) {
        abtoPhone.logMsg("CallKit: Perform Set Muted Call Action \(action.callUUID)", withLevel: 5, forSender: "PushKitWrapper")

        guard let currentCall = currentCalls[action.callUUID] else {
            action.fail()
            return
        }

        currentCall.muteCall(action.isMuted)
        action.fulfill()
    }
    
    public func provider(_ provider: CXProvider, perform action: CXSetGroupCallAction) {
        action.fail()
    }
    
    public func provider(_ provider: CXProvider, perform action: CXPlayDTMFCallAction) {
        abtoPhone.logMsg("CallKit: Perform Play DTMF Call Action \(action.callUUID)", withLevel: 5, forSender: "PushKitWrapper")

        guard let currentCall = currentCalls[action.callUUID] else {
            action.fail()
            return
        }

        currentCall.sendDtmf(action.digits)
        action.fulfill()
    }

    public func provider(_ provider: CXProvider, timedOutPerforming action: CXAction) {
        abtoPhone.logMsg("CallKit: Timed Out Performing Action \(action)", withLevel: 5, forSender: "PushKitWrapper")
    }
    
    public func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {
        abtoPhone.logMsg("CallKit: Did Activate Audio Session", withLevel: 5, forSender: "PushKitWrapper")
        setupOsAudioSession()
    }

    public func provider(_ provider: CXProvider, didDeactivate audioSession: AVAudioSession) {
        abtoPhone.logMsg("CallKit: Did Deactivate Audio Session", withLevel: 5, forSender: "PushKitWrapper")
        finishOsAudioSession()
    }
}

extension AbtoCallKitProvider {
    // MARK: CallKit configuration

    private static func providerConfiguration(type: CXHandle.HandleType) -> CXProviderConfiguration {
        let mainBundle = Bundle.main
        let callKitName = NSLocalizedString("CallKitName", value: "", comment: "Name of application for CallKit")
        var localizedName = AbtoCallKitProvider.isInvalidIdValue(callKitName) ? nil : callKitName
        if localizedName == nil {
            localizedName = mainBundle.localizedInfoDictionary?["CFBundleDisplayName"] as? String
        }
        if localizedName == nil {
            localizedName = mainBundle.infoDictionary?["CFBundleDisplayName"] as? String
        }
        if localizedName == nil {
            localizedName = mainBundle.infoDictionary?["CFBundleName"] as? String
        }
        var callkitImage = UIImage(named: "callkit")
        if callkitImage == nil {
            if let bundleIcons = mainBundle.infoDictionary?["CFBundleIcons"] as? Dictionary<String, Any>, let primaryIcon = bundleIcons["CFBundlePrimaryIcon"] as? Dictionary<String, Any>, let iconFiles = primaryIcon["CFBundleIconFiles"] as? Array<String>, iconFiles.count > 0 {
                callkitImage = UIImage(named: iconFiles[0])
            }
        }
        
        let providerConfiguration = CXProviderConfiguration(localizedName: localizedName ?? "Default app name")
        
        providerConfiguration.supportsVideo = true
        providerConfiguration.maximumCallGroups = 1
        providerConfiguration.maximumCallsPerCallGroup = 1
        providerConfiguration.supportedHandleTypes = [type]
        
        if let iconMaskImage = callkitImage {
            providerConfiguration.iconTemplateImageData = iconMaskImage.pngData()
        }

        //providerConfiguration.ringtoneSound = "ringtone.mp3" // Custom default ringtone
        
        return providerConfiguration
    }
}

extension AbtoCallKitProvider : AbtoPhoneInterfaceObserver {

    // MARK: ABTO VoIP SDK delegate

    public func onRegistered(_ accId: Int) {
        self.registered = true

        let event = AbtoPhoneEvent.registerSuccess(accId)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onRegistrationFailed(_ accId: Int, statusCode: Int32, statusText: String) {
        self.registered = false

        let event = AbtoPhoneEvent.registerFailed(accId, Int(statusCode), statusText)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onUnRegistered(_ accId: Int) {
        self.registered = false

        let event = AbtoPhoneEvent.unregister(accId)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onRemoteAlerting(_ accId: Int, statusCode: Int32) {
        let event = AbtoPhoneEvent.remoteAlerting(accId, Int(statusCode))
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onIncomingCall(_ callId: Int, remoteContact: String) {
        // Dummy, SDK will trigger onIncomingCall:remoteContact:andInviteRequest:
    }

    public func onIncomingCall(_ callId: Int, remoteContact: String, andInviteRequest inviteRequest: String) {
        let number = self.delegate.sipCallerNumber(inviteRequest, andRemoteContact: remoteContact) ?? self.delegate.anonymousNumber()
        let displayName = self.delegate.sipCallerName(inviteRequest, andRemoteContact: remoteContact)
        let contactId = self.delegate.sipCallerContactId(inviteRequest, andRemoteContact: remoteContact)
        var callUUID = self.delegate.sipCallMatchId(inviteRequest, andRemoteContact: remoteContact)
        let hasVideo = callId != kInvalidCallId ? abtoPhone.isVideoCall(callId) : false

        if callUUID?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "" == "" {
            callUUID = UUID().uuidString
            abtoPhone.logMsg("Invite does not include custom header value for call matching using random value '\(callUUID ?? "")'", withLevel: 5, forSender: "PushKitWrapper")
        }
        
        reportIncomingCall(callId, withNumber: number, andDisplayName: displayName, andCallId: callUUID, andContactId: contactId, withCompletion: { [self] error, call in

            if finishIfBroken(call) {
                // call was ended before it managed to be notified to CallKit
                abtoPhone.logMsg("Call finished earlier than processed by CallKit", withLevel: 5, forSender: "PushKitWrapper")

                // check if we need to emit 2 signals incomingCall and callDisconnected in this case
                return
            }

            if call.isPushKit {
                // If dummy event, emit call disconnected event to Qt
                if callId == kInvalidCallId {
                    // TODO: check if we need to Call onCallDisconnected directly
                    onCallDisconnected(call.internalId, remoteContact: remoteContact, statusCode: 487, message: "Request Terminated")
                }
            } else {
                if callId != kInvalidCallId {
                    // Emit signal only for real calls
                    let event = AbtoPhoneEvent.callIncoming(call.internalId, remoteContact)
                    NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
                }
            }

        }, callFinishBlock: { call, code, message in
            // TODO: check what is required here
        }, hasVideo: hasVideo)
        
    }
    
    public func onCallConnected(_ callId: Int, remoteContact: String) {
        let wrapperId = wrapperInternalCallId(callId)
        connectCall(bySipId: callId)
        
        let event = AbtoPhoneEvent.callConnected(wrapperId, remoteContact)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onCallDisconnected(_ callId: Int, remoteContact: String, statusCode: Int, message: String) {
        let wrapperId = wrapperInternalCallId(callId)
        _ = finishedCall(bySipId: callId)
        
        let event = AbtoPhoneEvent.callDisconnected(wrapperId, statusCode, message)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onCallAlerting(_ callId: Int, statusCode: Int32) {
        let wrapperId = wrapperInternalCallId(callId)
        alertedCall(bySipId: callId, alert: Int(statusCode))

        let event = AbtoPhoneEvent.callAlerting(wrapperId, Int(statusCode))
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onCallHeld(_ callId: Int, state: Bool) {
        let wrapperId = wrapperInternalCallId(callId)
        _ = callRemoteHold(bySipId: callId, status: state)
        
        let event = AbtoPhoneEvent.callHold(wrapperId, state)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onToneReceived(_ callId: Int, tone: Int) {
        let wrapperId = wrapperInternalCallId(callId)
        let event = AbtoPhoneEvent.callDtmfTone(wrapperId, tone)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onTextMessageReceived(_ from: String, to: String, body: String) {
        let event = AbtoPhoneEvent.textMessageReceived(from, to, body)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onTextMessageStatus(_ address: String, reason: String, status: Bool) {
        let event = AbtoPhoneEvent.textMessageStatus(address, reason, status)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onPresenceChanged(_ uri: String, status: PhoneBuddyStatus, note: String) {
        // Dummy, unused
    }
    
    public func onTransferStatus(_ callId: Int, statusCode: Int32, message: String) {
        let wrapperId = wrapperInternalCallId(callId)

        if statusCode == 200 {
            endCall(bySipId: callId)
        }

        let event = AbtoPhoneEvent.callTransfering(wrapperId, Int(statusCode), message)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onNetworkStateChanged(_ event: PhoneNetworkEvent, isIpv6 ipv6: Bool) {
        let event = AbtoPhoneEvent.networkStateChanged(event != .notReachable, ipv6)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onMwiInfo(_ accId: Int, withMimeType type: String, andText text: String) {
        let event = AbtoPhoneEvent.mwiInfo(accId, type, text)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onZrtpSas(_ callId: Int, sas: String, isVerified verified: Bool) {
        let event = AbtoPhoneEvent.callZrtpSas(callId, sas, verified)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onZrtpSecureState(_ callId: Int, secured: Bool) {
        let event = AbtoPhoneEvent.callZrtpSecureState(callId, secured)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
    public func onZrtpError(_ callId: Int, error: Int, subcode: Int) {
        let event = AbtoPhoneEvent.callZrtpError(callId, error, subcode)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: event)
    }
    
}

extension AbtoCallKitProvider {
    
    // MARK: AbtoCall actions
    
    func reportIncomingCall(_ callId: Int, withNumber handle: String, andDisplayName displayName: String?, andCallId callID: String?, andContactId contactId: String?, withCompletion completion: @escaping (_ error: Error?, _ call: AbtoCall) -> Void, callFinishBlock finishBlock: @escaping (_ call: AbtoCall, _ code: Int, _ message: String) -> Void, hasVideo: Bool) {
        
        var currentCall: AbtoCall? = nil
        let isPushKit = callId == kPushkitId

        if let callID = callID {
            currentCall = findCall(byMatchId: callID)
        }

        abtoPhone.logMsg("reportIncomingCall: new call with Call-ID '\(callID ?? "")' and callId \(callId)", withLevel: 5, forSender: "PushKitWrapper")
        
        if callId == kPushkitId {
            if let currentCall = currentCall {
                if currentCall.isPushKit {
                    // It's incoming PushKit, but call already registered
                    abtoPhone.logMsg("reportIncomingCall: PushKit call, but '\(currentCall.uuid)' already registered", withLevel: 5, forSender: "PushKitWrapper")
                } else if currentCall.sipId != kInvalidCallId {
                    // We have real call and receive PushKit event
                    abtoPhone.logMsg("reportIncomingCall: We have real callId \(currentCall.sipId) and receive PushKit event '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")
                } else {
                    abtoPhone.logMsg("reportIncomingCall: TODO: Call with callId \(currentCall.sipId) and call '\(currentCall.uuid)' check condition", withLevel: 5, forSender: "PushKitWrapper")
                }
            } else {
                // Find empty slot for PushKit call
                abtoPhone.logMsg("reportIncomingCall: will create new PushKit call with Call-ID '\(callID ?? "")'", withLevel: 5, forSender: "PushKitWrapper")
            }
        } else {
            if (callId == kInvalidCallId) {
                if let callID = callID, rejectCallList.contains(callID) {
                    // Call already rejected by user and so by SDK
                    abtoPhone.logMsg("reportIncomingCall: Call with Call-ID '\(callID)' already rejected by user and so by SDK", withLevel: 5, forSender: "PushKitWrapper")
//                    rejectCallList.removeAll { $0 == callID }
                    rejectCallList.removeAll { $0 as AnyObject === callID as AnyObject }
                    return
                }
                
                if let currentCall = currentCall {
                    if currentCall.isPushKit {
                        // Call rejected by SDK, so need to finish PushKit call and notift QT
                        abtoPhone.logMsg("reportIncomingCall: Call '\(currentCall.uuid)' rejected by SDK, so need to finish PushKit call and notift QT", withLevel: 5, forSender: "PushKitWrapper")
                        finishedCall(currentCall)

                        currentCall.finishBlock?(currentCall, 500, "Internal Server Error")
                        currentCall.sipId = kInvalidCallId
                    } else {
                        // ignore such events
                        // TODO: check if we need to notify about broken calls, to generate history for example
                        abtoPhone.logMsg("reportIncomingCall: Ignore Call '\(currentCall.uuid)' with invalid callId", withLevel: 5, forSender: "PushKitWrapper")
                    }
                } else {
                    if let callID = callID {
                        abtoPhone.logMsg("reportIncomingCall: Ignore Call with Call-ID '\(callID)' with invalid callId", withLevel: 5, forSender: "PushKitWrapper")
                    } else {
                        abtoPhone.logMsg("reportIncomingCall: Ignore Call with invalid callId", withLevel: 5, forSender: "PushKitWrapper")
                    }
                }
                
                return
            }

            // We have real callId

            if let callID = callID, rejectCallList.contains(callID) {
                abtoPhone.logMsg("reportIncomingCall: Finish Call with Call-ID '\(callID)' silently as already processed by user", withLevel: 5, forSender: "PushKitWrapper")
                // Reject real SIP call
                abtoPhone.hangUpCall(callId, status: 486)

                // Call processed
                rejectCallList.removeAll { $0 as AnyObject === callID as AnyObject }
                return
            }
            
            // check if need to deactivate audio
            if !audioSessionActive {
                abtoPhone.deactivateAudio()
            }

            if let currentCall = currentCall {
                if currentCall.isPushKit {
                    // just update call with required info
                    abtoPhone.logMsg("reportIncomingCall: Update PushKit Call '\(currentCall.uuid)' with real SIP callId \(callId)", withLevel: 5, forSender: "PushKitWrapper")

                    currentCall.sipId = callId
                    currentCall.isVideo = hasVideo

                    // And process user actions if available
                    if currentCall.answerAction != nil {
                        abtoPhone.logMsg("reportIncomingCall: Already answered PushKit Call '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")
                        _ = currentCall.answerCall(200)
                        // TODO: do we need to check result and process failure case?
                    }
                } else {
                    // Seems like second call with same Call-ID, which we currnetly reject
                    // though need to check other options why this might happen
                    // e.g. transfer call, etc
                    abtoPhone.hangUpCall(callId, status: 607)
                }
                
                return
            } else {
                abtoPhone.logMsg("reportIncomingCall: will create new call with callId \(callId) and Call-ID '\(callID ?? "")'", withLevel: 5, forSender: "PushKitWrapper")
            }
        }
        
        let newCall : AbtoCall
            
        if let currentCall = currentCall {
            newCall = currentCall
        } else {
            newCall = AbtoCall(phone: abtoPhone, callId: synthesizeCallId(), handle: handle, pushKit: isPushKit, video: hasVideo)
            newCall.callID = callID
            newCall.finishBlock = finishBlock
            
            if !isPushKit {
                newCall.sipId = callId
            }

            abtoPhone.logMsg("reportIncomingCall: new call created with callId \(callId), Call-ID '\(callID ?? "")' and call '\(newCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")

            currentCalls[newCall.uuid] = newCall
            
            completion(nil, newCall)
        }
        
        let update = CXCallUpdate()
        update.remoteHandle = CXHandle(type: handleType, value: handle)
        update.hasVideo = hasVideo
        update.supportsGrouping = false
        update.supportsUngrouping = false
        update.supportsHolding = true
        update.supportsDTMF = true

        if !AbtoCallKitProvider.isInvalidIdValue(displayName), let displayName = displayName {
            update.localizedCallerName = displayName
            newCall.displayName = displayName
        }
        
        provider.reportNewIncomingCall(with: newCall.uuid, update: update) { [self] error in
            DispatchQueue.main.async(execute: { [self] in
                newCall.isKnownByCallKit = true

                if let error = error {
                    abtoPhone.logMsg("reportNewIncomingCallWithUUID: call '\(newCall.uuid)' error '\(error)'", withLevel: 5, forSender: "PushKitWrapper")
                    
                    if (error as NSError).code != CXErrorCodeIncomingCallError.Code.callUUIDAlreadyExists.rawValue {
                        abtoPhone.logMsg("reportNewIncomingCallWithUUID: Finish unreported call '\(newCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")
                        currentCalls.removeValue(forKey: newCall.uuid)
                        _ = newCall.endCall(486)
                        injectSdkEventsForBrokenCall(newCall);
                    }
                } else {
                    abtoPhone.logMsg("reportNewIncomingCallWithUUID: new call created with callId \(callId) and call '\(newCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")
                    if callId == kInvalidCallId {
                        finishedCall(currentCall)
                    } else {
                        if !newCall.isConnected, newCall.callInjectDate == nil {
                            newCall.callInjectDate = Date()
                        }
                    }
                }
            })
        }
    }
    
    func connectCall(_ currentCall: AbtoCall?) {
        guard let currentCall = currentCall else {
            return
        }

        currentCall.isConnected = true
        
        if currentCall.isOutgoing {
            abtoPhone.logMsg("connectCall outgoing  with callId \(currentCall.sipId) and call '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")
            provider.reportOutgoingCall(with: currentCall.uuid, connectedAt: currentCall.connectDate)
        } else {
            if (currentCall.answerAction != nil) {
                abtoPhone.logMsg("connectCall answered with callId \(currentCall.sipId) and call '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")
                currentCall.answerAction?.fulfill(withDateConnected: currentCall.connectDate ?? Date())
                currentCall.answerAction = nil
            }
        }
        if currentCall.isVideo && !currentCall.isSendingVideo {
            currentCall.toggleVideoSend()
        }
    }
    
    func finishedCall(_ currentCall: AbtoCall?) {
        guard let currentCall = currentCall else {
            return
        }
        abtoPhone.logMsg("finishedCall with callId \(currentCall.sipId) and call '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")

        if !currentCall.isEnded {
            currentCall.isEnded = true
        }

        if (currentCall.hangupAction != nil) {
            abtoPhone.logMsg("finishedCall with callId \(currentCall.sipId) and call '\(currentCall.uuid)' with hangup action", withLevel: 5, forSender: "PushKitWrapper")
            currentCall.hangupAction?.fulfill(withDateEnded: currentCall.endDate ?? Date())
        } else {
            abtoPhone.logMsg("finishedCall with callId \(currentCall.sipId) and call '\(currentCall.uuid)' on remote side", withLevel: 5, forSender: "PushKitWrapper")
            var reason: CXCallEndedReason

            if currentCall.isErrorCall {
                reason = .failed
            } else if currentCall.isConnected {
                reason = .remoteEnded
            } else {
                reason = .unanswered
            }

            provider.reportCall(with: currentCall.uuid, endedAt: currentCall.endDate, reason: reason)
        }

        if currentCall.isKnownByCallKit {
            currentCalls.removeValue(forKey: currentCall.uuid)
        } else {
            if let callID = currentCall.callID {
                rejectCallList.append(callID)
            }
        }
    }
    
    func alertedCall(_ currentCall: AbtoCall?, alert status: Int) {
        guard let currentCall = currentCall else {
            return
        }

        abtoPhone.logMsg("alertedCall with callId \(currentCall.sipId) and call '\(currentCall.uuid)'", withLevel: 5, forSender: "PushKitWrapper")
        
        currentCall.alertingStatus = status
    }
    
    func finishIfBroken(_ call: AbtoCall?) -> Bool {
        if let callID = call?.callID {
            if !rejectCallList.contains(callID) {
                return false
            }
        }

        rejectCallList.removeAll { $0 as AnyObject === call?.callID as AnyObject }

        finishedCall(call)

        return true
    }
    
    func injectSdkEventsForBrokenCall(_ call: AbtoCall) {
        let remoteContact = "sip:\(call.callHandle)@dummyhost"
        
        let eventStart = AbtoPhoneEvent.callOutgoing(call.internalId, remoteContact)
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: eventStart)

        let eventEnd = AbtoPhoneEvent.callDisconnected(call.internalId, 500, "Internal Server Error")
        NotificationCenter.default.post(name: .AbtoPhoneEvent, object: eventEnd)
    }
    
    ///////////////////
    
    func connectCall(bySipId callId: Int) {
        abtoPhone.logMsg("connectCallById \(callId)", withLevel: 5, forSender: "PushKitWrapper")

        guard let currentCall = findCall(bySipId: callId) else {
            return
        }
        connectCall(currentCall)
    }
    
    func finishedCall(bySipId callId: Int) -> Bool {
        abtoPhone.logMsg("finishedCallById \(callId)", withLevel: 5, forSender: "PushKitWrapper")

        guard let currentCall = findCall(bySipId: callId) else {
            return false
        }
        finishedCall(currentCall)

        return true
    }
    
    func endCall(bySipId callId: Int) {
        abtoPhone.logMsg("endCallById \(callId)", withLevel: 5, forSender: "PushKitWrapper")

        guard let currentCall = findCall(bySipId: callId) else {
            return
        }
        endCall(currentCall)
    }
    
    func alertedCall(bySipId callId: Int, alert status: Int) {
        abtoPhone.logMsg("alertedCallById \(callId) status \(status)", withLevel: 5, forSender: "PushKitWrapper")

        guard let currentCall = findCall(bySipId: callId) else {
            return
        }
        
        alertedCall(currentCall, alert: status)
    }

    func callRemoteHold(bySipId callId: Int, status: Bool) -> Bool {
        abtoPhone.logMsg("callRemoteHoldById \(callId) status \(status)", withLevel: 5, forSender: "PushKitWrapper")

        guard let currentCall = findCall(bySipId: callId) else {
            return false
        }

        currentCall.isRemoteHold = status
        return true
    }
}

extension AbtoCallKitProvider {

    // MARK: Audio setup

    func setupOsAudioSession() {
        abtoPhone.logMsg("setupOsAudioSession", withLevel: 5, forSender: "PushKitWrapper")
        
        do {
            let session = AVAudioSession.sharedInstance()

            try session.setCategory(.playAndRecord, options: [.mixWithOthers, .allowBluetooth, .allowBluetoothA2DP])
            try session.setMode(.voiceChat)
            try session.setActive(true, options: [.notifyOthersOnDeactivation])

            abtoPhone.activateAudio()
            audioSessionActive = true
            abtoPhone.logMsg("setupOsAudioSession - activateAudio", withLevel: 5, forSender: "PushKitWrapper")
        } catch {
            audioSessionActive = false
            abtoPhone.logMsg("setupOsAudioSession - failed", withLevel: 5, forSender: "PushKitWrapper")
        }
    }
    
    func finishOsAudioSession() {
        abtoPhone.logMsg("finishOsAudioSession", withLevel: 5, forSender: "PushKitWrapper")
        abtoPhone.deactivateAudio()
        let session = AVAudioSession.sharedInstance()

        do {
            try session.setActive(false, options: [.notifyOthersOnDeactivation])
            audioSessionActive = false
        } catch {
            audioSessionActive = true
            abtoPhone.logMsg("finishOsAudioSession - failed", withLevel: 5, forSender: "PushKitWrapper")
        }
    }
    
}

extension AbtoCallKitProvider {
    
    // MARK: Utilities

    func findCall(bySipId callId: Int) -> AbtoCall? {
        for currentCall in currentCalls.values {
            if currentCall.sipId == callId {
                return currentCall
            }
        }

        return nil
    }

    func findCall(byMatchId callId: String) -> AbtoCall? {
        for currentCall in currentCalls.values {
            if currentCall.callID == callId {
                return currentCall
            }
        }

        return nil
    }

    func wrapperInternalCallId(_ callId: Int) -> Int {
        let currentCall = findCall(bySipId: callId)

        return currentCall?.internalId ?? callId
    }
    
    func synthesizeCallId() -> Int {
        var callId = kInitialCallIndex
        synthesizeLoop: while callId < kMaxCallIndex {
            for currentCall in currentCalls.values {
                if callId == currentCall.internalId {
                    callId += 1
                    continue synthesizeLoop
                }
            }

            return callId
        }

        return kInvalidCallId
    }

    static func isInvalidIdValue(_ value: String?) -> Bool {
        guard let value = value, value.count > 0, value.trimmingCharacters(in: CharacterSet.whitespaces).count > 0 else {
            return true
        }

        return false
    }
}
