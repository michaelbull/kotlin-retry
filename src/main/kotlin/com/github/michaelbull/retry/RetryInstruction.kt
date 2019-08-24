package com.github.michaelbull.retry

import com.github.michaelbull.retry.policy.RetryPolicy

/**
 * Represents an instruction for the [retry] function to follow after
 * evaluating a [RetryPolicy].
 */
@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
inline class RetryInstruction @PublishedApi internal constructor(
    val delayMillis: Long
)

/**
 * Instructs the [retry] function to stop attempting retries.
 */
val StopRetrying = RetryInstruction(-1L)

/**
 * Instructs the [retry] function to retry the operation immediately.
 */
val RetryImmediately = RetryInstruction(0L)

/**
 * Instructs the [retry] function to retry the operation after [delayMillis].
 */
@Suppress("NOTHING_TO_INLINE", "FunctionName")
inline fun RetryAfter(delayMillis: Long): RetryInstruction {
    require(delayMillis > 0) { "delayMillis must be positive: $delayMillis" }
    return RetryInstruction(delayMillis)
}
