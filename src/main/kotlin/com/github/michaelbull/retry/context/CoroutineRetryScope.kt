package com.github.michaelbull.retry.context

import com.github.michaelbull.retry.RetryScope
import kotlin.random.Random

@PublishedApi
internal class CoroutineRetryScope(
    private val status: RetryStatus,
    private val retryRandom: RetryRandom
) : RetryScope {

    override val attempt: Int
        get() = status.attempt

    override val previousDelay: Long
        get() = status.previousDelay

    override val cumulativeDelay: Long
        get() = status.cumulativeDelay

    override val random: Random
        get() = retryRandom.random
}
