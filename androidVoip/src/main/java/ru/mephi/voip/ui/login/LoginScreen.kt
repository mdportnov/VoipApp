@file:OptIn(ExperimentalComposeUiApi::class)

package ru.mephi.voip.ui.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.focus.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import ru.mephi.shared.data.model.Account
import ru.mephi.voip.data.LoginStatus
import ru.mephi.voip.data.PhoneManager

@Composable
internal fun LoginScreen(
    goBack: () -> Unit,
    accountRepo: PhoneManager = get(),
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var sipInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var isLocked by remember { mutableStateOf(false) }
    val onRun = { account: Account ->
        keyboardController?.hide()
        scope.launch {
            runCredentialsCheck(
                goBack = goBack,
                setErrorMsg = { s ->
                    errorMsg = s; isError = s.isNotEmpty()
                },
                setLockState = { b -> isLocked = b },
                account = account,
                phoneManager = accountRepo
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        if (isLocked) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 4.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Вход",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(bottom = 16.dp)
            )
            val width = LocalConfiguration.current.screenWidthDp
            Column(
                modifier = Modifier.width(if (width * 0.7 > 256) 256.dp else (width * 0.7).dp)
            ) {
                OutlinedTextField(
                    value = sipInput,
                    onValueChange = { s -> sipInput = s; isError = false },
                    label = { Text(text = "Номер") },
                    readOnly = isLocked,
                    isError = isError,
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
                    onValueChange = { s -> passwordInput = s; isError = false },
                    label = { Text(text = "Пароль") },
                    readOnly = isLocked,
                    isError = isError,
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
                        if (!isLocked) onRun(Account(login = sipInput, password = passwordInput))
                    })
                )
            }
            ErrorText(
                isError = isError,
                errorMsg = errorMsg
            )
            Button(
                onClick = {
                    if (!isLocked) {
                        onRun(Account(login = sipInput, password = passwordInput))
                    }
                },
                enabled = !isLocked,
                modifier = Modifier.width(if (width * 0.7 > 256) 256.dp else (width * 0.7).dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Войти",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
    BackHandler(!isLocked) {
        goBack()
    }
}

private suspend fun runCredentialsCheck(
    goBack: () -> Unit,
    setErrorMsg: (String) -> Unit,
    setLockState: (Boolean) -> Unit,
    account: Account,
    phoneManager: PhoneManager
) {
    setErrorMsg("")
    setLockState(true)
    if (account.login.isEmpty() || account.password.isEmpty()) {
        setErrorMsg("Номер или пароль не может быть пустым!")
        setLockState(false)
        return
    }
    if (account.login.toIntOrNull() == null) {
        setErrorMsg("В номере допустимы только цифры!")
        setLockState(false)
        return
    }
    if (phoneManager.accountsList.value.map { it.login }.contains(account.login)) {
        setErrorMsg("В этот аккаунт уже выполнен вход!")
        setLockState(false)
        return
    }
    phoneManager.startNewAccountLogin(account)
    phoneManager.loginStatus.collect { status ->
        when (status) {
            LoginStatus.LOGIN_FAILURE -> {
                setErrorMsg("Не удалось войти в акакунт"); setLockState(false); return@collect
            }
            LoginStatus.LOGIN_IN_PROGRESS -> {
                setLockState(true); setErrorMsg("")
            }
            LoginStatus.LOGIN_SUCCESSFUL -> {
                setLockState(false); goBack(); return@collect
            }
            LoginStatus.DATA_FETCH_FAILURE -> {
                setErrorMsg("Не удалось получить данные о пользователе"); setLockState(false); return@collect
            }
            else -> {}
        }
    }
}

@Composable
private fun ErrorText(
    isError: Boolean,
    errorMsg: String
) {
    Box(
        modifier = Modifier.defaultMinSize(minHeight = 24.dp)
    ) {
        if (isError) {
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 1.dp, horizontal = 4.dp)
            )
        }
    }
}
