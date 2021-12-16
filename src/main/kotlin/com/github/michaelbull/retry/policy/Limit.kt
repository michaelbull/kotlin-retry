package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.ContinueRetrying
import com.github.michaelbull.retry.RetryAfter
import com.github.michaelbull.retry.RetryInstruction
import com.github.michaelbull.retry.StopRetrying
import com.github.michaelbull.retry.context.RetryStatus
import com.github.michaelbull.retry.saturatedAdd

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to
 * [StopRetrying] after the [RetryStatus.attempt] reaches the specified
 * [limit].
 */
fun limitAttempts(limit: Int): RetryPolicy<*> {
    require(limit > 0) { "limit must be positive: $limit" }

    return {
        if (attempt + 1 >= limit) {
            StopRetrying
        } else {
            ContinueRetrying
        }
    }
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to
 * [StopRetrying] if [this] policy returns an [instruction][RetryInstruction]
 * to [RetryAfter] an amount of time that is greater than or equal to
 * [delayMillis].
 */
fun <E> RetryPolicy<E>.limitByDelay(delayMillis: Long): RetryPolicy<E> {
    require(delayMillis > 0) { "delayMillis must be positive: $delayMillis" }

    return {
        val instruction = this@limitByDelay.invoke(this)

        if (instruction == StopRetrying || instruction == ContinueRetrying) {
            instruction
        } else if (instruction.delayMillis >= delayMillis) {
            StopRetrying
        } else {
            instruction
        }
    }
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to
 * [StopRetrying] if [this] policy returns an [instruction][RetryInstruction]
 * to [RetryAfter] an amount of time that when added to the
 * [RetryStatus.cumulativeDelay] is greater than or equal to [delayMillis].
 */
fun <E> RetryPolicy<E>.limitByCumulativeDelay(delayMillis: Long): RetryPolicy<E> {
    require(delayMillis > 0) { "delayMillis must be positive: $delayMillis" }

    return {
        val instruction = this@limitByCumulativeDelay.invoke(this)

        if (instruction == StopRetrying || instruction == ContinueRetrying) {
            instruction
        } else {
            val delay = cumulativeDelay saturatedAdd instruction.delayMillis

            if (delay >= delayMillis) {
                StopRetrying
            } else {
                instruction
            }
        }
    }
}
