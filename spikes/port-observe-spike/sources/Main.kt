// Port-observe spike (Spike A): prove that SceneGraph field changes made on the
// RENDER thread (and a TASK thread) are delivered as roSGNodeEvents to the MAIN
// thread's message port via the PORT form of observeField/observeFieldScoped,
// waking port.waitMessage() while main() never returns.
package com.nuvyyo.roku

import kotlin.brs.BrsInline
import kotlin.brs.roku.*

// The stdlib has NO port-form observeField API yet (SceneGraph.kt has the
// function-name forms only) — @BrsInline shims splice the raw BrightScript
// port-form calls at the call site. Do NOT promote these to stdlib API here.
@BrsInline("return node.observeField(fieldName, port)")
private external fun observeFieldWithPort(node: RoSGNode, fieldName: String, port: RoMessagePort): Boolean

@BrsInline("return node.observeFieldScoped(fieldName, port)")
private external fun observeFieldScopedWithPort(node: RoSGNode, fieldName: String, port: RoMessagePort): Boolean

private fun check(name: String, pass: Boolean, detail: String) {
    if (pass) {
        println("SPIKE|PASS|$name|$detail")
    } else {
        println("SPIKE|FAIL|$name|$detail")
    }
}

fun main() {
    val dt = RoDateTime.create()
    dt.mark()
    println("===SPIKE_SENTINEL_${dt.asSeconds()}===")
    println("SPIKE|START|port-observe-spike")

    val screen = RoSGScreen.create()
    val port = RoMessagePort.create()
    screen.setMessagePort(port)
    val scene = screen.createScene("SpikeScene")
    screen.show()

    // Fixture component; its init (render thread) starts a 0.5s Timer.
    val probe = scene.createChild("PortProbe")
    probe.setField("id", "probe1")

    // PORT-form observers, attached from the main thread.
    val okEcho = observeFieldWithPort(probe, "echo", port)
    val okTimer = observeFieldWithPort(probe, "timerOut", port)
    val okInput = observeFieldScopedWithPort(probe, "input", port)
    println("SPIKE|observe_returns|echo=$okEcho|timerOut=$okTimer|inputScoped=$okInput")

    // First rendezvous: main-thread set -> render-thread onChange -> echo event.
    probe.setField("input", "ping1")

    var gotEcho1 = false
    var gotEcho2 = false
    var gotTimer = false
    var gotInput1 = false
    var gotInput2 = false
    var gotTask = false
    var sentPing2 = false
    var taskStarted = false
    var accessorsChecked = false
    var accessorNodeInfo = "none"
    var accessorSubtype = "none"
    var accessorData = "none"
    var taskObserveOk = false
    var taskNode: RoSGNode? = null
    var eventCount = 0

    val clock = RoTimespan.create()
    clock.mark()

    while (clock.totalMilliseconds() < 20000) {
        val msg = port.waitMessage(100)
        if (msg == null) continue
        val msgType = typeOf(msg)
        if (msgType != "roSGNodeEvent") {
            println("SPIKE|OTHER_EVENT|$msgType")
            continue
        }
        val ev = msg as RoSGNodeEvent
        val field = ev.getField()
        val dataStr = "${ev.getData()}"
        eventCount++
        println("SPIKE|EVENT|#$eventCount|field=$field|data=$dataStr|at=${clock.totalMilliseconds()}ms")

        if (!accessorsChecked && field == "echo") {
            accessorsChecked = true
            val n = ev.getNode()
            val rn = ev.getRoSGNode()
            accessorNodeInfo = "typeOf=${typeOf(n)} value=$n"
            accessorSubtype = rn.subtype()
            accessorData = dataStr
            println("SPIKE|ACCESSORS|getField=$field|getData=$dataStr|getNode:$accessorNodeInfo|getRoSGNode.subtype=$accessorSubtype")
        }

        if (field == "input") {
            if (dataStr == "ping1") gotInput1 = true
            if (dataStr == "ping2") gotInput2 = true
        } else if (field == "echo") {
            if (dataStr == "echo:ping1") gotEcho1 = true
            if (dataStr == "echo:ping2") gotEcho2 = true
            if (gotEcho1 && !sentPing2) {
                // Second rendezvous cycle, issued from inside the wait loop.
                sentPing2 = true
                probe.setField("input", "ping2")
            }
        } else if (field == "timerOut") {
            gotTimer = true
        } else if (field == "taskOut") {
            if (dataStr == "task-wrote:hello-task") gotTask = true
        }

        // Stretch: once the render-thread checks are in, run the task check.
        if (gotEcho1 && gotEcho2 && gotTimer && !taskStarted) {
            taskStarted = true
            val t = RoSGNode.create("SpikeTask")
            taskNode = t
            taskObserveOk = observeFieldWithPort(t, "taskOut", port)
            println("SPIKE|task_observe_return|$taskObserveOk")
            t.setField("taskIn", "hello-task")
            t.setField("control", "RUN")
        }

        if (gotEcho1 && gotEcho2 && gotTimer && gotInput1 && gotInput2 && gotTask) break
    }

    println("SPIKE|LOOP_DONE|events=$eventCount|elapsed=${clock.totalMilliseconds()}ms")

    // Cross-check: read the fields directly after the event phase.
    val finalEcho = "${probe.getField("echo")}"
    val finalTimer = "${probe.getField("timerOut")}"
    var finalTask = "no-task-node"
    val t2 = taskNode
    if (t2 != null) finalTask = "${t2.getField("taskOut")}"
    println("SPIKE|FINAL_FIELDS|echo=$finalEcho|timerOut=$finalTimer|taskOut=$finalTask")

    check("port_observe_accepted", okEcho && okTimer, "observeField(field, port) returned echo=$okEcho timerOut=$okTimer")
    check("scoped_port_observe_accepted", okInput, "observeFieldScoped(field, port) from main returned $okInput")
    check("render_onchange_to_main_port", gotEcho1 && gotEcho2, "echo events for ping1=$gotEcho1 ping2=$gotEcho2 (two full set->onChange->event cycles)")
    check("main_set_self_event", gotInput1 && gotInput2, "input events seen ping1=$gotInput1 ping2=$gotInput2 (scoped observer)")
    check("spontaneous_timer_write", gotTimer, "timerOut event from render-thread Timer handler")
    check("event_accessors", accessorsChecked && accessorData == "echo:ping1" && accessorSubtype == "PortProbe", "data=$accessorData subtype=$accessorSubtype getNode($accessorNodeInfo)")
    check("task_thread_write_to_main_port", gotTask && taskObserveOk, "taskOut event received=$gotTask observeReturn=$taskObserveOk")
    check("final_field_reads", finalEcho == "echo:ping2" && finalTimer == "timer-fired", "echo=$finalEcho timerOut=$finalTimer taskOut=$finalTask")

    println("SPIKE|END")

    // Keep pumping briefly so the app doesn't exit the instant results print.
    val lingering = RoTimespan.create()
    lingering.mark()
    while (lingering.totalMilliseconds() < 3000) {
        val msg = port.waitMessage(100)
        if (msg != null && typeOf(msg) == "roSGScreenEvent") {
            val ev = msg as RoSGScreenEvent
            if (ev.isScreenClosed()) return
        }
    }
    screen.close()
}
