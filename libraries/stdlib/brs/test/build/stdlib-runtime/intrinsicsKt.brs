function brsTypeOf_AnyN_k_(value as Dynamic) as String
    return "Object"
end function

function brsIsInvalid_AnyN_k_(value as Dynamic) as Boolean
    return value = invalid
end function

function brsCreateObject_Str_Arr_k_(className as String, args as Object) as Dynamic
    error_Any_k_("brsCreateObject should be lowered by the backend")
end function

function brsCreateArray_I_k_(size = 0) as Object
    if size = invalid then
        size = 0
    end if
    error_Any_k_("brsCreateArray should be lowered by the backend")
end function

function brsArrayLength_Any_k_(array as Object) as Integer
    error_Any_k_("brsArrayLength should be lowered by the backend")
end function

function brsCreateAssociativeArray_k_() as Object
    error_Any_k_("brsCreateAssociativeArray should be lowered by the backend")
end function

function brsStringLength_Str_k_(str as String) as Integer
    return Len(str)
end function

function brsStringConcat_Str_Str_k_(str1 as String, str2 as String) as String
    return str1 + str2
end function

sub brsPrint_AnyN_k_(value as Dynamic)
end sub

function brsParseJson_Str_k_(jsonString as String) as Dynamic
    error_Any_k_("brsParseJson should be lowered by the backend")
end function

function brsFormatJson_AnyN_k_(obj as Dynamic) as String
    error_Any_k_("brsFormatJson should be lowered by the backend")
end function

function mapToPlainAA_MapStrAnyN_k_(map as Object) as Object
    result = CreateObject("roAssociativeArray")
    entries = map.__get_entries()
    iter = entries.iterator_k_()
    while iter.hasNext_k_()
        entry = iter.next_k_()
        result.AddReplace(entry.__get_key(), entry.__get_value())
    end while
    return result
end function

function brsCreatePlainAA_k_() as Object
    error_Any_k_("brsCreatePlainAA should be lowered by the backend")
end function

sub brsAAAddReplace_Any_Str_AnyN_k_(aa as Object, key as String, value as Dynamic)
    error_Any_k_("brsAAAddReplace should be lowered by the backend")
end sub
