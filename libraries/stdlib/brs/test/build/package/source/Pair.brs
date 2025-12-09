function Pair_create_AnyN_AnyN_PairAnyNAnyN_k_(first as Dynamic, second as Dynamic) as Object
    this = {}
    this.__type = "Pair"
    this.__proto = ["Pair"]
    this.first = first
    this.second = second
    this.equals = Pair_equals
    this.hashCode = Pair_hashCode
    this.toString = Pair_toString
    this.copy = Pair_copy
    this.component1 = Pair_component1
    this.component2 = Pair_component2
    return this
end function

function Pair_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "Pair" then
        return false
    end if
    if m.first <> other.first then
        return false
    end if
    if m.second <> other.second then
        return false
    end if
    return true
end function

function Pair_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.first)
    result = ((result * 31) + m.second)
    return result
end function

function Pair_toString() as String
    return ((("Pair(first=" + m.first) + ", second=") + m.second) + ")"
end function

function Pair_copy(first = invalid, second = invalid) as Object
    if first = invalid then
        first = m.first
    end if
    if second = invalid then
        second = m.second
    end if
    return Pair_create_AnyN_AnyN_PairAnyNAnyN_k_(first, second)
end function

function Pair_component1() as Dynamic
    return m.first
end function

function Pair_component2() as Dynamic
    return m.second
end function

function to_rAnyN_AnyN_PairAnyNAnyN_k_(m as Dynamic, that as Dynamic) as Object
    return Pair_create_AnyN_AnyN_PairAnyNAnyN_k_(m, that)
end function
