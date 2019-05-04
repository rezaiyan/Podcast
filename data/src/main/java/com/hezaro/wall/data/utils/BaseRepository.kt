package com.hezaro.wall.data.utils

import com.hezaro.wall.data.model.ErrorModel
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import com.squareup.moshi.Moshi
import retrofit2.Call
import java.net.HttpURLConnection

open class BaseRepository {
    @Suppress("SENSELESS_COMPARISON")
    fun <T, R> request(call: Call<T>?, transform: (T) -> R): Either<Failure, R> {

        return try {
            val response = call!!.execute()

            if (response == null)
                Either.Left(Failure.NetworkConnection())
            else {
                val errorBody = response.errorBody()
                when {
                    response.isSuccessful -> Either.Right(transform((response.body()!!)))
                    (!response.isSuccessful && errorBody != null) -> {
                        when {
                            response.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> Either.Left(
                                Failure.ServerError(
                                    response.code(),
                                    "Your API key is incorrect"
                                )
                            )
                            response.code() != HttpURLConnection.HTTP_OK -> Either.Left(
                                Failure.ServerError(
                                    response.code(),
                                    "Payload is invalid"
                                )
                            )
                            else -> {
                                val moshi = Moshi.Builder().build()
                                val jsonAdapter = moshi.adapter(ErrorModel::class.java)
                                val errorModel = jsonAdapter.fromJson(errorBody.string())

                                val errorMessage = errorModel!!.msg
                                val errorCode = response.code()

                                Either.Left(Failure.ServerError(errorCode, errorMessage))
                            }
                        }

                    }
                    else -> Either.Left(Failure.NetworkConnection())
                }

            }
        } catch (exception: Throwable) {
            Either.Left(Failure.FeatureFailure(exception))
        }
    }


}