package ru.mephi.voip.ui.call

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import org.abtollc.sdk.AbtoPhone
import ru.mephi.voip.call.abto.AbtoApp

abstract class AbtoViewModel(app: Application, open var sp: SharedPreferences) : AndroidViewModel(app){
    val abtoApp = app as AbtoApp
    var phone: AbtoPhone = abtoApp.abtoPhone
}