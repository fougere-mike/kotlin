function Grouping_keyOf_AnyN_k_(element as Dynamic) as Dynamic
end function

function Grouping_sourceIterator_k_() as Object
end function

function eachCount_rGrouping_k_(m as Object) as Object
    return fold_rGrouping_AnyN_Function3_k_(m, 0, {invoke: function(_unused as Dynamic, acc as Integer, _unused1 as Dynamic) as Integer
        return acc + 1
    end function})
end function

function fold_rGrouping_AnyN_Function3_k_(m as Object, initialValue as Dynamic, operation as Object) as Object
    result = HashMap_create_k_()
    iterator = m.sourceIterator_k_()
    while iterator.hasNext_k_()
        element = iterator.next_k_()
        key = m.keyOf_AnyN_k_(element)
        tmp0_elvis_lhs = result.get_AnyN_k_(key)
        __when_tmp0 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp0 = initialValue
        else if true then
            __when_tmp0 = tmp0_elvis_lhs
        end if
        accumulator = __when_tmp0
        set_rMutableMap_AnyN_AnyN_k_(result, key, operation.invoke(key, accumulator, element))
    end while
    return result
end function

function fold_rGrouping_Function2_Function3_k_(m as Object, initialValueSelector as Object, operation as Object) as Object
    result = HashMap_create_k_()
    iterator = m.sourceIterator_k_()
    while iterator.hasNext_k_()
        element = iterator.next_k_()
        key = m.keyOf_AnyN_k_(element)
        accumulator = result.get_AnyN_k_(key)
        __when_tmp1 = invalid
        if (accumulator = invalid) and not result.containsKey_AnyN_k_(key) then
            __when_tmp1 = initialValueSelector.invoke(key, element)
        else if true then
            __when_tmp1 = operation.invoke(key, accumulator, element)
        end if
        set_rMutableMap_AnyN_AnyN_k_(result, key, __when_tmp1)
    end while
    return result
end function

function reduce_rGrouping_Function3_k_(m as Object, operation as Object) as Object
    result = HashMap_create_k_()
    iterator = m.sourceIterator_k_()
    while iterator.hasNext_k_()
        element = iterator.next_k_()
        key = m.keyOf_AnyN_k_(element)
        accumulator = result.get_AnyN_k_(key)
        __when_tmp2 = invalid
        if (accumulator = invalid) and not result.containsKey_AnyN_k_(key) then
            __when_tmp2 = element
        else if true then
            __when_tmp2 = operation.invoke(key, accumulator, element)
        end if
        set_rMutableMap_AnyN_AnyN_k_(result, key, __when_tmp2)
    end while
    return result
end function

function groupBy_rIterable_Function1_k_(m as Object, keySelector as Object) as Object
    return groupByTo_rIterable_Any_Function1_k_(m, HashMap_create_k_(), keySelector)
end function

function groupBy_rIterable_Function1_Function1_k_(m as Object, keySelector as Object, valueTransform as Object) as Object
    return groupByTo_rIterable_Any_Function1_Function1_k_(m, HashMap_create_k_(), keySelector, valueTransform)
end function

function groupByTo_rIterable_Any_Function1_k_(m as Object, destination as Object, keySelector as Object) as Object
    __iter_6 = m.iterator_k_()
    while __iter_6.hasNext_k_()
        element = __iter_6.next_k_()
        key = keySelector.invoke(element)
        list = getOrPut_rMutableMap_AnyN_Function0_k_(destination, key, {invoke: function() as Object
            return ArrayList_create_k_()
        end function})
        list.add_AnyN_k_(element)

    end while

    return destination
end function

function groupByTo_rIterable_Any_Function1_Function1_k_(m as Object, destination as Object, keySelector as Object, valueTransform as Object) as Object
    __iter_7 = m.iterator_k_()
    while __iter_7.hasNext_k_()
        element = __iter_7.next_k_()
        key = keySelector.invoke(element)
        list = getOrPut_rMutableMap_AnyN_Function0_k_(destination, key, {invoke: function() as Object
            return ArrayList_create_k_()
        end function})
        list.add_AnyN_k_(valueTransform.invoke(element))

    end while

    return destination
