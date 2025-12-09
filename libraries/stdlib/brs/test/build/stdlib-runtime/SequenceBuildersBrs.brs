function emptySequence_SequenceAnyN_k_() as Object
    return EmptySequence_getInstance()
end function

function sequenceOf_AnyN_SequenceAnyN_k_(element as Dynamic) as Object
    return Anon_3468192e_create_AnonAnyN_k_()
end function

function sequenceOf_Arr_SequenceAnyN_k_(elements as Object) as Object
    __when_tmp0 = invalid
    if isEmpty_rArr_Z_k_(elements) then
        __when_tmp0 = emptySequence_SequenceAnyN_k_()
    else if true then
        __when_tmp0 = asSequence_rArr_SequenceAnyN_k_(elements)
    end if
    return __when_tmp0

end function

function sequenceOf_IteratorAnyN_SequenceAnyN_k_(iterator as Object) as Object
    return Anon_6bc553d_create_AnonAnyN_k_()
end function

function sequence_SequenceAnyN_k_() as Object
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Coroutine-based sequence building is not supported for BRS")
end function

function generateSequence_AnyN_Function1AnyAnyN_SequenceAny_k_(seed as Dynamic, nextFunction as Object) as Object
    __when_tmp1 = invalid
    if seed = invalid then
        __when_tmp1 = EmptySequence_getInstance()
    else if true then
        __when_tmp1 = GeneratorSequence_create_Function0AnyN_Function1AnyAnyN_GeneratorSequenceAny_k_({seed: seed, invoke: function() as Dynamic
            return m.seed
        end function}, nextFunction)
    end if
    return __when_tmp1

end function

function generateSequence_Function0AnyN_Function1AnyAnyN_SequenceAny_k_(seedFunction as Object, nextFunction as Object) as Object
    return GeneratorSequence_create_Function0AnyN_Function1AnyAnyN_GeneratorSequenceAny_k_(seedFunction, nextFunction)
end function

function asSequence_rArr_SequenceAnyN_k_(m as Object) as Object
    __when_tmp2 = invalid
    if isEmpty_rArr_Z_k_(m) then
        __when_tmp2 = emptySequence_SequenceAnyN_k_()
    else if true then
        __when_tmp2 = IndexedSequence_create_Arr_IndexedSequenceAnyN_k_(m)
    end if
    return __when_tmp2

end function

function asSequence_rIterableAnyN_SequenceAnyN_k_(m as Object) as Object
    return Sequence_Function0IteratorAnyN_SequenceAnyN_k_({this: m, invoke: function() as Object
        return m.this.iterator_IteratorAnyN_k_()
    end function})
end function

function asSequence_rIteratorAnyN_SequenceAnyN_k_(m as Object) as Object
    return Sequence_Function0IteratorAnyN_SequenceAnyN_k_({this: m, invoke: function() as Object
        return m.this
    end function})
end function

function Sequence_Function0IteratorAnyN_SequenceAnyN_k_(iterator as Object) as Object
    return Anon_2545b920_create_AnonAnyN_k_()
end function

function EmptySequence_create_EmptySequence_k_() as Object
    this = {}
    this.__type = "EmptySequence"
    this.__proto = ["EmptySequence"]
    this.iterator_IteratorNothing_k_ = EmptySequence_iterator_IteratorNothing_k_
    this.drop_I_SequenceNothing_k_ = EmptySequence_drop_I_SequenceNothing_k_
    this.take_I_SequenceNothing_k_ = EmptySequence_take_I_SequenceNothing_k_
    return this
end function

function EmptySequence_getInstance() as Object
    if m.EmptySequence_instance = invalid then
        m.EmptySequence_instance = EmptySequence_create_EmptySequence_k_()
    end if
    return m.EmptySequence_instance
end function

function EmptySequence_iterator_IteratorNothing_k_() as Object
    return SequenceEmptyIterator_getInstance()
end function

function EmptySequence_drop_I_SequenceNothing_k_(n as Integer) as Object
    return EmptySequence_getInstance()
end function

function EmptySequence_take_I_SequenceNothing_k_(n as Integer) as Object
    return EmptySequence_getInstance()
end function

function SequenceEmptyIterator_create_SequenceEmptyIterator_k_() as Object
    this = {}
    this.__type = "SequenceEmptyIterator"
    this.__proto = ["SequenceEmptyIterator"]
    this.hasNext_Z_k_ = SequenceEmptyIterator_hasNext_Z_k_
    this.next = SequenceEmptyIterator_next
    return this
end function

function SequenceEmptyIterator_getInstance() as Object
    if m.SequenceEmptyIterator_instance = invalid then
        m.SequenceEmptyIterator_instance = SequenceEmptyIterator_create_SequenceEmptyIterator_k_()
    end if
    return m.SequenceEmptyIterator_instance
end function

function SequenceEmptyIterator_hasNext_Z_k_() as Boolean
    return false
end function

sub SequenceEmptyIterator_next()
    throw NoSuchElementException_create_NoSuchElementException_k_()
end sub

function GeneratorSequence_create_Function0AnyN_Function1AnyAnyN_GeneratorSequenceAny_k_(getInitialValue as Object, getNextValue as Object) as Object
    this = {}
    this.__type = "GeneratorSequence"
    this.__proto = ["GeneratorSequence"]
    this.getInitialValue = getInitialValue
    this.getNextValue = getNextValue
    this.iterator_IteratorAny_k_ = GeneratorSequence_iterator_IteratorAny_k_
    this.get_getInitialValue = GeneratorSequence_get_getInitialValue_Function0AnyN_k_
    this.get_getNextValue = GeneratorSequence_get_getNextValue_Function1AnyAnyN_k_
    return this
end function

function GeneratorSequence_iterator_IteratorAny_k_() as Object
    return Anon_40ae6e43_create_AnonAny_k_()
end function

function GeneratorSequence_get_getInitialValue_Function0AnyN_k_() as Object
    return m.getInitialValue
end function

function GeneratorSequence_get_getNextValue_Function1AnyAnyN_k_() as Object
    return m.getNextValue
end function

function IndexedSequence_create_Arr_IndexedSequenceAnyN_k_(array as Object) as Object
    this = {}
    this.__type = "IndexedSequence"
    this.__proto = ["IndexedSequence"]
    this.array = array
    this.iterator_IteratorAnyN_k_ = IndexedSequence_iterator_IteratorAnyN_k_
    this.get_array = IndexedSequence_get_array_Arr_k_
    return this
end function

function IndexedSequence_iterator_IteratorAnyN_k_() as Object
    return m.get_array().iterator_IteratorAnyN_k_()
end function

function IndexedSequence_get_array_Arr_k_() as Object
    return m.array
end function

function SequenceScope_create_SequenceScopeAnyN_k_() as Object
    this = {}
    this.__type = "SequenceScope"
    this.__proto = ["SequenceScope"]
    return this
end function

function DropTakeSequence_drop_I_SequenceAnyN_k_(n as Integer) as Object
end function

function DropTakeSequence_take_I_SequenceAnyN_k_(n as Integer) as Object
end function
