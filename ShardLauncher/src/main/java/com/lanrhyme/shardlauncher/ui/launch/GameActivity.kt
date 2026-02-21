/*
 * Shard Launcher
 * Copyright (C) 2025 LanRhyme
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.lanrhyme.shardlauncher.ui.launch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lanrhyme.shardlauncher.ui.theme.ShardLauncherTheme
import com.lanrhyme.shardlauncher.utils.Logger
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.bridge.ZLNativeInvoker
import com.movtery.zalithlauncher.game.input.AWTInputEvent
import com.movtery.zalithlauncher.game.version.installed.Version
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.lwjgl.glfw.CallbackBridge

private const val INTENT_VERSION = "INTENT_VERSION"

class GameActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    private var mTextureView: TextureView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val version: Version? = intent.getParcelableExtra(INTENT_VERSION)
        if (version == null) {
            Logger.lError("No version specified for GameActivity")
            finish()
            return
        }

        window?.apply {
            setBackgroundDrawable(Color.BLACK.toDrawable())
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            @Suppress("DEPRECATION")
            decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }

        ZLBridge.dlopen("libpojavexec.so")
        
        ZLNativeInvoker.init(this)

        setContent {
            ShardLauncherTheme {
                GameScreenContent(
                    version = version,
                    viewModel = gameViewModel,
                    getWindowSize = getWindowSize,
                    onExit = { exitCode, isSignal ->
                        if (exitCode != 0) {
                            Toast.makeText(
                                this@GameActivity,
                                "Game exited with code $exitCode" + if (isSignal) " (signal)" else "",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        finish()
                    }
                )
            }
        }
    }

    private val getWindowSize = {
        val displayMetrics = resources.displayMetrics
        IntSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    override fun onResume() {
        super.onResume()
        refreshWindowSize()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val isPressed = event.action == KeyEvent.ACTION_DOWN

        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (isPressed) {
                gameViewModel.exitGame()
                return true
            }
        }

        return super.dispatchKeyEvent(event)
    }

    private fun refreshWindowSize() {
        val displayMetrics = resources.displayMetrics
        
        CallbackBridge.physicalWidth = displayMetrics.widthPixels
        CallbackBridge.physicalHeight = displayMetrics.heightPixels
        CallbackBridge.windowWidth = displayMetrics.widthPixels
        CallbackBridge.windowHeight = displayMetrics.heightPixels
        
        mTextureView?.surfaceTexture?.apply {
            setDefaultBufferSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
    }

    @Composable
    private fun GameScreenContent(
        version: Version,
        viewModel: GameViewModel,
        getWindowSize: () -> IntSize,
        onExit: (Int, Boolean) -> Unit
    ) {
        val isGameRunning by viewModel.isGameRunning.collectAsStateWithLifecycle()

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    TextureView(ctx).apply {
                        isOpaque = true
                        alpha = 1.0f

                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                                Logger.lInfo("Surface available: ${width}x${height}")

                                refreshWindowSize()

                                Logger.lInfo("GameActivity: About to call setupBridgeWindow()")
                                ZLBridge.setupBridgeWindow(Surface(surface))
                                Logger.lInfo("GameActivity: setupBridgeWindow() returned successfully")

                                if (!isGameRunning) {
                                    viewModel.launchGame(
                                        activity = this@GameActivity,
                                        version = version,
                                        getWindowSize = getWindowSize,
                                        onExit = onExit
                                    )
                                }
                            }

                            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                                Logger.lInfo("Surface size changed: ${width}x${height}")
                                refreshWindowSize()
                            }

                            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                                Logger.lInfo("Surface destroyed")
                                ZLBridge.releaseBridgeWindow()
                                return true
                            }

                            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                            }
                        }

                        setOnTouchListener { v, event ->
                            handleTouchEvent(event)
                            true
                        }
                    }.also { view ->
                        mTextureView = view
                    }
                }
            )
        }
    }

    private fun handleTouchEvent(event: MotionEvent) {
        val action = event.actionMasked
        val x = event.x.toInt()
        val y = event.y.toInt()

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                ZLBridge.sendMousePos(x, y)
                ZLBridge.sendMousePress(AWTInputEvent.BUTTON1_MASK, true)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                ZLBridge.sendMousePress(AWTInputEvent.BUTTON1_MASK, false)
            }
            MotionEvent.ACTION_MOVE -> {
                ZLBridge.sendMousePos(x, y)
            }
        }
    }

    companion object {
        fun startGame(context: Context, version: Version) {
            val intent = Intent(context, GameActivity::class.java).apply {
                putExtra(INTENT_VERSION, version)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}

class GameViewModel : ViewModel() {
    private var launchJob: Job? = null

    private val _isGameRunning = MutableStateFlow(false)
    val isGameRunning = _isGameRunning.asStateFlow()

    fun launchGame(
        activity: Activity,
        version: Version,
        getWindowSize: () -> IntSize,
        onExit: (Int, Boolean) -> Unit
    ) {
        if (_isGameRunning.value) {
            Logger.lWarning("Game is already running")
            return
        }

        launchJob = viewModelScope.launch(Dispatchers.IO) {
            _isGameRunning.value = true

            try {
                val exitCode = GameLaunchManager.launchGame(
                    activity = activity,
                    version = version,
                    getWindowSize = getWindowSize,
                    onExit = { code, isSignal ->
                        Logger.lInfo("Game exited with code $code, signal: $isSignal")
                        viewModelScope.launch(Dispatchers.Main) {
                            _isGameRunning.value = false
                            onExit(code, isSignal)
                        }
                    }
                )
                Logger.lInfo("Launch process finished with code: $exitCode")
            } catch (e: Exception) {
                Logger.lError("Failed to launch game", e)
                viewModelScope.launch(Dispatchers.Main) {
                    _isGameRunning.value = false
                    onExit(-1, false)
                }
            }
        }
    }

    fun exitGame() {
        launchJob?.cancel()
        _isGameRunning.value = false
    }

    override fun onCleared() {
        super.onCleared()
        launchJob?.cancel()
    }
}
