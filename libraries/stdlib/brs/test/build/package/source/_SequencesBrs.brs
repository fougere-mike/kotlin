function contains_rSequenceAnyN_AnyN_Z_k_(m as Object, element as Dynamic) as Boolean
    return indexOf_rSequenceAnyN_AnyN_I_k_(m, element) >= 0
end function

function elementAt_rSequenceAnyN_I_AnyN_k_(m as Object, index as Integer) as Dynamic
    return elementAtOrElse_rSequenceAnyN_I_Function1IAnyN_AnyN_k_(m, index, {index: index, invoke: function(it as Integer) as Dynamic
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_(("Sequence doesn't contain element at index " + m.index) + ".")
    end function})
end function

function elementAtOrElse_rSequenceAnyN_I_Function1IAnyN_AnyN_k_(m as Object, index as Integer, defaultValue as Object) as Dynamic
    if index < 0 then
        return defaultValue.invoke(index)
    end if
    iterator = m.iterator_IteratorAnyN_k_()
    count = 0
    while iterator.hasNext_Z_k_()
        element = iterator.next_AnyN_k_()
        unary = count
        count = (unary + 1)
        if index = unary then
            return element
        end if
    end while
    return defaultValue.invoke(index)
end function

function elementAtOrNull_rSequenceAnyN_I_AnyN_k_(m as Object, index as Integer) as Dynamic
    if index < 0 then
        return invalid
    end if
    iterator = m.iterator_IteratorAnyN_k_()
    count = 0
    while iterator.hasNext_Z_k_()
        element = iterator.next_AnyN_k_()
        unary = count
        count = (unary + 1)
        if index = unary then
            return element
        end if
    end while
    return invalid
end function

function find_rSequenceAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Object) as Dynamic
    return firstOrNull_rSequenceAnyN_Function1AnyNZ_AnyN_k_(m, predicate)
end function

function findLast_rSequenceAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Object) as Dynamic
    return lastOrNull_rSequenceAnyN_Function1AnyNZ_AnyN_k_(m, predicate)
end function

function first_rSequenceAnyN_AnyN_k_(m as Object) as Dynamic
    iterator = m.iterator_IteratorAnyN_k_()
    if not iterator.hasNext_Z_k_() then
        throw NoSuchElementException_create_StrN_NoSuchElementException_k_("Sequence is empty.")
    end if
    return iterator.next_AnyN_k_()
end function

function first_rSequenceAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Object) as Dynamic
    for each element in m
        if predicate.invoke(element) then
            return element
        end if
    end for
    throw NoSuchElementException_create_StrN_NoSuchElementException_k_("Sequence contains no element matching the predicate.")
end function

function firstOrNull_rSequenceAnyN_AnyN_k_(m as Object) as Dynamic
    iterator = m.iterator_IteratorAnyN_k_()
    if not iterator.hasNext_Z_k_() then
        return invalid
    end if
    return iterator.next_AnyN_k_()
end function

function firstOrNull_rSequenceAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Object) as Dynamic
    for each element in m
        if predicate.invoke(element) then
            return element
        end if
    end for
    return invalid
end function

function firstNotNullOf_rSequenceAnyN_Function1AnyNAnyN_Any_k_(m as Object, transform as Object) as Object
    tmp0_elvis_lhs = firstNotNullOfOrNull_rSequenceAnyN_Function1AnyNAnyN_AnyN_k_(m, transform)
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        throw NoSuchElementException_create_StrN_NoSuchElementException_k_("No element of the sequence was transformed to a non-null value.")
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    return __when_tmp0

end function

function firstNotNullOfOrNull_rSequenceAnyN_Function1AnyNAnyN_AnyN_k_(m as Object, transform as Object) as Dynamic
    for each element in m
        result = transform.invoke(element)
        if result <> invalid then
            return result
        end if

    end for
    return invalid
end function

function indexOf_rSequenceAnyN_AnyN_I_k_(m as Object, element as Dynamic) as Integer
    index = 0
    for each item in m
        if element = item then
            return index
        end if
        index = (index + 1)

    end for
    return -1
