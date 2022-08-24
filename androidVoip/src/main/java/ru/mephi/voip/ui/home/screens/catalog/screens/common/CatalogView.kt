package ru.mephi.voip.ui.home.screens.catalog.screens.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ru.mephi.shared.vm.CatalogStatus
import ru.mephi.shared.vm.CatalogUtils

@Composable
internal fun CatalogView(
    codeStr: String,
    status: CatalogStatus,
    catalogList: @Composable () -> Unit
) {
    when (status) {
        CatalogStatus.OK -> {
            catalogList()
        }
        CatalogStatus.LOADING -> {
            OnLoading()
        }
        CatalogStatus.NOT_FOUND -> {
            if (codeStr == CatalogUtils.INIT_CODE_STR) {
                OnNetworkFailure()
            } else {
                OnNotFound()
            }
        }
        CatalogStatus.NETWORK_FAILURE -> {
            OnNetworkFailure()
        }
    }
}

@Composable
internal fun OnLoading() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}


@Composable
private fun OnNotFound() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(128.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = buildAnnotatedString {
                withStyle(
                    style = MaterialTheme.typography.headlineMedium.toSpanStyle()
                        .copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    append("Ничего не найдено!")
                }
            })
        }
    }
}

@Composable
internal fun OnNetworkFailure() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                modifier = Modifier.size(128.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = buildAnnotatedString {
                withStyle(
                    style = MaterialTheme.typography.headlineMedium.toSpanStyle()
                        .copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    append("Нет подключения!")
                }
            })
        }
    }
}