package ru.mephi.voip.eventbus

import ru.mephi.shared.data.sip.AccountStatus

sealed class Event {
    class EnableAccount
    class DisableAccount
    class ChangeAccountStatus(val accountStatus: AccountStatus)
}