function Grouping_keyOf_AnyN_k_(element as Dynamic) as Dynamic
end function

function Grouping_sourceIterator_k_() as Object
end function

function eachCount_rGrouping_k_(m as Object) as Object
    tmp0 = m
    tmp2 = 0
    tmp_ret_1 = invalid

    while true
        this = tmp0
        initialValue = tmp2
        result = HashMap_create_k_()
        iterator = this.sourceIterator_k_()
        while iterator.hasNext_k_()
            element = iterator.next_k_()
            key = this.keyOf_AnyN_k_(element)
            tmp0_elvis_lhs = result.get_AnyN_k_(key)
            __when_tmp0 = invalid
            if tmp0_elvis_lhs = invalid then
                __when_tmp0 = initialValue
            else if true then
                __when_tmp0 = tmp0_elvis_lhs
            end if
            accumulator = __when_tmp0
            tmp0_1 = key
            tmp2_1 = accumulator
            tmp4 = element
            tmp_ret_0 = invalid

            while true
                _unused = tmp0_1
                acc = tmp2_1
                _unused = tmp4
                tmp_ret_0 = (acc + 1)
                exit while
            end while
            set_rMutableMap_AnyN_AnyN_k_(result, key, tmp_ret_0)
        end while
        tmp_ret_1 = result
        exit while
    end while
    return tmp_ret_1

end function

function fold_rGrouping_AnyN_Function3_k_(m as Object, initialValue as Dynamic, operation as Object) as Object
    result = HashMap_create_k_()
    iterator = m.sourceIterator_k_()
    while iterator.hasNext_k_()
        element = iterator.next_k_()
        key = m.keyOf_AnyN_k_(element)
        tmp0_elvis_lhs = result.get_AnyN_k_(key)
        __when_tmp1 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp1 = initialValue
        else if true then
            __when_tmp1 = tmp0_elvis_lhs
        end if
        accumulator = __when_tmp1
        set_rMutableMap_AnyN_AnyN_k_(result, key, operation.invoke_AnyN_AnyN_AnyN_k_(key, accumulator, element))
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
        __when_tmp2 = invalid
        if (accumulator = invalid) and not result.containsKey_AnyN_k_(key) then
            __when_tmp2 = initialValueSelector.invoke_AnyN_AnyN_k_(key, element)
        else if true then
            __when_tmp2 = operation.invoke_AnyN_AnyN_AnyN_k_(key, accumulator, element)
        end if
        set_rMutableMap_AnyN_AnyN_k_(result, key, __when_tmp2)
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
        __when_tmp3 = invalid
        if (accumulator = invalid) and not result.containsKey_AnyN_k_(key) then
            __when_tmp3 = element
        else if true then
            __when_tmp3 = operation.invoke_AnyN_AnyN_AnyN_k_(key, accumulator, element)
        end if
        set_rMutableMap_AnyN_AnyN_k_(result, key, __when_tmp3)
    end while
    return result
end function

function groupBy_rIterable_Function1_k_(m as Object, keySelector as Object) as Object
    tmp0 = m
    tmp2 = HashMap_create_k_()
    tmp_ret_2 = invalid

    while true
        this = tmp0
        destination = tmp2
        __iter_6 = this.iterator_k_()
        while __iter_6.hasNext_k_()
            element = __iter_6.next_k_()
            key = keySelector.invoke_AnyN_k_(element)
            tmp0_1 = destination
            tmp2_1 = key
            tmp_ret_0 = invalid

            while true
                this = tmp0_1
                key = tmp2_1
                value = this.get_AnyN_k_(key)
                if (value <> invalid) or this.containsKey_AnyN_k_(key) then
                    tmp_ret_0 = value
                    exit while
                end if
                tmp_ret_1 = invalid
                while true
                    tmp_ret_1 = ArrayList_create_k_()
                    exit while
                end while
                answer = tmp_ret_1
                this.put_AnyN_AnyN_k_(key, answer)
                tmp_ret_0 = answer
                exit while
            end while
            list = tmp_ret_0
            list.add_AnyN_k_(element)

        end while
        tmp_ret_2 = destination
        exit while
    end while
    return tmp_ret_2

end function

function groupBy_rIterable_Function1_Function1_k_(m as Object, keySelector as Object, valueTransform as Object) as Object
    tmp0 = m
    tmp2 = HashMap_create_k_()
    tmp_ret_2 = invalid

    while true
        this = tmp0
        destination = tmp2
        __iter_7 = this.iterator_k_()
        while __iter_7.hasNext_k_()
            element = __iter_7.next_k_()
            key = keySelector.invoke_AnyN_k_(element)
            tmp0_1 = destination
            tmp2_1 = key
            tmp_ret_0 = invalid

            while true
                this = tmp0_1
                key = tmp2_1
                value = this.get_AnyN_k_(key)
                if (value <> invalid) or this.containsKey_AnyN_k_(key) then
                    tmp_ret_0 = value
                    exit while
                end if
                tmp_ret_1 = invalid
                while true
                    tmp_ret_1 = ArrayList_create_k_()
                    exit while
                end while
                answer = tmp_ret_1
                this.put_AnyN_AnyN_k_(key, answer)
                tmp_ret_0 = answer
                exit while
            end while
            list = tmp_ret_0
            list.add_AnyN_k_(valueTransform.invoke_AnyN_k_(element))

        end while
        tmp_ret_2 = destination
        exit while
    end while
    return tmp_ret_2

end function

