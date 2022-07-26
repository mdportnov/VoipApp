import SwiftUI
import shared
import Toaster

class CatalogVM: ObservableObject {
    var startCodeStr = "01 000 00"
    var currentCodeStr: String

    var catalogStack = NSMutableArray()

    let catalogDao: CatalogDao

    let api = KtorApiService()

    let searchDB = SearchDB()

    @Published var loadingResource: Resource<UnitM>

    @Published var catalogTitle = "VoIP MEPhI"

    init() {
        catalogDao = CatalogDao()
        loadingResource = ResourceLoading<UnitM>(data: nil)
        currentCodeStr = startCodeStr
        getUnitByCodeStr(codeStr: startCodeStr)
    }

    func deleteCache() {
        catalogDao.deleteAll()
    }

    func getSearchRecords() -> [SearchRecord] {
        searchDB.getAll()
    }

    func addSearchRecord(searchRecord: SearchRecord) {
        if (searchDB.isExists(name: searchRecord.name)) {
            return
        }
        searchDB.insert(record: SearchRecord(id: searchRecord.id, name: searchRecord.name, type: searchRecord.type))
    }

    func goNext(codeStr: String) {
        if currentCodeStr != codeStr {
            currentCodeStr = codeStr
            getUnitByCodeStr(codeStr: currentCodeStr)
        }
    }

    func goBack() {
        if catalogStack.count == 1 {
            return
        }

        CommonUtilsKt.pop(catalogStack)
        currentCodeStr = (catalogStack.lastObject as! UnitM).code_str

        if catalogStack.count >= 1 {
            let prevUnit = CommonUtilsKt.peek(catalogStack) as? UnitM
            loadingResource = ResourceSuccess(data: prevUnit)
            catalogTitle = prevUnit?.shortname.uppercased() ?? "VoIP MEPhI"
        }
    }

    func performSearch(searchQuery: String, searchType: SearchType) {
        loadingResource = ResourceLoading(data: nil)
        Toast(text: "Поиск по запросу \"\(searchQuery)\"...", duration: Delay.short).show()

        if searchType == SearchType.units {
            api.getUnitsByName(filterLike: searchQuery) { resource, error in
                switch resource {
                case is ResourceErrorNotFoundError<NSArray>:
                    self.loadingResource = ResourceError(exception: nil, data: nil)
                    Toast(text: resource?.message, duration: Delay.short).show()
                case is ResourceSuccess<NSArray>:
                    let remoteUnits = resource?.data ?? []

                    if remoteUnits.count == 0 {
                        Toast(text: resource?.message, duration: Delay.short).show()
                        self.loadingResource = ResourceError(exception: nil, data: nil)
                        break
                    }

                    let unitOfUnits = UnitM(
                            code_str: "", name: searchQuery, fullname: searchQuery, shortname: searchQuery,
                            parent_code: "", parent_name: "", children: remoteUnits as? [UnitM], appointments: nil, scrollPosition: nil
                    )

                    self.loadingResource = ResourceSuccess(data: unitOfUnits)
                    CommonUtilsKt.push(self.catalogStack, item: unitOfUnits)
                    self.catalogTitle = searchQuery.uppercased()

                    print("Current stack:\n")
                    self.catalogStack.forEach { item in
                        print((item as! UnitM).shortname)
                    }
                default:
                    Toast(text: resource?.message, duration: Delay.short).show()
                }
            }
        } else {
            api.getUsersByName(filterLike: searchQuery) { resource, error in
                switch resource {
                case is ResourceErrorEmptyError<UnitM>:
                    Toast(text: resource?.message, duration: Delay.short).show()
                    self.loadingResource = ResourceError(exception: nil, data: nil)
                case is ResourceSuccess<UnitM>:
                    print("ResourceSuccess response on .\(searchQuery)!")
                    let unitOfUsers = resource?.data

                    if unitOfUsers == nil || unitOfUsers?.appointments?.count == 0 {
                        Toast(text: "Ничего не найдено", duration: Delay.short).show()
                        break
                    }

                    unitOfUsers?.shortname = searchQuery
                    self.loadingResource = ResourceSuccess(data: unitOfUsers)

                    CommonUtilsKt.push(self.catalogStack, item: unitOfUsers)
                    self.catalogTitle = unitOfUsers?.shortname.uppercased() ?? "VoIP MEPhI"

                    print("Current stack:\n")
                    self.catalogStack.forEach { item in
                        print((item as! UnitM).shortname)
                    }
                default:
                    Toast(text: resource?.message, duration: Delay.short).show()
                }
            }
        }
        addSearchRecord(searchRecord: SearchRecord(id: nil, name: searchQuery, type: searchType))
    }

    func getUnitByCodeStr(codeStr: String) {
        loadingResource = ResourceLoading(data: nil)
        var remoteUnits: NSArray = []

        let currentSavedUnits = catalogDao.getUnitByCodeStr(code_str: codeStr)

        if Reachability.isConnectedToNetwork() {
            api.getUnitByCodeStr(codeStr: codeStr) { resource, error in
                if resource is ResourceErrorNetworkError {
                    print("ResourceErrorNetworkError response on .\(codeStr)!")
                    if currentSavedUnits != nil {
                        let newUnits = self.catalogDao.getUnitByCodeStr(code_str: codeStr)
                        self.loadingResource = ResourceSuccess(data: newUnits)
                        CommonUtilsKt.push(self.catalogStack, item: newUnits)
 //                       self.catalogTitle = newUnits?.first!.shortname.uppercased() ?? "VoIP MEPhI"
                        self.catalogTitle = ""
                    } else {
                        Toast(text: resource?.message, duration: Delay.short).show()
                        self.goBack()
                    }
                }
//                if resource is ResourceSuccess<NSArray> {
//                    print("ResourceSuccess response on .\(codeStr)!")
//                    remoteUnits = resource?.data
//                    currentSavedUnits?.forEach { UnitM in
//                        self.catalogDao.deleteByCodeStr(code_str: UnitM.code_str)
//                    }
//
//                    remoteUnits.forEach { UnitM in
//                        self.catalogDao.add(unitM: (UnitM as AnyObject).toKodeIn)
//                    }
//
//                    let newUnits = self.catalogDao.getUnitByCodeStr(code_str: codeStr)
//                    self.loadingResource = ResourceSuccess(data: newUnits)
//                    CommonUtilsKt.push(self.catalogStack, item: newUnits)
////                    self.catalogTitle = newUnits?.first!.shortname.uppercased() ?? "VoIP MEPhI"
//                    self.catalogTitle = ""
//                    print("Current stack:\n")
//                    self.catalogStack.forEach { item in
//                        print((item as! UnitM).shortname)
//                    }
//                }
            }

        } else {
            if currentSavedUnits != nil {
                let newUnits = self.catalogDao.getUnitByCodeStr(code_str: codeStr)
                self.loadingResource = ResourceSuccess(data: newUnits)
                CommonUtilsKt.push(self.catalogStack, item: newUnits)
//                self.catalogTitle = newUnits?.first!.shortname.uppercased() ?? "VoIP MEPhI"
                self.catalogTitle = ""
            } else {
                Toast(text: "Информация не закэширована", duration: Delay.short).show()
                self.goBack()
            }
        }
    }
}

struct CatalogScreen_Previews: PreviewProvider {
    static var previews: some View {
        CatalogScreen(vm: .init())
    }
}
