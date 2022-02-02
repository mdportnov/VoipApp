package ru.mephi.voip.call.ui

import android.app.Application
import android.os.Handler
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.model.CallStatus
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.repository.CallsRepository
import ru.mephi.shared.data.repository.CatalogRepository

class CallViewModel(
    app: Application, private val catalogRepository: CatalogRepository,
    private val callsRepository: CallsRepository,
) : AndroidViewModel(app) {

    private var fetchNameJob: Job? = null

    private var _displayTime = mutableStateOf("")
    val displayTime: State<String> get() = _displayTime

    private var _buttonsState = mutableStateOf(CallButtonsState.OUTGOING_CALL)
    val buttonsState: State<CallButtonsState> get() = _buttonsState

    private var _callState = mutableStateOf(CallState.NONE)
    val callState: State<CallState> get() = _callState

    private var _callHoldState = mutableStateOf(HoldState.NONE)
    val callHoldState: State<HoldState> get() = _callHoldState

    private var _isBluetoothEnabled = MutableStateFlow(false)
    val isBluetoothEnabled: StateFlow<Boolean> get() = _isBluetoothEnabled

    private var _isMicMuted = MutableStateFlow(false)
    val isMicMuted: StateFlow<Boolean> get() = _isMicMuted

    private var _isSpeakerModeEnabled = MutableStateFlow(false)
    val isSpeakerModeEnabled: StateFlow<Boolean> get() = _isSpeakerModeEnabled

    private var _callerName = mutableStateOf("")
    val callerName: State<String> get() = _callerName

    private var _callerAppointment: MutableState<String?> = mutableStateOf("")
    val callerAppointment: State<String?> get() = _callerAppointment

    private var _callerUnit = mutableStateOf("")
    val callerUnit: State<String> get() = _callerUnit

    private var mPointTime: Long = 0
    var mTotalTime: Long = 0
    private val mHandler = Handler()

    private val mUpdateTimeTask: Runnable = object : Runnable {
        override fun run() {
            mTotalTime += System.currentTimeMillis() - mPointTime
            mPointTime = System.currentTimeMillis()
            var seconds = (mTotalTime / 1000).toInt()
            val minutes = seconds / 60
            seconds %= 60
            if (seconds < 10)
                _displayTime.value = "$minutes:0$seconds"
            else
                _displayTime.value = "$minutes:$seconds"
            mHandler.postDelayed(this, 1000)
        }
    }

    fun saveInfoAboutCall(sipNumber: String, callStatus: CallStatus) {
        addRecord(
            CallRecord(
                sipNumber = sipNumber, sipName = _callerName.value,
                status = callStatus, time = Clock.System.now().epochSeconds
            )
        )
    }

    /**
     * Retrieve info about call
     *
     * @param sipNumber
     * @param isIncoming
     */
    fun retrieveInfoAboutCall(sipNumber: String) {
        fetchNameJob?.cancel()

        viewModelScope.launch {
            fetchNameJob = catalogRepository.getInfoByPhone(sipNumber)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let {
                                _callerUnit.value = it.name

                                it.appointment?.let { appointment ->
                                    _callerAppointment.value = appointment
                                }

                                it.display_name.also { name ->
                                    _callerName.value = name
                                }
                            }
                        }
                        is Resource.Error -> {
                            _callerName.value = "..."
                        }
                        is Resource.Loading -> {
                            _callerName.value = "..."
                        }
                    }
                }.launchIn(this)
        }
    }

    private fun addRecord(callRecord: ru.mephi.shared.data.model.CallRecord) {
        callsRepository.addRecord(callRecord)
    }

    /**
     * Check total time
     *
     */
    fun checkTotalTime() {
        if (mTotalTime != 0L) {
            mHandler.removeCallbacks(mUpdateTimeTask)
            mHandler.postDelayed(mUpdateTimeTask, 100)
        }
    }

    private fun startTimer() {
        if (mTotalTime == 0L) {
            mPointTime = System.currentTimeMillis()
            mHandler.removeCallbacks(mUpdateTimeTask)
            mHandler.postDelayed(mUpdateTimeTask, 100)
        }
    }

    /**
     * On call connected
     *
     */
    fun onCallConnected(number: String) {
        _callState.value = CallState.CONNECTED
        _buttonsState.value = CallButtonsState.CALL_PROCESS
        startTimer()
    }

    /**
     * Изменяет состояние звонка
     * @property _callState State, наблюдаемый в Compose-компонентах
     * @param callState значение, получаемое на основании обратных вызовов AbtoSDK
     */
    fun changeCallState(callState: CallState) {
        _callState.value = callState
    }

    /**
     * Изменяет состояние удерживания звонка
     *
     * @param holdState
     */
    fun changeHoldState(holdState: HoldState) {
        _callHoldState.value = holdState
    }

    fun changeButtonState(buttonState: CallButtonsState) {
        _buttonsState.value = buttonState
    }

    /**
     * Изменяет состояние микрофона
     */
    fun micMute() {
        _isMicMuted.value = !_isMicMuted.value
    }

    /**
     * Изменяет состояние разговорного динамика
     */
    fun changeSound() {
        _isSpeakerModeEnabled.value = !_isSpeakerModeEnabled.value
    }

    fun changeBluetooth() {
        _isBluetoothEnabled.value = !_isBluetoothEnabled.value
    }
}