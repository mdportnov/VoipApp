import SwiftUI

struct KeyPadRow: View {
    var keys: [String]

    var body: some View {
        HStack {
            ForEach(keys, id: \.self) { key in
                KeyPadButton(key: key)
            }
        }
    }
}


struct KeyPadRow_Previews: PreviewProvider {
    static var previews: some View {
        KeyPadRow(keys: ["1", "2", "3"])
            .padding()
            .frame(width: 80, height: 80)
            .previewLayout(.sizeThatFits)
    }
}
