@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.settings.screens.common.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import ru.mephi.voip.R
import ru.mephi.voip.utils.getImageUrl

@Composable
fun AccountCard(
    displayedName: String = "",
    sip: String = "",
    status: String = "",
    onClick: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getImageUrl(sip.filter { it.isDigit() }))
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                error = painterResource(id = R.drawable.ic_dummy_avatar),
                placeholder = painterResource(id = R.drawable.ic_dummy_avatar),
                contentDescription = null,
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 12.dp)
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxHeight()
                        .weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = displayedName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = sip,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(IntrinsicSize.Max)
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.secondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                trailingIcon?.invoke()
            }
        }
    }
}
