package com.github.michaelbull.retry.context

import com.github.michaelbull.retry.retry
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

private val DEFAULT = RetryRandom()

val CoroutineContext.retryRandom: RetryRandom
    get() = get(RetryRandom) ?: DEFAULT

/**
 * Represents the random number generator used for backoff calculations during
 * a [retrying][retry] operation.
 */
class RetryRandom(
    val random: Random = Random.Default
) : AbstractCoroutineContextElement(RetryRandom) {

    companion object Key : CoroutineContext.Key<RetryRandom>

    override fun toString() = "RetryRandom($random)"
}
