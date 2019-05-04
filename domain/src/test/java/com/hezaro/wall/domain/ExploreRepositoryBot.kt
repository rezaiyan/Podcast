package com.hezaro.wall.domain

import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Meta
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.amshove.kluent.`should equal`
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.mockito.*
import retrofit2.Response
import retrofit2.mock.Calls

class ExploreRepositoryBot {

    private var api = mock(ApiService::class)
    private var repository = ExploreRepository.ExploreRepositoryImpl(api)

    fun withInvalidResponse(page: Int, offset: Int, sort: String): ExploreRepositoryBot {
        val call = Calls.response(
            Response.error<com.hezaro.wall.data.model.Response<MutableList<Episode>>>(
                402,
                ResponseBody.create(MediaType.parse("json"), "")
            )
        )
        Mockito.`when`(api.explore(sort, page, offset)).thenReturn(call)
        return this
    }

    fun runAndVerifyUnsuccessful(page: Int, offset: Int, sort: String): ExploreRepositoryBot {
        val explore = repository.explore(page, offset, sort)
        explore shouldBeInstanceOf Either::class.java
        explore.isLeft shouldBe true
        explore.either({ failure ->
            failure shouldBeInstanceOf Failure.ServerError::class.java
            (failure as Failure.ServerError).code `should equal` 402
        }, {})
        return this
    }

    fun withValidResponse(page: Int, offset: Int, sort: String): ExploreRepositoryBot {
        val list = mutableListOf<Episode>()
        val call = Calls.response(Response.success(com.hezaro.wall.data.model.Response(Meta(200), list)))
        Mockito.`when`(api.explore(sort, page, offset)).thenReturn(call)
        return this
    }

    fun runAndVerifySuccessful(page: Int, offset: Int, sort: String): ExploreRepositoryBot {
        val explore = repository.explore(page, offset, sort)
        explore shouldBeInstanceOf Either::class.java
        explore.isRight shouldBe true
        explore.either({}, { response ->
            response.size `should equal` 0
        })
        return this
    }
}