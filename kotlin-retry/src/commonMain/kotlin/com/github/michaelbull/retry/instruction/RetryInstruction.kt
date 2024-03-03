package com.github.michaelbull.retry.instruction

import com.github.michaelbull.retry.policy.RetryPolicy
import com.github.michaelbull.retry.retry
import kotlin.jvm.JvmInline

/**
 * Represents an instruction for the [retry] function to follow after
 * evaluating a [RetryPolicy].
 */
@JvmInline
public value class RetryInstruction @PublishedApi internal constructor(
    public val delayMillis: Long,
) {
    public operator fun component1(): Long = delayMillis
}
