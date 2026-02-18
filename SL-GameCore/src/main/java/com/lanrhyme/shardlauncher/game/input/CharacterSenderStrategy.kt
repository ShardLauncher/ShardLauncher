package com.lanrhyme.shardlauncher.game.input

import android.view.KeyEvent

/**
 * 字符发送策略，用于支持不同的游戏引擎（如 AWT 或 LWJGL）
 */
interface CharacterSenderStrategy {
    fun sendChar(char: Char)
    fun sendKeyPress(keyCode: Int, keyChar: Char, mods: Int, isPressed: Boolean)
    fun sendEnter() = sendChar('\n')
    fun sendBackspace() = sendKeyPress(KeyEvent.KEYCODE_DEL, '\b', 0, true).also { sendKeyPress(KeyEvent.KEYCODE_DEL, '\b', 0, false) }
    fun sendTab() = sendChar('\t')
    fun sendUp() = sendKeyPress(KeyEvent.KEYCODE_DPAD_UP, '\u0000', 0, true).also { sendKeyPress(KeyEvent.KEYCODE_DPAD_UP, '\u0000', 0, false) }
    fun sendDown() = sendKeyPress(KeyEvent.KEYCODE_DPAD_DOWN, '\u0000', 0, true).also { sendKeyPress(KeyEvent.KEYCODE_DPAD_DOWN, '\u0000', 0, false) }
    fun sendLeft() = sendKeyPress(KeyEvent.KEYCODE_DPAD_LEFT, '\u0000', 0, true).also { sendKeyPress(KeyEvent.KEYCODE_DPAD_LEFT, '\u0000', 0, false) }
    fun sendRight() = sendKeyPress(KeyEvent.KEYCODE_DPAD_RIGHT, '\u0000', 0, true).also { sendKeyPress(KeyEvent.KEYCODE_DPAD_RIGHT, '\u0000', 0, false) }
    fun sendOther(keyEvent: KeyEvent)
}
