sub stringExtensionsTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("String Extensions", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("length", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(5, Len("hello"), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(0, Len(""), invalid)
        end function})
        m.test_Str_Function0V_k_("isEmpty and isNotEmpty", {invoke: function() as Void
            assertTrue_Z_StrN_k_(isEmpty_rStr_k_(""), invalid)
            assertFalse_Z_StrN_k_(isEmpty_rStr_k_("hello"), invalid)
            assertFalse_Z_StrN_k_(isNotEmpty_rStr_k_(""), invalid)
            assertTrue_Z_StrN_k_(isNotEmpty_rStr_k_("hello"), invalid)
        end function})
        m.test_Str_Function0V_k_("isBlank and isNotBlank", {invoke: function() as Void
            assertTrue_Z_StrN_k_(isBlank_rStr_k_(""), invalid)
            assertTrue_Z_StrN_k_(isBlank_rStr_k_("   "), invalid)
            assertFalse_Z_StrN_k_(isBlank_rStr_k_("hello"), invalid)
            assertTrue_Z_StrN_k_(isNotBlank_rStr_k_("hello"), invalid)
        end function})
        m.test_Str_Function0V_k_("uppercase", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("HELLO", uppercase_rStr_k_("hello"), invalid)
            assertEquals_AnyN_AnyN_StrN_k_("HELLO WORLD", uppercase_rStr_k_("Hello World"), invalid)
        end function})
        m.test_Str_Function0V_k_("lowercase", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("hello", lowercase_rStr_k_("HELLO"), invalid)
            assertEquals_AnyN_AnyN_StrN_k_("hello world", lowercase_rStr_k_("Hello World"), invalid)
        end function})
        m.test_Str_Function0V_k_("substring", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("ello", substring_rStr_I_k_("hello", 1), invalid)
            assertEquals_AnyN_AnyN_StrN_k_("ell", substring_rStr_I_I_k_("hello", 1, 4), invalid)
        end function})
        m.test_Str_Function0V_k_("contains", {invoke: function() as Void
            assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_("hello world", "world", invalid), invalid)
            assertFalse_Z_StrN_k_(contains_rStr_Str_Z_k_("hello world", "xyz", invalid), invalid)
            assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_("Hello World", "world", true), invalid)
        end function})
        m.test_Str_Function0V_k_("startsWith", {invoke: function() as Void
            assertTrue_Z_StrN_k_(startsWith_rStr_Str_Z_k_("hello", "hel", invalid), invalid)
            assertFalse_Z_StrN_k_(startsWith_rStr_Str_Z_k_("hello", "xyz", invalid), invalid)
            assertTrue_Z_StrN_k_(startsWith_rStr_Str_Z_k_("Hello", "hel", true), invalid)
        end function})
        m.test_Str_Function0V_k_("endsWith", {invoke: function() as Void
            assertTrue_Z_StrN_k_(endsWith_rStr_Str_Z_k_("hello", "llo", invalid), invalid)
            assertFalse_Z_StrN_k_(endsWith_rStr_Str_Z_k_("hello", "xyz", invalid), invalid)
            assertTrue_Z_StrN_k_(endsWith_rStr_Str_Z_k_("Hello", "LLO", true), invalid)
        end function})
        m.test_Str_Function0V_k_("indexOf", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(6, indexOf_rStr_Str_I_Z_k_("hello world", "world", invalid, invalid), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(-1, indexOf_rStr_Str_I_Z_k_("hello", "xyz", invalid, invalid), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(0, indexOf_rStr_Str_I_Z_k_("hello hello", "hello", invalid, invalid), invalid)
        end function})
        m.test_Str_Function0V_k_("lastIndexOf", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(6, lastIndexOf_rStr_Str_I_Z_k_("hello hello", "hello", invalid, invalid), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(-1, lastIndexOf_rStr_Str_I_Z_k_("hello", "xyz", invalid, invalid), invalid)
        end function})
        m.test_Str_Function0V_k_("padStart", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("  abc", padStart_rStr_I_C_k_("abc", 5, invalid), invalid)
            assertEquals_AnyN_AnyN_StrN_k_("00abc", padStart_rStr_I_C_k_("abc", 5, "0"), invalid)
        end function})
        m.test_Str_Function0V_k_("padEnd", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("abc  ", padEnd_rStr_I_C_k_("abc", 5, invalid), invalid)
            assertEquals_AnyN_AnyN_StrN_k_("abc00", padEnd_rStr_I_C_k_("abc", 5, "0"), invalid)
        end function})
        m.test_Str_Function0V_k_("get char", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("h", "hello".get_I_k_(0), invalid)
            assertEquals_AnyN_AnyN_StrN_k_("o", "hello".get_I_k_(4), invalid)
        end function})
        m.test_Str_Function0V_k_("equals", {invoke: function() as Void
            assertTrue_Z_StrN_k_("hello".equals("hello"), invalid)
            assertFalse_Z_StrN_k_("hello".equals("HELLO"), invalid)
            assertTrue_Z_StrN_k_(equals_rStr_StrN_Z_k_("hello", "HELLO", true), invalid)
        end function})
        m.test_Str_Function0V_k_("compareTo", {invoke: function() as Void
            assertTrue_Z_StrN_k_("abc".compareTo_Str_k_("xyz") < 0, invalid)
            assertTrue_Z_StrN_k_("xyz".compareTo_Str_k_("abc") > 0, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(0, "abc".compareTo_Str_k_("abc"), invalid)
        end function})
        m.test_Str_Function0V_k_("isNullOrEmpty", {invoke: function() as Void
            assertTrue_Z_StrN_k_(isNullOrEmpty_rCharSequenceN_k_(invalid), invalid)
            assertTrue_Z_StrN_k_(isNullOrEmpty_rCharSequenceN_k_(""), invalid)
            assertFalse_Z_StrN_k_(isNullOrEmpty_rCharSequenceN_k_("hello"), invalid)
        end function})
        m.test_Str_Function0V_k_("repeat", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("abcabcabc", repeat_rStr_I_k_("abc", 3), invalid)
            assertEquals_AnyN_AnyN_StrN_k_("", repeat_rStr_I_k_("abc", 0), invalid)
        end function})
        m.test_Str_Function0V_k_("lastIndex", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(4, get_lastIndex_rStr_k_("hello"), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(-1, get_lastIndex_rStr_k_(""), invalid)
        end function})
    end function})
end sub
