/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.addons.modloader

/**
 * Mod loader enumeration
 */
enum class ModLoader(val displayName: String) {
    FORGE("Forge"),
    FABRIC("Fabric"),
    QUILT("Quilt"),
    NEOFORGE("NeoForge"),
    OPTIFINE("OptiFine"),
    LITE_LOADER("LiteLoader"),
    CLEANROOM("Cleanroom"),
    UNKNOWN("Unknown");
    
    fun getLoaderEnvKey(): String {
        return when (this) {
            FORGE -> "INST_FORGE"
            FABRIC -> "INST_FABRIC"
            QUILT -> "INST_QUILT"
            NEOFORGE -> "INST_NEOFORGE"
            OPTIFINE -> "INST_OPTIFINE"
            LITE_LOADER -> "INST_LITELOADER"
            CLEANROOM -> "INST_CLEANROOM"
            UNKNOWN -> "INST_UNKNOWN"
        }
    }
}