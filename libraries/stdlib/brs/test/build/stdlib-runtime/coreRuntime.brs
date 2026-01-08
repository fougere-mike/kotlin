function equals_AnyN_AnyN_k_(obj1 as Dynamic, obj2 as Dynamic) as Boolean
    if __kotlin_identityEquals(obj1, obj2) then
        return true
    end if
    if (obj1 = invalid) or (obj2 = invalid) then
        return false
    end if
    return obj1.equals(obj2)
end function

function toString_AnyN_k_(obj as Dynamic) as String
    if obj = invalid then
        return "null"
    end if
    t = Type(obj)
    if (t = "String") or (t = "roString") then
        return obj
    end if
    if ((((((((t = "Integer") or (t = "LongInteger")) or (t = "Float")) or (t = "Double")) or (t = "roInt")) or (t = "roFloat")) or (t = "roDouble")) or (t = "roInteger")) or (t = "roLongInteger") then
        return __kotlin_numToStr_AnyN_k_(obj)
    end if
    if (t = "Boolean") or (t = "roBoolean") then
        if obj then
            return "true"
        end if
        return "false"
    end if
    return obj.toString()
end function

function hashCode_AnyN_k_(obj as Dynamic) as Integer
    if obj = invalid then
        return 0
    end if
    return obj.hashCode()
end function

function getStringHashCode_Str_k_(str as String) as Integer
    hash = 0
    progression = until_rI_I_k_(0, Len(str))
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        hash = ((31 * hash) + get_code_rC_k_(Mid(str, i + 1, 1)))


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            hash = ((31 * hash) + get_code_rC_k_(Mid(str, i + 1, 1)))

        end while

    end if

    return hash
end function

function getBooleanHashCode_Z_k_(value as Boolean) as Integer
    __when_tmp0 = invalid
    if value then
        __when_tmp0 = 1231
    else if true then
        __when_tmp0 = 1237
    end if
    return __when_tmp0

end function

function get_objectHashCodeCounter_k_() as Integer
    return GetGlobalAA().objectHashCodeCounter
end function

sub set_objectHashCodeCounter_I_k_(value as Integer)
    m.objectHashCodeCounter = value
end sub

function identityHashCode_AnyN_k_(obj as Dynamic) as Integer
    if obj = invalid then
        return 0
    end if
    tmp0_subject = obj
    __when_tmp1 = invalid
    if Type(tmp0_subject) = "roString" then
        __when_tmp1 = getStringHashCode_Str_k_(obj)
    else if Type(tmp0_subject) = "roBoolean" then
        __when_tmp1 = getBooleanHashCode_Z_k_(obj)
    else if __kotlin_isInstanceOf(tmp0_subject, "Number") then
        __when_tmp1 = obj.hashCode()
    else if true then
        set_objectHashCodeCounter_I_k_(get_objectHashCodeCounter_k_() + 1)
        __when_tmp1 = get_objectHashCodeCounter_k_()
    end if
    return __when_tmp1

end function

function newThrowable_StrN_ThrowableN_k_(message as Dynamic, cause as Dynamic) as Object
    return Throwable_create_StrN_ThrowableN_k_(message, cause)
end function

sub captureStack_Throwable_k_(instance as Object)
    instance.captureStack_k_()
end sub

function isInstance_AnyN_Str_k_(obj as Dynamic, type_ as String) as Boolean
    if obj = invalid then
        return false
    end if
    return true
end function

function checkCast_AnyN_Str_k_(obj as Dynamic, type_ as String) as Dynamic
    if obj = invalid then
        return invalid
    end if
    if not isInstance_AnyN_Str_k_(obj, type_) then
        throw ClassCastException_create_StrN_k_((("Cannot cast " + toString_AnyN_k_("/* Unsupported: IrGetClassImpl */".get_simpleName())) + " to ") + type_)
    end if
    return obj
end function

function __kotlin_numToStr_I_k_(value as Integer) as String
    s = Str(value)
    if Left(s, 1) = " " then
        return Mid(s, 2)
    end if
    return s
end function

function __kotlin_numToStr_J_k_(value as LongInteger) as String
    if (value >= 10000000000&) or (value <= -10000000000&) then
        return __kotlin_longToFixedStr_J_k_(value)
    end if
    s = Str(value)
    if Left(s, 1) = " " then
        return Mid(s, 2)
    end if
    return s
end function

function __kotlin_longToFixedStr_J_k_(value as LongInteger) as String
    if value = 0& then
        return "0"
    end if
    isNegative = value < 0
    __when_tmp2 = invalid
    if isNegative then
        __when_tmp2 = -value
    else if true then
        __when_tmp2 = value
    end if
    remaining = __when_tmp2

    result = ""
    while remaining > 0
        digit = remaining mod 10
        result = (__kotlin_numToStr_I_k_(digit) + result)
        remaining = (remaining / 10)
    end while
    __when_tmp3 = invalid
    if isNegative then
        __when_tmp3 = ("-" + result)
    else if true then
        __when_tmp3 = result
    end if
    return __when_tmp3

