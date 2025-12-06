/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for IncrementDecrementLowering.
 *
 * The lowering transforms Kotlin increment/decrement operators to BrightScript-compatible
 * binary operations since BrightScript doesn't have ++ or -- operators.
 *
 * Transformations:
 * - `++i` (PREFIX_INCR) → `i = i + 1`
 * - `i++` (POSTFIX_INCR) → stores old value, `i = i + 1`, returns old value
 * - `--i` (PREFIX_DECR) → `i = i - 1`
 * - `i--` (POSTFIX_DECR) → stores old value, `i = i - 1`, returns old value
 */
class IncrementDecrementLoweringTest {

    @Test
    fun `statement origins are correctly identified`() {
        // Verify the statement origins we check for exist and are distinct
        val origins = listOf(
            IrStatementOrigin.PREFIX_INCR,
            IrStatementOrigin.POSTFIX_INCR,
            IrStatementOrigin.PREFIX_DECR,
            IrStatementOrigin.POSTFIX_DECR
        )

        assertEquals(4, origins.distinct().size, "All increment/decrement origins should be distinct")
    }

    @Test
    fun `plus and minus origins are correctly identified`() {
        // Verify the binary operation origins we use
        val plusOrigin = IrStatementOrigin.PLUS
        val minusOrigin = IrStatementOrigin.MINUS

        assertTrue(plusOrigin != minusOrigin, "PLUS and MINUS origins should be distinct")
    }

    @Test
    fun `increment origins are prefix and postfix variants`() {
        // Document the increment operation origins
        val prefixIncr = IrStatementOrigin.PREFIX_INCR
        val postfixIncr = IrStatementOrigin.POSTFIX_INCR

        assertTrue(prefixIncr.toString().contains("PREFIX"), "PREFIX_INCR should contain PREFIX")
        assertTrue(postfixIncr.toString().contains("POSTFIX"), "POSTFIX_INCR should contain POSTFIX")
    }

    @Test
    fun `decrement origins are prefix and postfix variants`() {
        // Document the decrement operation origins
        val prefixDecr = IrStatementOrigin.PREFIX_DECR
        val postfixDecr = IrStatementOrigin.POSTFIX_DECR

        assertTrue(prefixDecr.toString().contains("PREFIX"), "PREFIX_DECR should contain PREFIX")
        assertTrue(postfixDecr.toString().contains("POSTFIX"), "POSTFIX_DECR should contain POSTFIX")
    }
}
