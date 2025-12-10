sub sort_rArr_k_(m as Object)
    if m.count() <= 1 then
        return
    end if
    quickSort_Arr_I_I_Function2I_k_(m, 0, m.count() - 1, {invoke: function(a as Object, b as Object) as Integer
        return a.compareTo_AnyN_k_(b)
    end function})
end sub

sub sortWith_rArr_Comparator_k_(m as Object, comparator as Object)
    if m.count() <= 1 then
        return
    end if
    quickSort_Arr_I_I_Function2I_k_(m, 0, m.count() - 1, {comparator: comparator, invoke: function(a as Dynamic, b as Dynamic) as Integer
        return m.comparator.compare_AnyN_AnyN_k_(a, b)
    end function})
end sub

sub sortWith_rArr_Comparator_I_I_k_(m as Object, comparator as Object, fromIndex = 0, toIndex = m.count())
    if (fromIndex < 0) or (toIndex > m.count()) then
        throw IndexOutOfBoundsException_create_StrN_k_((((("fromIndex: " + fromIndex) + ", toIndex: ") + toIndex) + ", size: ") + m.count())
    end if
    if fromIndex >= (toIndex - 1) then
        return
    end if
    quickSort_Arr_I_I_Function2I_k_(m, fromIndex, toIndex - 1, {comparator: comparator, invoke: function(a as Dynamic, b as Dynamic) as Integer
        return m.comparator.compare_AnyN_AnyN_k_(a, b)
    end function})
end sub

sub sort_rMutableList_k_(m as Object)
    if m.get_size() <= 1 then
        return
    end if
    quickSortList_MutableList_I_I_Function2I_k_(m, 0, m.get_size() - 1, {invoke: function(a as Object, b as Object) as Integer
        return a.compareTo_AnyN_k_(b)
    end function})
end sub

sub sortWith_rMutableList_Comparator_k_(m as Object, comparator as Object)
    if m.get_size() <= 1 then
        return
    end if
    quickSortList_MutableList_I_I_Function2I_k_(m, 0, m.get_size() - 1, {comparator: comparator, invoke: function(a as Dynamic, b as Dynamic) as Integer
        return m.comparator.compare_AnyN_AnyN_k_(a, b)
    end function})
end sub

function sorted_rIterable_k_(m as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") then
        if m.get_size() <= 1 then
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
        if m.get_size() <= 1 then
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
    quickSortList_MutableList_I_I_Function2I_k_(list, 0, list.get_size() - 1, {invoke: function(a as Object, b as Object) as Integer
        return b.compareTo_AnyN_k_(a)
    end function})
    return list
end function

function sortedBy_rIterable_Function1_k_(m as Object, selector as Object) as Object
    return sortedWith_rIterable_Comparator_k_(m, compareBy_Function1ComparableN_k_(selector))
end function

function sortedByDescending_rIterable_Function1_k_(m as Object, selector as Object) as Object
    return sortedWith_rIterable_Comparator_k_(m, compareByDescending_Function1ComparableN_k_(selector))
end function

sub sortDescending_rMutableList_k_(m as Object)
    if m.get_size() <= 1 then
        return
    end if
    quickSortList_MutableList_I_I_Function2I_k_(m, 0, m.get_size() - 1, {invoke: function(a as Object, b as Object) as Integer
        return b.compareTo_AnyN_k_(a)
    end function})
end sub

sub sortBy_rMutableList_Function1_k_(m as Object, selector as Object)
    if m.get_size() > 1 then
        sortWith_rMutableList_Comparator_k_(m, compareBy_Function1ComparableN_k_(selector))
    end if
end sub

sub sortByDescending_rMutableList_Function1_k_(m as Object, selector as Object)
    if m.get_size() > 1 then
        sortWith_rMutableList_Comparator_k_(m, compareByDescending_Function1ComparableN_k_(selector))
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
        if compareFn.invoke(array[j], pivot) <= 0 then
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
        while (j >= low) and (compareFn.invoke(array[j], key) > 0)
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
        if compareFn.invoke(list.get_I_k_(j), pivot) <= 0 then
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
        while (j >= low) and (compareFn.invoke(list.get_I_k_(j), key) > 0)
            list.set_I_AnyN_k_(j + 1, list.get_I_k_(j))
            j = (j - 1)
        end while
        list.set_I_AnyN_k_(j + 1, key)
        i = (i + 1)
    end while
end sub

sub reverse_rMutableList_k_(m as Object)
    midPoint = m.get_size() / 2
    i = 0
    while i < midPoint
        tmp = m.get_I_k_(i)
        m.set_I_AnyN_k_(i, m.get_I_k_((m.get_size() - i) - 1))
        m.set_I_AnyN_k_((m.get_size() - i) - 1, tmp)
        i = (i + 1)
    end while
end sub

function reversed_rIterable_k_(m as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") and (m.get_size() <= 1) then
        return toList_rIterable_k_(m)
    end if
    list = toMutableList_rIterable_k_(m)
    reverse_rMutableList_k_(list)
    return list
end function
