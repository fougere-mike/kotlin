function contains_rSequence_AnyN_k_(m as Object, element as Dynamic) as Boolean
    return indexOf_rSequence_AnyN_k_(m, element) >= 0
end function

function elementAt_rSequence_I_k_(m as Object, index as Integer) as Dynamic
    return elementAtOrElse_rSequence_I_Function1I_k_(m, index, {index: index, invoke: function(it as Integer) as Dynamic
        throw IndexOutOfBoundsException_create_StrN_k_(("Sequence doesn't contain element at index " + m.index) + ".")
    end function})
end function

function elementAtOrElse_rSequence_I_Function1I_k_(m as Object, index as Integer, defaultValue as Object) as Dynamic
    if index < 0 then
        return defaultValue.invoke(index)
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
    return defaultValue.invoke(index)
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
    return firstOrNull_rSequence_Function1Z_k_(m, predicate)
end function

function findLast_rSequence_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    return lastOrNull_rSequence_Function1Z_k_(m, predicate)
end function

function first_rSequence_k_(m as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        throw NoSuchElementException_create_StrN_k_("Sequence is empty.")
    end if
    return iterator.next_k_()
end function

function first_rSequence_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    for each element in m
        if predicate.invoke(element) then
            return element
        end if
    end for
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
    for each element in m
        if predicate.invoke(element) then
            return element
        end if
    end for
    return invalid
end function

function firstNotNullOf_rSequence_Function1_k_(m as Object, transform as Object) as Object
    tmp0_elvis_lhs = firstNotNullOfOrNull_rSequence_Function1_k_(m, transform)
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        throw NoSuchElementException_create_StrN_k_("No element of the sequence was transformed to a non-null value.")
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    return __when_tmp0

end function

function firstNotNullOfOrNull_rSequence_Function1_k_(m as Object, transform as Object) as Dynamic
    for each element in m
        result = transform.invoke(element)
        if result <> invalid then
            return result
        end if

    end for
    return invalid
end function

function indexOf_rSequence_AnyN_k_(m as Object, element as Dynamic) as Integer
    index = 0
    for each item in m
        if element = item then
            return index
        end if
        index = (index + 1)

    end for
    return -1
end function

function indexOfFirst_rSequence_Function1Z_k_(m as Object, predicate as Object) as Integer
    index = 0
    for each item in m
        if predicate.invoke(item) then
            return index
        end if
        index = (index + 1)

    end for
    return -1
end function

function indexOfLast_rSequence_Function1Z_k_(m as Object, predicate as Object) as Integer
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
    for each element in m
        if predicate.invoke(element) then
            last = element
            found = true
        end if
    end for
    if not found then
        throw NoSuchElementException_create_StrN_k_("Sequence contains no element matching the predicate.")
    end if
    return last
end function

function lastIndexOf_rSequence_AnyN_k_(m as Object, element as Dynamic) as Integer
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
    for each element in m
        if predicate.invoke(element) then
            last = element
        end if
    end for
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
    for each element in m
        if predicate.invoke(element) then
            if found then
                throw IllegalArgumentException_create_StrN_k_("Sequence contains more than one matching element.")
            end if
            single = element
            found = true
        end if
    end for
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

function all_rSequence_Function1Z_k_(m as Object, predicate as Object) as Boolean
    for each element in m
        if not predicate.invoke(element) then
            return false
        end if
    end for
    return true
end function

function any_rSequence_k_(m as Object) as Boolean
    for each element in m
                return true
    end for
    return false
end function

function any_rSequence_Function1Z_k_(m as Object, predicate as Object) as Boolean
    for each element in m
        if predicate.invoke(element) then
            return true
        end if
    end for
    return false
end function

function count_rSequence_k_(m as Object) as Integer
    count = 0
    for each element in m
        count = (count + 1)
    end for
    return count
end function

function count_rSequence_Function1Z_k_(m as Object, predicate as Object) as Integer
    count = 0
    for each element in m
        if predicate.invoke(element) then
            count = (count + 1)
        end if
    end for
    return count
end function

function fold_rSequence_AnyN_Function2_k_(m as Object, initial as Dynamic, operation as Object) as Dynamic
    accumulator = initial
    for each element in m
        accumulator = operation.invoke(accumulator, element)
    end for
    return accumulator
end function

function foldIndexed_rSequence_AnyN_Function3I_k_(m as Object, initial as Dynamic, operation as Object) as Dynamic
    index = 0
    accumulator = initial
    for each element in m
        __incr_tmp_20 = index
        index = (__incr_tmp_20 + 1)

        accumulator = operation.invoke(__incr_tmp_20, accumulator, element)

    end for
    return accumulator
end function

sub forEach_rSequence_Function1V_k_(m as Object, action as Object)
    for each element in m
        action.invoke(element)
    end for
end sub

sub forEachIndexed_rSequence_Function2IV_k_(m as Object, action as Object)
    index = 0
    for each item in m
        __incr_tmp_21 = index
        index = (__incr_tmp_21 + 1)

        action.invoke(__incr_tmp_21, item)

    end for
end sub

function none_rSequence_k_(m as Object) as Boolean
    for each element in m
                return false
    end for
    return true
end function

function none_rSequence_Function1Z_k_(m as Object, predicate as Object) as Boolean
    for each element in m
        if predicate.invoke(element) then
            return false
        end if
    end for
    return true
end function

function reduce_rSequence_Function2_k_(m as Object, operation as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        throw UnsupportedOperationException_create_StrN_k_("Empty sequence can't be reduced.")
    end if
    accumulator = iterator.next_k_()
    while iterator.hasNext_k_()
        accumulator = operation.invoke(accumulator, iterator.next_k_())
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
        __incr_tmp_22 = index
        index = (__incr_tmp_22 + 1)

        accumulator = operation.invoke(__incr_tmp_22, accumulator, iterator.next_k_())
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
        __incr_tmp_23 = index
        index = (__incr_tmp_23 + 1)

        accumulator = operation.invoke(__incr_tmp_23, accumulator, iterator.next_k_())
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
        accumulator = operation.invoke(accumulator, iterator.next_k_())
    end while
    return accumulator
end function

function scan_rSequence_AnyN_Function2_k_(m as Object, initial as Dynamic, operation as Object) as Object
    return Anon_4ef408da_create_k_()
end function

function scanIndexed_rSequence_AnyN_Function3I_k_(m as Object, initial as Dynamic, operation as Object) as Object
    return Anon_c03da6b_create_k_()
end function

function sum_rSequenceB_k_(m as Object) as Integer
    sum = 0
    for each element in m
        sum = (sum + element)

    end for
    return sum
end function

function sum_rSequenceS_k_(m as Object) as Integer
    sum = 0
    for each element in m
        sum = (sum + element)

    end for
    return sum
end function

function sum_rSequenceI_k_(m as Object) as Integer
    sum = 0
    for each element in m
        sum = (sum + element)

    end for
    return sum
end function

function sum_rSequenceJ_k_(m as Object) as LongInteger
    sum = 0&
    for each element in m
        sum = (sum + element)

    end for
    return sum
end function

function sum_rSequenceF_k_(m as Object) as Float
    sum = 0.0!
    for each element in m
        sum = (sum + element)

    end for
    return sum
end function

function sum_rSequenceD_k_(m as Object) as Double
    sum = 0.0#
    for each element in m
        sum = (sum + element)

    end for
    return sum
end function

function sumBy_rSequence_Function1I_k_(m as Object, selector as Object) as Integer
    sum = 0
    for each element in m
        sum = (sum + selector.invoke(element))

    end for
    return sum
end function

function sumByDouble_rSequence_Function1D_k_(m as Object, selector as Object) as Double
    sum = 0.0#
    for each element in m
        sum = (sum + selector.invoke(element))

    end for
    return sum
end function

function sumOf_rSequence_Function1I_k_(m as Object, selector as Object) as Integer
    sum = 0
    for each element in m
        sum = (sum + selector.invoke(element))

    end for
    return sum
end function

function sumOf_rSequence_Function1J_k_(m as Object, selector as Object) as LongInteger
    sum = 0&
    for each element in m
        sum = (sum + selector.invoke(element))

    end for
    return sum
end function

function sumOf_rSequence_Function1D_k_(m as Object, selector as Object) as Double
    sum = 0.0#
    for each element in m
        sum = (sum + selector.invoke(element))

    end for
    return sum
end function

function sumOf_rSequence_Function1UInt_k_(m as Object, selector as Object) as Object
    sum = 0
    for each element in m
        sum = (sum + selector.invoke(element))

    end for
    return sum
end function

function sumOf_rSequence_Function1ULong_k_(m as Object, selector as Object) as Object
    sum = 0&
    for each element in m
        sum = (sum + selector.invoke(element))

    end for
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
        if (max < e) < 0 then
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
        if (min > e) > 0 then
            min = e
        end if
    end while
    return min
end function

function groupBy_rSequence_Function1_k_(m as Object, keySelector as Object) as Object
    return groupByTo_rSequence_Any_Function1_k_(m, HashMap_create_k_(), keySelector)
end function

function groupBy_rSequence_Function1_Function1_k_(m as Object, keySelector as Object, valueTransform as Object) as Object
    return groupByTo_rSequence_Any_Function1_Function1_k_(m, HashMap_create_k_(), keySelector, valueTransform)
end function

function groupByTo_rSequence_Any_Function1_k_(m as Object, destination as Object, keySelector as Object) as Object
    for each element in m
        key = keySelector.invoke(element)
        list = getOrPut_rMutableMap_AnyN_Function0_k_(destination, key, {invoke: function() as Object
            return ArrayList_create_k_()
        end function})
        list.add_AnyN_k_(element)

    end for
    return destination
end function

function groupByTo_rSequence_Any_Function1_Function1_k_(m as Object, destination as Object, keySelector as Object, valueTransform as Object) as Object
    for each element in m
        key = keySelector.invoke(element)
        list = getOrPut_rMutableMap_AnyN_Function0_k_(destination, key, {invoke: function() as Object
            return ArrayList_create_k_()
        end function})
        list.add_AnyN_k_(valueTransform.invoke(element))

    end for
    return destination
end function

function partition_rSequence_Function1Z_k_(m as Object, predicate as Object) as Object
    first = ArrayList_create_k_()
    second = ArrayList_create_k_()
    for each element in m
        if predicate.invoke(element) then
            first.add_AnyN_k_(element)
        else if true then
            second.add_AnyN_k_(element)
        end if
    end for
    return Pair_create_AnyN_AnyN_k_(first, second)
end function

function joinTo_rSequence_Any_CharSequence_CharSequence_CharSequence_I_CharSequence_Function1CharSequenceN_k_(m as Object, buffer as Object, separator = ", ", prefix = "", postfix = "", limit = -1, truncated = "...", transform = invalid) as Object
    buffer.append_CharSequenceN_k_(prefix)
    count = 0
    for each element in m
        count = (count + 1)
        if count > 1 then
            buffer.append_CharSequenceN_k_(separator)
        end if
        if (limit < 0) or (count <= limit) then
            if transform <> invalid then
                buffer.append_CharSequenceN_k_(transform.invoke(element))
            else if __kotlin_isInstanceOf(element, "CharSequence") then
                buffer.append_CharSequenceN_k_(element)
            else if __kotlin_isInstanceOf(element, "Char") then
                buffer.append_C_k_(element)
            else if true then
                buffer.append_CharSequenceN_k_(toString_AnyN_k_(element))
            end if
        else if true then
            exit for
        end if

    end for
    if (limit >= 0) and (count > limit) then
        buffer.append_CharSequenceN_k_(truncated)
    end if
    buffer.append_CharSequenceN_k_(postfix)
    return buffer
end function

function joinToString_rtpr9o_k_(m as Object, separator = ", ", prefix = "", postfix = "", limit = -1, truncated = "...", transform = invalid) as String
    return joinTo_rSequence_Any_CharSequence_CharSequence_CharSequence_I_CharSequence_Function1CharSequenceN_k_(m, StringBuilder_create_k_(), separator, prefix, postfix, limit, truncated, transform).toString()
end function

function onEach_rSequence_Function1V_k_(m as Object, action as Object) as Object
    return map_rSequence_Function1_k_(m, {action: action, invoke: function(it as Dynamic) as Dynamic
        m.action.invoke(it)
        return it
    end function})
end function

function onEachIndexed_rSequence_Function2IV_k_(m as Object, action as Object) as Object
    return mapIndexed_rSequence_Function2I_k_(m, {action: action, invoke: function(index as Integer, element as Dynamic) as Dynamic
        m.action.invoke(index, element)
        return element
    end function})
end function

function withIndex_rSequence_k_(m as Object) as Object
    return mapIndexed_rSequence_Function2I_k_(m, {invoke: function(index as Integer, element as Dynamic) as Object
        return IndexedValue_create_I_AnyN_k_(index, element)
    end function})
end function

function mapNotNull_rSequence_Function1_k_(m as Object, transform as Object) as Object
    return TransformingSequence_create_Sequence_Function1_k_(FilteringSequence_create_Sequence_Z_Function1Z_k_(map_rSequence_Function1_k_(m, transform), true, {invoke: function(it as Dynamic) as Boolean
        return it <> invalid
    end function}), {invoke: function(it as Dynamic) as Object
        return it
    end function})
end function

function mapIndexedNotNull_rSequence_Function2I_k_(m as Object, transform as Object) as Object
    return TransformingSequence_create_Sequence_Function1_k_(FilteringSequence_create_Sequence_Z_Function1Z_k_(mapIndexed_rSequence_Function2I_k_(m, transform), true, {invoke: function(it as Dynamic) as Boolean
        return it <> invalid
    end function}), {invoke: function(it as Dynamic) as Object
        return it
    end function})
end function
