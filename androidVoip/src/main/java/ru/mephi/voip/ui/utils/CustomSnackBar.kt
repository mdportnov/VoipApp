package ru.mephi.voip.ui.utils

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import ru.mephi.voip.R

class CustomSnackBar(rootView: View, text: String, duration: Int) {
    private val snackBar = Snackbar.make(rootView, text, duration)

    fun show() = snackBar.show()
    fun dismiss() = snackBar.dismiss()

    init {
        val inflater = LayoutInflater.from(rootView.context)
        val snackView = inflater.inflate(R.layout.custom_snackbar, null)
        snackView.findViewById<TextView>(R.id.snack_bar_text).text = text
        val layout = snackBar.view as Snackbar.SnackbarLayout
        layout.addView(snackView)
        snackBar.setAnchorView(R.id.bottom_nav)
        snackBar.duration = duration
    }
}