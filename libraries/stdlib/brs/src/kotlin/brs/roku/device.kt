/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlin.brs.roku

import kotlin.brs.*

/**
 * BrightScript roDeviceInfo - provides device information.
 */
@BrsExternal
public external class RoDeviceInfo : RoInterface {
    public constructor()

    // Device identification
    public fun getModel(): String
    public fun getModelDisplayName(): String
    public fun getModelType(): String
    public fun getModelDetails(): RoAssociativeArray
    public fun getFriendlyName(): String
    public fun getOSVersion(): RoAssociativeArray
    public fun getVersion(): String
    public fun getRIDA(): String
    public fun isRIDADisabled(): Boolean
    public fun getChannelClientId(): String
    public fun getUserCountryCode(): String

    // Display information
    public fun getDisplayType(): String
    public fun getDisplayMode(): String
    public fun getDisplayAspectRatio(): String
    public fun getDisplaySize(): RoAssociativeArray
    public fun getDisplayProperties(): RoAssociativeArray
    public fun getUIResolution(): RoAssociativeArray
    public fun getGraphicsPlatform(): String
    public fun getSupportedGraphicsResolutions(): RoArray

    // Video capabilities
    public fun canDecodeVideo(codec: RoAssociativeArray): RoAssociativeArray
    public fun getVideoMode(): String
    public fun getSupportedVideoModes(): RoArray
    public fun get4kVideoMode(): String

    // Audio capabilities
    public fun canDecodeAudio(codec: RoAssociativeArray): RoAssociativeArray
    public fun getSoundEffectsVolume(): Int
    public fun getAudioOutputChannel(): String
    public fun getAudioDecoderInfo(): RoAssociativeArray
    public fun getAudioGuideEnabled(): Boolean
    public fun isAudioGuideEnabled(): Boolean

    // Network
    public fun getLinkStatus(): Boolean
    public fun enableLinkStatusEvent(enable: Boolean): Boolean
    public fun getConnectionType(): String
    public fun getExternalIp(): String
    public fun getIPAddrs(): RoAssociativeArray
    public fun getConnectionInfo(): RoAssociativeArray
    public fun getInternetStatus(): Boolean
    public fun enableInternetStatusEvent(enable: Boolean): Boolean

    // Features and capabilities
    public fun hasFeature(feature: String): Boolean
    public fun getDrmInfo(): RoAssociativeArray
    public fun getDrmInfoEx(): RoAssociativeArray
    public fun getCaptionsMode(): String
    public fun setCaptionsMode(mode: String): Boolean
    public fun getCaptionsOption(option: String): String
    public fun enableCaptionsChangeEvent(enable: Boolean): Boolean
    public fun getClockFormat(): String
    public fun timeSinceLastKeypress(): Int
    public fun enableLowGeneralMemoryEvent(enable: Boolean): Boolean
    public fun getGeneralMemoryLevel(): String
    public fun isStoreDemoMode(): Boolean

    // HDMI
    public fun enableHDMIHotplugEvent(enable: Boolean): Boolean
    public fun isHDMIHotplugDetected(): Boolean

    // Power state
    public fun enableScreensaverExitedEvent(enable: Boolean): Boolean
    public fun enableAppFocusEvent(enable: Boolean): Boolean

    // Random
    public fun getRandomUUID(): String
}

/**
 * BrightScript roAppInfo - provides app information.
 */
@BrsExternal
public external class RoAppInfo : RoInterface {
    public constructor()

    public fun getID(): String
    public fun isDev(): Boolean
    public fun getVersion(): String
    public fun getTitle(): String
    public fun getSubtitle(): String
    public fun getDevID(): String
    public fun getValue(key: String): String
}

/**
 * BrightScript roAppManager - manages the app.
 */
@BrsExternal
public external class RoAppManager : RoInterface {
    public constructor()

    public fun getScreensaverTimeout(): Int
    public fun setScreensaverTimeout(seconds: Int): Boolean
    public fun isScreensaverEnabled(): Boolean
    public fun setAutomaticAudioGuideEnabled(enabled: Boolean): Boolean
    public fun clearVoiceTextInput(): Boolean
    public fun setUserSignedIn(signedIn: Boolean): Boolean
    public fun getUpTime(): RoTimespan
}

/**
 * BrightScript roInput - receives deep link and input events.
 */
@BrsExternal
public external class RoInput : RoInterface {
    public constructor()

    public fun setMessagePort(port: RoMessagePort)
    public fun enableTransportEvents()
    public fun eventResponse(event: RoAssociativeArray): Boolean
}

/**
 * BrightScript roRegistry - persistent storage.
 */
@BrsExternal
public external class RoRegistry : RoInterface {
    public constructor()

    public fun getSectionList(): RoArray
    public fun delete(section: String): Boolean
    public fun flush(): Boolean
    public fun getSpaceAvailable(): Int
}

/**
 * BrightScript roRegistrySection - a section within the registry.
 */
@BrsExternal
public external class RoRegistrySection : RoInterface {
    public constructor(section: String)

    public fun read(key: String): String
    public fun write(key: String, value: String): Boolean
    public fun delete(key: String): Boolean
    public fun exists(key: String): Boolean
    public fun flush(): Boolean
    public fun getKeyList(): RoArray
}

/**
 * BrightScript roFileSystem - file system access.
 */
@BrsExternal
public external class RoFileSystem : RoInterface {
    public constructor()

    public fun getVolumeList(): RoArray
    public fun getDirectoryListing(path: String): RoArray
    public fun find(path: String, pattern: String): RoArray
    public fun findRecursive(path: String, pattern: String): RoArray
    public fun exists(path: String): Boolean
    public fun stat(path: String): RoAssociativeArray
    public fun createDirectory(path: String): Boolean
    public fun delete(path: String): Boolean
    public fun copyFile(source: String, dest: String): Boolean
    public fun rename(source: String, dest: String): Boolean
    public fun match(pattern: String, text: String): Boolean
    public fun getVolumeInfo(path: String): RoAssociativeArray
}

/**
 * BrightScript roPath - path manipulation.
 */
@BrsExternal
public external class RoPath : RoInterface {
    public constructor(path: String)

    public fun setString(path: String): Boolean
    public fun getString(): String
    public fun isValid(): Boolean
    public fun split(): RoAssociativeArray
    public fun change(newPath: String): Boolean
}
