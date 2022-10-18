package ru.mephi.voip.ui.common.paddings

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable

@Composable
fun NavBarPadding() = CommonPadding(padding = getNavBarPadding())

@Composable
fun getNavBarPadding() = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
