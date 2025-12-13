package com.lanrhyme.shardlauncher.game.account

import com.lanrhyme.shardlauncher.game.account.auth_server.data.AuthServer
import java.util.Objects

const val ACCOUNT_TYPE_MICROSOFT = "Microsoft"
const val ACCOUNT_TYPE_LOCAL = "Local"

fun Account.isAuthServerAccount(): Boolean {
    return !isLocalAccount() && !Objects.isNull(otherBaseUrl) && otherBaseUrl != "0"
}

fun Account.isMicrosoftAccount(): Boolean {
    return accountType == ACCOUNT_TYPE_MICROSOFT
}

fun Account.isLocalAccount(): Boolean {
    return accountType == ACCOUNT_TYPE_LOCAL
}

fun Account?.isNoLoginRequired(): Boolean {
    return this == null || isLocalAccount()
}

fun Account.accountTypePriority(): Int {
    return when (this.accountType) {
        ACCOUNT_TYPE_MICROSOFT -> 0 //微软账号优先
        null -> Int.MAX_VALUE
        else -> 1
    }
}

fun Account.getDisplayName(): String {
    return if (isMicrosoftAccount()) "微软账号" else if (accountType == ACCOUNT_TYPE_LOCAL) "离线账号" else accountType ?: "未知"
}
