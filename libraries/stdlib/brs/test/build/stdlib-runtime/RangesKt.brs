function ClosedRange_contains_Any_k_(value as Object) as Boolean
    return (value.compareTo_AnyN_k_(m.__get_start()) >= 0) and (value.compareTo_AnyN_k_(m.__get_endInclusive()) <= 0)
end function

function ClosedRange_isEmpty_k_() as Boolean
    return m.__get_start().compareTo_AnyN_k_(m.__get_endInclusive()) > 0
end function

function ClosedRange___get_start_k_() as Object
end function

function ClosedRange___get_endInclusive_k_() as Object
end function

function OpenEndRange_contains_Any_k_(value as Object) as Boolean
    return (value.compareTo_AnyN_k_(m.__get_start()) >= 0) and (value.compareTo_AnyN_k_(m.__get_endExclusive()) < 0)
end function

function OpenEndRange_isEmpty_k_() as Boolean
    return m.__get_start().compareTo_AnyN_k_(m.__get_endExclusive()) >= 0
end function

function OpenEndRange___get_start_k_() as Object
end function

function OpenEndRange___get_endExclusive_k_() as Object
end function

function IntRange_create_I_I_k_(start as Integer, endInclusive as Integer) as Object
    this = IntProgression_create_I_I_I_k_(start, endInclusive, 1)
    this._super = {}
    this._super.contains_Any_k_ = this.contains_Any_k_
    this._super.isEmpty_k_ = this.isEmpty_k_
    this._super.equals_AnyN_k_ = this.equals_AnyN_k_
    this._super.hashCode_k_ = this.hashCode_k_
    this._super.toString_k_ = this.toString_k_
    this.__proto = ["IntRange", "ClosedRange", this.__proto]
    this.__type = "IntRange"
    this.contains_Any_k_ = IntRange_contains_Any_k_
    this.isEmpty_k_ = IntRange_isEmpty_k_
    this.equals_AnyN_k_ = IntRange_equals_AnyN_k_
    this.equals = IntRange_equals_AnyN_k_
    this.hashCode_k_ = IntRange_hashCode_k_
    this.hashCode = IntRange_hashCode_k_
    this.toString_k_ = IntRange_toString_k_
    this.toString = IntRange_toString_k_
    this.__get_start = IntRange___get_start_k_
    this.__get_endInclusive = IntRange___get_endInclusive_k_
    return this
end function

function IntRange_contains_Any_k_(value as Integer) as Boolean
    return (m.__get_first() <= value) and (value <= m.__get_last())
end function

function IntRange_isEmpty_k_() as Boolean
    return m.__get_first() > m.__get_last()
end function

function IntRange_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "IntRange") and ((m.isEmpty_k_() and other.isEmpty_k_()) or ((m.__get_first() = other.__get_first()) and (m.__get_last() = other.__get_last())))
end function

function IntRange_hashCode_k_() as Integer
    __when_tmp0 = invalid
    if m.isEmpty_k_() then
        __when_tmp0 = -1
    else if true then
        __when_tmp0 = ((31 * m.__get_first()) + m.__get_last())
    end if
    return __when_tmp0

end function

function IntRange_toString_k_() as String
    return (__kotlin_numToStr_I_k_(m.__get_first()) + "..") + __kotlin_numToStr_I_k_(m.__get_last())
end function

function IntRange___get_start_k_() as Integer
    return m.__get_first()
end function

function IntRange___get_endInclusive_k_() as Integer
    return m.__get_last()
end function

