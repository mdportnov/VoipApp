import SwiftUI

struct OutgoingInterfaceView: View {
    @EnvironmentObject var callManager: CallManager

    @Binding var receiverID: String
    @Binding var hasActivateCall: Bool
    @Binding var callID: UUID?

    var body: some View {
        HStack {
            // MARK: Voice Call
            Button(action: {
                startCall(to: self.$receiverID.wrappedValue, hasVideo: false)
            }) {
                Image(systemName: "phone.fill.arrow.up.right")
            }
        }
    }

    func startCall(to receiverID: String, hasVideo: Bool) {
        let uuid = UUID()
        self.callID = uuid

        callManager.startCall(with: uuid, receiverID: receiverID) { error in
            if let error = error { print(error.localizedDescription) }
            else { self.hasActivateCall = true }
        }
    }
}