package com.samdev.heartbeat

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.SecureRandom
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {

    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private val STRING_LENGTH = 12
    private val ALPHANUMERIC_REGEX = "[a-zA-Z0-9]+";

    @Test
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
    }


    // speed decreased with larger STRING_LENGTH, but extremely fast for smaller STRING_LENGTH
    @Test
    fun givenAStringLength_usingStringBuilder_returnValidRandomString() {
        val random = Random()
        val sb = StringBuilder(STRING_LENGTH)
        for (i in 0 until STRING_LENGTH) sb.append(HeartbeatController.RANGE[random.nextInt(HeartbeatController.RANGE.length)])
        val randomString =  sb.toString()

        println(randomString)
        assertEquals(STRING_LENGTH, randomString.length)
    }


    // speed increases with larger STRING_LENGTH
    @Test
    fun givenAStringLength_usingCollections_returnValidRandomString() {
        val randomString = (1..STRING_LENGTH)
                .map { kotlin.random.Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")

        println(randomString)

        assert(randomString.matches(Regex(ALPHANUMERIC_REGEX)))
        assertEquals(STRING_LENGTH, randomString.length)
    }
}