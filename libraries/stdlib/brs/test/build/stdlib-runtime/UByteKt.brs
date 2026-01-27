function UByte_create_B_k_(data as Integer) as Object
    this = {}
    this.__type = "UByte"
    this.__proto = ["UByte", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.compareTo_AnyN_k_ = UByte_compareTo_AnyN_k_
    this.compareTo_UShort_k_ = UByte_compareTo_UShort_k_
    this.compareTo_UInt_k_ = UByte_compareTo_UInt_k_
    this.compareTo_ULong_k_ = UByte_compareTo_ULong_k_
    this.plus_UByte_k_ = UByte_plus_UByte_k_
    this.plus_UShort_k_ = UByte_plus_UShort_k_
    this.plus_UInt_k_ = UByte_plus_UInt_k_
    this.plus_ULong_k_ = UByte_plus_ULong_k_
    this.minus_UByte_k_ = UByte_minus_UByte_k_
    this.minus_UShort_k_ = UByte_minus_UShort_k_
    this.minus_UInt_k_ = UByte_minus_UInt_k_
    this.minus_ULong_k_ = UByte_minus_ULong_k_
    this.times_UByte_k_ = UByte_times_UByte_k_
    this.times_UShort_k_ = UByte_times_UShort_k_
    this.times_UInt_k_ = UByte_times_UInt_k_
    this.times_ULong_k_ = UByte_times_ULong_k_
    this.div_UByte_k_ = UByte_div_UByte_k_
    this.div_UShort_k_ = UByte_div_UShort_k_
    this.div_UInt_k_ = UByte_div_UInt_k_
    this.div_ULong_k_ = UByte_div_ULong_k_
    this.rem_UByte_k_ = UByte_rem_UByte_k_
    this.rem_UShort_k_ = UByte_rem_UShort_k_
    this.rem_UInt_k_ = UByte_rem_UInt_k_
    this.rem_ULong_k_ = UByte_rem_ULong_k_
    this.floorDiv_UByte_k_ = UByte_floorDiv_UByte_k_
    this.floorDiv_UShort_k_ = UByte_floorDiv_UShort_k_
    this.floorDiv_UInt_k_ = UByte_floorDiv_UInt_k_
    this.floorDiv_ULong_k_ = UByte_floorDiv_ULong_k_
    this.mod_UByte_k_ = UByte_mod_UByte_k_
    this.mod_UShort_k_ = UByte_mod_UShort_k_
    this.mod_UInt_k_ = UByte_mod_UInt_k_
    this.mod_ULong_k_ = UByte_mod_ULong_k_
    this.inc_k_ = UByte_inc_k_
    this.dec_k_ = UByte_dec_k_
    this.rangeTo_UByte_k_ = UByte_rangeTo_UByte_k_
    this.rangeUntil_UByte_k_ = UByte_rangeUntil_UByte_k_
    this.and_UByte_k_ = UByte_and_UByte_k_
    this.or_UByte_k_ = UByte_or_UByte_k_
    this.xor_UByte_k_ = UByte_xor_UByte_k_
    this.inv_k_ = UByte_inv_k_
    this.toByte_k_ = UByte_toByte_k_
    this.toShort_k_ = UByte_toShort_k_
    this.toInt_k_ = UByte_toInt_k_
    this.toLong_k_ = UByte_toLong_k_
    this.toUByte_k_ = UByte_toUByte_k_
    this.toUShort_k_ = UByte_toUShort_k_
    this.toUInt_k_ = UByte_toUInt_k_
    this.toULong_k_ = UByte_toULong_k_
    this.toFloat_k_ = UByte_toFloat_k_
    this.toDouble_k_ = UByte_toDouble_k_
    this.toString_k_ = UByte_toString_k_
    this.toString = UByte_toString_k_
    this.equals_AnyN_k_ = UByte_equals_AnyN_k_
    this.equals = UByte_equals_AnyN_k_
    this.hashCode_k_ = UByte_hashCode_k_
    this.hashCode = UByte_hashCode_k_
    this.__get_data = UByte___get_data_k_
    this.data = data
    return this
end function

function UByte_compareTo_AnyN_k_(other as Object) as Integer
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = (this.__get_data() and 255)
        exit while
    end while
    tmp0_1 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_1
        tmp_ret_1 = (this.__get_data() and 255)
        exit while
    end while
    return __kotlin_intCompare(tmp_ret_0, tmp_ret_1)

end function

function UByte_compareTo_UShort_k_(other as Object) as Integer
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = (this.__get_data() and 255)
        exit while
    end while
    tmp0_1 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_1
        tmp_ret_1 = (this.__get_data() and 65535)
        exit while
    end while
    return __kotlin_intCompare(tmp_ret_0, tmp_ret_1)

end function

function UByte_compareTo_UInt_k_(other as Object) as Integer
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    return tmp_ret_0.compareTo_AnyN_k_(other)

end function

function UByte_compareTo_ULong_k_(other as Object) as Integer
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = ULong_create_J_k_(this.__get_data() and 255&)
        exit while
    end while
    return tmp_ret_0.compareTo_AnyN_k_(other)

end function

function UByte_plus_UByte_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp0_2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp2 = tmp_ret_1

    tmp_ret_2 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_2 = UInt_create_I_k_(this.__get_data() + other.__get_data())
        exit while
    end while
    return tmp_ret_2

end function

function UByte_plus_UShort_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp0_2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    tmp2 = tmp_ret_1

    tmp_ret_2 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_2 = UInt_create_I_k_(this.__get_data() + other.__get_data())
        exit while
    end while
    return tmp_ret_2

end function

function UByte_plus_UInt_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() + other.__get_data())
        exit while
    end while
    return tmp_ret_1

