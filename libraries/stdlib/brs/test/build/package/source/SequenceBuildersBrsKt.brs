function emptySequence_k_() as Object
    return EmptySequence_getInstance()
end function

function sequenceOf_AnyN_k_(element as Dynamic) as Object
    return Anon_482e7927_create_AnyN_k_(element)
end function

function sequenceOf_Arr_k_(elements as Object) as Object
    __when_tmp0 = invalid
    tmp0 = elements
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = (this.count() = 0)
        exit while
    end while
    if tmp_ret_0 then
        __when_tmp0 = emptySequence_k_()
    else if true then
        __when_tmp0 = asSequence_rArr_k_(elements)
    end if

    return __when_tmp0

end function

function sequenceOf_Iterator_k_(iterator as Object) as Object
    return Anon_5e92070a_create_Iterator_k_(iterator)
end function

function sequence_k_() as Object
    throw UnsupportedOperationException_create_StrN_k_("Coroutine-based sequence building is not supported for BRS")
end function

function generateSequence_AnyN_Function1_k_(seed as Dynamic, nextFunction as Object) as Object
    __when_tmp1 = invalid
    if seed = invalid then
        __when_tmp1 = EmptySequence_getInstance()
    else if true then
        __when_tmp1 = GeneratorSequence_create_Function0_Function1_k_(generateSequence_lambda_create_AnyN_k_(seed), nextFunction)
    end if
    return __when_tmp1

end function

function generateSequence_Function0_Function1_k_(seedFunction as Object, nextFunction as Object) as Object
    return GeneratorSequence_create_Function0_Function1_k_(seedFunction, nextFunction)
end function

function asSequence_rArr_k_(m as Object) as Object
    __when_tmp2 = invalid
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = (this.count() = 0)
        exit while
    end while
    if tmp_ret_0 then
        __when_tmp2 = emptySequence_k_()
    else if true then
        __when_tmp2 = IndexedSequence_create_Arr_k_(m)
    end if

    return __when_tmp2

end function

function asSequence_rIterable_k_(m as Object) as Object
    return Sequence_Function0Iterator_k_(asSequence_lambda_create_Iterable_k_(m))
end function

function asSequence_rIterator_k_(m as Object) as Object
    return Sequence_Function0Iterator_k_(asSequence_lambda_1_create_Iterator_k_(m))
end function

function Sequence_Function0Iterator_k_(iterator as Object) as Object
    return Anon_5cb525f3_create_Function0Iterator_k_(iterator)
end function

