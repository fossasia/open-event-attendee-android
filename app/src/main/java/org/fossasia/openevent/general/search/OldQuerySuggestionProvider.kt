package org.fossasia.openevent.general.search

import android.content.SearchRecentSuggestionsProvider

class OldQuerySuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "org.fossasia.openevent.general.search.OldQuerySuggestionProvider"
        const val MODE: Int = DATABASE_MODE_QUERIES or DATABASE_MODE_2LINES
    }
}
