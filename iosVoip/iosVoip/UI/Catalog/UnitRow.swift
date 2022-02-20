import SwiftUI
import shared

struct UnitRow: View {
    var unit: UnitM
    var viewModel: CatalogVM

    init(viewModel: CatalogVM, unit: UnitM) {
        self.viewModel = viewModel
        self.unit = unit
    }

    var body: some View {
        HStack {
            Button(action : {
                viewModel.goNext(codeStr: unit.code_str)
            }){
                Text(unit.name).multilineTextAlignment(.leading).foregroundColor(
                        Reachability.isConnectedToNetwork() ? .black : viewModel.catalogDao.checkByCodeStr(code_str: unit.code_str) ? .black : .gray
                )
            }
        }
    }
}
