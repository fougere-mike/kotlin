/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsCreateObject
import kotlin.brs.Dynamic

/**
 * Provides access to deep link and ECP (External Control Protocol) input.
 *
 * roInput is used to receive input from deep linking, voice commands,
 * and external control protocol messages.
 *
 * Example usage:
 * ```kotlin
 * val input = RoInput.create()
 * val port = RoMessagePort.create()
 * input.setMessagePort(port)
 *
 * // Wait for input events
 * val msg = port.waitMessage(0)
 * if (msg != null) {
 *     val event = msg as RoInputEvent
 *     val info = event.getInfo()
 *     // Process the input
 * }
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/roinput.md">roInput</a>
 */
public external interface RoInput : ISetMessagePort, IGetMessagePort {
    override fun setMessagePort(port: IMessagePort)
    override fun getMessagePort(): IMessagePort?

    /**
     * Marks the input as handled, preventing it from being processed elsewhere.
     */
    public fun markAsHandled()

    public companion object {
        /**
         * Creates a new roInput instance.
         *
         * Compiles to: `CreateObject("roInput")`
         *
         * @return A new RoInput instance.
         */
        @BrsCreateObject("roInput")
        public fun create(): RoInput = definedExternally
    }
}

/**
 * Event received from roInput for deep links and ECP messages.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/events/roinputevent.md">roInputEvent</a>
 */
public external interface RoInputEvent {
    /**
     * Returns true if this is a deep link input event.
     */
    public fun isInput(): Boolean

    /**
     * Returns true if this is a screen saver exit event.
     */
    public fun isScreenSaverExitedEvent(): Boolean

    /**
     * Returns the input parameters as an associative array.
     * Contains data from deep links, voice commands, etc.
     *
     * @return Map of parameter names to values.
     */
    public fun getInfo(): Dynamic
}
