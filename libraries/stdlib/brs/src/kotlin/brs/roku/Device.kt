/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

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
 * val deviceInfo = RoDeviceInfo()
 * println("Model: ${deviceInfo.getModel()}")
 * println("Version: ${deviceInfo.getSoftwareVersion()}")
 * println("Display: ${deviceInfo.getDisplayMode()}")
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/rodeviceinfo.md">roDeviceInfo</a>
 */
public class RoDeviceInfo {

    private val native: Dynamic

    /**
     * Creates a new roDeviceInfo instance.
     */
    public constructor() {
        native = brsCreateDeviceInfo()
    }

    // ==================== Device Identification ====================

    /**
     * Returns the model number of the Roku device.
     */
    public fun getModel(): String = brsGetModel(native)

    /**
     * Returns the model's descriptive name.
     */
    public fun getModelDisplayName(): String = brsGetModelDisplayName(native)

    /**
     * Returns the model details as a map with keys like "VendorName", "ModelNumber".
     */
    public fun getModelDetails(): Dynamic = brsGetModelDetails(native)

    /**
     * Returns the device's friendly name (user-configurable).
     */
    public fun getFriendlyName(): String = brsGetFriendlyName(native)

    /**
     * Returns the unique device ID.
     */
    public fun getDeviceUniqueId(): String = brsGetDeviceUniqueId(native)

    /**
     * Returns the advertising ID (RIDA) for the device.
     *
     * Note: This may be reset by the user.
     */
    public fun getRIDA(): String = brsGetRIDA(native)

    /**
     * Checks if the user has disabled ad tracking.
     */
    public fun isRIDADisabled(): Boolean = brsIsRIDADisabled(native)

    /**
     * Returns the channel client ID for analytics.
     */
    public fun getChannelClientId(): String = brsGetChannelClientId(native)

    // ==================== Software Information ====================

    /**
     * Returns the current Roku OS version string.
     */
    public fun getSoftwareVersion(): String = brsGetVersion(native)

    /**
     * Returns the OS version as a map with "major", "minor", "revision", "build" keys.
     */
    public fun getOSVersion(): Dynamic = brsGetOSVersion(native)

    // ==================== Display Information ====================

    /**
     * Returns the UI resolution as a map with "width" and "height" keys.
     */
    public fun getUIResolution(): Dynamic = brsGetUIResolution(native)

    /**
     * Returns the display type ("HDTV", "4K", etc.).
     */
    public fun getDisplayType(): String = brsGetDisplayType(native)

    /**
     * Returns the display mode ("720p", "1080p", "2160p", etc.).
     */
    public fun getDisplayMode(): String = brsGetDisplayMode(native)

    /**
     * Returns the video mode ("480i", "480p", "720p", "1080i", "1080p", "2160p").
     */
    public fun getVideoMode(): String = brsGetVideoMode(native)

    /**
     * Returns the display aspect ratio ("4x3" or "16x9").
     */
    public fun getDisplayAspectRatio(): String = brsGetDisplayAspectRatio(native)

    /**
     * Returns display properties as a map.
     */
    public fun getDisplayProperties(): Dynamic = brsGetDisplayProperties(native)

    /**
     * Returns the display size in inches (diagonal).
     */
    public fun getDisplaySize(): Dynamic = brsGetDisplaySize(native)

    // ==================== Graphics Platform ====================

    /**
     * Returns the graphics platform ("opengl" or "directfb").
     */
    public fun getGraphicsPlatform(): String = brsGetGraphicsPlatform(native)

    // ==================== Network Information ====================

    /**
     * Returns the connection type ("", "WiredConnection", "WifiConnection").
     */
    public fun getConnectionType(): String = brsGetConnectionType(native)

    /**
     * Returns the external IP address.
     */
    public fun getExternalIp(): String = brsGetExternalIp(native)

    /**
     * Returns all IP addresses as a map keyed by interface name.
     */
    public fun getIPAddrs(): Dynamic = brsGetIPAddrs(native)

    /**
     * Returns the link status as a map with "link" and potentially "internet" keys.
     */
    public fun getLinkStatus(): Dynamic = brsGetLinkStatus(native)

    /**
     * Checks if the device has an internet connection.
     */
    public fun hasInternetConnection(): Boolean {
        val status = getLinkStatus()
        return brsLookupBool(status, "internet")
    }

    // ==================== Locale Information ====================

    /**
     * Returns the current locale setting (e.g., "en_US").
     */
    public fun getCurrentLocale(): String = brsGetCurrentLocale(native)

    /**
     * Returns the country code (e.g., "US").
     */
    public fun getCountryCode(): String = brsGetCountryCode(native)

    /**
     * Returns the user's preferred caption mode.
     */
    public fun getCaptionsMode(): String = brsGetCaptionsMode(native)

    /**
     * Returns the system time zone (e.g., "US/Eastern").
     */
    public fun getTimeZone(): String = brsGetTimeZone(native)

