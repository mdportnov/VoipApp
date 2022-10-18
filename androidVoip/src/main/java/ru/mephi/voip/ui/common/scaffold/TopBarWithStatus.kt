@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.common.scaffold

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.mephi.voip.ui.common.SipStatusActionButton
import ru.mephi.voip.ui.common.paddings.StatusBarPadding

@Composable
fun TopBarWithStatus(
    title: String
) {
    Column {
        StatusBarPadding()
        TopAppBar(
            title = {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            actions = { SipStatusActionButton() },
            windowInsets = MutableWindowInsets(),
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            )
        )
    }
}