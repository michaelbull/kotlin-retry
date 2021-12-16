package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.ContinueRetrying
import com.github.michaelbull.retry.RetryAfter
import com.github.michaelbull.retry.RetryFailure
import com.github.michaelbull.retry.RetryInstruction
import com.github.michaelbull.retry.StopRetrying
import com.github.michaelbull.retry.context.RetryStatus
import kotlin.math.max

/**
 * A [RetryPolicy] evaluates a [RetryFailure] in context of the current
 * [RetryStatus] and returns a [RetryInstruction].
 */
typealias RetryPolicy<E> = suspend RetryFailure<E>.() -> RetryInstruction

/**
 * Creates a combined [RetryPolicy] of [this] and the [other].
 *
 * If either policy returns an [instruction][RetryInstruction] to
 * [StopRetrying], the combined [policy][RetryPolicy] will return an
 * [instruction][RetryInstruction] to [StopRetrying].
 *
 * If both [policies][RetryPolicy] return an [instruction][RetryInstruction] to
 * [RetryAfter] a given delay, the combined [policy][RetryPolicy] will return
 * an [instruction][RetryInstruction] to [RetryAfter] the larger delay.
 */
operator fun <E> RetryPolicy<E>.plus(other: RetryPolicy<E>): RetryPolicy<E> = {
    val a = invoke(this)
    val b = other.invoke(this)

    when {
        a == StopRetrying || b == StopRetrying -> StopRetrying
        a == ContinueRetrying && b == ContinueRetrying -> ContinueRetrying
        else -> RetryAfter(max(a.delayMillis, b.delayMillis))
    }
}