    /**
     * Returns the user's preferred audio language.
     */
    public fun getPreferredAudioLanguage(): String = brsGetPreferredAudioLanguage(native)

    // ==================== Audio/Video Capabilities ====================

    /**
     * Checks if the device supports the specified audio codec.
     *
     * @param codec Audio codec to check (e.g., "aac", "ac3", "eac3").
     * @return Map with "result" key indicating support.
     */
    public fun canDecodeAudio(codec: Map<String, String>): Dynamic = brsCanDecodeAudio(native, codec)

    /**
     * Checks if the device supports the specified video format.
     *
     * @param video Video format parameters to check.
     * @return Map with "result" key indicating support.
     */
    public fun canDecodeVideo(video: Map<String, String>): Dynamic = brsCanDecodeVideo(native, video)

    /**
     * Returns a list of supported audio codecs.
     */
    public fun getSupportedAudioCodecs(): Dynamic = brsGetSupportedAudioCodecs(native)

    /**
     * Checks if the device has a specific feature.
     *
     * @param feature Feature name (e.g., "hdr10_playback", "dolby_vision_playback").
     */
    public fun hasFeature(feature: String): Boolean = brsHasFeature(native, feature)

    // ==================== Memory Information ====================

    /**
     * Returns the general memory level ("normal", "low", "critical").
     */
    public fun getGeneralMemoryLevel(): String = brsGetGeneralMemoryLevel(native)

    // ==================== Power and Audio Output ====================

    /**
     * Returns the audio output channel type ("Stereo" or "5.1 surround").
     */
    public fun getAudioOutputChannel(): String = brsGetAudioOutputChannel(native)

    /**
     * Checks if auto audio mode is enabled.
     */
    public fun isAutoAudioModeEnabled(): Boolean = brsIsAutoAudioModeEnabled(native)

    /**
     * Checks if audio guide (screen reader) is enabled.
     */
    public fun isAudioGuideEnabled(): Boolean = brsIsAudioGuideEnabled(native)

    // ==================== User Settings ====================

    /**
     * Checks if "Limit ad tracking" is enabled.
     */
    public fun isAdTrackingLimited(): Boolean = brsIsRIDADisabled(native)

    /**
     * Returns the clock format preference (12h or 24h).
     */
    public fun getClockFormat(): String = brsGetClockFormat(native)

    // ==================== BrightScript Intrinsics ====================

    @BrsInline("return CreateObject(\"roDeviceInfo\")")
    private external fun brsCreateDeviceInfo(): Dynamic

    @BrsInline("return di.GetModel()")
    private external fun brsGetModel(di: Dynamic): String

    @BrsInline("return di.GetModelDisplayName()")
    private external fun brsGetModelDisplayName(di: Dynamic): String

    @BrsInline("return di.GetModelDetails()")
    private external fun brsGetModelDetails(di: Dynamic): Dynamic

    @BrsInline("return di.GetFriendlyName()")
    private external fun brsGetFriendlyName(di: Dynamic): String

    @BrsInline("return di.GetDeviceUniqueId()")
    private external fun brsGetDeviceUniqueId(di: Dynamic): String

    @BrsInline("return di.GetRIDA()")
    private external fun brsGetRIDA(di: Dynamic): String

    @BrsInline("return di.IsRIDADisabled()")
    private external fun brsIsRIDADisabled(di: Dynamic): Boolean

    @BrsInline("return di.GetChannelClientId()")
    private external fun brsGetChannelClientId(di: Dynamic): String

    @BrsInline("return di.GetVersion()")
    private external fun brsGetVersion(di: Dynamic): String

    @BrsInline("return di.GetOSVersion()")
    private external fun brsGetOSVersion(di: Dynamic): Dynamic

    @BrsInline("return di.GetUIResolution()")
    private external fun brsGetUIResolution(di: Dynamic): Dynamic

    @BrsInline("return di.GetDisplayType()")
    private external fun brsGetDisplayType(di: Dynamic): String

    @BrsInline("return di.GetDisplayMode()")
    private external fun brsGetDisplayMode(di: Dynamic): String

    @BrsInline("return di.GetVideoMode()")
    private external fun brsGetVideoMode(di: Dynamic): String

    @BrsInline("return di.GetDisplayAspectRatio()")
    private external fun brsGetDisplayAspectRatio(di: Dynamic): String

    @BrsInline("return di.GetDisplayProperties()")
    private external fun brsGetDisplayProperties(di: Dynamic): Dynamic

    @BrsInline("return di.GetDisplaySize()")
    private external fun brsGetDisplaySize(di: Dynamic): Dynamic

    @BrsInline("return di.GetGraphicsPlatform()")
    private external fun brsGetGraphicsPlatform(di: Dynamic): String

    @BrsInline("return di.GetConnectionType()")
    private external fun brsGetConnectionType(di: Dynamic): String

    @BrsInline("return di.GetExternalIp()")
    private external fun brsGetExternalIp(di: Dynamic): String

