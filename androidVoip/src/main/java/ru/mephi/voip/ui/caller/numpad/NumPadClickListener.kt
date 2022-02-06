package ru.mephi.voip.ui.caller.numpad

import android.view.View
import ru.mephi.voip.R
import ru.mephi.voip.ui.caller.numpad.NumPadLogic.returnList
import ru.mephi.voip.ui.caller.numpad.NumPadLogic.returnNum

interface NumPadClickListener {
    fun onNumClicked(nums: StringBuilder, view: View)
}

interface NumPadLongClickListener {
    fun onNumLongClicked(nums: StringBuilder, view: View)
}

fun StringBuilder.isNotExceed(size: Int = 5) = this.length <= size

var numbers: StringBuilder = StringBuilder()

class NumPadClick(listener: NumPadClickListener) : View.OnClickListener {
    private val mListener: NumPadClickListener = listener
    override fun onClick(view: View) {
        numbers = returnList(returnNum(view), numbers)
        mListener.onNumClicked(numbers, view)
    }
}

class NumPadLongClick(listener: NumPadLongClickListener) : View.OnLongClickListener {
    private val mListener: NumPadLongClickListener = listener
    override fun onLongClick(view: View): Boolean {
        if (view.id == R.id.input_delete) {
            numbers.clear()
            mListener.onNumLongClicked(numbers, view)
        }
        return true
    }
}