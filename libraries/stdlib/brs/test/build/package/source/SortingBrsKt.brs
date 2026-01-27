sub sort_rArr_k_(m as Object)
    if m.count() <= 1 then
        return
    end if
    quickSort_Arr_I_I_Function2I_k_(m, 0, m.count() - 1, sort_lambda_create_k_())
end sub

sub sortWith_rArr_Comparator_k_(m as Object, comparator as Object)
    if m.count() <= 1 then
        return
    end if
    quickSort_Arr_I_I_Function2I_k_(m, 0, m.count() - 1, sortWith_lambda_create_Comparator_k_(comparator))
end sub

sub sortWith_rArr_Comparator_I_I_k_(m as Object, comparator as Object, fromIndex = 0, toIndex = m.count())
    if fromIndex = invalid then
        fromIndex = 0
    end if
    if toIndex = invalid then
        toIndex = m.count()
    end if
    if (fromIndex < 0) or (toIndex > m.count()) then
        throw IndexOutOfBoundsException_create_StrN_k_((((("fromIndex: " + __kotlin_numToStr_I_k_(fromIndex)) + ", toIndex: ") + __kotlin_numToStr_I_k_(toIndex)) + ", size: ") + __kotlin_numToStr_I_k_(m.count()))
    end if
    if fromIndex >= (toIndex - 1) then
        return
    end if
    quickSort_Arr_I_I_Function2I_k_(m, fromIndex, toIndex - 1, sortWith_lambda_1_create_Comparator_k_(comparator))
end sub

sub sort_rMutableList_k_(m as Object)
    if m.__get_size() <= 1 then
        return
    end if
    quickSortList_MutableList_I_I_Function2I_k_(m, 0, m.__get_size() - 1, sort_lambda_1_create_k_())
end sub

sub sortWith_rMutableList_Comparator_k_(m as Object, comparator as Object)
    if m.__get_size() <= 1 then
        return
    end if
    quickSortList_MutableList_I_I_Function2I_k_(m, 0, m.__get_size() - 1, sortWith_lambda_2_create_Comparator_k_(comparator))
end sub

