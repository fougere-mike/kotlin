sub standardFunctionsTests_rTestRunner_k_(m as Object)
    m.suite("Standard Functions", {_this_suite: _this_suite, it: it, it: it, it: it, it: it, _this_run: _this_run, it: it, it: it, it: it, it: it, _this_apply: _this_apply, name: name, this: this, this: this, value: value, value: value, this: this, this: this, value: value, this: this, this: this, this: this, name: name, value: value, this: this, this: this, this: this, other: other, _this_apply: _this_apply, invoke: function() as Void
        m._this_suite.test("let returns lambda result", {it: it, invoke: function() as Void
            result = let_rAnyN_Function1AnyNAnyN_AnyN_k_("hello", {invoke: function(it as String) as Integer
                return m.it.length
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(5, result)
        end function})
        m._this_suite.test("let with null", {it: it, invoke: function() as Void
            str = invalid
            tmp0_safe_receiver = str
            __when_tmp0 = invalid
            if tmp0_safe_receiver = invalid then
                __when_tmp0 = invalid
            else if true then
                __when_tmp0 = let_rAnyN_Function1AnyNAnyN_AnyN_k_(tmp0_safe_receiver, {invoke: function(it as String) as Integer
                    return m.it.length
                end function})
            end if
            result = __when_tmp0

            assertNull_AnyN_StrN_k_(result)
        end function})
        m._this_suite.test("let chain", {it: it, it: it, invoke: function() as Void
            result = let_rAnyN_Function1AnyNAnyN_AnyN_k_(let_rAnyN_Function1AnyNAnyN_AnyN_k_(5, {invoke: function(it as Integer) as Integer
                return m.it * 2
            end function}), {invoke: function(it as Integer) as Integer
                return m.it + 1
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(11, result)
        end function})
        m._this_suite.test("run returns lambda result", {_this_run: _this_run, invoke: function() as Void
            result = run_rAnyN_Function1AnyNAnyN_AnyN_k_("hello", {_this_run: _this_run, invoke: function() as Integer
                return m._this_run.length
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(5, result)
        end function})
        m._this_suite.test("run without receiver", {_this_suite: _this_suite, invoke: function() as Void
            result = run_rAnyN_Function1AnyNAnyN_AnyN_k_(m._this_suite, {invoke: function() as Integer
                a = 1
                b = 2
                return a + b
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(3, result)
        end function})
        m._this_suite.test("also returns receiver", {it: it, invoke: function() as Void
            sideEffect = 0
            result = also_rAnyN_Function1AnyNV_AnyN_k_("hello", {sideEffect: {value: sideEffect}, invoke: function(it as String) as Void
                m.sideEffect.value = it.length
            end function})
            assertEquals_AnyN_AnyN_StrN_k_("hello", result)
            assertEquals_AnyN_AnyN_StrN_k_(5, sideEffect)
        end function})
        m._this_suite.test("also chain", {it: it, it: it, it: it, invoke: function() as Void
            list = mutableListOf_MutableListAnyN_k_()
            result = also_rAnyN_Function1AnyNV_AnyN_k_(also_rAnyN_Function1AnyNV_AnyN_k_(also_rAnyN_Function1AnyNV_AnyN_k_(list, {invoke: function(it as Object) as Void
                m.it.add(1)
            end function}), {invoke: function(it as Object) as Void
                m.it.add(2)
            end function}), {invoke: function(it as Object) as Void
                m.it.add(3)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3]), result)
        end function})
        m._this_suite.test("apply returns receiver", {_this_apply: _this_apply, invoke: function() as Void
            sb = apply_rAnyN_Function1AnyNV_AnyN_k_(StringBuilder_create_StringBuilder_k_(), {_this_apply: _this_apply, invoke: function() as Void
                m._this_apply.append("hello")
                m._this_apply.append(" ")
                m._this_apply.append("world")
            end function})
            assertEquals_AnyN_AnyN_StrN_k_("hello world", sb.toString())
        end function})
        m._this_suite.test("apply for configuration", {name: name, this: this, this: this, value: value, value: value, this: this, this: this, value: value, this: this, this: this, this: this, name: name, value: value, this: this, this: this, this: this, other: other, _this_apply: _this_apply, invoke: function() as Void
            config = apply_rAnyN_Function1AnyNV_AnyN_k_(Config_create_Str_I_Config_k_(), {_this_apply: _this_apply, invoke: function() as Void
                m._this_apply.name = "test"
                m._this_apply.value = 42
            end function})
            assertEquals_AnyN_AnyN_StrN_k_("test", config.name)
            assertEquals_AnyN_AnyN_StrN_k_(42, config.value)
        end function})
        m._this_suite.test("check passes when true", {invoke: function() as Void
            check_Z_k_(true)
            assertTrue_Z_StrN_k_(true)
        end function})
        m._this_suite.test("require passes when true", {invoke: function() as Void
            require_Z_k_(true)
            assertTrue_Z_StrN_k_(true)
        end function})
        m._this_suite.test("to creates Pair", {invoke: function() as Void
            pair = to_rAnyN_AnyN_PairAnyNAnyN_k_("key", 42)
            assertEquals_AnyN_AnyN_StrN_k_("key", pair.first)
            assertEquals_AnyN_AnyN_StrN_k_(42, pair.second)
        end function})
        m._this_suite.test("Pair destructuring", {invoke: function() as Void
            __destruct_2 = to_rAnyN_AnyN_PairAnyNAnyN_k_("hello", "world")
            a = __destruct_2.component1()
            b = __destruct_2.component2()
            assertEquals_AnyN_AnyN_StrN_k_("hello", a)
            assertEquals_AnyN_AnyN_StrN_k_("world", b)
        end function})
    end function})
end sub
