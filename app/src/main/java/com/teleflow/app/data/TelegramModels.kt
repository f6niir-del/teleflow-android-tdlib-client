package com.teleflow.app.data

data class TelegramChat(
    val id: Long,
    val title: String,
    val unreadCount: Int,
    val preview: String,
    val lastMessageDate: Int
)

data class TelegramMessage(
    val id: Long,
    val chatId: Long,
    val isOutgoing: Boolean,
    val text: String,
    val date: Int
)

sealed interface AuthorizationState {
    data object ConfigurationMissing : AuthorizationState
    data object Initializing : AuthorizationState
    data object PhoneNumber : AuthorizationState
    data object Code : AuthorizationState
    data object Password : AuthorizationState
    data class QrConfirmation(val link: String) : AuthorizationState
    data object Ready : AuthorizationState
    data object LoggingOut : AuthorizationState
    data object Closed : AuthorizationState
}
