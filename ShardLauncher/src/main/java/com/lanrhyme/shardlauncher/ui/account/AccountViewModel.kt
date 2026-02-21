package com.lanrhyme.shardlauncher.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.AccountType
import com.movtery.zalithlauncher.game.account.localLogin
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MicrosoftLoginState {
    object Idle : MicrosoftLoginState()
    object InProgress : MicrosoftLoginState()
    object Success : MicrosoftLoginState()
    data class Error(val message: String) : MicrosoftLoginState()
}

class AccountViewModel(application: Application) : AndroidViewModel(application) {

    val accounts: StateFlow<List<Account>> = AccountsManager.accountsFlow
    val selectedAccount: StateFlow<Account?> = AccountsManager.currentAccountFlow

    val microsoftLoginState = kotlinx.coroutines.flow.MutableStateFlow<MicrosoftLoginState>(MicrosoftLoginState.Idle)

    fun startMicrosoftLogin() {
        viewModelScope.launch {
            try {
                microsoftLoginState.value = MicrosoftLoginState.InProgress
                // TODO: Implement Microsoft login using ZalithLauncherCore
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
        localLogin(username, null)
    }

    fun deleteAccount(account: Account) {
        AccountsManager.deleteAccount(account)
    }

    fun updateOfflineAccount(account: Account, newUsername: String) {
        account.username = newUsername
        AccountsManager.saveAccount(account)
    }
}
