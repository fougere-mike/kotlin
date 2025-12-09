sub standardFunctionsTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Standard Functions", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("let returns lambda result", {invoke: function() as Void
            result = let_rAnyN_Function1AnyNAnyN_AnyN_k_("hello", {invoke: function(it as String) as Integer
                return it.get_length()
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(5, result)
        end function})
        m.test_Str_Function0V_k_("let with null", {invoke: function() as Void
            str = invalid
            tmp0_safe_receiver = str
            __when_tmp0 = invalid
            if tmp0_safe_receiver = invalid then
                __when_tmp0 = invalid
            else if true then
                __when_tmp0 = let_rAnyN_Function1AnyNAnyN_AnyN_k_(tmp0_safe_receiver, {invoke: function(it as String) as Integer
                    return it.get_length()
                end function})
            end if
            result = __when_tmp0

            assertNull_AnyN_StrN_k_(result)
        end function})
        m.test_Str_Function0V_k_("let chain", {invoke: function() as Void
            result = let_rAnyN_Function1AnyNAnyN_AnyN_k_(let_rAnyN_Function1AnyNAnyN_AnyN_k_(5, {invoke: function(it as Integer) as Integer
                return it * 2
            end function}), {invoke: function(it as Integer) as Integer
                return it + 1
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(11, result)
        end function})
        m.test_Str_Function0V_k_("run returns lambda result", {invoke: function() as Void
            result = run_rAnyN_Function1AnyNAnyN_AnyN_k_("hello", {invoke: function(m as String) as Integer
                return m.get_length()
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(5, result)
        end function})
        m.test_Str_Function0V_k_("run without receiver", {_this_suite: _this_suite, invoke: function() as Void
            result = run_rAnyN_Function1AnyNAnyN_AnyN_k_(m._this_suite, {invoke: function(m as Object) as Integer
                a = 1
                b = 2
                return a + b
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(3, result)
        end function})
        m.test_Str_Function0V_k_("also returns receiver", {invoke: function() as Void
            sideEffect = 0
            result = also_rAnyN_Function1AnyNV_AnyN_k_("hello", {sideEffect: {value: sideEffect}, invoke: function(it as String) as Void
                m.sideEffect.value = it.get_length()
            end function})
            assertEquals_AnyN_AnyN_StrN_k_("hello", result)
            assertEquals_AnyN_AnyN_StrN_k_(5, sideEffect)
        end function})
        m.test_Str_Function0V_k_("also chain", {invoke: function() as Void
            list = mutableListOf_MutableListAnyN_k_()
            result = also_rAnyN_Function1AnyNV_AnyN_k_(also_rAnyN_Function1AnyNV_AnyN_k_(also_rAnyN_Function1AnyNV_AnyN_k_(list, {invoke: function(it as Object) as Void
                it.add_AnyN_Z_k_(1)
            end function}), {invoke: function(it as Object) as Void
                it.add_AnyN_Z_k_(2)
            end function}), {invoke: function(it as Object) as Void
                it.add_AnyN_Z_k_(3)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3]), result)
        end function})
        m.test_Str_Function0V_k_("apply returns receiver", {invoke: function() as Void
            sb = apply_rAnyN_Function1AnyNV_AnyN_k_(StringBuilder_create_StringBuilder_k_(), {invoke: function(m as Object) as Void
                m.append_StrN_StringBuilder_k_("hello")
                m.append_StrN_StringBuilder_k_(" ")
                m.append_StrN_StringBuilder_k_("world")
            end function})
            assertEquals_AnyN_AnyN_StrN_k_("hello world", sb.toString())
        end function})
        m.test_Str_Function0V_k_("apply for configuration", {name: name, this: m, this: m, value: value, value: value, this: m, this: m, value: value, this: m, this: m, this: m, name: name, value: value, this: m, this: m, this: m, other: other, invoke: function() as Void
            config = apply_rAnyN_Function1AnyNV_AnyN_k_(Config_create_Str_I_Config_k_(), {invoke: function(m as Object) as Void
                m.set_name("test")
                m.set_value(42)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_("test", config.name)
            assertEquals_AnyN_AnyN_StrN_k_(42, config.value)
        end function})
        m.test_Str_Function0V_k_("check passes when true", {invoke: function() as Void
            check_Z_k_(true)
            assertTrue_Z_StrN_k_(true)
        end function})
        m.test_Str_Function0V_k_("require passes when true", {invoke: function() as Void
            require_Z_k_(true)
            assertTrue_Z_StrN_k_(true)
        end function})
        m.test_Str_Function0V_k_("to creates Pair", {invoke: function() as Void
            pair = to_rAnyN_AnyN_PairAnyNAnyN_k_("key", 42)
            assertEquals_AnyN_AnyN_StrN_k_("key", pair.first)
            assertEquals_AnyN_AnyN_StrN_k_(42, pair.second)
        end function})
        m.test_Str_Function0V_k_("Pair destructuring", {invoke: function() as Void
            __destruct_2 = to_rAnyN_AnyN_PairAnyNAnyN_k_("hello", "world")
            a = __destruct_2.component1()
            b = __destruct_2.component2()
            assertEquals_AnyN_AnyN_StrN_k_("hello", a)
            assertEquals_AnyN_AnyN_StrN_k_("world", b)
        end function})
    end function})
end sub
