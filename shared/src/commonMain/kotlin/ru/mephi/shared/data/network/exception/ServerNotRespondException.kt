package ru.mephi.shared.data.network.exception

class ServerNotRespondException : Exception() {
    override val message: String
        get() = "Сервер не отвечает"
}