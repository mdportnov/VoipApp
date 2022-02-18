package ru.mephi.voip.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.content.res.AppCompatResources
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import ru.mephi.shared.appContext
import ru.mephi.shared.data.database.CatalogDao
import ru.mephi.voip.BuildConfig
import ru.mephi.voip.R
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.data.CatalogRepository
import ru.mephi.voip.utils.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE
import ru.mephi.voip.utils.PACKAGE_NAME
import ru.mephi.voip.utils.launchMailClientIntent
import ru.mephi.voip.utils.toast


class SettingsFragment : PreferenceFragmentCompat(), KoinComponent {
    private val catalogRepository: CatalogRepository by inject()
    private val catalogDao: CatalogDao by inject()
    private val accountStatusRepository: AccountStatusRepository by inject()

    @SuppressLint("NewApi")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        val backgroundWorkPreference = SwitchPreferenceCompat(context).apply {
            key = getString(R.string.sp_background_enabled)
            title = "Работать в фоновом режиме"
            isSingleLineTitle = false
            icon = appContext.getDrawable(R.drawable.ic_baseline_cloud_24)
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    val switched: Boolean = (preference as SwitchPreferenceCompat).isChecked
                    accountStatusRepository.changeBackGroundWork(newValue as Boolean)
                    preference.isChecked = !switched
                    true
                }
        }

        val deleteHistoryPreference = Preference(context).apply {
            key = "delete_search_history"
            title = "Удалить историю запросов"
            icon = appContext.getDrawable(R.drawable.ic_baseline_delete_sweep_24)
        }

        val deleteCatalogCachePreference = Preference(context).apply {
            key = "delete_catalog_cache_history"
            title = "Удалить кэш каталога"
            icon = appContext.getDrawable(R.drawable.ic_baseline_playlist_remove_24)
        }

        val voipSettings = PreferenceCategory(context).apply {
            key = "voip"
            title = "VoIP"
        }

        val dataSettings = PreferenceCategory(context).apply {
            key = "data"
            title = "Данные"
        }

        screen.addPreference(voipSettings)
        screen.addPreference(dataSettings)

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
            icon = appContext.getDrawable(R.drawable.ic_baseline_add_to_home_screen_24)
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    val switched: Boolean = (preference as SwitchPreferenceCompat).isChecked
                    if (newValue as Boolean) {
                        if (!Settings.canDrawOverlays(context)) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$PACKAGE_NAME")
                            )
                            startActivityForResult(
                                intent,
                                ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE
                            )
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
            icon = AppCompatResources.getDrawable(appContext, R.drawable.ic_baseline_info_24)
        }

        val developedByPreference = Preference(context).apply {
            key = "developed"
            summary = "НИЯУ МИФИ, Управление информатизации"
            title = "Разработано"
            icon = appContext.getDrawable(R.drawable.ic_baseline_auto_fix_high_24)
        }

        val feedbackPreference = Preference(context).apply {
            key = "feedback"
            summary = "Сообщить о технических вопросах или предложить новые функции"
            title = "Отправить фидбэк"
            icon = appContext.getDrawable(R.drawable.ic_baseline_send_24)
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

        voipSettings.addPreference(backgroundWorkPreference)
        voipSettings.addPreference(callScreenPreference)
        dataSettings.addPreference(deleteHistoryPreference)
        dataSettings.addPreference(deleteCatalogCachePreference)
        preferenceScreen = screen
    }
}