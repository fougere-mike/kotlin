function toList_rSequence_k_(m as Object) as Object
    return toMutableList_rSequence_k_(m)
end function

function toMutableList_rSequence_k_(m as Object) as Object
    return toCollection_rSequence_Any_k_(m, ArrayList_create_k_())
end function

function toSet_rSequence_k_(m as Object) as Object
    return toCollection_rSequence_Any_k_(m, HashSet_create_k_())
end function

function toMutableSet_rSequence_k_(m as Object) as Object
    return toCollection_rSequence_Any_k_(m, HashSet_create_k_())
end function

function toHashSet_rSequence_k_(m as Object) as Object
    return toCollection_rSequence_Any_k_(m, HashSet_create_k_())
end function

function toCollection_rSequence_Any_k_(m as Object, destination as Object) as Object
    __iter_20 = m.iterator_k_()
    while __iter_20.hasNext_k_()
        item = __iter_20.next_k_()
        destination.add_AnyN_k_(item)

    end while

    return destination
end function

function associate_rSequence_Function1Pair_k_(m as Object, transform as Object) as Object
    return associateTo_rSequence_Any_Function1Pair_k_(m, HashMap_create_k_(), transform)
end function

function associateBy_rSequence_Function1_k_(m as Object, keySelector as Object) as Object
    return associateByTo_rSequence_Any_Function1_k_(m, HashMap_create_k_(), keySelector)
end function

function associateBy_rSequence_Function1_Function1_k_(m as Object, keySelector as Object, valueTransform as Object) as Object
    return associateByTo_rSequence_Any_Function1_Function1_k_(m, HashMap_create_k_(), keySelector, valueTransform)
end function

function associateTo_rSequence_Any_Function1Pair_k_(m as Object, destination as Object, transform as Object) as Object
    __iter_21 = m.iterator_k_()
    while __iter_21.hasNext_k_()
        element = __iter_21.next_k_()
        pair = transform.invoke(element)
        destination.put_AnyN_AnyN_k_(pair.first, pair.second)

    end while

    return destination
end function

function associateByTo_rSequence_Any_Function1_k_(m as Object, destination as Object, keySelector as Object) as Object
    __iter_22 = m.iterator_k_()
    while __iter_22.hasNext_k_()
        element = __iter_22.next_k_()
        destination.put_AnyN_AnyN_k_(keySelector.invoke(element), element)

    end while

    return destination
end function

function associateByTo_rSequence_Any_Function1_Function1_k_(m as Object, destination as Object, keySelector as Object, valueTransform as Object) as Object
    __iter_23 = m.iterator_k_()
    while __iter_23.hasNext_k_()
        element = __iter_23.next_k_()
        destination.put_AnyN_AnyN_k_(keySelector.invoke(element), valueTransform.invoke(element))

    end while

    return destination
end function

function associateWith_rSequence_Function1_k_(m as Object, valueSelector as Object) as Object
    result = HashMap_create_k_()
    return associateWithTo_rSequence_Any_Function1_k_(m, result, valueSelector)
end function

function associateWithTo_rSequence_Any_Function1_k_(m as Object, destination as Object, valueSelector as Object) as Object
    __iter_24 = m.iterator_k_()
    while __iter_24.hasNext_k_()
        element = __iter_24.next_k_()
        destination.put_AnyN_AnyN_k_(element, valueSelector.invoke(element))

    end while

    return destination
end function

function map_rSequence_Function1_k_(m as Object, transform as Object) as Object
    return TransformingSequence_create_Sequence_Function1_k_(m, transform)
end function

function mapIndexed_rSequence_Function2I_k_(m as Object, transform as Object) as Object
    return TransformingIndexedSequence_create_Sequence_Function2I_k_(m, transform)
end function

function filter_rSequence_Function1Z_k_(m as Object, predicate as Object) as Object
    return FilteringSequence_create_Sequence_Z_Function1Z_k_(m, true, predicate)
end function

function filterNot_rSequence_Function1Z_k_(m as Object, predicate as Object) as Object
    return FilteringSequence_create_Sequence_Z_Function1Z_k_(m, false, predicate)
end function

function filterIndexed_rSequence_Function2IZ_k_(m as Object, predicate as Object) as Object
    return FilteringIndexedSequence_create_Sequence_Z_Function2IZ_k_(m, true, predicate)
end function

function filterNotNull_rSequence_k_(m as Object) as Object
    return filter_rSequence_Function1Z_k_(m, {invoke: function(it as Dynamic) as Boolean
        return it <> invalid
    end function})
end function

function flatMap_rSequence_Function1Iterable_k_(m as Object, transform as Object) as Object
    return FlatteningSequence_create_Sequence_Function1_Function1Iterator_k_(m, transform, {invoke: function(it as Object) as Object
        return it.iterator_k_()
    end function})
end function

function flatten_rSequenceSequence_k_(m as Object) as Object
    return flatten_rSequence_Function1Iterator_k_(m, {invoke: function(it as Object) as Object
        return it.iterator_k_()
    end function})
end function

function flatten_rSequenceIterable_k_(m as Object) as Object
    return flatten_rSequence_Function1Iterator_k_(m, {invoke: function(it as Object) as Object
        return it.iterator_k_()
    end function})
end function

function flatten_rSequence_Function1Iterator_k_(m as Object, iterator as Object) as Object
    if __kotlin_isInstanceOf(m, "TransformingSequence") then
        return m.flatten_Function1Iterator_k_(iterator)
    end if
    return FlatteningSequence_create_Sequence_Function1_Function1Iterator_k_(m, {invoke: function(it as Dynamic) as Dynamic
        return it
    end function}, iterator)
