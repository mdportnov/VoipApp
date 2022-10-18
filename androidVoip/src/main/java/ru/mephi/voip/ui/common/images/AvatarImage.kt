package ru.mephi.voip.ui.common.images

import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import ru.mephi.voip.R

@Composable
fun AvatarImage(
    modifier: Modifier = Modifier,
    avatarUrl: String,
    size: Int,
    context: Context = LocalContext.current,
) {
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(avatarUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build(),
        modifier = modifier
            .clip(CircleShape)
            .size(size.dp),
        contentScale = ContentScale.Crop,
        error = painterResource(id = R.drawable.ic_dummy_avatar),
        placeholder = painterResource(id = R.drawable.ic_dummy_avatar),
        contentDescription = null
    )
}
