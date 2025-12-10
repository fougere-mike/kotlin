function get__adapter_k_() as Dynamic
    return GetGlobalAA()._adapter
end function

sub set__adapter_FrameworkAdapterN_k_(value as Dynamic)
    m._adapter = value
end sub

function adapter_k_() as Object
    if get__adapter_k_() = invalid then
        set__adapter_FrameworkAdapterN_k_(BareAdapter_create_k_())
    end if
    return get__adapter_k_()
end function

sub suite_Str_Z_Function0V_k_(name as String, ignored = false, suiteFn = invalid)
    if ignored = invalid then
        ignored = false
    end if
    adapter_k_().suite_Str_Z_Function0V_k_(name, ignored, suiteFn)
end sub

sub test_Str_Z_Function0V_k_(name as String, ignored = false, testFn = invalid)
    if ignored = invalid then
        ignored = false
    end if
    adapter_k_().test_Str_Z_Function0V_k_(name, ignored, testFn)
end sub
