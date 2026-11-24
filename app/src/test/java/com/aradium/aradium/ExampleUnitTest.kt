package com.aradium.aradium

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun formatTime_isCorrect() {
        assertEquals("1:05", formatTime(65000))
        assertEquals("0:00", formatTime(0))
    }
}
