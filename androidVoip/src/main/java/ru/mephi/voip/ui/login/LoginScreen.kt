package ru.mephi.voip.ui.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import ru.mephi.shared.data.model.Account
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.data.AccountStatusRepository

@Composable
internal fun LoginScreen(
    goBack: () -> Unit,
    accountRepo: AccountStatusRepository = get(),
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var sipInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var isLocked by remember { mutableStateOf(false) }
    val onRun = { account: Account ->
        scope.launch {
            runCredentialsCheck(
                goBack = goBack,
                setErrorMsg = { s ->
                    errorMsg = s; isError = s.isNotEmpty()
                },
                setLockState = { b -> isLocked = b },
                account = account,
                accountRepo = accountRepo
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
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Вход",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(bottom = 32.dp)
            )
            LoginTextField(
                value = sipInput,
                onValueChange = { s -> sipInput = s; isError = false },
                label = { Text(text = "Номер") },
                readOnly = isLocked,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            LoginTextField(
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
            ErrorText(
                isError = isError,
                errorMsg = errorMsg
            )
            Row(
                modifier = Modifier
                    .padding(start = 28.dp, end = 28.dp, bottom = 32.dp, top = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { goBack() },
                    enabled = !isLocked
                ) {
                    Text(
                        text = "Назад",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                OutlinedButton(
                    onClick = {
                        if (!isLocked) onRun(Account(login = sipInput, password = passwordInput))
                    },
                    enabled = !isLocked
                ) {
                    Text(
                        text = "Войти",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
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
    accountRepo: AccountStatusRepository
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
    if (accountRepo.accountsList.value.map { it.login }.contains(account.login)) {
        setErrorMsg("В этот аккаунт уже выполнен вход!")
        setLockState(false)
        return
    }
    val exitPhoneOnError = accountRepo.isSipEnabled.value
    accountRepo.setActiveAccount(account)
    accountRepo.phoneStatus.collect { status ->
        when (status) {
            AccountStatus.LOADING -> {}
            AccountStatus.REGISTERED -> {
                accountRepo.addAccount(account); setLockState(false); goBack(); return@collect
            }
            else -> {
                setErrorMsg("Не удалось войти в акакунт"); if (exitPhoneOnError) accountRepo.exitPhone(); setLockState(false); return@collect
            }
        }
    }
}

@Composable
private fun LoginTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val width = LocalConfiguration.current.screenWidthDp
    Column(
        modifier = Modifier.wrapContentSize(),
        horizontalAlignment = Alignment.Start
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            singleLine = true,
            isError = isError,
            readOnly = readOnly,
            modifier = modifier.width(if (width * 0.7 > 256) 256.dp else (width * 0.7).dp),
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
        )
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