function sorted_rIterable_k_(m as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") then
        if m.__get_size() <= 1 then
            return toList_rIterable_k_(m)
        end if
        list = toMutableList_rIterable_k_(m)
        sort_rMutableList_k_(list)
        return list
    end if
    list = toMutableList_rIterable_k_(m)
    sort_rMutableList_k_(list)
    return list
end function

function sortedWith_rIterable_Comparator_k_(m as Object, comparator as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") then
        if m.__get_size() <= 1 then
            return toList_rIterable_k_(m)
        end if
        list = toMutableList_rIterable_k_(m)
        sortWith_rMutableList_Comparator_k_(list, comparator)
        return list
    end if
    list = toMutableList_rIterable_k_(m)
    sortWith_rMutableList_Comparator_k_(list, comparator)
    return list
end function

function sortedDescending_rIterable_k_(m as Object) as Object
    list = toMutableList_rIterable_k_(m)
    quickSortList_MutableList_I_I_Function2I_k_(list, 0, list.__get_size() - 1, sortedDescending_lambda_create_k_())
    return list
end function

function sortedBy_rIterable_Function1_k_(m as Object, selector as Object) as Object
    tmp0 = selector
    tmp_ret_1 = invalid

    while true
        selector = tmp0
        tmp_ret_1 = sortedBy_lambda_create_Function1AnyNComparableN_k_(selector)
        exit while
    end while
    return sortedWith_rIterable_Comparator_k_(m, tmp_ret_1)

end function

function sortedByDescending_rIterable_Function1_k_(m as Object, selector as Object) as Object
    tmp0 = selector
    tmp_ret_1 = invalid

    while true
        selector = tmp0
        tmp_ret_1 = sortedByDescending_lambda_create_Function1AnyNComparableN_k_(selector)
        exit while
    end while
    return sortedWith_rIterable_Comparator_k_(m, tmp_ret_1)

end function

sub sortDescending_rMutableList_k_(m as Object)
    if m.__get_size() <= 1 then
        return
    end if
    quickSortList_MutableList_I_I_Function2I_k_(m, 0, m.__get_size() - 1, sortDescending_lambda_create_k_())
end sub

sub sortBy_rMutableList_Function1_k_(m as Object, selector as Object)
    tmp0 = selector
    tmp_ret_1 = invalid

    while true
        selector = tmp0
        tmp_ret_1 = sortBy_lambda_create_Function1AnyNComparableN_k_(selector)
        exit while
    end while
    if m.__get_size() > 1 then
        sortWith_rMutableList_Comparator_k_(m, tmp_ret_1)
    end if
end sub

sub sortByDescending_rMutableList_Function1_k_(m as Object, selector as Object)
    tmp0 = selector
    tmp_ret_1 = invalid

    while true
        selector = tmp0
        tmp_ret_1 = sortByDescending_lambda_create_Function1AnyNComparableN_k_(selector)
        exit while
    end while
    if m.__get_size() > 1 then
        sortWith_rMutableList_Comparator_k_(m, tmp_ret_1)
    end if
end sub

sub quickSort_Arr_I_I_Function2I_k_(array as Object, low as Integer, high as Integer, compareFn as Object)
    if low >= high then
        return
    end if
    if (high - low) < 10 then
        insertionSort_Arr_I_I_Function2I_k_(array, low, high, compareFn)
        return
    end if
    pivotIndex = partition_Arr_I_I_Function2I_k_(array, low, high, compareFn)
    quickSort_Arr_I_I_Function2I_k_(array, low, pivotIndex - 1, compareFn)
    quickSort_Arr_I_I_Function2I_k_(array, pivotIndex + 1, high, compareFn)
end sub

function partition_Arr_I_I_Function2I_k_(array as Object, low as Integer, high as Integer, compareFn as Object) as Integer
    pivot = array[high]
    i = low - 1
    j = low
    while j < high
        if compareFn.invoke_AnyN_AnyN_k_(array[j], pivot) <= 0 then
            i = (i + 1)
            temp = array[i]
            array[i] = array[j]
            array[j] = temp
        end if
        j = (j + 1)
    end while
    temp = array[i + 1]
    array[i + 1] = array[high]
    array[high] = temp
    return i + 1
end function

sub insertionSort_Arr_I_I_Function2I_k_(array as Object, low as Integer, high as Integer, compareFn as Object)
    i = low + 1
    while i <= high
        key = array[i]
        j = i - 1
        while (j >= low) and (compareFn.invoke_AnyN_AnyN_k_(array[j], key) > 0)
            array[j + 1] = array[j]
            j = (j - 1)
        end while
        array[j + 1] = key
        i = (i + 1)
    end while
end sub

sub quickSortList_MutableList_I_I_Function2I_k_(list as Object, low as Integer, high as Integer, compareFn as Object)
    if low >= high then
        return
    end if
    if (high - low) < 10 then
        insertionSortList_MutableList_I_I_Function2I_k_(list, low, high, compareFn)
        return
    end if
    pivotIndex = partitionList_MutableList_I_I_Function2I_k_(list, low, high, compareFn)
    quickSortList_MutableList_I_I_Function2I_k_(list, low, pivotIndex - 1, compareFn)
    quickSortList_MutableList_I_I_Function2I_k_(list, pivotIndex + 1, high, compareFn)
end sub

function partitionList_MutableList_I_I_Function2I_k_(list as Object, low as Integer, high as Integer, compareFn as Object) as Integer
    pivot = list.get_I_k_(high)
    i = low - 1
    j = low
    while j < high
        if compareFn.invoke_AnyN_AnyN_k_(list.get_I_k_(j), pivot) <= 0 then
            i = (i + 1)
            temp = list.get_I_k_(i)
            list.set_I_AnyN_k_(i, list.get_I_k_(j))
            list.set_I_AnyN_k_(j, temp)
        end if
        j = (j + 1)
    end while
    temp = list.get_I_k_(i + 1)
    list.set_I_AnyN_k_(i + 1, list.get_I_k_(high))
    list.set_I_AnyN_k_(high, temp)
    return i + 1
end function

sub insertionSortList_MutableList_I_I_Function2I_k_(list as Object, low as Integer, high as Integer, compareFn as Object)
    i = low + 1
    while i <= high
        key = list.get_I_k_(i)
        j = i - 1
        while (j >= low) and (compareFn.invoke_AnyN_AnyN_k_(list.get_I_k_(j), key) > 0)
            list.set_I_AnyN_k_(j + 1, list.get_I_k_(j))
            j = (j - 1)
        end while
        list.set_I_AnyN_k_(j + 1, key)
        i = (i + 1)
    end while
end sub

sub reverse_rMutableList_k_(m as Object)
    midPoint = m.__get_size() / 2
    i = 0
    while i < midPoint
        tmp = m.get_I_k_(i)
        m.set_I_AnyN_k_(i, m.get_I_k_((m.__get_size() - i) - 1))
        m.set_I_AnyN_k_((m.__get_size() - i) - 1, tmp)
        i = (i + 1)
    end while
end sub

function reversed_rIterable_k_(m as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") and (m.__get_size() <= 1) then
        return toList_rIterable_k_(m)
    end if
    list = toMutableList_rIterable_k_(m)
    reverse_rMutableList_k_(list)
    return list
end function

function sort_lambda_create_k_() as Object
    this = {}
    this.__type = "sort_lambda"
    this.__proto = ["sort_lambda", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = sort_lambda_invoke_AnyN_AnyN_k_
    return this
end function

function sort_lambda_invoke_AnyN_AnyN_k_(a as Object, b as Object) as Integer
    return brsCompareTo_AnyN_AnyN_k_(a, b)
end function

function sortWith_lambda_create_Comparator_k_(_comparator as Object) as Object
    this = {}
    this.__type = "sortWith_lambda"
    this.__proto = ["sortWith_lambda", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = sortWith_lambda_invoke_AnyN_AnyN_k_
    this._comparator = _comparator
    return this
end function

function sortWith_lambda_invoke_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
    return m._comparator.compare_AnyN_AnyN_k_(a, b)
end function

function sortWith_lambda_1_create_Comparator_k_(_comparator as Object) as Object
    this = {}
    this.__type = "sortWith_lambda_1"
    this.__proto = ["sortWith_lambda_1", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = sortWith_lambda_1_invoke_AnyN_AnyN_k_
    this._comparator = _comparator
    return this
end function

function sortWith_lambda_1_invoke_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
    return m._comparator.compare_AnyN_AnyN_k_(a, b)
end function

function sort_lambda_1_create_k_() as Object
    this = {}
    this.__type = "sort_lambda_1"
    this.__proto = ["sort_lambda_1", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = sort_lambda_1_invoke_AnyN_AnyN_k_
    return this
end function

function sort_lambda_1_invoke_AnyN_AnyN_k_(a as Object, b as Object) as Integer
    return brsCompareTo_AnyN_AnyN_k_(a, b)
end function

function sortWith_lambda_2_create_Comparator_k_(_comparator as Object) as Object
    this = {}
    this.__type = "sortWith_lambda_2"
    this.__proto = ["sortWith_lambda_2", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = sortWith_lambda_2_invoke_AnyN_AnyN_k_
    this._comparator = _comparator
    return this
end function

function sortWith_lambda_2_invoke_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
    return m._comparator.compare_AnyN_AnyN_k_(a, b)
end function

function sortedDescending_lambda_create_k_() as Object
    this = {}
    this.__type = "sortedDescending_lambda"
    this.__proto = ["sortedDescending_lambda", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = sortedDescending_lambda_invoke_AnyN_AnyN_k_
    return this
end function

function sortedDescending_lambda_invoke_AnyN_AnyN_k_(a as Object, b as Object) as Integer
    return brsCompareTo_AnyN_AnyN_k_(b, a)
end function

function sortedBy_lambda_create_Function1AnyNComparableN_k_(_selector as Object) as Object
    this = {}
    this.__type = "sortedBy_lambda"
    this.__proto = ["sortedBy_lambda", "Comparator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.compare_AnyN_AnyN_k_ = sortedBy_lambda_compare_AnyN_AnyN_k_
    this._selector = _selector
    return this
end function

function sortedBy_lambda_compare_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
    tmp0 = a
    tmp2 = b
    tmp_ret_0 = invalid

    while true
        a = tmp0
        b = tmp2
        tmp_ret_0 = compareValues_AnyN_AnyN_k_(m._selector.invoke_AnyN_k_(a), m._selector.invoke_AnyN_k_(b))
        exit while
    end while
    return tmp_ret_0

end function

function sortedByDescending_lambda_create_Function1AnyNComparableN_k_(_selector as Object) as Object
    this = {}
    this.__type = "sortedByDescending_lambda"
    this.__proto = ["sortedByDescending_lambda", "Comparator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.compare_AnyN_AnyN_k_ = sortedByDescending_lambda_compare_AnyN_AnyN_k_
    this._selector = _selector
    return this
end function

function sortedByDescending_lambda_compare_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
    tmp0 = b
    tmp2 = a
    tmp_ret_0 = invalid

    while true
        a = tmp0
        b = tmp2
        tmp_ret_0 = compareValues_AnyN_AnyN_k_(m._selector.invoke_AnyN_k_(a), m._selector.invoke_AnyN_k_(b))
        exit while
    end while
    return tmp_ret_0

end function

function sortDescending_lambda_create_k_() as Object
    this = {}
    this.__type = "sortDescending_lambda"
    this.__proto = ["sortDescending_lambda", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = sortDescending_lambda_invoke_AnyN_AnyN_k_
    return this
end function

function sortDescending_lambda_invoke_AnyN_AnyN_k_(a as Object, b as Object) as Integer
    return brsCompareTo_AnyN_AnyN_k_(b, a)
end function

function sortBy_lambda_create_Function1AnyNComparableN_k_(_selector as Object) as Object
    this = {}
    this.__type = "sortBy_lambda"
    this.__proto = ["sortBy_lambda", "Comparator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.compare_AnyN_AnyN_k_ = sortBy_lambda_compare_AnyN_AnyN_k_
    this._selector = _selector
    return this
end function

function sortBy_lambda_compare_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
    tmp0 = a
    tmp2 = b
    tmp_ret_0 = invalid

    while true
        a = tmp0
        b = tmp2
        tmp_ret_0 = compareValues_AnyN_AnyN_k_(m._selector.invoke_AnyN_k_(a), m._selector.invoke_AnyN_k_(b))
        exit while
    end while
    return tmp_ret_0

end function

function sortByDescending_lambda_create_Function1AnyNComparableN_k_(_selector as Object) as Object
    this = {}
    this.__type = "sortByDescending_lambda"
    this.__proto = ["sortByDescending_lambda", "Comparator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.compare_AnyN_AnyN_k_ = sortByDescending_lambda_compare_AnyN_AnyN_k_
    this._selector = _selector
    return this
end function

function sortByDescending_lambda_compare_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
    tmp0 = b
    tmp2 = a
    tmp_ret_0 = invalid

    while true
        a = tmp0
        b = tmp2
        tmp_ret_0 = compareValues_AnyN_AnyN_k_(m._selector.invoke_AnyN_k_(a), m._selector.invoke_AnyN_k_(b))
        exit while
    end while
    return tmp_ret_0

end function
