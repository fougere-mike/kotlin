function UInt_create_I_UInt_k_(data as Integer) as Object
    this = {}
    this.__type = "UInt"
    this.__proto = ["UInt"]
    this.data = data
    this.compareTo_UByte_I_k_ = UInt_compareTo_UByte_I_k_
    this.compareTo_UShort_I_k_ = UInt_compareTo_UShort_I_k_
    this.compareTo_UInt_I_k_ = UInt_compareTo_UInt_I_k_
    this.compareTo_ULong_I_k_ = UInt_compareTo_ULong_I_k_
    this.plus_UByte_UInt_k_ = UInt_plus_UByte_UInt_k_
    this.plus_UShort_UInt_k_ = UInt_plus_UShort_UInt_k_
    this.plus_UInt_UInt_k_ = UInt_plus_UInt_UInt_k_
    this.plus_ULong_ULong_k_ = UInt_plus_ULong_ULong_k_
    this.minus_UByte_UInt_k_ = UInt_minus_UByte_UInt_k_
    this.minus_UShort_UInt_k_ = UInt_minus_UShort_UInt_k_
    this.minus_UInt_UInt_k_ = UInt_minus_UInt_UInt_k_
    this.minus_ULong_ULong_k_ = UInt_minus_ULong_ULong_k_
    this.times_UByte_UInt_k_ = UInt_times_UByte_UInt_k_
    this.times_UShort_UInt_k_ = UInt_times_UShort_UInt_k_
    this.times_UInt_UInt_k_ = UInt_times_UInt_UInt_k_
    this.times_ULong_ULong_k_ = UInt_times_ULong_ULong_k_
    this.div_UByte_UInt_k_ = UInt_div_UByte_UInt_k_
    this.div_UShort_UInt_k_ = UInt_div_UShort_UInt_k_
    this.div_UInt_UInt_k_ = UInt_div_UInt_UInt_k_
    this.div_ULong_ULong_k_ = UInt_div_ULong_ULong_k_
    this.rem_UByte_UInt_k_ = UInt_rem_UByte_UInt_k_
    this.rem_UShort_UInt_k_ = UInt_rem_UShort_UInt_k_
    this.rem_UInt_UInt_k_ = UInt_rem_UInt_UInt_k_
    this.rem_ULong_ULong_k_ = UInt_rem_ULong_ULong_k_
    this.floorDiv_UByte_UInt_k_ = UInt_floorDiv_UByte_UInt_k_
    this.floorDiv_UShort_UInt_k_ = UInt_floorDiv_UShort_UInt_k_
    this.floorDiv_UInt_UInt_k_ = UInt_floorDiv_UInt_UInt_k_
    this.floorDiv_ULong_ULong_k_ = UInt_floorDiv_ULong_ULong_k_
    this.mod_UByte_UByte_k_ = UInt_mod_UByte_UByte_k_
    this.mod_UShort_UShort_k_ = UInt_mod_UShort_UShort_k_
    this.mod_UInt_UInt_k_ = UInt_mod_UInt_UInt_k_
    this.mod_ULong_ULong_k_ = UInt_mod_ULong_ULong_k_
    this.inc_UInt_k_ = UInt_inc_UInt_k_
    this.dec_UInt_k_ = UInt_dec_UInt_k_
    this.rangeTo_UInt_UIntRange_k_ = UInt_rangeTo_UInt_UIntRange_k_
    this.rangeUntil_UInt_UIntRange_k_ = UInt_rangeUntil_UInt_UIntRange_k_
    this.shl_I_UInt_k_ = UInt_shl_I_UInt_k_
    this.shr_I_UInt_k_ = UInt_shr_I_UInt_k_
    this.and_UInt_UInt_k_ = UInt_and_UInt_UInt_k_
    this.or_UInt_UInt_k_ = UInt_or_UInt_UInt_k_
    this.xor_UInt_UInt_k_ = UInt_xor_UInt_UInt_k_
    this.inv_UInt_k_ = UInt_inv_UInt_k_
    this.toByte_B_k_ = UInt_toByte_B_k_
    this.toShort_S_k_ = UInt_toShort_S_k_
    this.toInt_I_k_ = UInt_toInt_I_k_
    this.toLong_J_k_ = UInt_toLong_J_k_
    this.toUByte_UByte_k_ = UInt_toUByte_UByte_k_
    this.toUShort_UShort_k_ = UInt_toUShort_UShort_k_
    this.toUInt_UInt_k_ = UInt_toUInt_UInt_k_
    this.toULong_ULong_k_ = UInt_toULong_ULong_k_
    this.toFloat_F_k_ = UInt_toFloat_F_k_
    this.toDouble_D_k_ = UInt_toDouble_D_k_
    this.toString_Str_k_ = UInt_toString_Str_k_
    this.equals_AnyN_Z_k_ = UInt_equals_AnyN_Z_k_
    this.hashCode_I_k_ = UInt_hashCode_I_k_
    this.get_data = UInt_get_data_I_k_
    return this
