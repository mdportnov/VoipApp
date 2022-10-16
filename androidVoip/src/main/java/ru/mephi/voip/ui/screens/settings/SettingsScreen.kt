@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package ru.mephi.voip.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import ru.mephi.shared.data.model.Account
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.data.AccountManagementStatus
import ru.mephi.voip.data.PhoneManager
import ru.mephi.voip.data.SettingsRepository
import ru.mephi.voip.ui.common.AccountCard
import ru.mephi.voip.ui.common.SipStatusActionButton
import ru.mephi.voip.ui.dialogs.LoginDialog
import ru.mephi.voip.ui.dialogs.ManageAccountsDialog
import ru.mephi.voip.ui.screens.settings.etc.AboutApp
import ru.mephi.voip.ui.screens.settings.etc.ParamsDivider
import ru.mephi.voip.ui.screens.settings.params.*
import ru.mephi.voip.ui.screens.settings.params.ClearSearchHistoryParam
import ru.mephi.voip.ui.screens.settings.params.BackgroundModeParam
import ru.mephi.voip.ui.screens.settings.params.CallScreenAlwaysEnabledParam
import ru.mephi.voip.ui.screens.settings.params.InitialScreenParam
import ru.mephi.voip.utils.launchDialer
import ru.mephi.voip.utils.launchMailClientIntent
import ru.mephi.voip.vm.SettingsViewModel

@Composable
internal fun SettingsScreen(
    phoneManager: PhoneManager = get(),
    settingsVM: SettingsViewModel = get()
) {
    val isLoginOpen by settingsVM.isLoginOpen.collectAsState()
    val isAccManagerOpen by settingsVM.isAccManagerOpen.collectAsState()
    val accountManagementStatus by phoneManager.accountManagementStatus.collectAsState()
    val phoneStatus by phoneManager.phoneStatus.collectAsState()
    Scaffold(
        topBar = { SettingsTopBar() },
        floatingActionButton = { SettingsFAB(
            accountManagementStatus = accountManagementStatus,
            phoneStatus = phoneStatus,
            settingsVM = settingsVM
        ) }
    ) {
        Box(modifier = Modifier.padding(it)) {
            if (isLoginOpen) {
                LoginDialog(onDismiss = settingsVM::closeLoginDialog)
            }
            if (isAccManagerOpen) {
                ManageAccountsDialog(
                    onDismiss = settingsVM::closeAccManagerDialog,
                    openLoginDialog = settingsVM::openLoginDialog
                )
            }
            SettingsContent(
                accountManagementStatus = accountManagementStatus,
                phoneStatus = phoneStatus,
                settingsVM = settingsVM,
                phoneManager = phoneManager
            )
        }
    }
}

@Composable
private fun SettingsTopBar() {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.bottom_bar_title_settings),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = { SipStatusActionButton() },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        )
    )
}

@Composable
private fun SettingsFAB(
    accountManagementStatus: AccountManagementStatus,
    phoneStatus: AccountStatus,
    settingsRepo: SettingsRepository = get(),
    settingsVM: SettingsViewModel = get()
) {
    val scope = rememberCoroutineScope()
    val isSipEnabled by settingsVM.isSipEnabled.collectAsState()
    Box(
        modifier = Modifier.wrapContentSize()
    ) {
        ExtendedFloatingActionButton(
            onClick = {
                when (accountManagementStatus) {
                    AccountManagementStatus.ACC_SELECTED -> {
                        when (phoneStatus) {
                            AccountStatus.STARTING_UP,
                            AccountStatus.RESTARTING,
                            AccountStatus.SHUTTING_DOWN -> { }
                            else -> {
                                scope.launch {
                                    settingsRepo.enableSip(!isSipEnabled)
                                }
                            }
                        }
                    }
                    AccountManagementStatus.ACC_NOT_SELECTED -> { settingsVM.openAccManagerDialog() }
                    AccountManagementStatus.NO_SAVED_ACC -> { settingsVM.openLoginDialog() }
                    else -> {}
                }
            },
            icon = {
                Icon(
                    imageVector = when (accountManagementStatus) {
                        AccountManagementStatus.ACC_SELECTED -> Icons.Outlined.Sip
                        AccountManagementStatus.ACC_NOT_SELECTED -> Icons.Outlined.Group
                        AccountManagementStatus.NO_SAVED_ACC -> Icons.Outlined.Add
                        else -> Icons.Default.ErrorOutline
                    },
                    contentDescription = null
                )
            },
            text = {
                Text(
                    text = when (accountManagementStatus) {
                        AccountManagementStatus.ACC_SELECTED -> when (isSipEnabled) {
                            true -> "Выключить SIP"
                            false -> "Включить SIP"
                        }
                        AccountManagementStatus.ACC_NOT_SELECTED -> "Выбрать аккаунт"
                        AccountManagementStatus.NO_SAVED_ACC -> "Добавить аккаунт"
                        else -> "Неизвестная ошибка"
                    }
                )
            }
        )
        AnimatedVisibility(
            visible = when (phoneStatus) {
                AccountStatus.STARTING_UP,
                AccountStatus.RESTARTING,
                AccountStatus.SHUTTING_DOWN -> true
                else -> false
            },
            enter = fadeIn(initialAlpha = 0.6f, animationSpec = tween(50)),
            exit = fadeOut(targetAlpha = 0.4f, animationSpec = tween(50)),
            modifier = Modifier.matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(color = Color(0x343E3E3E), shape = RoundedCornerShape(16.dp))
            )
        }
    }
}

