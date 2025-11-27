/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlin.brs.roku

import kotlin.brs.*

/**
 * BrightScript roUrlTransfer - HTTP client for network requests.
 */
@BrsExternal
public external class RoUrlTransfer : RoInterface {
    public constructor()

    /**
     * Sets the URL for the request.
     */
    public fun setUrl(url: String): Boolean

    /**
     * Gets the current URL.
     */
    public fun getUrl(): String

    /**
     * Sets the message port for async operations.
     */
    public fun setPort(port: RoMessagePort)

    /**
     * Performs a synchronous GET request.
     * @return The response body as a string.
     */
    public fun getToString(): String

    /**
     * Performs a synchronous GET request to a file.
     * @param filename The local file path to save to.
     * @return HTTP response code.
     */
    public fun getToFile(filename: String): Int

    /**
     * Starts an asynchronous GET request.
     * @return True if the request was started.
     */
    public fun asyncGetToString(): Boolean

    /**
     * Starts an asynchronous GET request to a file.
     * @param filename The local file path to save to.
     * @return True if the request was started.
     */
    public fun asyncGetToFile(filename: String): Boolean

    /**
     * Performs a synchronous HEAD request.
     * @return HTTP response code.
     */
    public fun head(): Int

    /**
     * Starts an asynchronous HEAD request.
     * @return True if the request was started.
     */
    public fun asyncHead(): Boolean

    /**
     * Performs a synchronous POST request.
     * @param body The request body.
     * @return HTTP response code.
     */
    public fun postFromString(body: String): Int

    /**
     * Performs a synchronous POST request from a file.
     * @param filename The local file path to read from.
     * @return HTTP response code.
     */
    public fun postFromFile(filename: String): Int

    /**
     * Starts an asynchronous POST request.
     * @param body The request body.
     * @return True if the request was started.
     */
    public fun asyncPostFromString(body: String): Boolean

    /**
     * Starts an asynchronous POST request from a file.
     * @param filename The local file path to read from.
     * @return True if the request was started.
     */
    public fun asyncPostFromFile(filename: String): Boolean

    /**
     * Cancels an async request.
     * @return True if a request was cancelled.
     */
    public fun asyncCancel(): Boolean

    /**
     * Sets a request header.
     */
    public fun addHeader(name: String, value: String): Boolean

    /**
     * Sets the HTTP method.
     */
    public fun setRequest(method: String): Boolean

    /**
     * Gets the response headers.
     */
    public fun getResponseHeaders(): RoAssociativeArray

    /**
     * Gets the response code.
     */
    public fun getResponseCode(): Int

    /**
     * Gets failure reason if request failed.
     */
    public fun getFailureReason(): String

    /**
     * Enables cookies.
     */
    public fun enableCookies()

    /**
     * Gets cookies for a domain.
     */
    public fun getCookies(domain: String, path: String): RoArray

    /**
     * Adds a cookie.
     */
    public fun addCookies(cookies: RoArray): Boolean

    /**
     * Clears all cookies.
     */
    public fun clearCookies()

    /**
     * Enables or disables encoding of the request body.
     */
    public fun enableEncodings(enable: Boolean): Boolean

    /**
     * Sets whether to retain body on redirect.
     */
    public fun retainBodyOnRedirect(retain: Boolean): Boolean

    /**
     * Enables SSL certificate verification.
     */
    public fun setCertificatesFile(path: String): Boolean

    /**
     * Enables or disables host verification.
     */
    public fun setHostnameVerification(enable: Boolean): Boolean

    /**
     * Enables fresh connections only.
     */
    public fun enableFreshConnection(enable: Boolean): Boolean

    /**
     * Enables peer verification.
     */
    public fun enablePeerVerification(enable: Boolean): Boolean

    /**
     * Sets minimum TLS version.
     */
    public fun setMinimumTransferRate(rate: Int, period: Int): Boolean

    /**
     * Initializes client certificates.
     */
    public fun initClientCertificates(): Boolean

    /**
     * Sets client certificate password.
     */
    public fun setClientCertificatePassword(password: String): Boolean

    /**
     * Gets response body as string.
     */
    public fun getString(): String

    /**
     * Escapes URL characters.
     */
    public fun escape(text: String): String

    /**
     * Unescapes URL characters.
     */
    public fun unescape(text: String): String

    /**
     * URL encodes a string.
     */
    public fun urlEncode(text: String): String
}

/**
 * Event received from roUrlTransfer async operations.
 */
@BrsExternal
public external interface RoUrlEvent : RoInterface {
    public fun getInt(): Int
    public fun getResponseCode(): Int
    public fun getFailureReason(): String
    public fun getString(): String
    public fun getSourceIdentity(): Int
    public fun getTargetIpAddress(): String
    public fun getResponseHeaders(): RoAssociativeArray
    public fun getResponseHeadersArray(): RoArray
}

/**
 * BrightScript roSocketAddress for network addressing.
 */
@BrsExternal
public external class RoSocketAddress : RoInterface {
    public constructor()

    public fun setHostname(hostname: String): Boolean
    public fun setAddress(address: String): Boolean
    public fun setPort(port: Int)
    public fun getHostname(): String
    public fun getAddress(): String
    public fun getPort(): Int
    public fun isAddressValid(): Boolean
}

/**
 * BrightScript roStreamSocket for TCP connections.
 */
@BrsExternal
public external class RoStreamSocket : RoInterface {
    public constructor()

    public fun setMessagePort(port: RoMessagePort)
    public fun setSendToAddress(address: RoSocketAddress): Boolean
    public fun setAddress(address: RoSocketAddress): Boolean
    public fun getCountRcvBuf(): Int
    public fun getCountSendBuf(): Int
    public fun status(): Int
    public fun connect(): Boolean
    public fun listen(backlog: Int): Boolean
    public fun isConnected(): Boolean
    public fun isListening(): Boolean
    public fun accept(): RoStreamSocket?
    public fun send(data: String, start: Int, length: Int): Int
    public fun sendStr(data: String): Int
    public fun receive(length: Int, start: Int): RoByteArray
    public fun receiveStr(length: Int): String
    public fun close()
}
