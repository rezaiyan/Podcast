package com.hezaro.wall.data.base

import com.hezaro.wall.sdk.test.UnitTest
import kotlinx.coroutines.runBlocking
import org.junit.*

class BaseRepositoryTest : UnitTest() {


    @Test
    fun test_with_null_response() {

        runBlocking {
            BaseRepositoryBot()
                    .withThisResponse(null)
                    .loginReturnsThiscall()
                    .verifyNullResponse()
        }

    }

    @Test
    fun test_with_null_call_occurred_exception() {

        runBlocking {
            BaseRepositoryBot()
                    .withThisCall(null)
                    .loginReturnsThiscall()
                    .verifyException()
        }
    }

    @Test
    fun `test with successful response`(){
        runBlocking {
            BaseRepositoryBot()
                    .verifySuccessful()
        }
    }

    @Test
    fun `test with unsuccessful response`(){
        runBlocking {
            BaseRepositoryBot()
                    .withErrorBody()
                    .loginReturnsThiscall()
                    .executeCallReturns()
                    .verifyUnsuccessful()
        }
    }

}