package com.hezaro.wall.sdk.base

import com.hezaro.wall.sdk.base.exception.Failure
import kotlinx.coroutines.Deferred
import retrofit2.Response

open class BaseRepository {

    suspend fun <T, R> request(call: Deferred<Response<T>>, transform: (T) -> R): Either<Failure, R> {
        return try {
            val response = call.await()
                when {
                    response.isSuccessful -> Either.Right(transform((response.body()!!)))
                    else -> Either.Left(Failure.NetworkConnection())
                }

        } catch (exception: Throwable) {
            Either.Left(Failure.FeatureFailure(exception))
        }
    }


}