package ru.mephi.shared.data.network.exception

class NetworkException : Exception() {
    override val message: String
        get() = "Интернет-соединение отсутствует"
}