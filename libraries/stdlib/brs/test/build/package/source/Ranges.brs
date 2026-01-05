function ClosedRange_contains_Any_k_(value as Object) as Boolean
    return (value.compareTo(m.get_start()) >= 0) and (value.compareTo(m.get_endInclusive()) <= 0)
end function

function ClosedRange_isEmpty_k_() as Boolean
    return m.get_start().compareTo(m.get_endInclusive()) > 0
end function

function ClosedRange_get_start_k_() as Object
end function

function ClosedRange_get_endInclusive_k_() as Object
end function

function OpenEndRange_contains_Any_k_(value as Object) as Boolean
    return (value.compareTo(m.get_start()) >= 0) and (value.compareTo(m.get_endExclusive()) < 0)
end function

function OpenEndRange_isEmpty_k_() as Boolean
    return m.get_start().compareTo(m.get_endExclusive()) >= 0
end function

function OpenEndRange_get_start_k_() as Object
end function

function OpenEndRange_get_endExclusive_k_() as Object
end function

function IntRange_create_I_I_k_(start as Integer, endInclusive as Integer) as Object
    this = IntProgression_create_I_I_I_k_(start, endInclusive, 1)
    this._super = {}
    this._super.contains_I_k_ = this.contains_I_k_
    this._super.isEmpty_k_ = this.isEmpty_k_
    this._super.equals_AnyN_k_ = this.equals_AnyN_k_
    this._super.hashCode_k_ = this.hashCode_k_
    this._super.toString_k_ = this.toString_k_
    this.__proto = ["IntRange", "ClosedRange", this.__proto]
    this.__type = "IntRange"
    this.contains_I_k_ = IntRange_contains_I_k_
    this.isEmpty_k_ = IntRange_isEmpty_k_
    this.equals_AnyN_k_ = IntRange_equals_AnyN_k_
    this.equals = IntRange_equals_AnyN_k_
    this.hashCode_k_ = IntRange_hashCode_k_
    this.hashCode = IntRange_hashCode_k_
    this.toString_k_ = IntRange_toString_k_
    this.toString = IntRange_toString_k_
    this.get_start = IntRange_get_start_k_
    this.get_endInclusive = IntRange_get_endInclusive_k_
    return this
end function

function IntRange_contains_I_k_(value as Integer) as Boolean
    return (m.get_first() <= value) and (value <= m.get_last())
end function

function IntRange_isEmpty_k_() as Boolean
    return m.get_first() > m.get_last()
end function

function IntRange_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "IntRange") and ((m.isEmpty_k_() and other.isEmpty_k_()) or ((m.get_first() = other.get_first()) and (m.get_last() = other.get_last())))
end function

function IntRange_hashCode_k_() as Integer
    __when_tmp0 = invalid
    if m.isEmpty_k_() then
        __when_tmp0 = -1
    else if true then
        __when_tmp0 = ((31 * m.get_first()) + m.get_last())
    end if
    return __when_tmp0

end function

function IntRange_toString_k_() as String
    return (__kotlin_numToStr_I_k_(m.get_first()) + "..") + __kotlin_numToStr_I_k_(m.get_last())
end function

function IntRange_get_start_k_() as Integer
    return m.get_first()
end function

function IntRange_get_endInclusive_k_() as Integer
    return m.get_last()
end function

function IntRange_Companion_create_k_() as Object
    this = {}
    this.__type = "IntRange_Companion"
    this.__proto = ["IntRange_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.get_EMPTY = IntRange_Companion_get_EMPTY_k_
    this.EMPTY = IntRange_create_I_I_k_(1, 0)
    return this
end function

function IntRange_Companion_getInstance() as Object
    if m.IntRange_Companion_instance = invalid then
        m.IntRange_Companion_instance = IntRange_Companion_create_k_()
    end if
    return m.IntRange_Companion_instance
end function

function IntRange_Companion_get_EMPTY_k_() as Object
    return m.EMPTY
end function

function LongRange_create_J_J_k_(start as LongInteger, endInclusive as LongInteger) as Object
    this = LongProgression_create_J_J_J_k_(start, endInclusive, 1&)
    this._super = {}
    this._super.contains_J_k_ = this.contains_J_k_
    this._super.isEmpty_k_ = this.isEmpty_k_
    this._super.equals_AnyN_k_ = this.equals_AnyN_k_
    this._super.hashCode_k_ = this.hashCode_k_
    this._super.toString_k_ = this.toString_k_
    this.__proto = ["LongRange", "ClosedRange", this.__proto]
    this.__type = "LongRange"
    this.contains_J_k_ = LongRange_contains_J_k_
    this.isEmpty_k_ = LongRange_isEmpty_k_
    this.equals_AnyN_k_ = LongRange_equals_AnyN_k_
    this.equals = LongRange_equals_AnyN_k_
    this.hashCode_k_ = LongRange_hashCode_k_
    this.hashCode = LongRange_hashCode_k_
    this.toString_k_ = LongRange_toString_k_
    this.toString = LongRange_toString_k_
    this.get_start = LongRange_get_start_k_
    this.get_endInclusive = LongRange_get_endInclusive_k_
    return this
end function

function LongRange_contains_J_k_(value as LongInteger) as Boolean
    return (m.get_first() <= value) and (value <= m.get_last())
end function

function LongRange_isEmpty_k_() as Boolean
    return m.get_first() > m.get_last()
end function

function LongRange_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "LongRange") and ((m.isEmpty_k_() and other.isEmpty_k_()) or ((m.get_first() = other.get_first()) and (m.get_last() = other.get_last())))
end function

