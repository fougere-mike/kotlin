function UInt_create_I_k_(data as Integer) as Object
    this = {}
    this.__type = "UInt"
    this.__proto = ["UInt", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.compareTo_UByte_k_ = UInt_compareTo_UByte_k_
    this.compareTo_UShort_k_ = UInt_compareTo_UShort_k_
    this.compareTo_AnyN_k_ = UInt_compareTo_AnyN_k_
    this.compareTo_ULong_k_ = UInt_compareTo_ULong_k_
    this.plus_UByte_k_ = UInt_plus_UByte_k_
    this.plus_UShort_k_ = UInt_plus_UShort_k_
    this.plus_UInt_k_ = UInt_plus_UInt_k_
    this.plus_ULong_k_ = UInt_plus_ULong_k_
    this.minus_UByte_k_ = UInt_minus_UByte_k_
    this.minus_UShort_k_ = UInt_minus_UShort_k_
    this.minus_UInt_k_ = UInt_minus_UInt_k_
    this.minus_ULong_k_ = UInt_minus_ULong_k_
    this.times_UByte_k_ = UInt_times_UByte_k_
    this.times_UShort_k_ = UInt_times_UShort_k_
    this.times_UInt_k_ = UInt_times_UInt_k_
    this.times_ULong_k_ = UInt_times_ULong_k_
    this.div_UByte_k_ = UInt_div_UByte_k_
    this.div_UShort_k_ = UInt_div_UShort_k_
    this.div_UInt_k_ = UInt_div_UInt_k_
    this.div_ULong_k_ = UInt_div_ULong_k_
    this.rem_UByte_k_ = UInt_rem_UByte_k_
    this.rem_UShort_k_ = UInt_rem_UShort_k_
    this.rem_UInt_k_ = UInt_rem_UInt_k_
    this.rem_ULong_k_ = UInt_rem_ULong_k_
    this.floorDiv_UByte_k_ = UInt_floorDiv_UByte_k_
    this.floorDiv_UShort_k_ = UInt_floorDiv_UShort_k_
    this.floorDiv_UInt_k_ = UInt_floorDiv_UInt_k_
    this.floorDiv_ULong_k_ = UInt_floorDiv_ULong_k_
    this.mod_UByte_k_ = UInt_mod_UByte_k_
    this.mod_UShort_k_ = UInt_mod_UShort_k_
    this.mod_UInt_k_ = UInt_mod_UInt_k_
    this.mod_ULong_k_ = UInt_mod_ULong_k_
    this.inc_k_ = UInt_inc_k_
    this.dec_k_ = UInt_dec_k_
    this.rangeTo_UInt_k_ = UInt_rangeTo_UInt_k_
    this.rangeUntil_UInt_k_ = UInt_rangeUntil_UInt_k_
    this.shl_I_k_ = UInt_shl_I_k_
    this.shr_I_k_ = UInt_shr_I_k_
    this.and_UInt_k_ = UInt_and_UInt_k_
    this.or_UInt_k_ = UInt_or_UInt_k_
    this.xor_UInt_k_ = UInt_xor_UInt_k_
    this.inv_k_ = UInt_inv_k_
    this.toByte_k_ = UInt_toByte_k_
    this.toShort_k_ = UInt_toShort_k_
    this.toInt_k_ = UInt_toInt_k_
    this.toLong_k_ = UInt_toLong_k_
    this.toUByte_k_ = UInt_toUByte_k_
    this.toUShort_k_ = UInt_toUShort_k_
    this.toUInt_k_ = UInt_toUInt_k_
    this.toULong_k_ = UInt_toULong_k_
    this.toFloat_k_ = UInt_toFloat_k_
    this.toDouble_k_ = UInt_toDouble_k_
    this.toString_k_ = UInt_toString_k_
    this.toString = UInt_toString_k_
    this.equals_AnyN_k_ = UInt_equals_AnyN_k_
    this.equals = UInt_equals_AnyN_k_
    this.hashCode_k_ = UInt_hashCode_k_
    this.hashCode = UInt_hashCode_k_
    this.__get_data = UInt___get_data_k_
    this.data = data
    return this
end function

function UInt_compareTo_UByte_k_(other as Object) as Integer
    tmp0 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    return m.compareTo_AnyN_k_(tmp_ret_0)

end function

function UInt_compareTo_UShort_k_(other as Object) as Integer
    tmp0 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    return m.compareTo_AnyN_k_(tmp_ret_0)

end function

function UInt_compareTo_AnyN_k_(other as Object) as Integer
    return uintCompare_I_I_k_(m.__get_data(), other.__get_data())
end function

function UInt_compareTo_ULong_k_(other as Object) as Integer
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = uintToULong_I_k_(this.__get_data())
        exit while
    end while
    return tmp_ret_0.compareTo_AnyN_k_(other)

end function

function UInt_plus_UByte_k_(other as Object) as Object
    tmp0 = m
    tmp0_1 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() + other.__get_data())
        exit while
    end while
    return tmp_ret_1

