package ru.mephi.voip.ui.call

enum class CallButtonsState {
    OUTGOING_CALL, INCOMING_CALL, CALL_PROCESS, CALL_ENDED
}

enum class CallState(val status: String, val statusCode: Int) {
    NONE("Нет звонка", 0),
    CALL_INCOMING("Входящий звонок проинициализирован", 180),
    CALL_OUTGOING("Исходящий звонок", 180),
    TRYING("...", 100),
    RINGING("Звонок", 180),
    SESSION_PROGRESSING("Сессия в процессе", 183),

    // когда сбросил собеседник при исходящем от тебя вызове
    NORMAL_CALL_CLEARING("Обычный сброс вызова", 200),
    OK("Соединено", 200),
    ACCEPTED("Принято", 202),

    BAD_REQUEST("Неправильный запрос", 400),
    UNAUTHORIZED("Неавторизованный", 401),
    FORBIDDEN("Запрещено", 403),
    NOT_FOUND("Не найдено", 404),
    PROXY_AUTH_REQUIRED("Требуется аутентификация прокси-сервера", 407),
    REQUEST_TIMEOUT("Таймаут запроса", 408),

    // когда занято у нас
    BUSY_HERE("Запрос прекращен", 486),

    // когда при входящем тебе сбросил собеседник или когда ты сбросил при своем неотвеченном исходящем
    REQUEST_TERMINATED("Запрос прекращен", 487),
    TEMPORARY_UNAVAILABLE("Временно недоступен", 480),
    BAD_GATEWAY("Bad Gateway", 502),
    SERVICE_UNAVAILABLE("Сервис недоступен", 503),

}

enum class HoldState(val status: String?) {
    NONE(null),
    ACTIVE(""),
    LOCAL_HOLD("Звонок на удержании"),
    REMOTE_HOLD("Собеседник установил звонок на удержание"),
    ERROR("Ошибка")
}