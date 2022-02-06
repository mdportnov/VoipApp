package ru.mephi.voip.ui.caller.numpad

import android.view.View
import ru.mephi.voip.R

internal object NumPadLogic {
    fun returnNum(view: View): String {
        return when (view.id) {
            R.id.btnNumpad0 -> "0"
            R.id.btnNumpad1 -> "1"
            R.id.btnNumpad2 -> "2"
            R.id.btnNumpad3 -> "3"
            R.id.btnNumpad4 -> "4"
            R.id.btnNumpad5 -> "5"
            R.id.btnNumpad6 -> "6"
            R.id.btnNumpad7 -> "7"
            R.id.btnNumpad8 -> "8"
            R.id.btnNumpad9 -> "9"
            R.id.btnNumpadStar -> "*"
            R.id.btnNumpadPound -> "#"
            R.id.input_delete -> "del"
            else -> ""
        }
    }

    fun returnList(input: String, numbers: StringBuilder): StringBuilder {
        when (input) {
            "del" ->
                if (numbers.isNotEmpty())
                    numbers.deleteCharAt(numbers.lastIndex)
            "*" -> {
                // TODO
            }
            "#" -> {
                // TODO
            }
            else -> {
                if (numbers.isNotExceed())
                    numbers.append(input)
            }
        }
        return numbers
    }
}
