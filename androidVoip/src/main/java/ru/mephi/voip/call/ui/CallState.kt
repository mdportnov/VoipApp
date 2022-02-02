package ru.mephi.voip.call.ui

enum class CallButtonsState {
    OUTGOING_CALL, INCOMING_CALL, CALL_PROCESS, CALL_ENDED
}

enum class CallState(val status: String, val statusCode: Int) {
    NONE("Нет звонка", 0),
    CALL_INCOMING("Входящий звонок проинициализирован", 0),
    CALL_OUTGOING("Исходящий звонок", 0),
    CONNECTED("Соединено", 200),
    TRYING("...", 100),
    RINGING("Звонок", 180),
    SESSION_PROGRESS("Сессия в процессе", 183),
    NORMAL_CALL_CLEARING("Обычный сброс вызова", 200), // когда сбросил собеседник при исходящем от тебя вызове
    REQUEST_TERMINATED("Запрос прекращен", 487), // когда при входящем тебе сбросил собеседник или когда ты сбросил при своем неотвеченном исходящем
    BUSY_HERE("Запрос прекращен", 486), // когда занято у нас
}

enum class HoldState(val status: String?) {
    NONE(null),
    ACTIVE(""),
    LOCAL_HOLD("Звонок на удержании"),
    REMOTE_HOLD("Собеседник установил звонок на удержание"),
    ERROR("Ошибка")
}