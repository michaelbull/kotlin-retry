package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.attempt.FailedAttempt
import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.RetryInstruction
import com.github.michaelbull.retry.instruction.StopRetrying

public fun interface RetryPredicate<E> {
    public operator fun invoke(attempt: FailedAttempt<E>): Boolean
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to [ContinueRetrying] if the [FailedAttempt]
 * satisfies the given [predicate], or an [instruction][RetryInstruction] to [StopRetrying] if it doesn't.
 */
public fun <E> continueIf(predicate: RetryPredicate<E>): RetryPolicy<E> {
    return RetryPolicy { attempt ->
        if (predicate(attempt)) {
            ContinueRetrying
        } else {
            StopRetrying
        }
    }
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to [ContinueRetrying] if the [FailedAttempt]
 * _does not_ satisfy the given [predicate], or an [instruction][RetryInstruction] to [StopRetrying] if it does.
 */
public fun <E> continueUnless(predicate: RetryPredicate<E>): RetryPolicy<E> {
    return RetryPolicy { attempt ->
        if (!predicate(attempt)) {
            ContinueRetrying
        } else {
            StopRetrying
        }
    }
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to [StopRetrying] if the [FailedAttempt]
 * satisfies the given [predicate], or an [instruction][RetryInstruction] to [ContinueRetrying] if it doesn't.
 */
public fun <E> stopIf(predicate: RetryPredicate<E>): RetryPolicy<E> {
    return RetryPolicy { attempt ->
        if (predicate(attempt)) {
            StopRetrying
        } else {
            ContinueRetrying
        }
    }
}

/**
 * Creates a [RetryPolicy] that returns an [instruction][RetryInstruction] to [StopRetrying] if the [FailedAttempt]
 * _does not_ satisfy the given [predicate], or an [instruction][RetryInstruction] to [ContinueRetrying] if it does.
 */
public fun <E> stopUnless(predicate: RetryPredicate<E>): RetryPolicy<E> {
    return RetryPolicy { attempt ->
        if (!predicate(attempt)) {
            StopRetrying
        } else {
            ContinueRetrying
        }
    }
}
