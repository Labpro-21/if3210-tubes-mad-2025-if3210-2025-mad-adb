package com.example.adbpurrytify.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages authentication tokens securely.
 */
@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveAuthToken(token: String) {
        prefs.edit { putString(KEY_AUTH_TOKEN, token) }
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveRefreshToken(token: String) {
        prefs.edit { putString(KEY_REFRESH_TOKEN, token) }
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearTokens() {
        prefs.edit {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_REFRESH_TOKEN)
        }
    }

    fun hasTokens(): Boolean {
        return getAuthToken() != null && getRefreshToken() != null
    }



    companion object {
        private const val PREFS_NAME = "purrytify_secure_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"

        // These static methods will help during the transition to DI
        // They should be removed once the migration is complete
        @Volatile
        private var INSTANCE: TokenManager? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = TokenManager(context.applicationContext)
                    }
                }
            }
        }

        // Static methods to maintain compatibility during transition
        fun getAuthToken(): String? = INSTANCE?.getAuthToken()
        fun getRefreshToken(): String? = INSTANCE?.getRefreshToken()
        fun saveAuthToken(token: String) = INSTANCE?.saveAuthToken(token)
        fun saveRefreshToken(token: String) = INSTANCE?.saveRefreshToken(token)
        fun clearTokens() = INSTANCE?.clearTokens()
        fun hasTokens(): Boolean = INSTANCE?.hasTokens() ?: false
    }
}