package ru.mephi.voip.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.mephi.voip.R
import ru.mephi.voip.databinding.SettingsActivityBinding
import ru.mephi.voip.databinding.ToolbarSettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsActivityBinding
    private lateinit var toolbarBinding: ToolbarSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        toolbarBinding = binding.toolbarSettings
        setContentView(binding.root)
        toolbarBinding.arrowBack.setOnClickListener {
            finish()
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
    }
}