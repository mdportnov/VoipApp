@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.screens.settings.dialogs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.koin.androidx.compose.get
import ru.mephi.voip.vm.SettingsViewModel

@Composable
fun LoginDialog(
    settingsVM: SettingsViewModel = get()
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val loginErrorMsg by settingsVM.loginErrorMsg.collectAsState()
    val isLoginUiLocked by settingsVM.isLoginUiLocked.collectAsState()
    var sipInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    Dialog(
        onDismissRequest = settingsVM::closeLoginDialog,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = settingsVM::closeLoginDialog
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .padding(top = 34.dp, bottom = 42.dp, start = 10.dp, end = 10.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Вход",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(bottom = 12.dp)
                    )
                    val width = LocalConfiguration.current.screenWidthDp
                    Column(
                        modifier = Modifier.width(if (width * 0.7 > 256) 256.dp else (width * 0.7).dp)
                    ) {
                        OutlinedTextField(
                            value = sipInput,
                            onValueChange = { s -> sipInput = s; settingsVM.clearErrorMsg() },
                            label = { Text(text = "Номер") },
                            readOnly = isLoginUiLocked,
                            isError = loginErrorMsg.isNotEmpty(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = {
                                focusManager.moveFocus(
                                    FocusDirection.Down
                                )
                            })
                        )
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { s -> passwordInput = s; settingsVM.clearErrorMsg() },
                            label = { Text(text = "Пароль") },
                            readOnly = isLoginUiLocked,
                            isError = loginErrorMsg.isNotEmpty(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                    Icon(
                                        imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                settingsVM.startNewAccountLogin(
                                    login = sipInput,
                                    password = passwordInput
                                )
                            })
                        )
                    }
                    if (loginErrorMsg.isNotEmpty()) {
                        Text(
                            text = loginErrorMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 1.dp, horizontal = 4.dp)
                        )
                    }
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            settingsVM.startNewAccountLogin(
                                login = sipInput,
                                password = passwordInput
                            )
                        },
                        enabled = !isLoginUiLocked,
                        modifier = Modifier.padding(top = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Войти",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
    BackHandler(
        enabled = true,
        onBack = settingsVM::closeLoginDialog
    )
}
