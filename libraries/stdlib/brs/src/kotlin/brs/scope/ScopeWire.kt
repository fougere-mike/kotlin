/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.scope

import kotlin.brs.Dynamic
import kotlin.brs.ScopeClosedException
import kotlin.brs.ScopeRequestException
import kotlin.brs.roku.RoArray
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode

/**
 * The ScopeHandle wire contract (design of record:
 * docs/superpowers/plans/2026-08-12-scopehandle-design.md §6.3/A.2): kind-tagged
 * envelope AAs over per-node inbox fields. Every payload value is marshal-safe
 * data (primitives/String/AA/array/node refs) — function values and class
 * instances never cross a component boundary (spike-pinned).
 *
 * Envelope shapes:
 * - request: `{ kind:"request", key, replyTo:<child node>, name, args:<RoArray?>, captures:<AA?> }`
 *   (rtq-carrier reply form: `replyToChannel:<child channel id>` replaces the
 *   `replyTo` node ref — the envelope is otherwise identical; the owner's
 *   outcome-posting switches on which reply key is present)
 * - cancel:  `{ kind:"cancel",  key }`
 * - outcome: `{ kind:"outcome", key, status:"value"|"error"|"closed", value?, message?, number?, backtrace? }`
 *
 * Handlers ignore unknown kinds (forward-compat, design decision 9).
 *
 * Visibility note: the build/parse/copy helpers are `public` in this
 * non-default-imported package because the stdlib device tests compile as a
 * separate module (no friend-module support in cli-brs) and the runBlocking
 * unit suite must exercise them directly. They are protocol machinery, not
 * user API — user code goes through `kotlin.brs.ScopeHandle`/`exposeScope`.
 */

/** Advertisement field on an owner node: absent = not a host; "field" = field backend. */
internal const val SCOPE_AD_FIELD: String = "__kotlinScope"

/** The field-backend advertisement value (doubles as the floor backend's name). */
internal const val SCOPE_AD_FIELD_BACKEND: String = "field"

/**
 * Prefix of the rtq-backend advertisement: `"rtq:<channelId>"` — the suffix IS
 * the owner's registered scope channel (the ad doubles as the routing address).
 */
internal const val SCOPE_AD_RTQ_PREFIX: String = "rtq:"

/** Inbox AA field (alwaysNotify=true), installed on BOTH owner and child nodes. */
internal const val SCOPE_INBOX_FIELD: String = "__kotlinScopeInbox"

/** Default once-per-request watchdog deadline (test hook shrinks per component). */
internal const val SCOPE_WATCHDOG_MS_DEFAULT: Int = 30_000

internal const val SCOPE_KIND_REQUEST: String = "request"
internal const val SCOPE_KIND_CANCEL: String = "cancel"
internal const val SCOPE_KIND_OUTCOME: String = "outcome"

internal const val SCOPE_STATUS_VALUE: String = "value"
internal const val SCOPE_STATUS_ERROR: String = "error"
internal const val SCOPE_STATUS_CLOSED: String = "closed"

/**
 * A parsed inbox envelope. Fields default defensively ("" / 0 / null) so a
 * malformed or unknown payload parses to an ignorable result instead of
 * crashing an inbox handler. Which fields are meaningful depends on [kind].
 */
public class ScopeEnvelope internal constructor(
    public val kind: String,
    public val key: String,
    public val name: String,
    public val status: String,
    public val replyTo: RoSGNode?,
    public val replyToChannel: String,
    public val args: RoArray?,
    public val captures: RoAssociativeArray?,
    public val value: Dynamic?,
    public val message: String,
    public val number: Int,
    public val backtrace: Dynamic?,
)

/** Parses an inbox payload. Never throws; see [ScopeEnvelope] for defaults. */
public fun parseScopeEnvelope(envelope: RoAssociativeArray): ScopeEnvelope {
    return ScopeEnvelope(
        kind = envelope.lookup("kind") as? String ?: "",
        key = envelope.lookup("key") as? String ?: "",
        name = envelope.lookup("name") as? String ?: "",
        status = envelope.lookup("status") as? String ?: "",
        replyTo = envelope.lookup("replyTo") as? RoSGNode,
        replyToChannel = envelope.lookup("replyToChannel") as? String ?: "",
        args = envelope.lookup("args") as? RoArray,
        captures = envelope.lookup("captures") as? RoAssociativeArray,
        value = envelope.lookup("value"),
        message = envelope.lookup("message") as? String ?: "",
        number = envelope.lookup("number") as? Int ?: 0,
        backtrace = envelope.lookup("backtrace"),
    )
}

public fun buildScopeRequestEnvelope(
    key: String,
    replyTo: RoSGNode,
    name: String,
    args: RoArray?,
    captures: RoAssociativeArray?,
): RoAssociativeArray {
    val aa = RoAssociativeArray.create()
    aa.addReplace("kind", SCOPE_KIND_REQUEST)
    aa.addReplace("key", key)
    aa.addReplace("replyTo", replyTo)
    aa.addReplace("name", name)
    aa.addReplace("args", args)
    aa.addReplace("captures", captures)
    return aa
}

/**
 * The rtq-carrier request form: the child's own registered CHANNEL ID as the
 * reply address (`replyToChannel` replaces the `replyTo` node ref).
 */
public fun buildScopeChannelRequestEnvelope(
    key: String,
    replyToChannel: String,
    name: String,
    args: RoArray?,
    captures: RoAssociativeArray?,
): RoAssociativeArray {
    val aa = RoAssociativeArray.create()
    aa.addReplace("kind", SCOPE_KIND_REQUEST)
    aa.addReplace("key", key)
    aa.addReplace("replyToChannel", replyToChannel)
    aa.addReplace("name", name)
    aa.addReplace("args", args)
    aa.addReplace("captures", captures)
    return aa
}

