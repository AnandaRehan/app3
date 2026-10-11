package com.ehan.app3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ehan.app3.data.ChatDatabase
import com.ehan.app3.ui.MainViewModel
import com.ehan.app3.ui.screen.ChatScreen
import com.ehan.app3.ui.theme.App3Theme
import com.ehan.app3.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    private val viewmodel: MainViewModel by viewModels {
        MainViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = ChatDatabase.getDatabase(this)

        setContent {
            val lifecycleOwner = LocalLifecycleOwner.current
            val userPreferences by viewmodel.userPreferences.collectAsStateWithLifecycle(
                lifecycleOwner = lifecycleOwner
            )
            val isDark = when (userPreferences.themeMode) {
                ThemeMode.LIGHT.label -> false
                ThemeMode.DARK.label -> true
                else -> isSystemInDarkTheme()
            }

            App3Theme(
                darkTheme = isDark,
                accentColor = userPreferences.accentColor
            ) {
                val chatViewModel: ChatViewModel = viewModel(
                    factory = ChatViewModelFactory(
                        chatDao = database.chatDao()
                    )
                )
                Greeting(
                    name = "App3 Bot",
                    viewmodel = viewmodel,
                    viewModel = chatViewModel
                )
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    viewmodel: MainViewModel,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val userPreferences by viewmodel.userPreferences.collectAsStateWithLifecycle(
        lifecycleOwner = lifecycleOwner
    )

    ChatScreen(
        viewModel = viewModel,
        userPreferences = userPreferences,
        onThemeModeChange = { mode ->
            viewmodel.setThemeMode(mode)
        },
        onAccentColorChange = { color ->
            viewmodel.selectAccentColor(color)
        }
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hello $name!",
                style = MaterialTheme.typography.titleLarge
            )
        }
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
                        text = "App3 Bot Studio",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}
