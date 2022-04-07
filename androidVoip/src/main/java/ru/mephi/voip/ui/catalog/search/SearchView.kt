package ru.mephi.voip.ui.catalog.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ru.mephi.shared.data.model.SearchType
import ru.mephi.voip.R

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun SearchView(
    searchText: String,
    placeholderText: String = "",
    onSearchTextChanged: (String) -> Unit = {},
    onClearClick: () -> Unit = {},
    searchType: SearchType,
    onChangeSearchType: () -> Unit,
    onSubmit: () -> Unit
) {
    var showClearButton by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier
                .padding(vertical = 2.dp)
                .onFocusChanged { focusState -> showClearButton = focusState.isFocused }
                .focusRequester(focusRequester),
            value = searchText, onValueChange = onSearchTextChanged,
            placeholder = {
                Text(text = placeholderText)
            },
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                backgroundColor = Color.Transparent,
                cursorColor = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
            ),
            trailingIcon = {
                AnimatedVisibility(
                    visible = showClearButton,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = { onClearClick() }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Очистить"
                        )
                    }
                }
            },
            maxLines = 1,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                onSubmit()
            })
        )
        Switch(checked = searchType == SearchType.UNITS, onCheckedChange = {
            onChangeSearchType()
        }, modifier = Modifier.fillMaxWidth())
    }

}

@Composable
fun NoSearchResults() {
    Column(
        modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ничего не найдено")
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchBarUI(
    searchText: String,
    onSearchTextChanged: (String) -> Unit = {},
    onClearClick: () -> Unit = {},
    searchType: SearchType,
    onChangeSearchType: () -> Unit = {},
    onSubmit: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SearchView(
            searchText = searchText,
            placeholderText = stringResource(
                if (searchType == SearchType.UNITS)
                    R.string.search_of_units else R.string.search_of_appointments
            ),
            onSearchTextChanged = onSearchTextChanged,
            onClearClick = onClearClick,
            searchType = searchType,
            onChangeSearchType = onChangeSearchType,
            onSubmit = onSubmit
        )
    }
}