/**
 * The owner channel id carried by an `"rtq:<channelId>"` advertisement, or ""
 * for the field form (any unrecognized value degrades to the field carrier —
 * forward-compat: an older child talking to a future backend still posts
 * somewhere harmless rather than crashing).
 */
public fun scopeAdChannelId(ad: String): String {
    if (ad.startsWith(SCOPE_AD_RTQ_PREFIX)) {
        return ad.substring(SCOPE_AD_RTQ_PREFIX.length)
    }
    return ""
}

public fun buildScopeCancelEnvelope(key: String): RoAssociativeArray {
    val aa = RoAssociativeArray.create()
    aa.addReplace("kind", SCOPE_KIND_CANCEL)
    aa.addReplace("key", key)
    return aa
}

public fun buildScopeValueOutcome(key: String, value: Any?): RoAssociativeArray {
    val aa = RoAssociativeArray.create()
    aa.addReplace("kind", SCOPE_KIND_OUTCOME)
    aa.addReplace("key", key)
    aa.addReplace("status", SCOPE_STATUS_VALUE)
    aa.addReplace("value", value)
    return aa
}

public fun buildScopeClosedOutcome(key: String): RoAssociativeArray {
    val aa = RoAssociativeArray.create()
    aa.addReplace("kind", SCOPE_KIND_OUTCOME)
    aa.addReplace("key", key)
    aa.addReplace("status", SCOPE_STATUS_CLOSED)
    return aa
}

public fun buildScopeErrorOutcome(
    key: String,
    message: String,
    number: Int,
    backtrace: Dynamic?,
): RoAssociativeArray {
    val aa = RoAssociativeArray.create()
    aa.addReplace("kind", SCOPE_KIND_OUTCOME)
    aa.addReplace("key", key)
    aa.addReplace("status", SCOPE_STATUS_ERROR)
    aa.addReplace("message", message)
    aa.addReplace("number", number)
    aa.addReplace("backtrace", backtrace)
    return aa
}

/**
 * Builds an error outcome from a caught [Throwable], TaskException-style
 * (TaskRunner.taskExceptionFrom precedent): message/number/backtrace cross as
 * DATA; the exception object itself never does. Works for both Kotlin
 * exception instances and native BrightScript error objects — at runtime both
 * are AAs carrying `message`/`number` (and `backtrace` for native errors).
 */
public fun buildScopeFailureOutcome(key: String, cause: Throwable): RoAssociativeArray {
    val info = scopeErrorInfoFrom(cause)
    return buildScopeErrorOutcome(key, info.message, info.number, info.backtrace)
}

internal class ScopeErrorInfo(
    internal val message: String,
    internal val number: Int,
    internal val backtrace: Dynamic?,
)

/**
 * Extracts marshal-safe failure info from a caught [Throwable] by reading the
 * runtime AA's data keys (never property getters — a native BrightScript error
 * object has no Kotlin getter slots).
 */
internal fun scopeErrorInfoFrom(cause: Throwable): ScopeErrorInfo {
    var message = "ScopeHandle request failed"
    var number = 0
    var backtrace: Dynamic? = null
    val aa = cause as? RoAssociativeArray
    if (aa != null) {
        val rawMessage = aa.lookup("message")
        if (rawMessage != null) {
            message = "$rawMessage"
        }
        val rawNumber = aa.lookup("number")
        if (rawNumber is Int) {
            number = rawNumber
        }
        backtrace = aa.lookup("backtrace")
    }
    return ScopeErrorInfo(message, number, backtrace)
}

/**
 * Maps a parsed outcome to the throwable the child park delivers, or null for
 * a value outcome. This IS the child-side mapping (onKotlinScopeOutcome uses
 * it), public so the unit suite can pin it.
 */
public fun scopeThrowableForOutcome(envelope: ScopeEnvelope): Throwable? {
    if (envelope.status == SCOPE_STATUS_VALUE) {
        return null
    }
    if (envelope.status == SCOPE_STATUS_CLOSED) {
        return ScopeClosedException("owner scope closed")
    }
    return ScopeRequestException(envelope.message, envelope.number, envelope.backtrace)
}

/**
 * Recursive by-copy of AAs and arrays, floor-safe (plain loops, no roUtils).
 * Used by the same-component fast path so its args have identical by-copy
 * semantics to the wire path (where the field write itself copies). Node refs
 * copy by reference — matching the wire, where node refs survive every channel
 * (spike Q1b). Everything else (primitives, strings, other ro* components)
 * passes through unchanged.
 */
public fun deepCopyAA(value: Any?): Any? {
    if (value == null) {
        return null
    }
    // Node check FIRST: nodes are reference-preserving on the wire.
    if (value is RoSGNode) {
        return value
    }
    if (value is RoAssociativeArray) {
        val copy = RoAssociativeArray.create()
        val ks = value.keys()
        while (ks.count() > 0) {
            val k = "${ks.shift()}"
            copy.addReplace(k, deepCopyAA(value.lookup(k)))
        }
        return copy
    }
    if (value is RoArray) {
        val copy = RoArray.create(0, true)
        // append() is a non-destructive shallow copy of the source; draining
        // the WORK array (native arrays have no safe indexed read — the
        // documented count()/shift() pattern) leaves the source untouched.
        val work = RoArray.create(0, true)
        work.append(value)
        while (work.count() > 0) {
            copy.push(deepCopyAA(work.shift()))
        }
        return copy
    }
    return value
}
