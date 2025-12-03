/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.Dynamic

/**
 * Interface for message ports that receive events from asynchronous operations.
 * Maps to BrightScript's ifMessagePort interface.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifmessageport.md">ifMessagePort</a>
 */
public interface IMessagePort {
    /**
     * Waits for a message on the port, with a timeout.
     *
     * @param timeout Maximum time to wait in milliseconds. Use 0 for no timeout.
     * @return The received message, or null if timeout occurred.
     */
    public fun waitMessage(timeout: Int): Dynamic?

    /**
     * Gets a message from the port without blocking.
     *
     * @return The message if one is available, or null otherwise.
     */
    public fun getMessage(): Dynamic?

    /**
     * Peeks at the next message without removing it from the queue.
     *
     * @return The message if one is available, or null otherwise.
     */
    public fun peekMessage(): Dynamic?
}

/**
 * Interface for components that can have a message port set.
 * Maps to BrightScript's ifSetMessagePort interface.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifsetmessageport.md">ifSetMessagePort</a>
 */
public interface ISetMessagePort {
    /**
     * Sets the message port to receive events from this component.
     *
     * @param port The message port to receive events.
     */
    public fun setMessagePort(port: RoMessagePort)
}

/**
 * Interface for components that can return their message port.
 * Maps to BrightScript's ifGetMessagePort interface.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifgetmessageport.md">ifGetMessagePort</a>
 */
public interface IGetMessagePort {
    /**
     * Returns the message port currently set on this component.
     *
     * @return The message port, or null if none is set.
     */
    public fun getMessagePort(): RoMessagePort?
}

/**
 * Interface for iterable BrightScript components.
 * Maps to BrightScript's ifEnum interface.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifenum.md">ifEnum</a>
 */
public interface IEnum<T> {
    /**
     * Resets iteration to the beginning.
     */
    public fun reset()

    /**
     * Returns the next element, or null if iteration is complete.
     */
    public fun next(): T?

    /**
     * Checks if there are more elements.
     */
    public fun isNext(): Boolean
}

/**
 * Interface for components that support HTTP agent functionality.
 * Maps to BrightScript's ifHttpAgent interface.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifhttpagent.md">ifHttpAgent</a>
 */
public interface IHttpAgent {
    /**
     * Adds a custom HTTP header to requests.
     *
     * @param name Header name.
     * @param value Header value.
     * @return True if successful.
     */
    public fun addHeader(name: String, value: String): Boolean

    /**
     * Sets the path to the certificate file for HTTPS.
     *
     * @param path Path to the certificate file.
     * @return True if successful.
     */
    public fun setCertificatesFile(path: String): Boolean

    /**
     * Initializes client certificates for mutual TLS authentication.
     *
     * @return True if successful.
     */
    public fun initClientCertificates(): Boolean

    /**
     * Sets custom HTTP headers from a map.
     *
     * @param headers Map of header name to value.
     * @return True if all headers were set successfully.
     */
    public fun setHeaders(headers: Map<String, String>): Boolean
}

/**
 * Interface for URL transfer operations.
 * Maps to BrightScript's ifUrlTransfer interface.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifurltransfer.md">ifUrlTransfer</a>
 */
public interface IUrlTransfer {
    /**
     * Sets the URL to use for the transfer.
     *
     * @param url The URL to transfer to/from.
     * @return True if the URL is valid.
     */
    public fun setUrl(url: String): Boolean

    /**
     * Returns the URL currently set.
     */
    public fun getUrl(): String

    /**
     * Performs a synchronous HTTP GET and returns the response as a string.
     *
     * @return Response body as string, or null on failure.
     */
    public fun getToString(): String?

    /**
     * Performs a synchronous HTTP POST with the given body.
     *
     * @param body The request body to send.
     * @return HTTP response code.
     */
    public fun postFromString(body: String): Int

    /**
     * Performs a synchronous HTTP HEAD request.
     *
     * @return HTTP response code.
     */
    public fun head(): Int

    /**
     * Starts an asynchronous HTTP GET.
     *
     * @return True if the request was started successfully.
     */
    public fun asyncGetToString(): Boolean

    /**
     * Starts an asynchronous HTTP POST.
     *
     * @param body The request body to send.
     * @return True if the request was started successfully.
     */
    public fun asyncPostFromString(body: String): Boolean

    /**
     * Cancels an in-progress asynchronous transfer.
     *
     * @return True if a transfer was cancelled.
     */
    public fun asyncCancel(): Boolean
}
