package com.lanrhyme.shardlauncher.api

import com.google.gson.JsonObject
import com.lanrhyme.shardlauncher.game.account.microsoft.models.MinecraftAuthResponse
import com.lanrhyme.shardlauncher.game.account.microsoft.models.MinecraftProfile
import com.lanrhyme.shardlauncher.game.account.microsoft.models.XBLResponse
import com.lanrhyme.shardlauncher.game.account.microsoft.models.XSTSResponse
import com.lanrhyme.shardlauncher.game.account.microsoft.models.XBLRequest
import com.lanrhyme.shardlauncher.game.account.microsoft.models.XSTSRequest
import com.lanrhyme.shardlauncher.game.account.microsoft.models.MinecraftAuthRequest
import com.lanrhyme.shardlauncher.game.account.microsoft.models.EntitlementsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface MinecraftAuthService {

    @POST("https://user.auth.xbox.com/user/authenticate")
    suspend fun authXbl(@Body body: XBLRequest): retrofit2.Response<XBLResponse>

    @POST("https://xsts.auth.xbox.com/xsts/authorize")
    suspend fun authXsts(@Body body: XSTSRequest): retrofit2.Response<XSTSResponse>

    @POST("https://api.minecraftservices.com/authentication/login_with_xbox")
    suspend fun loginWithMinecraft(@Body body: MinecraftAuthRequest): retrofit2.Response<MinecraftAuthResponse>

    @GET("https://api.minecraftservices.com/minecraft/profile")
    suspend fun getMinecraftProfile(@Header("Authorization") auth: String): retrofit2.Response<MinecraftProfile>

    @GET("https://api.minecraftservices.com/entitlements/mcstore")
    suspend fun checkOwnership(@Header("Authorization") auth: String): retrofit2.Response<EntitlementsResponse>
}