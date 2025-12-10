sub forEach_rIterable_Function1V_k_(m as Object, action as Object)
    __iter_47 = m.iterator_k_()
    while __iter_47.hasNext_k_()
        element = __iter_47.next_k_()
        action.invoke(element)
    end while

end sub

sub forEachIndexed_rIterable_Function2IV_k_(m as Object, action as Object)
    index = 0
    __iter_49 = m.iterator_k_()
    while __iter_49.hasNext_k_()
        item = __iter_49.next_k_()
        __incr_tmp_48 = index
        index = (__incr_tmp_48 + 1)

        action.invoke(__incr_tmp_48, item)

    end while

end sub

function map_rIterable_Function1_k_(m as Object, transform as Object) as Object
    result = ArrayList_create_k_()
    __iter_50 = m.iterator_k_()
    while __iter_50.hasNext_k_()
        item = __iter_50.next_k_()
        result.add_AnyN_k_(transform.invoke(item))

    end while

    return result
end function

function mapIndexed_rIterable_Function2I_k_(m as Object, transform as Object) as Object
    result = ArrayList_create_k_()
    index = 0
    __iter_52 = m.iterator_k_()
    while __iter_52.hasNext_k_()
        item = __iter_52.next_k_()
        __incr_tmp_51 = index
        index = (__incr_tmp_51 + 1)

        result.add_AnyN_k_(transform.invoke(__incr_tmp_51, item))

    end while

    return result
end function

function mapNotNull_rIterable_Function1_k_(m as Object, transform as Object) as Object
    result = ArrayList_create_k_()
    __iter_53 = m.iterator_k_()
    while __iter_53.hasNext_k_()
        item = __iter_53.next_k_()
        transformed = transform.invoke(item)
        if transformed <> invalid then
            result.add_AnyN_k_(transformed)
        end if

    end while

    return result
end function

function filter_rIterable_Function1Z_k_(m as Object, predicate as Object) as Object
    result = ArrayList_create_k_()
    __iter_54 = m.iterator_k_()
    while __iter_54.hasNext_k_()
        item = __iter_54.next_k_()
        if predicate.invoke(item) then
            result.add_AnyN_k_(item)
        end if
    end while

    return result
end function

function filterIndexed_rIterable_Function2IZ_k_(m as Object, predicate as Object) as Object
    result = ArrayList_create_k_()
    index = 0
    __iter_55 = m.iterator_k_()
    while __iter_55.hasNext_k_()
        item = __iter_55.next_k_()
        unary = index
        index = (unary + 1)
        if predicate.invoke(unary, item) then
            result.add_AnyN_k_(item)
        end if

    end while

    return result
end function

function filterNotNull_rIterable_k_(m as Object) as Object
    result = ArrayList_create_k_()
    __iter_56 = m.iterator_k_()
    while __iter_56.hasNext_k_()
        item = __iter_56.next_k_()
        if item <> invalid then
            result.add_AnyN_k_(item)
        end if
    end while

    return result
end function

function filterNot_rIterable_Function1Z_k_(m as Object, predicate as Object) as Object
    result = ArrayList_create_k_()
    __iter_57 = m.iterator_k_()
    while __iter_57.hasNext_k_()
        item = __iter_57.next_k_()
        if not predicate.invoke(item) then
            result.add_AnyN_k_(item)
        end if
    end while

    return result
end function

function find_rIterable_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    __iter_58 = m.iterator_k_()
    while __iter_58.hasNext_k_()
        element = __iter_58.next_k_()
        if predicate.invoke(element) then
            return element
        end if
    end while

    return invalid
end function

function findLast_rIterable_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    last = invalid
    __iter_59 = m.iterator_k_()
    while __iter_59.hasNext_k_()
        element = __iter_59.next_k_()
        if predicate.invoke(element) then
            last = element
        end if
    end while

    return last
end function

function first_rIterable_k_(m as Object) as Dynamic
    tmp0_subject = m
    if __kotlin_isInstanceOf(tmp0_subject, "List") then
        if m.isEmpty_k_() then
            throw NoSuchElementException_create_StrN_k_("List is empty.")
        end if
        return m.get_I_k_(0)
    else if true then
        iterator = m.iterator_k_()
        if not iterator.hasNext_k_() then
            throw NoSuchElementException_create_StrN_k_("Collection is empty.")
        end if
        return iterator.next_k_()
    end if

end function

