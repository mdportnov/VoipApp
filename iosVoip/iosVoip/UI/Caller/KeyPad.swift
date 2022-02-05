import SwiftUI

struct KeyPad: View {
    @Binding var string: String
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        VStack {
            KeyPadRow(keys: ["1", "2", "3"])
            KeyPadRow(keys: ["4", "5", "6"])
            KeyPadRow(keys: ["7", "8", "9"])
            KeyPadRow(keys: ["", "0", "⌫"])
        }.environment(\.keyPadButtonAction, self.keyWasPressed(_:))
            .padding()
            .background(colorScheme == .dark ? .black : .white)
            .cornerRadius(30)
            .shadow(color: .black.opacity(0.3), radius: 2)
            .frame(height: 400)
    }

    private func keyWasPressed(_ key: String) {
        switch key {
        case "⌫":
            if string.isEmpty { string = "" }
            else { string.removeLast() }
        default: string += key
        }
    }
}

struct KeyPad_Previews: PreviewProvider {
    static var previews: some View {
        KeyPad(string: .constant("09025"))
            .padding()
            .frame(height: 400)
//            .previewLayout(.sizeThatFits)
    }
}

