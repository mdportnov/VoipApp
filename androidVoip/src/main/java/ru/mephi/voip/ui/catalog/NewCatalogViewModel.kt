package ru.mephi.voip.ui.catalog

import androidx.core.text.isDigitsOnly
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mephi.shared.*
import ru.mephi.shared.base.MainIoExecutor
import ru.mephi.shared.data.model.SearchRecord
import ru.mephi.shared.data.model.SearchType
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.data.network.Resource
import ru.mephi.voip.R
import ru.mephi.voip.data.CatalogRepository
import ru.mephi.voip.utils.isOnline
import ru.mephi.voip.utils.toast

data class HistorySearchModelState(
    val searchText: String = "",
    val historyRecords: List<SearchRecord> = arrayListOf(),
) {
    companion object {
        val Empty = HistorySearchModelState()
    }
}

class NewCatalogViewModel(private val repository: CatalogRepository) : MainIoExecutor() {
    private val _expandedCardIdsList = MutableStateFlow(listOf<Int>())
    val expandedCardIdsList: StateFlow<List<Int>> get() = _expandedCardIdsList

    fun onCardArrowClicked(cardId: Int) {
        _expandedCardIdsList.value = _expandedCardIdsList.value.toMutableList().also { list ->
            if (list.contains(cardId)) list.remove(cardId) else list.add(cardId)
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    private val _isProgressBarVisible = MutableStateFlow(false)
    val isProgressBarVisible: StateFlow<Boolean> get() = _isProgressBarVisible

    fun changeSearchType() {
        searchType.value = if (searchType.value == SearchType.UNITS)
            SearchType.USERS else SearchType.UNITS
    }

    var isSearchFieldVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var allSearchHistory: ArrayList<SearchRecord> = ArrayList()
    private var searchText: MutableStateFlow<String> = MutableStateFlow("")
    var searchType: MutableStateFlow<SearchType> = MutableStateFlow(SearchType.UNITS)
    private var showProgressBar: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var matchedSearchRecords: MutableStateFlow<List<SearchRecord>> =
        MutableStateFlow(arrayListOf())

    init {
        retrieveSearchHistory()
        goNext("01 000 00")
    }

    val searchHistoryModelState = combine(
        searchText,
        matchedSearchRecords,
    ) { searchText, matchedSearchRecords ->
        HistorySearchModelState(
            searchText,
            matchedSearchRecords,
        )
    }

    fun onSearchTextChanged(changedSearchText: String) {
        searchText.value = changedSearchText
        if (changedSearchText.isEmpty()) {
            matchedSearchRecords.value = arrayListOf()
            return
        }
        val searchResults = allSearchHistory.filter { x ->
            x.name.startsWith(changedSearchText, true) && x.type == searchType.value
        }
        matchedSearchRecords.value = searchResults
    }

    fun onClearClick() {
        searchText.value = ""
        isSearchFieldVisible.value = false
        matchedSearchRecords.value = arrayListOf()
    }

    private fun retrieveSearchHistory() {
        allSearchHistory.addAll(repository.getSearchRecords())
    }

    fun performSearch(query: String) {
        _catalogStack.value.lastOrNull()?.let {
            if (it.shortname != query) {
                if (searchType.value == SearchType.USERS) {
                    search(query, SearchType.USERS)
                }
                if (searchType.value == SearchType.UNITS) {
                    search(query, SearchType.UNITS)
                }
            }
        }
    }

    private val _catalogStack: MutableStateFlow<Stack<UnitM>> = MutableStateFlow(mutableListOf())
    var catalogStack: StateFlow<Stack<UnitM>> = _catalogStack

    // Состояние стека - отображаемая позиция
    private val _catalogStateFlow = MutableStateFlow(0)
    val catalogStateFlow: StateFlow<Int> = _catalogStateFlow

    private fun addSearchRecord(record: SearchRecord) =
        if (!containsSearchRecord(record)) repository.addSearchRecord(record) else Unit

    private fun containsSearchRecord(searchRecord: SearchRecord) =
        repository.containsSearchRecord(searchRecord)

    fun onRefresh() {
        if (isOnline(appContext)) {
            if (catalogStack.value.isNullOrEmpty())
                goNext("01 000 000")
        } else {
            appContext.toast("Обновление невозможно")
//            showSnackBar
        }
        _isRefreshing.value = false
    }

    fun goBack() {
        dismissProgressBar()
        _expandedCardIdsList.value = listOf()
        _catalogStack.value.pop()
        _catalogStateFlow.value--
    }

    private fun pushPageToCatalog(newPage: UnitM) {
        _expandedCardIdsList.value = listOf()
        _catalogStack.value.push(newPage)
        _catalogStateFlow.value++
    }

    fun popFromCatalogTill(stackItem: UnitM) {
        val count = catalogStack.value.popFromCatalogTill(stackItem)
        _catalogStateFlow.value -= count
    }

    private var jobGoNext: Job? = null

    fun goNext(codeStr: String, currScrollPos: Int = 0) {
        jobGoNext?.cancel()
        jobGoNext = launch(ioDispatcher) {
            repository.getUnitByCodeStr(codeStr)
                .onEach { resource ->
                    when (resource) {
                        is Resource.Loading -> showProgressBar()
                        is Resource.Success -> {
                            dismissProgressBar()
                            resource.data?.let { units ->
                                units[0].scrollPosition = currScrollPos
                                pushPageToCatalog(units[0])
                            }
                        }
                        is Resource.Error.EmptyError -> {
                            showSnackBar(appContext.getString(R.string.empty_unit))
                        }
                        is Resource.Error.NotFoundError -> {
                            showSnackBar(resource.message!!)
                        }
                        is Resource.Error.NetworkError -> {
                            showSnackBar(appContext.getString(R.string.connection_lost))
                        }
                        is Resource.Error.ServerNotRespondError -> {
                            showSnackBar(appContext.getString(R.string.smth_wrong))
                        }
                        else -> dismissProgressBar()
                    }
                }.launchIn(this)
        }
    }

    private fun search(query: String, type: SearchType) {
        addSearchRecord(SearchRecord(name = query, type = type))
        launch(ioDispatcher) {
            if (type == SearchType.USERS)
                if (query.isDigitsOnly()) {
                    repository.getUserByPhone(query).onEach { resource ->
                        when (resource) {
                            is Resource.Loading -> showProgressBar()
                            is Resource.Success -> {
                                dismissProgressBar()
                                resource.data?.let { newPage ->
                                    pushPageToCatalog(newPage)
                                    scrollCatalogToStart()
                                }
                            }
                            is Resource.Error.NotFoundError -> {
                                resource.message?.let { showSnackBar(it) }
                            }
                            is Resource.Error.UndefinedError -> {
                                resource.message?.let {
                                    showSnackBar(it)
                                }
                            }
                            else -> dismissProgressBar()
                        }
                    }.launchIn(this)
                } else
                    repository.getUsersByName(query).onEach { resource ->
                        when (resource) {
                            is Resource.Loading -> showProgressBar()
                            is Resource.Success -> {
                                dismissProgressBar()
                                resource.data?.let { newPage ->
                                    pushPageToCatalog(newPage)
                                    scrollCatalogToStart()
                                }
                            }
                            is Resource.Error.NotFoundError -> {
                                resource.message?.let { showSnackBar(it) }
                            }
                            is Resource.Error.UndefinedError -> {
                                resource.message?.let {
                                    showSnackBar(it)
                                }
                            }
                            else -> dismissProgressBar()
                        }
                    }.launchIn(this)
            else
                repository.getUnitsByName(query).onEach { resource ->
                    when (resource) {
                        is Resource.Loading -> showProgressBar()
                        is Resource.Success -> {
                            dismissProgressBar()
                            resource.data?.let { newPage ->
                                pushPageToCatalog(newPage)
                                scrollCatalogToStart()
                            }
                        }
                        is Resource.Error.NotFoundError -> {
                            resource.message?.let { showSnackBar(it) }
                        }
                        is Resource.Error.UndefinedError -> {
                            resource.message?.let {
                                showSnackBar(it)
                            }
                        }
                        else -> {}
                    }
                }.launchIn(this)
        }
    }

    private fun showSnackBar(text: String) {
        dismissProgressBar()
        launch(ioDispatcher) {
//            eventChannel.send(Event.ShowSnackBar(text))
        }
    }

    fun dismissProgressBar() {
        _isProgressBarVisible.value = false
    }

    private fun showProgressBar() {
        _isProgressBarVisible.value = true
    }

    private fun scrollCatalogToStart() = launch(ioDispatcher) {
//        eventChannel.send(Event.ScrollRvTo())
    }

    fun goToStartPage() {
//        repeat(catalogStack.size - 1) {
//            goBack()
//        }
    }
}