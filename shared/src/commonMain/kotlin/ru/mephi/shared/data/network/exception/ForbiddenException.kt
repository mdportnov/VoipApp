package ru.mephi.shared.data.network.exception

class ForbiddenException : Exception() {
    override val message: String
        get() = "Доступ к ресурсу запрещён"
}