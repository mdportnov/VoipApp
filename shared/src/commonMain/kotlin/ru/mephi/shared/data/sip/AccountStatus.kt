package ru.mephi.shared.data.sip

enum class AccountStatus(val status: String) {
    NO_CONNECTION("Сеть недоступна"),
    CONNECTING("Подключение..."),
    UNREGISTERED("Не зарегистрирован"),
    REGISTRATION_FAILED("Ошибка регистрации"),
    REGISTERED("Аккаунт активен"),
    RECONNECTING("Переподключение..."),
    STARTING_UP("Включение SIP..."),
    SHUTTING_DOWN("Выключение SIP..."),
    RESTARTING("Перезапуск SIP...")
}
