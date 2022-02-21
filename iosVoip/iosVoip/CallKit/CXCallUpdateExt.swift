import CallKit

extension CXCallUpdate {
    func update(with remoteUserID: String, incoming: Bool) {
        // the other caller is identified by a CXHandle object
        let remoteHandle = CXHandle(type: .generic, value: remoteUserID)

        self.remoteHandle = remoteHandle
        localizedCallerName = remoteUserID
    }

    func onFailed(with uuid: UUID) {
        let remoteHandle = CXHandle(type: .generic, value: "Unknown")

        self.remoteHandle = remoteHandle
        localizedCallerName = "Unknown"
    }
}