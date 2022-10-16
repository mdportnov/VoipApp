@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.koin.androidx.compose.get
import ru.mephi.voip.data.PhoneManager
import ru.mephi.voip.ui.MasterActivity
import ru.mephi.voip.ui.common.AccountCard

@Composable
internal fun ManageAccountsDialog(
    onDismiss: () -> Unit,
    openLoginDialog: () -> Unit,
    phoneManager: PhoneManager = get()
) {
    val activity = LocalContext.current as MasterActivity
    val accountsList by phoneManager.accountsList.collectAsState()
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
        ) {
            Scaffold(
                modifier = Modifier
                    .padding(top = 34.dp, bottom = 42.dp, start = 10.dp, end = 10.dp)
                    .background(color = Color.Transparent)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = false) { },
                topBar = {
                    TopAppBar(
                        title = { Text(text = "Управление аккаунтами") },
                        navigationIcon = {
                            IconButton(onClick = { onDismiss() }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                            }
                        }
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        text = { Text("Добавить аккаунт") },
                        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                        onClick = { if (activity.checkNonGrantedPermissions()) openLoginDialog() })
                }
            ) {
                Box(
                    modifier = Modifier.padding(it)
                ) {
                    LazyColumn(modifier = Modifier.padding(horizontal = 6.dp)) {
                        itemsIndexed(items = accountsList) { _, item ->
                            AccountCard(
                                displayedName = item.displayedName,
                                sip = item.login,
                                sipDescription = "Номер:",
                                status = when(item.isActive) {
                                    true -> "Выбранный аккаунт"
                                    false -> "Неактивный аккаунт"
                                },
                                onClick = {
                                    if (!item.isActive) {
                                        phoneManager.setActiveAccount(item)
                                    }
                                    onDismiss()
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            phoneManager.removeAccount(item)
                                            onDismiss()
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
