function RoDeviceInfo_create_RoDeviceInfo_k_() as Object
    this = {}
    this.__type = "RoDeviceInfo"
    this.__proto = ["RoDeviceInfo"]
    this.getModel_Str_k_ = RoDeviceInfo_getModel_Str_k_
    this.getModelDisplayName_Str_k_ = RoDeviceInfo_getModelDisplayName_Str_k_
    this.getModelDetails_Dynamic_k_ = RoDeviceInfo_getModelDetails_Dynamic_k_
    this.getFriendlyName_Str_k_ = RoDeviceInfo_getFriendlyName_Str_k_
    this.getDeviceUniqueId_Str_k_ = RoDeviceInfo_getDeviceUniqueId_Str_k_
    this.getRIDA_Str_k_ = RoDeviceInfo_getRIDA_Str_k_
    this.isRIDADisabled_Z_k_ = RoDeviceInfo_isRIDADisabled_Z_k_
    this.getChannelClientId_Str_k_ = RoDeviceInfo_getChannelClientId_Str_k_
    this.getSoftwareVersion_Str_k_ = RoDeviceInfo_getSoftwareVersion_Str_k_
    this.getOSVersion_Dynamic_k_ = RoDeviceInfo_getOSVersion_Dynamic_k_
    this.getUIResolution_Dynamic_k_ = RoDeviceInfo_getUIResolution_Dynamic_k_
    this.getDisplayType_Str_k_ = RoDeviceInfo_getDisplayType_Str_k_
    this.getDisplayMode_Str_k_ = RoDeviceInfo_getDisplayMode_Str_k_
    this.getVideoMode_Str_k_ = RoDeviceInfo_getVideoMode_Str_k_
    this.getDisplayAspectRatio_Str_k_ = RoDeviceInfo_getDisplayAspectRatio_Str_k_
    this.getDisplayProperties_Dynamic_k_ = RoDeviceInfo_getDisplayProperties_Dynamic_k_
    this.getDisplaySize_Dynamic_k_ = RoDeviceInfo_getDisplaySize_Dynamic_k_
    this.getGraphicsPlatform_Str_k_ = RoDeviceInfo_getGraphicsPlatform_Str_k_
    this.getConnectionType_Str_k_ = RoDeviceInfo_getConnectionType_Str_k_
    this.getExternalIp_Str_k_ = RoDeviceInfo_getExternalIp_Str_k_
    this.getIPAddrs_Dynamic_k_ = RoDeviceInfo_getIPAddrs_Dynamic_k_
    this.getLinkStatus_Dynamic_k_ = RoDeviceInfo_getLinkStatus_Dynamic_k_
    this.hasInternetConnection_Z_k_ = RoDeviceInfo_hasInternetConnection_Z_k_
    this.getCurrentLocale_Str_k_ = RoDeviceInfo_getCurrentLocale_Str_k_
    this.getCountryCode_Str_k_ = RoDeviceInfo_getCountryCode_Str_k_
    this.getCaptionsMode_Str_k_ = RoDeviceInfo_getCaptionsMode_Str_k_
    this.getTimeZone_Str_k_ = RoDeviceInfo_getTimeZone_Str_k_
    this.getPreferredAudioLanguage_Str_k_ = RoDeviceInfo_getPreferredAudioLanguage_Str_k_
    this.canDecodeAudio_MapStrStr_Dynamic_k_ = RoDeviceInfo_canDecodeAudio_MapStrStr_Dynamic_k_
    this.canDecodeVideo_MapStrStr_Dynamic_k_ = RoDeviceInfo_canDecodeVideo_MapStrStr_Dynamic_k_
    this.getSupportedAudioCodecs_Dynamic_k_ = RoDeviceInfo_getSupportedAudioCodecs_Dynamic_k_
    this.hasFeature_Str_Z_k_ = RoDeviceInfo_hasFeature_Str_Z_k_
    this.getGeneralMemoryLevel_Str_k_ = RoDeviceInfo_getGeneralMemoryLevel_Str_k_
    this.getAudioOutputChannel_Str_k_ = RoDeviceInfo_getAudioOutputChannel_Str_k_
    this.isAutoAudioModeEnabled_Z_k_ = RoDeviceInfo_isAutoAudioModeEnabled_Z_k_
    this.isAudioGuideEnabled_Z_k_ = RoDeviceInfo_isAudioGuideEnabled_Z_k_
    this.isAdTrackingLimited_Z_k_ = RoDeviceInfo_isAdTrackingLimited_Z_k_
    this.getClockFormat_Str_k_ = RoDeviceInfo_getClockFormat_Str_k_
    this.get_native = RoDeviceInfo_get_native_Dynamic_k_
    this.native = CreateObject("roDeviceInfo")
    return this
