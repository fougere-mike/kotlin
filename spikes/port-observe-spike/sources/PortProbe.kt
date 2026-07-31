package com.nuvyyo.roku.components.portprobe

import kotlin.brs.roku.RoSGNodeEvent

// ObserverProbe-style fixture for the port-observe spike.
//
// - `input` is set by the main-thread driver; its @BrsOnChange handler runs on
//   the RENDER thread and writes `echo`.
// - A Timer child (started in init) fires ~500ms later on the RENDER thread and
//   writes `timerOut` — a spontaneous render-thread write not triggered by a
//   main-thread field set at that moment.
class PortProbe : GroupComponent() {

    @SGStringField
    @BrsOnChange("onInputChanged")
    var input: String = ""

    @SGStringField(alwaysNotify = true)
    var echo: String = ""

    @SGStringField(alwaysNotify = true)
    var timerOut: String = ""

    init {
        println("PortProbe: init (render thread)")
        val timer = top.createChild("Timer")
        timer.setField("duration", 0.5)
        timer.setField("repeat", false)
        timer.observeFieldScoped("fire", brsName(::onTimerFired))
        timer.setField("control", "start")
    }

    private fun onInputChanged(msg: RoSGNodeEvent) {
        val v = "${msg.getData()}"
        println("PortProbe: onInputChanged data=$v (render thread)")
        top.setField("echo", "echo:$v")
    }

    private fun onTimerFired(msg: RoSGNodeEvent) {
        println("PortProbe: timer fired (render thread)")
        top.setField("timerOut", "timer-fired")
    }
}
