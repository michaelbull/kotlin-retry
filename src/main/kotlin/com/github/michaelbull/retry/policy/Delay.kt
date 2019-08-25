package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.ContinueRetrying
import com.github.michaelbull.retry.RetryAfter
import com.github.michaelbull.retry.RetryInstruction
import com.github.michaelbull.retry.StopRetrying
import kotlin.math.min

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to
 * [RetryAfter] the specified [delayMillis].
 */
fun constantDelay(delayMillis: Long): RetryPolicy<*> {
    require(delayMillis > 0) { "delayMillis must be positive: $delayMillis" }

    return {
        RetryAfter(delayMillis)
    }
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to
 * [RetryAfter] the specified [delayMillis] if [this] policy returns an
 * [instruction][RetryInstruction] to [RetryAfter] an amount of time that is
 * greater than [delayMillis].
 */
fun <E> RetryPolicy<E>.maxDelay(delayMillis: Long): RetryPolicy<E> {
    require(delayMillis > 0) { "delayMillis must be positive: $delayMillis" }

    return {
        val instruction = this.(this@maxDelay)()

        if (instruction == StopRetrying || instruction == ContinueRetrying) {
            instruction
        } else {
            RetryAfter(min(instruction.delayMillis, delayMillis))
        }
    }
}
