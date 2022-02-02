package ru.mephi.voip.ui.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.TranslateAnimation

const val DURATION: Long = 50

class Animation {
    companion object {
        fun toggleArrow(view: View, isExpanded: Boolean): Boolean {
            return if (isExpanded) {
                view.animate().setDuration(200).rotation(180F)
                true
            } else {
                view.animate().setDuration(200).rotation(0F)
                false
            }
        }
    }
}

fun setFadeInAnimation(itemView: View, i: Int, onAttach: Boolean) {
    var p = i
    if (!onAttach)
        p = -1
    val isNotFirstItem = p == -1
    p++
    itemView.alpha = 0f
    val animatorSet = AnimatorSet()
    val animator = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 0.5f, 1.0f)
    ObjectAnimator.ofFloat(itemView, "alpha", 0f).start()
    animator.startDelay = if (isNotFirstItem) DURATION / 2 else p * DURATION / 3
    animator.duration = 500
    animatorSet.play(animator)
    animator.start()
}

fun setFromLeftToRightAnimation(itemView: View, i: Int, onAttach: Boolean) {
    var p = i
    if (!onAttach)
        p = -1
    val not_first_item = p == -1
    p += 1
    itemView.translationX = -400f
    itemView.alpha = 0f
    val animatorSet = AnimatorSet()
    val animatorTranslateY = ObjectAnimator.ofFloat(itemView, "translationX", -400f, 0f)
    val animatorAlpha = ObjectAnimator.ofFloat(itemView, "alpha", 1f)
    ObjectAnimator.ofFloat(itemView, "alpha", 0f).start()
    animatorTranslateY.startDelay = if (not_first_item) DURATION else p * DURATION
    animatorTranslateY.duration = (if (not_first_item) 2 else 1) * DURATION
    animatorSet.playTogether(animatorTranslateY, animatorAlpha)
    animatorSet.start()
}

var slideAnimationDuration = 400L

fun View.slideUp() {
    this.visibility = View.VISIBLE
    val animate = TranslateAnimation(
        0F,
        0F,
        this.height.toFloat() + 200,
        0F
    )

    animate.duration = slideAnimationDuration
//    animate.fillAfter = true
    this.startAnimation(animate)
}

fun View.slideDown() {
    val animate = TranslateAnimation(
        0F,  // fromXDelta
        0F,  // toXDelta
        0F,  // fromYDelta
        this.height.toFloat() + 500
    ) // toYDelta

    animate.duration = slideAnimationDuration
//    animate.fillAfter = true
    this.startAnimation(animate)
    this.visibility = View.GONE
}

fun View.fadeIn() {
    this.animate().alpha(1f).setDuration(slideAnimationDuration)
        .setInterpolator(AccelerateInterpolator()).start()
}

fun View.fadeOut() {
    this.animate().alpha(0f).setDuration(slideAnimationDuration)
        .setInterpolator(AccelerateInterpolator()).start()
}