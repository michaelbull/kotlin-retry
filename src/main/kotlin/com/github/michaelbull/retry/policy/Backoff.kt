package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.binaryExponential
import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.RetryAfter
import com.github.michaelbull.retry.instruction.RetryInstruction
import com.github.michaelbull.retry.saturatedAdd
import com.github.michaelbull.retry.saturatedMultiply
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/* https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/ */

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to
 * [RetryAfter] an amount of milliseconds between [min] and [max] inclusive,
 * increasing the delay by 2 to the power of the number of attempts made.
 *
 * @throws IllegalArgumentException if [min] or [max] are not positive.
 */
fun <E> binaryExponentialBackoff(
    min: Long,
    max: Long,
): RetryPolicy<E> {
    require(min > 0) { "min must be positive, but was $min" }
    require(max > 0) { "max must be positive, but was $max" }

    return RetryPolicy { attempt ->
        val delay = min(max, min saturatedMultiply attempt.number.binaryExponential())
        RetryAfter(delay)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun <E> binaryExponentialBackoff(range: LongRange): RetryPolicy<E> {
    return binaryExponentialBackoff(range.first, range.last)
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to
 * [RetryAfter] a random amount of milliseconds between 0 and [max] inclusive,
 * increasing the delay by 2 to the power of number of attempts made.
 *
 * @throws IllegalArgumentException if [min] or [max] are not positive.
 */
fun <E> fullJitterBackoff(
    min: Long,
    max: Long,
    random: Random = Random,
): RetryPolicy<E> {
    require(min > 0) { "min must be positive, but was $min" }
    require(max > 0) { "max must be positive, but was $max" }

    return RetryPolicy { attempt ->
        val jitter = min(max, min saturatedMultiply attempt.number.binaryExponential())
        val randomJitter = random.nextLong(jitter saturatedAdd 1)

        if (randomJitter == 0L) {
            ContinueRetrying
        } else {
            RetryAfter(randomJitter)
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun <E> fullJitterBackoff(range: LongRange, random: Random = Random): RetryPolicy<E> {
    return fullJitterBackoff(range.first, range.last, random)
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to
 * [RetryAfter] an amount of milliseconds equally portioned between the
 * [binaryExponentialBackoff] and [fullJitterBackoff].
 *
 * @throws IllegalArgumentException if [min] or [max] are not positive.
 */
fun <E> equalJitterBackoff(
    min: Long,
    max: Long,
    random: Random = Random,
): RetryPolicy<E> {
    require(min > 0) { "min must be positive, but was $min" }
    require(max > 0) { "max must be positive, but was $max" }

    return RetryPolicy { attempt ->
        val jitter = min(max, min saturatedMultiply attempt.number.binaryExponential())
        val randomJitter = random.nextLong((jitter / 2) saturatedAdd 1)
        val delayWithJitter = (jitter / 2) saturatedAdd randomJitter

        RetryAfter(delayWithJitter)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun <E> equalJitterBackoff(range: LongRange, random: Random = Random): RetryPolicy<E> {
    return equalJitterBackoff(range.first, range.last, random)
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to
 * [RetryAfter] a random amount of milliseconds between [min] and [max]
 * inclusive, increasing by [correlation] times the previous delay.
 *
 * @throws IllegalArgumentException if [min] or [max] are not positive.
 */
fun <E> decorrelatedJitterBackoff(
    min: Long,
    max: Long,
    correlation: Long = 3,
    random: Random = Random,
): RetryPolicy<E> {
    require(min > 0) { "min must be positive, but was $min" }
    require(max > 0) { "max must be positive, but was $max" }

    return RetryPolicy { attempt ->
        val jitter = max(min, attempt.previousDelay saturatedMultiply correlation)
        val randomJitter = random.nextLong(min, jitter saturatedAdd 1)
        val delayWithJitter = min(max, randomJitter)

        RetryAfter(delayWithJitter)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun <E> decorrelatedJitterBackoff(
    range: LongRange,
    correlation: Long = 3,
    random: Random = Random,
): RetryPolicy<E> {
    return decorrelatedJitterBackoff(range.first, range.last, correlation, random)
}

