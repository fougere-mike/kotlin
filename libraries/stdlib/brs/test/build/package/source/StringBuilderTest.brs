sub stringBuilderTests_rTestRunner_k_(m as Object)
    m.suite("StringBuilder", {_this_suite: _this_suite, invoke: function() as Void
        m._this_suite.test("create empty", {invoke: function() as Void
            sb = StringBuilder_create_StringBuilder_k_()
            assertEquals_AnyN_AnyN_StrN_k_(0, sb.length)
            assertEquals_AnyN_AnyN_StrN_k_("", sb.toString())
        end function})
        m._this_suite.test("create with initial content", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("hello")
            assertEquals_AnyN_AnyN_StrN_k_(5, sb.length)
            assertEquals_AnyN_AnyN_StrN_k_("hello", sb.toString())
        end function})
        m._this_suite.test("append string", {invoke: function() as Void
            sb = StringBuilder_create_StringBuilder_k_()
            sb.append("hello")
            sb.append(" ")
            sb.append("world")
            assertEquals_AnyN_AnyN_StrN_k_("hello world", sb.toString())
        end function})
        m._this_suite.test("append various types", {invoke: function() as Void
            sb = StringBuilder_create_StringBuilder_k_()
            sb.append(42)
            sb.append(true)
            sb.append("X")
            assertEquals_AnyN_AnyN_StrN_k_("42trueX", sb.toString())
        end function})
        m._this_suite.test("append chaining", {invoke: function() as Void
            result = StringBuilder_create_StringBuilder_k_().append("a").append("b").append("c").toString()
            assertEquals_AnyN_AnyN_StrN_k_("abc", result)
        end function})
        m._this_suite.test("insert", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("hello world")
            sb.insert(6, "beautiful ")
            assertEquals_AnyN_AnyN_StrN_k_("hello beautiful world", sb.toString())
        end function})
        m._this_suite.test("insert at beginning", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("world")
            sb.insert(0, "hello ")
            assertEquals_AnyN_AnyN_StrN_k_("hello world", sb.toString())
        end function})
        m._this_suite.test("deleteAt", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("hello")
            sb.deleteAt(2)
            assertEquals_AnyN_AnyN_StrN_k_("helo", sb.toString())
        end function})
        m._this_suite.test("charAt", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("hello")
            assertEquals_AnyN_AnyN_StrN_k_("h", sb[0])
            assertEquals_AnyN_AnyN_StrN_k_("e", sb[1])
            assertEquals_AnyN_AnyN_StrN_k_("o", sb[4])
        end function})
        m._this_suite.test("substring", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("hello world")
            assertEquals_AnyN_AnyN_StrN_k_("world", sb.substring(6))
            assertEquals_AnyN_AnyN_StrN_k_("ello", sb.substring(1, 5))
        end function})
        m._this_suite.test("reverse", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("hello")
            sb.reverse()
            assertEquals_AnyN_AnyN_StrN_k_("olleh", sb.toString())
        end function})
        m._this_suite.test("clear", {invoke: function() as Void
            sb = StringBuilder_create_Str_StringBuilder_k_("hello")
            sb.clear()
            assertEquals_AnyN_AnyN_StrN_k_(0, sb.length)
            assertEquals_AnyN_AnyN_StrN_k_("", sb.toString())
        end function})
        m._this_suite.test("isEmpty", {invoke: function() as Void
            sb = StringBuilder_create_StringBuilder_k_()
            assertTrue_Z_StrN_k_(isEmpty_rCharSequence_Z_k_(sb))
            sb.append("x")
            assertFalse_Z_StrN_k_(isEmpty_rCharSequence_Z_k_(sb))
        end function})
        m._this_suite.test("isNotEmpty", {invoke: function() as Void
            sb = StringBuilder_create_StringBuilder_k_()
            assertFalse_Z_StrN_k_(isNotEmpty_rCharSequence_Z_k_(sb))
            sb.append("x")
            assertTrue_Z_StrN_k_(isNotEmpty_rCharSequence_Z_k_(sb))
        end function})
        m._this_suite.test("appendLine", {invoke: function() as Void
            sb = StringBuilder_create_StringBuilder_k_()
            sb.appendLine("hello")
            sb.appendLine("world")
            assertTrue_Z_StrN_k_(contains_rStr_Str_Z_Z_k_(sb.toString(), "hello"))
            assertTrue_Z_StrN_k_(contains_rStr_Str_Z_Z_k_(sb.toString(), "world"))
        end function})
    end function})
end sub
