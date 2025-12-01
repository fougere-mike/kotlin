/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.time

/**
 * BrightScript-specific time mark for monotonic time measurements.
 * Wraps a millisecond reading from roTimespan.
 */
public class BrsTimeMark internal constructor(internal val readingMs: Double) : Comparable<BrsTimeMark> {

    /**
     * Returns the elapsed time since this time mark was recorded.
     */
    public fun elapsedNow(): Double = BrsMonotonicTimeSource.read() - readingMs

    /**
     * Returns a time mark that is [milliseconds] after this time mark.
     */
    public operator fun plus(milliseconds: Double): BrsTimeMark = BrsTimeMark(readingMs + milliseconds)

    /**
     * Returns a time mark that is [milliseconds] before this time mark.
     */
    public operator fun minus(milliseconds: Double): BrsTimeMark = BrsTimeMark(readingMs - milliseconds)

    /**
     * Returns the difference in milliseconds between this and [other] time mark.
     */
    public operator fun minus(other: BrsTimeMark): Double = readingMs - other.readingMs

    override fun compareTo(other: BrsTimeMark): Int = readingMs.compareTo(other.readingMs)

    override fun equals(other: Any?): Boolean = other is BrsTimeMark && readingMs == other.readingMs

    override fun hashCode(): Int = readingMs.hashCode()

    override fun toString(): String = "BrsTimeMark($readingMs ms)"
}

/**
 * BrightScript monotonic time source using roTimespan.
 * Provides monotonically increasing time measurements in milliseconds.
 */
public object BrsMonotonicTimeSource {

    /**
     * Returns the current time reading in milliseconds.
     * Uses roTimespan internally in BrightScript.
     */
    internal fun read(): Double = brsIntrinsicCurrentTimeMillis()

    /**
     * Returns a [BrsTimeMark] representing the current instant.
     */
    public fun markNow(): BrsTimeMark = BrsTimeMark(read())

    /**
     * Measures the time elapsed while executing [block] and returns the result in milliseconds.
     */
    public fun <T> measureTimeMillis(block: () -> T): Pair<T, Double> {
        val start = readTime()
        val result = block()
        val elapsed = readTime() - start
        return Pair(result, elapsed)
    }

    /**
     * Public accessor for reading current time.
     */
    public fun readTime(): Double = brsIntrinsicCurrentTimeMillis()

    override fun toString(): String = "BrsMonotonicTimeSource(roTimespan)"
}

/**
 * Intrinsic function to get current time in milliseconds.
 * This will be replaced by the BrightScript backend with a call to roTimespan.
 */
@PublishedApi
internal external fun brsIntrinsicCurrentTimeMillis(): Double
