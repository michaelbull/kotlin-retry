package com.github.michaelbull.retry

import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.RetryAfter
import com.github.michaelbull.retry.instruction.StopRetrying
import com.github.michaelbull.retry.policy.RetryPolicy
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.policy.stopAtAttempts
import com.github.michaelbull.retry.policy.stopAtRetries
import io.mockk.coVerifyOrder
import io.mockk.spyk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalCoroutinesApi
class RetryTest {

    private data class AttemptsException(val attempts: Int) : Exception()

    @Test
    fun `retry continues until succeeding`() = runTest {
        var attempts = 0

        retry(stopAtAttempts(5)) {
            attempts++

            if (attempts < 5) {
                throw AttemptsException(attempts)
            }
        }

        assertEquals(5, attempts)
    }

    @Test
    fun `retry continues up to specified number of attempts`() = runTest {
        var attempts = 0

        assertThrows<AttemptsException> {
            retry(stopAtAttempts(10)) {
                attempts++

                if (attempts < 15) {
                    throw AttemptsException(attempts)
                }
            }
        }

        assertEquals(10, attempts)
    }

    @Test
    fun `retry throws most recent exception if attempts exhausted`() = runTest {
        var attempts = 0
        lateinit var mostRecentException: Exception

        try {
            retry(stopAtAttempts(5)) {
                attempts++
                throw AttemptsException(attempts)
            }
        } catch (ex: AttemptsException) {
            mostRecentException = ex
        }

        assertEquals(AttemptsException(5), mostRecentException)
    }

    @Test
    fun `retry does not swallow CancellationExceptions`() = runTest {
        assertThrows<CancellationException> {
            retry(stopAtAttempts(10)) {
                throw CancellationException()
            }
        }
    }

    @Test
    fun `retry does not continue after throwing CancellationException`() = runTest {
        var attempts = 0

        assertThrows<CancellationException> {
            retry(stopAtAttempts(5)) {
                attempts++

                if (attempts == 2) {
                    throw CancellationException()
                } else {
                    throw Exception()
                }
            }
        }

        assertEquals(2, attempts)
    }

    @Test
    fun `retry delays between attempts for duration calculated from backoff strategy`() {
        val dispatcher = spyk(StandardTestDispatcher())

        var attempts = 0

        val backoff = RetryPolicy<Throwable> { attempt ->
            when (attempt.number) {
                0 -> 1000L
                1 -> 2000L
                2 -> 3000L
                3 -> 4000L
                else -> fail()
            }.let(::RetryAfter)
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

    @Test
    fun `retry adheres to custom policy`() = runTest {
        val customPolicy = RetryPolicy<Throwable> { (failure) ->
            if (failure is AttemptsException) ContinueRetrying else StopRetrying
        }

        val policy = customPolicy + stopAtAttempts(15)

        var attempts = 0
        lateinit var mostRecentException: Exception

        try {
            retry(policy) {
                attempts++
                throw AttemptsException(attempts)
            }
        } catch (ex: AttemptsException) {
            mostRecentException = ex
        }

        assertEquals(AttemptsException(15), mostRecentException)
    }
}
