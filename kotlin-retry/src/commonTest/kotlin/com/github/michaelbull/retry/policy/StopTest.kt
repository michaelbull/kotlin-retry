package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.attempt.FailedAttempt
import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.RetryAfter
import com.github.michaelbull.retry.instruction.StopRetrying
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class StopTest {

    @Test
    fun stopAtAttemptsContinuesUnderMaximum() {
        val policy = stopAtAttempts<Unit>(5)

        val attempt = FailedAttempt(
            failure = Unit,
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0
        )

        val instruction = policy(attempt)

        assertEquals(ContinueRetrying, instruction)
    }

    @Test
    fun stopAtAttemptsStopsAtMaximum() {
        val policy = stopAtAttempts<Unit>(2)

        val attempt = FailedAttempt(
            failure = Unit,
            number = 2,
            previousDelay = 100,
            cumulativeDelay = 300
        )

        val instruction = policy(attempt)

        assertEquals(StopRetrying, instruction)
    }

    @Test
    fun stopAtDelayContinuesUnderMaximum() {
        val policy = constantDelay<Unit>(20).stopAtDelay(50)

        val attempt = FailedAttempt(
            failure = Unit,
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(RetryAfter(20), instruction)
    }

    @Test
    fun stopAtDelayStopsAtMaximum() {
        val policy = constantDelay<Unit>(80).stopAtDelay(80)

        val attempt = FailedAttempt(
            failure = Unit,
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(StopRetrying, instruction)
    }

    @Test
    fun stopAtDelayStopsAboveMaximum() {
        val policy = constantDelay<Unit>(300).stopAtDelay(150)

        val attempt = FailedAttempt(
            failure = Unit,
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(StopRetrying, instruction)
    }

    @Test
    fun stopAtCumulativeDelayContinuesUnderMaximum() {
        val policy = constantDelay<Unit>(20).stopAtCumulativeDelay(50)

        val attempt = FailedAttempt(
            failure = Unit,
            number = 1,
            previousDelay = 20,
            cumulativeDelay = 20,
        )

        val instruction = policy(attempt)

        assertEquals(RetryAfter(20), instruction)
    }

    @Test
    fun stopAtCumulativeDelayStopsAtMaximum() {
        val policy = constantDelay<Unit>(40).stopAtCumulativeDelay(80)

        val attempt = FailedAttempt(
            failure = Unit,
            number = 2,
            previousDelay = 40,
            cumulativeDelay = 80,
        )

        val instruction = policy(attempt)

        assertEquals(StopRetrying, instruction)
    }

    @Test
    fun stopAtCumulativeDelayStopsAboveMaximum() {
        val policy = constantDelay<Unit>(60).stopAtCumulativeDelay(150)

        val attempt = FailedAttempt(
            failure = Unit,
            number = 3,
            previousDelay = 60,
            cumulativeDelay = 180,
        )

        val instruction = policy(attempt)

        assertEquals(StopRetrying, instruction)
    }
}
