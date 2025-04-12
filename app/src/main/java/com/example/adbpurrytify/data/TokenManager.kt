// data/TokenManager.kt
package com.example.adbpurrytify.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenManager {

    private const val PREFS_NAME = "purrytify_secure_prefs"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private var sharedPreferences: SharedPreferences? = null

    fun initialize(context: Context) {
        if (sharedPreferences == null) {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            sharedPreferences = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    private fun getPrefs(): SharedPreferences {
        return sharedPreferences ?: throw IllegalStateException("TokenManager not initialized. Call initialize() first.")
    }

    fun saveAuthToken(token: String) {
        getPrefs().edit { putString(KEY_AUTH_TOKEN, token) }
    }

    fun getAuthToken(): String? {
        return getPrefs().getString(KEY_AUTH_TOKEN, null)
    }

    fun saveRefreshToken(token: String) {
        getPrefs().edit { putString(KEY_REFRESH_TOKEN, token) }
    }

    fun getRefreshToken(): String? {
        return getPrefs().getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearTokens() {
        getPrefs().edit {
            remove(KEY_AUTH_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
        }
    }

    fun hasTokens(): Boolean {
        return getAuthToken() != null && getRefreshToken() != null
    }
}
