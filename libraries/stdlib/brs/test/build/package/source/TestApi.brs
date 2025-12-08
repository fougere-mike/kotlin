function get__adapter_FrameworkAdapterN_k_() as Dynamic
    return GetGlobalAA()._adapter
end function

sub set__adapter_FrameworkAdapterN_k_(value as Dynamic)
    m._adapter = value
end sub

function adapter_FrameworkAdapter_k_() as Object
    if get__adapter_FrameworkAdapterN_k_() = invalid then
        set__adapter_FrameworkAdapterN_k_(BareAdapter_create_BareAdapter_k_())
    end if
    return CHECK_NOT_NULL_AnyN_Any_k_(get__adapter_FrameworkAdapterN_k_())
end function

sub suite_Str_Z_Function0V_k_(name as String, ignored = false, suiteFn = invalid)
    adapter_FrameworkAdapter_k_().suite(name, ignored, suiteFn)
end sub

sub test_Str_Z_Function0V_k_(name as String, ignored = false, testFn = invalid)
    adapter_FrameworkAdapter_k_().test(name, ignored, testFn)
end sub
