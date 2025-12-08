sub forEach_rIterableAnyN_Function1AnyNV_k_(m as Object, action as Function)
    for each element in m
        action.invoke(element)
    end for
end sub

sub forEachIndexed_rIterableAnyN_Function2IAnyNV_k_(m as Object, action as Function)
    index = 0
    for each item in m
        action.invoke(index = (index + 1), item)
    end for
end sub

function map_rIterableAnyN_Function1AnyNAnyN_ListAnyN_k_(m as Object, transform as Function) as Object
    result = ArrayList_create_ArrayListAnyN_k_()
    for each item in m
        result.add(transform.invoke(item))

    end for
    return result
end function

function mapIndexed_rIterableAnyN_Function2IAnyNAnyN_ListAnyN_k_(m as Object, transform as Function) as Object
    result = ArrayList_create_ArrayListAnyN_k_()
    index = 0
    for each item in m
        result.add(transform.invoke(index = (index + 1), item))

    end for
    return result
end function

function mapNotNull_rIterableAnyN_Function1AnyNAnyN_ListAny_k_(m as Object, transform as Function) as Object
    result = ArrayList_create_ArrayListAnyN_k_()
    for each item in m
        transformed = transform.invoke(item)
        if transformed <> invalid then
            result.add(transformed)
        end if

    end for
    return result
end function

function filter_rIterableAnyN_Function1AnyNZ_ListAnyN_k_(m as Object, predicate as Function) as Object
    result = ArrayList_create_ArrayListAnyN_k_()
    for each item in m
        if predicate.invoke(item) then
            result.add(item)
        end if
    end for
    return result
end function

function filterIndexed_rIterableAnyN_Function2IAnyNZ_ListAnyN_k_(m as Object, predicate as Function) as Object
    result = ArrayList_create_ArrayListAnyN_k_()
    index = 0
    for each item in m
        unary = index
        index = (unary + 1)
        if predicate.invoke(unary, item) then
            result.add(item)
        end if

    end for
    return result
end function

function filterNotNull_rIterableAnyN_ListAny_k_(m as Object) as Object
    result = ArrayList_create_ArrayListAnyN_k_()
    for each item in m
        if item <> invalid then
            result.add(item)
        end if
    end for
    return result
end function

function filterNot_rIterableAnyN_Function1AnyNZ_ListAnyN_k_(m as Object, predicate as Function) as Object
    result = ArrayList_create_ArrayListAnyN_k_()
    for each item in m
        if predicate.invoke(item).not() then
            result.add(item)
        end if
    end for
    return result
end function

function find_rIterableAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Function) as Dynamic
    for each element in m
        if predicate.invoke(element) then
            return element
        end if
    end for
    return invalid
end function

function findLast_rIterableAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Function) as Dynamic
    last = invalid
    for each element in m
        if predicate.invoke(element) then
            last = element
        end if
    end for
    return last
end function

function first_rIterableAnyN_AnyN_k_(m as Object) as Dynamic
    tmp0_subject = m
    if __kotlin_isInstanceOf(tmp0_subject, "List") then
        if m.isEmpty() then
            throw NoSuchElementException_create_StrN_NoSuchElementException_k_("List is empty.")
        end if
        return m[0]
    else if true then
        iterator = m.iterator()
        if iterator.hasNext().not() then
            throw NoSuchElementException_create_StrN_NoSuchElementException_k_("Collection is empty.")
        end if
        return iterator.next()
    end if

end function

function first_rIterableAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Function) as Dynamic
    for each element in m
        if predicate.invoke(element) then
            return element
        end if
    end for
    throw NoSuchElementException_create_StrN_NoSuchElementException_k_("Collection contains no element matching the predicate.")
end function

