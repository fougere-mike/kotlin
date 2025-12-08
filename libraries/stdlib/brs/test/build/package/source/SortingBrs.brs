sub sort_rArr_k_(m as Object)
    if m.size <= 1 then
        return
    end if
    quickSort_Arr_I_I_Function2AnyNAnyNI_k_(m, 0, m.size - 1, {invoke: function(a as Object, b as Object) as Integer
        return a.compareTo(b)
    end function})
end sub

sub sortWith_rArr_ComparatorAnyN_k_(m as Object, comparator as Object)
    if m.size <= 1 then
        return
    end if
    quickSort_Arr_I_I_Function2AnyNAnyNI_k_(m, 0, m.size - 1, {comparator: comparator, invoke: function(a as Dynamic, b as Dynamic) as Integer
        return m.comparator.compare(a, b)
    end function})
end sub

sub sortWith_rArr_ComparatorAnyN_I_I_k_(m as Object, comparator as Object, fromIndex = 0, toIndex = m.size)
    if (fromIndex < 0) or (toIndex > m.size) then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_((((("fromIndex: " + fromIndex) + ", toIndex: ") + toIndex) + ", size: ") + m.size)
    end if
    if fromIndex >= (toIndex - 1) then
        return
    end if
    quickSort_Arr_I_I_Function2AnyNAnyNI_k_(m, fromIndex, toIndex - 1, {comparator: comparator, invoke: function(a as Dynamic, b as Dynamic) as Integer
        return m.comparator.compare(a, b)
    end function})
end sub

sub sort_rMutableListAny_k_(m as Object)
    if m.size <= 1 then
        return
    end if
    quickSortList_MutableListAnyN_I_I_Function2AnyNAnyNI_k_(m, 0, m.size - 1, {invoke: function(a as Object, b as Object) as Integer
        return a.compareTo(b)
    end function})
end sub

sub sortWith_rMutableListAnyN_ComparatorAnyN_k_(m as Object, comparator as Object)
    if m.size <= 1 then
        return
    end if
    quickSortList_MutableListAnyN_I_I_Function2AnyNAnyNI_k_(m, 0, m.size - 1, {comparator: comparator, invoke: function(a as Dynamic, b as Dynamic) as Integer
        return m.comparator.compare(a, b)
    end function})
end sub

