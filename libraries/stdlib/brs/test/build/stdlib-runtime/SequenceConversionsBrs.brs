function toList_rSequenceAnyN_ListAnyN_k_(m as Object) as Object
    return toMutableList_rSequenceAnyN_MutableListAnyN_k_(m)
end function

function toMutableList_rSequenceAnyN_MutableListAnyN_k_(m as Object) as Object
    return toCollection_rSequenceAnyN_Any_Any_k_(m, ArrayList_create_ArrayListAnyN_k_())
end function

function toSet_rSequenceAnyN_SetAnyN_k_(m as Object) as Object
    return toCollection_rSequenceAnyN_Any_Any_k_(m, HashSet_create_HashSetAnyN_k_())
end function

function toMutableSet_rSequenceAnyN_MutableSetAnyN_k_(m as Object) as Object
    return toCollection_rSequenceAnyN_Any_Any_k_(m, HashSet_create_HashSetAnyN_k_())
end function

function toHashSet_rSequenceAnyN_HashSetAnyN_k_(m as Object) as Object
    return toCollection_rSequenceAnyN_Any_Any_k_(m, HashSet_create_HashSetAnyN_k_())
end function

function toCollection_rSequenceAnyN_Any_Any_k_(m as Object, destination as Object) as Object
    for each item in m
        destination.add(item)

    end for
    return destination
end function

function associate_rSequenceAnyN_Function1AnyNPairAnyNAnyN_MapAnyNAnyN_k_(m as Object, transform as Function) as Object
    return associateTo_rSequenceAnyN_Any_Function1AnyNPairAnyNAnyN_Any_k_(m, HashMap_create_HashMapAnyNAnyN_k_(), transform)
end function

function associateBy_rSequenceAnyN_Function1AnyNAnyN_MapAnyNAnyN_k_(m as Object, keySelector as Function) as Object
    return associateByTo_rSequenceAnyN_Any_Function1AnyNAnyN_Any_k_(m, HashMap_create_HashMapAnyNAnyN_k_(), keySelector)
end function

function associateBy_rSequenceAnyN_Function1AnyNAnyN_Function1AnyNAnyN_MapAnyNAnyN_k_(m as Object, keySelector as Function, valueTransform as Function) as Object
    return associateByTo_rSequenceAnyN_Any_Function1AnyNAnyN_Function1AnyNAnyN_Any_k_(m, HashMap_create_HashMapAnyNAnyN_k_(), keySelector, valueTransform)
end function

function associateTo_rSequenceAnyN_Any_Function1AnyNPairAnyNAnyN_Any_k_(m as Object, destination as Object, transform as Function) as Object
    for each element in m
        pair = transform.invoke(element)
        destination.put(pair.first, pair.second)

    end for
    return destination
end function

function associateByTo_rSequenceAnyN_Any_Function1AnyNAnyN_Any_k_(m as Object, destination as Object, keySelector as Function) as Object
    for each element in m
        destination.put(keySelector.invoke(element), element)

    end for
    return destination
end function

function associateByTo_rSequenceAnyN_Any_Function1AnyNAnyN_Function1AnyNAnyN_Any_k_(m as Object, destination as Object, keySelector as Function, valueTransform as Function) as Object
    for each element in m
        destination.put(keySelector.invoke(element), valueTransform.invoke(element))

    end for
    return destination
end function

function associateWith_rSequenceAnyN_Function1AnyNAnyN_MapAnyNAnyN_k_(m as Object, valueSelector as Function) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    return associateWithTo_rSequenceAnyN_Any_Function1AnyNAnyN_Any_k_(m, result, valueSelector)
end function

function associateWithTo_rSequenceAnyN_Any_Function1AnyNAnyN_Any_k_(m as Object, destination as Object, valueSelector as Function) as Object
    for each element in m
        destination.put(element, valueSelector.invoke(element))

    end for
    return destination
end function

