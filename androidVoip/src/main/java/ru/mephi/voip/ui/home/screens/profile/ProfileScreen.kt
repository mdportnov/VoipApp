@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.data.PhoneManager
import ru.mephi.voip.ui.home.screens.profile.common.dialogs.ManageAccountsDialog
import ru.mephi.voip.ui.home.screens.profile.common.items.AccountCard
import ru.mephi.voip.ui.profile.ProfileViewModel
import ru.mephi.voip.ui.profile.favourites.FavouriteContactsBoard
import ru.mephi.voip.ui.profile.getAnnotatedText
import ru.mephi.voip.utils.launchDialer
import ru.mephi.voip.utils.launchMailClientIntent

@Composable
internal fun ProfileScreen(
    openSettings: () -> Unit,
    openLogin: () -> Unit,
    phone: PhoneManager = get()
) {
    val scope = rememberCoroutineScope()
    var openManager by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { ProfileTopBar(openSettings) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "Управлять аккаунтами") },
                icon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
                onClick = { openManager = true })
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            val currentAccount = phone.currentAccount.collectAsState()
            if (currentAccount.value.login.isNotEmpty()) {
                val phoneStatus = phone.phoneStatus.collectAsState()
                Box(modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 8.dp)) {
                    AccountCard(
                        displayedName = currentAccount.value.displayedName,
                        sip = "Ваш номер: ${currentAccount.value.login}",
                        status = "Статус: ${phoneStatus.value.status}",
                        onClick = null,
                        trailingIcon = { IconStatus(phoneStatus.value) }
                    )
                }
                PhoneButton()
                FavouriteContactsBoard(
                    modifier = Modifier.fillMaxWidth(),
                    profileViewModel = get(),
                    phoneManager = get()
                )
            } else {
                val annotatedText = getAnnotatedText()
                val localContext = LocalContext.current
                val hapticFeedback = LocalHapticFeedback.current
                ClickableText(text = annotatedText, onClick = { offset ->
                    annotatedText.getStringAnnotations(
                        tag = "email", start = offset, end = offset
                    ).firstOrNull()?.let { annotation ->
                        localContext.launchMailClientIntent(annotation.item)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    annotatedText.getStringAnnotations(
                        tag = "phone", start = offset, end = offset
                    ).firstOrNull()?.let { annotation ->
                        localContext.launchDialer(annotation.item)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
        if (openManager) {
            ManageAccountsDialog(
                onDismiss = { openManager = false },
                openAddAccountDialog = {
                    scope.launch {
                        openManager = false
                        delay(250)
                        openLogin()
                    }
                }
            )
        }
    }

}

@Composable
private fun IconStatus(
    status: AccountStatus
) {
    IconButton(onClick = {  }) {
        Icon(
            modifier = Modifier.size(34.dp),
            imageVector = when (status) {
                AccountStatus.UNREGISTERED -> Icons.Outlined.PhoneDisabled
                AccountStatus.CONNECTING,
                AccountStatus.STARTING_UP,
                AccountStatus.SHUTTING_DOWN -> Icons.Outlined.Sync
                AccountStatus.RESTARTING -> Icons.Outlined.RestartAlt
                AccountStatus.NO_CONNECTION -> Icons.Outlined.WifiOff
                AccountStatus.REGISTRATION_FAILED,
                AccountStatus.RECONNECTING -> Icons.Outlined.ErrorOutline
                AccountStatus.REGISTERED -> Icons.Outlined.Done
            },
            tint = when (status) {
                AccountStatus.REGISTRATION_FAILED,
                AccountStatus.RECONNECTING,
                AccountStatus.NO_CONNECTION -> MaterialTheme.colorScheme.error
                AccountStatus.CONNECTING,
                AccountStatus.UNREGISTERED,
                AccountStatus.STARTING_UP,
                AccountStatus.SHUTTING_DOWN,
                AccountStatus.RESTARTING -> Color(0xFFDE7411)
                AccountStatus.REGISTERED -> Color(0xFF58B95D)
            },
            contentDescription = null
        )
    }
}


@Composable
private fun ProfileTopBar(
    openSettings: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    CenterAlignedTopAppBar(
        title = { Text(text = "Профиль", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = { }, enabled = false) {
                Image(
                    painter = painterResource(id = R.drawable.logo_mephi),
                    contentDescription = null,
                )
            }
        },
        actions = {
            IconButton(modifier = Modifier, onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                openSettings()
            }) {
                Icon(Icons.Default.Settings, contentDescription = null)
            }
        }
    )
}

@Composable
private fun PhoneButton(
    viewModel: ProfileViewModel = get(),
    accountRepository: PhoneManager = get()
) {
    val sipStatus = accountRepository.isSipEnabled.collectAsState()
    val phoneStatus = accountRepository.phoneStatus.collectAsState()
    Button(
        onClick = { viewModel.toggleSipStatus() },
        modifier = Modifier
            .padding(top = 4.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)
            .wrapContentHeight()
            .fillMaxWidth(),
        enabled = when (phoneStatus.value) {
            AccountStatus.SHUTTING_DOWN,
            AccountStatus.RESTARTING,
            AccountStatus.STARTING_UP -> false
            else -> true
        }
    ) {
        Text(if (!sipStatus.value) "Включить SIP" else "Выключить SIP")
    }
}