function EmptySequence_create_k_() as Object
    this = {}
    this.__type = "EmptySequence"
    this.__proto = ["EmptySequence", "Sequence", "DropTakeSequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = EmptySequence_iterator_k_
    this.drop_I_k_ = EmptySequence_drop_I_k_
    this.take_I_k_ = EmptySequence_take_I_k_
    return this
end function

function EmptySequence_getInstance() as Object
    if GetGlobalAA().EmptySequence_instance = invalid then
        GetGlobalAA().EmptySequence_instance = EmptySequence_create_k_()
    end if
    return GetGlobalAA().EmptySequence_instance
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
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = SequenceEmptyIterator_hasNext_k_
    this.next_k_ = SequenceEmptyIterator_next_k_
    return this
end function

function SequenceEmptyIterator_getInstance() as Object
    if GetGlobalAA().SequenceEmptyIterator_instance = invalid then
        GetGlobalAA().SequenceEmptyIterator_instance = SequenceEmptyIterator_create_k_()
    end if
    return GetGlobalAA().SequenceEmptyIterator_instance
end function

function SequenceEmptyIterator_hasNext_k_() as Boolean
    return false
end function

function SequenceEmptyIterator_next_k_() as Dynamic
    throw NoSuchElementException_create_k_()
end function

function GeneratorSequence_create_Function0_Function1_k_(getInitialValue as Object, getNextValue as Object) as Object
    this = {}
    this.__type = "GeneratorSequence"
    this.__proto = ["GeneratorSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = GeneratorSequence_iterator_k_
    this.__get_getInitialValue = GeneratorSequence___get_getInitialValue_k_
    this.__get_getNextValue = GeneratorSequence___get_getNextValue_k_
    this.getInitialValue = getInitialValue
    this.getNextValue = getNextValue
    return this
end function

function GeneratorSequence_iterator_k_() as Object
    return Anon_59f092f1_create_GeneratorSequence_k_(m)
end function

function GeneratorSequence___get_getInitialValue_k_() as Object
    return m.getInitialValue
end function

function GeneratorSequence___get_getNextValue_k_() as Object
    return m.getNextValue
end function

function IndexedSequence_create_Arr_k_(array as Object) as Object
    this = {}
    this.__type = "IndexedSequence"
    this.__proto = ["IndexedSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = IndexedSequence_iterator_k_
    this.__get_array = IndexedSequence___get_array_k_
    this.array = array
    return this
end function

function IndexedSequence_iterator_k_() as Object
    return m.__get_array().iterator_k_()
end function

function IndexedSequence___get_array_k_() as Object
    return m.array
end function

function SequenceScope_create_k_() as Object
    this = {}
    this.__type = "SequenceScope"
    this.__proto = ["SequenceScope"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    return this
end function

function DropTakeSequence_drop_I_k_(n as Integer) as Object
end function

function DropTakeSequence_take_I_k_(n as Integer) as Object
end function

function Anon_189f25de_create_AnyN_k_(_element as Dynamic) as Object
    this = {}
    this.__type = "Anon_189f25de"
    this.__proto = ["Anon_189f25de", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = Anon_189f25de_hasNext_k_
    this.next_k_ = Anon_189f25de_next_k_
    this.__get_hasNext = Anon_189f25de___get_hasNext_k_
    this.__set_hasNext = Anon_189f25de___set_hasNext_Z_k_
    this.hasNext = true
    this._element = _element
    return this
end function

function Anon_189f25de_hasNext_k_() as Boolean
    return m.__get_hasNext()
end function

function Anon_189f25de_next_k_() as Dynamic
    if not m.__get_hasNext() then
        throw NoSuchElementException_create_k_()
    end if
    m.__set_hasNext(false)
    return m._element
end function

function Anon_189f25de___get_hasNext_k_() as Boolean
    return m.hasNext
end function

sub Anon_189f25de___set_hasNext_Z_k_(value as Boolean)
    m.hasNext = value
end sub

function Anon_482e7927_create_AnyN_k_(_element as Dynamic) as Object
    this = {}
    this.__type = "Anon_482e7927"
    this.__proto = ["Anon_482e7927", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = Anon_482e7927_iterator_k_
    this._element = _element
    return this
end function

function Anon_482e7927_iterator_k_() as Object
    return Anon_189f25de_create_AnyN_k_(m._element)
end function

function Anon_5e92070a_create_Iterator_k_(_iterator as Object) as Object
    this = {}
    this.__type = "Anon_5e92070a"
    this.__proto = ["Anon_5e92070a", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = Anon_5e92070a_iterator_k_
    this._iterator = _iterator
    return this
end function

function Anon_5e92070a_iterator_k_() as Object
    return m._iterator
end function

function generateSequence_lambda_create_AnyN_k_(_seed as Dynamic) as Object
    this = {}
    this.__type = "generateSequence_lambda"
    this.__proto = ["generateSequence_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = generateSequence_lambda_invoke_k_
    this._seed = _seed
    return this
end function

function generateSequence_lambda_invoke_k_() as Dynamic
    return m._seed
end function

function asSequence_lambda_create_Iterable_k_(_this_asSequence as Object) as Object
    this = {}
    this.__type = "asSequence_lambda"
    this.__proto = ["asSequence_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = asSequence_lambda_invoke_k_
    this._this_asSequence = _this_asSequence
    return this
end function

function asSequence_lambda_invoke_k_() as Object
    return m._this_asSequence.iterator_k_()
end function

function asSequence_lambda_1_create_Iterator_k_(_this_asSequence as Object) as Object
    this = {}
    this.__type = "asSequence_lambda_1"
    this.__proto = ["asSequence_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = asSequence_lambda_1_invoke_k_
    this._this_asSequence = _this_asSequence
    return this
end function

function asSequence_lambda_1_invoke_k_() as Object
    return m._this_asSequence
end function

function Anon_5cb525f3_create_Function0Iterator_k_(_iterator as Object) as Object
    this = {}
    this.__type = "Anon_5cb525f3"
    this.__proto = ["Anon_5cb525f3", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = Anon_5cb525f3_iterator_k_
    this._iterator = _iterator
    return this
end function

function Anon_5cb525f3_iterator_k_() as Object
    return m._iterator.invoke_k_()
end function

function Anon_59f092f1_create_GeneratorSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_59f092f1"
    this.__proto = ["Anon_59f092f1", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.calcNext_k_ = Anon_59f092f1_calcNext_k_
    this.hasNext_k_ = Anon_59f092f1_hasNext_k_
    this.next_k_ = Anon_59f092f1_next_k_
    this.__get_nextItem = Anon_59f092f1___get_nextItem_k_
    this.__set_nextItem = Anon_59f092f1___set_nextItem_AnyN_k_
    this.__get_nextState = Anon_59f092f1___get_nextState_k_
    this.__set_nextState = Anon_59f092f1___set_nextState_I_k_
    this.nextItem = invalid
    this.nextState = -2
    this.this_0 = this_0
    return this
end function

sub Anon_59f092f1_calcNext_k_()
    __when_tmp3 = invalid
    if m.__get_nextState() = -2 then
        __when_tmp3 = m.this_0.__get_getInitialValue().invoke_k_()
    else if true then
        __when_tmp3 = m.this_0.__get_getNextValue().invoke_AnyN_k_(m.__get_nextItem())
    end if
    m.__set_nextItem(__when_tmp3)

    __when_tmp4 = invalid
    if m.__get_nextItem() = invalid then
        __when_tmp4 = 0
    else if true then
        __when_tmp4 = 1
    end if
    m.__set_nextState(__when_tmp4)

end sub

function Anon_59f092f1_hasNext_k_() as Boolean
    if m.__get_nextState() < 0 then
        m.calcNext_k_()
    end if
    return m.__get_nextState() = 1
end function

function Anon_59f092f1_next_k_() as Object
    if m.__get_nextState() < 0 then
        m.calcNext_k_()
    end if
    if m.__get_nextState() = 0 then
        throw NoSuchElementException_create_k_()
    end if
    result = m.__get_nextItem()
    m.__set_nextState(-1)
    return result
end function

function Anon_59f092f1___get_nextItem_k_() as Dynamic
    return m.nextItem
end function

sub Anon_59f092f1___set_nextItem_AnyN_k_(value as Dynamic)
    m.nextItem = value
end sub

function Anon_59f092f1___get_nextState_k_() as Integer
    return m.nextState
end function

sub Anon_59f092f1___set_nextState_I_k_(value as Integer)
    m.nextState = value
end sub
