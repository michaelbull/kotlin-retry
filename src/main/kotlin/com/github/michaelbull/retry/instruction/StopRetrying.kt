package com.github.michaelbull.retry.instruction

import com.github.michaelbull.retry.retry

/**
 * Instructs the [retry] function to stop attempting retries.
 */
val StopRetrying = RetryInstruction(-1L)
