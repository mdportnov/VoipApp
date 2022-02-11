import Foundation

let ACTION_OPEN_DTMF = "Send DTMF"
let ACTION_TRANSFER_CALL = "Transfer Call"
let ACTION_HOLD_CALL = "Hold Call"
let ACTION_RESUME_CALL = "Resume Call"
let ACTION_ROUTE_BLUETOOTH = "Bluetooth"
let ACTION_ROUTE_SPEAKER = "Speaker"
let ACTION_ROUTE_INTERNAL = "Internal/Headset"
let ACTION_AUDIO_ROUTE = "Change audio route"
let ACTION_CANCEL = "Cancel"
let ACTION_START_RECORDING = "Start Recording"
let ACTION_STOP_RECORDING = "Stop Recording"

let AUDIO_ROUTE_DIALOG_HEADER = "Audio route"
let AUDIO_ROUTE_DIALOG_MESSAGE = "Select audio output"
let TRANSFER_CALL_DIALOG_HEADER = "Transfer call"
let TRANSFER_CALL_DIALOG_MESSAGE = "Enter number to transfer"

let IM_HISTORY_KEY = "IM_HISTORY"
let IM_FROM_KEY = "IM_FROM"
let IM_TO_KEY = "IM_TO"
let IM_BODY_KEY = "IM_BODY"
let IM_IS_READ_KEY = "IM_IS_READ"

enum PhoneEvents: Int {
    case regSuccess,
    regFailed,
    unregSuccess,
    unregFailed,
    remoteAlerting
}

enum CallEvents: Int {
    case connected,
    disconnected,
    alerting,
    incoming,
    transfering
}

let SETTINGS_KEY = "settings"

let CALL_ID_ARGUMENT       = "callId"
let CONTACT_ARGUMENT       = "contact"
let STATUS_ARGUMENT        = "status"
let MESSAGE_ARGUMENT       = "message"
let VIDEO_ARGUMENT         = "isVideo"

let NOTIFICATION_NEW_IM = "NOTIFICATION_NEW_IM"

let NOTIFICATION_PHONE_EVENT = "NOTIFICATION_PHONE_EVENT"
let NOTIFICATION_CALL_EVENT  = "NOTIFICATION_CALL_EVENT"
let NOTIFICATION_UI_EVENT    = "NOTIFICATION_UI_EVENT"

let BASE_CODEC_PRIORITY = 128

let AUTOLOGIN_KEY = "Autologin"
