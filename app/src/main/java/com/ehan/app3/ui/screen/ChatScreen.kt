package com.ehan.app3.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.ehan.app3.ChatViewModel
import com.ehan.app3.models.NetworkMonitor
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel
) {
    val context = LocalContext.current

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

    val listState = rememberLazyListState()

    val snackbarHostState =
        remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    LaunchedEffect(isOnline) {
        viewModel.updateNetworkStatus(isOnline)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->

            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message
                )

                viewModel.clearError()
            }
        }
    }

    LaunchedEffect(messages.size) {

        if (messages.isNotEmpty()) {

            listState.animateScrollToItem(
                messages.lastIndex
            )
        }
    }

    Scaffold(
        topBar = {
            ChatHeader(
                isOnline = isOnline,
                onClearChat = {
                    viewModel.clearChat()
                }
            )
        },

        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },

        bottomBar = {
            MessageInput(
                text = inputText,

                onTextChange = {
                    inputText = it
                },

                onSend = {
                    if (inputText.isNotBlank()) {

                        viewModel.sendMessage(
                            inputText
                        )

                        inputText = ""
                    }
                },

                onCommand = { command ->

                    viewModel.sendMessage(
                        command
                    )
                }
            )
        }
    ) { innerPadding ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),

                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = 12.dp,
                    bottom = 12.dp
                ),

                state = listState,

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                items(
                    items = messages,
                    key = { it.id }
                ) { message ->

                    MessageBubble(
                        message = message
                    )
                }

                if (isTyping) {

                    item {

                        Text(
                            text = "Bot sedang mengetik...",

                            modifier = Modifier.padding(
                                start = 8.dp,
                                bottom = 8.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatHeader(
    isOnline: Boolean,
    onClearChat: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),

        color = Color(0xFF075E54)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "App3 Bot",
                    color = Color.White
                )

                Text(
                    text = if (isOnline) {
                        "● Online"
                    } else {
                        "● Offline"
                    },
                    color = Color.White.copy(
                        alpha = 0.8f
                    )
                )
            }

            IconButton(
                onClick = onClearChat
            ) {
                Icon(
                    imageVector =
                        Icons.Filled.Delete,

                    contentDescription =
                        "Hapus semua chat",

                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: com.ehan.app3.data.ChatMessage
) {
    val clipboardManager =
        LocalClipboardManager.current

    Row(
        modifier = Modifier.fillMaxWidth(),

        horizontalArrangement =
            if (message.isBot) {
                Arrangement.Start
            } else {
                Arrangement.End
            }
    ) {

        Surface(
            shape = RoundedCornerShape(12.dp),

            color =
                if (message.isBot) {
                    Color.White
                } else {
                    Color(0xFFD9FDD3)
                }
        ) {

            Row(
                modifier = Modifier.padding(
                    start = 12.dp,
                    top = 8.dp,
                    end = 4.dp,
                    bottom = 8.dp
                ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = message.text
                )

                if (message.isBot) {

                    Spacer(
                        modifier = Modifier.width(4.dp)
                    )

                    IconButton(
                        onClick = {

                            clipboardManager.setText(
                                AnnotatedString(
                                    message.text
                                )
                            )
                        }
                    ) {

                        Text(
                            text = "⧉",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onCommand: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        rememberScrollState()
                    ),

                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                AssistChip(
                    onClick = {
                        onCommand("/menu")
                    },
                    label = {
                        Text("Menu")
                    }
                )

                AssistChip(
                    onClick = {
                        onCommand("/download")
                    },
                    label = {
                        Text("Download")
                    }
                )

                AssistChip(
                    onClick = {
                        onCommand("/ai")
                    },
                    label = {
                        Text("AI")
                    }
                )

                AssistChip(
                    onClick = {
                        onCommand("/tools")
                    },
                    label = {
                        Text("Tools")
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                TextField(
                    value = text,

                    onValueChange =
                        onTextChange,

                    modifier =
                        Modifier.weight(1f),

                    placeholder = {
                        Text("Tulis pesan...")
                    },

                    singleLine = true
                )

                IconButton(
                    onClick = onSend,

                    enabled =
                        text.isNotBlank()
                ) {

                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.Send,

                        contentDescription =
                            "Kirim"
                    )
                }
            }
        }
    }
}