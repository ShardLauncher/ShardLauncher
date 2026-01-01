/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.utils.network

import java.util.Objects

/**
 * Server address parser
 */
data class ServerAddress(val host: String, val port: Int) {
    companion object {
        private const val UNKNOWN_PORT = -1
        private val PORT_RANGE = 0..65535
        
        fun parse(address: String): ServerAddress {
            require(address.isNotEmpty()) { "Address cannot be empty" }
            
            return when {
                // Handle IPv6 address -> [host]:port
                address.startsWith('[') -> parseIPv6(address)
                // Normal host:port format
                ':' in address -> parseWithPort(address)
                else -> ServerAddress(address, UNKNOWN_PORT)
            }
        }
        
        private fun parseIPv6(address: String): ServerAddress {
            val closeBracketIndex = address.indexOf(']').takeIf { it != -1 }
                ?: throw illegalAddress(address)
            
            val host = address.substring(1, closeBracketIndex)
            
            return when (val remaining = address.substring(closeBracketIndex + 1)) {
                "" -> ServerAddress(host, UNKNOWN_PORT)
                else -> {
                    require(remaining.startsWith(':')) { "Expected colon after IPv6 address" }
                    val portPart = remaining.substring(1)
                    parsePort(host, portPart)
                }
            }
        }
        
        private fun parseWithPort(address: String): ServerAddress {
            val colonPos = address.indexOf(':')
            val hostPart = address.substring(0, colonPos)
            val portPart = address.substring(colonPos + 1)
            return parsePort(hostPart, portPart)
        }
        
        private fun parsePort(host: String, portPart: String): ServerAddress {
            val port = portPart.toIntOrNull()
                ?.takeIf { it in PORT_RANGE }
                ?: throw illegalAddress("$host:$portPart")
            
            return ServerAddress(host, port)
        }
        
        private fun illegalAddress(address: String): IllegalArgumentException = 
            IllegalArgumentException("Invalid server address: $address")
    }
    
    constructor(host: String) : this(host, UNKNOWN_PORT)
    
    override fun toString(): String = "ServerAddress[host='$host', port=$port]"
}
