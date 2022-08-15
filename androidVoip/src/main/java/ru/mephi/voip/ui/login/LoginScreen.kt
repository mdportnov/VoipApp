@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import ru.mephi.shared.data.model.Account
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.shared.vm.SavedAccountsViewModel
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.profile.ProfileViewModel
import timber.log.Timber

@Composable
internal fun LoginScreen(
    goBack: () -> Unit,
    accountRepo: AccountStatusRepository = get(),
    saVM: SavedAccountsViewModel = get()
) {
    val height = LocalConfiguration.current.screenHeightDp
    val width = LocalConfiguration.current.screenWidthDp
    val scope = rememberCoroutineScope()
    var isError by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var sipInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(vertical = 16.dp)
                ) {
                    OutlinedTextField(
                        value = sipInput,
                        onValueChange = { s -> sipInput = s; isError = false },
                        label = { Text(text = "SIP") },
                        singleLine = true,
                        isError = isError,
                        readOnly = isLocked,
                        modifier = Modifier.width(if (width * 0.7 > 256) 256.dp else (width * 0.7).dp)
                    )
                    var passwordVisible by rememberSaveable { mutableStateOf(false) }
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { s -> passwordInput = s; isError = false },
                        label = { Text(text = "Пароль") },
                        singleLine = true,
                        isError = isError,
                        readOnly = isLocked,
                        modifier = Modifier.width(if (width * 0.7 > 256) 256.dp else (width * 0.7).dp),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
                    )
                    Box(
                        modifier = Modifier.defaultMinSize(minHeight = 28.dp)
                    ) {
                        if (isError) {
                            Text(
                                text = errorMsg,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 14.dp)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(start = 28.dp, end = 28.dp, bottom = 34.dp)
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
                            isLocked = true
                            if (sipInput.toIntOrNull() == null || passwordInput.isEmpty()) {
                                errorMsg = "Введены некоректные данные"
                                isError = true
                                isLocked = false
                            } else if (accountRepo.accountsList.value.map { it.login }.contains(sipInput)) {
                                errorMsg = "Такой аккаунт уже существует"
                                isError = true
                                isLocked = false
                            } else {
                                val input = Account(login = sipInput, password = passwordInput)
                                val exitPhoneOnError = accountRepo.isSipEnabled.value
                                accountRepo.setActiveAccount(input)
                                scope.launch {
                                    accountRepo.phoneStatus.collect { status ->
                                        when (status) {
                                            AccountStatus.LOADING -> {}
                                            AccountStatus.REGISTERED -> {
                                                accountRepo.addAccount(input); isLocked = false; goBack(); return@collect
                                            }
                                            else -> {
                                                isError = true; errorMsg = "Не удалось войти в акакунт"; if (exitPhoneOnError) accountRepo.exitPhone(); isLocked = false; return@collect
                                            }
                                        }
                                    }
                                }
                            }
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
    ) {
        Box(
            modifier = Modifier
                .padding(
                    bottom = it.calculateBottomPadding().value.let { v ->
                        return@let (if (height / 2f > v) height / 2f else v).dp
                    }
                )
                .fillMaxSize()
        ) {
            if (isLocked) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
            Text(
                text = "Вход",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.BottomCenter)
            )
        }
    }
}