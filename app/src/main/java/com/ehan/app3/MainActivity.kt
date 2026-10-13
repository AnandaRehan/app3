package com.ehan.app3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ehan.app3.data.ChatDatabase
import com.ehan.app3.data.DownloadedMediaRepository
import com.ehan.app3.models.NetworkMonitor
import com.ehan.app3.ui.MainViewModel
import com.ehan.app3.ui.screen.ChatScreen
import com.ehan.app3.ui.screen.GalleryScreen
import com.ehan.app3.service.FloatingDownloaderService
import com.ehan.app3.ui.theme.App3Theme
import com.ehan.app3.ui.theme.AppPalette
import com.ehan.app3.ui.theme.ThemeMode
import kotlinx.coroutines.launch

enum class AppScreen {
    CHAT,
    GALLERY
}

class MainActivity : ComponentActivity() {
    private val viewmodel: MainViewModel by viewModels {
        MainViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = ChatDatabase.getDatabase(this)
        val downloadedMediaRepository = DownloadedMediaRepository(database.downloadedMediaDao())

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

                MainAppRoot(
                    viewmodel = viewmodel,
                    chatViewModel = chatViewModel,
                    downloadedMediaRepository = downloadedMediaRepository
                )
            }
        }
    }
}

@Composable
fun MainAppRoot(
    viewmodel: MainViewModel,
    chatViewModel: ChatViewModel,
    downloadedMediaRepository: DownloadedMediaRepository
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val userPreferences by viewmodel.userPreferences.collectAsStateWithLifecycle(
        lifecycleOwner = lifecycleOwner
    )
    val activePalette = AppPalette.fromKey(userPreferences.accentColor)

    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.CHAT) }

    val mediaCount by downloadedMediaRepository.mediaCount.collectAsState(initial = 0)

    val networkMonitor = remember { NetworkMonitor(context.applicationContext) }
    val isOnline by networkMonitor.isOnline.collectAsState()

    val isFloatingRunning by FloatingDownloaderService.isServiceRunning.collectAsStateWithLifecycle(
        lifecycleOwner = lifecycleOwner
    )
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        FloatingDownloaderService.start(context)
    }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (FloatingDownloaderService.canDrawOverlays(context)) {
            Toast.makeText(context, "Izin hamparan aktif! Memulai pop-up...", Toast.LENGTH_SHORT).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                FloatingDownloaderService.start(context)
            }
        } else {
            Toast.makeText(context, "Izin hamparan layar belum diberikan", Toast.LENGTH_SHORT).show()
        }
    }

    val onToggleFloating: (Boolean) -> Unit = { shouldEnable ->
        if (shouldEnable) {
            if (!FloatingDownloaderService.canDrawOverlays(context)) {
                showOverlayPermissionDialog = true
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    FloatingDownloaderService.start(context)
                }
            }
        } else {
            FloatingDownloaderService.stop(context)
        }
    }

    // Handle Back Press
    BackHandler(enabled = drawerState.isOpen || currentScreen != AppScreen.CHAT) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (currentScreen != AppScreen.CHAT) {
            currentScreen = AppScreen.CHAT
        }
    }

    val headerGradient = Brush.horizontalGradient(
        colors = listOf(
            activePalette.previewPrimary,
            activePalette.previewSecondary,
            activePalette.previewTertiary
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxSize(),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) {
                    // Header Profil Laci
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerGradient)
                            .statusBarsPadding()
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.22f))
                                        .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SmartToy,
                                        contentDescription = "Bot Avatar",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "App3 Studio",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "AI & Media Downloader",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Status Pill & Accent Pill
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                        Text(
                                            text = if (isOnline) "Online" else "Offline",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Surface(
                                    color = Color.White.copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = activePalette.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Menu Navigasi Utama
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "NAVIGASI UTAMA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )

                        // 1. Chat Bot
                        NavigationDrawerItem(
                            icon = {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                            },
                            label = {
                                Text(
                                    "Chat Bot Studio",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (currentScreen == AppScreen.CHAT) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = currentScreen == AppScreen.CHAT,
                            onClick = {
                                currentScreen = AppScreen.CHAT
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = activePalette.previewPrimary.copy(alpha = 0.15f),
                                selectedIconColor = activePalette.previewPrimary,
                                selectedTextColor = activePalette.previewPrimary
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        // 2. Galeri & Riwayat Unduhan
                        NavigationDrawerItem(
                            icon = {
                                Icon(Icons.Filled.Movie, contentDescription = null)
                            },
                            label = {
                                Text(
                                    "Galeri & Unduhan",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (currentScreen == AppScreen.GALLERY) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            badge = {
                                if (mediaCount > 0) {
                                    Badge(
                                        containerColor = activePalette.previewPrimary,
                                        contentColor = Color.White
                                    ) {
                                        Text("$mediaCount")
                                    }
                                }
                            },
                            selected = currentScreen == AppScreen.GALLERY,
                            onClick = {
                                currentScreen = AppScreen.GALLERY
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = activePalette.previewPrimary.copy(alpha = 0.15f),
                                selectedIconColor = activePalette.previewPrimary,
                                selectedTextColor = activePalette.previewPrimary
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        // 3. Pop-up Downloader Melayang Card with Switch
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isFloatingRunning) {
                                    activePalette.previewPrimary.copy(alpha = 0.12f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                }
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isFloatingRunning) activePalette.previewPrimary.copy(alpha = 0.45f) else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isFloatingRunning) activePalette.previewPrimary else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Download,
                                            contentDescription = "Floating Pop-up Downloader",
                                            tint = if (isFloatingRunning) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Pop-up Melayang",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isFloatingRunning) "Aktif di layar (Siaga unduh)" else "Unduh otomatis dari clipboard",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isFloatingRunning) activePalette.previewPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Switch(
                                    checked = isFloatingRunning,
                                    onCheckedChange = onToggleFloating,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = activePalette.previewPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Setting: Pilihan Tema Warna
                        Text(
                            text = "PILIHAN TEMA WARNA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppPalette.values().forEach { palette ->
                                val isSelected = palette.key == activePalette.key
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(palette.previewPrimary, palette.previewSecondary)
                                            )
                                        )
                                        .clickable {
                                            viewmodel.selectAccentColor(palette.key)
                                        }
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Mode Tampilan (Terang / Gelap / Sistem)
                        Text(
                            text = "MODE TAMPILAN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple(ThemeMode.SYSTEM, Icons.Default.BrightnessAuto, "Otomatis"),
                                Triple(ThemeMode.LIGHT, Icons.Default.LightMode, "Terang"),
                                Triple(ThemeMode.DARK, Icons.Default.DarkMode, "Gelap")
                            ).forEach { (mode, icon, label) ->
                                val isSelected = userPreferences.themeMode == mode.label
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewmodel.setThemeMode(mode.label) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) activePalette.previewPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Footer Laci
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "App3 Bot Studio v2.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "TikTok & YouTube Downloader Ready",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        )
                    }
                }
            }
        }
    ) {
        when (currentScreen) {
            AppScreen.CHAT -> {
                ChatScreen(
                    viewModel = chatViewModel,
                    userPreferences = userPreferences,
                    onThemeModeChange = { mode ->
                        viewmodel.setThemeMode(mode)
                    },
                    onAccentColorChange = { color ->
                        viewmodel.selectAccentColor(color)
                    },
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
            AppScreen.GALLERY -> {
                GalleryScreen(
                    repository = downloadedMediaRepository,
                    currentAccentColor = userPreferences.accentColor,
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    },
                    onNavigateToChat = {
                        currentScreen = AppScreen.CHAT
                    }
                )
            }
        }
    }

    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Layers,
                    contentDescription = null,
                    tint = activePalette.previewPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Izin Hamparan Layar Diperlukan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Agar pop-up bubble downloader dapat melayang di atas aplikasi lain (seperti TikTok atau YouTube) dan otomatis mengunduh link dari papan klip tanpa membuka aplikasi, aktifkan izin 'Tampilkan di atas aplikasi lain' di pengaturan ponsel.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverlayPermissionDialog = false
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        overlayPermissionLauncher.launch(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = activePalette.previewPrimary)
                ) {
                    Text("Buka Pengaturan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showOverlayPermissionDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
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
        Greeting("App3 Bot Studio")
    }
}
