package com.github.michaelbull.retry.instruction

import com.github.michaelbull.retry.retry

/**
 * Instructs the [retry] function to continue attempting.
 */
public val ContinueRetrying: RetryInstruction = RetryInstruction(0L)
