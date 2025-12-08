function RoUrlTransfer_create_RoUrlTransfer_k_() as Object
    this = {}
    this.__type = "RoUrlTransfer"
    this.__proto = ["RoUrlTransfer"]
    this.setUrl_Str_Z_k_ = RoUrlTransfer_setUrl_Str_Z_k_
    this.getUrl_Str_k_ = RoUrlTransfer_getUrl_Str_k_
    this.getToString_StrN_k_ = RoUrlTransfer_getToString_StrN_k_
    this.postFromString_Str_I_k_ = RoUrlTransfer_postFromString_Str_I_k_
    this.head_I_k_ = RoUrlTransfer_head_I_k_
    this.asyncGetToString_Z_k_ = RoUrlTransfer_asyncGetToString_Z_k_
    this.asyncPostFromString_Str_Z_k_ = RoUrlTransfer_asyncPostFromString_Str_Z_k_
    this.asyncCancel_Z_k_ = RoUrlTransfer_asyncCancel_Z_k_
    this.addHeader_Str_Str_Z_k_ = RoUrlTransfer_addHeader_Str_Str_Z_k_
    this.setCertificatesFile_Str_Z_k_ = RoUrlTransfer_setCertificatesFile_Str_Z_k_
    this.initClientCertificates_Z_k_ = RoUrlTransfer_initClientCertificates_Z_k_
    this.setHeaders_MapStrStr_Z_k_ = RoUrlTransfer_setHeaders_MapStrStr_Z_k_
    this.setMessagePort_RoMessagePort_k_ = RoUrlTransfer_setMessagePort_RoMessagePort_k_
    this.getMessagePort_RoMessagePortN_k_ = RoUrlTransfer_getMessagePort_RoMessagePortN_k_
    this.setRequest_Str_k_ = RoUrlTransfer_setRequest_Str_k_
    this.retainBodyOnError_Z_k_ = RoUrlTransfer_retainBodyOnError_Z_k_
    this.setUserAndPassword_Str_Str_Z_k_ = RoUrlTransfer_setUserAndPassword_Str_Str_Z_k_
    this.setMinimumTransferRate_I_I_k_ = RoUrlTransfer_setMinimumTransferRate_I_I_k_
    this.enableEncodings_Z_k_ = RoUrlTransfer_enableEncodings_Z_k_
    this.enableResume_Z_k_ = RoUrlTransfer_enableResume_Z_k_
    this.enablePeerVerification_Z_k_ = RoUrlTransfer_enablePeerVerification_Z_k_
    this.enableHostVerification_Z_k_ = RoUrlTransfer_enableHostVerification_Z_k_
    this.enableFreshConnection_Z_k_ = RoUrlTransfer_enableFreshConnection_Z_k_
    this.getToFile_Str_I_k_ = RoUrlTransfer_getToFile_Str_I_k_
    this.postFromFile_Str_I_k_ = RoUrlTransfer_postFromFile_Str_I_k_
    this.asyncGetToFile_Str_Z_k_ = RoUrlTransfer_asyncGetToFile_Str_Z_k_
    this.asyncPostFromFile_Str_Z_k_ = RoUrlTransfer_asyncPostFromFile_Str_Z_k_
    this.asyncPostFromFileToString_Dynamic_Z_k_ = RoUrlTransfer_asyncPostFromFileToString_Dynamic_Z_k_
    this.getResponseCode_I_k_ = RoUrlTransfer_getResponseCode_I_k_
    this.getResponseHeaders_Dynamic_k_ = RoUrlTransfer_getResponseHeaders_Dynamic_k_
    this.getResponseHeader_Str_Str_k_ = RoUrlTransfer_getResponseHeader_Str_Str_k_
    this.getFailureReason_Str_k_ = RoUrlTransfer_getFailureReason_Str_k_
    this.getIdentity_I_k_ = RoUrlTransfer_getIdentity_I_k_
    this.escape_Str_Str_k_ = RoUrlTransfer_escape_Str_Str_k_
    this.unescape_Str_Str_k_ = RoUrlTransfer_unescape_Str_Str_k_
    this.get_native = RoUrlTransfer_get_native_Dynamic_k_
    m.native = CreateObject("roUrlTransfer")
    return this
