package ru.mephi.shared.data.sip

enum class AccountStatus(val status: String) {
    NO_CONNECTION("Сеть недоступна"),
    LOADING("Подключение..."),
    UNREGISTERED("Не зарегистрирован"),
    REGISTRATION_FAILED("Ошибка регистрации"),
    REGISTERED("Аккаунт активен"),
    CHANGING("Обновление..."),
}
