sub unsignedTests_rTestRunner_k_(m as Object)
    m.suite("Unsigned Basics", {_this_suite: _this_suite, invoke: function() as Void
        m._this_suite.test("UInt basics", {invoke: function() as Void
            a = UInt_create_I_UInt_k_(1)
            b = UInt_create_I_UInt_k_(2)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(3), a + b)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(0), UInt_Companion_get_MIN_VALUE_UInt_k_())
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(-1), UInt_Companion_get_MAX_VALUE_UInt_k_())
        end function})
        m._this_suite.test("UInt comparison", {invoke: function() as Void
            a = UInt_create_I_UInt_k_(1)
            b = UInt_create_I_UInt_k_(2)
            assertTrue_Z_StrN_k_((a < b) < 0)
            assertTrue_Z_StrN_k_((b > a) > 0)
            assertEquals_AnyN_AnyN_StrN_k_(0, a.compareTo(a))
        end function})
        m._this_suite.test("UInt arithmetic", {invoke: function() as Void
            a = UInt_create_I_UInt_k_(10)
            b = UInt_create_I_UInt_k_(3)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(13), a + b)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(7), a - b)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(30), a * b)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(3), a / b)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(1), a mod b)
        end function})
        m._this_suite.test("UInt conversions", {invoke: function() as Void
            i = -1
            u = toUInt_rI_UInt_k_(i)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_Companion_get_MAX_VALUE_UInt_k_(), u)
            assertEquals_AnyN_AnyN_StrN_k_(-1, u.toULong().data)
        end function})
        m._this_suite.test("ULong basics", {invoke: function() as Void
            a = ULong_create_J_ULong_k_(1&)
            b = ULong_create_J_ULong_k_(2&)
            assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_ULong_k_(3&), a + b)
            assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_ULong_k_(0&), ULong_Companion_get_MIN_VALUE_ULong_k_())
            assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_ULong_k_(-1&), ULong_Companion_get_MAX_VALUE_ULong_k_())
        end function})
        m._this_suite.test("UByte basics", {invoke: function() as Void
            a = UByte_create_B_UByte_k_(1)
            b = UByte_create_B_UByte_k_(2)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(3), a + b)
            assertEquals_AnyN_AnyN_StrN_k_(UByte_create_B_UByte_k_(0), UByte_Companion_get_MIN_VALUE_UByte_k_())
            assertEquals_AnyN_AnyN_StrN_k_(UByte_create_B_UByte_k_(-1), UByte_Companion_get_MAX_VALUE_UByte_k_())
        end function})
        m._this_suite.test("UShort basics", {invoke: function() as Void
            a = UShort_create_S_UShort_k_(1)
            b = UShort_create_S_UShort_k_(2)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(3), a + b)
            assertEquals_AnyN_AnyN_StrN_k_(UShort_create_S_UShort_k_(0), UShort_Companion_get_MIN_VALUE_UShort_k_())
            assertEquals_AnyN_AnyN_StrN_k_(UShort_create_S_UShort_k_(-1), UShort_Companion_get_MAX_VALUE_UShort_k_())
        end function})
    end function})
    m.suite("Unsigned Arrays", {_this_suite: _this_suite, it: it, it: it, it: it, it: it, invoke: function() as Void
        m._this_suite.test("UIntArray", {it: it, invoke: function() as Void
            arr = UIntArray_I_Function1IUInt_UIntArray_k_(3, {invoke: function(it as Integer) as Object
                return UInt_create_I_UInt_k_(m.it + 1)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(3, arr.size)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(1), arr[0])
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(2), arr[1])
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(3), arr[2])
            arr.set(1, UInt_create_I_UInt_k_(10))
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(10), arr[1])
        end function})
        m._this_suite.test("UIntArray creation", {invoke: function() as Void
            arr = uintArrayOf_UIntArray_UIntArray_k_([UInt_create_I_UInt_k_(1), UInt_create_I_UInt_k_(2), UInt_create_I_UInt_k_(3)])
            assertEquals_AnyN_AnyN_StrN_k_(3, arr.size)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(1), arr[0])
        end function})
        m._this_suite.test("ULongArray", {it: it, invoke: function() as Void
            arr = ULongArray_I_Function1IULong_ULongArray_k_(2, {invoke: function(it as Integer) as Object
                return ULong_create_J_ULong_k_(m.it)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(2, arr.size)
            assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_ULong_k_(0&), arr[0])
            assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_ULong_k_(1&), arr[1])
        end function})
        m._this_suite.test("UByteArray", {it: it, invoke: function() as Void
            arr = UByteArray_I_Function1IUByte_UByteArray_k_(2, {invoke: function(it as Integer) as Object
                return UByte_create_B_UByte_k_(m.it)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(2, arr.size)
            assertEquals_AnyN_AnyN_StrN_k_(UByte_create_B_UByte_k_(0), arr[0])
        end function})
        m._this_suite.test("UShortArray", {it: it, invoke: function() as Void
            arr = UShortArray_I_Function1IUShort_UShortArray_k_(2, {invoke: function(it as Integer) as Object
                return UShort_create_S_UShort_k_(m.it)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(2, arr.size)
            assertEquals_AnyN_AnyN_StrN_k_(UShort_create_S_UShort_k_(0), arr[0])
        end function})
    end function})
    m.suite("Unsigned Ranges", {_this_suite: _this_suite, invoke: function() as Void
        m._this_suite.test("UIntRange", {invoke: function() as Void
            range = UInt_create_I_UInt_k_(1).rangeTo(UInt_create_I_UInt_k_(5))
            list = toList_rIterableAnyN_ListAnyN_k_(range)
            assertEquals_AnyN_AnyN_StrN_k_(5, list.size)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(1), list[0])
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(5), list[4])
        end function})
        m._this_suite.test("UIntRange until", {invoke: function() as Void
            range = until_rUInt_UInt_UIntRange_k_(UInt_create_I_UInt_k_(1), UInt_create_I_UInt_k_(5))
            list = toList_rIterableAnyN_ListAnyN_k_(range)
            assertEquals_AnyN_AnyN_StrN_k_(4, list.size)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(1), list[0])
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(4), list[3])
        end function})
        m._this_suite.test("UIntRange downTo", {invoke: function() as Void
            range = downTo_rUInt_UInt_UIntProgression_k_(UInt_create_I_UInt_k_(5), UInt_create_I_UInt_k_(1))
            list = toList_rIterableAnyN_ListAnyN_k_(range)
            assertEquals_AnyN_AnyN_StrN_k_(5, list.size)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(5), list[0])
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(1), list[4])
        end function})
        m._this_suite.test("UIntRange step", {invoke: function() as Void
            range = step_rUIntProgression_I_UIntProgression_k_(UInt_create_I_UInt_k_(1).rangeTo(UInt_create_I_UInt_k_(10)), 2)
            list = toList_rIterableAnyN_ListAnyN_k_(range)
            assertEquals_AnyN_AnyN_StrN_k_(5, list.size)
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(1), list[0])
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(3), list[1])
            assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_UInt_k_(9), list[4])
        end function})
        m._this_suite.test("ULongRange", {invoke: function() as Void
            range = ULong_create_J_ULong_k_(1&).rangeTo(ULong_create_J_ULong_k_(3&))
            list = toList_rIterableAnyN_ListAnyN_k_(range)
            assertEquals_AnyN_AnyN_StrN_k_(3, list.size)
            assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_ULong_k_(1&), list[0])
            assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_ULong_k_(3&), list[2])
        end function})
    end function})
end sub
