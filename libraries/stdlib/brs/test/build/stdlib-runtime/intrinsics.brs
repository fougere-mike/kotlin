function brsTypeOf_AnyN_Str_k_(value as Dynamic) as String
    return "Object"
end function

function brsIsInvalid_AnyN_Z_k_(value as Dynamic) as Boolean
    return value = invalid
end function

function brsCreateObject_Str_Arr_AnyN_k_(className as String, args as Object) as Dynamic
    error_Any_k_("brsCreateObject should be lowered by the backend")
end function

function brsCreateArray_I_Any_k_(size = 0) as Object
    error_Any_k_("brsCreateArray should be lowered by the backend")
end function

function brsArrayLength_Any_I_k_(array as Object) as Integer
    error_Any_k_("brsArrayLength should be lowered by the backend")
end function

function brsCreateAssociativeArray_Any_k_() as Object
    error_Any_k_("brsCreateAssociativeArray should be lowered by the backend")
end function

function brsStringLength_Str_I_k_(str as String) as Integer
    return str.get_length()
end function

function brsStringConcat_Str_Str_Str_k_(str1 as String, str2 as String) as String
    return str1 + str2
end function

sub brsPrint_AnyN_k_(value as Dynamic)
end sub

function brsParseJson_Str_AnyN_k_(jsonString as String) as Dynamic
    error_Any_k_("brsParseJson should be lowered by the backend")
end function

function brsFormatJson_AnyN_Str_k_(obj as Dynamic) as String
    error_Any_k_("brsFormatJson should be lowered by the backend")
end function
