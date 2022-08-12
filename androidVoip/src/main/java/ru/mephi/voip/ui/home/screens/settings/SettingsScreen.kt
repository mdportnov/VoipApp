@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MenuOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import org.koin.androidx.compose.get
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.common.CommonColor
import ru.mephi.voip.ui.home.screens.settings.items.ClickPreference
import ru.mephi.voip.ui.settings.SettingsViewModel

@Composable
internal fun SettingsScreen(
    addAccount: () -> Unit,
    switchAccount: () -> Unit,
    sVM: SettingsViewModel = get()
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        CurrentAccountCard(addAccount, switchAccount)
//        CachePreferences(
//            removeCatalogCache = sVM::deleteAllCatalogCache,
//            removeSearchHistory = sVM::deleteAllSearchRecords,
//            removeAllFavourites = sVM::deleteAllFavouritesRecords
//        )
//        ru.mephi.voip.ui.settings.SettingsScreen()
    }
}

@Composable
private fun CurrentAccountCard(
    addAccount: () -> Unit,
    switchAccount: () -> Unit,
    accountRepository: AccountStatusRepository = get()
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(top = 4.dp)
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
                    .data("https://voip.mephi.ru/public/photos/350387_1639658360190215130_Portnov%20M%20D_Портнов-2.jpeg")
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
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
                    text = "Михаил Портнов",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val tmp = accountRepository.currentAccount.collectAsState()
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = MaterialTheme.typography.labelLarge.toSpanStyle()) {
                            append("Ваш номер: ")
                        }
                        withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle()) {
                            append(tmp.value.login)
                        }
                    },
                    maxLines = 1,
                    overflow  = TextOverflow.Ellipsis
                )
                val accountStatus by accountRepository.phoneStatus.collectAsState("")
                Text(
                    text = "Статус: $accountStatus",
                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.secondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.CenterStart)) {
                IconButton(
                    onClick = { expanded = true }
                ) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Добавить аккаунт") },
                        onClick = { addAccount() },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = null
                            )
                        })
                    DropdownMenuItem(
                        text = { Text("Сменить аккаунт") },
                        onClick = { switchAccount() },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.MenuOpen,
                                contentDescription = null
                            )
                        })
                    DropdownMenuItem(
                        text = { Text("Выйти") },
                        onClick = {  },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Logout,
                                contentDescription = null
                            )
                        })
                }
            }
        }
    }
}

@Composable
private fun CachePreferences(
    removeCatalogCache: () -> Unit,
    removeSearchHistory: () -> Unit,
    removeAllFavourites: () -> Unit
) {
    Column(modifier = Modifier.wrapContentHeight()) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp, start = 2.dp),
            text = "Память",
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Divider(
            color = CommonColor(),
            thickness = 0.8.dp,
            modifier = Modifier.fillMaxWidth()
        )
        ClickPreference(
            title = "Очистить кэш каталога",
            description = "",
            onClick = removeCatalogCache
        )
        ClickPreference(
            title = "Очистить историю запросов",
            description = "",
            onClick = removeSearchHistory
        )
        ClickPreference(
            title = "Очистить список избранных",
            description = "",
            onClick = removeAllFavourites
        )
    }
}
