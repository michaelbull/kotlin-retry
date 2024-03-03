package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.RetryAfter
import com.github.michaelbull.retry.instruction.RetryInstruction
import com.github.michaelbull.retry.instruction.StopRetrying

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to [RetryAfter] the specified [delayMillis].
 */
fun <E> constantDelay(delayMillis: Long): RetryPolicy<E> {
    val instruction = RetryAfter(delayMillis)
    return RetryPolicy { instruction }
}

/**
 * Creates a [RetryPolicy] ensuring the [delay][RetryInstruction.delayMillis] of instructions is at least
 * [minDelayMillis].
 *
 * @throws [IllegalArgumentException] if [minDelayMillis] is not positive.
 */
fun <E> RetryPolicy<E>.delayAtLeast(minDelayMillis: Long): RetryPolicy<E> {
    require(minDelayMillis > 0) { "minDelayMillis must be positive, but was $minDelayMillis" }

    return RetryPolicy { attempt ->
        val instruction = this(attempt)

        if (instruction == StopRetrying || instruction == ContinueRetrying) {
            instruction
        } else {
            val delay = instruction.delayMillis.coerceAtLeast(minDelayMillis)
            RetryAfter(delay)
        }
    }
}

/**
 * Creates a [RetryPolicy] ensuring the [delay][RetryInstruction.delayMillis] of instructions is at most
 * [maxDelayMillis].
 *
 * @throws IllegalArgumentException if [maxDelayMillis] is not positive.
 */
fun <E> RetryPolicy<E>.delayAtMost(maxDelayMillis: Long): RetryPolicy<E> {
    require(maxDelayMillis > 0) { "maxDelayMillis must be positive, but was $maxDelayMillis" }

    return RetryPolicy { attempt ->
        val instruction = this(attempt)

        if (instruction == StopRetrying || instruction == ContinueRetrying) {
            instruction
        } else {
            val delay = instruction.delayMillis.coerceAtMost(maxDelayMillis)
            RetryAfter(delay)
        }
    }
}

/**
 * Creates a [RetryPolicy] ensuring the [delay][RetryInstruction.delayMillis] of instructions lies in the specified
 * range [minDelayMillis]..[maxDelayMillis].
 *
 * @throws IllegalArgumentException if [minDelayMillis] or [maxDelayMillis] are not positive.
 */
fun <E> RetryPolicy<E>.delayIn(minDelayMillis: Long, maxDelayMillis: Long): RetryPolicy<E> {
    require(minDelayMillis > 0) { "minDelayMillis must be positive, but was $minDelayMillis" }
    require(maxDelayMillis > 0) { "maxDelayMillis must be positive, but was $maxDelayMillis" }

    return RetryPolicy { attempt ->
        val instruction = this(attempt)

        if (instruction == StopRetrying || instruction == ContinueRetrying) {
            instruction
        } else {
            val delay = instruction.delayMillis.coerceIn(minDelayMillis, maxDelayMillis)
            RetryAfter(delay)
        }
    }
}

/**
 * Creates a [RetryPolicy] ensuring the [delay][RetryInstruction.delayMillis] of instructions lies in the specified
 * [range].
 *
 * @throws IllegalArgumentException if the specified [range] is empty.
 */
fun <E> RetryPolicy<E>.delayIn(range: LongRange): RetryPolicy<E> {
    require(!range.isEmpty()) { "range must not be empty" }

    return RetryPolicy { attempt ->
        val instruction = this(attempt)

        if (instruction == StopRetrying || instruction == ContinueRetrying) {
            instruction
        } else {
            val delay = instruction.delayMillis.coerceIn(range)
            RetryAfter(delay)
        }
    }
}
