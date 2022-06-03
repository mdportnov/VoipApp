package ru.mephi.voip.data

enum class SavingResult(val text: String) {
    ALREADY_SAVED("Контакт уже сохранён в избранном"),
    EMPTY_LINE("Невозможно сохранить - отсутствует sip-номер"),
    SUCCESS("Успешно сохранено")
}
