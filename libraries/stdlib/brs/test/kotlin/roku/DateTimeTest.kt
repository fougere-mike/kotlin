/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.roku

import kotlin.brs.roku.*
import kotlin.test.*

/**
 * Tests for Roku DateTime APIs.
 */
fun TestRunner.dateTimeTests() {
    suite("DateTime") {
        test("RoDateTime creation") {
            val dt = RoDateTime.create()
            // Just verify it can be created
            assertTrue(true)
        }

        test("RoDateTime mark") {
            val dt = RoDateTime.create()
            dt.mark()
            val seconds = dt.asSeconds()
            // Should be a reasonable epoch time (after year 2000)
            assertTrue(seconds > 946684800)  // Jan 1, 2000
        }

        test("RoDateTime components") {
            val dt = RoDateTime.create()
            dt.mark()

            val year = dt.getYear()
            val month = dt.getMonth()
            val day = dt.getDayOfMonth()

            // Basic sanity checks
            assertTrue(year >= 2024)
            assertTrue(month in 1..12)
            assertTrue(day in 1..31)
        }

        test("RoDateTime time components") {
            val dt = RoDateTime.create()
            dt.mark()

            val hours = dt.getHours()
            val minutes = dt.getMinutes()
            val seconds = dt.getSeconds()

            assertTrue(hours in 0..23)
            assertTrue(minutes in 0..59)
            assertTrue(seconds in 0..59)
        }

        test("RoDateTime milliseconds") {
            val dt = RoDateTime.create()
            dt.mark()

            val ms = dt.getMilliseconds()
            assertTrue(ms in 0..999)
        }

        test("RoDateTime day of week") {
            val dt = RoDateTime.create()
            dt.mark()

            val dow = dt.getDayOfWeek()
            assertTrue(dow in 0..6)
        }

        test("RoTimespan creation") {
            val ts = RoTimespan.create()
            // Just verify it can be created
            assertTrue(true)
        }

        test("RoTimespan mark and measure") {
            val ts = RoTimespan.create()
            ts.mark()

            // Do some work
            var sum = 0
            for (i in 1..1000) {
                sum += i
            }

            val elapsed = ts.totalMilliseconds()
            // Should have taken some time (at least 0ms)
            assertTrue(elapsed >= 0)
        }

        test("RoTimespan totalSeconds") {
            val ts = RoTimespan.create()
            ts.mark()

            val seconds = ts.totalSeconds()
            assertTrue(seconds >= 0)
        }

        test("multiple timespans") {
            val ts1 = RoTimespan.create()
            val ts2 = RoTimespan.create()

            ts1.mark()

            // Do some work
            var sum = 0
            for (i in 1..1000) {
                sum += i
            }

            ts2.mark()

            val elapsed1 = ts1.totalMilliseconds()
            val elapsed2 = ts2.totalMilliseconds()

            // ts1 should have more elapsed time than ts2
            assertTrue(elapsed1 >= elapsed2)
        }
    }
}
