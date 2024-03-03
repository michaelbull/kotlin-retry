package com.github.michaelbull.retry.instruction

import com.github.michaelbull.retry.retry

/**
 * Instructs the [retry] function to continue attempting.
 */
val ContinueRetrying = RetryInstruction(0L)
