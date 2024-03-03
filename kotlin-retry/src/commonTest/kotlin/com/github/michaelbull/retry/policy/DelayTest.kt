package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.attempt.FailedAttempt
import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.StopRetrying
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class DelayTest {

    @Test
    fun delayAtMostStoppedPolicy() {
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
    fun delayAtMostRunningPolicy() {
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
    fun delayAtMostLowerBound() {
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
    fun delayAtMostUpperBound() {
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
    fun delayAtMostReduce() {
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
