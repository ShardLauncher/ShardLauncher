package com.lanrhyme.shardlauncher.game.account

import androidx.room.Entity
import androidx.room.PrimaryKey
// import com.lanrhyme.shardlauncher.game.account.wardrobe.SkinModelType
// import com.lanrhyme.shardlauncher.game.account.wardrobe.SkinFileDownloader
// import com.lanrhyme.shardlauncher.game.path.PathManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

@Entity(tableName = "accounts")
data class Account(
    /**
     * 唯一 UUID，标识该账号
     */
    @PrimaryKey
    val uniqueUUID: String = UUID.randomUUID().toString().lowercase(),
    var accessToken: String = "0",
    var clientToken: String = "0",
    var username: String = "Steve",
    var profileId: String = "", 
    var refreshToken: String = "0",
    var xUid: String? = null,
    var otherBaseUrl: String? = null,
    var otherAccount: String? = null,
    var otherPassword: String? = null,
    var accountType: String? = null,
    // var skinModelType: SkinModelType = SkinModelType.NONE
) {
    // Properties for UI logic
    @androidx.room.Ignore
    var skinUrl: String? = null 

/*
    fun getSkinFile() = File(PathManager.DIR_ACCOUNT_SKIN, "$uniqueUUID.png")

    val hasSkinFile: Boolean
        get() = getSkinFile().exists()

    suspend fun downloadSkin() = withContext(Dispatchers.IO) {
        when {
            isMicrosoftAccount() -> updateSkin("https://sessionserver.mojang.com")
            isAuthServerAccount() -> updateSkin(otherBaseUrl!!.removeSuffix("/") + "/sessionserver/")
            else -> {}
        }
    }

    private suspend fun updateSkin(url: String) {
        val skinFile = getSkinFile()
        if (skinFile.exists()) skinFile.delete() 

        runCatching {
            SkinFileDownloader().yggdrasil(url, skinFile, profileId) { modelType ->
                // this.skinModelType = modelType
            }
        }.onFailure { e ->
             e.printStackTrace()
        }
        AccountsManager.refreshAccountsAvatar()
    }
*/
}