end function

function groupingBy_rIterable_Function1_k_(m as Object, keySelector as Object) as Object
    return Anon_2c5f702b_create_Iterable_Function1_k_(m, keySelector)
end function

function associate_rIterable_Function1Pair_k_(m as Object, transform as Object) as Object
    result = HashMap_create_k_()
    __iter_8 = m.iterator_k_()
    while __iter_8.hasNext_k_()
        element = __iter_8.next_k_()
        pair = transform.invoke(element)
        set_rMutableMap_AnyN_AnyN_k_(result, pair.first, pair.second)

    end while

    return result
end function

function associateBy_rIterable_Function1_k_(m as Object, keySelector as Object) as Object
    result = HashMap_create_k_()
    __iter_9 = m.iterator_k_()
    while __iter_9.hasNext_k_()
        element = __iter_9.next_k_()
        set_rMutableMap_AnyN_AnyN_k_(result, keySelector.invoke(element), element)

    end while

    return result
end function

function associateBy_rIterable_Function1_Function1_k_(m as Object, keySelector as Object, valueTransform as Object) as Object
    result = HashMap_create_k_()
    __iter_10 = m.iterator_k_()
    while __iter_10.hasNext_k_()
        element = __iter_10.next_k_()
        set_rMutableMap_AnyN_AnyN_k_(result, keySelector.invoke(element), valueTransform.invoke(element))

    end while

    return result
end function

function associateByTo_rIterable_Any_Function1_k_(m as Object, destination as Object, keySelector as Object) as Object
    __iter_11 = m.iterator_k_()
    while __iter_11.hasNext_k_()
        element = __iter_11.next_k_()
        set_rMutableMap_AnyN_AnyN_k_(destination, keySelector.invoke(element), element)

    end while

    return destination
end function

function associateByTo_rIterable_Any_Function1_Function1_k_(m as Object, destination as Object, keySelector as Object, valueTransform as Object) as Object
    __iter_12 = m.iterator_k_()
    while __iter_12.hasNext_k_()
        element = __iter_12.next_k_()
        set_rMutableMap_AnyN_AnyN_k_(destination, keySelector.invoke(element), valueTransform.invoke(element))

    end while

    return destination
end function

function associateWith_rIterable_Function1_k_(m as Object, valueSelector as Object) as Object
    result = HashMap_create_k_()
    __iter_13 = m.iterator_k_()
    while __iter_13.hasNext_k_()
        element = __iter_13.next_k_()
        set_rMutableMap_AnyN_AnyN_k_(result, element, valueSelector.invoke(element))

    end while

    return result
end function

function associateWithTo_rIterable_Any_Function1_k_(m as Object, destination as Object, valueSelector as Object) as Object
    __iter_14 = m.iterator_k_()
    while __iter_14.hasNext_k_()
        element = __iter_14.next_k_()
        set_rMutableMap_AnyN_AnyN_k_(destination, element, valueSelector.invoke(element))

    end while

    return destination
end function

function Anon_2c5f702b_create_Iterable_Function1_k_(_this_groupingBy as Object, _keySelector as Object) as Object
    this = {}
    this.__type = "Anon_2c5f702b"
    this.__proto = ["Anon_2c5f702b", "Grouping"]
    this.__id = __kotlin_nextObjectId()
    this.sourceIterator_k_ = Anon_2c5f702b_sourceIterator_k_
    this.keyOf_AnyN_k_ = Anon_2c5f702b_keyOf_AnyN_k_
    this._this_groupingBy = _this_groupingBy
    this._keySelector = _keySelector
    return this
end function

function Anon_2c5f702b_sourceIterator_k_() as Object
    return m._this_groupingBy.iterator_k_()
end function

function Anon_2c5f702b_keyOf_AnyN_k_(element as Dynamic) as Dynamic
    return m._keySelector.invoke(element)
end function
