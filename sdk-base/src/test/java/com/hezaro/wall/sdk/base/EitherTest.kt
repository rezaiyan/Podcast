package com.hezaro.wall.sdk.base

import com.hezaro.wall.sdk.base.Either.Left
import com.hezaro.wall.sdk.base.Either.Right
import com.hezaro.wall.sdk.test.AndroidTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqualTo
import org.junit.Test

class EitherTest : AndroidTest() {

    @Test fun `Either Right should return correct type`() {
        val result = Right("wall")

        result shouldBeInstanceOf Either::class.java
        result.isRight shouldBe true
        result.isLeft shouldBe false
        result.either({},
                { right ->
                    right shouldBeInstanceOf String::class.java
                    right shouldEqualTo "wall"
                })
    }

    @Test fun `Either Left should return correct type`() {
        val result = Left("wall")

        result shouldBeInstanceOf Either::class.java
        result.isLeft shouldBe true
        result.isRight shouldBe false
        result.either(
                { left ->
                    left shouldBeInstanceOf String::class.java
                    left shouldEqualTo "wall"
                }, {})
    }
}