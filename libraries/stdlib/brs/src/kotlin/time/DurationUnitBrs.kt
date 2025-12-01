/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.time

/**
 * BrightScript-specific time unit enum for duration conversions.
 * This is separate from kotlin.time.DurationUnit to avoid conflicts during bootstrap compilation.
 */
public enum class BrsDurationUnit(internal val scale: Double) {
    /**
     * Time unit representing one nanosecond, which is 1/1000 of a microsecond.
     */
    NANOSECONDS(1e0),
    /**
     * Time unit representing one microsecond, which is 1/1000 of a millisecond.
     */
    MICROSECONDS(1e3),
    /**
     * Time unit representing one millisecond, which is 1/1000 of a second.
     */
    MILLISECONDS(1e6),
    /**
     * Time unit representing one second.
     */
    SECONDS(1e9),
    /**
     * Time unit representing one minute.
     */
    MINUTES(60e9),
    /**
     * Time unit representing one hour.
     */
    HOURS(3600e9),
    /**
     * Time unit representing one day, which is always equal to 24 hours.
     */
    DAYS(86400e9);
}

internal fun convertBrsDurationUnit(value: Double, sourceUnit: BrsDurationUnit, targetUnit: BrsDurationUnit): Double {
    val sourceCompareTarget = sourceUnit.scale.compareTo(targetUnit.scale)
    return when {
        sourceCompareTarget > 0 -> value * (sourceUnit.scale / targetUnit.scale)
        sourceCompareTarget < 0 -> value / (targetUnit.scale / sourceUnit.scale)
        else -> value
    }
}

internal fun convertBrsDurationUnitOverflow(value: Long, sourceUnit: BrsDurationUnit, targetUnit: BrsDurationUnit): Long {
    val sourceCompareTarget = sourceUnit.scale.compareTo(targetUnit.scale)
    return when {
        sourceCompareTarget > 0 -> value * (sourceUnit.scale / targetUnit.scale).toLong()
        sourceCompareTarget < 0 -> value / (targetUnit.scale / sourceUnit.scale).toLong()
        else -> value
    }
}

internal fun convertBrsDurationUnit(value: Long, sourceUnit: BrsDurationUnit, targetUnit: BrsDurationUnit): Long {
    val sourceCompareTarget = sourceUnit.scale.compareTo(targetUnit.scale)
    return when {
        sourceCompareTarget > 0 -> {
            val scale = (sourceUnit.scale / targetUnit.scale).toLong()
            val result = value * scale
            when {
                result / scale == value -> result
                value > 0 -> Long.MAX_VALUE
                else -> Long.MIN_VALUE
            }
        }
        sourceCompareTarget < 0 -> value / (targetUnit.scale / sourceUnit.scale).toLong()
        else -> value
    }
}
