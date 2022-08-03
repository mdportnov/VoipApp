package ru.mephi.voip.ui.common

import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun CommonColor(): Color {
    return TopAppBarDefaults.smallTopAppBarColors().containerColor(
        scrollFraction = 1.0f
    ).value
}