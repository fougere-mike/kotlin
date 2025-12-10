function RoDeviceInfo_create_k_() as Object
    this = {}
    this.__type = "RoDeviceInfo"
    this.__proto = ["RoDeviceInfo"]
    this.__id = __kotlin_nextObjectId()
    this.getModel_k_ = RoDeviceInfo_getModel_k_
    this.getModelDisplayName_k_ = RoDeviceInfo_getModelDisplayName_k_
    this.getModelDetails_k_ = RoDeviceInfo_getModelDetails_k_
    this.getFriendlyName_k_ = RoDeviceInfo_getFriendlyName_k_
    this.getDeviceUniqueId_k_ = RoDeviceInfo_getDeviceUniqueId_k_
    this.getRIDA_k_ = RoDeviceInfo_getRIDA_k_
    this.isRIDADisabled_k_ = RoDeviceInfo_isRIDADisabled_k_
    this.getChannelClientId_k_ = RoDeviceInfo_getChannelClientId_k_
    this.getSoftwareVersion_k_ = RoDeviceInfo_getSoftwareVersion_k_
    this.getOSVersion_k_ = RoDeviceInfo_getOSVersion_k_
    this.getUIResolution_k_ = RoDeviceInfo_getUIResolution_k_
    this.getDisplayType_k_ = RoDeviceInfo_getDisplayType_k_
    this.getDisplayMode_k_ = RoDeviceInfo_getDisplayMode_k_
    this.getVideoMode_k_ = RoDeviceInfo_getVideoMode_k_
    this.getDisplayAspectRatio_k_ = RoDeviceInfo_getDisplayAspectRatio_k_
    this.getDisplayProperties_k_ = RoDeviceInfo_getDisplayProperties_k_
    this.getDisplaySize_k_ = RoDeviceInfo_getDisplaySize_k_
    this.getGraphicsPlatform_k_ = RoDeviceInfo_getGraphicsPlatform_k_
    this.getConnectionType_k_ = RoDeviceInfo_getConnectionType_k_
    this.getExternalIp_k_ = RoDeviceInfo_getExternalIp_k_
    this.getIPAddrs_k_ = RoDeviceInfo_getIPAddrs_k_
    this.getLinkStatus_k_ = RoDeviceInfo_getLinkStatus_k_
    this.hasInternetConnection_k_ = RoDeviceInfo_hasInternetConnection_k_
    this.getCurrentLocale_k_ = RoDeviceInfo_getCurrentLocale_k_
    this.getCountryCode_k_ = RoDeviceInfo_getCountryCode_k_
    this.getCaptionsMode_k_ = RoDeviceInfo_getCaptionsMode_k_
    this.getTimeZone_k_ = RoDeviceInfo_getTimeZone_k_
    this.getPreferredAudioLanguage_k_ = RoDeviceInfo_getPreferredAudioLanguage_k_
    this.canDecodeAudio_MapStrStr_k_ = RoDeviceInfo_canDecodeAudio_MapStrStr_k_
    this.canDecodeVideo_MapStrStr_k_ = RoDeviceInfo_canDecodeVideo_MapStrStr_k_
    this.getSupportedAudioCodecs_k_ = RoDeviceInfo_getSupportedAudioCodecs_k_
    this.hasFeature_Str_k_ = RoDeviceInfo_hasFeature_Str_k_
    this.getGeneralMemoryLevel_k_ = RoDeviceInfo_getGeneralMemoryLevel_k_
    this.getAudioOutputChannel_k_ = RoDeviceInfo_getAudioOutputChannel_k_
    this.isAutoAudioModeEnabled_k_ = RoDeviceInfo_isAutoAudioModeEnabled_k_
    this.isAudioGuideEnabled_k_ = RoDeviceInfo_isAudioGuideEnabled_k_
    this.isAdTrackingLimited_k_ = RoDeviceInfo_isAdTrackingLimited_k_
    this.getClockFormat_k_ = RoDeviceInfo_getClockFormat_k_
    this.get_native = RoDeviceInfo_get_native_k_
    this.native = CreateObject("roDeviceInfo")
    return this
end function

function RoDeviceInfo_getModel_k_() as String
    return m.get_native().GetModel()
end function

function RoDeviceInfo_getModelDisplayName_k_() as String
    return m.get_native().GetModelDisplayName()
end function

function RoDeviceInfo_getModelDetails_k_() as Object
    return m.get_native().GetModelDetails()
end function

function RoDeviceInfo_getFriendlyName_k_() as String
    return m.get_native().GetFriendlyName()
end function

function RoDeviceInfo_getDeviceUniqueId_k_() as String
    return m.get_native().GetDeviceUniqueId()
end function

function RoDeviceInfo_getRIDA_k_() as String
    return m.get_native().GetRIDA()
end function

function RoDeviceInfo_isRIDADisabled_k_() as Boolean
    return m.get_native().IsRIDADisabled()
end function

function RoDeviceInfo_getChannelClientId_k_() as String
    return m.get_native().GetChannelClientId()
end function

function RoDeviceInfo_getSoftwareVersion_k_() as String
    return m.get_native().GetVersion()
end function

function RoDeviceInfo_getOSVersion_k_() as Object
    return m.get_native().GetOSVersion()
