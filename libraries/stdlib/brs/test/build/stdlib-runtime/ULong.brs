function ULong_create_J_ULong_k_(data as LongInteger) as Object
    this = {}
    this.__type = "ULong"
    this.__proto = ["ULong"]
    this.data = data
    this.compareTo_UByte_I_k_ = ULong_compareTo_UByte_I_k_
    this.compareTo_UShort_I_k_ = ULong_compareTo_UShort_I_k_
    this.compareTo_UInt_I_k_ = ULong_compareTo_UInt_I_k_
    this.compareTo_ULong_I_k_ = ULong_compareTo_ULong_I_k_
    this.plus_UByte_ULong_k_ = ULong_plus_UByte_ULong_k_
    this.plus_UShort_ULong_k_ = ULong_plus_UShort_ULong_k_
    this.plus_UInt_ULong_k_ = ULong_plus_UInt_ULong_k_
    this.plus_ULong_ULong_k_ = ULong_plus_ULong_ULong_k_
    this.minus_UByte_ULong_k_ = ULong_minus_UByte_ULong_k_
    this.minus_UShort_ULong_k_ = ULong_minus_UShort_ULong_k_
    this.minus_UInt_ULong_k_ = ULong_minus_UInt_ULong_k_
    this.minus_ULong_ULong_k_ = ULong_minus_ULong_ULong_k_
    this.times_UByte_ULong_k_ = ULong_times_UByte_ULong_k_
    this.times_UShort_ULong_k_ = ULong_times_UShort_ULong_k_
    this.times_UInt_ULong_k_ = ULong_times_UInt_ULong_k_
    this.times_ULong_ULong_k_ = ULong_times_ULong_ULong_k_
    this.div_UByte_ULong_k_ = ULong_div_UByte_ULong_k_
    this.div_UShort_ULong_k_ = ULong_div_UShort_ULong_k_
    this.div_UInt_ULong_k_ = ULong_div_UInt_ULong_k_
    this.div_ULong_ULong_k_ = ULong_div_ULong_ULong_k_
    this.rem_UByte_ULong_k_ = ULong_rem_UByte_ULong_k_
    this.rem_UShort_ULong_k_ = ULong_rem_UShort_ULong_k_
    this.rem_UInt_ULong_k_ = ULong_rem_UInt_ULong_k_
    this.rem_ULong_ULong_k_ = ULong_rem_ULong_ULong_k_
    this.floorDiv_UByte_ULong_k_ = ULong_floorDiv_UByte_ULong_k_
    this.floorDiv_UShort_ULong_k_ = ULong_floorDiv_UShort_ULong_k_
    this.floorDiv_UInt_ULong_k_ = ULong_floorDiv_UInt_ULong_k_
    this.floorDiv_ULong_ULong_k_ = ULong_floorDiv_ULong_ULong_k_
    this.mod_UByte_UByte_k_ = ULong_mod_UByte_UByte_k_
    this.mod_UShort_UShort_k_ = ULong_mod_UShort_UShort_k_
    this.mod_UInt_UInt_k_ = ULong_mod_UInt_UInt_k_
    this.mod_ULong_ULong_k_ = ULong_mod_ULong_ULong_k_
    this.inc_ULong_k_ = ULong_inc_ULong_k_
    this.dec_ULong_k_ = ULong_dec_ULong_k_
    this.rangeTo_ULong_ULongRange_k_ = ULong_rangeTo_ULong_ULongRange_k_
    this.rangeUntil_ULong_ULongRange_k_ = ULong_rangeUntil_ULong_ULongRange_k_
    this.shl_I_ULong_k_ = ULong_shl_I_ULong_k_
    this.shr_I_ULong_k_ = ULong_shr_I_ULong_k_
    this.and_ULong_ULong_k_ = ULong_and_ULong_ULong_k_
    this.or_ULong_ULong_k_ = ULong_or_ULong_ULong_k_
    this.xor_ULong_ULong_k_ = ULong_xor_ULong_ULong_k_
    this.inv_ULong_k_ = ULong_inv_ULong_k_
    this.toByte_B_k_ = ULong_toByte_B_k_
    this.toShort_S_k_ = ULong_toShort_S_k_
    this.toInt_I_k_ = ULong_toInt_I_k_
    this.toLong_J_k_ = ULong_toLong_J_k_
    this.toUByte_UByte_k_ = ULong_toUByte_UByte_k_
    this.toUShort_UShort_k_ = ULong_toUShort_UShort_k_
    this.toUInt_UInt_k_ = ULong_toUInt_UInt_k_
    this.toULong_ULong_k_ = ULong_toULong_ULong_k_
    this.toFloat_F_k_ = ULong_toFloat_F_k_
    this.toDouble_D_k_ = ULong_toDouble_D_k_
    this.toString_Str_k_ = ULong_toString_Str_k_
    this.equals_AnyN_Z_k_ = ULong_equals_AnyN_Z_k_
    this.hashCode_I_k_ = ULong_hashCode_I_k_
    this.get_data = ULong_get_data_J_k_
    return this
