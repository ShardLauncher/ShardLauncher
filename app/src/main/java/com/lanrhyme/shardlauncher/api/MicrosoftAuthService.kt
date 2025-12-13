package com.lanrhyme.shardlauncher.api

import com.lanrhyme.shardlauncher.model.auth.AuthTokenResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface MicrosoftAuthService {

    @FormUrlEncoded
    @POST("consumers/oauth2/v2.0/token")
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("grant_type") grantType: String = "authorization_code"
    ): AuthTokenResponse

    @FormUrlEncoded
    @POST("consumers/oauth2/v2.0/devicecode")
    suspend fun getDeviceCode(
        @Field("client_id") clientId: String,
        @Field("scope") scope: String
    ): com.lanrhyme.shardlauncher.game.account.microsoft.models.DeviceCodeResponse

    @FormUrlEncoded
    @POST("consumers/oauth2/v2.0/token")
    suspend fun getToken(
        @Field("grant_type") grantType: String,
        @Field("device_code") deviceCode: String,
        @Field("client_id") clientId: String
    ): retrofit2.Response<com.lanrhyme.shardlauncher.game.account.microsoft.models.TokenResponse>
}