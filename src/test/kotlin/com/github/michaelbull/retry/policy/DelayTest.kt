package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.attempt.FailedAttempt
import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.StopRetrying
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class DelayTest {

    @Test
    fun `maxDelay does not change stopped policy`() {
        val policy = constantDelay<Unit>(30)
            .stopAtDelay(5)
            .delayAtMost(50)

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
    fun `maxDelay does not change immediate retry`() {
        val policy = stopAtAttempts<Unit>(5).delayAtMost(20)

        val attempt = FailedAttempt(
            failure = Unit,
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(ContinueRetrying, instruction)
    }

    @Test
    fun `maxDelay does not change delay lower than maximum delay`() {
        val policy = constantDelay<Unit>(30).delayAtMost(50)

        val attempt = FailedAttempt(
            failure = Unit,
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(30, instruction.delayMillis)
    }

    @Test
    fun `maxDelay does not change delay equal to maximum delay`() {
        val policy = constantDelay<Unit>(100).delayAtMost(100)

        val attempt = FailedAttempt(
            failure = Unit,
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(100, instruction.delayMillis)
    }

    @Test
    fun `maxDelay reduces delay greater than maximum delay`() {
        val policy = constantDelay<Unit>(200).delayAtMost(150)

        val attempt = FailedAttempt(
            failure = Unit,
            number = 0,
            previousDelay = 0,
            cumulativeDelay = 0,
        )

        val instruction = policy(attempt)

        assertEquals(150, instruction.delayMillis)
    }
}
