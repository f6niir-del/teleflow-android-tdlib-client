package com.teleflow.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teleflow.app.R
import com.teleflow.app.TeleFlowApplication
import com.teleflow.app.data.AuthorizationState
import com.teleflow.app.data.TelegramChat
import com.teleflow.app.data.TelegramMessage
import com.teleflow.app.data.TelegramRepository
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TeleFlowViewModel(private val repository: TelegramRepository) : ViewModel() {
    val authorization: StateFlow<AuthorizationState> = repository.authorization
    val chats = repository.chats
    val messages = repository.messages
    val error = repository.error

    init { repository.boot() }

    fun phone(value: String) = repository.submitPhone(value.trim())
    fun code(value: String) = repository.submitCode(value.trim())
    fun password(value: String) = repository.submitPassword(value)
    fun resendCode() = repository.resendCode()
    fun requestQr() = repository.requestQrLogin()
    fun logout() = repository.logout()
    fun loadMessages(chatId: Long) = repository.loadMessages(chatId)
    fun send(chatId: Long, text: String) = repository.sendText(chatId, text.trim())
    fun retry() = repository.retry()
    fun clearError() = repository.clearError()
}

class TeleFlowViewModelFactory(private val repository: TelegramRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = TeleFlowViewModel(repository) as T
}

