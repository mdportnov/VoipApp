package ru.mephi.voip.ui.catalog

import androidx.core.text.isDigitsOnly
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mephi.shared.Stack
import ru.mephi.shared.base.MainIoExecutor
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.SearchRecord
import ru.mephi.shared.data.model.SearchType
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.pop
import ru.mephi.shared.popFromCatalogTill
import ru.mephi.shared.push
import ru.mephi.voip.data.CatalogRepository

data class HistorySearchModelState(
    val searchText: String = "",
    val historyRecords: List<SearchRecord> = arrayListOf(),
) {
    companion object {
        val Empty = HistorySearchModelState()
    }
}

const val init_code_str = "01 000 00"

class CatalogViewModel(private val repository: CatalogRepository) : MainIoExecutor() {
    private val _expandedCardIdsList = MutableStateFlow(listOf<Int>())
    val expandedCardIdsList: StateFlow<List<Int>> get() = _expandedCardIdsList

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    private val _isProgressBarVisible = MutableStateFlow(false)
    val isProgressBarVisible: StateFlow<Boolean> get() = _isProgressBarVisible

    var isSearchFieldVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var searchType: MutableStateFlow<SearchType> = MutableStateFlow(SearchType.USERS)

    private var allSearchHistory: ArrayList<SearchRecord> = ArrayList()
    private var searchText: MutableStateFlow<String> = MutableStateFlow("")
    private var matchedRecords: MutableStateFlow<List<SearchRecord>> = MutableStateFlow(listOf())

    private val _catalogStack: MutableStateFlow<Stack<UnitM>> = MutableStateFlow(mutableListOf())
    var catalogStack: StateFlow<Stack<UnitM>> = _catalogStack

    // Состояние стека - отображаемая позиция
    private val _catalogStateFlow = MutableStateFlow(0)
    val catalogStateFlow: StateFlow<Int> = _catalogStateFlow

    fun onCardArrowClicked(cardId: Int) {
        _expandedCardIdsList.value = _expandedCardIdsList.value.toMutableList().also { list ->
            if (list.contains(cardId)) list.remove(cardId) else list.add(cardId)
        }
    }

    fun changeSearchType() {
        searchType.value =
            if (searchType.value == SearchType.UNITS) SearchType.USERS else SearchType.UNITS
    }

    fun isExistsInDatabase(codeStr: String) = repository.isExistsInDatabase(codeStr = codeStr)

    val searchHistoryModelState =
        combine(searchText, matchedRecords) { searchText, matchedSearchRecords ->
            HistorySearchModelState(searchText, matchedSearchRecords)
        }

    fun onSearchTextChanged(changedSearchText: String) {
        searchText.value = changedSearchText
        if (changedSearchText.isEmpty()) {
            matchedRecords.value = arrayListOf()
            return
        }
        val searchResults = allSearchHistory.filter { x ->
            x.name.startsWith(
                changedSearchText, true
            ) && x.type == searchType.value && !x.name.equals(changedSearchText, ignoreCase = true)
        }
        matchedRecords.value = searchResults
    }

    fun onClearClick() {
        searchText.value = ""
        isSearchFieldVisible.value = false
        matchedRecords.value = arrayListOf()
    }

    fun retrieveSearchHistory() {
        allSearchHistory.clear()
        allSearchHistory.addAll(repository.getSearchRecords())
    }

    fun performSearch(query: String) {
        if (query.length < 3) {
            showSnackBar("Введите более длинный запрос")
        } else _catalogStack.value.lastOrNull()?.let {
            if (it.shortname != query) {
                search(query, searchType.value)
            }
        }
    }

    private fun addSearchRecord(record: SearchRecord) =
        if (!containsSearchRecord(record)) repository.addSearchRecord(record) else Unit

    private fun containsSearchRecord(searchRecord: SearchRecord) =
        repository.containsSearchRecord(searchRecord)

