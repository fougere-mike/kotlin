function UShort_create_S_UShort_k_(data as Integer) as Object
    this = {}
    this.__type = "UShort"
    this.__proto = ["UShort"]
    this.data = data
    this.compareTo_UByte_I_k_ = UShort_compareTo_UByte_I_k_
    this.compareTo_UShort_I_k_ = UShort_compareTo_UShort_I_k_
    this.compareTo_UInt_I_k_ = UShort_compareTo_UInt_I_k_
    this.compareTo_ULong_I_k_ = UShort_compareTo_ULong_I_k_
    this.plus_UByte_UInt_k_ = UShort_plus_UByte_UInt_k_
    this.plus_UShort_UInt_k_ = UShort_plus_UShort_UInt_k_
    this.plus_UInt_UInt_k_ = UShort_plus_UInt_UInt_k_
    this.plus_ULong_ULong_k_ = UShort_plus_ULong_ULong_k_
    this.minus_UByte_UInt_k_ = UShort_minus_UByte_UInt_k_
    this.minus_UShort_UInt_k_ = UShort_minus_UShort_UInt_k_
    this.minus_UInt_UInt_k_ = UShort_minus_UInt_UInt_k_
    this.minus_ULong_ULong_k_ = UShort_minus_ULong_ULong_k_
    this.times_UByte_UInt_k_ = UShort_times_UByte_UInt_k_
    this.times_UShort_UInt_k_ = UShort_times_UShort_UInt_k_
    this.times_UInt_UInt_k_ = UShort_times_UInt_UInt_k_
    this.times_ULong_ULong_k_ = UShort_times_ULong_ULong_k_
    this.div_UByte_UInt_k_ = UShort_div_UByte_UInt_k_
    this.div_UShort_UInt_k_ = UShort_div_UShort_UInt_k_
    this.div_UInt_UInt_k_ = UShort_div_UInt_UInt_k_
    this.div_ULong_ULong_k_ = UShort_div_ULong_ULong_k_
    this.rem_UByte_UInt_k_ = UShort_rem_UByte_UInt_k_
    this.rem_UShort_UInt_k_ = UShort_rem_UShort_UInt_k_
    this.rem_UInt_UInt_k_ = UShort_rem_UInt_UInt_k_
    this.rem_ULong_ULong_k_ = UShort_rem_ULong_ULong_k_
    this.floorDiv_UByte_UInt_k_ = UShort_floorDiv_UByte_UInt_k_
    this.floorDiv_UShort_UInt_k_ = UShort_floorDiv_UShort_UInt_k_
    this.floorDiv_UInt_UInt_k_ = UShort_floorDiv_UInt_UInt_k_
    this.floorDiv_ULong_ULong_k_ = UShort_floorDiv_ULong_ULong_k_
    this.mod_UByte_UByte_k_ = UShort_mod_UByte_UByte_k_
    this.mod_UShort_UShort_k_ = UShort_mod_UShort_UShort_k_
    this.mod_UInt_UInt_k_ = UShort_mod_UInt_UInt_k_
    this.mod_ULong_ULong_k_ = UShort_mod_ULong_ULong_k_
    this.inc_UShort_k_ = UShort_inc_UShort_k_
    this.dec_UShort_k_ = UShort_dec_UShort_k_
    this.rangeTo_UShort_UIntRange_k_ = UShort_rangeTo_UShort_UIntRange_k_
    this.rangeUntil_UShort_UIntRange_k_ = UShort_rangeUntil_UShort_UIntRange_k_
    this.and_UShort_UShort_k_ = UShort_and_UShort_UShort_k_
    this.or_UShort_UShort_k_ = UShort_or_UShort_UShort_k_
    this.xor_UShort_UShort_k_ = UShort_xor_UShort_UShort_k_
    this.inv_UShort_k_ = UShort_inv_UShort_k_
    this.toByte_B_k_ = UShort_toByte_B_k_
    this.toShort_S_k_ = UShort_toShort_S_k_
    this.toInt_I_k_ = UShort_toInt_I_k_
    this.toLong_J_k_ = UShort_toLong_J_k_
    this.toUByte_UByte_k_ = UShort_toUByte_UByte_k_
    this.toUShort_UShort_k_ = UShort_toUShort_UShort_k_
    this.toUInt_UInt_k_ = UShort_toUInt_UInt_k_
    this.toULong_ULong_k_ = UShort_toULong_ULong_k_
    this.toFloat_F_k_ = UShort_toFloat_F_k_
    this.toDouble_D_k_ = UShort_toDouble_D_k_
    this.toString_Str_k_ = UShort_toString_Str_k_
    this.equals_AnyN_Z_k_ = UShort_equals_AnyN_Z_k_
    this.hashCode_I_k_ = UShort_hashCode_I_k_
    this.get_data = UShort_get_data_S_k_
    return this