function firstOrNull_rIterableAnyN_AnyN_k_(m as Object) as Dynamic
    tmp0_subject = m
    if __kotlin_isInstanceOf(tmp0_subject, "List") then
        __when_tmp0 = invalid
        if m.isEmpty() then
            __when_tmp0 = invalid
        else if true then
            __when_tmp0 = m[0]
        end if
        return __when_tmp0
    else if true then
        iterator = m.iterator()
        if iterator.hasNext().not() then
            return invalid
        end if
        return iterator.next()
    end if

end function

function firstOrNull_rIterableAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Function) as Dynamic
    for each element in m
        if predicate.invoke(element) then
            return element
        end if
    end for
    return invalid
end function

function last_rIterableAnyN_AnyN_k_(m as Object) as Dynamic
    tmp0_subject = m
    if __kotlin_isInstanceOf(tmp0_subject, "List") then
        if m.isEmpty() then
            throw NoSuchElementException_create_StrN_NoSuchElementException_k_("List is empty.")
        end if
        return m[m.size - 1]
    else if true then
        iterator = m.iterator()
        if iterator.hasNext().not() then
            throw NoSuchElementException_create_StrN_NoSuchElementException_k_("Collection is empty.")
        end if
        last = iterator.next()
        while iterator.hasNext()
            last = iterator.next()
        end while
        return last
    end if

end function

function last_rIterableAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Function) as Dynamic
    last = invalid
    found = false
    for each element in m
        if predicate.invoke(element) then
            last = element
            found = true
        end if
    end for
    if found.not() then
        throw NoSuchElementException_create_StrN_NoSuchElementException_k_("Collection contains no element matching the predicate.")
    end if
    return last
end function

function lastOrNull_rIterableAnyN_AnyN_k_(m as Object) as Dynamic
    tmp0_subject = m
    if __kotlin_isInstanceOf(tmp0_subject, "List") then
        __when_tmp1 = invalid
        if m.isEmpty() then
            __when_tmp1 = invalid
        else if true then
            __when_tmp1 = m[m.size - 1]
        end if
        return __when_tmp1
    else if true then
        iterator = m.iterator()
        if iterator.hasNext().not() then
            return invalid
        end if
        last = iterator.next()
        while iterator.hasNext()
            last = iterator.next()
        end while
        return last
    end if

end function

function lastOrNull_rIterableAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Function) as Dynamic
    last = invalid
    for each element in m
        if predicate.invoke(element) then
            last = element
        end if
    end for
    return last
end function

function any_rIterableAnyN_Z_k_(m as Object) as Boolean
    if __kotlin_isInstanceOf(m, "Collection") then
        return m.isEmpty().not()
    end if
    return m.iterator().hasNext()
end function

function any_rIterableAnyN_Function1AnyNZ_Z_k_(m as Object, predicate as Function) as Boolean
    if __kotlin_isInstanceOf(m, "Collection") and m.isEmpty() then
        return false
    end if
    for each element in m
        if predicate.invoke(element) then
            return true
        end if
    end for
    return false
end function

function all_rIterableAnyN_Function1AnyNZ_Z_k_(m as Object, predicate as Function) as Boolean
    if __kotlin_isInstanceOf(m, "Collection") and m.isEmpty() then
        return true
    end if
    for each element in m
        if predicate.invoke(element).not() then
            return false
        end if
    end for
    return true
end function

function none_rIterableAnyN_Z_k_(m as Object) as Boolean
    if __kotlin_isInstanceOf(m, "Collection") then
        return m.isEmpty()
    end if
    return m.iterator().hasNext().not()
end function

function none_rIterableAnyN_Function1AnyNZ_Z_k_(m as Object, predicate as Function) as Boolean
    if __kotlin_isInstanceOf(m, "Collection") and m.isEmpty() then
        return true
    end if
    for each element in m
        if predicate.invoke(element) then
            return false
        end if
    end for
    return true
end function

function count_rIterableAnyN_I_k_(m as Object) as Integer
    if __kotlin_isInstanceOf(m, "Collection") then
        return m.size
    end if
    count = 0
    for each element in m
        count = (unary + 1)
    end for
    return count
