package com.lanrhyme.shardlauncher.ui.components.dialog

import com.lanrhyme.shardlauncher.ui.components.layout.SliderLayoutCard
import com.lanrhyme.shardlauncher.ui.components.basic.SearchTextField
import com.lanrhyme.shardlauncher.ui.components.basic.TitleAndSummary
import com.lanrhyme.shardlauncher.ui.components.basic.ShardDropdownMenu
import com.lanrhyme.shardlauncher.ui.components.basic.glow
import com.lanrhyme.shardlauncher.ui.components.basic.selectableCard
import com.lanrhyme.shardlauncher.ui.components.layout.SwitchLayoutCard
import android.net.Uri
import android.content.Intent
import com.lanrhyme.shardlauncher.ui.components.basic.ShardDialog
import com.lanrhyme.shardlauncher.ui.components.business.MusicCard
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import coil.compose.AsyncImagePainter.State
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.lanrhyme.shardlauncher.data.SettingsRepository
import com.lanrhyme.shardlauncher.model.MusicItem
import com.lanrhyme.shardlauncher.ui.music.MusicPlayerViewModel
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 音乐播放器对话框组件
 * 支持播放、暂停、进度控制和播放模式切换
 *
 * @param onDismissRequest 关闭对话框时的回调
 * @param musicPlayerViewModel 音乐播放器 ViewModel
 */
@Composable
fun MusicPlayerDialog(onDismissRequest: () -> Unit, musicPlayerViewModel: MusicPlayerViewModel) {
    var selectedTab by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(MusicPlayerTab.MusicList) }

    ShardDialog(visible = true, onDismissRequest = onDismissRequest) {
        Column {
            Row(modifier = Modifier.weight(1f)) {
                NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    NavigationRailItem(
                            selected = selectedTab == MusicPlayerTab.MusicList,
                            onClick = { selectedTab = MusicPlayerTab.MusicList },
                            icon = {
                                Icon(Icons.Default.LibraryMusic, contentDescription = "音乐列表")
                            },
                            label = { Text("音乐列表") }
                    )
                    NavigationRailItem(
                            selected = selectedTab == MusicPlayerTab.Settings,
                            onClick = { selectedTab = MusicPlayerTab.Settings },
                            icon = {
                                Icon(Icons.Default.Settings, contentDescription = "设置")
                            },
                            label = { Text("设置") }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { onDismissRequest() }) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Box(modifier = Modifier.weight(3f).padding(16.dp)) {
                    AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = ""
                    ) {
                        when (it) {
                            MusicPlayerTab.MusicList ->
                                    MusicListPage(musicPlayerViewModel = musicPlayerViewModel)
                            MusicPlayerTab.Settings ->
                                    MusicPlayerSettingsPage(
                                            musicPlayerViewModel = musicPlayerViewModel
                                    )
                        }
                    }
                }
            }
            CurrentlyPlayingCard(musicPlayerViewModel = musicPlayerViewModel)
        }
    }
}

enum class MusicPlayerTab {
    MusicList,
    Settings
}

