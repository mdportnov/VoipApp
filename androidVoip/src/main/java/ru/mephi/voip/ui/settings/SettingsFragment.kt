package ru.mephi.voip.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent
import ru.mephi.shared.data.database.CatalogDao
import ru.mephi.shared.data.repository.CatalogRepository
import ru.mephi.voip.BuildConfig
import ru.mephi.voip.R
import ru.mephi.voip.ui.utils.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE
import ru.mephi.voip.ui.utils.PACKAGE_NAME
import ru.mephi.voip.ui.utils.launchMailClientIntent
import ru.mephi.voip.ui.utils.toast


class SettingsFragment : PreferenceFragmentCompat() {
    private val catalogRepository: CatalogRepository by inject()
    private val catalogDao: CatalogDao by KoinJavaComponent.inject(CatalogDao::class.java)

    @SuppressLint("NewApi")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        val backgroundWorkPreference = SwitchPreferenceCompat(context).apply {
            key = getString(R.string.background_work_settings)
            title = "Работать в фоновом режиме"
        }

        val deleteHistoryPreference = Preference(context).apply {
            key = "delete_search_history"
            title = "Удалить историю запросов"
        }

        val deleteCatalogCachePreference = Preference(context).apply {
            key = "delete_catalog_cache_history"
            title = "Удалить кэш каталога"
        }

//        val accountsCategory = PreferenceCategory(context).apply {
//            key = "accounts"
//            title = "SIP Аккаунты"
//        }

        val settingsCategory = PreferenceCategory(context).apply {
            key = "settings"
            title = "Настройки"
        }

//        screen.addPreference(accountsCategory)
        screen.addPreference(settingsCategory)

        deleteHistoryPreference.setOnPreferenceClickListener {
            catalogRepository.deleteAllSearchRecords()
            toast("История поиска удалена")
            true
        }

        deleteCatalogCachePreference.setOnPreferenceClickListener {
            catalogDao.deleteAll()
            toast("Кэш каталога удален")
            true
        }

        val callScreenPreference = SwitchPreferenceCompat(context).apply {
            key = getString(R.string.call_screen_always_settings)
            title = "Показывать экран входящего вызова"
            isSingleLineTitle = false
            summary =
                "Если sip-клиент активен, то при активации этой " +
                        "функции будет появляться экран входящего вызова. " +
                        "Если функция отключена, будет выводиться только уведомление."
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    val switched: Boolean = (preference as SwitchPreferenceCompat).isChecked
                    if(newValue as Boolean){
                        if (!Settings.canDrawOverlays(context)) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$PACKAGE_NAME")
                            )
                            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
                        }
                    }
                    preference.isChecked = !switched
                    true
                }
        }

        val versionPreference = Preference(context).apply {
            key = "version"
            summary = BuildConfig.VERSION_NAME
            title = "Версия"
        }

        val developedByPreference = Preference(context).apply {
            key = "developed"
            summary = "НИЯУ МИФИ, Управление информатизации"
            title = "Разработано"
        }

        val feedbackPreference = Preference(context).apply {
            key = "feedback"
            summary = "Сообщить о технических вопросах или предложить новые функции"
            title = "Отправить фидбэк"
            setOnPreferenceClickListener {
                context.launchMailClientIntent(resources.getString(R.string.support_email))
                true
            }
        }

        val aboutCategory = PreferenceCategory(context).apply {
            key = "about"
            title = "О приложении"
        }

        screen.addPreference(aboutCategory)
        aboutCategory.addPreference(feedbackPreference)
        aboutCategory.addPreference(versionPreference)
        aboutCategory.addPreference(developedByPreference)

        settingsCategory.addPreference(backgroundWorkPreference)
        settingsCategory.addPreference(callScreenPreference)
        settingsCategory.addPreference(deleteHistoryPreference)
        settingsCategory.addPreference(deleteCatalogCachePreference)
        preferenceScreen = screen
    }
}