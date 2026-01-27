sub set_rMutableMap_AnyN_AnyN_k_(m as Object, key as Dynamic, value as Dynamic)
    m.put_AnyN_AnyN_k_(key, value)
end sub

function getOrElse_rMap_AnyN_Function0_k_(m as Object, key as Dynamic, defaultValue as Object) as Dynamic
    tmp0_elvis_lhs = m.get_AnyN_k_(key)
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp0 = defaultValue.invoke_k_()
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    return __when_tmp0

end function

function getOrPut_rMutableMap_AnyN_Function0_k_(m as Object, key as Dynamic, defaultValue as Object) as Dynamic
    value = m.get_AnyN_k_(key)
    if (value <> invalid) or m.containsKey_AnyN_k_(key) then
        return value
    end if
    answer = defaultValue.invoke_k_()
    m.put_AnyN_AnyN_k_(key, answer)
    return answer
end function

function getOrDefault_rMap_AnyN_AnyN_k_(m as Object, key as Dynamic, defaultValue as Dynamic) as Dynamic
    tmp0_elvis_lhs = m.get_AnyN_k_(key)
    __when_tmp1 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp1 = defaultValue
    else if true then
        __when_tmp1 = tmp0_elvis_lhs
    end if
    return __when_tmp1

end function

function toMap_rMap_k_(m as Object) as Object
    result = HashMap_create_k_()
    __iter_17 = m.__get_entries().iterator_k_()
    while __iter_17.hasNext_k_()
        entry = __iter_17.next_k_()
        result.put_AnyN_AnyN_k_(entry.__get_key(), entry.__get_value())

    end while

    return result
end function

function toMutableMap_rMap_k_(m as Object) as Object
    result = HashMap_create_k_()
    __iter_18 = m.__get_entries().iterator_k_()
    while __iter_18.hasNext_k_()
        entry = __iter_18.next_k_()
        result.put_AnyN_AnyN_k_(entry.__get_key(), entry.__get_value())

    end while

    return result
end function

function plus_rMap_Map_k_(m as Object, map as Object) as Object
    result = HashMap_create_k_()
    __iter_19 = m.__get_entries().iterator_k_()
    while __iter_19.hasNext_k_()
        entry = __iter_19.next_k_()
        result.put_AnyN_AnyN_k_(entry.__get_key(), entry.__get_value())

    end while

    __iter_20 = map.__get_entries().iterator_k_()
    while __iter_20.hasNext_k_()
        entry = __iter_20.next_k_()
        result.put_AnyN_AnyN_k_(entry.__get_key(), entry.__get_value())

    end while

    return result
end function

function plus_rMap_Pair_k_(m as Object, pair as Object) as Object
    result = HashMap_create_k_()
    __iter_21 = m.__get_entries().iterator_k_()
    while __iter_21.hasNext_k_()
        entry = __iter_21.next_k_()
        result.put_AnyN_AnyN_k_(entry.__get_key(), entry.__get_value())

    end while

    result.put_AnyN_AnyN_k_(pair.first, pair.second)
    return result
end function
