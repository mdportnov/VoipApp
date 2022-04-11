package ru.mephi.voip.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

// https://proandroiddev.com/expandable-lists-in-jetpack-compose-b0b78c767b4

const val FADE_OUT_ANIMATION_DURATION = 100
const val FADE_IN_ANIMATION_DURATION = 100
const val EXPAND_ANIMATION_DURATION = 100
const val COLLAPSE_ANIMATION_DURATION = 100

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExpandableContent(
    visible: Boolean = true,
    content: @Composable () -> Unit
) {
    val enterFadeIn = remember {
        fadeIn(
            animationSpec = TweenSpec(
                durationMillis = FADE_IN_ANIMATION_DURATION,
                easing = FastOutLinearInEasing
            )
        )
    }
    val enterExpand = remember {
        expandVertically(animationSpec = tween(EXPAND_ANIMATION_DURATION))
    }
    val exitFadeOut = remember {
        fadeOut(
            animationSpec = TweenSpec(
                durationMillis = FADE_OUT_ANIMATION_DURATION,
                easing = LinearOutSlowInEasing
            )
        )
    }
    val exitCollapse = remember {
        shrinkVertically(animationSpec = tween(COLLAPSE_ANIMATION_DURATION))
    }

    AnimatedVisibility(
        visible = visible,
        enter = enterExpand + enterFadeIn,
        exit = exitCollapse + exitFadeOut,
        modifier = Modifier.padding(top = 5.dp, bottom = 2.dp)
    ) {
        content()
    }
}

@SuppressLint("UnusedTransitionTargetStateParameter")
@Composable
fun ExpandableCard(
    modifier: Modifier = Modifier,
    onCardArrowClick: () -> Unit,
    content: @Composable (Modifier) -> Unit,
    expandableContent: @Composable () -> Unit,
    expanded: Boolean,
) {
    val transitionState = remember {
        MutableTransitionState(expanded).apply {
            targetState = !expanded
        }
    }
    val transition = updateTransition(transitionState, label = "")

    val cardPaddingHorizontal by transition.animateDp({
        tween(durationMillis = EXPAND_ANIMATION_DURATION)
    }, label = "") {
        if (expanded) 5.dp else 10.dp
    }

    val cardElevation by transition.animateDp({
        tween(durationMillis = EXPAND_ANIMATION_DURATION)
    }, label = "") {
        if (expanded) 24.dp else 0.dp
    }

    val cardRoundedCorners by transition.animateDp({
        tween(
            durationMillis = EXPAND_ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    }, label = "") {
        if (expanded) 10.dp else 5.dp
    }
    val arrowRotationDegree by transition.animateFloat({
        tween(durationMillis = EXPAND_ANIMATION_DURATION)
    }, label = "") {
        if (expanded) 0f else 180f
    }

    Card(
        elevation = cardElevation,
        shape = RoundedCornerShape(cardRoundedCorners),
        modifier = modifier
            .padding(
                start = cardPaddingHorizontal,
                end = cardPaddingHorizontal,
                top = 5.dp,
                bottom = 5.dp,
            )
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                content(Modifier.fillMaxWidth(0.9f))
                CardArrow(
                    degrees = arrowRotationDegree,
                    onClick = onCardArrowClick
                )
            }
            ExpandableContent(
                visible = expanded,
                content = expandableContent
            )
        }
    }
}

@Composable
fun CardArrow(
    degrees: Float,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        content = {
            Icon(
                Icons.Default.ExpandMore,
                contentDescription = "Expandable Arrow",
                modifier = Modifier
                    .size(30.dp)
                    .rotate(degrees)
            )
        },
    )
}
