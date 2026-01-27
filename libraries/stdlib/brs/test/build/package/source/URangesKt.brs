function differenceModulo_UInt_UInt_UInt_k_(a as Object, b as Object, c as Object) as Object
    ac = a.rem_UInt_k_(c)
    bc = b.rem_UInt_k_(c)
    __when_tmp0 = invalid
    if ac.compareTo_AnyN_k_(bc) >= 0 then
        tmp0 = ac
        tmp2 = bc
        tmp_ret_0 = invalid
        while true
            this = tmp0
            other = tmp2
            tmp_ret_0 = UInt_create_I_k_(this.__get_data() - other.__get_data())
            exit while
        end while
        __when_tmp0 = tmp_ret_0
    else if true then
        tmp0 = ac
        tmp2 = bc
        tmp_ret_1 = invalid

        while true
            this = tmp0
            other = tmp2
            tmp_ret_1 = UInt_create_I_k_(this.__get_data() - other.__get_data())
            exit while
        end while
        tmp0 = tmp_ret_1
        tmp2 = c
        tmp_ret_2 = invalid
        while true
            this = tmp0
            other = tmp2
            tmp_ret_2 = UInt_create_I_k_(this.__get_data() + other.__get_data())
            exit while
        end while
        __when_tmp0 = tmp_ret_2
    end if
    return __when_tmp0

end function

function differenceModulo_ULong_ULong_ULong_k_(a as Object, b as Object, c as Object) as Object
    ac = a.rem_ULong_k_(c)
    bc = b.rem_ULong_k_(c)
    __when_tmp1 = invalid
    if ac.compareTo_AnyN_k_(bc) >= 0 then
        tmp0 = ac
        tmp2 = bc
        tmp_ret_0 = invalid
        while true
            this = tmp0
            other = tmp2
            tmp_ret_0 = ULong_create_J_k_(this.__get_data() - other.__get_data())
            exit while
        end while
        __when_tmp1 = tmp_ret_0
    else if true then
        tmp0 = ac
        tmp2 = bc
        tmp_ret_1 = invalid

        while true
            this = tmp0
            other = tmp2
            tmp_ret_1 = ULong_create_J_k_(this.__get_data() - other.__get_data())
            exit while
        end while
        tmp0 = tmp_ret_1
        tmp2 = c
        tmp_ret_2 = invalid
        while true
            this = tmp0
            other = tmp2
            tmp_ret_2 = ULong_create_J_k_(this.__get_data() + other.__get_data())
            exit while
        end while
        __when_tmp1 = tmp_ret_2
    end if
    return __when_tmp1

end function

function getProgressionLastElement_UInt_UInt_I_k_(start as Object, end_ as Object, step_ as Integer) as Object
    __when_tmp4 = invalid
    if step_ > 0 then
        __when_tmp2 = invalid
        if start.compareTo_AnyN_k_(end_) >= 0 then
            __when_tmp2 = end_
        else if true then
            tmp0 = end_
            tmp2 = differenceModulo_UInt_UInt_UInt_k_(end_, start, toUInt_rI_k_(step_))
            tmp_ret_0 = invalid
            while true
                this = tmp0
                other = tmp2
                tmp_ret_0 = UInt_create_I_k_(this.__get_data() - other.__get_data())
                exit while
            end while
            __when_tmp2 = tmp_ret_0
        end if
        __when_tmp4 = __when_tmp2
    else if step_ < 0 then
        __when_tmp3 = invalid
        if start.compareTo_AnyN_k_(end_) <= 0 then
            __when_tmp3 = end_
        else if true then
            tmp0 = end_
            tmp2 = differenceModulo_UInt_UInt_UInt_k_(start, end_, toUInt_rI_k_(-step_))
            tmp_ret_1 = invalid
            while true
                this = tmp0
                other = tmp2
                tmp_ret_1 = UInt_create_I_k_(this.__get_data() + other.__get_data())
                exit while
            end while
            __when_tmp3 = tmp_ret_1
        end if
        __when_tmp4 = __when_tmp3
    else if true then
        throw IllegalArgumentException_create_StrN_k_("Step is zero.")
    end if
    return __when_tmp4

end function

