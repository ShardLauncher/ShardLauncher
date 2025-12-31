/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.account

/**
 * Account type constants
 */
object AccountType {
    const val LOCAL = "local"
    const val MICROSOFT = "microsoft"
    const val AUTHSERVER = "authserver"
}

// Legacy constants for backward compatibility
const val ACCOUNT_TYPE_LOCAL = AccountType.LOCAL
const val ACCOUNT_TYPE_MICROSOFT = AccountType.MICROSOFT
const val ACCOUNT_TYPE_AUTHSERVER = AccountType.AUTHSERVER

/**
 * Check if account is local/offline account
 */
fun Account.isLocalAccount(): Boolean {
    return accountType == AccountType.LOCAL
}

/**
 * Check if account is Microsoft account
 */
fun Account.isMicrosoftAccount(): Boolean {
    return accountType == AccountType.MICROSOFT
}

/**
 * Check if account is auth server account
 */
fun Account.isAuthServerAccount(): Boolean {
    return accountType == AccountType.AUTHSERVER
}

/**
 * Check if account has skin file
 */
val Account.hasSkinFile: Boolean
    get() = false // Simplified implementation

/**
 * Get display name for account
 */
fun Account.getDisplayName(): String {
    return username.takeIf { it.isNotBlank() } ?: "Unknown User"
}

/**
 * Get account type priority for sorting
 */
fun Account.accountTypePriority(): Int {
    return when (accountType) {
        AccountType.MICROSOFT -> 0
        AccountType.AUTHSERVER -> 1
        AccountType.LOCAL -> 2
        else -> 3
    }
}