end function

function count_rIterableAnyN_Function1AnyNZ_I_k_(m as Object, predicate as Function) as Integer
    if __kotlin_isInstanceOf(m, "Collection") and m.isEmpty() then
        return 0
    end if
    count = 0
    for each element in m
        if predicate.invoke(element) then
            count = (unary + 1)
        end if
    end for
    return count
end function

function fold_rIterableAnyN_AnyN_Function2AnyNAnyNAnyN_AnyN_k_(m as Object, initial as Dynamic, operation as Function) as Dynamic
    accumulator = initial
    for each element in m
        accumulator = operation.invoke(accumulator, element)
    end for
    return accumulator
end function

function foldIndexed_rIterableAnyN_AnyN_Function3IAnyNAnyNAnyN_AnyN_k_(m as Object, initial as Dynamic, operation as Function) as Dynamic
    index = 0
    accumulator = initial
    for each element in m
        accumulator = operation.invoke(index = (index + 1), accumulator, element)
    end for
    return accumulator
end function

function reduce_rIterableAnyN_Function2AnyNAnyNAnyN_AnyN_k_(m as Object, operation as Function) as Dynamic
    iterator = m.iterator()
    if iterator.hasNext().not() then
        throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Empty collection can't be reduced.")
    end if
    accumulator = iterator.next()
    while iterator.hasNext()
        accumulator = operation.invoke(accumulator, iterator.next())
    end while
    return accumulator
end function

function reduceIndexed_rIterableAnyN_Function3IAnyNAnyNAnyN_AnyN_k_(m as Object, operation as Function) as Dynamic
    iterator = m.iterator()
    if iterator.hasNext().not() then
        throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Empty collection can't be reduced.")
    end if
    index = 1
    accumulator = iterator.next()
    while iterator.hasNext()
        accumulator = operation.invoke(index = (index + 1), accumulator, iterator.next())
    end while
    return accumulator
end function

function reduceOrNull_rIterableAnyN_Function2AnyNAnyNAnyN_AnyN_k_(m as Object, operation as Function) as Dynamic
    iterator = m.iterator()
    if iterator.hasNext().not() then
        return invalid
    end if
    accumulator = iterator.next()
    while iterator.hasNext()
        accumulator = operation.invoke(accumulator, iterator.next())
    end while
    return accumulator
end function

function sum_rIterableI_I_k_(m as Object) as Integer
    sum = 0
    for each element in m
        sum = (sum + element)
    end for
    return sum
end function

function sum_rIterableJ_J_k_(m as Object) as LongInteger
    sum = 0&
    for each element in m
        sum = (sum + element)
    end for
    return sum
end function

function sum_rIterableF_F_k_(m as Object) as Float
    sum = 0.0!
    for each element in m
        sum = (sum + element)
    end for
    return sum
end function

function sum_rIterableD_D_k_(m as Object) as Double
    sum = 0.0#
    for each element in m
        sum = (sum + element)
    end for
    return sum
end function

function sumOf_rIterableAnyN_Function1AnyNI_I_k_(m as Object, selector as Function) as Integer
    sum = 0
    for each element in m
        sum = (sum + selector.invoke(element))
    end for
    return sum
end function

function sumOfDouble_rIterableAnyN_Function1AnyND_D_k_(m as Object, selector as Function) as Double
    sum = 0.0#
    for each element in m
        sum = (sum + selector.invoke(element))
    end for
    return sum
end function

function take_rIterableAnyN_I_ListAnyN_k_(m as Object, n as Integer) as Object
    require_Z_Function0Any_k_(n >= 0, {n: n, invoke: function() as Object
        return ("Requested element count " + m.n) + " is less than zero."
    end function})
    if n = 0 then
        return emptyList_ListAnyN_k_()
    end if
    if __kotlin_isInstanceOf(m, "Collection") then
        if n >= m.size then
            return toList_rIterableAnyN_ListAnyN_k_(m)
        end if
    end if
    count = 0
    list = ArrayList_create_ArrayListAnyN_k_()
    for each item in m
        list.add(item)
        count = (count + 1)
        if count = n then
            exit for
        end if

    end for
    return list
