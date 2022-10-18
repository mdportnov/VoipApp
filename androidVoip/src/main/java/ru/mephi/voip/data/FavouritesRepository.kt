package ru.mephi.voip.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.mephi.shared.data.database.FavouritesDB
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.FavouriteRecord

class FavouritesRepository(
    private val favoritesDB: FavouritesDB
) {
    private val _rawFavouritesList = MutableStateFlow(emptyList<FavouriteRecord>())
    val rawFavouritesList = _rawFavouritesList.asStateFlow()

    fun getAllFavourites() {
        _rawFavouritesList.value = favoritesDB.getAllFavourites()
    }

    fun addFavourite(appointment: Appointment): SavingResult {
        return when {
            appointment.line.isEmpty() -> SavingResult.EMPTY_LINE
            favoritesDB.isExists(appointment.line) -> SavingResult.ALREADY_SAVED
            else -> {
                FavouriteRecord(sipName = appointment.fio, sipNumber = appointment.line).let { favRec ->
                    favoritesDB.addFavourite(favRec)
                    _rawFavouritesList.value = _rawFavouritesList.value.toMutableList().apply { add(favRec) }
                }
                SavingResult.SUCCESS
            }
        }
    }

    fun removeFavourite(phoneNumber: String) {
        _rawFavouritesList.value.firstOrNull { it.sipNumber == phoneNumber }?.let { favRec ->
            favoritesDB.deleteRecords(favRec)
            _rawFavouritesList.value = _rawFavouritesList.value.toMutableList().apply { remove(favRec) }
        }
    }

    fun removeAllFavourites() {
        favoritesDB.deleteAll()
        _rawFavouritesList.value = emptyList()
    }
}