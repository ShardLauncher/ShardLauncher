package com.lanrhyme.shardlauncher.path

import android.content.Context
import java.io.File

class PathManager {
    companion object {
        lateinit var DIR_FILES_PRIVATE: File
        lateinit var DIR_FILES_EXTERNAL: File
        lateinit var DIR_CACHE: File
        lateinit var DIR_NATIVE_LIB: String

        lateinit var DIR_GAME: File
        lateinit var DIR_DATA_BASES: File
        lateinit var DIR_ACCOUNT_SKIN: File
        lateinit var DIR_MULTIRT: File
        lateinit var DIR_JNA: File
        lateinit var DIR_COMPONENTS: File
        lateinit var DIR_MOUSE_POINTER: File
        lateinit var DIR_CACHE_GAME_DOWNLOADER: File
        lateinit var DIR_CACHE_MODPACK_DOWNLOADER: File
        lateinit var DIR_CACHE_MOD_UPDATER: File
        lateinit var DIR_CACHE_APP_ICON: File
        lateinit var DIR_LAUNCHER_LOGS: File
        lateinit var DIR_IMAGE_CACHE: File
        lateinit var DIR_CONTROL_LAYOUTS: File
        var DIR_RUNTIME_MOD: File? = null
        lateinit var DIR_NATIVE_LOGS: File

        lateinit var FILE_CRASH_REPORT: File
        lateinit var FILE_SETTINGS: File
        lateinit var FILE_MINECRAFT_VERSIONS: File

        fun refreshPaths(context: Context) {
            DIR_FILES_PRIVATE = context.filesDir
            DIR_FILES_EXTERNAL = context.getExternalFilesDir(null)!!
            DIR_CACHE = context.cacheDir
            DIR_NATIVE_LIB = context.applicationInfo.nativeLibraryDir

            DIR_DATA_BASES = File(DIR_FILES_PRIVATE.parentFile, "databases")
            DIR_GAME = File(DIR_FILES_PRIVATE, "games")
            DIR_ACCOUNT_SKIN = File(DIR_GAME, "account_skins")
            DIR_MULTIRT = File(DIR_GAME, "runtimes")
            DIR_JNA = File(DIR_GAME, "jna_dir")
            DIR_COMPONENTS = File(DIR_FILES_PRIVATE, "components")
            DIR_MOUSE_POINTER = File(DIR_FILES_PRIVATE, "mouse_pointer")
            DIR_CACHE_GAME_DOWNLOADER = File(DIR_CACHE, "temp_game")
            DIR_CACHE_MODPACK_DOWNLOADER = File(DIR_CACHE, "temp_modpack")
            DIR_CACHE_MOD_UPDATER = File(DIR_CACHE, "temp_mod_updater")
            DIR_CACHE_APP_ICON = File(DIR_CACHE, "app_icons")
            DIR_LAUNCHER_LOGS = File(DIR_FILES_EXTERNAL, "logs")
            DIR_NATIVE_LOGS = File(DIR_FILES_EXTERNAL, "native_logs")
            DIR_IMAGE_CACHE = File(DIR_CACHE, "images")
            DIR_CONTROL_LAYOUTS = File(DIR_FILES_EXTERNAL, "control_layouts")

            FILE_CRASH_REPORT = File(DIR_LAUNCHER_LOGS, "launcher_crash.log")
            FILE_SETTINGS = File(DIR_FILES_PRIVATE, "settings.json")
            FILE_MINECRAFT_VERSIONS = File(DIR_GAME, "minecraft_versions.json")

            createDirs()
        }

        private fun createDirs() {
            DIR_RUNTIME_MOD?.mkdirs()
            DIR_GAME.mkdirs()
            DIR_ACCOUNT_SKIN.mkdirs()
            DIR_MULTIRT.mkdirs()
            DIR_JNA.mkdirs()
            DIR_COMPONENTS.mkdirs()
            DIR_MOUSE_POINTER.mkdirs()
            DIR_CACHE_GAME_DOWNLOADER.mkdirs()
            DIR_CACHE_MODPACK_DOWNLOADER.mkdirs()
            DIR_CACHE_MOD_UPDATER.mkdirs()
            DIR_CACHE_APP_ICON.mkdirs()
            DIR_LAUNCHER_LOGS.mkdirs()
            DIR_NATIVE_LOGS.mkdirs()
            DIR_IMAGE_CACHE.mkdirs()
            DIR_CONTROL_LAYOUTS.mkdirs()
        }
    }
}