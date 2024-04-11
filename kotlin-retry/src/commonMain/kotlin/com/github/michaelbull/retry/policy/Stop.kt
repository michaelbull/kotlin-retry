package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.attempt.FailedAttempt
import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.RetryAfter
import com.github.michaelbull.retry.instruction.RetryInstruction
import com.github.michaelbull.retry.instruction.StopRetrying
import com.github.michaelbull.retry.saturatedAdd

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to [StopRetrying] after the
 * [number of attempts][FailedAttempt.number] reaches the specified [count].
 *
 * Unlike [stopAtRetries], the first invocation **is** counted towards the limit.
 *
 * ```kotlin
 * var attempts = 1
 *
 * retry(stopAtAttempts(3)) {
 *     println(attempts++)
 *     throw RuntimeException()
 * }
 *
 * // prints:
 * // 1
 * // 2
 * // 3
 * ```
 *
 * @throws IllegalArgumentException if [count] is not positive.
 */
public fun <E> stopAtAttempts(count: Int): RetryPolicy<E> {
    require(count > 0) { "count must be positive, but was $count" }

    return stopIf { attempt ->
        attempt.number + 1 >= count
    }
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to [StopRetrying] after the
 * [number of attempts][FailedAttempt.number] reaches the specified [count].
 *
 * Unlike [stopAtAttempts], the first invocation **is not** counted towards the limit.
 *
 * ```kotlin
 * var attempt = 1
 *
 * retry(stopAtRetries(3)) {
 *     println(attempt++)
 *     throw RuntimeException()
 * }
 *
 * // prints:
 * // 1
 * // 2
 * // 3
 * // 4
 * ```
 *
 * @throws [IllegalArgumentException] if [count] is negative.
 */
public fun <E> stopAtRetries(count: Int): RetryPolicy<E> {
    require(count >= 0) { "count must be non-negative, but was $count" }

    return stopIf { attempt ->
        attempt.number >= count
    }
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to [StopRetrying] if this policy returns an
 * [instruction][RetryInstruction] to [RetryAfter] an amount of time that is greater than or equal to [delayMillis].
 *
 * @throws IllegalArgumentException if [delayMillis] is not positive.
 */
public fun <E> RetryPolicy<E>.stopAtDelay(delayMillis: Long): RetryPolicy<E> {
    require(delayMillis > 0) { "delayMillis must be positive, but was: $delayMillis" }

    return RetryPolicy { attempt ->
        val instruction = this(attempt)

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
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to [StopRetrying] if this policy returns an
 * [instruction][RetryInstruction] to [RetryAfter] an amount of time that, when added to the
 * [FailedAttempt.cumulativeDelay], is greater than or equal to [delayMillis].
 *
 * @throws IllegalArgumentException if [delayMillis] is not positive.
 */
public fun <E> RetryPolicy<E>.stopAtCumulativeDelay(delayMillis: Long): RetryPolicy<E> {
    require(delayMillis > 0) { "delayMillis must be positive, but was: $delayMillis" }

    return RetryPolicy { attempt ->
        val instruction = this(attempt)

        if (instruction == StopRetrying || instruction == ContinueRetrying) {
            instruction
        } else {
            val delay = attempt.cumulativeDelay saturatedAdd instruction.delayMillis

            if (delay >= delayMillis) {
                StopRetrying
            } else {
                instruction
            }
        }
    }
}
