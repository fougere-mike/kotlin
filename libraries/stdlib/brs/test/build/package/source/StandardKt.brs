function let_rAnyN_Function1_k_(m as Dynamic, block as Object) as Dynamic
    return block.invoke_AnyN_k_(m)
end function

function also_rAnyN_Function1V_k_(m as Dynamic, block as Object) as Dynamic
    block.invoke_AnyN_k_(m)
    return m
end function

function apply_rAnyN_Function1V_k_(m as Dynamic, block as Object) as Dynamic
    block.invoke_AnyN_k_(m)
    return m
end function

function run_rAnyN_Function1_k_(m as Dynamic, block as Object) as Dynamic
    return block.invoke_AnyN_k_(m)
end function

function run_Function0_k_(block as Object) as Dynamic
    return block.invoke_k_()
end function