end function

function RoUrlTransfer_setUrl_Str_Z_k_(url as String) as Boolean
    return m.native.SetUrl(url)
end function

function RoUrlTransfer_getUrl_Str_k_() as String
    return m.native.GetUrl()
end function

function RoUrlTransfer_getToString_StrN_k_() as Dynamic
    return m.native.GetToString()
end function

function RoUrlTransfer_postFromString_Str_I_k_(body as String) as Integer
    return m.native.PostFromString(body)
end function

function RoUrlTransfer_head_I_k_() as Integer
    return m.native.Head()
end function

function RoUrlTransfer_asyncGetToString_Z_k_() as Boolean
    return m.native.AsyncGetToString()
end function

function RoUrlTransfer_asyncPostFromString_Str_Z_k_(body as String) as Boolean
    return m.native.AsyncPostFromString(body)
end function

function RoUrlTransfer_asyncCancel_Z_k_() as Boolean
    return m.native.AsyncCancel()
end function

function RoUrlTransfer_addHeader_Str_Str_Z_k_(name as String, value as String) as Boolean
    return m.native.AddHeader(name, value)
end function

function RoUrlTransfer_setCertificatesFile_Str_Z_k_(path as String) as Boolean
    return m.native.SetCertificatesFile(path)
end function

function RoUrlTransfer_initClientCertificates_Z_k_() as Boolean
    return m.native.InitClientCertificates()
end function

function RoUrlTransfer_setHeaders_MapStrStr_Z_k_(headers as Object) as Boolean
    return m.native.SetHeaders(headers)
end function

sub RoUrlTransfer_setMessagePort_RoMessagePort_k_(port as Object)
    m.native.SetMessagePort(port.getNative())
end sub

function RoUrlTransfer_getMessagePort_RoMessagePortN_k_() as Dynamic
    port = m.native.GetMessagePort()
    __when_tmp0 = invalid
    if port <> invalid then
        __when_tmp0 = RoMessagePort_create_Dynamic_RoMessagePort_k_(port)
    else if true then
        __when_tmp0 = invalid
    end if
    return __when_tmp0

end function

sub RoUrlTransfer_setRequest_Str_k_(method as String)
    m.native.SetRequest(method)
end sub

sub RoUrlTransfer_retainBodyOnError_Z_k_(retain as Boolean)
    m.native.RetainBodyOnError(retain)
end sub

function RoUrlTransfer_setUserAndPassword_Str_Str_Z_k_(user as String, password as String) as Boolean
    return m.native.SetUserAndPassword(user, password)
end function

sub RoUrlTransfer_setMinimumTransferRate_I_I_k_(bytesPerSecond as Integer, periodInSeconds as Integer)
    m.native.SetMinimumTransferRate(bytesPerSecond, periodInSeconds)
end sub

sub RoUrlTransfer_enableEncodings_Z_k_(enable as Boolean)
    m.native.EnableEncodings(enable)
end sub

sub RoUrlTransfer_enableResume_Z_k_(enable as Boolean)
    m.native.EnableResume(enable)
end sub

sub RoUrlTransfer_enablePeerVerification_Z_k_(enable as Boolean)
    m.native.EnablePeerVerification(enable)
end sub

sub RoUrlTransfer_enableHostVerification_Z_k_(enable as Boolean)
    m.native.EnableHostVerification(enable)
end sub

sub RoUrlTransfer_enableFreshConnection_Z_k_(enable as Boolean)
    m.native.EnableFreshConnection(enable)
end sub

