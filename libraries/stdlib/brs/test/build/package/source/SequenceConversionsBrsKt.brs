function toList_rSequence_k_(m as Object) as Object
    return toMutableList_rSequence_k_(m)
end function

function toMutableList_rSequence_k_(m as Object) as Object
    return toCollection_rSequence_Any_k_(m, ArrayList_create_k_())
end function

function toSet_rSequence_k_(m as Object) as Object
    return toCollection_rSequence_Any_k_(m, HashSet_create_k_())
end function

function toMutableSet_rSequence_k_(m as Object) as Object
    return toCollection_rSequence_Any_k_(m, HashSet_create_k_())
end function

function toHashSet_rSequence_k_(m as Object) as Object
    return toCollection_rSequence_Any_k_(m, HashSet_create_k_())
end function

function toCollection_rSequence_Any_k_(m as Object, destination as Object) as Object
    __iter_22 = m.iterator_k_()
    while __iter_22.hasNext_k_()
        item = __iter_22.next_k_()
        destination.add_AnyN_k_(item)

    end while

    return destination
end function

function associate_rSequence_Function1Pair_k_(m as Object, transform as Object) as Object
    tmp0 = m
    tmp2 = HashMap_create_k_()
    tmp_ret_0 = invalid

    while true
        this = tmp0
        destination = tmp2
        __iter_23 = this.iterator_k_()
        while __iter_23.hasNext_k_()
            element = __iter_23.next_k_()
            pair = transform.invoke_AnyN_k_(element)
            destination.put_AnyN_AnyN_k_(pair.first, pair.second)

        end while
        tmp_ret_0 = destination
        exit while
    end while
    return tmp_ret_0

end function

function associateBy_rSequence_Function1_k_(m as Object, keySelector as Object) as Object
    tmp0 = m
    tmp2 = HashMap_create_k_()
    tmp_ret_0 = invalid

    while true
        this = tmp0
        destination = tmp2
        __iter_24 = this.iterator_k_()
        while __iter_24.hasNext_k_()
            element = __iter_24.next_k_()
            destination.put_AnyN_AnyN_k_(keySelector.invoke_AnyN_k_(element), element)

        end while
        tmp_ret_0 = destination
        exit while
    end while
    return tmp_ret_0

end function

function associateBy_rSequence_Function1_Function1_k_(m as Object, keySelector as Object, valueTransform as Object) as Object
    tmp0 = m
    tmp2 = HashMap_create_k_()
    tmp_ret_0 = invalid

    while true
        this = tmp0
        destination = tmp2
        __iter_25 = this.iterator_k_()
        while __iter_25.hasNext_k_()
            element = __iter_25.next_k_()
            destination.put_AnyN_AnyN_k_(keySelector.invoke_AnyN_k_(element), valueTransform.invoke_AnyN_k_(element))

        end while
        tmp_ret_0 = destination
        exit while
    end while
    return tmp_ret_0

end function

function associateTo_rSequence_Any_Function1Pair_k_(m as Object, destination as Object, transform as Object) as Object
    __iter_26 = m.iterator_k_()
    while __iter_26.hasNext_k_()
        element = __iter_26.next_k_()
        pair = transform.invoke_AnyN_k_(element)
        destination.put_AnyN_AnyN_k_(pair.first, pair.second)

    end while

    return destination
end function

function associateByTo_rSequence_Any_Function1_k_(m as Object, destination as Object, keySelector as Object) as Object
    __iter_27 = m.iterator_k_()
    while __iter_27.hasNext_k_()
        element = __iter_27.next_k_()
        destination.put_AnyN_AnyN_k_(keySelector.invoke_AnyN_k_(element), element)

    end while

    return destination
end function

function associateByTo_rSequence_Any_Function1_Function1_k_(m as Object, destination as Object, keySelector as Object, valueTransform as Object) as Object
    __iter_28 = m.iterator_k_()
    while __iter_28.hasNext_k_()
        element = __iter_28.next_k_()
        destination.put_AnyN_AnyN_k_(keySelector.invoke_AnyN_k_(element), valueTransform.invoke_AnyN_k_(element))

    end while

    return destination
