@file:OptIn(ExperimentalAnimationApi::class)

package ru.mephi.voip.utils

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

object NavAnimationUtils {
    val SLIDE_RIGHT = AnimatedContentScope.SlideDirection.Right
    val SLIDE_LEFT = AnimatedContentScope.SlideDirection.Left
    val ANIMATION: FiniteAnimationSpec<IntOffset> = tween(400)
}