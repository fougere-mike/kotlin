package com.nuvyyo.roku.components.rtqspiketask

import kotlin.brs.BrsInline
import kotlin.brs.roku.*

// Task-thread half of the render-thread-queue spike: creates its own
// roRenderThreadQueue handle on the TASK thread and posts to the channel the
// render-thread probes registered (checks: rtq_create on task thread,
// rtq_cross_thread_post). Driven directly via control=RUN (no runTask needed).
@BrsInline("return type(CreateObject(\"roRenderThreadQueue\"))")
private external fun rtqCreateTypeNameTask(): String

@BrsInline("return CreateObject(\"roRenderThreadQueue\")")
private external fun rtqCreateTask(): Dynamic?

@BrsInline("q.PostMessage(channel, data)")
private external fun rtqPostTask(q: Dynamic?, channel: String, data: Any?)

class RtqSpikeTask : TaskComponent() {

    override fun run() {
        val t = rtqCreateTypeNameTask()
        println("SPIKE|INFO|task_rtq_create|type=$t")
        val q = rtqCreateTask()
        if (q != null) {
            val payload = RoAssociativeArray.create()
            payload.addReplace("src", "task")
            payload.addReplace("num", 42)
            rtqPostTask(q, "spike.pump", payload)
            println("SPIKE|INFO|task_posted|channel=spike.pump")
        } else {
            println("SPIKE|INFO|task_posted|skipped-no-rtq-on-task-thread")
        }
    }
}
