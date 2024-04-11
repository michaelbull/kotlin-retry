package com.github.michaelbull.retry.result

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.retry.instruction.RetryInstruction
import com.github.michaelbull.retry.policy.RetryPolicy
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Calls the specified function [block] and returns its [Result], handling any [Err] returned from the [block] function
 * execution retrying the invocation according to [instructions][RetryInstruction] from the [policy].
 */
public suspend inline fun <V, E> runRetrying(policy: RetryPolicy<E>, block: () -> Result<V, E>): Result<V, E> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    return retry(policy, block)
}
