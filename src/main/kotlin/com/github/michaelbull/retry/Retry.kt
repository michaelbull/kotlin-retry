package com.github.michaelbull.retry

import com.github.michaelbull.retry.context.CoroutineRetryScope
import com.github.michaelbull.retry.context.RetryStatus
import com.github.michaelbull.retry.context.retryRandom
import com.github.michaelbull.retry.policy.RetryPolicy
import com.github.michaelbull.retry.policy.constantDelay
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.coroutineContext

/**
 * Runs the [block] of code, following [RetryInstruction]s returned when
 * evaluating the [policy].
 */
suspend inline fun <T> retry(
    noinline policy: RetryPolicy<Throwable> = constantDelay(50) + limitAttempts(5),
    block: RetryScope.() -> T
): T {
    contract {
        callsInPlace(policy, InvocationKind.UNKNOWN)
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    val status = RetryStatus()
    val scope = CoroutineRetryScope(status, coroutineContext.retryRandom)

    lateinit var mostRecentFailure: Throwable

    while (true) {
        try {
            return block(scope)
        } catch (t: Throwable) {
            /* avoid swallowing CancellationExceptions */
            if (t is CancellationException) {
                throw t
            }

            val failure = DelegatedRetryFailure(t, scope)
            val instruction = failure.policy()

            status.incrementAttempts()

            if (instruction == StopRetrying) {
                mostRecentFailure = t
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

    throw mostRecentFailure
}
