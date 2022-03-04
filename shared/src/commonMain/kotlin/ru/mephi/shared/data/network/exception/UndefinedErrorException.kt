package ru.mephi.shared.data.network.exception

class UndefinedErrorException : Exception() {
    override val message: String
        get() = "Что-то пошло не так"
}