end function

function indexOfFirst_rSequenceAnyN_Function1AnyNZ_I_k_(m as Object, predicate as Object) as Integer
    index = 0
    for each item in m
        if predicate.invoke(item) then
            return index
        end if
        index = (index + 1)

    end for
    return -1
end function

function indexOfLast_rSequenceAnyN_Function1AnyNZ_I_k_(m as Object, predicate as Object) as Integer
    lastIndex = -1
    index = 0
    for each item in m
        if predicate.invoke(item) then
            lastIndex = index
        end if
        index = (index + 1)

    end for
    return lastIndex
end function

function last_rSequenceAnyN_AnyN_k_(m as Object) as Dynamic
    iterator = m.iterator_IteratorAnyN_k_()
    if not iterator.hasNext_Z_k_() then
        throw NoSuchElementException_create_StrN_NoSuchElementException_k_("Sequence is empty.")
    end if
    last = iterator.next_AnyN_k_()
    while iterator.hasNext_Z_k_()
        last = iterator.next_AnyN_k_()
    end while
    return last
end function

function last_rSequenceAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Object) as Dynamic
    last = invalid
    found = false
    for each element in m
        if predicate.invoke(element) then
            last = element
            found = true
        end if
    end for
    if not found then
        throw NoSuchElementException_create_StrN_NoSuchElementException_k_("Sequence contains no element matching the predicate.")
    end if
    return last
end function

function lastIndexOf_rSequenceAnyN_AnyN_I_k_(m as Object, element as Dynamic) as Integer
    lastIndex = -1
    index = 0
    for each item in m
        if element = item then
            lastIndex = index
        end if
        index = (index + 1)

    end for
    return lastIndex
end function

function lastOrNull_rSequenceAnyN_AnyN_k_(m as Object) as Dynamic
    iterator = m.iterator_IteratorAnyN_k_()
    if not iterator.hasNext_Z_k_() then
        return invalid
    end if
    last = iterator.next_AnyN_k_()
    while iterator.hasNext_Z_k_()
        last = iterator.next_AnyN_k_()
    end while
    return last
end function

function lastOrNull_rSequenceAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Object) as Dynamic
    last = invalid
    for each element in m
        if predicate.invoke(element) then
            last = element
        end if
    end for
    return last
end function

function single_rSequenceAnyN_AnyN_k_(m as Object) as Dynamic
    iterator = m.iterator_IteratorAnyN_k_()
    if not iterator.hasNext_Z_k_() then
        throw NoSuchElementException_create_StrN_NoSuchElementException_k_("Sequence is empty.")
    end if
    single = iterator.next_AnyN_k_()
    if iterator.hasNext_Z_k_() then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Sequence has more than one element.")
    end if
    return single
end function

function single_rSequenceAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Object) as Dynamic
    single = invalid
    found = false
    for each element in m
        if predicate.invoke(element) then
            if found then
                throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Sequence contains more than one matching element.")
            end if
            single = element
            found = true
        end if
    end for
    if not found then
        throw NoSuchElementException_create_StrN_NoSuchElementException_k_("Sequence contains no element matching the predicate.")
    end if
    return single
end function

function singleOrNull_rSequenceAnyN_AnyN_k_(m as Object) as Dynamic
    iterator = m.iterator_IteratorAnyN_k_()
    if not iterator.hasNext_Z_k_() then
        return invalid
    end if
    single = iterator.next_AnyN_k_()
    if iterator.hasNext_Z_k_() then
        return invalid
    end if
    return single
end function

function singleOrNull_rSequenceAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Object) as Dynamic
    single = invalid
    found = false
    for each element in m
        if predicate.invoke(element) then
            if found then
                return invalid
            end if
            single = element
            found = true
        end if
    end for
    if not found then
        return invalid
    end if
    return single
end function

function all_rSequenceAnyN_Function1AnyNZ_Z_k_(m as Object, predicate as Object) as Boolean
    for each element in m
        if not predicate.invoke(element) then
            return false
        end if
    end for
    return true
