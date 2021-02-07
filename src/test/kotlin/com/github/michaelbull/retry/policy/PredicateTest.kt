package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.retry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalCoroutinesApi
class PredicateTest {

    private val retryIllegalState = retryIf<Throwable> {
        reason is IllegalStateException
    }

    private val dontRetryOob = retryUnless<Throwable> {
        reason is IndexOutOfBoundsException
    }

    @Test
    fun `retryIf should retry specified exception`() = runBlockingTest {
        var attempts = 0

        retry(limitAttempts(5) + retryIllegalState) {
            attempts++
            if (attempts < 5) {
                throw IllegalStateException()
            }
        }

        assertEquals(5, attempts)
    }

    @Test
    fun `retryIf should not retry unspecified exception`() = runBlockingTest {
        var attempts = 0

        assertThrows<UnsupportedOperationException> {
            retry(limitAttempts(5) + retryIllegalState) {
                attempts++
                if (attempts < 5) {
                    throw UnsupportedOperationException()
                }
            }
        }

        assertEquals(1, attempts)
    }

    @Test
    fun `retryUnless should not retry specified exception`() = runBlockingTest {
        var attempts = 0

        assertThrows<IndexOutOfBoundsException> {
            retry(limitAttempts(5) + dontRetryOob) {
                attempts++
                if (attempts < 5) {
                    throw IndexOutOfBoundsException()
                }
            }
        }

        assertEquals(1, attempts)
    }

    @Test
    fun `retryUnless should retry unspecified exception`() = runBlockingTest {
        var attempts = 0

        retry(limitAttempts(5) + dontRetryOob) {
            attempts++
            if (attempts < 5) {
                throw IllegalStateException()
            }
        }

        assertEquals(5, attempts)
    }
}
