/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.components

enum class Components(val component: String, val displayName: String) {
    AUTH_LIBS("auth_libs", "Authentication Libraries"),
    CACIOCAVALLO("caciocavallo", "Caciocavallo (Java 8)"),
    CACIOCAVALLO17("caciocavallo17", "Caciocavallo (Java 17+)"),
    LWJGL3("lwjgl3", "LWJGL 3"),
    LAUNCHER("launcher", "Launcher Component Patches")
}
