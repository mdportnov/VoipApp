@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)

package ru.mephi.voip.ui.call


import android.view.HapticFeedbackConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import ru.mephi.voip.R
import ru.mephi.voip.utils.getImageUrl

@Composable
internal fun CallScreenNew(
    pickUp: () -> Unit,
    holdCall: () -> Unit,
    hangUp: () -> Unit,
    stopActivity: () -> Unit,
    transferCall: () -> Unit,
    callVM: CallViewModel = get()
) {
    val state = callVM.buttonsState.value
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    LaunchedEffect(true) {
        callVM.clearDialPadText()
    }
    ModalBottomSheetLayout(
        sheetContent = {
            InCallDialPad(callVM, transferCall)
        },
        sheetState = sheetState,
    ) {
        Scaffold(
            bottomBar = {
                CallScreenBottomBar(
                    state = state,
                    sheetState = sheetState,
                    holdCall = holdCall,
                    callVM = callVM
                )
            },
            topBar = {
                CallScreenTopBar(
                    state = state,
                    stopActivity = stopActivity,
                    callVM = callVM
                )
            }
        ) {
            Box(Modifier.padding(it)) {}
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 12.dp, top = 34.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (state == CallButtonsState.INCOMING_CALL) {
                InCallMasterButton(
                    color = Color(0xFF74B853),
                    icon = Icons.Filled.Call,
                    onClick = pickUp
                )
            }
            InCallMasterButton(
                color = Color(0xFFE0393B),
                icon = Icons.Filled.CallEnd,
                onClick = hangUp
            )
        }
    }
}

@Composable
private fun InCallDialPad(
    callVM: CallViewModel,
    transferCall: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            val dialPadText by callVM.dialPadText.collectAsState()
            Text(
                style = MaterialTheme.typography.headlineLarge,
                text = dialPadText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Divider(
                color = Color(0xFFC6C2CB),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                thickness = 1.dp
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    DialPadTextButton("1", callVM)
                    DialPadTextButton("2", callVM)
                    DialPadTextButton("3", callVM)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    DialPadTextButton("4", callVM)
                    DialPadTextButton("5", callVM)
                    DialPadTextButton("6", callVM)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    DialPadTextButton("7", callVM)
                    DialPadTextButton("8", callVM)
                    DialPadTextButton("9", callVM)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    val view = LocalView.current
                    DialPadIconButton(
                        icon = Icons.Outlined.Backspace,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            callVM.backspaceDialPadText()
                        },
                        onLongClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            callVM.clearDialPadText()
                        })
                    DialPadTextButton("0", callVM)
                    DialPadIconButton(
                        icon = Icons.Outlined.PhoneCallback,
                        onClick = { transferCall() },
                        onLongClick = {}
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .height(108.dp)
            )
        }
    }
}

@Composable
private fun DialPadTextButton(
    label: String,
    callVM: CallViewModel
) {
    val view = LocalView.current
    Text(
        text = label,
        style = MaterialTheme.typography.displaySmall.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                callVM.appendDialPadText(label)
            }
    )
}

@Composable
private fun DialPadIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() },
                    onTap = { onClick() }
                )
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(34.dp)
        )
    }
}

