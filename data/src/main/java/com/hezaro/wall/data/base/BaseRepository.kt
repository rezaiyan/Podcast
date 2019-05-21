package com.hezaro.wall.data.base

import com.google.gson.JsonParseException
import com.hezaro.wall.data.model.ErrorModel
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import com.squareup.moshi.Moshi
import retrofit2.Call
import java.net.HttpURLConnection

/**
 * Wrapper to API call for concrete repositories
 */
// TODO : This class has to has an abstraction
open class BaseRepository {

    /**
     * @param T     The response type of [call]
     * @param R     The return type of method if is successful and return [Either.Right]
     *
     * Executes [call] and return a result as a [Either]
     * The [Either] can has whether [R] or [Failure], that wrapped inside of itself.
     * In some situations such as [JsonParseException] the catch block is calling.
     *
     * @param call        Is a [Call] to execution
     * @param transform   Is a transformation function
     * @return [Either]   if api call is successful is [Either.Right] else [Either.Left]
     */
    //TODO : Error handling is not complete.

    @Suppress("SENSELESS_COMPARISON")
    inline fun <T, R> request(call: Call<T>, transform: (T) -> R): Either<Failure, R> {

        return try {

            val response = call.execute()

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
                                    "خطایی از سمت سرور پیش آمده, بزودی رفع میشود"
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