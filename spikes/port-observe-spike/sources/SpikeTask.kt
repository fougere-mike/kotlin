package com.nuvyyo.roku.components.spiketask

// Stretch check for the port-observe spike: a Task component whose run body
// (TASK thread) writes `taskOut`. The main-thread driver observes `taskOut`
// with the port form and sets control=RUN.
class SpikeTask : TaskComponent() {

    @SGStringField
    var taskIn: String = ""

    @SGStringField(alwaysNotify = true)
    var taskOut: String = ""

    init {
        top.setField("functionName", brsName(::taskMain))
    }

    private fun taskMain() {
        val v = "${top.getField("taskIn")}"
        println("SpikeTask: taskMain running (task thread), taskIn=$v")
        top.setField("taskOut", "task-wrote:$v")
    }
}
