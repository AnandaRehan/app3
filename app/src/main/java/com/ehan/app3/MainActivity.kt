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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ehan.app3.data.ChatDatabase
import com.ehan.app3.ui.theme.App3Theme
import com.ehan.app3.ui.theme.ThemeMode
import com.ehan.app3.data.UserPreferencesRepository
import com.ehan.app3.ui.MainViewModel
import com.ehan.app3.ui.screen.ChatScreen

class MainActivity : ComponentActivity() {
    private val viewmodel: MainViewModel by viewModels {
        MainViewModel.provideFactory(application)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = ChatDatabase.getDatabase(this)

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
                val chatViewModel: ChatViewModel =
                    viewModel(
                        factory = ChatViewModelFactory(
                            chatDao = database.chatDao(),
                            context = applicationContext
                        )
                    )
                Greeting(
                    name = "Template",
                    viewmodel = viewmodel,
                    viewModel = chatViewModel
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, viewmodel: MainViewModel, viewModel: ChatViewModel, modifier: Modifier = Modifier) {
    val context: Context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val userPreferences by viewmodel.userPreferences.collectAsStateWithLifecycle(lifecycleOwner = lifecycleOwner)
    val statusNotification by viewmodel.statusMessage.collectAsStateWithLifecycle(lifecycleOwner = lifecycleOwner)
    ChatScreen(
        viewModel = viewModel
    )
    /**Scaffold { innerPadding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            
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
    }*/
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