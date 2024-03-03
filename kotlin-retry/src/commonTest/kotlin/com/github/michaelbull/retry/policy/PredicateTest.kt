package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.attempt.FailedAttempt
import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.StopRetrying
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class PredicateTest {

    @Test
    fun continueIfExpectedException() {
        val policy = continueIf<Throwable> { (failure) ->
            failure is IllegalStateException
        }

        val attempt = FailedAttempt<Throwable>(
            failure = IllegalStateException(),
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(ContinueRetrying, instruction)
    }

    @Test
    fun continueIfUnexpectedException() {
        val policy = continueIf<Throwable> { (failure) ->
            failure is IllegalStateException
        }

        val attempt = FailedAttempt<Throwable>(
            failure = UnsupportedOperationException(),
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(StopRetrying, instruction)
    }

    @Test
    fun continueUnlessExpectedException() {
        val policy = continueUnless<Throwable> { (failure) ->
            failure is IllegalStateException
        }

        val attempt = FailedAttempt<Throwable>(
            failure = IllegalStateException(),
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(StopRetrying, instruction)
    }

    @Test
    fun continueUnlessUnexpectedException() {
        val policy = continueUnless<Throwable> { (failure) ->
            failure is IllegalStateException
        }

        val attempt = FailedAttempt<Throwable>(
            failure = UnsupportedOperationException(),
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(ContinueRetrying, instruction)
    }

    @Test
    fun stopIfExpectedException() {
        val policy = stopIf<Throwable> { (failure) ->
            failure is IllegalStateException
        }

        val attempt = FailedAttempt<Throwable>(
            failure = IllegalStateException(),
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(StopRetrying, instruction)
    }

    @Test
    fun stopIfUnexpectedException() {
        val policy = stopIf<Throwable> { (failure) ->
            failure is IllegalStateException
        }

        val attempt = FailedAttempt<Throwable>(
            failure = UnsupportedOperationException(),
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(ContinueRetrying, instruction)
    }

    @Test
    fun stopUnlessUnexpectedException() {
        val policy = stopUnless<Throwable> { (failure) ->
            failure is IllegalStateException
        }

        val attempt = FailedAttempt<Throwable>(
            failure = UnsupportedOperationException(),
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(StopRetrying, instruction)
    }

    @Test
    fun stopUnlessExpectedException() {
        val policy = stopUnless<Throwable> { (failure) ->
            failure is IllegalStateException
        }

        val attempt = FailedAttempt<Throwable>(
            failure = IllegalStateException(),
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(ContinueRetrying, instruction)
    }
}
