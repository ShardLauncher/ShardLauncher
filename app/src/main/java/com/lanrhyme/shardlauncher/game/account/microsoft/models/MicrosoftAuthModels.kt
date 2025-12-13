package com.lanrhyme.shardlauncher.game.account.microsoft.models

import com.google.gson.annotations.SerializedName

data class DeviceCodeResponse(
    @SerializedName("device_code") val deviceCode: String,
    @SerializedName("user_code") val userCode: String,
    @SerializedName("verification_uri") val verificationUri: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("interval") val interval: Int,
    @SerializedName("message") val message: String?
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("scope") val scope: String?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("user_id") val userId: String?
)

data class ErrorResponse(
    @SerializedName("error") val error: String,
    @SerializedName("error_description") val errorDescription: String?
)

// Xbox Live Auth
data class XBLRequest(
    @SerializedName("Properties") val properties: XBLProperties,
    @SerializedName("RelyingParty") val relyingParty: String,
    @SerializedName("TokenType") val tokenType: String
)

data class XBLProperties(
    @SerializedName("AuthMethod") val authMethod: String,
    @SerializedName("SiteName") val siteName: String,
    @SerializedName("RpsTicket") val rpsTicket: String
)

data class XBLResponse(
    @SerializedName("IssueInstant") val issueInstant: String,
    @SerializedName("NotAfter") val notAfter: String,
    @SerializedName("Token") val token: String,
    @SerializedName("DisplayClaims") val displayClaims: DisplayClaims
)

data class DisplayClaims(
    @SerializedName("xui") val xui: List<XuiClaim>
)

data class XuiClaim(
    @SerializedName("uhs") val uhs: String
)

// XSTS Auth
data class XSTSRequest(
    @SerializedName("Properties") val properties: XSTSProperties,
    @SerializedName("RelyingParty") val relyingParty: String,
    @SerializedName("TokenType") val tokenType: String
)

data class XSTSProperties(
    @SerializedName("SandboxId") val sandboxId: String,
    @SerializedName("UserTokens") val userTokens: List<String>
)

data class XSTSResponse(
    @SerializedName("IssueInstant") val issueInstant: String,
    @SerializedName("NotAfter") val notAfter: String,
    @SerializedName("Token") val token: String,
    @SerializedName("DisplayClaims") val displayClaims: DisplayClaims,
    @SerializedName("XErr") val xErr: String? // Error code if any
)

// Minecraft Auth
data class MinecraftAuthRequest(
    @SerializedName("identityToken") val identityToken: String
)

data class MinecraftAuthResponse(
    @SerializedName("username") val username: String?, // Sometimes returned
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("token_type") val tokenType: String
)

// Game Ownership
data class EntitlementsResponse(
    @SerializedName("items") val items: List<EntitlementItem>,
    @SerializedName("signature") val signature: String,
    @SerializedName("keyId") val keyId: String
)

data class EntitlementItem(
    @SerializedName("name") val name: String,
    @SerializedName("signature") val signature: String
)

// Minecraft Profile
data class MinecraftProfile(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("skins") val skins: List<MinecraftSkin>,
    @SerializedName("capes") val capes: List<MinecraftCape>
)

data class MinecraftSkin(
    @SerializedName("id") val id: String,
    @SerializedName("state") val state: String,
    @SerializedName("url") val url: String,
    @SerializedName("variant") val variant: String?,
    @SerializedName("alias") val alias: String?
)

data class MinecraftCape(
    @SerializedName("id") val id: String,
    @SerializedName("state") val state: String,
    @SerializedName("url") val url: String,
    @SerializedName("alias") val alias: String?
)
