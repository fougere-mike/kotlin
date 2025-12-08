sub stringExtensionsTests_rTestRunner_k_(m as Object)
    m.suite("String Extensions", {_this_suite: _this_suite, invoke: function() as Void
        m._this_suite.test("length", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(5, "hello".length)
            assertEquals_AnyN_AnyN_StrN_k_(0, "".length)
        end function})
        m._this_suite.test("isEmpty and isNotEmpty", {invoke: function() as Void
            assertTrue_Z_StrN_k_(isEmpty_rStr_Z_k_(""))
            assertFalse_Z_StrN_k_(isEmpty_rStr_Z_k_("hello"))
            assertFalse_Z_StrN_k_(isNotEmpty_rStr_Z_k_(""))
            assertTrue_Z_StrN_k_(isNotEmpty_rStr_Z_k_("hello"))
        end function})
        m._this_suite.test("isBlank and isNotBlank", {invoke: function() as Void
            assertTrue_Z_StrN_k_(isBlank_rStr_Z_k_(""))
            assertTrue_Z_StrN_k_(isBlank_rStr_Z_k_("   "))
            assertFalse_Z_StrN_k_(isBlank_rStr_Z_k_("hello"))
            assertTrue_Z_StrN_k_(isNotBlank_rStr_Z_k_("hello"))
        end function})
        m._this_suite.test("uppercase", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("HELLO", uppercase_rStr_Str_k_("hello"))
            assertEquals_AnyN_AnyN_StrN_k_("HELLO WORLD", uppercase_rStr_Str_k_("Hello World"))
        end function})
        m._this_suite.test("lowercase", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("hello", lowercase_rStr_Str_k_("HELLO"))
            assertEquals_AnyN_AnyN_StrN_k_("hello world", lowercase_rStr_Str_k_("Hello World"))
        end function})
        m._this_suite.test("substring", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("ello", substring_rStr_I_Str_k_("hello", 1))
            assertEquals_AnyN_AnyN_StrN_k_("ell", substring_rStr_I_I_Str_k_("hello", 1, 4))
        end function})
        m._this_suite.test("contains", {invoke: function() as Void
            assertTrue_Z_StrN_k_(contains_rStr_Str_Z_Z_k_("hello world", "world"))
            assertFalse_Z_StrN_k_(contains_rStr_Str_Z_Z_k_("hello world", "xyz"))
            assertTrue_Z_StrN_k_(contains_rStr_Str_Z_Z_k_("Hello World", "world", true))
        end function})
        m._this_suite.test("startsWith", {invoke: function() as Void
            assertTrue_Z_StrN_k_(startsWith_rStr_Str_Z_Z_k_("hello", "hel"))
            assertFalse_Z_StrN_k_(startsWith_rStr_Str_Z_Z_k_("hello", "xyz"))
            assertTrue_Z_StrN_k_(startsWith_rStr_Str_Z_Z_k_("Hello", "hel", true))
        end function})
        m._this_suite.test("endsWith", {invoke: function() as Void
            assertTrue_Z_StrN_k_(endsWith_rStr_Str_Z_Z_k_("hello", "llo"))
            assertFalse_Z_StrN_k_(endsWith_rStr_Str_Z_Z_k_("hello", "xyz"))
            assertTrue_Z_StrN_k_(endsWith_rStr_Str_Z_Z_k_("Hello", "LLO", true))
        end function})
        m._this_suite.test("indexOf", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(6, indexOf_rStr_Str_I_Z_I_k_("hello world", "world"))
            assertEquals_AnyN_AnyN_StrN_k_(-1, indexOf_rStr_Str_I_Z_I_k_("hello", "xyz"))
            assertEquals_AnyN_AnyN_StrN_k_(0, indexOf_rStr_Str_I_Z_I_k_("hello hello", "hello"))
        end function})
        m._this_suite.test("lastIndexOf", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(6, lastIndexOf_rStr_Str_I_Z_I_k_("hello hello", "hello"))
            assertEquals_AnyN_AnyN_StrN_k_(-1, lastIndexOf_rStr_Str_I_Z_I_k_("hello", "xyz"))
        end function})
        m._this_suite.test("padStart", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("  abc", padStart_rStr_I_C_Str_k_("abc", 5))
            assertEquals_AnyN_AnyN_StrN_k_("00abc", padStart_rStr_I_C_Str_k_("abc", 5, "0"))
        end function})
        m._this_suite.test("padEnd", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("abc  ", padEnd_rStr_I_C_Str_k_("abc", 5))
            assertEquals_AnyN_AnyN_StrN_k_("abc00", padEnd_rStr_I_C_Str_k_("abc", 5, "0"))
        end function})
        m._this_suite.test("get char", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("h", "hello"[0])
            assertEquals_AnyN_AnyN_StrN_k_("o", "hello"[4])
        end function})
        m._this_suite.test("equals", {invoke: function() as Void
            assertTrue_Z_StrN_k_("hello".equals("hello"))
            assertFalse_Z_StrN_k_("hello".equals("HELLO"))
            assertTrue_Z_StrN_k_(equals_rStr_StrN_Z_Z_k_("hello", "HELLO", true))
        end function})
        m._this_suite.test("compareTo", {invoke: function() as Void
            assertTrue_Z_StrN_k_("abc".compareTo("xyz") < 0)
            assertTrue_Z_StrN_k_("xyz".compareTo("abc") > 0)
            assertEquals_AnyN_AnyN_StrN_k_(0, "abc".compareTo("abc"))
        end function})
        m._this_suite.test("isNullOrEmpty", {invoke: function() as Void
            assertTrue_Z_StrN_k_(isNullOrEmpty_rCharSequenceN_Z_k_(invalid))
            assertTrue_Z_StrN_k_(isNullOrEmpty_rCharSequenceN_Z_k_(""))
            assertFalse_Z_StrN_k_(isNullOrEmpty_rCharSequenceN_Z_k_("hello"))
        end function})
        m._this_suite.test("repeat", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_("abcabcabc", repeat_rStr_I_Str_k_("abc", 3))
            assertEquals_AnyN_AnyN_StrN_k_("", repeat_rStr_I_Str_k_("abc", 0))
        end function})
        m._this_suite.test("lastIndex", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(4, get_lastIndex_rStr_I_k_("hello"))
            assertEquals_AnyN_AnyN_StrN_k_(-1, get_lastIndex_rStr_I_k_(""))
        end function})
    end function})
end sub
