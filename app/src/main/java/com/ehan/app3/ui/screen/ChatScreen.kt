package com.ehan.app3.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.ehan.app3.ChatViewModel
import com.ehan.app3.bot.baileys.BaileysSocket
import com.ehan.app3.data.UserPreferences
import com.ehan.app3.models.NetworkMonitor
import com.ehan.app3.ui.theme.AppPalette
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    userPreferences: UserPreferences = UserPreferences(),
    onThemeModeChange: (String) -> Unit = {},
    onAccentColorChange: (String) -> Unit = {},
    onOpenDrawer: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val networkMonitor = remember {
        NetworkMonitor(
            context.applicationContext
        )
    }

    DisposableEffect(networkMonitor) {
        networkMonitor.start()

        onDispose {
            networkMonitor.stop()
        }
    }

    val isOnline by networkMonitor.isOnline.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var inputText by rememberSaveable {
        mutableStateOf("")
    }

    var showThemeStudio by rememberSaveable {
        mutableStateOf(false)
    }

    var selectedMessageIds by remember {
        mutableStateOf(emptySet<Long>())
    }

    val isSelectionMode = selectedMessageIds.isNotEmpty()

    // Bersihkan ID terpilih yang mungkin sudah tidak ada di daftar messages
    LaunchedEffect(messages) {
        if (selectedMessageIds.isNotEmpty()) {
            val validIds = messages.map { it.id }.toSet()
            selectedMessageIds = selectedMessageIds.intersect(validIds)
        }
    }

    // Tekan tombol Back sistem untuk membatalkan mode pilih ala WhatsApp
    BackHandler(enabled = isSelectionMode) {
        selectedMessageIds = emptySet()
    }

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isOnline) {
        viewModel.updateNetworkStatus(isOnline)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message = message)
                viewModel.clearError()
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val activePalette = AppPalette.fromKey(userPreferences.accentColor)

    Scaffold(
        topBar = {
            ChatHeader(
                isOnline = isOnline,
                currentThemeMode = userPreferences.themeMode,
                currentAccentColor = userPreferences.accentColor,
                showThemeStudio = showThemeStudio && !isSelectionMode,
                selectedCount = selectedMessageIds.size,
                onCancelSelection = {
                    selectedMessageIds = emptySet()
                },
                onCopySelected = {
                    if (selectedMessageIds.size == 1) {
                        val selectedId = selectedMessageIds.first()
                        val msg = messages.find { it.id == selectedId }
                        if (msg != null) {
                            val payload = BaileysSocket.parsePayload(msg.payloadJson)
                            val copyText = payload?.caption ?: payload?.text ?: msg.text
                            clipboardManager.setText(AnnotatedString(copyText))
                            selectedMessageIds = emptySet()
                            scope.launch {
                                snackbarHostState.showSnackbar("Pesan disalin ke clipboard")
                            }
                        }
                    }
                },
                onDeleteSelected = {
                    val count = selectedMessageIds.size
                    if (count > 0) {
                        viewModel.deleteMessages(selectedMessageIds)
                        selectedMessageIds = emptySet()
                        scope.launch {
                            snackbarHostState.showSnackbar("$count pesan dihapus")
                        }
                    }
                },
                onToggleThemeStudio = { showThemeStudio = !showThemeStudio },
                onThemeModeChange = { mode ->
                    onThemeModeChange(mode)
                },
                onAccentColorChange = { color ->
                    onAccentColorChange(color)
                },
                onClearChat = {
                    viewModel.clearChat()
                    selectedMessageIds = emptySet()
                    scope.launch {
                        snackbarHostState.showSnackbar("Riwayat percakapan dibersihkan")
                    }
                },
                onOpenDrawer = onOpenDrawer
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            MessageInput(
                text = inputText,
                currentAccentColor = userPreferences.accentColor,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                onCommand = { command ->
                    viewModel.sendMessage(command)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            activePalette.previewPrimary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background,
                            activePalette.previewSecondary.copy(alpha = 0.06f)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 10.dp,
                    top = 14.dp,
                    bottom = 16.dp
                ),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty() && !isTyping) {
                    item {
                        EmptyChatState(
                            currentAccentColor = userPreferences.accentColor,
                            onCommandClick = { command ->
                                viewModel.sendMessage(command)
                            }
                        )
                    }
                } else {
                    items(
                        items = messages,
                        key = { it.id }
                    ) { message ->
                        val isSelected = message.id in selectedMessageIds
                        MessageBubble(
                            message = message,
                            currentAccentColor = userPreferences.accentColor,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onToggleSelect = {
                                selectedMessageIds = if (isSelected) {
                                    selectedMessageIds - message.id
                                } else {
                                    selectedMessageIds + message.id
                                }
                            },
                            onLongPressSelect = {
                                selectedMessageIds = if (isSelected) {
                                    selectedMessageIds - message.id
                                } else {
                                    selectedMessageIds + message.id
                                }
                            },
                            onCopied = { snackbarMsg ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(snackbarMsg)
                                }
                            },
                            onSendCommand = { commandId ->
                                if (isSelectionMode) {
                                    selectedMessageIds = if (isSelected) {
                                        selectedMessageIds - message.id
                                    } else {
                                        selectedMessageIds + message.id
                                    }
                                } else {
                                    viewModel.sendMessage(commandId)
                                }
                            }
                        )
                    }

                    if (isTyping) {
                        item {
                            TypingIndicator(currentAccentColor = userPreferences.accentColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator(currentAccentColor: String = "Indigo") {
    val activePalette = AppPalette.fromKey(currentAccentColor)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            border = BorderStroke(1.dp, activePalette.previewPrimary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(activePalette.previewPrimary)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(activePalette.previewSecondary)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(activePalette.previewTertiary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Bot sedang mengetik...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun MessageInput(
    text: String,
    currentAccentColor: String = "Indigo",
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onCommand: (String) -> Unit
) {
    val activePalette = AppPalette.fromKey(currentAccentColor)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 10.dp, end = 12.dp, bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickCommandList.forEach { item ->
                    AssistChip(
                        onClick = { onCommand(item.command) },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = item.badgeColor,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            width = 1.dp,
                            color = item.badgeColor.copy(alpha = 0.45f)
                        ),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = item.badgeColor.copy(alpha = 0.10f),
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("message_input_field"),
                    placeholder = {
                        Text(
                            text = "Tulis pesan atau ketik /menu...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        focusedBorderColor = activePalette.previewPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                val enabled = text.isNotBlank()
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            if (enabled) {
                                Brush.linearGradient(
                                    listOf(
                                        activePalette.previewPrimary,
                                        activePalette.previewSecondary
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onSend,
                        enabled = enabled,
                        modifier = Modifier.testTag("send_message_button"),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Kirim"
                        )
                    }
                }
            }
        }
    }
}
