/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsInline
import kotlin.brs.Dynamic

/**
 * Message port for receiving events from asynchronous BrightScript operations.
 *
 * roMessagePort is used to receive events from components that perform
 * asynchronous operations, such as roUrlTransfer, roInput, etc.
 *
 * Example usage:
 * ```kotlin
 * val port = RoMessagePort()
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
public class RoMessagePort : IMessagePort {

    /** The native BrightScript roMessagePort instance. */
    private val native: Dynamic

    /**
     * Creates a new message port.
     */
    public constructor() {
        native = brsCreateMessagePort()
    }

    /**
     * Internal constructor for wrapping an existing native message port.
     */
    internal constructor(nativePort: Dynamic) {
        native = nativePort
    }

    /**
     * Waits for a message on the port, with a timeout.
     *
     * @param timeout Maximum time to wait in milliseconds.
     * @return The received message, or null if timeout occurred.
     */
    override fun waitMessage(timeout: Int): Dynamic? {
        return brsWaitMessage(native, timeout)
    }

    /**
     * Gets a message from the port without blocking.
     *
     * @return The message if one is available, or null otherwise.
     */
    override fun getMessage(): Dynamic? {
        return brsGetMessage(native)
    }

    /**
     * Peeks at the next message without removing it from the queue.
     *
     * @return The message if one is available, or null otherwise.
     */
    override fun peekMessage(): Dynamic? {
        return brsPeekMessage(native)
    }

    /**
     * Returns the native BrightScript object for use with other components.
     */
    internal fun getNative(): Dynamic = native

    // ==================== BrightScript Intrinsics ====================

    @BrsInline("return CreateObject(\"roMessagePort\")")
    private external fun brsCreateMessagePort(): Dynamic

    @BrsInline("return port.WaitMessage(timeout)")
    private external fun brsWaitMessage(port: Dynamic, timeout: Int): Dynamic?

    @BrsInline("return port.GetMessage()")
    private external fun brsGetMessage(port: Dynamic): Dynamic?

    @BrsInline("return port.PeekMessage()")
    private external fun brsPeekMessage(port: Dynamic): Dynamic?
}
