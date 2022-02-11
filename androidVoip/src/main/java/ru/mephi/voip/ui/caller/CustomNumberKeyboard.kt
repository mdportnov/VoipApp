package ru.mephi.voip.ui.caller

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.voip.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import ru.mephi.shared.appContext


@ExperimentalAnimationApi
@Composable
fun NumPad(
    limitation: Int,
    mutableInputState: String,
    mutableNumPadState: Boolean,
    onLimitExceeded: (() -> Unit)? = null,
    onTapWhenUp: (String) -> Unit,
) {
    var inputVal by remember { mutableStateOf(mutableInputState) }
    var isNumPadStateUp by remember { mutableStateOf(mutableNumPadState) }
    val activity = (LocalContext.current as? Activity)

    BackHandler {
        if (isNumPadStateUp)
            isNumPadStateUp = !isNumPadStateUp
        else
            activity?.finish()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        AnimatedVisibility(
            visible = isNumPadStateUp,
            enter = slideInVertically() + expandVertically(Alignment.Top)
                    + fadeIn(initialAlpha = 0.3f),
            exit = shrinkVertically() + fadeOut()
        ) {
            KeyBoard(
                modifier = Modifier,
                input = inputVal,
                onNumClick = { digit ->
                    if (inputVal.length + 1 > limitation)
                        onLimitExceeded?.let { it() }
                    else
                        inputVal += digit.toString()
                },
                onDeleteDigit = {
                    inputVal = inputVal.dropLast(1)
                },
                onSwipeDown = {
                    isNumPadStateUp = !isNumPadStateUp
                }
            )
        }
        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.End), onClick = {
                if (isNumPadStateUp)
                    onTapWhenUp(inputVal)
                else {
                    isNumPadStateUp = !isNumPadStateUp
                }
            }, backgroundColor = colorResource(id = R.color.colorGreen)
        ) {
            val isPermissionGranted = false
            if (isNumPadStateUp)
                Icon(
                    Icons.Default.Call,
                    contentDescription = "",
                    tint = colorResource(
                        id = if (isPermissionGranted)
                            R.color.colorGreen else R.color.colorGray
                    )
                ) else
                Icon(
                    painterResource(id = R.drawable.ic_baseline_dialpad_24),
                    contentDescription = "",
                    tint = colorResource(
                        id = if (isPermissionGranted)
                            R.color.colorGreen else R.color.colorGray
                    )
                )
        }
    }
}

@Composable
fun KeyBoard(
    modifier: Modifier,
    input: String,
    onNumClick: (digit: Char) -> Unit,
    onDeleteDigit: () -> Unit,
    onSwipeDown: () -> Unit,
) {
    Card(
        modifier = modifier.padding(10.dp),
        shape = RoundedCornerShape(4.dp),
        elevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = input,
                fontSize = 30.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                textAlign = TextAlign.Center
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                NumberButton(number = 1, onClick = onNumClick, modifier = Modifier.weight(1f))
                NumberButton(number = 2, onClick = onNumClick, modifier = Modifier.weight(1f))
                NumberButton(number = 3, onClick = onNumClick, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                NumberButton(number = 4, onClick = onNumClick, modifier = Modifier.weight(1f))
                NumberButton(number = 5, onClick = onNumClick, modifier = Modifier.weight(1f))
                NumberButton(number = 6, onClick = onNumClick, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                NumberButton(number = 7, onClick = onNumClick, modifier = Modifier.weight(1f))
                NumberButton(number = 8, onClick = onNumClick, modifier = Modifier.weight(1f))
                NumberButton(number = 9, onClick = onNumClick, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
//                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = {
                        onSwipeDown()
                    },
                    modifier = Modifier
                        .weight(1f)
//                        .aspectRatio(1f)
                        .padding(4.dp),
                    border = ButtonDefaults.outlinedBorder.copy(
                        brush = SolidColor(colorResource(id = R.color.colorAccent))
                    )
                ) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Убрать",
                        tint = colorResource(id = R.color.colorAccent)
                    )
                }
                NumberButton(number = 0, onClick = onNumClick, modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = {
                        onDeleteDigit()
                    },
                    modifier = Modifier
                        .weight(1f)
//                        .aspectRatio(1f)
                        .padding(4.dp),
                    border = ButtonDefaults.outlinedBorder.copy(
                        brush = SolidColor(colorResource(id = R.color.colorRed))
                    )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = colorResource(id = R.color.colorRed)
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberButton(
    number: Int,
    onClick: (digit: Char) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = {
            onClick(number.digitToChar())
        },
        modifier = modifier
//            .aspectRatio(1f)
            .padding(4.dp),
        border = ButtonDefaults.outlinedBorder.copy(
            brush = SolidColor(colorResource(id = R.color.colorPrimaryDark))
        )
    ) {
        Text(
            text = number.toString(),
            fontSize = 20.sp,
            color = colorResource(id = R.color.colorPrimaryDark)
        )
    }
}