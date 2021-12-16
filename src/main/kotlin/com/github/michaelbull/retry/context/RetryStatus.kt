package com.github.michaelbull.retry.context

import com.github.michaelbull.retry.retry
import com.github.michaelbull.retry.saturatedAdd

/**
 * Represents the current status of a [retrying][retry] operation.
 */
class RetryStatus(
    attempt: Int = 0,
    previousDelay: Long = 0,
    cumulativeDelay: Long = 0
) {

    /**
     * The zero-based attempt number.
     */
    var attempt: Int = attempt
        private set(value) {
            require(value >= 0) { "attempt must be non-negative: $value" }
            field = value
        }

    /**
     * The delay between this attempt and the previous.
     */
    var previousDelay: Long = previousDelay
        set(value) {
            require(value >= 0) { "previousDelay must be non-negative: $value" }
            field = value
        }

    /**
     * The cumulative delay across all attempts.
     */
    var cumulativeDelay: Long = cumulativeDelay
        private set(value) {
            require(value >= 0) { "cumulativeDelay must be non-negative: $value" }
            field = value
        }

    fun incrementAttempts() {
        attempt++
    }

    fun incrementCumulativeDelay(delayMillis: Long) {
        require(delayMillis > 0) { "delayMillis must be positive: $delayMillis" }
        cumulativeDelay = cumulativeDelay saturatedAdd delayMillis
    }

    override fun toString(): String {
        return "RetryStatus(attempt=$attempt, previousDelay=$previousDelay, cumulativeDelay=$cumulativeDelay)"
    }
}
