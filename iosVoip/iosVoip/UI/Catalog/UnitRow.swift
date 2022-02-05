import SwiftUI
import shared

struct UnitRow: View {
    var unit: UnitM
    var viewModel: CatalogVM
    
    init(viewModel: CatalogVM, unit: UnitM){
        self.viewModel = viewModel
        self.unit = unit
    }
    
    var body: some View {
        HStack(){
            Text(unit.name).multilineTextAlignment(.leading).onTapGesture {
                viewModel.goNext(codeStr: unit.code_str)
            }.foregroundColor(
                Reachability.isConnectedToNetwork() ? .black : viewModel.catalogDao.checkByCodeStr(code_str: unit.code_str) ? .black : .gray
            )
            Spacer()
        }
    }
}