function first_rIterable_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    __iter_60 = m.iterator_k_()
    while __iter_60.hasNext_k_()
        element = __iter_60.next_k_()
        if predicate.invoke(element) then
            return element
        end if
    end while

    throw NoSuchElementException_create_StrN_k_("Collection contains no element matching the predicate.")
end function

function firstOrNull_rIterable_k_(m as Object) as Dynamic
    tmp0_subject = m
    if __kotlin_isInstanceOf(tmp0_subject, "List") then
        __when_tmp0 = invalid
        if m.isEmpty_k_() then
            __when_tmp0 = invalid
        else if true then
            __when_tmp0 = m.get_I_k_(0)
        end if
        return __when_tmp0
    else if true then
        iterator = m.iterator_k_()
        if not iterator.hasNext_k_() then
            return invalid
        end if
        return iterator.next_k_()
    end if

end function

function firstOrNull_rIterable_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    __iter_61 = m.iterator_k_()
    while __iter_61.hasNext_k_()
        element = __iter_61.next_k_()
        if predicate.invoke(element) then
            return element
        end if
    end while

    return invalid
end function

function last_rIterable_k_(m as Object) as Dynamic
    tmp0_subject = m
    if __kotlin_isInstanceOf(tmp0_subject, "List") then
        if m.isEmpty_k_() then
            throw NoSuchElementException_create_StrN_k_("List is empty.")
        end if
        return m.get_I_k_(m.get_size() - 1)
    else if true then
        iterator = m.iterator_k_()
        if not iterator.hasNext_k_() then
            throw NoSuchElementException_create_StrN_k_("Collection is empty.")
        end if
        last = iterator.next_k_()
        while iterator.hasNext_k_()
            last = iterator.next_k_()
        end while
        return last
    end if

end function

function last_rIterable_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    last = invalid
    found = false
    __iter_62 = m.iterator_k_()
    while __iter_62.hasNext_k_()
        element = __iter_62.next_k_()
        if predicate.invoke(element) then
            last = element
            found = true
        end if
    end while

    if not found then
        throw NoSuchElementException_create_StrN_k_("Collection contains no element matching the predicate.")
    end if
    return last
end function

function lastOrNull_rIterable_k_(m as Object) as Dynamic
    tmp0_subject = m
    if __kotlin_isInstanceOf(tmp0_subject, "List") then
        __when_tmp1 = invalid
        if m.isEmpty_k_() then
            __when_tmp1 = invalid
        else if true then
            __when_tmp1 = m.get_I_k_(m.get_size() - 1)
        end if
        return __when_tmp1
    else if true then
        iterator = m.iterator_k_()
        if not iterator.hasNext_k_() then
            return invalid
        end if
        last = iterator.next_k_()
        while iterator.hasNext_k_()
            last = iterator.next_k_()
        end while
        return last
    end if

end function

function lastOrNull_rIterable_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    last = invalid
    __iter_63 = m.iterator_k_()
    while __iter_63.hasNext_k_()
        element = __iter_63.next_k_()
        if predicate.invoke(element) then
            last = element
        end if
    end while

    return last
end function

function any_rIterable_k_(m as Object) as Boolean
    if __kotlin_isInstanceOf(m, "Collection") then
        return not m.isEmpty_k_()
    end if
    return m.iterator_k_().hasNext_k_()
end function

function any_rIterable_Function1Z_k_(m as Object, predicate as Object) as Boolean
    if __kotlin_isInstanceOf(m, "Collection") and m.isEmpty_k_() then
        return false
    end if
    __iter_64 = m.iterator_k_()
    while __iter_64.hasNext_k_()
        element = __iter_64.next_k_()
        if predicate.invoke(element) then
            return true
        end if
    end while

    return false
end function

function all_rIterable_Function1Z_k_(m as Object, predicate as Object) as Boolean
    if __kotlin_isInstanceOf(m, "Collection") and m.isEmpty_k_() then
        return true
    end if
    __iter_65 = m.iterator_k_()
    while __iter_65.hasNext_k_()
        element = __iter_65.next_k_()
        if not predicate.invoke(element) then
            return false
        end if
    end while

    return true
end function

function none_rIterable_k_(m as Object) as Boolean
    if __kotlin_isInstanceOf(m, "Collection") then
        return m.isEmpty_k_()
    end if
    return not m.iterator_k_().hasNext_k_()
end function

function none_rIterable_Function1Z_k_(m as Object, predicate as Object) as Boolean
    if __kotlin_isInstanceOf(m, "Collection") and m.isEmpty_k_() then
        return true
    end if
    __iter_66 = m.iterator_k_()
    while __iter_66.hasNext_k_()
        element = __iter_66.next_k_()
        if predicate.invoke(element) then
            return false
        end if
    end while

    return true
