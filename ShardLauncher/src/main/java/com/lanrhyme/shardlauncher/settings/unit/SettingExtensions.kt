package com.lanrhyme.shardlauncher.settings.unit

/**
 * Extension functions for setting units
 */

fun IntSettingUnit.getOrMin(): Int {
    val value = getValue()
    // IntSettingUnit doesn't have a public 'min' property, but we can access it if we know the structure.
    // Based on the code, it uses valueRange.
    // However, since valueRange is private, I will add a method to IntSettingUnit or just use getValue() 
    // because readValue() already performs coerceIn(valueRange).
    return value
}
