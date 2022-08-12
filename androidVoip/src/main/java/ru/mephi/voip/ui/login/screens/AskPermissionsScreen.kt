@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package ru.mephi.voip.ui.login.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Composable
internal fun AskPermissionsScreen(
    goBack: () -> Unit,
    goNext: () -> Unit
) {
    val height = LocalConfiguration.current.screenHeightDp
    val permissions = rememberMultiplePermissionsState(permissions = listOf(Manifest.permission.USE_SIP, Manifest.permission.RECORD_AUDIO))
    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .padding(vertical = 28.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { }
                ) {
                    Text(
                        text = "Пропустить",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                OutlinedButton(
                    onClick = { permissions.launchMultiplePermissionRequest() }
                ) {
                    Text(
                        text = "Предоставить",
                        style = MaterialTheme.typography.labelLarge
                    )
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
            Text(
                text = "Предоставьте разрешения",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(start = 22.dp)
                    .wrapContentSize()
                    .align(Alignment.BottomStart)
            )
        }
    }
}