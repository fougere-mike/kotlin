function Comparator_compare_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
end function

function compareBy_Function1ComparableN_k_(selector as Object) as Object
    return compareBy_lambda_create_Function1ComparableN_k_(selector)
end function

function compareByDescending_Function1ComparableN_k_(selector as Object) as Object
    return compareByDescending_lambda_create_Function1ComparableN_k_(selector)
end function

function compareValuesBy_AnyN_AnyN_Function1ComparableN_k_(a as Dynamic, b as Dynamic, selector as Object) as Integer
    return compareValues_AnyN_AnyN_k_(selector.invoke_AnyN_k_(a), selector.invoke_AnyN_k_(b))
end function

function compareValues_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
    return brsCompareTo_AnyN_AnyN_k_(a, b)
end function

function naturalOrder_k_() as Object
    return NaturalOrderComparator_getInstance()
end function

function reverseOrder_k_() as Object
    return ReverseOrderComparator_getInstance()
end function

function reversed_rComparator_k_(m as Object) as Object
    return reversed_lambda_create_Comparator_k_(m)
end function

function NaturalOrderComparator_create_k_() as Object
    this = {}
    this.__type = "NaturalOrderComparator"
    this.__proto = ["NaturalOrderComparator", "Comparator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.compare_AnyN_AnyN_k_ = NaturalOrderComparator_compare_AnyN_AnyN_k_
    return this
end function

function NaturalOrderComparator_getInstance() as Object
    if GetGlobalAA().NaturalOrderComparator_instance = invalid then
        GetGlobalAA().NaturalOrderComparator_instance = NaturalOrderComparator_create_k_()
    end if
    return GetGlobalAA().NaturalOrderComparator_instance
end function

function NaturalOrderComparator_compare_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
    return brsCompareTo_AnyN_AnyN_k_(a, b)
end function

function ReverseOrderComparator_create_k_() as Object
    this = {}
    this.__type = "ReverseOrderComparator"
    this.__proto = ["ReverseOrderComparator", "Comparator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.compare_AnyN_AnyN_k_ = ReverseOrderComparator_compare_AnyN_AnyN_k_
    return this
end function

function ReverseOrderComparator_getInstance() as Object
    if GetGlobalAA().ReverseOrderComparator_instance = invalid then
        GetGlobalAA().ReverseOrderComparator_instance = ReverseOrderComparator_create_k_()
    end if
    return GetGlobalAA().ReverseOrderComparator_instance
end function

function ReverseOrderComparator_compare_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
    return brsCompareTo_AnyN_AnyN_k_(b, a)
end function

function compareBy_lambda_create_Function1ComparableN_k_(_selector as Object) as Object
    this = {}
    this.__type = "compareBy_lambda"
    this.__proto = ["compareBy_lambda", "Comparator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.compare_AnyN_AnyN_k_ = compareBy_lambda_compare_AnyN_AnyN_k_
    this._selector = _selector
    return this
end function

function compareBy_lambda_compare_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
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

function compareByDescending_lambda_create_Function1ComparableN_k_(_selector as Object) as Object
    this = {}
    this.__type = "compareByDescending_lambda"
    this.__proto = ["compareByDescending_lambda", "Comparator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.compare_AnyN_AnyN_k_ = compareByDescending_lambda_compare_AnyN_AnyN_k_
    this._selector = _selector
    return this
end function

function compareByDescending_lambda_compare_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
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

function reversed_lambda_create_Comparator_k_(_this_reversed as Object) as Object
    this = {}
    this.__type = "reversed_lambda"
    this.__proto = ["reversed_lambda", "Comparator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.compare_AnyN_AnyN_k_ = reversed_lambda_compare_AnyN_AnyN_k_
    this._this_reversed = _this_reversed
    return this
end function

function reversed_lambda_compare_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
    return m._this_reversed.compare_AnyN_AnyN_k_(b, a)
end function
