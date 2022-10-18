package ru.mephi.voip.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.mephi.voip.data.FavouritesRepository
import ru.mephi.voip.entities.preview.FavouritePreview
import ru.mephi.voip.entities.status.FavouritesStatus
import ru.mephi.voip.utils.getImageUrl

class FavouritesViewModel(
    favouritesRepo: FavouritesRepository
) : ViewModel() {
    private val _favouritesStatus = MutableStateFlow(FavouritesStatus.LOADING)
    val favouritesStatus = _favouritesStatus.asStateFlow()
    private val _favouritesList = MutableStateFlow(emptyList<FavouritePreview>())
    val favouritesList = _favouritesList.asStateFlow()

    init {
        favouritesRepo.getAllFavourites()
        viewModelScope.launch {
            favouritesRepo.rawFavouritesList.collect { list ->
                _favouritesList.value = list.map { FavouritePreview(
                    phoneNumber = it.sipNumber,
                    displayedPhoneNumber = "Номер: ${it.sipNumber}",
                    avatarUrl = getImageUrl(it.sipNumber),
                    displayedName = it.sipName
                ) }
            }
        }
        viewModelScope.launch {
            favouritesList.collect { list ->
                when(list.isEmpty()) {
                    true -> _favouritesStatus.value = FavouritesStatus.EMPTY
                    false -> _favouritesStatus.value = FavouritesStatus.EXISTS
                }
            }
        }
    }
}

