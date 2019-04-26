package com.hezaro.wall.sdk.base.exception

/**
 * Base Class for handling errors/failures/exceptions.
 * Every feature specific failure should extend [FeatureFailure] class.
 */
sealed class Failure : Throwable() {

    class UserNotFound : Failure()
    class NetworkConnection : Failure()
    data class ServerError(val code: Int, override val message: String) : Failure()
    data class FeatureFailure(val throwable: Throwable) : Failure()
}
