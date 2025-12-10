function IMessagePort_waitMessage_I_k_(timeout as Integer) as Dynamic
end function

function IMessagePort_getMessage_k_() as Dynamic
end function

function IMessagePort_peekMessage_k_() as Dynamic
end function

sub ISetMessagePort_setMessagePort_RoMessagePort_k_(port as Object)
end sub

function IGetMessagePort_getMessagePort_k_() as Dynamic
end function

sub IEnum_reset_k_()
end sub

function IEnum_next_k_() as Dynamic
end function

function IEnum_isNext_k_() as Boolean
end function

function IHttpAgent_addHeader_Str_Str_k_(name as String, value as String) as Boolean
end function

function IHttpAgent_setCertificatesFile_Str_k_(path as String) as Boolean
end function

function IHttpAgent_initClientCertificates_k_() as Boolean
end function

function IHttpAgent_setHeaders_MapStrStr_k_(headers as Object) as Boolean
end function

function IUrlTransfer_setUrl_Str_k_(url as String) as Boolean
end function

function IUrlTransfer_getUrl_k_() as String
end function

function IUrlTransfer_getToString_k_() as Dynamic
end function

function IUrlTransfer_postFromString_Str_k_(body as String) as Integer
end function

function IUrlTransfer_head_k_() as Integer
end function

function IUrlTransfer_asyncGetToString_k_() as Boolean
end function

function IUrlTransfer_asyncPostFromString_Str_k_(body as String) as Boolean
end function

function IUrlTransfer_asyncCancel_k_() as Boolean
end function