function IntRange_Companion_create_k_() as Object
    this = {}
    this.__type = "IntRange_Companion"
    this.__proto = ["IntRange_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_EMPTY = IntRange_Companion___get_EMPTY_k_
    this.EMPTY = IntRange_create_I_I_k_(1, 0)
    return this
end function

function IntRange_Companion_getInstance() as Object
    if GetGlobalAA().IntRange_Companion_instance = invalid then
        GetGlobalAA().IntRange_Companion_instance = IntRange_Companion_create_k_()
    end if
    return GetGlobalAA().IntRange_Companion_instance
end function

function IntRange_Companion___get_EMPTY_k_() as Object
    return m.EMPTY
end function

function LongRange_create_J_J_k_(start as LongInteger, endInclusive as LongInteger) as Object
    this = LongProgression_create_J_J_J_k_(start, endInclusive, 1&)
    this._super = {}
    this._super.contains_Any_k_ = this.contains_Any_k_
    this._super.isEmpty_k_ = this.isEmpty_k_
    this._super.equals_AnyN_k_ = this.equals_AnyN_k_
    this._super.hashCode_k_ = this.hashCode_k_
    this._super.toString_k_ = this.toString_k_
    this.__proto = ["LongRange", "ClosedRange", this.__proto]
    this.__type = "LongRange"
    this.contains_Any_k_ = LongRange_contains_Any_k_
    this.isEmpty_k_ = LongRange_isEmpty_k_
    this.equals_AnyN_k_ = LongRange_equals_AnyN_k_
    this.equals = LongRange_equals_AnyN_k_
    this.hashCode_k_ = LongRange_hashCode_k_
    this.hashCode = LongRange_hashCode_k_
    this.toString_k_ = LongRange_toString_k_
    this.toString = LongRange_toString_k_
    this.__get_start = LongRange___get_start_k_
    this.__get_endInclusive = LongRange___get_endInclusive_k_
    return this
end function

function LongRange_contains_Any_k_(value as LongInteger) as Boolean
    return (m.__get_first() <= value) and (value <= m.__get_last())
end function

function LongRange_isEmpty_k_() as Boolean
    return m.__get_first() > m.__get_last()
end function

function LongRange_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "LongRange") and ((m.isEmpty_k_() and other.isEmpty_k_()) or ((m.__get_first() = other.__get_first()) and (m.__get_last() = other.__get_last())))
end function

function LongRange_hashCode_k_() as Integer
    __when_tmp1 = invalid
    if m.isEmpty_k_() then
        __when_tmp1 = -1
    else if true then
        __when_tmp1 = ((31 * ((m.__get_first() or __kotlin_ushr(m.__get_first(), 32)) and not (m.__get_first() and __kotlin_ushr(m.__get_first(), 32)))) + ((m.__get_last() or __kotlin_ushr(m.__get_last(), 32)) and not (m.__get_last() and __kotlin_ushr(m.__get_last(), 32))))
    end if
    return __when_tmp1

end function

function LongRange_toString_k_() as String
    return (__kotlin_numToStr_J_k_(m.__get_first()) + "..") + __kotlin_numToStr_J_k_(m.__get_last())
end function

function LongRange___get_start_k_() as LongInteger
    return m.__get_first()
end function

function LongRange___get_endInclusive_k_() as LongInteger
    return m.__get_last()
end function

function LongRange_Companion_create_k_() as Object
    this = {}
    this.__type = "LongRange_Companion"
    this.__proto = ["LongRange_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_EMPTY = LongRange_Companion___get_EMPTY_k_
    this.EMPTY = LongRange_create_J_J_k_(1&, 0&)
    return this
end function

function LongRange_Companion_getInstance() as Object
    if GetGlobalAA().LongRange_Companion_instance = invalid then
        GetGlobalAA().LongRange_Companion_instance = LongRange_Companion_create_k_()
    end if
    return GetGlobalAA().LongRange_Companion_instance
end function

function LongRange_Companion___get_EMPTY_k_() as Object
    return m.EMPTY
end function

function CharRange_create_C_C_k_(start as Object, endInclusive as Object) as Object
    this = CharProgression_create_C_C_I_k_(start, endInclusive, 1)
    this._super = {}
    this._super.contains_Any_k_ = this.contains_Any_k_
    this._super.isEmpty_k_ = this.isEmpty_k_
    this._super.equals_AnyN_k_ = this.equals_AnyN_k_
    this._super.hashCode_k_ = this.hashCode_k_
    this._super.toString_k_ = this.toString_k_
    this.__proto = ["CharRange", "ClosedRange", this.__proto]
    this.__type = "CharRange"
    this.contains_Any_k_ = CharRange_contains_Any_k_
    this.isEmpty_k_ = CharRange_isEmpty_k_
    this.equals_AnyN_k_ = CharRange_equals_AnyN_k_
    this.equals = CharRange_equals_AnyN_k_
    this.hashCode_k_ = CharRange_hashCode_k_
    this.hashCode = CharRange_hashCode_k_
    this.toString_k_ = CharRange_toString_k_
    this.toString = CharRange_toString_k_
    this.__get_start = CharRange___get_start_k_
    this.__get_endInclusive = CharRange___get_endInclusive_k_
    return this
end function

function CharRange_contains_Any_k_(value as Object) as Boolean
    return (__kotlin_stringCompare(m.__get_first(), value) <= 0) and (__kotlin_stringCompare(value, m.__get_last()) <= 0)
end function

function CharRange_isEmpty_k_() as Boolean
    return __kotlin_stringCompare(m.__get_first(), m.__get_last()) > 0
end function

function CharRange_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "CharRange") and ((m.isEmpty_k_() and other.isEmpty_k_()) or ((m.__get_first() = other.__get_first()) and (m.__get_last() = other.__get_last())))
end function

