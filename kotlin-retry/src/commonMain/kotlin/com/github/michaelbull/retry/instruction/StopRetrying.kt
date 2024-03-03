package com.github.michaelbull.retry.instruction

import com.github.michaelbull.retry.retry

/**
 * Instructs the [retry] function to stop attempting retries.
 */
public val StopRetrying: RetryInstruction = RetryInstruction(-1L)
