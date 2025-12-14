package com.lanrhyme.shardlauncher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay

@Composable
fun CustomDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tween = tween<Float>(durationMillis = 300)

    var showDialog by remember { mutableStateOf(visible) }
    LaunchedEffect(visible) {
        if (visible) {
            showDialog = true
        } else {
            delay(300)
            showDialog = false
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            val view = LocalView.current
            val darkTheme = isSystemInDarkTheme()
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.parent as? DialogWindowProvider)?.window
                    if (window != null) {
                        WindowCompat.setDecorFitsSystemWindows(window, false)
                        val insetsController = WindowCompat.getInsetsController(window, view)
                        insetsController.hide(WindowInsetsCompat.Type.systemBars())
                        insetsController.systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        window.statusBarColor = Color.Transparent.toArgb()
                        window.navigationBarColor = Color.Transparent.toArgb()
                        insetsController.isAppearanceLightStatusBars = !darkTheme
                    }
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween),
                exit = fadeOut(tween)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onDismissRequest
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val bgAlpha by animateFloatAsState(
                        targetValue = if (visible) 0.4f else 0f,
                        animationSpec = tween,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = bgAlpha }
                            .background(Color.Black)
                    )

                    val progress by animateFloatAsState(
                        targetValue = if (visible) 1f else 0f,
                        animationSpec = tween,
                    )
                    Surface(
                        modifier = modifier
                            .graphicsLayer {
                                alpha = progress
                                translationY = (1f - progress) * 80.dp.toPx()
                            }
                            .widthIn(max = 600.dp)
                            .heightIn(max = 380.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {}
                            ),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.background,
                        tonalElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) { content() }
                    }
                }
            }
        }
    }

}