/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlin.brs.roku

import kotlin.brs.*
import kotlin.js.definedExternally

// ============================================
// SceneGraph Media Nodes
// ============================================
// Note: Video, Audio, ContentNode are defined in scenegraph.kt

// ============================================
// Legacy Media Objects (non-SceneGraph)
// ============================================

/**
 * roAudioPlayer - plays audio streams and files.
 *
 * Used in legacy (non-SceneGraph) apps for audio playback.
 */
@BrsExternal
@BrsName("roAudioPlayer")
public external class RoAudioPlayer : RoInterface {
    public constructor()

    public fun setMessagePort(port: RoMessagePort)
    public fun addContent(content: RoAssociativeArray): Boolean
    public fun clearContent(): Boolean
    public fun play(): Boolean
    public fun stop(): Boolean
    public fun pause(): Boolean
    public fun resume(): Boolean
    public fun setLoop(loop: Boolean)
    public fun setNext(item: Int)
    public fun seek(offsetMs: Int): Boolean
    public fun setTimedMetaDataForKeys(keys: RoArray)
    public fun getPlaybackSpeed(): Int
    public fun setPlaybackSpeed(speed: Int)
    public fun getStreamUrl(): String
}

/**
 * roVideoPlayer - plays video streams and files.
 *
 * Used in legacy (non-SceneGraph) apps for video playback.
 */
@BrsExternal
@BrsName("roVideoPlayer")
public external class RoVideoPlayer : RoInterface {
    public constructor()

    public fun setMessagePort(port: RoMessagePort)
    public fun addContent(content: RoAssociativeArray): Boolean
    public fun clearContent(): Boolean
    public fun play(): Boolean
    public fun stop(): Boolean
    public fun pause(): Boolean
    public fun resume(): Boolean
    public fun setLoop(loop: Boolean)
    public fun setNext(item: Int)
    public fun seek(offsetMs: Int): Boolean
    public fun setPositionNotificationPeriod(period: Int)
    public fun setCGMS(level: Int): Boolean
    public fun setMaxVideoDecodeResolution(width: Int, height: Int)
    public fun getPlaybackSpeed(): Int
    public fun setPlaybackSpeed(speed: Int)
    public fun setDestinationRect(rect: RoAssociativeArray)
    public fun getStreamUrl(): String
    public fun enableTrickPlay(enable: Boolean)
    public fun enableCookies()
    public fun addHeader(name: String, value: String)
    public fun setTimedMetaDataForKeys(keys: RoArray)
}

/**
 * roAudioPlayerEvent - events from roAudioPlayer.
 */
@BrsExternal
@BrsName("roAudioPlayerEvent")
public external class RoAudioPlayerEvent : RoInterface {
    public fun getInt(): Int
    public fun getMessage(): String
    public fun getIndex(): Int
    public fun getData(): Any?
    public fun getInfo(): RoAssociativeArray
    public fun isStatusMessage(): Boolean
    public fun isFullResult(): Boolean
    public fun isPartialResult(): Boolean
    public fun isListItemSelected(): Boolean
    public fun isPaused(): Boolean
    public fun isResumed(): Boolean
    public fun isRequestFailed(): Boolean
    public fun isRequestSucceeded(): Boolean
    public fun isStreamStarted(): Boolean
    public fun isFormatDetected(): Boolean
    public fun isTimedMetaData(): Boolean
}

/**
 * roVideoPlayerEvent - events from roVideoPlayer.
 */
