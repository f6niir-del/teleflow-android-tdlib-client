package com.teleflow.app.config

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.teleflow.app.BuildConfig
import java.security.SecureRandom

data class TelegramConfiguration(
    val apiId: Int,
    val apiHash: String
) {
    val isConfigured: Boolean
        get() = apiId > 0 && apiHash.length >= 16

    companion object {
        fun fromBuildConfig(): TelegramConfiguration = TelegramConfiguration(
            apiId = BuildConfig.TELEGRAM_API_ID,
            apiHash = BuildConfig.TELEGRAM_API_HASH
        )
    }
}

/** Stores the TDLib database key in Android Keystore-backed encrypted storage. */
class DatabaseKeyProvider(context: Context) {
    private val preferences = EncryptedSharedPreferences.create(
        context,
        "teleflow_secure_store",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun databaseKey(): String {
        val existing = preferences.getString(KEY, null)
        if (existing != null) return existing

        val generated = ByteArray(32).also(SecureRandom()::nextBytes)
        return Base64.encodeToString(generated, Base64.NO_WRAP).also { key ->
            preferences.edit().putString(KEY, key).commit()
        }
    }

    private companion object {
        const val KEY = "tdlib_database_key"
    }
}
