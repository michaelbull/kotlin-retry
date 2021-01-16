package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.retry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalCoroutinesApi
class RetryIfTest {

    private val retryIllegalState = retryIf<Throwable> {
        reason is IllegalStateException
    }

    @Test
    fun `retry with filter should retry specified exception`() = runBlockingTest {
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
    fun `retry with filter should not retry unspecified exception`() = runBlockingTest {
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
}
