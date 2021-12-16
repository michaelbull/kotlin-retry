package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.DelegatedRetryFailure
import com.github.michaelbull.retry.RetryInstruction
import com.github.michaelbull.retry.context.CoroutineRetryScope
import com.github.michaelbull.retry.context.RetryRandom
import com.github.michaelbull.retry.context.RetryStatus
import com.github.michaelbull.retry.context.retryRandom
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

@ExperimentalCoroutinesApi
class BackoffTest {

    private val exponential = binaryExponentialBackoff(10L..8000L)
    private val fullJitter = fullJitterBackoff(10L..8000L)
    private val equalJitter = equalJitterBackoff(10L..8000L)
    private val decorrelatedJitter = decorrelatedJitterBackoff(10L..8000L)

    private val random = mockk<Random>(relaxed = true).apply {
        every { nextLong(any()) } answers { firstArg<Long>() - 1 }
        every { nextLong(any(), any()) } answers { secondArg<Long>() - 1 }
    }

    private suspend fun simulate(attempt: Int, policy: RetryPolicy<Unit>): RetryInstruction {
        var lastInstruction: RetryInstruction? = null
        val status = RetryStatus()

        repeat(attempt + 1) {
            val retryScope = CoroutineRetryScope(status, coroutineContext.retryRandom)
            val instruction = DelegatedRetryFailure(Unit, retryScope).policy()
            val delay = instruction.delayMillis

            if (delay > 0) {
                status.previousDelay = delay
                status.incrementAttempts()
                status.incrementCumulativeDelay(delay)
            }

            lastInstruction = instruction
        }

        return lastInstruction!!
    }

    @ArgumentsSource(ExponentialBackoffArgumentProvider::class)
    @ParameterizedTest(name = "exponentialBackoff(attempt={0}) returns delay={1}")
    fun `exponential backoff`(attempt: Int, expectedDelay: Long) {
        runBlockingTest {
            val instruction = simulate(attempt, exponential)
            assertEquals(expectedDelay, instruction.delayMillis)
        }
    }

    @ArgumentsSource(ExponentialBackoffArgumentProvider::class)
    @ParameterizedTest(name = "fullJitter(attempt={0}) returns delay={1}")
    fun `full jitter`(attempt: Int, expectedDelay: Long) {
        runBlockingTest(RetryRandom(random)) {
            val instruction = simulate(attempt, fullJitter)
            assertEquals(expectedDelay, instruction.delayMillis)
        }
    }

    @Test
    fun `full jitter lower random bound`() {
        val lowerBoundRandom = mockk<Random>(relaxed = true).apply {
            every { nextLong(any()) } answers { 0 }
            every { nextLong(any(), any()) } answers { firstArg() }
        }

        runBlockingTest(RetryRandom(lowerBoundRandom)) {
            val instruction = simulate(0, fullJitter)
            assertEquals(0L, instruction.delayMillis)
        }
    }

    @ArgumentsSource(ExponentialBackoffArgumentProvider::class)
    @ParameterizedTest(name = "equalJitter(attempt={0}) returns delay={1}")
    fun `equal jitter`(attempt: Int, expectedDelay: Long) {
        runBlockingTest(RetryRandom(random)) {
            val instruction = simulate(attempt, equalJitter)
            assertEquals(expectedDelay, instruction.delayMillis)
        }
    }

    @ArgumentsSource(DecorrelatedJitterArgumentProvider::class)
    @ParameterizedTest(name = "decorrelatedJitter(attempt={0}) returns delay={1}")
    fun `decorrelated jitter`(attempt: Int, expectedDelay: Long) {
        runBlockingTest(RetryRandom(random)) {
            val instruction = simulate(attempt, decorrelatedJitter)
            assertEquals(expectedDelay, instruction.delayMillis)
        }
    }

    private class ExponentialBackoffArgumentProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            return Stream.of(
                Pair(0, 10L),
                Pair(1, 20L),
                Pair(2, 40L),
                Pair(3, 80L),
                Pair(4, 160L),
                Pair(5, 320L),
                Pair(6, 640L),
                Pair(7, 1280L),
                Pair(8, 2560L),
                Pair(9, 5120L),
                Pair(10, 8000L)
            ).map { Arguments.of(it.first, it.second) }
        }
    }

    private class DecorrelatedJitterArgumentProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            return Stream.of(
                Pair(0, 10L),
                Pair(1, 30L),
                Pair(2, 90L),
                Pair(3, 270L),
                Pair(4, 810L),
                Pair(5, 2430L),
                Pair(6, 7290L),
                Pair(7, 8000L),
                Pair(8, 8000L),
                Pair(9, 8000L),
                Pair(10, 8000L)
            ).map { Arguments.of(it.first, it.second) }
        }
    }
}