function LongRange_hashCode_k_() as Integer
    __when_tmp1 = invalid
    if m.isEmpty_k_() then
        __when_tmp1 = -1
    else if true then
        __when_tmp1 = ((31 * ((m.get_first() or __kotlin_ushr(m.get_first(), 32)) and not (m.get_first() and __kotlin_ushr(m.get_first(), 32)))) + ((m.get_last() or __kotlin_ushr(m.get_last(), 32)) and not (m.get_last() and __kotlin_ushr(m.get_last(), 32))))
    end if
    return __when_tmp1

end function

function LongRange_toString_k_() as String
    return (__kotlin_numToStr_J_k_(m.get_first()) + "..") + __kotlin_numToStr_J_k_(m.get_last())
end function

function LongRange_get_start_k_() as LongInteger
    return m.get_first()
end function

function LongRange_get_endInclusive_k_() as LongInteger
    return m.get_last()
end function

function LongRange_Companion_create_k_() as Object
    this = {}
    this.__type = "LongRange_Companion"
    this.__proto = ["LongRange_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.get_EMPTY = LongRange_Companion_get_EMPTY_k_
    this.EMPTY = LongRange_create_J_J_k_(1&, 0&)
    return this
end function

function LongRange_Companion_getInstance() as Object
    if m.LongRange_Companion_instance = invalid then
        m.LongRange_Companion_instance = LongRange_Companion_create_k_()
    end if
    return m.LongRange_Companion_instance
end function

function LongRange_Companion_get_EMPTY_k_() as Object
    return m.EMPTY
end function

function CharRange_create_C_C_k_(start as Object, endInclusive as Object) as Object
    this = CharProgression_create_C_C_I_k_(start, endInclusive, 1)
    this._super = {}
    this._super.contains_C_k_ = this.contains_C_k_
    this._super.isEmpty_k_ = this.isEmpty_k_
    this._super.equals_AnyN_k_ = this.equals_AnyN_k_
    this._super.hashCode_k_ = this.hashCode_k_
    this._super.toString_k_ = this.toString_k_
    this.__proto = ["CharRange", "ClosedRange", this.__proto]
    this.__type = "CharRange"
    this.contains_C_k_ = CharRange_contains_C_k_
    this.isEmpty_k_ = CharRange_isEmpty_k_
    this.equals_AnyN_k_ = CharRange_equals_AnyN_k_
    this.equals = CharRange_equals_AnyN_k_
    this.hashCode_k_ = CharRange_hashCode_k_
    this.hashCode = CharRange_hashCode_k_
    this.toString_k_ = CharRange_toString_k_
    this.toString = CharRange_toString_k_
    this.get_start = CharRange_get_start_k_
    this.get_endInclusive = CharRange_get_endInclusive_k_
    return this
end function

function CharRange_contains_C_k_(value as Object) as Boolean
    return ((m.get_first() <= value) <= 0) and ((value <= m.get_last()) <= 0)
end function

function CharRange_isEmpty_k_() as Boolean
    return (m.get_first() > m.get_last()) > 0
end function

function CharRange_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "CharRange") and ((m.isEmpty_k_() and other.isEmpty_k_()) or ((m.get_first() = other.get_first()) and (m.get_last() = other.get_last())))
end function

function CharRange_hashCode_k_() as Integer
    __when_tmp2 = invalid
    if m.isEmpty_k_() then
        __when_tmp2 = -1
    else if true then
        __when_tmp2 = ((31 * get_code_rC_k_(m.get_first())) + get_code_rC_k_(m.get_last()))
    end if
    return __when_tmp2

