function contains_rSequence_AnyN_k_(m as Object, element as Dynamic) as Boolean
    return indexOf_rSequence_AnyN_k_(m, element) >= 0
end function

function elementAt_rSequence_I_k_(m as Object, index as Integer) as Dynamic
    return elementAtOrElse_rSequence_I_Function1I_k_(m, index, elementAt_lambda_create_I_k_(index))
end function

function elementAtOrElse_rSequence_I_Function1I_k_(m as Object, index as Integer, defaultValue as Object) as Dynamic
    if index < 0 then
        return defaultValue.invoke_AnyN_k_(index)
    end if
    iterator = m.iterator_k_()
    count = 0
    while iterator.hasNext_k_()
        element = iterator.next_k_()
        unary = count
        count = (unary + 1)
        if index = unary then
            return element
        end if
    end while
    return defaultValue.invoke_AnyN_k_(index)
end function

function elementAtOrNull_rSequence_I_k_(m as Object, index as Integer) as Dynamic
    if index < 0 then
        return invalid
    end if
    iterator = m.iterator_k_()
    count = 0
    while iterator.hasNext_k_()
        element = iterator.next_k_()
        unary = count
        count = (unary + 1)
        if index = unary then
            return element
        end if
    end while
    return invalid
end function

function find_rSequence_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    tmp0 = m
    tmp_ret_0 = invalid

    __ret_done_0 = false
    while true
        this = tmp0
        __iter_33 = this.iterator_k_()
        while __iter_33.hasNext_k_()
            element = __iter_33.next_k_()
            if predicate.invoke_AnyN_k_(element) then
                tmp_ret_0 = element
                                __ret_done_0 = true
                exit while

            end if
        end while
        if __ret_done_0 then
            exit while
        end if
        tmp_ret_0 = invalid
        __ret_done_0 = true
        exit while

    end while

    return tmp_ret_0

end function

function findLast_rSequence_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        last = invalid
        __iter_34 = this.iterator_k_()
        while __iter_34.hasNext_k_()
            element = __iter_34.next_k_()
            if predicate.invoke_AnyN_k_(element) then
                last = element
            end if
        end while
        tmp_ret_0 = last
        exit while
    end while
    return tmp_ret_0

end function

