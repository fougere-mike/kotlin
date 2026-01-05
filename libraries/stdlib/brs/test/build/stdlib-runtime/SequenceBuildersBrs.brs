function emptySequence_k_() as Object
    return EmptySequence_getInstance()
end function

function sequenceOf_AnyN_k_(element as Dynamic) as Object
    return Anon_35897518_create_AnyN_k_(element)
end function

function sequenceOf_Arr_k_(elements as Object) as Object
    __when_tmp0 = invalid
    if isEmpty_rArr_k_(elements) then
        __when_tmp0 = emptySequence_k_()
    else if true then
        __when_tmp0 = asSequence_rArr_k_(elements)
    end if
    return __when_tmp0

end function

function sequenceOf_Iterator_k_(iterator as Object) as Object
    return Anon_5051fe4f_create_Iterator_k_(iterator)
end function

function sequence_k_() as Object
    throw UnsupportedOperationException_create_StrN_k_("Coroutine-based sequence building is not supported for BRS")
end function

function generateSequence_AnyN_Function1_k_(seed as Dynamic, nextFunction as Object) as Object
    __when_tmp1 = invalid
    if seed = invalid then
        __when_tmp1 = EmptySequence_getInstance()
    else if true then
        __when_tmp1 = GeneratorSequence_create_Function0_Function1_k_({seed: seed, invoke: function() as Dynamic
            return m.seed
        end function}, nextFunction)
    end if
    return __when_tmp1

end function

function generateSequence_Function0_Function1_k_(seedFunction as Object, nextFunction as Object) as Object
    return GeneratorSequence_create_Function0_Function1_k_(seedFunction, nextFunction)
end function

function asSequence_rArr_k_(m as Object) as Object
    __when_tmp2 = invalid
    if isEmpty_rArr_k_(m) then
        __when_tmp2 = emptySequence_k_()
    else if true then
        __when_tmp2 = IndexedSequence_create_Arr_k_(m)
    end if
    return __when_tmp2

end function

function asSequence_rIterable_k_(m as Object) as Object
    return Sequence_Function0Iterator_k_({this: m, invoke: function() as Object
        return m.this.iterator_k_()
    end function})
end function

function asSequence_rIterator_k_(m as Object) as Object
    return Sequence_Function0Iterator_k_({this: m, invoke: function() as Object
        return m.this
    end function})
end function

function Sequence_Function0Iterator_k_(iterator as Object) as Object
    return Anon_5bb37371_create_Function0Iterator_k_(iterator)
end function

