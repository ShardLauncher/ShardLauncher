package com.lanrhyme.shardlauncher.game.account

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lanrhyme.shardlauncher.database.AppDatabase
import com.lanrhyme.shardlauncher.game.account.auth_server.data.AuthServer
import com.lanrhyme.shardlauncher.game.account.auth_server.data.AuthServerDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

object AccountsManager {
    private const val TAG = "AccountsManager"
    private val scope = CoroutineScope(Dispatchers.IO)

    // Account related
    private val _accounts = CopyOnWriteArrayList<Account>()
    private val _accountsFlow = MutableStateFlow<List<Account>>(emptyList())
    val accountsFlow: StateFlow<List<Account>> = _accountsFlow

    private val _currentAccountFlow = MutableStateFlow<Account?>(null)
    val currentAccountFlow: StateFlow<Account?> = _currentAccountFlow

    // Auth Server related
    private val _authServers = CopyOnWriteArrayList<AuthServer>()
    private val _authServersFlow = MutableStateFlow<List<AuthServer>>(emptyList())
    val authServersFlow: StateFlow<List<AuthServer>> = _authServersFlow

    /** Control variable to refresh all account avatars */
    var refreshAccountAvatar by mutableStateOf(false)
        private set

    private lateinit var database: AppDatabase
    private lateinit var accountDao: AccountDao
    private lateinit var authServerDao: AuthServerDao
    private lateinit var context: Context

    // Simple preference storage for current account UUID
    private const val PREFS_NAME = "account_prefs"
    private const val KEY_CURRENT_ACCOUNT_UUID = "current_account_uuid"

    fun initialize(context: Context) {
        this.context = context.applicationContext
        com.lanrhyme.shardlauncher.game.path.PathManager.refreshPaths(context) // Initialize paths
        database = AppDatabase.getInstance(context)
        accountDao = database.accountDao()
        authServerDao = database.authServerDao()
        
        // Initial load
        reloadAccounts()
        reloadAuthServers()
    }

    private fun getCurrentAccountUUID(): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENT_ACCOUNT_UUID, null)
    }

    private fun saveCurrentAccountUUID(uuid: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENT_ACCOUNT_UUID, uuid).apply()
    }

    fun reloadAccounts() {
        scope.launch {
            suspendReloadAccounts()
        }
    }

    fun refreshAccountsAvatar() {
        this.refreshAccountAvatar = !this.refreshAccountAvatar
    }

    private suspend fun suspendReloadAccounts() {
        val loadedAccounts = accountDao.getAllAccounts()
        _accounts.clear()
        _accounts.addAll(loadedAccounts)

        _accounts.sortWith(compareBy<Account>(
            { it.username }
        ))
        _accountsFlow.value = _accounts.toList()

        val currentUUID = getCurrentAccountUUID()
        if (_accounts.isNotEmpty()) {
            if (currentUUID == null || !isAccountExists(currentUUID)) {
                setCurrentAccount(_accounts[0])
            } else {
                 refreshCurrentAccountState()
            }
        } else {
            _currentAccountFlow.value = null
        }

        Log.i(TAG, "Loaded ${_accounts.size} accounts")
    }

    fun reloadAuthServers() {
        scope.launch {
            val loadedServers = authServerDao.getAllServers()
            _authServers.clear()
            _authServers.addAll(loadedServers)

            _authServers.sortWith { o1, o2 -> o1.serverName.compareTo(o2.serverName) }
            _authServersFlow.value = _authServers.toList()

            Log.i(TAG, "Loaded ${_authServers.size} auth servers")
        }
    }

    fun getCurrentAccount(): Account? {
        val uuid = getCurrentAccountUUID() ?: return _accounts.firstOrNull()
        return _accounts.find { it.uniqueUUID == uuid } ?: _accounts.firstOrNull()
    }

    fun setCurrentAccount(account: Account) {
        saveCurrentAccountUUID(account.uniqueUUID)
        refreshCurrentAccountState()
    }

    private fun refreshCurrentAccountState() {
        _currentAccountFlow.value = getCurrentAccount()
    }

    fun saveAccount(account: Account) {
        scope.launch {
            suspendSaveAccount(account)
        }
    }

    suspend fun suspendSaveAccount(account: Account) {
        runCatching {
            accountDao.saveAccount(account)
            Log.i(TAG, "Saved account: ${account.username}")
        }.onFailure { e ->
            Log.e(TAG, "Failed to save account: ${account.username}", e)
        }
        suspendReloadAccounts()
    }

    fun deleteAccount(account: Account) {
        scope.launch {
            accountDao.deleteAccount(account)
            // Cleanup logic for skin files would go here (requires FileUtils/PathManager)
            suspendReloadAccounts()
        }
    }

    suspend fun saveAuthServer(server: AuthServer) {
        runCatching {
            authServerDao.saveServer(server)
            Log.i(TAG, "Saved auth server: ${server.serverName} -> ${server.baseUrl}")
        }.onFailure { e ->
            Log.e(TAG, "Failed to save auth server: ${server.serverName}", e)
        }
        reloadAuthServers()
    }

    fun deleteAuthServer(server: AuthServer) {
        scope.launch {
            authServerDao.deleteServer(server)
            reloadAuthServers()
        }
    }
    
    fun hasMicrosoftAccount(): Boolean = _accounts.any { it.isMicrosoftAccount() }

    fun isAccountExists(uniqueUUID: String): Boolean {
        return uniqueUUID.isNotEmpty() && _accounts.any { it.uniqueUUID == uniqueUUID }
    }

    fun isAuthServerExists(baseUrl: String): Boolean {
        return baseUrl.isNotEmpty() && _authServers.any { it.baseUrl == baseUrl }
    }
}
