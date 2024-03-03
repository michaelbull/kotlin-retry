package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.attempt.FailedAttempt
import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.StopRetrying
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class StopTest {

    @Test
    fun `stopAtAttempts retries if below maximum attempts`() {
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
    fun `stopAtAttempts stops retrying if maximum attempts reached`() {
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
    fun `stopAtDelay returns result of policy if below maximum delay`() = runTest {
        val policy = constantDelay<Unit>(20).stopAtDelay(50)

        val attempt = FailedAttempt(
            failure = Unit,
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(20, instruction.delayMillis)
    }

    @Test
    fun `stopAtDelay stops retrying if equal to maximum delay`() = runTest {
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
    fun `stopAtDelay stops retrying if more than maximum delay`() = runTest {
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
    fun `stopAtCumulativeDelay returns result of policy if below maximum cumulative delay`() = runTest {
        val policy = constantDelay<Unit>(20).stopAtCumulativeDelay(50)

        val attempt = FailedAttempt(
            failure = Unit,
            number = 1,
            previousDelay = 20,
            cumulativeDelay = 20,
        )

        val instruction = policy(attempt)

        assertEquals(20, instruction.delayMillis)
    }

    @Test
    fun `stopAtCumulativeDelay stops retrying if equal to maximum cumulative delay`() = runTest {
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
    fun `stopAtCumulativeDelay stops retrying if more than maximum cumulative delay`() = runTest {
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
