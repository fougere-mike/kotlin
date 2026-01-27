function TransformingSequence_create_Sequence_Function1_k_(sequence as Object, transformer as Object) as Object
    this = {}
    this.__type = "TransformingSequence"
    this.__proto = ["TransformingSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = TransformingSequence_iterator_k_
    this.flatten_Function1Iterator_k_ = TransformingSequence_flatten_Function1Iterator_k_
    this.__get_sequence = TransformingSequence___get_sequence_k_
    this.__get_transformer = TransformingSequence___get_transformer_k_
    this.sequence = sequence
    this.transformer = transformer
    return this
end function

function TransformingSequence_iterator_k_() as Object
    return Anon_3b94279c_create_TransformingSequence_k_(m)
end function

function TransformingSequence_flatten_Function1Iterator_k_(iterator as Object) as Object
    return FlatteningSequence_create_Sequence_Function1_Function1Iterator_k_(m.__get_sequence(), m.__get_transformer(), iterator)
end function

function TransformingSequence___get_sequence_k_() as Object
    return m.sequence
end function

function TransformingSequence___get_transformer_k_() as Object
    return m.transformer
end function

function TransformingIndexedSequence_create_Sequence_Function2I_k_(sequence as Object, transformer as Object) as Object
    this = {}
    this.__type = "TransformingIndexedSequence"
    this.__proto = ["TransformingIndexedSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = TransformingIndexedSequence_iterator_k_
    this.__get_sequence = TransformingIndexedSequence___get_sequence_k_
    this.__get_transformer = TransformingIndexedSequence___get_transformer_k_
    this.sequence = sequence
    this.transformer = transformer
    return this
end function

function TransformingIndexedSequence_iterator_k_() as Object
    return Anon_53fb1669_create_TransformingIndexedSequence_k_(m)
end function

function TransformingIndexedSequence___get_sequence_k_() as Object
    return m.sequence
end function

function TransformingIndexedSequence___get_transformer_k_() as Object
    return m.transformer
end function

function FilteringSequence_create_Sequence_Z_Function1Z_k_(sequence as Object, sendWhen = true, predicate = invalid) as Object
    this = {}
    this.__type = "FilteringSequence"
    this.__proto = ["FilteringSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = FilteringSequence_iterator_k_
    this.__get_sequence = FilteringSequence___get_sequence_k_
    this.__get_sendWhen = FilteringSequence___get_sendWhen_k_
    this.__get_predicate = FilteringSequence___get_predicate_k_
    this.sequence = sequence
    this.sendWhen = sendWhen
    this.predicate = predicate
    return this
end function

function FilteringSequence_iterator_k_() as Object
    return Anon_7ffa637d_create_FilteringSequence_k_(m)
end function

function FilteringSequence___get_sequence_k_() as Object
    return m.sequence
end function

function FilteringSequence___get_sendWhen_k_() as Boolean
    return m.sendWhen
end function

function FilteringSequence___get_predicate_k_() as Object
    return m.predicate
end function

function FilteringIndexedSequence_create_Sequence_Z_Function2IZ_k_(sequence as Object, sendWhen = true, predicate = invalid) as Object
    this = {}
    this.__type = "FilteringIndexedSequence"
    this.__proto = ["FilteringIndexedSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = FilteringIndexedSequence_iterator_k_
    this.__get_sequence = FilteringIndexedSequence___get_sequence_k_
    this.__get_sendWhen = FilteringIndexedSequence___get_sendWhen_k_
    this.__get_predicate = FilteringIndexedSequence___get_predicate_k_
    this.sequence = sequence
    this.sendWhen = sendWhen
    this.predicate = predicate
    return this
end function

function FilteringIndexedSequence_iterator_k_() as Object
    return Anon_35adf9c5_create_FilteringIndexedSequence_k_(m)
end function

function FilteringIndexedSequence___get_sequence_k_() as Object
    return m.sequence
end function

function FilteringIndexedSequence___get_sendWhen_k_() as Boolean
    return m.sendWhen
end function

function FilteringIndexedSequence___get_predicate_k_() as Object
    return m.predicate
end function

function FlatteningSequence_create_Sequence_Function1_Function1Iterator_k_(sequence as Object, transformer as Object, iterator as Object) as Object
    this = {}
    this.__type = "FlatteningSequence"
    this.__proto = ["FlatteningSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = FlatteningSequence_iterator_k_
    this.__get_sequence = FlatteningSequence___get_sequence_k_
    this.__get_transformer = FlatteningSequence___get_transformer_k_
    this.__get_iterator = FlatteningSequence___get_iterator_k_
    this.sequence = sequence
    this.transformer = transformer
    this.iterator = iterator
    return this
end function

function FlatteningSequence_iterator_k_() as Object
    return Anon_5e131807_create_FlatteningSequence_k_(m)
end function

function FlatteningSequence___get_sequence_k_() as Object
    return m.sequence
end function

function FlatteningSequence___get_transformer_k_() as Object
    return m.transformer
end function

function FlatteningSequence___get_iterator_k_() as Object
    return m.iterator
end function

function TakingSequence_create_Sequence_I_k_(sequence as Object, count as Integer) as Object
    this = {}
    this.__type = "TakingSequence"
    this.__proto = ["TakingSequence", "Sequence", "DropTakeSequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.drop_I_k_ = TakingSequence_drop_I_k_
    this.take_I_k_ = TakingSequence_take_I_k_
    this.iterator_k_ = TakingSequence_iterator_k_
    this.__get_sequence = TakingSequence___get_sequence_k_
    this.__get_count = TakingSequence___get_count_k_
    this.sequence = sequence
    this.count = count
    return this
end function

function TakingSequence_drop_I_k_(n as Integer) as Object
    __when_tmp1 = invalid
    if n >= m.__get_count() then
        __when_tmp1 = emptySequence_k_()
    else if true then
        __when_tmp1 = SubSequence_create_Sequence_I_I_k_(m.__get_sequence(), n, m.__get_count())
    end if
    return __when_tmp1

end function

function TakingSequence_take_I_k_(n as Integer) as Object
    __when_tmp2 = invalid
    if n >= m.__get_count() then
        __when_tmp2 = m
    else if true then
        __when_tmp2 = TakingSequence_create_Sequence_I_k_(m.__get_sequence(), n)
    end if
    return __when_tmp2

end function

function TakingSequence_iterator_k_() as Object
    return Anon_70608cdf_create_TakingSequence_k_(m)
end function

function TakingSequence___get_sequence_k_() as Object
    return m.sequence
end function

function TakingSequence___get_count_k_() as Integer
    return m.count
end function

function TakingWhileSequence_create_Sequence_Function1Z_k_(sequence as Object, predicate as Object) as Object
    this = {}
    this.__type = "TakingWhileSequence"
    this.__proto = ["TakingWhileSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = TakingWhileSequence_iterator_k_
    this.__get_sequence = TakingWhileSequence___get_sequence_k_
    this.__get_predicate = TakingWhileSequence___get_predicate_k_
    this.sequence = sequence
    this.predicate = predicate
    return this
end function

function TakingWhileSequence_iterator_k_() as Object
    return Anon_4d8e7819_create_TakingWhileSequence_k_(m)
end function

function TakingWhileSequence___get_sequence_k_() as Object
    return m.sequence
end function

function TakingWhileSequence___get_predicate_k_() as Object
    return m.predicate
end function

function DroppingSequence_create_Sequence_I_k_(sequence as Object, count as Integer) as Object
    this = {}
    this.__type = "DroppingSequence"
    this.__proto = ["DroppingSequence", "Sequence", "DropTakeSequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.drop_I_k_ = DroppingSequence_drop_I_k_
    this.take_I_k_ = DroppingSequence_take_I_k_
    this.iterator_k_ = DroppingSequence_iterator_k_
    this.__get_sequence = DroppingSequence___get_sequence_k_
    this.__get_count = DroppingSequence___get_count_k_
    this.sequence = sequence
    this.count = count
    return this
end function

function DroppingSequence_drop_I_k_(n as Integer) as Object
    return DroppingSequence_create_Sequence_I_k_(m.__get_sequence(), m.__get_count() + n)
end function

function DroppingSequence_take_I_k_(n as Integer) as Object
    return SubSequence_create_Sequence_I_I_k_(m.__get_sequence(), m.__get_count(), n)
end function

function DroppingSequence_iterator_k_() as Object
    return Anon_5d42c695_create_DroppingSequence_k_(m)
end function

function DroppingSequence___get_sequence_k_() as Object
    return m.sequence
end function

function DroppingSequence___get_count_k_() as Integer
    return m.count
end function

function DroppingWhileSequence_create_Sequence_Function1Z_k_(sequence as Object, predicate as Object) as Object
    this = {}
    this.__type = "DroppingWhileSequence"
    this.__proto = ["DroppingWhileSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = DroppingWhileSequence_iterator_k_
    this.__get_sequence = DroppingWhileSequence___get_sequence_k_
    this.__get_predicate = DroppingWhileSequence___get_predicate_k_
    this.sequence = sequence
    this.predicate = predicate
    return this
end function

function DroppingWhileSequence_iterator_k_() as Object
    return Anon_147a8d7b_create_DroppingWhileSequence_k_(m)
end function

function DroppingWhileSequence___get_sequence_k_() as Object
    return m.sequence
end function

function DroppingWhileSequence___get_predicate_k_() as Object
    return m.predicate
end function

function SubSequence_create_Sequence_I_I_k_(sequence as Object, startIndex as Integer, endIndex as Integer) as Object
    this = {}
    this.__type = "SubSequence"
    this.__proto = ["SubSequence", "Sequence", "DropTakeSequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.drop_I_k_ = SubSequence_drop_I_k_
    this.take_I_k_ = SubSequence_take_I_k_
    this.iterator_k_ = SubSequence_iterator_k_
    this.__get_sequence = SubSequence___get_sequence_k_
    this.__get_startIndex = SubSequence___get_startIndex_k_
    this.__get_endIndex = SubSequence___get_endIndex_k_
    this.__get_count = SubSequence___get_count_k_
    this.sequence = sequence
    this.startIndex = startIndex
    this.endIndex = endIndex
    return this
end function

function SubSequence_drop_I_k_(n as Integer) as Object
    __when_tmp3 = invalid
    if n >= m.__get_count() then
        __when_tmp3 = emptySequence_k_()
    else if true then
        __when_tmp3 = SubSequence_create_Sequence_I_I_k_(m.__get_sequence(), m.__get_startIndex() + n, m.__get_endIndex())
    end if
    return __when_tmp3

end function

function SubSequence_take_I_k_(n as Integer) as Object
    __when_tmp4 = invalid
    if n >= m.__get_count() then
        __when_tmp4 = m
    else if true then
        __when_tmp4 = SubSequence_create_Sequence_I_I_k_(m.__get_sequence(), m.__get_startIndex(), m.__get_startIndex() + n)
    end if
    return __when_tmp4

end function

function SubSequence_iterator_k_() as Object
    return Anon_29fe8948_create_SubSequence_k_(m)
end function

function SubSequence___get_sequence_k_() as Object
    return m.sequence
end function

function SubSequence___get_startIndex_k_() as Integer
    return m.startIndex
end function

function SubSequence___get_endIndex_k_() as Integer
    return m.endIndex
end function

function SubSequence___get_count_k_() as Integer
    return m.__get_endIndex() - m.__get_startIndex()
end function

function DistinctSequence_create_Sequence_Function1_k_(source as Object, keySelector as Object) as Object
    this = {}
    this.__type = "DistinctSequence"
    this.__proto = ["DistinctSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = DistinctSequence_iterator_k_
    this.__get_source = DistinctSequence___get_source_k_
    this.__get_keySelector = DistinctSequence___get_keySelector_k_
    this.source = source
    this.keySelector = keySelector
    return this
end function

function DistinctSequence_iterator_k_() as Object
    return Anon_3b58dc7_create_DistinctSequence_k_(m)
end function

function DistinctSequence___get_source_k_() as Object
    return m.source
end function

function DistinctSequence___get_keySelector_k_() as Object
    return m.keySelector
end function

function MergingSequence_create_Sequence_Sequence_Function2_k_(sequence1 as Object, sequence2 as Object, transform as Object) as Object
    this = {}
    this.__type = "MergingSequence"
    this.__proto = ["MergingSequence", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = MergingSequence_iterator_k_
    this.__get_sequence1 = MergingSequence___get_sequence1_k_
    this.__get_sequence2 = MergingSequence___get_sequence2_k_
    this.__get_transform = MergingSequence___get_transform_k_
    this.sequence1 = sequence1
    this.sequence2 = sequence2
    this.transform = transform
    return this
end function

function MergingSequence_iterator_k_() as Object
    return Anon_142ae540_create_MergingSequence_k_(m)
end function

function MergingSequence___get_sequence1_k_() as Object
    return m.sequence1
end function

function MergingSequence___get_sequence2_k_() as Object
    return m.sequence2
end function

function MergingSequence___get_transform_k_() as Object
    return m.transform
end function

function Anon_3b94279c_create_TransformingSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_3b94279c"
    this.__proto = ["Anon_3b94279c", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.next_k_ = Anon_3b94279c_next_k_
    this.hasNext_k_ = Anon_3b94279c_hasNext_k_
    this.__get_iterator = Anon_3b94279c___get_iterator_k_
    this.iterator = this_0.__get_sequence().iterator_k_()
    this.this_0 = this_0
    return this
end function

function Anon_3b94279c_next_k_() as Dynamic
    return m.this_0.__get_transformer().invoke_AnyN_k_(m.__get_iterator().next_k_())
end function

function Anon_3b94279c_hasNext_k_() as Boolean
    return m.__get_iterator().hasNext_k_()
end function

function Anon_3b94279c___get_iterator_k_() as Object
    return m.iterator
end function

function Anon_53fb1669_create_TransformingIndexedSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_53fb1669"
    this.__proto = ["Anon_53fb1669", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.next_k_ = Anon_53fb1669_next_k_
    this.hasNext_k_ = Anon_53fb1669_hasNext_k_
    this.__get_iterator = Anon_53fb1669___get_iterator_k_
    this.__get_index = Anon_53fb1669___get_index_k_
    this.__set_index = Anon_53fb1669___set_index_I_k_
    this.iterator = this_0.__get_sequence().iterator_k_()
    this.index = 0
    this.this_0 = this_0
    return this
end function

function Anon_53fb1669_next_k_() as Dynamic
    __incr_tmp_31 = m.__get_index()
    m.__set_index(__incr_tmp_31 + 1)
    return m.this_0.__get_transformer().invoke_AnyN_AnyN_k_(__incr_tmp_31, m.__get_iterator().next_k_())

end function

function Anon_53fb1669_hasNext_k_() as Boolean
    return m.__get_iterator().hasNext_k_()
end function

function Anon_53fb1669___get_iterator_k_() as Object
    return m.iterator
end function

function Anon_53fb1669___get_index_k_() as Integer
    return m.index
end function

sub Anon_53fb1669___set_index_I_k_(value as Integer)
    m.index = value
end sub

function Anon_7ffa637d_create_FilteringSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_7ffa637d"
    this.__proto = ["Anon_7ffa637d", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.calcNext_k_ = Anon_7ffa637d_calcNext_k_
    this.next_k_ = Anon_7ffa637d_next_k_
    this.hasNext_k_ = Anon_7ffa637d_hasNext_k_
    this.__get_iterator = Anon_7ffa637d___get_iterator_k_
    this.__get_nextState = Anon_7ffa637d___get_nextState_k_
    this.__set_nextState = Anon_7ffa637d___set_nextState_I_k_
    this.__get_nextItem = Anon_7ffa637d___get_nextItem_k_
    this.__set_nextItem = Anon_7ffa637d___set_nextItem_AnyN_k_
    this.iterator = this_0.__get_sequence().iterator_k_()
    this.nextState = -1
    this.nextItem = invalid
    this.this_0 = this_0
    return this
end function

sub Anon_7ffa637d_calcNext_k_()
    while m.__get_iterator().hasNext_k_()
        item = m.__get_iterator().next_k_()
        if m.this_0.__get_predicate().invoke_AnyN_k_(item) = m.this_0.__get_sendWhen() then
            m.__set_nextItem(item)
            m.__set_nextState(1)
            return
        end if
    end while
    m.__set_nextState(0)
end sub

function Anon_7ffa637d_next_k_() as Dynamic
    if m.__get_nextState() = -1 then
        m.calcNext_k_()
    end if
    if m.__get_nextState() = 0 then
        throw NoSuchElementException_create_k_()
    end if
    result = m.__get_nextItem()
    m.__set_nextItem(invalid)
    m.__set_nextState(-1)
    return result
end function

function Anon_7ffa637d_hasNext_k_() as Boolean
    if m.__get_nextState() = -1 then
        m.calcNext_k_()
    end if
    return m.__get_nextState() = 1
end function

function Anon_7ffa637d___get_iterator_k_() as Object
    return m.iterator
end function

function Anon_7ffa637d___get_nextState_k_() as Integer
    return m.nextState
end function

sub Anon_7ffa637d___set_nextState_I_k_(value as Integer)
    m.nextState = value
end sub

function Anon_7ffa637d___get_nextItem_k_() as Dynamic
    return m.nextItem
end function

sub Anon_7ffa637d___set_nextItem_AnyN_k_(value as Dynamic)
    m.nextItem = value
end sub

function Anon_35adf9c5_create_FilteringIndexedSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_35adf9c5"
    this.__proto = ["Anon_35adf9c5", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.calcNext_k_ = Anon_35adf9c5_calcNext_k_
    this.next_k_ = Anon_35adf9c5_next_k_
    this.hasNext_k_ = Anon_35adf9c5_hasNext_k_
    this.__get_iterator = Anon_35adf9c5___get_iterator_k_
    this.__get_index = Anon_35adf9c5___get_index_k_
    this.__set_index = Anon_35adf9c5___set_index_I_k_
    this.__get_nextState = Anon_35adf9c5___get_nextState_k_
    this.__set_nextState = Anon_35adf9c5___set_nextState_I_k_
    this.__get_nextItem = Anon_35adf9c5___get_nextItem_k_
    this.__set_nextItem = Anon_35adf9c5___set_nextItem_AnyN_k_
    this.iterator = this_0.__get_sequence().iterator_k_()
    this.index = 0
    this.nextState = -1
    this.nextItem = invalid
    this.this_0 = this_0
    return this
end function

sub Anon_35adf9c5_calcNext_k_()
    while m.__get_iterator().hasNext_k_()
        item = m.__get_iterator().next_k_()
        __incr_tmp_32 = m.__get_index()
        m.__set_index(__incr_tmp_32 + 1)
        if m.this_0.__get_predicate().invoke_AnyN_AnyN_k_(__incr_tmp_32, item) = m.this_0.__get_sendWhen() then
            m.__set_nextItem(item)
            m.__set_nextState(1)
            return
        end if

    end while
    m.__set_nextState(0)
end sub

function Anon_35adf9c5_next_k_() as Dynamic
    if m.__get_nextState() = -1 then
        m.calcNext_k_()
    end if
    if m.__get_nextState() = 0 then
        throw NoSuchElementException_create_k_()
    end if
    result = m.__get_nextItem()
    m.__set_nextItem(invalid)
    m.__set_nextState(-1)
    return result
end function

function Anon_35adf9c5_hasNext_k_() as Boolean
    if m.__get_nextState() = -1 then
        m.calcNext_k_()
    end if
    return m.__get_nextState() = 1
end function

function Anon_35adf9c5___get_iterator_k_() as Object
    return m.iterator
end function

function Anon_35adf9c5___get_index_k_() as Integer
    return m.index
end function

sub Anon_35adf9c5___set_index_I_k_(value as Integer)
    m.index = value
end sub

function Anon_35adf9c5___get_nextState_k_() as Integer
    return m.nextState
end function

sub Anon_35adf9c5___set_nextState_I_k_(value as Integer)
    m.nextState = value
end sub

function Anon_35adf9c5___get_nextItem_k_() as Dynamic
    return m.nextItem
end function

sub Anon_35adf9c5___set_nextItem_AnyN_k_(value as Dynamic)
    m.nextItem = value
end sub

function Anon_5e131807_create_FlatteningSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_5e131807"
    this.__proto = ["Anon_5e131807", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.next_k_ = Anon_5e131807_next_k_
    this.hasNext_k_ = Anon_5e131807_hasNext_k_
    this.ensureItemIterator_k_ = Anon_5e131807_ensureItemIterator_k_
    this.__get_iterator = Anon_5e131807___get_iterator_k_
    this.__get_itemIterator = Anon_5e131807___get_itemIterator_k_
    this.__set_itemIterator = Anon_5e131807___set_itemIterator_IteratorN_k_
    this.iterator = this_0.__get_sequence().iterator_k_()
    this.itemIterator = invalid
    this.this_0 = this_0
    return this
end function

function Anon_5e131807_next_k_() as Dynamic
    if not m.ensureItemIterator_k_() then
        throw NoSuchElementException_create_k_()
    end if
    return m.__get_itemIterator().next_k_()
end function

function Anon_5e131807_hasNext_k_() as Boolean
    return m.ensureItemIterator_k_()
end function

function Anon_5e131807_ensureItemIterator_k_() as Boolean
    tmp0_safe_receiver = m.__get_itemIterator()
    __when_tmp0 = invalid
    if tmp0_safe_receiver = invalid then
        __when_tmp0 = invalid
    else if true then
        __when_tmp0 = tmp0_safe_receiver.hasNext_k_()
    end if
    if __when_tmp0 = false then
        m.__set_itemIterator(invalid)
    end if

    while m.__get_itemIterator() = invalid
        if not m.__get_iterator().hasNext_k_() then
            return false
        else if true then
            element = m.__get_iterator().next_k_()
            nextItemIterator = m.this_0.__get_iterator().invoke_AnyN_k_(m.this_0.__get_transformer().invoke_AnyN_k_(element))
            if nextItemIterator.hasNext_k_() then
                m.__set_itemIterator(nextItemIterator)
                return true
            end if
        end if
    end while
    return true
end function

function Anon_5e131807___get_iterator_k_() as Object
    return m.iterator
end function

function Anon_5e131807___get_itemIterator_k_() as Dynamic
    return m.itemIterator
end function

sub Anon_5e131807___set_itemIterator_IteratorN_k_(value as Dynamic)
    m.itemIterator = value
end sub

function Anon_70608cdf_create_TakingSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_70608cdf"
    this.__proto = ["Anon_70608cdf", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.next_k_ = Anon_70608cdf_next_k_
    this.hasNext_k_ = Anon_70608cdf_hasNext_k_
    this.__get_left = Anon_70608cdf___get_left_k_
    this.__set_left = Anon_70608cdf___set_left_I_k_
    this.__get_iterator = Anon_70608cdf___get_iterator_k_
    this.left = this_0.__get_count()
    this.iterator = this_0.__get_sequence().iterator_k_()
    return this
end function

function Anon_70608cdf_next_k_() as Dynamic
    if m.__get_left() = 0 then
        throw NoSuchElementException_create_k_()
    end if
    m.__set_left(m.__get_left() - 1)
    return m.__get_iterator().next_k_()
end function

function Anon_70608cdf_hasNext_k_() as Boolean
    return (m.__get_left() > 0) and m.__get_iterator().hasNext_k_()
end function

function Anon_70608cdf___get_left_k_() as Integer
    return m.left
end function

sub Anon_70608cdf___set_left_I_k_(value as Integer)
    m.left = value
end sub

function Anon_70608cdf___get_iterator_k_() as Object
    return m.iterator
end function

function Anon_4d8e7819_create_TakingWhileSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_4d8e7819"
    this.__proto = ["Anon_4d8e7819", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.calcNext_k_ = Anon_4d8e7819_calcNext_k_
    this.next_k_ = Anon_4d8e7819_next_k_
    this.hasNext_k_ = Anon_4d8e7819_hasNext_k_
    this.__get_iterator = Anon_4d8e7819___get_iterator_k_
    this.__get_nextState = Anon_4d8e7819___get_nextState_k_
    this.__set_nextState = Anon_4d8e7819___set_nextState_I_k_
    this.__get_nextItem = Anon_4d8e7819___get_nextItem_k_
    this.__set_nextItem = Anon_4d8e7819___set_nextItem_AnyN_k_
    this.iterator = this_0.__get_sequence().iterator_k_()
    this.nextState = -1
    this.nextItem = invalid
    this.this_0 = this_0
    return this
end function

sub Anon_4d8e7819_calcNext_k_()
    if m.__get_iterator().hasNext_k_() then
        item = m.__get_iterator().next_k_()
        if m.this_0.__get_predicate().invoke_AnyN_k_(item) then
            m.__set_nextState(1)
            m.__set_nextItem(item)
            return
        end if
    end if
    m.__set_nextState(0)
end sub

function Anon_4d8e7819_next_k_() as Dynamic
    if m.__get_nextState() = -1 then
        m.calcNext_k_()
    end if
    if m.__get_nextState() = 0 then
        throw NoSuchElementException_create_k_()
    end if
    result = m.__get_nextItem()
    m.__set_nextItem(invalid)
    m.__set_nextState(-1)
    return result
end function

function Anon_4d8e7819_hasNext_k_() as Boolean
    if m.__get_nextState() = -1 then
        m.calcNext_k_()
    end if
    return m.__get_nextState() = 1
end function

function Anon_4d8e7819___get_iterator_k_() as Object
    return m.iterator
end function

function Anon_4d8e7819___get_nextState_k_() as Integer
    return m.nextState
end function

sub Anon_4d8e7819___set_nextState_I_k_(value as Integer)
    m.nextState = value
end sub

function Anon_4d8e7819___get_nextItem_k_() as Dynamic
    return m.nextItem
end function

sub Anon_4d8e7819___set_nextItem_AnyN_k_(value as Dynamic)
    m.nextItem = value
end sub

function Anon_5d42c695_create_DroppingSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_5d42c695"
    this.__proto = ["Anon_5d42c695", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.drop_k_ = Anon_5d42c695_drop_k_
    this.next_k_ = Anon_5d42c695_next_k_
    this.hasNext_k_ = Anon_5d42c695_hasNext_k_
    this.__get_iterator = Anon_5d42c695___get_iterator_k_
    this.__get_left = Anon_5d42c695___get_left_k_
    this.__set_left = Anon_5d42c695___set_left_I_k_
    this.iterator = this_0.__get_sequence().iterator_k_()
    this.left = this_0.__get_count()
    return this
end function

sub Anon_5d42c695_drop_k_()
    while (m.__get_left() > 0) and m.__get_iterator().hasNext_k_()
        m.__get_iterator().next_k_()
        m.__set_left(m.__get_left() - 1)
    end while
end sub

function Anon_5d42c695_next_k_() as Dynamic
    m.drop_k_()
    return m.__get_iterator().next_k_()
end function

function Anon_5d42c695_hasNext_k_() as Boolean
    m.drop_k_()
    return m.__get_iterator().hasNext_k_()
end function

function Anon_5d42c695___get_iterator_k_() as Object
    return m.iterator
end function

function Anon_5d42c695___get_left_k_() as Integer
    return m.left
end function

sub Anon_5d42c695___set_left_I_k_(value as Integer)
    m.left = value
end sub

function Anon_147a8d7b_create_DroppingWhileSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_147a8d7b"
    this.__proto = ["Anon_147a8d7b", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.drop_k_ = Anon_147a8d7b_drop_k_
    this.next_k_ = Anon_147a8d7b_next_k_
    this.hasNext_k_ = Anon_147a8d7b_hasNext_k_
    this.__get_iterator = Anon_147a8d7b___get_iterator_k_
    this.__get_dropState = Anon_147a8d7b___get_dropState_k_
    this.__set_dropState = Anon_147a8d7b___set_dropState_I_k_
    this.__get_nextItem = Anon_147a8d7b___get_nextItem_k_
    this.__set_nextItem = Anon_147a8d7b___set_nextItem_AnyN_k_
    this.iterator = this_0.__get_sequence().iterator_k_()
    this.dropState = -1
    this.nextItem = invalid
    this.this_0 = this_0
    return this
end function

sub Anon_147a8d7b_drop_k_()
    while m.__get_iterator().hasNext_k_()
        item = m.__get_iterator().next_k_()
        if not m.this_0.__get_predicate().invoke_AnyN_k_(item) then
            m.__set_nextItem(item)
            m.__set_dropState(1)
            return
        end if
    end while
    m.__set_dropState(0)
end sub

function Anon_147a8d7b_next_k_() as Dynamic
    if m.__get_dropState() = -1 then
        m.drop_k_()
    end if
    if m.__get_dropState() = 1 then
        result = m.__get_nextItem()
        m.__set_nextItem(invalid)
        m.__set_dropState(0)
        return result
    end if
    return m.__get_iterator().next_k_()
end function

function Anon_147a8d7b_hasNext_k_() as Boolean
    if m.__get_dropState() = -1 then
        m.drop_k_()
    end if
    return (m.__get_dropState() = 1) or m.__get_iterator().hasNext_k_()
end function

function Anon_147a8d7b___get_iterator_k_() as Object
    return m.iterator
end function

function Anon_147a8d7b___get_dropState_k_() as Integer
    return m.dropState
end function

sub Anon_147a8d7b___set_dropState_I_k_(value as Integer)
    m.dropState = value
end sub

function Anon_147a8d7b___get_nextItem_k_() as Dynamic
    return m.nextItem
end function

sub Anon_147a8d7b___set_nextItem_AnyN_k_(value as Dynamic)
    m.nextItem = value
end sub

function Anon_29fe8948_create_SubSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_29fe8948"
    this.__proto = ["Anon_29fe8948", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.drop_k_ = Anon_29fe8948_drop_k_
    this.hasNext_k_ = Anon_29fe8948_hasNext_k_
    this.next_k_ = Anon_29fe8948_next_k_
    this.__get_iterator = Anon_29fe8948___get_iterator_k_
    this.__get_position = Anon_29fe8948___get_position_k_
    this.__set_position = Anon_29fe8948___set_position_I_k_
    this.iterator = this_0.__get_sequence().iterator_k_()
    this.position = 0
    this.this_0 = this_0
    return this
end function

sub Anon_29fe8948_drop_k_()
    while (m.__get_position() < m.this_0.__get_startIndex()) and m.__get_iterator().hasNext_k_()
        m.__get_iterator().next_k_()
        m.__set_position(m.__get_position() + 1)
    end while
end sub

function Anon_29fe8948_hasNext_k_() as Boolean
    m.drop_k_()
    return (m.__get_position() < m.this_0.__get_endIndex()) and m.__get_iterator().hasNext_k_()
end function

function Anon_29fe8948_next_k_() as Dynamic
    m.drop_k_()
    if m.__get_position() >= m.this_0.__get_endIndex() then
        throw NoSuchElementException_create_k_()
    end if
    m.__set_position(m.__get_position() + 1)
    return m.__get_iterator().next_k_()
end function

function Anon_29fe8948___get_iterator_k_() as Object
    return m.iterator
end function

function Anon_29fe8948___get_position_k_() as Integer
    return m.position
end function

sub Anon_29fe8948___set_position_I_k_(value as Integer)
    m.position = value
end sub

function Anon_3b58dc7_create_DistinctSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_3b58dc7"
    this.__proto = ["Anon_3b58dc7", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.calcNext_k_ = Anon_3b58dc7_calcNext_k_
    this.hasNext_k_ = Anon_3b58dc7_hasNext_k_
    this.next_k_ = Anon_3b58dc7_next_k_
    this.__get_sourceIterator = Anon_3b58dc7___get_sourceIterator_k_
    this.__get_observed = Anon_3b58dc7___get_observed_k_
    this.__get_nextValue = Anon_3b58dc7___get_nextValue_k_
    this.__set_nextValue = Anon_3b58dc7___set_nextValue_AnyN_k_
    this.__get_nextState = Anon_3b58dc7___get_nextState_k_
    this.__set_nextState = Anon_3b58dc7___set_nextState_I_k_
    this.sourceIterator = this_0.__get_source().iterator_k_()
    this.observed = HashSet_create_k_()
    this.nextValue = invalid
    this.nextState = -1
    this.this_0 = this_0
    return this
end function

sub Anon_3b58dc7_calcNext_k_()
    while m.__get_sourceIterator().hasNext_k_()
        next_ = m.__get_sourceIterator().next_k_()
        key = m.this_0.__get_keySelector().invoke_AnyN_k_(next_)
        if m.__get_observed().add_AnyN_k_(key) then
            m.__set_nextValue(next_)
            m.__set_nextState(1)
            return
        end if
    end while
    m.__set_nextState(0)
end sub

function Anon_3b58dc7_hasNext_k_() as Boolean
    if m.__get_nextState() = -1 then
        m.calcNext_k_()
    end if
    return m.__get_nextState() = 1
end function

function Anon_3b58dc7_next_k_() as Dynamic
    if m.__get_nextState() = -1 then
        m.calcNext_k_()
    end if
    if m.__get_nextState() = 0 then
        throw NoSuchElementException_create_k_()
    end if
    value = m.__get_nextValue()
    m.__set_nextValue(invalid)
    m.__set_nextState(-1)
    return value
end function

function Anon_3b58dc7___get_sourceIterator_k_() as Object
    return m.sourceIterator
end function

function Anon_3b58dc7___get_observed_k_() as Object
    return m.observed
end function

function Anon_3b58dc7___get_nextValue_k_() as Dynamic
    return m.nextValue
end function

sub Anon_3b58dc7___set_nextValue_AnyN_k_(value as Dynamic)
    m.nextValue = value
end sub

function Anon_3b58dc7___get_nextState_k_() as Integer
    return m.nextState
end function

sub Anon_3b58dc7___set_nextState_I_k_(value as Integer)
    m.nextState = value
end sub

function Anon_142ae540_create_MergingSequence_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_142ae540"
    this.__proto = ["Anon_142ae540", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = Anon_142ae540_hasNext_k_
    this.next_k_ = Anon_142ae540_next_k_
    this.__get_iterator1 = Anon_142ae540___get_iterator1_k_
    this.__get_iterator2 = Anon_142ae540___get_iterator2_k_
    this.iterator1 = this_0.__get_sequence1().iterator_k_()
    this.iterator2 = this_0.__get_sequence2().iterator_k_()
    this.this_0 = this_0
    return this
end function

function Anon_142ae540_hasNext_k_() as Boolean
    return m.__get_iterator1().hasNext_k_() and m.__get_iterator2().hasNext_k_()
end function

function Anon_142ae540_next_k_() as Dynamic
    return m.this_0.__get_transform().invoke_AnyN_AnyN_k_(m.__get_iterator1().next_k_(), m.__get_iterator2().next_k_())
end function

function Anon_142ae540___get_iterator1_k_() as Object
    return m.iterator1
end function

function Anon_142ae540___get_iterator2_k_() as Object
    return m.iterator2
end function