end function

function count_rIterable_k_(m as Object) as Integer
    if __kotlin_isInstanceOf(m, "Collection") then
        return m.get_size()
    end if
    count = 0
    __iter_67 = m.iterator_k_()
    while __iter_67.hasNext_k_()
        element = __iter_67.next_k_()
        count = (count + 1)
    end while

    return count
end function

function count_rIterable_Function1Z_k_(m as Object, predicate as Object) as Integer
    if __kotlin_isInstanceOf(m, "Collection") and m.isEmpty_k_() then
        return 0
    end if
    count = 0
    __iter_68 = m.iterator_k_()
    while __iter_68.hasNext_k_()
        element = __iter_68.next_k_()
        if predicate.invoke(element) then
            count = (count + 1)
        end if
    end while

    return count
end function

function fold_rIterable_AnyN_Function2_k_(m as Object, initial as Dynamic, operation as Object) as Dynamic
    accumulator = initial
    __iter_69 = m.iterator_k_()
    while __iter_69.hasNext_k_()
        element = __iter_69.next_k_()
        accumulator = operation.invoke(accumulator, element)
    end while

    return accumulator
end function

function foldIndexed_rIterable_AnyN_Function3I_k_(m as Object, initial as Dynamic, operation as Object) as Dynamic
    index = 0
    accumulator = initial
    __iter_71 = m.iterator_k_()
    while __iter_71.hasNext_k_()
        element = __iter_71.next_k_()
        __incr_tmp_70 = index
        index = (__incr_tmp_70 + 1)

        accumulator = operation.invoke(__incr_tmp_70, accumulator, element)

    end while

    return accumulator
end function

