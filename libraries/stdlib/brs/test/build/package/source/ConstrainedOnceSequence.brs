function ConstrainedOnceSequence_create_SequenceAnyN_ConstrainedOnceSequenceAnyN_k_(sequence as Object) as Object
    this = {}
    this.__type = "ConstrainedOnceSequence"
    this.__proto = ["ConstrainedOnceSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_IteratorAnyN_k_ = ConstrainedOnceSequence_iterator_IteratorAnyN_k_
    this.get_sequence = ConstrainedOnceSequence_get_sequence_SequenceAnyN_k_
    this.get_consumed = ConstrainedOnceSequence_get_consumed_Z_k_
    this.set_consumed = ConstrainedOnceSequence_set_consumed_Z_k_
    this.sequence = sequence
    this.consumed = false
    return this
end function

function ConstrainedOnceSequence_iterator_IteratorAnyN_k_() as Object
    if m.get_consumed() then
        throw IllegalStateException_create_StrN_IllegalStateException_k_("This sequence can be consumed only once.")
    end if
    m.set_consumed(true)
    return m.get_sequence().iterator_IteratorAnyN_k_()
end function

function ConstrainedOnceSequence_get_sequence_SequenceAnyN_k_() as Object
    return m.sequence
end function

function ConstrainedOnceSequence_get_consumed_Z_k_() as Boolean
    return m.consumed
end function

sub ConstrainedOnceSequence_set_consumed_Z_k_(value as Boolean)
    m.consumed = value
end sub