end function

function associateWith_rSequence_Function1_k_(m as Object, valueSelector as Object) as Object
    result = HashMap_create_k_()
    tmp0 = m
    tmp2 = result
    tmp_ret_0 = invalid

    while true
        this = tmp0
        destination = tmp2
        __iter_29 = this.iterator_k_()
        while __iter_29.hasNext_k_()
            element = __iter_29.next_k_()
            destination.put_AnyN_AnyN_k_(element, valueSelector.invoke_AnyN_k_(element))

        end while
        tmp_ret_0 = destination
        exit while
    end while
    return tmp_ret_0

end function

function associateWithTo_rSequence_Any_Function1_k_(m as Object, destination as Object, valueSelector as Object) as Object
    __iter_30 = m.iterator_k_()
    while __iter_30.hasNext_k_()
        element = __iter_30.next_k_()
        destination.put_AnyN_AnyN_k_(element, valueSelector.invoke_AnyN_k_(element))

    end while

    return destination
end function

function map_rSequence_Function1_k_(m as Object, transform as Object) as Object
    return TransformingSequence_create_Sequence_Function1_k_(m, transform)
end function

function mapIndexed_rSequence_Function2I_k_(m as Object, transform as Object) as Object
    return TransformingIndexedSequence_create_Sequence_Function2I_k_(m, transform)
end function

function filter_rSequence_Function1Z_k_(m as Object, predicate as Object) as Object
    return FilteringSequence_create_Sequence_Z_Function1Z_k_(m, true, predicate)
end function

function filterNot_rSequence_Function1Z_k_(m as Object, predicate as Object) as Object
    return FilteringSequence_create_Sequence_Z_Function1Z_k_(m, false, predicate)
end function

function filterIndexed_rSequence_Function2IZ_k_(m as Object, predicate as Object) as Object
    return FilteringIndexedSequence_create_Sequence_Z_Function2IZ_k_(m, true, predicate)
end function

function filterNotNull_rSequence_k_(m as Object) as Object
    return filter_rSequence_Function1Z_k_(m, filterNotNull_lambda_create_k_())
end function

function flatMap_rSequence_Function1Iterable_k_(m as Object, transform as Object) as Object
    return FlatteningSequence_create_Sequence_Function1_Function1Iterator_k_(m, transform, flatMap_lambda_create_k_())
end function

function flatten_rSequenceSequence_k_(m as Object) as Object
    return flatten_rSequence_Function1Iterator_k_(m, flatten_lambda_create_k_())
end function

function flatten_rSequenceIterable_k_(m as Object) as Object
    return flatten_rSequence_Function1Iterator_k_(m, flatten_lambda_1_create_k_())
end function

function flatten_rSequence_Function1Iterator_k_(m as Object, iterator as Object) as Object
    if __kotlin_isInstanceOf(m, "TransformingSequence") then
        return m.flatten_Function1Iterator_k_(iterator)
    end if
    return FlatteningSequence_create_Sequence_Function1_Function1Iterator_k_(m, flatten_lambda_2_create_k_(), iterator)
end function

function take_rSequence_I_k_(m as Object, n as Integer) as Object
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

    __when_tmp0 = invalid
    if n = 0 then
        __when_tmp0 = emptySequence_k_()
    else if __kotlin_isInstanceOf(m, "DropTakeSequence") then
        __when_tmp0 = m.take_I_k_(n)
    else if true then
        __when_tmp0 = TakingSequence_create_Sequence_I_k_(m, n)
    end if
    return __when_tmp0

end function

function takeWhile_rSequence_Function1Z_k_(m as Object, predicate as Object) as Object
    return TakingWhileSequence_create_Sequence_Function1Z_k_(m, predicate)
end function

