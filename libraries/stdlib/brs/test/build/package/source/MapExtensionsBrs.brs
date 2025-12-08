sub set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(m as Object, key as Dynamic, value as Dynamic)
    m.put(key, value)
end sub

function getOrElse_rMapAnyNAnyN_AnyN_Function0AnyN_AnyN_k_(m as Object, key as Dynamic, defaultValue as Function) as Dynamic
    tmp0_elvis_lhs = m.get(key)
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp0 = defaultValue.invoke()
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    return __when_tmp0

end function

function getOrPut_rMutableMapAnyNAnyN_AnyN_Function0AnyN_AnyN_k_(m as Object, key as Dynamic, defaultValue as Function) as Dynamic
    value = m.get(key)
    __when_tmp1 = invalid
    if (value = invalid) and m.containsKey(key).not() then
        __when_tmp1 = answer
    else if true then
        __when_tmp1 = value
    end if
    return __when_tmp1

end function

function getOrDefault_rMapAnyNAnyN_AnyN_AnyN_AnyN_k_(m as Object, key as Dynamic, defaultValue as Dynamic) as Dynamic
    tmp0_elvis_lhs = m.get(key)
    __when_tmp2 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp2 = defaultValue
    else if true then
        __when_tmp2 = tmp0_elvis_lhs
    end if
    return __when_tmp2

end function

function toMap_rMapAnyNAnyN_MapAnyNAnyN_k_(m as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    for each entry in m.entries
        result.put(entry.key, entry.value)

    end for
    return result
end function

function toMutableMap_rMapAnyNAnyN_MutableMapAnyNAnyN_k_(m as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    for each entry in m.entries
        result.put(entry.key, entry.value)

    end for
    return result
end function

function plus_rMapAnyNAnyN_MapAnyNAnyN_MapAnyNAnyN_k_(m as Object, map as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    for each entry in m.entries
        result.put(entry.key, entry.value)

    end for
    for each entry in map.entries
        result.put(entry.key, entry.value)

    end for
    return result
end function

function plus_rMapAnyNAnyN_PairAnyNAnyN_MapAnyNAnyN_k_(m as Object, pair as Object) as Object
    result = HashMap_create_HashMapAnyNAnyN_k_()
    for each entry in m.entries
        result.put(entry.key, entry.value)

    end for
    result.put(pair.first, pair.second)
    return result
end function