@BrsExternal
@BrsName("roVideoPlayerEvent")
public external class RoVideoPlayerEvent : RoInterface {
    public fun getInt(): Int
    public fun getMessage(): String
    public fun getIndex(): Int
    public fun getData(): Any?
    public fun getInfo(): RoAssociativeArray
    public fun isStatusMessage(): Boolean
    public fun isFullResult(): Boolean
    public fun isPartialResult(): Boolean
    public fun isListItemSelected(): Boolean
    public fun isPaused(): Boolean
    public fun isResumed(): Boolean
    public fun isRequestFailed(): Boolean
    public fun isRequestSucceeded(): Boolean
    public fun isStreamStarted(): Boolean
    public fun isPlaybackPosition(): Boolean
    public fun isFormatDetected(): Boolean
    public fun isDownloadSegmentInfo(): Boolean
    public fun isSegmentDownloadStarted(): Boolean
    public fun isTimedMetaData(): Boolean
    public fun isStreamSegmentInfo(): Boolean
    public fun isCaptionModeChanged(): Boolean
}

/**
 * roAudioResource - plays audio resources (short sounds).
 */
@BrsExternal
@BrsName("roAudioResource")
public external class RoAudioResource : RoInterface {
    public constructor(resource: String)

    public fun trigger(volume: Int, channel: Int = definedExternally): Boolean
    public fun stop(): Boolean
    public fun isPlaying(): Boolean
    public fun setMaxStreams(maxStreams: Int): Boolean
}

/**
 * roAudioGuide - text-to-speech for accessibility.
 */
@BrsExternal
@BrsName("roAudioGuide")
public external class RoAudioGuide : RoInterface {
    public constructor()

    public fun setMessagePort(port: RoMessagePort)
    public fun say(text: String, flush: Boolean = definedExternally, voice: String = definedExternally): Int
    public fun flush(): Boolean
    public fun silence(duration: Int): Int
}

/**
 * roMicrophone - captures audio from microphone (for voice search).
 */
@BrsExternal
@BrsName("roMicrophone")
public external class RoMicrophone : RoInterface {
    public constructor()

    public fun setMessagePort(port: RoMessagePort)
    public fun startRecording(): Boolean
    public fun stopRecording(): RoByteArray
    public fun canRecord(): Boolean
    public fun recordToFile(path: String, sampleRate: Int = definedExternally): Boolean
}

// ============================================
// Streaming/DRM
// ============================================

/**
 * roBitmap - bitmap image manipulation.
 */
@BrsExternal
@BrsName("roBitmap")
public external class RoBitmap : RoInterface {
    public constructor(options: RoAssociativeArray)

    public fun getWidth(): Int
    public fun getHeight(): Int
    public fun clear(color: Int)
    public fun getByteArray(x: Int, y: Int, width: Int, height: Int): RoByteArray
    public fun setAlphaEnable(enable: Boolean)
    public fun drawObject(x: Int, y: Int, source: RoBitmap): Boolean
    public fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, color: Int)
    public fun drawPoint(x: Int, y: Int, size: Float, color: Int)
    public fun drawRect(x: Int, y: Int, width: Int, height: Int, color: Int)
    public fun drawText(text: String, x: Int, y: Int, color: Int, font: RoInterface): Boolean
    public fun finish(): Boolean
}

/**
 * roFont - font for text rendering.
 */
@BrsExternal
@BrsName("roFont")
public external class RoFont : RoInterface {
    public constructor()

    public fun getDefaultFont(size: Int, bold: Boolean = definedExternally, italic: Boolean = definedExternally): RoFont
    public fun getDefaultFontSize(): Int
    public fun getOneLineHeight(): Int
    public fun getOneLineWidth(text: String, maxWidth: Int = definedExternally): Int
    public fun getAscent(): Int
    public fun getDescent(): Int
    public fun getMaxAdvance(): Int
}

/**
 * roFontRegistry - manages available fonts.
 */
@BrsExternal
@BrsName("roFontRegistry")
public external class RoFontRegistry : RoInterface {
    public constructor()

    public fun getFamilies(): RoArray
    public fun getFont(family: String, size: Int, bold: Boolean = definedExternally, italic: Boolean = definedExternally): RoFont
    public fun getDefaultFont(size: Int, bold: Boolean = definedExternally, italic: Boolean = definedExternally): RoFont
    public fun register(path: String): Boolean
}
