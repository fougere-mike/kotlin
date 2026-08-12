package spike

import kotlin.brs.BrsInline
import kotlin.brs.roku.RoMessagePort
import kotlin.brs.roku.RoSGScreen
import kotlin.brs.roku.RoSGScreenEvent
import kotlin.brs.typeOf

// Raw-BRS epoch string (same expression as the channel-matrix spike's
// main.brs): the Kotlin path (__kotlin_numToStr) stringifies via BRS Str(),
// whose parameter is a FLOAT — epoch-sized values come out as "1.786574e+09"
// with precision loss, which breaks deploy.sh's sentinel freshness regex.
@BrsInline("return CreateObject(\"roDateTime\").asSeconds().toStr()")
private external fun epochSecondsStr(): String

// Boots the SpikeScene; all probe logic lives in the components. The sentinel
// line is what deploy.sh anchors its stale-buffer filtering on (same format
// as the channel-matrix spike).
fun main() {
    println("===SPIKE_SENTINEL_" + epochSecondsStr() + "===")

    val screen = RoSGScreen.create()
    val port = RoMessagePort.create()
    screen.setMessagePort(port)
    screen.createScene("SpikeScene")
    screen.show()

    while (true) {
        val msg = port.waitMessage(0)
        if (msg != null && typeOf(msg) == "roSGScreenEvent") {
            val ev = msg as RoSGScreenEvent
            if (ev.isScreenClosed()) return
        }
    }
}