end function

function UInt_compareTo_UByte_I_k_(other as Object) as Integer
    return m.compareTo_UInt_I_k_(other.toUInt_UInt_k_())
end function

function UInt_compareTo_UShort_I_k_(other as Object) as Integer
    return m.compareTo_UInt_I_k_(other.toUInt_UInt_k_())
end function

function UInt_compareTo_UInt_I_k_(other as Object) as Integer
    return uintCompare_I_I_I_k_(m.get_data(), other.get_data())
end function

function UInt_compareTo_ULong_I_k_(other as Object) as Integer
    return m.toULong_ULong_k_().compareTo_ULong_I_k_(other)
end function

function UInt_plus_UByte_UInt_k_(other as Object) as Object
    return m + other.toUInt_UInt_k_()
end function

function UInt_plus_UShort_UInt_k_(other as Object) as Object
    return m + other.toUInt_UInt_k_()
end function

function UInt_plus_UInt_UInt_k_(other as Object) as Object
    return UInt_create_I_UInt_k_(m.get_data() + other.get_data())
end function

function UInt_plus_ULong_ULong_k_(other as Object) as Object
    return m.toULong_ULong_k_() + other
end function

function UInt_minus_UByte_UInt_k_(other as Object) as Object
    return m.minus_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UInt_minus_UShort_UInt_k_(other as Object) as Object
    return m.minus_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UInt_minus_UInt_UInt_k_(other as Object) as Object
    return UInt_create_I_UInt_k_(m.get_data().minus_I_I_k_(other.get_data()))
end function

function UInt_minus_ULong_ULong_k_(other as Object) as Object
    return m.toULong_ULong_k_().minus_ULong_ULong_k_(other)
end function

function UInt_times_UByte_UInt_k_(other as Object) as Object
    return m.times_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UInt_times_UShort_UInt_k_(other as Object) as Object
    return m.times_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UInt_times_UInt_UInt_k_(other as Object) as Object
    return UInt_create_I_UInt_k_(m.get_data().times_I_I_k_(other.get_data()))
end function

function UInt_times_ULong_ULong_k_(other as Object) as Object
    return m.toULong_ULong_k_().times_ULong_ULong_k_(other)
end function

function UInt_div_UByte_UInt_k_(other as Object) as Object
    return m.div_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UInt_div_UShort_UInt_k_(other as Object) as Object
    return m.div_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UInt_div_UInt_UInt_k_(other as Object) as Object
    return uintDivide_I_I_UInt_k_(m.get_data(), other.get_data())
end function

function UInt_div_ULong_ULong_k_(other as Object) as Object
    return m.toULong_ULong_k_().div_ULong_ULong_k_(other)
end function

function UInt_rem_UByte_UInt_k_(other as Object) as Object
    return m.rem_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UInt_rem_UShort_UInt_k_(other as Object) as Object
    return m.rem_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UInt_rem_UInt_UInt_k_(other as Object) as Object
    return uintRemainder_I_I_UInt_k_(m.get_data(), other.get_data())
end function

function UInt_rem_ULong_ULong_k_(other as Object) as Object
    return m.toULong_ULong_k_().rem_ULong_ULong_k_(other)
end function

function UInt_floorDiv_UByte_UInt_k_(other as Object) as Object
    return m.floorDiv_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UInt_floorDiv_UShort_UInt_k_(other as Object) as Object
    return m.floorDiv_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UInt_floorDiv_UInt_UInt_k_(other as Object) as Object
    return m.div_UInt_UInt_k_(other)
end function

function UInt_floorDiv_ULong_ULong_k_(other as Object) as Object
    return m.toULong_ULong_k_().floorDiv_ULong_ULong_k_(other)
end function

function UInt_mod_UByte_UByte_k_(other as Object) as Object
    return m.mod_UInt_UInt_k_(other.toUInt_UInt_k_()).toUByte_UByte_k_()
end function

function UInt_mod_UShort_UShort_k_(other as Object) as Object
    return m.mod_UInt_UInt_k_(other.toUInt_UInt_k_()).toUShort_UShort_k_()
end function

function UInt_mod_UInt_UInt_k_(other as Object) as Object
    return m.rem_UInt_UInt_k_(other)
end function

function UInt_mod_ULong_ULong_k_(other as Object) as Object
    return m.toULong_ULong_k_().mod_ULong_ULong_k_(other)
end function

function UInt_inc_UInt_k_() as Object
    return UInt_create_I_UInt_k_(m.get_data() + 1)
end function

function UInt_dec_UInt_k_() as Object
    return UInt_create_I_UInt_k_(m.get_data() - 1)
end function

function UInt_rangeTo_UInt_UIntRange_k_(other as Object) as Object
    return UIntRange_create_UInt_UInt_UIntRange_k_(m, other)