function getProgressionLastElement_ULong_ULong_J_k_(start as Object, end_ as Object, step_ as LongInteger) as Object
    __when_tmp7 = invalid
    if step_ > 0 then
        __when_tmp5 = invalid
        if start.compareTo_AnyN_k_(end_) >= 0 then
            __when_tmp5 = end_
        else if true then
            tmp0 = end_
            tmp2 = differenceModulo_ULong_ULong_ULong_k_(end_, start, toULong_rJ_k_(step_))
            tmp_ret_0 = invalid
            while true
                this = tmp0
                other = tmp2
                tmp_ret_0 = ULong_create_J_k_(this.__get_data() - other.__get_data())
                exit while
            end while
            __when_tmp5 = tmp_ret_0
        end if
        __when_tmp7 = __when_tmp5
    else if step_ < 0 then
        __when_tmp6 = invalid
        if start.compareTo_AnyN_k_(end_) <= 0 then
            __when_tmp6 = end_
        else if true then
            tmp0 = end_
            tmp2 = differenceModulo_ULong_ULong_ULong_k_(start, end_, toULong_rJ_k_(-step_))
            tmp_ret_1 = invalid
            while true
                this = tmp0
                other = tmp2
                tmp_ret_1 = ULong_create_J_k_(this.__get_data() + other.__get_data())
                exit while
            end while
            __when_tmp6 = tmp_ret_1
        end if
        __when_tmp7 = __when_tmp6
    else if true then
        throw IllegalArgumentException_create_StrN_k_("Step is zero.")
    end if
    return __when_tmp7

end function

function UIntRange_create_UInt_UInt_k_(start as Object, endInclusive as Object) as Object
    this = UIntProgression_create_UInt_UInt_I_k_(start, endInclusive, 1)
    this._super = {}
    this._super.contains_Any_k_ = this.contains_Any_k_
    this._super.isEmpty_k_ = this.isEmpty_k_
    this._super.equals_AnyN_k_ = this.equals_AnyN_k_
    this._super.hashCode_k_ = this.hashCode_k_
    this._super.toString_k_ = this.toString_k_
    this.__proto = ["UIntRange", "ClosedRange", "OpenEndRange", this.__proto]
    this.__type = "UIntRange"
    this.contains_Any_k_ = UIntRange_contains_Any_k_
    this.isEmpty_k_ = UIntRange_isEmpty_k_
    this.equals_AnyN_k_ = UIntRange_equals_AnyN_k_
    this.equals = UIntRange_equals_AnyN_k_
    this.hashCode_k_ = UIntRange_hashCode_k_
    this.hashCode = UIntRange_hashCode_k_
    this.toString_k_ = UIntRange_toString_k_
    this.toString = UIntRange_toString_k_
    this.__get_start = UIntRange___get_start_k_
    this.__get_endInclusive = UIntRange___get_endInclusive_k_
    this.__get_endExclusive = UIntRange___get_endExclusive_k_
    return this
end function

function UIntRange_contains_Any_k_(value as Object) as Boolean
    return (m.__get_first().compareTo_AnyN_k_(value) <= 0) and (value.compareTo_AnyN_k_(m.__get_last()) <= 0)
end function

function UIntRange_isEmpty_k_() as Boolean
    return m.__get_first().compareTo_AnyN_k_(m.__get_last()) > 0
end function

function UIntRange_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "UIntRange") and ((m.isEmpty_k_() and other.isEmpty_k_()) or (brsStructuralEquals_AnyN_AnyN_k_(m.__get_first(), other.__get_first()) and brsStructuralEquals_AnyN_AnyN_k_(m.__get_last(), other.__get_last())))
end function

function UIntRange_hashCode_k_() as Integer
    __when_tmp8 = invalid
    if m.isEmpty_k_() then
        __when_tmp8 = -1
    else if true then
        tmp0 = m.__get_first()
        tmp_ret_0 = invalid

        while true
            this = tmp0
            tmp_ret_0 = this.__get_data()
            exit while
        end while
        tmp0_1 = m.__get_last()
        tmp_ret_1 = invalid

        while true
            this = tmp0_1
            tmp_ret_1 = this.__get_data()
            exit while
        end while
        __when_tmp8 = ((31 * tmp_ret_0) + tmp_ret_1)
    end if
    return __when_tmp8

end function

function UIntRange_toString_k_() as String
    return (m.__get_first().toString() + "..") + m.__get_last().toString()
end function

function UIntRange___get_start_k_() as Object
    return m.__get_first()
end function

