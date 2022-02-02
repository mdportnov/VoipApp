package ru.mephi.voip.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val BottomSheetShape = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomEnd = 0.dp,
    bottomStart = 0.dp
)

@Composable
fun BottomSheetWithCloseDialog(
    onClosePressed: () -> Unit,
    modifier: Modifier = Modifier,
    closeButtonColor: Color = Color.Gray,
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        Box(modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            IconButton(
                onClick = onClosePressed,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(29.dp)
            ) {
                Icon(Icons.Filled.Close, tint = closeButtonColor, contentDescription = null)
            }
        }

        content()
    }
}