@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.shared.vm.SavedAccountsViewModel
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.home.screens.settings.Screens
import ru.mephi.voip.ui.home.screens.settings.screens.common.dialogs.ManageAccountsDialog
import ru.mephi.voip.ui.home.screens.settings.screens.common.items.AccountCard
import ru.mephi.voip.ui.home.screens.settings.screens.common.items.AccountMore
import ru.mephi.voip.ui.home.screens.settings.screens.common.items.SettingsItem
import ru.mephi.voip.ui.profile.ProfileViewModel
import ru.mephi.voip.utils.launchMailClientIntent

@Composable
internal fun SettingsHomeScreen(
    settingsNavCtl: NavController,
    openLogin: () -> Unit,
    accountRepository: AccountStatusRepository = get(),
    saVM: SavedAccountsViewModel = get(),
    pVM: ProfileViewModel = get()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var openManager by remember { mutableStateOf(false) }
    val list = accountRepository.accountsList.collectAsState()
    Scaffold(
        topBar = { ProfileTopBar() },
        floatingActionButton = {
            val currentAccount = accountRepository.currentAccount.collectAsState()
            val sipStatus = accountRepository.isSipEnabled.collectAsState()
            if (currentAccount.value.login.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = { Text(if (!sipStatus.value) "Включить SIP" else "Выключить SIP") },
                    icon = { Icon(imageVector = Icons.Outlined.Sip, contentDescription = null) },
                    onClick = { pVM.toggleSipStatus() },
                )
            }
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            Box(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 10.dp)) {
                if (list.value.isEmpty()) {
                    AccountCard(
                        displayedName = "Без аккаунта",
                        sip = "Нет добавленных аккаунтов",
                        status = "Нажмите, чтобы добавить",
                        onClick = { openLogin() },
                        trailingIcon = { AccountMore { openLogin() } }
                    )
                } else {
                    val account = saVM.currentAccount.collectAsState()
                    val currentAccount = accountRepository.currentAccount.collectAsState()
                    val accountStatus = accountRepository.phoneStatus.collectAsState()
                    AccountCard(
                        displayedName = "${account.value.firstname} ${account.value.lastname}",
                        sip = "Ваш номер: ${currentAccount.value.login}",
                        status = "Статус: ${accountStatus.value.status}",
                        onClick = null,
                        trailingIcon = { AccountMore { openLogin() } }
                    )
                }
            }
            SettingsHomeDivider()
            SettingsItem(
                icon = Icons.Outlined.CloudSync,
                title = "Работа в фоне",
                onClick = { settingsNavCtl.navigate(route = Screens.BackgroundWorkScreen.route) }
            )
            SettingsItem(
                icon = Icons.Outlined.MyLocation,
                title = "Стартовый экран",
                onClick = { settingsNavCtl.navigate(route = Screens.StartSelectionScreen.route) }
            )
            SettingsItem(
                icon = Icons.Outlined.Fullscreen,
                title = "Экран входящего вызова",
                onClick = { settingsNavCtl.navigate(route = Screens.EnableOverlayScreen.route) }
            )
            SettingsItem(
                icon = Icons.Outlined.DeleteOutline,
                title = "Очистить память",
                onClick = { settingsNavCtl.navigate(route = Screens.ClearDataScreen.route) }
            )
            SettingsHomeDivider()
            SettingsItem(
                icon = Icons.Outlined.Mail,
                title = "Обратная связь",
                onClick = { context.launchMailClientIntent("voip@mephi.ru") }
            )
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "О приложении",
                onClick = { settingsNavCtl.navigate(route = Screens.AboutAppScreen.route) }
            )
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
private fun ProfileTopBar(
    accountRepository: AccountStatusRepository = get(),
) {
    SmallTopAppBar(
        title = { Text(text = "Настройки", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = { },
        actions = {
            val phoneStatus = accountRepository.phoneStatus.collectAsState()
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = when (phoneStatus.value) {
                        AccountStatus.UNREGISTERED -> Icons.Outlined.PhoneDisabled
                        AccountStatus.LOADING -> Icons.Outlined.Sync
                        AccountStatus.NO_CONNECTION -> Icons.Outlined.WifiOff
                        AccountStatus.REGISTRATION_FAILED, AccountStatus.RECONNECTING -> Icons.Outlined.ErrorOutline
                        else -> Icons.Outlined.Done
                    },
                    contentDescription = null
                )
            }
        }
    )
}


@Composable
private fun SettingsHomeDivider() {
    Divider(
        color = TopAppBarDefaults.smallTopAppBarColors().containerColor(
            scrollFraction = 1.0f
        ).value,
        thickness = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
    )
}