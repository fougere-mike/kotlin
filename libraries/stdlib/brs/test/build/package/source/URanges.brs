function differenceModulo_UInt_UInt_UInt_UInt_k_(a as Object, b as Object, c as Object) as Object
    ac = a mod c
    bc = b mod c
    __when_tmp0 = invalid
    if (ac >= bc) >= 0 then
        __when_tmp0 = (ac - bc)
    else if true then
        __when_tmp0 = ((ac - bc) + c)
    end if
    return __when_tmp0

end function

function differenceModulo_ULong_ULong_ULong_ULong_k_(a as Object, b as Object, c as Object) as Object
    ac = a mod c
    bc = b mod c
    __when_tmp1 = invalid
    if (ac >= bc) >= 0 then
        __when_tmp1 = (ac - bc)
    else if true then
        __when_tmp1 = ((ac - bc) + c)
    end if
    return __when_tmp1

end function

function getProgressionLastElement_UInt_UInt_I_UInt_k_(start as Object, end_ as Object, step_ as Integer) as Object
    __when_tmp4 = invalid
    if step_ > 0 then
        __when_tmp2 = invalid
        if (start >= end_) >= 0 then
            __when_tmp2 = end_
        else if true then
            __when_tmp2 = (end_ - differenceModulo_UInt_UInt_UInt_UInt_k_(end_, start, toUInt_rI_UInt_k_(step_)))
        end if
        __when_tmp4 = __when_tmp2
    else if step_ < 0 then
        __when_tmp3 = invalid
        if (start <= end_) <= 0 then
            __when_tmp3 = end_
        else if true then
            __when_tmp3 = (end_ + differenceModulo_UInt_UInt_UInt_UInt_k_(start, end_, toUInt_rI_UInt_k_(-step_)))
        end if
        __when_tmp4 = __when_tmp3
    else if true then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Step is zero.")
    end if
    return __when_tmp4

end function

function getProgressionLastElement_ULong_ULong_J_ULong_k_(start as Object, end_ as Object, step_ as LongInteger) as Object
    __when_tmp7 = invalid
    if step_ > 0 then
        __when_tmp5 = invalid
        if (start >= end_) >= 0 then
            __when_tmp5 = end_
        else if true then
            __when_tmp5 = (end_ - differenceModulo_ULong_ULong_ULong_ULong_k_(end_, start, toULong_rJ_ULong_k_(step_)))
        end if
        __when_tmp7 = __when_tmp5
    else if step_ < 0 then
        __when_tmp6 = invalid
        if (start <= end_) <= 0 then
            __when_tmp6 = end_
        else if true then
            __when_tmp6 = (end_ + differenceModulo_ULong_ULong_ULong_ULong_k_(start, end_, toULong_rJ_ULong_k_(-step_)))
        end if
        __when_tmp7 = __when_tmp6
    else if true then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Step is zero.")
    end if
    return __when_tmp7

end function

function UIntRange_create_UInt_UInt_UIntRange_k_(start as Object, endInclusive as Object) as Object
    this = UIntProgression_create_UInt_UInt_I_UIntProgression_k_(start, endInclusive, 1)
    this._super = {}
    this._super.contains_UInt_Z_k_ = this.contains_UInt_Z_k_
    this._super.isEmpty_Z_k_ = this.isEmpty_Z_k_
    this._super.equals_AnyN_Z_k_ = this.equals_AnyN_Z_k_
    this._super.hashCode_I_k_ = this.hashCode_I_k_
    this._super.toString_Str_k_ = this.toString_Str_k_
    this.__proto = ["UIntRange", this.__proto]
    this.__type = "UIntRange"
    this.contains_UInt_Z_k_ = UIntRange_contains_UInt_Z_k_
    this.isEmpty_Z_k_ = UIntRange_isEmpty_Z_k_
    this.equals_AnyN_Z_k_ = UIntRange_equals_AnyN_Z_k_
    this.hashCode_I_k_ = UIntRange_hashCode_I_k_
    this.toString_Str_k_ = UIntRange_toString_Str_k_
    this.get_start = UIntRange_get_start_UInt_k_
    this.get_endInclusive = UIntRange_get_endInclusive_UInt_k_
    this.get_endExclusive = UIntRange_get_endExclusive_UInt_k_
    return this