    fun onRefresh() {
        _catalogStateFlow.value = _catalogStateFlow.value
        if (catalogStack.value.isEmpty()) goNext(init_code_str)
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
            repository.getUnitByCodeStr(codeStr).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> showProgressBar()
                    is Resource.Success -> {
                        dismissProgressBar()
                        resource.data?.let { unit ->
                            unit.scrollPosition = currScrollPos
                            pushPageToCatalog(unit)
                        }
                    }
                    is Resource.Error.EmptyError -> showSnackBar(resource.message!!)
                    is Resource.Error.NotFoundError -> showSnackBar(resource.message!!)
                    is Resource.Error.NetworkError -> showSnackBar(resource.message!!)
                    is Resource.Error.ServerNotRespondError -> showSnackBar(resource.message!!)
                    is Resource.Error.UndefinedError -> showSnackBar(resource.message!!)
                }
            }.launchIn(this)
        }
    }

    private fun search(query: String, type: SearchType) {
        addSearchRecord(SearchRecord(name = query, type = type))
        launch(ioDispatcher) {
            if (type == SearchType.USERS) if (query.isDigitsOnly()) {
                repository.getUserByPhone(query).onEach { resource ->
                    when (resource) {
                        is Resource.Loading -> showProgressBar()
                        is Resource.Success -> {
                            dismissProgressBar()
                            resource.data?.let { newPage ->
                                pushPageToCatalog(newPage)
                            }
                        }
                        is Resource.Error.NotFoundError -> showSnackBar(resource.message!!)
                        is Resource.Error.UndefinedError -> showSnackBar(resource.message!!)
                        is Resource.Error.EmptyError -> showSnackBar(resource.message!!)
                        is Resource.Error.NetworkError -> showSnackBar(resource.message!!)
                        is Resource.Error.ServerNotRespondError -> showSnackBar(resource.message!!)
                    }
                }.launchIn(this)
            } else repository.getUsersByName(query).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> showProgressBar()
                    is Resource.Success -> {
                        dismissProgressBar()
                        resource.data?.let { newPage ->
                            pushPageToCatalog(newPage)
                        }
                    }
                    is Resource.Error.NotFoundError -> showSnackBar(resource.message!!)
                    is Resource.Error.UndefinedError -> showSnackBar(resource.message!!)
                    is Resource.Error.EmptyError -> showSnackBar(resource.message!!)
                    is Resource.Error.NetworkError -> showSnackBar(resource.message!!)
                    is Resource.Error.ServerNotRespondError -> showSnackBar(resource.message!!)
                }
            }.launchIn(this)
            else repository.getUnitsByName(query).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> showProgressBar()
                    is Resource.Success -> {
                        dismissProgressBar()
                        resource.data?.let { newPage ->
                            pushPageToCatalog(newPage)
                        }
                    }
                    is Resource.Error.NotFoundError -> showSnackBar(resource.message!!)
                    is Resource.Error.UndefinedError -> showSnackBar(resource.message!!)
                    is Resource.Error.EmptyError -> showSnackBar(resource.message!!)
                    is Resource.Error.NetworkError -> showSnackBar(resource.message!!)
                    is Resource.Error.ServerNotRespondError -> showSnackBar(resource.message!!)
                }
            }.launchIn(this)
        }
    }

    sealed class Event {
        class ShowSnackBar(val text: String) : Event()
        object DismissSnackBar : Event()
    }

    private val snackBarEventsChannel = MutableSharedFlow<Event>()
    val snackBarEvents = snackBarEventsChannel.asSharedFlow()

    private fun showSnackBar(text: String) {
        dismissProgressBar()
        launch(ioDispatcher) {
            snackBarEventsChannel.emit(Event.ShowSnackBar(text))
        }
    }

    private fun dismissProgressBar() {
        _isProgressBarVisible.value = false
    }

    private fun showProgressBar() {
        _isProgressBarVisible.value = true
    }

    fun goToStartPage() {
        repeat(catalogStack.value.size - 1) {
            goBack()
        }
    }

    fun addToFavourite(record: Appointment) = repository.addToFavourite(record)
}