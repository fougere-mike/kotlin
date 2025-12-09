function UByte_create_B_UByte_k_(data as Integer) as Object
    this = {}
    this.__type = "UByte"
    this.__proto = ["UByte"]
    this.data = data
    this.compareTo_UByte_I_k_ = UByte_compareTo_UByte_I_k_
    this.compareTo_UShort_I_k_ = UByte_compareTo_UShort_I_k_
    this.compareTo_UInt_I_k_ = UByte_compareTo_UInt_I_k_
    this.compareTo_ULong_I_k_ = UByte_compareTo_ULong_I_k_
    this.plus_UByte_UInt_k_ = UByte_plus_UByte_UInt_k_
    this.plus_UShort_UInt_k_ = UByte_plus_UShort_UInt_k_
    this.plus_UInt_UInt_k_ = UByte_plus_UInt_UInt_k_
    this.plus_ULong_ULong_k_ = UByte_plus_ULong_ULong_k_
    this.minus_UByte_UInt_k_ = UByte_minus_UByte_UInt_k_
    this.minus_UShort_UInt_k_ = UByte_minus_UShort_UInt_k_
    this.minus_UInt_UInt_k_ = UByte_minus_UInt_UInt_k_
    this.minus_ULong_ULong_k_ = UByte_minus_ULong_ULong_k_
    this.times_UByte_UInt_k_ = UByte_times_UByte_UInt_k_
    this.times_UShort_UInt_k_ = UByte_times_UShort_UInt_k_
    this.times_UInt_UInt_k_ = UByte_times_UInt_UInt_k_
    this.times_ULong_ULong_k_ = UByte_times_ULong_ULong_k_
    this.div_UByte_UInt_k_ = UByte_div_UByte_UInt_k_
    this.div_UShort_UInt_k_ = UByte_div_UShort_UInt_k_
    this.div_UInt_UInt_k_ = UByte_div_UInt_UInt_k_
    this.div_ULong_ULong_k_ = UByte_div_ULong_ULong_k_
    this.rem_UByte_UInt_k_ = UByte_rem_UByte_UInt_k_
    this.rem_UShort_UInt_k_ = UByte_rem_UShort_UInt_k_
    this.rem_UInt_UInt_k_ = UByte_rem_UInt_UInt_k_
    this.rem_ULong_ULong_k_ = UByte_rem_ULong_ULong_k_
    this.floorDiv_UByte_UInt_k_ = UByte_floorDiv_UByte_UInt_k_
    this.floorDiv_UShort_UInt_k_ = UByte_floorDiv_UShort_UInt_k_
    this.floorDiv_UInt_UInt_k_ = UByte_floorDiv_UInt_UInt_k_
    this.floorDiv_ULong_ULong_k_ = UByte_floorDiv_ULong_ULong_k_
    this.mod_UByte_UByte_k_ = UByte_mod_UByte_UByte_k_
    this.mod_UShort_UShort_k_ = UByte_mod_UShort_UShort_k_
    this.mod_UInt_UInt_k_ = UByte_mod_UInt_UInt_k_
    this.mod_ULong_ULong_k_ = UByte_mod_ULong_ULong_k_
    this.inc_UByte_k_ = UByte_inc_UByte_k_
    this.dec_UByte_k_ = UByte_dec_UByte_k_
    this.rangeTo_UByte_UIntRange_k_ = UByte_rangeTo_UByte_UIntRange_k_
    this.rangeUntil_UByte_UIntRange_k_ = UByte_rangeUntil_UByte_UIntRange_k_
    this.and_UByte_UByte_k_ = UByte_and_UByte_UByte_k_
    this.or_UByte_UByte_k_ = UByte_or_UByte_UByte_k_
    this.xor_UByte_UByte_k_ = UByte_xor_UByte_UByte_k_
    this.inv_UByte_k_ = UByte_inv_UByte_k_
    this.toByte_B_k_ = UByte_toByte_B_k_
    this.toShort_S_k_ = UByte_toShort_S_k_
    this.toInt_I_k_ = UByte_toInt_I_k_
    this.toLong_J_k_ = UByte_toLong_J_k_
    this.toUByte_UByte_k_ = UByte_toUByte_UByte_k_
    this.toUShort_UShort_k_ = UByte_toUShort_UShort_k_
    this.toUInt_UInt_k_ = UByte_toUInt_UInt_k_
    this.toULong_ULong_k_ = UByte_toULong_ULong_k_
    this.toFloat_F_k_ = UByte_toFloat_F_k_
    this.toDouble_D_k_ = UByte_toDouble_D_k_
    this.toString_Str_k_ = UByte_toString_Str_k_
    this.equals_AnyN_Z_k_ = UByte_equals_AnyN_Z_k_
    this.hashCode_I_k_ = UByte_hashCode_I_k_
    this.get_data = UByte_get_data_B_k_
    return this