@Composable
private fun CallScreenTopBar(
    state: CallButtonsState,
    stopActivity: () -> Unit,
    callVM: CallViewModel = get()
) {
    val appointment = callVM.comradeAppointment.collectAsState()
    Column(
        modifier = Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
            IconButton(onClick = { stopActivity() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        }
        AsyncImage(
            modifier = Modifier
                .size(172.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            model = ImageRequest.Builder(LocalContext.current)
                .data(getImageUrl(appointment.value.line))
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            error = painterResource(id = R.drawable.ic_dummy_avatar),
            placeholder = painterResource(id = R.drawable.ic_dummy_avatar),
            contentDescription = null
        )
        Text(
            text = appointment.value.fullName,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 2.dp),
            style = MaterialTheme.typography.headlineMedium,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Номер: ${appointment.value.line}",
            maxLines = 1,
            style = MaterialTheme.typography.labelLarge,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = when(state) {
                CallButtonsState.CALL_PROCESS, CallButtonsState.CALL_ENDED -> callVM.displayTime.value
                CallButtonsState.INCOMING_CALL -> "Входящий звонок"
                CallButtonsState.OUTGOING_CALL -> "Набор номера"
            },
            maxLines = 1,
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.labelMedium,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CallScreenBottomBar(
    state: CallButtonsState,
    sheetState: ModalBottomSheetState,
    holdCall: () -> Unit,
    callVM: CallViewModel = get()
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state != CallButtonsState.INCOMING_CALL) {
            Row(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                val isSpeakerModeEnabled by callVM.isSpeakerModeEnabled.collectAsState()
                InCallButton(
                    title = "Динамик",
                    icon = if (isSpeakerModeEnabled) Icons.Filled.VolumeUp else Icons.Outlined.VolumeUp,
                    onClick = { callVM.changeSound() },
                    status = when {
                        state != CallButtonsState.CALL_PROCESS -> ButtonStatus.INACTIVE
                        isSpeakerModeEnabled -> ButtonStatus.SELECTED
                        !isSpeakerModeEnabled -> ButtonStatus.NOT_SELECTED
                        else -> ButtonStatus.INACTIVE
                    }
                )
                val isMicMuted by callVM.isMicMuted.collectAsState()
                InCallButton(
                    title = "Заглушить",
                    icon = if (isMicMuted) Icons.Filled.MicOff else Icons.Outlined.MicOff,
                    onClick = { callVM.micMute() },
                    status = when {
                        state != CallButtonsState.CALL_PROCESS -> ButtonStatus.INACTIVE
                        isMicMuted -> ButtonStatus.SELECTED
                        !isMicMuted -> ButtonStatus.NOT_SELECTED
                        else -> ButtonStatus.INACTIVE
                    }
                )
                val isBluetoothEnabled by callVM.isBluetoothEnabled.collectAsState()
                InCallButton(
                    title = "Bluetooth",
                    icon = if (isBluetoothEnabled) Icons.Filled.PhoneBluetoothSpeaker else Icons.Outlined.PhoneBluetoothSpeaker,
                    onClick = { callVM.changeBluetooth() },
                    status = when {
                        state != CallButtonsState.CALL_PROCESS -> ButtonStatus.INACTIVE
                        isBluetoothEnabled -> ButtonStatus.SELECTED
                        !isBluetoothEnabled -> ButtonStatus.NOT_SELECTED
                        else -> ButtonStatus.INACTIVE
                    }
                )
            }
            Spacer(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .height(16.dp)
            )
            Row(
                modifier = Modifier.width(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                val holdState = callVM.callHoldState.value
                InCallButton(
                    title = "Удерживать",
                    icon = when (holdState) {
                        HoldState.REMOTE_HOLD -> Icons.Filled.SettingsPhone
                        else -> Icons.Outlined.SettingsPhone
                    },
                    onClick = { holdCall() },
                    status = when {
                        state != CallButtonsState.CALL_PROCESS -> ButtonStatus.INACTIVE
                        holdState == HoldState.LOCAL_HOLD -> ButtonStatus.SELECTED
                        else -> ButtonStatus.NOT_SELECTED
                    }
                )
                InCallButton(
                    title = "Клавиатура",
                    icon = Icons.Outlined.Dialpad,
                    onClick = { scope.launch { sheetState.show() } },
                    status = when {
                        state != CallButtonsState.CALL_PROCESS -> ButtonStatus.INACTIVE
                        sheetState.isVisible -> ButtonStatus.SELECTED
                        else -> ButtonStatus.NOT_SELECTED
                    }
                )
            }
            Spacer(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .height(92.dp)
            )
        }
    }
}

@Composable
private fun InCallButton(
    title: String,
    icon: ImageVector,
    status: ButtonStatus = ButtonStatus.NOT_SELECTED,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.wrapContentSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = { onClick() },
            enabled = status != ButtonStatus.INACTIVE
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = when (status) {
                    ButtonStatus.INACTIVE -> MaterialTheme.colorScheme.outline
                    ButtonStatus.SELECTED -> MaterialTheme.colorScheme.tertiary
                    ButtonStatus.NOT_SELECTED -> LocalContentColor.current
                }
            )
        }
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge.copy(
                color = when (status) {
                    ButtonStatus.INACTIVE -> MaterialTheme.colorScheme.outline
                    ButtonStatus.SELECTED -> MaterialTheme.colorScheme.tertiary
                    ButtonStatus.NOT_SELECTED -> LocalContentColor.current
                }
            )
        )
    }
}

@Composable
private fun InCallMasterButton(
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        shape = CircleShape,
        color = color,
        shadowElevation = 4.dp
    ) {
        Box {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center),
                tint = MaterialTheme.colorScheme.background
            )
        }
    }
}

enum class ButtonStatus {
    INACTIVE, SELECTED, NOT_SELECTED
}
