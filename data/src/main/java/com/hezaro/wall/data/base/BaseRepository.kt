package com.hezaro.wall.data.base

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.hezaro.wall.data.model.ErrorBody
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
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
            val errorBody = response.errorBody()?.string()
            if (response == null)
                Either.Left(Failure.NetworkConnection())
            else {
                when {
                    response.isSuccessful -> Either.Right(transform((response.body()!!)))
                    (response.isSuccessful.not()) -> {
                        when {
                            response.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR && response.code() != HttpURLConnection.HTTP_GATEWAY_TIMEOUT ->
                                Either.Left(
                                    Failure.ServerError(
                                        response.code(),
                                        "خطایی از سمت سرور پیش آمده, بزودی رفع میشود"
                                    )
                                )
                            response.code() == HttpURLConnection.HTTP_GATEWAY_TIMEOUT -> Either.Left(Failure.NetworkConnection())
                            errorBody.isNullOrEmpty() -> {
                                Either.Left(Failure.FeatureFailure(response.raw().code(), ""))
                            }
                            else -> {
                                val type = object : TypeToken<ErrorBody>() {}.type
                                val errorModel = Gson().fromJson<ErrorBody>(errorBody, type)
                                val errorMessage = errorModel!!.meta.message
                                val errorCode = errorModel.meta.status

                                Either.Left(Failure.FeatureFailure(errorCode, errorMessage))
                            }
                        }
                    }
                    else -> Either.Left(Failure.NetworkConnection())
                }
            }
        } catch (exception: Throwable) {
            Either.Left(Failure.ExceptionFailure(exception))
        }
    }
}