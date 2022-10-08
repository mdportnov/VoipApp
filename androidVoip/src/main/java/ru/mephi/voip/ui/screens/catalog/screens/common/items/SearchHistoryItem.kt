package ru.mephi.voip.ui.screens.catalog.screens.common.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.mephi.shared.vm.SearchType

@Composable
internal fun SearchHistoryItem(
    searchStr: String,
    searchType: SearchType,
    applySearchStr: (TextFieldValue) -> Unit,
    runSearch: (String, SearchType) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable { runSearch(searchStr, searchType) }
    ) {
        IconButton(onClick = { runSearch(searchStr, searchType) }) {
            Icon(imageVector = Icons.Default.History, contentDescription = null)
        }
        Text(
            text = searchStr,
            modifier = Modifier
                .wrapContentHeight()
                .weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = { applySearchStr(
            TextFieldValue(text = searchStr, selection = TextRange(searchStr.length))
        ) }) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
        }
    }
}