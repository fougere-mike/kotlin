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
    for each item in m
        destination.add_AnyN_k_(item)

    end for
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
    for each element in m
        pair = transform.invoke(element)
        destination.put_AnyN_AnyN_k_(pair.first, pair.second)

    end for
    return destination
end function

function associateByTo_rSequence_Any_Function1_k_(m as Object, destination as Object, keySelector as Object) as Object
    for each element in m
        destination.put_AnyN_AnyN_k_(keySelector.invoke(element), element)

    end for
    return destination
end function

function associateByTo_rSequence_Any_Function1_Function1_k_(m as Object, destination as Object, keySelector as Object, valueTransform as Object) as Object
    for each element in m
        destination.put_AnyN_AnyN_k_(keySelector.invoke(element), valueTransform.invoke(element))

    end for
    return destination
end function

function associateWith_rSequence_Function1_k_(m as Object, valueSelector as Object) as Object
    result = HashMap_create_k_()
    return associateWithTo_rSequence_Any_Function1_k_(m, result, valueSelector)
end function

function associateWithTo_rSequence_Any_Function1_k_(m as Object, destination as Object, valueSelector as Object) as Object
    for each element in m
        destination.put_AnyN_AnyN_k_(element, valueSelector.invoke(element))

    end for
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
        return ("Requested element count " + m.n) + " is less than zero."
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
        return ("Requested element count " + m.n) + " is less than zero."
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
    return Anon_3e249127_create_k_()
end function

function sortedWith_rSequence_Comparator_k_(m as Object, comparator as Object) as Object
    return Anon_63a81935_create_k_()
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
    return Anon_2aa41765_create_k_()
end function
