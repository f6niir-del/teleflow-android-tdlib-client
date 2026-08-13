package com.teleflow.app

import android.app.Application
import com.teleflow.app.config.DatabaseKeyProvider
import com.teleflow.app.config.TelegramConfiguration
import com.teleflow.app.config.TelegramConfigurationStore
import com.teleflow.app.data.TelegramRepository
import com.teleflow.app.data.TdlibJsonClient

class TeleFlowApplication : Application() {
    private val configurationStore by lazy { TelegramConfigurationStore(applicationContext) }

    val telegramRepository: TelegramRepository by lazy {
        createRepository(configurationStore.load())
    }

    fun configureTelegram(apiId: Int, apiHash: String): Boolean {
        val configuration = TelegramConfiguration(apiId = apiId, apiHash = apiHash.trim())
        if (!configuration.isConfigured) return false
        configurationStore.save(configuration)
        telegramRepository.configure(configuration)
        return true
    }

    private fun createRepository(configuration: TelegramConfiguration): TelegramRepository = TelegramRepository(
        client = TdlibJsonClient(
            appContext = applicationContext,
            configuration = configuration,
            databaseKeyProvider = DatabaseKeyProvider(applicationContext)
        ),
        configuration = configuration
    )
}
