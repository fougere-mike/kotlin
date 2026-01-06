function UShort_create_S_k_(data as Integer) as Object
    this = {}
    this.__type = "UShort"
    this.__proto = ["UShort", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.compareTo_UByte_k_ = UShort_compareTo_UByte_k_
    this.compareTo_UShort_k_ = UShort_compareTo_UShort_k_
    this.compareTo_UInt_k_ = UShort_compareTo_UInt_k_
    this.compareTo_ULong_k_ = UShort_compareTo_ULong_k_
    this.plus_UByte_k_ = UShort_plus_UByte_k_
    this.plus_UShort_k_ = UShort_plus_UShort_k_
    this.plus_UInt_k_ = UShort_plus_UInt_k_
    this.plus_ULong_k_ = UShort_plus_ULong_k_
    this.minus_UByte_k_ = UShort_minus_UByte_k_
    this.minus_UShort_k_ = UShort_minus_UShort_k_
    this.minus_UInt_k_ = UShort_minus_UInt_k_
    this.minus_ULong_k_ = UShort_minus_ULong_k_
    this.times_UByte_k_ = UShort_times_UByte_k_
    this.times_UShort_k_ = UShort_times_UShort_k_
    this.times_UInt_k_ = UShort_times_UInt_k_
    this.times_ULong_k_ = UShort_times_ULong_k_
    this.div_UByte_k_ = UShort_div_UByte_k_
    this.div_UShort_k_ = UShort_div_UShort_k_
    this.div_UInt_k_ = UShort_div_UInt_k_
    this.div_ULong_k_ = UShort_div_ULong_k_
    this.rem_UByte_k_ = UShort_rem_UByte_k_
    this.rem_UShort_k_ = UShort_rem_UShort_k_
    this.rem_UInt_k_ = UShort_rem_UInt_k_
    this.rem_ULong_k_ = UShort_rem_ULong_k_
    this.floorDiv_UByte_k_ = UShort_floorDiv_UByte_k_
    this.floorDiv_UShort_k_ = UShort_floorDiv_UShort_k_
    this.floorDiv_UInt_k_ = UShort_floorDiv_UInt_k_
    this.floorDiv_ULong_k_ = UShort_floorDiv_ULong_k_
    this.mod_UByte_k_ = UShort_mod_UByte_k_
    this.mod_UShort_k_ = UShort_mod_UShort_k_
    this.mod_UInt_k_ = UShort_mod_UInt_k_
    this.mod_ULong_k_ = UShort_mod_ULong_k_
    this.inc_k_ = UShort_inc_k_
    this.dec_k_ = UShort_dec_k_
    this.rangeTo_UShort_k_ = UShort_rangeTo_UShort_k_
    this.rangeUntil_UShort_k_ = UShort_rangeUntil_UShort_k_
    this.and_UShort_k_ = UShort_and_UShort_k_
    this.or_UShort_k_ = UShort_or_UShort_k_
    this.xor_UShort_k_ = UShort_xor_UShort_k_
    this.inv_k_ = UShort_inv_k_
    this.toByte_k_ = UShort_toByte_k_
    this.toShort_k_ = UShort_toShort_k_
    this.toInt_k_ = UShort_toInt_k_
    this.toLong_k_ = UShort_toLong_k_
    this.toUByte_k_ = UShort_toUByte_k_
    this.toUShort_k_ = UShort_toUShort_k_
    this.toUInt_k_ = UShort_toUInt_k_
    this.toULong_k_ = UShort_toULong_k_
    this.toFloat_k_ = UShort_toFloat_k_
    this.toDouble_k_ = UShort_toDouble_k_
    this.toString_k_ = UShort_toString_k_
    this.toString = UShort_toString_k_
    this.equals_AnyN_k_ = UShort_equals_AnyN_k_
    this.equals = UShort_equals_AnyN_k_
    this.hashCode_k_ = UShort_hashCode_k_
    this.hashCode = UShort_hashCode_k_
    this.get_data = UShort_get_data_k_
    this.data = data
    return this
end function

function UShort_compareTo_UByte_k_(other as Object) as Integer
    return __kotlin_intCompare(m, other)
end function

function UShort_compareTo_UShort_k_(other as Object) as Integer
    return __kotlin_intCompare(m, other)
end function

function UShort_compareTo_UInt_k_(other as Object) as Integer
    return m.toUInt_k_().compareTo_UInt_k_(other)
end function

function UShort_compareTo_ULong_k_(other as Object) as Integer
    return m.toULong_k_().compareTo_ULong_k_(other)
end function

function UShort_plus_UByte_k_(other as Object) as Object
    return m.toUInt_k_().plus_UInt_k_(other.toUInt_k_())
end function

function UShort_plus_UShort_k_(other as Object) as Object
    return m.toUInt_k_().plus_UInt_k_(other.toUInt_k_())
end function

function UShort_plus_UInt_k_(other as Object) as Object
    return m.toUInt_k_().plus_UInt_k_(other)
end function

function UShort_plus_ULong_k_(other as Object) as Object
    return m.toULong_k_().plus_ULong_k_(other)
end function

function UShort_minus_UByte_k_(other as Object) as Object
    return m.toUInt_k_().minus_UInt_k_(other.toUInt_k_())
end function

function UShort_minus_UShort_k_(other as Object) as Object
    return m.toUInt_k_().minus_UInt_k_(other.toUInt_k_())
end function

function UShort_minus_UInt_k_(other as Object) as Object
    return m.toUInt_k_().minus_UInt_k_(other)
end function

function UShort_minus_ULong_k_(other as Object) as Object
    return m.toULong_k_().minus_ULong_k_(other)
end function

function UShort_times_UByte_k_(other as Object) as Object
    return m.toUInt_k_().times_UInt_k_(other.toUInt_k_())
end function

function UShort_times_UShort_k_(other as Object) as Object
    return m.toUInt_k_().times_UInt_k_(other.toUInt_k_())
end function

function UShort_times_UInt_k_(other as Object) as Object
    return m.toUInt_k_().times_UInt_k_(other)
end function

function UShort_times_ULong_k_(other as Object) as Object
    return m.toULong_k_().times_ULong_k_(other)
end function

function UShort_div_UByte_k_(other as Object) as Object
    return m.toUInt_k_().div_UInt_k_(other.toUInt_k_())
end function

function UShort_div_UShort_k_(other as Object) as Object
    return m.toUInt_k_().div_UInt_k_(other.toUInt_k_())
end function

function UShort_div_UInt_k_(other as Object) as Object
    return m.toUInt_k_().div_UInt_k_(other)
end function

function UShort_div_ULong_k_(other as Object) as Object
    return m.toULong_k_().div_ULong_k_(other)
end function

function UShort_rem_UByte_k_(other as Object) as Object
    return m.toUInt_k_().rem_UInt_k_(other.toUInt_k_())
end function

function UShort_rem_UShort_k_(other as Object) as Object
    return m.toUInt_k_().rem_UInt_k_(other.toUInt_k_())
end function

function UShort_rem_UInt_k_(other as Object) as Object
    return m.toUInt_k_().rem_UInt_k_(other)
end function

function UShort_rem_ULong_k_(other as Object) as Object
    return m.toULong_k_().rem_ULong_k_(other)
end function

function UShort_floorDiv_UByte_k_(other as Object) as Object
    return m.toUInt_k_().floorDiv_UInt_k_(other.toUInt_k_())
end function

function UShort_floorDiv_UShort_k_(other as Object) as Object
    return m.toUInt_k_().floorDiv_UInt_k_(other.toUInt_k_())
end function

function UShort_floorDiv_UInt_k_(other as Object) as Object
    return m.toUInt_k_().floorDiv_UInt_k_(other)
end function

function UShort_floorDiv_ULong_k_(other as Object) as Object
    return m.toULong_k_().floorDiv_ULong_k_(other)
end function

function UShort_mod_UByte_k_(other as Object) as Object
    return m.toUInt_k_().mod_UInt_k_(other.toUInt_k_()).toUByte_k_()
end function

function UShort_mod_UShort_k_(other as Object) as Object
    return m.toUInt_k_().mod_UInt_k_(other.toUInt_k_()).toUShort_k_()
end function

function UShort_mod_UInt_k_(other as Object) as Object
    return m.toUInt_k_().mod_UInt_k_(other)
end function

function UShort_mod_ULong_k_(other as Object) as Object
    return m.toULong_k_().mod_ULong_k_(other)
end function

function UShort_inc_k_() as Object
    return UShort_create_S_k_(m.get_data() + 1)
end function

function UShort_dec_k_() as Object
    return UShort_create_S_k_(m.get_data() - 1)
end function

function UShort_rangeTo_UShort_k_(other as Object) as Object
    return UIntRange_create_UInt_UInt_k_(m.toUInt_k_(), other.toUInt_k_())
end function

function UShort_rangeUntil_UShort_k_(other as Object) as Object
    return until_rUInt_UInt_k_(m.toUInt_k_(), other.toUInt_k_())
end function

function UShort_and_UShort_k_(other as Object) as Object
    return UShort_create_S_k_(m and other)
end function

function UShort_or_UShort_k_(other as Object) as Object
    return UShort_create_S_k_(m or other)
end function

function UShort_xor_UShort_k_(other as Object) as Object
    return UShort_create_S_k_((m or other) and not (m and other))
end function

function UShort_inv_k_() as Object
    return UShort_create_S_k_(not m)
end function

function UShort_toByte_k_() as Integer
    return m.get_data()
end function

function UShort_toShort_k_() as Integer
    return m.get_data()
end function

function UShort_toInt_k_() as Integer
    return m.get_data() and 65535
end function

function UShort_toLong_k_() as LongInteger
    return m.get_data() and 65535&
end function

function UShort_toUByte_k_() as Object
    return UByte_create_B_k_(m.get_data())
end function

function UShort_toUShort_k_() as Object
    return m
end function

function UShort_toUInt_k_() as Object
    return UInt_create_I_k_(m.get_data() and 65535)
end function

function UShort_toULong_k_() as Object
    return ULong_create_J_k_(m.get_data() and 65535&)
end function

function UShort_toFloat_k_() as Float
    return m
end function

function UShort_toDouble_k_() as Double
    return m
end function

function UShort_toString_k_() as String
    return __kotlin_numToStr_I_k_(m)
end function

function UShort_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "UShort") and (m.get_data() = other.get_data())
end function

