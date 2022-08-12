@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.login.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import ru.mephi.voip.data.InitDataStore
import ru.mephi.voip.data.InitRequirement

@Composable
internal fun HelloScreen(
    skipInit: () -> Unit,
    goNext: () -> Unit,
    initDataStore: InitDataStore = get()
) {
    val scope = rememberCoroutineScope()
    val height = LocalConfiguration.current.screenHeightDp
    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .padding(vertical = 28.dp, horizontal = 34.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            initDataStore.setInitRequirement(InitRequirement.SKIP_INIT)
                        }
                        skipInit()
                    }
                ) {
                    Text(
                        text = "Пропустить",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                OutlinedButton(
                    onClick = { goNext() }
                ) {
                    Text(
                        text = "Войти",
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
                text = "Добро пожаловать!",
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