end function

function UIntRange_contains_UInt_Z_k_(value as Object) as Boolean
    return ((m.first <= value) <= 0) and ((value <= m.last) <= 0)
end function

function UIntRange_isEmpty_Z_k_() as Boolean
    return (m.first > m.last) > 0
end function

function UIntRange_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "UIntRange") and ((m.isEmpty() and other.isEmpty()) or ((m.first = other.first) and (m.last = other.last)))
end function

function UIntRange_hashCode_I_k_() as Integer
    __when_tmp8 = invalid
    if m.isEmpty() then
        __when_tmp8 = -1
    else if true then
        __when_tmp8 = ((31 * m.first) + m.last)
    end if
    return __when_tmp8

end function

function UIntRange_toString_Str_k_() as String
    return (m.first + "..") + m.last
end function

function UIntRange_get_start_UInt_k_() as Object
    return m.first
end function

function UIntRange_get_endInclusive_UInt_k_() as Object
    return m.last
end function

function UIntRange_get_endExclusive_UInt_k_() as Object
    if m.last = UInt_Companion_get_MAX_VALUE_UInt_k_() then
        error_Any_k_("Cannot return the exclusive upper bound of a range that includes MAX_VALUE.")
    end if
    return m.last + 1
end function

function UIntRange_Companion_create_Companion_k_() as Object
    this = {}
    this.__type = "UIntRange_Companion"
    this.__proto = ["UIntRange_Companion"]
    this.EMPTY = UIntRange_create_UInt_UInt_UIntRange_k_(UInt_Companion_get_MAX_VALUE_UInt_k_(), UInt_Companion_get_MIN_VALUE_UInt_k_())
    this.get_EMPTY = UIntRange_Companion_get_EMPTY_UIntRange_k_
    return this
end function

function UIntRange_Companion_getInstance() as Object
    if m.UIntRange_Companion_instance = invalid then
        m.UIntRange_Companion_instance = UIntRange_Companion_create()
    end if
    return m.UIntRange_Companion_instance
end function

function UIntRange_Companion_get_EMPTY_UIntRange_k_() as Object
    return m.EMPTY
end function

function UIntProgression_create_UInt_UInt_I_UIntProgression_k_(start as Object, endInclusive as Object, step_ as Integer) as Object
    this = {}
    this.__type = "UIntProgression"
    this.__proto = ["UIntProgression"]
    this.first = start
    this.last = getProgressionLastElement_UInt_UInt_I_UInt_k_(start, endInclusive, step_)
    this.step = step_
    this.iterator_IteratorUInt_k_ = UIntProgression_iterator_IteratorUInt_k_
    this.isEmpty_Z_k_ = UIntProgression_isEmpty_Z_k_
    this.equals_AnyN_Z_k_ = UIntProgression_equals_AnyN_Z_k_
    this.hashCode_I_k_ = UIntProgression_hashCode_I_k_
    this.toString_Str_k_ = UIntProgression_toString_Str_k_
    this.get_first = UIntProgression_get_first_UInt_k_
    this.get_last = UIntProgression_get_last_UInt_k_
    this.get_step = UIntProgression_get_step_I_k_
    return this
end function

function UIntProgression_iterator_IteratorUInt_k_() as Object
    return UIntProgressionIterator_create_UInt_UInt_I_UIntProgressionIterator_k_(m.first, m.last, m.step)
end function

function UIntProgression_isEmpty_Z_k_() as Boolean
    __when_tmp9 = invalid
    if m.step > 0 then
        __when_tmp9 = ((m.first > m.last) > 0)
    else if true then
        __when_tmp9 = ((m.first < m.last) < 0)
    end if
    return __when_tmp9

end function

