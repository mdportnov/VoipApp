package ru.mephi.voip.ui.catalog.adapter

import android.content.SearchRecentSuggestionsProvider
import ru.mephi.voip.BuildConfig

class CatalogSuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".CatalogSuggestionProvider"
        const val MODE: Int = DATABASE_MODE_QUERIES
    }
}