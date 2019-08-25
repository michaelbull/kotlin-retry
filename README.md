# kotlin-retry

[![Release](https://api.bintray.com/packages/michaelbull/maven/kotlin-retry/images/download.svg)](https://bintray.com/michaelbull/maven/kotlin-retry/_latestVersion) [![Build Status](https://travis-ci.org/michaelbull/kotlin-retry.svg?branch=master)](https://travis-ci.org/michaelbull/kotlin-retry) [![License](https://img.shields.io/github/license/michaelbull/kotlin-retry.svg)](https://github.com/michaelbull/kotlin-retry/blob/master/LICENSE)

[`retry`][retry] is a higher-order function for retrying operations that may fail.

```kotlin
retry(limitAttempts(10) + constantDelay(delayMillis = 50L)) {
    /* your code */
}
```

## Installation

```groovy
repositories {
    maven { url = 'https://dl.bintray.com/michaelbull/maven' }
}

dependencies {
    compile 'com.michael-bull.kotlin-retry:kotlin-retry:1.0.1'
}
```

## Introduction

IO operations often experience temporary failures that warant re-execution, 
e.g. a database transaction that may fail due to a deadlock.<sup>[[1]][innodb-deadlocks][[2]][postgres-deadlocks]</sup>

> _“even if your application logic is correct, you must still handle the case
>  where a transaction must be retried”_
>
> — _[Deadlocks in InnoDB][innodb-deadlocks]_ 
 
The [`retry`][retry] function simplifies this process by wrapping the
application logic and applying a specified [`RetryPolicy`][retry-policy].

In the example below, either of the calls to `customers.nameFromId` may fail,
abandoning the remaining logic within the `printExchangeBetween` function. As
such, we may want to retry this operation until 5 attempts in total have been
executed:

```kotlin
suspend fun printExchangeBetween(a: Long, b: Long) {
  val customer1 = customers.nameFromId(a)
  val customer2 = customers.nameFromId(b)
  println("$customer1 exchanged with $customer2")
}

fun main() = runBlocking {
    retry(limitAttempts(5)) { 
        printExchangeBetween(1L, 2L)
    }
}
```

We can also provide a [`RetryPolicy`][retry-policy] that only retries failures
of a specific type. The example below will retry the operation only if the
reason for failure was a `SQLDataException`, pausing for 20 milliseconds before
retrying and stopping after 5 total attempts.

```kotlin
val retryTimeouts: RetryPolicy<Throwable> = {
    if (reason is SQLDataException) ContinueRetrying else StopRetrying
}

suspend fun printExchangeBetween(a: Long, b: Long) {
  val customer1 = customers.nameFromId(a)
  val customer2 = customers.nameFromId(b)
  println("$customer1 exchanged with $customer2")
}

fun main() = runBlocking {
    retry(retryTimeouts + limitAttempts(5) + constantDelay(20)) { 
        printExchangeBetween(1L, 2L)
    }
}
```

## Backoff

The examples above retry executions immediately after they fail, however we may
wish to spread out retries out with an ever-increasing delay. This is known as
a "backoff" and comes in many forms. This library includes all the forms of
backoff strategy detailed the article by Marc Brooker on the AWS Architecture
Blog entitled ["Exponential Backoff And Jitter"][aws-backoff].

#### Binary Exponential

> _“exponential backoff means that clients multiply their backoff by a constant
>   after each attempt, up to some maximum value”_
>
> ```
> sleep = min(cap, base * 2 ** attempt)
> ```

```kotlin
retry(limitAttempts(5) + binaryExponentialBackoff(base = 10L, max = 5000L)) {
    /* code */
}
```

#### Full Jitter

> _“trying to improve the performance of a system by adding randomness ... we
>  want to spread out the spikes to an approximately constant rate”_
>
> ```
> sleep = random_between(0, min(cap, base * 2 ** attempt))
> ```

```kotlin
retry(limitAttempts(5) + fullJitterBackoff(base = 10L, max = 5000L)) {
    /* code */
}
```

#### Equal Jitter

> _“Equal Jitter, where we always keep some of the backoff and jitter by a
>   smaller amount”_
>
> ```
> temp = min(cap, base * 2 ** attempt)
> sleep = temp / 2 + random_between(0, temp / 2)
> ```

```kotlin
retry(limitAttempts(5) + equalJitterBackoff(base = 10L, max = 5000L)) {
    /* code */
}
```

#### Decorrelated Jitter

> _“Decorrelated Jitter, which is similar to “Full Jitter”, but we also
>   increase the maximum jitter based on the last random value”_
>
> ```
> sleep = min(cap, random_between(base, sleep * 3))
> ```

```kotlin
retry(limitAttempts(5) + decorrelatedJitterBackoff(base = 10L, max = 5000L)) {
    /* code */
}
```

## Inspiration

- [Control.Retry](http://hackage.haskell.org/package/retry-0.8.0.1/docs/Control-Retry.html)
- [tokio_retry](https://docs.rs/tokio-retry/0.2.0/tokio_retry/)

## Contributing

Bug reports and pull requests are welcome on [GitHub][github].

## License

This project is available under the terms of the ISC license. See the
[`LICENSE`](LICENSE) file for the copyright information and licensing terms.

[github]: https://github.com/michaelbull/kotlin-retry
[retry]: https://github.com/michaelbull/kotlin-retry/blob/master/src/main/kotlin/com/github/michaelbull/retry/Retry.kt
[innodb-deadlocks]: https://dev.mysql.com/doc/refman/8.0/en/innodb-deadlocks.html
[postgres-deadlocks]: https://www.postgresql.org/docs/current/explicit-locking.html#LOCKING-DEADLOCKS
[retry-policy]: https://github.com/michaelbull/kotlin-retry/blob/master/src/main/kotlin/com/github/michaelbull/retry/policy/RetryPolicy.kt
[aws-backoff]: https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/
[haskell-retry]: http://hackage.haskell.org/package/retry-0.8.0.1/docs/Control-Retry.html
