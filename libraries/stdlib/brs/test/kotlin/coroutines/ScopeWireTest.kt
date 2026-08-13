package test.coroutines

import kotlin.test.*
import kotlin.brs.ScopeClosedException
import kotlin.brs.ScopeRequest
import kotlin.brs.ScopeRequest1
import kotlin.brs.ScopeRequest2
import kotlin.brs.ScopeRequestException
import kotlin.brs.kotlinScopeTestRegistry
import kotlin.brs.scopeHandleOf
import kotlin.brs.roku.RoArray
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.strToI
import kotlin.brs.scope.allocateScopeRequestKey
import kotlin.brs.scope.buildScopeCancelEnvelope
import kotlin.brs.scope.buildScopeClosedOutcome
import kotlin.brs.scope.buildScopeErrorOutcome
import kotlin.brs.scope.buildScopeFailureOutcome
import kotlin.brs.scope.buildScopeRequestEnvelope
import kotlin.brs.scope.buildScopeValueOutcome
import kotlin.brs.scope.deepCopyAA
import kotlin.brs.scope.parseScopeEnvelope
import kotlin.brs.scope.scopeThrowableForOutcome
import kotlin.coroutines.builders.runBlocking
import kotlin.coroutines.cancellation.CancellationException

// Request declarations mirror the intended user shape: objects extending the
// abstract request classes with an explicit wire-name constructor argument.
// WireProbe/WireProbeDup share a name on purpose (duplicate-registration test).
object WireProbe : ScopeRequest<Int>("wireProbe")
object WireProbeDup : ScopeRequest<String>("wireProbe")
object BadHashName : ScopeRequest<Int>("bad#name")
object Arity1Probe : ScopeRequest1<Int, Int>("arity1Probe")
object Arity2Probe : ScopeRequest2<Int, Int, Int>("arity2Probe")

