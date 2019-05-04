package com.hezaro.wall.domain

import org.junit.*

class ExploreRepositoryTest {

    @Test
    fun `login request with incorrect payload and return server error`() {

        ExploreRepositoryBot()
            .withInvalidResponse(page = 0, offset = 0, sort = "")
            .runAndVerifyUnsuccessful(page = 0, offset = 0, sort = "")
    }

    @Test
    fun `login request with correct payload and return server error with successful response`() {
        ExploreRepositoryBot()
            .withValidResponse(page = 1, offset = 20, sort = "best")
            .runAndVerifySuccessful(page = 1, offset = 20, sort = "best")
    }
}