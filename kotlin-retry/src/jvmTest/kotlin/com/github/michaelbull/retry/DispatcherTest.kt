package com.github.michaelbull.retry

import com.github.michaelbull.retry.instruction.RetryAfter
import com.github.michaelbull.retry.policy.RetryPolicy
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.policy.stopAtRetries
import io.mockk.coVerifyOrder
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.fail

@ExperimentalCoroutinesApi
class DispatcherTest {

    private data class AttemptsException(val attempts: Int) : Exception()

    @Test
    fun retryDelaysCoroutineDispatcher() {
        val dispatcher = spyk(StandardTestDispatcher())

        var attempts = 0

        val backoff = RetryPolicy<Throwable> { attempt ->
            when (attempt.number) {
                0 -> RetryAfter(1000L)
                1 -> RetryAfter(2000L)
                2 -> RetryAfter(3000L)
                3 -> RetryAfter(4000L)
                else -> fail()
            }
        }

        val policy = backoff + stopAtRetries(4)

        runTest(dispatcher) {
            retry(policy) {
                attempts++

                if (attempts <= 4) {
                    throw AttemptsException(attempts)
                }
            }
        }

        coVerifyOrder {
            dispatcher.scheduleResumeAfterDelay(1000, any())
            dispatcher.scheduleResumeAfterDelay(2000, any())
            dispatcher.scheduleResumeAfterDelay(3000, any())
            dispatcher.scheduleResumeAfterDelay(4000, any())
        }
    }
}
