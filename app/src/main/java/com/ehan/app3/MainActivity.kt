package com.ehan.app3

import android.os.Bundle
import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
// import androidx.compose.foundation.layout.weight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ehan.app3.ui.theme.App3Theme
import com.ehan.app3.ui.theme.ThemeMode
import com.ehan.app3.data.UserPreferencesRepository
import com.ehan.app3.ui.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewmodel: MainViewModel by viewModels {
        MainViewModel.provideFactory(application)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context: Context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            val userPreferences by viewmodel.userPreferences.collectAsStateWithLifecycle(lifecycleOwner = lifecycleOwner)
            val statusNotification by viewmodel.statusMessage.collectAsStateWithLifecycle(lifecycleOwner = lifecycleOwner)
            App3Theme(
                darkTheme = when (userPreferences.themeMode) {
                    ThemeMode.LIGHT.label -> {
                        false
                    }
                    ThemeMode.DARK.label -> {
                        true
                    }
                    else -> {
                        isSystemInDarkTheme() ?: false
                    }
                }
            ) {
                Greeting(
                    name = "Template",
                    viewmodel = viewmodel
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, viewmodel: MainViewModel, modifier: Modifier = Modifier) {
    val context: Context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val userPreferences by viewmodel.userPreferences.collectAsStateWithLifecycle(lifecycleOwner = lifecycleOwner)
    val statusNotification by viewmodel.statusMessage.collectAsStateWithLifecycle(lifecycleOwner = lifecycleOwner)
    Scaffold { innerPadding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ChatScreen()
            /**
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = name
                )
                Button(
                    onClick = {
                        when (userPreferences.themeMode) {
                            ThemeMode.DARK.label -> {
                                viewmodel.setThemeMode((ThemeMode.LIGHT).label)
                            }
                            ThemeMode.LIGHT.label -> {
                                viewmodel.setThemeMode(ThemeMode.SYSTEM.label)
                            }
                            else -> {
                                viewmodel.setThemeMode(ThemeMode.DARK.label)
                            }
                        }
                    }
                ) {
                    Text(
                        text = "tema mode"
                    )
                }
            }*/
        }
    }
}

data class ChatMessage(
    val text: String,
    val isBot: Boolean
)

@Composable
fun ChatScreen() {

    var inputText by rememberSaveable {
        mutableStateOf("")
    }

    val messages = rememberSaveable {
        mutableStateListOf(
            ChatMessage(
                text = "Halo! Saya bot sederhana 🤖",
                isBot = true
            ),
            ChatMessage(
                text = "Ketik \"menu\" untuk melihat perintah.",
                isBot = true
            )
        )
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // Header
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
                    fontSize = 20.sp,
                    color = Color(0xFF075E54)
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
                    text = "● Online",
                    color = Color(0xFFB9F6CA),
                    fontSize = 13.sp
                )
            }
        }

        // Daftar pesan
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(messages) { message ->

                MessageBubble(
                    message = message
                )
            }
        }

        // Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    inputText = it
                },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Ketik pesan...")
                },
                maxLines = 3
            )

            IconButton(
                onClick = {

                    val text = inputText.trim()

                    if (text.isNotEmpty()) {

                        // Pesan user
                        messages.add(
                            ChatMessage(
                                text = text,
                                isBot = false
                            )
                        )

                        // Balasan bot
                        messages.add(
                            ChatMessage(
                                text = botReply(text),
                                isBot = true
                            )
                        )

                        inputText = ""

                        scope.launch {
                            listState.animateScrollToItem(
                                messages.size - 1
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Kirim"
                )
            }
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
                fontSize = 15.sp
            )
        }
    }
}

fun botReply(message: String): String {

    return when (message.lowercase().trim()) {

        "halo",
        "hai",
        "hello" ->
            "Halo! Ada yang bisa saya bantu? 🤖"

        "menu" ->
            """
            Menu Bot:

            1. halo
            2. menu
            3. info
            """.trimIndent()

        "info" ->
            "Saya adalah bot Android sederhana yang dibuat menggunakan Kotlin dan Jetpack Compose."

        else ->
            "Maaf, saya belum mengerti pesan itu."
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    App3Theme {
        Scaffold { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Android"
                    )
                }
            }
        }
    }
}