package com.teleflow.app

import android.app.Application
import com.teleflow.app.config.DatabaseKeyProvider
import com.teleflow.app.config.TelegramConfiguration
import com.teleflow.app.data.TelegramRepository
import com.teleflow.app.data.TdlibJsonClient

class TeleFlowApplication : Application() {
    val telegramRepository: TelegramRepository by lazy {
        val configuration = TelegramConfiguration.fromBuildConfig()
        TelegramRepository(
            client = TdlibJsonClient(
                appContext = applicationContext,
                configuration = configuration,
                databaseKeyProvider = DatabaseKeyProvider(applicationContext)
            ),
            configuration = configuration
        )
    }
}