end function

function UByte_compareTo_UByte_I_k_(other as Object) as Integer
    return m.compareTo_I_I_k_(other)
end function

function UByte_compareTo_UShort_I_k_(other as Object) as Integer
    return m.compareTo_I_I_k_(other)
end function

function UByte_compareTo_UInt_I_k_(other as Object) as Integer
    return m.toUInt_UInt_k_().compareTo_UInt_I_k_(other)
end function

function UByte_compareTo_ULong_I_k_(other as Object) as Integer
    return m.toULong_ULong_k_().compareTo_ULong_I_k_(other)
end function

function UByte_plus_UByte_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_() + other.toUInt_UInt_k_()
end function

function UByte_plus_UShort_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_() + other.toUInt_UInt_k_()
end function

function UByte_plus_UInt_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_() + other
end function

function UByte_plus_ULong_ULong_k_(other as Object) as Object
    return m.toULong_ULong_k_() + other
end function

function UByte_minus_UByte_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().minus_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UByte_minus_UShort_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().minus_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UByte_minus_UInt_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().minus_UInt_UInt_k_(other)
end function

function UByte_minus_ULong_ULong_k_(other as Object) as Object
    return m.toULong_ULong_k_().minus_ULong_ULong_k_(other)
end function

function UByte_times_UByte_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().times_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UByte_times_UShort_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().times_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UByte_times_UInt_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().times_UInt_UInt_k_(other)
end function

function UByte_times_ULong_ULong_k_(other as Object) as Object
    return m.toULong_ULong_k_().times_ULong_ULong_k_(other)
end function

function UByte_div_UByte_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().div_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UByte_div_UShort_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().div_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UByte_div_UInt_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().div_UInt_UInt_k_(other)
end function

function UByte_div_ULong_ULong_k_(other as Object) as Object
    return m.toULong_ULong_k_().div_ULong_ULong_k_(other)
end function

function UByte_rem_UByte_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().rem_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UByte_rem_UShort_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().rem_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UByte_rem_UInt_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().rem_UInt_UInt_k_(other)
end function

function UByte_rem_ULong_ULong_k_(other as Object) as Object
    return m.toULong_ULong_k_().rem_ULong_ULong_k_(other)
end function

function UByte_floorDiv_UByte_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().floorDiv_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UByte_floorDiv_UShort_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().floorDiv_UInt_UInt_k_(other.toUInt_UInt_k_())
end function

function UByte_floorDiv_UInt_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().floorDiv_UInt_UInt_k_(other)
end function

function UByte_floorDiv_ULong_ULong_k_(other as Object) as Object
    return m.toULong_ULong_k_().floorDiv_ULong_ULong_k_(other)
end function

function UByte_mod_UByte_UByte_k_(other as Object) as Object
    return m.toUInt_UInt_k_().mod_UInt_UInt_k_(other.toUInt_UInt_k_()).toUByte_UByte_k_()
end function

function UByte_mod_UShort_UShort_k_(other as Object) as Object
    return m.toUInt_UInt_k_().mod_UInt_UInt_k_(other.toUInt_UInt_k_()).toUShort_UShort_k_()
end function

function UByte_mod_UInt_UInt_k_(other as Object) as Object
    return m.toUInt_UInt_k_().mod_UInt_UInt_k_(other)
end function

function UByte_mod_ULong_ULong_k_(other as Object) as Object
    return m.toULong_ULong_k_().mod_ULong_ULong_k_(other)
end function

function UByte_inc_UByte_k_() as Object
    return UByte_create_B_UByte_k_(m.get_data() + 1)
end function

function UByte_dec_UByte_k_() as Object
    return UByte_create_B_UByte_k_(m.get_data() - 1)
end function

