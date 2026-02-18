package com.lanrhyme.shardlauncher.ui.launch

import android.app.Activity
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lanrhyme.shardlauncher.bridge.SLBridge
import com.lanrhyme.shardlauncher.game.input.AWTInputEvent
import com.lanrhyme.shardlauncher.game.launch.GameLaunchManager
import com.lanrhyme.shardlauncher.game.version.installed.VersionsManager
import com.lanrhyme.shardlauncher.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel : ViewModel() {
    private var launchJob: Job? = null
    var isGameRunning by mutableStateOf(false)
        private set

    fun launchGame(
        activity: Activity,
        versionName: String,
        onExit: () -> Unit
    ) {
        if (isGameRunning) return
        
        launchJob = viewModelScope.launch(Dispatchers.IO) {
            isGameRunning = true
            val version = VersionsManager.versions.find { it.getVersionName() == versionName }
            if (version == null) {
                Logger.lError("Version not found: $versionName")
                withContext(Dispatchers.Main) { onExit() }
                return@launch
            }

            try {
                val exitCode = GameLaunchManager.launchGame(
                    activity = activity,
                    version = version,
                    getWindowSize = {
                        IntSize(activity.window.decorView.width, activity.window.decorView.height)
                    },
                    onExit = { code, isSignal ->
                        Logger.lInfo("Game exited with code $code, signal: $isSignal")
                        viewModelScope.launch(Dispatchers.Main) {
                            isGameRunning = false
                            onExit()
                        }
                    }
                )
                Logger.lInfo("Launch process finished with code: $exitCode")
            } catch (e: Exception) {
                Logger.lError("Failed to launch game", e)
                viewModelScope.launch(Dispatchers.Main) {
                    isGameRunning = false
                    onExit()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        launchJob?.cancel()
    }
}

@Composable
fun GameScreen(
    versionName: String,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as Activity
    val viewModel: GameViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                SurfaceView(ctx).apply {
                    holder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            SLBridge.setupBridgeWindow(holder.surface)
                            viewModel.launchGame(activity, versionName, onExit)
                        }

                        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                        }

                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            SLBridge.releaseBridgeWindow()
                        }
                    })
                    
                    setOnTouchListener { v, event ->
                        val action = event.actionMasked
                        val x = event.x
                        val y = event.y

                        when (action) {
                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                                SLBridge.sendMousePos(x.toInt(), y.toInt())
                                SLBridge.sendMousePress(AWTInputEvent.BUTTON1_MASK, true)
                                v.performClick()
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                                SLBridge.sendMousePress(AWTInputEvent.BUTTON1_MASK, false)
                            }
                            MotionEvent.ACTION_MOVE -> {
                                SLBridge.sendMousePos(x.toInt(), y.toInt())
                            }
                        }
                        true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
