package ru.mephi.voip.ui.catalog.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.shared.appContext
import ru.mephi.shared.data.model.UnitM
import ru.mephi.voip.data.CatalogRepository
import ru.mephi.voip.ui.catalog.CatalogViewModel
import ru.mephi.voip.utils.ColorAccent
import ru.mephi.voip.utils.ColorGray
import ru.mephi.voip.utils.isOnline

@Composable
fun UnitCatalogItem(record: UnitM, viewModel: CatalogViewModel) {
    val textSize = when {
        record.name.length > 280 -> 8F.sp
        record.name.length > 200 -> 15.sp
        record.name.length > 80 -> 18.sp
        else -> 20.sp
    }

    Column {
        TextButton(onClick = {
            if (isOnline(appContext)) {
                viewModel.goNext(record.code_str)
            } else {
                if (viewModel.isExistsInDatabase(record.code_str)) {
                    viewModel.goNext(record.code_str)
                }
            }
        }) {
            Text(
                text = record.fullname,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    color =
                    when {
                        isOnline(appContext) -> ColorGray
                        viewModel.isExistsInDatabase(record.code_str) -> ColorGray
                        else -> Color.LightGray
                    }, fontSize = textSize
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
            )
        }

        record.parent_name?.let { name ->
            TextButton(onClick = { record.parent_code?.let { code -> viewModel.goNext(code) } }) {
                Text(
                    text = name,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    style = TextStyle(color = ColorAccent, fontSize = 12.sp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun UnitCatalogItemPreview() =
    UnitCatalogItem(
        record = UnitM(
            "",
            "",
            "Отдел ИИКС",
            "",
            "",
            "Управление эксплуатации и развития имущественного комплекса",
            arrayListOf(),
            arrayListOf(),
            1
        ),
        viewModel = CatalogViewModel(CatalogRepository())
    )
