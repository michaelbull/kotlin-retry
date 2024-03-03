package com.github.michaelbull.retry.instruction

import com.github.michaelbull.retry.retry

/**
 * Instructs the [retry] function to retry the operation after [delayMillis].
 */
@Suppress("NOTHING_TO_INLINE", "FunctionName")
inline fun RetryAfter(delayMillis: Long): RetryInstruction {
    require(delayMillis > 0) { "delayMillis must be positive, but was: $delayMillis" }
    return RetryInstruction(delayMillis)
}
