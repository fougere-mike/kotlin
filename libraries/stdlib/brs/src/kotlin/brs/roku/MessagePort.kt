/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsCreateObject
import kotlin.brs.Dynamic

/**
 * Message port for receiving events from asynchronous BrightScript operations.
 *
 * roMessagePort is used to receive events from components that perform
 * asynchronous operations, such as roUrlTransfer, roInput, etc.
 *
 * Example usage:
 * ```kotlin
 * val port = RoMessagePort.create()
 * urlTransfer.setMessagePort(port)
 *
 * val msg = port.waitMessage(5000)
 * if (msg != null) {
 *     // Process message
 * }
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/romessageport.md">roMessagePort</a>
 */
public external interface RoMessagePort : IMessagePort {
    /**
     * Waits for a message on the port, with a timeout.
     *
     * @param timeout Maximum time to wait in milliseconds.
     * @return The received message, or null if timeout occurred.
     */
    override fun waitMessage(timeout: Int): Dynamic?

    /**
     * Gets a message from the port without blocking.
     *
     * @return The message if one is available, or null otherwise.
     */
    override fun getMessage(): Dynamic?

    /**
     * Peeks at the next message without removing it from the queue.
     *
     * @return The message if one is available, or null otherwise.
     */
    override fun peekMessage(): Dynamic?

    public companion object {
        /**
         * Creates a new message port.
         *
         * Compiles to: `CreateObject("roMessagePort")`
         *
         * @return A new RoMessagePort instance.
         */
        @BrsCreateObject("roMessagePort")
        public fun create(): RoMessagePort = definedExternally
    }
}