function UIntProgression_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "UIntProgression") and ((m.isEmpty() and other.isEmpty()) or (((m.first = other.first) and (m.last = other.last)) and (m.step = other.step)))
end function

function UIntProgression_hashCode_I_k_() as Integer
    __when_tmp10 = invalid
    if m.isEmpty() then
        __when_tmp10 = -1
    else if true then
        __when_tmp10 = ((31 * ((31 * m.first) + m.last)) + m.step)
    end if
    return __when_tmp10

end function

function UIntProgression_toString_Str_k_() as String
    __when_tmp11 = invalid
    if m.step > 0 then
        __when_tmp11 = ((((m.first + "..") + m.last) + " step ") + m.step)
    else if true then
        __when_tmp11 = ((((m.first + " downTo ") + m.last) + " step ") + -m.step)
    end if
    return __when_tmp11

end function

function UIntProgression_get_first_UInt_k_() as Object
    return m.first
end function

function UIntProgression_get_last_UInt_k_() as Object
    return m.last
end function

function UIntProgression_get_step_I_k_() as Integer
    return m.step
end function

function UIntProgression_Companion_create_Companion_k_() as Object
    this = {}
    this.__type = "UIntProgression_Companion"
    this.__proto = ["UIntProgression_Companion"]
    this.fromClosedRange_UInt_UInt_I_UIntProgression_k_ = UIntProgression_Companion_fromClosedRange_UInt_UInt_I_UIntProgression_k_
    return this
end function

function UIntProgression_Companion_getInstance() as Object
    if m.UIntProgression_Companion_instance = invalid then
        m.UIntProgression_Companion_instance = UIntProgression_Companion_create()
    end if
    return m.UIntProgression_Companion_instance
end function

function UIntProgression_Companion_fromClosedRange_UInt_UInt_I_UIntProgression_k_(rangeStart as Object, rangeEnd as Object, step_ as Integer) as Object
    return UIntProgression_create_UInt_UInt_I_UIntProgression_k_(rangeStart, rangeEnd, step_)
end function

function UIntProgressionIterator_create_UInt_UInt_I_UIntProgressionIterator_k_(first as Object, last as Object, step_ as Integer) as Object
    this = {}
    this.__type = "UIntProgressionIterator"
    this.__proto = ["UIntProgressionIterator"]
    this.finalElement = last
    this.hasNext = __when_tmp12
    this.step = toUInt_rI_UInt_k_(step_)
    this.next = __when_tmp13
    this.hasNext_Z_k_ = UIntProgressionIterator_hasNext_Z_k_
    this.next_UInt_k_ = UIntProgressionIterator_next_UInt_k_
    this.get_finalElement = UIntProgressionIterator_get_finalElement_UInt_k_
    this.get_hasNext = UIntProgressionIterator_get_hasNext_Z_k_
    this.set_hasNext = UIntProgressionIterator_set_hasNext_Z_k_
    this.get_step = UIntProgressionIterator_get_step_UInt_k_
    this.get_next = UIntProgressionIterator_get_next_UInt_k_
    this.set_next = UIntProgressionIterator_set_next_UInt_k_
    return this
end function

function UIntProgressionIterator_hasNext_Z_k_() as Boolean
    return m.hasNext
end function

function UIntProgressionIterator_next_UInt_k_() as Object
    value = m.next
    if value = m.finalElement then
        if m.hasNext.not() then
            throw NoSuchElementException_create_NoSuchElementException_k_()
        end if
        m.hasNext = false
    else if true then
        m.next = (m.next + m.step)
    end if
    return value
end function

function UIntProgressionIterator_get_finalElement_UInt_k_() as Object
    return m.finalElement
end function

function UIntProgressionIterator_get_hasNext_Z_k_() as Boolean
    return m.hasNext
end function

sub UIntProgressionIterator_set_hasNext_Z_k_(value as Boolean)
    m.hasNext = value
end sub

function UIntProgressionIterator_get_step_UInt_k_() as Object
    return m.step