function EmptySequence_create_k_() as Object
    this = {}
    this.__type = "EmptySequence"
    this.__proto = ["EmptySequence", "Sequence", "DropTakeSequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = EmptySequence_iterator_k_
    this.drop_I_k_ = EmptySequence_drop_I_k_
    this.take_I_k_ = EmptySequence_take_I_k_
    return this
end function

function EmptySequence_getInstance() as Object
    if m.EmptySequence_instance = invalid then
        m.EmptySequence_instance = EmptySequence_create_k_()
    end if
    return m.EmptySequence_instance
end function

function EmptySequence_iterator_k_() as Object
    return SequenceEmptyIterator_getInstance()
end function

function EmptySequence_drop_I_k_(n as Integer) as Object
    return EmptySequence_getInstance()
end function

function EmptySequence_take_I_k_(n as Integer) as Object
    return EmptySequence_getInstance()
end function

function SequenceEmptyIterator_create_k_() as Object
    this = {}
    this.__type = "SequenceEmptyIterator"
    this.__proto = ["SequenceEmptyIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = SequenceEmptyIterator_hasNext_k_
    this.next_k_ = SequenceEmptyIterator_next_k_
    return this
end function

function SequenceEmptyIterator_getInstance() as Object
    if m.SequenceEmptyIterator_instance = invalid then
        m.SequenceEmptyIterator_instance = SequenceEmptyIterator_create_k_()
    end if
    return m.SequenceEmptyIterator_instance
end function

function SequenceEmptyIterator_hasNext_k_() as Boolean
    return false
end function

sub SequenceEmptyIterator_next_k_()
    throw NoSuchElementException_create_k_()
end sub

function GeneratorSequence_create_Function0_Function1_k_(getInitialValue as Object, getNextValue as Object) as Object
    this = {}
    this.__type = "GeneratorSequence"
    this.__proto = ["GeneratorSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = GeneratorSequence_iterator_k_
    this.get_getInitialValue = GeneratorSequence_get_getInitialValue_k_
    this.get_getNextValue = GeneratorSequence_get_getNextValue_k_
    this.getInitialValue = getInitialValue
    this.getNextValue = getNextValue
    return this
end function

function GeneratorSequence_iterator_k_() as Object
    return Anon_7e87bd7e_create_GeneratorSequence_k_(m)
end function

function GeneratorSequence_get_getInitialValue_k_() as Object
    return m.getInitialValue
end function

function GeneratorSequence_get_getNextValue_k_() as Object
    return m.getNextValue
end function

function IndexedSequence_create_Arr_k_(array as Object) as Object
    this = {}
    this.__type = "IndexedSequence"
    this.__proto = ["IndexedSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = IndexedSequence_iterator_k_
    this.get_array = IndexedSequence_get_array_k_
    this.array = array
    return this
end function

function IndexedSequence_iterator_k_() as Object
    return m.get_array().iterator_k_()
end function

function IndexedSequence_get_array_k_() as Object
    return m.array
end function

function SequenceScope_create_k_() as Object
    this = {}
    this.__type = "SequenceScope"
    this.__proto = ["SequenceScope"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function DropTakeSequence_drop_I_k_(n as Integer) as Object
end function

function DropTakeSequence_take_I_k_(n as Integer) as Object
end function

function Anon_45507d66_create_AnyN_k_(_element as Dynamic) as Object
    this = {}
    this.__type = "Anon_45507d66"
    this.__proto = ["Anon_45507d66", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = Anon_45507d66_hasNext_k_
    this.next_k_ = Anon_45507d66_next_k_
    this.get_hasNext = Anon_45507d66_get_hasNext_k_
    this.set_hasNext = Anon_45507d66_set_hasNext_Z_k_
    this.hasNext = true
    this._element = _element
    return this
end function

function Anon_45507d66_hasNext_k_() as Boolean
    return m.get_hasNext()
end function

function Anon_45507d66_next_k_() as Dynamic
    if not m.get_hasNext() then
        throw NoSuchElementException_create_k_()
    end if
    m.set_hasNext(false)
    return m._element
end function

function Anon_45507d66_get_hasNext_k_() as Boolean
    return m.hasNext
end function

sub Anon_45507d66_set_hasNext_Z_k_(value as Boolean)
    m.hasNext = value
end sub

function Anon_35897518_create_AnyN_k_(_element as Dynamic) as Object
    this = {}
    this.__type = "Anon_35897518"
    this.__proto = ["Anon_35897518", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = Anon_35897518_iterator_k_
    this._element = _element
    return this
end function

function Anon_35897518_iterator_k_() as Object
    return Anon_45507d66_create_AnyN_k_(m._element)
end function

function Anon_5051fe4f_create_Iterator_k_(_iterator as Object) as Object
    this = {}
    this.__type = "Anon_5051fe4f"
    this.__proto = ["Anon_5051fe4f", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = Anon_5051fe4f_iterator_k_
    this._iterator = _iterator
    return this
end function

function Anon_5051fe4f_iterator_k_() as Object
    return m._iterator
end function

function Anon_5bb37371_create_Function0Iterator_k_(_iterator as Object) as Object
    this = {}
    this.__type = "Anon_5bb37371"
    this.__proto = ["Anon_5bb37371", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = Anon_5bb37371_iterator_k_
    this._iterator = _iterator
    return this
end function

function Anon_5bb37371_iterator_k_() as Object
    return m._iterator.invoke()
end function

function Anon_7e87bd7e_create_GeneratorSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_7e87bd7e"
    this.__proto = ["Anon_7e87bd7e", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.calcNext_k_ = Anon_7e87bd7e_calcNext_k_
    this.hasNext_k_ = Anon_7e87bd7e_hasNext_k_
    this.next_k_ = Anon_7e87bd7e_next_k_
    this.get_nextItem = Anon_7e87bd7e_get_nextItem_k_
    this.set_nextItem = Anon_7e87bd7e_set_nextItem_AnyN_k_
    this.get_nextState = Anon_7e87bd7e_get_nextState_k_
    this.set_nextState = Anon_7e87bd7e_set_nextState_I_k_
    this.nextItem = invalid
    this.nextState = -2
    this.this_0 = this_0
    return this
end function

sub Anon_7e87bd7e_calcNext_k_()
    __when_tmp3 = invalid
    if m.get_nextState() = -2 then
        __when_tmp3 = m.this_0.get_getInitialValue().invoke()
    else if true then
        __when_tmp3 = m.this_0.get_getNextValue().invoke(m.get_nextItem())
    end if
    m.set_nextItem(__when_tmp3)

    __when_tmp4 = invalid
    if m.get_nextItem() = invalid then
        __when_tmp4 = 0
    else if true then
        __when_tmp4 = 1
    end if
    m.set_nextState(__when_tmp4)

end sub

function Anon_7e87bd7e_hasNext_k_() as Boolean
    if m.get_nextState() < 0 then
        m.calcNext_k_()
    end if
    return m.get_nextState() = 1
end function

function Anon_7e87bd7e_next_k_() as Object
    if m.get_nextState() < 0 then
        m.calcNext_k_()
    end if
    if m.get_nextState() = 0 then
        throw NoSuchElementException_create_k_()
    end if
    result = m.get_nextItem()
    m.set_nextState(-1)
    return result
end function

function Anon_7e87bd7e_get_nextItem_k_() as Dynamic
    return m.nextItem
end function

sub Anon_7e87bd7e_set_nextItem_AnyN_k_(value as Dynamic)
    m.nextItem = value
end sub

function Anon_7e87bd7e_get_nextState_k_() as Integer
    return m.nextState
end function

sub Anon_7e87bd7e_set_nextState_I_k_(value as Integer)
    m.nextState = value
end sub