function groupByTo_rIterable_Any_Function1_k_(m as Object, destination as Object, keySelector as Object) as Object
    __iter_8 = m.iterator_k_()
    while __iter_8.hasNext_k_()
        element = __iter_8.next_k_()
        key = keySelector.invoke_AnyN_k_(element)
        tmp0 = destination
        tmp2 = key
        tmp_ret_0 = invalid

        while true
            this = tmp0
            key = tmp2
            value = this.get_AnyN_k_(key)
            if (value <> invalid) or this.containsKey_AnyN_k_(key) then
                tmp_ret_0 = value
                exit while
            end if
            tmp_ret_1 = invalid
            while true
                tmp_ret_1 = ArrayList_create_k_()
                exit while
            end while
            answer = tmp_ret_1
            this.put_AnyN_AnyN_k_(key, answer)
            tmp_ret_0 = answer
            exit while
        end while
        list = tmp_ret_0
        list.add_AnyN_k_(element)

    end while

    return destination
end function

function groupByTo_rIterable_Any_Function1_Function1_k_(m as Object, destination as Object, keySelector as Object, valueTransform as Object) as Object
    __iter_9 = m.iterator_k_()
    while __iter_9.hasNext_k_()
        element = __iter_9.next_k_()
        key = keySelector.invoke_AnyN_k_(element)
        tmp0 = destination
        tmp2 = key
        tmp_ret_0 = invalid

        while true
            this = tmp0
            key = tmp2
            value = this.get_AnyN_k_(key)
            if (value <> invalid) or this.containsKey_AnyN_k_(key) then
                tmp_ret_0 = value
                exit while
            end if
            tmp_ret_1 = invalid
            while true
                tmp_ret_1 = ArrayList_create_k_()
                exit while
            end while
            answer = tmp_ret_1
            this.put_AnyN_AnyN_k_(key, answer)
            tmp_ret_0 = answer
            exit while
        end while
        list = tmp_ret_0
        list.add_AnyN_k_(valueTransform.invoke_AnyN_k_(element))

    end while

    return destination
end function

function groupingBy_rIterable_Function1_k_(m as Object, keySelector as Object) as Object
    return Anon_4725f922_create_Iterable_Function1_k_(m, keySelector)
end function

function associate_rIterable_Function1Pair_k_(m as Object, transform as Object) as Object
    result = HashMap_create_k_()
    __iter_10 = m.iterator_k_()
    while __iter_10.hasNext_k_()
        element = __iter_10.next_k_()
        pair = transform.invoke_AnyN_k_(element)
        set_rMutableMap_AnyN_AnyN_k_(result, pair.first, pair.second)

    end while

    return result
end function

function associateBy_rIterable_Function1_k_(m as Object, keySelector as Object) as Object
    result = HashMap_create_k_()
    __iter_11 = m.iterator_k_()
    while __iter_11.hasNext_k_()
        element = __iter_11.next_k_()
        set_rMutableMap_AnyN_AnyN_k_(result, keySelector.invoke_AnyN_k_(element), element)

    end while

    return result
end function

function associateBy_rIterable_Function1_Function1_k_(m as Object, keySelector as Object, valueTransform as Object) as Object
    result = HashMap_create_k_()
    __iter_12 = m.iterator_k_()
    while __iter_12.hasNext_k_()
        element = __iter_12.next_k_()
        set_rMutableMap_AnyN_AnyN_k_(result, keySelector.invoke_AnyN_k_(element), valueTransform.invoke_AnyN_k_(element))

    end while

    return result
end function

function associateByTo_rIterable_Any_Function1_k_(m as Object, destination as Object, keySelector as Object) as Object
    __iter_13 = m.iterator_k_()
    while __iter_13.hasNext_k_()
        element = __iter_13.next_k_()
        set_rMutableMap_AnyN_AnyN_k_(destination, keySelector.invoke_AnyN_k_(element), element)

    end while

    return destination
end function

function associateByTo_rIterable_Any_Function1_Function1_k_(m as Object, destination as Object, keySelector as Object, valueTransform as Object) as Object
    __iter_14 = m.iterator_k_()
    while __iter_14.hasNext_k_()
        element = __iter_14.next_k_()
        set_rMutableMap_AnyN_AnyN_k_(destination, keySelector.invoke_AnyN_k_(element), valueTransform.invoke_AnyN_k_(element))

    end while

    return destination
end function

function associateWith_rIterable_Function1_k_(m as Object, valueSelector as Object) as Object
    result = HashMap_create_k_()
    __iter_15 = m.iterator_k_()
    while __iter_15.hasNext_k_()
        element = __iter_15.next_k_()
        set_rMutableMap_AnyN_AnyN_k_(result, element, valueSelector.invoke_AnyN_k_(element))

    end while

    return result
end function

function associateWithTo_rIterable_Any_Function1_k_(m as Object, destination as Object, valueSelector as Object) as Object
    __iter_16 = m.iterator_k_()
    while __iter_16.hasNext_k_()
        element = __iter_16.next_k_()
        set_rMutableMap_AnyN_AnyN_k_(destination, element, valueSelector.invoke_AnyN_k_(element))

    end while

    return destination
end function

function Anon_4725f922_create_Iterable_Function1_k_(_this_groupingBy as Object, _keySelector as Object) as Object
    this = {}
    this.__type = "Anon_4725f922"
    this.__proto = ["Anon_4725f922", "Grouping"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.sourceIterator_k_ = Anon_4725f922_sourceIterator_k_
    this.keyOf_AnyN_k_ = Anon_4725f922_keyOf_AnyN_k_
    this._this_groupingBy = _this_groupingBy
    this._keySelector = _keySelector
    return this
end function

function Anon_4725f922_sourceIterator_k_() as Object
    return m._this_groupingBy.iterator_k_()
end function

function Anon_4725f922_keyOf_AnyN_k_(element as Dynamic) as Dynamic
    return m._keySelector.invoke_AnyN_k_(element)
end function
