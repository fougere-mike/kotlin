function mapOf_PairAnyNAnyN_MapAnyNAnyN_k_(pair as Object) as Object
    return hashMapOf_Arr_HashMapAnyNAnyN_k_([pair])
end function

function buildMapInternal_Function1MutableMapAnyNAnyNV_MapAnyNAnyN_k_(builderAction as Object) as Object
    map = HashMap_create_HashMapAnyNAnyN_k_()
    builderAction.invoke(map)
    return map
end function

function buildMapInternal_I_Function1MutableMapAnyNAnyNV_MapAnyNAnyN_k_(capacity as Integer, builderAction as Object) as Object
    map = HashMap_create_I_HashMapAnyNAnyN_k_(capacity)
    builderAction.invoke(map)
    return map
end function

function mapCapacity_I_I_k_(expectedSize as Integer) as Integer
    return expectedSize
end function
