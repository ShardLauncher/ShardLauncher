/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.utils.math

import java.math.BigDecimal

data class RankedValue<T>(val value: T, val delta: Int)

fun <T> findNearestPositive(
    targetValue: Int,
    objects: List<T>,
    valueProvider: (T) -> Int
): RankedValue<T>? {
    var minDelta = Int.MAX_VALUE
    var selectedObject: T? = null

    for (obj in objects) {
        val value = valueProvider(obj)
        if (value < targetValue) continue

        val delta = value - targetValue
        if (delta == 0) return RankedValue(obj, 0)
        if (delta < minDelta) {
            minDelta = delta
            selectedObject = obj
        }
    }

    return selectedObject?.let { RankedValue(it, minDelta) }
}

fun Float.addBigDecimal(other: Float): Float =
    BigDecimal(this.toDouble()).add(BigDecimal(other.toDouble())).toFloat()

fun Float.subtractBigDecimal(other: Float): Float =
    BigDecimal(this.toDouble()).subtract(BigDecimal(other.toDouble())).toFloat()

fun Float.multiplyBigDecimal(other: Float): Float =
    BigDecimal(this.toDouble()).multiply(BigDecimal(other.toDouble())).toFloat()

fun Float.divideBigDecimal(other: Float): Float =
    BigDecimal(this.toDouble()).divide(BigDecimal(other.toDouble())).toFloat()
