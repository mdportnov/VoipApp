package ru.mephi.voip.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import org.koin.androidx.compose.inject
import ru.mephi.shared.data.model.NameItem
import ru.mephi.voip.R
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.components.AccountStatusWidget
import ru.mephi.voip.utils.*

@Composable
fun ProfileContent(modifier: Modifier) {
    val viewModel: ProfileViewModel by inject()
    val accountRepository: AccountStatusRepository by inject()
    val hapticFeedback = LocalHapticFeedback.current
    val localContext = LocalContext.current
    val scaffoldState = rememberScaffoldState()

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current).data(data = viewModel.imageUrl)
            .apply(block = fun ImageRequest.Builder.() {
                crossfade(true)
                diskCachePolicy(CachePolicy.ENABLED)
                memoryCachePolicy(CachePolicy.ENABLED)
            }).build()
    )

    val accountsCount by accountRepository.accountsCount.collectAsState()
    val activeAccount by accountRepository.activeAccount.collectAsState()
    val accountStatus by accountRepository.status.collectAsState()
    val isSipEnabled by accountRepository.isSipEnabled.collectAsState(initial = false)
    val name by accountRepository.displayName.collectAsState(NameItem())
    val switchColor = if (isSipEnabled) ColorAccent else ColorGreen

    Column(
        modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentHeight()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_mephi),
                contentDescription = "лого"
            )

            if (activeAccount != null) Box(
                modifier = Modifier.size(120.dp)
            ) {
                Image(
                    painter = painter,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(100.dp)
                        .align(Alignment.Center)
                )

                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                    else -> {}
                }

                AccountStatusWidget(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    accountStatusRepository = accountRepository,
                    scaffoldState = scaffoldState
                )
            }
        }

        Column(horizontalAlignment = Alignment.Start, modifier = modifier) {
            if (activeAccount != null) Column {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSipEnabled) stringResource(R.string.disable_sip)
                        else stringResource(R.string.enable_sip),
                        color = switchColor,
                        fontSize = with(LocalDensity.current) {
                            (dimensionResource(id = R.dimen.profile_text_size).value.sp / fontScale)
                        },
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Switch(
                        checked = isSipEnabled,
                        onCheckedChange = {
                            viewModel.toggleSipStatus()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = switchColor, uncheckedThumbColor = switchColor
                        ),
                    )
                }
                if (!name?.display_name.isNullOrEmpty()) Text(fontSize = with(LocalDensity.current) {
                    (dimensionResource(id = R.dimen.profile_text_size).value.sp / fontScale)
                },
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Left,
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(ColorAccent)) {
                            append("Имя: ")
                        }
                        append(name!!.display_name)
                    })

                viewModel.getCurrentUserNumber()?.let {
                    Text(fontSize = with(LocalDensity.current) {
                        (dimensionResource(id = R.dimen.profile_text_size).value.sp / fontScale)
                    },
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Left,
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(ColorAccent)) {
                                append("Номер SIP: ")
                            }
                            append(it)
                        })
                }

                if (isSipEnabled)
                    Text(fontSize = with(LocalDensity.current) {
                        (dimensionResource(id = R.dimen.profile_text_size).value.sp / fontScale)
                    },
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Left,
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(ColorAccent)) {
                                append("Статус: ")
                            }
                            append(accountStatus.status)
                        })

                Spacer(modifier = Modifier.height(20.dp))
            }
            else if (accountsCount > 0) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(CenterHorizontally),
                    text = stringResource(id = R.string.no_active_account),
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = dimensionResource(id = R.dimen.profile_text_size).value.sp
                    )
                )
            }

            if (accountsCount == 0) {
                val annotatedText = getAnnotatedText()
                ClickableText(text = annotatedText, onClick = { offset ->
                    annotatedText.getStringAnnotations(
                        tag = "email", start = offset, end = offset
                    ).firstOrNull()?.let { annotation ->
                        localContext.launchMailClientIntent(annotation.item)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }

                    annotatedText.getStringAnnotations(
                        tag = "phone", start = offset, end = offset
                    ).firstOrNull()?.let { annotation ->
                        localContext.launchDialer(annotation.item)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                })
            }
        }
    }
}


@Composable
private fun getAnnotatedText() = buildAnnotatedString {
    withStyle(
        style = SpanStyle(
            color = Color.Black,
            fontSize = dimensionResource(id = R.dimen.profile_text_size).value.sp
        )
    ) {
        append("Для получения доступа к ip-телефонии НИЯУ МИФИ оставьте заявку письмом на ")
    }

    addStyle(
        style = ParagraphStyle(
            textAlign = TextAlign.Center
        ), start = 0, end = 74
    )

    addStringAnnotation(
        "email", stringResource(R.string.support_email), start = 74, end = 88
    )

    withStyle(
        style = SpanStyle(
            color = ColorPrimaryDark,
            fontWeight = FontWeight.SemiBold,
            fontSize = dimensionResource(id = R.dimen.profile_text_size).value.sp,
            textDecoration = TextDecoration.Underline,
        )
    ) {
        append("voip@mephi.ru")
    }

    addStyle(
        style = ParagraphStyle(
            textAlign = TextAlign.Center
        ), start = 74, end = 88 // ru.mephi.voip@mephi.ru
    )

    addStyle(
        style = ParagraphStyle(
            textAlign = TextAlign.Center
        ), start = 88, end = 102 // или по номеру
    )

    withStyle(
        style = SpanStyle(fontSize = dimensionResource(id = R.dimen.profile_text_size).value.sp)
    ) {
        append(" или по номеру ")
    }

    addStringAnnotation(
        "phone", "+74957885699, 7777", start = 102, end = 131
    )

    withStyle(
        style = SpanStyle(
            color = ColorPrimaryDark,
            fontWeight = FontWeight.SemiBold,
            fontSize = dimensionResource(id = R.dimen.profile_text_size).value.sp,
            textDecoration = TextDecoration.Underline,
        )
    ) {
        append("+7 (495) 788 56 99, доб. 7777")
    }

    addStyle(
        style = ParagraphStyle(
            textAlign = TextAlign.Center
        ), start = 102, end = 131 // +7 (495) 788 56 99, доб. 7777
    )
}