function map_rSequenceAnyN_Function1AnyNAnyN_SequenceAnyN_k_(m as Object, transform as Function) as Object
    return TransformingSequence_create_SequenceAnyN_Function1AnyNAnyN_TransformingSequenceAnyNAnyN_k_(m, transform)
end function

function mapIndexed_rSequenceAnyN_Function2IAnyNAnyN_SequenceAnyN_k_(m as Object, transform as Function) as Object
    return TransformingIndexedSequence_create_obcj3k_k_(m, transform)
end function

function filter_rSequenceAnyN_Function1AnyNZ_SequenceAnyN_k_(m as Object, predicate as Function) as Object
    return FilteringSequence_create_SequenceAnyN_Z_Function1AnyNZ_FilteringSequenceAnyN_k_(m, true, predicate)
end function

function filterNot_rSequenceAnyN_Function1AnyNZ_SequenceAnyN_k_(m as Object, predicate as Function) as Object
    return FilteringSequence_create_SequenceAnyN_Z_Function1AnyNZ_FilteringSequenceAnyN_k_(m, false, predicate)
end function

function filterIndexed_rSequenceAnyN_Function2IAnyNZ_SequenceAnyN_k_(m as Object, predicate as Function) as Object
    return FilteringIndexedSequence_create_SequenceAnyN_Z_Function2IAnyNZ_FilteringIndexedSequenceAnyN_k_(m, true, predicate)
end function

function filterNotNull_rSequenceAnyN_SequenceAny_k_(m as Object) as Object
    return filter_rSequenceAnyN_Function1AnyNZ_SequenceAnyN_k_(m, {invoke: function(it as Dynamic) as Boolean
        return it <> invalid
    end function})
end function

function flatMap_rSequenceAnyN_Function1AnyNIterableAnyN_SequenceAnyN_k_(m as Object, transform as Function) as Object
    return FlatteningSequence_create_hoq5fb_k_(m, transform, {invoke: function(it as Object) as Object
        return it.iterator()
    end function})
end function

function flatten_rSequenceSequenceAnyN_SequenceAnyN_k_(m as Object) as Object
    return flatten_rSequenceAnyN_Function1AnyNIteratorAnyN_SequenceAnyN_k_(m, {invoke: function(it as Object) as Object
        return it.iterator()
    end function})
end function

function flatten_rSequenceIterableAnyN_SequenceAnyN_k_(m as Object) as Object
    return flatten_rSequenceAnyN_Function1AnyNIteratorAnyN_SequenceAnyN_k_(m, {invoke: function(it as Object) as Object
        return it.iterator()
    end function})
end function

function flatten_rSequenceAnyN_Function1AnyNIteratorAnyN_SequenceAnyN_k_(m as Object, iterator as Function) as Object
    if __kotlin_isInstanceOf(m, "TransformingSequence") then
        return m.flatten(iterator)
    end if
    return FlatteningSequence_create_hoq5fb_k_(m, {invoke: function(it as Dynamic) as Dynamic
        return it
    end function}, iterator)
end function

function take_rSequenceAnyN_I_SequenceAnyN_k_(m as Object, n as Integer) as Object
    require_Z_Function0Any_k_(n >= 0, {n: n, invoke: function() as Object
        return ("Requested element count " + m.n) + " is less than zero."
    end function})
    __when_tmp0 = invalid
    if n = 0 then
        __when_tmp0 = emptySequence_SequenceAnyN_k_()
    else if __kotlin_isInstanceOf(m, "DropTakeSequence") then
        __when_tmp0 = m.take(n)
    else if true then
        __when_tmp0 = TakingSequence_create_SequenceAnyN_I_TakingSequenceAnyN_k_(m, n)
    end if
    return __when_tmp0

end function

