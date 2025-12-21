/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.settings

object AllSettings {
    // RAM allocation in MB (default: 2048MB = 2GB)
    private var ramAllocationValue = 2048
    
    fun ramAllocation() = object {
        fun getValue() = ramAllocationValue
        fun setValue(value: Int) {
            ramAllocationValue = value
        }
    }

    // Resolution ratio (default: 100%)
    private var resolutionRatioValue = 100
    
    fun resolutionRatio() = object {
        fun getValue() = resolutionRatioValue
        fun setValue(value: Int) {
            resolutionRatioValue = value
        }
    }

    // Advanced settings with default false values
    private var dumpShadersValue = false
    fun dumpShaders() = object {
        fun getValue() = dumpShadersValue
        fun setValue(value: Boolean) {
            dumpShadersValue = value
        }
    }

    private var zinkPreferSystemDriverValue = false
    fun zinkPreferSystemDriver() = object {
        fun getValue() = zinkPreferSystemDriverValue
        fun setValue(value: Boolean) {
            zinkPreferSystemDriverValue = value
        }
    }

    private var vsyncInZinkValue = false
    fun vsyncInZink() = object {
        fun getValue() = vsyncInZinkValue
        fun setValue(value: Boolean) {
            vsyncInZinkValue = value
        }
    }

    private var bigCoreAffinityValue = false
    fun bigCoreAffinity() = object {
        fun getValue() = bigCoreAffinityValue
        fun setValue(value: Boolean) {
            bigCoreAffinityValue = value
        }
    }
}
