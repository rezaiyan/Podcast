package com.hezaro.wall.data.utils

import com.hezaro.wall.data.model.ErrorModel
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import com.nhaarman.mockitokotlin2.given
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBeInstanceOf
import org.mockito.*
import retrofit2.Call
import retrofit2.Response
import kotlin.test.assertFalse

class BaseRepositoryBot {

    private var api = mock(ApiService::class)
    @Suppress("UNCHECKED_CAST")
    private var call = mock(Call::class) as Call<com.hezaro.wall.data.model.Response<UserInfo>>?
    @Suppress("UNCHECKED_CAST")
    private var response1 = mock(Response::class) as Response<com.hezaro.wall.data.model.Response<UserInfo>>?
    private var repository = BaseRepository()
    private val errorMessage = "اطلاعات برای اهراز هویت ارسال نشده است"
    val errorResponse = Response.error<ErrorModel>(
            ResponseBody.create(
                    MediaType.parse("application/json"),
                "{\"playStatus\":false,\"msg\":\"$errorMessage\",\"success\":false}"
            ),
        okhttp3.Response.Builder().request(Request.Builder().url("http://wall.hezaro.com").build()).code(401).protocol(
            Protocol.HTTP_1_1
        ).message("Error message test").build()
    )

    fun withThisResponse(response: Response<com.hezaro.wall.data.model.Response<UserInfo>>?): BaseRepositoryBot {
        this.response1 = response
        return this
    }

    fun withThisCall(call: Call<com.hezaro.wall.data.model.Response<UserInfo>>?): BaseRepositoryBot {
        this.call = call
        return this
    }


    fun loginReturnsThiscall(): BaseRepositoryBot {
        Mockito.`when`(api.login("id_token")).thenReturn(call)
        return this
    }

    fun withErrorBody(): BaseRepositoryBot {

        given { response1!!.errorBody() }.willReturn(errorResponse.errorBody())
        return this
    }


    fun executeCallReturns(): BaseRepositoryBot {
        given { call!!.execute() }.willReturn(response1!!)
        return this
    }
    @Suppress("RedundantLambdaArrow")
    fun verifyNullResponse() {
        val onResult =
            { _: com.hezaro.wall.data.model.Response<UserInfo> -> Either.Left(Failure.NetworkConnection()) }
        val result = repository.request(api.login("id_token"), onResult)

        result.either({ failure -> failure shouldBeInstanceOf Failure.NetworkConnection::class.java }, {})
    }
    @Suppress("RedundantLambdaArrow")
    fun verifyException() {
        val onResult =
            { _: com.hezaro.wall.data.model.Response<UserInfo> -> Either.Left(Failure.FeatureFailure(Throwable())) }
        val result = repository.request(api.login("id_token"), onResult)

        result.either({ failure -> failure shouldBeInstanceOf Failure.FeatureFailure::class.java }, {})
    }

    fun verifySuccessful() {
        val onResult = { userInfo: com.hezaro.wall.data.model.Response<UserInfo> -> Either.Right(userInfo) }
        val result = repository.request(api.login("id_token"), onResult)

        result.either({ }, { response -> response shouldBeInstanceOf Either.Right::class.java })
    }

    fun verifyUnsuccessful() {
        val onResult = { userInfo: com.hezaro.wall.data.model.Response<UserInfo> -> Either.Right(userInfo) }
        val result = repository.request(api.login("id_token"), onResult)

        result.either({ failure ->
            failure shouldBeInstanceOf Failure.ServerError::class.java
            assertFalse((failure as Failure.ServerError).message.isNullOrBlank())
//            failure.code shouldBe 401
        }, {})
    }
}