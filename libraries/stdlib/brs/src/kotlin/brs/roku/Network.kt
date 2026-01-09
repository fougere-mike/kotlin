/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsCreateObject
import kotlin.brs.BrsInline
import kotlin.brs.Dynamic

/**
 * HTTP/HTTPS URL transfer component for making network requests.
 *
 * roUrlTransfer provides functionality for making HTTP GET, POST, HEAD
 * and other requests, both synchronously and asynchronously.
 *
 * Example usage (synchronous):
 * ```kotlin
 * val http = RoUrlTransfer.create()
 * http.setUrl("https://api.example.com/data")
 * val response = http.getToString()
 * if (response != null) {
 *     println(response)
 * }
 * ```
 *
 * Example usage (asynchronous):
 * ```kotlin
 * val http = RoUrlTransfer.create()
 * val port = RoMessagePort.create()
 * http.setMessagePort(port)
 * http.setUrl("https://api.example.com/data")
 * http.asyncGetToString()
 *
 * val msg = port.waitMessage(10000)
 * if (msg != null) {
 *     val event = msg as RoUrlEvent
 *     if (event.getResponseCode() == 200) {
 *         println(event.getString())
 *     }
 * }
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/rourltransfer.md">roUrlTransfer</a>
 */
public external interface RoUrlTransfer : ISetMessagePort, IGetMessagePort, IHttpAgent, IUrlTransfer {
    // ==================== IUrlTransfer Implementation ====================

    override fun setUrl(url: String): Boolean
    override fun getUrl(): String
    override fun getToString(): String?
    override fun postFromString(body: String): Int
    override fun head(): Int
    override fun asyncGetToString(): Boolean
    override fun asyncPostFromString(body: String): Boolean
    override fun asyncCancel(): Boolean

    // ==================== IHttpAgent Implementation ====================

    override fun addHeader(name: String, value: String): Boolean
    override fun setCertificatesFile(path: String): Boolean
    override fun initClientCertificates(): Boolean
    override fun setHeaders(headers: Any?): Boolean

    // ==================== ISetMessagePort / IGetMessagePort ====================

    override fun setMessagePort(port: IMessagePort)
    override fun getMessagePort(): IMessagePort?

    // ==================== Request Configuration ====================

    /**
     * Sets the HTTP request method (GET, POST, PUT, DELETE, etc.).
     *
     * @param method The HTTP method to use.
     */
    public fun setRequest(method: String)

    /**
     * Enables or disables retaining the response body on error responses.
     *
     * @param retain True to retain body on error.
     */
    public fun retainBodyOnError(retain: Boolean)

    /**
     * Enables HTTP authentication.
     *
     * @param user Username.
     * @param password Password.
     * @return True if successful.
     */
    public fun setUserAndPassword(user: String, password: String): Boolean

    /**
     * Sets the minimum transfer rate in bytes per second.
     * If the transfer falls below this rate for the specified time, it fails.
     *
     * @param bytesPerSecond Minimum bytes per second.
     * @param periodInSeconds Time period to measure.
     */
    public fun setMinimumTransferRate(bytesPerSecond: Int, periodInSeconds: Int)

    /**
     * Enables or disables automatic encoding of URLs.
     *
     * @param enable True to enable URL encoding.
     */
    public fun enableEncodings(enable: Boolean)

    /**
     * Enables or disables HTTP redirects.
     *
     * @param enable True to follow redirects.
     */
    public fun enableResume(enable: Boolean)

    /**
     * Enables or disables peer verification for HTTPS.
     *
     * @param enable True to verify peer certificates.
     */
    public fun enablePeerVerification(enable: Boolean)

    /**
     * Enables or disables host verification for HTTPS.
     *
     * @param enable True to verify host names.
     */
    public fun enableHostVerification(enable: Boolean)

    /**
     * Enables or disables fresh connections (no connection reuse).
     *
     * @param enable True to use fresh connections.
     */
    public fun enableFreshConnection(enable: Boolean)

    // ==================== File Transfers ====================

    /**
     * Performs a synchronous download to a file.
     *
     * @param path Local file path to save to.
     * @return HTTP response code.
     */
    public fun getToFile(path: String): Int

    /**
     * Performs a synchronous upload from a file.
     *
     * @param path Local file path to upload.
     * @return HTTP response code.
     */
    public fun postFromFile(path: String): Int

    /**
     * Starts an asynchronous download to a file.
     *
     * @param path Local file path to save to.
     * @return True if started successfully.
     */
    public fun asyncGetToFile(path: String): Boolean

    /**
     * Starts an asynchronous upload from a file.
     *
     * @param path Local file path to upload.
     * @return True if started successfully.
     */
    public fun asyncPostFromFile(path: String): Boolean

    /**
     * Starts an asynchronous multipart form upload.
     *
     * @param elements List of form elements.
     * @return True if started successfully.
     */
    public fun asyncPostFromFileToString(elements: Any?): Boolean

    // ==================== Response Information ====================

    /**
     * Returns the HTTP response code from the last synchronous transfer.
     */
    public fun getResponseCode(): Int

    /**
     * Returns response headers from the last transfer.
     */
    public fun getResponseHeaders(): Dynamic

    /**
     * Returns the failure reason if the last transfer failed.
     */
    public fun getFailureReason(): String

    /**
     * Returns a unique identity for this transfer object.
     */
    public fun getIdentity(): Int

    /**
     * Escapes a string for use in a URL.
     *
     * @param text Text to escape.
     * @return URL-escaped string.
     */
    public fun escape(text: String): String

    /**
     * Unescapes a URL-encoded string.
     *
     * @param text URL-encoded text.
     * @return Unescaped string.
     */
    public fun unescape(text: String): String

    public companion object {
        /**
         * Creates a new roUrlTransfer instance.
         *
         * Compiles to: `CreateObject("roUrlTransfer")`
         *
         * @return A new RoUrlTransfer instance.
         */
        @BrsCreateObject("roUrlTransfer")
        public fun create(): RoUrlTransfer = definedExternally
    }
}

