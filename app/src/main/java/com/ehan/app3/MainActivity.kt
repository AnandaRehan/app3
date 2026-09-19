package com.ehan.app3

import android.os.Bundle
import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
// import androidx.compose.foundation.layout.weight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
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
                darkTheme = viewmodel.darkTheme
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
                            ThemeMode.DARK -> {
                                viewmodel.setThemeMode(ThemeMode.LIGHT)
                            }
                            ThemeMode.LIGHT -> {
                                viewmodel.setThemeMode(ThemeMode.SYSTEM)
                            }
                            else -> {
                                viewmodel.setThemeMode(ThemeMode.DARK)
                            }
                        }
                    }
                ) {
                    Text(
                        text = "tema mode"
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    private val viewmodel: MainViewModel by viewModels {
        MainViewModel.provideFactory(application)
    }
    App3Theme {
        Greeting("Android", viewmodel)
    }
}