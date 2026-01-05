sub unsignedTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Unsigned Basics", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("UInt basics", {invoke: function() as Void
            a = UInt_create_I_k_(1)
            b = UInt_create_I_k_(2)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(3), a + b, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(0), UInt_Companion_get_MIN_VALUE_k_(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(-1), UInt_Companion_get_MAX_VALUE_k_(), invalid)
        end function})
        m.test_Str_Function0V_k_("UInt comparison", {invoke: function() as Void
            a = UInt_create_I_k_(1)
            b = UInt_create_I_k_(2)
            assertTrue_Z_StrN_k_(a.compareTo_UInt_k_(b) < 0, invalid)
            assertTrue_Z_StrN_k_(b.compareTo_UInt_k_(a) > 0, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(0, a.compareTo_UInt_k_(a), invalid)
        end function})
        m.test_Str_Function0V_k_("UInt arithmetic", {invoke: function() as Void
            a = UInt_create_I_k_(10)
            b = UInt_create_I_k_(3)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(13), a + b, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(7), a - b, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(30), a * b, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(3), a / b, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(1), a mod b, invalid)
        end function})
        m.test_Str_Function0V_k_("UInt conversions", {invoke: function() as Void
            i = -1
            u = toUInt_rI_k_(i)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_Companion_get_MAX_VALUE_k_(), u, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(-1, u.toULong_k_().get_data(), invalid)
        end function})
        m.test_Str_Function0V_k_("ULong basics", {invoke: function() as Void
            a = ULong_create_J_k_(1&)
            b = ULong_create_J_k_(2&)
            assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_k_(3&), a + b, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_k_(0&), ULong_Companion_get_MIN_VALUE_k_(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_k_(-1&), ULong_Companion_get_MAX_VALUE_k_(), invalid)
        end function})
        m.test_Str_Function0V_k_("UByte basics", {invoke: function() as Void
            a = UByte_create_B_k_(1)
            b = UByte_create_B_k_(2)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(3), a + b, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UByte_create_B_k_(0), UByte_Companion_get_MIN_VALUE_k_(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UByte_create_B_k_(-1), UByte_Companion_get_MAX_VALUE_k_(), invalid)
        end function})
        m.test_Str_Function0V_k_("UShort basics", {invoke: function() as Void
            a = UShort_create_S_k_(1)
            b = UShort_create_S_k_(2)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(3), a + b, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UShort_create_S_k_(0), UShort_Companion_get_MIN_VALUE_k_(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UShort_create_S_k_(-1), UShort_Companion_get_MAX_VALUE_k_(), invalid)
        end function})
    end function})
    m.suite_Str_Function1TestRunnerV_k_("Unsigned Arrays", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("UIntArray", {invoke: function() as Void
            arr = UIntArray_I_Function1IUInt_k_(3, {invoke: function(it as Integer) as Object
                return UInt_create_I_k_(it + 1)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(3, arr.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(1), arr.get_I_k_(0), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(2), arr.get_I_k_(1), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(3), arr.get_I_k_(2), invalid)
            arr.set_I_UInt_k_(1, UInt_create_I_k_(10))
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(10), arr.get_I_k_(1), invalid)
        end function})
        m.test_Str_Function0V_k_("UIntArray creation", {invoke: function() as Void
            arr = uintArrayOf_UIntArray_k_([UInt_create_I_k_(1), UInt_create_I_k_(2), UInt_create_I_k_(3)])
            assertEquals_AnyN_AnyN_StrN_k_(3, arr.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(1), arr.get_I_k_(0), invalid)
        end function})
        m.test_Str_Function0V_k_("ULongArray", {invoke: function() as Void
            arr = ULongArray_I_Function1IULong_k_(2, {invoke: function(it as Integer) as Object
                return ULong_create_J_k_(it)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(2, arr.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_k_(0&), arr.get_I_k_(0), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_k_(1&), arr.get_I_k_(1), invalid)
        end function})
        m.test_Str_Function0V_k_("UByteArray", {invoke: function() as Void
            arr = UByteArray_I_Function1IUByte_k_(2, {invoke: function(it as Integer) as Object
                return UByte_create_B_k_(it)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(2, arr.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UByte_create_B_k_(0), arr.get_I_k_(0), invalid)
        end function})
        m.test_Str_Function0V_k_("UShortArray", {invoke: function() as Void
            arr = UShortArray_I_Function1IUShort_k_(2, {invoke: function(it as Integer) as Object
                return UShort_create_S_k_(it)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(2, arr.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UShort_create_S_k_(0), arr.get_I_k_(0), invalid)
        end function})
    end function})
    m.suite_Str_Function1TestRunnerV_k_("Unsigned Ranges", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("UIntRange", {invoke: function() as Void
            range = UInt_create_I_k_(1).rangeTo_UInt_k_(UInt_create_I_k_(5))
            list = toList_rIterable_k_(range)
            assertEquals_AnyN_AnyN_StrN_k_(5, list.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(1), list.get_I_k_(0), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(5), list.get_I_k_(4), invalid)
        end function})
        m.test_Str_Function0V_k_("UIntRange until", {invoke: function() as Void
            range = until_rUInt_UInt_k_(UInt_create_I_k_(1), UInt_create_I_k_(5))
            list = toList_rIterable_k_(range)
            assertEquals_AnyN_AnyN_StrN_k_(4, list.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(1), list.get_I_k_(0), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(4), list.get_I_k_(3), invalid)
        end function})
        m.test_Str_Function0V_k_("UIntRange downTo", {invoke: function() as Void
            range = downTo_rUInt_UInt_k_(UInt_create_I_k_(5), UInt_create_I_k_(1))
            list = toList_rIterable_k_(range)
            assertEquals_AnyN_AnyN_StrN_k_(5, list.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(5), list.get_I_k_(0), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(1), list.get_I_k_(4), invalid)
        end function})
        m.test_Str_Function0V_k_("UIntRange step", {invoke: function() as Void
            range = step_rUIntProgression_I_k_(UInt_create_I_k_(1).rangeTo_UInt_k_(UInt_create_I_k_(10)), 2)
            list = toList_rIterable_k_(range)
            assertEquals_AnyN_AnyN_StrN_k_(5, list.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(1), list.get_I_k_(0), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(3), list.get_I_k_(1), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(9), list.get_I_k_(4), invalid)
        end function})
        m.test_Str_Function0V_k_("ULongRange", {invoke: function() as Void
            range = ULong_create_J_k_(1&).rangeTo_ULong_k_(ULong_create_J_k_(3&))
            list = toList_rIterable_k_(range)
            assertEquals_AnyN_AnyN_StrN_k_(3, list.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_k_(1&), list.get_I_k_(0), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_k_(3&), list.get_I_k_(2), invalid)
        end function})
    end function})
end sub