function RoUrlTransfer_getToFile_Str_I_k_(path as String) as Integer
    return m.native.GetToFile(path)
end function

function RoUrlTransfer_postFromFile_Str_I_k_(path as String) as Integer
    return m.native.PostFromFile(path)
end function

function RoUrlTransfer_asyncGetToFile_Str_Z_k_(path as String) as Boolean
    return m.native.AsyncGetToFile(path)
end function

function RoUrlTransfer_asyncPostFromFile_Str_Z_k_(path as String) as Boolean
    return m.native.AsyncPostFromFile(path)
end function

function RoUrlTransfer_asyncPostFromFileToString_Dynamic_Z_k_(elements as Object) as Boolean
    return m.native.AsyncPostFromFileToString(elements)
end function

function RoUrlTransfer_getResponseCode_I_k_() as Integer
    return m.native.GetResponseCode()
end function

function RoUrlTransfer_getResponseHeaders_Dynamic_k_() as Object
    return m.native.GetResponseHeaders()
end function

function RoUrlTransfer_getResponseHeader_Str_Str_k_(name as String) as String
    headers = m.getResponseHeaders()
    return invalid
end function

function RoUrlTransfer_getFailureReason_Str_k_() as String
    return m.native.GetFailureReason()
end function

function RoUrlTransfer_getIdentity_I_k_() as Integer
    return m.native.GetIdentity()
end function

function RoUrlTransfer_escape_Str_Str_k_(text as String) as String
    return m.native.Escape(text)
end function

function RoUrlTransfer_unescape_Str_Str_k_(text as String) as String
    return m.native.Unescape(text)
end function

function RoUrlTransfer_get_native_Dynamic_k_() as Object
    return m.native
end function

function RoUrlEvent_create_Dynamic_RoUrlEvent_k_(event as Object) as Object
    this = {}
    this.__type = "RoUrlEvent"
    this.__proto = ["RoUrlEvent"]
    this.event = event
    this.getResponseCode_I_k_ = RoUrlEvent_getResponseCode_I_k_
    this.getString_Str_k_ = RoUrlEvent_getString_Str_k_
    this.getFailureReason_Str_k_ = RoUrlEvent_getFailureReason_Str_k_
    this.getSourceIdentity_I_k_ = RoUrlEvent_getSourceIdentity_I_k_
    this.getResponseHeaders_Dynamic_k_ = RoUrlEvent_getResponseHeaders_Dynamic_k_
    this.getResponseHeader_Str_Str_k_ = RoUrlEvent_getResponseHeader_Str_Str_k_
    this.getTargetIpAddress_Str_k_ = RoUrlEvent_getTargetIpAddress_Str_k_
    this.getBytesReceived_I_k_ = RoUrlEvent_getBytesReceived_I_k_
    this.get_event = RoUrlEvent_get_event_Dynamic_k_
    return this
end function

function RoUrlEvent_getResponseCode_I_k_() as Integer
    return m.event.GetResponseCode()
end function

function RoUrlEvent_getString_Str_k_() as String
    return m.event.GetString()
end function

function RoUrlEvent_getFailureReason_Str_k_() as String
    return m.event.GetFailureReason()
end function

function RoUrlEvent_getSourceIdentity_I_k_() as Integer
    return m.event.GetSourceIdentity()
end function

function RoUrlEvent_getResponseHeaders_Dynamic_k_() as Object
    return m.event.GetResponseHeaders()
end function

function RoUrlEvent_getResponseHeader_Str_Str_k_(name as String) as String
    headers = m.getResponseHeaders()
    return invalid
end function

function RoUrlEvent_getTargetIpAddress_Str_k_() as String
    return m.event.GetTargetIpAddress()
end function

function RoUrlEvent_getBytesReceived_I_k_() as Integer
    return m.event.GetInt()
end function

function RoUrlEvent_get_event_Dynamic_k_() as Object
    return m.event
end function
