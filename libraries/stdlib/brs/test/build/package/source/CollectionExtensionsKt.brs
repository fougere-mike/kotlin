sub forEach_rIterable_Function1V_k_(m as Object, action as Object)
    __iter_116 = m.iterator_k_()
    while __iter_116.hasNext_k_()
        element = __iter_116.next_k_()
        action.invoke_AnyN_k_(element)
    end while

end sub

sub forEachIndexed_rIterable_Function2IV_k_(m as Object, action as Object)
    index = 0
    __iter_118 = m.iterator_k_()
    while __iter_118.hasNext_k_()
        item = __iter_118.next_k_()
        __incr_tmp_117 = index
        index = (__incr_tmp_117 + 1)

        action.invoke_AnyN_AnyN_k_(__incr_tmp_117, item)

    end while

end sub

function map_rIterable_Function1_k_(m as Object, transform as Object) as Object
    result = ArrayList_create_k_()
    __iter_119 = m.iterator_k_()
    while __iter_119.hasNext_k_()
        item = __iter_119.next_k_()
        result.add_AnyN_k_(transform.invoke_AnyN_k_(item))

    end while

    return result
end function

function mapIndexed_rIterable_Function2I_k_(m as Object, transform as Object) as Object
    result = ArrayList_create_k_()
    index = 0
    __iter_121 = m.iterator_k_()
    while __iter_121.hasNext_k_()
        item = __iter_121.next_k_()
        __incr_tmp_120 = index
        index = (__incr_tmp_120 + 1)

        result.add_AnyN_k_(transform.invoke_AnyN_AnyN_k_(__incr_tmp_120, item))


    end while

    return result
end function

function mapNotNull_rIterable_Function1_k_(m as Object, transform as Object) as Object
    result = ArrayList_create_k_()
    __iter_122 = m.iterator_k_()
    while __iter_122.hasNext_k_()
        item = __iter_122.next_k_()
        transformed = transform.invoke_AnyN_k_(item)
        if transformed <> invalid then
            result.add_AnyN_k_(transformed)
        end if

    end while

    return result
end function

function filter_rIterable_Function1Z_k_(m as Object, predicate as Object) as Object
    result = ArrayList_create_k_()
    __iter_123 = m.iterator_k_()
    while __iter_123.hasNext_k_()
        item = __iter_123.next_k_()
        if predicate.invoke_AnyN_k_(item) then
            result.add_AnyN_k_(item)
        end if
    end while

    return result
end function

function filterIndexed_rIterable_Function2IZ_k_(m as Object, predicate as Object) as Object
    result = ArrayList_create_k_()
    index = 0
    __iter_124 = m.iterator_k_()
    while __iter_124.hasNext_k_()
        item = __iter_124.next_k_()
        unary = index
        index = (unary + 1)
        if predicate.invoke_AnyN_AnyN_k_(unary, item) then
            result.add_AnyN_k_(item)
        end if

    end while

    return result
end function

function filterNotNull_rIterable_k_(m as Object) as Object
    result = ArrayList_create_k_()
    __iter_125 = m.iterator_k_()
    while __iter_125.hasNext_k_()
        item = __iter_125.next_k_()
        if item <> invalid then
            result.add_AnyN_k_(item)
        end if
    end while

    return result
end function

function filterNot_rIterable_Function1Z_k_(m as Object, predicate as Object) as Object
    result = ArrayList_create_k_()
    __iter_126 = m.iterator_k_()
    while __iter_126.hasNext_k_()
        item = __iter_126.next_k_()
        if not predicate.invoke_AnyN_k_(item) then
            result.add_AnyN_k_(item)
        end if
    end while

    return result
end function

function find_rIterable_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    __iter_127 = m.iterator_k_()
    while __iter_127.hasNext_k_()
        element = __iter_127.next_k_()
        if predicate.invoke_AnyN_k_(element) then
            return element
        end if
    end while

    return invalid
end function

function findLast_rIterable_Function1Z_k_(m as Object, predicate as Object) as Dynamic
    last = invalid
    __iter_128 = m.iterator_k_()
    while __iter_128.hasNext_k_()
        element = __iter_128.next_k_()
        if predicate.invoke_AnyN_k_(element) then
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
    __iter_129 = m.iterator_k_()
    while __iter_129.hasNext_k_()
        element = __iter_129.next_k_()
        if predicate.invoke_AnyN_k_(element) then
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
    __iter_130 = m.iterator_k_()
    while __iter_130.hasNext_k_()
        element = __iter_130.next_k_()
        if predicate.invoke_AnyN_k_(element) then
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
        return m.get_I_k_(m.__get_size() - 1)
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
    __iter_131 = m.iterator_k_()
    while __iter_131.hasNext_k_()
        element = __iter_131.next_k_()
        if predicate.invoke_AnyN_k_(element) then
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
            __when_tmp1 = m.get_I_k_(m.__get_size() - 1)
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
    __iter_132 = m.iterator_k_()
    while __iter_132.hasNext_k_()
        element = __iter_132.next_k_()
        if predicate.invoke_AnyN_k_(element) then
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
    __iter_133 = m.iterator_k_()
    while __iter_133.hasNext_k_()
        element = __iter_133.next_k_()
        if predicate.invoke_AnyN_k_(element) then
            return true
        end if
    end while

    return false