end function

function UInt_rangeUntil_UInt_UIntRange_k_(other as Object) as Object
    return until_rUInt_UInt_UIntRange_k_(m, other)
end function

function UInt_shl_I_UInt_k_(bitCount as Integer) as Object
    return UInt_create_I_UInt_k_(m.get_data().shl_I_I_k_(bitCount))
end function

function UInt_shr_I_UInt_k_(bitCount as Integer) as Object
    return UInt_create_I_UInt_k_(m.get_data().ushr_I_I_k_(bitCount))
end function

function UInt_and_UInt_UInt_k_(other as Object) as Object
    return UInt_create_I_UInt_k_(m.get_data().and_I_I_k_(other.get_data()))
end function

function UInt_or_UInt_UInt_k_(other as Object) as Object
    return UInt_create_I_UInt_k_(m.get_data().or_I_I_k_(other.get_data()))
end function

function UInt_xor_UInt_UInt_k_(other as Object) as Object
    return UInt_create_I_UInt_k_(m.get_data().xor_I_I_k_(other.get_data()))
end function

function UInt_inv_UInt_k_() as Object
    return UInt_create_I_UInt_k_(m.get_data().inv_I_k_())
end function

function UInt_toByte_B_k_() as Integer
    return m.get_data()
end function

function UInt_toShort_S_k_() as Integer
    return m.get_data()
end function

function UInt_toInt_I_k_() as Integer
    return m.get_data()
end function

function UInt_toLong_J_k_() as LongInteger
    return uintToLong_I_J_k_(m.get_data())
end function

function UInt_toUByte_UByte_k_() as Object
    return UByte_create_B_UByte_k_(m.get_data())
end function

function UInt_toUShort_UShort_k_() as Object
    return UShort_create_S_UShort_k_(m.get_data())
end function

function UInt_toUInt_UInt_k_() as Object
    return m
end function

function UInt_toULong_ULong_k_() as Object
    return uintToULong_I_ULong_k_(m.get_data())
end function

function UInt_toFloat_F_k_() as Float
    return uintToFloat_I_F_k_(m.get_data())
end function

function UInt_toDouble_D_k_() as Double
    return uintToDouble_I_D_k_(m.get_data())
end function

function UInt_toString_Str_k_() as String
    return uintToString_I_Str_k_(m.get_data())
end function

function UInt_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "UInt") and (m.get_data() = other.get_data())
end function

function UInt_hashCode_I_k_() as Integer
    return m.get_data()
end function

function UInt_get_data_I_k_() as Integer
    return m.data
end function

function UInt_Companion_create_Companion_k_() as Object
    this = {}
    this.__type = "UInt_Companion"
    this.__proto = ["UInt_Companion"]
    this.MIN_VALUE = UInt_create_I_UInt_k_(0)
    this.MAX_VALUE = UInt_create_I_UInt_k_(-1)
    this.SIZE_BYTES = 4
    this.SIZE_BITS = 32
    this.get_MIN_VALUE = UInt_Companion_get_MIN_VALUE_UInt_k_
    this.get_MAX_VALUE = UInt_Companion_get_MAX_VALUE_UInt_k_
    this.get_SIZE_BYTES = UInt_Companion_get_SIZE_BYTES_I_k_
    this.get_SIZE_BITS = UInt_Companion_get_SIZE_BITS_I_k_
    return this
end function

function UInt_Companion_getInstance() as Object
    if m.UInt_Companion_instance = invalid then
        m.UInt_Companion_instance = UInt_Companion_create_Companion_k_()
    end if
    return m.UInt_Companion_instance
end function

function UInt_Companion_get_MIN_VALUE_UInt_k_() as Object
    return m.MIN_VALUE
end function

function UInt_Companion_get_MAX_VALUE_UInt_k_() as Object
    return m.MAX_VALUE
end function

function UInt_Companion_get_SIZE_BYTES_I_k_() as Integer
    return m.SIZE_BYTES
end function

function UInt_Companion_get_SIZE_BITS_I_k_() as Integer
    return m.SIZE_BITS
end function

function toUInt_rB_UInt_k_(m as Integer) as Object
    return UInt_create_I_UInt_k_(m)
end function

function toUInt_rS_UInt_k_(m as Integer) as Object
    return UInt_create_I_UInt_k_(m)
end function

function toUInt_rI_UInt_k_(m as Integer) as Object
    return UInt_create_I_UInt_k_(m)
end function

function toUInt_rJ_UInt_k_(m as LongInteger) as Object
    return UInt_create_I_UInt_k_(m)
end function

function toUInt_rF_UInt_k_(m as Float) as Object
    return floatToUInt_F_UInt_k_(m)
end function

function toUInt_rD_UInt_k_(m as Double) as Object
    return doubleToUInt_D_UInt_k_(m)
end function
