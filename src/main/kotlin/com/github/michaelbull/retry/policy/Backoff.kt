package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.ContinueRetrying
import com.github.michaelbull.retry.RetryAfter
import com.github.michaelbull.retry.RetryInstruction
import com.github.michaelbull.retry.binaryExponential
import com.github.michaelbull.retry.saturatedAdd
import com.github.michaelbull.retry.saturatedMultiply
import kotlin.math.max
import kotlin.math.min

/* https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/ */

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to
 * [RetryAfter] an amount of milliseconds between [base] and [max] inclusive,
 * increasing the delay by 2 to the power of the number of attempts made.
 */
fun binaryExponentialBackoff(base: Long, max: Long): RetryPolicy<*> {
    require(base > 0) { "base must be positive: $base" }
    require(max > 0) { "max must be positive: $max" }

    return {
        /* sleep = min(cap, base * 2 ** attempt) */
        val delay = min(max, base saturatedMultiply attempt.binaryExponential())

        RetryAfter(delay)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun binaryExponentialBackoff(range: LongRange): RetryPolicy<*> {
    return binaryExponentialBackoff(range.first, range.last)
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to
 * [RetryAfter] a random amount of milliseconds between 0 and [max] inclusive,
 * increasing the delay by 2 to the power of number of attempts made.
 */
fun fullJitterBackoff(base: Long, max: Long): RetryPolicy<*> {
    require(base > 0) { "base must be positive: $base" }
    require(max > 0) { "max must be positive: $max" }

    return {
        /* sleep = random_between(0, min(cap, base * 2 ** attempt)) */
        val delay = min(max, base saturatedMultiply attempt.binaryExponential())
        val randomDelay = random.nextLong(delay saturatedAdd 1)
        if (randomDelay == 0L) ContinueRetrying else RetryAfter(randomDelay)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun fullJitterBackoff(range: LongRange): RetryPolicy<*> {
    return fullJitterBackoff(range.first, range.last)
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to
 * [RetryAfter] an amount of milliseconds equally portioned between the
 * [binaryExponentialBackoff] and [fullJitterBackoff].
 */
fun equalJitterBackoff(base: Long, max: Long): RetryPolicy<*> {
    require(base > 0) { "base must be positive: $base" }
    require(max > 0) { "max must be positive: $max" }

    return {
        /* temp = min(cap, base * 2 ** attempt) */
        val delay = min(max, base saturatedMultiply attempt.binaryExponential())

        /* sleep = temp / 2 + random_between(0, temp / 2) */
        val randomDelay = (delay / 2) saturatedAdd random.nextLong((delay / 2) saturatedAdd 1)

        RetryAfter(randomDelay)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun equalJitterBackoff(range: LongRange): RetryPolicy<*> {
    return equalJitterBackoff(range.first, range.last)
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to
 * [RetryAfter] a random amount of milliseconds between [base] and [max]
 * inclusive, increasing by 3 times the previous delay.
 */
fun decorrelatedJitterBackoff(base: Long, max: Long): RetryPolicy<*> {
    require(base > 0) { "base must be positive: $base" }
    require(max > 0) { "max must be positive: $max" }

    return {
        /* sleep = min(cap, random_between(base, sleep * 3)) */
        val delay = max(base, previousDelay saturatedMultiply 3)
        val randomDelay = min(max, random.nextLong(base, delay saturatedAdd 1))

        RetryAfter(randomDelay)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun decorrelatedJitterBackoff(range: LongRange): RetryPolicy<*> {
    return decorrelatedJitterBackoff(range.first, range.last)
}
