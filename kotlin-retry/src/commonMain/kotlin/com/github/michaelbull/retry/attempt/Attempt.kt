package com.github.michaelbull.retry.attempt

public fun firstAttempt(): Attempt {
    return Attempt(
        number = 0,
        previousDelay = 0,
        cumulativeDelay = 0,
    )
}

public data class Attempt(

    /**
     * The zero-based attempt number.
     */
    private var number: Int,

    /**
     * The delay between this attempt and the previous.
     */
    private var previousDelay: Long,

    /**
     * The cumulative delay across all attempts.
     */
    private var cumulativeDelay: Long,
) {

    init {
        require(number >= 0) { "number must be non-negative, but was $number" }
        require(previousDelay >= 0) { "previousDelay must be non-negative, but was $previousDelay" }
        require(cumulativeDelay >= 0) { "cumulativeDelay must be non-negative, but was $cumulativeDelay" }
    }

    public fun <E> failedWith(failure: E): FailedAttempt<E> {
        return FailedAttempt(
            failure = failure,
            number = number,
            previousDelay = previousDelay,
            cumulativeDelay = cumulativeDelay,
        )
    }

    public fun retryImmediately() {
        number += 1
        previousDelay = 0
    }

    public fun retryAfter(delayMillis: Long) {
        number += 1
        previousDelay = delayMillis
        cumulativeDelay += delayMillis
    }
}
