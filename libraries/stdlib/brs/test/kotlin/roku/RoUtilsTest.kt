/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.roku

import kotlin.brs.asDynamic
import kotlin.brs.roku.*
import kotlin.test.*

/**
 * Tests for the roUtils bindings (OS 15.0 reference APIs).
 *
 * This suite runs on the MAIN thread (runBlocking regime), where the
 * SetRef/GetRef node APIs legitimately no-op or fail — so it exercises only
 * roUtils, which has no render-thread restriction. The SetRef/GetRef/Move*
 * node bindings get their device coverage from the render-thread E2E suites
 * that build on them.
 */
fun TestRunner.roUtilsTests() {
    suite("RoUtils (OS 15 reference APIs)") {
        test("create returns a live roUtils object") {
            val utils = RoUtils.create()
            assertNotNull(utils, "CreateObject(\"roUtils\") should not be invalid on OS 15+")
        }

        test("isSameObject is true for the same AA instance") {
            val utils = RoUtils.create()
            val aa = RoAssociativeArray.create()
            aa.addReplace("key", "value")
            assertTrue(utils.isSameObject(aa.asDynamic(), aa.asDynamic()), "An AA should be the same object as itself")
        }

        test("deepCopy yields a distinct object") {
            val utils = RoUtils.create()
            val aa = RoAssociativeArray.create()
            aa.addReplace("key", "value")
            val copy = utils.deepCopy(aa.asDynamic())
            assertNotNull(copy, "deepCopy should return a value")
            assertFalse(utils.isSameObject(aa.asDynamic(), copy), "A deep copy should not be the same object as its source")
        }
    }
}