end function

function UIntProgressionIterator_get_next_UInt_k_() as Object
    return m.next
end function

sub UIntProgressionIterator_set_next_UInt_k_(value as Object)
    m.next = value
end sub

function ULongRange_create_ULong_ULong_ULongRange_k_(start as Object, endInclusive as Object) as Object
    this = ULongProgression_create_ULong_ULong_J_ULongProgression_k_(start, endInclusive, 1&)
    this._super = {}
    this._super.contains_ULong_Z_k_ = this.contains_ULong_Z_k_
    this._super.isEmpty_Z_k_ = this.isEmpty_Z_k_
    this._super.equals_AnyN_Z_k_ = this.equals_AnyN_Z_k_
    this._super.hashCode_I_k_ = this.hashCode_I_k_
    this._super.toString_Str_k_ = this.toString_Str_k_
    this.__proto = ["ULongRange", this.__proto]
    this.__type = "ULongRange"
    this.contains_ULong_Z_k_ = ULongRange_contains_ULong_Z_k_
    this.isEmpty_Z_k_ = ULongRange_isEmpty_Z_k_
    this.equals_AnyN_Z_k_ = ULongRange_equals_AnyN_Z_k_
    this.hashCode_I_k_ = ULongRange_hashCode_I_k_
    this.toString_Str_k_ = ULongRange_toString_Str_k_
    this.get_start = ULongRange_get_start_ULong_k_
    this.get_endInclusive = ULongRange_get_endInclusive_ULong_k_
    this.get_endExclusive = ULongRange_get_endExclusive_ULong_k_
    return this
end function

function ULongRange_contains_ULong_Z_k_(value as Object) as Boolean
    return ((m.first <= value) <= 0) and ((value <= m.last) <= 0)
end function

function ULongRange_isEmpty_Z_k_() as Boolean
    return (m.first > m.last) > 0
end function

function ULongRange_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "ULongRange") and ((m.isEmpty() and other.isEmpty()) or ((m.first = other.first) and (m.last = other.last)))
end function

function ULongRange_hashCode_I_k_() as Integer
    __when_tmp14 = invalid
    if m.isEmpty() then
        __when_tmp14 = -1
    else if true then
        __when_tmp14 = ((31 * m.first.xor(m.first.shr(32))) + m.last.xor(m.last.shr(32)))
    end if
    return __when_tmp14

end function

function ULongRange_toString_Str_k_() as String
    return (m.first + "..") + m.last
end function

function ULongRange_get_start_ULong_k_() as Object
    return m.first
end function

function ULongRange_get_endInclusive_ULong_k_() as Object
    return m.last
end function

function ULongRange_get_endExclusive_ULong_k_() as Object
    if m.last = ULong_Companion_get_MAX_VALUE_ULong_k_() then
        error_Any_k_("Cannot return the exclusive upper bound of a range that includes MAX_VALUE.")
    end if
    return m.last + 1
end function

function ULongRange_Companion_create_Companion_k_() as Object
    this = {}
    this.__type = "ULongRange_Companion"
    this.__proto = ["ULongRange_Companion"]
    this.EMPTY = ULongRange_create_ULong_ULong_ULongRange_k_(ULong_Companion_get_MAX_VALUE_ULong_k_(), ULong_Companion_get_MIN_VALUE_ULong_k_())
    this.get_EMPTY = ULongRange_Companion_get_EMPTY_ULongRange_k_
    return this
end function

function ULongRange_Companion_getInstance() as Object
    if m.ULongRange_Companion_instance = invalid then
        m.ULongRange_Companion_instance = ULongRange_Companion_create()
    end if
    return m.ULongRange_Companion_instance
end function

function ULongRange_Companion_get_EMPTY_ULongRange_k_() as Object
    return m.EMPTY
end function

