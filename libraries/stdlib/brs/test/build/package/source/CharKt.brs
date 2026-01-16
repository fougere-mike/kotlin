function Char_create_k_() as Object
    this = {}
    this.__type = "Char"
    this.__proto = ["Char", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.compareTo_C_k_ = Char_compareTo_C_k_
    this.plus_I_k_ = Char_plus_I_k_
    this.minus_C_k_ = Char_minus_C_k_
    this.minus_I_k_ = Char_minus_I_k_
    this.inc_k_ = Char_inc_k_
    this.dec_k_ = Char_dec_k_
    this.rangeTo_C_k_ = Char_rangeTo_C_k_
    this.rangeUntil_C_k_ = Char_rangeUntil_C_k_
    this.toByte_k_ = Char_toByte_k_
    this.toChar_k_ = Char_toChar_k_
    this.toShort_k_ = Char_toShort_k_
    this.toInt_k_ = Char_toInt_k_
    this.toLong_k_ = Char_toLong_k_
    this.toFloat_k_ = Char_toFloat_k_
    this.toDouble_k_ = Char_toDouble_k_
    this.toString_k_ = Char_toString_k_
    this.toString = Char_toString_k_
    this.equals_AnyN_k_ = Char_equals_AnyN_k_
    this.equals = Char_equals_AnyN_k_
    this.hashCode_k_ = Char_hashCode_k_
    this.hashCode = Char_hashCode_k_
    return this
end function

function Char_compareTo_C_k_(other as Object) as Integer
end function

function Char_plus_I_k_(other as Integer) as Object
end function

function Char_minus_C_k_(other as Object) as Integer
end function

function Char_minus_I_k_(other as Integer) as Object
end function

function Char_inc_k_() as Object
end function

function Char_dec_k_() as Object
end function

function Char_rangeTo_C_k_(other as Object) as Object
end function

function Char_rangeUntil_C_k_(other as Object) as Object
end function

function Char_toByte_k_() as Integer
end function

function Char_toChar_k_() as Object
end function

function Char_toShort_k_() as Integer
end function

function Char_toInt_k_() as Integer
end function

function Char_toLong_k_() as LongInteger
end function

function Char_toFloat_k_() as Float
end function

function Char_toDouble_k_() as Double
end function

function Char_toString_k_() as String
end function

function Char_equals_AnyN_k_(other as Dynamic) as Boolean
end function

function Char_hashCode_k_() as Integer
end function

function Char_Companion_create_k_() as Object
    this = {}
    this.__type = "Char_Companion"
    this.__proto = ["Char_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.get_MIN_VALUE = Char_Companion_get_MIN_VALUE_k_
    this.get_MAX_VALUE = Char_Companion_get_MAX_VALUE_k_
    this.get_MIN_HIGH_SURROGATE = Char_Companion_get_MIN_HIGH_SURROGATE_k_
    this.get_MAX_HIGH_SURROGATE = Char_Companion_get_MAX_HIGH_SURROGATE_k_
    this.get_MIN_LOW_SURROGATE = Char_Companion_get_MIN_LOW_SURROGATE_k_
    this.get_MAX_LOW_SURROGATE = Char_Companion_get_MAX_LOW_SURROGATE_k_
    this.get_MIN_SURROGATE = Char_Companion_get_MIN_SURROGATE_k_
    this.get_MAX_SURROGATE = Char_Companion_get_MAX_SURROGATE_k_
    this.get_SIZE_BYTES = Char_Companion_get_SIZE_BYTES_k_
    this.get_SIZE_BITS = Char_Companion_get_SIZE_BITS_k_
    this.MIN_VALUE = ""
    this.MAX_VALUE = "￿"
    this.MIN_HIGH_SURROGATE = "?"
    this.MAX_HIGH_SURROGATE = "?"
    this.MIN_LOW_SURROGATE = "?"
    this.MAX_LOW_SURROGATE = "?"
    this.MIN_SURROGATE = "?"
    this.MAX_SURROGATE = "?"
    this.SIZE_BYTES = 2
    this.SIZE_BITS = 16
    return this
end function

function Char_Companion_getInstance() as Object
    if GetGlobalAA().Char_Companion_instance = invalid then
        GetGlobalAA().Char_Companion_instance = Char_Companion_create_k_()
    end if
    return GetGlobalAA().Char_Companion_instance
end function

function Char_Companion_get_MIN_VALUE_k_() as Object
    return m.MIN_VALUE
end function

function Char_Companion_get_MAX_VALUE_k_() as Object
    return m.MAX_VALUE
end function

function Char_Companion_get_MIN_HIGH_SURROGATE_k_() as Object
    return m.MIN_HIGH_SURROGATE
end function

function Char_Companion_get_MAX_HIGH_SURROGATE_k_() as Object
    return m.MAX_HIGH_SURROGATE
end function

function Char_Companion_get_MIN_LOW_SURROGATE_k_() as Object
    return m.MIN_LOW_SURROGATE
end function

function Char_Companion_get_MAX_LOW_SURROGATE_k_() as Object
    return m.MAX_LOW_SURROGATE
end function

function Char_Companion_get_MIN_SURROGATE_k_() as Object
    return m.MIN_SURROGATE
end function

function Char_Companion_get_MAX_SURROGATE_k_() as Object
    return m.MAX_SURROGATE
end function

function Char_Companion_get_SIZE_BYTES_k_() as Integer
    return m.SIZE_BYTES
end function

function Char_Companion_get_SIZE_BITS_k_() as Integer
    return m.SIZE_BITS
end function
