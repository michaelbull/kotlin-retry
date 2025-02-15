package com.github.michaelbull.retry.result

import arrow.core.Either
import com.github.michaelbull.retry.instruction.RetryInstruction
import com.github.michaelbull.retry.policy.RetryPolicy
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Calls the specified function [block] and returns its [Either], handling any [Either.Left] returned from the [block] function
 * execution retrying the invocation according to [instructions][RetryInstruction] from the [policy].
 */
public suspend inline fun <A, B> runRetrying(policy: RetryPolicy<A>, block: () -> Either<A, B>): Either<A, B> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    return retry(policy, block)
}
