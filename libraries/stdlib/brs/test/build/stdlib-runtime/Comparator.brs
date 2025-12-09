function Comparator_compare_AnyN_AnyN_I_k_(a as Dynamic, b as Dynamic) as Integer
end function

function compareBy_Function1AnyNComparableStarN_ComparatorAnyN_k_(selector as Object) as Object
    return {selector: selector, invoke: function(a as Dynamic, b as Dynamic) as Integer
        return compareValuesBy_AnyN_AnyN_Function1AnyNComparableStarN_I_k_(a, b, m.selector)
    end function}
end function

function compareByDescending_Function1AnyNComparableStarN_ComparatorAnyN_k_(selector as Object) as Object
    return {selector: selector, invoke: function(a as Dynamic, b as Dynamic) as Integer
        return compareValuesBy_AnyN_AnyN_Function1AnyNComparableStarN_I_k_(b, a, m.selector)
    end function}
end function

function compareValuesBy_AnyN_AnyN_Function1AnyNComparableStarN_I_k_(a as Dynamic, b as Dynamic, selector as Object) as Integer
    return compareValues_AnyN_AnyN_I_k_(selector.invoke(a), selector.invoke(b))
end function

function compareValues_AnyN_AnyN_I_k_(a as Dynamic, b as Dynamic) as Integer
    if EQEQEQ_AnyN_AnyN_Z_k_(a, b) then
        return 0
    end if
    if a = invalid then
        return -1
    end if
    if b = invalid then
        return 1
    end if
    return a.compareTo_AnyN_I_k_(b)
end function

function naturalOrder_ComparatorAny_k_() as Object
    return NaturalOrderComparator_getInstance()
end function

function reverseOrder_ComparatorAny_k_() as Object
    return ReverseOrderComparator_getInstance()
end function

function reversed_rComparatorAnyN_ComparatorAnyN_k_(m as Object) as Object
    return {this: m, invoke: function(a as Dynamic, b as Dynamic) as Integer
        return m.this.compare_AnyN_AnyN_I_k_(b, a)
    end function}
end function

function NaturalOrderComparator_create_NaturalOrderComparator_k_() as Object
    this = {}
    this.__type = "NaturalOrderComparator"
    this.__proto = ["NaturalOrderComparator"]
    this.compare_ComparableAny_ComparableAny_I_k_ = NaturalOrderComparator_compare_ComparableAny_ComparableAny_I_k_
    return this
end function

function NaturalOrderComparator_getInstance() as Object
    if m.NaturalOrderComparator_instance = invalid then
        m.NaturalOrderComparator_instance = NaturalOrderComparator_create_NaturalOrderComparator_k_()
    end if
    return m.NaturalOrderComparator_instance
end function

function NaturalOrderComparator_compare_ComparableAny_ComparableAny_I_k_(a as Object, b as Object) as Integer
    return a.compareTo_AnyN_I_k_(b)
end function

function ReverseOrderComparator_create_ReverseOrderComparator_k_() as Object
    this = {}
    this.__type = "ReverseOrderComparator"
    this.__proto = ["ReverseOrderComparator"]
    this.compare_ComparableAny_ComparableAny_I_k_ = ReverseOrderComparator_compare_ComparableAny_ComparableAny_I_k_
    return this
end function

function ReverseOrderComparator_getInstance() as Object
    if m.ReverseOrderComparator_instance = invalid then
        m.ReverseOrderComparator_instance = ReverseOrderComparator_create_ReverseOrderComparator_k_()
    end if
    return m.ReverseOrderComparator_instance
end function

function ReverseOrderComparator_compare_ComparableAny_ComparableAny_I_k_(a as Object, b as Object) as Integer
    return b.compareTo_AnyN_I_k_(a)
end function