end function

function UByte_plus_ULong_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = ULong_create_J_k_(this.__get_data() and 255&)
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

function UByte_minus_UByte_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp0_2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp2 = tmp_ret_1

    tmp_ret_2 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_2 = UInt_create_I_k_(this.__get_data() - other.__get_data())
        exit while
    end while
    return tmp_ret_2

end function

function UByte_minus_UShort_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp0_2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    tmp2 = tmp_ret_1

    tmp_ret_2 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_2 = UInt_create_I_k_(this.__get_data() - other.__get_data())
        exit while
    end while
    return tmp_ret_2

end function

function UByte_minus_UInt_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() - other.__get_data())
        exit while
    end while
    return tmp_ret_1

end function

function UByte_minus_ULong_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = ULong_create_J_k_(this.__get_data() and 255&)
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

function UByte_times_UByte_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp0_2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp2 = tmp_ret_1

    tmp_ret_2 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_2 = UInt_create_I_k_(this.__get_data() * other.__get_data())
        exit while
    end while
    return tmp_ret_2

end function

function UByte_times_UShort_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp0_2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    tmp2 = tmp_ret_1

    tmp_ret_2 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_2 = UInt_create_I_k_(this.__get_data() * other.__get_data())
        exit while
    end while
    return tmp_ret_2

end function

function UByte_times_UInt_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() * other.__get_data())
        exit while
    end while
    return tmp_ret_1

end function

function UByte_times_ULong_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = ULong_create_J_k_(this.__get_data() and 255&)
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

function UByte_div_UByte_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0_1 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_1
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    return tmp_ret_0.div_UInt_k_(tmp_ret_1)

end function

function UByte_div_UShort_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0_1 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_1
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    return tmp_ret_0.div_UInt_k_(tmp_ret_1)

end function

function UByte_div_UInt_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    return tmp_ret_0.div_UInt_k_(other)

end function

function UByte_div_ULong_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = ULong_create_J_k_(this.__get_data() and 255&)
        exit while
    end while
    return tmp_ret_0.div_ULong_k_(other)

end function

function UByte_rem_UByte_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0_1 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_1
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    return tmp_ret_0.rem_UInt_k_(tmp_ret_1)

