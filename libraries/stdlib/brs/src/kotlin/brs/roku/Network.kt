/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

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
 * val http = RoUrlTransfer()
 * http.setUrl("https://api.example.com/data")
 * val response = http.getToString()
 * if (response != null) {
 *     println(response)
 * }
 * ```
 *
 * Example usage (asynchronous):
 * ```kotlin
 * val http = RoUrlTransfer()
 * val port = RoMessagePort()
 * http.setMessagePort(port)
 * http.setUrl("https://api.example.com/data")
 * http.asyncGetToString()
 *
 * val msg = port.waitMessage(10000)
 * if (msg != null) {
 *     val event = RoUrlEvent(msg)
 *     if (event.getResponseCode() == 200) {
 *         println(event.getString())
 *     }
 * }
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/rourltransfer.md">roUrlTransfer</a>
 */
public class RoUrlTransfer : ISetMessagePort, IGetMessagePort, IHttpAgent, IUrlTransfer {

    private val native: Dynamic

    /**
     * Creates a new roUrlTransfer instance.
     */
    public constructor() {
        native = brsCreateUrlTransfer()
    }

    // ==================== IUrlTransfer Implementation ====================

    override fun setUrl(url: String): Boolean {
        return brsSetUrl(native, url)
    }

    override fun getUrl(): String {
        return brsGetUrl(native)
    }

    override fun getToString(): String? {
        return brsGetToString(native)
    }

    override fun postFromString(body: String): Int {
        return brsPostFromString(native, body)
    }

    override fun head(): Int {
        return brsHead(native)
    }

    override fun asyncGetToString(): Boolean {
        return brsAsyncGetToString(native)
    }

    override fun asyncPostFromString(body: String): Boolean {
        return brsAsyncPostFromString(native, body)
    }

    override fun asyncCancel(): Boolean {
        return brsAsyncCancel(native)
    }

    // ==================== IHttpAgent Implementation ====================

    override fun addHeader(name: String, value: String): Boolean {
        return brsAddHeader(native, name, value)
    }

    override fun setCertificatesFile(path: String): Boolean {
        return brsSetCertificatesFile(native, path)
    }

    override fun initClientCertificates(): Boolean {
        return brsInitClientCertificates(native)
    }

    override fun setHeaders(headers: Map<String, String>): Boolean {
        return brsSetHeaders(native, headers)
    }

    // ==================== ISetMessagePort / IGetMessagePort ====================

    override fun setMessagePort(port: RoMessagePort) {
        brsSetMessagePort(native, port.getNative())
    }

    override fun getMessagePort(): RoMessagePort? {
        val port = brsGetMessagePort(native)
        return if (port != null) RoMessagePort(port) else null
    }

    // ==================== Request Configuration ====================

    /**
     * Sets the HTTP request method (GET, POST, PUT, DELETE, etc.).
     *
     * @param method The HTTP method to use.
     */
    public fun setRequest(method: String) {
        brsSetRequest(native, method)
    }

    /**
     * Enables or disables retaining the response body on error responses.
     *
     * @param retain True to retain body on error.
     */
    public fun retainBodyOnError(retain: Boolean) {
        brsRetainBodyOnError(native, retain)
    }

    /**
     * Enables HTTP authentication.
     *
     * @param user Username.
     * @param password Password.
     * @return True if successful.
     */
    public fun setUserAndPassword(user: String, password: String): Boolean {
        return brsSetUserAndPassword(native, user, password)
    }

    /**
     * Sets the minimum transfer rate in bytes per second.
     * If the transfer falls below this rate for the specified time, it fails.
     *
     * @param bytesPerSecond Minimum bytes per second.
     * @param periodInSeconds Time period to measure.
     */
    public fun setMinimumTransferRate(bytesPerSecond: Int, periodInSeconds: Int) {
        brsSetMinimumTransferRate(native, bytesPerSecond, periodInSeconds)
    }

    /**
     * Enables or disables automatic encoding of URLs.
     *
     * @param enable True to enable URL encoding.
     */
    public fun enableEncodings(enable: Boolean) {
        brsEnableEncodings(native, enable)
    }

    /**
     * Enables or disables HTTP redirects.
     *
     * @param enable True to follow redirects.
     */
    public fun enableResume(enable: Boolean) {
        brsEnableResume(native, enable)
    }

    /**
     * Enables or disables peer verification for HTTPS.
     *
     * @param enable True to verify peer certificates.
     */
    public fun enablePeerVerification(enable: Boolean) {
        brsEnablePeerVerification(native, enable)
    }

    /**
     * Enables or disables host verification for HTTPS.
     *
     * @param enable True to verify host names.
     */
    public fun enableHostVerification(enable: Boolean) {
        brsEnableHostVerification(native, enable)
    }

    /**
     * Enables or disables fresh connections (no connection reuse).
     *
     * @param enable True to use fresh connections.
     */
    public fun enableFreshConnection(enable: Boolean) {
        brsEnableFreshConnection(native, enable)
    }

    // ==================== File Transfers ====================