end function

function all_rIterable_Function1Z_k_(m as Object, predicate as Object) as Boolean
    if __kotlin_isInstanceOf(m, "Collection") and m.isEmpty_k_() then
        return true
    end if
    __iter_134 = m.iterator_k_()
    while __iter_134.hasNext_k_()
        element = __iter_134.next_k_()
        if not predicate.invoke_AnyN_k_(element) then
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
    __iter_135 = m.iterator_k_()
    while __iter_135.hasNext_k_()
        element = __iter_135.next_k_()
        if predicate.invoke_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function count_rIterable_k_(m as Object) as Integer
    if __kotlin_isInstanceOf(m, "Collection") then
        return m.__get_size()
    end if
    count = 0
    __iter_136 = m.iterator_k_()
    while __iter_136.hasNext_k_()
        element = __iter_136.next_k_()
        count = (count + 1)
    end while

    return count
end function

function count_rIterable_Function1Z_k_(m as Object, predicate as Object) as Integer
    if __kotlin_isInstanceOf(m, "Collection") and m.isEmpty_k_() then
        return 0
    end if
    count = 0
    __iter_137 = m.iterator_k_()
    while __iter_137.hasNext_k_()
        element = __iter_137.next_k_()
        if predicate.invoke_AnyN_k_(element) then
            count = (count + 1)
        end if
    end while

    return count
end function

function fold_rIterable_AnyN_Function2_k_(m as Object, initial as Dynamic, operation as Object) as Dynamic
    accumulator = initial
    __iter_138 = m.iterator_k_()
    while __iter_138.hasNext_k_()
        element = __iter_138.next_k_()
        accumulator = operation.invoke_AnyN_AnyN_k_(accumulator, element)
    end while

    return accumulator
end function

function foldIndexed_rIterable_AnyN_Function3I_k_(m as Object, initial as Dynamic, operation as Object) as Dynamic
    index = 0
    accumulator = initial
    __iter_140 = m.iterator_k_()
    while __iter_140.hasNext_k_()
        element = __iter_140.next_k_()
        __incr_tmp_139 = index
        index = (__incr_tmp_139 + 1)

        accumulator = operation.invoke_AnyN_AnyN_AnyN_k_(__incr_tmp_139, accumulator, element)

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
        accumulator = operation.invoke_AnyN_AnyN_k_(accumulator, iterator.next_k_())
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
        __incr_tmp_141 = index
        index = (__incr_tmp_141 + 1)

        accumulator = operation.invoke_AnyN_AnyN_AnyN_k_(__incr_tmp_141, accumulator, iterator.next_k_())
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
        accumulator = operation.invoke_AnyN_AnyN_k_(accumulator, iterator.next_k_())
    end while
    return accumulator
end function

function sum_rIterableI_k_(m as Object) as Integer
    sum = 0
    __iter_142 = m.iterator_k_()
    while __iter_142.hasNext_k_()
        element = __iter_142.next_k_()
        sum = (sum + element)
    end while

    return sum
end function

function sum_rIterableJ_k_(m as Object) as LongInteger
    sum = 0&
    __iter_143 = m.iterator_k_()
    while __iter_143.hasNext_k_()
        element = __iter_143.next_k_()
        sum = (sum + element)
    end while

    return sum
end function

function sum_rIterableF_k_(m as Object) as Float
    sum = 0.0!
    __iter_144 = m.iterator_k_()
    while __iter_144.hasNext_k_()
        element = __iter_144.next_k_()
        sum = (sum + element)
    end while

    return sum
end function

function sum_rIterableD_k_(m as Object) as Double
    sum = 0.0#
    __iter_145 = m.iterator_k_()
    while __iter_145.hasNext_k_()
        element = __iter_145.next_k_()
        sum = (sum + element)
    end while

    return sum
end function

