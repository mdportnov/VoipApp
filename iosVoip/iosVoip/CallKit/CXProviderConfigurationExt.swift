import UIKit
import CallKit

extension CXProviderConfiguration {

    static var custom: CXProviderConfiguration {
        let configuration = CXProviderConfiguration(localizedName: "VoIP MEPhI Caller")

        // Native call log shows video icon if it was video call.
        configuration.supportsVideo = true
        configuration.maximumCallsPerCallGroup = 1

        // Support generic type to handle *User ID*
        configuration.supportedHandleTypes = [.generic]

        // Icon image forwarding to app in CallKit View
        if let iconImage = UIImage(named: "App Icon") {
            configuration.iconTemplateImageData = iconImage.pngData()
        }

        // Ringing sound
        configuration.ringtoneSound = "Rington.caf"

        return configuration
    }
}
