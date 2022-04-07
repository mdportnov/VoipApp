package ru.mephi.voip.ui.catalog.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.shared.data.model.UnitM
import ru.mephi.voip.data.CatalogRepository
import ru.mephi.voip.ui.catalog.NewCatalogViewModel
import ru.mephi.voip.utils.ColorAccent
import ru.mephi.voip.utils.ColorGray

@Composable
fun UnitCatalogItem(record: UnitM, viewModel: NewCatalogViewModel) {

    val textSize = when {
        record.name.length > 280 -> 8F.sp
        record.name.length > 200 -> 15.sp
        record.name.length > 80 -> 18.sp
        else -> 20.sp
    }

    Column {
        TextButton(onClick = { viewModel.goNext(record.code_str) }) {
            Text(
                text = record.fullname,
                textAlign = TextAlign.Center,
                style = TextStyle(color = ColorGray, fontSize = textSize),
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
        viewModel = NewCatalogViewModel(CatalogRepository())
    )