end function

function CharRange_toString_k_() as String
    return (m.get_first().toString() + "..") + m.get_last().toString()
end function

function CharRange_get_start_k_() as Object
    return m.get_first()
end function

function CharRange_get_endInclusive_k_() as Object
    return m.get_last()
end function

function CharRange_Companion_create_k_() as Object
    this = {}
    this.__type = "CharRange_Companion"
    this.__proto = ["CharRange_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.get_EMPTY = CharRange_Companion_get_EMPTY_k_
    this.EMPTY = CharRange_create_C_C_k_(1, 0)
    return this
end function

function CharRange_Companion_getInstance() as Object
    if m.CharRange_Companion_instance = invalid then
        m.CharRange_Companion_instance = CharRange_Companion_create_k_()
    end if
    return m.CharRange_Companion_instance
end function

function CharRange_Companion_get_EMPTY_k_() as Object
    return m.EMPTY
end function

function IntProgression_create_I_I_I_k_(start as Integer, endInclusive as Integer, step_ as Integer) as Object
    this = {}
    this.__type = "IntProgression"
    this.__proto = ["IntProgression", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = IntProgression_iterator_k_
    this.isEmpty_k_ = IntProgression_isEmpty_k_
    this.equals_AnyN_k_ = IntProgression_equals_AnyN_k_
    this.equals = IntProgression_equals_AnyN_k_
    this.hashCode_k_ = IntProgression_hashCode_k_
    this.hashCode = IntProgression_hashCode_k_
    this.toString_k_ = IntProgression_toString_k_
    this.toString = IntProgression_toString_k_
    this.get_first = IntProgression_get_first_k_
    this.get_last = IntProgression_get_last_k_
    this.get_step = IntProgression_get_step_k_
    this.first = start
    this.last = getProgressionLastElement_I_I_I_k_(start, endInclusive, step_)
    this.step = step_
    return this
end function

function IntProgression_iterator_k_() as Object
    return IntProgressionIterator_create_I_I_I_k_(m.get_first(), m.get_last(), m.get_step())
end function

function IntProgression_isEmpty_k_() as Boolean
    __when_tmp3 = invalid
    if m.get_step() > 0 then
        __when_tmp3 = (m.get_first() > m.get_last())
    else if true then
        __when_tmp3 = (m.get_first() < m.get_last())
    end if
    return __when_tmp3

end function

function IntProgression_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "IntProgression") and ((m.isEmpty_k_() and other.isEmpty_k_()) or (((m.get_first() = other.get_first()) and (m.get_last() = other.get_last())) and (m.get_step() = other.get_step())))
end function

function IntProgression_hashCode_k_() as Integer
    __when_tmp4 = invalid
    if m.isEmpty_k_() then
        __when_tmp4 = -1
    else if true then
        __when_tmp4 = ((31 * ((31 * m.get_first()) + m.get_last())) + m.get_step())
    end if
    return __when_tmp4

end function

function IntProgression_toString_k_() as String
    __when_tmp5 = invalid
    if m.get_step() > 0 then
        __when_tmp5 = ((((__kotlin_numToStr_I_k_(m.get_first()) + "..") + __kotlin_numToStr_I_k_(m.get_last())) + " step ") + __kotlin_numToStr_I_k_(m.get_step()))
    else if true then
        __when_tmp5 = ((((__kotlin_numToStr_I_k_(m.get_first()) + " downTo ") + __kotlin_numToStr_I_k_(m.get_last())) + " step ") + __kotlin_numToStr_I_k_(-m.get_step()))
    end if
    return __when_tmp5

end function

function IntProgression_get_first_k_() as Integer
    return m.first
end function

function IntProgression_get_last_k_() as Integer
    return m.last
end function

function IntProgression_get_step_k_() as Integer
    return m.step
end function

function IntProgression_Companion_create_k_() as Object
    this = {}
    this.__type = "IntProgression_Companion"
    this.__proto = ["IntProgression_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.fromClosedRange_I_I_I_k_ = IntProgression_Companion_fromClosedRange_I_I_I_k_
    return this
end function

function IntProgression_Companion_getInstance() as Object
    if m.IntProgression_Companion_instance = invalid then
        m.IntProgression_Companion_instance = IntProgression_Companion_create_k_()
    end if
    return m.IntProgression_Companion_instance
end function

function IntProgression_Companion_fromClosedRange_I_I_I_k_(rangeStart as Integer, rangeEnd as Integer, step_ as Integer) as Object
    return IntProgression_create_I_I_I_k_(rangeStart, rangeEnd, step_)
end function

function LongProgression_create_J_J_J_k_(start as LongInteger, endInclusive as LongInteger, step_ as LongInteger) as Object
    this = {}
    this.__type = "LongProgression"
    this.__proto = ["LongProgression", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = LongProgression_iterator_k_
    this.isEmpty_k_ = LongProgression_isEmpty_k_
    this.equals_AnyN_k_ = LongProgression_equals_AnyN_k_
    this.equals = LongProgression_equals_AnyN_k_
    this.hashCode_k_ = LongProgression_hashCode_k_
    this.hashCode = LongProgression_hashCode_k_
    this.toString_k_ = LongProgression_toString_k_
    this.toString = LongProgression_toString_k_
    this.get_first = LongProgression_get_first_k_
    this.get_last = LongProgression_get_last_k_
    this.get_step = LongProgression_get_step_k_
    this.first = start
    this.last = getProgressionLastElement_J_J_J_k_(start, endInclusive, step_)
    this.step = step_
    return this
end function

function LongProgression_iterator_k_() as Object
    return LongProgressionIterator_create_J_J_J_k_(m.get_first(), m.get_last(), m.get_step())
end function

function LongProgression_isEmpty_k_() as Boolean
    __when_tmp6 = invalid
    if m.get_step() > 0 then
        __when_tmp6 = (m.get_first() > m.get_last())
    else if true then
        __when_tmp6 = (m.get_first() < m.get_last())
    end if
    return __when_tmp6

end function

function LongProgression_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "LongProgression") and ((m.isEmpty_k_() and other.isEmpty_k_()) or (((m.get_first() = other.get_first()) and (m.get_last() = other.get_last())) and (m.get_step() = other.get_step())))
end function

function LongProgression_hashCode_k_() as Integer
    __when_tmp7 = invalid
    if m.isEmpty_k_() then
        __when_tmp7 = -1
    else if true then
        __when_tmp7 = ((31 * ((31 * ((m.get_first() or __kotlin_ushr(m.get_first(), 32)) and not (m.get_first() and __kotlin_ushr(m.get_first(), 32)))) + ((m.get_last() or __kotlin_ushr(m.get_last(), 32)) and not (m.get_last() and __kotlin_ushr(m.get_last(), 32))))) + ((m.get_step() or __kotlin_ushr(m.get_step(), 32)) and not (m.get_step() and __kotlin_ushr(m.get_step(), 32))))
    end if
    return __when_tmp7

end function

function LongProgression_toString_k_() as String
    __when_tmp8 = invalid
    if m.get_step() > 0 then
        __when_tmp8 = ((((__kotlin_numToStr_J_k_(m.get_first()) + "..") + __kotlin_numToStr_J_k_(m.get_last())) + " step ") + __kotlin_numToStr_J_k_(m.get_step()))
    else if true then
        __when_tmp8 = ((((__kotlin_numToStr_J_k_(m.get_first()) + " downTo ") + __kotlin_numToStr_J_k_(m.get_last())) + " step ") + __kotlin_numToStr_J_k_(-m.get_step()))
    end if
    return __when_tmp8

end function

function LongProgression_get_first_k_() as LongInteger
    return m.first
end function

function LongProgression_get_last_k_() as LongInteger
    return m.last
end function

function LongProgression_get_step_k_() as LongInteger
    return m.step
end function

function LongProgression_Companion_create_k_() as Object
    this = {}
    this.__type = "LongProgression_Companion"
    this.__proto = ["LongProgression_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.fromClosedRange_J_J_J_k_ = LongProgression_Companion_fromClosedRange_J_J_J_k_
    return this
end function

function LongProgression_Companion_getInstance() as Object
    if m.LongProgression_Companion_instance = invalid then
        m.LongProgression_Companion_instance = LongProgression_Companion_create_k_()
    end if
    return m.LongProgression_Companion_instance
end function

function LongProgression_Companion_fromClosedRange_J_J_J_k_(rangeStart as LongInteger, rangeEnd as LongInteger, step_ as LongInteger) as Object
    return LongProgression_create_J_J_J_k_(rangeStart, rangeEnd, step_)
end function

function CharProgression_create_C_C_I_k_(start as Object, endInclusive as Object, step_ as Integer) as Object
    this = {}
    this.__type = "CharProgression"
    this.__proto = ["CharProgression", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = CharProgression_iterator_k_
    this.isEmpty_k_ = CharProgression_isEmpty_k_
    this.equals_AnyN_k_ = CharProgression_equals_AnyN_k_
    this.equals = CharProgression_equals_AnyN_k_
    this.hashCode_k_ = CharProgression_hashCode_k_
    this.hashCode = CharProgression_hashCode_k_
    this.toString_k_ = CharProgression_toString_k_
    this.toString = CharProgression_toString_k_
    this.get_first = CharProgression_get_first_k_
    this.get_last = CharProgression_get_last_k_
    this.get_step = CharProgression_get_step_k_
    this.first = start
    this.last = getProgressionLastElement_I_I_I_k_(get_code_rC_k_(start), get_code_rC_k_(endInclusive), step_)
    this.step = step_
    return this
end function

function CharProgression_iterator_k_() as Object
    return CharProgressionIterator_create_C_C_I_k_(m.get_first(), m.get_last(), m.get_step())
end function

function CharProgression_isEmpty_k_() as Boolean
    __when_tmp9 = invalid
    if m.get_step() > 0 then
        __when_tmp9 = ((m.get_first() > m.get_last()) > 0)
    else if true then
        __when_tmp9 = ((m.get_first() < m.get_last()) < 0)
    end if
    return __when_tmp9

end function

function CharProgression_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "CharProgression") and ((m.isEmpty_k_() and other.isEmpty_k_()) or (((m.get_first() = other.get_first()) and (m.get_last() = other.get_last())) and (m.get_step() = other.get_step())))
end function

function CharProgression_hashCode_k_() as Integer
    __when_tmp10 = invalid
    if m.isEmpty_k_() then
        __when_tmp10 = -1
    else if true then
        __when_tmp10 = ((31 * ((31 * get_code_rC_k_(m.get_first())) + get_code_rC_k_(m.get_last()))) + m.get_step())
    end if
    return __when_tmp10

end function

function CharProgression_toString_k_() as String
    __when_tmp11 = invalid
    if m.get_step() > 0 then
        __when_tmp11 = ((((m.get_first().toString() + "..") + m.get_last().toString()) + " step ") + __kotlin_numToStr_I_k_(m.get_step()))
    else if true then
        __when_tmp11 = ((((m.get_first().toString() + " downTo ") + m.get_last().toString()) + " step ") + __kotlin_numToStr_I_k_(-m.get_step()))
    end if
    return __when_tmp11

end function

function CharProgression_get_first_k_() as Object
    return m.first
end function

function CharProgression_get_last_k_() as Object
    return m.last
end function

function CharProgression_get_step_k_() as Integer
    return m.step
end function

function CharProgression_Companion_create_k_() as Object
    this = {}
    this.__type = "CharProgression_Companion"
    this.__proto = ["CharProgression_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.fromClosedRange_C_C_I_k_ = CharProgression_Companion_fromClosedRange_C_C_I_k_
    return this
end function

function CharProgression_Companion_getInstance() as Object
    if m.CharProgression_Companion_instance = invalid then
        m.CharProgression_Companion_instance = CharProgression_Companion_create_k_()
    end if
    return m.CharProgression_Companion_instance
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
    this.get_step = IntProgressionIterator_get_step_k_
    this.get_finalElement = IntProgressionIterator_get_finalElement_k_
    this.get_hasNext = IntProgressionIterator_get_hasNext_k_
    this.set_hasNext = IntProgressionIterator_set_hasNext_Z_k_
    this.get_next = IntProgressionIterator_get_next_k_
    this.set_next = IntProgressionIterator_set_next_I_k_
    this.step = step_
    this.finalElement = last
    __when_tmp12 = invalid
    if this.get_step() > 0 then
        __when_tmp12 = (first <= last)
    else if true then
        __when_tmp12 = (first >= last)
    end if
    this.hasNext = __when_tmp12
    __when_tmp13 = invalid
    if this.get_hasNext() then
        __when_tmp13 = first
    else if true then
        __when_tmp13 = this.get_finalElement()
    end if
    this.next = __when_tmp13
    return this
end function

function IntProgressionIterator_hasNext_k_() as Boolean
    return m.get_hasNext()
end function

function IntProgressionIterator_nextInt_k_() as Integer
    value = m.get_next()
    if value = m.get_finalElement() then
        if not m.get_hasNext() then
            throw NoSuchElementException_create_k_()
        end if
        m.set_hasNext(false)
    else if true then
        m.set_next(m.get_next() + m.get_step())
    end if
    return value
end function

function IntProgressionIterator_get_step_k_() as Integer
    return m.step
end function

function IntProgressionIterator_get_finalElement_k_() as Integer
    return m.finalElement
end function

function IntProgressionIterator_get_hasNext_k_() as Boolean
    return m.hasNext
end function

sub IntProgressionIterator_set_hasNext_Z_k_(value as Boolean)
    m.hasNext = value
end sub

function IntProgressionIterator_get_next_k_() as Integer
    return m.next
end function

sub IntProgressionIterator_set_next_I_k_(value as Integer)
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
    this.get_step = LongProgressionIterator_get_step_k_
    this.get_finalElement = LongProgressionIterator_get_finalElement_k_
    this.get_hasNext = LongProgressionIterator_get_hasNext_k_
    this.set_hasNext = LongProgressionIterator_set_hasNext_Z_k_
    this.get_next = LongProgressionIterator_get_next_k_
    this.set_next = LongProgressionIterator_set_next_J_k_
    this.step = step_
    this.finalElement = last
    __when_tmp14 = invalid
    if this.get_step() > 0 then
        __when_tmp14 = (first <= last)
    else if true then
        __when_tmp14 = (first >= last)
    end if
    this.hasNext = __when_tmp14
    __when_tmp15 = invalid
    if this.get_hasNext() then
        __when_tmp15 = first
    else if true then
        __when_tmp15 = this.get_finalElement()
    end if
    this.next = __when_tmp15
    return this
end function

function LongProgressionIterator_hasNext_k_() as Boolean
    return m.get_hasNext()
end function

function LongProgressionIterator_nextLong_k_() as LongInteger
    value = m.get_next()
    if value = m.get_finalElement() then
        if not m.get_hasNext() then
            throw NoSuchElementException_create_k_()
        end if
        m.set_hasNext(false)
    else if true then
        m.set_next(m.get_next() + m.get_step())
    end if
    return value
end function

function LongProgressionIterator_get_step_k_() as LongInteger
    return m.step
end function

function LongProgressionIterator_get_finalElement_k_() as LongInteger
    return m.finalElement
end function

function LongProgressionIterator_get_hasNext_k_() as Boolean
    return m.hasNext
end function

sub LongProgressionIterator_set_hasNext_Z_k_(value as Boolean)
    m.hasNext = value
end sub

function LongProgressionIterator_get_next_k_() as LongInteger
    return m.next
end function

sub LongProgressionIterator_set_next_J_k_(value as LongInteger)
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
    this.get_step = CharProgressionIterator_get_step_k_
    this.get_finalElement = CharProgressionIterator_get_finalElement_k_
    this.get_hasNext = CharProgressionIterator_get_hasNext_k_
    this.set_hasNext = CharProgressionIterator_set_hasNext_Z_k_
    this.get_next = CharProgressionIterator_get_next_k_
    this.set_next = CharProgressionIterator_set_next_I_k_
    this.step = step_
    this.finalElement = get_code_rC_k_(last)
    __when_tmp16 = invalid
    if this.get_step() > 0 then
        __when_tmp16 = ((first <= last) <= 0)
    else if true then
        __when_tmp16 = ((first >= last) >= 0)
    end if
    this.hasNext = __when_tmp16
    __when_tmp17 = invalid
    if this.get_hasNext() then
        __when_tmp17 = get_code_rC_k_(first)
    else if true then
        __when_tmp17 = this.get_finalElement()
    end if
    this.next = __when_tmp17
    return this
end function

function CharProgressionIterator_hasNext_k_() as Boolean
    return m.get_hasNext()
end function

function CharProgressionIterator_nextChar_k_() as Object
    value = m.get_next()
    if value = m.get_finalElement() then
        if not m.get_hasNext() then
            throw NoSuchElementException_create_k_()
        end if
        m.set_hasNext(false)
    else if true then
        m.set_next(m.get_next() + m.get_step())
    end if
    return value
end function

function CharProgressionIterator_get_step_k_() as Integer
    return m.step
end function

function CharProgressionIterator_get_finalElement_k_() as Integer
    return m.finalElement
end function

function CharProgressionIterator_get_hasNext_k_() as Boolean
    return m.hasNext
end function

sub CharProgressionIterator_set_hasNext_Z_k_(value as Boolean)
    m.hasNext = value
end sub

function CharProgressionIterator_get_next_k_() as Integer
    return m.next
end function

sub CharProgressionIterator_set_next_I_k_(value as Integer)
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