end function

function UInt_plus_UShort_k_(other as Object) as Object
    tmp0 = m
    tmp0_1 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() + other.__get_data())
        exit while
    end while
    return tmp_ret_1

end function

function UInt_plus_UInt_k_(other as Object) as Object
    return UInt_create_I_k_(m.__get_data() + other.__get_data())
end function

function UInt_plus_ULong_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = uintToULong_I_k_(this.__get_data())
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = ULong_create_J_k_(this.__get_data() + other.__get_data())
        exit while
    end while
    return tmp_ret_1

end function

function UInt_minus_UByte_k_(other as Object) as Object
    tmp0 = m
    tmp0_1 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() - other.__get_data())
        exit while
    end while
    return tmp_ret_1

end function

function UInt_minus_UShort_k_(other as Object) as Object
    tmp0 = m
    tmp0_1 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() - other.__get_data())
        exit while
    end while
    return tmp_ret_1

end function

function UInt_minus_UInt_k_(other as Object) as Object
    return UInt_create_I_k_(m.__get_data() - other.__get_data())
end function

function UInt_minus_ULong_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = uintToULong_I_k_(this.__get_data())
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = ULong_create_J_k_(this.__get_data() - other.__get_data())
        exit while
    end while
    return tmp_ret_1

end function

function UInt_times_UByte_k_(other as Object) as Object
    tmp0 = m
    tmp0_1 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() * other.__get_data())
        exit while
    end while
    return tmp_ret_1

end function

function UInt_times_UShort_k_(other as Object) as Object
    tmp0 = m
    tmp0_1 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() * other.__get_data())
        exit while
    end while
    return tmp_ret_1

end function

function UInt_times_UInt_k_(other as Object) as Object
    return UInt_create_I_k_(m.__get_data() * other.__get_data())
end function

function UInt_times_ULong_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = uintToULong_I_k_(this.__get_data())
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = ULong_create_J_k_(this.__get_data() * other.__get_data())
        exit while
    end while
    return tmp_ret_1

end function

function UInt_div_UByte_k_(other as Object) as Object
    tmp0 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    return m.div_UInt_k_(tmp_ret_0)

end function

function UInt_div_UShort_k_(other as Object) as Object
    tmp0 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    return m.div_UInt_k_(tmp_ret_0)

end function

function UInt_div_UInt_k_(other as Object) as Object
    return uintDivide_I_I_k_(m.__get_data(), other.__get_data())
end function

function UInt_div_ULong_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = uintToULong_I_k_(this.__get_data())
        exit while
    end while
    return tmp_ret_0.div_ULong_k_(other)

end function

function UInt_rem_UByte_k_(other as Object) as Object
    tmp0 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    return m.rem_UInt_k_(tmp_ret_0)

end function

function UInt_rem_UShort_k_(other as Object) as Object
    tmp0 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    return m.rem_UInt_k_(tmp_ret_0)

end function

function UInt_rem_UInt_k_(other as Object) as Object
    return uintRemainder_I_I_k_(m.__get_data(), other.__get_data())
end function

function UInt_rem_ULong_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = uintToULong_I_k_(this.__get_data())
        exit while
    end while
    return tmp_ret_0.rem_ULong_k_(other)

end function

function UInt_floorDiv_UByte_k_(other as Object) as Object
    tmp0 = m
    tmp0_1 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = this.div_UInt_k_(other)
        exit while
    end while
    return tmp_ret_1

end function

function UInt_floorDiv_UShort_k_(other as Object) as Object
    tmp0 = m
    tmp0_1 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = this.div_UInt_k_(other)
        exit while
    end while
    return tmp_ret_1

end function

function UInt_floorDiv_UInt_k_(other as Object) as Object
    return m.div_UInt_k_(other)
end function

function UInt_floorDiv_ULong_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = uintToULong_I_k_(this.__get_data())
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = this.div_ULong_k_(other)
        exit while
    end while
    return tmp_ret_1

end function

function UInt_mod_UByte_k_(other as Object) as Object
    tmp0_1 = m
    tmp0_2 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0_2
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        this = tmp0_1
        other = tmp2
        tmp_ret_1 = this.rem_UInt_k_(other)
        exit while
    end while
    tmp0 = tmp_ret_1

    tmp_ret_2 = invalid

    while true
        this = tmp0
        tmp_ret_2 = UByte_create_B_k_(this.__get_data())
        exit while
    end while
    return tmp_ret_2

end function

function UInt_mod_UShort_k_(other as Object) as Object
    tmp0_1 = m
    tmp0_2 = other
    tmp_ret_0 = invalid

    while true
        this = tmp0_2
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        this = tmp0_1
        other = tmp2
        tmp_ret_1 = this.rem_UInt_k_(other)
        exit while
    end while
    tmp0 = tmp_ret_1

    tmp_ret_2 = invalid

    while true
        this = tmp0
        tmp_ret_2 = UShort_create_S_k_(this.__get_data())
        exit while
    end while
    return tmp_ret_2

