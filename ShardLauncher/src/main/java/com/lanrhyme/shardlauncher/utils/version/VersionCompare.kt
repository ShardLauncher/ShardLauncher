/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.utils.version

import java.util.regex.Pattern

private val mSnapshotRegex = Pattern.compile("^\\d+[a-zA-Z]\\d+[a-zA-Z]$")

/**
 * Simple version comparison without external dependencies
 */
fun String.compareVersion(other: String): Int {
    val v1 = this.split(".", "-")
    val v2 = other.split(".", "-")
    val len = maxOf(v1.size, v2.size)
    for (i in 0 until len) {
        val s1 = v1.getOrNull(i) ?: "0"
        val s2 = v2.getOrNull(i) ?: "0"
        val n1 = s1.toIntOrNull()
        val n2 = s2.toIntOrNull()
        if (n1 != null && n2 != null) {
            if (n1 != n2) return n1.compareTo(n2)
        } else {
            val res = s1.compareTo(s2, ignoreCase = true)
            if (res != 0) return res
        }
    }
    return 0
}

fun String.isLowerOrEqualVer(releaseVer: String, snapshotVer: String): Boolean {
    return if (isSnapshotVer()) compareVersion(snapshotVer) <= 0
    else compareVersion(releaseVer) <= 0
}

fun String.isSnapshotVer(): Boolean = mSnapshotRegex.matcher(this).matches()
