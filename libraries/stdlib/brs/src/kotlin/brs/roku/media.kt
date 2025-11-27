/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlin.brs.roku

import kotlin.brs.*

// ============================================
// SceneGraph Media Nodes
// ============================================

/**
 * Video node - plays video content.
 *
 * Primary video playback node in SceneGraph apps.
 */
@BrsExternal
public external class Video : Group {
    public var content: Node?
    public var control: String
    public var state: String
    public var position: Float
    public var duration: Float
    public var bufferingStatus: RoAssociativeArray
    public var videoFormat: String
    public var streamingSegment: RoAssociativeArray
    public var loop: Boolean
    public var mute: Boolean
    public var enableUI: Boolean
    public var enableTrickPlay: Boolean
    public var trickPlayBar: Node?
    public var retrievingBar: Node?
    public var bufferingBar: Node?
    public var width: Float
    public var height: Float
    public var maxVideoDecodeResolution: String
    public var globalCaptionMode: String
    public var subtitleConfig: RoAssociativeArray
    public var audioTrack: Int
    public var notificationInterval: Float
    public var timedMetaDataSelectionKeys: RoArray
    public var timedMetaData: RoAssociativeArray
    public var contentIsPlaylist: Boolean
    public var disableScreenSaver: Boolean

    public fun seek(position: Int)
    public fun pause()
    public fun resume()
}

/**
 * Audio node - plays audio content.
 *
 * Primary audio playback node in SceneGraph apps.
 */
@BrsExternal
public external class Audio : Node {
    public var content: Node?
    public var contentIndex: Int
    public var control: String
    public var state: String
    public var loop: Boolean
    public var seek: Int
    public var timedMetaDataSelectionKeys: RoArray
    public var timedMetaData: RoAssociativeArray
    public var streamInfo: RoAssociativeArray
    public var notificationInterval: Float

    public fun pause()
    public fun resume()
}

/**
 * ContentNode - content metadata container.
 *
 * Used to hold metadata for media items.
 */
@BrsExternal
public external class ContentNode : Node {
    public var title: String
    public var titleSeason: String
    public var description: String
    public var rating: String
    public var starRating: Int
    public var userStarRating: Int
    public var releaseDate: String
    public var length: Int
    public var numEpisodes: Int
    public var hdBranded: Boolean
    public var hdBrandDisplayMode: String
    public var isHD: Boolean
    public var live: Boolean
    public var url: String
    public var streamFormat: String
    public var contentType: String
    public var sdPosterUrl: String
    public var hdPosterUrl: String
    public var fhdPosterUrl: String
    public var shortDescriptionLine1: String
    public var shortDescriptionLine2: String
    public var actors: RoArray
    public var directors: RoArray
    public var categories: RoArray
    public var genres: RoArray
    public var subtitleConfig: RoAssociativeArray
    public var playStart: Int
    public var playDuration: Int
    public var bookmarkPosition: Int
    public var handlerConfigVideo: RoAssociativeArray
    public var handlerConfigAudio: RoAssociativeArray
}

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

    public fun trigger(volume: Int, channel: Int = 0): Boolean
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
    public fun say(text: String, flush: Boolean = false, voice: String = ""): Int
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
    public fun recordToFile(path: String, sampleRate: Int = 16000): Boolean
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

    public fun getDefaultFont(size: Int, bold: Boolean = false, italic: Boolean = false): RoFont
    public fun getDefaultFontSize(): Int
    public fun getOneLineHeight(): Int
    public fun getOneLineWidth(text: String, maxWidth: Int = 0): Int
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
    public fun getFont(family: String, size: Int, bold: Boolean = false, italic: Boolean = false): RoFont
    public fun getDefaultFont(size: Int, bold: Boolean = false, italic: Boolean = false): RoFont
    public fun register(path: String): Boolean
}
