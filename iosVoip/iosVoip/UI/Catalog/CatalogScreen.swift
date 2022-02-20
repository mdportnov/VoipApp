import SwiftUI
import shared
import Toaster


struct CatalogScreen: View {
    @ObservedObject private(set) var viewModel: CatalogVM

    @State private var searchQuery: String = ""

    @State private var allSearchRecords: [String] = []

    @State private var searchType = SearchType.users
    @State private var searchTypeIsOn = true

    init(vm: CatalogVM) {
        viewModel = vm
    }

    var body: some View {
        NavigationView {
            listView()
                    .navigationBarTitle(viewModel.catalogTitle)
                    .navigationBarItems(
                            leading:
                            Button(action: {
                                viewModel.goBack()
                            }) {
                                Image(systemName: "chevron.backward")
                                        .foregroundColor(viewModel.catalogStack.count == 1 ? .gray : .orange)
                            },
                            trailing:
                            HStack {
                                Toggle(isOn: $searchTypeIsOn) {
                                    Label(searchTypeIsOn ? "Поиск сотрудников..." : "Поиск подразделений...", systemImage: searchTypeIsOn ? "person" : "person.3")
                                }
                                        .onChange(of: searchTypeIsOn) { _isOn in
                                            if (_isOn == true) {
                                                searchType = SearchType.users
                                            } else {
                                                searchType = SearchType.units
                                            }
                                        }
                                Button(action: {
                                    viewModel.getUnitByCodeStr(codeStr: viewModel.startCodeStr)
                                }) {
                                    Image(systemName: "arrow.triangle.2.circlepath")
                                }
                            }
                    )
        }
                .searchable(text: $searchQuery, placement: .navigationBarDrawer(displayMode: .always), prompt:
                Text(searchTypeIsOn ? "Поиск сотрудников..." : "Поиск подразделений...")) {
                    ForEach(allSearchRecords.filter {
                        $0.localizedCaseInsensitiveContains(searchQuery)
                    }, id: \.self) { record in
                        Text(record).searchCompletion(record)
                    }
                }
                .onSubmit(of: .search) {
                    performSearch(searchQuery: searchQuery, searchType: searchTypeIsOn ? SearchType.users : SearchType.units)
                }
                .onAppear {
                    allSearchRecords = viewModel.getSearchRecords().map { (searchRecord: SearchRecord) -> String in
                        searchRecord.name
                    }
                }
    }

    private func performSearch(searchQuery: String, searchType: SearchType) {
        let searchQuery = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines)

        let lastUnit = viewModel.catalogStack.lastObject as! UnitM
        if searchQuery.count < 3 {
            Toast(text: "Слишком короткой запрос", duration: Delay.long).show()
        } else if lastUnit.shortname == searchQuery {
            return
        } else {
            viewModel.performSearch(searchQuery: searchQuery, searchType: searchType)
        }
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
            return AnyView(List {
                ForEach(units.data?.appointments ?? someAppointments) { item in
                    AppointmentRow(appointment: item)
                }
                ForEach(units.data?.children ?? someUnits) { item in
                    UnitRow(viewModel: viewModel, unit: item)
                }
            })
        case is ResourceError<UnitM>:
            return AnyView(List {
                let unit = viewModel.catalogStack.lastObject as! UnitM
                ForEach(unit.appointments ?? someAppointments) { item in
                    AppointmentRow(appointment: item)
                }
                ForEach(unit.children ?? someUnits) { item in
                    UnitRow(viewModel: viewModel, unit: item)
                }
            })
        default:
            return AnyView(AnyView(Text("Ошибка").multilineTextAlignment(.center)))
        }
    }
}

extension UnitM: Identifiable {
}

extension Appointment: Identifiable {
}

extension NameItem: Identifiable {
}

enum LoadableNameItem {
    case loading
    case result(NameItem)
    case error(String)
}
