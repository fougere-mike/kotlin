function IMessagePort_waitMessage_I_DynamicN_k_(timeout as Integer) as Dynamic
end function

function IMessagePort_getMessage_DynamicN_k_() as Dynamic
end function

function IMessagePort_peekMessage_DynamicN_k_() as Dynamic
end function

sub ISetMessagePort_setMessagePort_RoMessagePort_k_(port as Object)
end sub

function IGetMessagePort_getMessagePort_RoMessagePortN_k_() as Dynamic
end function

sub IEnum_reset()
end sub

function IEnum_next_AnyN_k_() as Dynamic
end function

function IEnum_isNext_Z_k_() as Boolean
end function

function IHttpAgent_addHeader_Str_Str_Z_k_(name as String, value as String) as Boolean
end function

function IHttpAgent_setCertificatesFile_Str_Z_k_(path as String) as Boolean
end function

function IHttpAgent_initClientCertificates_Z_k_() as Boolean
end function

function IHttpAgent_setHeaders_MapStrStr_Z_k_(headers as Object) as Boolean
end function

function IUrlTransfer_setUrl_Str_Z_k_(url as String) as Boolean
end function

function IUrlTransfer_getUrl_Str_k_() as String
end function

function IUrlTransfer_getToString_StrN_k_() as Dynamic
end function

function IUrlTransfer_postFromString_Str_I_k_(body as String) as Integer
end function

function IUrlTransfer_head_I_k_() as Integer
end function

function IUrlTransfer_asyncGetToString_Z_k_() as Boolean
end function

function IUrlTransfer_asyncPostFromString_Str_Z_k_(body as String) as Boolean
end function

function IUrlTransfer_asyncCancel_Z_k_() as Boolean
end function
