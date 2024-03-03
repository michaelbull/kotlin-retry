package com.github.michaelbull.retry.instruction

import com.github.michaelbull.retry.retry

/**
 * Instructs the [retry] function to retry the operation after [delayMillis].
 *
 * @throws IllegalArgumentException if [delayMillis] is not positive.
 */
@Suppress("FunctionName")
public fun RetryAfter(delayMillis: Long): RetryInstruction {
    require(delayMillis > 0) { "delayMillis must be positive, but was: $delayMillis" }
    return RetryInstruction(delayMillis)
}
