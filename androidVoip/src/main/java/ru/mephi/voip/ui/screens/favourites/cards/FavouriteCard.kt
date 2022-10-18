package ru.mephi.voip.ui.screens.favourites.cards

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.voip.entities.preview.FavouritePreview
import ru.mephi.voip.ui.common.images.AvatarImage
import ru.mephi.voip.ui.screens.favourites.menus.FavouriteMenu

@Composable
internal fun FavouriteCard(
    favourite: FavouritePreview
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(vertical = 12.dp, horizontal = 4.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { expanded = true },
                        onTap = { }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarImage(
                avatarUrl = favourite.avatarUrl,
                size = 108
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = favourite.displayedName,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = favourite.displayedPhoneNumber,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        FavouriteMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            favourite = favourite
        )
    }
}