end function

function UByte_rem_UShort_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0_1 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_1
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    return tmp_ret_0.rem_UInt_k_(tmp_ret_1)

end function

function UByte_rem_UInt_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    return tmp_ret_0.rem_UInt_k_(other)

end function

function UByte_rem_ULong_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = ULong_create_J_k_(this.__get_data() and 255&)
        exit while
    end while
    return tmp_ret_0.rem_ULong_k_(other)

end function

function UByte_floorDiv_UByte_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp0_2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp2 = tmp_ret_1

    tmp_ret_2 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_2 = this.div_UInt_k_(other)
        exit while
    end while
    return tmp_ret_2

end function

function UByte_floorDiv_UShort_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp0_2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_2
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    tmp2 = tmp_ret_1

    tmp_ret_2 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_2 = this.div_UInt_k_(other)
        exit while
    end while
    return tmp_ret_2

end function

function UByte_floorDiv_UInt_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = this.div_UInt_k_(other)
        exit while
    end while
    return tmp_ret_1

end function

function UByte_floorDiv_ULong_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = ULong_create_J_k_(this.__get_data() and 255&)
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

function UByte_mod_UByte_k_(other as Object) as Object
    tmp0_2 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_2
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0_1 = tmp_ret_0

    tmp0_3 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_3
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp2 = tmp_ret_1

    tmp_ret_2 = invalid

    while true
        this = tmp0_1
        other = tmp2
        tmp_ret_2 = this.rem_UInt_k_(other)
        exit while
    end while
    tmp0 = tmp_ret_2

    tmp_ret_3 = invalid

    while true
        this = tmp0
        tmp_ret_3 = UByte_create_B_k_(this.__get_data())
        exit while
    end while
    return tmp_ret_3

end function

function UByte_mod_UShort_k_(other as Object) as Object
    tmp0_2 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_2
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0_1 = tmp_ret_0

    tmp0_3 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_3
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 65535)
        exit while
    end while
    tmp2 = tmp_ret_1

    tmp_ret_2 = invalid

    while true
        this = tmp0_1
        other = tmp2
        tmp_ret_2 = this.rem_UInt_k_(other)
        exit while
    end while
    tmp0 = tmp_ret_2

    tmp_ret_3 = invalid

    while true
        this = tmp0
        tmp_ret_3 = UShort_create_S_k_(this.__get_data())
        exit while
    end while
    return tmp_ret_3

end function

function UByte_mod_UInt_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0 = tmp_ret_0

    tmp2 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_1 = this.rem_UInt_k_(other)
        exit while
    end while
    return tmp_ret_1

end function

function UByte_mod_ULong_k_(other as Object) as Object
    tmp0_1 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0_1
        tmp_ret_0 = ULong_create_J_k_(this.__get_data() and 255&)
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

function UByte_inc_k_() as Object
    return UByte_create_B_k_(m.__get_data() + 1)
end function

function UByte_dec_k_() as Object
    return UByte_create_B_k_(m.__get_data() - 1)
end function

function UByte_rangeTo_UByte_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0_1 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_1
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    return UIntRange_create_UInt_UInt_k_(tmp_ret_0, tmp_ret_1)

end function

function UByte_rangeUntil_UByte_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    tmp0_1 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_1
        tmp_ret_1 = UInt_create_I_k_(this.__get_data() and 255)
        exit while
    end while
    return until_rUInt_UInt_k_(tmp_ret_0, tmp_ret_1)

end function

function UByte_and_UByte_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = (this.__get_data() and 255)
        exit while
    end while
    tmp0_1 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_1
        tmp_ret_1 = (this.__get_data() and 255)
        exit while
    end while
    return UByte_create_B_k_(tmp_ret_0 and tmp_ret_1)

end function

function UByte_or_UByte_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = (this.__get_data() and 255)
        exit while
    end while
    tmp0_1 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_1
        tmp_ret_1 = (this.__get_data() and 255)
        exit while
    end while
    return UByte_create_B_k_(tmp_ret_0 or tmp_ret_1)

