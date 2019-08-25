package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.ContinueRetrying
import com.github.michaelbull.retry.RetryFailure
import com.github.michaelbull.retry.StopRetrying
import com.github.michaelbull.retry.context.RetryStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class LimitTest {

    private val failure = RetryFailure(Unit)

    @Test
    fun `limitAttempts should retry if below maximum attempts`() {
        runBlockingTest(RetryStatus()) {
            val policy = limitAttempts(5)
            val instruction = failure.policy()
            assertEquals(ContinueRetrying, instruction)
        }
    }

    @Test
    fun `limitAttempts should stop retrying if reached maximum attempts`() {
        runBlockingTest(RetryStatus(attempt = 2)) {
            val policy = limitAttempts(2)
            val instruction = failure.policy()
            assertEquals(StopRetrying, instruction)
        }
    }

    @Test
    fun `maxDelay should not change stopped policy`() {
        runBlockingTest(RetryStatus()) {
            val policy = constantDelay(30).limitByDelay<Unit>(5).maxDelay(50)
            val instruction = failure.policy()
            assertEquals(StopRetrying, instruction)
        }
    }

    @Test
    fun `maxDelay should not change immediate retry`() {
        runBlockingTest(RetryStatus()) {
            val policy = limitAttempts(5)
            val instruction = failure.policy()
            assertEquals(ContinueRetrying, instruction)
        }
    }

    @Test
    fun `maxDelay should not change delay lower than maximum delay`() {
        runBlockingTest(RetryStatus()) {
            val policy = constantDelay(30).maxDelay<Unit>(50)
            val instruction = failure.policy()
            assertEquals(30, instruction.delayMillis)
        }
    }

    @Test
    fun `maxDelay should not change delay equal to maximum delay`() {
        runBlockingTest(RetryStatus()) {
            val policy = constantDelay(100).maxDelay<Unit>(100)
            val instruction = failure.policy()
            assertEquals(100, instruction.delayMillis)
        }
    }

    @Test
    fun `maxDelay should reduce delay greater than maximum delay`() {
        runBlockingTest {
            val policy = constantDelay(200).maxDelay<Unit>(150)
            val instruction = failure.policy()
            assertEquals(150, instruction.delayMillis)
        }
    }

    @Test
    fun `limitByDelay should return result of policy if below maximum delay`() {
        runBlockingTest(RetryStatus()) {
            val policy = constantDelay(20).limitByDelay<Unit>(50)
            val instruction = failure.policy()
            assertEquals(20, instruction.delayMillis)
        }
    }

    @Test
    fun `limitByDelay should stop retrying if equal to maximum delay`() {
        runBlockingTest(RetryStatus()) {
            val policy = constantDelay(80).limitByDelay<Unit>(80)
            val instruction = failure.policy()
            assertEquals(StopRetrying, instruction)
        }
    }

    @Test
    fun `limitByDelay should stop retrying if more than maximum delay`() {
        runBlockingTest(RetryStatus()) {
            val policy = constantDelay(300).limitByDelay<Unit>(150)
            val instruction = failure.policy()
            assertEquals(StopRetrying, instruction)
        }
    }

    @Test
    fun `limitByCumulativeDelay should return result of policy if below maximum cumulative delay`() {
        runBlockingTest(
            RetryStatus(
                attempt = 1,
                previousDelay = 20,
                cumulativeDelay = 20
            )
        ) {
            val policy = constantDelay(20).limitByCumulativeDelay<Unit>(50)
            val instruction = failure.policy()
            assertEquals(20, instruction.delayMillis)
        }
    }

    @Test
    fun `limitByCumulativeDelay should stop retrying if equal to maximum cumulative delay`() {
        runBlockingTest(
            RetryStatus(
                attempt = 2,
                previousDelay = 40,
                cumulativeDelay = 80
            )
        ) {
            val policy = constantDelay(40).limitByCumulativeDelay<Unit>(80)
            val instruction = failure.policy()
            assertEquals(StopRetrying, instruction)
        }
    }

    @Test
    fun `limitByCumulativeDelay should stop retrying if more than maximum cumulative delay`() {
        runBlockingTest(
            RetryStatus(
                attempt = 3,
                previousDelay = 60,
                cumulativeDelay = 180
            )
        ) {
            val policy = constantDelay(60).limitByCumulativeDelay<Unit>(150)
            val instruction = failure.policy()
            assertEquals(StopRetrying, instruction)
        }
    }
}
