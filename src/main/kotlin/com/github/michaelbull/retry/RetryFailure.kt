package com.github.michaelbull.retry

/**
 * Represents a failed [retry] attempt.
 */
class RetryFailure<T>(

    /**
     * The reason the attempt failed.
     */
    val reason: T
)
