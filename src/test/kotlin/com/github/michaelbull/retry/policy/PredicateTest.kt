package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.attempt.FailedAttempt
import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.StopRetrying
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class PredicateTest {

    @Test
    fun `continueIf should continue on specified exception`() {
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
    fun `continueIf should not continue on unspecified exception`() {
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
    fun `continueUnless should not continue on specified exception`() {
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
    fun `continueUnless should continue on unspecified exception`() {
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
    fun `stopIf should stop on specified exception`() {
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
    fun `stopIf should not stop on specified exception`() {
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
    fun `stopUnless should stop on unspecified exception`() {
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
    fun `stopUnless should not stop on specified exception`() {
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
