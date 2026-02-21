package com.lanrhyme.shardlauncher.game.input

import android.view.KeyEvent
import com.lanrhyme.shardlauncher.bridge.SLBridge
import com.lanrhyme.shardlauncher.game.keycodes.LwjglGlfwKeycode

object LWJGLCharSender : CharacterSenderStrategy {
    override fun sendChar(char: Char) {
        SLBridge.sendInputData(SLBridge.EVENT_TYPE_CHAR, char.code, 0, 0, 0)
    }

    override fun sendKeyPress(keyCode: Int, keyChar: Char, mods: Int, isPressed: Boolean) {
        val lwjglKeyCode = EfficientAndroidLWJGLKeycode.getIndexByKey(keyCode)
            .takeIf { it >= 0 }?.let { EfficientAndroidLWJGLKeycode.getValueByIndex(it) }
            ?: LwjglGlfwKeycode.GLFW_KEY_UNKNOWN

        SLBridge.sendInputData(SLBridge.EVENT_TYPE_KEY, lwjglKeyCode.toInt(), keyChar.code, if (isPressed) 1 else 0, mods)
    }

    override fun sendOther(keyEvent: KeyEvent) {
        EfficientAndroidLWJGLKeycode.getIndexByKey(keyEvent.keyCode)
            .takeIf { it >= 0 }?.let { index ->
                val lwjglKeyCode = EfficientAndroidLWJGLKeycode.getValueByIndex(index)
                val keyChar = keyEvent.unicodeChar.toChar()
                val action = keyEvent.action

                if (action == KeyEvent.ACTION_DOWN) {
                    SLBridge.sendInputData(SLBridge.EVENT_TYPE_KEY, lwjglKeyCode.toInt(), keyChar.code, 1, 0)
                } else if (action == KeyEvent.ACTION_UP) {
                    SLBridge.sendInputData(SLBridge.EVENT_TYPE_KEY, lwjglKeyCode.toInt(), keyChar.code, 0, 0)
                }
            }
    }
}
