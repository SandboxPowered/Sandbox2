package org.sandboxpowered.fabric.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object TimingUtil {
    private val executor = Executors.newSingleThreadExecutor()

    fun execute(
        func: () -> Unit,
        timeout: Long = 5,
        unit: TimeUnit = TimeUnit.SECONDS,
        executor: ExecutorService? = null,
    ): Result<Any, Exception> {
        return try {
            Success((executor ?: this.executor).submit(func).get(timeout, unit))
        } catch (exception: TimeoutException) {
            Timeout(exception)
        } catch (exception: Exception) {
            Error(exception)
        }
    }

    fun <T> executeWithResponse(
        func: () -> T,
        timeout: Long = 5,
        unit: TimeUnit = TimeUnit.SECONDS,
        executor: ExecutorService? = null,
    ): Result<T, Exception> {
        return try {
            Success((executor ?: this.executor).submit(func).get(timeout, unit))
        } catch (exception: TimeoutException) {
            Timeout(exception)
        } catch (exception: Exception) {
            Error(exception)
        }
    }

    sealed class Result<out T, out E : Throwable>
    class Success<out T>(val value: T) : Result<T, Nothing>()
    class Timeout<out E : Throwable>(error: E) : Error<E>(error)
    open class Error<out E : Throwable>(val error: E) : Result<Nothing, E>()
}