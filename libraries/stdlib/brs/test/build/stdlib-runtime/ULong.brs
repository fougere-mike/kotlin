function ULong_create_J_k_(data as LongInteger) as Object
    this = {}
    this.__type = "ULong"
    this.__proto = ["ULong", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.compareTo_UByte_k_ = ULong_compareTo_UByte_k_
    this.compareTo_UShort_k_ = ULong_compareTo_UShort_k_
    this.compareTo_UInt_k_ = ULong_compareTo_UInt_k_
    this.compareTo_ULong_k_ = ULong_compareTo_ULong_k_
    this.plus_UByte_k_ = ULong_plus_UByte_k_
    this.plus_UShort_k_ = ULong_plus_UShort_k_
    this.plus_UInt_k_ = ULong_plus_UInt_k_
    this.plus_ULong_k_ = ULong_plus_ULong_k_
    this.minus_UByte_k_ = ULong_minus_UByte_k_
    this.minus_UShort_k_ = ULong_minus_UShort_k_
    this.minus_UInt_k_ = ULong_minus_UInt_k_
    this.minus_ULong_k_ = ULong_minus_ULong_k_
    this.times_UByte_k_ = ULong_times_UByte_k_
    this.times_UShort_k_ = ULong_times_UShort_k_
    this.times_UInt_k_ = ULong_times_UInt_k_
    this.times_ULong_k_ = ULong_times_ULong_k_
    this.div_UByte_k_ = ULong_div_UByte_k_
    this.div_UShort_k_ = ULong_div_UShort_k_
    this.div_UInt_k_ = ULong_div_UInt_k_
    this.div_ULong_k_ = ULong_div_ULong_k_
    this.rem_UByte_k_ = ULong_rem_UByte_k_
    this.rem_UShort_k_ = ULong_rem_UShort_k_
    this.rem_UInt_k_ = ULong_rem_UInt_k_
    this.rem_ULong_k_ = ULong_rem_ULong_k_
    this.floorDiv_UByte_k_ = ULong_floorDiv_UByte_k_
    this.floorDiv_UShort_k_ = ULong_floorDiv_UShort_k_
    this.floorDiv_UInt_k_ = ULong_floorDiv_UInt_k_
    this.floorDiv_ULong_k_ = ULong_floorDiv_ULong_k_
    this.mod_UByte_k_ = ULong_mod_UByte_k_
    this.mod_UShort_k_ = ULong_mod_UShort_k_
    this.mod_UInt_k_ = ULong_mod_UInt_k_
    this.mod_ULong_k_ = ULong_mod_ULong_k_
    this.inc_k_ = ULong_inc_k_
    this.dec_k_ = ULong_dec_k_
    this.rangeTo_ULong_k_ = ULong_rangeTo_ULong_k_
    this.rangeUntil_ULong_k_ = ULong_rangeUntil_ULong_k_
    this.shl_I_k_ = ULong_shl_I_k_
    this.shr_I_k_ = ULong_shr_I_k_
    this.and_ULong_k_ = ULong_and_ULong_k_
    this.or_ULong_k_ = ULong_or_ULong_k_
    this.xor_ULong_k_ = ULong_xor_ULong_k_
    this.inv_k_ = ULong_inv_k_
    this.toByte_k_ = ULong_toByte_k_
    this.toShort_k_ = ULong_toShort_k_
    this.toInt_k_ = ULong_toInt_k_
    this.toLong_k_ = ULong_toLong_k_
    this.toUByte_k_ = ULong_toUByte_k_
    this.toUShort_k_ = ULong_toUShort_k_
    this.toUInt_k_ = ULong_toUInt_k_
    this.toULong_k_ = ULong_toULong_k_
    this.toFloat_k_ = ULong_toFloat_k_
    this.toDouble_k_ = ULong_toDouble_k_
    this.toString_k_ = ULong_toString_k_
    this.toString = ULong_toString_k_
    this.equals_AnyN_k_ = ULong_equals_AnyN_k_
    this.equals = ULong_equals_AnyN_k_
    this.hashCode_k_ = ULong_hashCode_k_
    this.hashCode = ULong_hashCode_k_
    this.get_data = ULong_get_data_k_
    this.data = data
    return this
end function

function ULong_compareTo_UByte_k_(other as Object) as Integer
    return m.compareTo_ULong_k_(other.toULong_k_())
end function

function ULong_compareTo_UShort_k_(other as Object) as Integer
    return m.compareTo_ULong_k_(other.toULong_k_())
end function

function ULong_compareTo_UInt_k_(other as Object) as Integer
    return m.compareTo_ULong_k_(other.toULong_k_())
end function

function ULong_compareTo_ULong_k_(other as Object) as Integer
    return ulongCompare_J_J_k_(m.get_data(), other.get_data())
end function

function ULong_plus_UByte_k_(other as Object) as Object
    return m.plus_ULong_k_(other.toULong_k_())
end function

function ULong_plus_UShort_k_(other as Object) as Object
    return m.plus_ULong_k_(other.toULong_k_())
end function

function ULong_plus_UInt_k_(other as Object) as Object
    return m.plus_ULong_k_(other.toULong_k_())
end function

function ULong_plus_ULong_k_(other as Object) as Object
    return ULong_create_J_k_(m.get_data() + other.get_data())
end function

function ULong_minus_UByte_k_(other as Object) as Object
    return m.minus_ULong_k_(other.toULong_k_())
end function

function ULong_minus_UShort_k_(other as Object) as Object
    return m.minus_ULong_k_(other.toULong_k_())
end function

function ULong_minus_UInt_k_(other as Object) as Object
    return m.minus_ULong_k_(other.toULong_k_())
end function

function ULong_minus_ULong_k_(other as Object) as Object
    return ULong_create_J_k_(m.get_data() - other.get_data())
end function

function ULong_times_UByte_k_(other as Object) as Object
    return m.times_ULong_k_(other.toULong_k_())
end function

function ULong_times_UShort_k_(other as Object) as Object
    return m.times_ULong_k_(other.toULong_k_())
end function

function ULong_times_UInt_k_(other as Object) as Object
    return m.times_ULong_k_(other.toULong_k_())
end function

function ULong_times_ULong_k_(other as Object) as Object
    return ULong_create_J_k_(m.get_data() * other.get_data())
end function

function ULong_div_UByte_k_(other as Object) as Object
    return m.div_ULong_k_(other.toULong_k_())
end function

function ULong_div_UShort_k_(other as Object) as Object
    return m.div_ULong_k_(other.toULong_k_())
end function

function ULong_div_UInt_k_(other as Object) as Object
    return m.div_ULong_k_(other.toULong_k_())
end function

function ULong_div_ULong_k_(other as Object) as Object
    return ulongDivide_J_J_k_(m.get_data(), other.get_data())
end function

function ULong_rem_UByte_k_(other as Object) as Object
    return m.rem_ULong_k_(other.toULong_k_())
end function

function ULong_rem_UShort_k_(other as Object) as Object
    return m.rem_ULong_k_(other.toULong_k_())
end function

function ULong_rem_UInt_k_(other as Object) as Object
    return m.rem_ULong_k_(other.toULong_k_())
end function

function ULong_rem_ULong_k_(other as Object) as Object
    return ulongRemainder_J_J_k_(m.get_data(), other.get_data())
end function

function ULong_floorDiv_UByte_k_(other as Object) as Object
    return m.floorDiv_ULong_k_(other.toULong_k_())
end function

function ULong_floorDiv_UShort_k_(other as Object) as Object
    return m.floorDiv_ULong_k_(other.toULong_k_())
end function

function ULong_floorDiv_UInt_k_(other as Object) as Object
    return m.floorDiv_ULong_k_(other.toULong_k_())
end function

function ULong_floorDiv_ULong_k_(other as Object) as Object
    return m.div_ULong_k_(other)
end function

function ULong_mod_UByte_k_(other as Object) as Object
    return m.mod_ULong_k_(other.toULong_k_()).toUByte_k_()
end function

function ULong_mod_UShort_k_(other as Object) as Object
    return m.mod_ULong_k_(other.toULong_k_()).toUShort_k_()
end function

function ULong_mod_UInt_k_(other as Object) as Object
    return m.mod_ULong_k_(other.toULong_k_()).toUInt_k_()
end function

function ULong_mod_ULong_k_(other as Object) as Object
    return m.rem_ULong_k_(other)
end function

function ULong_inc_k_() as Object
    return ULong_create_J_k_(m.get_data() + 1&)
end function

function ULong_dec_k_() as Object
    return ULong_create_J_k_(m.get_data() - 1&)
end function

function ULong_rangeTo_ULong_k_(other as Object) as Object
    return ULongRange_create_ULong_ULong_k_(m, other)
end function

function ULong_rangeUntil_ULong_k_(other as Object) as Object
    return until_rULong_ULong_k_(m, other)
end function

function ULong_shl_I_k_(bitCount as Integer) as Object
    return ULong_create_J_k_(m.get_data() * (2 ^ bitCount))
end function

function ULong_shr_I_k_(bitCount as Integer) as Object
    return ULong_create_J_k_(__kotlin_ushr(m.get_data(), bitCount))
end function

function ULong_and_ULong_k_(other as Object) as Object
    return ULong_create_J_k_(m.get_data() and other.get_data())
end function

function ULong_or_ULong_k_(other as Object) as Object
    return ULong_create_J_k_(m.get_data() or other.get_data())
end function

function ULong_xor_ULong_k_(other as Object) as Object
    return ULong_create_J_k_((m.get_data() or other.get_data()) and not (m.get_data() and other.get_data()))
end function

function ULong_inv_k_() as Object
    return ULong_create_J_k_(not m.get_data())
end function

function ULong_toByte_k_() as Integer
    return m.get_data()
end function

function ULong_toShort_k_() as Integer
    return m.get_data()
end function

function ULong_toInt_k_() as Integer
    return m.get_data()
end function

function ULong_toLong_k_() as LongInteger
    return m.get_data()
end function

function ULong_toUByte_k_() as Object
    return UByte_create_B_k_(m.get_data())
end function

function ULong_toUShort_k_() as Object
    return UShort_create_S_k_(m.get_data())
end function

function ULong_toUInt_k_() as Object
    return UInt_create_I_k_(m.get_data())
end function

function ULong_toULong_k_() as Object
    return m
end function

function ULong_toFloat_k_() as Float
    return ulongToFloat_J_k_(m.get_data())
end function

function ULong_toDouble_k_() as Double
    return ulongToDouble_J_k_(m.get_data())
end function

function ULong_toString_k_() as String
    return ulongToString_J_k_(m.get_data())
end function

function ULong_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "ULong") and (m.get_data() = other.get_data())
end function