end function

function UShort_compareTo_UByte_I_k_(other as Object) as Integer
    return m.compareTo(other)
end function

function UShort_compareTo_UShort_I_k_(other as Object) as Integer
    return m.compareTo(other)
end function

function UShort_compareTo_UInt_I_k_(other as Object) as Integer
    return m.toUInt().compareTo(other)
end function

function UShort_compareTo_ULong_I_k_(other as Object) as Integer
    return m.toULong().compareTo(other)
end function

function UShort_plus_UByte_UInt_k_(other as Object) as Object
    return m.toUInt() + other.toUInt()
end function

function UShort_plus_UShort_UInt_k_(other as Object) as Object
    return m.toUInt() + other.toUInt()
end function

function UShort_plus_UInt_UInt_k_(other as Object) as Object
    return m.toUInt() + other
end function

function UShort_plus_ULong_ULong_k_(other as Object) as Object
    return m.toULong() + other
end function

function UShort_minus_UByte_UInt_k_(other as Object) as Object
    return m.toUInt().minus(other.toUInt())
end function

function UShort_minus_UShort_UInt_k_(other as Object) as Object
    return m.toUInt().minus(other.toUInt())
end function

function UShort_minus_UInt_UInt_k_(other as Object) as Object
    return m.toUInt().minus(other)
end function

function UShort_minus_ULong_ULong_k_(other as Object) as Object
    return m.toULong().minus(other)
end function

function UShort_times_UByte_UInt_k_(other as Object) as Object
    return m.toUInt().times(other.toUInt())
end function

function UShort_times_UShort_UInt_k_(other as Object) as Object
    return m.toUInt().times(other.toUInt())
end function

function UShort_times_UInt_UInt_k_(other as Object) as Object
    return m.toUInt().times(other)
end function

function UShort_times_ULong_ULong_k_(other as Object) as Object
    return m.toULong().times(other)
end function

function UShort_div_UByte_UInt_k_(other as Object) as Object
    return m.toUInt().div(other.toUInt())
end function

function UShort_div_UShort_UInt_k_(other as Object) as Object
    return m.toUInt().div(other.toUInt())
end function

function UShort_div_UInt_UInt_k_(other as Object) as Object
    return m.toUInt().div(other)
end function

function UShort_div_ULong_ULong_k_(other as Object) as Object
    return m.toULong().div(other)
end function

function UShort_rem_UByte_UInt_k_(other as Object) as Object
    return m.toUInt().rem(other.toUInt())
end function

function UShort_rem_UShort_UInt_k_(other as Object) as Object
    return m.toUInt().rem(other.toUInt())
end function

function UShort_rem_UInt_UInt_k_(other as Object) as Object
    return m.toUInt().rem(other)
end function

function UShort_rem_ULong_ULong_k_(other as Object) as Object
    return m.toULong().rem(other)
end function

function UShort_floorDiv_UByte_UInt_k_(other as Object) as Object
    return m.toUInt().floorDiv(other.toUInt())
end function

function UShort_floorDiv_UShort_UInt_k_(other as Object) as Object
    return m.toUInt().floorDiv(other.toUInt())
end function

function UShort_floorDiv_UInt_UInt_k_(other as Object) as Object
    return m.toUInt().floorDiv(other)
end function

function UShort_floorDiv_ULong_ULong_k_(other as Object) as Object
    return m.toULong().floorDiv(other)
end function

function UShort_mod_UByte_UByte_k_(other as Object) as Object
    return m.toUInt().mod(other.toUInt()).toUByte()
end function

function UShort_mod_UShort_UShort_k_(other as Object) as Object
    return m.toUInt().mod(other.toUInt()).toUShort()
end function

function UShort_mod_UInt_UInt_k_(other as Object) as Object
    return m.toUInt().mod(other)
end function

function UShort_mod_ULong_ULong_k_(other as Object) as Object
    return m.toULong().mod(other)
