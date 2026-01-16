package com.lanrhyme.shardlauncher.data

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.lanrhyme.shardlauncher.common.SidebarPosition
import com.lanrhyme.shardlauncher.ui.settings.BackgroundItem
import com.lanrhyme.shardlauncher.ui.theme.ThemeColor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

class ColorTypeAdapter : TypeAdapter<Color>() {
    override fun write(out: JsonWriter, value: Color?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.toArgb())
        }
    }

    override fun read(input: JsonReader): Color? {
        if (input.peek() == com.google.gson.stream.JsonToken.NULL) {
            input.nextNull()
            return null
        }
        return Color(input.nextLong().toInt())
    }
}

class SettingsRepository(context: Context) {

    private val properties = Properties()
    private val settingsFile: File
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Color::class.java, ColorTypeAdapter())
        .create()

    init {
        val dataDir = File(context.getExternalFilesDir(null), ".shardlauncher")
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
        settingsFile = File(dataDir, PREFS_NAME)
        if (settingsFile.exists()) {
            FileInputStream(settingsFile).use { properties.load(it) }
        }
    }

    private fun saveProperties() {
        FileOutputStream(settingsFile).use { properties.store(it, null) }
    }

    fun getBackgroundItems(): List<BackgroundItem> {
        val json = properties.getProperty(KEY_BACKGROUND_ITEMS)
        return if (json != null) {
            val type = object : TypeToken<List<BackgroundItem>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun setBackgroundItems(items: List<BackgroundItem>) {
        val json = gson.toJson(items)
        properties.setProperty(KEY_BACKGROUND_ITEMS, json)
        saveProperties()
    }

    fun getRandomBackground(): Boolean {
        return properties.getProperty(KEY_RANDOM_BACKGROUND, "false").toBoolean()
    }

    fun setRandomBackground(enabled: Boolean) {
        properties.setProperty(KEY_RANDOM_BACKGROUND, enabled.toString())
        saveProperties()
    }

    fun getUiScale(): Float {
        return properties.getProperty(KEY_UI_SCALE, "1.0").toFloat()
    }

    fun setUiScale(scale: Float) {
        properties.setProperty(KEY_UI_SCALE, scale.toString())
        saveProperties()
    }

    fun getEnableVersionCheck(): Boolean {
        return properties.getProperty(KEY_ENABLE_VERSION_CHECK, "true").toBoolean()
    }

    fun setEnableVersionCheck(enabled: Boolean) {
        properties.setProperty(KEY_ENABLE_VERSION_CHECK, enabled.toString())
        saveProperties()
    }

    fun getLauncherBackgroundUri(): String? {
        return properties.getProperty(KEY_LAUNCHER_BACKGROUND_URI)
    }

    fun setLauncherBackgroundUri(uri: String?) {
        if (uri != null) {
            properties.setProperty(KEY_LAUNCHER_BACKGROUND_URI, uri)
        } else {
            properties.remove(KEY_LAUNCHER_BACKGROUND_URI)
        }
        saveProperties()
    }

    fun getLauncherBackgroundBlur(): Float {
        return properties.getProperty(KEY_LAUNCHER_BACKGROUND_BLUR, "0.0").toFloat()
    }

    fun setLauncherBackgroundBlur(blur: Float) {
        properties.setProperty(KEY_LAUNCHER_BACKGROUND_BLUR, blur.toString())
        saveProperties()
    }

    fun getLauncherBackgroundBrightness(): Float {
        return properties.getProperty(KEY_LAUNCHER_BACKGROUND_BRIGHTNESS, "0.0").toFloat()
    }

    fun setLauncherBackgroundBrightness(brightness: Float) {
        properties.setProperty(KEY_LAUNCHER_BACKGROUND_BRIGHTNESS, brightness.toString())
        saveProperties()
    }

    fun getEnableParallax(): Boolean {
        return properties.getProperty(KEY_ENABLE_PARALLAX, "false").toBoolean()
    }

    fun setEnableParallax(enabled: Boolean) {
        properties.setProperty(KEY_ENABLE_PARALLAX, enabled.toString())
        saveProperties()
    }

    fun getParallaxMagnitude(): Float {
        return properties.getProperty(KEY_PARALLAX_MAGNITUDE, "1.0").toFloat()
    }

    fun setParallaxMagnitude(magnitude: Float) {
        properties.setProperty(KEY_PARALLAX_MAGNITUDE, magnitude.toString())
        saveProperties()
    }

    fun getEnableBackgroundLightEffect(): Boolean {
        return properties.getProperty(KEY_ENABLE_BACKGROUND_LIGHT_EFFECT, "true").toBoolean()
    }

    fun setEnableBackgroundLightEffect(enabled: Boolean) {
        properties.setProperty(KEY_ENABLE_BACKGROUND_LIGHT_EFFECT, enabled.toString())
        saveProperties()
    }

    fun getAnimationSpeed(): Float {
        return properties.getProperty(KEY_ANIMATION_SPEED, "1.0f").toFloat()
    }

    fun setAnimationSpeed(speed: Float) {
        properties.setProperty(KEY_ANIMATION_SPEED, speed.toString())
        saveProperties()
    }

    fun getLightEffectAnimationSpeed(): Float {
        return properties.getProperty(KEY_LIGHT_EFFECT_ANIMATION_SPEED, "1.0f").toFloat()
    }

    fun setLightEffectAnimationSpeed(speed: Float) {
        properties.setProperty(KEY_LIGHT_EFFECT_ANIMATION_SPEED, speed.toString())
        saveProperties()
    }

    fun getEnableBackgroundLightEffectCustomColor(): Boolean {
        return properties.getProperty(KEY_ENABLE_BACKGROUND_LIGHT_EFFECT_CUSTOM_COLOR, "false").toBoolean()
    }

    fun setEnableBackgroundLightEffectCustomColor(enabled: Boolean) {
        properties.setProperty(KEY_ENABLE_BACKGROUND_LIGHT_EFFECT_CUSTOM_COLOR, enabled.toString())
        saveProperties()
    }

    fun getBackgroundLightEffectCustomColor(): Int {
        return properties.getProperty(KEY_BACKGROUND_LIGHT_EFFECT_CUSTOM_COLOR, DEFAULT_CUSTOM_PRIMARY_COLOR.toString()).toIntOrNull() ?: DEFAULT_CUSTOM_PRIMARY_COLOR
    }

    fun setBackgroundLightEffectCustomColor(color: Int) {
        properties.setProperty(KEY_BACKGROUND_LIGHT_EFFECT_CUSTOM_COLOR, color.toString())
        saveProperties()
    }

    fun getIsDarkTheme(systemIsDark: Boolean): Boolean {
        return properties.getProperty(KEY_IS_DARK_THEME, systemIsDark.toString()).toBoolean()
    }

    fun setIsDarkTheme(isDark: Boolean) {
        properties.setProperty(KEY_IS_DARK_THEME, isDark.toString())
        saveProperties()
    }

    fun getSidebarPosition(): SidebarPosition {
        val positionName = properties.getProperty(KEY_SIDEBAR_POSITION, SidebarPosition.Left.name)
        return SidebarPosition.valueOf(positionName ?: SidebarPosition.Left.name)
    }

    fun setSidebarPosition(position: SidebarPosition) {
        properties.setProperty(KEY_SIDEBAR_POSITION, position.name)
        saveProperties()
    }

    fun getThemeColor(): ThemeColor {
        val colorName = properties.getProperty(KEY_THEME_COLOR, ThemeColor.Green.name)
        return ThemeColor.valueOf(colorName ?: ThemeColor.Green.name)
    }

    fun setThemeColor(color: ThemeColor) {
        properties.setProperty(KEY_THEME_COLOR, color.name)
        saveProperties()
    }

    fun getCustomPrimaryColor(): Int {
        return properties.getProperty(KEY_CUSTOM_PRIMARY_COLOR, DEFAULT_CUSTOM_PRIMARY_COLOR.toString()).toIntOrNull() ?: DEFAULT_CUSTOM_PRIMARY_COLOR
    }

    fun setCustomPrimaryColor(color: Int) {
        properties.setProperty(KEY_CUSTOM_PRIMARY_COLOR, color.toString())
        saveProperties()
    }

    fun getLightColorScheme(): ColorScheme? {
        val json = properties.getProperty(KEY_LIGHT_COLOR_SCHEME)
        return if (json != null) {
            gson.fromJson(json, ColorScheme::class.java)
        } else {
            null
        }
    }

    fun setLightColorScheme(scheme: ColorScheme) {
        val json = gson.toJson(scheme)
        properties.setProperty(KEY_LIGHT_COLOR_SCHEME, json)
        saveProperties()
    }

    fun getDarkColorScheme(): ColorScheme? {
        val json = properties.getProperty(KEY_DARK_COLOR_SCHEME)
        return if (json != null) {
            gson.fromJson(json, ColorScheme::class.java)
        } else {
            null
        }
    }

    fun setDarkColorScheme(scheme: ColorScheme) {
        val json = gson.toJson(scheme)
        properties.setProperty(KEY_DARK_COLOR_SCHEME, json)
        saveProperties()
    }

    fun getIsGlowEffectEnabled(): Boolean {
        return properties.getProperty(KEY_IS_GLOW_EFFECT_ENABLED, "true").toBoolean()
    }

    fun setIsGlowEffectEnabled(enabled: Boolean) {
        properties.setProperty(KEY_IS_GLOW_EFFECT_ENABLED, enabled.toString())
        saveProperties()
    }

    fun getIsCardBlurEnabled(): Boolean {
        return properties.getProperty(KEY_IS_CARD_BLUR_ENABLED, "false").toBoolean()
    }

    fun setIsCardBlurEnabled(enabled: Boolean) {
        properties.setProperty(KEY_IS_CARD_BLUR_ENABLED, enabled.toString())
        saveProperties()
    }

    fun getCardAlpha(): Float {
        return properties.getProperty(KEY_CARD_ALPHA, "0.6").toFloat()
    }

    fun setCardAlpha(alpha: Float) {
        properties.setProperty(KEY_CARD_ALPHA, alpha.toString())
        saveProperties()
    }

    fun getCurrentGamePathId(): String {
        return properties.getProperty(KEY_CURRENT_GAME_PATH_ID, "default")
    }

    fun setCurrentGamePathId(id: String) {
        properties.setProperty(KEY_CURRENT_GAME_PATH_ID, id)
        saveProperties()
    }

        fun getIsMusicPlayerEnabled(): Boolean {

            return properties.getProperty(KEY_IS_MUSIC_PLAYER_ENABLED, "false").toBoolean()

        }

    

        fun setIsMusicPlayerEnabled(enabled: Boolean) {

            properties.setProperty(KEY_IS_MUSIC_PLAYER_ENABLED, enabled.toString())

            saveProperties()

        }

    

        fun getAutoPlayMusic(): Boolean {

            return properties.getProperty(KEY_AUTO_PLAY_MUSIC, "true").toBoolean()

        }

    

        fun setAutoPlayMusic(enabled: Boolean) {

            properties.setProperty(KEY_AUTO_PLAY_MUSIC, enabled.toString())

            saveProperties()

        }

    

        fun getMusicDirectories(): List<String> {

            val json = properties.getProperty(KEY_MUSIC_DIRECTORIES)

            return if (json != null) {

                val type = object : TypeToken<List<String>>() {}.type

                gson.fromJson(json, type)

            } else {

                emptyList()

            }

        }

    

        fun setMusicDirectories(directories: List<String>) {

            val json = gson.toJson(directories)

            properties.setProperty(KEY_MUSIC_DIRECTORIES, json)

            saveProperties()

        }

    

        fun getLastSelectedMusicDirectory(): String? {

            return properties.getProperty(KEY_LAST_SELECTED_MUSIC_DIRECTORY)

        }

    

        fun setLastSelectedMusicDirectory(directory: String?) {

            if (directory != null) {

                properties.setProperty(KEY_LAST_SELECTED_MUSIC_DIRECTORY, directory)

            } else {

                properties.remove(KEY_LAST_SELECTED_MUSIC_DIRECTORY)

            }

            saveProperties()

        }

    

        fun getMusicRepeatMode(): Int {

            return properties.getProperty(KEY_MUSIC_REPEAT_MODE, "0").toInt()

        }

    

        fun setMusicRepeatMode(mode: Int) {

            properties.setProperty(KEY_MUSIC_REPEAT_MODE, mode.toString())

            saveProperties()
        }

        fun getMusicVolume(): Float {
            return properties.getProperty(KEY_MUSIC_VOLUME, "1.0").toFloat()
        }

        fun setMusicVolume(volume: Float) {
            properties.setProperty(KEY_MUSIC_VOLUME, volume.toString())
            saveProperties()
        }

    // Generic get/set methods for settings units
    fun getBoolean(key: String, default: Boolean): Boolean {
        return properties.getProperty(key, default.toString()).toBoolean()
    }

    fun setBoolean(key: String, value: Boolean) {
        properties.setProperty(key, value.toString())
        saveProperties()
    }

    fun getInt(key: String, default: Int): Int {
        return properties.getProperty(key, default.toString()).toIntOrNull() ?: default
    }

    fun setInt(key: String, value: Int) {
        properties.setProperty(key, value.toString())
        saveProperties()
    }

    fun getString(key: String, default: String): String {
        return properties.getProperty(key, default)
    }

    fun setString(key: String, value: String) {
        properties.setProperty(key, value)
        saveProperties()
    }

        companion object {

            private const val PREFS_NAME = "launcher_settings.properties"

            private const val KEY_UI_SCALE = "ui_scale"

            private const val KEY_ENABLE_VERSION_CHECK = "enable_version_check"

            private const val KEY_LAUNCHER_BACKGROUND_URI = "launcher_background_uri"

            private const val KEY_LAUNCHER_BACKGROUND_BLUR = "launcher_background_blur"

            private const val KEY_LAUNCHER_BACKGROUND_BRIGHTNESS = "launcher_background_brightness"

            private const val KEY_ENABLE_PARALLAX = "enable_parallax"

            private const val KEY_PARALLAX_MAGNITUDE = "parallax_magnitude"

            private const val KEY_ENABLE_BACKGROUND_LIGHT_EFFECT = "enable_background_light_effect"

            private const val KEY_ENABLE_BACKGROUND_LIGHT_EFFECT_CUSTOM_COLOR = "enable_background_light_effect_custom_color"

            private const val KEY_BACKGROUND_LIGHT_EFFECT_CUSTOM_COLOR = "background_light_effect_custom_color"

            private const val KEY_ANIMATION_SPEED = "animation_speed"

            private const val KEY_LIGHT_EFFECT_ANIMATION_SPEED = "light_effect_animation_speed"

            private const val KEY_IS_DARK_THEME = "is_dark_theme"

            private const val KEY_SIDEBAR_POSITION = "sidebar_position"

            private const val KEY_THEME_COLOR = "theme_color"

            private const val KEY_CUSTOM_PRIMARY_COLOR = "custom_primary_color"

            private const val DEFAULT_CUSTOM_PRIMARY_COLOR = -9859931 // 0xFF698945 in decimal

            private const val KEY_IS_GLOW_EFFECT_ENABLED = "is_glow_effect_enabled"

            private const val KEY_IS_CARD_BLUR_ENABLED = "is_card_blur_enabled"

            private const val KEY_CARD_ALPHA = "card_alpha"

            private const val KEY_LIGHT_COLOR_SCHEME = "light_color_scheme"

            private const val KEY_DARK_COLOR_SCHEME = "dark_color_scheme"

            private const val KEY_BACKGROUND_ITEMS = "background_items"

            private const val KEY_RANDOM_BACKGROUND = "random_background"

            private const val KEY_IS_MUSIC_PLAYER_ENABLED = "is_music_player_enabled"

            private const val KEY_AUTO_PLAY_MUSIC = "auto_play_music"

            private const val KEY_MUSIC_DIRECTORIES = "music_directories"

            private const val KEY_LAST_SELECTED_MUSIC_DIRECTORY = "last_selected_music_directory"

            private const val KEY_MUSIC_REPEAT_MODE = "music_repeat_mode"

            private const val KEY_MUSIC_VOLUME = "music_volume"

            private const val KEY_CURRENT_GAME_PATH_ID = "current_game_path_id"

        }

    }