fun TestRunner.scopeWireTests() {
    suite("ScopeHandle wire protocol") {

        test("ScopeClosedException is a CancellationException") {
            // Constructed through the real child-side mapping (internal ctor).
            val env = parseScopeEnvelope(buildScopeClosedOutcome("u#1"))
            val t = scopeThrowableForOutcome(env)
            assertTrue(t is ScopeClosedException)
            assertTrue(t is CancellationException)
        }

        test("ScopeRequestException carries message, number, backtrace") {
            val e = ScopeRequestException("boom", 77, null)
            assertEquals("boom", e.message)
            assertEquals(77, e.number)
            assertTrue(e.backtrace == null)
        }

        test("request objects expose their wire names") {
            assertEquals("wireProbe", WireProbe.name)
            assertEquals("arity1Probe", Arity1Probe.name)
            assertEquals("arity2Probe", Arity2Probe.name)
        }

        test("request keys are unique with a monotonic uuid#n shape") {
            val k1 = allocateScopeRequestKey()
            val k2 = allocateScopeRequestKey()
            assertTrue(k1 != k2)
            assertTrue(k1.contains("#"))
            assertTrue(k2.contains("#"))
            val hash1 = k1.indexOf("#")
            val hash2 = k2.indexOf("#")
            // One UUID per caller instance: same prefix on both keys.
            assertEquals(k1.substring(0, hash1), k2.substring(0, hash2))
            val n1 = strToI(k1.substring(hash1 + 1))
            val n2 = strToI(k2.substring(hash2 + 1))
            assertTrue(n1 >= 1)
            assertEquals(n1 + 1, n2)
        }

        test("envelope round-trip: request") {
            val replyTo = RoSGNode.create("Node")
            val args = RoArray.create(0, true)
            args.push(41)
            val captures = RoAssociativeArray.create()
            captures.addReplace("c", "v")
            val aa = buildScopeRequestEnvelope("u#2", replyTo, "wireProbe", args, captures)
            val env = parseScopeEnvelope(aa)
            assertEquals("request", env.kind)
            assertEquals("u#2", env.key)
            assertEquals("wireProbe", env.name)
            val gotReply = env.replyTo
            assertTrue(gotReply != null)
            assertTrue(replyTo.isSameNode(gotReply!!))
            assertEquals(1, env.args?.count())
            assertEquals("v", "${env.captures?.lookup("c")}")
        }

        test("envelope round-trip: cancel") {
            val env = parseScopeEnvelope(buildScopeCancelEnvelope("u#3"))
            assertEquals("cancel", env.kind)
            assertEquals("u#3", env.key)
        }

        test("envelope round-trip: outcome value") {
            val env = parseScopeEnvelope(buildScopeValueOutcome("u#4", 42))
            assertEquals("outcome", env.kind)
            assertEquals("u#4", env.key)
            assertEquals("value", env.status)
            assertTrue((env.value as? Int) == 42)
            assertTrue(scopeThrowableForOutcome(env) == null)
        }

        test("envelope round-trip: outcome error") {
            val env = parseScopeEnvelope(buildScopeErrorOutcome("u#5", "bad", 12, null))
            assertEquals("outcome", env.kind)
            assertEquals("error", env.status)
            assertEquals("bad", env.message)
            assertEquals(12, env.number)
            val t = scopeThrowableForOutcome(env)
            assertTrue(t is ScopeRequestException)
            val sre = t as ScopeRequestException
            assertEquals("bad", sre.message)
            assertEquals(12, sre.number)
        }

        test("envelope round-trip: outcome closed") {
            val env = parseScopeEnvelope(buildScopeClosedOutcome("u#6"))
            assertEquals("outcome", env.kind)
            assertEquals("closed", env.status)
            assertTrue(scopeThrowableForOutcome(env) is ScopeClosedException)
        }

        test("failure marshalling round-trip preserves original message and number") {
            // Throwable -> outcome AA -> parse -> reconstructed exception.
            val original = ScopeRequestException("kaput", 99, null)
            val aa = buildScopeFailureOutcome("u#7", original)
            val t = scopeThrowableForOutcome(parseScopeEnvelope(aa))
            assertTrue(t is ScopeRequestException)
            val sre = t as ScopeRequestException
            assertEquals("kaput", sre.message)
            assertEquals(99, sre.number)
        }

        test("unknown envelope kind parses to an ignorable result") {
            val aa = RoAssociativeArray.create()
            aa.addReplace("kind", "future-flow-kind")
            aa.addReplace("key", "u#8")
            val env = parseScopeEnvelope(aa)
            assertEquals("future-flow-kind", env.kind)
            assertTrue(env.kind != "request")
            assertTrue(env.kind != "cancel")
            assertTrue(env.kind != "outcome")
        }

        test("deepCopyAA: nested mutation on the copy is invisible to the original") {
            val inner = RoAssociativeArray.create()
            inner.addReplace("marker", 1)
            val src = RoAssociativeArray.create()
            src.addReplace("nested", inner)
            val copy = deepCopyAA(src) as? RoAssociativeArray
            assertTrue(copy != null)
            val copyNested = copy!!.lookup("nested") as? RoAssociativeArray
            assertTrue(copyNested != null)
            copyNested!!.addReplace("marker", 2)
            assertTrue((inner.lookup("marker") as? Int) == 1)
        }

        test("deepCopyAA: arrays copied, node refs copied by reference") {
            val node = RoSGNode.create("Node")
            val arr = RoArray.create(0, true)
            arr.push("x")
            val src = RoAssociativeArray.create()
            src.addReplace("list", arr)
            src.addReplace("theNode", node)
            val copy = deepCopyAA(src) as? RoAssociativeArray
            assertTrue(copy != null)
            val copyList = copy!!.lookup("list") as? RoArray
            assertTrue(copyList != null)
            copyList!!.push("y")
            // Copied array grew; the original did not.
            assertEquals(2, copyList.count())
            assertEquals(1, arr.count())
            val copyNode = copy.lookup("theNode") as? RoSGNode
            assertTrue(copyNode != null)
            assertTrue(node.isSameNode(copyNode!!))
        }

        test("run(request) outside component context throws IllegalStateException") {
            val handle = scopeHandleOf(RoSGNode.create("Node"))
            var thrown: Throwable? = null
            var r = 0
            runBlocking {
                try {
                    r = handle.run(WireProbe)
                } catch (e: Throwable) {
                    thrown = e
                }
            }
            assertEquals(0, r)
            assertTrue(thrown is IllegalStateException)
            assertTrue("${thrown?.message}".contains("render-thread component context"))
        }

        test("registry accepts handlers of all arities") {
            val reg = kotlinScopeTestRegistry()
            reg.handle(WireProbe) { 1 }
            reg.handle(Arity1Probe) { a -> a }
            reg.handle(Arity2Probe) { a, b -> a + b }
        }

        test("registry rejects duplicate request names") {
            val reg = kotlinScopeTestRegistry()
            reg.handle(WireProbe) { 1 }
            var thrown: Throwable? = null
            try {
                reg.handle(WireProbeDup) { "x" }
            } catch (e: Throwable) {
                thrown = e
            }
            assertTrue(thrown is IllegalStateException)
            assertTrue("${thrown?.message}".contains("registered twice"))
        }

        test("registry rejects '#' in request names") {
            val reg = kotlinScopeTestRegistry()
            var thrown: Throwable? = null
            try {
                reg.handle(BadHashName) { 1 }
            } catch (e: Throwable) {
                thrown = e
            }
            assertTrue(thrown is IllegalStateException)
            assertTrue("${thrown?.message}".contains("may not contain '#'"))
        }
    }
}
