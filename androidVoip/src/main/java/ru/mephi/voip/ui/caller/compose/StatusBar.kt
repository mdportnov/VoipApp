package ru.mephi.voip.ui.caller.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import ru.mephi.voip.R
import ru.mephi.voip.ui.call.CallActivity
import ru.mephi.voip.ui.call.CallState
import ru.mephi.voip.ui.call.CallViewModel

@Composable
fun StatusBar() {
    val callViewModel = get<CallViewModel>()
    val context = LocalContext.current
    val callState = callViewModel.callState.collectAsState()
    val isStatusBarShowed = callViewModel.isStatusBarShowed.collectAsState()

    AnimatedVisibility(
        visible = isStatusBarShowed.value,
        enter = slideInVertically(),
        exit = slideOutVertically(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .background(color = colorResource(id = R.color.colorGreen))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            CallActivity.open(context.applicationContext)
                        }
                    )
                },
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (callState.value == CallState.CONNECTED) {
                Text(
                    text = if (callViewModel.callerName.value.isEmpty()) callViewModel.number else callViewModel.callerName.value,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Call, contentDescription = "call", tint = Color.White,
                        modifier = Modifier
                            .size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = callViewModel.displayTime.value, textAlign = TextAlign.Center,
                        color = Color.White, fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Вернуться к звонку",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}