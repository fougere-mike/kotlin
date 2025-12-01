/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.time

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

@Suppress("ACTUAL_WITHOUT_EXPECT") // visibility
internal actual typealias ValueTimeMarkReading = Any

internal interface DefaultTimeSource : TimeSource.WithComparableMarks {
    override fun markNow(): ValueTimeMark
    fun elapsedFrom(timeMark: ValueTimeMark): Duration
    fun differenceBetween(one: ValueTimeMark, another: ValueTimeMark): Duration
    fun adjustReading(timeMark: ValueTimeMark, duration: Duration): ValueTimeMark
}

@SinceKotlin("1.3")
internal actual object MonotonicTimeSource : DefaultTimeSource, TimeSource.WithComparableMarks {

    // BrightScript uses roTimespan for monotonic time measurements
    // The reading is stored as milliseconds (Double)

    private fun read(): Double = brsIntrinsicCurrentTimeMillis()

    actual override fun markNow(): ValueTimeMark = ValueTimeMark(read())

    actual override fun elapsedFrom(timeMark: ValueTimeMark): Duration =
        (read() - timeMark.reading as Double).milliseconds

    actual override fun differenceBetween(one: ValueTimeMark, another: ValueTimeMark): Duration {
        val ms1 = one.reading as Double
        val ms2 = another.reading as Double
        return if (ms1 == ms2) Duration.ZERO else (ms1 - ms2).milliseconds
    }

    actual override fun adjustReading(timeMark: ValueTimeMark, duration: Duration): ValueTimeMark =
        ValueTimeMark(sumCheckNaN(timeMark.reading as Double + duration.toDouble(DurationUnit.MILLISECONDS)))

    override fun toString(): String = "TimeSource(BrightScript.roTimespan)"
}

private fun sumCheckNaN(value: Double): Double = value.also {
    if (it.isNaN()) throw IllegalArgumentException("Summing infinities of different signs")
}

// Intrinsic function to get current time in milliseconds
// This will be replaced by the BrightScript backend with a call to roTimespan or similar
@PublishedApi
internal external fun brsIntrinsicCurrentTimeMillis(): Double
