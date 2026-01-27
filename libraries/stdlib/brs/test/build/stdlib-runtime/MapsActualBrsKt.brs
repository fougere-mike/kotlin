function mapOf_Pair_k_(pair as Object) as Object
    return hashMapOf_Arr_k_([pair])
end function

function buildMapInternal_Function1MutableMapV_k_(builderAction as Object) as Object
    map = HashMap_create_k_()
    builderAction.invoke_AnyN_k_(map)
    return map
end function

function buildMapInternal_I_Function1MutableMapV_k_(capacity as Integer, builderAction as Object) as Object
    map = HashMap_create_I_k_(capacity)
    builderAction.invoke_AnyN_k_(map)
    return map
end function

function mapCapacity_I_k_(expectedSize as Integer) as Integer
    return expectedSize
end function
