function TransformingSequence_create_Sequence_Function1_k_(sequence as Object, transformer as Object) as Object
    this = {}
    this.__type = "TransformingSequence"
    this.__proto = ["TransformingSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = TransformingSequence_iterator_k_
    this.flatten_Function1Iterator_k_ = TransformingSequence_flatten_Function1Iterator_k_
    this.get_sequence = TransformingSequence_get_sequence_k_
    this.get_transformer = TransformingSequence_get_transformer_k_
    this.sequence = sequence
    this.transformer = transformer
    return this
end function

function TransformingSequence_iterator_k_() as Object
    return Anon_23748430_create_TransformingSequence_k_(m)
end function

function TransformingSequence_flatten_Function1Iterator_k_(iterator as Object) as Object
    return FlatteningSequence_create_Sequence_Function1_Function1Iterator_k_(m.get_sequence(), m.get_transformer(), iterator)
end function

function TransformingSequence_get_sequence_k_() as Object
    return m.sequence
end function

function TransformingSequence_get_transformer_k_() as Object
    return m.transformer
end function

function TransformingIndexedSequence_create_Sequence_Function2I_k_(sequence as Object, transformer as Object) as Object
    this = {}
    this.__type = "TransformingIndexedSequence"
    this.__proto = ["TransformingIndexedSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = TransformingIndexedSequence_iterator_k_
    this.get_sequence = TransformingIndexedSequence_get_sequence_k_
    this.get_transformer = TransformingIndexedSequence_get_transformer_k_
    this.sequence = sequence
    this.transformer = transformer
    return this
end function

function TransformingIndexedSequence_iterator_k_() as Object
    return Anon_117a82e5_create_TransformingIndexedSequence_k_(m)
end function

function TransformingIndexedSequence_get_sequence_k_() as Object
    return m.sequence
end function

function TransformingIndexedSequence_get_transformer_k_() as Object
    return m.transformer
end function

function FilteringSequence_create_Sequence_Z_Function1Z_k_(sequence as Object, sendWhen = true, predicate = invalid) as Object
    this = {}
    this.__type = "FilteringSequence"
    this.__proto = ["FilteringSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = FilteringSequence_iterator_k_
    this.get_sequence = FilteringSequence_get_sequence_k_
    this.get_sendWhen = FilteringSequence_get_sendWhen_k_
    this.get_predicate = FilteringSequence_get_predicate_k_
    this.sequence = sequence
    this.sendWhen = sendWhen
    this.predicate = predicate
    return this
end function

function FilteringSequence_iterator_k_() as Object
    return Anon_5b68827c_create_FilteringSequence_k_(m)
end function

function FilteringSequence_get_sequence_k_() as Object
    return m.sequence
end function

function FilteringSequence_get_sendWhen_k_() as Boolean
    return m.sendWhen
end function

function FilteringSequence_get_predicate_k_() as Object
    return m.predicate
end function

function FilteringIndexedSequence_create_Sequence_Z_Function2IZ_k_(sequence as Object, sendWhen = true, predicate = invalid) as Object
    this = {}
    this.__type = "FilteringIndexedSequence"
    this.__proto = ["FilteringIndexedSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = FilteringIndexedSequence_iterator_k_
    this.get_sequence = FilteringIndexedSequence_get_sequence_k_
    this.get_sendWhen = FilteringIndexedSequence_get_sendWhen_k_
    this.get_predicate = FilteringIndexedSequence_get_predicate_k_
    this.sequence = sequence
    this.sendWhen = sendWhen
    this.predicate = predicate
    return this
end function

function FilteringIndexedSequence_iterator_k_() as Object
    return Anon_6625d4aa_create_FilteringIndexedSequence_k_(m)
end function

function FilteringIndexedSequence_get_sequence_k_() as Object
    return m.sequence
end function

function FilteringIndexedSequence_get_sendWhen_k_() as Boolean
    return m.sendWhen
end function

function FilteringIndexedSequence_get_predicate_k_() as Object
    return m.predicate
end function

function FlatteningSequence_create_Sequence_Function1_Function1Iterator_k_(sequence as Object, transformer as Object, iterator as Object) as Object
    this = {}
    this.__type = "FlatteningSequence"
    this.__proto = ["FlatteningSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = FlatteningSequence_iterator_k_
    this.get_sequence = FlatteningSequence_get_sequence_k_
    this.get_transformer = FlatteningSequence_get_transformer_k_
    this.get_iterator = FlatteningSequence_get_iterator_k_
    this.sequence = sequence
    this.transformer = transformer
    this.iterator = iterator
    return this
end function

function FlatteningSequence_iterator_k_() as Object
    return Anon_e283976_create_FlatteningSequence_k_(m)
end function

function FlatteningSequence_get_sequence_k_() as Object
    return m.sequence
end function

function FlatteningSequence_get_transformer_k_() as Object
    return m.transformer
end function

function FlatteningSequence_get_iterator_k_() as Object
    return m.iterator
end function

function TakingSequence_create_Sequence_I_k_(sequence as Object, count as Integer) as Object
    this = {}
    this.__type = "TakingSequence"
    this.__proto = ["TakingSequence", "Sequence", "DropTakeSequence"]
    this.__id = __kotlin_nextObjectId()
    this.drop_I_k_ = TakingSequence_drop_I_k_
    this.take_I_k_ = TakingSequence_take_I_k_
    this.iterator_k_ = TakingSequence_iterator_k_
    this.get_sequence = TakingSequence_get_sequence_k_
    this.get_count = TakingSequence_get_count_k_
    this.sequence = sequence
    this.count = count
    return this
end function

function TakingSequence_drop_I_k_(n as Integer) as Object
    __when_tmp1 = invalid
    if n >= m.get_count() then
        __when_tmp1 = emptySequence_k_()
    else if true then
        __when_tmp1 = SubSequence_create_Sequence_I_I_k_(m.get_sequence(), n, m.get_count())
    end if
    return __when_tmp1

end function

function TakingSequence_take_I_k_(n as Integer) as Object
    __when_tmp2 = invalid
    if n >= m.get_count() then
        __when_tmp2 = m
    else if true then
        __when_tmp2 = TakingSequence_create_Sequence_I_k_(m.get_sequence(), n)
    end if
    return __when_tmp2

end function

function TakingSequence_iterator_k_() as Object
    return Anon_5d1557e6_create_TakingSequence_k_(m)
end function

function TakingSequence_get_sequence_k_() as Object
    return m.sequence
end function

function TakingSequence_get_count_k_() as Integer
    return m.count
end function

function TakingWhileSequence_create_Sequence_Function1Z_k_(sequence as Object, predicate as Object) as Object
    this = {}
    this.__type = "TakingWhileSequence"
    this.__proto = ["TakingWhileSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = TakingWhileSequence_iterator_k_
    this.get_sequence = TakingWhileSequence_get_sequence_k_
    this.get_predicate = TakingWhileSequence_get_predicate_k_
    this.sequence = sequence
    this.predicate = predicate
    return this
end function

function TakingWhileSequence_iterator_k_() as Object
    return Anon_4f2fcbe0_create_TakingWhileSequence_k_(m)
end function

function TakingWhileSequence_get_sequence_k_() as Object
    return m.sequence
end function

function TakingWhileSequence_get_predicate_k_() as Object
    return m.predicate
end function

function DroppingSequence_create_Sequence_I_k_(sequence as Object, count as Integer) as Object
    this = {}
    this.__type = "DroppingSequence"
    this.__proto = ["DroppingSequence", "Sequence", "DropTakeSequence"]
    this.__id = __kotlin_nextObjectId()
    this.drop_I_k_ = DroppingSequence_drop_I_k_
    this.take_I_k_ = DroppingSequence_take_I_k_
    this.iterator_k_ = DroppingSequence_iterator_k_
    this.get_sequence = DroppingSequence_get_sequence_k_
    this.get_count = DroppingSequence_get_count_k_
    this.sequence = sequence
    this.count = count
    return this
end function

function DroppingSequence_drop_I_k_(n as Integer) as Object
    return DroppingSequence_create_Sequence_I_k_(m.get_sequence(), m.get_count() + n)
end function

function DroppingSequence_take_I_k_(n as Integer) as Object
    return SubSequence_create_Sequence_I_I_k_(m.get_sequence(), m.get_count(), n)
end function

function DroppingSequence_iterator_k_() as Object
    return Anon_5482416d_create_DroppingSequence_k_(m)
end function

function DroppingSequence_get_sequence_k_() as Object
    return m.sequence
end function

function DroppingSequence_get_count_k_() as Integer
    return m.count
end function

function DroppingWhileSequence_create_Sequence_Function1Z_k_(sequence as Object, predicate as Object) as Object
    this = {}
    this.__type = "DroppingWhileSequence"
    this.__proto = ["DroppingWhileSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = DroppingWhileSequence_iterator_k_
    this.get_sequence = DroppingWhileSequence_get_sequence_k_
    this.get_predicate = DroppingWhileSequence_get_predicate_k_
    this.sequence = sequence
    this.predicate = predicate
    return this
end function

function DroppingWhileSequence_iterator_k_() as Object
    return Anon_406a7eb9_create_DroppingWhileSequence_k_(m)
end function

function DroppingWhileSequence_get_sequence_k_() as Object
    return m.sequence
end function

function DroppingWhileSequence_get_predicate_k_() as Object
    return m.predicate
end function

function SubSequence_create_Sequence_I_I_k_(sequence as Object, startIndex as Integer, endIndex as Integer) as Object
    this = {}
    this.__type = "SubSequence"
    this.__proto = ["SubSequence", "Sequence", "DropTakeSequence"]
    this.__id = __kotlin_nextObjectId()
    this.drop_I_k_ = SubSequence_drop_I_k_
    this.take_I_k_ = SubSequence_take_I_k_
    this.iterator_k_ = SubSequence_iterator_k_
    this.get_sequence = SubSequence_get_sequence_k_
    this.get_startIndex = SubSequence_get_startIndex_k_
    this.get_endIndex = SubSequence_get_endIndex_k_
    this.get_count = SubSequence_get_count_k_
    this.sequence = sequence
    this.startIndex = startIndex
    this.endIndex = endIndex
    return this
end function

function SubSequence_drop_I_k_(n as Integer) as Object
    __when_tmp3 = invalid
    if n >= m.get_count() then
        __when_tmp3 = emptySequence_k_()
    else if true then
        __when_tmp3 = SubSequence_create_Sequence_I_I_k_(m.get_sequence(), m.get_startIndex() + n, m.get_endIndex())
    end if
    return __when_tmp3

end function

function SubSequence_take_I_k_(n as Integer) as Object
    __when_tmp4 = invalid
    if n >= m.get_count() then
        __when_tmp4 = m
    else if true then
        __when_tmp4 = SubSequence_create_Sequence_I_I_k_(m.get_sequence(), m.get_startIndex(), m.get_startIndex() + n)
    end if
    return __when_tmp4

end function

function SubSequence_iterator_k_() as Object
    return Anon_5fe91184_create_SubSequence_k_(m)
end function

function SubSequence_get_sequence_k_() as Object
    return m.sequence
end function

function SubSequence_get_startIndex_k_() as Integer
    return m.startIndex
end function

function SubSequence_get_endIndex_k_() as Integer
    return m.endIndex
end function

function SubSequence_get_count_k_() as Integer
    return m.get_endIndex() - m.get_startIndex()
end function

function DistinctSequence_create_Sequence_Function1_k_(source as Object, keySelector as Object) as Object
    this = {}
    this.__type = "DistinctSequence"
    this.__proto = ["DistinctSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = DistinctSequence_iterator_k_
    this.get_source = DistinctSequence_get_source_k_
    this.get_keySelector = DistinctSequence_get_keySelector_k_
    this.source = source
    this.keySelector = keySelector
    return this
end function

function DistinctSequence_iterator_k_() as Object
    return Anon_2eef9bff_create_DistinctSequence_k_(m)
end function

function DistinctSequence_get_source_k_() as Object
    return m.source
end function

function DistinctSequence_get_keySelector_k_() as Object
    return m.keySelector
end function

function MergingSequence_create_Sequence_Sequence_Function2_k_(sequence1 as Object, sequence2 as Object, transform as Object) as Object
    this = {}
    this.__type = "MergingSequence"
    this.__proto = ["MergingSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = MergingSequence_iterator_k_
    this.get_sequence1 = MergingSequence_get_sequence1_k_
    this.get_sequence2 = MergingSequence_get_sequence2_k_
    this.get_transform = MergingSequence_get_transform_k_
    this.sequence1 = sequence1
    this.sequence2 = sequence2
    this.transform = transform
    return this
end function

function MergingSequence_iterator_k_() as Object
    return Anon_70b25e9b_create_MergingSequence_k_(m)
end function

function MergingSequence_get_sequence1_k_() as Object
    return m.sequence1
end function

function MergingSequence_get_sequence2_k_() as Object
    return m.sequence2
end function

function MergingSequence_get_transform_k_() as Object
    return m.transform
end function

function Anon_23748430_create_TransformingSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_23748430"
    this.__proto = ["Anon_23748430", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.next_k_ = Anon_23748430_next_k_
    this.hasNext_k_ = Anon_23748430_hasNext_k_
    this.get_iterator = Anon_23748430_get_iterator_k_
    this.iterator = this_0.get_sequence().iterator_k_()
    this.this_0 = this_0
    return this
end function

function Anon_23748430_next_k_() as Dynamic
    return m.this_0.get_transformer().invoke(m.get_iterator().next_k_())
end function

function Anon_23748430_hasNext_k_() as Boolean
    return m.get_iterator().hasNext_k_()
end function

function Anon_23748430_get_iterator_k_() as Object
    return m.iterator
end function

function Anon_117a82e5_create_TransformingIndexedSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_117a82e5"
    this.__proto = ["Anon_117a82e5", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.next_k_ = Anon_117a82e5_next_k_
    this.hasNext_k_ = Anon_117a82e5_hasNext_k_
    this.get_iterator = Anon_117a82e5_get_iterator_k_
    this.get_index = Anon_117a82e5_get_index_k_
    this.set_index = Anon_117a82e5_set_index_I_k_
    this.iterator = this_0.get_sequence().iterator_k_()
    this.index = 0
    this.this_0 = this_0
    return this
end function

function Anon_117a82e5_next_k_() as Dynamic
    __incr_tmp_25 = m.get_index()
    m.set_index(__incr_tmp_25 + 1)
    return m.this_0.get_transformer().invoke(__incr_tmp_25, m.get_iterator().next_k_())

end function

function Anon_117a82e5_hasNext_k_() as Boolean
    return m.get_iterator().hasNext_k_()
end function

function Anon_117a82e5_get_iterator_k_() as Object
    return m.iterator
end function

function Anon_117a82e5_get_index_k_() as Integer
    return m.index
end function

sub Anon_117a82e5_set_index_I_k_(value as Integer)
    m.index = value
end sub

function Anon_5b68827c_create_FilteringSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_5b68827c"
    this.__proto = ["Anon_5b68827c", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.calcNext_k_ = Anon_5b68827c_calcNext_k_
    this.next_k_ = Anon_5b68827c_next_k_
    this.hasNext_k_ = Anon_5b68827c_hasNext_k_
    this.get_iterator = Anon_5b68827c_get_iterator_k_
    this.get_nextState = Anon_5b68827c_get_nextState_k_
    this.set_nextState = Anon_5b68827c_set_nextState_I_k_
    this.get_nextItem = Anon_5b68827c_get_nextItem_k_
    this.set_nextItem = Anon_5b68827c_set_nextItem_AnyN_k_
    this.iterator = this_0.get_sequence().iterator_k_()
    this.nextState = -1
    this.nextItem = invalid
    this.this_0 = this_0
    return this
end function

sub Anon_5b68827c_calcNext_k_()
    while m.get_iterator().hasNext_k_()
        item = m.get_iterator().next_k_()
        if m.this_0.get_predicate().invoke(item) = m.this_0.get_sendWhen() then
            m.set_nextItem(item)
            m.set_nextState(1)
            return
        end if
    end while
    m.set_nextState(0)
end sub

function Anon_5b68827c_next_k_() as Dynamic
    if m.get_nextState() = -1 then
        m.calcNext_k_()
    end if
    if m.get_nextState() = 0 then
        throw NoSuchElementException_create_k_()
    end if
    result = m.get_nextItem()
    m.set_nextItem(invalid)
    m.set_nextState(-1)
    return result
end function

function Anon_5b68827c_hasNext_k_() as Boolean
    if m.get_nextState() = -1 then
        m.calcNext_k_()
    end if
    return m.get_nextState() = 1
end function

function Anon_5b68827c_get_iterator_k_() as Object
    return m.iterator
end function

function Anon_5b68827c_get_nextState_k_() as Integer
    return m.nextState
end function

sub Anon_5b68827c_set_nextState_I_k_(value as Integer)
    m.nextState = value
end sub

function Anon_5b68827c_get_nextItem_k_() as Dynamic
    return m.nextItem
end function

sub Anon_5b68827c_set_nextItem_AnyN_k_(value as Dynamic)
    m.nextItem = value
end sub

function Anon_6625d4aa_create_FilteringIndexedSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_6625d4aa"
    this.__proto = ["Anon_6625d4aa", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.calcNext_k_ = Anon_6625d4aa_calcNext_k_
    this.next_k_ = Anon_6625d4aa_next_k_
    this.hasNext_k_ = Anon_6625d4aa_hasNext_k_
    this.get_iterator = Anon_6625d4aa_get_iterator_k_
    this.get_index = Anon_6625d4aa_get_index_k_
    this.set_index = Anon_6625d4aa_set_index_I_k_
    this.get_nextState = Anon_6625d4aa_get_nextState_k_
    this.set_nextState = Anon_6625d4aa_set_nextState_I_k_
    this.get_nextItem = Anon_6625d4aa_get_nextItem_k_
    this.set_nextItem = Anon_6625d4aa_set_nextItem_AnyN_k_
    this.iterator = this_0.get_sequence().iterator_k_()
    this.index = 0
    this.nextState = -1
    this.nextItem = invalid
    this.this_0 = this_0
    return this
end function

sub Anon_6625d4aa_calcNext_k_()
    while m.get_iterator().hasNext_k_()
        item = m.get_iterator().next_k_()
        __incr_tmp_26 = m.get_index()
        m.set_index(__incr_tmp_26 + 1)
        if m.this_0.get_predicate().invoke(__incr_tmp_26, item) = m.this_0.get_sendWhen() then
            m.set_nextItem(item)
            m.set_nextState(1)
            return
        end if

    end while
    m.set_nextState(0)
end sub

function Anon_6625d4aa_next_k_() as Dynamic
    if m.get_nextState() = -1 then
        m.calcNext_k_()
    end if
    if m.get_nextState() = 0 then
        throw NoSuchElementException_create_k_()
    end if
    result = m.get_nextItem()
    m.set_nextItem(invalid)
    m.set_nextState(-1)
    return result
end function

function Anon_6625d4aa_hasNext_k_() as Boolean
    if m.get_nextState() = -1 then
        m.calcNext_k_()
    end if
    return m.get_nextState() = 1
end function

function Anon_6625d4aa_get_iterator_k_() as Object
    return m.iterator
end function

function Anon_6625d4aa_get_index_k_() as Integer
    return m.index
end function

sub Anon_6625d4aa_set_index_I_k_(value as Integer)
    m.index = value
end sub

function Anon_6625d4aa_get_nextState_k_() as Integer
    return m.nextState
end function

sub Anon_6625d4aa_set_nextState_I_k_(value as Integer)
    m.nextState = value
end sub

function Anon_6625d4aa_get_nextItem_k_() as Dynamic
    return m.nextItem
end function

sub Anon_6625d4aa_set_nextItem_AnyN_k_(value as Dynamic)
    m.nextItem = value
end sub

function Anon_e283976_create_FlatteningSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_e283976"
    this.__proto = ["Anon_e283976", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.next_k_ = Anon_e283976_next_k_
    this.hasNext_k_ = Anon_e283976_hasNext_k_
    this.ensureItemIterator_k_ = Anon_e283976_ensureItemIterator_k_
    this.get_iterator = Anon_e283976_get_iterator_k_
    this.get_itemIterator = Anon_e283976_get_itemIterator_k_
    this.set_itemIterator = Anon_e283976_set_itemIterator_IteratorN_k_
    this.iterator = this_0.get_sequence().iterator_k_()
    this.itemIterator = invalid
    this.this_0 = this_0
    return this
end function

function Anon_e283976_next_k_() as Dynamic
    if not m.ensureItemIterator_k_() then
        throw NoSuchElementException_create_k_()
    end if
    return m.get_itemIterator().next_k_()
end function

function Anon_e283976_hasNext_k_() as Boolean
    return m.ensureItemIterator_k_()
end function

function Anon_e283976_ensureItemIterator_k_() as Boolean
    tmp0_safe_receiver = m.get_itemIterator()
    __when_tmp0 = invalid
    if tmp0_safe_receiver = invalid then
        __when_tmp0 = invalid
    else if true then
        __when_tmp0 = tmp0_safe_receiver.hasNext_k_()
    end if
    if __when_tmp0 = false then
        m.set_itemIterator(invalid)
    end if

    while m.get_itemIterator() = invalid
        if not m.get_iterator().hasNext_k_() then
            return false
        else if true then
            element = m.get_iterator().next_k_()
            nextItemIterator = m.this_0.get_iterator().invoke(m.this_0.get_transformer().invoke(element))
            if nextItemIterator.hasNext_k_() then
                m.set_itemIterator(nextItemIterator)
                return true
            end if
        end if
    end while
    return true
end function

function Anon_e283976_get_iterator_k_() as Object
    return m.iterator
end function

function Anon_e283976_get_itemIterator_k_() as Dynamic
    return m.itemIterator
end function

sub Anon_e283976_set_itemIterator_IteratorN_k_(value as Dynamic)
    m.itemIterator = value
end sub

function Anon_5d1557e6_create_TakingSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_5d1557e6"
    this.__proto = ["Anon_5d1557e6", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.next_k_ = Anon_5d1557e6_next_k_
    this.hasNext_k_ = Anon_5d1557e6_hasNext_k_
    this.get_left = Anon_5d1557e6_get_left_k_
    this.set_left = Anon_5d1557e6_set_left_I_k_
    this.get_iterator = Anon_5d1557e6_get_iterator_k_
    this.left = this_0.get_count()
    this.iterator = this_0.get_sequence().iterator_k_()
    return this
end function

function Anon_5d1557e6_next_k_() as Dynamic
    if m.get_left() = 0 then
        throw NoSuchElementException_create_k_()
    end if
    m.set_left(m.get_left() - 1)
    return m.get_iterator().next_k_()
end function

function Anon_5d1557e6_hasNext_k_() as Boolean
    return (m.get_left() > 0) and m.get_iterator().hasNext_k_()
end function

function Anon_5d1557e6_get_left_k_() as Integer
    return m.left
end function

sub Anon_5d1557e6_set_left_I_k_(value as Integer)
    m.left = value
end sub

function Anon_5d1557e6_get_iterator_k_() as Object
    return m.iterator
end function

function Anon_4f2fcbe0_create_TakingWhileSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_4f2fcbe0"
    this.__proto = ["Anon_4f2fcbe0", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.calcNext_k_ = Anon_4f2fcbe0_calcNext_k_
    this.next_k_ = Anon_4f2fcbe0_next_k_
    this.hasNext_k_ = Anon_4f2fcbe0_hasNext_k_
    this.get_iterator = Anon_4f2fcbe0_get_iterator_k_
    this.get_nextState = Anon_4f2fcbe0_get_nextState_k_
    this.set_nextState = Anon_4f2fcbe0_set_nextState_I_k_
    this.get_nextItem = Anon_4f2fcbe0_get_nextItem_k_
    this.set_nextItem = Anon_4f2fcbe0_set_nextItem_AnyN_k_
    this.iterator = this_0.get_sequence().iterator_k_()
    this.nextState = -1
    this.nextItem = invalid
    this.this_0 = this_0
    return this
end function

sub Anon_4f2fcbe0_calcNext_k_()
    if m.get_iterator().hasNext_k_() then
        item = m.get_iterator().next_k_()
        if m.this_0.get_predicate().invoke(item) then
            m.set_nextState(1)
            m.set_nextItem(item)
            return
        end if
    end if
    m.set_nextState(0)
end sub

function Anon_4f2fcbe0_next_k_() as Dynamic
    if m.get_nextState() = -1 then
        m.calcNext_k_()
    end if
    if m.get_nextState() = 0 then
        throw NoSuchElementException_create_k_()
    end if
    result = m.get_nextItem()
    m.set_nextItem(invalid)
    m.set_nextState(-1)
    return result
end function

function Anon_4f2fcbe0_hasNext_k_() as Boolean
    if m.get_nextState() = -1 then
        m.calcNext_k_()
    end if
    return m.get_nextState() = 1
end function

function Anon_4f2fcbe0_get_iterator_k_() as Object
    return m.iterator
end function

function Anon_4f2fcbe0_get_nextState_k_() as Integer
    return m.nextState
end function

sub Anon_4f2fcbe0_set_nextState_I_k_(value as Integer)
    m.nextState = value
end sub

function Anon_4f2fcbe0_get_nextItem_k_() as Dynamic
    return m.nextItem
end function

sub Anon_4f2fcbe0_set_nextItem_AnyN_k_(value as Dynamic)
    m.nextItem = value
end sub

function Anon_5482416d_create_DroppingSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_5482416d"
    this.__proto = ["Anon_5482416d", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.drop_k_ = Anon_5482416d_drop_k_
    this.next_k_ = Anon_5482416d_next_k_
    this.hasNext_k_ = Anon_5482416d_hasNext_k_
    this.get_iterator = Anon_5482416d_get_iterator_k_
    this.get_left = Anon_5482416d_get_left_k_
    this.set_left = Anon_5482416d_set_left_I_k_
    this.iterator = this_0.get_sequence().iterator_k_()
    this.left = this_0.get_count()
    return this
end function

sub Anon_5482416d_drop_k_()
    while (m.get_left() > 0) and m.get_iterator().hasNext_k_()
        m.get_iterator().next_k_()
        m.set_left(m.get_left() - 1)
    end while
end sub

function Anon_5482416d_next_k_() as Dynamic
    m.drop_k_()
    return m.get_iterator().next_k_()
end function

function Anon_5482416d_hasNext_k_() as Boolean
    m.drop_k_()
    return m.get_iterator().hasNext_k_()
end function

function Anon_5482416d_get_iterator_k_() as Object
    return m.iterator
end function

function Anon_5482416d_get_left_k_() as Integer
    return m.left
end function

sub Anon_5482416d_set_left_I_k_(value as Integer)
    m.left = value
end sub

function Anon_406a7eb9_create_DroppingWhileSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_406a7eb9"
    this.__proto = ["Anon_406a7eb9", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.drop_k_ = Anon_406a7eb9_drop_k_
    this.next_k_ = Anon_406a7eb9_next_k_
    this.hasNext_k_ = Anon_406a7eb9_hasNext_k_
    this.get_iterator = Anon_406a7eb9_get_iterator_k_
    this.get_dropState = Anon_406a7eb9_get_dropState_k_
    this.set_dropState = Anon_406a7eb9_set_dropState_I_k_
    this.get_nextItem = Anon_406a7eb9_get_nextItem_k_
    this.set_nextItem = Anon_406a7eb9_set_nextItem_AnyN_k_
    this.iterator = this_0.get_sequence().iterator_k_()
    this.dropState = -1
    this.nextItem = invalid
    this.this_0 = this_0
    return this
end function

sub Anon_406a7eb9_drop_k_()
    while m.get_iterator().hasNext_k_()
        item = m.get_iterator().next_k_()
        if not m.this_0.get_predicate().invoke(item) then
            m.set_nextItem(item)
            m.set_dropState(1)
            return
        end if
    end while
    m.set_dropState(0)
end sub

function Anon_406a7eb9_next_k_() as Dynamic
    if m.get_dropState() = -1 then
        m.drop_k_()
    end if
    if m.get_dropState() = 1 then
        result = m.get_nextItem()
        m.set_nextItem(invalid)
        m.set_dropState(0)
        return result
    end if
    return m.get_iterator().next_k_()
end function

function Anon_406a7eb9_hasNext_k_() as Boolean
    if m.get_dropState() = -1 then
        m.drop_k_()
    end if
    return (m.get_dropState() = 1) or m.get_iterator().hasNext_k_()
end function

function Anon_406a7eb9_get_iterator_k_() as Object
    return m.iterator
end function

function Anon_406a7eb9_get_dropState_k_() as Integer
    return m.dropState
end function

sub Anon_406a7eb9_set_dropState_I_k_(value as Integer)
    m.dropState = value
end sub

function Anon_406a7eb9_get_nextItem_k_() as Dynamic
    return m.nextItem
end function

sub Anon_406a7eb9_set_nextItem_AnyN_k_(value as Dynamic)
    m.nextItem = value
end sub

function Anon_5fe91184_create_SubSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_5fe91184"
    this.__proto = ["Anon_5fe91184", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.drop_k_ = Anon_5fe91184_drop_k_
    this.hasNext_k_ = Anon_5fe91184_hasNext_k_
    this.next_k_ = Anon_5fe91184_next_k_
    this.get_iterator = Anon_5fe91184_get_iterator_k_
    this.get_position = Anon_5fe91184_get_position_k_
    this.set_position = Anon_5fe91184_set_position_I_k_
    this.iterator = this_0.get_sequence().iterator_k_()
    this.position = 0
    this.this_0 = this_0
    return this
end function

sub Anon_5fe91184_drop_k_()
    while (m.get_position() < m.this_0.get_startIndex()) and m.get_iterator().hasNext_k_()
        m.get_iterator().next_k_()
        m.set_position(m.get_position() + 1)
    end while
end sub

function Anon_5fe91184_hasNext_k_() as Boolean
    m.drop_k_()
    return (m.get_position() < m.this_0.get_endIndex()) and m.get_iterator().hasNext_k_()
end function

function Anon_5fe91184_next_k_() as Dynamic
    m.drop_k_()
    if m.get_position() >= m.this_0.get_endIndex() then
        throw NoSuchElementException_create_k_()
    end if
    m.set_position(m.get_position() + 1)
    return m.get_iterator().next_k_()
end function

function Anon_5fe91184_get_iterator_k_() as Object
    return m.iterator
end function

function Anon_5fe91184_get_position_k_() as Integer
    return m.position
end function

sub Anon_5fe91184_set_position_I_k_(value as Integer)
    m.position = value
end sub

function Anon_2eef9bff_create_DistinctSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_2eef9bff"
    this.__proto = ["Anon_2eef9bff", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.calcNext_k_ = Anon_2eef9bff_calcNext_k_
    this.hasNext_k_ = Anon_2eef9bff_hasNext_k_
    this.next_k_ = Anon_2eef9bff_next_k_
    this.get_sourceIterator = Anon_2eef9bff_get_sourceIterator_k_
    this.get_observed = Anon_2eef9bff_get_observed_k_
    this.get_nextValue = Anon_2eef9bff_get_nextValue_k_
    this.set_nextValue = Anon_2eef9bff_set_nextValue_AnyN_k_
    this.get_nextState = Anon_2eef9bff_get_nextState_k_
    this.set_nextState = Anon_2eef9bff_set_nextState_I_k_
    this.sourceIterator = this_0.get_source().iterator_k_()
    this.observed = HashSet_create_k_()
    this.nextValue = invalid
    this.nextState = -1
    this.this_0 = this_0
    return this
end function

sub Anon_2eef9bff_calcNext_k_()
    while m.get_sourceIterator().hasNext_k_()
        next_ = m.get_sourceIterator().next_k_()
        key = m.this_0.get_keySelector().invoke(next_)
        if m.get_observed().add_AnyN_k_(key) then
            m.set_nextValue(next_)
            m.set_nextState(1)
            return
        end if
    end while
    m.set_nextState(0)
end sub

function Anon_2eef9bff_hasNext_k_() as Boolean
    if m.get_nextState() = -1 then
        m.calcNext_k_()
    end if
    return m.get_nextState() = 1
end function

function Anon_2eef9bff_next_k_() as Dynamic
    if m.get_nextState() = -1 then
        m.calcNext_k_()
    end if
    if m.get_nextState() = 0 then
        throw NoSuchElementException_create_k_()
    end if
    value = m.get_nextValue()
    m.set_nextValue(invalid)
    m.set_nextState(-1)
    return value
end function

function Anon_2eef9bff_get_sourceIterator_k_() as Object
    return m.sourceIterator
end function

function Anon_2eef9bff_get_observed_k_() as Object
    return m.observed
end function

function Anon_2eef9bff_get_nextValue_k_() as Dynamic
    return m.nextValue
end function

sub Anon_2eef9bff_set_nextValue_AnyN_k_(value as Dynamic)
    m.nextValue = value
end sub

function Anon_2eef9bff_get_nextState_k_() as Integer
    return m.nextState
end function

sub Anon_2eef9bff_set_nextState_I_k_(value as Integer)
    m.nextState = value
end sub

function Anon_70b25e9b_create_MergingSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_70b25e9b"
    this.__proto = ["Anon_70b25e9b", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = Anon_70b25e9b_hasNext_k_
    this.next_k_ = Anon_70b25e9b_next_k_
    this.get_iterator1 = Anon_70b25e9b_get_iterator1_k_
    this.get_iterator2 = Anon_70b25e9b_get_iterator2_k_
    this.iterator1 = this_0.get_sequence1().iterator_k_()
    this.iterator2 = this_0.get_sequence2().iterator_k_()
    this.this_0 = this_0
    return this
end function

function Anon_70b25e9b_hasNext_k_() as Boolean
    return m.get_iterator1().hasNext_k_() and m.get_iterator2().hasNext_k_()
end function

function Anon_70b25e9b_next_k_() as Dynamic
    return m.this_0.get_transform().invoke(m.get_iterator1().next_k_(), m.get_iterator2().next_k_())
end function

function Anon_70b25e9b_get_iterator1_k_() as Object
    return m.iterator1
end function

function Anon_70b25e9b_get_iterator2_k_() as Object
    return m.iterator2
end function
