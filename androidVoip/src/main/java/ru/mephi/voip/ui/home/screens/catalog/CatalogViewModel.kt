package ru.mephi.voip.ui.home.screens.catalog

import ru.mephi.shared.base.MainIoExecutor
import ru.mephi.shared.data.model.Appointment
import ru.mephi.voip.data.CatalogRepository

// TODO: Remove it
class CatalogViewModel(private val repository: CatalogRepository) : MainIoExecutor() {
    fun addToFavourites(appointment: Appointment) = repository.addToFavourites(appointment)
}