function ULongProgression_create_ULong_ULong_J_ULongProgression_k_(start as Object, endInclusive as Object, step_ as LongInteger) as Object
    this = {}
    this.__type = "ULongProgression"
    this.__proto = ["ULongProgression"]
    this.first = start
    this.last = getProgressionLastElement_ULong_ULong_J_ULong_k_(start, endInclusive, step_)
    this.step = step_
    this.iterator_IteratorULong_k_ = ULongProgression_iterator_IteratorULong_k_
    this.isEmpty_Z_k_ = ULongProgression_isEmpty_Z_k_
    this.equals_AnyN_Z_k_ = ULongProgression_equals_AnyN_Z_k_
    this.hashCode_I_k_ = ULongProgression_hashCode_I_k_
    this.toString_Str_k_ = ULongProgression_toString_Str_k_
    this.get_first = ULongProgression_get_first_ULong_k_
    this.get_last = ULongProgression_get_last_ULong_k_
    this.get_step = ULongProgression_get_step_J_k_
    return this
end function

function ULongProgression_iterator_IteratorULong_k_() as Object
    return ULongProgressionIterator_create_ULong_ULong_J_ULongProgressionIterator_k_(m.first, m.last, m.step)
end function

function ULongProgression_isEmpty_Z_k_() as Boolean
    __when_tmp15 = invalid
    if m.step > 0 then
        __when_tmp15 = ((m.first > m.last) > 0)
    else if true then
        __when_tmp15 = ((m.first < m.last) < 0)
    end if
    return __when_tmp15

end function

function ULongProgression_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "ULongProgression") and ((m.isEmpty() and other.isEmpty()) or (((m.first = other.first) and (m.last = other.last)) and (m.step = other.step)))
end function

function ULongProgression_hashCode_I_k_() as Integer
    __when_tmp16 = invalid
    if m.isEmpty() then
        __when_tmp16 = -1
    else if true then
        __when_tmp16 = ((31 * ((31 * m.first.xor(m.first.shr(32))) + m.last.xor(m.last.shr(32)))) + m.step.xor(m.step.ushr(32)))
    end if
    return __when_tmp16

end function

function ULongProgression_toString_Str_k_() as String
    __when_tmp17 = invalid
    if m.step > 0 then
        __when_tmp17 = ((((m.first + "..") + m.last) + " step ") + m.step)
    else if true then
        __when_tmp17 = ((((m.first + " downTo ") + m.last) + " step ") + -m.step)
    end if
    return __when_tmp17

end function

function ULongProgression_get_first_ULong_k_() as Object
    return m.first
end function

function ULongProgression_get_last_ULong_k_() as Object
    return m.last
end function

function ULongProgression_get_step_J_k_() as LongInteger
    return m.step
end function

function ULongProgression_Companion_create_Companion_k_() as Object
    this = {}
    this.__type = "ULongProgression_Companion"
    this.__proto = ["ULongProgression_Companion"]
    this.fromClosedRange_ULong_ULong_J_ULongProgression_k_ = ULongProgression_Companion_fromClosedRange_ULong_ULong_J_ULongProgression_k_
    return this
end function

function ULongProgression_Companion_getInstance() as Object
    if m.ULongProgression_Companion_instance = invalid then
        m.ULongProgression_Companion_instance = ULongProgression_Companion_create()
    end if
    return m.ULongProgression_Companion_instance
end function

function ULongProgression_Companion_fromClosedRange_ULong_ULong_J_ULongProgression_k_(rangeStart as Object, rangeEnd as Object, step_ as LongInteger) as Object
    return ULongProgression_create_ULong_ULong_J_ULongProgression_k_(rangeStart, rangeEnd, step_)
end function

