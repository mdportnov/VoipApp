package ru.mephi.shared.vm

import kotlinx.coroutines.flow.MutableStateFlow


class UserNotifierViewModel {

    val notifyMsg = MutableStateFlow("")

    fun notifyUser(msg: String) {
        notifyMsg.value = msg
    }

}