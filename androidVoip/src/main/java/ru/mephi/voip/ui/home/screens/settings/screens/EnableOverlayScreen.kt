@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.settings.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import ru.mephi.voip.ui.home.screens.settings.screens.common.items.SelectionButtonItem
import ru.mephi.voip.ui.home.screens.settings.screens.common.items.SettingsTopBar
import ru.mephi.voip.ui.settings.SettingsViewModel
import ru.mephi.voip.utils.launchDialer
import ru.mephi.voip.utils.launchMailClientIntent
import timber.log.Timber

@Composable
fun EnableOverlayScreen(
    goBack: () -> Unit,
    sVM: SettingsViewModel = get()
) {
    val context = LocalContext.current
    val uiState = sVM.uiState.collectAsState()
    val startForResult = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        sVM.enableCallScreenAlways(Settings.canDrawOverlays(context))
    }
    val requestIntent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    )
    Scaffold(
        topBar = { SettingsTopBar(title = "Экран входящего вызова", goBack = goBack) }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column {
                val isSelected = uiState.value.isCallScreenAlwaysEnabled
                SelectionButtonItem(
                    isSelected = isSelected,
                    onClick = { if (!isSelected) {
                        if (!Settings.canDrawOverlays(context)) {
                            startForResult.launch(requestIntent)
                        } else {
                            sVM.enableCallScreenAlways(true)
                        }
                    } },
                    title = "Разрешить"
                )
                SelectionButtonItem(
                    isSelected = !isSelected,
                    onClick = { if (isSelected) sVM.enableCallScreenAlways(false) },
                    title = "Запретить"
                )
                Divider(
                    color = TopAppBarDefaults.smallTopAppBarColors().containerColor(
                        scrollFraction = 1.0f
                    ).value,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 10.dp)
                )
                val annotatedString = buildAnnotatedString {
                    withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle()) {
                        append("Чтобы отображать экран входящего вызова приложению необходимо разрешение на рисование поверх других окон. Выключено по умолчанию.\n")
                    }

                    pushStringAnnotation(tag = "permission", annotation = "")
                    withStyle(
                        style = MaterialTheme.typography.bodyMedium.toSpanStyle()
                            .copy(color = MaterialTheme.colorScheme.secondary)
                    ) {
                        append("Нажмите здесь, чтобы отозвать разрешение.")
                    }
                    pop()
                }
                ClickableText(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 10.dp),
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(
                            tag = "permission",
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let {
                            startForResult.launch(requestIntent)
                        }
                    }
                )
            }
        }
    }
    BackHandler(true) {
        goBack()
    }
}