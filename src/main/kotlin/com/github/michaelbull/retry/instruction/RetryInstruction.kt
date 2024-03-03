package com.github.michaelbull.retry.instruction

import com.github.michaelbull.retry.policy.RetryPolicy
import com.github.michaelbull.retry.retry

/**
 * Represents an instruction for the [retry] function to follow after
 * evaluating a [RetryPolicy].
 */
@JvmInline
value class RetryInstruction @PublishedApi internal constructor(
    val delayMillis: Long,
) {
    operator fun component1() = delayMillis
}
