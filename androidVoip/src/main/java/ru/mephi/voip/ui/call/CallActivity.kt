package ru.mephi.voip.ui.call

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.RemoteException
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.annotation.ExperimentalCoilApi
import kotlinx.coroutines.launch
import org.abtollc.sdk.*
import org.abtollc.sdk.OnCallHeldListener.HoldState
import org.abtollc.sdk.OnCallHeldListener.HoldState.*
import org.abtollc.sdk.OnInitializeListener.InitializeState
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.utils.appContext
import ru.mephi.shared.data.model.CallStatus
import ru.mephi.voip.R
import ru.mephi.voip.abto.parseRemoteContact
import ru.mephi.voip.data.PhoneManager
import ru.mephi.voip.ui.theme.MasterTheme
import ru.mephi.voip.utils.toast
import timber.log.Timber

class CallActivity : AppCompatActivity(), LifecycleOwner,
    OnCallConnectedListener, OnInitializeListener,
    OnRemoteAlertingListener,
    OnCallHeldListener, OnCallErrorListener {

    companion object {
        fun create(context: Context, name: String, isIncoming: Boolean) {
            val intent = Intent(context, CallActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(AbtoPhone.IS_INCOMING, isIncoming)
            intent.putExtra(AbtoPhone.REMOTE_CONTACT, name)
            intent.putExtra(context.getString(R.string.isCallStillGoingOn), false)
            context.startActivity(intent)
        }

        fun open(context: Context) {
            val intent = Intent(context, CallActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(context.getString(R.string.isCallStillGoingOn), true)
            context.startActivity(intent)
        }
    }

    private val callViewModel: CallViewModel by inject()

    private lateinit var mPowerManager: PowerManager
    private lateinit var mWakeLock: WakeLock

    private var bIsIncoming: Boolean = false
    private var isCallStillGoingOn: Boolean = false

    private lateinit var phone: AbtoPhone

    @OptIn(ExperimentalCoilApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWakeLocks()
        isCallStillGoingOn =
            intent.getBooleanExtra(appContext.getString(R.string.isCallStillGoingOn), false)

        if (!isCallStillGoingOn) {
            callViewModel.changeButtonState(CallButtonsState.INCOMING_CALL)
            callViewModel.changeCallStatus(CallStatus.NONE)
            parseRemoteContact(intent.getStringExtra(AbtoPhone.REMOTE_CONTACT)!!).second.let {
                callViewModel.number = it
                callViewModel.comradeAppointment.value = Appointment(line = it, lineShown = it)
            }
            callViewModel.number =
                parseRemoteContact(intent.getStringExtra(AbtoPhone.REMOTE_CONTACT)!!).second
        } else
            callViewModel.changeButtonState(CallButtonsState.CALL_PROCESS)

        setContent {
            MasterTheme {
//                CallScreen(
//                    callViewModel,
//                    ::pickUp,
//                    ::holdCall,
//                    ::hangUp,
//                    ::stopActivity,
//                    ::transferCall
//                )
                CallScreenNew(
                    pickUp = ::pickUp,
                    holdCall = {
                        phone.holdRetriveCall(callViewModel.activeCallId)
                    },
                    hangUp = ::hangUp,
                    stopActivity = ::stopActivity,
                    transferCall = ::transferCall
                )
            }
        }

        callViewModel.retrieveInfoAboutCall(callViewModel.number)

        callViewModel.showStatusBar()

        initPhoneListeners()
        initPhoneStates()

//        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        // Проверка - в каком режиме будет запущена активность - входящий или исходящий звонок
        bIsIncoming = intent.getBooleanExtra(AbtoPhone.IS_INCOMING, false)
        val startedFromService = intent.getBooleanExtra(AbtoPhone.ABTO_SERVICE_MARKER, false)

        if (bIsIncoming) AbtoCallEventsReceiver.cancelIncCallNotification(
            this, callViewModel.activeCallId
        )

        if (!isCallStillGoingOn)
            callViewModel.activeCallId =
                intent.getIntExtra(AbtoPhone.CALL_ID, AbtoPhone.INVALID_CALL_ID)

        if (startedFromService) {
            phone.initialize(true)
            phone.setInitializeListener(this)
        } else
            answerCallByIntent()

        // Если мы совершаем исходящий
        if (!bIsIncoming && !isCallStillGoingOn)
            startOutgoingCallByIntent()
    }

    private fun initPhoneStates() {
        lifecycleScope.launch {
            callViewModel.isMicMuted.collect {
                phone.setMicrophoneMute(it)
            }
        }

        lifecycleScope.launch {
            callViewModel.isSpeakerModeEnabled.collect {
                phone.setSpeakerphoneOn(it)
            }
        }

        lifecycleScope.launch {
            callViewModel.isBluetoothEnabled.collect {
                phone.setBluetoothOn(it)
            }
        }

        lifecycleScope.launch {
            callViewModel.callStatus.collect {
                if (it == CallStatus.DECLINED_FROM_YOU ||
                    it == CallStatus.DECLINED_FROM_SIDE ||
                    it == CallStatus.MISSED
                ) {
                    stopActivity()
                }
            }
        }
    }

    private fun initPhoneListeners() {
        phone = (application as AbtoApplication).abtoPhone
        phone.setCallConnectedListener(this)
        phone.setOnCallHeldListener(this)
        phone.setRemoteAlertingListener(this)
    }

    override fun onBackPressed() {}

    private fun hangUp() {
        callViewModel.isDeclinedFromMySide = true
        if (callViewModel.callState.value != CallState.OK) {
            callViewModel.changeCallStatus(CallStatus.DECLINED_FROM_YOU)
        }
        try {
            if (bIsIncoming) {
                if (callViewModel.mTotalTime > 1000) {
                    callViewModel.changeCallStatus(CallStatus.INCOMING)
                } else {
                    callViewModel.changeCallState(CallState.CALL_OUTGOING)
                    callViewModel.changeCallStatus(CallStatus.DECLINED_FROM_YOU)
                }

                phone.rejectCall(callViewModel.activeCallId)
            } else {
                callViewModel.changeCallStatus(CallStatus.OUTCOMING)
                phone.hangUp(callViewModel.activeCallId)
            }
        } finally {
            stopActivity()
        }
    }

    private fun transferCall() {
        toast("Call transferring")
        phone.callXfer(callViewModel.activeCallId, callViewModel.dialPadText.value)
    }

    private fun stopActivity() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        deactivateSensor()
        callViewModel.changeInputState("")
        if (callViewModel.isNumPadVisible.value)
            callViewModel.toggleNumPad()
        finish()
    }

    override fun onCallConnected(callId: Int, remoteContact: String) {
        callViewModel.onCallConnected(callViewModel.number)
    }

    override fun onRemoteAlerting(callId: Int, statusCode: Int, accId: Long) {
        if (callViewModel.activeCallId == AbtoPhone.INVALID_CALL_ID) callViewModel.activeCallId =
            callId

        when (statusCode) {
            OnRemoteAlertingListener.TRYING -> callViewModel.changeCallState(CallState.TRYING)
            OnRemoteAlertingListener.RINGING -> callViewModel.changeCallState(CallState.RINGING)
            OnRemoteAlertingListener.SESSION_PROGRESS -> callViewModel.changeCallState(CallState.SESSION_PROGRESSING)
        }
    }

    override fun onCallHeld(callId: Int, state: HoldState) {
        when (state) {
            LOCAL_HOLD -> callViewModel.changeHoldState(ru.mephi.voip.ui.call.HoldState.LOCAL_HOLD)
            REMOTE_HOLD -> callViewModel.changeHoldState(ru.mephi.voip.ui.call.HoldState.REMOTE_HOLD)
            ACTIVE -> callViewModel.changeHoldState(ru.mephi.voip.ui.call.HoldState.ACTIVE)
            ERROR -> callViewModel.changeHoldState(ru.mephi.voip.ui.call.HoldState.ERROR)
            NONE -> callViewModel.changeHoldState(ru.mephi.voip.ui.call.HoldState.NONE)
        }
    }

    override fun onCallError(remoteContact: String?, statusCode: Int, message: String?) {
        Toast.makeText(this, "onCallError: $statusCode", Toast.LENGTH_SHORT).show()
    }

    override fun onInitializeState(state: InitializeState?, message: String?) {
        if (state == InitializeState.SUCCESS) {
            phone.setInitializeListener(null)
            answerCallByIntent()
        }
    }

    private fun answerCallByIntent() {
        if (intent.getBooleanExtra(AbtoCallEventsReceiver.KEY_PICK_UP_AUDIO, false))
            pickUp()
    }

    private fun pickUp() {
        try {
            callViewModel.changeButtonState(CallButtonsState.CALL_PROCESS)
            phone.answerCall(callViewModel.activeCallId, CallState.OK.statusCode, false)
            callViewModel.changeCallStatus(CallStatus.INCOMING)
        } catch (e: RemoteException) {
        }
    }

    private fun holdCall() {
        phone.holdRetriveCall(callViewModel.activeCallId)
    }

    private fun startOutgoingCallByIntent() {
        val phoneManager: PhoneManager by inject()
        val sipNumber = intent.getStringExtra(AbtoPhone.REMOTE_CONTACT)!!
        callViewModel.changeCallStatus(CallStatus.OUTCOMING)
        callViewModel.changeCallState(CallState.CALL_OUTGOING)
        callViewModel.changeButtonState(CallButtonsState.OUTGOING_CALL)

        try {
            callViewModel.activeCallId =
                phone.startCall(sipNumber, phoneManager.accId)
        } catch (e: RemoteException) {
            callViewModel.activeCallId = -1
            e.printStackTrace()
        }

        // Verify returned callId.
        // End this activity when call can't be started.
        if (callViewModel.activeCallId == -1) {
            toast("Не получается позвонить контакту: $sipNumber")
            stopActivity()
        }
    }

    private fun initWakeLocks() {
        mPowerManager = getSystemService(POWER_SERVICE) as PowerManager
        mWakeLock = mPowerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "ru.mephi.ru.mephi.voip:wakelogtag"
        )
        activateSensor()
    }

    private fun activateSensor() {
        if (!mWakeLock.isHeld)
            mWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
    }

    private fun deactivateSensor() {
        if (mWakeLock.isHeld)
            mWakeLock.release()
    }
}