package com.lanrhyme.shardlauncher.utils.string

import org.apache.maven.artifact.versioning.ComparableVersion

fun compareChar(s1: String, s2: String): Int {
    val regex = "\\d+".toRegex()

    val parts1 = regex.findAll(s1).map { it.value }.toList()
    val parts2 = regex.findAll(s2).map { it.value }.toList()

    val minSize = minOf(parts1.size, parts2.size)

    for (i in 0 until minSize) {
        val num1 = parts1[i].toIntOrNull()
        val num2 = parts2[i].toIntOrNull()

        if (num1 != null && num2 != null) {
            if (num1 != num2) return num1.compareTo(num2)
        } else {
            val strCompare = parts1[i].compareTo(parts2[i], ignoreCase = true)
            if (strCompare != 0) return strCompare
        }
    }

    return s1.compareTo(s2, ignoreCase = true)
}

/**
 * 与另一个版本比较
 */
fun String.compareVersion(otherVer: String): Int {
    return ComparableVersion(this).compareTo(ComparableVersion(otherVer))
}

/**
 * 是否等于另一个版本（版本语义一致即可）
 */
fun String.isVersionEqualTo(otherVer: String): Boolean {
    return ComparableVersion(this) == ComparableVersion(otherVer)
}

/**
 * 是否大于等于另一个版本
 */
fun String.isBiggerOrEqualTo(otherVer: String): Boolean {
    return ComparableVersion(this) >= ComparableVersion(otherVer)
}

/**
 * 是否大于另一个版本
 */
fun String.isBiggerTo(otherVer: String): Boolean {
    return ComparableVersion(this) > ComparableVersion(otherVer)
}

/**
 * 是否小于等于另一个版本
 */
fun String.isLowerOrEqualTo(otherVer: String): Boolean {
    return ComparableVersion(this) <= ComparableVersion(otherVer)
}

/**
 * 是否小于另一个版本
 */
fun String.isLowerTo(otherVer: String): Boolean {
    return ComparableVersion(this) < ComparableVersion(otherVer)
}

/**
 * 是否等于另一个版本
 */
fun String.isEqualTo(otherVer: String): Boolean {
    return ComparableVersion(this) == ComparableVersion(otherVer)
}

/**
 * 是否在某个版本区间之内（闭区间）
 */
fun String.isBetween(min: String, max: String): Boolean {
    val ver = ComparableVersion(this)
    return ver >= ComparableVersion(min) && ver <= ComparableVersion(max)
}

/**
 * 是否是早于某个主版本的大版本变更（比如用于判定是否是 1.13 以前）
 */
fun String.isBeforeMajorVersion(major: Int): Boolean {
    val majorVersion = this.split('.').firstOrNull()?.toIntOrNull() ?: return false
    return majorVersion < major
}