end function

function ULong_compareTo_UByte_I_k_(other as Object) as Integer
    return m.compareTo(other.toULong())
end function

function ULong_compareTo_UShort_I_k_(other as Object) as Integer
    return m.compareTo(other.toULong())
end function

function ULong_compareTo_UInt_I_k_(other as Object) as Integer
    return m.compareTo(other.toULong())
end function

function ULong_compareTo_ULong_I_k_(other as Object) as Integer
    return ulongCompare_J_J_I_k_(m.data, other.data)
end function

function ULong_plus_UByte_ULong_k_(other as Object) as Object
    return m + other.toULong()
end function

function ULong_plus_UShort_ULong_k_(other as Object) as Object
    return m + other.toULong()
end function

function ULong_plus_UInt_ULong_k_(other as Object) as Object
    return m + other.toULong()
end function

function ULong_plus_ULong_ULong_k_(other as Object) as Object
    return ULong_create_J_ULong_k_(m.data + other.data)
end function

function ULong_minus_UByte_ULong_k_(other as Object) as Object
    return m.minus(other.toULong())
end function

function ULong_minus_UShort_ULong_k_(other as Object) as Object
    return m.minus(other.toULong())
end function

function ULong_minus_UInt_ULong_k_(other as Object) as Object
    return m.minus(other.toULong())
end function

function ULong_minus_ULong_ULong_k_(other as Object) as Object
    return ULong_create_J_ULong_k_(m.data.minus(other.data))
end function

function ULong_times_UByte_ULong_k_(other as Object) as Object
    return m.times(other.toULong())
end function

function ULong_times_UShort_ULong_k_(other as Object) as Object
    return m.times(other.toULong())
end function

function ULong_times_UInt_ULong_k_(other as Object) as Object
    return m.times(other.toULong())
end function

function ULong_times_ULong_ULong_k_(other as Object) as Object
    return ULong_create_J_ULong_k_(m.data.times(other.data))
end function

function ULong_div_UByte_ULong_k_(other as Object) as Object
    return m.div(other.toULong())
end function

function ULong_div_UShort_ULong_k_(other as Object) as Object
    return m.div(other.toULong())
end function

function ULong_div_UInt_ULong_k_(other as Object) as Object
    return m.div(other.toULong())
end function

function ULong_div_ULong_ULong_k_(other as Object) as Object
    return ulongDivide_J_J_ULong_k_(m.data, other.data)
end function

function ULong_rem_UByte_ULong_k_(other as Object) as Object
    return m.rem(other.toULong())
end function

function ULong_rem_UShort_ULong_k_(other as Object) as Object
    return m.rem(other.toULong())
end function

function ULong_rem_UInt_ULong_k_(other as Object) as Object
    return m.rem(other.toULong())
end function

function ULong_rem_ULong_ULong_k_(other as Object) as Object
    return ulongRemainder_J_J_ULong_k_(m.data, other.data)
end function

function ULong_floorDiv_UByte_ULong_k_(other as Object) as Object
    return m.floorDiv(other.toULong())
end function

function ULong_floorDiv_UShort_ULong_k_(other as Object) as Object
    return m.floorDiv(other.toULong())
end function

function ULong_floorDiv_UInt_ULong_k_(other as Object) as Object
    return m.floorDiv(other.toULong())
end function

function ULong_floorDiv_ULong_ULong_k_(other as Object) as Object
    return m.div(other)
end function

function ULong_mod_UByte_UByte_k_(other as Object) as Object
    return m.mod(other.toULong()).toUByte()
end function

function ULong_mod_UShort_UShort_k_(other as Object) as Object
    return m.mod(other.toULong()).toUShort()
end function

function ULong_mod_UInt_UInt_k_(other as Object) as Object
    return m.mod(other.toULong()).toUInt()
end function

function ULong_mod_ULong_ULong_k_(other as Object) as Object
    return m.rem(other)
end function

function ULong_inc_ULong_k_() as Object
    return ULong_create_J_ULong_k_(m.data + 1&)
end function

function ULong_dec_ULong_k_() as Object
    return ULong_create_J_ULong_k_(m.data - 1&)
end function