end function

function drop_rIterableAnyN_I_ListAnyN_k_(m as Object, n as Integer) as Object
    require_Z_Function0Any_k_(n >= 0, {n: n, invoke: function() as Object
        return ("Requested element count " + m.n) + " is less than zero."
    end function})
    if n = 0 then
        return toList_rIterableAnyN_ListAnyN_k_(m)
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    count = 0
    for each item in m
        if count >= n then
            list.add(item)
        end if
        count = (unary + 1)

    end for
    return list
end function

function takeWhile_rIterableAnyN_Function1AnyNZ_ListAnyN_k_(m as Object, predicate as Function) as Object
    list = ArrayList_create_ArrayListAnyN_k_()
    for each item in m
        if predicate.invoke(item).not() then
            exit for
        end if
        list.add(item)

    end for
    return list
end function

function dropWhile_rIterableAnyN_Function1AnyNZ_ListAnyN_k_(m as Object, predicate as Function) as Object
    yielding = false
    list = ArrayList_create_ArrayListAnyN_k_()
    for each item in m
        if yielding then
            list.add(item)
        else if predicate.invoke(item).not() then
            yielding = true
            list.add(item)
        end if
    end for
    return list
end function

function distinct_rIterableAnyN_ListAnyN_k_(m as Object) as Object
    return toList_rIterableAnyN_ListAnyN_k_(toMutableSet_rIterableAnyN_MutableSetAnyN_k_(m))
end function

function distinctBy_rIterableAnyN_Function1AnyNAnyN_ListAnyN_k_(m as Object, selector as Function) as Object
    set = HashSet_create_HashSetAnyN_k_()
    list = ArrayList_create_ArrayListAnyN_k_()
    for each e in m
        key = selector.invoke(e)
        if set.add(key) then
            list.add(e)
        end if

    end for
    return list
end function

function flatMap_rIterableAnyN_Function1AnyNIterableAnyN_ListAnyN_k_(m as Object, transform as Function) as Object
    result = ArrayList_create_ArrayListAnyN_k_()
    for each element in m
        list = transform.invoke(element)
        result.addAll(toList_rIterableAnyN_ListAnyN_k_(list))

    end for
    return result
end function

function flatten_rIterableIterableAnyN_ListAnyN_k_(m as Object) as Object
    result = ArrayList_create_ArrayListAnyN_k_()
    for each element in m
        result.addAll(toList_rIterableAnyN_ListAnyN_k_(element))

    end for
    return result
end function