end function

function UShort_inc_UShort_k_() as Object
    return UShort_create_S_UShort_k_(m.data + 1)
end function

function UShort_dec_UShort_k_() as Object
    return UShort_create_S_UShort_k_(m.data - 1)
end function

function UShort_rangeTo_UShort_UIntRange_k_(other as Object) as Object
    return UIntRange_create_UInt_UInt_UIntRange_k_(m.toUInt(), other.toUInt())
end function

function UShort_rangeUntil_UShort_UIntRange_k_(other as Object) as Object
    return until_rUInt_UInt_UIntRange_k_(m.toUInt(), other.toUInt())
end function

function UShort_and_UShort_UShort_k_(other as Object) as Object
    return UShort_create_S_UShort_k_(m.and(other))
end function

function UShort_or_UShort_UShort_k_(other as Object) as Object
    return UShort_create_S_UShort_k_(m.or(other))
end function

function UShort_xor_UShort_UShort_k_(other as Object) as Object
    return UShort_create_S_UShort_k_(m.xor(other))
end function

function UShort_inv_UShort_k_() as Object
    return UShort_create_S_UShort_k_(m.inv())
end function

function UShort_toByte_B_k_() as Integer
    return m.data
end function

function UShort_toShort_S_k_() as Integer
    return m.data
end function

function UShort_toInt_I_k_() as Integer
    return m.data.and(65535)
end function

function UShort_toLong_J_k_() as LongInteger
    return m.data.and(65535&)
end function

function UShort_toUByte_UByte_k_() as Object
    return UByte_create_B_UByte_k_(m.data)
end function

function UShort_toUShort_UShort_k_() as Object
    return m
end function

function UShort_toUInt_UInt_k_() as Object
    return UInt_create_I_UInt_k_(m.data.and(65535))
end function

function UShort_toULong_ULong_k_() as Object
    return ULong_create_J_ULong_k_(m.data.and(65535&))
end function

function UShort_toFloat_F_k_() as Float
    return m
end function

function UShort_toDouble_D_k_() as Double
    return m
end function

function UShort_toString_Str_k_() as String
    return Str(m)
end function

function UShort_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "UShort") and (m.data = other.data)
end function

function UShort_hashCode_I_k_() as Integer
    return m.data
end function

function UShort_get_data_S_k_() as Integer
    return m.data
end function

function UShort_Companion_create_Companion_k_() as Object
    this = {}
    this.__type = "UShort_Companion"
    this.__proto = ["UShort_Companion"]
    this.MIN_VALUE = UShort_create_S_UShort_k_(0)
    this.MAX_VALUE = UShort_create_S_UShort_k_(-1)
    this.SIZE_BYTES = 2
    this.SIZE_BITS = 16
    this.get_MIN_VALUE = UShort_Companion_get_MIN_VALUE_UShort_k_
    this.get_MAX_VALUE = UShort_Companion_get_MAX_VALUE_UShort_k_
    this.get_SIZE_BYTES = UShort_Companion_get_SIZE_BYTES_I_k_
    this.get_SIZE_BITS = UShort_Companion_get_SIZE_BITS_I_k_
    return this
end function

function UShort_Companion_getInstance() as Object
    if m.UShort_Companion_instance = invalid then
        m.UShort_Companion_instance = UShort_Companion_create()
    end if
    return m.UShort_Companion_instance
end function

function UShort_Companion_get_MIN_VALUE_UShort_k_() as Object
    return m.MIN_VALUE
end function

function UShort_Companion_get_MAX_VALUE_UShort_k_() as Object
    return m.MAX_VALUE
end function

function UShort_Companion_get_SIZE_BYTES_I_k_() as Integer
    return m.SIZE_BYTES
end function

function UShort_Companion_get_SIZE_BITS_I_k_() as Integer
    return m.SIZE_BITS
end function

function toUShort_rB_UShort_k_(m as Integer) as Object
    return UShort_create_S_UShort_k_(m)
end function

function toUShort_rS_UShort_k_(m as Integer) as Object
    return UShort_create_S_UShort_k_(m)
end function

function toUShort_rI_UShort_k_(m as Integer) as Object
    return UShort_create_S_UShort_k_(m)
end function

function toUShort_rJ_UShort_k_(m as LongInteger) as Object
    return UShort_create_S_UShort_k_(m)
end function
