package com.github.michaelbull.retry

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.retry.context.CoroutineRetryScope
import com.github.michaelbull.retry.context.RetryStatus
import com.github.michaelbull.retry.context.retryRandom
import com.github.michaelbull.retry.policy.RetryPolicy
import com.github.michaelbull.retry.policy.constantDelay
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import kotlinx.coroutines.delay
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.coroutineContext

/**
 * Runs the [block] of code, following [RetryInstruction]s returned when
 * evaluating the [policy].
 */
suspend inline fun <V, E> retryResult(
    noinline policy: RetryPolicy<E> = constantDelay(50) + limitAttempts(5),
    block: RetryScope.() -> Result<V, E>
): Result<V, E> {
    contract {
        callsInPlace(policy, InvocationKind.UNKNOWN)
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    val status = RetryStatus()
    val scope = CoroutineRetryScope(status, coroutineContext.retryRandom)

    lateinit var mostRecentFailure: Err<E>

    while (true) {
        val result = block(scope)

        if (result is Ok) {
            return result
        } else if (result is Err) {
            val failure = DelegatedRetryFailure(result.error, scope)
            val instruction = failure.policy()

            status.incrementAttempts()

            if (instruction == StopRetrying) {
                mostRecentFailure = result
                break
            } else if (instruction == ContinueRetrying) {
                status.previousDelay = 0
                continue
            } else {
                val delay = instruction.delayMillis
                delay(delay)
                status.previousDelay = delay
                status.incrementCumulativeDelay(delay)
            }
        }
    }

    return mostRecentFailure
}