end function

function any_rSequenceAnyN_Z_k_(m as Object) as Boolean
    for each element in m
                return true
    end for
    return false
end function

function any_rSequenceAnyN_Function1AnyNZ_Z_k_(m as Object, predicate as Object) as Boolean
    for each element in m
        if predicate.invoke(element) then
            return true
        end if
    end for
    return false
end function

function count_rSequenceAnyN_I_k_(m as Object) as Integer
    count = 0
    for each element in m
        count = (count + 1)
    end for
    return count
end function

function count_rSequenceAnyN_Function1AnyNZ_I_k_(m as Object, predicate as Object) as Integer
    count = 0
    for each element in m
        if predicate.invoke(element) then
            count = (count + 1)
        end if
    end for
    return count
end function

function fold_rSequenceAnyN_AnyN_Function2AnyNAnyNAnyN_AnyN_k_(m as Object, initial as Dynamic, operation as Object) as Dynamic
    accumulator = initial
    for each element in m
        accumulator = operation.invoke(accumulator, element)
    end for
    return accumulator
end function

function foldIndexed_rSequenceAnyN_AnyN_Function3IAnyNAnyNAnyN_AnyN_k_(m as Object, initial as Dynamic, operation as Object) as Dynamic
    index = 0
    accumulator = initial
    for each element in m
        __incr_tmp_4 = index
        index = (__incr_tmp_4 + 1)

        accumulator = operation.invoke(__incr_tmp_4, accumulator, element)

    end for
    return accumulator
end function

sub forEach_rSequenceAnyN_Function1AnyNV_k_(m as Object, action as Object)
    for each element in m
        action.invoke(element)
    end for
end sub

sub forEachIndexed_rSequenceAnyN_Function2IAnyNV_k_(m as Object, action as Object)
    index = 0
    for each item in m
        __incr_tmp_5 = index
        index = (__incr_tmp_5 + 1)

        action.invoke(__incr_tmp_5, item)

    end for
end sub

function none_rSequenceAnyN_Z_k_(m as Object) as Boolean
    for each element in m
                return false
    end for
    return true
end function

function none_rSequenceAnyN_Function1AnyNZ_Z_k_(m as Object, predicate as Object) as Boolean
    for each element in m
        if predicate.invoke(element) then
            return false
        end if
    end for
    return true
end function

function reduce_rSequenceAnyN_Function2AnyNAnyNAnyN_AnyN_k_(m as Object, operation as Object) as Dynamic
    iterator = m.iterator_IteratorAnyN_k_()
    if not iterator.hasNext_Z_k_() then
        throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Empty sequence can't be reduced.")
    end if
    accumulator = iterator.next_AnyN_k_()
    while iterator.hasNext_Z_k_()
        accumulator = operation.invoke(accumulator, iterator.next_AnyN_k_())
    end while
    return accumulator
end function

function reduceIndexed_rSequenceAnyN_Function3IAnyNAnyNAnyN_AnyN_k_(m as Object, operation as Object) as Dynamic
    iterator = m.iterator_IteratorAnyN_k_()
    if not iterator.hasNext_Z_k_() then
        throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Empty sequence can't be reduced.")
    end if
    index = 1
    accumulator = iterator.next_AnyN_k_()
    while iterator.hasNext_Z_k_()
        __incr_tmp_6 = index
        index = (__incr_tmp_6 + 1)

        accumulator = operation.invoke(__incr_tmp_6, accumulator, iterator.next_AnyN_k_())
    end while
    return accumulator
end function

function reduceIndexedOrNull_rSequenceAnyN_Function3IAnyNAnyNAnyN_AnyN_k_(m as Object, operation as Object) as Dynamic
    iterator = m.iterator_IteratorAnyN_k_()
    if not iterator.hasNext_Z_k_() then
        return invalid
    end if
    index = 1
    accumulator = iterator.next_AnyN_k_()
    while iterator.hasNext_Z_k_()
        __incr_tmp_7 = index
        index = (__incr_tmp_7 + 1)

        accumulator = operation.invoke(__incr_tmp_7, accumulator, iterator.next_AnyN_k_())
    end while
    return accumulator
