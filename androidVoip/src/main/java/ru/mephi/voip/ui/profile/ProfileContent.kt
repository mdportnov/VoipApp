package ru.mephi.voip.ui.profile

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import org.koin.androidx.compose.inject
import ru.mephi.shared.appContext
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.profile.bottomsheet.BottomSheetScreen
import ru.mephi.voip.ui.settings.SettingsActivity
import ru.mephi.voip.utils.*

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ProfileContent(openSheet: (BottomSheetScreen) -> Unit, navController: NavController) {
    val viewModel by inject<ProfileViewModel>()
    val accountRepository by inject<AccountStatusRepository>()
    val hapticFeedback = LocalHapticFeedback.current
    val localContext = LocalContext.current
    val painter = rememberImagePainter(
        data = viewModel.getImageUrl(),
        builder = {
            crossfade(true)
            diskCachePolicy(CachePolicy.ENABLED)
            memoryCachePolicy(CachePolicy.ENABLED)
        }
    )

    val accountsCount by accountRepository.accountsCount.collectAsState()
    val accountStatus by accountRepository.status.collectAsState(initial = AccountStatus.CHANGING)
    val isSipEnabled by accountRepository.isSipEnabled.collectAsState()
    val name by accountRepository.displayName.collectAsState(NameItem())

    val switchColor = if (isSipEnabled)
        ColorAccent else ColorGreen

    Scaffold(
        topBar = {
            TopAppBar(backgroundColor = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = { }) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_mephi),
                            contentDescription = "лого",
                        )
                    }
                    Text(
                        text = "Профиль", style = TextStyle(color = Color.Black, fontSize = 20.sp),
                    )
                    IconButton(
                        modifier = Modifier,
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            appContext.startActivity(
                                Intent(
                                    appContext,
                                    SettingsActivity::class.java
                                ).apply { flags = FLAG_ACTIVITY_NEW_TASK }
                            )
                        }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Настройки"
                        )
                    }
                }
            }
        },
        content = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
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

                    if (accountsCount > 0)
                        Box(
                            modifier = Modifier
                                .size(120.dp)
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
                                is ImagePainter.State.Loading -> {
                                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                                }
                                is ImagePainter.State.Error -> {
                                    // If you wish to display some content if the request fails
                                }
                                else -> {}
                            }

                            IconButton(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(
                                        colorResource(id = R.color.colorPrimary),
                                        CircleShape
                                    ),
                                onClick = {
                                    if (accountStatus != AccountStatus.REGISTERED) {
                                        if (viewModel.accountRepository.isSipEnabled.value) {
                                            hapticFeedback.performHapticFeedback(
                                                HapticFeedbackType.LongPress
                                            )
                                            viewModel.retryRegistration()
                                        }
//                                        else // TODO
//                                            showSnackBar(
//                                                localContext.findViewById(R.id.main_container),
//                                                "Включите SIP"
//                                            )
                                    }
                                }
                            ) {
                                Icon(
                                    when (accountStatus) {
                                        AccountStatus.REGISTERED, AccountStatus.NO_CONNECTION ->
                                            Icons.Filled.CheckCircle
                                        AccountStatus.UNREGISTERED, AccountStatus.REGISTRATION_FAILED,
                                        AccountStatus.CHANGING, AccountStatus.LOADING ->
                                            Icons.Filled.Refresh
                                    }, "Статус",
                                    tint = when (accountStatus) {
                                        AccountStatus.REGISTERED -> colorResource(id = R.color.colorGreen)
                                        AccountStatus.UNREGISTERED, AccountStatus.REGISTRATION_FAILED ->
                                            colorResource(id = R.color.colorAccent)
                                        AccountStatus.NO_CONNECTION, AccountStatus.CHANGING,
                                        AccountStatus.LOADING -> colorResource(id = R.color.colorGray)
                                    },
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 0.dp, 20.dp, 0.dp),
                    horizontalAlignment = Alignment.Start
                ) {

                    if (accountsCount > 0)
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text =
                                if (isSipEnabled) stringResource(R.string.disable_sip)
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
                                    checkedThumbColor = switchColor,
                                    uncheckedThumbColor = switchColor
                                ),
                            )
                        }

                    if (!name?.display_name.isNullOrEmpty())
                        Text(
                            fontSize = with(LocalDensity.current) {
                                (dimensionResource(id = R.dimen.profile_text_size).value.sp / fontScale)
                            },
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Left,
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = colorResource(id = R.color.colorAccent))) {
                                    append("Имя: ")
                                }
                                append(name!!.display_name)
                            }
                        )

                    viewModel.phone.getCurrentUserNumber()?.let {
                        Text(
                            fontSize = with(LocalDensity.current) {
                                (dimensionResource(id = R.dimen.profile_text_size).value.sp / fontScale)
                            },
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Left,
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = colorResource(id = R.color.colorAccent))) {
                                    append("Номер SIP: ")
                                }
                                append(it)
                            }
                        )
                    }

                    Text(
                        fontSize = with(LocalDensity.current) {
                            (dimensionResource(id = R.dimen.profile_text_size).value.sp / fontScale)
                        },
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Left,
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = colorResource(id = R.color.colorAccent))) {
                                append("Статус: ")
                            }
                            append(accountStatus.status)
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (accountsCount == 0) {
                        val annotatedText = getAnnotatedText()
                        ClickableText(
                            text = annotatedText,
                            onClick = { offset ->
                                annotatedText.getStringAnnotations(
                                    tag = "email",
                                    start = offset,
                                    end = offset
                                ).firstOrNull()?.let { annotation ->
                                    localContext.launchMailClientIntent(annotation.item)
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }

                                annotatedText.getStringAnnotations(
                                    tag = "phone",
                                    start = offset,
                                    end = offset
                                ).firstOrNull()?.let { annotation ->
                                    localContext.launchDialer(annotation.item)
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize(), verticalArrangement = Arrangement.Bottom
            ) {
                if (accountsCount > 0)
                    ExtendedFloatingActionButton(
                        icon = { Icon(Icons.Filled.Edit, "", tint = Color.White) },
                        text = {
                            Text(
                                text = stringResource(R.string.change_account) + " (${accountsCount})",
                                color = Color.White
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(16.dp, 16.dp, 16.dp, 0.dp),
                        backgroundColor = colorResource(id = R.color.colorGreen),
                        onClick = {
                            openSheet(BottomSheetScreen.ScreenChangeAccount)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

//                    navController.navigate(
//                        R.id.action_navigation_profile_to_settingsFragment,
//                    )
                        }
                    )
                ExtendedFloatingActionButton(
                    icon = { Icon(Icons.Filled.Add, "", tint = Color.White) },
                    text = {
                        Text(
                            text = stringResource(R.string.add_new_account),
                            color = Color.White
                        )
                    },
                    backgroundColor = colorResource(id = R.color.colorGreen),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp, 8.dp, 16.dp, 16.dp),
                    onClick = {
                        openSheet(BottomSheetScreen.ScreenAddNewAccount)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
            }
        }
    )
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
        ),
        start = 0,
        end = 74
    )

    addStringAnnotation(
        "email",
        stringResource(R.string.support_email),
        start = 74,
        end = 88
    )

    withStyle(
        style = SpanStyle(
            color = colorResource(id = R.color.colorPrimaryDark),
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
        ),
        start = 74,
        end = 88 // ru.mephi.voip@mephi.ru
    )

    addStyle(
        style = ParagraphStyle(
            textAlign = TextAlign.Center
        ),
        start = 88,
        end = 102 // или по номеру
    )

    withStyle(
        style = SpanStyle(fontSize = dimensionResource(id = R.dimen.profile_text_size).value.sp)
    ) {
        append(" или по номеру ")
    }

    addStringAnnotation(
        "phone",
        "+74957885699, 7777",
        start = 102,
        end = 131
    )

    withStyle(
        style = SpanStyle(
            color = colorResource(id = R.color.colorPrimaryDark),
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
        ),
        start = 102,
        end = 131 // +7 (495) 788 56 99, доб. 7777
    )
}