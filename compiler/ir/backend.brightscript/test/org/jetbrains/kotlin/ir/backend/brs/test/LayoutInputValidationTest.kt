/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.test

import org.jetbrains.kotlin.ir.backend.brs.LayoutInputValidation
import org.jetbrains.kotlin.ir.backend.brs.NodeEntryInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for the pure static-layout input validation (spec §5.9c) — the
 * IR-free half of the extractor's required-input work: findings for a layout
 * child missing a constructor input, and the `__kotlinInputsReady="true"`
 * marker attribute for satisfied input-bearing children.
 *
 * NOTE: `./run-compiler-tests.sh` filters `*GoldenFile*` only and does NOT run
 * this class; run it explicitly:
 * `./gradlew :compiler:backend.brightscript:test --tests "*LayoutInputValidation*" --no-configuration-cache -Dorg.gradle.dependency.verification=off`
 */
class LayoutInputValidationTest {
    private val required = mapOf("AiringDetailsScreen" to listOf("airingId"))

    @Test
    fun satisfiedChildGetsTheMarker() {
        val nodes = listOf(NodeEntryInfo("AiringDetailsScreen", "d", mapOf("airingId" to "123"), emptyList()))
        assertTrue(LayoutInputValidation.validate("Owner", nodes, required).isEmpty())
        val marked = LayoutInputValidation.withInputMarkers(nodes, required)
        assertEquals("true", marked[0].attributes["__kotlinInputsReady"])
        // The marker never displaces the input itself.
        assertEquals("123", marked[0].attributes["airingId"])
    }

    @Test
    fun missingInputIsAFindingAndNoMarker() {
        val nodes = listOf(NodeEntryInfo("AiringDetailsScreen", "d", emptyMap(), emptyList()))
        val findings = LayoutInputValidation.validate("Owner", nodes, required)
        assertEquals(1, findings.size)
        assertEquals("airingId", findings[0].missingInput)
        assertEquals("Owner", findings[0].ownerComponent)
        assertEquals("d", findings[0].childId)
        assertEquals("AiringDetailsScreen", findings[0].childType)
        assertTrue(findings[0].message().contains("construct AiringDetailsScreen in code"))
        assertTrue(LayoutInputValidation.withInputMarkers(nodes, required)[0].attributes.isEmpty())
    }

    @Test
    fun nestedChildrenAreCheckedAndUnknownTypesIgnored() {
        val inner = NodeEntryInfo("AiringDetailsScreen", "inner", emptyMap(), emptyList())
        val nodes = listOf(
            NodeEntryInfo("LayoutGroup", "g", emptyMap(), listOf(inner)),
            NodeEntryInfo("Label", "l", emptyMap(), emptyList()),
        )
        val findings = LayoutInputValidation.validate("Owner", nodes, required)
        assertEquals(listOf("inner"), findings.map { it.childId })
    }

    @Test
    fun nestedSatisfiedChildIsMarkedAndNonInputTypesAreNot() {
        val inner = NodeEntryInfo("AiringDetailsScreen", "inner", mapOf("airingId" to "7"), emptyList())
        val nodes = listOf(NodeEntryInfo("LayoutGroup", "g", emptyMap(), listOf(inner)))
        val marked = LayoutInputValidation.withInputMarkers(nodes, required)
        // The container (not an input-bearing type) gets no marker; its child does.
        assertTrue(marked[0].attributes.isEmpty())
        assertEquals("true", marked[0].children[0].attributes["__kotlinInputsReady"])
    }

    @Test
    fun everyMissingInputIsItsOwnFinding() {
        val twoInputs = mapOf("Pair" to listOf("a", "b"))
        val nodes = listOf(NodeEntryInfo("Pair", "p", mapOf("a" to "1"), emptyList()))
        val findings = LayoutInputValidation.validate("Owner", nodes, twoInputs)
        assertEquals(listOf("b"), findings.map { it.missingInput })
        // Partially satisfied → no marker (the gate must stay closed).
        assertTrue(LayoutInputValidation.withInputMarkers(nodes, twoInputs)[0].attributes.keys.none { it == "__kotlinInputsReady" })
    }
}
