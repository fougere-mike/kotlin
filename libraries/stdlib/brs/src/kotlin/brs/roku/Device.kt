/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsCreateObject
import kotlin.brs.BrsInline
import kotlin.brs.Dynamic

/**
 * Provides information about the Roku device.
 *
 * roDeviceInfo provides access to device model, firmware version, network
 * information, display properties, and other hardware/software details.
 *
 * Example usage:
 * ```kotlin
 * val deviceInfo = RoDeviceInfo.create()
 * println("Model: ${deviceInfo.getModel()}")
 * println("Version: ${deviceInfo.getVersion()}")
 * println("Display: ${deviceInfo.getDisplayMode()}")
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/rodeviceinfo.md">roDeviceInfo</a>
 */
public external interface RoDeviceInfo {
    // ==================== Device Identification ====================

    /**
     * Returns the model number of the Roku device.
     */
    public fun getModel(): String

    /**
     * Returns the model's descriptive name.
     */
    public fun getModelDisplayName(): String

    /**
     * Returns the model details as a map with keys like "VendorName", "ModelNumber".
     */
    public fun getModelDetails(): Dynamic

    /**
     * Returns the device's friendly name (user-configurable).
     */
    public fun getFriendlyName(): String

    /**
     * Returns the unique device ID.
     */
    public fun getDeviceUniqueId(): String

    /**
     * Returns the advertising ID (RIDA) for the device.
     *
     * Note: This may be reset by the user.
     */
    public fun getRIDA(): String

    /**
     * Checks if the user has disabled ad tracking.
     */
    public fun isRIDADisabled(): Boolean

    /**
     * Returns the channel client ID for analytics.
     */
    public fun getChannelClientId(): String

    // ==================== Software Information ====================

    /**
     * Returns the current Roku OS version string.
     */
    public fun getVersion(): String

    /**
     * Returns the OS version as a map with "major", "minor", "revision", "build" keys.
     */
    public fun getOSVersion(): Dynamic

    // ==================== Display Information ====================

    /**
     * Returns the UI resolution as a map with "width" and "height" keys.
     */
    public fun getUIResolution(): Dynamic

    /**
     * Returns the display type ("HDTV", "4K", etc.).
     */
    public fun getDisplayType(): String

    /**
     * Returns the display mode ("720p", "1080p", "2160p", etc.).
     */
    public fun getDisplayMode(): String

    /**
     * Returns the video mode ("480i", "480p", "720p", "1080i", "1080p", "2160p").
     */
    public fun getVideoMode(): String

    /**
     * Returns the display aspect ratio ("4x3" or "16x9").
     */
    public fun getDisplayAspectRatio(): String

    /**
     * Returns display properties as a map.
     */
    public fun getDisplayProperties(): Dynamic

    /**
     * Returns the display size in inches (diagonal).
     */
    public fun getDisplaySize(): Dynamic

    // ==================== Graphics Platform ====================

    /**
     * Returns the graphics platform ("opengl" or "directfb").
     */
    public fun getGraphicsPlatform(): String

    // ==================== Network Information ====================

    /**
     * Returns the connection type ("", "WiredConnection", "WifiConnection").
     */
    public fun getConnectionType(): String

    /**
     * Returns the external IP address.
     */
    public fun getExternalIp(): String

    /**
     * Returns all IP addresses as a map keyed by interface name.
     */
    public fun getIPAddrs(): Dynamic

    /**
     * Returns the link status as a map with "link" and potentially "internet" keys.
     */
    public fun getLinkStatus(): Dynamic

    // ==================== Locale Information ====================

    /**
     * Returns the current locale setting (e.g., "en_US").
     */
    public fun getCurrentLocale(): String

    /**
     * Returns the country code (e.g., "US").
     */
    public fun getCountryCode(): String

    /**
     * Returns the user's preferred caption mode.
     */
    public fun getCaptionsMode(): String

    /**
     * Returns the system time zone (e.g., "US/Eastern").
     */
    public fun getTimeZone(): String

    /**
     * Returns the user's preferred audio language.
     */
    public fun getPreferredAudioLanguage(): String

    // ==================== Audio/Video Capabilities ====================

    /**
     * Checks if the device supports the specified audio codec.
     *
     * @param codec Audio codec parameters to check.
     * @return Map with "result" key indicating support.
     */
    public fun canDecodeAudio(codec: Dynamic): Dynamic

    /**
     * Checks if the device supports the specified video format.
     *
     * @param video Video format parameters to check.
     * @return Map with "result" key indicating support.
     */
    public fun canDecodeVideo(video: Dynamic): Dynamic

    /**
     * Returns a list of supported audio codecs.
     */
    public fun getSupportedAudioCodecs(): Dynamic

    /**
     * Checks if the device has a specific feature.
     *
     * @param feature Feature name (e.g., "hdr10_playback", "dolby_vision_playback").
     */
    public fun hasFeature(feature: String): Boolean

    // ==================== Memory Information ====================

    /**
     * Returns the general memory level ("normal", "low", "critical").
     */
    public fun getGeneralMemoryLevel(): String

    // ==================== Power and Audio Output ====================

    /**
     * Returns the audio output channel type ("Stereo" or "5.1 surround").
     */
    public fun getAudioOutputChannel(): String

    /**
     * Checks if auto audio mode is enabled.
     */
    public fun isAutoAudioModeEnabled(): Boolean

    /**
     * Checks if audio guide (screen reader) is enabled.
     */
    public fun isAudioGuideEnabled(): Boolean

    // ==================== User Settings ====================

    /**
     * Returns the clock format preference (12h or 24h).
     */
    public fun getClockFormat(): String

    public companion object {
        /**
         * Creates a new roDeviceInfo instance.
         *
         * Compiles to: `CreateObject("roDeviceInfo")`
         *
         * @return A new RoDeviceInfo instance.
         */
        @BrsCreateObject("roDeviceInfo")
        public fun create(): RoDeviceInfo = definedExternally
    }
}

