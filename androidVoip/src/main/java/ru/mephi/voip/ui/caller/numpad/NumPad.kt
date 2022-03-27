package ru.mephi.voip.ui.caller.numpad

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

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