// ==================== Extension Functions ====================

@BrsInline("if headers[name] <> invalid then return headers[name] else return \"\"")
private external fun brsLookupStr(headers: Dynamic, name: String): String

/**
 * Returns a specific response header value.
 *
 * @param name Header name.
 * @return Header value, or empty string if not found.
 */
public fun RoUrlTransfer.getResponseHeader(name: String): String {
    val headers = getResponseHeaders()
    return brsLookupStr(headers, name)
}

/**
 * Event received from roUrlTransfer async operations.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/events/rourlevent.md">roUrlEvent</a>
 */
public external interface RoUrlEvent {
    /**
     * Returns the HTTP response code.
     */
    public fun getResponseCode(): Int

    /**
     * Returns the response body as a string.
     */
    public fun getString(): String

    /**
     * Returns the failure reason if the request failed.
     */
    public fun getFailureReason(): String

    /**
     * Returns the source identity of the transfer.
     */
    public fun getSourceIdentity(): Int

    /**
     * Returns the response headers.
     */
    public fun getResponseHeaders(): Dynamic

    /**
     * Returns the target IP address of the connection.
     */
    public fun getTargetIpAddress(): String

    /**
     * Returns the number of bytes downloaded.
     */
    public fun getInt(): Int
}

// ==================== RoUrlEvent Extension Functions ====================

/**
 * Returns the number of bytes received.
 */
public fun RoUrlEvent.getBytesReceived(): Int = getInt()

/**
 * Returns a specific response header value.
 *
 * @param name Header name.
 * @return Header value, or empty string if not found.
 */
public fun RoUrlEvent.getResponseHeader(name: String): String {
    val headers = getResponseHeaders()
    return brsLookupStr(headers, name)
}
