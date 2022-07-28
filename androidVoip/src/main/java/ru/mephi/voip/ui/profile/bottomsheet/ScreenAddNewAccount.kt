package ru.mephi.voip.ui.profile.bottomsheet

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject
import ru.mephi.shared.data.model.Account
import ru.mephi.voip.R
import ru.mephi.voip.ui.MasterActivity
import ru.mephi.voip.ui.profile.ProfileViewModel
import ru.mephi.voip.utils.ColorAccent
import ru.mephi.voip.utils.ColorGray
import ru.mephi.voip.utils.ColorPrimaryDark

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun ScreenAddNewAccount(scaffoldState: BottomSheetScaffoldState) {
    val viewModel by inject<ProfileViewModel>()

    var textLogin by viewModel.newLogin
    val textPassword by viewModel.newPassword
    val hapticFeedback = LocalHapticFeedback.current
    val maxNumberLength = 6
    val (focusRequester) = FocusRequester.createRefs()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val accountList = viewModel.accountList.collectAsState()

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
                focusedBorderColor = ColorPrimaryDark,
                focusedLabelColor = ColorAccent,
                cursorColor = ColorAccent,
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
                    Toast.makeText(
                        context,
                        "Номер не может быть больше $maxNumberLength символов",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        )

        var passwordVisibility by remember { mutableStateOf(false) }

        OutlinedTextField(
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = ColorPrimaryDark,
                focusedLabelColor = ColorAccent,
                cursorColor = ColorAccent,
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
                    Icon(imageVector = image, "")
                }
            }
        )

        OutlinedButton(onClick = {
            if (textLogin.toIntOrNull() == null || textPassword.isEmpty()) {
                Toast.makeText(context, "Введены некоректные данные", Toast.LENGTH_SHORT).show()
            } else if (accountList.value.map { it.login }
                    .contains(textLogin)) {
                Toast.makeText(context, "Такой аккаунт уже существует", Toast.LENGTH_SHORT).show()
            } else {
                keyboardController?.hide()
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
                color = ColorPrimaryDark
            )
        }
    }
}


@Composable
fun AccountItem(account: Account, onCloseBottomSheet: () -> Unit) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val viewModel by inject<ProfileViewModel>()

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
                    if ((context as MasterActivity).checkNonGrantedPermissions()) viewModel.updateActiveAccount(account)
                }, modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(10.dp)
            ) {
                Icon(
                    Icons.Filled.CheckCircle, "Status",
                    tint = if (account.isActive) ColorAccent else ColorGray,
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