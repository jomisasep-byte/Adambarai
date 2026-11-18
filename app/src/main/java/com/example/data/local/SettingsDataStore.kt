package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "music_guard_prefs")

data class AppSettings(
  val themePreference: ThemePreference = ThemePreference.CYBER_DARK,
  val backendProxyUrl: String = "",
  val requireUploadConsent: Boolean = true,
)

class SettingsDataStore(private val context: Context) {
  private object Keys {
    val THEME = stringPreferencesKey("theme_preference")
    val BACKEND_PROXY_URL = stringPreferencesKey("backend_proxy_url")
    val REQUIRE_CONSENT = booleanPreferencesKey("require_upload_consent")
  }

  val settingsFlow: Flow<AppSettings> =
    context.dataStore.data.map { prefs ->
      val themeName = prefs[Keys.THEME] ?: ThemePreference.CYBER_DARK.name
      val theme =
        ThemePreference.entries.firstOrNull { it.name == themeName } ?: ThemePreference.CYBER_DARK
      AppSettings(
        themePreference = theme,
        backendProxyUrl = prefs[Keys.BACKEND_PROXY_URL] ?: "",
        requireUploadConsent = prefs[Keys.REQUIRE_CONSENT] ?: true,
      )
    }

  suspend fun setThemePreference(theme: ThemePreference) {
    context.dataStore.edit { it[Keys.THEME] = theme.name }
  }

  suspend fun setBackendProxyUrl(url: String) {
    context.dataStore.edit { it[Keys.BACKEND_PROXY_URL] = url.trim() }
  }

  suspend fun setRequireUploadConsent(required: Boolean) {
    context.dataStore.edit { it[Keys.REQUIRE_CONSENT] = required }
  }
}