    /**
     * Performs a synchronous download to a file.
     *
     * @param path Local file path to save to.
     * @return HTTP response code.
     */
    public fun getToFile(path: String): Int {
        return brsGetToFile(native, path)
    }

    /**
     * Performs a synchronous upload from a file.
     *
     * @param path Local file path to upload.
     * @return HTTP response code.
     */
    public fun postFromFile(path: String): Int {
        return brsPostFromFile(native, path)
    }

    /**
     * Starts an asynchronous download to a file.
     *
     * @param path Local file path to save to.
     * @return True if started successfully.
     */
    public fun asyncGetToFile(path: String): Boolean {
        return brsAsyncGetToFile(native, path)
    }

    /**
     * Starts an asynchronous upload from a file.
     *
     * @param path Local file path to upload.
     * @return True if started successfully.
     */
    public fun asyncPostFromFile(path: String): Boolean {
        return brsAsyncPostFromFile(native, path)
    }

    /**
     * Starts an asynchronous multipart form upload.
     *
     * @param elements List of form elements.
     * @return True if started successfully.
     */
    public fun asyncPostFromFileToString(elements: Dynamic): Boolean {
        return brsAsyncPostFromFileToString(native, elements)
    }

    // ==================== Response Information ====================

    /**
     * Returns the HTTP response code from the last synchronous transfer.
     */
    public fun getResponseCode(): Int {
        return brsGetResponseCode(native)
    }

    /**
     * Returns response headers from the last transfer.
     */
    public fun getResponseHeaders(): Dynamic {
        return brsGetResponseHeaders(native)
    }

    /**
     * Returns a specific response header value.
     *
     * @param name Header name.
     * @return Header value, or empty string if not found.
     */
    public fun getResponseHeader(name: String): String {
        val headers = getResponseHeaders()
        return brsLookupStr(headers, name)
    }

    /**
     * Returns the failure reason if the last transfer failed.
     */
    public fun getFailureReason(): String {
        return brsGetFailureReason(native)
    }

    /**
     * Returns a unique identity for this transfer object.
     */
    public fun getIdentity(): Int {
        return brsGetIdentity(native)
    }

    /**
     * Escapes a string for use in a URL.
     *
     * @param text Text to escape.
     * @return URL-escaped string.
     */
    public fun escape(text: String): String {
        return brsEscape(native, text)
    }

    /**
     * Unescapes a URL-encoded string.
     *
     * @param text URL-encoded text.
     * @return Unescaped string.
     */
    public fun unescape(text: String): String {
        return brsUnescape(native, text)
    }

    // ==================== BrightScript Intrinsics ====================

    @BrsInline("return CreateObject(\"roUrlTransfer\")")
    private external fun brsCreateUrlTransfer(): Dynamic

    @BrsInline("return ut.SetUrl(url)")
    private external fun brsSetUrl(ut: Dynamic, url: String): Boolean

    @BrsInline("return ut.GetUrl()")
    private external fun brsGetUrl(ut: Dynamic): String

    @BrsInline("return ut.AddHeader(name, value)")
    private external fun brsAddHeader(ut: Dynamic, name: String, value: String): Boolean

    @BrsInline("return ut.SetHeaders(headers)")
    private external fun brsSetHeaders(ut: Dynamic, headers: Map<String, String>): Boolean

    @BrsInline("ut.SetRequest(method)")
    private external fun brsSetRequest(ut: Dynamic, method: String)

    @BrsInline("ut.RetainBodyOnError(retain)")
    private external fun brsRetainBodyOnError(ut: Dynamic, retain: Boolean)

    @BrsInline("return ut.SetCertificatesFile(path)")
    private external fun brsSetCertificatesFile(ut: Dynamic, path: String): Boolean

    @BrsInline("return ut.InitClientCertificates()")
    private external fun brsInitClientCertificates(ut: Dynamic): Boolean

    @BrsInline("return ut.SetUserAndPassword(user, password)")
    private external fun brsSetUserAndPassword(ut: Dynamic, user: String, password: String): Boolean

    @BrsInline("ut.SetMinimumTransferRate(bytesPerSecond, periodInSeconds)")
    private external fun brsSetMinimumTransferRate(ut: Dynamic, bytesPerSecond: Int, periodInSeconds: Int)

    @BrsInline("ut.EnableEncodings(enable)")
    private external fun brsEnableEncodings(ut: Dynamic, enable: Boolean)

    @BrsInline("ut.EnableResume(enable)")
    private external fun brsEnableResume(ut: Dynamic, enable: Boolean)

    @BrsInline("ut.EnablePeerVerification(enable)")
    private external fun brsEnablePeerVerification(ut: Dynamic, enable: Boolean)

    @BrsInline("ut.EnableHostVerification(enable)")
    private external fun brsEnableHostVerification(ut: Dynamic, enable: Boolean)

    @BrsInline("ut.EnableFreshConnection(enable)")
    private external fun brsEnableFreshConnection(ut: Dynamic, enable: Boolean)

