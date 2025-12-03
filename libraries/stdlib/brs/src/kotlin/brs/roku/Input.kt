/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsInline
import kotlin.brs.Dynamic

/**
 * Provides access to deep link and ECP (External Control Protocol) input.
 *
 * roInput is used to receive input from deep linking, voice commands,
 * and external control protocol messages.
 *
 * Example usage:
 * ```kotlin
 * val input = RoInput()
 * val port = RoMessagePort()
 * input.setMessagePort(port)
 *
 * // Wait for input events
 * val msg = port.waitMessage(0)
 * if (msg != null) {
 *     val event = RoInputEvent(msg)
 *     val info = event.getInfo()
 *     // Process the input
 * }
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/roinput.md">roInput</a>
 */
public class RoInput : ISetMessagePort, IGetMessagePort {

    private val native: Dynamic

    /**
     * Creates a new roInput instance.
     */
    public constructor() {
        native = brsCreateInput()
    }

    override fun setMessagePort(port: RoMessagePort) {
        brsSetMessagePort(native, port.getNative())
    }

    override fun getMessagePort(): RoMessagePort? {
        val port = brsGetMessagePort(native)
        return if (port != null) RoMessagePort(port) else null
    }

    /**
     * Marks the input as handled, preventing it from being processed elsewhere.
     */
    public fun markAsHandled() {
        brsMarkAsHandled(native)
    }

    /**
     * Returns the native BrightScript object.
     */
    internal fun getNative(): Dynamic = native

    // ==================== BrightScript Intrinsics ====================

    @BrsInline("return CreateObject(\"roInput\")")
    private external fun brsCreateInput(): Dynamic

    @BrsInline("input.SetMessagePort(port)")
    private external fun brsSetMessagePort(input: Dynamic, port: Dynamic)

    @BrsInline("return input.GetMessagePort()")
    private external fun brsGetMessagePort(input: Dynamic): Dynamic?

    @BrsInline("input.MarkAsHandled()")
    private external fun brsMarkAsHandled(input: Dynamic)
}

/**
 * Event received from roInput for deep links and ECP messages.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/events/roinputevent.md">roInputEvent</a>
 */
public class RoInputEvent(private val event: Dynamic) {

    /**
     * Returns true if this is a deep link input event.
     */
    public fun isInput(): Boolean = brsIsInput(event)

    /**
     * Returns true if this is a screen saver exit event.
     */
    public fun isScreenSaverExitedEvent(): Boolean = brsIsScreenSaverExitedEvent(event)

    /**
     * Returns the input parameters as an associative array.
     * Contains data from deep links, voice commands, etc.
     *
     * @return Map of parameter names to values.
     */
    public fun getInfo(): Dynamic = brsGetInfo(event)

    @BrsInline("return ev.IsInput()")
    private external fun brsIsInput(ev: Dynamic): Boolean

    @BrsInline("return ev.IsScreenSaverExitedEvent()")
    private external fun brsIsScreenSaverExitedEvent(ev: Dynamic): Boolean

    @BrsInline("return ev.GetInfo()")
    private external fun brsGetInfo(ev: Dynamic): Dynamic
}