end function

function RoDeviceInfo_getModel_Str_k_() as String
    return m.get_native().GetModel()
end function

function RoDeviceInfo_getModelDisplayName_Str_k_() as String
    return m.get_native().GetModelDisplayName()
end function

function RoDeviceInfo_getModelDetails_Dynamic_k_() as Object
    return m.get_native().GetModelDetails()
end function

function RoDeviceInfo_getFriendlyName_Str_k_() as String
    return m.get_native().GetFriendlyName()
end function

function RoDeviceInfo_getDeviceUniqueId_Str_k_() as String
    return m.get_native().GetDeviceUniqueId()
end function

function RoDeviceInfo_getRIDA_Str_k_() as String
    return m.get_native().GetRIDA()
end function

function RoDeviceInfo_isRIDADisabled_Z_k_() as Boolean
    return m.get_native().IsRIDADisabled()
end function

function RoDeviceInfo_getChannelClientId_Str_k_() as String
    return m.get_native().GetChannelClientId()
end function

function RoDeviceInfo_getSoftwareVersion_Str_k_() as String
    return m.get_native().GetVersion()
end function

function RoDeviceInfo_getOSVersion_Dynamic_k_() as Object
    return m.get_native().GetOSVersion()
end function

function RoDeviceInfo_getUIResolution_Dynamic_k_() as Object
    return m.get_native().GetUIResolution()
end function

function RoDeviceInfo_getDisplayType_Str_k_() as String
    return m.get_native().GetDisplayType()
end function

function RoDeviceInfo_getDisplayMode_Str_k_() as String
    return m.get_native().GetDisplayMode()
end function

function RoDeviceInfo_getVideoMode_Str_k_() as String
    return m.get_native().GetVideoMode()
end function

function RoDeviceInfo_getDisplayAspectRatio_Str_k_() as String
    return m.get_native().GetDisplayAspectRatio()
end function

function RoDeviceInfo_getDisplayProperties_Dynamic_k_() as Object
    return m.get_native().GetDisplayProperties()
end function

function RoDeviceInfo_getDisplaySize_Dynamic_k_() as Object
    return m.get_native().GetDisplaySize()
end function

function RoDeviceInfo_getGraphicsPlatform_Str_k_() as String
    return m.get_native().GetGraphicsPlatform()
end function

function RoDeviceInfo_getConnectionType_Str_k_() as String
    return m.get_native().GetConnectionType()
end function

function RoDeviceInfo_getExternalIp_Str_k_() as String
    return m.get_native().GetExternalIp()
end function

function RoDeviceInfo_getIPAddrs_Dynamic_k_() as Object
    return m.get_native().GetIPAddrs()
end function

function RoDeviceInfo_getLinkStatus_Dynamic_k_() as Object
    return m.get_native().GetLinkStatus()
end function

function RoDeviceInfo_hasInternetConnection_Z_k_() as Boolean
    status = m.getLinkStatus_Dynamic_k_()
    return invalid
end function

function RoDeviceInfo_getCurrentLocale_Str_k_() as String
    return m.get_native().GetCurrentLocale()
