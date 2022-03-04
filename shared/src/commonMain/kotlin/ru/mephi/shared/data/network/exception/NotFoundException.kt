package ru.mephi.shared.data.network.exception

class NotFoundException(private val query: String) : Exception() {
    override val message: String
        get() = "По запросу \"${query}\" ничего не найдено"
}