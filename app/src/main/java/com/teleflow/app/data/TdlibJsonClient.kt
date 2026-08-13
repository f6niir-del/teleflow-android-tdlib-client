package com.teleflow.app.data

import android.content.Context
import com.teleflow.app.config.DatabaseKeyProvider
import com.teleflow.app.config.TelegramConfiguration
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.drinkless.tdlib.JsonClient
import org.drinkless.tdlib.TdLibInitializer
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TdlibRequestException(message: String) : IllegalStateException(message)

/** A single-client coroutine bridge for TDLib's asynchronous JSON interface. */
class TdlibJsonClient(
    private val appContext: Context,
    private val configuration: TelegramConfiguration,
    private val databaseKeyProvider: DatabaseKeyProvider
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val pending = ConcurrentHashMap<String, CompletableDeferred<JsonObject>>()
    private val _updates = MutableSharedFlow<JsonObject>(
        replay = 0,
        extraBufferCapacity = 128,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val updates: SharedFlow<JsonObject> = _updates

    @Volatile private var clientId: Long = 0L
    private var receiverJob: Job? = null

    fun start(scope: CoroutineScope) {
        if (clientId != 0L || !configuration.isConfigured) return
        val result = TdLibInitializer.init()
        if (result !is org.drinkless.tdlib.TdLibInitResult.Success) {
            throw TdlibRequestException("TDLib native initialization failed")
        }
        clientId = JsonClient.create()
        receiverJob = scope.launch(Dispatchers.IO) {
            while (isActive && clientId != 0L) {
                val raw = JsonClient.receive(clientId, 1.0) ?: continue
                val response = runCatching { json.parseToJsonElement(raw).jsonObject }.getOrNull() ?: continue
                val extra = response["@extra"]?.jsonPrimitive?.content
                val deferred = extra?.let(pending::remove)
                if (deferred != null) deferred.complete(response) else _updates.emit(response)
            }
        }
    }

    suspend fun request(type: String, body: JsonObject = buildJsonObject { }): JsonObject {
        check(clientId != 0L) { "TDLib client has not started" }
        val extra = UUID.randomUUID().toString()
        val result = CompletableDeferred<JsonObject>()
        pending[extra] = result
        val request = buildJsonObject {
            put("@type", type)
            body.forEach { (key, value) -> put(key, value) }
            put("@extra", extra)
        }
        JsonClient.send(clientId, request.toString())
        return try {
            withTimeout(30_000) { result.await() }.also(::throwIfError)
        } finally {
            pending.remove(extra)
        }
    }

    fun parameters(): JsonObject = buildJsonObject {
        val databaseDirectory = File(appContext.filesDir, "tdlib").apply { mkdirs() }
        put("database_directory", databaseDirectory.absolutePath)
        put("files_directory", File(appContext.filesDir, "tdlib-files").apply { mkdirs() }.absolutePath)
        put("use_file_database", true)
        put("use_chat_info_database", true)
        put("use_message_database", true)
        put("use_secret_chats", true)
        put("api_id", configuration.apiId)
        put("api_hash", configuration.apiHash)
        put("system_language_code", appContext.resources.configuration.locales[0].toLanguageTag())
        put("device_model", android.os.Build.MODEL.take(64))
        put("system_version", "Android ${android.os.Build.VERSION.RELEASE}")
        put("application_version", "TeleFlow ${com.teleflow.app.BuildConfig.VERSION_NAME}")
        put("enable_storage_optimizer", true)
        put("ignore_file_names", false)
    }

    fun encryptionKey(): String = databaseKeyProvider.databaseKey()

    fun close() {
        if (clientId == 0L) return
        runCatching { JsonClient.send(clientId, "{\"@type\":\"close\"}") }
        receiverJob?.cancel()
        runCatching { JsonClient.destroy(clientId) }
        clientId = 0L
        pending.values.forEach { it.cancel() }
        pending.clear()
    }

    private fun throwIfError(response: JsonObject) {
        if (response["@type"]?.jsonPrimitive?.content == "error") {
            val message = response["message"]?.jsonPrimitive?.content ?: "Telegram request failed"
            throw TdlibRequestException(message)
        }
    }
}
