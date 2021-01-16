package com.github.michaelbull.retry.policy

import com.github.michaelbull.retry.ContinueRetrying
import com.github.michaelbull.retry.StopRetrying

fun <E : Throwable> filter(test: (Throwable) -> Boolean): RetryPolicy<E> = {
    if (test(this.reason)) {
        ContinueRetrying
    } else {
        StopRetrying
    }
}
