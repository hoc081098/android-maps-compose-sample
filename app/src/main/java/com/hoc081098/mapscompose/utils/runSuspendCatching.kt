package com.hoc081098.mapscompose.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * https://github.com/Kotlin/kotlinx.coroutines/issues/1814
 */
@OptIn(ExperimentalContracts::class)
@Suppress("RedundantSuspendModifier")
suspend inline fun <R> runSuspendCatching(block: () -> R): Result<R> {
  contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
  return try {
    Result.success(block())
  } catch (c: CancellationException) {
    throw c
  } catch (e: Throwable) {
    Result.failure(e)
  }
}

suspend inline fun <T> retrySuspend(
  times: Int,
  initialDelay: Duration,
  factor: Double,
  maxDelay: Duration = Duration.INFINITE,
  shouldRetry: (Int, Throwable) -> Boolean = { _, _ -> true },
  block: (times: Int) -> T,
): T {
  var currentDelay = initialDelay
  repeat(times - 1) {
    try {
      return block(it)
    } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
      if (!shouldRetry(it, e)) {
        throw e
      }
      // you can log an error here and/or make a more finer-grained
      // analysis of the cause to see if retry is needed
    }
    delay(currentDelay)
    currentDelay = (currentDelay * factor).coerceAtMost(maxDelay)
  }
  return block(times - 1) // last attempt
}

inline fun <T> Result<T>.mapFailure(f: (Throwable) -> Throwable): Result<T> = fold(
  onSuccess = { Result.success(it) },
  onFailure = { Result.failure(f(it)) }
)
