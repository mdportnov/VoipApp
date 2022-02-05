package ru.mephi.voip.ui.catalog

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.mephi.shared.*
import ru.mephi.shared.base.MainIoExecutor
import ru.mephi.shared.data.model.SearchRecord
import ru.mephi.shared.data.model.SearchType
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.repository.CatalogRepository
import ru.mephi.voip.R

class CatalogViewModel(private val repository: CatalogRepository) : MainIoExecutor() {
    var catalogStack: Stack<UnitM> = mutableListOf()
    var catalogLiveData: MutableLiveData<Stack<UnitM>> = MutableLiveData()
    var breadcrumbStack: Stack<UnitM> = mutableListOf()
    var breadcrumbLiveData: MutableLiveData<Stack<UnitM>> = MutableLiveData()

    private fun pushPageToCatalog(newPage: UnitM) {
        catalogStack.push(newPage)
        catalogLiveData.postValue(catalogStack)
        breadcrumbStack.push(catalogStack.peek()!!)
        breadcrumbLiveData.postValue(breadcrumbStack)
    }

    fun getSearchRecords() = repository.getSearchRecords()

    fun addSearchRecord(record: SearchRecord) =
        if (!containsSearchRecord(record)) repository.addSearchRecord(record) else Unit

    private fun containsSearchRecord(searchRecord: SearchRecord) =
        repository.containsSearchRecord(searchRecord)

    init {
        catalogLiveData.value = catalogStack
        breadcrumbLiveData.value = breadcrumbStack
    }

    fun goBack() {
        catalogStack.pop()
        catalogLiveData.postValue(catalogStack)
        breadcrumbStack.pop()
        breadcrumbLiveData.postValue(breadcrumbStack)
    }

    fun goNext(codeStr: String, currScrollPos: Int = 0) {
        launch(ioDispatcher) {
            repository.getUnitByCodeStr(codeStr)
                .onEach { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            resource.data?.let { units ->
                                units[0].scrollPosition = currScrollPos
                                pushPageToCatalog(units[0])
                            }
                        }
                        is Resource.Error.EmptyError -> {
                            showSnackBar(appContext.getString(R.string.empty_unit))
                        }
                        is Resource.Error.NotFoundError -> {
                            showSnackBar(appContext.getString(R.string.not_found))
                        }
                        is Resource.Error.NetworkError -> {
                            showSnackBar(appContext.getString(R.string.connection_lost))
                        }
                        else -> {}
                    }
                }.launchIn(this)
        }
    }

    fun search(query: String, type: SearchType) {
        launch(ioDispatcher) {
            if (type == SearchType.USERS)
                repository.getUsersByName(query).onEach { resource ->
                    when (resource) {
                        is Resource.Success -> {
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
            else
                repository.getUnitsByName(query).onEach { resource ->
                    when (resource) {
                        is Resource.Success -> {
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

    sealed class Event {
        class ShowSnackBar(val text: String) : Event()
        class ShowToast(val text: String) : Event()
        class ScrollRvTo(val pos: Int = 0) : Event()
        sealed class ProgressBar : Event() {
            object Show : ProgressBar()
            object Dismiss : ProgressBar()
        }
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    private fun showSnackBar(text: String) {
        launch(ioDispatcher) {
            eventChannel.send(Event.ShowSnackBar(text))
        }
    }

    fun dismissProgressBar() = launch(ioDispatcher) {
        eventChannel.send(Event.ProgressBar.Dismiss)
    }

    fun showProgressBar() = launch(ioDispatcher) {
        eventChannel.send(Event.ProgressBar.Show)
    }

    private fun scrollCatalogToStart() = launch(ioDispatcher) {
        eventChannel.send(Event.ScrollRvTo())
    }

    fun goToStartPage() {
        repeat(catalogStack.size - 1) {
            goBack()
        }
    }
}