function UIntRange___get_endInclusive_k_() as Object
    return m.__get_last()
end function

function UIntRange___get_endExclusive_k_() as Object
    if brsStructuralEquals_AnyN_AnyN_k_(m.__get_last(), UInt_Companion_getInstance().__get_MAX_VALUE()) then
        error_Any_k_("Cannot return the exclusive upper bound of a range that includes MAX_VALUE.")
    end if
    return m.__get_last().plus_UInt_k_(UInt_create_I_k_(1))
end function

function UIntRange_Companion_create_k_() as Object
    this = {}
    this.__type = "UIntRange_Companion"
    this.__proto = ["UIntRange_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_EMPTY = UIntRange_Companion___get_EMPTY_k_
    this.EMPTY = UIntRange_create_UInt_UInt_k_(UInt_Companion_getInstance().__get_MAX_VALUE(), UInt_Companion_getInstance().__get_MIN_VALUE())
    return this
end function

function UIntRange_Companion_getInstance() as Object
    if GetGlobalAA().UIntRange_Companion_instance = invalid then
        GetGlobalAA().UIntRange_Companion_instance = UIntRange_Companion_create_k_()
    end if
    return GetGlobalAA().UIntRange_Companion_instance
end function

function UIntRange_Companion___get_EMPTY_k_() as Object
    return m.EMPTY
end function

function UIntProgression_create_UInt_UInt_I_k_(start as Object, endInclusive as Object, step_ as Integer) as Object
    this = {}
    this.__type = "UIntProgression"
    this.__proto = ["UIntProgression", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = UIntProgression_iterator_k_
    this.isEmpty_k_ = UIntProgression_isEmpty_k_
    this.equals_AnyN_k_ = UIntProgression_equals_AnyN_k_
    this.equals = UIntProgression_equals_AnyN_k_
    this.hashCode_k_ = UIntProgression_hashCode_k_
    this.hashCode = UIntProgression_hashCode_k_
    this.toString_k_ = UIntProgression_toString_k_
    this.toString = UIntProgression_toString_k_
    this.__get_first = UIntProgression___get_first_k_
    this.__get_last = UIntProgression___get_last_k_
    this.__get_step = UIntProgression___get_step_k_
    this.first = start
    this.last = getProgressionLastElement_UInt_UInt_I_k_(start, endInclusive, step_)
    this.step = step_
    return this
end function

function UIntProgression_iterator_k_() as Object
    return UIntProgressionIterator_create_UInt_UInt_I_k_(m.__get_first(), m.__get_last(), m.__get_step())
end function

function UIntProgression_isEmpty_k_() as Boolean
    __when_tmp9 = invalid
    if m.__get_step() > 0 then
        __when_tmp9 = (m.__get_first().compareTo_AnyN_k_(m.__get_last()) > 0)
    else if true then
        __when_tmp9 = (m.__get_first().compareTo_AnyN_k_(m.__get_last()) < 0)
    end if
    return __when_tmp9

end function

function UIntProgression_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "UIntProgression") and ((m.isEmpty_k_() and other.isEmpty_k_()) or ((brsStructuralEquals_AnyN_AnyN_k_(m.__get_first(), other.__get_first()) and brsStructuralEquals_AnyN_AnyN_k_(m.__get_last(), other.__get_last())) and (m.__get_step() = other.__get_step())))
end function

function UIntProgression_hashCode_k_() as Integer
    __when_tmp10 = invalid
    if m.isEmpty_k_() then
        __when_tmp10 = -1
    else if true then
        tmp0 = m.__get_first()
        tmp_ret_0 = invalid

        while true
            this = tmp0
            tmp_ret_0 = this.__get_data()
            exit while
        end while
        tmp0_1 = m.__get_last()
        tmp_ret_1 = invalid

        while true
            this = tmp0_1
            tmp_ret_1 = this.__get_data()
            exit while
        end while
        __when_tmp10 = ((31 * ((31 * tmp_ret_0) + tmp_ret_1)) + m.__get_step())
    end if
    return __when_tmp10

end function

function UIntProgression_toString_k_() as String
    __when_tmp11 = invalid
    if m.__get_step() > 0 then
        __when_tmp11 = ((((m.__get_first().toString() + "..") + m.__get_last().toString()) + " step ") + __kotlin_numToStr_I_k_(m.__get_step()))
    else if true then
        __when_tmp11 = ((((m.__get_first().toString() + " downTo ") + m.__get_last().toString()) + " step ") + __kotlin_numToStr_I_k_(-m.__get_step()))
    end if
    return __when_tmp11

end function

function UIntProgression___get_first_k_() as Object
    return m.first
end function

function UIntProgression___get_last_k_() as Object
    return m.last
end function

function UIntProgression___get_step_k_() as Integer
    return m.step
end function

function UIntProgression_Companion_create_k_() as Object
    this = {}
    this.__type = "UIntProgression_Companion"
    this.__proto = ["UIntProgression_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.fromClosedRange_UInt_UInt_I_k_ = UIntProgression_Companion_fromClosedRange_UInt_UInt_I_k_
    return this
end function

function UIntProgression_Companion_getInstance() as Object
    if GetGlobalAA().UIntProgression_Companion_instance = invalid then
        GetGlobalAA().UIntProgression_Companion_instance = UIntProgression_Companion_create_k_()
    end if
    return GetGlobalAA().UIntProgression_Companion_instance
end function

function UIntProgression_Companion_fromClosedRange_UInt_UInt_I_k_(rangeStart as Object, rangeEnd as Object, step_ as Integer) as Object
    return UIntProgression_create_UInt_UInt_I_k_(rangeStart, rangeEnd, step_)
end function

function UIntProgressionIterator_create_UInt_UInt_I_k_(first as Object, last as Object, step_ as Integer) as Object
    this = {}
    this.__type = "UIntProgressionIterator"
    this.__proto = ["UIntProgressionIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = UIntProgressionIterator_hasNext_k_
    this.next_k_ = UIntProgressionIterator_next_k_
    this.__get_finalElement = UIntProgressionIterator___get_finalElement_k_
    this.__get_hasNext = UIntProgressionIterator___get_hasNext_k_
    this.__set_hasNext = UIntProgressionIterator___set_hasNext_Z_k_
    this.__get_step = UIntProgressionIterator___get_step_k_
    this.__get_next = UIntProgressionIterator___get_next_k_
    this.__set_next = UIntProgressionIterator___set_next_UInt_k_
    this.finalElement = last
    __when_tmp12 = invalid
    if step_ > 0 then
        __when_tmp12 = (first.compareTo_AnyN_k_(last) <= 0)
    else if true then
        __when_tmp12 = (first.compareTo_AnyN_k_(last) >= 0)
    end if
    this.hasNext = __when_tmp12
    this.step = toUInt_rI_k_(step_)
    __when_tmp13 = invalid
    if this.__get_hasNext() then
        __when_tmp13 = first
    else if true then
        __when_tmp13 = this.__get_finalElement()
    end if
    this.next = __when_tmp13
    return this
end function

function UIntProgressionIterator_hasNext_k_() as Boolean
    return m.__get_hasNext()
end function

function UIntProgressionIterator_next_k_() as Object
    value = m.__get_next()
    if brsStructuralEquals_AnyN_AnyN_k_(value, m.__get_finalElement()) then
        if not m.__get_hasNext() then
            throw NoSuchElementException_create_k_()
        end if
        m.__set_hasNext(false)
    else if true then
        tmp0 = m.__get_next()
        tmp2 = m.__get_step()
        tmp_ret_0 = invalid

        while true
            this = tmp0
            other = tmp2
            tmp_ret_0 = UInt_create_I_k_(this.__get_data() + other.__get_data())
            exit while
        end while
        m.__set_next(tmp_ret_0)
    end if
    return value
end function

function UIntProgressionIterator___get_finalElement_k_() as Object
    return m.finalElement
end function

function UIntProgressionIterator___get_hasNext_k_() as Boolean
    return m.hasNext
end function

sub UIntProgressionIterator___set_hasNext_Z_k_(value as Boolean)
    m.hasNext = value
end sub

function UIntProgressionIterator___get_step_k_() as Object
    return m.step
end function

function UIntProgressionIterator___get_next_k_() as Object
    return m.next
end function

sub UIntProgressionIterator___set_next_UInt_k_(value as Object)
    m.next = value
end sub

function ULongRange_create_ULong_ULong_k_(start as Object, endInclusive as Object) as Object
    this = ULongProgression_create_ULong_ULong_J_k_(start, endInclusive, 1&)
    this._super = {}
    this._super.contains_Any_k_ = this.contains_Any_k_
    this._super.isEmpty_k_ = this.isEmpty_k_
    this._super.equals_AnyN_k_ = this.equals_AnyN_k_
    this._super.hashCode_k_ = this.hashCode_k_
    this._super.toString_k_ = this.toString_k_
    this.__proto = ["ULongRange", "ClosedRange", "OpenEndRange", this.__proto]
    this.__type = "ULongRange"
    this.contains_Any_k_ = ULongRange_contains_Any_k_
    this.isEmpty_k_ = ULongRange_isEmpty_k_
    this.equals_AnyN_k_ = ULongRange_equals_AnyN_k_
    this.equals = ULongRange_equals_AnyN_k_
    this.hashCode_k_ = ULongRange_hashCode_k_
    this.hashCode = ULongRange_hashCode_k_
    this.toString_k_ = ULongRange_toString_k_
    this.toString = ULongRange_toString_k_
    this.__get_start = ULongRange___get_start_k_
    this.__get_endInclusive = ULongRange___get_endInclusive_k_
    this.__get_endExclusive = ULongRange___get_endExclusive_k_
    return this
end function

function ULongRange_contains_Any_k_(value as Object) as Boolean
    return (m.__get_first().compareTo_AnyN_k_(value) <= 0) and (value.compareTo_AnyN_k_(m.__get_last()) <= 0)
end function

function ULongRange_isEmpty_k_() as Boolean
    return m.__get_first().compareTo_AnyN_k_(m.__get_last()) > 0
end function

function ULongRange_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "ULongRange") and ((m.isEmpty_k_() and other.isEmpty_k_()) or (brsStructuralEquals_AnyN_AnyN_k_(m.__get_first(), other.__get_first()) and brsStructuralEquals_AnyN_AnyN_k_(m.__get_last(), other.__get_last())))
end function

function ULongRange_hashCode_k_() as Integer
    __when_tmp14 = invalid
    if m.isEmpty_k_() then
        __when_tmp14 = -1
    else if true then
        tmp0_1 = m.__get_first()
        tmp0_2 = m.__get_first()
        tmp2_1 = 32
        tmp_ret_0 = invalid

        while true
            this = tmp0_2
            bitCount = tmp2_1
            tmp_ret_0 = ULong_create_J_k_(__kotlin_ushr(this.__get_data(), bitCount))
            exit while
        end while
        tmp2 = tmp_ret_0

        tmp_ret_1 = invalid

        while true
            this = tmp0_1
            other = tmp2
            tmp_ret_1 = ULong_create_J_k_((this.__get_data() or other.__get_data()) and not (this.__get_data() and other.__get_data()))
            exit while
        end while
        tmp0 = tmp_ret_1

        tmp_ret_2 = invalid

        while true
            this = tmp0
            tmp_ret_2 = this.__get_data()
            exit while
        end while
        tmp0_4 = m.__get_last()
        tmp0_5 = m.__get_last()
        tmp2_3 = 32
        tmp_ret_3 = invalid

        while true
            this = tmp0_5
            bitCount = tmp2_3
            tmp_ret_3 = ULong_create_J_k_(__kotlin_ushr(this.__get_data(), bitCount))
            exit while
        end while
        tmp2_2 = tmp_ret_3

        tmp_ret_4 = invalid

        while true
            this = tmp0_4
            other = tmp2_2
            tmp_ret_4 = ULong_create_J_k_((this.__get_data() or other.__get_data()) and not (this.__get_data() and other.__get_data()))
            exit while
        end while
        tmp0_3 = tmp_ret_4

        tmp_ret_5 = invalid

        while true
            this = tmp0_3
            tmp_ret_5 = this.__get_data()
            exit while
        end while
        __when_tmp14 = ((31 * tmp_ret_2) + tmp_ret_5)
    end if
    return __when_tmp14

end function

function ULongRange_toString_k_() as String
    return (m.__get_first().toString() + "..") + m.__get_last().toString()
end function

function ULongRange___get_start_k_() as Object
    return m.__get_first()
end function

function ULongRange___get_endInclusive_k_() as Object
    return m.__get_last()
end function

function ULongRange___get_endExclusive_k_() as Object
    if brsStructuralEquals_AnyN_AnyN_k_(m.__get_last(), ULong_Companion_getInstance().__get_MAX_VALUE()) then
        error_Any_k_("Cannot return the exclusive upper bound of a range that includes MAX_VALUE.")
    end if
    return m.__get_last().plus_UInt_k_(UInt_create_I_k_(1))
end function

function ULongRange_Companion_create_k_() as Object
    this = {}
    this.__type = "ULongRange_Companion"
    this.__proto = ["ULongRange_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_EMPTY = ULongRange_Companion___get_EMPTY_k_
    this.EMPTY = ULongRange_create_ULong_ULong_k_(ULong_Companion_getInstance().__get_MAX_VALUE(), ULong_Companion_getInstance().__get_MIN_VALUE())
    return this
end function

function ULongRange_Companion_getInstance() as Object
    if GetGlobalAA().ULongRange_Companion_instance = invalid then
        GetGlobalAA().ULongRange_Companion_instance = ULongRange_Companion_create_k_()
    end if
    return GetGlobalAA().ULongRange_Companion_instance
end function

function ULongRange_Companion___get_EMPTY_k_() as Object
    return m.EMPTY
end function

function ULongProgression_create_ULong_ULong_J_k_(start as Object, endInclusive as Object, step_ as LongInteger) as Object
    this = {}
    this.__type = "ULongProgression"
    this.__proto = ["ULongProgression", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = ULongProgression_iterator_k_
    this.isEmpty_k_ = ULongProgression_isEmpty_k_
    this.equals_AnyN_k_ = ULongProgression_equals_AnyN_k_
    this.equals = ULongProgression_equals_AnyN_k_
    this.hashCode_k_ = ULongProgression_hashCode_k_
    this.hashCode = ULongProgression_hashCode_k_
    this.toString_k_ = ULongProgression_toString_k_
    this.toString = ULongProgression_toString_k_
    this.__get_first = ULongProgression___get_first_k_
    this.__get_last = ULongProgression___get_last_k_
    this.__get_step = ULongProgression___get_step_k_
    this.first = start
    this.last = getProgressionLastElement_ULong_ULong_J_k_(start, endInclusive, step_)
    this.step = step_
    return this
end function

function ULongProgression_iterator_k_() as Object
    return ULongProgressionIterator_create_ULong_ULong_J_k_(m.__get_first(), m.__get_last(), m.__get_step())
end function

function ULongProgression_isEmpty_k_() as Boolean
    __when_tmp15 = invalid
    if m.__get_step() > 0 then
        __when_tmp15 = (m.__get_first().compareTo_AnyN_k_(m.__get_last()) > 0)
    else if true then
        __when_tmp15 = (m.__get_first().compareTo_AnyN_k_(m.__get_last()) < 0)
    end if
    return __when_tmp15

end function

function ULongProgression_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "ULongProgression") and ((m.isEmpty_k_() and other.isEmpty_k_()) or ((brsStructuralEquals_AnyN_AnyN_k_(m.__get_first(), other.__get_first()) and brsStructuralEquals_AnyN_AnyN_k_(m.__get_last(), other.__get_last())) and (m.__get_step() = other.__get_step())))
end function

function ULongProgression_hashCode_k_() as Integer
    __when_tmp16 = invalid
    if m.isEmpty_k_() then
        __when_tmp16 = -1
    else if true then
        tmp0_1 = m.__get_first()
        tmp0_2 = m.__get_first()
        tmp2_1 = 32
        tmp_ret_0 = invalid

        while true
            this = tmp0_2
            bitCount = tmp2_1
            tmp_ret_0 = ULong_create_J_k_(__kotlin_ushr(this.__get_data(), bitCount))
            exit while
        end while
        tmp2 = tmp_ret_0

        tmp_ret_1 = invalid

        while true
            this = tmp0_1
            other = tmp2
            tmp_ret_1 = ULong_create_J_k_((this.__get_data() or other.__get_data()) and not (this.__get_data() and other.__get_data()))
            exit while
        end while
        tmp0 = tmp_ret_1

        tmp_ret_2 = invalid

        while true
            this = tmp0
            tmp_ret_2 = this.__get_data()
            exit while
        end while
        tmp0_4 = m.__get_last()
        tmp0_5 = m.__get_last()
        tmp2_3 = 32
        tmp_ret_3 = invalid

        while true
            this = tmp0_5
            bitCount = tmp2_3
            tmp_ret_3 = ULong_create_J_k_(__kotlin_ushr(this.__get_data(), bitCount))
            exit while
        end while
        tmp2_2 = tmp_ret_3

        tmp_ret_4 = invalid

        while true
            this = tmp0_4
            other = tmp2_2
            tmp_ret_4 = ULong_create_J_k_((this.__get_data() or other.__get_data()) and not (this.__get_data() and other.__get_data()))
            exit while
        end while
        tmp0_3 = tmp_ret_4

        tmp_ret_5 = invalid

        while true
            this = tmp0_3
            tmp_ret_5 = this.__get_data()
            exit while
        end while
        __when_tmp16 = ((31 * ((31 * tmp_ret_2) + tmp_ret_5)) + ((m.__get_step() or __kotlin_ushr(m.__get_step(), 32)) and not (m.__get_step() and __kotlin_ushr(m.__get_step(), 32))))
    end if
    return __when_tmp16

end function

function ULongProgression_toString_k_() as String
    __when_tmp17 = invalid
    if m.__get_step() > 0 then
        __when_tmp17 = ((((m.__get_first().toString() + "..") + m.__get_last().toString()) + " step ") + __kotlin_numToStr_J_k_(m.__get_step()))
    else if true then
        __when_tmp17 = ((((m.__get_first().toString() + " downTo ") + m.__get_last().toString()) + " step ") + __kotlin_numToStr_J_k_(-m.__get_step()))
    end if
    return __when_tmp17

end function

function ULongProgression___get_first_k_() as Object
    return m.first
end function

function ULongProgression___get_last_k_() as Object
    return m.last
end function

function ULongProgression___get_step_k_() as LongInteger
    return m.step
end function

function ULongProgression_Companion_create_k_() as Object
    this = {}
    this.__type = "ULongProgression_Companion"
    this.__proto = ["ULongProgression_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.fromClosedRange_ULong_ULong_J_k_ = ULongProgression_Companion_fromClosedRange_ULong_ULong_J_k_
    return this
end function

function ULongProgression_Companion_getInstance() as Object
    if GetGlobalAA().ULongProgression_Companion_instance = invalid then
        GetGlobalAA().ULongProgression_Companion_instance = ULongProgression_Companion_create_k_()
    end if
    return GetGlobalAA().ULongProgression_Companion_instance
end function

function ULongProgression_Companion_fromClosedRange_ULong_ULong_J_k_(rangeStart as Object, rangeEnd as Object, step_ as LongInteger) as Object
    return ULongProgression_create_ULong_ULong_J_k_(rangeStart, rangeEnd, step_)
end function

function ULongProgressionIterator_create_ULong_ULong_J_k_(first as Object, last as Object, step_ as LongInteger) as Object
    this = {}
    this.__type = "ULongProgressionIterator"
    this.__proto = ["ULongProgressionIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = ULongProgressionIterator_hasNext_k_
    this.next_k_ = ULongProgressionIterator_next_k_
    this.__get_finalElement = ULongProgressionIterator___get_finalElement_k_
    this.__get_hasNext = ULongProgressionIterator___get_hasNext_k_
    this.__set_hasNext = ULongProgressionIterator___set_hasNext_Z_k_
    this.__get_step = ULongProgressionIterator___get_step_k_
    this.__get_next = ULongProgressionIterator___get_next_k_
    this.__set_next = ULongProgressionIterator___set_next_ULong_k_
    this.finalElement = last
    __when_tmp18 = invalid
    if step_ > 0 then
        __when_tmp18 = (first.compareTo_AnyN_k_(last) <= 0)
    else if true then
        __when_tmp18 = (first.compareTo_AnyN_k_(last) >= 0)
    end if
    this.hasNext = __when_tmp18
    this.step = toULong_rJ_k_(step_)
    __when_tmp19 = invalid
    if this.__get_hasNext() then
        __when_tmp19 = first
    else if true then
        __when_tmp19 = this.__get_finalElement()
    end if
    this.next = __when_tmp19
    return this
end function

function ULongProgressionIterator_hasNext_k_() as Boolean
    return m.__get_hasNext()
end function

function ULongProgressionIterator_next_k_() as Object
    value = m.__get_next()
    if brsStructuralEquals_AnyN_AnyN_k_(value, m.__get_finalElement()) then
        if not m.__get_hasNext() then
            throw NoSuchElementException_create_k_()
        end if
        m.__set_hasNext(false)
    else if true then
        tmp0 = m.__get_next()
        tmp2 = m.__get_step()
        tmp_ret_0 = invalid

        while true
            this = tmp0
            other = tmp2
            tmp_ret_0 = ULong_create_J_k_(this.__get_data() + other.__get_data())
            exit while
        end while
        m.__set_next(tmp_ret_0)
    end if
    return value
end function

function ULongProgressionIterator___get_finalElement_k_() as Object
    return m.finalElement
end function

function ULongProgressionIterator___get_hasNext_k_() as Boolean
    return m.hasNext
end function

sub ULongProgressionIterator___set_hasNext_Z_k_(value as Boolean)
    m.hasNext = value
end sub

function ULongProgressionIterator___get_step_k_() as Object
    return m.step
end function

function ULongProgressionIterator___get_next_k_() as Object
    return m.next
end function

sub ULongProgressionIterator___set_next_ULong_k_(value as Object)
    m.next = value
end sub

function until_rUInt_UInt_k_(m as Object, to_ as Object) as Object
    if to_.compareTo_AnyN_k_(UInt_Companion_getInstance().__get_MIN_VALUE()) <= 0 then
        return UIntRange_Companion_getInstance().__get_EMPTY()
    end if
    tmp0 = to_
    tmp2 = UInt_create_I_k_(1)
    tmp_ret_0 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp_ret_0 = UInt_create_I_k_(this.__get_data() - other.__get_data())
        exit while
    end while
    return m.rangeTo_UInt_k_(tmp_ret_0)

end function

function until_rULong_ULong_k_(m as Object, to_ as Object) as Object
    if to_.compareTo_AnyN_k_(ULong_Companion_getInstance().__get_MIN_VALUE()) <= 0 then
        return ULongRange_Companion_getInstance().__get_EMPTY()
    end if
    tmp0 = to_
    tmp2 = UInt_create_I_k_(1)
    tmp_ret_2 = invalid

    while true
        this = tmp0
        other = tmp2
        tmp0_1 = this
        tmp0_2 = other
        tmp_ret_0 = invalid

        while true
            this = tmp0_2
            tmp_ret_0 = uintToULong_I_k_(this.__get_data())
            exit while
        end while
        tmp2_1 = tmp_ret_0

        tmp_ret_1 = invalid

        while true
            this = tmp0_1
            other = tmp2_1
            tmp_ret_1 = ULong_create_J_k_(this.__get_data() - other.__get_data())
            exit while
        end while
        tmp_ret_2 = tmp_ret_1
        exit while
    end while
    return m.rangeTo_ULong_k_(tmp_ret_2)

end function

function downTo_rUInt_UInt_k_(m as Object, to_ as Object) as Object
    return UIntProgression_Companion_getInstance().fromClosedRange_UInt_UInt_I_k_(m, to_, -1)
end function

function downTo_rULong_ULong_k_(m as Object, to_ as Object) as Object
    return ULongProgression_Companion_getInstance().fromClosedRange_ULong_ULong_J_k_(m, to_, -1&)
end function

function step_rUIntProgression_I_k_(m as Object, step_ as Integer) as Object
    checkStepIsPositive_Z_Number_k_(step_ > 0, step_)
    __when_tmp20 = invalid
    if m.__get_step() > 0 then
        __when_tmp20 = step_
    else if true then
        __when_tmp20 = -step_
    end if
    return UIntProgression_Companion_getInstance().fromClosedRange_UInt_UInt_I_k_(m.__get_first(), m.__get_last(), __when_tmp20)

end function

function step_rULongProgression_J_k_(m as Object, step_ as LongInteger) as Object
    checkStepIsPositive_Z_Number_k_(step_ > 0, step_)
    __when_tmp21 = invalid
    if m.__get_step() > 0 then
        __when_tmp21 = step_
    else if true then
        __when_tmp21 = -step_
    end if
    return ULongProgression_Companion_getInstance().fromClosedRange_ULong_ULong_J_k_(m.__get_first(), m.__get_last(), __when_tmp21)

end function

sub checkStepIsPositive_Z_Number_k_(isPositive as Boolean, step_ as Object)
    if not isPositive then
        throw IllegalArgumentException_create_StrN_k_(("Step must be positive, was: " + step_.toString()) + ".")
    end if
end sub
