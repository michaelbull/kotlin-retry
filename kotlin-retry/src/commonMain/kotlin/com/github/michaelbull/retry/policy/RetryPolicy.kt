package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.attempt.FailedAttempt
import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.RetryAfter
import com.github.michaelbull.retry.instruction.RetryInstruction
import com.github.michaelbull.retry.instruction.StopRetrying
import kotlin.math.max

public fun interface RetryPolicy<E> {
    public operator fun invoke(attempt: FailedAttempt<E>): RetryInstruction
}

/**
 * Merges the [previous](prev) and [current](curr) [RetryPolicy] into a single [RetryPolicy].
 *
 * If either policy returns an [instruction][RetryInstruction] to [StopRetrying], the combined [policy][RetryPolicy]
 * will return an [instruction][RetryInstruction] to [StopRetrying].
 *
 * If both [policies][RetryPolicy] return an [instruction][RetryInstruction] to [RetryAfter] a given delay, the combined
 * [policy][RetryPolicy] will return an [instruction][RetryInstruction] to [RetryAfter] the larger delay.
 */
public fun <E> RetryPolicy(prev: RetryPolicy<E>, curr: RetryPolicy<E>): RetryPolicy<E> {
    return RetryPolicy { attempt ->
        val prevInstruction = prev(attempt)
        val currInstruction = curr(attempt)

        val eitherStopping = prevInstruction == StopRetrying || currInstruction == StopRetrying
        val bothContinuing = prevInstruction == ContinueRetrying && currInstruction == ContinueRetrying

        when {
            eitherStopping -> StopRetrying
            bothContinuing -> ContinueRetrying
            else -> {
                val prevDelay = prevInstruction.delayMillis;
                val currDelay = currInstruction.delayMillis;
                val greatestDelay = max(prevDelay, currDelay)
                RetryAfter(greatestDelay)
            }
        }
    }
}

public fun <E> RetryPolicy(first: RetryPolicy<E>, second: RetryPolicy<E>, vararg rest: RetryPolicy<E>): RetryPolicy<E> {
    return listOf(first, second, *rest).reduce(::RetryPolicy)
}

public operator fun <E> RetryPolicy<E>.plus(other: RetryPolicy<E>): RetryPolicy<E> {
    return RetryPolicy(this, other)
}
