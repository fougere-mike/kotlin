function let_rAnyN_Function1AnyNAnyN_AnyN_k_(m as Dynamic, block as Function) as Dynamic
    return block.invoke(m)
end function

function also_rAnyN_Function1AnyNV_AnyN_k_(m as Dynamic, block as Function) as Dynamic
    block.invoke(m)
    return m
end function

function apply_rAnyN_Function1AnyNV_AnyN_k_(m as Dynamic, block as Function) as Dynamic
    block.invoke(m)
    return m
end function

function run_rAnyN_Function1AnyNAnyN_AnyN_k_(m as Dynamic, block as Function) as Dynamic
    return block.invoke(m)
end function

function run_Function0AnyN_AnyN_k_(block as Function) as Dynamic
    return block.invoke()
end function
