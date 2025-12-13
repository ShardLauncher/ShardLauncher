package com.lanrhyme.shardlauncher.game.path

import android.content.Context
import java.io.File

object PathManager {
    lateinit var DIR_FILES_PRIVATE: File
    lateinit var DIR_FILES_EXTERNAL: File
    lateinit var DIR_CACHE: File
    
    lateinit var DIR_GAME: File
    lateinit var DIR_ACCOUNT_SKIN: File
    lateinit var DIR_LAUNCHER_LOGS: File

    fun refreshPaths(context: Context) {
        DIR_FILES_PRIVATE = context.filesDir
        DIR_FILES_EXTERNAL = context.getExternalFilesDir(null) ?: context.filesDir
        DIR_CACHE = context.cacheDir

        DIR_GAME = File(DIR_FILES_PRIVATE, "games")
        DIR_ACCOUNT_SKIN = File(DIR_GAME, "account_skins")
        DIR_LAUNCHER_LOGS = File(DIR_FILES_EXTERNAL, "logs")

        createDirs()
    }

    private fun createDirs() {
        DIR_GAME.mkdirs()
        DIR_ACCOUNT_SKIN.mkdirs()
        DIR_LAUNCHER_LOGS.mkdirs()
    }
}
