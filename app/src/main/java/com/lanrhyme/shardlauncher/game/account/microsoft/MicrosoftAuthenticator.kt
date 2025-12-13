package com.lanrhyme.shardlauncher.game.account.microsoft

import com.lanrhyme.shardlauncher.BuildConfig
import com.lanrhyme.shardlauncher.api.ApiClient
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.account.ACCOUNT_TYPE_MICROSOFT
import com.lanrhyme.shardlauncher.game.account.AccountsManager
import com.lanrhyme.shardlauncher.game.account.microsoft.models.*
import com.lanrhyme.shardlauncher.game.account.wardrobe.SkinModelType
import com.lanrhyme.shardlauncher.utils.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.util.UUID

object MicrosoftAuthenticator {
    private const val CLIENT_ID = BuildConfig.CLIENT_ID // Or hardcoded for now if simpler
    private const val SCOPE = "XboxLive.signin offline_access openid profile email"

    suspend fun getDeviceCode(): DeviceCodeResponse {
        return ApiClient.microsoftAuthService.getDeviceCode(CLIENT_ID, SCOPE)
    }

    suspend fun pollForToken(deviceCode: String, interval: Int, expiresIn: Int): TokenResponse {
        var elapsedTime = 0
        while (elapsedTime < expiresIn) {
            try {
                // We use a custom grant type for device code
                val response = ApiClient.microsoftAuthService.getToken(
                    grantType = "urn:ietf:params:oauth:grant-type:device_code",
                    deviceCode = deviceCode,
                    clientId = CLIENT_ID
                )
                
                if (response.isSuccessful && response.body() != null) {
                    return response.body()!!
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody?.contains("authorization_pending") == true) {
                        // Continue polling
                    } else if (errorBody?.contains("slow_down") == true) {
                        delay(interval * 1000L + 5000) // Add extra delay
                    } else {
                         // Stop on other errors
                         throw IOException("Token polling failed: $errorBody")
                    }
                }
            } catch (e: Exception) {
                Logger.lError("Polling error", e)
            }
            
            delay(interval * 1000L)
            elapsedTime += interval
        }
        throw IOException("Polling timed out")
    }

    suspend fun loginWithMicrosoft(deviceCodeResponse: DeviceCodeResponse): Flow<String> = flow {
        emit("正在等待用户授权...")
        val tokenResponse = pollForToken(deviceCodeResponse.deviceCode, deviceCodeResponse.interval, deviceCodeResponse.expiresIn)
        
        emit("获取Xbox Live令牌...")
        val xblToken = authenticateXBL(tokenResponse.accessToken)
        
        emit("获取XSTS令牌...")
        val xstsToken = authenticateXSTS(xblToken.token)
        
        emit("登录Minecraft...")
        val mcToken = loginWithXbox(xstsToken.uhs, xstsToken.token)
        
        emit("检查游戏所有权...")
        checkOwnership(mcToken.accessToken)
        
        emit("获取档案信息...")
        val profile = getProfile(mcToken.accessToken)
        
        emit("保存账户...")
        val account = Account(
            username = profile.name,
            uniqueUUID = profile.id, // Using profile ID as unique UUID for now
            accessToken = mcToken.accessToken,
            refreshToken = tokenResponse.refreshToken,
            accountType = ACCOUNT_TYPE_MICROSOFT,
            profileId = profile.id,
            // skinModelType = SkinModelType.NONE // Placeholder, need logic to parse skins
        )
        // Parse skins if available
        if (profile.skins.isNotEmpty()) {
             // Simple logic for now
             val skin = profile.skins.first()
             account.skinUrl = skin.url
             // account.skinModelType = if (skin.variant == "slim") SkinModelType.ALEX else SkinModelType.STEVE
             // For now just skip logic
             if (skin.variant == "slim") {
                 // do nothing
             }
        }
        
        AccountsManager.saveAccount(account)
        emit("登录成功!")
    }

    private suspend fun authenticateXBL(accessToken: String): XBLResponse {
        val request = XBLRequest(
            properties = XBLProperties("RPS", "user.auth.xboxlive.com", "d=$accessToken"),
            relyingParty = "http://auth.xboxlive.com",
            tokenType = "JWT"
        )
        val response = ApiClient.minecraftAuthService.authXbl(request)
        if (!response.isSuccessful) throw IOException("XBL Auth failed: ${response.code()}")
        return response.body()!!
    }

    private suspend fun authenticateXSTS(xblToken: String): XSTSAuthResult {
        val request = XSTSRequest(
             properties = XSTSProperties("RETAIL", listOf(xblToken)),
             relyingParty = "rp://api.minecraftservices.com/",
             tokenType = "JWT"
        )
        val response = ApiClient.minecraftAuthService.authXsts(request)
        if (!response.isSuccessful) throw IOException("XSTS Auth failed: ${response.code()}")
        val body = response.body()!!
        return XSTSAuthResult(body.token, body.displayClaims.xui[0].uhs)
    }

    private suspend fun loginWithXbox(uhs: String, xstsToken: String): MinecraftAuthResponse {
        val request = MinecraftAuthRequest("XBL3.0 x=$uhs;$xstsToken")
        val response = ApiClient.minecraftAuthService.loginWithMinecraft(request)
         if (!response.isSuccessful) throw IOException("Minecraft Login failed: ${response.code()}")
        return response.body()!!
    }

    private suspend fun checkOwnership(accessToken: String) {
        val response = ApiClient.minecraftAuthService.checkOwnership("Bearer $accessToken")
         if (!response.isSuccessful) throw IOException("Ownership check failed: ${response.code()}")
        val body = response.body()!!
        if (body.items.isEmpty()) throw IOException("Account does not own Minecraft")
    }

    private suspend fun getProfile(accessToken: String): MinecraftProfile {
        val response = ApiClient.minecraftAuthService.getMinecraftProfile("Bearer $accessToken")
         if (!response.isSuccessful) throw IOException("Profile fetch failed: ${response.code()}")
        return response.body()!!
    }

    private data class XSTSAuthResult(val token: String, val uhs: String)
}
