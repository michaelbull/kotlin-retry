package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.ContinueRetrying
import com.github.michaelbull.retry.RetryFailure
import com.github.michaelbull.retry.RetryInstruction
import com.github.michaelbull.retry.StopRetrying

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to
 * [ContinueRetrying] if the [RetryFailure] satisfies the given [predicate],
 * or an [instruction][RetryInstruction] to [StopRetrying] if it doesn't.
 */
inline fun <E> retryIf(crossinline predicate: RetryFailure<E>.() -> Boolean): RetryPolicy<E> = {
    if (predicate(this)) {
        ContinueRetrying
    } else {
        StopRetrying
    }
}
