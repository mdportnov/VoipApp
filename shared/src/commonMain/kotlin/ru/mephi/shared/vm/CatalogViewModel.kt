package ru.mephi.shared.vm

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mephi.shared.base.MainIoExecutor
import ru.mephi.shared.data.database.CatalogDao
import ru.mephi.shared.data.database.SearchDB
import ru.mephi.shared.data.model.SearchRecord
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.repo.VoIPServiceRepository


class CatalogViewModel(
    private val lVM: LoggerViewModel,
    private val vsRepo: VoIPServiceRepository,
    private val cDao: CatalogDao,
    private val searchDB: SearchDB
) : MainIoExecutor() {

    val navigateUnitMap: MutableMap<String, MutableStateFlow<UnitM>> = mutableMapOf()
    val selectedSearchHistory = MutableStateFlow(emptyList<String>())

    var currentSearchStr = MutableStateFlow("")
    var currentSearchType = MutableStateFlow(SearchType.SEARCH_USER)

    private var totalSearchHistory = mutableListOf<SearchRecord>()

    private var job: Job = launch(ioDispatcher) { }

    init {
        MutableStateFlow(UnitM()).let {
            navigateUnitMap[CatalogUtils.INIT_CODE_STR] = it
            searchUnitByCodeStr(CatalogUtils.INIT_CODE_STR, it)
        }
        launch(ioDispatcher) {
            totalSearchHistory.addAll(searchDB.getAll().reversed())
            currentSearchStr.combine(currentSearchType) { str, type ->
                totalSearchHistory.filter { s ->
                    s.searchType == type && s.searchStr.contains(str)
                }.map { s -> s.searchStr }
            }.collect { s -> selectedSearchHistory.value = s }
        }
    }

    fun navigateNext(
        codeStr: String,
        shortname: String
    ) {
        navigateUnitMap[codeStr]?.let {
            if (!(it.value.appointment_num == "" && it.value.child_num == "")) {
                return
            }
        }
        MutableStateFlow(UnitM(
            shortname = shortname,
            code_str = codeStr
        )).let {
            navigateUnitMap[codeStr] = it
            searchUnitByCodeStr(codeStr, it)
        }
    }

    fun navigateBack() {
        job.cancel()
    }

    private fun searchUnitByCodeStr(
        codeStr: String,
        stackItem: MutableStateFlow<UnitM>
    ) {
        job.cancel()
        job = launch(ioDispatcher) {
            if (cDao.isUnitExistsByCodeStr(codeStr)) {
                stackItem.value = cDao.getUnitByCodeStr(codeStr)
                return@launch
            }
            vsRepo.getUnitByCodeStr(codeStr).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> { lVM.e("Loading") }
                    is Resource.Success -> {
                        resource.data?.let { unit ->
                            stackItem.value = unit
                        }
                    }
                    is Resource.Error.EmptyError -> { lVM.e("EmptyError") }
                    is Resource.Error.NotFoundError -> { lVM.e("NotFoundError") }
                    is Resource.Error.NetworkError -> { lVM.e("NetworkError") }
                    is Resource.Error.ServerNotRespondError -> { lVM.e("ServerNotRespondError") }
                    is Resource.Error.UndefinedError -> { lVM.e("UndefinedError") }
                }
            }.launchIn(this)
        }
    }

    fun runSearch(
        searchStr: String,
        searchType: SearchType
    ) {
        job.cancel()

        if (searchStr.length <= 3) {
            lVM.e("searchStr is too short!")
            return
        }

        addSearchRecord(searchStr, searchType)
        val codeStr = CatalogUtils.getCodeStrBySearch(searchStr, searchType)
        MutableStateFlow(UnitM(
            shortname = searchStr,
            code_str = codeStr
        )).let {
            navigateUnitMap[codeStr] = it
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
        stackItem: MutableStateFlow<UnitM>
    ) {
        job = launch(ioDispatcher) {
            vsRepo.getUserByPhone(SIP).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        resource.data?.let { app ->
                            stackItem.value.let { unit ->
                                stackItem.value = UnitM(
                                    shortname = unit.shortname,
                                    code_str = unit.code_str,
                                    appointment_num = "1",
                                    appointments = listOf(app)
                                )
                            }
                        }
                    }
                    is Resource.Error.EmptyError -> {}
                    is Resource.Error.NotFoundError -> {}
                    is Resource.Error.NetworkError -> {}
                    is Resource.Error.ServerNotRespondError -> {}
                    is Resource.Error.UndefinedError -> {}
                }
            }.launchIn(this)
        }
    }

    private fun searchUserByName(
        searchName: String,
        stackItem: MutableStateFlow<UnitM>
    ) {
        lVM.e(stackItem.value.toString())
        job = launch(ioDispatcher) {
            vsRepo.getUsersByName(searchName).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> { }
                    is Resource.Success -> {
                        resource.data?.let { unit ->
                            stackItem.value = unit
                        }
                    }
                    is Resource.Error.EmptyError -> { }
                    is Resource.Error.NotFoundError -> { }
                    is Resource.Error.NetworkError -> { }
                    is Resource.Error.ServerNotRespondError -> { }
                    is Resource.Error.UndefinedError -> { }
                }
            }.launchIn(this)
        }
    }

    private fun searchUnit(
        searchStr: String,
        stackItem: MutableStateFlow<UnitM>
    ) {
        job = launch(ioDispatcher) {
            vsRepo.getUnitsByName(searchStr).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> { }
                    is Resource.Success -> {
                        resource.data?.let { units ->
                            stackItem.value.let { unit ->
                                stackItem.value = UnitM(
                                    shortname = unit.shortname,
                                    code_str = unit.code_str,
                                    child_num = units.size.toString(),
                                    children = units
                                )
                            }
                        }
                    }
                    is Resource.Error.NotFoundError -> { }
                    is Resource.Error.UndefinedError -> { }
                    is Resource.Error.EmptyError -> { }
                    is Resource.Error.NetworkError -> { }
                    is Resource.Error.ServerNotRespondError -> { }
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

    fun getCodeStrBySearch(
        searchStr: String,
        searchType: SearchType,
    ): String {
        return "$searchStr|${searchType.name}"
    }
}

enum class SearchType{
    SEARCH_UNIT, SEARCH_USER
}
