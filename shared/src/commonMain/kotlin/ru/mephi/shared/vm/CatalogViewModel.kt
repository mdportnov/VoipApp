package ru.mephi.shared.vm

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.base.MainIoExecutor
import ru.mephi.shared.data.database.CatalogDao
import ru.mephi.shared.data.database.SearchDB
import ru.mephi.shared.data.model.SearchRecord
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.repo.VoIPServiceRepository
import ru.mephi.shared.utils.pop


class CatalogViewModel : MainIoExecutor(), KoinComponent {

    private val lVM: LoggerViewModel by inject()
    private val vsRepo: VoIPServiceRepository by inject()
    private val cDao: CatalogDao by inject()
    private val searchDB: SearchDB by inject()

    val navigateUnitMap: MutableMap<String, ExtendedUnitM> = mutableMapOf()
    val selectedSearchHistory = MutableStateFlow(emptyList<String>())
    val stack = mutableListOf<StackUnitM>()

    private var totalSearchHistory = mutableListOf<SearchRecord>()

    private var job: Job = launch(ioDispatcher) { }

    init {
        goHome()
        ExtendedUnitM(MutableStateFlow(UnitM())).let {
            navigateUnitMap[CatalogUtils.INIT_CODE_STR] = it
            searchUnitByCodeStr(CatalogUtils.INIT_CODE_STR, it)
        }
        // TODO: load search history
//        launch(ioDispatcher) {
//            totalSearchHistory.addAll(searchDB.getAll().reversed())
//        }
    }

    fun goHome() {
        stack.clear()
        stack.add(StackUnitM(codeStr = CatalogUtils.INIT_CODE_STR, shortname = "МИФИ"))
    }

    fun navigateBack(unitM: UnitM = UnitM()): Int {
        var ret = 0
        if (unitM.code_str.isNotEmpty()) {
            while (stack.isNotEmpty()) {
                if (stack.last().codeStr != unitM.code_str) {
                    ret++
                    stack.pop()
                } else {
                    return ret
                }
            }
        } else {
            ret++
            stack.pop()
        }
        return ret
    }

    fun navigateNext(unitM: UnitM) {
        lVM.e("$unitM")
        stack.add(StackUnitM(unitM.code_str, unitM.shortname))
        navigateUnitMap[unitM.code_str]?.let {
            if (it.unitM.value.children.isEmpty() && it.unitM.value.appointments.isEmpty()) {
                searchUnitByCodeStr(unitM.code_str, it)
            }
        } ?: run {
            ExtendedUnitM(
                unitM = MutableStateFlow(unitM)
            ).let {
                navigateUnitMap[unitM.code_str] = it
                searchUnitByCodeStr(unitM.code_str, it)
            }
        }
    }