function takeWhile_rSequenceAnyN_Function1AnyNZ_SequenceAnyN_k_(m as Object, predicate as Function) as Object
    return TakingWhileSequence_create_SequenceAnyN_Function1AnyNZ_TakingWhileSequenceAnyN_k_(m, predicate)
end function

function drop_rSequenceAnyN_I_SequenceAnyN_k_(m as Object, n as Integer) as Object
    require_Z_Function0Any_k_(n >= 0, {n: n, invoke: function() as Object
        return ("Requested element count " + m.n) + " is less than zero."
    end function})
    __when_tmp1 = invalid
    if n = 0 then
        __when_tmp1 = m
    else if __kotlin_isInstanceOf(m, "DropTakeSequence") then
        __when_tmp1 = m.drop(n)
    else if true then
        __when_tmp1 = DroppingSequence_create_SequenceAnyN_I_DroppingSequenceAnyN_k_(m, n)
    end if
    return __when_tmp1

end function

function dropWhile_rSequenceAnyN_Function1AnyNZ_SequenceAnyN_k_(m as Object, predicate as Function) as Object
    return DroppingWhileSequence_create_SequenceAnyN_Function1AnyNZ_DroppingWhileSequenceAnyN_k_(m, predicate)
end function

function distinct_rSequenceAnyN_SequenceAnyN_k_(m as Object) as Object
    return distinctBy_rSequenceAnyN_Function1AnyNAnyN_SequenceAnyN_k_(m, {invoke: function(it as Dynamic) as Dynamic
        return it
    end function})
end function

function distinctBy_rSequenceAnyN_Function1AnyNAnyN_SequenceAnyN_k_(m as Object, selector as Function) as Object
    return DistinctSequence_create_SequenceAnyN_Function1AnyNAnyN_DistinctSequenceAnyNAnyN_k_(m, selector)
end function

function zip_rSequenceAnyN_SequenceAnyN_SequencePairAnyNAnyN_k_(m as Object, other as Object) as Object
    return MergingSequence_create_efuchp_k_(m, other, {invoke: function(t1 as Dynamic, t2 as Dynamic) as Object
        return to_rAnyN_AnyN_PairAnyNAnyN_k_(t1, t2)
    end function})
end function

function zip_rSequenceAnyN_SequenceAnyN_Function2AnyNAnyNAnyN_SequenceAnyN_k_(m as Object, other as Object, transform as Function) as Object
    return MergingSequence_create_efuchp_k_(m, other, transform)
end function

function sorted_rSequenceAny_SequenceAny_k_(m as Object) as Object
    return Anon_58308f24_create_AnonAny_k_()
end function

function sortedWith_rSequenceAnyN_ComparatorAnyN_SequenceAnyN_k_(m as Object, comparator as Object) as Object
    return Anon_41604b98_create_AnonAnyN_k_()
end function

function sortedDescending_rSequenceAny_SequenceAny_k_(m as Object) as Object
    return sortedWith_rSequenceAnyN_ComparatorAnyN_SequenceAnyN_k_(m, reverseOrder_ComparatorAny_k_())
end function

function sortedBy_rSequenceAnyN_Function1AnyNAnyN_SequenceAnyN_k_(m as Object, selector as Function) as Object
    return sortedWith_rSequenceAnyN_ComparatorAnyN_SequenceAnyN_k_(m, compareBy_Function1AnyNComparableStarN_ComparatorAnyN_k_(selector))
end function

function sortedByDescending_rSequenceAnyN_Function1AnyNAnyN_SequenceAnyN_k_(m as Object, selector as Function) as Object
    return sortedWith_rSequenceAnyN_ComparatorAnyN_SequenceAnyN_k_(m, compareByDescending_Function1AnyNComparableStarN_ComparatorAnyN_k_(selector))
end function

function ifEmpty_rSequenceAnyN_Function0SequenceAnyN_SequenceAnyN_k_(m as Object, defaultValue as Function) as Object
    return Anon_57dea250_create_AnonAnyN_k_()
end function
