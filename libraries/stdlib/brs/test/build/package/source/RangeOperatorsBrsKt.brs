function rangeTo_rI_I_k_(m as Integer, other as Integer) as Object
    return IntRange_create_I_I_k_(m, other)
end function

function rangeTo_rI_J_k_(m as Integer, other as LongInteger) as Object
    return LongRange_create_J_J_k_(m, other)
end function

function downTo_rI_I_k_(m as Integer, to_ as Integer) as Object
    return IntProgression_Companion_getInstance().fromClosedRange_I_I_I_k_(m, to_, -1)
end function

function downTo_rI_J_k_(m as Integer, to_ as LongInteger) as Object
    return LongProgression_Companion_getInstance().fromClosedRange_J_J_J_k_(m, to_, -1&)
end function

function step_rIntProgression_I_k_(m as Object, step_ as Integer) as Object
    checkStepIsPositive_Z_Number_k_(step_ > 0, step_)
    __when_tmp0 = invalid
    if m.__get_step() > 0 then
        __when_tmp0 = step_
    else if true then
        __when_tmp0 = -step_
    end if
    return IntProgression_Companion_getInstance().fromClosedRange_I_I_I_k_(m.__get_first(), m.__get_last(), __when_tmp0)

end function

function rangeTo_rJ_J_k_(m as LongInteger, other as LongInteger) as Object
    return LongRange_create_J_J_k_(m, other)
end function

function downTo_rJ_I_k_(m as LongInteger, to_ as Integer) as Object
    return LongProgression_Companion_getInstance().fromClosedRange_J_J_J_k_(m, to_, -1&)
end function

function downTo_rJ_J_k_(m as LongInteger, to_ as LongInteger) as Object
    return LongProgression_Companion_getInstance().fromClosedRange_J_J_J_k_(m, to_, -1&)
end function

function step_rLongProgression_J_k_(m as Object, step_ as LongInteger) as Object
    checkStepIsPositive_Z_Number_k_(step_ > 0, step_)
    __when_tmp1 = invalid
    if m.__get_step() > 0 then
        __when_tmp1 = step_
    else if true then
        __when_tmp1 = -step_
    end if
    return LongProgression_Companion_getInstance().fromClosedRange_J_J_J_k_(m.__get_first(), m.__get_last(), __when_tmp1)

end function

function rangeTo_rC_C_k_(m as Object, other as Object) as Object
    return CharRange_create_C_C_k_(m, other)
end function

function downTo_rC_C_k_(m as Object, to_ as Object) as Object
    return CharProgression_Companion_getInstance().fromClosedRange_C_C_I_k_(m, to_, -1)
end function

function step_rCharProgression_I_k_(m as Object, step_ as Integer) as Object
    checkStepIsPositive_Z_Number_k_(step_ > 0, step_)
    __when_tmp2 = invalid
    if m.__get_step() > 0 then
        __when_tmp2 = step_
    else if true then
        __when_tmp2 = -step_
    end if
    return CharProgression_Companion_getInstance().fromClosedRange_C_C_I_k_(m.__get_first(), m.__get_last(), __when_tmp2)

end function
