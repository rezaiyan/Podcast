package com.hezaro.wall.domain

import com.hezaro.wall.data.local.EpisodeDao
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
    private var database = mock(EpisodeDao::class)
    private var repository = EpisodesRepository.EpisodesRepositoryImpl(api, database)

    fun withInvalidResponse(page: Int, offset: Int, sort: String): ExploreRepositoryBot {
        val call = Calls.response(
            Response.error<com.hezaro.wall.data.model.Response<ArrayList<Episode>>>(
                402,
                ResponseBody.create(MediaType.parse("json"), "")
            )
        )
        Mockito.`when`(api.episodes(sort, page, offset)).thenReturn(call)
        return this
    }

    fun runAndVerifyUnsuccessful(page: Int, offset: Int, sort: String): ExploreRepositoryBot {
        val either = repository.episodes(page, offset, sort)
        either shouldBeInstanceOf Either::class.java
        either.isLeft shouldBe true
        either.either({ failure ->
            failure shouldBeInstanceOf Failure.ServerError::class.java
            (failure as Failure.ServerError).code `should equal` 402
        }, {})
        return this
    }

    fun withValidResponse(page: Int, offset: Int, sort: String): ExploreRepositoryBot {
        val list = arrayListOf<Episode>()
        val call = Calls.response(Response.success(com.hezaro.wall.data.model.Response(Meta(200, "OK"), list)))
        Mockito.`when`(api.episodes(sort, page, offset)).thenReturn(call)
        return this
    }

    fun runAndVerifySuccessful(page: Int, offset: Int, sort: String): ExploreRepositoryBot {
        val either = repository.episodes(page, offset, sort)
        either shouldBeInstanceOf Either::class.java
        either.isRight shouldBe true
        either.either({}, { response ->
            response.size `should equal` 0
        })
        return this
    }
}