function ULong_rangeTo_ULong_ULongRange_k_(other as Object) as Object
    return ULongRange_create_ULong_ULong_ULongRange_k_(m, other)
end function

function ULong_rangeUntil_ULong_ULongRange_k_(other as Object) as Object
    return until_rULong_ULong_ULongRange_k_(m, other)
end function

function ULong_shl_I_ULong_k_(bitCount as Integer) as Object
    return ULong_create_J_ULong_k_(m.data.shl(bitCount))
end function

function ULong_shr_I_ULong_k_(bitCount as Integer) as Object
    return ULong_create_J_ULong_k_(m.data.ushr(bitCount))
end function

function ULong_and_ULong_ULong_k_(other as Object) as Object
    return ULong_create_J_ULong_k_(m.data.and(other.data))
end function

function ULong_or_ULong_ULong_k_(other as Object) as Object
    return ULong_create_J_ULong_k_(m.data.or(other.data))
end function

function ULong_xor_ULong_ULong_k_(other as Object) as Object
    return ULong_create_J_ULong_k_(m.data.xor(other.data))
end function

function ULong_inv_ULong_k_() as Object
    return ULong_create_J_ULong_k_(m.data.inv())
end function

function ULong_toByte_B_k_() as Integer
    return m.data
end function

function ULong_toShort_S_k_() as Integer
    return m.data
end function

function ULong_toInt_I_k_() as Integer
    return m.data
end function

function ULong_toLong_J_k_() as LongInteger
    return m.data
end function

function ULong_toUByte_UByte_k_() as Object
    return UByte_create_B_UByte_k_(m.data)
end function

function ULong_toUShort_UShort_k_() as Object
    return UShort_create_S_UShort_k_(m.data)
end function

function ULong_toUInt_UInt_k_() as Object
    return UInt_create_I_UInt_k_(m.data)
end function

function ULong_toULong_ULong_k_() as Object
    return m
end function

function ULong_toFloat_F_k_() as Float
    return ulongToFloat_J_F_k_(m.data)
end function

function ULong_toDouble_D_k_() as Double
    return ulongToDouble_J_D_k_(m.data)
end function

function ULong_toString_Str_k_() as String
    return ulongToString_J_Str_k_(m.data)
end function

function ULong_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "ULong") and (m.data = other.data)
end function

function ULong_hashCode_I_k_() as Integer
    return m.data.hashCode()
end function

function ULong_get_data_J_k_() as LongInteger
    return m.data
end function

function ULong_Companion_create_Companion_k_() as Object
    this = {}
    this.__type = "ULong_Companion"
    this.__proto = ["ULong_Companion"]
    this.MIN_VALUE = ULong_create_J_ULong_k_(0&)
    this.MAX_VALUE = ULong_create_J_ULong_k_(-1&)
    this.SIZE_BYTES = 8
    this.SIZE_BITS = 64
    this.get_MIN_VALUE = ULong_Companion_get_MIN_VALUE_ULong_k_
    this.get_MAX_VALUE = ULong_Companion_get_MAX_VALUE_ULong_k_
    this.get_SIZE_BYTES = ULong_Companion_get_SIZE_BYTES_I_k_
    this.get_SIZE_BITS = ULong_Companion_get_SIZE_BITS_I_k_
    return this
end function

function ULong_Companion_getInstance() as Object
    if m.ULong_Companion_instance = invalid then
        m.ULong_Companion_instance = ULong_Companion_create()
    end if
    return m.ULong_Companion_instance
end function

function ULong_Companion_get_MIN_VALUE_ULong_k_() as Object
    return m.MIN_VALUE
end function

function ULong_Companion_get_MAX_VALUE_ULong_k_() as Object
    return m.MAX_VALUE
end function

function ULong_Companion_get_SIZE_BYTES_I_k_() as Integer
    return m.SIZE_BYTES
end function

function ULong_Companion_get_SIZE_BITS_I_k_() as Integer
    return m.SIZE_BITS
end function

function toULong_rB_ULong_k_(m as Integer) as Object
    return ULong_create_J_ULong_k_(m)
end function

function toULong_rS_ULong_k_(m as Integer) as Object
    return ULong_create_J_ULong_k_(m)
end function

function toULong_rI_ULong_k_(m as Integer) as Object
    return ULong_create_J_ULong_k_(m)
end function

function toULong_rJ_ULong_k_(m as LongInteger) as Object
    return ULong_create_J_ULong_k_(m)
end function

function toULong_rF_ULong_k_(m as Float) as Object
    return floatToULong_F_ULong_k_(m)
end function

function toULong_rD_ULong_k_(m as Double) as Object
    return doubleToULong_D_ULong_k_(m)
end function