function sumOf_rIterable_Function1I_k_(m as Object, selector as Object) as Integer
    sum = 0
    __iter_146 = m.iterator_k_()
    while __iter_146.hasNext_k_()
        element = __iter_146.next_k_()
        sum = (sum + selector.invoke_AnyN_k_(element))
    end while

    return sum
end function

function sumOfDouble_rIterable_Function1D_k_(m as Object, selector as Object) as Double
    sum = 0.0#
    __iter_147 = m.iterator_k_()
    while __iter_147.hasNext_k_()
        element = __iter_147.next_k_()
        sum = (sum + selector.invoke_AnyN_k_(element))
    end while

    return sum
end function

function take_rIterable_I_k_(m as Object, n as Integer) as Object
    tmp0 = n >= 0
    tmp_ret_1 = invalid
    while true
        value = tmp0
        if not value then
            tmp_ret_0 = invalid
            while true
                tmp_ret_0 = (("Requested element count " + __kotlin_numToStr_I_k_(n)) + " is less than zero.")
                exit while
            end while
            throw IllegalArgumentException_create_StrN_k_(toString_AnyN_k_(tmp_ret_0))
        end if
        tmp_ret_1 = invalid
        exit while
    end while

    if n = 0 then
        return emptyList_k_()
    end if
    if __kotlin_isInstanceOf(m, "Collection") then
        if n >= m.__get_size() then
            return toList_rIterable_k_(m)
        end if
    end if
    count = 0
    list = ArrayList_create_k_()
    __iter_148 = m.iterator_k_()
    while __iter_148.hasNext_k_()
        item = __iter_148.next_k_()
        list.add_AnyN_k_(item)
        count = (count + 1)
        if count = n then
            exit while
        end if

    end while

    return list
end function

function drop_rIterable_I_k_(m as Object, n as Integer) as Object
    tmp0 = n >= 0
    tmp_ret_1 = invalid
    while true
        value = tmp0
        if not value then
            tmp_ret_0 = invalid
            while true
                tmp_ret_0 = (("Requested element count " + __kotlin_numToStr_I_k_(n)) + " is less than zero.")
                exit while
            end while
            throw IllegalArgumentException_create_StrN_k_(toString_AnyN_k_(tmp_ret_0))
        end if
        tmp_ret_1 = invalid
        exit while
    end while

    if n = 0 then
        return toList_rIterable_k_(m)
    end if
    list = ArrayList_create_k_()
    count = 0
    __iter_149 = m.iterator_k_()
    while __iter_149.hasNext_k_()
        item = __iter_149.next_k_()
        if count >= n then
            list.add_AnyN_k_(item)
        end if
        count = (count + 1)

    end while

    return list
end function

function takeWhile_rIterable_Function1Z_k_(m as Object, predicate as Object) as Object
    list = ArrayList_create_k_()
    __iter_150 = m.iterator_k_()
    while __iter_150.hasNext_k_()
        item = __iter_150.next_k_()
        if not predicate.invoke_AnyN_k_(item) then
            exit while
        end if
        list.add_AnyN_k_(item)

    end while

    return list
end function

function dropWhile_rIterable_Function1Z_k_(m as Object, predicate as Object) as Object
    yielding = false
    list = ArrayList_create_k_()
    __iter_151 = m.iterator_k_()
    while __iter_151.hasNext_k_()
        item = __iter_151.next_k_()
        if yielding then
            list.add_AnyN_k_(item)
        else if not predicate.invoke_AnyN_k_(item) then
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
    __iter_152 = m.iterator_k_()
    while __iter_152.hasNext_k_()
        e = __iter_152.next_k_()
        key = selector.invoke_AnyN_k_(e)
        if set.add_AnyN_k_(key) then
            list.add_AnyN_k_(e)
        end if

    end while

    return list
end function

function flatMap_rIterable_Function1Iterable_k_(m as Object, transform as Object) as Object
    result = ArrayList_create_k_()
    __iter_153 = m.iterator_k_()
    while __iter_153.hasNext_k_()
        element = __iter_153.next_k_()
        list = transform.invoke_AnyN_k_(element)
        result.addAll_Collection_k_(toList_rIterable_k_(list))

    end while

    return result
end function

function flatten_rIterableIterable_k_(m as Object) as Object
    result = ArrayList_create_k_()
    __iter_154 = m.iterator_k_()
    while __iter_154.hasNext_k_()
        element = __iter_154.next_k_()
        result.addAll_Collection_k_(toList_rIterable_k_(element))

    end while

    return result
end function

