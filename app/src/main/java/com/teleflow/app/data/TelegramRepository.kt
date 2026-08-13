package com.teleflow.app.data

import com.teleflow.app.config.TelegramConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Maps TDLib updates into small UI-ready state without fabricating conversations or messages. */
class TelegramRepository(
    private val client: TdlibJsonClient,
    private val configuration: TelegramConfiguration
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val chatIndex = linkedMapOf<Long, TelegramChat>()

    private val _authorization = MutableStateFlow<AuthorizationState>(
        if (configuration.isConfigured) AuthorizationState.Initializing else AuthorizationState.ConfigurationMissing
    )
    val authorization: StateFlow<AuthorizationState> = _authorization.asStateFlow()

    private val _chats = MutableStateFlow<List<TelegramChat>>(emptyList())
    val chats: StateFlow<List<TelegramChat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<Map<Long, List<TelegramMessage>>>(emptyMap())
    val messages: StateFlow<Map<Long, List<TelegramMessage>>> = _messages.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var booted = false

    fun boot() {
        if (booted || !configuration.isConfigured) return
        booted = true
        scope.launch {
            client.updates.collectLatest(::handleUpdate)
        }
        runCatching { client.start(scope) }.onFailure { fail(it) }
    }

    fun retry() {
        _error.value = null
        if (!booted) boot() else if (authorization.value == AuthorizationState.Closed) {
            booted = false
            boot()
        }
    }

    fun submitPhone(phoneNumber: String) = request("setAuthenticationPhoneNumber") {
        put("phone_number", phoneNumber)
        put("settings", buildJsonObject { put("@type", "phoneNumberAuthenticationSettings") })
    }

    fun submitCode(code: String) = request("checkAuthenticationCode") { put("code", code) }

    fun submitPassword(password: String) = request("checkAuthenticationPassword") { put("password", password) }

    fun resendCode() = request("resendAuthenticationCode") { }

    fun requestQrLogin() = request("requestQrCodeAuthentication") { }

    fun logout() = request("logOut") { }

    fun loadMessages(chatId: Long) = scope.launch {
        runCatching {
            client.request("getChatHistory", buildJsonObject {
                put("chat_id", chatId)
                put("from_message_id", 0)
                put("offset", 0)
                put("limit", 50)
                put("only_local", false)
            })
        }.onSuccess { response ->
            val mapped = response["messages"]?.jsonArray.orEmpty()
                .mapNotNull { element -> (element as? JsonObject)?.let(::parseMessage) }
                .sortedBy { it.id }
            _messages.value = _messages.value + (chatId to mapped)
        }.onFailure(::fail)
    }

    fun sendText(chatId: Long, text: String) = request("sendMessage") {
        put("chat_id", chatId)
        put("message_thread_id", 0)
        put("reply_to", buildJsonObject { put("@type", "inputMessageReplyTo") })
        put("options", buildJsonObject { put("@type", "messageSendOptions") })
        put("input_message_content", buildJsonObject {
            put("@type", "inputMessageText")
            put("text", buildJsonObject {
                put("@type", "formattedText")
                put("text", text)
                put("entities", JsonArray(emptyList()))
            })
            put("link_preview_options", buildJsonObject { put("@type", "linkPreviewOptions") })
            put("clear_draft", true)
        })
    }

    fun clearError() { _error.value = null }

    private fun request(type: String, body: JsonObjectBuilder.() -> Unit) = scope.launch {
        runCatching { client.request(type, buildJsonObject(body)) }.onFailure(::fail)
    }

    private fun handleUpdate(update: JsonObject) {
        when (update.string("@type")) {
            "updateAuthorizationState" -> {
                val state = update["authorization_state"]?.jsonObject ?: return
                handleAuthorization(state)
            }
            "updateNewChat" -> update["chat"]?.jsonObject?.let(::upsertChat)
            "updateChatLastMessage", "updateChatReadInbox", "updateChatPosition" -> {
                // TDLib always sends updateNewChat before any chat ID becomes visible.
                // The full chat cache remains authoritative; a later load refreshes it.
            }
            "updateNewMessage" -> update["message"]?.jsonObject?.let { message ->
                parseMessage(message)?.let { parsed ->
                    val current = _messages.value[parsed.chatId].orEmpty()
                    _messages.value = _messages.value + (parsed.chatId to (current + parsed).distinctBy { it.id })
                }
            }
        }
    }

    private fun handleAuthorization(state: JsonObject) {
        when (state.string("@type")) {
            "authorizationStateWaitTdlibParameters" -> request("setTdlibParameters") {
                client.parameters().forEach { (key, value) -> put(key, value) }
            }
            "authorizationStateWaitEncryptionKey" -> request("checkDatabaseEncryptionKey") {
                put("encryption_key", client.encryptionKey())
            }
            "authorizationStateWaitPhoneNumber" -> _authorization.value = AuthorizationState.PhoneNumber
            "authorizationStateWaitCode", "authorizationStateWaitEmailCode" -> _authorization.value = AuthorizationState.Code
            "authorizationStateWaitPassword" -> _authorization.value = AuthorizationState.Password
            "authorizationStateWaitOtherDeviceConfirmation" -> {
                _authorization.value = AuthorizationState.QrConfirmation(state.string("link"))
            }
            "authorizationStateReady" -> {
                _authorization.value = AuthorizationState.Ready
                request("loadChats") {
                    put("chat_list", buildJsonObject { put("@type", "chatListMain") })
                    put("limit", 50)
                }
            }
            "authorizationStateLoggingOut", "authorizationStateClosing" -> _authorization.value = AuthorizationState.LoggingOut
            "authorizationStateClosed" -> _authorization.value = AuthorizationState.Closed
        }
    }

    private fun upsertChat(chat: JsonObject) {
        val id = chat.long("id") ?: return
        chatIndex[id] = TelegramChat(
            id = id,
            title = chat.string("title").ifBlank { id.toString() },
            unreadCount = chat.int("unread_count"),
            preview = chat["last_message"]?.jsonObject?.let(::messagePreview).orEmpty(),
            lastMessageDate = chat["last_message"]?.jsonObject?.int("date") ?: 0
        )
        _chats.value = chatIndex.values.sortedWith(
            compareByDescending<TelegramChat> { it.lastMessageDate }.thenBy { it.title.lowercase() }
        )
    }

    private fun parseMessage(message: JsonObject): TelegramMessage? {
        val id = message.long("id") ?: return null
        val chatId = message.long("chat_id") ?: return null
        return TelegramMessage(
            id = id,
            chatId = chatId,
            isOutgoing = message.boolean("is_outgoing"),
            text = message["content"]?.jsonObject?.let(::messagePreview).orEmpty(),
            date = message.int("date")
        )
    }

    private fun messagePreview(content: JsonObject): String = when (content.string("@type")) {
        "messageText" -> content["text"]?.jsonObject?.string("text").orEmpty()
        "messagePhoto" -> content["caption"]?.jsonObject?.string("text").orEmpty().ifBlank { "Photo" }
        "messageVideo" -> content["caption"]?.jsonObject?.string("text").orEmpty().ifBlank { "Video" }
        "messageDocument" -> content["caption"]?.jsonObject?.string("text").orEmpty().ifBlank { "Document" }
        "messageVoiceNote" -> "Voice message"
        "messageSticker" -> "Sticker"
        else -> ""
    }

    private fun fail(error: Throwable) {
        _error.value = error.message?.takeIf { it.isNotBlank() } ?: "Telegram request failed"
    }

    private fun JsonObject.string(key: String): String = this[key]?.jsonPrimitive?.content.orEmpty()
    private fun JsonObject.int(key: String): Int = this[key]?.jsonPrimitive?.intOrNull ?: 0
    private fun JsonObject.long(key: String): Long? = this[key]?.jsonPrimitive?.longOrNull
    private fun JsonObject.boolean(key: String): Boolean = this[key]?.jsonPrimitive?.booleanOrNull ?: false
}
