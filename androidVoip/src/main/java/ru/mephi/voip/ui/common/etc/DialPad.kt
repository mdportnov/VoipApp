package ru.mephi.voip.ui.common.etc

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhoneCallback
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
                    onLongClick = callVM::clearDialPadText,
                    enabled = dialPadText.isNotEmpty(),
                    iconSize = 26.dp
                )
            }
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 24.dp),
                thickness = 1.dp
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        onClick = closeDialPad,
                        iconSize = 34.dp
                    )
                    DialPadTextButton("0", callVM::appendDialPadText)
                    DialPadIconButton(
                        icon = Icons.Outlined.PhoneCallback,
                        onClick = launchCall,
                        iconSize = 34.dp
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
    DialPadButton(
        onClick = { appendText(label) },
        onLongClick = null,
        enabled = true,
        vibrateBefore = true
    ) {
        Box(
            modifier = Modifier
                .height(52.dp)
                .width(72.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.displaySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun DialPadIconButton(
    icon: ImageVector,
    iconSize: Dp,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    DialPadButton(
        onClick = onClick,
        onLongClick = onLongClick,
        enabled = enabled,
        vibrateBefore = false
    ) {
        Box(
            modifier = Modifier.size(52.dp)
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
}

@Composable
private fun DialPadButton(
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    vibrateBefore: Boolean,
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(enabled) {
        if (!enabled) {
            interactionSource.emit(PressInteraction.Release(PressInteraction.Press(Offset(0f, 0f))))
        }
    }
    Box(
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(12.dp))
            .indication(interactionSource, LocalIndication.current)
            .pointerInput(enabled) {
                detectTapGestures(
                    onLongPress = when (onLongClick == null || !enabled) {
                        true -> null
                        false -> { _ ->
                            if (!vibrateBefore) {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            }
                            onLongClick()
                        }
                    },
                    onTap = when (onClick == null || !enabled) {
                        true -> null
                        false -> { _ ->
                            if (!vibrateBefore) {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            }
                            onClick()
                        }
                    },
                    onPress = {
                        when {
                            !enabled -> return@detectTapGestures
                            !vibrateBefore -> {}
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS)
                            }
                            else -> {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                        }
                        PressInteraction
                            .Press(it)
                            .let { press ->
                                interactionSource.emit(press)
                                tryAwaitRelease()
                                interactionSource.emit(PressInteraction.Release(press))
                            }
                    }
                )
            },
    ) {
        content()
    }
}