function toList_rIterableAnyN_ListAnyN_k_(m as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") then
        __when_tmp3 = invalid
        if tmp0_subject = 0 then
            __when_tmp3 = emptyList_ListAnyN_k_()
        else if tmp0_subject = 1 then
            __when_tmp2 = invalid
            if __kotlin_isInstanceOf(m, "List") then
                __when_tmp2 = m.get(0)
            else if true then
                __when_tmp2 = m.iterator().next()
            end if
            __when_tmp3 = listOf_Arr_ListAnyN_k_([__when_tmp2])
        else if true then
            __when_tmp3 = ArrayList_create_CollectionAnyN_ArrayListAnyN_k_(m)
        end if
        return __when_tmp3

    end if
    return toMutableList_rIterableAnyN_MutableListAnyN_k_(m)
end function

function toMutableList_rIterableAnyN_MutableListAnyN_k_(m as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") then
        return ArrayList_create_CollectionAnyN_ArrayListAnyN_k_(m)
    end if
    result = ArrayList_create_ArrayListAnyN_k_()
    for each element in m
        result.add(element)

    end for
    return result
end function

function toSet_rIterableAnyN_SetAnyN_k_(m as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") then
        __when_tmp4 = invalid
        if tmp0_subject = 0 then
            __when_tmp4 = emptySet_SetAnyN_k_()
        else if true then
            __when_tmp4 = toMutableSet_rIterableAnyN_MutableSetAnyN_k_(m)
        end if
        return __when_tmp4

    end if
    return toMutableSet_rIterableAnyN_MutableSetAnyN_k_(m)
end function

function toMutableSet_rIterableAnyN_MutableSetAnyN_k_(m as Object) as Object
    set = HashSet_create_HashSetAnyN_k_()
    for each item in m
        set.add(item)
    end for
    return set
end function

function joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(m as Object, separator = ", ", prefix = "", postfix = "", limit = -1, truncated = "...") as String
    sb = StringBuilder_create_StringBuilder_k_()
    sb.append(prefix)
    count = 0
    for each element in m
        count = (count + 1)
        if count > 1 then
            sb.append(separator)
        end if
        if (limit < 0) or (count <= limit) then
            sb.append(element.toString())
        else if true then
            exit for
        end if

    end for
    if (limit >= 0) and (count > limit) then
        sb.append(truncated)
    end if
    sb.append(postfix)
    return sb.toString()
end function

function joinToString_77mgo1_k_(m as Object, separator = ", ", prefix = "", postfix = "", limit = -1, truncated = "...", transform = invalid) as String
    sb = StringBuilder_create_StringBuilder_k_()
    sb.append(prefix)
    count = 0
    for each element in m
        count = (count + 1)
        if count > 1 then
            sb.append(separator)
        end if
        if (limit < 0) or (count <= limit) then
            sb.append(transform.invoke(element))
        else if true then
            exit for
        end if

    end for
    if (limit >= 0) and (count > limit) then
        sb.append(truncated)
    end if
    sb.append(postfix)
    return sb.toString()
end function

function contains_rIterableAnyN_AnyN_Z_k_(m as Object, element as Dynamic) as Boolean
    if __kotlin_isInstanceOf(m, "Collection") then
        return m.contains(element)
    end if
    return indexOf_rIterableAnyN_AnyN_I_k_(m, element) >= 0
end function

function indexOf_rIterableAnyN_AnyN_I_k_(m as Object, element as Dynamic) as Integer
    if __kotlin_isInstanceOf(m, "List") then
        return m.indexOf(element)
    end if
    index = 0
    for each item in m
        if element = item then
            return index
        end if
        index = (unary + 1)

    end for
    return -1
end function

function indexOfFirst_rIterableAnyN_Function1AnyNZ_I_k_(m as Object, predicate as Function) as Integer
    index = 0
    for each item in m
        if predicate.invoke(item) then
            return index
        end if
        index = (unary + 1)

    end for
    return -1
end function

function indexOfLast_rIterableAnyN_Function1AnyNZ_I_k_(m as Object, predicate as Function) as Integer
    lastIndex = -1
    index = 0
    for each item in m
        if predicate.invoke(item) then
            lastIndex = index
        end if
        index = (unary + 1)

    end for
    return lastIndex
end function

function single_rIterableAnyN_AnyN_k_(m as Object) as Dynamic
    tmp0_subject = m
    if __kotlin_isInstanceOf(tmp0_subject, "List") then
        __when_tmp5 = invalid
        if tmp1_subject = 0 then
            throw NoSuchElementException_create_StrN_NoSuchElementException_k_("List is empty.")
        else if tmp1_subject = 1 then
            __when_tmp5 = m[0]
        else if true then
            throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("List has more than one element.")
        end if
        return __when_tmp5

    else if true then
        iterator = m.iterator()
        if iterator.hasNext().not() then
            throw NoSuchElementException_create_StrN_NoSuchElementException_k_("Collection is empty.")
        end if
        single = iterator.next()
        if iterator.hasNext() then
            throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Collection has more than one element.")
        end if
        return single
    end if

end function

function single_rIterableAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Function) as Dynamic
    single = invalid
    found = false
    for each element in m
        if predicate.invoke(element) then
            if found then
                throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Collection contains more than one matching element.")
            end if
            single = element
            found = true
        end if
    end for
    if found.not() then
        throw NoSuchElementException_create_StrN_NoSuchElementException_k_("Collection contains no element matching the predicate.")
    end if
    return single
end function

function singleOrNull_rIterableAnyN_AnyN_k_(m as Object) as Dynamic
    tmp0_subject = m
    if __kotlin_isInstanceOf(tmp0_subject, "List") then
        __when_tmp6 = invalid
        if m.size = 1 then
            __when_tmp6 = m[0]
        else if true then
            __when_tmp6 = invalid
        end if
        return __when_tmp6

    else if true then
        iterator = m.iterator()
        if iterator.hasNext().not() then
            return invalid
        end if
        single = iterator.next()
        if iterator.hasNext() then
            return invalid
        end if
        return single
    end if

end function

function singleOrNull_rIterableAnyN_Function1AnyNZ_AnyN_k_(m as Object, predicate as Function) as Dynamic
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
    if found.not() then
        return invalid
    end if
    return single
end function

function partition_rIterableAnyN_Function1AnyNZ_PairListAnyNListAnyN_k_(m as Object, predicate as Function) as Object
    first = ArrayList_create_ArrayListAnyN_k_()
    second = ArrayList_create_ArrayListAnyN_k_()
    for each element in m
        if predicate.invoke(element) then
            first.add(element)
        else if true then
            second.add(element)
        end if
    end for
    return Pair_create_AnyN_AnyN_PairAnyNAnyN_k_(first, second)
end function

function zip_rIterableAnyN_IterableAnyN_ListPairAnyNAnyN_k_(m as Object, other as Object) as Object
    first = m.iterator()
    second = other.iterator()
    result = ArrayList_create_ArrayListAnyN_k_()
    while first.hasNext() and second.hasNext()
        result.add(Pair_create_AnyN_AnyN_PairAnyNAnyN_k_(first.next(), second.next()))
    end while
    return result
end function

function zip_rIterableAnyN_IterableAnyN_Function2AnyNAnyNAnyN_ListAnyN_k_(m as Object, other as Object, transform as Function) as Object
    first = m.iterator()
    second = other.iterator()
    result = ArrayList_create_ArrayListAnyN_k_()
    while first.hasNext() and second.hasNext()
        result.add(transform.invoke(first.next(), second.next()))
    end while
    return result
end function

function plus_rCollectionAnyN_AnyN_ListAnyN_k_(m as Object, element as Dynamic) as Object
    result = ArrayList_create_I_ArrayListAnyN_k_(m.size + 1)
    result.addAll(m)
    result.add(element)
    return result
end function

function plus_rCollectionAnyN_IterableAnyN_ListAnyN_k_(m as Object, elements as Object) as Object
    result = ArrayList_create_ArrayListAnyN_k_()
    result.addAll(m)
    result.addAll(toList_rIterableAnyN_ListAnyN_k_(elements))
    return result
end function

function minus_rIterableAnyN_AnyN_ListAnyN_k_(m as Object, element as Dynamic) as Object
    result = ArrayList_create_ArrayListAnyN_k_()
    removed = false
    for each item in m
        if removed.not() and (item = element) then
            removed = true
        else if true then
            result.add(item)
        end if
    end for
    return result
end function

function minus_rIterableAnyN_IterableAnyN_ListAnyN_k_(m as Object, elements as Object) as Object
    other = toSet_rIterableAnyN_SetAnyN_k_(elements)
    return filterNot_rIterableAnyN_Function1AnyNZ_ListAnyN_k_(m, {other: other, invoke: function(it as Dynamic) as Boolean
        return m.other.contains(it)
    end function})
end function
