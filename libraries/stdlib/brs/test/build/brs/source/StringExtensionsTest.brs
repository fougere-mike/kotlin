sub stringExtensionsTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("String Extensions", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("length", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(5, Len("hello"))
            assertEquals_AnyN_AnyN_StrN_k_(0, Len(""))
        end function})
        m.test_Str_Function0V_k_("isEmpty and isNotEmpty", {invoke: function() as Void
            assertTrue_Z_StrN_k_(isEmpty_rStr_k_(""))
            assertFalse_Z_StrN_k_(isEmpty_rStr_k_("hello"))
            assertFalse_Z_StrN_k_(isNotEmpty_rStr_k_(""))
            assertTrue_Z_StrN_k_(isNotEmpty_rStr_k_("hello"))
        end function})
        m.test_Str_Function0V_k_("isBlank and isNotBlank", {invoke: function() as Void
            assertTrue_Z_StrN_k_(isBlank_rStr_k_(""))
            assertTrue_Z_StrN_k_(isBlank_rStr_k_("   "))
            assertFalse_Z_StrN_k_(isBlank_rStr_k_("hello"))
            assertTrue_Z_StrN_k_(isNotBlank_rStr_k_("hello"))
        end function})
        m.test_Str_Function0V_k_("uppercase", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("HELLO", uppercase_rStr_k_("hello"))
            assertEquals_AnyN_AnyN_StrN_k_("HELLO WORLD", uppercase_rStr_k_("Hello World"))
        end function})
        m.test_Str_Function0V_k_("lowercase", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("hello", lowercase_rStr_k_("HELLO"))
            assertEquals_AnyN_AnyN_StrN_k_("hello world", lowercase_rStr_k_("Hello World"))
        end function})
        m.test_Str_Function0V_k_("substring", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("ello", substring_rStr_I_k_("hello", 1))
            assertEquals_AnyN_AnyN_StrN_k_("ell", substring_rStr_I_I_k_("hello", 1, 4))
        end function})
        m.test_Str_Function0V_k_("contains", {invoke: function() as Void
            assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_("hello world", "world"))
            assertFalse_Z_StrN_k_(contains_rStr_Str_Z_k_("hello world", "xyz"))
            assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_("Hello World", "world", true))
        end function})
        m.test_Str_Function0V_k_("startsWith", {invoke: function() as Void
            assertTrue_Z_StrN_k_(startsWith_rStr_Str_Z_k_("hello", "hel"))
            assertFalse_Z_StrN_k_(startsWith_rStr_Str_Z_k_("hello", "xyz"))
            assertTrue_Z_StrN_k_(startsWith_rStr_Str_Z_k_("Hello", "hel", true))
        end function})
        m.test_Str_Function0V_k_("endsWith", {invoke: function() as Void
            assertTrue_Z_StrN_k_(endsWith_rStr_Str_Z_k_("hello", "llo"))
            assertFalse_Z_StrN_k_(endsWith_rStr_Str_Z_k_("hello", "xyz"))
            assertTrue_Z_StrN_k_(endsWith_rStr_Str_Z_k_("Hello", "LLO", true))
        end function})
        m.test_Str_Function0V_k_("indexOf", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(6, indexOf_rStr_Str_I_Z_k_("hello world", "world"))
            assertEquals_AnyN_AnyN_StrN_k_(-1, indexOf_rStr_Str_I_Z_k_("hello", "xyz"))
            assertEquals_AnyN_AnyN_StrN_k_(0, indexOf_rStr_Str_I_Z_k_("hello hello", "hello"))
        end function})
        m.test_Str_Function0V_k_("lastIndexOf", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(6, lastIndexOf_rStr_Str_I_Z_k_("hello hello", "hello"))
            assertEquals_AnyN_AnyN_StrN_k_(-1, lastIndexOf_rStr_Str_I_Z_k_("hello", "xyz"))
        end function})
        m.test_Str_Function0V_k_("padStart", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("  abc", padStart_rStr_I_C_k_("abc", 5))
            assertEquals_AnyN_AnyN_StrN_k_("00abc", padStart_rStr_I_C_k_("abc", 5, "0"))
        end function})
        m.test_Str_Function0V_k_("padEnd", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("abc  ", padEnd_rStr_I_C_k_("abc", 5))
            assertEquals_AnyN_AnyN_StrN_k_("abc00", padEnd_rStr_I_C_k_("abc", 5, "0"))
        end function})
        m.test_Str_Function0V_k_("get char", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("h", "hello".get_I_k_(0))
            assertEquals_AnyN_AnyN_StrN_k_("o", "hello".get_I_k_(4))
        end function})
        m.test_Str_Function0V_k_("equals", {invoke: function() as Void
            assertTrue_Z_StrN_k_("hello".equals("hello"))
            assertFalse_Z_StrN_k_("hello".equals("HELLO"))
            assertTrue_Z_StrN_k_(equals_rStr_StrN_Z_k_("hello", "HELLO", true))
        end function})
        m.test_Str_Function0V_k_("compareTo", {invoke: function() as Void
            assertTrue_Z_StrN_k_("abc".compareTo_Str_k_("xyz") < 0)
            assertTrue_Z_StrN_k_("xyz".compareTo_Str_k_("abc") > 0)
            assertEquals_AnyN_AnyN_StrN_k_(0, "abc".compareTo_Str_k_("abc"))
        end function})
        m.test_Str_Function0V_k_("isNullOrEmpty", {invoke: function() as Void
            assertTrue_Z_StrN_k_(isNullOrEmpty_rCharSequenceN_k_(invalid))
            assertTrue_Z_StrN_k_(isNullOrEmpty_rCharSequenceN_k_(""))
            assertFalse_Z_StrN_k_(isNullOrEmpty_rCharSequenceN_k_("hello"))
        end function})
        m.test_Str_Function0V_k_("repeat", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("abcabcabc", repeat_rStr_I_k_("abc", 3))
            assertEquals_AnyN_AnyN_StrN_k_("", repeat_rStr_I_k_("abc", 0))
        end function})
        m.test_Str_Function0V_k_("lastIndex", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(4, get_lastIndex_rStr_k_("hello"))
            assertEquals_AnyN_AnyN_StrN_k_(-1, get_lastIndex_rStr_k_(""))
        end function})
    end function})
end sub