end function

function reduceOrNull_rSequenceAnyN_Function2AnyNAnyNAnyN_AnyN_k_(m as Object, operation as Object) as Dynamic
    iterator = m.iterator_IteratorAnyN_k_()
    if not iterator.hasNext_Z_k_() then
        return invalid
    end if
    accumulator = iterator.next_AnyN_k_()
    while iterator.hasNext_Z_k_()
        accumulator = operation.invoke(accumulator, iterator.next_AnyN_k_())
    end while
    return accumulator
end function

function scan_rSequenceAnyN_AnyN_Function2AnyNAnyNAnyN_SequenceAnyN_k_(m as Object, initial as Dynamic, operation as Object) as Object
    return Anon_a6e6a5b_create_AnonAnyNAnyN_k_()
end function

function scanIndexed_rSequenceAnyN_AnyN_Function3IAnyNAnyNAnyN_SequenceAnyN_k_(m as Object, initial as Dynamic, operation as Object) as Object
    return Anon_cb952f4_create_AnonAnyNAnyN_k_()
end function

function sum_rSequenceB_I_k_(m as Object) as Integer
    sum = 0
    for each element in m
        sum = (sum + element)

    end for
    return sum
end function

function sum_rSequenceS_I_k_(m as Object) as Integer
    sum = 0
    for each element in m
        sum = (sum + element)

    end for
    return sum
end function

function sum_rSequenceI_I_k_(m as Object) as Integer
    sum = 0
    for each element in m
        sum = (sum + element)

    end for
    return sum
end function

function sum_rSequenceJ_J_k_(m as Object) as LongInteger
    sum = 0&
    for each element in m
        sum = (sum + element)

    end for
    return sum
end function

function sum_rSequenceF_F_k_(m as Object) as Float
    sum = 0.0!
    for each element in m
        sum = (sum + element)

    end for
    return sum
end function

function sum_rSequenceD_D_k_(m as Object) as Double
    sum = 0.0#
    for each element in m
        sum = (sum + element)

    end for
    return sum
end function

function sumBy_rSequenceAnyN_Function1AnyNI_I_k_(m as Object, selector as Object) as Integer
    sum = 0
    for each element in m
        sum = (sum + selector.invoke(element))

    end for
    return sum
end function

function sumByDouble_rSequenceAnyN_Function1AnyND_D_k_(m as Object, selector as Object) as Double
    sum = 0.0#
    for each element in m
        sum = (sum + selector.invoke(element))

    end for
    return sum
end function

function sumOf_rSequenceAnyN_Function1AnyNI_I_k_(m as Object, selector as Object) as Integer
    sum = 0
    for each element in m
        sum = (sum + selector.invoke(element))

    end for
    return sum
end function

function sumOf_rSequenceAnyN_Function1AnyNJ_J_k_(m as Object, selector as Object) as LongInteger
    sum = 0&
    for each element in m
        sum = (sum + selector.invoke(element))

    end for
    return sum
end function

function sumOf_rSequenceAnyN_Function1AnyND_D_k_(m as Object, selector as Object) as Double
    sum = 0.0#
    for each element in m
        sum = (sum + selector.invoke(element))

    end for
    return sum
end function

function sumOf_rSequenceAnyN_Function1AnyNUInt_UInt_k_(m as Object, selector as Object) as Object
    sum = 0
    for each element in m
        sum = (sum + selector.invoke(element))

    end for
    return sum
end function

function sumOf_rSequenceAnyN_Function1AnyNULong_ULong_k_(m as Object, selector as Object) as Object
    sum = 0&
    for each element in m
        sum = (sum + selector.invoke(element))

    end for
    return sum
end function