function reduce_rIterable_Function2_k_(m as Object, operation as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        throw UnsupportedOperationException_create_StrN_k_("Empty collection can't be reduced.")
    end if
    accumulator = iterator.next_k_()
    while iterator.hasNext_k_()
        accumulator = operation.invoke(accumulator, iterator.next_k_())
    end while
    return accumulator
end function

function reduceIndexed_rIterable_Function3I_k_(m as Object, operation as Object) as Dynamic
    iterator = m.iterator_k_()
    if not iterator.hasNext_k_() then
        throw UnsupportedOperationException_create_StrN_k_("Empty collection can't be reduced.")
    end if
    index = 1
    accumulator = iterator.next_k_()
    while iterator.hasNext_k_()
        __incr_tmp_72 = index
        index = (__incr_tmp_72 + 1)

        accumulator = operation.invoke(__incr_tmp_72, accumulator, iterator.next_k_())
    end while
    return accumulator
end function

function reduceOrNull_rIterable_Function2_k_(m as Object, operation as Object) as Dynamic
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

function sum_rIterableI_k_(m as Object) as Integer
    sum = 0
    __iter_73 = m.iterator_k_()
    while __iter_73.hasNext_k_()
        element = __iter_73.next_k_()
        sum = (sum + element)
    end while

    return sum
end function

function sum_rIterableJ_k_(m as Object) as LongInteger
    sum = 0&
    __iter_74 = m.iterator_k_()
    while __iter_74.hasNext_k_()
        element = __iter_74.next_k_()
        sum = (sum + element)
    end while

    return sum
end function

function sum_rIterableF_k_(m as Object) as Float
    sum = 0.0!
    __iter_75 = m.iterator_k_()
    while __iter_75.hasNext_k_()
        element = __iter_75.next_k_()
        sum = (sum + element)
    end while

    return sum
end function

function sum_rIterableD_k_(m as Object) as Double
    sum = 0.0#
    __iter_76 = m.iterator_k_()
    while __iter_76.hasNext_k_()
        element = __iter_76.next_k_()
        sum = (sum + element)
    end while

    return sum
end function

function sumOf_rIterable_Function1I_k_(m as Object, selector as Object) as Integer
    sum = 0
    __iter_77 = m.iterator_k_()
    while __iter_77.hasNext_k_()
        element = __iter_77.next_k_()
        sum = (sum + selector.invoke(element))
    end while

    return sum
end function

function sumOfDouble_rIterable_Function1D_k_(m as Object, selector as Object) as Double
    sum = 0.0#
    __iter_78 = m.iterator_k_()
    while __iter_78.hasNext_k_()
        element = __iter_78.next_k_()
        sum = (sum + selector.invoke(element))
    end while

    return sum
end function

function take_rIterable_I_k_(m as Object, n as Integer) as Object
    require_Z_Function0Any_k_(n >= 0, {n: n, invoke: function() as Object
        return ("Requested element count " + m.n) + " is less than zero."
    end function})
    if n = 0 then
        return emptyList_k_()
    end if
    if __kotlin_isInstanceOf(m, "Collection") then
        if n >= m.get_size() then
            return toList_rIterable_k_(m)
        end if
    end if
    count = 0
    list = ArrayList_create_k_()
    __iter_79 = m.iterator_k_()
    while __iter_79.hasNext_k_()
        item = __iter_79.next_k_()
        list.add_AnyN_k_(item)
        count = (count + 1)
        if count = n then
            exit while
        end if

    end while

    return list
end function

function drop_rIterable_I_k_(m as Object, n as Integer) as Object
    require_Z_Function0Any_k_(n >= 0, {n: n, invoke: function() as Object
        return ("Requested element count " + m.n) + " is less than zero."
    end function})
    if n = 0 then
        return toList_rIterable_k_(m)
    end if
    list = ArrayList_create_k_()
    count = 0
    __iter_80 = m.iterator_k_()
    while __iter_80.hasNext_k_()
        item = __iter_80.next_k_()
        if count >= n then
            list.add_AnyN_k_(item)
        end if
        count = (count + 1)

    end while

    return list
end function

function takeWhile_rIterable_Function1Z_k_(m as Object, predicate as Object) as Object
    list = ArrayList_create_k_()
    __iter_81 = m.iterator_k_()
    while __iter_81.hasNext_k_()
        item = __iter_81.next_k_()
        if not predicate.invoke(item) then
            exit while
        end if
        list.add_AnyN_k_(item)

    end while

    return list
end function

function dropWhile_rIterable_Function1Z_k_(m as Object, predicate as Object) as Object
    yielding = false
    list = ArrayList_create_k_()
    __iter_82 = m.iterator_k_()
    while __iter_82.hasNext_k_()
        item = __iter_82.next_k_()
        if yielding then
            list.add_AnyN_k_(item)
        else if not predicate.invoke(item) then
            yielding = true
            list.add_AnyN_k_(item)
        end if
    end while

    return list
end function

function distinct_rIterable_k_(m as Object) as Object
    return toList_rIterable_k_(toMutableSet_rIterable_k_(m))
end function

function distinctBy_rIterable_Function1_k_(m as Object, selector as Object) as Object
    set = HashSet_create_k_()
    list = ArrayList_create_k_()
    __iter_83 = m.iterator_k_()
    while __iter_83.hasNext_k_()
        e = __iter_83.next_k_()
        key = selector.invoke(e)
        if set.add_AnyN_k_(key) then
            list.add_AnyN_k_(e)
        end if

    end while

    return list
end function

function flatMap_rIterable_Function1Iterable_k_(m as Object, transform as Object) as Object
    result = ArrayList_create_k_()
    __iter_84 = m.iterator_k_()
    while __iter_84.hasNext_k_()
        element = __iter_84.next_k_()
        list = transform.invoke(element)
        result.addAll_Collection_k_(toList_rIterable_k_(list))

    end while

    return result
end function

function flatten_rIterableIterable_k_(m as Object) as Object
    result = ArrayList_create_k_()
    __iter_85 = m.iterator_k_()
    while __iter_85.hasNext_k_()
        element = __iter_85.next_k_()
        result.addAll_Collection_k_(toList_rIterable_k_(element))

    end while

    return result
end function

function toList_rIterable_k_(m as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") then
        tmp0_subject = m.get_size()
        __when_tmp3 = invalid
        if tmp0_subject = 0 then
            __when_tmp3 = emptyList_k_()
        else if tmp0_subject = 1 then
            __when_tmp2 = invalid
            if __kotlin_isInstanceOf(m, "List") then
                __when_tmp2 = m.get_I_k_(0)
            else if true then
                __when_tmp2 = m.iterator_k_().next_k_()
            end if
            __when_tmp3 = listOf_Arr_k_([__when_tmp2])
        else if true then
            __when_tmp3 = ArrayList_create_Collection_k_(m)
        end if
        return __when_tmp3

    end if
    return toMutableList_rIterable_k_(m)
end function

function toMutableList_rIterable_k_(m as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") then
        return ArrayList_create_Collection_k_(m)
    end if
    result = ArrayList_create_k_()
    __iter_86 = m.iterator_k_()
    while __iter_86.hasNext_k_()
        element = __iter_86.next_k_()
        result.add_AnyN_k_(element)

    end while

    return result
end function

function toSet_rIterable_k_(m as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") then
        tmp0_subject = m.get_size()
        __when_tmp4 = invalid
        if tmp0_subject = 0 then
            __when_tmp4 = emptySet_k_()
        else if true then
            __when_tmp4 = toMutableSet_rIterable_k_(m)
        end if
        return __when_tmp4

    end if
    return toMutableSet_rIterable_k_(m)
end function

function toMutableSet_rIterable_k_(m as Object) as Object
    set = HashSet_create_k_()
    __iter_87 = m.iterator_k_()
    while __iter_87.hasNext_k_()
        item = __iter_87.next_k_()
        set.add_AnyN_k_(item)
    end while

    return set
end function

function joinToString_rIterable_CharSequence_CharSequence_CharSequence_I_CharSequence_k_(m as Object, separator = ", ", prefix = "", postfix = "", limit = -1, truncated = "...") as String
    sb = StringBuilder_create_k_()
    sb.append_CharSequenceN_k_(prefix)
    count = 0
    __iter_88 = m.iterator_k_()
    while __iter_88.hasNext_k_()
        element = __iter_88.next_k_()
        count = (count + 1)
        if count > 1 then
            sb.append_CharSequenceN_k_(separator)
        end if
        if (limit < 0) or (count <= limit) then
            sb.append_StrN_k_(toString_AnyN_k_(element))
        else if true then
            exit while
        end if

    end while

    if (limit >= 0) and (count > limit) then
        sb.append_CharSequenceN_k_(truncated)
    end if
    sb.append_CharSequenceN_k_(postfix)
    return sb.toString()
end function

function joinToString_v4kj63_k_(m as Object, separator = ", ", prefix = "", postfix = "", limit = -1, truncated = "...", transform = invalid) as String
    sb = StringBuilder_create_k_()
    sb.append_CharSequenceN_k_(prefix)
    count = 0
    __iter_89 = m.iterator_k_()
    while __iter_89.hasNext_k_()
        element = __iter_89.next_k_()
        count = (count + 1)
        if count > 1 then
            sb.append_CharSequenceN_k_(separator)
        end if
        if (limit < 0) or (count <= limit) then
            sb.append_CharSequenceN_k_(transform.invoke(element))
        else if true then
            exit while
        end if

    end while

    if (limit >= 0) and (count > limit) then
        sb.append_CharSequenceN_k_(truncated)
    end if
    sb.append_CharSequenceN_k_(postfix)
    return sb.toString()
end function

function contains_rIterable_AnyN_k_(m as Object, element as Dynamic) as Boolean
    if __kotlin_isInstanceOf(m, "Collection") then
        return m.contains_AnyN_k_(element)
    end if
    return indexOf_rIterable_AnyN_k_(m, element) >= 0
end function

function indexOf_rIterable_AnyN_k_(m as Object, element as Dynamic) as Integer
    if __kotlin_isInstanceOf(m, "List") then
        return m.indexOf_AnyN_k_(element)
    end if
    index = 0
    __iter_90 = m.iterator_k_()
    while __iter_90.hasNext_k_()
        item = __iter_90.next_k_()
        if element = item then
            return index
        end if
        index = (index + 1)

    end while

    return -1
end function

function indexOfFirst_rIterable_Function1Z_k_(m as Object, predicate as Object) as Integer
    index = 0
    __iter_91 = m.iterator_k_()
    while __iter_91.hasNext_k_()
        item = __iter_91.next_k_()
        if predicate.invoke(item) then
            return index
        end if
        index = (index + 1)

    end while

    return -1
end function

function indexOfLast_rIterable_Function1Z_k_(m as Object, predicate as Object) as Integer
    lastIndex = -1
    index = 0
    __iter_92 = m.iterator_k_()
    while __iter_92.hasNext_k_()
        item = __iter_92.next_k_()
        if predicate.invoke(item) then
            lastIndex = index
        end if
        index = (index + 1)

    end while

    return lastIndex
end function

function single_rIterable_k_(m as Object) as Dynamic
    tmp0_subject = m
    if __kotlin_isInstanceOf(tmp0_subject, "List") then
        tmp1_subject = m.get_size()
        __when_tmp5 = invalid
        if tmp1_subject = 0 then
            throw NoSuchElementException_create_StrN_k_("List is empty.")
        else if tmp1_subject = 1 then
            __when_tmp5 = m.get_I_k_(0)
        else if true then
            throw IllegalArgumentException_create_StrN_k_("List has more than one element.")
        end if
        return __when_tmp5

    else if true then
        iterator = m.iterator_k_()
        if not iterator.hasNext_k_() then
            throw NoSuchElementException_create_StrN_k_("Collection is empty.")
        end if
        single = iterator.next_k_()
        if iterator.hasNext_k_() then
            throw IllegalArgumentException_create_StrN_k_("Collection has more than one element.")
        end if
        return single
    end if

end function

function single_rIterable_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    single = invalid
    found = false
    __iter_93 = m.iterator_k_()
    while __iter_93.hasNext_k_()
        element = __iter_93.next_k_()
        if predicate.invoke(element) then
            if found then
                throw IllegalArgumentException_create_StrN_k_("Collection contains more than one matching element.")
            end if
            single = element
            found = true
        end if
    end while

    if not found then
        throw NoSuchElementException_create_StrN_k_("Collection contains no element matching the predicate.")
    end if
    return single
end function

function singleOrNull_rIterable_k_(m as Object) as Dynamic
    tmp0_subject = m
    if __kotlin_isInstanceOf(tmp0_subject, "List") then
        __when_tmp6 = invalid
        if m.get_size() = 1 then
            __when_tmp6 = m.get_I_k_(0)
        else if true then
            __when_tmp6 = invalid
        end if
        return __when_tmp6

    else if true then
        iterator = m.iterator_k_()
        if not iterator.hasNext_k_() then
            return invalid
        end if
        single = iterator.next_k_()
        if iterator.hasNext_k_() then
            return invalid
        end if
        return single
    end if

end function

function singleOrNull_rIterable_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    single = invalid
    found = false
    __iter_94 = m.iterator_k_()
    while __iter_94.hasNext_k_()
        element = __iter_94.next_k_()
        if predicate.invoke(element) then
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

function partition_rIterable_Function1Z_k_(m as Object, predicate as Object) as Object
    first = ArrayList_create_k_()
    second = ArrayList_create_k_()
    __iter_95 = m.iterator_k_()
    while __iter_95.hasNext_k_()
        element = __iter_95.next_k_()
        if predicate.invoke(element) then
            first.add_AnyN_k_(element)
        else if true then
            second.add_AnyN_k_(element)
        end if
    end while

    return Pair_create_AnyN_AnyN_k_(first, second)
end function

function zip_rIterable_Iterable_k_(m as Object, other as Object) as Object
    first = m.iterator_k_()
    second = other.iterator_k_()
    result = ArrayList_create_k_()
    while first.hasNext_k_() and second.hasNext_k_()
        result.add_AnyN_k_(Pair_create_AnyN_AnyN_k_(first.next_k_(), second.next_k_()))
    end while
    return result
end function

function zip_rIterable_Iterable_Function2_k_(m as Object, other as Object, transform as Object) as Object
    first = m.iterator_k_()
    second = other.iterator_k_()
    result = ArrayList_create_k_()
    while first.hasNext_k_() and second.hasNext_k_()
        result.add_AnyN_k_(transform.invoke(first.next_k_(), second.next_k_()))
    end while
    return result
end function

function plus_rCollection_AnyN_k_(m as Object, element as Dynamic) as Object
    result = ArrayList_create_I_k_(m.get_size() + 1)
    result.addAll_Collection_k_(m)
    result.add_AnyN_k_(element)
    return result
end function

function plus_rCollection_Iterable_k_(m as Object, elements as Object) as Object
    result = ArrayList_create_k_()
    result.addAll_Collection_k_(m)
    result.addAll_Collection_k_(toList_rIterable_k_(elements))
    return result
end function

function minus_rIterable_AnyN_k_(m as Object, element as Dynamic) as Object
    result = ArrayList_create_k_()
    removed = false
    __iter_96 = m.iterator_k_()
    while __iter_96.hasNext_k_()
        item = __iter_96.next_k_()
        if not removed and (item = element) then
            removed = true
        else if true then
            result.add_AnyN_k_(item)
        end if
    end while

    return result
end function

function minus_rIterable_Iterable_k_(m as Object, elements as Object) as Object
    other = toSet_rIterable_k_(elements)
    return filterNot_rIterable_Function1Z_k_(m, {other: other, invoke: function(it as Dynamic) as Boolean
        return m.other.contains_AnyN_k_(it)
    end function})
end function
