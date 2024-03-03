package com.github.michaelbull.retry.attempt

public data class FailedAttempt<E>(

    /**
     * The attempt failure.
     */
    val failure: E,

    /**
     * The zero-based attempt number.
     */
    val number: Int,

    /**
     * The delay between this attempt and the previous.
     */
    val previousDelay: Long,

    /**
     * The cumulative delay across all attempts.
     */
    val cumulativeDelay: Long,
)