function ULong_hashCode_k_() as Integer
    return hashCode_rJ_k_(m.get_data())
end function

function ULong_get_data_k_() as LongInteger
    return m.data
end function

function ULong_Companion_create_k_() as Object
    this = {}
    this.__type = "ULong_Companion"
    this.__proto = ["ULong_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.get_MIN_VALUE = ULong_Companion_get_MIN_VALUE_k_
    this.get_MAX_VALUE = ULong_Companion_get_MAX_VALUE_k_
    this.get_SIZE_BYTES = ULong_Companion_get_SIZE_BYTES_k_
    this.get_SIZE_BITS = ULong_Companion_get_SIZE_BITS_k_
    this.MIN_VALUE = ULong_create_J_k_(0&)
    this.MAX_VALUE = ULong_create_J_k_(-1&)
    this.SIZE_BYTES = 8
    this.SIZE_BITS = 64
    return this
end function

function ULong_Companion_getInstance() as Object
    if GetGlobalAA().ULong_Companion_instance = invalid then
        GetGlobalAA().ULong_Companion_instance = ULong_Companion_create_k_()
    end if
    return GetGlobalAA().ULong_Companion_instance
end function

function ULong_Companion_get_MIN_VALUE_k_() as Object
    return m.MIN_VALUE
end function

function ULong_Companion_get_MAX_VALUE_k_() as Object
    return m.MAX_VALUE
end function

function ULong_Companion_get_SIZE_BYTES_k_() as Integer
    return m.SIZE_BYTES
end function

function ULong_Companion_get_SIZE_BITS_k_() as Integer
    return m.SIZE_BITS
end function

function toULong_rB_k_(m as Integer) as Object
    return ULong_create_J_k_(m)
end function

function toULong_rS_k_(m as Integer) as Object
    return ULong_create_J_k_(m)
end function

function toULong_rI_k_(m as Integer) as Object
    return ULong_create_J_k_(m)
end function

function toULong_rJ_k_(m as LongInteger) as Object
    return ULong_create_J_k_(m)
end function

function toULong_rF_k_(m as Float) as Object
    return floatToULong_F_k_(m)
end function

function toULong_rD_k_(m as Double) as Object
    return doubleToULong_D_k_(m)
end function
