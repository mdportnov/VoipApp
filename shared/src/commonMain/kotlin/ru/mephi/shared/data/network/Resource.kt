package ru.mephi.shared.data.network

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Success<T>(data: T? = null) : Resource<T>(data)
    sealed class Error<T>(exception: Exception? = null, data: T? = null) :
        Resource<T>(data, exception?.message) {
        class EmptyError<T>(exception: Exception? = null, data: T? = null) :
            Error<T>(exception, data)

        class NetworkError<T>(exception: Exception? = null, data: T? = null) :
            Error<T>(exception, data)

        class NotFoundError<T>(exception: Exception? = null, data: T? = null) :
            Error<T>(exception, data)

        class UndefinedError<T>(exception: Exception? = null, data: T? = null) :
            Error<T>(exception, data)
    }
}