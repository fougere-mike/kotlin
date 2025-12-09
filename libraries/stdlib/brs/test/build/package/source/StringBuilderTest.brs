sub stringBuilderTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("StringBuilder", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("create empty", {invoke: function() as Void
            sb = StringBuilder_create_StringBuilder_k_()
            assertEquals_AnyN_AnyN_StrN_k_(0, sb.get_length())
            assertEquals_AnyN_AnyN_StrN_k_("", sb.toString())
        end function})
        m.test_Str_Function0V_k_("create with initial content", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("hello")
            assertEquals_AnyN_AnyN_StrN_k_(5, sb.get_length())
            assertEquals_AnyN_AnyN_StrN_k_("hello", sb.toString())
        end function})
        m.test_Str_Function0V_k_("append string", {invoke: function() as Void
            sb = StringBuilder_create_StringBuilder_k_()
            sb.append_StrN_StringBuilder_k_("hello")
            sb.append_StrN_StringBuilder_k_(" ")
            sb.append_StrN_StringBuilder_k_("world")
            assertEquals_AnyN_AnyN_StrN_k_("hello world", sb.toString())
        end function})
        m.test_Str_Function0V_k_("append various types", {invoke: function() as Void
            sb = StringBuilder_create_StringBuilder_k_()
            sb.append_I_StringBuilder_k_(42)
            sb.append_Z_StringBuilder_k_(true)
            sb.append_C_StringBuilder_k_("X")
            assertEquals_AnyN_AnyN_StrN_k_("42trueX", sb.toString())
        end function})
        m.test_Str_Function0V_k_("append chaining", {invoke: function() as Void
            result = StringBuilder_create_StringBuilder_k_().append_StrN_StringBuilder_k_("a").append_StrN_StringBuilder_k_("b").append_StrN_StringBuilder_k_("c").toString()
            assertEquals_AnyN_AnyN_StrN_k_("abc", result)
        end function})
        m.test_Str_Function0V_k_("insert", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("hello world")
            sb.insert_I_StrN_StringBuilder_k_(6, "beautiful ")
            assertEquals_AnyN_AnyN_StrN_k_("hello beautiful world", sb.toString())
        end function})
        m.test_Str_Function0V_k_("insert at beginning", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("world")
            sb.insert_I_StrN_StringBuilder_k_(0, "hello ")
            assertEquals_AnyN_AnyN_StrN_k_("hello world", sb.toString())
        end function})
        m.test_Str_Function0V_k_("deleteAt", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("hello")
            sb.deleteAt_I_StringBuilder_k_(2)
            assertEquals_AnyN_AnyN_StrN_k_("helo", sb.toString())
        end function})
        m.test_Str_Function0V_k_("charAt", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("hello")
            assertEquals_AnyN_AnyN_StrN_k_("h", sb.get_I_C_k_(0))
            assertEquals_AnyN_AnyN_StrN_k_("e", sb.get_I_C_k_(1))
            assertEquals_AnyN_AnyN_StrN_k_("o", sb.get_I_C_k_(4))
        end function})
        m.test_Str_Function0V_k_("substring", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("hello world")
            assertEquals_AnyN_AnyN_StrN_k_("world", sb.substring_I_Str_k_(6))
            assertEquals_AnyN_AnyN_StrN_k_("ello", sb.substring_I_I_Str_k_(1, 5))
        end function})
        m.test_Str_Function0V_k_("reverse", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("hello")
            sb.reverse_StringBuilder_k_()
            assertEquals_AnyN_AnyN_StrN_k_("olleh", sb.toString())
        end function})
        m.test_Str_Function0V_k_("clear", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("hello")
            sb.clear_StringBuilder_k_()
            assertEquals_AnyN_AnyN_StrN_k_(0, sb.get_length())
            assertEquals_AnyN_AnyN_StrN_k_("", sb.toString())
        end function})
        m.test_Str_Function0V_k_("isEmpty", {invoke: function() as Void
            sb = StringBuilder_create_StringBuilder_k_()
            assertTrue_Z_StrN_k_(isEmpty_rCharSequence_Z_k_(sb))
            sb.append_StrN_StringBuilder_k_("x")
            assertFalse_Z_StrN_k_(isEmpty_rCharSequence_Z_k_(sb))
        end function})
        m.test_Str_Function0V_k_("isNotEmpty", {invoke: function() as Void
            sb = StringBuilder_create_StringBuilder_k_()
            assertFalse_Z_StrN_k_(isNotEmpty_rCharSequence_Z_k_(sb))
            sb.append_StrN_StringBuilder_k_("x")
            assertTrue_Z_StrN_k_(isNotEmpty_rCharSequence_Z_k_(sb))
        end function})
        m.test_Str_Function0V_k_("appendLine", {invoke: function() as Void
            sb = StringBuilder_create_StringBuilder_k_()
            sb.appendLine_StrN_StringBuilder_k_("hello")
            sb.appendLine_StrN_StringBuilder_k_("world")
            assertTrue_Z_StrN_k_(contains_rStr_Str_Z_Z_k_(sb.toString(), "hello"))
            assertTrue_Z_StrN_k_(contains_rStr_Str_Z_Z_k_(sb.toString(), "world"))
        end function})
    end function})
end sub