function max_rSequenceAny_Any_k_(m as Object) as Object
    tmp0_elvis_lhs = maxOrNull_rSequenceAny_AnyN_k_(m)
    __when_tmp1 = invalid
    if tmp0_elvis_lhs = invalid then
        throw NoSuchElementException_create_NoSuchElementException_k_()
    else if true then
        __when_tmp1 = tmp0_elvis_lhs
    end if
    return __when_tmp1

end function

function maxOrNull_rSequenceAny_AnyN_k_(m as Object) as Dynamic
    iterator = m.iterator_IteratorAnyN_k_()
    if not iterator.hasNext_Z_k_() then
        return invalid
    end if
    max = iterator.next_AnyN_k_()
    while iterator.hasNext_Z_k_()
        e = iterator.next_AnyN_k_()
        if (max < e) < 0 then
            max = e
        end if
    end while
    return max
end function

function min_rSequenceAny_Any_k_(m as Object) as Object
    tmp0_elvis_lhs = minOrNull_rSequenceAny_AnyN_k_(m)
    __when_tmp2 = invalid
    if tmp0_elvis_lhs = invalid then
        throw NoSuchElementException_create_NoSuchElementException_k_()
    else if true then
        __when_tmp2 = tmp0_elvis_lhs
    end if
    return __when_tmp2

end function

function minOrNull_rSequenceAny_AnyN_k_(m as Object) as Dynamic
    iterator = m.iterator_IteratorAnyN_k_()
    if not iterator.hasNext_Z_k_() then
        return invalid
    end if
    min = iterator.next_AnyN_k_()
    while iterator.hasNext_Z_k_()
        e = iterator.next_AnyN_k_()
        if (min > e) > 0 then
            min = e
        end if
    end while
    return min
end function

function groupBy_rSequenceAnyN_Function1AnyNAnyN_MapAnyNListAnyN_k_(m as Object, keySelector as Object) as Object
    return groupByTo_rSequenceAnyN_Any_Function1AnyNAnyN_Any_k_(m, HashMap_create_HashMapAnyNAnyN_k_(), keySelector)
end function

function groupBy_rSequenceAnyN_Function1AnyNAnyN_Function1AnyNAnyN_MapAnyNListAnyN_k_(m as Object, keySelector as Object, valueTransform as Object) as Object
    return groupByTo_rSequenceAnyN_Any_Function1AnyNAnyN_Function1AnyNAnyN_Any_k_(m, HashMap_create_HashMapAnyNAnyN_k_(), keySelector, valueTransform)
end function

function groupByTo_rSequenceAnyN_Any_Function1AnyNAnyN_Any_k_(m as Object, destination as Object, keySelector as Object) as Object
    for each element in m
        key = keySelector.invoke(element)
        list = getOrPut_rMutableMapAnyNAnyN_AnyN_Function0AnyN_AnyN_k_(destination, key, {invoke: function() as Object
            return ArrayList_create_ArrayListAnyN_k_()
        end function})
        list.add_AnyN_Z_k_(element)

    end for
    return destination
end function

function groupByTo_rSequenceAnyN_Any_Function1AnyNAnyN_Function1AnyNAnyN_Any_k_(m as Object, destination as Object, keySelector as Object, valueTransform as Object) as Object
    for each element in m
        key = keySelector.invoke(element)
        list = getOrPut_rMutableMapAnyNAnyN_AnyN_Function0AnyN_AnyN_k_(destination, key, {invoke: function() as Object
            return ArrayList_create_ArrayListAnyN_k_()
        end function})
        list.add_AnyN_Z_k_(valueTransform.invoke(element))

    end for
    return destination
end function

function partition_rSequenceAnyN_Function1AnyNZ_PairListAnyNListAnyN_k_(m as Object, predicate as Object) as Object
    first = ArrayList_create_ArrayListAnyN_k_()
    second = ArrayList_create_ArrayListAnyN_k_()
    for each element in m
        if predicate.invoke(element) then
            first.add_AnyN_Z_k_(element)
        else if true then
            second.add_AnyN_Z_k_(element)
        end if
    end for
    return Pair_create_AnyN_AnyN_PairAnyNAnyN_k_(first, second)
end function