function drop_rSequence_I_k_(m as Object, n as Integer) as Object
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

    __when_tmp1 = invalid
    if n = 0 then
        __when_tmp1 = m
    else if __kotlin_isInstanceOf(m, "DropTakeSequence") then
        __when_tmp1 = m.drop_I_k_(n)
    else if true then
        __when_tmp1 = DroppingSequence_create_Sequence_I_k_(m, n)
    end if
    return __when_tmp1

end function

function dropWhile_rSequence_Function1Z_k_(m as Object, predicate as Object) as Object
    return DroppingWhileSequence_create_Sequence_Function1Z_k_(m, predicate)
end function

function distinct_rSequence_k_(m as Object) as Object
    return distinctBy_rSequence_Function1_k_(m, distinct_lambda_create_k_())
end function

function distinctBy_rSequence_Function1_k_(m as Object, selector as Object) as Object
    return DistinctSequence_create_Sequence_Function1_k_(m, selector)
end function

function zip_rSequence_Sequence_k_(m as Object, other as Object) as Object
    return MergingSequence_create_Sequence_Sequence_Function2_k_(m, other, zip_lambda_create_k_())
end function

function zip_rSequence_Sequence_Function2_k_(m as Object, other as Object, transform as Object) as Object
    return MergingSequence_create_Sequence_Sequence_Function2_k_(m, other, transform)
end function

function sorted_rSequence_k_(m as Object) as Object
    return Anon_49b46b95_create_Sequence_k_(m)
end function

function sortedWith_rSequence_Comparator_k_(m as Object, comparator as Object) as Object
    return Anon_29986244_create_Sequence_Comparator_k_(m, comparator)
end function

function sortedDescending_rSequence_k_(m as Object) as Object
    return sortedWith_rSequence_Comparator_k_(m, reverseOrder_k_())
end function

function sortedBy_rSequence_Function1_k_(m as Object, selector as Object) as Object
    tmp_ret_1 = invalid
    while true
        tmp_ret_1 = sortedBy_lambda_1_create_Function1_k_(selector)
        exit while
    end while
    return sortedWith_rSequence_Comparator_k_(m, tmp_ret_1)

end function

function sortedByDescending_rSequence_Function1_k_(m as Object, selector as Object) as Object
    tmp_ret_1 = invalid
    while true
        tmp_ret_1 = sortedByDescending_lambda_1_create_Function1_k_(selector)
        exit while
    end while
    return sortedWith_rSequence_Comparator_k_(m, tmp_ret_1)

end function

function ifEmpty_rSequence_Function0Sequence_k_(m as Object, defaultValue as Object) as Object
    return Anon_2a369f63_create_Sequence_Function0Sequence_k_(m, defaultValue)
end function

