function RoDeviceInfo_Companion_create_k_() as Object
    this = {}
    this.__type = "RoDeviceInfo_Companion"
    this.__proto = ["RoDeviceInfo_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoDeviceInfo_Companion_getInstance() as Object
    if m.RoDeviceInfo_Companion_instance = invalid then
        m.RoDeviceInfo_Companion_instance = RoDeviceInfo_Companion_create_k_()
    end if
    return m.RoDeviceInfo_Companion_instance
end function

function hasInternetConnection_rRoDeviceInfo_k_(m as Object) as Boolean
    status = m.getLinkStatus()
    return invalid
end function

function getSoftwareVersion_rRoDeviceInfo_k_(m as Object) as String
    return m.getVersion()
end function

function isAdTrackingLimited_rRoDeviceInfo_k_(m as Object) as Boolean
    return m.isRIDADisabled()
end function

function RoAppInfo_Companion_create_k_() as Object
    this = {}
    this.__type = "RoAppInfo_Companion"
    this.__proto = ["RoAppInfo_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoAppInfo_Companion_getInstance() as Object
    if m.RoAppInfo_Companion_instance = invalid then
        m.RoAppInfo_Companion_instance = RoAppInfo_Companion_create_k_()
    end if
    return m.RoAppInfo_Companion_instance
end function
