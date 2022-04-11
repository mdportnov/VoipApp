package ru.mephi.voip.ui.components.numpad

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

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

    AnimatedVisibility(
        visible = numPadState,
        enter = slideInVertically() + expandVertically() + fadeIn(),
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