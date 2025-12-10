function emptySequence_k_() as Object
    return EmptySequence_getInstance()
end function

function sequenceOf_AnyN_k_(element as Dynamic) as Object
    return Anon_4393a87_create_k_()
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
    return Anon_3adcdbc0_create_k_()
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
    return Anon_5cee664b_create_k_()
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
    return Anon_1c650d81_create_k_()
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
