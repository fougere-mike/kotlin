function TransformingSequence_create_SequenceAnyN_Function1AnyNAnyN_TransformingSequenceAnyNAnyN_k_(sequence as Object, transformer as Object) as Object
    this = {}
    this.__type = "TransformingSequence"
    this.__proto = ["TransformingSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_IteratorAnyN_k_ = TransformingSequence_iterator_IteratorAnyN_k_
    this.flatten_Function1AnyNIteratorAnyN_SequenceAnyN_k_ = TransformingSequence_flatten_Function1AnyNIteratorAnyN_SequenceAnyN_k_
    this.get_sequence = TransformingSequence_get_sequence_SequenceAnyN_k_
    this.get_transformer = TransformingSequence_get_transformer_Function1AnyNAnyN_k_
    this.sequence = sequence
    this.transformer = transformer
    return this
end function

function TransformingSequence_iterator_IteratorAnyN_k_() as Object
    return Anon_1b641b60_create_AnonAnyNAnyN_k_()
end function

function TransformingSequence_flatten_Function1AnyNIteratorAnyN_SequenceAnyN_k_(iterator as Object) as Object
    return FlatteningSequence_create_hoq5fb_k_(m.get_sequence(), m.get_transformer(), iterator)
end function

function TransformingSequence_get_sequence_SequenceAnyN_k_() as Object
    return m.sequence
end function

function TransformingSequence_get_transformer_Function1AnyNAnyN_k_() as Object
    return m.transformer
end function

function TransformingIndexedSequence_create_obcj3k_k_(sequence as Object, transformer as Object) as Object
    this = {}
    this.__type = "TransformingIndexedSequence"
    this.__proto = ["TransformingIndexedSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_IteratorAnyN_k_ = TransformingIndexedSequence_iterator_IteratorAnyN_k_
    this.get_sequence = TransformingIndexedSequence_get_sequence_SequenceAnyN_k_
    this.get_transformer = TransformingIndexedSequence_get_transformer_Function2IAnyNAnyN_k_
    this.sequence = sequence
    this.transformer = transformer
    return this
end function

function TransformingIndexedSequence_iterator_IteratorAnyN_k_() as Object
    return Anon_4d705b45_create_AnonAnyNAnyN_k_()
end function

function TransformingIndexedSequence_get_sequence_SequenceAnyN_k_() as Object
    return m.sequence
end function

function TransformingIndexedSequence_get_transformer_Function2IAnyNAnyN_k_() as Object
    return m.transformer
end function

function FilteringSequence_create_SequenceAnyN_Z_Function1AnyNZ_FilteringSequenceAnyN_k_(sequence as Object, sendWhen = true, predicate = invalid) as Object
    this = {}
    this.__type = "FilteringSequence"
    this.__proto = ["FilteringSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_IteratorAnyN_k_ = FilteringSequence_iterator_IteratorAnyN_k_
    this.get_sequence = FilteringSequence_get_sequence_SequenceAnyN_k_
    this.get_sendWhen = FilteringSequence_get_sendWhen_Z_k_
    this.get_predicate = FilteringSequence_get_predicate_Function1AnyNZ_k_
    this.sequence = sequence
    this.sendWhen = sendWhen
    this.predicate = predicate
    return this
end function

function FilteringSequence_iterator_IteratorAnyN_k_() as Object
    return Anon_1d0c0049_create_AnonAnyN_k_()
end function

function FilteringSequence_get_sequence_SequenceAnyN_k_() as Object
    return m.sequence
end function

function FilteringSequence_get_sendWhen_Z_k_() as Boolean
    return m.sendWhen
end function

function FilteringSequence_get_predicate_Function1AnyNZ_k_() as Object
    return m.predicate
end function

function FilteringIndexedSequence_create_SequenceAnyN_Z_Function2IAnyNZ_FilteringIndexedSequenceAnyN_k_(sequence as Object, sendWhen = true, predicate = invalid) as Object
    this = {}
    this.__type = "FilteringIndexedSequence"
    this.__proto = ["FilteringIndexedSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_IteratorAnyN_k_ = FilteringIndexedSequence_iterator_IteratorAnyN_k_
    this.get_sequence = FilteringIndexedSequence_get_sequence_SequenceAnyN_k_
    this.get_sendWhen = FilteringIndexedSequence_get_sendWhen_Z_k_
    this.get_predicate = FilteringIndexedSequence_get_predicate_Function2IAnyNZ_k_
    this.sequence = sequence
    this.sendWhen = sendWhen
    this.predicate = predicate
    return this
end function

function FilteringIndexedSequence_iterator_IteratorAnyN_k_() as Object
    return Anon_45637d0d_create_AnonAnyN_k_()
end function

function FilteringIndexedSequence_get_sequence_SequenceAnyN_k_() as Object
    return m.sequence
end function

function FilteringIndexedSequence_get_sendWhen_Z_k_() as Boolean
    return m.sendWhen
end function

function FilteringIndexedSequence_get_predicate_Function2IAnyNZ_k_() as Object
    return m.predicate
end function

function FlatteningSequence_create_hoq5fb_k_(sequence as Object, transformer as Object, iterator as Object) as Object
    this = {}
    this.__type = "FlatteningSequence"
    this.__proto = ["FlatteningSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_IteratorAnyN_k_ = FlatteningSequence_iterator_IteratorAnyN_k_
    this.get_sequence = FlatteningSequence_get_sequence_SequenceAnyN_k_
    this.get_transformer = FlatteningSequence_get_transformer_Function1AnyNAnyN_k_
    this.get_iterator = FlatteningSequence_get_iterator_Function1AnyNIteratorAnyN_k_
    this.sequence = sequence
    this.transformer = transformer
    this.iterator = iterator
    return this
end function

function FlatteningSequence_iterator_IteratorAnyN_k_() as Object
    return Anon_8d4bf61_create_AnonAnyNAnyNAnyN_k_()
end function

function FlatteningSequence_get_sequence_SequenceAnyN_k_() as Object
    return m.sequence
end function

function FlatteningSequence_get_transformer_Function1AnyNAnyN_k_() as Object
    return m.transformer
end function

function FlatteningSequence_get_iterator_Function1AnyNIteratorAnyN_k_() as Object
    return m.iterator
end function

function TakingSequence_create_SequenceAnyN_I_TakingSequenceAnyN_k_(sequence as Object, count as Integer) as Object
    this = {}
    this.__type = "TakingSequence"
    this.__proto = ["TakingSequence", "Sequence", "DropTakeSequence"]
    this.__id = __kotlin_nextObjectId()
    this.drop_I_SequenceAnyN_k_ = TakingSequence_drop_I_SequenceAnyN_k_
    this.take_I_SequenceAnyN_k_ = TakingSequence_take_I_SequenceAnyN_k_
    this.iterator_IteratorAnyN_k_ = TakingSequence_iterator_IteratorAnyN_k_
    this.get_sequence = TakingSequence_get_sequence_SequenceAnyN_k_
    this.get_count = TakingSequence_get_count_I_k_
    this.sequence = sequence
    this.count = count
    return this
end function

function TakingSequence_drop_I_SequenceAnyN_k_(n as Integer) as Object
    __when_tmp1 = invalid
    if n >= m.get_count() then
        __when_tmp1 = emptySequence_SequenceAnyN_k_()
    else if true then
        __when_tmp1 = SubSequence_create_SequenceAnyN_I_I_SubSequenceAnyN_k_(m.get_sequence(), n, m.get_count())
    end if
    return __when_tmp1

end function

function TakingSequence_take_I_SequenceAnyN_k_(n as Integer) as Object
    __when_tmp2 = invalid
    if n >= m.get_count() then
        __when_tmp2 = m
    else if true then
        __when_tmp2 = TakingSequence_create_SequenceAnyN_I_TakingSequenceAnyN_k_(m.get_sequence(), n)
    end if
    return __when_tmp2

end function

function TakingSequence_iterator_IteratorAnyN_k_() as Object
    return Anon_38babc22_create_AnonAnyN_k_()
end function

function TakingSequence_get_sequence_SequenceAnyN_k_() as Object
    return m.sequence
end function

function TakingSequence_get_count_I_k_() as Integer
    return m.count
end function

function TakingWhileSequence_create_SequenceAnyN_Function1AnyNZ_TakingWhileSequenceAnyN_k_(sequence as Object, predicate as Object) as Object
    this = {}
    this.__type = "TakingWhileSequence"
    this.__proto = ["TakingWhileSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_IteratorAnyN_k_ = TakingWhileSequence_iterator_IteratorAnyN_k_
    this.get_sequence = TakingWhileSequence_get_sequence_SequenceAnyN_k_
    this.get_predicate = TakingWhileSequence_get_predicate_Function1AnyNZ_k_
    this.sequence = sequence
    this.predicate = predicate
    return this
end function

function TakingWhileSequence_iterator_IteratorAnyN_k_() as Object
    return Anon_67a86535_create_AnonAnyN_k_()
end function

function TakingWhileSequence_get_sequence_SequenceAnyN_k_() as Object
    return m.sequence
end function

function TakingWhileSequence_get_predicate_Function1AnyNZ_k_() as Object
    return m.predicate
end function

function DroppingSequence_create_SequenceAnyN_I_DroppingSequenceAnyN_k_(sequence as Object, count as Integer) as Object
    this = {}
    this.__type = "DroppingSequence"
    this.__proto = ["DroppingSequence", "Sequence", "DropTakeSequence"]
    this.__id = __kotlin_nextObjectId()
    this.drop_I_SequenceAnyN_k_ = DroppingSequence_drop_I_SequenceAnyN_k_
    this.take_I_SequenceAnyN_k_ = DroppingSequence_take_I_SequenceAnyN_k_
    this.iterator_IteratorAnyN_k_ = DroppingSequence_iterator_IteratorAnyN_k_
    this.get_sequence = DroppingSequence_get_sequence_SequenceAnyN_k_
    this.get_count = DroppingSequence_get_count_I_k_
    this.sequence = sequence
    this.count = count
    return this
end function

function DroppingSequence_drop_I_SequenceAnyN_k_(n as Integer) as Object
    return DroppingSequence_create_SequenceAnyN_I_DroppingSequenceAnyN_k_(m.get_sequence(), m.get_count() + n)
end function

function DroppingSequence_take_I_SequenceAnyN_k_(n as Integer) as Object
    return SubSequence_create_SequenceAnyN_I_I_SubSequenceAnyN_k_(m.get_sequence(), m.get_count(), n)
end function

function DroppingSequence_iterator_IteratorAnyN_k_() as Object
    return Anon_458037d9_create_AnonAnyN_k_()
end function

function DroppingSequence_get_sequence_SequenceAnyN_k_() as Object
    return m.sequence
end function

function DroppingSequence_get_count_I_k_() as Integer
    return m.count
end function

function DroppingWhileSequence_create_SequenceAnyN_Function1AnyNZ_DroppingWhileSequenceAnyN_k_(sequence as Object, predicate as Object) as Object
    this = {}
    this.__type = "DroppingWhileSequence"
    this.__proto = ["DroppingWhileSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_IteratorAnyN_k_ = DroppingWhileSequence_iterator_IteratorAnyN_k_
    this.get_sequence = DroppingWhileSequence_get_sequence_SequenceAnyN_k_
    this.get_predicate = DroppingWhileSequence_get_predicate_Function1AnyNZ_k_
    this.sequence = sequence
    this.predicate = predicate
    return this
end function

function DroppingWhileSequence_iterator_IteratorAnyN_k_() as Object
    return Anon_57800592_create_AnonAnyN_k_()
end function

function DroppingWhileSequence_get_sequence_SequenceAnyN_k_() as Object
    return m.sequence
end function

function DroppingWhileSequence_get_predicate_Function1AnyNZ_k_() as Object
    return m.predicate
end function

function SubSequence_create_SequenceAnyN_I_I_SubSequenceAnyN_k_(sequence as Object, startIndex as Integer, endIndex as Integer) as Object
    this = {}
    this.__type = "SubSequence"
    this.__proto = ["SubSequence", "Sequence", "DropTakeSequence"]
    this.__id = __kotlin_nextObjectId()
    this.drop_I_SequenceAnyN_k_ = SubSequence_drop_I_SequenceAnyN_k_
    this.take_I_SequenceAnyN_k_ = SubSequence_take_I_SequenceAnyN_k_
    this.iterator_IteratorAnyN_k_ = SubSequence_iterator_IteratorAnyN_k_
    this.get_sequence = SubSequence_get_sequence_SequenceAnyN_k_
    this.get_startIndex = SubSequence_get_startIndex_I_k_
    this.get_endIndex = SubSequence_get_endIndex_I_k_
    this.get_count = SubSequence_get_count_I_k_
    this.sequence = sequence
    this.startIndex = startIndex
    this.endIndex = endIndex
    return this
end function

function SubSequence_drop_I_SequenceAnyN_k_(n as Integer) as Object
    __when_tmp3 = invalid
    if n >= m.get_count() then
        __when_tmp3 = emptySequence_SequenceAnyN_k_()
    else if true then
        __when_tmp3 = SubSequence_create_SequenceAnyN_I_I_SubSequenceAnyN_k_(m.get_sequence(), m.get_startIndex() + n, m.get_endIndex())
    end if
    return __when_tmp3

end function

function SubSequence_take_I_SequenceAnyN_k_(n as Integer) as Object
    __when_tmp4 = invalid
    if n >= m.get_count() then
        __when_tmp4 = m
    else if true then
        __when_tmp4 = SubSequence_create_SequenceAnyN_I_I_SubSequenceAnyN_k_(m.get_sequence(), m.get_startIndex(), m.get_startIndex() + n)
    end if
    return __when_tmp4

end function

function SubSequence_iterator_IteratorAnyN_k_() as Object
    return Anon_1ff67f85_create_AnonAnyN_k_()
end function

function SubSequence_get_sequence_SequenceAnyN_k_() as Object
    return m.sequence
end function

function SubSequence_get_startIndex_I_k_() as Integer
    return m.startIndex
end function

function SubSequence_get_endIndex_I_k_() as Integer
    return m.endIndex
end function

function SubSequence_get_count_I_k_() as Integer
    return m.get_endIndex() - m.get_startIndex()
end function

function DistinctSequence_create_SequenceAnyN_Function1AnyNAnyN_DistinctSequenceAnyNAnyN_k_(source as Object, keySelector as Object) as Object
    this = {}
    this.__type = "DistinctSequence"
    this.__proto = ["DistinctSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_IteratorAnyN_k_ = DistinctSequence_iterator_IteratorAnyN_k_
    this.get_source = DistinctSequence_get_source_SequenceAnyN_k_
    this.get_keySelector = DistinctSequence_get_keySelector_Function1AnyNAnyN_k_
    this.source = source
    this.keySelector = keySelector
    return this
end function

function DistinctSequence_iterator_IteratorAnyN_k_() as Object
    return Anon_31cdae6e_create_AnonAnyNAnyN_k_()
end function

function DistinctSequence_get_source_SequenceAnyN_k_() as Object
    return m.source
end function

function DistinctSequence_get_keySelector_Function1AnyNAnyN_k_() as Object
    return m.keySelector
end function

function MergingSequence_create_efuchp_k_(sequence1 as Object, sequence2 as Object, transform as Object) as Object
    this = {}
    this.__type = "MergingSequence"
    this.__proto = ["MergingSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_IteratorAnyN_k_ = MergingSequence_iterator_IteratorAnyN_k_
    this.get_sequence1 = MergingSequence_get_sequence1_SequenceAnyN_k_
    this.get_sequence2 = MergingSequence_get_sequence2_SequenceAnyN_k_
    this.get_transform = MergingSequence_get_transform_Function2AnyNAnyNAnyN_k_
    this.sequence1 = sequence1
    this.sequence2 = sequence2
    this.transform = transform
    return this
end function

function MergingSequence_iterator_IteratorAnyN_k_() as Object
    return Anon_74c4c452_create_AnonAnyNAnyNAnyN_k_()
end function

function MergingSequence_get_sequence1_SequenceAnyN_k_() as Object
    return m.sequence1
end function

function MergingSequence_get_sequence2_SequenceAnyN_k_() as Object
    return m.sequence2
end function

function MergingSequence_get_transform_Function2AnyNAnyNAnyN_k_() as Object
    return m.transform
end function
