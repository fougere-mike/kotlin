function Comparator_compare_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
end function

function compareBy_Function1ComparableN_k_(selector as Object) as Object
    return {selector: selector, invoke: function(a as Dynamic, b as Dynamic) as Integer
        return compareValuesBy_AnyN_AnyN_Function1ComparableN_k_(a, b, m.selector)
    end function, compare_AnyN_AnyN_k_: function(a as Dynamic, b as Dynamic) as Integer
        return compareValuesBy_AnyN_AnyN_Function1ComparableN_k_(a, b, m.selector)
    end function}
end function

function compareByDescending_Function1ComparableN_k_(selector as Object) as Object
    return {selector: selector, invoke: function(a as Dynamic, b as Dynamic) as Integer
        return compareValuesBy_AnyN_AnyN_Function1ComparableN_k_(b, a, m.selector)
    end function, compare_AnyN_AnyN_k_: function(a as Dynamic, b as Dynamic) as Integer
        return compareValuesBy_AnyN_AnyN_Function1ComparableN_k_(b, a, m.selector)
    end function}
end function

function compareValuesBy_AnyN_AnyN_Function1ComparableN_k_(a as Dynamic, b as Dynamic, selector as Object) as Integer
    return compareValues_AnyN_AnyN_k_(selector.invoke(a), selector.invoke(b))
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
    return {this: m, invoke: function(a as Dynamic, b as Dynamic) as Integer
        return m.this.compare_AnyN_AnyN_k_(b, a)
    end function, compare_AnyN_AnyN_k_: function(a as Dynamic, b as Dynamic) as Integer
        return m.this.compare_AnyN_AnyN_k_(b, a)
    end function}
end function

function NaturalOrderComparator_create_k_() as Object
    this = {}
    this.__type = "NaturalOrderComparator"
    this.__proto = ["NaturalOrderComparator", "Comparator"]
    this.__id = __kotlin_nextObjectId()
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