end function

function __kotlin_numToStr_F_k_(value as Float) as String
    s = Str(value)
    if Left(s, 1) = " " then
        return Mid(s, 2)
    end if
    return s
end function

function __kotlin_numToStr_D_k_(value as Double) as String
    s = Str(value)
    if Left(s, 1) = " " then
        return Mid(s, 2)
    end if
    return s
end function

function __kotlin_numToStr_AnyN_k_(value as Dynamic) as String
    if value = invalid then
        return "null"
    end if
    s = Str(value)
    if Left(s, 1) = " " then
        return Mid(s, 2)
    end if
    return s
end function

function __kotlin_charSequenceLength_CharSequenceN_k_(value as Dynamic) as Integer
    if value = invalid then
        return 0
    end if
    t = Type(value)
    if (t = "String") or (t = "roString") then
        return Len(value)
    end if
    return value.get_length()
end function

function __kotlin_toJsonValue_AnyN_k_(value as Dynamic) as Dynamic
    if value = invalid then
        return invalid
    end if
    t = Type(value)
    if ((((((((((((t = "String") or (t = "roString")) or (t = "Integer")) or (t = "LongInteger")) or (t = "Float")) or (t = "Double")) or (t = "roInt")) or (t = "roFloat")) or (t = "roDouble")) or (t = "roInteger")) or (t = "roLongInteger")) or (t = "Boolean")) or (t = "roBoolean") then
        return value
    end if
    if t = "roAssociativeArray" then
        if value.DoesExist("__type") then
            type_ = value.Lookup("__type")
            if (type_ = "LinkedHashMap") or (type_ = "HashMap") then
                return __kotlin_mapToPlainAA_Any_k_(value)
            end if
            if ((type_ = "ArrayList") or (type_ = "LinkedHashSet")) or (type_ = "HashSet") then
                return __kotlin_collectionToPlainArray_Any_k_(value)
            end if
            if type_ = "EmptyMap" then
                return CreateObject("roAssociativeArray")
            end if
            if type_ = "EmptyList" then
                return CreateObject("roArray", 0, true)
            end if
        end if
        if value.DoesExist("get_map") then
            return __kotlin_mapToPlainAA_Any_k_(value)
        end if
        return value
    end if
    if t = "roArray" then
        return __kotlin_arrayToPlainArray_Any_k_(value)
    end if
    return value
end function

function __kotlin_mapToPlainAA_Any_k_(map as Object) as Object
    result = CreateObject("roAssociativeArray")
    if map.DoesExist("get_map") then
        internalMap = map.get_map()
        keys = internalMap.Keys()
        indexedObject = keys
        inductionVariable = 0
        last = indexedObject.count()
        while inductionVariable < last
            internalKey = indexedObject[inductionVariable]
            inductionVariable = (inductionVariable + 1)

            entry = internalMap.Lookup(internalKey)
            originalKey = entry.Lookup("k")
            value = entry.Lookup("v")
            keyStr = toString_AnyN_k_(originalKey)
            convertedValue = __kotlin_toJsonValue_AnyN_k_(value)
            result.AddReplace(keyStr, convertedValue)

        end while
    else if true then
        keys = map.Keys()
        indexedObject = keys
        inductionVariable = 0
        last = indexedObject.count()
        while inductionVariable < last
            key = indexedObject[inductionVariable]
            inductionVariable = (inductionVariable + 1)

            value = map.Lookup(key)
            valueType = Type(value)
            if (valueType <> "Function") and (valueType <> "roFunction") then
                isInternalKey = ((((((((Left(key, Len("__")) = "__") or (Right(key, Len("_k_")) = "_k_")) or (Left(key, Len("get_")) = "get_")) or (Left(key, Len("set_")) = "set_")) or (key = "equals")) or (key = "hashCode")) or (key = "toString")) or (key = "copy")) or (key = "_size")
                if not isInternalKey then
                    convertedValue = __kotlin_toJsonValue_AnyN_k_(value)
                    result.AddReplace(key, convertedValue)
                end if
            end if

        end while
    end if
    return result
end function

function __kotlin_collectionToPlainArray_Any_k_(collection as Object) as Object
    array = collection.get_array()
    return __kotlin_arrayToPlainArray_Any_k_(array)
end function

function __kotlin_arrayToPlainArray_Any_k_(array as Object) as Object
    count = array.count()
    result = CreateObject("roArray", 0, true)
    i = 0
    while i < count
        element = array[i]
        convertedElement = __kotlin_toJsonValue_AnyN_k_(element)
        result.push(convertedElement)
        i = (i + 1)
    end while
    return result
end function
