function Grouping_keyOf_AnyN_AnyN_k_(element as Dynamic) as Dynamic
end function

function Grouping_sourceIterator_IteratorAnyN_k_() as Object
end function

function eachCount_rGroupingAnyNAnyN_MapAnyNI_k_(m as Object) as Object
    return fold_rGroupingAnyNAnyN_AnyN_Function3AnyNAnyNAnyNAnyN_MapAnyNAnyN_k_(m, 0, {invoke: function(_unused as Dynamic, acc as Integer, _unused1 as Dynamic) as Integer
        return acc + 1
    end function})
end function

function fold_rGroupingAnyNAnyN_AnyN_Function3AnyNAnyNAnyNAnyN_MapAnyNAnyN_k_(m as Object, initialValue as Dynamic, operation as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    iterator = m.sourceIterator_IteratorAnyN_k_()
    while iterator.hasNext_Z_k_()
        element = iterator.next_AnyN_k_()
        key = m.keyOf_AnyN_AnyN_k_(element)
        tmp0_elvis_lhs = result.get_AnyN_AnyN_k_(key)
        __when_tmp0 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp0 = initialValue
        else if true then
            __when_tmp0 = tmp0_elvis_lhs
        end if
        accumulator = __when_tmp0
        set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(result, key, operation.invoke(key, accumulator, element))
    end while
    return result
end function

function fold_rGroupingAnyNAnyN_Function2AnyNAnyNAnyN_Function3AnyNAnyNAnyNAnyN_MapAnyNAnyN_k_(m as Object, initialValueSelector as Object, operation as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    iterator = m.sourceIterator_IteratorAnyN_k_()
    while iterator.hasNext_Z_k_()
        element = iterator.next_AnyN_k_()
        key = m.keyOf_AnyN_AnyN_k_(element)
        accumulator = result.get_AnyN_AnyN_k_(key)
        __when_tmp1 = invalid
        if (accumulator = invalid) and not result.containsKey_AnyN_Z_k_(key) then
            __when_tmp1 = initialValueSelector.invoke(key, element)
        else if true then
            __when_tmp1 = operation.invoke(key, accumulator, element)
        end if
        set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(result, key, __when_tmp1)
    end while
    return result
end function

function reduce_rGroupingAnyNAnyN_Function3AnyNAnyNAnyNAnyN_MapAnyNAnyN_k_(m as Object, operation as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    iterator = m.sourceIterator_IteratorAnyN_k_()
    while iterator.hasNext_Z_k_()
        element = iterator.next_AnyN_k_()
        key = m.keyOf_AnyN_AnyN_k_(element)
        accumulator = result.get_AnyN_AnyN_k_(key)
        __when_tmp2 = invalid
        if (accumulator = invalid) and not result.containsKey_AnyN_Z_k_(key) then
            __when_tmp2 = element
        else if true then
            __when_tmp2 = operation.invoke(key, accumulator, element)
        end if
        set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(result, key, __when_tmp2)
    end while
    return result
end function

function groupBy_rIterableAnyN_Function1AnyNAnyN_MapAnyNListAnyN_k_(m as Object, keySelector as Object) as Object
    return groupByTo_rIterableAnyN_Any_Function1AnyNAnyN_Any_k_(m, HashMap_create_HashMapAnyNAnyN_k_(), keySelector)
end function

function groupBy_rIterableAnyN_Function1AnyNAnyN_Function1AnyNAnyN_MapAnyNListAnyN_k_(m as Object, keySelector as Object, valueTransform as Object) as Object
    return groupByTo_rIterableAnyN_Any_Function1AnyNAnyN_Function1AnyNAnyN_Any_k_(m, HashMap_create_HashMapAnyNAnyN_k_(), keySelector, valueTransform)
end function

function groupByTo_rIterableAnyN_Any_Function1AnyNAnyN_Any_k_(m as Object, destination as Object, keySelector as Object) as Object
    for each element in m.array
        key = keySelector.invoke(element)
        list = getOrPut_rMutableMapAnyNAnyN_AnyN_Function0AnyN_AnyN_k_(destination, key, {invoke: function() as Object
            return ArrayList_create_ArrayListAnyN_k_()
        end function})
        list.add_AnyN_Z_k_(element)

    end for
    return destination
end function

function groupByTo_rIterableAnyN_Any_Function1AnyNAnyN_Function1AnyNAnyN_Any_k_(m as Object, destination as Object, keySelector as Object, valueTransform as Object) as Object
    for each element in m.array
        key = keySelector.invoke(element)
        list = getOrPut_rMutableMapAnyNAnyN_AnyN_Function0AnyN_AnyN_k_(destination, key, {invoke: function() as Object
            return ArrayList_create_ArrayListAnyN_k_()
        end function})
        list.add_AnyN_Z_k_(valueTransform.invoke(element))

    end for
    return destination
end function

function groupingBy_rIterableAnyN_Function1AnyNAnyN_GroupingAnyNAnyN_k_(m as Object, keySelector as Object) as Object
    return Anon_d498f9b_create_AnonAnyNAnyN_k_()
end function

function associate_rIterableAnyN_Function1AnyNPairAnyNAnyN_MapAnyNAnyN_k_(m as Object, transform as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    for each element in m.array
        pair = transform.invoke(element)
        set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(result, pair.first, pair.second)

    end for
    return result
end function

function associateBy_rIterableAnyN_Function1AnyNAnyN_MapAnyNAnyN_k_(m as Object, keySelector as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    for each element in m.array
        set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(result, keySelector.invoke(element), element)

    end for
    return result
end function

function associateBy_rIterableAnyN_Function1AnyNAnyN_Function1AnyNAnyN_MapAnyNAnyN_k_(m as Object, keySelector as Object, valueTransform as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    for each element in m.array
        set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(result, keySelector.invoke(element), valueTransform.invoke(element))

    end for
    return result
end function

function associateByTo_rIterableAnyN_Any_Function1AnyNAnyN_Any_k_(m as Object, destination as Object, keySelector as Object) as Object
    for each element in m.array
        set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(destination, keySelector.invoke(element), element)

    end for
    return destination
end function

function associateByTo_rIterableAnyN_Any_Function1AnyNAnyN_Function1AnyNAnyN_Any_k_(m as Object, destination as Object, keySelector as Object, valueTransform as Object) as Object
    for each element in m.array
        set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(destination, keySelector.invoke(element), valueTransform.invoke(element))

    end for
    return destination
end function

function associateWith_rIterableAnyN_Function1AnyNAnyN_MapAnyNAnyN_k_(m as Object, valueSelector as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    for each element in m.array
        set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(result, element, valueSelector.invoke(element))

    end for
    return result
end function

function associateWithTo_rIterableAnyN_Any_Function1AnyNAnyN_Any_k_(m as Object, destination as Object, valueSelector as Object) as Object
    for each element in m.array
        set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(destination, element, valueSelector.invoke(element))

    end for
    return destination
end function