end function

function RoDeviceInfo_getCountryCode_Str_k_() as String
    return m.get_native().GetCountryCode()
end function

function RoDeviceInfo_getCaptionsMode_Str_k_() as String
    return m.get_native().GetCaptionsMode()
end function

function RoDeviceInfo_getTimeZone_Str_k_() as String
    return m.get_native().GetTimeZone()
end function

function RoDeviceInfo_getPreferredAudioLanguage_Str_k_() as String
    return m.get_native().GetPreferredAudioLanguage()
end function

function RoDeviceInfo_canDecodeAudio_MapStrStr_Dynamic_k_(codec as Object) as Object
    return m.get_native().CanDecodeAudio(codec)
end function

function RoDeviceInfo_canDecodeVideo_MapStrStr_Dynamic_k_(video as Object) as Object
    return m.get_native().CanDecodeVideo(video)
end function

function RoDeviceInfo_getSupportedAudioCodecs_Dynamic_k_() as Object
    return m.get_native().GetSupportedAudioCodecs()
end function

function RoDeviceInfo_hasFeature_Str_Z_k_(feature as String) as Boolean
    return m.get_native().HasFeature(feature)
end function

function RoDeviceInfo_getGeneralMemoryLevel_Str_k_() as String
    return m.get_native().GetGeneralMemoryLevel()
end function

function RoDeviceInfo_getAudioOutputChannel_Str_k_() as String
    return m.get_native().GetAudioOutputChannel()
end function

function RoDeviceInfo_isAutoAudioModeEnabled_Z_k_() as Boolean
    return m.get_native().IsAutoAudioModeEnabled()
end function

function RoDeviceInfo_isAudioGuideEnabled_Z_k_() as Boolean
    return m.get_native().IsAudioGuideEnabled()
end function

function RoDeviceInfo_isAdTrackingLimited_Z_k_() as Boolean
    return m.get_native().IsRIDADisabled()
end function

function RoDeviceInfo_getClockFormat_Str_k_() as String
    return m.get_native().GetClockFormat()
end function

function RoDeviceInfo_get_native_Dynamic_k_() as Object
    return m.native
end function

function RoAppInfo_create_RoAppInfo_k_() as Object
    this = {}
    this.__type = "RoAppInfo"
    this.__proto = ["RoAppInfo"]
    this.getID_Str_k_ = RoAppInfo_getID_Str_k_
    this.isDev_Z_k_ = RoAppInfo_isDev_Z_k_
    this.getTitle_Str_k_ = RoAppInfo_getTitle_Str_k_
    this.getVersion_Str_k_ = RoAppInfo_getVersion_Str_k_
    this.getSubtitle_Str_k_ = RoAppInfo_getSubtitle_Str_k_
    this.getDevID_Str_k_ = RoAppInfo_getDevID_Str_k_
    this.getValue_Str_Str_k_ = RoAppInfo_getValue_Str_Str_k_
    this.get_native = RoAppInfo_get_native_Dynamic_k_
    this.native = CreateObject("roAppInfo")
    return this
end function

function RoAppInfo_getID_Str_k_() as String
    return m.get_native().GetID()
end function

function RoAppInfo_isDev_Z_k_() as Boolean
    return m.get_native().IsDev()
end function

function RoAppInfo_getTitle_Str_k_() as String
    return m.get_native().GetTitle()
end function

function RoAppInfo_getVersion_Str_k_() as String
    return m.get_native().GetVersion()
end function

function RoAppInfo_getSubtitle_Str_k_() as String
    return m.get_native().GetSubtitle()
end function

function RoAppInfo_getDevID_Str_k_() as String
    return m.get_native().GetDevID()
end function

function RoAppInfo_getValue_Str_Str_k_(key as String) as String
    return m.get_native().GetValue(key)
end function

function RoAppInfo_get_native_Dynamic_k_() as Object
    return m.native
end function