end function

function take_rSequence_I_k_(m as Object, n as Integer) as Object
    require_Z_Function0Any_k_(n >= 0, {n: n, invoke: function() as Object
        return ("Requested element count " + __kotlin_numToStr_I_k_(m.n)) + " is less than zero."
    end function})
    __when_tmp0 = invalid
    if n = 0 then
        __when_tmp0 = emptySequence_k_()
    else if __kotlin_isInstanceOf(m, "DropTakeSequence") then
        __when_tmp0 = m.take_I_k_(n)
    else if true then
        __when_tmp0 = TakingSequence_create_Sequence_I_k_(m, n)
    end if
    return __when_tmp0

end function

function takeWhile_rSequence_Function1Z_k_(m as Object, predicate as Object) as Object
    return TakingWhileSequence_create_Sequence_Function1Z_k_(m, predicate)
end function

function drop_rSequence_I_k_(m as Object, n as Integer) as Object
    require_Z_Function0Any_k_(n >= 0, {n: n, invoke: function() as Object
        return ("Requested element count " + __kotlin_numToStr_I_k_(m.n)) + " is less than zero."
    end function})
    __when_tmp1 = invalid
    if n = 0 then
        __when_tmp1 = m
    else if __kotlin_isInstanceOf(m, "DropTakeSequence") then
        __when_tmp1 = m.drop_I_k_(n)
    else if true then
        __when_tmp1 = DroppingSequence_create_Sequence_I_k_(m, n)
    end if
    return __when_tmp1

end function

function dropWhile_rSequence_Function1Z_k_(m as Object, predicate as Object) as Object
    return DroppingWhileSequence_create_Sequence_Function1Z_k_(m, predicate)
end function

function distinct_rSequence_k_(m as Object) as Object
    return distinctBy_rSequence_Function1_k_(m, {invoke: function(it as Dynamic) as Dynamic
        return it
    end function})
end function

function distinctBy_rSequence_Function1_k_(m as Object, selector as Object) as Object
    return DistinctSequence_create_Sequence_Function1_k_(m, selector)
end function

function zip_rSequence_Sequence_k_(m as Object, other as Object) as Object
    return MergingSequence_create_Sequence_Sequence_Function2_k_(m, other, {invoke: function(t1 as Dynamic, t2 as Dynamic) as Object
        return to_rAnyN_AnyN_k_(t1, t2)
    end function})
end function

function zip_rSequence_Sequence_Function2_k_(m as Object, other as Object, transform as Object) as Object
    return MergingSequence_create_Sequence_Sequence_Function2_k_(m, other, transform)
end function

function sorted_rSequence_k_(m as Object) as Object
    return Anon_33c9f46f_create_Sequence_k_(m)
end function

function sortedWith_rSequence_Comparator_k_(m as Object, comparator as Object) as Object
    return Anon_17c523e2_create_Sequence_Comparator_k_(m, comparator)
end function

function sortedDescending_rSequence_k_(m as Object) as Object
    return sortedWith_rSequence_Comparator_k_(m, reverseOrder_k_())
end function

function sortedBy_rSequence_Function1_k_(m as Object, selector as Object) as Object
    return sortedWith_rSequence_Comparator_k_(m, compareBy_Function1ComparableN_k_(selector))
end function

function sortedByDescending_rSequence_Function1_k_(m as Object, selector as Object) as Object
    return sortedWith_rSequence_Comparator_k_(m, compareByDescending_Function1ComparableN_k_(selector))
end function

function ifEmpty_rSequence_Function0Sequence_k_(m as Object, defaultValue as Object) as Object
    return Anon_40917f5e_create_Sequence_Function0Sequence_k_(m, defaultValue)
end function

function Anon_33c9f46f_create_Sequence_k_(_this_sorted as Object) as Object
    this = {}
    this.__type = "Anon_33c9f46f"
    this.__proto = ["Anon_33c9f46f", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = Anon_33c9f46f_iterator_k_
    this._this_sorted = _this_sorted
    return this
end function

function Anon_33c9f46f_iterator_k_() as Object
    sortedList = toMutableList_rSequence_k_(m._this_sorted)
    sort_rMutableList_k_(sortedList)
    return sortedList.iterator_k_()
end function

function Anon_17c523e2_create_Sequence_Comparator_k_(_this_sortedWith as Object, _comparator as Object) as Object
    this = {}
    this.__type = "Anon_17c523e2"
    this.__proto = ["Anon_17c523e2", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = Anon_17c523e2_iterator_k_
    this._this_sortedWith = _this_sortedWith
    this._comparator = _comparator
    return this
end function

function Anon_17c523e2_iterator_k_() as Object
    sortedList = toMutableList_rSequence_k_(m._this_sortedWith)
    sortWith_rMutableList_Comparator_k_(sortedList, m._comparator)
    return sortedList.iterator_k_()
end function

function Anon_40917f5e_create_Sequence_Function0Sequence_k_(_this_ifEmpty as Object, _defaultValue as Object) as Object
    this = {}
    this.__type = "Anon_40917f5e"
    this.__proto = ["Anon_40917f5e", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = Anon_40917f5e_iterator_k_
    this._this_ifEmpty = _this_ifEmpty
    this._defaultValue = _defaultValue
    return this
end function

function Anon_40917f5e_iterator_k_() as Object
    iterator = m._this_ifEmpty.iterator_k_()
    if iterator.hasNext_k_() then
        return iterator
    else if true then
        return m._defaultValue.invoke().iterator_k_()
    end if
end function
