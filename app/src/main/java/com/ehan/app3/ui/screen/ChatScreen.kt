package com.ehan.app3.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ehan.app3.data.ChatMessage
import com.ehan.app3.models.NetworkMonitor

@Composable
fun ChatScreen(
    viewModel: ChatViewModel
) {

    // Pesan dari Room
    val messages by viewModel.messages.collectAsState()

    // State input
    var inputText by rememberSaveable {
        androidx.compose.runtime.mutableStateOf("")
    }

    // Network monitor
    val context = LocalContext.current

    val networkMonitor = remember {
        NetworkMonitor(context)
    }

    val isOnline by networkMonitor.isOnline.collectAsState()

    val listState = rememberLazyListState()

    // Scroll otomatis ke pesan terbaru
    LaunchedEffect(messages.size) {

        if (messages.isNotEmpty()) {

            listState.animateScrollToItem(
                messages.lastIndex
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        ChatHeader(
            isOnline = isOnline
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(
                items = messages,
                key = { it.id }
            ) { message ->

                MessageBubble(
                    message = message
                )
            }
        }

        MessageInput(
            text = inputText,
            onTextChange = {
                inputText = it
            },
            onSend = {

                viewModel.sendMessage(inputText)

                inputText = ""
            }
        )
    }
}

@Composable
fun ChatHeader(
    isOnline: Boolean
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF075E54))
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    Color.White,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "B",
                color = Color(0xFF075E54),
                fontSize = 20.sp
            )
        }

        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {

            Text(
                text = "Bot Chat",
                color = Color.White,
                fontSize = 18.sp
            )

            Text(
                text = if (isOnline) {
                    "● Online"
                } else {
                    "● Offline"
                },
                color = if (isOnline) {
                    Color(0xFFB9F6CA)
                } else {
                    Color.LightGray
                },
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isBot) {
            Arrangement.Start
        } else {
            Arrangement.End
        }
    ) {

        Box(
            modifier = Modifier
                .background(
                    color = if (message.isBot) {
                        Color.White
                    } else {
                        Color(0xFFD9FDD3)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 10.dp
                )
        ) {

            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("Ketik pesan...")
            },
            maxLines = 3
        )

        IconButton(
            onClick = onSend
        ) {

            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Kirim"
            )
        }
    }
}