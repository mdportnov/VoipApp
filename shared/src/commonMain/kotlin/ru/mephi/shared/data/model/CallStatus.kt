package ru.mephi.shared.data.model

enum class CallStatus(val text: String) {
    NONE("Ошибка"), INCOMING("Входящий"), OUTCOMING("Исходящий"),
    MISSED("Пропущенный"), DECLINED_FROM_YOU("Отклонённый"), DECLINED_FROM_SIDE("Отклонённый")
}