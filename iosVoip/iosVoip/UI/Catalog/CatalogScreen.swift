import SwiftUI
import shared
import Toaster

struct CatalogScreen: View {
    @ObservedObject private(set) var viewModel: CatalogVM
    
    @State private var searchQuery: String = ""
    
    @State private var searchType = SearchType.units
    @State private var searchTypeIsOn = true
    
    var body: some View {
        NavigationView {
            listView()
                .navigationBarTitle(viewModel.catalogTitle)
                .navigationBarItems(
                    leading:
                        Button(action: {
                            viewModel.goBack()
                        }){
                            Image(systemName: "chevron.backward")
                        },
                    trailing:
                        HStack{
                            Toggle(isOn: $searchTypeIsOn){
                                Text(searchTypeIsOn ? "Поиск сотрудников..." : "Поиск подразделений...")
                            }
                            Button(action: {
                                self.viewModel.getUnitByCodeStr(codeStr: viewModel.startCodeStr)
                            }) {
                                Image(systemName: "arrow.triangle.2.circlepath")
                            }
                        }
                )
        }.searchable(text: $searchQuery, placement: .navigationBarDrawer(displayMode: .always), prompt:
                        Text(searchTypeIsOn ? "Поиск сотрудников..." : "Поиск подразделений...")) {
            Text("Когос").searchCompletion("Когос")
            Text("Романов").searchCompletion("Романов")
            Text("Климов").searchCompletion("Климов")
            Text("Егоров").searchCompletion("Егоров")
        }.onSubmit(of: .search) {
            performSearch(searchQuery: searchQuery, searchType: searchTypeIsOn ? SearchType.users : SearchType.units)
        }
    }
    
    private func performSearch(searchQuery : String, searchType: SearchType){
        viewModel.performSearch(searchQuery: searchQuery, searchType: searchType)
        Toast(text: "Поиск по запросу \"\(searchQuery)\"...", duration: Delay.short).show()
    }
    
    private func listView() -> AnyView {
        let someUnits: [UnitM] = []
        let someAppointments: [Appointment] = []
        
        let units = viewModel.loadingResource
        switch units {
        case is ResourceLoading<UnitM>:
            return AnyView(
                ProgressView("Загрузка...")
                    .progressViewStyle(CircularProgressViewStyle(tint: .orange))
            )
        case is ResourceSuccess<UnitM>:
            return AnyView(List{
                ForEach(units.data?.appointments ?? someAppointments){ item in
                    AppointmentRow(appointment: item)
                }
                ForEach(units.data?.children ?? someUnits){ item in
                    UnitRow(viewModel: viewModel, unit: item)
                }
            })
        case is ResourceError<UnitM>:
            return AnyView(Text("Ошибка").multilineTextAlignment(.center))
        default:
            return AnyView(AnyView(Text("Ошибка").multilineTextAlignment(.center)))
        }
    }
}

extension UnitM : Identifiable{}
extension Appointment : Identifiable{}
extension NameItem : Identifiable{}

enum LoadableUnit {
    case loading
    case result(UnitM)
    case error(String)
}

enum LoadableNameItem{
    case loading
    case result(NameItem)
    case error(String)
}
