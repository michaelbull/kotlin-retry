package com.github.michaelbull.retry

import com.github.michaelbull.retry.policy.RetryPolicy
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import io.mockk.coVerifyOrder
import io.mockk.spyk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class RetryTest {

    private data class AttemptsException(val attempts: Int) : Exception()

    @Test
    fun `retry should attempt until succeeding`() = runBlockingTest {
        var attempts = 0

        val result = retry(limitAttempts(5)) {
            attempts++

            if (attempts < 5) {
                throw AttemptsException(attempts)
            } else {
                attempts
            }
        }

        assertEquals(5, result)
    }

    @Test
    fun `retry should attempt for specified number of attempts`() = runBlockingTest {
        var attempts = 0

        try {
            retry(limitAttempts(10)) {
                attempts++

                if (attempts < 15) {
                    throw AttemptsException(attempts)
                } else {
                    attempts
                }
            }
        } catch (ignored: Exception) {
            /* empty */
        }

        assertEquals(10, attempts)
    }

    @Test
    fun `retry should throw most recent exception if attempts exhausted`() = runBlockingTest {
        var attempts = 0
        lateinit var mostRecentException: Exception

        try {
            @Suppress("IMPLICIT_NOTHING_AS_TYPE_PARAMETER")
            retry(limitAttempts(5)) {
                attempts++
                throw AttemptsException(attempts)
            }
        } catch (ex: AttemptsException) {
            mostRecentException = ex
        }

        assertEquals(AttemptsException(5), mostRecentException)
    }

    @Test
    fun `retry should not attempt again after throwing CancellationException`() {
        var attempts = 0

        try {
            runBlockingTest {
                @Suppress("IMPLICIT_NOTHING_AS_TYPE_PARAMETER")
                retry(limitAttempts(5)) {
                    attempts++

                    if (attempts == 2) {
                        throw CancellationException()
                    } else {
                        throw Exception()
                    }
                }
            }
        } catch (ignored: CancellationException) {
            /* empty */
        }

        assertEquals(2, attempts)
    }

    @Test
    fun `retry should delay between retries for duration calculated from backoff strategy`() {
        val dispatcher = spyk(TestCoroutineDispatcher())

        var attempts = 0

        val backoff: RetryPolicy<*> = {
            when (attempts) {
                1 -> 1000L
                2 -> 2000L
                3 -> 3000L
                4 -> 4000L
                else -> fail()
            }.let(::RetryAfter)
        }

        runBlockingTest(dispatcher) {
            retry(limitAttempts(5) + backoff) {
                attempts++

                if (attempts < 5) {
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
    fun `retry should adhere to custom policy`() = runBlockingTest {
        val policy: RetryPolicy<Throwable> = {
            if (reason is AttemptsException) ContinueRetrying else StopRetrying
        }

        var attempts = 0
        lateinit var mostRecentException: Exception

        try {
            @Suppress("IMPLICIT_NOTHING_AS_TYPE_PARAMETER")
            retry(policy + limitAttempts(15)) {
                attempts++
                throw AttemptsException(attempts)
            }
        } catch (ex: AttemptsException) {
            mostRecentException = ex
        }

        assertEquals(AttemptsException(15), mostRecentException)
    }
}
