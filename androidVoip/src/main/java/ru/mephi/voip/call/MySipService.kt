package ru.mephi.voip.call

class MySipService {
//    private val _status = MutableStateFlow(AccountStatus.UNREGISTERED)
//    val status: StateFlow<AccountStatus> = _status
//
//    private var _accountsCount = MutableStateFlow(0)
//    val accountsCount: StateFlow<Int> = _accountsCount
//
//    fun fetchStatus(newStatus: AccountStatus? = null) {
//        CoroutineScope(Dispatchers.Main).launch {
//            _status.emit(AccountStatus.LOADING)
//
//            status.replayCache.lastOrNull()?.let { lastStatus ->
//                Timber.d("Switching Status from: \"${lastStatus}\" to \"${newStatus?.status}\"")
//            }
//            if (newStatus == null && abtoPhone.getSipProfileState(abtoPhone.currentAccountId)?.statusCode == 200) {
//                // На случай, если активность была удалена, а AbtoApp активен и
//                // statusCode аккаунт = 200 (зарегистрирован). Вызывается при отрисовке фрагмента
//                _status.emit(AccountStatus.REGISTERED)
//                fetchName()
//            } else if (newStatus != null)
//                _status.emit(newStatus)
//        }
//
//        val list = getAccountsList()
//        _accountsCount.value = list.size
//
//        if (newStatus == AccountStatus.REGISTERED)
//            fetchName()
//        else if (newStatus == AccountStatus.UNREGISTERED)
//            _displayName.value = null
//    }

}
