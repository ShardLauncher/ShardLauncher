package com.lanrhyme.shardlauncher.game.account.auth_server.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servers")
data class AuthServer(
    /**
     * 认证服务器基础链接
     */
    @PrimaryKey
    val baseUrl: String,
    /**
     * 认证服务器显示名称
     */
    var serverName: String,
    /**
     * 认证服务器注册链接（注册新账号跳转）
     */
    var register: String? = null
)
