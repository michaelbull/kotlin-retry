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
import org.junit.jupiter.api.assertThrows

@ExperimentalCoroutinesApi
class RetryTest {

    private data class AttemptsException(val attempts: Int) : Exception()

    @Test
    fun `retry continues until succeeding`() = runBlockingTest {
        var attempts = 0

        retry(limitAttempts(5)) {
            attempts++

            if (attempts < 5) {
                throw AttemptsException(attempts)
            }
        }

        assertEquals(5, attempts)
    }

    @Test
    fun `retry continues up to specified number of attempts`() {
        var attempts = 0

        assertThrows<AttemptsException> {
            runBlockingTest {
                retry(limitAttempts(10)) {
                    attempts++

                    if (attempts < 15) {
                        throw AttemptsException(attempts)
                    }
                }
            }
        }

        assertEquals(10, attempts)
    }

    @Test
    fun `retry throws most recent exception if attempts exhausted`() = runBlockingTest {
        var attempts = 0
        lateinit var mostRecentException: Exception

        try {
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
    fun `retry does not swallow CancellationExceptions`() {
        assertThrows<CancellationException> {
            runBlockingTest {
                retry {
                    throw CancellationException()
                }
            }
        }
    }

    @Test
    fun `retry does not continue after throwing CancellationException`() {
        var attempts = 0

        assertThrows<CancellationException> {
            runBlockingTest {
                retry(limitAttempts(5)) {
                    attempts++

                    if (attempts == 2) {
                        throw CancellationException()
                    } else {
                        throw Exception()
                    }
                }
            }
        }

        assertEquals(2, attempts)
    }

    @Test
    fun `retry delays between attempts for duration calculated from backoff strategy`() {
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
    fun `retry adheres to custom policy`() = runBlockingTest {
        val policy: RetryPolicy<Throwable> = {
            if (reason is AttemptsException) ContinueRetrying else StopRetrying
        }

        var attempts = 0
        lateinit var mostRecentException: Exception

        try {
            retry(policy + limitAttempts(15)) {
                attempts++
                throw AttemptsException(attempts)
            }
        } catch (ex: AttemptsException) {
            mostRecentException = ex
        }

        assertEquals(AttemptsException(15), mostRecentException)
    }

    @Test
    fun `retry has correct coroutine context`() = runBlockingTest {
        retry {
            // This crashes because coroutineContext is obtained from the withTimeout scope
            println("My retryStatus is ${attempt}")

//                // This is okay (naming conflict with coroutineContext)
//                println("My retryStatus is ${kotlin.coroutines.coroutineContext.retryStatus}")
//
//                // This is also okay
//                coroutineScope {
//                    println("My retryStatus is ${coroutineContext.retryStatus}")
//                }
//
//                Ok(Unit)
//            }
        }

    }
}