function toList_rIterable_k_(m as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") then
        tmp0_subject = m.__get_size()
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
    __iter_155 = m.iterator_k_()
    while __iter_155.hasNext_k_()
        element = __iter_155.next_k_()
        result.add_AnyN_k_(element)

    end while

    return result
end function

function toSet_rIterable_k_(m as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") then
        tmp0_subject = m.__get_size()
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
    __iter_156 = m.iterator_k_()
    while __iter_156.hasNext_k_()
        item = __iter_156.next_k_()
        set.add_AnyN_k_(item)
    end while

    return set
end function

function joinToString_rIterable_CharSequence_CharSequence_CharSequence_I_CharSequence_k_(m as Object, separator = ", ", prefix = "", postfix = "", limit = -1, truncated = "...") as String
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
    sb = StringBuilder_create_k_()
    sb.append_CharSequenceN_k_(prefix)
    count = 0
    __iter_157 = m.iterator_k_()
    while __iter_157.hasNext_k_()
        element = __iter_157.next_k_()
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
    sb = StringBuilder_create_k_()
    sb.append_CharSequenceN_k_(prefix)
    count = 0
    __iter_158 = m.iterator_k_()
    while __iter_158.hasNext_k_()
        element = __iter_158.next_k_()
        count = (count + 1)
        if count > 1 then
            sb.append_CharSequenceN_k_(separator)
        end if
        if (limit < 0) or (count <= limit) then
            sb.append_CharSequenceN_k_(transform.invoke_AnyN_k_(element))
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
    __iter_159 = m.iterator_k_()
    while __iter_159.hasNext_k_()
        item = __iter_159.next_k_()
        if brsStructuralEquals_AnyN_AnyN_k_(element, item) then
            return index
        end if
        index = (index + 1)

    end while

    return -1
end function

function indexOfFirst_rIterable_Function1Z_k_(m as Object, predicate as Object) as Integer
    index = 0
    __iter_160 = m.iterator_k_()
    while __iter_160.hasNext_k_()
        item = __iter_160.next_k_()
        if predicate.invoke_AnyN_k_(item) then
            return index
        end if
        index = (index + 1)

    end while

    return -1
end function

function indexOfLast_rIterable_Function1Z_k_(m as Object, predicate as Object) as Integer
    lastIndex = -1
    index = 0
    __iter_161 = m.iterator_k_()
    while __iter_161.hasNext_k_()
        item = __iter_161.next_k_()
        if predicate.invoke_AnyN_k_(item) then
            lastIndex = index
        end if
        index = (index + 1)

    end while

    return lastIndex
end function

function single_rIterable_k_(m as Object) as Dynamic
    tmp0_subject = m
    if __kotlin_isInstanceOf(tmp0_subject, "List") then
        tmp1_subject = m.__get_size()
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
    __iter_162 = m.iterator_k_()
    while __iter_162.hasNext_k_()
        element = __iter_162.next_k_()
        if predicate.invoke_AnyN_k_(element) then
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
        if m.__get_size() = 1 then
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
    __iter_163 = m.iterator_k_()
    while __iter_163.hasNext_k_()
        element = __iter_163.next_k_()
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

function partition_rIterable_Function1Z_k_(m as Object, predicate as Object) as Object
    first = ArrayList_create_k_()
    second = ArrayList_create_k_()
    __iter_164 = m.iterator_k_()
    while __iter_164.hasNext_k_()
        element = __iter_164.next_k_()
        if predicate.invoke_AnyN_k_(element) then
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
        result.add_AnyN_k_(transform.invoke_AnyN_AnyN_k_(first.next_k_(), second.next_k_()))
    end while
    return result
end function

function plus_rCollection_AnyN_k_(m as Object, element as Dynamic) as Object
    result = ArrayList_create_I_k_(m.__get_size() + 1)
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
    __iter_165 = m.iterator_k_()
    while __iter_165.hasNext_k_()
        item = __iter_165.next_k_()
        if not removed and brsStructuralEquals_AnyN_AnyN_k_(item, element) then
            removed = true
        else if true then
            result.add_AnyN_k_(item)
        end if

    end while

    return result
end function

function minus_rIterable_Iterable_k_(m as Object, elements as Object) as Object
    other = toSet_rIterable_k_(elements)
    tmp0 = m
    tmp_ret_1 = invalid

    while true
        this = tmp0
        result = ArrayList_create_k_()
        __iter_166 = this.iterator_k_()
        while __iter_166.hasNext_k_()
            item = __iter_166.next_k_()
            tmp0_1 = item
            tmp_ret_0 = invalid

            while true
                it = tmp0_1
                tmp_ret_0 = other.contains_AnyN_k_(it)
                exit while
            end while
            if not tmp_ret_0 then
                result.add_AnyN_k_(item)
            end if

        end while
        tmp_ret_1 = result
        exit while
    end while
    return tmp_ret_1

end function
