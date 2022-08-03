package ru.mephi.voip.ui.home.screens.catalog.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun SearchHistoryItem(
    searchStr: String,
    applyStr: (searchStr: String) -> Unit,
    runSearch: (searchStr: String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clickable { runSearch(searchStr) }
    ) {
        IconButton(onClick = { runSearch(searchStr) }) {
            Icon(imageVector = Icons.Default.History, contentDescription = null)
        }
        Text(
            text = searchStr,
            modifier = Modifier
                .wrapContentHeight()
                .width((LocalConfiguration.current.screenWidthDp - 128).dp),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = { applyStr(searchStr) }) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
        }
    }
}