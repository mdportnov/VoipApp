@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.settings.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material.icons.outlined.PlaylistRemove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import ru.mephi.voip.ui.home.screens.settings.screens.common.items.SettingsItem
import ru.mephi.voip.ui.home.screens.settings.screens.common.items.SettingsTopBar
import ru.mephi.voip.ui.settings.SettingsViewModel

@Composable
fun ClearDataScreen(
    goBack: () -> Unit,
    sVM: SettingsViewModel = get()
) {
    val context = LocalContext.current
    Scaffold(
        topBar = { SettingsTopBar(title = "Очистить данные", goBack = goBack) }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column {
                SettingsItem(icon = Icons.Outlined.DeleteSweep, title = "Удалить историю запросов") {
                    sVM.deleteAllSearchRecords()
                    Toast.makeText(context, "История поиска удалена", Toast.LENGTH_SHORT).show()
                }
                SettingsItem(icon = Icons.Outlined.PlaylistRemove, title = "Удалить кэш католога") {
                    sVM.deleteAllCatalogCache()
                    Toast.makeText(context, "Кэш каталога удалён", Toast.LENGTH_SHORT).show()
                }
                SettingsItem(icon = Icons.Outlined.PersonRemove, title = "Удалить избранные контакты") {
                    sVM.deleteAllFavouritesRecords()
                    Toast.makeText(context, "Избранные контакты удалены", Toast.LENGTH_SHORT).show()
                }
                Divider(
                    color = TopAppBarDefaults.smallTopAppBarColors().containerColor(
                        scrollFraction = 1.0f
                    ).value,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 10.dp)
                )
                Text(
                    text = "Вы можете очистить данные, которые сохраняет приложения для облегчения работы пользователя или экономии данных.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
        }
    }
    BackHandler(true) {
        goBack()
    }
}