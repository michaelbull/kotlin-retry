package com.github.michaelbull.retry

import com.github.michaelbull.retry.policy.constantDelay
import com.github.michaelbull.retry.policy.continueIf
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.policy.stopAtAttempts
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class RetryTest {

    private data class AttemptsException(val attempts: Int) : Exception()

    @Test
    fun retryToAttemptLimit() = runTest {
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
    fun retryExhaustingAttemptLimit() = runTest {
        var attempts = 0

        val exception = assertFailsWith<AttemptsException> {
            retry(stopAtAttempts(10)) {
                attempts++

                if (attempts < 15) {
                    throw AttemptsException(attempts)
                }
            }
        }

        assertEquals(AttemptsException(10), exception)
    }

    @Test
    fun retryThrowsCancellationException() = runTest {
        assertFailsWith<CancellationException> {
            retry(stopAtAttempts(10)) {
                throw CancellationException()
            }
        }
    }

    @Test
    fun retryStopsAfterCancellation() = runTest {
        var attempts = 0

        assertFailsWith<CancellationException> {
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
    fun retryWithCustomPolicy() = runTest {
        val customPolicy = continueIf<Throwable> { (failure) ->
            failure is AttemptsException
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

    @Test
    fun cancelRetryFromJob() = runTest {
        val policy = constantDelay<Throwable>(100)
        var attempts = 0

        val job = backgroundScope.launch {
            retry(policy) {
                attempts++
                throw AttemptsException(attempts)
            }
        }

        testScheduler.advanceTimeBy(350)
        testScheduler.runCurrent()

        job.cancel()

        testScheduler.advanceUntilIdle()

        assertTrue(job.isCancelled)
        assertEquals(4, attempts)

        testScheduler.advanceTimeBy(2000)
        testScheduler.runCurrent()

        assertTrue(job.isCancelled)
        assertEquals(4, attempts)
    }

    @Test
    fun cancelRetryWithinJob() = runTest {
        val policy = constantDelay<Throwable>(20)
        var attempts = 0

        val job = launch {
            retry(policy) {
                attempts++

                if (attempts == 15) {
                    cancel()
                } else {
                    throw AttemptsException(attempts)
                }
            }
        }

        testScheduler.advanceUntilIdle()

        assertTrue(job.isCancelled)
        assertEquals(15, attempts)

        testScheduler.advanceTimeBy(2000)
        testScheduler.runCurrent()

        assertTrue(job.isCancelled)
        assertEquals(15, attempts)
    }

    @Test
    fun cancelRetryFromWithinChildJob() = runTest {
        val policy = constantDelay<Throwable>(20)
        var attempts = 0

        lateinit var childJobOne: Deferred<Int>
        lateinit var childJobTwo: Deferred<Int>

        val parentJob = launch {
            retry(policy) {
                childJobOne = async {
                    delay(100)
                    attempts
                }

                childJobTwo = async {
                    delay(50)

                    if (attempts == 15) {
                        cancel()
                    }

                    1
                }

                attempts = childJobOne.await() + childJobTwo.await()

                throw AttemptsException(attempts)
            }
        }

        testScheduler.advanceUntilIdle()

        assertTrue(parentJob.isCancelled)
        assertFalse(childJobOne.isCancelled)
        assertTrue(childJobTwo.isCancelled)
        assertEquals(15, attempts)

        testScheduler.advanceTimeBy(2000)
        testScheduler.runCurrent()

        assertTrue(parentJob.isCancelled)
        assertFalse(childJobOne.isCancelled)
        assertTrue(childJobTwo.isCancelled)
        assertEquals(15, attempts)
    }
}
