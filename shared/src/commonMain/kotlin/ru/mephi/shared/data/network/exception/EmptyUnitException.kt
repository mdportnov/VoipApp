package ru.mephi.shared.data.network.exception

class EmptyUnitException : Exception() {
    override val message: String
        get() = "Пустой пункт"
}