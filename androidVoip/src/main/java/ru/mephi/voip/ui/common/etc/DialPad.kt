package ru.mephi.voip.ui.common.etc

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhoneCallback
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import ru.mephi.voip.ui.call.CallViewModel

@Composable
fun DialPad(
    isVisible: Boolean,
    closeDialPad: () -> Unit,
    launchCall: () -> Unit,
    bottomPadding: Dp = 0.dp,
    callVM: CallViewModel = get()
) {
    Surface(
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 56.dp, end = 4.dp, top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val dialPadText by callVM.dialPadText.collectAsState()
                Text(
                    text = dialPadText,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                DialPadIconButton(
                    icon = Icons.Outlined.Backspace,
                    onClick = callVM::backspaceDialPadText,
                    enabled = dialPadText.isNotEmpty(),
                    iconSize = 26.dp
                )
            }
            Divider(
                color = Color(0xFFC6C2CB),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 14.dp),
                thickness = 1.dp
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    DialPadTextButton("1", callVM::appendDialPadText)
                    DialPadTextButton("2", callVM::appendDialPadText)
                    DialPadTextButton("3", callVM::appendDialPadText)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    DialPadTextButton("4", callVM::appendDialPadText)
                    DialPadTextButton("5", callVM::appendDialPadText)
                    DialPadTextButton("6", callVM::appendDialPadText)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    DialPadTextButton("7", callVM::appendDialPadText)
                    DialPadTextButton("8", callVM::appendDialPadText)
                    DialPadTextButton("9", callVM::appendDialPadText)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    DialPadIconButton(
                        icon = Icons.Outlined.Close,
                        onClick = closeDialPad
                    )
                    DialPadTextButton("0", callVM::appendDialPadText)
                    DialPadIconButton(
                        icon = Icons.Outlined.PhoneCallback,
                        onClick = launchCall
                    )
                }
            }
            Spacer(modifier = Modifier.height(bottomPadding))
        }
    }
    BackHandler(
        enabled = isVisible,
        onBack = closeDialPad
    )
}

@Composable
private fun DialPadTextButton(
    label: String,
    appendText: (String) -> Unit
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
                appendText(label)
            }
    )
}

@Composable
private fun DialPadIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    iconSize: Dp = 34.dp
) {
    val view = LocalView.current
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(iconSize),
            tint = when (enabled) {
                true -> LocalContentColor.current
                false -> LocalContentColor.current.copy(alpha = 0.7f)
            }
        )
    }
}
