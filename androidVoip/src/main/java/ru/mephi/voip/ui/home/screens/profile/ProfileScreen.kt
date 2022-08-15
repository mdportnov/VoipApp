@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import ru.mephi.shared.vm.SavedAccountsViewModel
import ru.mephi.voip.R
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.home.screens.profile.common.dialogs.ManageAccountsDialog
import ru.mephi.voip.ui.profile.ProfileViewModel
import ru.mephi.voip.ui.profile.favourites.FavouriteContactsBoard
import ru.mephi.voip.ui.profile.getAnnotatedText
import ru.mephi.voip.utils.getImageUrl
import ru.mephi.voip.utils.launchDialer
import ru.mephi.voip.utils.launchMailClientIntent

@Composable
internal fun ProfileScreen(
    openSettings: () -> Unit,
    openLogin: () -> Unit,
    accountRepository: AccountStatusRepository = get(),
) {
    val scope = rememberCoroutineScope()
    var openManager by remember { mutableStateOf(false) }
    val list = accountRepository.accountsList.collectAsState()
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
            if (list.value.isNotEmpty()) {
                CurrentAccountCard()
                PhoneButton()
                FavouriteContactsBoard(
                    modifier = Modifier.fillMaxWidth(),
                    profileViewModel = get(),
                    accountStatusRepository = get()
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
private fun CurrentAccountCard(
    accountRepository: AccountStatusRepository = get(),
    savedAccountsVM: SavedAccountsViewModel = get()
) {
    val account = savedAccountsVM.currentAccount.collectAsState()
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(top = 4.dp, start = 12.dp, end = 12.dp)
            .wrapContentHeight()
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getImageUrl(account.value.lineShown))
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                error = painterResource(id = R.drawable.ic_dummy_avatar),
                placeholder = painterResource(id = R.drawable.ic_dummy_avatar),
                contentDescription = null,
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 12.dp)
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .width((LocalConfiguration.current.screenWidthDp - 148).dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text ="${account.value.firstname} ${account.value.lastname}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val currentAccount = accountRepository.currentAccount.collectAsState()
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = MaterialTheme.typography.labelLarge.toSpanStyle()) {
                            append("Ваш номер: ")
                        }
                        withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle()) {
                            append(currentAccount.value.login)
                        }
                    },
                    maxLines = 1,
                    overflow  = TextOverflow.Ellipsis
                )
                val accountStatus by accountRepository.phoneStatus.collectAsState()
                Text(
                    text = "Статус: ${accountStatus.status}",
                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.secondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PhoneButton(
    viewModel: ProfileViewModel = get(),
    accountRepository: AccountStatusRepository = get()
) {
    val sipStatus = accountRepository.isSipEnabled.collectAsState()
    val currentAccount = accountRepository.currentAccount.collectAsState()
    Button(
        onClick = { viewModel.toggleSipStatus() },
        modifier = Modifier
            .padding(top = 4.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)
            .wrapContentHeight()
            .fillMaxWidth(),
        enabled = currentAccount.value.login.isNotEmpty()
    ) {
        Text(if (!sipStatus.value) "Включить SIP" else "Выключить SIP")
    }
}