function sorted_rIterableAny_ListAny_k_(m as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") then
        if m.size <= 1 then
            return toList_rIterableAnyN_ListAnyN_k_(m)
        end if
        list = toMutableList_rIterableAnyN_MutableListAnyN_k_(m)
        sort_rMutableListAny_k_(list)
        return list
    end if
    list = toMutableList_rIterableAnyN_MutableListAnyN_k_(m)
    sort_rMutableListAny_k_(list)
    return list
end function

function sortedWith_rIterableAnyN_ComparatorAnyN_ListAnyN_k_(m as Object, comparator as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") then
        if m.size <= 1 then
            return toList_rIterableAnyN_ListAnyN_k_(m)
        end if
        list = toMutableList_rIterableAnyN_MutableListAnyN_k_(m)
        sortWith_rMutableListAnyN_ComparatorAnyN_k_(list, comparator)
        return list
    end if
    list = toMutableList_rIterableAnyN_MutableListAnyN_k_(m)
    sortWith_rMutableListAnyN_ComparatorAnyN_k_(list, comparator)
    return list
end function

function sortedDescending_rIterableAny_ListAny_k_(m as Object) as Object
    list = toMutableList_rIterableAnyN_MutableListAnyN_k_(m)
    quickSortList_MutableListAnyN_I_I_Function2AnyNAnyNI_k_(list, 0, list.size - 1, {invoke: function(a as Object, b as Object) as Integer
        return b.compareTo(a)
    end function})
    return list
end function

function sortedBy_rIterableAnyN_Function1AnyNAnyN_ListAnyN_k_(m as Object, selector as Function) as Object
    return sortedWith_rIterableAnyN_ComparatorAnyN_ListAnyN_k_(m, compareBy_Function1AnyNComparableStarN_ComparatorAnyN_k_(selector))
end function

function sortedByDescending_rIterableAnyN_Function1AnyNAnyN_ListAnyN_k_(m as Object, selector as Function) as Object
    return sortedWith_rIterableAnyN_ComparatorAnyN_ListAnyN_k_(m, compareByDescending_Function1AnyNComparableStarN_ComparatorAnyN_k_(selector))
end function

sub sortDescending_rMutableListAny_k_(m as Object)
    if m.size <= 1 then
        return
    end if
    quickSortList_MutableListAnyN_I_I_Function2AnyNAnyNI_k_(m, 0, m.size - 1, {invoke: function(a as Object, b as Object) as Integer
        return b.compareTo(a)
    end function})
end sub

sub sortBy_rMutableListAnyN_Function1AnyNAnyN_k_(m as Object, selector as Function)
    if m.size > 1 then
        sortWith_rMutableListAnyN_ComparatorAnyN_k_(m, compareBy_Function1AnyNComparableStarN_ComparatorAnyN_k_(selector))
    end if
end sub

sub sortByDescending_rMutableListAnyN_Function1AnyNAnyN_k_(m as Object, selector as Function)
    if m.size > 1 then
        sortWith_rMutableListAnyN_ComparatorAnyN_k_(m, compareByDescending_Function1AnyNComparableStarN_ComparatorAnyN_k_(selector))
    end if
end sub

sub quickSort_Arr_I_I_Function2AnyNAnyNI_k_(array as Object, low as Integer, high as Integer, compareFn as Function)
    if low >= high then
        return
    end if
    if (high - low) < 10 then
        insertionSort_Arr_I_I_Function2AnyNAnyNI_k_(array, low, high, compareFn)
        return
    end if
    pivotIndex = partition_Arr_I_I_Function2AnyNAnyNI_I_k_(array, low, high, compareFn)
    quickSort_Arr_I_I_Function2AnyNAnyNI_k_(array, low, pivotIndex - 1, compareFn)
    quickSort_Arr_I_I_Function2AnyNAnyNI_k_(array, pivotIndex + 1, high, compareFn)
end sub

function partition_Arr_I_I_Function2AnyNAnyNI_I_k_(array as Object, low as Integer, high as Integer, compareFn as Function) as Integer
    pivot = array[high]
    i = low - 1
    j = low
    while j < high
        if compareFn.invoke(array[j], pivot) <= 0 then
            i = (i + 1)
            temp = array[i]
            array.set(i, array[j])
            array.set(j, temp)
        end if
        j = (j + 1)
    end while
    temp = array[i + 1]
    array.set(i + 1, array[high])
    array.set(high, temp)
    return i + 1
end function

sub insertionSort_Arr_I_I_Function2AnyNAnyNI_k_(array as Object, low as Integer, high as Integer, compareFn as Function)
    i = low + 1
    while i <= high
        key = array[i]
        j = i - 1
        while (j >= low) and (compareFn.invoke(array[j], key) > 0)
            array.set(j + 1, array[j])
            j = (j - 1)
        end while
        array.set(j + 1, key)
        i = (i + 1)
    end while
end sub

sub quickSortList_MutableListAnyN_I_I_Function2AnyNAnyNI_k_(list as Object, low as Integer, high as Integer, compareFn as Function)
    if low >= high then
        return
    end if
    if (high - low) < 10 then
        insertionSortList_MutableListAnyN_I_I_Function2AnyNAnyNI_k_(list, low, high, compareFn)
        return
    end if
    pivotIndex = partitionList_MutableListAnyN_I_I_Function2AnyNAnyNI_I_k_(list, low, high, compareFn)
    quickSortList_MutableListAnyN_I_I_Function2AnyNAnyNI_k_(list, low, pivotIndex - 1, compareFn)
    quickSortList_MutableListAnyN_I_I_Function2AnyNAnyNI_k_(list, pivotIndex + 1, high, compareFn)
end sub

function partitionList_MutableListAnyN_I_I_Function2AnyNAnyNI_I_k_(list as Object, low as Integer, high as Integer, compareFn as Function) as Integer
    pivot = list[high]
    i = low - 1
    j = low
    while j < high
        if compareFn.invoke(list[j], pivot) <= 0 then
            i = (i + 1)
            temp = list[i]
            list.set(i, list[j])
            list.set(j, temp)
        end if
        j = (j + 1)
    end while
    temp = list[i + 1]
    list.set(i + 1, list[high])
    list.set(high, temp)
    return i + 1
end function

sub insertionSortList_MutableListAnyN_I_I_Function2AnyNAnyNI_k_(list as Object, low as Integer, high as Integer, compareFn as Function)
    i = low + 1
    while i <= high
        key = list[i]
        j = i - 1
        while (j >= low) and (compareFn.invoke(list[j], key) > 0)
            list.set(j + 1, list[j])
            j = (j - 1)
        end while
        list.set(j + 1, key)
        i = (i + 1)
    end while
end sub

sub reverse_rMutableListAnyN_k_(m as Object)
    midPoint = m.size / 2
    i = 0
    while i < midPoint
        tmp = m[i]
        m.set(i, m[(m.size - i) - 1])
        m.set((m.size - i) - 1, tmp)
        i = (i + 1)
    end while
end sub

function reversed_rIterableAnyN_ListAnyN_k_(m as Object) as Object
    if __kotlin_isInstanceOf(m, "Collection") and (m.size <= 1) then
        return toList_rIterableAnyN_ListAnyN_k_(m)
    end if
    list = toMutableList_rIterableAnyN_MutableListAnyN_k_(m)
    reverse_rMutableListAnyN_k_(list)
    return list
end function