function UShort_hashCode_k_() as Integer
    return m.get_data()
end function

function UShort_get_data_k_() as Integer
    return m.data
end function

function UShort_Companion_create_k_() as Object
    this = {}
    this.__type = "UShort_Companion"
    this.__proto = ["UShort_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.get_MIN_VALUE = UShort_Companion_get_MIN_VALUE_k_
    this.get_MAX_VALUE = UShort_Companion_get_MAX_VALUE_k_
    this.get_SIZE_BYTES = UShort_Companion_get_SIZE_BYTES_k_
    this.get_SIZE_BITS = UShort_Companion_get_SIZE_BITS_k_
    this.MIN_VALUE = UShort_create_S_k_(0)
    this.MAX_VALUE = UShort_create_S_k_(-1)
    this.SIZE_BYTES = 2
    this.SIZE_BITS = 16
    return this
end function

function UShort_Companion_getInstance() as Object
    if GetGlobalAA().UShort_Companion_instance = invalid then
        GetGlobalAA().UShort_Companion_instance = UShort_Companion_create_k_()
    end if
    return GetGlobalAA().UShort_Companion_instance
end function

function UShort_Companion_get_MIN_VALUE_k_() as Object
    return m.MIN_VALUE
end function

function UShort_Companion_get_MAX_VALUE_k_() as Object
    return m.MAX_VALUE
end function

function UShort_Companion_get_SIZE_BYTES_k_() as Integer
    return m.SIZE_BYTES
end function

function UShort_Companion_get_SIZE_BITS_k_() as Integer
    return m.SIZE_BITS
end function

function toUShort_rB_k_(m as Integer) as Object
    return UShort_create_S_k_(m)
end function

function toUShort_rS_k_(m as Integer) as Object
    return UShort_create_S_k_(m)
end function

function toUShort_rI_k_(m as Integer) as Object
    return UShort_create_S_k_(m)
end function

function toUShort_rJ_k_(m as LongInteger) as Object
    return UShort_create_S_k_(m)
end function
