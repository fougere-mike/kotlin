function ConstrainedOnceSequence_create_Sequence_k_(sequence as Object) as Object
    this = {}
    this.__type = "ConstrainedOnceSequence"
    this.__proto = ["ConstrainedOnceSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = ConstrainedOnceSequence_iterator_k_
    this.__get_sequence = ConstrainedOnceSequence___get_sequence_k_
    this.__get_consumed = ConstrainedOnceSequence___get_consumed_k_
    this.__set_consumed = ConstrainedOnceSequence___set_consumed_Z_k_
    this.sequence = sequence
    this.consumed = false
    return this
end function

function ConstrainedOnceSequence_iterator_k_() as Object
    if m.__get_consumed() then
        throw IllegalStateException_create_StrN_k_("This sequence can be consumed only once.")
    end if
    m.__set_consumed(true)
    return m.__get_sequence().iterator_k_()
end function

function ConstrainedOnceSequence___get_sequence_k_() as Object
    return m.sequence
end function

function ConstrainedOnceSequence___get_consumed_k_() as Boolean
    return m.consumed
end function

sub ConstrainedOnceSequence___set_consumed_Z_k_(value as Boolean)
    m.consumed = value
end sub
