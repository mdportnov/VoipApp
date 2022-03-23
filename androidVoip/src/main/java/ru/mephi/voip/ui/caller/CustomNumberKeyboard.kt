package ru.mephi.voip.ui.caller

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.voip.R

@Composable
fun NumPad(
    limitation: Int,
    inputState: String,
    numPadState: Boolean,
    onNumPadStateChange: () -> Unit,
    onInputStateChanged: (String) -> Unit,
    onLimitExceeded: (() -> Unit)? = null,
) {
    var inputVal by remember { mutableStateOf(inputState) }
    val activity = (LocalContext.current as? Activity)

    BackHandler {
        if (numPadState) onNumPadStateChange()
        else activity?.finish()
    }

    AnimatedVisibility(
        visible = numPadState,
        enter = slideInVertically() + expandVertically()
                + fadeIn(initialAlpha = 0.3f),
        exit = shrinkVertically() + fadeOut()
    ) {
        KeyBoard(
            modifier = Modifier,
            input = inputVal,
            onNumClick = { digit ->
                if (inputState.length + 1 > limitation)
                    onLimitExceeded?.let { it() }
                else
                    inputVal += digit.toString()
                onInputStateChanged(inputVal)
            },
            onDeleteDigit = {
                inputVal = inputVal.dropLast(1)
                onInputStateChanged(inputVal)
            },
            onSwipeDown = {
                onNumPadStateChange()
            }
        )
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
        modifier = modifier
            .padding(10.dp)
            .background(Color.White),
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
                textAlign = TextAlign.Center,
                style = TextStyle(color = Color.Black)
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
                OutlinedButton(
                    onClick = {
                        onSwipeDown()
                    },
                    modifier = Modifier
                        .weight(1f)
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
        modifier = modifier.padding(4.dp),
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