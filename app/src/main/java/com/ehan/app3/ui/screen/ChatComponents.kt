package com.ehan.app3.ui.screen

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ehan.app3.data.ChatMessage
import com.ehan.app3.ui.theme.AppPalette
import com.ehan.app3.ui.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuickCommandItem(
    val command: String,
    val label: String,
    val description: String,
    val icon: ImageVector,
    val badgeColor: Color
)

val QuickCommandList = listOf(
    QuickCommandItem(
        command = "/tiktok",
        label = "TikTok HD",
        description = "Unduh video & slide tanpa WM",
        icon = Icons.Filled.VideoFile,
        badgeColor = Color(0xFFE11D48)
    ),
    QuickCommandItem(
        command = "/menu",
        label = "Menu Utama",
        description = "Daftar fitur populer bot",
        icon = Icons.Filled.GridView,
        badgeColor = Color(0xFF6366F1)
    ),
    QuickCommandItem(
        command = "/ai Halo! Perkenalkan dirimu",
        label = "Tanya AI",
        description = "Mulai percakapan pintar",
        icon = Icons.Filled.AutoAwesome,
        badgeColor = Color(0xFFEC4899)
    ),
    QuickCommandItem(
        command = "/download",
        label = "Downloader",
        description = "Panduan unduh media & MP3",
        icon = Icons.Filled.Download,
        badgeColor = Color(0xFF10B981)
    ),
    QuickCommandItem(
        command = "/tools",
        label = "Tools",
        description = "Utilitas & alat bantu cepat",
        icon = Icons.Filled.Build,
        badgeColor = Color(0xFF0EA5E9)
    ),
    QuickCommandItem(
        command = "/style",
        label = "Gaya Teks",
        description = "Format teks unik & tebal",
        icon = Icons.Filled.FormatPaint,
        badgeColor = Color(0xFFF59E0B)
    ),
    QuickCommandItem(
        command = "/calc 125 * 8",
        label = "Kalkulator",
        description = "Hitung ekspresi matematika",
        icon = Icons.Filled.Calculate,
        badgeColor = Color(0xFF8B5CF6)
    ),
    QuickCommandItem(
        command = "/status",
        label = "Status Sistem",
        description = "Cek kondisi engine & AI",
        icon = Icons.Filled.Speed,
        badgeColor = Color(0xFF14B8A6)
    )
)

private data class ParsedActionLink(
    val label: String,
    val url: String,
    val isCover: Boolean
)

private val MARKDOWN_LINK_REGEX = Regex("""•?\s*🖼️?\s*\[([^\]]+)]\((https?://[^)]+)\)""")

private fun parseMessageContent(rawText: String): Pair<String, List<ParsedActionLink>> {
    val links = mutableListOf<ParsedActionLink>()
    val cleanedLines = mutableListOf<String>()

    rawText.lines().forEach { line ->
        val match = MARKDOWN_LINK_REGEX.find(line)
        if (match != null) {
            val label = match.groupValues[1].trim()
            val url = match.groupValues[2].trim()
            val isCover = label.contains("Thumbnail", ignoreCase = true) ||
                label.contains("Cover", ignoreCase = true)
            links.add(ParsedActionLink(label = label, url = url, isCover = isCover))
        } else {
            cleanedLines.add(line)
        }
    }

    val cleanText = cleanedLines
        .joinToString("\n")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()

    return cleanText to links
}

private fun enqueueFileDownload(
    context: Context,
    url: String,
    label: String,
    onResult: (String) -> Unit
) {
    try {
        val ext = when {
            label.contains("MP3", ignoreCase = true) || label.contains("Audio", ignoreCase = true) -> "mp3"
            label.contains("Foto", ignoreCase = true) || label.contains("Slide", ignoreCase = true) -> "jpg"
            else -> "mp4"
        }
        val fileName = "TikTok_${System.currentTimeMillis()}.$ext"
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(label)
            .setDescription("Mengunduh $fileName")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (dm != null) {
            dm.enqueue(request)
            onResult("Mengunduh $fileName ke folder Downloads...")
        } else {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onResult("Membuka tautan unduhan di browser...")
        }
    } catch (e: Exception) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onResult("Membuka tautan unduhan...")
        } catch (_: Exception) {
            onResult("Gagal membuka tautan unduhan.")
        }
    }
}