end function

function UInt_mod_UInt_k_(other as Object) as Object
    return m.rem_UInt_k_(other)
end function

function UInt_mod_ULong_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = uintToULong_I_k_(this.__get_data())
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = this.rem_ULong_k_(other)
        exit while
    end while
    return tmp_ret_1

end function

function UInt_inc_k_() as Object
    return UInt_create_I_k_(m.__get_data() + 1)
end function

function UInt_dec_k_() as Object
    return UInt_create_I_k_(m.__get_data() - 1)
end function

function UInt_rangeTo_UInt_k_(other as Object) as Object
    return UIntRange_create_UInt_UInt_k_(m, other)
end function

function UInt_rangeUntil_UInt_k_(other as Object) as Object
    return until_rUInt_UInt_k_(m, other)
end function

function UInt_shl_I_k_(bitCount as Integer) as Object
    return UInt_create_I_k_(m.__get_data() * (2 ^ bitCount))
end function

function UInt_shr_I_k_(bitCount as Integer) as Object
    return UInt_create_I_k_(__kotlin_ushr(m.__get_data(), bitCount))
end function

function UInt_and_UInt_k_(other as Object) as Object
    return UInt_create_I_k_(m.__get_data() and other.__get_data())
end function

function UInt_or_UInt_k_(other as Object) as Object
    return UInt_create_I_k_(m.__get_data() or other.__get_data())
end function

function UInt_xor_UInt_k_(other as Object) as Object
    return UInt_create_I_k_((m.__get_data() or other.__get_data()) and not (m.__get_data() and other.__get_data()))
end function

function UInt_inv_k_() as Object
    return UInt_create_I_k_(not m.__get_data())
end function

function UInt_toByte_k_() as Integer
    return m.__get_data()
end function

function UInt_toShort_k_() as Integer
    return m.__get_data()
end function

function UInt_toInt_k_() as Integer
    return m.__get_data()
end function

function UInt_toLong_k_() as LongInteger
    return uintToLong_I_k_(m.__get_data())
end function

function UInt_toUByte_k_() as Object
    return UByte_create_B_k_(m.__get_data())
end function

function UInt_toUShort_k_() as Object
    return UShort_create_S_k_(m.__get_data())
end function

function UInt_toUInt_k_() as Object
    return m
end function

function UInt_toULong_k_() as Object
    return uintToULong_I_k_(m.__get_data())
end function

function UInt_toFloat_k_() as Float
    return uintToFloat_I_k_(m.__get_data())
end function

function UInt_toDouble_k_() as Double
    return uintToDouble_I_k_(m.__get_data())
end function

function UInt_toString_k_() as String
    return uintToString_I_k_(m.__get_data())
end function

function UInt_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "UInt") and (m.__get_data() = other.__get_data())
end function

function UInt_hashCode_k_() as Integer
    return m.__get_data()
end function

function UInt___get_data_k_() as Integer
    return m.data
end function

function UInt_Companion_create_k_() as Object
    this = {}
    this.__type = "UInt_Companion"
    this.__proto = ["UInt_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_MIN_VALUE = UInt_Companion___get_MIN_VALUE_k_
    this.__get_MAX_VALUE = UInt_Companion___get_MAX_VALUE_k_
    this.__get_SIZE_BYTES = UInt_Companion___get_SIZE_BYTES_k_
    this.__get_SIZE_BITS = UInt_Companion___get_SIZE_BITS_k_
    this.MIN_VALUE = UInt_create_I_k_(0)
    this.MAX_VALUE = UInt_create_I_k_(-1)
    this.SIZE_BYTES = 4
    this.SIZE_BITS = 32
    return this
end function

function UInt_Companion_getInstance() as Object
    if GetGlobalAA().UInt_Companion_instance = invalid then
        GetGlobalAA().UInt_Companion_instance = UInt_Companion_create_k_()
    end if
    return GetGlobalAA().UInt_Companion_instance
end function

function UInt_Companion___get_MIN_VALUE_k_() as Object
    return m.MIN_VALUE
end function

function UInt_Companion___get_MAX_VALUE_k_() as Object
    return m.MAX_VALUE
end function

function UInt_Companion___get_SIZE_BYTES_k_() as Integer
    return m.SIZE_BYTES
end function

function UInt_Companion___get_SIZE_BITS_k_() as Integer
    return m.SIZE_BITS
end function

function toUInt_rB_k_(m as Integer) as Object
    return UInt_create_I_k_(m)
end function

function toUInt_rS_k_(m as Integer) as Object
    return UInt_create_I_k_(m)
end function

function toUInt_rI_k_(m as Integer) as Object
    return UInt_create_I_k_(m)
end function

function toUInt_rJ_k_(m as LongInteger) as Object
    return UInt_create_I_k_(m)
end function

function toUInt_rF_k_(m as Float) as Object
    return floatToUInt_F_k_(m)
end function

function toUInt_rD_k_(m as Double) as Object
    return doubleToUInt_D_k_(m)
end function