function filterNotNull_lambda_create_k_() as Object
    this = {}
    this.__type = "filterNotNull_lambda"
    this.__proto = ["filterNotNull_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = filterNotNull_lambda_invoke_AnyN_k_
    return this
end function

function filterNotNull_lambda_invoke_AnyN_k_(it as Dynamic) as Boolean
    return it <> invalid
end function

function flatMap_lambda_create_k_() as Object
    this = {}
    this.__type = "flatMap_lambda"
    this.__proto = ["flatMap_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = flatMap_lambda_invoke_AnyN_k_
    return this
end function

function flatMap_lambda_invoke_AnyN_k_(it as Object) as Object
    return it.iterator_k_()
end function

function flatten_lambda_create_k_() as Object
    this = {}
    this.__type = "flatten_lambda"
    this.__proto = ["flatten_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = flatten_lambda_invoke_AnyN_k_
    return this
end function

function flatten_lambda_invoke_AnyN_k_(it as Object) as Object
    return it.iterator_k_()
end function

function flatten_lambda_1_create_k_() as Object
    this = {}
    this.__type = "flatten_lambda_1"
    this.__proto = ["flatten_lambda_1", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = flatten_lambda_1_invoke_AnyN_k_
    return this
end function

function flatten_lambda_1_invoke_AnyN_k_(it as Object) as Object
    return it.iterator_k_()
end function

function flatten_lambda_2_create_k_() as Object
    this = {}
    this.__type = "flatten_lambda_2"
    this.__proto = ["flatten_lambda_2", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = flatten_lambda_2_invoke_AnyN_k_
    return this
end function

function flatten_lambda_2_invoke_AnyN_k_(it as Dynamic) as Dynamic
    return it
end function

function distinct_lambda_create_k_() as Object
    this = {}
    this.__type = "distinct_lambda"
    this.__proto = ["distinct_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = distinct_lambda_invoke_AnyN_k_
    return this
end function

function distinct_lambda_invoke_AnyN_k_(it as Dynamic) as Dynamic
    return it
end function

function zip_lambda_create_k_() as Object
    this = {}
    this.__type = "zip_lambda"
    this.__proto = ["zip_lambda", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = zip_lambda_invoke_AnyN_AnyN_k_
    return this
end function

function zip_lambda_invoke_AnyN_AnyN_k_(t1 as Dynamic, t2 as Dynamic) as Object
    return to_rAnyN_AnyN_k_(t1, t2)
end function

function Anon_49b46b95_create_Sequence_k_(_this_sorted as Object) as Object
    this = {}
    this.__type = "Anon_49b46b95"
    this.__proto = ["Anon_49b46b95", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = Anon_49b46b95_iterator_k_
    this._this_sorted = _this_sorted
    return this
end function

function Anon_49b46b95_iterator_k_() as Object
    sortedList = toMutableList_rSequence_k_(m._this_sorted)
    sort_rMutableList_k_(sortedList)
    return sortedList.iterator_k_()
end function

function Anon_29986244_create_Sequence_Comparator_k_(_this_sortedWith as Object, _comparator as Object) as Object
    this = {}
    this.__type = "Anon_29986244"
    this.__proto = ["Anon_29986244", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = Anon_29986244_iterator_k_
    this._this_sortedWith = _this_sortedWith
    this._comparator = _comparator
    return this
end function

function Anon_29986244_iterator_k_() as Object
    sortedList = toMutableList_rSequence_k_(m._this_sortedWith)
    sortWith_rMutableList_Comparator_k_(sortedList, m._comparator)
    return sortedList.iterator_k_()
end function

function sortedBy_lambda_1_create_Function1_k_(_selector as Object) as Object
    this = {}
    this.__type = "sortedBy_lambda_1"
    this.__proto = ["sortedBy_lambda_1", "Comparator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.compare_AnyN_AnyN_k_ = sortedBy_lambda_1_compare_AnyN_AnyN_k_
    this._selector = _selector
    return this
end function

function sortedBy_lambda_1_compare_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
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

function sortedByDescending_lambda_1_create_Function1_k_(_selector as Object) as Object
    this = {}
    this.__type = "sortedByDescending_lambda_1"
    this.__proto = ["sortedByDescending_lambda_1", "Comparator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.compare_AnyN_AnyN_k_ = sortedByDescending_lambda_1_compare_AnyN_AnyN_k_
    this._selector = _selector
    return this
end function

function sortedByDescending_lambda_1_compare_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
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

function Anon_2a369f63_create_Sequence_Function0Sequence_k_(_this_ifEmpty as Object, _defaultValue as Object) as Object
    this = {}
    this.__type = "Anon_2a369f63"
    this.__proto = ["Anon_2a369f63", "Sequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.iterator_k_ = Anon_2a369f63_iterator_k_
    this._this_ifEmpty = _this_ifEmpty
    this._defaultValue = _defaultValue
    return this
end function

function Anon_2a369f63_iterator_k_() as Object
    iterator = m._this_ifEmpty.iterator_k_()
    if iterator.hasNext_k_() then
        return iterator
    else if true then
        return m._defaultValue.invoke_k_().iterator_k_()
    end if
end function