end function

function RoDeviceInfo_getUIResolution_k_() as Object
    return m.get_native().GetUIResolution()
end function

function RoDeviceInfo_getDisplayType_k_() as String
    return m.get_native().GetDisplayType()
end function

function RoDeviceInfo_getDisplayMode_k_() as String
    return m.get_native().GetDisplayMode()
end function

function RoDeviceInfo_getVideoMode_k_() as String
    return m.get_native().GetVideoMode()
end function

function RoDeviceInfo_getDisplayAspectRatio_k_() as String
    return m.get_native().GetDisplayAspectRatio()
end function

function RoDeviceInfo_getDisplayProperties_k_() as Object
    return m.get_native().GetDisplayProperties()
end function

function RoDeviceInfo_getDisplaySize_k_() as Object
    return m.get_native().GetDisplaySize()
end function

function RoDeviceInfo_getGraphicsPlatform_k_() as String
    return m.get_native().GetGraphicsPlatform()
end function

function RoDeviceInfo_getConnectionType_k_() as String
    return m.get_native().GetConnectionType()
end function

function RoDeviceInfo_getExternalIp_k_() as String
    return m.get_native().GetExternalIp()
end function

function RoDeviceInfo_getIPAddrs_k_() as Object
    return m.get_native().GetIPAddrs()
end function

function RoDeviceInfo_getLinkStatus_k_() as Object
    return m.get_native().GetLinkStatus()
end function

function RoDeviceInfo_hasInternetConnection_k_() as Boolean
    status = m.getLinkStatus_k_()
    return invalid
end function

function RoDeviceInfo_getCurrentLocale_k_() as String
    return m.get_native().GetCurrentLocale()
end function

function RoDeviceInfo_getCountryCode_k_() as String
    return m.get_native().GetCountryCode()
end function

function RoDeviceInfo_getCaptionsMode_k_() as String
    return m.get_native().GetCaptionsMode()
end function

function RoDeviceInfo_getTimeZone_k_() as String
    return m.get_native().GetTimeZone()
end function

function RoDeviceInfo_getPreferredAudioLanguage_k_() as String
    return m.get_native().GetPreferredAudioLanguage()
end function

function RoDeviceInfo_canDecodeAudio_MapStrStr_k_(codec as Object) as Object
    return m.get_native().CanDecodeAudio(codec)
end function

function RoDeviceInfo_canDecodeVideo_MapStrStr_k_(video as Object) as Object
    return m.get_native().CanDecodeVideo(video)
end function

function RoDeviceInfo_getSupportedAudioCodecs_k_() as Object
    return m.get_native().GetSupportedAudioCodecs()
end function

function RoDeviceInfo_hasFeature_Str_k_(feature as String) as Boolean
    return m.get_native().HasFeature(feature)
end function

function RoDeviceInfo_getGeneralMemoryLevel_k_() as String
    return m.get_native().GetGeneralMemoryLevel()
end function

function RoDeviceInfo_getAudioOutputChannel_k_() as String
    return m.get_native().GetAudioOutputChannel()
end function

function RoDeviceInfo_isAutoAudioModeEnabled_k_() as Boolean
    return m.get_native().IsAutoAudioModeEnabled()
end function

function RoDeviceInfo_isAudioGuideEnabled_k_() as Boolean
    return m.get_native().IsAudioGuideEnabled()
end function

function RoDeviceInfo_isAdTrackingLimited_k_() as Boolean
    return m.get_native().IsRIDADisabled()
end function

function RoDeviceInfo_getClockFormat_k_() as String
    return m.get_native().GetClockFormat()
end function

function RoDeviceInfo_get_native_k_() as Object
    return m.native
end function

function RoAppInfo_create_k_() as Object
    this = {}
    this.__type = "RoAppInfo"
    this.__proto = ["RoAppInfo"]
    this.__id = __kotlin_nextObjectId()
    this.getID_k_ = RoAppInfo_getID_k_
    this.isDev_k_ = RoAppInfo_isDev_k_
    this.getTitle_k_ = RoAppInfo_getTitle_k_
    this.getVersion_k_ = RoAppInfo_getVersion_k_
    this.getSubtitle_k_ = RoAppInfo_getSubtitle_k_
    this.getDevID_k_ = RoAppInfo_getDevID_k_
    this.getValue_Str_k_ = RoAppInfo_getValue_Str_k_
    this.get_native = RoAppInfo_get_native_k_
    this.native = CreateObject("roAppInfo")
    return this
end function

function RoAppInfo_getID_k_() as String
    return m.get_native().GetID()
end function

function RoAppInfo_isDev_k_() as Boolean
    return m.get_native().IsDev()
end function

function RoAppInfo_getTitle_k_() as String
    return m.get_native().GetTitle()
end function

function RoAppInfo_getVersion_k_() as String
    return m.get_native().GetVersion()
end function

function RoAppInfo_getSubtitle_k_() as String
    return m.get_native().GetSubtitle()
end function

function RoAppInfo_getDevID_k_() as String
    return m.get_native().GetDevID()
end function

function RoAppInfo_getValue_Str_k_(key as String) as String
    return m.get_native().GetValue(key)
end function

function RoAppInfo_get_native_k_() as Object
    return m.native
end function