    private fun searchUnitByCodeStr(
        codeStr: String,
        extendedUnitM: ExtendedUnitM
    ) {
        lVM.e("searchUnitByCodeStr: called, codeStr=$codeStr")
        job.cancel()
        job = launch(ioDispatcher) {
            if (cDao.isUnitExistsByCodeStr(codeStr)) {
                extendedUnitM.unitM.value = cDao.getUnitByCodeStr(codeStr)
                return@launch
            }
            vsRepo.getUnitByCodeStr(codeStr).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> { extendedUnitM.onLoading() }
                    is Resource.Success -> {
                        resource.data?.let { unit ->
                            extendedUnitM.onOk()
                            cDao.addUnit(unit)
                            extendedUnitM.unitM.value = unit
                        } ?: run { extendedUnitM.onNotFound() }
                    }
                    is Resource.Error.EmptyError -> { extendedUnitM.onNotFound() }
                    is Resource.Error.NotFoundError -> { extendedUnitM.onNotFound() }
                    is Resource.Error.NetworkError -> { extendedUnitM.onNetworkFailure() }
                    is Resource.Error.ServerNotRespondError -> { extendedUnitM.onNetworkFailure() }
                    is Resource.Error.UndefinedError -> { extendedUnitM.onNetworkFailure() }
                }
            }.launchIn(this)
        }
    }

    fun runSearch(
        searchStr: String,
        searchType: SearchType
    ) {
        lVM.e("runSearch: called, searchStr=$searchStr, searchType=$searchType")
        job.cancel()

        if (searchStr.length <= 3) {
            lVM.e("runSearch: searchStr is too short!")
            return
        }

        goHome()
        stack.add(StackUnitM("Search", searchStr))
        addSearchRecord(searchStr, searchType)

        ExtendedUnitM(
            unitM = MutableStateFlow(UnitM(
                shortname = searchStr,
                code_str = "Search"
            ))
        ).let {
            navigateUnitMap["Search"] = it
            when(searchType) {
                SearchType.SEARCH_UNIT -> searchUnit(searchStr, it)
                SearchType.SEARCH_USER -> {
                    if (searchStr.matches(CatalogUtils.ONLY_DIGITS_REGEX)) {
                        searchUserBySIP(searchStr, it)
                    } else {
                        searchUserByName(searchStr, it)
                    }
                }
            }
        }
    }

    private fun searchUserBySIP(
        SIP: String,
        extendedUnitM: ExtendedUnitM
    ) {
        lVM.e("searchUserBySIP: called, SIP=$SIP")
        job = launch(ioDispatcher) {
            vsRepo.getUserByPhone(SIP).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> { extendedUnitM.onLoading() }
                    is Resource.Success -> {
                        resource.data?.let { app ->
                            extendedUnitM.onOk()
                            extendedUnitM.unitM.value.let { unit ->
                                extendedUnitM.unitM.value = UnitM(
                                    shortname = unit.shortname,
                                    code_str = unit.code_str,
                                    appointment_num = "1",
                                    appointments = listOf(app)
                                )
                            }
                        } ?: run { extendedUnitM.onNotFound() }
                    }
                    is Resource.Error.EmptyError -> { extendedUnitM.onNotFound() }
                    is Resource.Error.NotFoundError -> { extendedUnitM.onNotFound() }
                    is Resource.Error.NetworkError -> { extendedUnitM.onNetworkFailure() }
                    is Resource.Error.ServerNotRespondError -> { extendedUnitM.onNetworkFailure() }
                    is Resource.Error.UndefinedError -> { extendedUnitM.onNetworkFailure() }
                }
            }.launchIn(this)
        }
    }

    private fun searchUserByName(
        searchName: String,
        extendedUnitM: ExtendedUnitM
    ) {
        lVM.e("searchUserByName: called, searchName=$searchName")
        job = launch(ioDispatcher) {
            vsRepo.getUsersByName(searchName).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> { extendedUnitM.onLoading() }
                    is Resource.Success -> {
                        resource.data?.let { unit ->
                            extendedUnitM.onOk()
                            extendedUnitM.unitM.value = unit
                        } ?: run { extendedUnitM.onNotFound() }
                    }
                    is Resource.Error.EmptyError -> { extendedUnitM.onNotFound() }
                    is Resource.Error.NotFoundError -> { extendedUnitM.onNotFound() }
                    is Resource.Error.NetworkError -> { extendedUnitM.onNetworkFailure() }
                    is Resource.Error.ServerNotRespondError -> { extendedUnitM.onNetworkFailure() }
                    is Resource.Error.UndefinedError -> { extendedUnitM.onNetworkFailure() }
                }
            }.launchIn(this)
        }
    }

    private fun searchUnit(
        searchStr: String,
        extendedUnitM: ExtendedUnitM
    ) {
        lVM.e("searchUnit: called! Args: searchStr=$searchStr")
        job = launch(ioDispatcher) {
            vsRepo.getUnitsByName(searchStr).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> { extendedUnitM.onLoading() }
                    is Resource.Success -> {
                        resource.data?.let { units ->
                            extendedUnitM.onOk()
                            extendedUnitM.unitM.value.let { unit ->
                                extendedUnitM.unitM.value = UnitM(
                                    shortname = unit.shortname,
                                    code_str = unit.code_str,
                                    child_num = units.size.toString(),
                                    children = units
                                )
                            }
                        } ?: run { extendedUnitM.onNotFound() }
                    }
                    is Resource.Error.NotFoundError -> { extendedUnitM.onNotFound() }
                    is Resource.Error.EmptyError -> { extendedUnitM.onNotFound() }
                    is Resource.Error.NetworkError -> { extendedUnitM.onNetworkFailure() }
                    is Resource.Error.ServerNotRespondError -> { extendedUnitM.onNetworkFailure() }
                    is Resource.Error.UndefinedError -> { extendedUnitM.onNetworkFailure() }
                }
            }.launchIn(this)
        }
    }

    private fun addSearchRecord(
        searchStr: String,
        searchType: SearchType
    ) {
        launch(ioDispatcher) {
            if (!searchDB.isExists(searchStr, searchType)) {
                lVM.d("writing new search record!")
                SearchRecord(
                    searchStr = searchStr,
                    searchType = searchType
                ).let {
                    searchDB.insert(it)
                    totalSearchHistory.add(0, it)
                }
                searchDB.insert(
                    SearchRecord(
                        searchStr = searchStr,
                        searchType = searchType
                    )
                )
            }
        }
    }
}

object CatalogUtils {
    const val INIT_CODE_STR = "01 000 00"
    val ONLY_DIGITS_REGEX = Regex("[0-9]+")
}

class ExtendedUnitM(
    var unitM: MutableStateFlow<UnitM>,
    val status: MutableStateFlow<CatalogStatus> = MutableStateFlow(CatalogStatus.OK)
) {
    fun onOk() { status.value = CatalogStatus.OK }

    fun onLoading() { status.value = CatalogStatus.LOADING }

    fun onNotFound() { status.value = CatalogStatus.NOT_FOUND }

    fun onNetworkFailure() { status.value = CatalogStatus.NETWORK_FAILURE }
}

data class StackUnitM(
    val codeStr: String = "",
    val shortname: String = ""
)

enum class CatalogStatus {
    OK, LOADING, NOT_FOUND, NETWORK_FAILURE
}

enum class SearchType{
    SEARCH_UNIT, SEARCH_USER
}
