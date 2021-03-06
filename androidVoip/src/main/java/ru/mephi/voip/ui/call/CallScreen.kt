package ru.mephi.voip.ui.call

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ru.mephi.voip.R
import ru.mephi.voip.ui.components.numpad.NumPad
import ru.mephi.voip.utils.getImageUrl
import ru.mephi.voip.utils.toast

private val buttonSize = 100.dp
private val iconSize = 30.dp

@ExperimentalCoilApi
@Composable
fun CallScreen(
    viewModel: CallViewModel,
    pickUp: () -> Unit,
    holdCall: () -> Unit,
    hangUp: () -> Unit,
    stopActivity: () -> Unit,
    transferCall: () -> Unit
) {
    val timeState = viewModel.displayTime.value
    val holdState = viewModel.callHoldState.value
    val callerName = viewModel.callerName.value
    val callerAppointment = viewModel.callerAppointment.value
    val callerUnit = viewModel.callerUnit.value

    val context = LocalContext.current
    val activity = context as Activity

    // https://medium.com/@lbenevento/handling-statusbar-colors-when-using-modalbottomsheets-in-jetpack-compose-181ece86cbcc
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(Color.Transparent)

    val gradient = listOf(
        Color(0x00FFFFFF),
        Color(0x4A03FFC5),
        Color(0xC900BCD4),
        Color(0xD5124DE4),
        Color(0xF35B1BF0),
    )

    val blurPainter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current).data(data = getImageUrl(viewModel.number))
            .apply(block = fun ImageRequest.Builder.() {
                diskCachePolicy(CachePolicy.ENABLED)
                memoryCachePolicy(CachePolicy.ENABLED)
            }).build()
    )

    val callerPhotoPainter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current).data(data = getImageUrl(viewModel.number))
            .apply(block = fun ImageRequest.Builder.() {
                transformations(RoundedCornersTransformation(10f))
            }).build()
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

    Box {
        ConstraintLayout(
            constraintSet, modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = blurPainter,
                contentDescription = "",
                contentScale = ContentScale.FillBounds,
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
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .background(brush = Brush.verticalGradient(gradient))
                    .padding(20.dp)
                    .fillMaxWidth()
                    .layoutId("info")
            ) {
                Text(
                    text = callerName,
                    textAlign = TextAlign.Left,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
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
                    text = callerUnit,
                    textAlign = TextAlign.Left,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )

                holdState.status?.let {
                    Text(text = it, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            ButtonsRow(
                Modifier
                    .fillMaxWidth()
                    .layoutId("options"), viewModel, pickUp, holdCall, hangUp
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
                IconButton(modifier = Modifier
                    .padding(16.dp, 0.dp, 0.dp, 8.dp)
                    .background(
                        colorResource(id = R.color.colorPrimary), CircleShape
                    )
                    .layoutId("back"), onClick = {
                    stopActivity()
                }) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "back",
                        tint = colorResource(id = R.color.colorBlack),
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            colorResource(id = R.color.colorPrimary), CircleShape
                        )
                        .padding(20.dp, 10.dp)
                        .layoutId("number")
                ) {
                    Text(
                        text = viewModel.number,
                        textAlign = TextAlign.Left,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (timeState.isNotBlank()) Box(
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 16.dp, 0.dp)
                        .background(
                            colorResource(id = R.color.colorPrimary), CircleShape
                        )
                        .padding(20.dp, 10.dp)
                        .layoutId("time")
                ) {
                    Text(
                        text = timeState, fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (viewModel.isNumPadVisible.value) Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center
        ) {
            NumPad(limitation = 11,
                inputState = viewModel.inputState.value,
                numPadState = viewModel.isNumPadVisible.value,
                onInputStateChanged = {
                    viewModel.changeInputState(it)
                },
                onNumPadStateChange = {
                    viewModel.toggleNumPad()
                },
                onLimitExceeded = {
                    activity.toast("???????????????? ???????????? ????????????")
                })
            AnimatedVisibility(
                visible = viewModel.inputState.value.length > 3,
                enter = slideInVertically() + expandVertically() + fadeIn(initialAlpha = 0.3f),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedButton(
                    onClick = {
                        transferCall()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        colorResource(id = R.color.colorGreen)
                    ),
                    elevation = ButtonDefaults.elevation()
                ) {
                    Text(
                        text = "?????????????????? ???????????? ????: ${viewModel.inputState.value}",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ButtonsRow(
    modifier: Modifier,
    viewModel: CallViewModel,
    pickUp: () -> Unit,
    holdCall: () -> Unit,
    hangUp: () -> Unit,
) {
    val micMuteState = viewModel.isMicMuted.collectAsState()
    val volumeState = viewModel.isSpeakerModeEnabled.collectAsState()
    val isBluetoothEnabled = viewModel.isBluetoothEnabled.collectAsState()

    val buttonsState = viewModel.buttonsState.value
    val holdState = viewModel.callHoldState.value

    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()
        ) {
            // ???????? ????????????????, ???????????????? ???????????? ???????????? ???? ??????????
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
                        Icons.Default.DialerSip,
                        tint = Color.White,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }

            // ???????? ???????????? ?? ????????????????, ???????????????? ????????????????????
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
                        Icons.Default.VolumeUp,
                        tint = Color.White,
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
                        Icons.Default.MicOff,
                        tint = Color.White,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }

            Button(
                onClick = { hangUp() },
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
                    Icons.Default.CallEnd,
                    tint = Color.White,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        if (buttonsState == CallButtonsState.CALL_PROCESS) Row(
            horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { holdCall() },
                modifier = Modifier
                    .padding(8.dp)
                    .size(buttonSize)
                    .clickable { },
                colors = ButtonDefaults.buttonColors(
                    colorResource(
                        id = if (holdState == HoldState.LOCAL_HOLD) R.color.colorAccent
                        else R.color.colorGray
                    )
                ),
                elevation = ButtonDefaults.elevation(),
                shape = RoundedCornerShape(20.dp),
            ) {
                Icon(
                    Icons.Default.PhonePaused,
                    tint = Color.White,
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
                        id = if (isBluetoothEnabled.value) R.color.colorPrimaryDark
                        else R.color.colorGray
                    )
                ),
                elevation = ButtonDefaults.elevation(),
                shape = RoundedCornerShape(20.dp),
            ) {
                Icon(
                    Icons.Default.PhoneBluetoothSpeaker,
                    tint = Color.White,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            }

            Button(
                onClick = {
                    viewModel.toggleNumPad()
                },
                modifier = Modifier
                    .padding(8.dp)
                    .size(buttonSize)
                    .clickable { },
                colors = ButtonDefaults.buttonColors(
                    colorResource(
                        id = if (viewModel.isNumPadVisible.value) R.color.colorAccent
                        else R.color.colorGray
                    )
                ),
                elevation = ButtonDefaults.elevation(),
                shape = RoundedCornerShape(20.dp),
            ) {
                Icon(
                    Icons.Default.SwapCalls,
                    tint = Color.White,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}