function first_rSequence_k_(m as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        throw NoSuchElementException_create_StrN_k_("Sequence is empty.")
    end if
    return iterator.next_k_()
end function

function first_rSequence_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    __iter_35 = m.iterator_k_()
    while __iter_35.hasNext_k_()
        element = __iter_35.next_k_()
        if predicate.invoke_AnyN_k_(element) then
            return element
        end if
    end while

    throw NoSuchElementException_create_StrN_k_("Sequence contains no element matching the predicate.")
end function

function firstOrNull_rSequence_k_(m as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        return invalid
    end if
    return iterator.next_k_()
end function

function firstOrNull_rSequence_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    __iter_36 = m.iterator_k_()
    while __iter_36.hasNext_k_()
        element = __iter_36.next_k_()
        if predicate.invoke_AnyN_k_(element) then
            return element
        end if
    end while

    return invalid
end function

function firstNotNullOf_rSequence_Function1_k_(m as Object, transform as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    __ret_done_1 = false
    while true
        this = tmp0
        __iter_37 = this.iterator_k_()
        while __iter_37.hasNext_k_()
            element = __iter_37.next_k_()
            result = transform.invoke_AnyN_k_(element)
            if result <> invalid then
                tmp_ret_0 = result
                __ret_done_1 = true
                exit while

            end if

        end while
        if __ret_done_1 then
            exit while
        end if
        tmp_ret_0 = invalid
        __ret_done_1 = true
        exit while

    end while

    tmp0_elvis_lhs = tmp_ret_0
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        throw NoSuchElementException_create_StrN_k_("No element of the sequence was transformed to a non-null value.")
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    return __when_tmp0

end function

function firstNotNullOfOrNull_rSequence_Function1_k_(m as Object, transform as Object) as Dynamic
    __iter_38 = m.iterator_k_()
    while __iter_38.hasNext_k_()
        element = __iter_38.next_k_()
        result = transform.invoke_AnyN_k_(element)
        if result <> invalid then
            return result
        end if

    end while

    return invalid
end function

function indexOf_rSequence_AnyN_k_(m as Object, element as Dynamic) as Integer
    index = 0
    __iter_39 = m.iterator_k_()
    while __iter_39.hasNext_k_()
        item = __iter_39.next_k_()
        if brsStructuralEquals_AnyN_AnyN_k_(element, item) then
            return index
        end if
        index = (index + 1)

    end while

    return -1
end function

function indexOfFirst_rSequence_Function1Z_k_(m as Object, predicate as Object) as Integer
    index = 0
    __iter_40 = m.iterator_k_()
    while __iter_40.hasNext_k_()
        item = __iter_40.next_k_()
        if predicate.invoke_AnyN_k_(item) then
            return index
        end if
        index = (index + 1)

    end while

    return -1
end function

function indexOfLast_rSequence_Function1Z_k_(m as Object, predicate as Object) as Integer
    lastIndex = -1
    index = 0
    __iter_41 = m.iterator_k_()
    while __iter_41.hasNext_k_()
        item = __iter_41.next_k_()
        if predicate.invoke_AnyN_k_(item) then
            lastIndex = index
        end if
        index = (index + 1)

    end while

    return lastIndex
end function

function last_rSequence_k_(m as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        throw NoSuchElementException_create_StrN_k_("Sequence is empty.")
    end if
    last = iterator.next_k_()
    while iterator.hasNext_k_()
        last = iterator.next_k_()
    end while
    return last
end function

function last_rSequence_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    last = invalid
    found = false
    __iter_42 = m.iterator_k_()
    while __iter_42.hasNext_k_()
        element = __iter_42.next_k_()
        if predicate.invoke_AnyN_k_(element) then
            last = element
            found = true
        end if
    end while

    if not found then
        throw NoSuchElementException_create_StrN_k_("Sequence contains no element matching the predicate.")
    end if
    return last
end function

function lastIndexOf_rSequence_AnyN_k_(m as Object, element as Dynamic) as Integer
    lastIndex = -1
    index = 0
    __iter_43 = m.iterator_k_()
    while __iter_43.hasNext_k_()
        item = __iter_43.next_k_()
        if brsStructuralEquals_AnyN_AnyN_k_(element, item) then
            lastIndex = index
        end if
        index = (index + 1)

    end while

    return lastIndex
end function

function lastOrNull_rSequence_k_(m as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        return invalid
    end if
    last = iterator.next_k_()
    while iterator.hasNext_k_()
        last = iterator.next_k_()
    end while
    return last
end function

function lastOrNull_rSequence_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    last = invalid
    __iter_44 = m.iterator_k_()
    while __iter_44.hasNext_k_()
        element = __iter_44.next_k_()
        if predicate.invoke_AnyN_k_(element) then
            last = element
        end if
    end while

    return last
end function

function single_rSequence_k_(m as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        throw NoSuchElementException_create_StrN_k_("Sequence is empty.")
    end if
    single = iterator.next_k_()
    if iterator.hasNext_k_() then
        throw IllegalArgumentException_create_StrN_k_("Sequence has more than one element.")
    end if
    return single
end function

function single_rSequence_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    single = invalid
    found = false
    __iter_45 = m.iterator_k_()
    while __iter_45.hasNext_k_()
        element = __iter_45.next_k_()
        if predicate.invoke_AnyN_k_(element) then
            if found then
                throw IllegalArgumentException_create_StrN_k_("Sequence contains more than one matching element.")
            end if
            single = element
            found = true
        end if
    end while

    if not found then
        throw NoSuchElementException_create_StrN_k_("Sequence contains no element matching the predicate.")
    end if
    return single
end function

function singleOrNull_rSequence_k_(m as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        return invalid
    end if
    single = iterator.next_k_()
    if iterator.hasNext_k_() then
        return invalid
    end if
    return single
end function

function singleOrNull_rSequence_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    single = invalid
    found = false
    __iter_46 = m.iterator_k_()
    while __iter_46.hasNext_k_()
        element = __iter_46.next_k_()
        if predicate.invoke_AnyN_k_(element) then
            if found then
                return invalid
            end if
            single = element
            found = true
        end if
    end while

    if not found then
        return invalid
    end if
    return single
end function

function all_rSequence_Function1Z_k_(m as Object, predicate as Object) as Boolean
    __iter_47 = m.iterator_k_()
    while __iter_47.hasNext_k_()
        element = __iter_47.next_k_()
        if not predicate.invoke_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function any_rSequence_k_(m as Object) as Boolean
    __iter_48 = m.iterator_k_()
    while __iter_48.hasNext_k_()
        element = __iter_48.next_k_()
                return true
    end while

    return false
end function

function any_rSequence_Function1Z_k_(m as Object, predicate as Object) as Boolean
    __iter_49 = m.iterator_k_()
    while __iter_49.hasNext_k_()
        element = __iter_49.next_k_()
        if predicate.invoke_AnyN_k_(element) then
            return true
        end if
    end while

    return false
end function

function count_rSequence_k_(m as Object) as Integer
    count = 0
    __iter_50 = m.iterator_k_()
    while __iter_50.hasNext_k_()
        element = __iter_50.next_k_()
        count = (count + 1)
    end while

    return count
end function

function count_rSequence_Function1Z_k_(m as Object, predicate as Object) as Integer
    count = 0
    __iter_51 = m.iterator_k_()
    while __iter_51.hasNext_k_()
        element = __iter_51.next_k_()
        if predicate.invoke_AnyN_k_(element) then
            count = (count + 1)
        end if
    end while

    return count
end function

function fold_rSequence_AnyN_Function2_k_(m as Object, initial as Dynamic, operation as Object) as Dynamic
    accumulator = initial
    __iter_52 = m.iterator_k_()
    while __iter_52.hasNext_k_()
        element = __iter_52.next_k_()
        accumulator = operation.invoke_AnyN_AnyN_k_(accumulator, element)
    end while

    return accumulator
end function

function foldIndexed_rSequence_AnyN_Function3I_k_(m as Object, initial as Dynamic, operation as Object) as Dynamic
    index = 0
    accumulator = initial
    __iter_54 = m.iterator_k_()
    while __iter_54.hasNext_k_()
        element = __iter_54.next_k_()
        __incr_tmp_53 = index
        index = (__incr_tmp_53 + 1)

        accumulator = operation.invoke_AnyN_AnyN_AnyN_k_(__incr_tmp_53, accumulator, element)

    end while

    return accumulator
end function

sub forEach_rSequence_Function1V_k_(m as Object, action as Object)
    __iter_55 = m.iterator_k_()
    while __iter_55.hasNext_k_()
        element = __iter_55.next_k_()
        action.invoke_AnyN_k_(element)
    end while

end sub

sub forEachIndexed_rSequence_Function2IV_k_(m as Object, action as Object)
    index = 0
    __iter_57 = m.iterator_k_()
    while __iter_57.hasNext_k_()
        item = __iter_57.next_k_()
        __incr_tmp_56 = index
        index = (__incr_tmp_56 + 1)

        action.invoke_AnyN_AnyN_k_(__incr_tmp_56, item)

    end while

end sub

function none_rSequence_k_(m as Object) as Boolean
    __iter_58 = m.iterator_k_()
    while __iter_58.hasNext_k_()
        element = __iter_58.next_k_()
                return false
    end while

    return true
end function

function none_rSequence_Function1Z_k_(m as Object, predicate as Object) as Boolean
    __iter_59 = m.iterator_k_()
    while __iter_59.hasNext_k_()
        element = __iter_59.next_k_()
        if predicate.invoke_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function reduce_rSequence_Function2_k_(m as Object, operation as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        throw UnsupportedOperationException_create_StrN_k_("Empty sequence can't be reduced.")
    end if
    accumulator = iterator.next_k_()
    while iterator.hasNext_k_()
        accumulator = operation.invoke_AnyN_AnyN_k_(accumulator, iterator.next_k_())
    end while
    return accumulator
end function

function reduceIndexed_rSequence_Function3I_k_(m as Object, operation as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        throw UnsupportedOperationException_create_StrN_k_("Empty sequence can't be reduced.")
    end if
    index = 1
    accumulator = iterator.next_k_()
    while iterator.hasNext_k_()
        __incr_tmp_60 = index
        index = (__incr_tmp_60 + 1)

        accumulator = operation.invoke_AnyN_AnyN_AnyN_k_(__incr_tmp_60, accumulator, iterator.next_k_())
    end while
    return accumulator
end function

function reduceIndexedOrNull_rSequence_Function3I_k_(m as Object, operation as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        return invalid
    end if
    index = 1
    accumulator = iterator.next_k_()
    while iterator.hasNext_k_()
        __incr_tmp_61 = index
        index = (__incr_tmp_61 + 1)

        accumulator = operation.invoke_AnyN_AnyN_AnyN_k_(__incr_tmp_61, accumulator, iterator.next_k_())
    end while
    return accumulator
end function

function reduceOrNull_rSequence_Function2_k_(m as Object, operation as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        return invalid
    end if
    accumulator = iterator.next_k_()
    while iterator.hasNext_k_()
        accumulator = operation.invoke_AnyN_AnyN_k_(accumulator, iterator.next_k_())
    end while
    return accumulator
end function

function scan_rSequence_AnyN_Function2_k_(m as Object, initial as Dynamic, operation as Object) as Object
    return Anon_59d8decf_create_Sequence_AnyN_Function2_k_(m, initial, operation)
end function

function scanIndexed_rSequence_AnyN_Function3I_k_(m as Object, initial as Dynamic, operation as Object) as Object
    return Anon_677a3fce_create_Sequence_AnyN_Function3I_k_(m, initial, operation)
end function

function sum_rSequenceB_k_(m as Object) as Integer
    sum = 0
    __iter_62 = m.iterator_k_()
    while __iter_62.hasNext_k_()
        element = __iter_62.next_k_()
        sum = (sum + element)

    end while

    return sum
end function

function sum_rSequenceS_k_(m as Object) as Integer
    sum = 0
    __iter_63 = m.iterator_k_()
    while __iter_63.hasNext_k_()
        element = __iter_63.next_k_()
        sum = (sum + element)

    end while

    return sum
end function

function sum_rSequenceI_k_(m as Object) as Integer
    sum = 0
    __iter_64 = m.iterator_k_()
    while __iter_64.hasNext_k_()
        element = __iter_64.next_k_()
        sum = (sum + element)

    end while

    return sum
end function

function sum_rSequenceJ_k_(m as Object) as LongInteger
    sum = 0&
    __iter_65 = m.iterator_k_()
    while __iter_65.hasNext_k_()
        element = __iter_65.next_k_()
        sum = (sum + element)

    end while

    return sum
end function

function sum_rSequenceF_k_(m as Object) as Float
    sum = 0.0!
    __iter_66 = m.iterator_k_()
    while __iter_66.hasNext_k_()
        element = __iter_66.next_k_()
        sum = (sum + element)

    end while

    return sum
end function

function sum_rSequenceD_k_(m as Object) as Double
    sum = 0.0#
    __iter_67 = m.iterator_k_()
    while __iter_67.hasNext_k_()
        element = __iter_67.next_k_()
        sum = (sum + element)

    end while

    return sum
end function

function sumBy_rSequence_Function1I_k_(m as Object, selector as Object) as Integer
    sum = 0
    __iter_68 = m.iterator_k_()
    while __iter_68.hasNext_k_()
        element = __iter_68.next_k_()
        sum = (sum + selector.invoke_AnyN_k_(element))

    end while

    return sum
end function

function sumByDouble_rSequence_Function1D_k_(m as Object, selector as Object) as Double
    sum = 0.0#
    __iter_69 = m.iterator_k_()
    while __iter_69.hasNext_k_()
        element = __iter_69.next_k_()
        sum = (sum + selector.invoke_AnyN_k_(element))

    end while

    return sum
end function

function sumOf_rSequence_Function1I_k_(m as Object, selector as Object) as Integer
    sum = 0
    __iter_70 = m.iterator_k_()
    while __iter_70.hasNext_k_()
        element = __iter_70.next_k_()
        sum = (sum + selector.invoke_AnyN_k_(element))

    end while

    return sum
end function

function sumOf_rSequence_Function1J_k_(m as Object, selector as Object) as LongInteger
    sum = 0&
    __iter_71 = m.iterator_k_()
    while __iter_71.hasNext_k_()
        element = __iter_71.next_k_()
        sum = (sum + selector.invoke_AnyN_k_(element))

    end while

    return sum
end function

function sumOf_rSequence_Function1D_k_(m as Object, selector as Object) as Double
    sum = 0.0#
    __iter_72 = m.iterator_k_()
    while __iter_72.hasNext_k_()
        element = __iter_72.next_k_()
        sum = (sum + selector.invoke_AnyN_k_(element))

    end while

    return sum
end function

function sumOf_rSequence_Function1UInt_k_(m as Object, selector as Object) as Object
    sum = UInt_create_I_k_(0)
    __iter_73 = m.iterator_k_()
    while __iter_73.hasNext_k_()
        element = __iter_73.next_k_()
        tmp0 = sum
        tmp2 = selector.invoke_AnyN_k_(element)
        tmp_ret_0 = invalid

        while true
            this = tmp0
            other = tmp2
            tmp_ret_0 = UInt_create_I_k_(this.__get_data() + other.__get_data())
            exit while
        end while
        sum = tmp_ret_0

    end while

    return sum
end function

function sumOf_rSequence_Function1ULong_k_(m as Object, selector as Object) as Object
    sum = ULong_create_J_k_(0&)
    __iter_74 = m.iterator_k_()
    while __iter_74.hasNext_k_()
        element = __iter_74.next_k_()
        tmp0 = sum
        tmp2 = selector.invoke_AnyN_k_(element)
        tmp_ret_0 = invalid

        while true
            this = tmp0
            other = tmp2
            tmp_ret_0 = ULong_create_J_k_(this.__get_data() + other.__get_data())
            exit while
        end while
        sum = tmp_ret_0

    end while

    return sum
end function

function max_rSequence_k_(m as Object) as Object
    tmp0_elvis_lhs = maxOrNull_rSequence_k_(m)
    __when_tmp1 = invalid
    if tmp0_elvis_lhs = invalid then
        throw NoSuchElementException_create_k_()
    else if true then
        __when_tmp1 = tmp0_elvis_lhs
    end if
    return __when_tmp1

end function

function maxOrNull_rSequence_k_(m as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        return invalid
    end if
    max = iterator.next_k_()
    while iterator.hasNext_k_()
        e = iterator.next_k_()
        if brsCompareTo_AnyN_AnyN_k_(max, e) < 0 then
            max = e
        end if
    end while
    return max
end function

function min_rSequence_k_(m as Object) as Object
    tmp0_elvis_lhs = minOrNull_rSequence_k_(m)
    __when_tmp2 = invalid
    if tmp0_elvis_lhs = invalid then
        throw NoSuchElementException_create_k_()
    else if true then
        __when_tmp2 = tmp0_elvis_lhs
    end if
    return __when_tmp2

end function

function minOrNull_rSequence_k_(m as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        return invalid
    end if
    min = iterator.next_k_()
    while iterator.hasNext_k_()
        e = iterator.next_k_()
        if brsCompareTo_AnyN_AnyN_k_(min, e) > 0 then
            min = e
        end if
    end while
    return min
end function

function groupBy_rSequence_Function1_k_(m as Object, keySelector as Object) as Object
    tmp0 = m
    tmp2 = HashMap_create_k_()
    tmp_ret_2 = invalid

    while true
        this = tmp0
        destination = tmp2
        __iter_75 = this.iterator_k_()
        while __iter_75.hasNext_k_()
            element = __iter_75.next_k_()
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

function groupBy_rSequence_Function1_Function1_k_(m as Object, keySelector as Object, valueTransform as Object) as Object
    tmp0 = m
    tmp2 = HashMap_create_k_()
    tmp_ret_2 = invalid

    while true
        this = tmp0
        destination = tmp2
        __iter_76 = this.iterator_k_()
        while __iter_76.hasNext_k_()
            element = __iter_76.next_k_()
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

function groupByTo_rSequence_Any_Function1_k_(m as Object, destination as Object, keySelector as Object) as Object
    __iter_77 = m.iterator_k_()
    while __iter_77.hasNext_k_()
        element = __iter_77.next_k_()
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

function groupByTo_rSequence_Any_Function1_Function1_k_(m as Object, destination as Object, keySelector as Object, valueTransform as Object) as Object
    __iter_78 = m.iterator_k_()
    while __iter_78.hasNext_k_()
        element = __iter_78.next_k_()
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

function partition_rSequence_Function1Z_k_(m as Object, predicate as Object) as Object
    first = ArrayList_create_k_()
    second = ArrayList_create_k_()
    __iter_79 = m.iterator_k_()
    while __iter_79.hasNext_k_()
        element = __iter_79.next_k_()
        if predicate.invoke_AnyN_k_(element) then
            first.add_AnyN_k_(element)
        else if true then
            second.add_AnyN_k_(element)
        end if

    end while

    return Pair_create_AnyN_AnyN_k_(first, second)
end function

function joinTo_rSequence_Any_CharSequence_CharSequence_CharSequence_I_CharSequence_Function1CharSequenceN_k_(m as Object, buffer as Object, separator = ", ", prefix = "", postfix = "", limit = -1, truncated = "...", transform = invalid) as Object
    if separator = invalid then
        separator = ", "
    end if
    if prefix = invalid then
        prefix = ""
    end if
    if postfix = invalid then
        postfix = ""
    end if
    if limit = invalid then
        limit = -1
    end if
    if truncated = invalid then
        truncated = "..."
    end if
    if transform = invalid then
        transform = invalid
    end if
    buffer.append_CharSequenceN_k_(prefix)
    count = 0
    __iter_80 = m.iterator_k_()
    while __iter_80.hasNext_k_()
        element = __iter_80.next_k_()
        count = (count + 1)
        if count > 1 then
            buffer.append_CharSequenceN_k_(separator)
        end if
        if (limit < 0) or (count <= limit) then
            if transform <> invalid then
                buffer.append_CharSequenceN_k_(transform.invoke_AnyN_k_(element))
            else if __kotlin_isInstanceOf(element, "CharSequence") then
                buffer.append_CharSequenceN_k_(element)
            else if __kotlin_isInstanceOf(element, "Char") then
                buffer.append_C_k_(element)
            else if true then
                buffer.append_CharSequenceN_k_(toString_AnyN_k_(element))
            end if
        else if true then
            exit while
        end if

    end while

    if (limit >= 0) and (count > limit) then
        buffer.append_CharSequenceN_k_(truncated)
    end if
    buffer.append_CharSequenceN_k_(postfix)
    return buffer
end function

function joinToString_rtpr9o_k_(m as Object, separator = ", ", prefix = "", postfix = "", limit = -1, truncated = "...", transform = invalid) as String
    if separator = invalid then
        separator = ", "
    end if
    if prefix = invalid then
        prefix = ""
    end if
    if postfix = invalid then
        postfix = ""
    end if
    if limit = invalid then
        limit = -1
    end if
    if truncated = invalid then
        truncated = "..."
    end if
    if transform = invalid then
        transform = invalid
    end if
    return joinTo_rSequence_Any_CharSequence_CharSequence_CharSequence_I_CharSequence_Function1CharSequenceN_k_(m, StringBuilder_create_k_(), separator, prefix, postfix, limit, truncated, transform).toString()
end function

function onEach_rSequence_Function1V_k_(m as Object, action as Object) as Object
    return map_rSequence_Function1_k_(m, onEach_lambda_create_Function1V_k_(action))
end function

function onEachIndexed_rSequence_Function2IV_k_(m as Object, action as Object) as Object
    return mapIndexed_rSequence_Function2I_k_(m, onEachIndexed_lambda_create_Function2IV_k_(action))
end function

function withIndex_rSequence_k_(m as Object) as Object
    return mapIndexed_rSequence_Function2I_k_(m, withIndex_lambda_create_k_())
end function

function mapNotNull_rSequence_Function1_k_(m as Object, transform as Object) as Object
    return TransformingSequence_create_Sequence_Function1_k_(FilteringSequence_create_Sequence_Z_Function1Z_k_(map_rSequence_Function1_k_(m, transform), true, mapNotNull_lambda_create_k_()), mapNotNull_lambda_1_create_k_())
end function

function mapIndexedNotNull_rSequence_Function2I_k_(m as Object, transform as Object) as Object
    return TransformingSequence_create_Sequence_Function1_k_(FilteringSequence_create_Sequence_Z_Function1Z_k_(mapIndexed_rSequence_Function2I_k_(m, transform), true, mapIndexedNotNull_lambda_create_k_()), mapIndexedNotNull_lambda_1_create_k_())
end function

function elementAt_lambda_create_I_k_(_index as Integer) as Object
    this = {}
    this.__type = "elementAt_lambda"
    this.__proto = ["elementAt_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = elementAt_lambda_invoke_AnyN_k_
    this._index = _index
    return this
end function

function elementAt_lambda_invoke_AnyN_k_(it as Integer) as Dynamic
    throw IndexOutOfBoundsException_create_StrN_k_(("Sequence doesn't contain element at index " + __kotlin_numToStr_I_k_(m._index)) + ".")
end function

function Anon_7d4f5b2f_create_Sequence_AnyN_Function2_k_(_this_scan as Object, _initial as Dynamic, _operation as Object) as Object
    this = {}
    this.__type = "Anon_7d4f5b2f"
    this.__proto = ["Anon_7d4f5b2f", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = Anon_7d4f5b2f_hasNext_k_
    this.next_k_ = Anon_7d4f5b2f_next_k_
    this.__get_iterator = Anon_7d4f5b2f___get_iterator_k_
    this.__get_accumulator = Anon_7d4f5b2f___get_accumulator_k_
    this.__set_accumulator = Anon_7d4f5b2f___set_accumulator_AnyN_k_
    this.__get_firstEmitted = Anon_7d4f5b2f___get_firstEmitted_k_
    this.__set_firstEmitted = Anon_7d4f5b2f___set_firstEmitted_Z_k_
    this.iterator = _this_scan.iterator_k_()
    this.accumulator = _initial
    this.firstEmitted = false
    this._initial = _initial
    this._operation = _operation
    return this
end function

function Anon_7d4f5b2f_hasNext_k_() as Boolean
    return not m.__get_firstEmitted() or m.__get_iterator().hasNext_k_()
end function

function Anon_7d4f5b2f_next_k_() as Dynamic
    if not m.__get_firstEmitted() then
        m.__set_firstEmitted(true)
        return m._initial
    end if
    m.__set_accumulator(m._operation.invoke_AnyN_AnyN_k_(m.__get_accumulator(), m.__get_iterator().next_k_()))
    return m.__get_accumulator()
end function

function Anon_7d4f5b2f___get_iterator_k_() as Object
    return m.iterator
end function

function Anon_7d4f5b2f___get_accumulator_k_() as Dynamic
    return m.accumulator
end function

sub Anon_7d4f5b2f___set_accumulator_AnyN_k_(value as Dynamic)
    m.accumulator = value
end sub

function Anon_7d4f5b2f___get_firstEmitted_k_() as Boolean
    return m.firstEmitted
end function

sub Anon_7d4f5b2f___set_firstEmitted_Z_k_(value as Boolean)
    m.firstEmitted = value
end sub

function Anon_59d8decf_create_Sequence_AnyN_Function2_k_(_this_scan as Object, _initial as Dynamic, _operation as Object) as Object
    this = {}
    this.__type = "Anon_59d8decf"
    this.__proto = ["Anon_59d8decf", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = Anon_59d8decf_iterator_k_
    this._this_scan = _this_scan
    this._initial = _initial
    this._operation = _operation
    return this
end function

function Anon_59d8decf_iterator_k_() as Object
    return Anon_7d4f5b2f_create_Sequence_AnyN_Function2_k_(m._this_scan, m._initial, m._operation)
end function

function Anon_6fadf7a5_create_Sequence_AnyN_Function3I_k_(_this_scanIndexed as Object, _initial as Dynamic, _operation as Object) as Object
    this = {}
    this.__type = "Anon_6fadf7a5"
    this.__proto = ["Anon_6fadf7a5", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = Anon_6fadf7a5_hasNext_k_
    this.next_k_ = Anon_6fadf7a5_next_k_
    this.__get_iterator = Anon_6fadf7a5___get_iterator_k_
    this.__get_accumulator = Anon_6fadf7a5___get_accumulator_k_
    this.__set_accumulator = Anon_6fadf7a5___set_accumulator_AnyN_k_
    this.__get_index = Anon_6fadf7a5___get_index_k_
    this.__set_index = Anon_6fadf7a5___set_index_I_k_
    this.__get_firstEmitted = Anon_6fadf7a5___get_firstEmitted_k_
    this.__set_firstEmitted = Anon_6fadf7a5___set_firstEmitted_Z_k_
    this.iterator = _this_scanIndexed.iterator_k_()
    this.accumulator = _initial
    this.index = 0
    this.firstEmitted = false
    this._initial = _initial
    this._operation = _operation
    return this
end function

function Anon_6fadf7a5_hasNext_k_() as Boolean
    return not m.__get_firstEmitted() or m.__get_iterator().hasNext_k_()
end function

function Anon_6fadf7a5_next_k_() as Dynamic
    if not m.__get_firstEmitted() then
        m.__set_firstEmitted(true)
        return m._initial
    end if
    __incr_tmp_81 = m.__get_index()
    m.__set_index(__incr_tmp_81 + 1)
    m.__set_accumulator(m._operation.invoke_AnyN_AnyN_AnyN_k_(__incr_tmp_81, m.__get_accumulator(), m.__get_iterator().next_k_()))

    return m.__get_accumulator()
end function

function Anon_6fadf7a5___get_iterator_k_() as Object
    return m.iterator
end function

function Anon_6fadf7a5___get_accumulator_k_() as Dynamic
    return m.accumulator
end function

sub Anon_6fadf7a5___set_accumulator_AnyN_k_(value as Dynamic)
    m.accumulator = value
end sub

function Anon_6fadf7a5___get_index_k_() as Integer
    return m.index
end function

sub Anon_6fadf7a5___set_index_I_k_(value as Integer)
    m.index = value
end sub

function Anon_6fadf7a5___get_firstEmitted_k_() as Boolean
    return m.firstEmitted
end function

sub Anon_6fadf7a5___set_firstEmitted_Z_k_(value as Boolean)
    m.firstEmitted = value
end sub

function Anon_677a3fce_create_Sequence_AnyN_Function3I_k_(_this_scanIndexed as Object, _initial as Dynamic, _operation as Object) as Object
    this = {}
    this.__type = "Anon_677a3fce"
    this.__proto = ["Anon_677a3fce", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = Anon_677a3fce_iterator_k_
    this._this_scanIndexed = _this_scanIndexed
    this._initial = _initial
    this._operation = _operation
    return this
end function

function Anon_677a3fce_iterator_k_() as Object
    return Anon_6fadf7a5_create_Sequence_AnyN_Function3I_k_(m._this_scanIndexed, m._initial, m._operation)
end function

function onEach_lambda_create_Function1V_k_(_action as Object) as Object
    this = {}
    this.__type = "onEach_lambda"
    this.__proto = ["onEach_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = onEach_lambda_invoke_AnyN_k_
    this._action = _action
    return this
end function

function onEach_lambda_invoke_AnyN_k_(it as Dynamic) as Dynamic
    m._action.invoke_AnyN_k_(it)
    return it
end function

function onEachIndexed_lambda_create_Function2IV_k_(_action as Object) as Object
    this = {}
    this.__type = "onEachIndexed_lambda"
    this.__proto = ["onEachIndexed_lambda", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = onEachIndexed_lambda_invoke_AnyN_AnyN_k_
    this._action = _action
    return this
end function

function onEachIndexed_lambda_invoke_AnyN_AnyN_k_(index as Integer, element as Dynamic) as Dynamic
    m._action.invoke_AnyN_AnyN_k_(index, element)
    return element
end function

function withIndex_lambda_create_k_() as Object
    this = {}
    this.__type = "withIndex_lambda"
    this.__proto = ["withIndex_lambda", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = withIndex_lambda_invoke_AnyN_AnyN_k_
    return this
end function

function withIndex_lambda_invoke_AnyN_AnyN_k_(index as Integer, element as Dynamic) as Object
    return IndexedValue_create_I_AnyN_k_(index, element)
end function

function mapNotNull_lambda_create_k_() as Object
    this = {}
    this.__type = "mapNotNull_lambda"
    this.__proto = ["mapNotNull_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = mapNotNull_lambda_invoke_AnyN_k_
    return this
end function

function mapNotNull_lambda_invoke_AnyN_k_(it as Dynamic) as Boolean
    return it <> invalid
end function

function mapNotNull_lambda_1_create_k_() as Object
    this = {}
    this.__type = "mapNotNull_lambda_1"
    this.__proto = ["mapNotNull_lambda_1", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = mapNotNull_lambda_1_invoke_AnyN_k_
    return this
end function

function mapNotNull_lambda_1_invoke_AnyN_k_(it as Dynamic) as Object
    return it
end function

function mapIndexedNotNull_lambda_create_k_() as Object
    this = {}
    this.__type = "mapIndexedNotNull_lambda"
    this.__proto = ["mapIndexedNotNull_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = mapIndexedNotNull_lambda_invoke_AnyN_k_
    return this
end function

function mapIndexedNotNull_lambda_invoke_AnyN_k_(it as Dynamic) as Boolean
    return it <> invalid
end function

function mapIndexedNotNull_lambda_1_create_k_() as Object
    this = {}
    this.__type = "mapIndexedNotNull_lambda_1"
    this.__proto = ["mapIndexedNotNull_lambda_1", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = mapIndexedNotNull_lambda_1_invoke_AnyN_k_
    return this
end function

function mapIndexedNotNull_lambda_1_invoke_AnyN_k_(it as Dynamic) as Object
    return it
end function