function ULongProgressionIterator_create_ULong_ULong_J_ULongProgressionIterator_k_(first as Object, last as Object, step_ as LongInteger) as Object
    this = {}
    this.__type = "ULongProgressionIterator"
    this.__proto = ["ULongProgressionIterator"]
    this.finalElement = last
    this.hasNext = __when_tmp18
    this.step = toULong_rJ_ULong_k_(step_)
    this.next = __when_tmp19
    this.hasNext_Z_k_ = ULongProgressionIterator_hasNext_Z_k_
    this.next_ULong_k_ = ULongProgressionIterator_next_ULong_k_
    this.get_finalElement = ULongProgressionIterator_get_finalElement_ULong_k_
    this.get_hasNext = ULongProgressionIterator_get_hasNext_Z_k_
    this.set_hasNext = ULongProgressionIterator_set_hasNext_Z_k_
    this.get_step = ULongProgressionIterator_get_step_ULong_k_
    this.get_next = ULongProgressionIterator_get_next_ULong_k_
    this.set_next = ULongProgressionIterator_set_next_ULong_k_
    return this
end function

function ULongProgressionIterator_hasNext_Z_k_() as Boolean
    return m.hasNext
end function

function ULongProgressionIterator_next_ULong_k_() as Object
    value = m.next
    if value = m.finalElement then
        if m.hasNext.not() then
            throw NoSuchElementException_create_NoSuchElementException_k_()
        end if
        m.hasNext = false
    else if true then
        m.next = (m.next + m.step)
    end if
    return value
end function

function ULongProgressionIterator_get_finalElement_ULong_k_() as Object
    return m.finalElement
end function

function ULongProgressionIterator_get_hasNext_Z_k_() as Boolean
    return m.hasNext
end function

sub ULongProgressionIterator_set_hasNext_Z_k_(value as Boolean)
    m.hasNext = value
end sub

function ULongProgressionIterator_get_step_ULong_k_() as Object
    return m.step
end function

function ULongProgressionIterator_get_next_ULong_k_() as Object
    return m.next
end function

sub ULongProgressionIterator_set_next_ULong_k_(value as Object)
    m.next = value
end sub

function until_rUInt_UInt_UIntRange_k_(m as Object, to_ as Object) as Object
    if (to_ <= UInt_Companion_get_MIN_VALUE_UInt_k_()) <= 0 then
        return UIntRange_Companion_get_EMPTY_UIntRange_k_()
    end if
    return m.rangeTo(to_ - 1)
end function

function until_rULong_ULong_ULongRange_k_(m as Object, to_ as Object) as Object
    if (to_ <= ULong_Companion_get_MIN_VALUE_ULong_k_()) <= 0 then
        return ULongRange_Companion_get_EMPTY_ULongRange_k_()
    end if
    return m.rangeTo(to_ - 1)
end function

function downTo_rUInt_UInt_UIntProgression_k_(m as Object, to_ as Object) as Object
    return UIntProgression_Companion_fromClosedRange_UInt_UInt_I_UIntProgression_k_(m, to_, -1)
end function

function downTo_rULong_ULong_ULongProgression_k_(m as Object, to_ as Object) as Object
    return ULongProgression_Companion_fromClosedRange_ULong_ULong_J_ULongProgression_k_(m, to_, -1&)
end function

function step_rUIntProgression_I_UIntProgression_k_(m as Object, step_ as Integer) as Object
    checkStepIsPositive_Z_Number_k_(step_ > 0, step_)
    __when_tmp20 = invalid
    if m.step > 0 then
        __when_tmp20 = step_
    else if true then
        __when_tmp20 = -step_
    end if
    return UIntProgression_Companion_fromClosedRange_UInt_UInt_I_UIntProgression_k_(m.first, m.last, __when_tmp20)

end function

function step_rULongProgression_J_ULongProgression_k_(m as Object, step_ as LongInteger) as Object
    checkStepIsPositive_Z_Number_k_(step_ > 0, step_)
    __when_tmp21 = invalid
    if m.step > 0 then
        __when_tmp21 = step_
    else if true then
        __when_tmp21 = -step_
    end if
    return ULongProgression_Companion_fromClosedRange_ULong_ULong_J_ULongProgression_k_(m.first, m.last, __when_tmp21)

end function

sub checkStepIsPositive_Z_Number_k_(isPositive as Boolean, step_ as Object)
    if isPositive.not() then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(("Step must be positive, was: " + step_) + ".")
    end if
end sub
