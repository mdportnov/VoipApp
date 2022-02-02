package ru.mephi.shared.data.network

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Success<T>(data: T? = null) : Resource<T>(data)
    sealed class Error<T>(message: String? = null, data: T? = null) : Resource<T>(data, message) {
        class EmptyError<T>(message: String? = null, data: T? = null) : Error<T>(message, data)
        class NotFoundError<T>(message: String, data: T? = null) : Error<T>(message, data)
        class UndefinedError<T>(message: String? = null, data: T? = null) : Error<T>(message, data)
    }
}