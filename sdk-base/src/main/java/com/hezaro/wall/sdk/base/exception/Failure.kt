package com.hezaro.wall.sdk.base.exception

/**
 * Base Class for handling errors/failures/exceptions.
 * Every feature specific failure should extend [FeatureFailure] class.
 */
sealed class Failure : Throwable() {

    class UserNotFound : Failure()
    class NetworkConnection(override val message: String = "اینترنت دردسترس نیست") : Failure()
    data class ServerError(val code: Int, override val message: String) : Failure()
    data class ExceptionFailure(val throwable: Throwable) : Failure()
    data class FeatureFailure(val code: Int, override val message: String) : Failure()
}
