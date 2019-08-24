package com.github.michaelbull.retry

/**
 * Multiplies [this] value by the [other] value, unless it would overflow in
 * which case [Long.MAX_VALUE] is returned.
 */
infix fun Long.saturatedMultiply(other: Long): Long {
    require(this >= 0 && other >= 0) { "saturatedMultiply is optimized for non-negative longs: $this x $other" }

    return if (this == 0L || other <= Long.MAX_VALUE / this) {
        this * other
    } else {
        Long.MAX_VALUE
    }
}

/**
 * Adds the [other] value to [this] value, unless it would overflow in which
 * case [Long.MAX_VALUE] is returned.
 */
infix fun Long.saturatedAdd(other: Long): Long {
    require(this >= 0 && other >= 0) { "saturatedAdd is optimized for non-negative longs: $this + $other" }

    return if (this == 0L || other <= Long.MAX_VALUE - this) {
        this + other
    } else {
        Long.MAX_VALUE
    }
}

/**
 * Returns 2 to the power of [this], unless it would overflow in which case
 * [Long.MAX_VALUE] is returned.
 */
fun Int.binaryExponential(): Long {
    return if (this < Long.SIZE_BITS - 1) {
        1L shl this
    } else {
        Long.MAX_VALUE
    }
}
