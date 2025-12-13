package com.lanrhyme.shardlauncher.game.account.wardrobe

import android.util.Base64
import com.google.gson.JsonObject
import com.lanrhyme.shardlauncher.utils.Logger
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import com.google.gson.Gson

class SkinFileDownloader {
    // We create a new client or use a shared one. Using a new one or shared is fine.
    // For simplicity, defining a basic one here to match ported logic's autonomy.
    private val mClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
        
    private val gson = Gson()

    /**
     * 尝试下载yggdrasil皮肤
     */
    @Throws(Exception::class)
    suspend fun yggdrasil(
        url: String,
        skinFile: File,
        uuid: String,
        changeSkinModel: (SkinModelType) -> Unit
    ) {
        val profileJson = fetchStringFromUrl("${url.removeSuffix("/")}/session/minecraft/profile/$uuid")
        val profileObject = gson.fromJson(profileJson, JsonObject::class.java)
        val properties = profileObject.get("properties").asJsonArray
        val rawValue = properties.get(0).asJsonObject.get("value").asString

        // Decode Base64 using Android Util
        val valueBytes = Base64.decode(rawValue, Base64.DEFAULT)
        val value = String(valueBytes)

        val valueObject = gson.fromJson(value, JsonObject::class.java)
        val texturesObject = valueObject.get("textures").asJsonObject
        
        if (!texturesObject.has("SKIN")) {
            // No skin
             changeSkinModel(SkinModelType.NONE)
             return
        }
        
        val skinObject = texturesObject.get("SKIN").asJsonObject
        val skinUrl = skinObject.get("url").asString

        val skinModelType = runCatching {
            skinObject.takeIf {
                it.has("metadata")
            }?.get("metadata")?.asJsonObject?.takeIf {
                it.has("model")
            }?.get("model")?.asString?.let { model ->
                if (model == "slim") SkinModelType.ALEX else SkinModelType.STEVE
            } ?: SkinModelType.STEVE
        }.getOrElse {
            Logger.lInfo("Can not get skin model type, defaulting to STEVE")
            SkinModelType.STEVE
        }

        downloadSkin(skinUrl, skinFile)
        changeSkinModel(skinModelType)
    }
    
    // Helper to fetch string - simplified since we don't need the full 'utils.network' package yet
    private fun fetchStringFromUrl(url: String): String {
        val request = Request.Builder().url(url).build()
        mClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("Failed to fetch $url: ${response.code}")
            return response.body?.string() ?: ""
        }
    }

    private fun downloadSkin(url: String, skinFile: File) {
        skinFile.parentFile?.apply {
            if (!exists()) mkdirs()
        }

        val request = Request.Builder()
            .url(url)
            .build()

        mClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                // throw RuntimeException("Unexpected code ${response.code}") // Use .code property
                 throw RuntimeException("Unexpected code $response")
            }

            try {
                // Use .body property
                response.body?.byteStream()?.use { inputStream ->
                    FileOutputStream(skinFile).use { outputStream ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.lError("Failed to download skin file", e)
            }
        }
    }
}
