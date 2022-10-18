package ru.mephi.voip.ui.screens.favourites.menus

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import ru.mephi.voip.data.FavouritesRepository
import ru.mephi.voip.entities.preview.FavouritePreview

@Composable
fun FavouriteMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    favourite: FavouritePreview,
    favouritesRepo: FavouritesRepository = get()
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(0.dp, (-46).dp)
    ) {
        DropdownMenuItem(
            text = { Text(text = "Позвонить") },
            leadingIcon = { Icon(imageVector = Icons.Outlined.Call, contentDescription = null) },
            onClick = {  }
        )
        DropdownMenuItem(
            text = { Text(text = "Удалить") },
            leadingIcon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = null) },
            onClick = {
                onDismissRequest()
                favouritesRepo.removeFavourite(favourite.phoneNumber)
            }
        )
        DropdownMenuItem(
            text = { Text(text = "Контакт") },
            leadingIcon = { Icon(imageVector = Icons.Outlined.Person, contentDescription = null) },
            onClick = { }
        )
    }
}