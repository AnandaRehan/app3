package com.ehan.app3.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.ehan.app3.data.DownloadedMedia
import com.ehan.app3.data.DownloadedMediaRepository
import com.ehan.app3.ui.theme.AppPalette
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MediaFilter(val label: String, val typeKey: String?) {
    ALL("Semua", null),
    VIDEO("🎬 Video", "VIDEO"),
    IMAGE("🖼️ Foto", "IMAGE"),
    AUDIO("🎵 Audio", "AUDIO")
}

@Composable
fun GalleryScreen(
    repository: DownloadedMediaRepository,
    currentAccentColor: String,
    onOpenDrawer: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val activePalette = AppPalette.fromKey(currentAccentColor)
    val allMediaList by repository.allMedia.collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableStateOf(MediaFilter.ALL) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var previewMedia by remember { mutableStateOf<DownloadedMedia?>(null) }

    BackHandler {
        if (previewMedia != null) {
            previewMedia = null
        } else {
            onNavigateToChat()
        }
    }

    val filteredList = remember(allMediaList, selectedFilter) {
        if (selectedFilter.typeKey == null) {
            allMediaList
        } else {
            allMediaList.filter { it.mediaType.equals(selectedFilter.typeKey, ignoreCase = true) }
        }
    }

    val videoCount = remember(allMediaList) {
        allMediaList.count { it.mediaType.equals("VIDEO", ignoreCase = true) }
    }
    val imageCount = remember(allMediaList) {
        allMediaList.count { it.mediaType.equals("IMAGE", ignoreCase = true) }
    }
    val audioCount = remember(allMediaList) {
        allMediaList.count { it.mediaType.equals("AUDIO", ignoreCase = true) }
    }

    val headerBrush = Brush.horizontalGradient(
        colors = listOf(
            activePalette.previewPrimary,
            activePalette.previewSecondary,
            activePalette.previewTertiary
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBrush)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("gallery_drawer_button"),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka menu navigasi"
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Galeri & Unduhan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${allMediaList.size} berkas tersimpan",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    if (allMediaList.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Hapus semua riwayat"
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MediaFilter.values().forEach { filter ->
                    val isSelected = filter == selectedFilter
                    val count = when (filter) {
                        MediaFilter.ALL -> allMediaList.size
                        MediaFilter.VIDEO -> videoCount
                        MediaFilter.IMAGE -> imageCount
                        MediaFilter.AUDIO -> audioCount
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text("${filter.label} ($count)")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = activePalette.previewPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) null else BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            if (filteredList.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(activePalette.previewPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (selectedFilter) {
                                    MediaFilter.VIDEO -> Icons.Default.Videocam
                                    MediaFilter.IMAGE -> Icons.Default.Image
                                    MediaFilter.AUDIO -> Icons.Default.Audiotrack
                                    else -> Icons.Default.Folder
                                },
                                contentDescription = null,
                                tint = activePalette.previewPrimary,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = if (allMediaList.isEmpty()) "Belum Ada Media Tersimpan" else "Tidak Ada Media Kategori Ini",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Kirim tautan TikTok (/tt <url>) atau YouTube (/yt <url>) di Chat Bot untuk mengunduh video, musik, atau foto langsung ke galeri.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onNavigateToChat,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = activePalette.previewPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buka Chat Bot Studio")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredList, key = { it.id }) { media ->
                        MediaItemCard(
                            media = media,
                            accentColor = activePalette.previewPrimary,
                            onPlayOrPreview = {
                                previewMedia = media
                            },
                            onOpenDirect = {
                                openMediaFileOrUrl(context, media)
                            },
                            onShare = {
                                shareMedia(context, media)
                            },
                            onDelete = {
                                scope.launch {
                                    repository.deleteMedia(media)
                                    snackbarHostState.showSnackbar("Media dihapus dari riwayat.")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog Konfirmasi Hapus Semua
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Hapus Semua Riwayat Unduhan?") },
            text = { Text("Daftar riwayat unduhan di galeri aplikasi akan dihapus. Berkas di folder Downloads perangkat Anda tetap aman.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.clearAll()
                            showClearConfirmDialog = false
                            snackbarHostState.showSnackbar("Semua riwayat unduhan telah dibersihkan.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Hapus Semua")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal Pratinjau Media
    previewMedia?.let { media ->
        MediaPreviewDialog(
            media = media,
            accentColor = activePalette.previewPrimary,
            onDismiss = { previewMedia = null },
            onOpenInApp = {
                openMediaFileOrUrl(context, media)
            },
            onShare = {
                shareMedia(context, media)
            }
        )
    }
}

@Composable
fun MediaItemCard(
    media: DownloadedMedia,
    accentColor: Color,
    onPlayOrPreview: () -> Unit,
    onOpenDirect: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val dateStr = remember(media.timestamp) {
        try {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            sdf.format(Date(media.timestamp))
        } catch (_: Exception) {
            ""
        }
    }

    val platformColor = when (media.sourcePlatform.uppercase()) {
        "YOUTUBE" -> Color(0xFFE50914)
        "TIKTOK" -> Color(0xFF000000)
        else -> accentColor
    }

    val typeIcon = when (media.mediaType.uppercase()) {
        "VIDEO" -> Icons.Default.Movie
        "AUDIO" -> Icons.Default.Audiotrack
        else -> Icons.Default.Image
    }

    val typeLabel = when (media.mediaType.uppercase()) {
        "VIDEO" -> "VIDEO"
        "AUDIO" -> "AUDIO MP3"
        else -> "FOTO"
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayOrPreview() },
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail or Type Icon Box
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (!media.thumbnailUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = media.thumbnailUrl,
                            contentDescription = media.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Play overlay indicator
                    if (media.mediaType.equals("VIDEO", ignoreCase = true) ||
                        media.mediaType.equals("AUDIO", ignoreCase = true)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = platformColor,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = media.sourcePlatform.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            color = accentColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = typeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        media.fileSizeFormatted?.takeIf { it.isNotBlank() }?.let { size ->
                            Text(
                                text = size,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    if (media.downloadUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.1f))
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("Link", media.downloadUrl))
                                    Toast.makeText(context, "Link disalin ke clipboard", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Tautan",
                                tint = accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = media.downloadUrl,
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )

            // Card Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onPlayOrPreview,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (media.mediaType.equals("IMAGE", ignoreCase = true)) Icons.Default.Image else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (media.mediaType.equals("IMAGE", ignoreCase = true)) "Lihat Foto" else "Putar",
                        color = accentColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onOpenDirect,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Buka berkas di aplikasi luar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan media",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus dari riwayat",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaPreviewDialog(
    media: DownloadedMedia,
    accentColor: Color,
    onDismiss: () -> Unit,
    onOpenInApp: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onShare,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan")
                    }
                }

                // Main Viewer Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    when (media.mediaType.uppercase()) {
                        "VIDEO" -> {
                            val localFile = media.filePath?.let { path ->
                                val f = File(path)
                                if (f.exists()) f else File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), f.name)
                            }
                            val videoPlayUrl = if (localFile != null && localFile.exists()) {
                                Uri.fromFile(localFile).toString()
                            } else {
                                media.downloadUrl
                            }
                            InlineVideoPlayer(
                                videoUrl = videoPlayUrl,
                                thumbnailUrl = media.thumbnailUrl,
                                fileName = media.filePath ?: media.title,
                                badgeLabel = "${media.sourcePlatform} VIDEO",
                                onDownloadClick = { _, _, _ ->
                                    onOpenInApp()
                                }
                            )
                        }
                        "IMAGE" -> {
                            val localFile = media.filePath?.let { path ->
                                val f = File(path)
                                if (f.exists()) f else File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), f.name)
                            }
                            val imageModel: Any = if (localFile != null && localFile.exists()) {
                                localFile
                            } else {
                                media.thumbnailUrl ?: media.downloadUrl
                            }
                            AsyncImage(
                                model = imageModel,
                                contentDescription = media.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        else -> {
                            // Audio preview
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Audiotrack,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(54.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = media.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "${media.sourcePlatform} Audio MP3 • ${media.fileSizeFormatted.orEmpty()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = onOpenInApp,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Putar Audio di Pemutar Eksternal")
                                }
                            }
                        }
                    }
                }

                // Tampilan Link Pengguna di Dialog
                if (media.downloadUrl.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = media.downloadUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("Link", media.downloadUrl))
                                Toast.makeText(context, "Link disalin ke clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin Tautan",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Bottom Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Platform: ${media.sourcePlatform}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        media.fileSizeFormatted?.let {
                            Text(
                                text = "Ukuran: $it",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenInApp,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Berkas", color = Color.White)
                    }
                }
            }
        }
    }
}

private fun openMediaFileOrUrl(context: Context, media: DownloadedMedia) {
    try {
        // Cek apakah berkas ada di penyimpanan publik Downloads
        val fileName = media.filePath
        if (!fileName.isNullOrBlank()) {
            val file = if (fileName.startsWith("/")) {
                File(fileName)
            } else {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            }
            if (file.exists()) {
                val mimeType = when (media.mediaType.uppercase()) {
                    "VIDEO" -> "video/*"
                    "IMAGE" -> "image/*"
                    "AUDIO" -> "audio/*"
                    else -> "*/*"
                }
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.fromFile(file), mimeType)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
                return
            }
        }

        // Fallback buka URL yang dikirim pengguna di browser / aplikasi terkait
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(media.downloadUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(media.downloadUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Error handling
        }
    }
}

private fun shareMedia(context: Context, media: DownloadedMedia) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, media.title)
            putExtra(
                Intent.EXTRA_TEXT,
                "🎬 ${media.title}\n\nLink: ${media.downloadUrl}"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Media").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (_: Exception) {
        // Ignored
    }
}
