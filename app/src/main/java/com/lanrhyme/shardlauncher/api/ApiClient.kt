package com.lanrhyme.shardlauncher.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val NEWS_BASE_URL = "https://news.bugjump.net/"
    private const val BMCLAPI_BASE_URL = "https://bmclapi2.bangbang93.com/"
    private const val FABRIC_BASE_URL = "https://bmclapi2.bangbang93.com/fabric-meta/"
    private const val QUILT_BASE_URL = "https://bmclapi2.bangbang93.com/quilt-meta/"
    private const val MICROSOFT_AUTH_BASE_URL = "https://login.microsoftonline.com/"
    private const val MOJANG_API_BASE_URL = "https://api.mojang.com/"
    private const val RMS_API_BASE_URL = "http://api.rms.net.cn/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun <T> createService(baseUrl: String, serviceClass: Class<T>): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(serviceClass)
    }

    val versionApiService: VersionApiService by lazy {
        createService(NEWS_BASE_URL, VersionApiService::class.java)
    }

    val bmclapiService: BmclapiService by lazy {
        createService(BMCLAPI_BASE_URL, BmclapiService::class.java)
    }

    val fabricApiService: FabricApiService by lazy {
        createService(FABRIC_BASE_URL, FabricApiService::class.java)
    }

    val forgeApiService: ForgeApiService by lazy {
        createService(BMCLAPI_BASE_URL, ForgeApiService::class.java)
    }

    val neoForgeApiService: NeoForgeApiService by lazy {
        createService(BMCLAPI_BASE_URL, NeoForgeApiService::class.java)
    }

    val quiltApiService: QuiltApiService by lazy {
        createService(QUILT_BASE_URL, QuiltApiService::class.java)
    }

    val optiFineApiService: OptiFineApiService by lazy {
        createService(BMCLAPI_BASE_URL, OptiFineApiService::class.java)
    }

    val microsoftAuthService: MicrosoftAuthService by lazy {
        createService(MICROSOFT_AUTH_BASE_URL, MicrosoftAuthService::class.java)
    }

    val minecraftAuthService: MinecraftAuthService by lazy {
        createService(MOJANG_API_BASE_URL, MinecraftAuthService::class.java) // Base URL matters less for @Url or absolute URLs mainly used in interface
    }

    val mojangApiService: MojangApiService by lazy {
        createService(MOJANG_API_BASE_URL, MojangApiService::class.java)
    }

    val rmsApiService: RmsApiService by lazy {
        createService(RMS_API_BASE_URL, RmsApiService::class.java)
    }
}
