sub set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(m as Object, key as Dynamic, value as Dynamic)
    m.put_AnyN_AnyN_AnyN_k_(key, value)
end sub

function getOrElse_rMapAnyNAnyN_AnyN_Function0AnyN_AnyN_k_(m as Object, key as Dynamic, defaultValue as Object) as Dynamic
    tmp0_elvis_lhs = m.get_AnyN_AnyN_k_(key)
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp0 = defaultValue.invoke()
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    return __when_tmp0

end function

function getOrPut_rMutableMapAnyNAnyN_AnyN_Function0AnyN_AnyN_k_(m as Object, key as Dynamic, defaultValue as Object) as Dynamic
    value = m.get_AnyN_AnyN_k_(key)
    if (value <> invalid) or m.containsKey_AnyN_Z_k_(key) then
        return value
    end if
    answer = defaultValue.invoke()
    m.put_AnyN_AnyN_AnyN_k_(key, answer)
    return answer
end function

function getOrDefault_rMapAnyNAnyN_AnyN_AnyN_AnyN_k_(m as Object, key as Dynamic, defaultValue as Dynamic) as Dynamic
    tmp0_elvis_lhs = m.get_AnyN_AnyN_k_(key)
    __when_tmp1 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp1 = defaultValue
    else if true then
        __when_tmp1 = tmp0_elvis_lhs
    end if
    return __when_tmp1

end function

function toMap_rMapAnyNAnyN_MapAnyNAnyN_k_(m as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    for each entry in m.get_entries()
        result.put_AnyN_AnyN_AnyN_k_(entry.get_key(), entry.get_value())

    end for
    return result
end function

function toMutableMap_rMapAnyNAnyN_MutableMapAnyNAnyN_k_(m as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    for each entry in m.get_entries()
        result.put_AnyN_AnyN_AnyN_k_(entry.get_key(), entry.get_value())

    end for
    return result
end function

function plus_rMapAnyNAnyN_MapAnyNAnyN_MapAnyNAnyN_k_(m as Object, map as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    for each entry in m.get_entries()
        result.put_AnyN_AnyN_AnyN_k_(entry.get_key(), entry.get_value())

    end for
    for each entry in map.get_entries()
        result.put_AnyN_AnyN_AnyN_k_(entry.get_key(), entry.get_value())

    end for
    return result
end function

function plus_rMapAnyNAnyN_PairAnyNAnyN_MapAnyNAnyN_k_(m as Object, pair as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    for each entry in m.get_entries()
        result.put_AnyN_AnyN_AnyN_k_(entry.get_key(), entry.get_value())

    end for
    result.put_AnyN_AnyN_AnyN_k_(pair.first, pair.second)
    return result
end function
