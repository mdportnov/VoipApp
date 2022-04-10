package ru.mephi.voip.ui.call

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.abtollc.sdk.AbtoPhone
import ru.mephi.voip.abto.AbtoApp

abstract class AbtoViewModel(app: Application) : AndroidViewModel(app) {
    val abtoApp = app as AbtoApp
    internal var phone: AbtoPhone = abtoApp.abtoPhone
}