// ==================== Extension Functions ====================

@BrsInline("if obj[key] <> invalid then return obj[key] else return false")
private external fun brsLookupBool(obj: Dynamic, key: String): Boolean

/**
 * Checks if the device has an internet connection.
 */
public fun RoDeviceInfo.hasInternetConnection(): Boolean {
    val status = getLinkStatus()
    return brsLookupBool(status, "internet")
}

/**
 * Alias for getVersion() for backward compatibility.
 */
public fun RoDeviceInfo.getSoftwareVersion(): String = getVersion()

/**
 * Alias for isRIDADisabled() for backward compatibility.
 */
public fun RoDeviceInfo.isAdTrackingLimited(): Boolean = isRIDADisabled()

/**
 * Provides information about the running channel/application.
 *
 * roAppInfo provides access to application ID, version, title, and other
 * metadata defined in the channel's manifest.
 *
 * Example usage:
 * ```kotlin
 * val appInfo = RoAppInfo.create()
 * println("Title: ${appInfo.getTitle()}")
 * println("Version: ${appInfo.getVersion()}")
 * println("ID: ${appInfo.getID()}")
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/roappinfo.md">roAppInfo</a>
 */
public external interface RoAppInfo {
    /**
     * Returns the channel ID.
     */
    public fun getID(): String

    /**
     * Checks if the channel is running in development mode.
     */
    public fun isDev(): Boolean

    /**
     * Returns the channel title from the manifest.
     */
    public fun getTitle(): String

    /**
     * Returns the channel version from the manifest.
     */
    public fun getVersion(): String

    /**
     * Returns the channel subtitle from the manifest.
     */
    public fun getSubtitle(): String

    /**
     * Returns the developer ID.
     */
    public fun getDevID(): String

    /**
     * Returns a specific value from the manifest.
     *
     * @param key The manifest key to retrieve.
     * @return The value, or empty string if not found.
     */
    public fun getValue(key: String): String

    public companion object {
        /**
         * Creates a new roAppInfo instance.
         *
         * Compiles to: `CreateObject("roAppInfo")`
         *
         * @return A new RoAppInfo instance.
         */
        @BrsCreateObject("roAppInfo")
        public fun create(): RoAppInfo = definedExternally
    }
}