    @BrsInline("return di.GetIPAddrs()")
    private external fun brsGetIPAddrs(di: Dynamic): Dynamic

    @BrsInline("return di.GetLinkStatus()")
    private external fun brsGetLinkStatus(di: Dynamic): Dynamic

    @BrsInline("return di.GetCurrentLocale()")
    private external fun brsGetCurrentLocale(di: Dynamic): String

    @BrsInline("return di.GetCountryCode()")
    private external fun brsGetCountryCode(di: Dynamic): String

    @BrsInline("return di.GetCaptionsMode()")
    private external fun brsGetCaptionsMode(di: Dynamic): String

    @BrsInline("return di.GetTimeZone()")
    private external fun brsGetTimeZone(di: Dynamic): String

    @BrsInline("return di.GetPreferredAudioLanguage()")
    private external fun brsGetPreferredAudioLanguage(di: Dynamic): String

    @BrsInline("return di.CanDecodeAudio(codec)")
    private external fun brsCanDecodeAudio(di: Dynamic, codec: Map<String, String>): Dynamic

    @BrsInline("return di.CanDecodeVideo(video)")
    private external fun brsCanDecodeVideo(di: Dynamic, video: Map<String, String>): Dynamic

    @BrsInline("return di.GetSupportedAudioCodecs()")
    private external fun brsGetSupportedAudioCodecs(di: Dynamic): Dynamic

    @BrsInline("return di.HasFeature(feature)")
    private external fun brsHasFeature(di: Dynamic, feature: String): Boolean

    @BrsInline("return di.GetGeneralMemoryLevel()")
    private external fun brsGetGeneralMemoryLevel(di: Dynamic): String

    @BrsInline("return di.GetAudioOutputChannel()")
    private external fun brsGetAudioOutputChannel(di: Dynamic): String

    @BrsInline("return di.IsAutoAudioModeEnabled()")
    private external fun brsIsAutoAudioModeEnabled(di: Dynamic): Boolean

    @BrsInline("return di.IsAudioGuideEnabled()")
    private external fun brsIsAudioGuideEnabled(di: Dynamic): Boolean

    @BrsInline("return di.GetClockFormat()")
    private external fun brsGetClockFormat(di: Dynamic): String

    @BrsInline("if obj[key] <> invalid then return obj[key] else return false")
    private external fun brsLookupBool(obj: Dynamic, key: String): Boolean
}

/**
 * Provides information about the running channel/application.
 *
 * roAppInfo provides access to application ID, version, title, and other
 * metadata defined in the channel's manifest.
 *
 * Example usage:
 * ```kotlin
 * val appInfo = RoAppInfo()
 * println("Title: ${appInfo.getTitle()}")
 * println("Version: ${appInfo.getVersion()}")
 * println("ID: ${appInfo.getID()}")
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/roappinfo.md">roAppInfo</a>
 */
public class RoAppInfo {

    private val native: Dynamic

    /**
     * Creates a new roAppInfo instance.
     */
    public constructor() {
        native = brsCreateAppInfo()
    }

    /**
     * Returns the channel ID.
     */
    public fun getID(): String = brsGetID(native)

    /**
     * Checks if the channel is running in development mode.
     */
    public fun isDev(): Boolean = brsIsDev(native)

    /**
     * Returns the channel title from the manifest.
     */
    public fun getTitle(): String = brsGetTitle(native)

    /**
     * Returns the channel version from the manifest.
     */
    public fun getVersion(): String = brsGetVersion(native)

    /**
     * Returns the channel subtitle from the manifest.
     */
    public fun getSubtitle(): String = brsGetSubtitle(native)

    /**
     * Returns the developer ID.
     */
    public fun getDevID(): String = brsGetDevID(native)

    /**
     * Returns a specific value from the manifest.
     *
     * @param key The manifest key to retrieve.
     * @return The value, or empty string if not found.
     */
    public fun getValue(key: String): String = brsGetValue(native, key)

    // ==================== BrightScript Intrinsics ====================

    @BrsInline("return CreateObject(\"roAppInfo\")")
    private external fun brsCreateAppInfo(): Dynamic

    @BrsInline("return ai.GetID()")
    private external fun brsGetID(ai: Dynamic): String

    @BrsInline("return ai.IsDev()")
    private external fun brsIsDev(ai: Dynamic): Boolean

    @BrsInline("return ai.GetTitle()")
    private external fun brsGetTitle(ai: Dynamic): String

    @BrsInline("return ai.GetVersion()")
    private external fun brsGetVersion(ai: Dynamic): String

    @BrsInline("return ai.GetSubtitle()")
    private external fun brsGetSubtitle(ai: Dynamic): String

    @BrsInline("return ai.GetDevID()")
    private external fun brsGetDevID(ai: Dynamic): String

    @BrsInline("return ai.GetValue(key)")
    private external fun brsGetValue(ai: Dynamic, key: String): String
}
