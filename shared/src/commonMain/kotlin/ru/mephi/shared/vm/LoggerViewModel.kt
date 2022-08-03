package ru.mephi.shared.vm

import kotlinx.coroutines.flow.MutableStateFlow

class LoggerViewModel {

    val log = MutableStateFlow(LogMessage(LogType.VERBOSE, ""))

    fun v(msg: String) { log(LogType.VERBOSE, msg) }

    fun d(msg: String) { log(LogType.DEBUG, msg) }

    fun i(msg: String) { log(LogType.INFO, msg) }

    fun w(msg: String) { log(LogType.WARNING, msg) }

    fun e(msg: String) { log(LogType.ERROR, msg) }

    fun log(
        logType: LogType,
        logMessage: String
    ) {
        log.value = LogMessage(logType, logMessage)
    }

}

data class LogMessage(
    val logType: LogType,
    val logMessage: String
)

enum class LogType {
    VERBOSE, DEBUG, INFO, WARNING, ERROR
}