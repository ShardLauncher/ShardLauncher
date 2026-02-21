/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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

package com.lanrhyme.shardlauncher.utils.string

/**
 * 比较版本号
 */
fun String.compareVersions(other: String): Int {
    val thisParts = this.split(".")
    val otherParts = other.split(".")
    val maxLength = maxOf(thisParts.size, otherParts.size)
    
    for (i in 0 until maxLength) {
        val thisPart = thisParts.getOrNull(i)?.toIntOrNull() ?: 0
        val otherPart = otherParts.getOrNull(i)?.toIntOrNull() ?: 0
        
        when {
            thisPart < otherPart -> return -1
            thisPart > otherPart -> return 1
        }
    }
    return 0
}

/**
 * 从字符串中提取指定前缀后到指定字符之间的内容
 */
fun String.extractUntilCharacter(prefix: String, endChar: Char): String? {
    val startIndex = this.indexOf(prefix)
    if (startIndex == -1) return null
    
    val contentStart = startIndex + prefix.length
    val endIndex = this.indexOf(endChar, contentStart)
    if (endIndex == -1) return null
    
    return this.substring(contentStart, endIndex)
}
/**
 * 检查字符串是否不为空且不为空白
 */
fun String?.isNotEmptyOrBlank(): Boolean {
    return !this.isNullOrBlank()
}

/**
 * 检查字符串是否为空或空白
 */
fun String?.isEmptyOrBlank(): Boolean {
    return this.isNullOrBlank()
}

/**
 * 获取异常的消息或toString
 */
fun Throwable.getMessageOrToString(): String {
    return this.message ?: this.toString()
}

/**
 * 将字符串转换为Unicode转义
 */
fun String.toUnicodeEscaped(): String {
    return this.map { char ->
        if (char.code > 127) {
            "\\u${char.code.toString(16).padStart(4, '0')}"
        } else {
            char.toString()
        }
    }.joinToString("")
}