function CharRange_hashCode_k_() as Integer
    __when_tmp2 = invalid
    if m.isEmpty_k_() then
        __when_tmp2 = -1
    else if true then
        __when_tmp2 = ((31 * __get_code_rC_k_(m.__get_first())) + __get_code_rC_k_(m.__get_last()))
    end if
    return __when_tmp2

end function

function CharRange_toString_k_() as String
    return (m.__get_first() + "..") + m.__get_last()
end function

function CharRange___get_start_k_() as Object
    return m.__get_first()
end function

function CharRange___get_endInclusive_k_() as Object
    return m.__get_last()
end function

function CharRange_Companion_create_k_() as Object
    this = {}
    this.__type = "CharRange_Companion"
    this.__proto = ["CharRange_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_EMPTY = CharRange_Companion___get_EMPTY_k_
    this.EMPTY = CharRange_create_C_C_k_(Chr(1), Chr(0))
    return this
end function

function CharRange_Companion_getInstance() as Object
    if GetGlobalAA().CharRange_Companion_instance = invalid then
        GetGlobalAA().CharRange_Companion_instance = CharRange_Companion_create_k_()
    end if
    return GetGlobalAA().CharRange_Companion_instance
end function

function CharRange_Companion___get_EMPTY_k_() as Object
    return m.EMPTY
end function

function IntProgression_create_I_I_I_k_(start as Integer, endInclusive as Integer, step_ as Integer) as Object
    this = {}
    this.__type = "IntProgression"
    this.__proto = ["IntProgression", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = IntProgression_iterator_k_
    this.isEmpty_k_ = IntProgression_isEmpty_k_
    this.equals_AnyN_k_ = IntProgression_equals_AnyN_k_
    this.equals = IntProgression_equals_AnyN_k_
    this.hashCode_k_ = IntProgression_hashCode_k_
    this.hashCode = IntProgression_hashCode_k_
    this.toString_k_ = IntProgression_toString_k_
    this.toString = IntProgression_toString_k_
    this.__get_first = IntProgression___get_first_k_
    this.__get_last = IntProgression___get_last_k_
    this.__get_step = IntProgression___get_step_k_
    this.first = start
    this.last = getProgressionLastElement_I_I_I_k_(start, endInclusive, step_)
    this.step = step_
    return this
end function

function IntProgression_iterator_k_() as Object
    return IntProgressionIterator_create_I_I_I_k_(m.__get_first(), m.__get_last(), m.__get_step())
end function

function IntProgression_isEmpty_k_() as Boolean
    __when_tmp3 = invalid
    if m.__get_step() > 0 then
        __when_tmp3 = (m.__get_first() > m.__get_last())
    else if true then
        __when_tmp3 = (m.__get_first() < m.__get_last())
    end if
    return __when_tmp3

end function

function IntProgression_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "IntProgression") and ((m.isEmpty_k_() and other.isEmpty_k_()) or (((m.__get_first() = other.__get_first()) and (m.__get_last() = other.__get_last())) and (m.__get_step() = other.__get_step())))
end function

function IntProgression_hashCode_k_() as Integer
    __when_tmp4 = invalid
    if m.isEmpty_k_() then
        __when_tmp4 = -1
    else if true then
        __when_tmp4 = ((31 * ((31 * m.__get_first()) + m.__get_last())) + m.__get_step())
    end if
    return __when_tmp4

end function

function IntProgression_toString_k_() as String
    __when_tmp5 = invalid
    if m.__get_step() > 0 then
        __when_tmp5 = ((((__kotlin_numToStr_I_k_(m.__get_first()) + "..") + __kotlin_numToStr_I_k_(m.__get_last())) + " step ") + __kotlin_numToStr_I_k_(m.__get_step()))
    else if true then
        __when_tmp5 = ((((__kotlin_numToStr_I_k_(m.__get_first()) + " downTo ") + __kotlin_numToStr_I_k_(m.__get_last())) + " step ") + __kotlin_numToStr_I_k_(-m.__get_step()))
    end if
    return __when_tmp5

end function

function IntProgression___get_first_k_() as Integer
    return m.first
end function

function IntProgression___get_last_k_() as Integer
    return m.last
end function

function IntProgression___get_step_k_() as Integer
    return m.step
end function

function IntProgression_Companion_create_k_() as Object
    this = {}
    this.__type = "IntProgression_Companion"
    this.__proto = ["IntProgression_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.fromClosedRange_I_I_I_k_ = IntProgression_Companion_fromClosedRange_I_I_I_k_
    return this
end function

function IntProgression_Companion_getInstance() as Object
    if GetGlobalAA().IntProgression_Companion_instance = invalid then
        GetGlobalAA().IntProgression_Companion_instance = IntProgression_Companion_create_k_()
    end if
    return GetGlobalAA().IntProgression_Companion_instance
end function

function IntProgression_Companion_fromClosedRange_I_I_I_k_(rangeStart as Integer, rangeEnd as Integer, step_ as Integer) as Object
    return IntProgression_create_I_I_I_k_(rangeStart, rangeEnd, step_)
end function

function LongProgression_create_J_J_J_k_(start as LongInteger, endInclusive as LongInteger, step_ as LongInteger) as Object
    this = {}
    this.__type = "LongProgression"
    this.__proto = ["LongProgression", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = LongProgression_iterator_k_
    this.isEmpty_k_ = LongProgression_isEmpty_k_
    this.equals_AnyN_k_ = LongProgression_equals_AnyN_k_
    this.equals = LongProgression_equals_AnyN_k_
    this.hashCode_k_ = LongProgression_hashCode_k_
    this.hashCode = LongProgression_hashCode_k_
    this.toString_k_ = LongProgression_toString_k_
    this.toString = LongProgression_toString_k_
    this.__get_first = LongProgression___get_first_k_
    this.__get_last = LongProgression___get_last_k_
    this.__get_step = LongProgression___get_step_k_
    this.first = start
    this.last = getProgressionLastElement_J_J_J_k_(start, endInclusive, step_)
    this.step = step_
    return this
end function

function LongProgression_iterator_k_() as Object
    return LongProgressionIterator_create_J_J_J_k_(m.__get_first(), m.__get_last(), m.__get_step())
end function

function LongProgression_isEmpty_k_() as Boolean
    __when_tmp6 = invalid
    if m.__get_step() > 0 then
        __when_tmp6 = (m.__get_first() > m.__get_last())
    else if true then
        __when_tmp6 = (m.__get_first() < m.__get_last())
    end if
    return __when_tmp6

end function

function LongProgression_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "LongProgression") and ((m.isEmpty_k_() and other.isEmpty_k_()) or (((m.__get_first() = other.__get_first()) and (m.__get_last() = other.__get_last())) and (m.__get_step() = other.__get_step())))
end function

function LongProgression_hashCode_k_() as Integer
    __when_tmp7 = invalid
    if m.isEmpty_k_() then
        __when_tmp7 = -1
    else if true then
        __when_tmp7 = ((31 * ((31 * ((m.__get_first() or __kotlin_ushr(m.__get_first(), 32)) and not (m.__get_first() and __kotlin_ushr(m.__get_first(), 32)))) + ((m.__get_last() or __kotlin_ushr(m.__get_last(), 32)) and not (m.__get_last() and __kotlin_ushr(m.__get_last(), 32))))) + ((m.__get_step() or __kotlin_ushr(m.__get_step(), 32)) and not (m.__get_step() and __kotlin_ushr(m.__get_step(), 32))))
    end if
    return __when_tmp7

end function

function LongProgression_toString_k_() as String
    __when_tmp8 = invalid
    if m.__get_step() > 0 then
        __when_tmp8 = ((((__kotlin_numToStr_J_k_(m.__get_first()) + "..") + __kotlin_numToStr_J_k_(m.__get_last())) + " step ") + __kotlin_numToStr_J_k_(m.__get_step()))
    else if true then
        __when_tmp8 = ((((__kotlin_numToStr_J_k_(m.__get_first()) + " downTo ") + __kotlin_numToStr_J_k_(m.__get_last())) + " step ") + __kotlin_numToStr_J_k_(-m.__get_step()))
    end if
    return __when_tmp8

end function

function LongProgression___get_first_k_() as LongInteger
    return m.first
end function

function LongProgression___get_last_k_() as LongInteger
    return m.last
end function

function LongProgression___get_step_k_() as LongInteger
    return m.step
end function

function LongProgression_Companion_create_k_() as Object
    this = {}
    this.__type = "LongProgression_Companion"
    this.__proto = ["LongProgression_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.fromClosedRange_J_J_J_k_ = LongProgression_Companion_fromClosedRange_J_J_J_k_
    return this
end function

function LongProgression_Companion_getInstance() as Object
    if GetGlobalAA().LongProgression_Companion_instance = invalid then
        GetGlobalAA().LongProgression_Companion_instance = LongProgression_Companion_create_k_()
    end if
    return GetGlobalAA().LongProgression_Companion_instance
end function

function LongProgression_Companion_fromClosedRange_J_J_J_k_(rangeStart as LongInteger, rangeEnd as LongInteger, step_ as LongInteger) as Object
    return LongProgression_create_J_J_J_k_(rangeStart, rangeEnd, step_)
end function

function CharProgression_create_C_C_I_k_(start as Object, endInclusive as Object, step_ as Integer) as Object
    this = {}
    this.__type = "CharProgression"
    this.__proto = ["CharProgression", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = CharProgression_iterator_k_
    this.isEmpty_k_ = CharProgression_isEmpty_k_
    this.equals_AnyN_k_ = CharProgression_equals_AnyN_k_
    this.equals = CharProgression_equals_AnyN_k_
    this.hashCode_k_ = CharProgression_hashCode_k_
    this.hashCode = CharProgression_hashCode_k_
    this.toString_k_ = CharProgression_toString_k_
    this.toString = CharProgression_toString_k_
    this.__get_first = CharProgression___get_first_k_
    this.__get_last = CharProgression___get_last_k_
    this.__get_step = CharProgression___get_step_k_
    this.first = start
    this.last = Chr(getProgressionLastElement_I_I_I_k_(__get_code_rC_k_(start), __get_code_rC_k_(endInclusive), step_))
    this.step = step_
    return this
end function

function CharProgression_iterator_k_() as Object
    return CharProgressionIterator_create_C_C_I_k_(m.__get_first(), m.__get_last(), m.__get_step())
end function

function CharProgression_isEmpty_k_() as Boolean
    __when_tmp9 = invalid
    if m.__get_step() > 0 then
        __when_tmp9 = (__kotlin_stringCompare(m.__get_first(), m.__get_last()) > 0)
    else if true then
        __when_tmp9 = (__kotlin_stringCompare(m.__get_first(), m.__get_last()) < 0)
    end if
    return __when_tmp9

end function

function CharProgression_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "CharProgression") and ((m.isEmpty_k_() and other.isEmpty_k_()) or (((m.__get_first() = other.__get_first()) and (m.__get_last() = other.__get_last())) and (m.__get_step() = other.__get_step())))
end function

function CharProgression_hashCode_k_() as Integer
    __when_tmp10 = invalid
    if m.isEmpty_k_() then
        __when_tmp10 = -1
    else if true then
        __when_tmp10 = ((31 * ((31 * __get_code_rC_k_(m.__get_first())) + __get_code_rC_k_(m.__get_last()))) + m.__get_step())
    end if
    return __when_tmp10

end function

function CharProgression_toString_k_() as String
    __when_tmp11 = invalid
    if m.__get_step() > 0 then
        __when_tmp11 = ((((m.__get_first() + "..") + m.__get_last()) + " step ") + __kotlin_numToStr_I_k_(m.__get_step()))
    else if true then
        __when_tmp11 = ((((m.__get_first() + " downTo ") + m.__get_last()) + " step ") + __kotlin_numToStr_I_k_(-m.__get_step()))
    end if
    return __when_tmp11

end function

function CharProgression___get_first_k_() as Object
    return m.first
end function

function CharProgression___get_last_k_() as Object
    return m.last
end function

function CharProgression___get_step_k_() as Integer
    return m.step
end function

function CharProgression_Companion_create_k_() as Object
    this = {}
    this.__type = "CharProgression_Companion"
    this.__proto = ["CharProgression_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.fromClosedRange_C_C_I_k_ = CharProgression_Companion_fromClosedRange_C_C_I_k_
    return this
end function

function CharProgression_Companion_getInstance() as Object
    if GetGlobalAA().CharProgression_Companion_instance = invalid then
        GetGlobalAA().CharProgression_Companion_instance = CharProgression_Companion_create_k_()
    end if
    return GetGlobalAA().CharProgression_Companion_instance
end function

function CharProgression_Companion_fromClosedRange_C_C_I_k_(rangeStart as Object, rangeEnd as Object, step_ as Integer) as Object
    return CharProgression_create_C_C_I_k_(rangeStart, rangeEnd, step_)
end function

function IntProgressionIterator_create_I_I_I_k_(first as Integer, last as Integer, step_ as Integer) as Object
    this = IntIterator_create_k_()
    this._super = {}
    this._super.hasNext_k_ = this.hasNext_k_
    this._super.nextInt_k_ = this.nextInt_k_
    this.__proto = ["IntProgressionIterator", this.__proto]
    this.__type = "IntProgressionIterator"
    this.hasNext_k_ = IntProgressionIterator_hasNext_k_
    this.nextInt_k_ = IntProgressionIterator_nextInt_k_
    this.__get_step = IntProgressionIterator___get_step_k_
    this.__get_finalElement = IntProgressionIterator___get_finalElement_k_
    this.__get_hasNext = IntProgressionIterator___get_hasNext_k_
    this.__set_hasNext = IntProgressionIterator___set_hasNext_Z_k_
    this.__get_next = IntProgressionIterator___get_next_k_
    this.__set_next = IntProgressionIterator___set_next_I_k_
    this.step = step_
    this.finalElement = last
    __when_tmp12 = invalid
    if this.__get_step() > 0 then
        __when_tmp12 = (first <= last)
    else if true then
        __when_tmp12 = (first >= last)
    end if
    this.hasNext = __when_tmp12
    __when_tmp13 = invalid
    if this.__get_hasNext() then
        __when_tmp13 = first
    else if true then
        __when_tmp13 = this.__get_finalElement()
    end if
    this.next = __when_tmp13
    return this
end function

function IntProgressionIterator_hasNext_k_() as Boolean
    return m.__get_hasNext()
end function

function IntProgressionIterator_nextInt_k_() as Integer
    value = m.__get_next()
    if value = m.__get_finalElement() then
        if not m.__get_hasNext() then
            throw NoSuchElementException_create_k_()
        end if
        m.__set_hasNext(false)
    else if true then
        m.__set_next(m.__get_next() + m.__get_step())
    end if
    return value
end function

function IntProgressionIterator___get_step_k_() as Integer
    return m.step
end function

function IntProgressionIterator___get_finalElement_k_() as Integer
    return m.finalElement
end function

function IntProgressionIterator___get_hasNext_k_() as Boolean
    return m.hasNext
end function

sub IntProgressionIterator___set_hasNext_Z_k_(value as Boolean)
    m.hasNext = value
end sub

function IntProgressionIterator___get_next_k_() as Integer
    return m.next
end function

sub IntProgressionIterator___set_next_I_k_(value as Integer)
    m.next = value
end sub

function LongProgressionIterator_create_J_J_J_k_(first as LongInteger, last as LongInteger, step_ as LongInteger) as Object
    this = LongIterator_create_k_()
    this._super = {}
    this._super.hasNext_k_ = this.hasNext_k_
    this._super.nextLong_k_ = this.nextLong_k_
    this.__proto = ["LongProgressionIterator", this.__proto]
    this.__type = "LongProgressionIterator"
    this.hasNext_k_ = LongProgressionIterator_hasNext_k_
    this.nextLong_k_ = LongProgressionIterator_nextLong_k_
    this.__get_step = LongProgressionIterator___get_step_k_
    this.__get_finalElement = LongProgressionIterator___get_finalElement_k_
    this.__get_hasNext = LongProgressionIterator___get_hasNext_k_
    this.__set_hasNext = LongProgressionIterator___set_hasNext_Z_k_
    this.__get_next = LongProgressionIterator___get_next_k_
    this.__set_next = LongProgressionIterator___set_next_J_k_
    this.step = step_
    this.finalElement = last
    __when_tmp14 = invalid
    if this.__get_step() > 0 then
        __when_tmp14 = (first <= last)
    else if true then
        __when_tmp14 = (first >= last)
    end if
    this.hasNext = __when_tmp14
    __when_tmp15 = invalid
    if this.__get_hasNext() then
        __when_tmp15 = first
    else if true then
        __when_tmp15 = this.__get_finalElement()
    end if
    this.next = __when_tmp15
    return this
end function

function LongProgressionIterator_hasNext_k_() as Boolean
    return m.__get_hasNext()
end function

function LongProgressionIterator_nextLong_k_() as LongInteger
    value = m.__get_next()
    if value = m.__get_finalElement() then
        if not m.__get_hasNext() then
            throw NoSuchElementException_create_k_()
        end if
        m.__set_hasNext(false)
    else if true then
        m.__set_next(m.__get_next() + m.__get_step())
    end if
    return value
end function

function LongProgressionIterator___get_step_k_() as LongInteger
    return m.step
end function

function LongProgressionIterator___get_finalElement_k_() as LongInteger
    return m.finalElement
end function

function LongProgressionIterator___get_hasNext_k_() as Boolean
    return m.hasNext
end function

sub LongProgressionIterator___set_hasNext_Z_k_(value as Boolean)
    m.hasNext = value
end sub

function LongProgressionIterator___get_next_k_() as LongInteger
    return m.next
end function

sub LongProgressionIterator___set_next_J_k_(value as LongInteger)
    m.next = value
end sub

function CharProgressionIterator_create_C_C_I_k_(first as Object, last as Object, step_ as Integer) as Object
    this = CharIterator_create_k_()
    this._super = {}
    this._super.hasNext_k_ = this.hasNext_k_
    this._super.nextChar_k_ = this.nextChar_k_
    this.__proto = ["CharProgressionIterator", this.__proto]
    this.__type = "CharProgressionIterator"
    this.hasNext_k_ = CharProgressionIterator_hasNext_k_
    this.nextChar_k_ = CharProgressionIterator_nextChar_k_
    this.__get_step = CharProgressionIterator___get_step_k_
    this.__get_finalElement = CharProgressionIterator___get_finalElement_k_
    this.__get_hasNext = CharProgressionIterator___get_hasNext_k_
    this.__set_hasNext = CharProgressionIterator___set_hasNext_Z_k_
    this.__get_next = CharProgressionIterator___get_next_k_
    this.__set_next = CharProgressionIterator___set_next_I_k_
    this.step = step_
    this.finalElement = __get_code_rC_k_(last)
    __when_tmp16 = invalid
    if this.__get_step() > 0 then
        __when_tmp16 = (__kotlin_stringCompare(first, last) <= 0)
    else if true then
        __when_tmp16 = (__kotlin_stringCompare(first, last) >= 0)
    end if
    this.hasNext = __when_tmp16
    __when_tmp17 = invalid
    if this.__get_hasNext() then
        __when_tmp17 = __get_code_rC_k_(first)
    else if true then
        __when_tmp17 = this.__get_finalElement()
    end if
    this.next = __when_tmp17
    return this
end function

function CharProgressionIterator_hasNext_k_() as Boolean
    return m.__get_hasNext()
end function

function CharProgressionIterator_nextChar_k_() as Object
    value = m.__get_next()
    if value = m.__get_finalElement() then
        if not m.__get_hasNext() then
            throw NoSuchElementException_create_k_()
        end if
        m.__set_hasNext(false)
    else if true then
        m.__set_next(m.__get_next() + m.__get_step())
    end if
    return Chr(value)
end function

function CharProgressionIterator___get_step_k_() as Integer
    return m.step
end function

function CharProgressionIterator___get_finalElement_k_() as Integer
    return m.finalElement
end function

function CharProgressionIterator___get_hasNext_k_() as Boolean
    return m.hasNext
end function

sub CharProgressionIterator___set_hasNext_Z_k_(value as Boolean)
    m.hasNext = value
end sub

function CharProgressionIterator___get_next_k_() as Integer
    return m.next
end function

sub CharProgressionIterator___set_next_I_k_(value as Integer)
    m.next = value
end sub

function getProgressionLastElement_I_I_I_k_(start as Integer, end_ as Integer, step_ as Integer) as Integer
    __when_tmp20 = invalid
    if step_ > 0 then
        __when_tmp18 = invalid
        if start >= end_ then
            __when_tmp18 = end_
        else if true then
            __when_tmp18 = (end_ - differenceModulo_I_I_I_k_(end_, start, step_))
        end if
        __when_tmp20 = __when_tmp18
    else if step_ < 0 then
        __when_tmp19 = invalid
        if start <= end_ then
            __when_tmp19 = end_
        else if true then
            __when_tmp19 = (end_ + differenceModulo_I_I_I_k_(start, end_, -step_))
        end if
        __when_tmp20 = __when_tmp19
    else if true then
        throw IllegalArgumentException_create_StrN_k_("Step is zero.")
    end if
    return __when_tmp20

end function

function getProgressionLastElement_J_J_J_k_(start as LongInteger, end_ as LongInteger, step_ as LongInteger) as LongInteger
    __when_tmp23 = invalid
    if step_ > 0 then
        __when_tmp21 = invalid
        if start >= end_ then
            __when_tmp21 = end_
        else if true then
            __when_tmp21 = (end_ - differenceModulo_J_J_J_k_(end_, start, step_))
        end if
        __when_tmp23 = __when_tmp21
    else if step_ < 0 then
        __when_tmp22 = invalid
        if start <= end_ then
            __when_tmp22 = end_
        else if true then
            __when_tmp22 = (end_ + differenceModulo_J_J_J_k_(start, end_, -step_))
        end if
        __when_tmp23 = __when_tmp22
    else if true then
        throw IllegalArgumentException_create_StrN_k_("Step is zero.")
    end if
    return __when_tmp23

end function

function differenceModulo_I_I_I_k_(a as Integer, b as Integer, c as Integer) as Integer
    return mod_I_I_k_(mod_I_I_k_(a, c) - mod_I_I_k_(b, c), c)
end function

function differenceModulo_J_J_J_k_(a as LongInteger, b as LongInteger, c as LongInteger) as LongInteger
    return mod_J_J_k_(mod_J_J_k_(a, c) - mod_J_J_k_(b, c), c)
end function

function mod_I_I_k_(a as Integer, b as Integer) as Integer
    mod_ = a mod b
    __when_tmp24 = invalid
    if mod_ >= 0 then
        __when_tmp24 = mod_
    else if true then
        __when_tmp24 = (mod_ + b)
    end if
    return __when_tmp24

end function

function mod_J_J_k_(a as LongInteger, b as LongInteger) as LongInteger
    mod_ = a mod b
    __when_tmp25 = invalid
    if mod_ >= 0 then
        __when_tmp25 = mod_
    else if true then
        __when_tmp25 = (mod_ + b)
    end if
    return __when_tmp25

end function
