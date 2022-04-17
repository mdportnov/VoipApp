package ru.mephi.shared.data.network.exception

class UndefinedException : Exception() {
    override val message: String
        get() = "Что-то пошло не так"
}