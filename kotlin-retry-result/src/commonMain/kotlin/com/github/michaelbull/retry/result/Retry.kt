package com.github.michaelbull.retry.result

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.asErr
import com.github.michaelbull.retry.attempt.Attempt
import com.github.michaelbull.retry.attempt.firstAttempt
import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.RetryInstruction
import com.github.michaelbull.retry.instruction.StopRetrying
import com.github.michaelbull.retry.policy.RetryPolicy
import kotlinx.coroutines.delay
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Calls the specified function [block] and returns its [Result], handling any [Err] returned from the [block] function
 * execution retrying the invocation according to [instructions][RetryInstruction] from the [policy].
 */
public suspend inline fun <V, E> retry(policy: RetryPolicy<E>, block: () -> Result<V, E>): Result<V, E> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    var attempt: Attempt? = null

    while (true) {
        val result = block()

        if (result.isOk) {
            return result
        } else {
            if (attempt == null) {
                attempt = firstAttempt()
            }

            val failedAttempt = attempt.failedWith(result.error)

            when (val instruction = policy(failedAttempt)) {
                StopRetrying -> {
                    return result.asErr()
                }

                ContinueRetrying -> {
                    attempt.retryImmediately()
                }

                else -> {
                    val (delayMillis) = instruction
                    delay(delayMillis)
                    attempt.retryAfter(delayMillis)
                }
            }
        }
    }
}