@Composable
fun TeleFlowApp() {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as TeleFlowApplication
    val viewModel: TeleFlowViewModel = viewModel(factory = TeleFlowViewModelFactory(application.telegramRepository))
    val authorization by viewModel.authorization.collectAsStateWithLifecycle()
    val chats by viewModel.chats.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val locale = Locale.getDefault()
    val layoutDirection = if (locale.language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
    var darkMode by rememberSaveable { mutableStateOf(false) }
    var selectedChat by rememberSaveable { mutableStateOf<Long?>(null) }
    var configurationError by rememberSaveable { mutableStateOf(false) }

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection) {
        TeleFlowTheme(darkMode = darkMode) {
            Surface(modifier = Modifier.fillMaxSize()) {
                when {
                    selectedChat != null && authorization == AuthorizationState.Ready -> ChatScreen(
                        chat = chats.firstOrNull { it.id == selectedChat },
                        messages = viewModel.messages.collectAsStateWithLifecycle().value[selectedChat].orEmpty(),
                        onBack = { selectedChat = null },
                        onLoad = { viewModel.loadMessages(selectedChat!!) },
                        onSend = { value -> viewModel.send(selectedChat!!, value) }
                    )
                    authorization == AuthorizationState.ConfigurationMissing -> ConfigurationScreen(
                        error = configurationError,
                        onSubmit = { apiId, apiHash ->
                            configurationError = !application.configureTelegram(apiId, apiHash)
                        }
                    )
                    authorization == AuthorizationState.Ready -> ChatListScreen(
                        chats = chats,
                        darkMode = darkMode,
                        error = error,
                        onDarkMode = { darkMode = !darkMode },
                        onLogout = viewModel::logout,
                        onRetry = viewModel::retry,
                        onDismissError = viewModel::clearError,
                        onOpenChat = { selectedChat = it.id }
                    )
                    else -> AuthenticationScreen(
                        authorization = authorization,
                        error = error,
                        onPhone = viewModel::phone,
                        onCode = viewModel::code,
                        onPassword = viewModel::password,
                        onResend = viewModel::resendCode,
                        onQr = viewModel::requestQr,
                        onRetry = viewModel::retry
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfigurationScreen(
    error: Boolean,
    onSubmit: (Int, String) -> Unit
) = CenteredContent {
    var apiId by rememberSaveable { mutableStateOf("") }
    var apiHash by rememberSaveable { mutableStateOf("") }

    Text(stringResource(R.string.configuration_required), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    Text(stringResource(R.string.configuration_description), style = MaterialTheme.typography.bodyLarge)
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = apiId,
        onValueChange = { apiId = it.filter(Char::isDigit) },
        label = { Text(stringResource(R.string.configuration_api_id)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = apiHash,
        onValueChange = { apiHash = it },
        label = { Text(stringResource(R.string.configuration_api_hash)) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(14.dp))
    Button(
        onClick = { onSubmit(apiId.toIntOrNull() ?: -1, apiHash) },
        modifier = Modifier.fillMaxWidth()
    ) { Text(stringResource(R.string.save_configuration)) }
    Spacer(Modifier.height(10.dp))
    Text(stringResource(R.string.configuration_hint), color = MaterialTheme.colorScheme.primary)
    if (error) {
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.configuration_invalid), color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun AuthenticationScreen(
    authorization: AuthorizationState,
    error: String?,
    onPhone: (String) -> Unit,
    onCode: (String) -> Unit,
    onPassword: (String) -> Unit,
    onResend: () -> Unit,
    onQr: () -> Unit,
    onRetry: () -> Unit
) = CenteredContent {
    when (authorization) {
        AuthorizationState.PhoneNumber -> {
            Text(stringResource(R.string.welcome_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.welcome_description), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))
            FormField(label = stringResource(R.string.phone_number), action = stringResource(R.string.continue_action), onSubmit = onPhone)
            TextButton(onClick = onQr) { Text(stringResource(R.string.qr_confirmation)) }
        }
        AuthorizationState.Code -> {
            Text(stringResource(R.string.verification_code), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            FormField(label = stringResource(R.string.verification_code), action = stringResource(R.string.sign_in), onSubmit = onCode)
            TextButton(onClick = onResend) { Text(stringResource(R.string.resend_code)) }
        }
        AuthorizationState.Password -> {
            Text(stringResource(R.string.password), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            FormField(label = stringResource(R.string.password), action = stringResource(R.string.sign_in), password = true, onSubmit = onPassword)
        }
        is AuthorizationState.QrConfirmation -> {
            Text(stringResource(R.string.qr_confirmation), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.qr_confirmation_description))
            Spacer(Modifier.height(12.dp))
            Text(authorization.link, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        }
        AuthorizationState.Closed -> {
            Text(stringResource(R.string.session_closed), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
        else -> {
            Text(stringResource(R.string.authorization_waiting), style = MaterialTheme.typography.bodyLarge)
        }
    }
    if (error != null) {
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.telegram_error), color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun FormField(label: String, action: String, password: Boolean = false, onSubmit: (String) -> Unit) {
    var value by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    Button(onClick = { if (value.isNotBlank()) onSubmit(value) }, modifier = Modifier.fillMaxWidth()) { Text(action) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatListScreen(
    chats: List<TelegramChat>,
    darkMode: Boolean,
    error: String?,
    onDarkMode: () -> Unit,
    onLogout: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
    onOpenChat: (TelegramChat) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var unreadOnly by rememberSaveable { mutableStateOf(false) }
    val filtered = chats.filter { chat ->
        (!unreadOnly || chat.unreadCount > 0) && chat.title.contains(query, ignoreCase = true)
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.chats), fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = onDarkMode) { Icon(if (darkMode) Icons.Default.LightMode else Icons.Default.DarkMode, stringResource(R.string.dark_mode)) }
                    IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, stringResource(R.string.log_out)) }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text(stringResource(R.string.search_chats)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { unreadOnly = false }, label = { Text(stringResource(R.string.all_chats)) })
                AssistChip(onClick = { unreadOnly = true }, label = { Text(stringResource(R.string.unread_chats)) })
            }
            if (error != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.telegram_error), modifier = Modifier.weight(1f))
                        IconButton(onClick = { onDismissError(); onRetry() }) { Icon(Icons.Default.Refresh, stringResource(R.string.retry)) }
                    }
                }
            }
            if (filtered.isEmpty()) EmptyChats() else LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(filtered, key = { it.id }) { chat -> ChatRow(chat, onOpenChat) }
            }
        }
    }
}

@Composable
private fun EmptyChats() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.no_chats), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.no_chats_description), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ChatRow(chat: TelegramChat, onOpenChat: (TelegramChat) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onOpenChat(chat) }.padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
            Text(chat.title.take(1).uppercase(), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(chat.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(chat.preview, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (chat.unreadCount > 0) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                Text(chat.unreadCount.toString(), color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreen(
    chat: TelegramChat?,
    messages: List<TelegramMessage>,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onSend: (String) -> Unit
) {
    var draft by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(chat?.id) { if (chat != null) onLoad() }
    BackHandler(onBack = onBack)
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text(chat?.title.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }
        )
    }, bottomBar = {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = draft, onValueChange = { draft = it }, label = { Text(stringResource(R.string.message)) }, modifier = Modifier.weight(1f), maxLines = 4)
            Spacer(Modifier.width(8.dp))
            FilledIconButton(onClick = { if (draft.isNotBlank()) { onSend(draft); draft = "" } }) { Icon(Icons.AutoMirrored.Filled.Send, stringResource(R.string.send)) }
        }
    }) { padding ->
        if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(stringResource(R.string.no_messages), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(messages, key = { it.id }) { message -> MessageBubble(message) }
        }
    }
}

@Composable
private fun MessageBubble(message: TelegramMessage) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.isOutgoing) Arrangement.End else Arrangement.Start) {
        Surface(color = if (message.isOutgoing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)) {
            Text(message.text, modifier = Modifier.padding(12.dp), color = if (message.isOutgoing) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CenteredContent(content: @Composable ColumnScope.() -> Unit) = Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start, content = content)
}
