package com.github.michaelbull.retry

import com.github.michaelbull.retry.context.RetryStatus
import com.github.michaelbull.retry.context.retryStatus
import com.github.michaelbull.retry.policy.RetryPolicy
import com.github.michaelbull.retry.policy.constantDelay
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private typealias Producer<T> = suspend () -> T

private val DEFAULT_POLICY: RetryPolicy<Throwable> = constantDelay(50) + limitAttempts(5)

/**
 * Runs [this] block of code, following [RetryInstruction]s returned when
 * evaluating the [policy].
 */
suspend fun <T> Producer<T>.retry(
    policy: RetryPolicy<Throwable> = DEFAULT_POLICY
): T {
    contract {
        callsInPlace(policy, InvocationKind.UNKNOWN)
        callsInPlace(this@retry, InvocationKind.AT_LEAST_ONCE)
    }

    return retry(policy, this)
}

/**
 * Runs the [block] of code, following [RetryInstruction]s returned when
 * evaluating the [policy].
 */
suspend fun <T> retry(
    policy: RetryPolicy<Throwable> = DEFAULT_POLICY,
    block: Producer<T>
): T {
    contract {
        callsInPlace(policy, InvocationKind.UNKNOWN)
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    return withContext(RetryStatus()) {
        lateinit var mostRecentFailure: Throwable

        while (true) {
            try {
                return@withContext block()
            } catch (ex: CancellationException) {
                /* avoid swallowing CancellationExceptions */
                throw ex
            } catch (t: Throwable) {
                val instruction = RetryFailure(t).policy()

                val status = coroutineContext.retryStatus
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
}