@Composable
fun ChatHeader(
    isOnline: Boolean,
    currentThemeMode: String,
    currentAccentColor: String,
    showThemeStudio: Boolean,
    onToggleThemeStudio: () -> Unit,
    onThemeModeChange: (String) -> Unit,
    onAccentColorChange: (String) -> Unit,
    onClearChat: () -> Unit
) {
    val activePalette = AppPalette.fromKey(currentAccentColor)
    val headerBrush = Brush.horizontalGradient(
        colors = listOf(
            activePalette.previewPrimary,
            activePalette.previewSecondary,
            activePalette.previewTertiary
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerBrush)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = "Bot Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "App3 Bot Studio",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = activePalette.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOnline) Color(0xFF4ADE80) else Color(0xFFFBBF24)
                            )
                    )
                    Text(
                        text = if (isOnline) "Online • Siap merespon perintah" else "Offline • Mode Lokal Aktif",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }

            IconButton(
                onClick = onToggleThemeStudio,
                modifier = Modifier.testTag("toggle_theme_studio_button"),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (showThemeStudio) Color.White else Color.White.copy(alpha = 0.18f),
                    contentColor = if (showThemeStudio) activePalette.previewPrimary else Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = "Ubah Warna & Tema"
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onClearChat,
                modifier = Modifier.testTag("clear_chat_button"),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.18f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteSweep,
                    contentDescription = "Hapus semua chat"
                )
            }
        }

        AnimatedVisibility(
            visible = showThemeStudio,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎨 Tema & Warna Aksen",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = activePalette.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppPalette.entries.forEach { palette ->
                            val isSelected = palette == activePalette
                            val swatchBrush = Brush.linearGradient(
                                listOf(
                                    palette.previewPrimary,
                                    palette.previewSecondary,
                                    palette.previewTertiary
                                )
                            )
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onAccentColorChange(palette.key) }
                                    .testTag("palette_${palette.key.lowercase()}"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                },
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) palette.previewPrimary else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(swatchBrush),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = palette.displayName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val modes = listOf(
                            Triple(ThemeMode.LIGHT.label, "Terang", Icons.Filled.LightMode),
                            Triple(ThemeMode.DARK.label, "Gelap", Icons.Filled.DarkMode),
                            Triple(ThemeMode.SYSTEM.label, "Otomatis", Icons.Filled.SettingsBrightness)
                        )
                        modes.forEach { (modeKey, label, icon) ->
                            val selected = currentThemeMode == modeKey
                            FilterChip(
                                selected = selected,
                                onClick = { onThemeModeChange(modeKey) },
                                label = { Text(label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_mode_${modeKey.lowercase().replace(" ", "_")}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatState(
    currentAccentColor: String,
    onCommandClick: (String) -> Unit
) {
    val activePalette = AppPalette.fromKey(currentAccentColor)
    val heroGradient = Brush.linearGradient(
        colors = listOf(
            activePalette.previewPrimary,
            activePalette.previewSecondary,
            activePalette.previewTertiary
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(heroGradient)
                    .padding(22.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "ASISTEN BOT & TIKTOK DOWNLOADER",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Halo! Saya App3 Bot 🤖",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tempel link TikTok secara langsung untuk unduh Video HD / Slide Foto tanpa watermark, atau pilih kartu perintah cepat di bawah.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ Pintasan Perintah Cepat",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Ketuk untuk jalankan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val chunkedCommands = QuickCommandList.chunked(2)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            chunkedCommands.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onCommandClick(item.command) }
                                .testTag("quick_card_${item.label.lowercase().replace(" ", "_")}"),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            shadowElevation = 2.dp,
                            border = BorderStroke(
                                width = 1.dp,
                                color = item.badgeColor.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(item.badgeColor.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = item.badgeColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = item.badgeColor.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = item.command.substringBefore(" "),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = item.badgeColor,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    currentAccentColor: String,
    onCopied: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val activePalette = AppPalette.fromKey(currentAccentColor)
    val timeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    val userBubbleBrush = Brush.linearGradient(
        colors = listOf(
            activePalette.previewPrimary,
            activePalette.previewSecondary
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (message.isBot) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        if (message.isBot) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(activePalette.previewPrimary, activePalette.previewTertiary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = "Bot",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        val bubbleShape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = if (message.isBot) 4.dp else 20.dp,
            bottomEnd = if (message.isBot) 20.dp else 4.dp
        )

        if (message.isBot) {
            val (displayText, actionLinks) = parseMessageContent(message.text)
            val coverLink = actionLinks.firstOrNull { it.isCover }
            val downloadLinks = actionLinks.filterNot { it.isCover }

            Surface(
                modifier = Modifier.widthIn(max = 335.dp),
                shape = bubbleShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 2.dp,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 14.dp,
                        top = 10.dp,
                        end = 10.dp,
                        bottom = 8.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "App3 Bot",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = displayText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(message.text))
                                onCopied("Pesan disalin ke clipboard")
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Salin pesan",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    if (coverLink != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        AsyncImage(
                            model = coverLink.url,
                            contentDescription = "TikTok Cover Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(165.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }

                    if (downloadLinks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            downloadLinks.forEachIndexed { idx, link ->
                                val isPrimaryHd = idx == 0
                                val icon = when {
                                    link.label.contains("Audio", ignoreCase = true) ||
                                        link.label.contains("MP3", ignoreCase = true) -> Icons.Filled.MusicNote
                                    link.label.contains("Foto", ignoreCase = true) ||
                                        link.label.contains("Slide", ignoreCase = true) -> Icons.Filled.PhotoLibrary
                                    else -> Icons.Filled.Download
                                }

                                if (isPrimaryHd) {
                                    Button(
                                        onClick = {
                                            enqueueFileDownload(
                                                context = context,
                                                url = link.url,
                                                label = link.label,
                                                onResult = onCopied
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = activePalette.previewPrimary,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = link.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            enqueueFileDownload(
                                                context = context,
                                                url = link.url,
                                                label = link.label,
                                                onResult = onCopied
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, activePalette.previewPrimary.copy(alpha = 0.5f)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = activePalette.previewPrimary,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = link.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.OpenInNew,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .clip(bubbleShape)
                    .background(userBubbleBrush)
                    .padding(horizontal = 15.dp, vertical = 10.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$timeFormatted • ${if (message.status == "PROCESSED") "✓✓" else "✓"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
