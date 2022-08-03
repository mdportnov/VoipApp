package ru.mephi.voip.ui.home.screens.catalog.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.shared.data.model.SearchRecord
import ru.mephi.voip.ui.home.screens.catalog.HistorySearchModelState

@Composable
fun SearchRecordsList(state: HistorySearchModelState, onClick: (SearchRecord) -> Unit) {
    state.historyRecords.forEach { record ->
        SearchRecordItem(record = record) {
            onClick(record)
        }
        Divider()
    }
}

@Composable
fun SearchRecordItem(record: SearchRecord, onClick: () -> Unit) {
//    Text(
//        record.name, modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//            .clickable { onClick() }, fontSize = 20.sp, fontWeight = FontWeight.Bold
//    )
}