package com.github.michaelbull.retry

import kotlin.random.Random

interface RetryScope {
    val attempt: Int
    val previousDelay: Long
    val cumulativeDelay: Long
    val random: Random
}
