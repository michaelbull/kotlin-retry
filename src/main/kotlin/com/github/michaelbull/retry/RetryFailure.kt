package com.github.michaelbull.retry

/**
 * Represents a failed [retry] attempt.
 */
interface RetryFailure<T> : RetryScope {

    /**
     * The reason the attempt failed.
     */
    val reason: T
}

@PublishedApi
internal class DelegatedRetryFailure<T>(
    override val reason: T,
    private val delegate: RetryScope
) : RetryFailure<T>, RetryScope by delegate
