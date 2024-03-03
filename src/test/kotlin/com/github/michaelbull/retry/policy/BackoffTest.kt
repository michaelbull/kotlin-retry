package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.attempt.firstAttempt
import com.github.michaelbull.retry.instruction.ContinueRetrying
import com.github.michaelbull.retry.instruction.RetryAfter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random

@ExperimentalCoroutinesApi
class BackoffTest {

    @Test
    fun binaryExponentialBackoff() {
        val policy = binaryExponentialBackoff<Unit>(10L..8000L)

        val expected = listOf(
            RetryAfter(10L),
            RetryAfter(20L),
            RetryAfter(40L),
            RetryAfter(80L),
            RetryAfter(160L),
            RetryAfter(320L),
            RetryAfter(640L),
            RetryAfter(1280L),
            RetryAfter(2560L),
            RetryAfter(5120L),
            RetryAfter(8000L),
        )

        val actual = policy.simulate(expected.size, Unit)

        assertEquals(expected, actual)
    }

    @Test
    fun fullJitterBackoff() {
        val policy = fullJitterBackoff<Unit>(10L..8000L, PenultimateRandom)

        val expected = listOf(
            RetryAfter(10L),
            RetryAfter(20L),
            RetryAfter(40L),
            RetryAfter(80L),
            RetryAfter(160L),
            RetryAfter(320L),
            RetryAfter(640L),
            RetryAfter(1280L),
            RetryAfter(2560L),
            RetryAfter(5120L),
            RetryAfter(8000L),
        )

        val actual = policy.simulate(expected.size, Unit)

        assertEquals(expected, actual)
    }

    @Test
    fun fullJitterBackoffLowerBound() {
        val policy = fullJitterBackoff<Unit>(10L..8000L, LowerBoundRandom)

        val expected = listOf(
            ContinueRetrying,
            ContinueRetrying,
            ContinueRetrying,
            ContinueRetrying,
            ContinueRetrying,
            ContinueRetrying,
            ContinueRetrying,
            ContinueRetrying,
            ContinueRetrying,
            ContinueRetrying,
        )

        val actual = policy.simulate(expected.size, Unit)

        assertEquals(expected, actual)
    }

    @Test
    fun equalJitter() {
        val policy = equalJitterBackoff<Unit>(10L..8000L, PenultimateRandom)

        val expected = listOf(
            RetryAfter(10L),
            RetryAfter(20L),
            RetryAfter(40L),
            RetryAfter(80L),
            RetryAfter(160L),
            RetryAfter(320L),
            RetryAfter(640L),
            RetryAfter(1280L),
            RetryAfter(2560L),
            RetryAfter(5120L),
            RetryAfter(8000L),
        )

        val actual = policy.simulate(expected.size, Unit)

        assertEquals(expected, actual)
    }

    @Test
    fun decorrelatedJitter() {
        val policy = decorrelatedJitterBackoff<Unit>(10L..8000L, 3, PenultimateRandom)

        val expected = listOf(
            RetryAfter(10L),
            RetryAfter(30L),
            RetryAfter(90L),
            RetryAfter(270L),
            RetryAfter(810L),
            RetryAfter(2430L),
            RetryAfter(7290L),
            RetryAfter(8000L),
            RetryAfter(8000L),
            RetryAfter(8000L),
            RetryAfter(8000L)
        )

        val actual = policy.simulate(expected.size, Unit)

        assertEquals(expected, actual)
    }

    private fun <E> RetryPolicy<E>.simulate(times: Int, failure: E) = buildList {
        val attempt = firstAttempt()

        repeat(times) {
            val failedAttempt = attempt.failedWith(failure)
            val instruction = this@simulate.invoke(failedAttempt)

            add(instruction)

            if (instruction == ContinueRetrying) {
                attempt.retryImmediately()
            } else if (instruction.delayMillis > 0) {
                attempt.retryAfter(instruction.delayMillis)
            }
        }
    }


    private object PenultimateRandom : Random() {
        override fun nextBits(bitCount: Int) = 0

        override fun nextLong(until: Long): Long {
            return until - 1
        }

        override fun nextLong(from: Long, until: Long): Long {
            return until - 1
        }
    }

    private object LowerBoundRandom : Random() {
        override fun nextBits(bitCount: Int) = 0

        override fun nextLong(until: Long): Long {
            return 0
        }

        override fun nextLong(from: Long, until: Long): Long {
            return from
        }
    }
}
