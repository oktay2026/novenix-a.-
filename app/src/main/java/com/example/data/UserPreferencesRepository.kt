package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "novenix_prefs")

data class BookmarkItem(
    val url: String,
    val title: String
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val DESKTOP_MODE = booleanPreferencesKey("desktop_mode")
        val BOOKMARKS = stringSetPreferencesKey("bookmarks_encoded")
    }

    val desktopModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DESKTOP_MODE] ?: false
    }

    val bookmarksFlow: Flow<List<BookmarkItem>> = context.dataStore.data.map { prefs ->
        val rawSet = prefs[Keys.BOOKMARKS] ?: emptySet()
        rawSet.mapNotNull { entry ->
            val separatorIndex = entry.indexOf('|')
            if (separatorIndex > 0) {
                val url = entry.substring(0, separatorIndex)
                val title = entry.substring(separatorIndex + 1).ifBlank { url }
                BookmarkItem(url = url, title = title)
            } else {
                null
            }
        }.sortedBy { it.title }
    }

    suspend fun setDesktopMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DESKTOP_MODE] = enabled
        }
    }

    suspend fun toggleBookmark(url: String, title: String): Boolean {
        var added = false
        val cleanUrl = url.trim()
        val cleanTitle = title.replace("|", " ").trim().ifBlank { "novenix.tech" }
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BOOKMARKS]?.toMutableSet() ?: mutableSetOf()
            val existing = current.firstOrNull { it.startsWith("$cleanUrl|") }
            if (existing != null) {
                current.remove(existing)
                added = false
            } else {
                current.add("$cleanUrl|$cleanTitle")
                added = true
            }
            prefs[Keys.BOOKMARKS] = current
        }
        return added
    }

    suspend fun removeBookmark(url: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BOOKMARKS]?.toMutableSet() ?: mutableSetOf()
            current.removeAll { it.startsWith("$url|") }
            prefs[Keys.BOOKMARKS] = current
        }
    }
}
