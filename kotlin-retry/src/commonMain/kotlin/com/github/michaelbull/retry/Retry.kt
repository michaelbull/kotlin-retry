package com.github.michaelbull.retry

import com.github.michaelbull.retry.attempt.Attempt
import com.github.michaelbull.retry.attempt.firstAttempt
import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.RetryInstruction
import com.github.michaelbull.retry.instruction.StopRetrying
import com.github.michaelbull.retry.policy.RetryPolicy
import kotlinx.coroutines.delay
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

/**
 * Calls the specified function [block] and returns its result if invocation was successful, catching any [Throwable]
 * exception that was thrown from the [block] function execution and retrying the invocation according to
 * [instructions][RetryInstruction] from the [policy].
 */
public suspend inline fun <T> retry(policy: RetryPolicy<Throwable>, block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    var attempt: Attempt? = null

    while (true) {
        try {
            return block()
        } catch (failure: Throwable) {
            /* avoid swallowing CancellationExceptions */
            if (failure is CancellationException) {
                throw failure
            } else {
                if (attempt == null) {
                    attempt = firstAttempt()
                }

                val failedAttempt = attempt.failedWith(failure)

                when (val instruction = policy(failedAttempt)) {
                    StopRetrying -> {
                        throw failure
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
}
