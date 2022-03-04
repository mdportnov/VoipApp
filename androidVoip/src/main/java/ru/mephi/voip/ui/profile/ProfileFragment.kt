package ru.mephi.voip.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.mephi.shared.appContext
import ru.mephi.shared.data.model.Account
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.ui.MainActivity
import ru.mephi.voip.ui.settings.SettingsActivity
import ru.mephi.voip.utils.*
import timber.log.Timber

@ExperimentalComposeUiApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
class ProfileFragment : Fragment() {
    private val viewModel: ProfileViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ProfileScreen(findNavController())
            }
        }
    }

    //https://dev.to/davidibrahim/how-to-use-multiple-bottom-sheets-in-android-compose-382p
    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    @Composable
    fun ProfileScreen(navController: NavController) {
        val scope = rememberCoroutineScope()
        val scaffoldState = rememberBottomSheetScaffoldState()
        var currentBottomSheet: BottomSheetScreen? by remember {
            mutableStateOf(null)
        }

        if (scaffoldState.bottomSheetState.isCollapsed)
            currentBottomSheet = null

        // to set the current sheet to null when the bottom sheet closes
        if (scaffoldState.bottomSheetState.isCollapsed)
            currentBottomSheet = null

        val closeSheet: () -> Unit = {
            scope.launch {
                scaffoldState.bottomSheetState.collapse()
            }
        }

        val openSheet: (BottomSheetScreen) -> Unit = {
            scope.launch {
                currentBottomSheet = it
                scaffoldState.bottomSheetState.expand()
            }
        }

        BottomSheetScaffold(
            sheetElevation = 20.dp,
            sheetPeekHeight = 0.dp, scaffoldState = scaffoldState,
            sheetShape = BottomSheetShape,
            sheetContent = {
                currentBottomSheet?.let { currentSheet ->
                    SheetLayout(currentSheet, closeSheet, scaffoldState)
                }
            }) { paddingValues ->
            Box(Modifier.padding(paddingValues)) {
                MainContent(openSheet, navController)
            }
        }
    }

    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    @Composable
    fun SheetLayout(
        currentScreen: BottomSheetScreen,
        onCloseBottomSheet: () -> Unit,
        scaffoldState: BottomSheetScaffoldState
    ) {
        BottomSheetWithCloseDialog(
            onCloseBottomSheet, title =
            when (currentScreen) {
                BottomSheetScreen.ScreenAddNewAccount -> stringResource(id = R.string.add_new_account)
                BottomSheetScreen.ScreenChangeAccount -> stringResource(id = R.string.change_account)
            }
        ) {
            when (currentScreen) {
                BottomSheetScreen.ScreenAddNewAccount -> ScreenAddNewAccount(scaffoldState)
                BottomSheetScreen.ScreenChangeAccount -> ScreenChangeAccount(onCloseBottomSheet)
            }
        }
    }

    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    @Composable
    fun ScreenAddNewAccount(scaffoldState: BottomSheetScaffoldState) {
        var textLogin = viewModel.newLogin.value
        val textPassword = viewModel.newPassword.value
        val hapticFeedback = LocalHapticFeedback.current
        val maxNumberLength = 6
        val (focusRequester) = FocusRequester.createRefs()
        val keyboardController = LocalSoftwareKeyboardController.current
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RectangleShape)
                .height(400.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusRequester.requestFocus() }
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(id = R.color.colorPrimaryDark),
                    focusedLabelColor = colorResource(id = R.color.colorAccent),
                    cursorColor = colorResource(id = R.color.colorAccent),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                label = { Text("SIP USER ID") },
                value = textLogin,
                onValueChange = {
                    if (it.length <= maxNumberLength) {
                        textLogin = it
                        viewModel.onNewAccountInputChange(login = it)
                    } else
                        toast("Номер не может быть больше $maxNumberLength символов")
                }
            )

            var passwordVisibility by remember { mutableStateOf(false) }

            OutlinedTextField(
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(id = R.color.colorPrimaryDark),
                    focusedLabelColor = colorResource(id = R.color.colorAccent),
                    cursorColor = colorResource(id = R.color.colorAccent),
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth()
                    .padding(16.dp),
                label = { Text("SIP PASSWORD") },
                value = textPassword,
                onValueChange = {
                    viewModel.onNewAccountInputChange(password = it)
                },
                trailingIcon = {
                    val image = if (passwordVisibility)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    IconButton(onClick = {
                        passwordVisibility = !passwordVisibility
                    }) {
                        Icon(imageVector  = image, "")
                    }
                }
            )

            OutlinedButton(onClick = {
                if (textLogin.toIntOrNull() == null || textPassword.isEmpty()) {
                    toast("Введены некоректные данные")
                } else if (viewModel.accountRepository.getAccountsList().map { it.login }
                        .contains(textLogin)) {
                    toast("Такой аккаунт уже существует")
                } else {
                    viewModel.addNewAccount()
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        scaffoldState.bottomSheetState.collapse()
                    }
                }
                viewModel.newLogin.value = ""
                viewModel.newPassword.value = ""
            }) {
                Text(
                    stringResource(R.string.add_account),
                    color = colorResource(id = R.color.colorPrimaryDark)
                )
            }
        }
    }

    @Composable
    fun ScreenChangeAccount(onCloseBottomSheet: () -> Unit) {
        val mList: MutableList<Account> by remember { mutableStateOf(viewModel.accountRepository.getAccountsList()) }

        viewModel.accountRepository.accountList.observe(viewLifecycleOwner) {
            mList.apply {
                clear()
                addAll(viewModel.accountRepository.getAccountsList())
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.White, shape = RectangleShape)
        ) {
            LazyColumn {
                items(items = mList) { acc ->
                    AccountItem(acc, onCloseBottomSheet)
                }
            }
        }
    }

    @Composable
    fun AccountItem(account: Account, onCloseBottomSheet: () -> Unit) {
        val context = LocalContext.current
        val hapticFeedback = LocalHapticFeedback.current

        Card(
            shape = MaterialTheme.shapes.medium, elevation = 2.dp,
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 20.dp)
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Box {
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCloseBottomSheet()
                        if ((context as MainActivity).hasPermissions())
                            viewModel.updateActiveAccount(account)
                        else
                            context.requestPermissions()
                    }, modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(10.dp)
                ) {
                    Icon(
                        Icons.Filled.CheckCircle, "Status",
                        tint = if (account.isActive)
                            colorResource(id = R.color.colorAccent)
                        else
                            colorResource(id = R.color.colorGray),
                    )
                }

                Text(
                    text = account.login,
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.Center),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                )
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCloseBottomSheet()
                        viewModel.removeAccount(account)
                    }, modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(10.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete, "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }

    sealed class BottomSheetScreen {
        object ScreenAddNewAccount : BottomSheetScreen()
        object ScreenChangeAccount : BottomSheetScreen()
    }

    @Composable
    fun MainContent(
        openSheet: (BottomSheetScreen) -> Unit,
        navController: NavController
    ) {
        val hapticFeedback = LocalHapticFeedback.current
        val localContext = LocalContext.current
        val painter = rememberImagePainter(
            data = viewModel.getImageUrl(),
            builder = {
                crossfade(true)
            }
        )

        val accountsCount by viewModel.accountRepository.accountsCount.collectAsState()
        val accountStatusLifeCycleAware = remember(viewModel.accountRepository.status, lifecycle) {
            viewModel.accountRepository.status.flowWithLifecycle(
                lifecycle = viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            )
        }
        val accountStatus by accountStatusLifeCycleAware.collectAsState(initial = AccountStatus.CHANGING)
        val isSipEnabled by viewModel.accountRepository.isSipEnabled.collectAsState()

        val name = viewModel.accountRepository.displayName.collectAsState(NameItem())

        val switchColor = colorResource(
            id = if (isSipEnabled)
                R.color.colorAccent else R.color.colorGreen
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    backgroundColor = Color.White, elevation = 10.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        Text(
                            modifier = Modifier.align(alignment = Alignment.TopCenter),
                            text = stringResource(R.string.sip_account_header),
                            fontSize = with(LocalDensity.current) {
                                (dimensionResource(id = R.dimen.profile_text_size).value.sp / fontScale)
                            }
                        )
                        IconButton(
                            modifier = Modifier.align(alignment = Alignment.TopEnd),
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                startActivity(Intent(appContext, SettingsActivity::class.java))
//                                navController.navigate(
//                                    R.id.action_navigation_profile_to_settingsFragment,
//                                )
                            }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_settings_24),
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
                                            } else
                                                showSnackBar(
                                                    requireActivity().findViewById(R.id.main_container),
                                                    "Включите SIP"
                                                )
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

                        if (!name.value?.display_name.isNullOrEmpty())
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
                                    append(name.value!!.display_name)
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
}

