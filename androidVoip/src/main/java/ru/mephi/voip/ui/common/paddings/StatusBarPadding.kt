package ru.mephi.voip.ui.common.paddings

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable

@Composable
fun StatusBarPadding() = CommonPadding(padding = getStatusBarPadding())

@Composable
fun getStatusBarPadding() = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()