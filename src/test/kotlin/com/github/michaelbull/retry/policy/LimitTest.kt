package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.ContinueRetrying
import com.github.michaelbull.retry.DelegatedRetryFailure
import com.github.michaelbull.retry.StopRetrying
import com.github.michaelbull.retry.context.CoroutineRetryScope
import com.github.michaelbull.retry.context.RetryStatus
import com.github.michaelbull.retry.context.retryRandom
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class LimitTest {

    @Test
    fun `limitAttempts retries if below maximum attempts`() = runBlockingTest {
        val policy = limitAttempts(5)
        val status = RetryStatus(attempt = 0)

        val retryScope = CoroutineRetryScope(status, coroutineContext.retryRandom)
        val instruction = DelegatedRetryFailure(Unit, retryScope).policy()
        assertEquals(ContinueRetrying, instruction)
    }

    @Test
    fun `limitAttempts stops retrying if maximum attempts reached`() = runBlockingTest {
        val policy = limitAttempts(2)
        val status = RetryStatus(attempt = 2)

        val retryScope = CoroutineRetryScope(status, coroutineContext.retryRandom)
        val instruction = DelegatedRetryFailure(Unit, retryScope).policy()
        assertEquals(StopRetrying, instruction)
    }

    @Test
    fun `maxDelay does not change stopped policy`() = runBlockingTest {
        val policy = constantDelay(30).limitByDelay<Unit>(5).maxDelay(50)
        val status = RetryStatus()

        val retryScope = CoroutineRetryScope(status, coroutineContext.retryRandom)
        val instruction = DelegatedRetryFailure(Unit, retryScope).policy()
        assertEquals(StopRetrying, instruction)
    }

    @Test
    fun `maxDelay does not change immediate retry`() = runBlockingTest {
        val policy = limitAttempts(5).maxDelay<Unit>(20)
        val status = RetryStatus()

        val retryScope = CoroutineRetryScope(status, coroutineContext.retryRandom)
        val instruction = DelegatedRetryFailure(Unit, retryScope).policy()
        assertEquals(ContinueRetrying, instruction)
    }

    @Test
    fun `maxDelay does not change delay lower than maximum delay`() = runBlockingTest {
        val policy = constantDelay(30).maxDelay<Unit>(50)
        val status = RetryStatus()

        val retryScope = CoroutineRetryScope(status, coroutineContext.retryRandom)
        val instruction = DelegatedRetryFailure(Unit, retryScope).policy()
        assertEquals(30, instruction.delayMillis)
    }

    @Test
    fun `maxDelay does not change delay equal to maximum delay`() = runBlockingTest {
        val policy = constantDelay(100).maxDelay<Unit>(100)
        val status = RetryStatus()

        val retryScope = CoroutineRetryScope(status, coroutineContext.retryRandom)
        val instruction = DelegatedRetryFailure(Unit, retryScope).policy()
        assertEquals(100, instruction.delayMillis)
    }

    @Test
    fun `maxDelay reduces delay greater than maximum delay`() = runBlockingTest {
        val policy = constantDelay(200).maxDelay<Unit>(150)
        val status = RetryStatus()

        val retryScope = CoroutineRetryScope(status, coroutineContext.retryRandom)
        val instruction = DelegatedRetryFailure(Unit, retryScope).policy()
        assertEquals(150, instruction.delayMillis)
    }

    @Test
    fun `limitByDelay returns result of policy if below maximum delay`() = runBlockingTest {
        val policy = constantDelay(20).limitByDelay<Unit>(50)
        val status = RetryStatus()

        val retryScope = CoroutineRetryScope(status, coroutineContext.retryRandom)
        val instruction = DelegatedRetryFailure(Unit, retryScope).policy()
        assertEquals(20, instruction.delayMillis)
    }

    @Test
    fun `limitByDelay stops retrying if equal to maximum delay`() = runBlockingTest {
        val policy = constantDelay(80).limitByDelay<Unit>(80)
        val status = RetryStatus()

        val retryScope = CoroutineRetryScope(status, coroutineContext.retryRandom)
        val instruction = DelegatedRetryFailure(Unit, retryScope).policy()
        assertEquals(StopRetrying, instruction)

    }

    @Test
    fun `limitByDelay stops retrying if more than maximum delay`() = runBlockingTest {
        val policy = constantDelay(300).limitByDelay<Unit>(150)
        val status = RetryStatus()

        val retryScope = CoroutineRetryScope(status, coroutineContext.retryRandom)
        val instruction = DelegatedRetryFailure(Unit, retryScope).policy()
        assertEquals(StopRetrying, instruction)
    }

    @Test
    fun `limitByCumulativeDelay returns result of policy if below maximum cumulative delay`() = runBlockingTest {
        val policy = constantDelay(20).limitByCumulativeDelay<Unit>(50)

        val status = RetryStatus(
            attempt = 1,
            previousDelay = 20,
            cumulativeDelay = 20
        )

        val retryScope = CoroutineRetryScope(status, coroutineContext.retryRandom)
        val instruction = DelegatedRetryFailure(Unit, retryScope).policy()
        assertEquals(20, instruction.delayMillis)
    }

    @Test
    fun `limitByCumulativeDelay stops retrying if equal to maximum cumulative delay`() = runBlockingTest {
        val policy = constantDelay(40).limitByCumulativeDelay<Unit>(80)

        val status = RetryStatus(
            attempt = 2,
            previousDelay = 40,
            cumulativeDelay = 80
        )

        val retryScope = CoroutineRetryScope(status, coroutineContext.retryRandom)
        val instruction = DelegatedRetryFailure(Unit, retryScope).policy()
        assertEquals(StopRetrying, instruction)
    }

    @Test
    fun `limitByCumulativeDelay stops retrying if more than maximum cumulative delay`() = runBlockingTest {
        val policy = constantDelay(60).limitByCumulativeDelay<Unit>(150)

        val status = RetryStatus(
            attempt = 3,
            previousDelay = 60,
            cumulativeDelay = 180
        )

        val retryScope = CoroutineRetryScope(status, coroutineContext.retryRandom)
        val instruction = DelegatedRetryFailure(Unit, retryScope).policy()
        assertEquals(StopRetrying, instruction)
    }
}
