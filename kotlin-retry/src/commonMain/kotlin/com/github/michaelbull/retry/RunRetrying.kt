package com.github.michaelbull.retry

import com.github.michaelbull.retry.instruction.RetryInstruction
import com.github.michaelbull.retry.policy.RetryPolicy
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Calls the specified function [block] and returns its result if invocation was successful, catching any [Throwable]
 * exception that was thrown from the [block] function execution and retrying the invocation according to
 * [instructions][RetryInstruction] from the [policy].
 */
public suspend inline fun <T> runRetrying(policy: RetryPolicy<Throwable>, block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    return retry(policy, block)
}