end function

function UByte_xor_UByte_k_(other as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = (this.__get_data() and 255)
        exit while
    end while
    tmp0_1 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_1
        tmp_ret_1 = (this.__get_data() and 255)
        exit while
    end while
    return UByte_create_B_k_((tmp_ret_0 or tmp_ret_1) and not (tmp_ret_0 and tmp_ret_1))

end function

function UByte_inv_k_() as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = (this.__get_data() and 255)
        exit while
    end while
    return UByte_create_B_k_(not tmp_ret_0)

end function

function UByte_toByte_k_() as Integer
    return m.__get_data()
end function

function UByte_toShort_k_() as Integer
    return m.__get_data() and 255
end function

function UByte_toInt_k_() as Integer
    return m.__get_data() and 255
end function

function UByte_toLong_k_() as LongInteger
    return m.__get_data() and 255&
end function

function UByte_toUByte_k_() as Object
    return m
end function

function UByte_toUShort_k_() as Object
    return UShort_create_S_k_(m.__get_data() and 255)
end function

function UByte_toUInt_k_() as Object
    return UInt_create_I_k_(m.__get_data() and 255)
end function

function UByte_toULong_k_() as Object
    return ULong_create_J_k_(m.__get_data() and 255&)
end function

function UByte_toFloat_k_() as Float
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = (this.__get_data() and 255)
        exit while
    end while
    return tmp_ret_0

end function

function UByte_toDouble_k_() as Double
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = (this.__get_data() and 255)
        exit while
    end while
    return tmp_ret_0

end function

function UByte_toString_k_() as String
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = (this.__get_data() and 255)
        exit while
    end while
    return __kotlin_numToStr_I_k_(tmp_ret_0)

end function

function UByte_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "UByte") and (m.__get_data() = other.__get_data())
end function

function UByte_hashCode_k_() as Integer
    return m.__get_data()
end function

function UByte___get_data_k_() as Integer
    return m.data
end function

function UByte_Companion_create_k_() as Object
    this = {}
    this.__type = "UByte_Companion"
    this.__proto = ["UByte_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_MIN_VALUE = UByte_Companion___get_MIN_VALUE_k_
    this.__get_MAX_VALUE = UByte_Companion___get_MAX_VALUE_k_
    this.__get_SIZE_BYTES = UByte_Companion___get_SIZE_BYTES_k_
    this.__get_SIZE_BITS = UByte_Companion___get_SIZE_BITS_k_
    this.MIN_VALUE = UByte_create_B_k_(0)
    this.MAX_VALUE = UByte_create_B_k_(-1)
    this.SIZE_BYTES = 1
    this.SIZE_BITS = 8
    return this
end function

function UByte_Companion_getInstance() as Object
    if GetGlobalAA().UByte_Companion_instance = invalid then
        GetGlobalAA().UByte_Companion_instance = UByte_Companion_create_k_()
    end if
    return GetGlobalAA().UByte_Companion_instance
end function

function UByte_Companion___get_MIN_VALUE_k_() as Object
    return m.MIN_VALUE
end function

function UByte_Companion___get_MAX_VALUE_k_() as Object
    return m.MAX_VALUE
end function

function UByte_Companion___get_SIZE_BYTES_k_() as Integer
    return m.SIZE_BYTES
end function

function UByte_Companion___get_SIZE_BITS_k_() as Integer
    return m.SIZE_BITS
end function

function toUByte_rB_k_(m as Integer) as Object
    return UByte_create_B_k_(m)
end function

function toUByte_rS_k_(m as Integer) as Object
    return UByte_create_B_k_(m)
end function

function toUByte_rI_k_(m as Integer) as Object
    return UByte_create_B_k_(m)
end function

function toUByte_rJ_k_(m as LongInteger) as Object
    return UByte_create_B_k_(m)
end function
