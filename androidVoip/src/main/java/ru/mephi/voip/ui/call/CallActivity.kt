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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.core.view.WindowCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import coil.transform.BlurTransformation
import coil.transform.RoundedCornersTransformation
import coil.util.CoilUtils
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.abtollc.sdk.*
import org.abtollc.sdk.OnCallHeldListener.HoldState
import org.abtollc.sdk.OnCallHeldListener.HoldState.*
import org.abtollc.sdk.OnInitializeListener.InitializeState
import org.koin.android.ext.android.inject
import ru.mephi.shared.data.model.CallStatus
import ru.mephi.shared.data.network.KtorClientBuilder
import ru.mephi.voip.R
import ru.mephi.voip.call.parseRemoteContact
import ru.mephi.voip.utils.toast
import timber.log.Timber


class CallActivity : AppCompatActivity(), LifecycleOwner,
    OnCallConnectedListener, OnInitializeListener,
    OnRemoteAlertingListener, OnCallDisconnectedListener,
    OnCallHeldListener, OnCallErrorListener {

    companion object {
        fun create(context: Context, name: String, isIncoming: Boolean) {
            val intent = Intent(context, CallActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(AbtoPhone.IS_INCOMING, isIncoming)
            intent.putExtra(AbtoPhone.REMOTE_CONTACT, name)
            context.startActivity(intent)
        }
    }

    private val viewModel: CallViewModel by inject()

    private lateinit var mPowerManager: PowerManager
    private lateinit var mWakeLock: WakeLock

    private var bIsIncoming: Boolean = false

    private var callStatus: CallStatus? = null

    private lateinit var phone: AbtoPhone
    private var activeCallId = AbtoPhone.INVALID_CALL_ID

    private lateinit var number: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWakeLocks()

        setContent {
            CallScreen()
        }

        initPhoneListeners()

        lifecycleScope.launch {
            viewModel.isMicMuted.collect {
                phone.setMicrophoneMute(it)
            }
        }

        lifecycleScope.launch {
            viewModel.isSpeakerModeEnabled.collect {
                phone.setSpeakerphoneOn(it)
            }
        }

        lifecycleScope.launch {
            viewModel.isBluetoothEnabled.collect {
                phone.setBluetoothOn(it)
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        // Проверка - в каком режиме будет запущена активность - входящий или исходящий звонок
        bIsIncoming = intent.getBooleanExtra(AbtoPhone.IS_INCOMING, false)
        val startedFromService = intent.getBooleanExtra(AbtoPhone.ABTO_SERVICE_MARKER, false)

        if (bIsIncoming) AbtoCallEventsReceiver.cancelIncCallNotification(this, activeCallId)
        activeCallId = intent.getIntExtra(AbtoPhone.CALL_ID, AbtoPhone.INVALID_CALL_ID)

        if (startedFromService) {
            phone.initialize(true)
            phone.setInitializeListener(this)
        } else
            answerCallByIntent()

        viewModel.checkTotalTime()

//        name = parseRemoteContact(intent.getStringExtra(AbtoPhone.REMOTE_CONTACT)!!).first
        number = parseRemoteContact(intent.getStringExtra(AbtoPhone.REMOTE_CONTACT)!!).second

//        viewModel.retrieveInfoAboutCall(sipNumber = number, callStatus = CallStatus.INCOMING)

        viewModel.changeButtonState(CallButtonsState.INCOMING_CALL)

        // Если мы совершаем исходящий
        if (!bIsIncoming)
            startOutgoingCallByIntent()
    }

    private fun getImageUrl() = KtorClientBuilder.PHOTO_REQUEST_URL_BY_PHONE + number

    private val buttonSize = 100.dp
    private val iconSize = 30.dp


    @OptIn(ExperimentalCoilApi::class)
    @Composable
    private fun CallScreen() {
        val timeState = viewModel.displayTime.value
        val holdState = viewModel.callHoldState.value
        val callerName = viewModel.callerName.value
        val callerAppointment = viewModel.callerAppointment.value
        val callerUnit = viewModel.callerUnit.value

        // https://medium.com/@lbenevento/handling-statusbar-colors-when-using-modalbottomsheets-in-jetpack-compose-181ece86cbcc
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(Color.Transparent)

        val gradient = listOf(
            Color(0xFFFFFF),
            Color(0x4A03FFC5),
            Color(0xC900BCD4),
            Color(0xD5124DE4),
            Color(0xF35B1BF0),
        )

        val imageLoader = ImageLoader.Builder(applicationContext)
            .okHttpClient {
                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(applicationContext))
                    .build()
            }.build()

        val request = ImageRequest.Builder(applicationContext)
            .data(getImageUrl())
            .build()

        imageLoader.enqueue(request)

        val blurPainter = rememberImagePainter(
            data = getImageUrl(),
            imageLoader = imageLoader,
            builder = {
                crossfade(200)
                transformations(BlurTransformation(applicationContext, 5f))
            }
        )

        val callerPhotoPainter = rememberImagePainter(
            data = getImageUrl(),
            imageLoader = imageLoader,
            builder = {
                crossfade(200)
                transformations(
                    RoundedCornersTransformation(
                        10f, 10f, 10f, 10f
                    )
                )
            }
        )

        val constraintSet = ConstraintSet {
            val mainImage = createRefFor("mainImage")
            val blurredImage = createRefFor("blurredImage")
            val topRow = createRefFor("topRow")
            val options = createRefFor("options")
            val info = createRefFor("info")

            val guildLineMiddle = createGuidelineFromTop(0.6f)

            constrain(blurredImage) {
                end.linkTo(parent.end)
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(guildLineMiddle)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            }

            constrain(mainImage) {
                top.linkTo(parent.top)
                bottom.linkTo(guildLineMiddle)
                end.linkTo(parent.end)
                start.linkTo(parent.start)
            }

            constrain(topRow) {
                top.linkTo(parent.top)
            }

            constrain(info) {
                bottom.linkTo(guildLineMiddle)
                height = Dimension.fillToConstraints
            }

            constrain(options) {
                top.linkTo(guildLineMiddle)
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
                start.linkTo(parent.start)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            }
        }

        ConstraintLayout(
            constraintSet, modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = blurPainter,
                contentDescription = "",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .layoutId("blurredImage")
            )

            Image(
                painter = callerPhotoPainter,
                contentDescription = "",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .size(300.dp)
                    .layoutId("mainImage")
            )

            Column(
                horizontalAlignment = Alignment.Start, modifier = Modifier
                    .background(brush = Brush.verticalGradient(gradient))
                    .padding(20.dp)
                    .fillMaxWidth()
                    .layoutId("info")
            ) {
                Text(
                    text = callerName, textAlign = TextAlign.Left,
                    fontSize = 20.sp, fontWeight = FontWeight.Medium
                )
                callerAppointment?.let { appointment ->
                    Text(
                        text = appointment.replaceFirstChar(Char::titlecase),
                        textAlign = TextAlign.Left,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = callerUnit, textAlign = TextAlign.Left,
                    fontSize = 20.sp, fontWeight = FontWeight.Medium
                )

                holdState.status?.let {
                    Text(text = it, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            ButtonsRow(
                Modifier
                    .fillMaxWidth()
                    .layoutId("options")
            )

            val constraintSetTopBar = ConstraintSet {
                val back = createRefFor("back")
                val num = createRefFor("number")
                val time = createRefFor("time")

                createHorizontalChain(back, num, time, chainStyle = ChainStyle.SpreadInside)

                constrain(back) {
                    centerHorizontallyTo(parent)
                    width = Dimension.wrapContent
                    height = Dimension.fillToConstraints
                }

                constrain(num) {
                    centerHorizontallyTo(parent)
                    width = Dimension.wrapContent
                    height = Dimension.fillToConstraints
                }

                constrain(time) {
                    centerHorizontallyTo(parent)
                    width = Dimension.wrapContent
                    height = Dimension.fillToConstraints
                }
            }

            ConstraintLayout(
                constraintSetTopBar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 33.dp)
                    .layoutId("topRow"),
            ) {
                IconButton(
                    modifier = Modifier
                        .padding(16.dp, 0.dp, 0.dp, 8.dp)
                        .background(
                            colorResource(id = R.color.colorPrimary),
                            CircleShape
                        )
                        .layoutId("back"),
                    onClick = { /* TODO */ }
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "back",
                        tint = colorResource(id = R.color.colorBlack),
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            colorResource(id = R.color.colorPrimary),
                            CircleShape
                        )
                        .padding(20.dp, 10.dp)
                        .layoutId("number")
                ) {
                    Text(
                        text = number, textAlign = TextAlign.Left,
                        fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                }

                if (timeState.isNotBlank())
                    Box(
                        modifier = Modifier
                            .padding(0.dp, 0.dp, 16.dp, 0.dp)
                            .background(
                                colorResource(id = R.color.colorPrimary),
                                CircleShape
                            )
                            .padding(20.dp, 10.dp)
                            .layoutId("time")
                    ) {
                        Text(
                            text = timeState,
                            fontSize = 20.sp, fontWeight = FontWeight.Bold
                        )
                    }
            }
        }
    }

    @Composable
    private fun ButtonsRow(modifier: Modifier) {
        val micMuteState = viewModel.isMicMuted.collectAsState()
        val volumeState = viewModel.isSpeakerModeEnabled.collectAsState()
        val isBluetoothEnabled = viewModel.isBluetoothEnabled.collectAsState()

        val buttonsState = viewModel.buttonsState.value
        val holdState = viewModel.callHoldState.value

        Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Если входящий, показать кнопку ответа на вызов
                if (buttonsState == CallButtonsState.INCOMING_CALL) {
                    Button(
                        onClick = { pickUp() },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(buttonSize)
                            .clickable { },
                        colors = ButtonDefaults.buttonColors(
                            colorResource(id = R.color.colorGreen)
                        ),
                        elevation = ButtonDefaults.elevation(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_dialer_sip_24_white),
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }

                // Если звонок в процессе, показать управление
                if (buttonsState == CallButtonsState.CALL_PROCESS) {
                    Button(
                        onClick = { viewModel.changeSound() },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(buttonSize)
                            .clickable { },
                        colors = ButtonDefaults.buttonColors(
                            colorResource(
                                id = if (volumeState.value) R.color.colorPrimaryDark
                                else R.color.colorGray
                            )
                        ),
                        elevation = ButtonDefaults.elevation(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_volume_up_24),
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    Button(
                        onClick = { viewModel.micMute() },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(buttonSize)
                            .clickable { },
                        colors = ButtonDefaults.buttonColors(
                            colorResource(
                                id = if (micMuteState.value) R.color.colorPrimaryDark
                                else R.color.colorGray
                            )
                        ),
                        elevation = ButtonDefaults.elevation(),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Icon(
                            painter = painterResource(
                                R.drawable.ic_baseline_mic_off_24
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }

                Button(
                    onClick = { hangUP() },
                    modifier = Modifier
                        .padding(8.dp)
                        .size(buttonSize)
                        .clickable { },
                    colors = ButtonDefaults.buttonColors(
                        colorResource(id = R.color.colorRed)
                    ),
                    elevation = ButtonDefaults.elevation(),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_call_end_24),
                        contentDescription = null,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }

            if (buttonsState == CallButtonsState.CALL_PROCESS)
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { holdCall() },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(buttonSize)
                            .clickable { },
                        colors = ButtonDefaults.buttonColors(
                            colorResource(
                                id = if (holdState == ru.mephi.voip.ui.call.HoldState.LOCAL_HOLD)
                                    R.color.colorAccent
                                else R.color.colorGray
                            )
                        ),
                        elevation = ButtonDefaults.elevation(),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Icon(
                            painter = painterResource(
                                R.drawable.ic_baseline_phone_paused_24
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                    }

                    Button(
                        onClick = { viewModel.changeBluetooth() },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(buttonSize)
                            .clickable { },
                        colors = ButtonDefaults.buttonColors(
                            colorResource(
                                id = if (isBluetoothEnabled.value)
                                    R.color.colorPrimaryDark
                                else R.color.colorGray
                            )
                        ),
                        elevation = ButtonDefaults.elevation(),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Icon(
                            painter = painterResource(
                                R.drawable.ic_baseline_phone_bluetooth_speaker_24
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                    }

                    Button(
                        onClick = { toast("Будет добавлено в ближайшем будущем") },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(buttonSize)
                            .clickable { },
                        colors = ButtonDefaults.buttonColors(
                            colorResource(
                                id = if (holdState == ru.mephi.voip.ui.call.HoldState.LOCAL_HOLD)
                                    R.color.colorAccent
                                else R.color.colorGray
                            )
                        ),
                        elevation = ButtonDefaults.elevation(),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Icon(
                            painter = painterResource(
                                R.drawable.ic_baseline_dialpad_24
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
        }
    }

    private fun initPhoneListeners() {
        phone = (application as AbtoApplication).abtoPhone
        phone.setCallConnectedListener(this)
        phone.setCallDisconnectedListener(this)
        phone.setOnCallHeldListener(this)
        phone.setRemoteAlertingListener(this)
    }

    override fun onBackPressed() {}

    private fun hangUP() {
        if (viewModel.callState.value != CallState.CONNECTED) {
            callStatus = CallStatus.DECLINED_FROM_YOU
        }
        try {
            if (bIsIncoming) {
                phone.rejectCall(activeCallId)
            } else phone.hangUp(activeCallId)
        } finally {
            stopActivity()
        }
    }

    private fun stopActivity() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        deactivateSensor()
        finish()
    }

    override fun onCallConnected(callId: Int, remoteContact: String) {
        viewModel.onCallConnected(number)
    }

    override fun onRemoteAlerting(callId: Int, statusCode: Int, accId: Long) {
        if (activeCallId == AbtoPhone.INVALID_CALL_ID) activeCallId = callId

        when (statusCode) {
            OnRemoteAlertingListener.TRYING -> viewModel.changeCallState(CallState.TRYING)
            OnRemoteAlertingListener.RINGING -> viewModel.changeCallState(CallState.RINGING)
            OnRemoteAlertingListener.SESSION_PROGRESS -> viewModel.changeCallState(CallState.SESSION_PROGRESS)
        }
    }

    override fun onCallDisconnected(
        callId: Int,
        remoteContact: String?,
        statusCode: Int,
        statusMessage: String?
    ) {
        when (statusCode) {
            200 -> viewModel.changeCallState(CallState.NORMAL_CALL_CLEARING)
            487 -> viewModel.changeCallState(CallState.REQUEST_TERMINATED)
            486 -> viewModel.changeCallState(CallState.BUSY_HERE)
        }

        viewModel.changeButtonState(CallButtonsState.CALL_ENDED)

        callStatus?.let { status ->
            if (status != CallStatus.DECLINED_FROM_YOU) {
                if (viewModel.callState.value == CallState.REQUEST_TERMINATED)
                    callStatus = CallStatus.MISSED

                if (viewModel.callState.value == CallState.NORMAL_CALL_CLEARING) {
                    if (viewModel.mTotalTime < 2000)
                        callStatus = CallStatus.DECLINED_FROM_SIDE
                }

                if (viewModel.callState.value == CallState.BUSY_HERE)
                    callStatus = CallStatus.DECLINED_FROM_YOU
            }

            viewModel.saveInfoAboutCall(number, callStatus!!)
        }

        Timber.d("call status code: $statusCode | msg: $statusMessage")
        stopActivity()
    }

    override fun onCallHeld(callId: Int, state: HoldState) {
        when (state) {
            LOCAL_HOLD -> viewModel.changeHoldState(ru.mephi.voip.ui.call.HoldState.LOCAL_HOLD)
            REMOTE_HOLD -> viewModel.changeHoldState(ru.mephi.voip.ui.call.HoldState.REMOTE_HOLD)
            ACTIVE -> viewModel.changeHoldState(ru.mephi.voip.ui.call.HoldState.ACTIVE)
            ERROR -> viewModel.changeHoldState(ru.mephi.voip.ui.call.HoldState.ERROR)
        }
    }

    override fun onCallError(remoteContact: String?, statusCode: Int, message: String?) {
        Toast.makeText(this, "onCallError: $statusCode", Toast.LENGTH_SHORT).show()
    }

    override fun onInitializeState(state: InitializeState?, message: String?) {
        if (state == InitializeState.SUCCESS) {
            phone.setInitializeListener(null)
            viewModel.retrieveInfoAboutCall(number)
            answerCallByIntent()
        }
    }

    private fun answerCallByIntent() {
        if (intent.getBooleanExtra(AbtoCallEventsReceiver.KEY_PICK_UP_AUDIO, false))
            pickUp()
    }

    private fun pickUp() {
        try {
            viewModel.changeButtonState(CallButtonsState.CALL_PROCESS)
            phone.answerCall(activeCallId, CallState.CONNECTED.statusCode, false)
            callStatus = CallStatus.INCOMING
        } catch (e: RemoteException) {
        }
    }

    private fun holdCall() {
        phone.holdRetriveCall(activeCallId)
    }

    private fun startOutgoingCallByIntent() {
        val sipNumber = intent.getStringExtra(AbtoPhone.REMOTE_CONTACT)!!

        viewModel.changeCallState(CallState.CALL_OUTGOING)
        viewModel.changeButtonState(CallButtonsState.OUTGOING_CALL)

        callStatus = CallStatus.OUTCOMING

        try {
            activeCallId =
                phone.startCall(sipNumber, phone.currentAccountId)
        } catch (e: RemoteException) {
            activeCallId = -1
            e.printStackTrace()
        }

        // Verify returned callId.
        // End this activity when call can't be started.
        if (activeCallId == -1) {
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