function UByte_rangeTo_UByte_UIntRange_k_(other as Object) as Object
    return UIntRange_create_UInt_UInt_UIntRange_k_(m.toUInt_UInt_k_(), other.toUInt_UInt_k_())
end function

function UByte_rangeUntil_UByte_UIntRange_k_(other as Object) as Object
    return until_rUInt_UInt_UIntRange_k_(m.toUInt_UInt_k_(), other.toUInt_UInt_k_())
end function

function UByte_and_UByte_UByte_k_(other as Object) as Object
    return UByte_create_B_UByte_k_(m.and_I_I_k_(other))
end function

function UByte_or_UByte_UByte_k_(other as Object) as Object
    return UByte_create_B_UByte_k_(m.or_I_I_k_(other))
end function

function UByte_xor_UByte_UByte_k_(other as Object) as Object
    return UByte_create_B_UByte_k_(m.xor_I_I_k_(other))
end function

function UByte_inv_UByte_k_() as Object
    return UByte_create_B_UByte_k_(m.inv_I_k_())
end function

function UByte_toByte_B_k_() as Integer
    return m.get_data()
end function

function UByte_toShort_S_k_() as Integer
    return m.get_data().and_I_I_k_(255)
end function

function UByte_toInt_I_k_() as Integer
    return m.get_data().and_I_I_k_(255)
end function

function UByte_toLong_J_k_() as LongInteger
    return m.get_data().and_J_J_k_(255&)
end function

function UByte_toUByte_UByte_k_() as Object
    return m
end function

function UByte_toUShort_UShort_k_() as Object
    return UShort_create_S_UShort_k_(m.get_data().and_I_I_k_(255))
end function

function UByte_toUInt_UInt_k_() as Object
    return UInt_create_I_UInt_k_(m.get_data().and_I_I_k_(255))
end function

function UByte_toULong_ULong_k_() as Object
    return ULong_create_J_ULong_k_(m.get_data().and_J_J_k_(255&))
end function

function UByte_toFloat_F_k_() as Float
    return m
end function

function UByte_toDouble_D_k_() as Double
    return m
end function

function UByte_toString_Str_k_() as String
    return Str(m)
end function

function UByte_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "UByte") and (m.get_data() = other.get_data())
end function

function UByte_hashCode_I_k_() as Integer
    return m.get_data()
end function

function UByte_get_data_B_k_() as Integer
    return m.data
end function

function UByte_Companion_create_Companion_k_() as Object
    this = {}
    this.__type = "UByte_Companion"
    this.__proto = ["UByte_Companion"]
    this.MIN_VALUE = UByte_create_B_UByte_k_(0)
    this.MAX_VALUE = UByte_create_B_UByte_k_(-1)
    this.SIZE_BYTES = 1
    this.SIZE_BITS = 8
    this.get_MIN_VALUE = UByte_Companion_get_MIN_VALUE_UByte_k_
    this.get_MAX_VALUE = UByte_Companion_get_MAX_VALUE_UByte_k_
    this.get_SIZE_BYTES = UByte_Companion_get_SIZE_BYTES_I_k_
    this.get_SIZE_BITS = UByte_Companion_get_SIZE_BITS_I_k_
    return this
end function

function UByte_Companion_getInstance() as Object
    if m.UByte_Companion_instance = invalid then
        m.UByte_Companion_instance = UByte_Companion_create_Companion_k_()
    end if
    return m.UByte_Companion_instance
end function

function UByte_Companion_get_MIN_VALUE_UByte_k_() as Object
    return m.MIN_VALUE
end function

function UByte_Companion_get_MAX_VALUE_UByte_k_() as Object
    return m.MAX_VALUE
end function

function UByte_Companion_get_SIZE_BYTES_I_k_() as Integer
    return m.SIZE_BYTES
end function

function UByte_Companion_get_SIZE_BITS_I_k_() as Integer
    return m.SIZE_BITS
end function

function toUByte_rB_UByte_k_(m as Integer) as Object
    return UByte_create_B_UByte_k_(m)
end function

function toUByte_rS_UByte_k_(m as Integer) as Object
    return UByte_create_B_UByte_k_(m)
end function

function toUByte_rI_UByte_k_(m as Integer) as Object
    return UByte_create_B_UByte_k_(m)
end function

function toUByte_rJ_UByte_k_(m as LongInteger) as Object
    return UByte_create_B_UByte_k_(m)
end function
