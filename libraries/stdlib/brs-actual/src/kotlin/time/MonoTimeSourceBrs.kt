/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.time

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

/**
 * The reading type for BrightScript time marks (stored as milliseconds).
 */
@Suppress("ACTUAL_WITHOUT_EXPECT")
internal actual typealias ValueTimeMarkReading = Any

/**
 * BrightScript monotonic time source implementation using roTimespan.
 * roTimespan provides millisecond-resolution monotonic timing in BrightScript.
 */
@SinceKotlin("1.3")
internal actual object MonotonicTimeSource : TimeSource.WithComparableMarks {

    /**
     * Reads the current time in milliseconds from roTimespan.
     * This intrinsic will be lowered by the BRS backend to create an roTimespan
     * object and call TotalMilliseconds().
     */
    private fun read(): Double = brsIntrinsicCurrentTimeMillis()

    actual override fun markNow(): ValueTimeMark = ValueTimeMark(read())

    actual fun elapsedFrom(timeMark: ValueTimeMark): Duration =
        (read() - timeMark.reading as Double).milliseconds

    actual fun differenceBetween(one: ValueTimeMark, another: ValueTimeMark): Duration {
        val ms1 = one.reading as Double
        val ms2 = another.reading as Double
        return if (ms1 == ms2) Duration.ZERO else (ms1 - ms2).milliseconds
    }

    actual fun adjustReading(timeMark: ValueTimeMark, duration: Duration): ValueTimeMark =
        ValueTimeMark(sumCheckNaN(timeMark.reading as Double + duration.toDouble(DurationUnit.MILLISECONDS)))

    override fun toString(): String = "TimeSource(roTimespan)"
}

private fun sumCheckNaN(value: Double): Double =
    value.also { if (it.isNaN()) throw IllegalArgumentException("Summing infinities of different signs") }

/**
 * Intrinsic function to get current monotonic time in milliseconds.
 * This will be replaced by the BrightScript backend with a call to roTimespan.TotalMilliseconds().
 *
 * BrightScript implementation:
 * ```brightscript
 * function brsIntrinsicCurrentTimeMillis() as Double
 *     timespan = CreateObject("roTimespan")
 *     return timespan.TotalMilliseconds()
 * end function
 * ```
 */
@PublishedApi
internal external fun brsIntrinsicCurrentTimeMillis(): Double