@Composable
private fun SettingsContent(
    accountManagementStatus: AccountManagementStatus,
    phoneStatus: AccountStatus,
    settingsVM: SettingsViewModel = get(),
    settingsRepo: SettingsRepository = get(),
    phoneManager: PhoneManager = get()
) {
    CompositionLocalProvider(
        LocalOverscrollConfiguration provides null
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            val currentAccount by phoneManager.currentAccount.collectAsState()
            when (accountManagementStatus) {
                AccountManagementStatus.ACC_SELECTED -> {
                    SelectedAccCard(
                        currentAccount = currentAccount,
                        phoneStatus = phoneStatus
                    )
                }
                AccountManagementStatus.ACC_NOT_SELECTED -> {
                    SettingsAccNotSelected()
                }
                AccountManagementStatus.NO_SAVED_ACC -> {
                    SettingsNoSavedAcc()
                }
                AccountManagementStatus.LOADING -> {
                    SettingsAccLoading()
                }
            }
            ParamsDivider()
            BackgroundModeParam(
                phoneStatus = phoneStatus,
                enableBackgroundMode = settingsRepo::enableBackgroundMode,
                settingsVM = settingsVM
            )
            CallScreenAlwaysEnabledParam(
                enableCallScreenAlways = settingsRepo::enableCallScreenAlways,
                settingsVM = settingsVM
            )
            InitialScreenParam(
                setStartScreen = settingsRepo::setStartScreen,
                settingsVM = settingsVM
            )
            ParamsDivider()
            ClearSearchHistoryParam(
                clearSearchHistory = settingsRepo::clearSearchHistory
            )
            ClearCatalogCacheParam(
                clearCatalogCache = settingsRepo::clearCatalogCache
            )
            ClearFavouritesParam(
                clearFavourites = settingsRepo::clearFavourites
            )
            ParamsDivider()
            SendFeedbackParam(
                sendFeedback = settingsRepo::sendFeedback
            )
            ParamsDivider()
            AboutApp()
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
            )
        }
    }
}

@Composable
private fun SelectedAccCard(
    currentAccount: Account,
    phoneStatus: AccountStatus
) {
    Box(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 2.dp)
    ) {
        AccountCard(
            displayedName = currentAccount.displayedName,
            sip = currentAccount.login,
            sipDescription = "Ваш номер:",
            status = "Статус: ${phoneStatus.status}",
            trailingIcon = {
                var isMenuOpened by remember { mutableStateOf(false) }
                IconButton(onClick = { isMenuOpened = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                }
                Box(
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    CurrentAccMenu(
                        expanded = isMenuOpened,
                        onDismiss = { isMenuOpened = false }
                    )
                }
            }
        )
    }
}