@Composable
fun MusicListPage(musicPlayerViewModel: MusicPlayerViewModel) {
    val musicList by musicPlayerViewModel.musicList.collectAsState()
    val mediaController by musicPlayerViewModel.mediaController.collectAsState()
    var currentMediaItem by remember { mutableStateOf(mediaController?.currentMediaItem) }

    DisposableEffect(mediaController) {
        val listener =
                object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        currentMediaItem = mediaItem
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            currentMediaItem = mediaController?.currentMediaItem
                        }
                    }
                }
        mediaController?.addListener(listener)

        onDispose { mediaController?.removeListener(listener) }
    }
    
    // Document Provider launcher for selecting audio files
    val context = LocalContext.current
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            musicPlayerViewModel.addMusicFile(it)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val searchQuery by musicPlayerViewModel.searchQuery.collectAsState()

        Row(
                modifier = Modifier.fillMaxWidth().height(36.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            var showPlayModeMenu by remember { mutableStateOf(false) }
            val repeatMode by musicPlayerViewModel.repeatMode.collectAsState()

            SearchTextField(
                    value = searchQuery,
                    onValueChange = { musicPlayerViewModel.searchMusic(it) },
                    hint = "搜索音乐",
                    modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { musicPlayerViewModel.loadMusicFiles() }) {
                Icon(Icons.Default.Refresh, contentDescription = "刷新")
            }
            Box {
                IconButton(onClick = { showPlayModeMenu = true }) {
                    Icon(
                            imageVector =
                                    when (repeatMode) {
                                        Player.REPEAT_MODE_OFF -> Icons.Default.Repeat
                                        Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                                        Player.REPEAT_MODE_ALL -> Icons.Default.Shuffle
                                        else -> Icons.Default.Repeat
                                    },
                            contentDescription = "播放模式"
                    )
                }
                ShardDropdownMenu(
                        expanded = showPlayModeMenu,
                        onDismissRequest = { showPlayModeMenu = false }
                ) {
                    DropdownMenuItem(
                            text = { Text("单曲循环") },
                            onClick = { // TODO:i18n
                                musicPlayerViewModel.setRepeatMode(Player.REPEAT_MODE_ONE)
                                showPlayModeMenu = false
                            }
                    )
                    DropdownMenuItem(
                            text = { Text("顺序播放") },
                            onClick = { // TODO:i18n
                                musicPlayerViewModel.setRepeatMode(Player.REPEAT_MODE_OFF)
                                showPlayModeMenu = false
                            }
                    )
                    DropdownMenuItem(
                            text = { Text("随机播放") },
                            onClick = { // TODO:i18n
                                musicPlayerViewModel.setRepeatMode(Player.REPEAT_MODE_ALL)
                                showPlayModeMenu = false
                            }
                    )
                }
            }
            IconButton(
                    onClick = {
                        audioPickerLauncher.launch(
                            arrayOf("audio/mpeg", "audio/wav", "audio/ogg", "audio/mp4", "audio/x-m4a")
                        )
                    }
            ) { Icon(Icons.Default.Add, contentDescription = "添加音乐") }
        }
        // 音乐列表
        LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(musicList) { item ->
                MusicCard(
                        item = item,
                        isSelected = item.mediaUri == currentMediaItem?.mediaId,
                        onCLick = {
                            val mediaItems =
                                    musicList.map { musicItem ->
                                        MediaItem.Builder()
                                                .setMediaId(musicItem.mediaUri)
                                                .setUri(Uri.parse(musicItem.mediaUri))
                                                .setMediaMetadata(
                                                        MediaMetadata.Builder()
                                                                .setTitle(musicItem.title)
                                                                .setArtist(musicItem.artist)
                                                                .setArtworkUri(
                                                                        Uri.parse(
                                                                                musicItem
                                                                                        .albumArtUri
                                                                        )
                                                                )
                                                                .build()
                                                )
                                                .build()
                                    }
                            val selectedIndex = musicList.indexOf(item)
                            if (selectedIndex != -1) {
                                mediaController?.setMediaItems(mediaItems, selectedIndex, 0)
                                mediaController?.prepare()
                                mediaController?.play()
                            }
                        },
                        onDelete = { musicPlayerViewModel.deleteMusicItem(item) }
                )
            }
        }
    }
}

