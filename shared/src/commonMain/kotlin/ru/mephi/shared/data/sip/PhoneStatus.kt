package ru.mephi.shared.data.sip

enum class PhoneStatus(val status: String) {
    STARTING_UP("включение SIP..."),
    NO_CONNECTION("сеть недоступна"),
    SHUTTING_DOWN("выключение SIP..."),
    RESTARTING("перезапуск SIP..."),
    CONNECTING("подключение..."),
    UNREGISTERED("SIP выключен"),
    REGISTRATION_FAILED("ошибка регистрации"),
    REGISTERED("аккаунт активен"),
    RECONNECTING("переподключение..."),
}