function joinTo_kxums0_k_(m as Object, buffer as Object, separator = ", ", prefix = "", postfix = "", limit = -1, truncated = "...", transform = invalid) as Object
    buffer.append_CharSequenceN_Appendable_k_(prefix)
    count = 0
    for each element in m
        count = (count + 1)
        if count > 1 then
            buffer.append_CharSequenceN_Appendable_k_(separator)
        end if
        if (limit < 0) or (count <= limit) then
            if transform <> invalid then
                buffer.append_CharSequenceN_Appendable_k_(transform.invoke(element))
            else if __kotlin_isInstanceOf(element, "CharSequence") then
                buffer.append_CharSequenceN_Appendable_k_(element)
            else if __kotlin_isInstanceOf(element, "Char") then
                buffer.append_C_Appendable_k_(element)
            else if true then
                buffer.append_CharSequenceN_Appendable_k_(toString_AnyN_Str_k_(element))
            end if
        else if true then
            exit for
        end if

    end for
    if (limit >= 0) and (count > limit) then
        buffer.append_CharSequenceN_Appendable_k_(truncated)
    end if
    buffer.append_CharSequenceN_Appendable_k_(postfix)
    return buffer
end function

function joinToString_h85ta6_k_(m as Object, separator = ", ", prefix = "", postfix = "", limit = -1, truncated = "...", transform = invalid) as String
    return joinTo_kxums0_k_(m, StringBuilder_create_StringBuilder_k_(), separator, prefix, postfix, limit, truncated, transform).toString()
end function

function onEach_rSequenceAnyN_Function1AnyNV_SequenceAnyN_k_(m as Object, action as Object) as Object
    return map_rSequenceAnyN_Function1AnyNAnyN_SequenceAnyN_k_(m, {action: action, invoke: function(it as Dynamic) as Dynamic
        m.action.invoke(it)
        return it
    end function})
end function

function onEachIndexed_rSequenceAnyN_Function2IAnyNV_SequenceAnyN_k_(m as Object, action as Object) as Object
    return mapIndexed_rSequenceAnyN_Function2IAnyNAnyN_SequenceAnyN_k_(m, {action: action, invoke: function(index as Integer, element as Dynamic) as Dynamic
        m.action.invoke(index, element)
        return element
    end function})
end function

function withIndex_rSequenceAnyN_SequenceIndexedValueAnyN_k_(m as Object) as Object
    return mapIndexed_rSequenceAnyN_Function2IAnyNAnyN_SequenceAnyN_k_(m, {invoke: function(index as Integer, element as Dynamic) as Object
        return IndexedValue_create_I_AnyN_IndexedValueAnyN_k_(index, element)
    end function})
end function

function mapNotNull_rSequenceAnyN_Function1AnyNAnyN_SequenceAny_k_(m as Object, transform as Object) as Object
    return TransformingSequence_create_SequenceAnyN_Function1AnyNAnyN_TransformingSequenceAnyNAnyN_k_(FilteringSequence_create_SequenceAnyN_Z_Function1AnyNZ_FilteringSequenceAnyN_k_(map_rSequenceAnyN_Function1AnyNAnyN_SequenceAnyN_k_(m, transform), true, {invoke: function(it as Dynamic) as Boolean
        return it <> invalid
    end function}), {invoke: function(it as Dynamic) as Object
        return it
    end function})
end function

function mapIndexedNotNull_rSequenceAnyN_Function2IAnyNAnyN_SequenceAny_k_(m as Object, transform as Object) as Object
    return TransformingSequence_create_SequenceAnyN_Function1AnyNAnyN_TransformingSequenceAnyNAnyN_k_(FilteringSequence_create_SequenceAnyN_Z_Function1AnyNZ_FilteringSequenceAnyN_k_(mapIndexed_rSequenceAnyN_Function2IAnyNAnyN_SequenceAnyN_k_(m, transform), true, {invoke: function(it as Dynamic) as Boolean
        return it <> invalid
    end function}), {invoke: function(it as Dynamic) as Object
        return it
    end function})
end function