@Composable
private fun CurrentAccMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    phoneManager: PhoneManager = get(),
    settingsVM: SettingsViewModel = get(),
) {
    var openHelp by remember { mutableStateOf(false) }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismiss() },
    ) {
        DropdownMenuItem(
            text = { Text("Добавить аккаунт") },
            onClick = {
                onDismiss()
                settingsVM.openLoginDialog()
            },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.PersonAdd, contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("Сменить аккаунт") },
            onClick = {
                onDismiss()
                settingsVM.openAccManagerDialog()
            },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Group, contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("Удалить аккаунт") },
            onClick = {
                onDismiss()
                phoneManager.removeAccount(phoneManager.currentAccount.value)
            },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.DeleteForever, contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("Помощь") },
            onClick = {
                onDismiss()
                openHelp = true
            },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.HelpOutline, contentDescription = null)
            }
        )
    }
    if (openHelp) {
        SettingsHelpDialog { openHelp = false }
    }
}

@Composable
private fun SettingsHelpDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { onDismiss() },
        icon = { Icon(Icons.Outlined.Group, contentDescription = null) },
        title = {
            Text(text = "Получение доступа")
        },
        text = {
            val annotatedString = getHelpText(withTitle = false)
            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(
                        tag = "email",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let {
                        context.launchMailClientIntent(it.item)
                    }

                    annotatedString.getStringAnnotations(
                        tag = "phone",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let {
                        context.launchDialer(it.item)
                    }
                },
                style = TextStyle(textAlign = TextAlign.Center)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text("Понятно")
            }
        }
    )
}

@Composable
private fun SettingsAccLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 96.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun SettingsNoSavedAcc() {
    val context = LocalContext.current
    val annotatedString = getHelpText(withTitle = true)
    OutlinedCard(
        onClick = { },
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 2.dp),
        enabled = false
    ) {
        ClickableText(
            text = annotatedString,
            onClick = { offset ->
                annotatedString.getStringAnnotations(
                    tag = "email",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let {
                    context.launchMailClientIntent(it.item)
                }

                annotatedString.getStringAnnotations(
                    tag = "phone",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let {
                    context.launchDialer(it.item)
                }
            },
            style = TextStyle(textAlign = TextAlign.Center),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun getHelpText(
    withTitle: Boolean = false
): AnnotatedString {
    return buildAnnotatedString {
        if (withTitle) {
            withStyle(style = MaterialTheme.typography.titleLarge.toSpanStyle()) {
                append("Получение доступа\n")
            }
            withStyle(style = SpanStyle(fontSize = 3.sp)) { append("\n") }
        }
        withStyle(
            style = MaterialTheme.typography.titleMedium.toSpanStyle()
                .copy(fontWeight = FontWeight.Normal)
        ) {
            append("Для получения доступа к IP-телефонии НИЯУ МИФИ вам необходимо оставить заявку\n")
            withStyle(style = SpanStyle(fontSize = 2.sp)) { append("\n") }

            append("Почта: ")
            pushStringAnnotation(tag = "email", annotation = "voip@mephi.ru")
            withStyle(
                style = MaterialTheme.typography.titleMedium.toSpanStyle()
                    .copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Normal
                    )
            ) {
                append("voip@mephi.ru")
            }
            pop()

            append("\nТелефон: ")
            pushStringAnnotation(tag = "phone", annotation = "+74957885699")
            withStyle(
                style = MaterialTheme.typography.titleMedium.toSpanStyle()
                    .copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Normal
                    )
            ) {
                append("+74957885699, доб. 7777")
            }
            pop()
        }
    }
}

@Composable
private fun SettingsAccNotSelected() {
    val annotatedString = buildAnnotatedString {
        withStyle(style = MaterialTheme.typography.titleLarge.toSpanStyle()) {
            append("Аккаунт не выбран\n")
        }
        withStyle(style = SpanStyle(fontSize = 3.sp)) { append("\n") }
        withStyle(
            style = MaterialTheme.typography.titleMedium.toSpanStyle()
                .copy(fontWeight = FontWeight.Normal)
        ) {
            append("Выберите аккаунт из списка уже добавленных аккаунтов или добавьте новый")
        }
    }
    OutlinedCard(
        onClick = { },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        enabled = false
    ) {
        ClickableText(
            text = annotatedString,
            onClick = { },
            style = TextStyle(textAlign = TextAlign.Center),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
        )
    }
}