@Composable
fun MusicPlayerSettingsPage(musicPlayerViewModel: MusicPlayerViewModel) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    var autoPlay by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(1f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(settingsRepository) {
        autoPlay = settingsRepository.getAutoPlayMusic()
        volume = settingsRepository.getMusicVolume()

        // 确保 MusicPlayerService 已启动，使其 ExoPlayer 实例存在
        try {
            val intent =
                    android.content.Intent(
                            context.applicationContext,
                            com.lanrhyme.shardlauncher.service.MusicPlayerService::class.java
                    )
            // 启动服务（对话在前景运行;startService 在这里没问题）
            context.applicationContext.startService(intent)
            android.util.Log.d(
                    "MusicPlayerDialog",
                    "Started MusicPlayerService to ensure player exists"
            )
        } catch (e: Exception) {
            android.util.Log.w(
                    "MusicPlayerDialog",
                    "Failed to start MusicPlayerService: ${e.message}"
            )
        }
    }

    LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            SwitchLayoutCard(
                    checked = autoPlay,
                    onCheckedChange = {
                        val newCheckedState = !autoPlay
                        autoPlay = newCheckedState
                        scope.launch { settingsRepository.setAutoPlayMusic(newCheckedState) }
                    },
                    title = "启动启动器时自动播放" // TODO:i18n
            )
        }

        item {
            SliderLayoutCard(
                    value = volume,
                    onValueChange = { newValue ->
                        volume = newValue
                        scope.launch { settingsRepository.setMusicVolume(newValue) }

                        // 发送给MusicPlayerService，以可靠地更新播放器音量
                        try {
                            val ctx = context.applicationContext
                            val intent =
                                    android.content.Intent(
                                                    ctx,
                                                    com.lanrhyme.shardlauncher.service
                                                                    .MusicPlayerService::class
                                                            .java
                                            )
                                            .apply {
                                                action =
                                                        com.lanrhyme.shardlauncher.service
                                                                .MusicPlayerService
                                                                .ACTION_SET_VOLUME
                                                putExtra(
                                                        com.lanrhyme.shardlauncher.service
                                                                .MusicPlayerService.EXTRA_VOLUME,
                                                        newValue
                                                )
                                            }
                            ctx.startService(intent)
                        } catch (e: Exception) {
                            android.util.Log.w(
                                    "MusicPlayerDialog",
                                    "Failed to send volume intent: ${e.message}"
                            )
                        }

                        // 另外，试着直接更新MediaController的底层播放器（如果有的话），作为备选方案
                        try {
                            val controller = musicPlayerViewModel.mediaController.value
                            if (controller != null) {
                                val getPlayerMethod = controller.javaClass.getMethod("getPlayer")
                                val playerObj = getPlayerMethod.invoke(controller)
                                playerObj?.let { pObj ->
                                    try {
                                        val setVol =
                                                pObj.javaClass.getMethod(
                                                        "setVolume",
                                                        java.lang.Float.TYPE
                                                )
                                        setVol.invoke(pObj, newValue)
                                        android.util.Log.d(
                                                "MusicPlayerDialog",
                                                "Set player volume via reflection: $newValue"
                                        )
                                    } catch (ignored: NoSuchMethodException) {
                                        try {
                                            val setVol =
                                                    pObj.javaClass.getMethod(
                                                            "setVolume",
                                                            java.lang.Float::class.java
                                                    )
                                            setVol.invoke(pObj, java.lang.Float.valueOf(newValue))
                                            android.util.Log.d(
                                                    "MusicPlayerDialog",
                                                    "Set player volume via reflection (boxed): $newValue"
                                            )
                                        } catch (ignored2: Exception) {
                                            // ignored
                                        }
                                    }
                                }
                            }
                        } catch (ignored: Exception) {
                            // ignore reflection failures
                        }
                    },
                    title = "音乐音量", // TODO:i18n
                    summary = "调整播放器音量", // TODO:i18n
                    valueRange = 0f..1f,
                    steps = 0,
                    enabled = true,
                    isGlowEffectEnabled = true
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentlyPlayingCard(musicPlayerViewModel: MusicPlayerViewModel) {
    val mediaController by musicPlayerViewModel.mediaController.collectAsState()
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentMediaItem by remember { mutableStateOf<MediaItem?>(null) }

    var isSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentPosition, duration) {
        if (!isSeeking && duration > 0) {
            sliderPosition = currentPosition.toFloat() / duration.toFloat()
        }
    }

    LaunchedEffect(mediaController) {
        val listener =
                object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        currentMediaItem = mediaItem
                        duration = mediaController?.duration?.takeIf { it > 0 } ?: 0L
                        currentPosition = 0L
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            duration = mediaController?.duration?.takeIf { it > 0 } ?: 0L
                            currentMediaItem = mediaController?.currentMediaItem
                            isPlaying = mediaController?.isPlaying == true
                        }
                    }
                }
        mediaController?.addListener(listener)

        duration = mediaController?.duration?.takeIf { it > 0 } ?: 0L
        currentMediaItem = mediaController?.currentMediaItem
        isPlaying = mediaController?.isPlaying == true

        while (true) {
            if (isPlaying) {
                currentPosition = mediaController?.currentPosition ?: 0L
            }
            delay(1000) // 每秒更新一次时间
        }
    }

    Card(modifier = Modifier.fillMaxWidth().height(80.dp), shape = MaterialTheme.shapes.large) {
        Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SubcomposeAsyncImage(
                    model = currentMediaItem?.mediaMetadata?.artworkUri,
                    contentDescription = "专辑封面",
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
            ) {
                when (painter.state) {
                    is State.Success -> {
                        SubcomposeAsyncImageContent(modifier = Modifier.fillMaxSize())
                    }
                    else -> { // 错误，加载中，空时使用默认封面
                        Box(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .background(
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                    Icons.Default.LibraryMusic,
                                    contentDescription = "专辑封面占位符",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 歌曲信息与进度条
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    currentMediaItem?.mediaMetadata?.title?.toString() ?: "暂无歌曲",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    Slider(
                            value = sliderPosition,
                            onValueChange = {
                                isSeeking = true
                                sliderPosition = it
                            },
                            onValueChangeFinished = {
                                mediaController?.seekTo((sliderPosition * duration).toLong())
                                isSeeking = false
                            },
                            modifier = Modifier.weight(1f),
                            enabled = mediaController != null && duration > 0,
                            colors =
                                    SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor =
                                                    MaterialTheme.colorScheme.surfaceVariant
                                    ),
                            interactionSource = interactionSource,
                            thumb = {
                                val thumbWidth = 8.dp
                                val thumbHeight = 25.dp
                                SliderDefaults.Thumb(
                                        interactionSource = interactionSource,
                                        modifier =
                                                Modifier.glow(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        cornerRadius = thumbHeight / 2,
                                                        blurRadius = 12.dp,
                                                        enabled = isPressed
                                                ),
                                        thumbSize = DpSize(width = thumbWidth, height = thumbHeight)
                                )
                            }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.width(100.dp)) {
                        if (duration > 0) {
                            val positionText =
                                    if (isSeeking) (sliderPosition * duration).toLong()
                                    else currentPosition
                            Text(
                                text =
                                    "${formatMillis(positionText)} / ${formatMillis(duration)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                        onClick = { mediaController?.seekToPreviousMediaItem() },
                        enabled = mediaController?.hasPreviousMediaItem() == true
                ) { Icon(Icons.Default.SkipPrevious, contentDescription = "Previous") }
                IconButton(
                        onClick = {
                            if (isPlaying) {
                                mediaController?.pause()
                            } else {
                                mediaController?.play()
                            }
                        },
                        enabled = mediaController != null
                ) {
                    Icon(
                            imageVector =
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }
                IconButton(
                        onClick = { mediaController?.seekToNextMediaItem() },
                        enabled = mediaController?.hasNextMediaItem() == true
                ) { Icon(Icons.Default.SkipNext, contentDescription = "Next") }
            }
        }
    }
}

private fun formatMillis(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