    @BrsInline("ut.SetMessagePort(port)")
    private external fun brsSetMessagePort(ut: Dynamic, port: Dynamic)

    @BrsInline("return ut.GetMessagePort()")
    private external fun brsGetMessagePort(ut: Dynamic): Dynamic?

    @BrsInline("return ut.GetToString()")
    private external fun brsGetToString(ut: Dynamic): String?

    @BrsInline("return ut.PostFromString(body)")
    private external fun brsPostFromString(ut: Dynamic, body: String): Int

    @BrsInline("return ut.Head()")
    private external fun brsHead(ut: Dynamic): Int

    @BrsInline("return ut.GetToFile(path)")
    private external fun brsGetToFile(ut: Dynamic, path: String): Int

    @BrsInline("return ut.PostFromFile(path)")
    private external fun brsPostFromFile(ut: Dynamic, path: String): Int

    @BrsInline("return ut.AsyncGetToString()")
    private external fun brsAsyncGetToString(ut: Dynamic): Boolean

    @BrsInline("return ut.AsyncPostFromString(body)")
    private external fun brsAsyncPostFromString(ut: Dynamic, body: String): Boolean

    @BrsInline("return ut.AsyncGetToFile(path)")
    private external fun brsAsyncGetToFile(ut: Dynamic, path: String): Boolean

    @BrsInline("return ut.AsyncPostFromFile(path)")
    private external fun brsAsyncPostFromFile(ut: Dynamic, path: String): Boolean

    @BrsInline("return ut.AsyncPostFromFileToString(elements)")
    private external fun brsAsyncPostFromFileToString(ut: Dynamic, elements: Dynamic): Boolean

    @BrsInline("return ut.AsyncCancel()")
    private external fun brsAsyncCancel(ut: Dynamic): Boolean

    @BrsInline("return ut.GetResponseCode()")
    private external fun brsGetResponseCode(ut: Dynamic): Int

    @BrsInline("return ut.GetResponseHeaders()")
    private external fun brsGetResponseHeaders(ut: Dynamic): Dynamic

    @BrsInline("return ut.GetFailureReason()")
    private external fun brsGetFailureReason(ut: Dynamic): String

    @BrsInline("return ut.GetIdentity()")
    private external fun brsGetIdentity(ut: Dynamic): Int

    @BrsInline("return ut.Escape(text)")
    private external fun brsEscape(ut: Dynamic, text: String): String

    @BrsInline("return ut.Unescape(text)")
    private external fun brsUnescape(ut: Dynamic, text: String): String

    @BrsInline("if headers[name] <> invalid then return headers[name] else return \"\"")
    private external fun brsLookupStr(headers: Dynamic, name: String): String
}

/**
 * Event received from roUrlTransfer async operations.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/events/rourlevent.md">roUrlEvent</a>
 */
public class RoUrlEvent(private val event: Dynamic) {

    /**
     * Returns the HTTP response code.
     */
    public fun getResponseCode(): Int = brsGetResponseCode(event)

    /**
     * Returns the response body as a string.
     */
    public fun getString(): String = brsGetString(event)

    /**
     * Returns the failure reason if the request failed.
     */
    public fun getFailureReason(): String = brsGetFailureReason(event)

    /**
     * Returns the source identity of the transfer.
     */
    public fun getSourceIdentity(): Int = brsGetSourceIdentity(event)

    /**
     * Returns the response headers.
     */
    public fun getResponseHeaders(): Dynamic = brsGetResponseHeaders(event)

    /**
     * Returns a specific response header value.
     *
     * @param name Header name.
     * @return Header value, or empty string if not found.
     */
    public fun getResponseHeader(name: String): String {
        val headers = getResponseHeaders()
        return brsLookupStr(headers, name)
    }

    /**
     * Returns the target IP address of the connection.
     */
    public fun getTargetIpAddress(): String = brsGetTargetIpAddress(event)

    /**
     * Returns the number of bytes downloaded.
     */
    public fun getBytesReceived(): Int = brsGetBytesReceived(event)

    @BrsInline("return ev.GetResponseCode()")
    private external fun brsGetResponseCode(ev: Dynamic): Int

    @BrsInline("return ev.GetString()")
    private external fun brsGetString(ev: Dynamic): String

    @BrsInline("return ev.GetFailureReason()")
    private external fun brsGetFailureReason(ev: Dynamic): String

    @BrsInline("return ev.GetSourceIdentity()")
    private external fun brsGetSourceIdentity(ev: Dynamic): Int

    @BrsInline("return ev.GetResponseHeaders()")
    private external fun brsGetResponseHeaders(ev: Dynamic): Dynamic

    @BrsInline("return ev.GetTargetIpAddress()")
    private external fun brsGetTargetIpAddress(ev: Dynamic): String

    @BrsInline("return ev.GetInt()")
    private external fun brsGetBytesReceived(ev: Dynamic): Int

    @BrsInline("if headers[name] <> invalid then return headers[name] else return \"\"")
    private external fun brsLookupStr(headers: Dynamic, name: String): String
}
