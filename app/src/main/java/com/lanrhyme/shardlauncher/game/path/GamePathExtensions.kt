/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.game.path

import com.lanrhyme.shardlauncher.path.PathManager
import java.io.File

/**
 * Get the game home directory (user's .minecraft equivalent)
 */
fun getGameHome(): String {
    return File(PathManager.DIR_GAME, ".minecraft").absolutePath
}
