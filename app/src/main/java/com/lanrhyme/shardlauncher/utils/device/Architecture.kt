/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.utils.device

object Architecture {
    const val ARCH_X86 = 0
    const val ARCH_ARM = 1
    const val ARCH_ARM64 = 2
    const val ARCH_X86_64 = 3

    val is64BitsDevice: Boolean
        get() = System.getProperty("os.arch")?.contains("64") == true

    fun archAsInt(arch: String?): Int {
        return when (arch?.lowercase()) {
            "x86", "i386", "i486", "i586", "i686" -> ARCH_X86
            "x86_64", "x86-64", "amd64" -> ARCH_X86_64
            "arm", "armeabi", "armeabi-v7a", "armv7", "armv7l" -> ARCH_ARM
            "arm64", "arm64-v8a", "armv8", "aarch64" -> ARCH_ARM64
            else -> -1
        }
    }
}
