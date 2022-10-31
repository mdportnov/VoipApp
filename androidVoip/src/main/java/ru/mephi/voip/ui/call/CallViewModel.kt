package ru.mephi.voip.ui.call

import android.content.Context
import android.os.Handler
import android.provider.MediaStore.Audio
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.abtollc.sdk.AbtoPhone
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.base.MainIoExecutor
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.model.CallStatus
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.repo.CallsRepository
import ru.mephi.shared.data.repo.VoIPServiceRepository
import ru.mephi.voip.data.CatalogRepository
import ru.mephi.voip.utils.AudioUtils
import ru.mephi.voip.utils.BluetoothReceiver

@Suppress("DEPRECATION")
class CallViewModel(
    private val callsRepository: CallsRepository,
) : MainIoExecutor(), KoinComponent {
    private val audioUtils by inject<AudioUtils>()
    private val bluetoothReceiver by inject<BluetoothReceiver>()

    private val vsRepo: VoIPServiceRepository by inject()
    var activeCallId = AbtoPhone.INVALID_CALL_ID
    var number: String = ""
    private var _isNumPadVisible = mutableStateOf(false)
    val isNumPadVisible: State<Boolean> get() = _isNumPadVisible

    var isDeclinedFromMySide = false

    private var _inputState = mutableStateOf("")
    val inputState: State<String> get() = _inputState

    fun changeInputState(newInput: String) {
        _inputState.value = newInput
    }

    fun toggleNumPad() {
        _isNumPadVisible.value = !_isNumPadVisible.value
    }

    private var _isStatusBarShowed = MutableStateFlow(false)
    val isStatusBarShowed: StateFlow<Boolean> get() = _isStatusBarShowed

    private fun hideStatusBar() {
        _isStatusBarShowed.value = false
    }

    fun showStatusBar() {
        _isStatusBarShowed.value = true
    }

    private var _callStatus = MutableStateFlow(CallStatus.NONE)
    val callStatus: StateFlow<CallStatus> get() = _callStatus

    private var _callState = MutableStateFlow(CallState.NONE)
    val callState: StateFlow<CallState> get() = _callState

    private var _displayTime = mutableStateOf("")
    val displayTime: State<String> get() = _displayTime

    private var _buttonsState = mutableStateOf(CallButtonsState.OUTGOING_CALL)
    val buttonsState: State<CallButtonsState> get() = _buttonsState

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

    private var _callerAppointment = mutableStateOf("")
    val callerAppointment: State<String?> get() = _callerAppointment

    private var _callerUnit = mutableStateOf("")
    val callerUnit: State<String> get() = _callerUnit

    private var mPointTime: Long = 0
    var mTotalTime: Long = 0
    private val mHandler = Handler()

    var comradeAppointment = MutableStateFlow(Appointment())
    var dialPadText = MutableStateFlow("")
    val isBluetoothReady = MutableStateFlow(false)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            isBluetoothReady.collect {
                if (!it && _isBluetoothEnabled.value) {
                    setBluetoothMode()
                }
            }
        }
    }

    fun appendDialPadText(text: String) {
        if (text.toInt() in 0..9) {
            dialPadText.value = dialPadText.value + text
        }
    }

    fun backspaceDialPadText() {
        dialPadText.value = dialPadText.value.dropLast(1)
    }

    fun clearDialPadText() {
        dialPadText.value = ""
    }

    private val mUpdateTimeTask: Runnable = object : Runnable {
        override fun run() {
            mTotalTime += System.currentTimeMillis() - mPointTime
            mPointTime = System.currentTimeMillis()
            var seconds = (mTotalTime / 1000).toInt()
            val minutes = seconds / 60
            seconds %= 60
            _displayTime.value = if (seconds < 10)
                "$minutes:0$seconds"
            else
                "$minutes:$seconds"
            mHandler.postDelayed(this, 1000)
        }
    }

    fun stopCall() {
        hideStatusBar()
        _callerUnit.value = ""
        _callerAppointment.value = ""
        _callerName.value = ""
        isDeclinedFromMySide = false
        audioUtils.setDefaultAudioMode()
        _isBluetoothEnabled.value = false
        _isSpeakerModeEnabled.value = false
        if (_isMicMuted.value) micMute()
        isBluetoothReady.value = false
        bluetoothReceiver.disable()
    }

    fun changeCallStatus(callStatus: CallStatus) {
        _callStatus.value = callStatus
    }

    fun saveInfoAboutCall(sipNumber: String) {
        stopTimer()
        addRecord(
            CallRecord(
                sipNumber = sipNumber,
                sipName =
                _callerName.value.ifEmpty { sipNumber },
                status = _callStatus.value,
                time = Clock.System.now().epochSeconds,
                duration = mTotalTime
            )
        )
        mTotalTime = 0
        changeCallStatus(CallStatus.NONE)
    }

    private var fetchNameJob: Job? = null

    /**
     * Retrieve info about call
     *
     * @param sipNumber
     * @param isIncoming
     */
    fun retrieveInfoAboutCall(sipNumber: String) {
        fetchNameJob?.cancel()

        launch(ioDispatcher) {
            fetchNameJob = vsRepo.getUserByPhone(sipNumber).onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let {
                                comradeAppointment.value = it
                                _callerAppointment.value = it.appointment
                                _callerName.value = it.fio
                            }
                        }
                        is Resource.Error -> {
//                            _callerName.value = "..."
                        }
                        is Resource.Loading -> {
//                            _callerName.value = "..."
                        }
                    }
                }.launchIn(this)
        }
    }

    private fun addRecord(callRecord: CallRecord) {
        callsRepository.addRecord(callRecord)
    }

    private fun startTimer() {
        stopTimer()
        mPointTime = System.currentTimeMillis()
        mHandler.postDelayed(mUpdateTimeTask, 100)
    }

    fun stopTimer() {
        _displayTime.value = ""
        mHandler.removeCallbacks(mUpdateTimeTask)
    }

    /**
     * On call connected
     *
     */
    fun onCallConnected(number: String) {
        this.number = number
        _callState.value = CallState.OK
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

    fun setBluetoothMode() {
        _isBluetoothEnabled.value = audioUtils.setBluetoothMode(!_isBluetoothEnabled.value)
        _isSpeakerModeEnabled.value = false
    }

    fun setSpeakerMode() {
        _isSpeakerModeEnabled.value = audioUtils.setSpeakerMode(!_isSpeakerModeEnabled.value)
        _isBluetoothEnabled.value = false
    }
}