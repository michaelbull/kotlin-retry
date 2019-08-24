package com.github.michaelbull.retry

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.retry.context.RetryStatus
import com.github.michaelbull.retry.context.retryStatus
import com.github.michaelbull.retry.policy.RetryPolicy
import com.github.michaelbull.retry.policy.constantDelay
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private typealias ResultProducer<V, E> = suspend () -> Result<V, E>

/**
 * Runs [this] block of code, following [RetryInstruction]s returned when
 * evaluating the [policy].
 */
suspend fun <V, E> ResultProducer<V, E>.retryResult(
    policy: RetryPolicy<E> = constantDelay(50) + limitAttempts(5)
) = retryResult(policy, this)

/**
 * Runs the [block] of code, following [RetryInstruction]s returned when
 * evaluating the [policy].
 */
suspend fun <V, E> retryResult(
    policy: RetryPolicy<E> = constantDelay(50) + limitAttempts(5),
    block: ResultProducer<V, E>
): Result<V, E> {
    return withContext(RetryStatus()) {
        lateinit var mostRecentFailure: Err<E>

        while (true) {
            val result = block()

            if (result is Ok) {
                return@withContext result
            } else if (result is Err) {
                val instruction = RetryFailure(result.error).policy()

                val status = coroutineContext.retryStatus
                status.incrementAttempts()

                if (instruction == StopRetrying) {
                    mostRecentFailure = result
                    break
                } else if (instruction == RetryImmediately) {
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

        mostRecentFailure
    }
}
