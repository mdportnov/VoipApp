//
//  AbtoCall.swift
//
//  Copyright © 2021 ABTO Software. All rights reserved.
//

import UIKit
import CallKit
import AbtoSipClientWrapper

final class AbtoCall {
    
    // MARK: Metadata Properties
    
    let abtoPhone: AbtoPhoneInterface

    let uuid: UUID
    var callID: String?

    var alertingStatus: Int
    var sipId: Int
    var internalId: Int
    var callHandle: String
    var displayName: String

    let isPushKit: Bool
    let isOutgoing: Bool
    var isVideo: Bool
    var isLocalHold: Bool
    var isRemoteHold: Bool
    var isMute: Bool
    var isSendingVideo: Bool
    var isErrorCall: Bool
    var isRecordingCall: Bool
    var isKnownByCallKit: Bool

    var answerAction : CXAnswerCallAction?
    var hangupAction : CXEndCallAction?

    var finishBlock: ((_ call: AbtoCall, _ code: Int, _ message: String) -> Void)?
    
    // MARK: Call State Properties
    
    var startDate: Date? {
        didSet {
            stateDidChange?()
            hasStartedDidChange?()
        }
    }
    
    var answerDate: Date? {
        didSet {
            stateDidChange?()
            hasAnsweredDidChange?()
        }
    }
    
    var connectDate: Date? {
        didSet {
            stateDidChange?()
            hasConnectedDidChange?()
        }
    }
    
    var endDate: Date? {
        didSet {
            stateDidChange?()
            hasEndedDidChange?()
        }
    }
    
    var callInjectDate: Date?
    
    var isOnHold = false {
        didSet {
            stateDidChange?()
        }
    }
    
    // MARK: State change callback blocks
    
    var stateDidChange: (() -> Void)?
    var hasStartedDidChange: (() -> Void)?
    var hasAnsweredDidChange: (() -> Void)?
    var hasConnectedDidChange: (() -> Void)?
    var hasEndedDidChange: (() -> Void)?
    
    // MARK: Derived Properties
    
    var isStarted: Bool {
        get {
            return startDate != nil
        }
        set {
            startDate = newValue ? Date() : nil
        }
    }
    
    var isAnswered: Bool {
        get {
            return answerDate != nil
        }
        set {
            answerDate = newValue ? Date() : nil
        }
    }

    var isConnected: Bool {
        get {
            return connectDate != nil
        }
        set {
            connectDate = newValue ? Date() : nil
        }
    }
    
    var isEnded: Bool {
        get {
            return endDate != nil
        }
        set {
            endDate = newValue ? Date() : nil
        }
    }
    
    var duration: TimeInterval {
        guard let connectDate = connectDate else {
            return 0
        }
        
        guard let endDate = endDate else {
            return Date().timeIntervalSince(connectDate)
        }
        
        return endDate.timeIntervalSince(connectDate)
    }
    
    // MARK: Initialization
    
    init(phone: AbtoPhoneInterface, callId: Int, handle: String, pushKit: Bool = false, outgoing: Bool = false, video: Bool = false) {
        uuid = UUID()
        isOutgoing = outgoing
        isVideo = video
        isPushKit = pushKit
        sipId = kInvalidCallId
        internalId = callId
        abtoPhone = phone
        callHandle = handle

        isLocalHold = false
        isRemoteHold = false
        isMute = false
        isSendingVideo = false
        isErrorCall = false
        isKnownByCallKit = false
        isRecordingCall = false
        alertingStatus = 0
        displayName = ""
    }
    
    // MARK: Actions
    
    func startCall() -> Bool {
        guard sipId == kInvalidCallId else {
            return false
        }

        let callId = abtoPhone.startCall(callHandle, withVideo: isVideo)
        guard callId != kInvalidCallId else {
            return false
        }

        internalId = callId
        sipId = callId
        abtoPhone.setCurrentCall(callId)

        return true
    }
    
    func endCall(_ status: Int) -> Bool {
        if hasValidSipId() && abtoPhone.hangUpCall(sipId, status: Int32(status)) {
            isEnded = true
            return true
        }

        return false
    }
    
    func answerCall(_ status: Int) -> Bool {
        if hasValidSipId() && abtoPhone.answerCall(sipId, status: Int32(status), withVideo: isVideo) {
            isAnswered = true
            return true
        }

        return false
    }
    
    func holdToggleCall() -> Bool {
        if hasValidSipId() && abtoPhone.holdRetrieveCall(sipId) {
            isLocalHold = !isLocalHold
            return true
        }

        return false
    }
    
    func muteCall(_ activate: Bool) {
        if hasValidSipId() && abtoPhone.muteMicrophone(sipId, on: activate) {
            isMute = activate
        }
    }

    func sendDtmf(_ dtmf: String?) {
        guard hasValidSipId(), let dtmf = dtmf, dtmf.count > 0, let tone = dtmf[dtmf.startIndex].asciiValue else {
            return
        }
        
        abtoPhone.sendTone(sipId, tone: unichar(tone))
    }
    
    func enableVideoIfRequired() {
        guard hasValidSipId(), !isConnected, !isEnded else {
            return
        }

        abtoPhone.setSendingRtpVideo(sipId, on: isSendingVideo)
    }
    
    func transferCall(_ toContact: String?) {
        guard hasValidSipId(), let toContact = toContact else {
            return
        }

        abtoPhone.transferCall(sipId, toContact: toContact)
    }

    func setCurrent() {
        guard hasValidSipId() else {
            return
        }

        abtoPhone.setCurrentCall(sipId)
    }
    
    func toggleVideoSend() {
        guard hasValidSipId() else {
            return
        }

        let state = !isSendingVideo

        if abtoPhone.setSendingRtpVideo(sipId, on: state) {
            isSendingVideo = state
        }
    }
    
    func hasValidSipId() -> Bool {
        return sipId != kInvalidCallId
    }
    
    func startRecordingCall(_ filename: String) -> Bool {
        guard hasValidSipId(), !isRecordingCall else {
            return false
        }
        
        isRecordingCall = abtoPhone.startRecording(for: sipId, filePath: filename)
        return true
    }
    
    func switchCamera(toFront: Bool) -> Bool {
        guard hasValidSipId() else {
            return false
        }

        return abtoPhone.switchCamera(toFront: sipId, on: toFront)
    }

    func stopRecordingCall() {
        guard isRecordingCall else {
            return
        }
        
        abtoPhone.stopRecording()
    }
    
    func setVideoViews(localView: UIImageView?, remoteView: UIImageView?) {
        guard hasValidSipId() else {
            return
        }
        
        abtoPhone.setCall(sipId, localView: localView)
        abtoPhone.setCall(sipId, remoteView: remoteView)
    }
    
}
