package com.lanrhyme.shardlauncher.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import com.lanrhyme.shardlauncher.data.AuthRepository // Keep generic auth logic if needed, but for now ignoring
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.account.AccountsManager
import com.lanrhyme.shardlauncher.game.account.ACCOUNT_TYPE_LOCAL
import com.lanrhyme.shardlauncher.game.account.ACCOUNT_TYPE_MICROSOFT
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class MicrosoftLoginState {
    object Idle : MicrosoftLoginState()
    object InProgress : MicrosoftLoginState()
    object Success : MicrosoftLoginState()
    data class Error(val message: String) : MicrosoftLoginState()
}

class AccountViewModel : ViewModel() {

    // Expose flows directly from AccountsManager
    val accounts: StateFlow<List<Account>> = AccountsManager.accountsFlow
    val selectedAccount: StateFlow<Account?> = AccountsManager.currentAccountFlow

    // Login state logic
    val microsoftLoginState = kotlinx.coroutines.flow.MutableStateFlow<MicrosoftLoginState>(MicrosoftLoginState.Idle)

    // Holds the device code response for UI to display
    val deviceCodeData = kotlinx.coroutines.flow.MutableStateFlow<com.lanrhyme.shardlauncher.game.account.microsoft.models.DeviceCodeResponse?>(null)

    fun startMicrosoftLogin() {
        viewModelScope.launch {
            try {
                microsoftLoginState.value = MicrosoftLoginState.InProgress
                val response = com.lanrhyme.shardlauncher.game.account.microsoft.MicrosoftAuthenticator.getDeviceCode()
                deviceCodeData.value = response
                
                // Start polling automatically or wait for user confirmation?
                // Usually we display code and start polling immediately
                com.lanrhyme.shardlauncher.game.account.microsoft.MicrosoftAuthenticator.loginWithMicrosoft(response)
                    .collect { status ->
                         // Update status message? We could add a Status state to MicrosoftLoginState
                         // For now just log or keep InProgress
                    }
                microsoftLoginState.value = MicrosoftLoginState.Success
            } catch (e: Exception) {
                microsoftLoginState.value = MicrosoftLoginState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun cancelMicrosoftLogin() {
         resetMicrosoftLoginState()
    }

    fun resetMicrosoftLoginState() {
        microsoftLoginState.value = MicrosoftLoginState.Idle
    }

    fun selectAccount(account: Account) {
        AccountsManager.setCurrentAccount(account)
    }

    fun addOfflineAccount(username: String) {
        val newAccount = Account(
            username = username,
            accountType = ACCOUNT_TYPE_LOCAL,
            profileId = com.lanrhyme.shardlauncher.game.account.wardrobe.getLocalUUIDWithSkinModel(username, com.lanrhyme.shardlauncher.game.account.wardrobe.SkinModelType.NONE)
            // ID is auto-generated
        )
        AccountsManager.saveAccount(newAccount)
    }

    fun deleteAccount(account: Account) {
        AccountsManager.deleteAccount(account)
    }

    fun updateOfflineAccount(account: Account, newUsername: String) {
        account.username = newUsername
        AccountsManager.saveAccount(account)
    }
}
