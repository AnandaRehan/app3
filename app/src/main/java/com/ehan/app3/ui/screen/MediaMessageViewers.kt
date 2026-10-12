package com.ehan.app3.ui.screen

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.ehan.app3.bot.baileys.MediaSource
import com.ehan.app3.bot.baileys.SlideMediaItem
import com.ehan.app3.bot.baileys.SlideMediaType
import kotlinx.coroutines.launch

@Composable
fun InlineVideoPlayer(
    videoUrl: String,
    thumbnailUrl: String? = null,
    fileName: String? = null,
    badgeLabel: String = "🎬 VIDEO",
    onDownloadClick: (url: String, label: String, fileName: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember(videoUrl) { mutableStateOf(false) }
    var isPrepared by remember(videoUrl) { mutableStateOf(false) }
    var isBuffering by remember(videoUrl) { mutableStateOf(false) }
    var isMuted by remember(videoUrl) { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

    DisposableEffect(videoUrl) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(270.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0B0F19))
    ) {
        if (isPlaying || isPrepared) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        videoViewRef = this
                        setVideoURI(Uri.parse(videoUrl))
                        setOnPreparedListener { mp ->
                            mediaPlayerRef = mp
                            mp.isLooping = true
                            val vol = if (isMuted) 0f else 1f
                            mp.setVolume(vol, vol)
                            isPrepared = true
                            isBuffering = false
                            if (isPlaying) {
                                start()
                            }
                        }
                        setOnErrorListener { _, _, _ ->
                            isBuffering = false
                            isPlaying = false
                            true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        val vv = videoViewRef
                        if (vv != null && isPrepared) {
                            if (vv.isPlaying) {
                                vv.pause()
                                isPlaying = false
                            } else {
                                vv.start()
                                isPlaying = true
                            }
                        }
                    }
            )
        }

        // Thumbnail overlay before video is prepared
        if (!isPrepared && !thumbnailUrl.isNullOrBlank()) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Thumbnail Video",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top Bar: Badge & Quick Download
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.65f)
            ) {
                Text(
                    text = badgeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (isPrepared) {
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            val vol = if (isMuted) 0f else 1f
                            try {
                                mediaPlayerRef?.setVolume(vol, vol)
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.65f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (isMuted) {
                                Icons.AutoMirrored.Filled.VolumeOff
                            } else {
                                Icons.AutoMirrored.Filled.VolumeUp
                            },
                            contentDescription = "Suara",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val resolvedName = fileName ?: "hd-${System.currentTimeMillis()}.mp4"
                        onDownloadClick(videoUrl, "Video ($resolvedName)", resolvedName)
                    },
                    modifier = Modifier.size(34.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.65f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = "Unduh Video",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Center Play / Pause / Buffering Overlay
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            if (isBuffering) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            } else if (!isPlaying) {
                IconButton(
                    onClick = {
                        isPlaying = true
                        if (!isPrepared) {
                            isBuffering = true
                        } else {
                            videoViewRef?.start()
                        }
                    },
                    modifier = Modifier.size(58.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.65f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Putar Video",
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }

        // Bottom Pause pill when playing
        if (isPlaying && isPrepared) {
            IconButton(
                onClick = {
                    videoViewRef?.pause()
                    isPlaying = false
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(34.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Pause,
                    contentDescription = "Jeda Video",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SingleImageViewer(
    image: MediaSource,
    onDownloadClick: (url: String, label: String, fileName: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(270.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = image.url,
            contentDescription = "Foto TikTok",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.65f)
            ) {
                Text(
                    text = "📸 FOTO",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            IconButton(
                onClick = {
                    val resolvedName = image.fileName ?: "img-${System.currentTimeMillis()}.jpg"
                    onDownloadClick(image.url, "Foto ($resolvedName)", resolvedName)
                },
                modifier = Modifier.size(34.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.65f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Unduh Foto",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Slider ala TikTok yang menyatukan Slide Foto dan Slide Video (Live Photo)
 * di dalam satu HorizontalPager interaktif.
 */
@Composable
fun TikTokCarouselViewer(
    slides: List<SlideMediaItem>,
    accentColor: Color,
    onDownloadClick: (url: String, label: String, fileName: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (slides.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(285.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0B0F19))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val item = slides[page]
                val hasBothPhotoAndVideo = !item.videoUrl.isNullOrBlank() && !item.imageUrl.isNullOrBlank()
                var showLiveVideo by remember(page) {
                    mutableStateOf(item.type == SlideMediaType.VIDEO && !item.videoUrl.isNullOrBlank())
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (showLiveVideo && !item.videoUrl.isNullOrBlank()) {
                        val vidFileName = item.videoFileName
                            ?: item.fileName
                            ?: "hd-${System.currentTimeMillis()}-slide${page + 1}.mp4"
                        InlineVideoPlayer(
                            videoUrl = item.videoUrl,
                            thumbnailUrl = item.imageUrl,
                            fileName = vidFileName,
                            badgeLabel = "🎞️ LIVE SLIDE ${page + 1}/${slides.size}",
                            onDownloadClick = { url, label, fn ->
                                onDownloadClick(url, label, fn)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val photoUrl = item.imageUrl ?: item.url
                        val imgFileName = item.imageFileName
                            ?: item.fileName
                            ?: "img-${System.currentTimeMillis()}-slide${page + 1}.jpg"
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = item.title ?: "Slide ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top badge + download for photo slide
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .align(Alignment.TopCenter),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.Black.copy(alpha = 0.65f)
                            ) {
                                Text(
                                    text = "🖼️ SLIDE ${page + 1}/${slides.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    onDownloadClick(photoUrl, "Foto Slide #${page + 1}", imgFileName)
                                },
                                modifier = Modifier.size(34.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color.Black.copy(alpha = 0.65f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = "Unduh Slide",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Toggle antara Live Video & Foto Statis jika slide memiliki keduanya
                    if (hasBothPhotoAndVideo) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { showLiveVideo = !showLiveVideo },
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.72f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (showLiveVideo) Icons.Filled.Image else Icons.Filled.Videocam,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (showLiveVideo) "Lihat Foto" else "Putar Live Video",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Left / Right Quick Navigation Arrows
            if (pagerState.currentPage > 0) {
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp)
                        .size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Slide Sebelumnya"
                    )
                }
            }

            if (pagerState.currentPage < slides.lastIndex) {
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                        .size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Slide Berikutnya"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TikTok-style Page Indicator Dots + Quick Download Current Slide
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                slides.forEachIndexed { index, slideItem ->
                    val isSelected = index == pagerState.currentPage
                    val isVideoSlide = slideItem.type == SlideMediaType.VIDEO
                    Box(
                        modifier = Modifier
                            .height(7.dp)
                            .width(if (isSelected) 20.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> accentColor
                                    isVideoSlide -> accentColor.copy(alpha = 0.45f)
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                            .clickable {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }

            val currentSlide = slides[pagerState.currentPage]
            val currentFileName = currentSlide.fileName ?: if (currentSlide.type == SlideMediaType.VIDEO) {
                "hd-${System.currentTimeMillis()}-slide${pagerState.currentPage + 1}.mp4"
            } else {
                "img-${System.currentTimeMillis()}-slide${pagerState.currentPage + 1}.jpg"
            }
            val currentLabel = if (currentSlide.type == SlideMediaType.VIDEO) {
                "⬇️ Slide #${pagerState.currentPage + 1} (Video)"
            } else {
                "⬇️ Slide #${pagerState.currentPage + 1} (Foto)"
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        onDownloadClick(currentSlide.url, currentLabel, currentFileName)
                    },
                shape = RoundedCornerShape(50),
                color = accentColor.copy(alpha = 0.14f)
            ) {
                Text(
                    text = currentLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
