package com.hezaro.wall.data.utils

import com.nhaarman.mockitokotlin2.given
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.data.model.ErrorModel
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBeInstanceOf
import org.mockito.Mockito
import retrofit2.Call
import retrofit2.Response
import kotlin.test.assertFalse


class BaseRepositoryBot {

    private var api = mock(ApiService::class)
    @Suppress("UNCHECKED_CAST")
    private var call = mock(Call::class) as Call<Nothing>?
    @Suppress("UNCHECKED_CAST")
    private var loginResponse = mock(Response::class) as Response<Nothing>?
    private var repository = BaseRepository()
    private val errorMessage = "اطلاعات برای اهراز هویت ارسال نشده است"
    val errorResponse = Response.error<ErrorModel>(
            ResponseBody.create(
                    MediaType.parse("application/json"),
                    "{\"status\":false,\"msg\":\"$errorMessage\",\"success\":false}"
            ), okhttp3.Response.Builder().request(Request.Builder().url("").build()).code(401).protocol(Protocol.HTTP_1_1).message("Error message test").build())


    fun withThisResponse(response: Response<Nothing>?): BaseRepositoryBot {
        this.loginResponse = response
        return this
    }

    fun withThisCall(call: Call<Nothing>?): BaseRepositoryBot {
        this.call = call
        return this
    }


    fun loginReturnsThiscall(): BaseRepositoryBot {
        Mockito.`when`(api.explore()).thenReturn(call)
        return this
    }

    fun withErrorBody(): BaseRepositoryBot {

        given { loginResponse!!.errorBody() }.willReturn(errorResponse.errorBody())
        return this
    }


    fun executeCallReturns(): BaseRepositoryBot {
        given { call!!.execute() }.willReturn(loginResponse!!)
        return this
    }
    @Suppress("RedundantLambdaArrow")
    fun verifyNullResponse() {
        val onResult = { _: Nothing -> Either.Left(Failure.NetworkConnection()) }
        val result = repository.request(api.explore(), onResult)

        result.either({ failure -> failure shouldBeInstanceOf Failure.NetworkConnection::class.java }, {})
    }
    @Suppress("RedundantLambdaArrow")
    fun verifyException() {
        val onResult = {  _: Nothing -> Either.Left(Failure.FeatureFailure(Throwable())) }
        val result = repository.request(api.explore(), onResult)

        result.either({ failure -> failure shouldBeInstanceOf Failure.FeatureFailure::class.java }, {})
    }

    fun verifySuccessful() {
        val onResult = { login: Nothing -> Either.Right(login) }
        val result = repository.request(api.explore(), onResult)

        result.either({ }, { response -> response shouldBeInstanceOf Either.Right::class.java })
    }

    fun verifyUnsuccessful() {
        val onResult = { msMovie: Nothing -> Either.Right(msMovie) }
        val result = repository.request(api.explore(), onResult)

        result.either({ failure ->
            failure shouldBeInstanceOf Failure.ServerError::class.java
            assertFalse((failure as Failure.ServerError).message.isNullOrBlank())
//            failure.code shouldBe 401
        }, {})
    }
}