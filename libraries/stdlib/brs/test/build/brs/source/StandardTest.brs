sub standardFunctionsTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Standard Functions", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("let returns lambda result", {invoke: function() as Void
            result = let_rAnyN_Function1_k_("hello", {invoke: function(it as String) as Integer
                return Len(it)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(5, result, invalid)
        end function})
        m.test_Str_Function0V_k_("let with null", {invoke: function() as Void
            str = invalid
            tmp0_safe_receiver = str
            __when_tmp0 = invalid
            if tmp0_safe_receiver = invalid then
                __when_tmp0 = invalid
            else if true then
                __when_tmp0 = let_rAnyN_Function1_k_(tmp0_safe_receiver, {invoke: function(it as String) as Integer
                    return Len(it)
                end function})
            end if
            result = __when_tmp0

            assertNull_AnyN_StrN_k_(result, invalid)
        end function})
        m.test_Str_Function0V_k_("let chain", {invoke: function() as Void
            result = let_rAnyN_Function1_k_(let_rAnyN_Function1_k_(5, {invoke: function(it as Integer) as Integer
                return it * 2
            end function}), {invoke: function(it as Integer) as Integer
                return it + 1
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(11, result, invalid)
        end function})
        m.test_Str_Function0V_k_("run returns lambda result", {invoke: function() as Void
            result = run_rAnyN_Function1_k_("hello", {invoke: function(m as String) as Integer
                return Len(m)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(5, result, invalid)
        end function})
        m.test_Str_Function0V_k_("run without receiver", {invoke: function() as Void
            result = run_rAnyN_Function1_k_(m, {invoke: function(m as Object) as Integer
                a = 1
                b = 2
                return a + b
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(3, result, invalid)
        end function})
        m.test_Str_Function0V_k_("also returns receiver", {invoke: function() as Void
            sideEffect = {value: 0}
            result = also_rAnyN_Function1V_k_("hello", {sideEffect: sideEffect, invoke: function(it as String) as Void
                m.sideEffect.value = Len(it)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_("hello", result, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(5, sideEffect.value, invalid)
        end function})
        m.test_Str_Function0V_k_("also chain", {invoke: function() as Void
            list = mutableListOf_k_()
            result = also_rAnyN_Function1V_k_(also_rAnyN_Function1V_k_(also_rAnyN_Function1V_k_(list, {invoke: function(it as Object) as Void
                it.add_AnyN_k_(1)
            end function}), {invoke: function(it as Object) as Void
                it.add_AnyN_k_(2)
            end function}), {invoke: function(it as Object) as Void
                it.add_AnyN_k_(3)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), result, invalid)
        end function})
        m.test_Str_Function0V_k_("apply returns receiver", {invoke: function() as Void
            sb = apply_rAnyN_Function1V_k_(StringBuilder_create_k_(), {invoke: function(m as Object) as Void
                m.append_StrN_k_("hello")
                m.append_StrN_k_(" ")
                m.append_StrN_k_("world")
            end function})
            assertEquals_AnyN_AnyN_StrN_k_("hello world", sb.toString(), invalid)
        end function})
        m.test_Str_Function0V_k_("apply for configuration", {invoke: function() as Void
            config = apply_rAnyN_Function1V_k_(Config_create_Str_I_k_(), {invoke: function(m as Object) as Void
                m.set_name("test")
                m.set_value(42)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_("test", config.name, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(42, config.value, invalid)
        end function})
        m.test_Str_Function0V_k_("check passes when true", {invoke: function() as Void
            check_Z_k_(true)
            assertTrue_Z_StrN_k_(true, invalid)
        end function})
        m.test_Str_Function0V_k_("require passes when true", {invoke: function() as Void
            require_Z_k_(true)
            assertTrue_Z_StrN_k_(true, invalid)
        end function})
        m.test_Str_Function0V_k_("to creates Pair", {invoke: function() as Void
            pair = to_rAnyN_AnyN_k_("key", 42)
            assertEquals_AnyN_AnyN_StrN_k_("key", pair.first, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(42, pair.second, invalid)
        end function})
        m.test_Str_Function0V_k_("Pair destructuring", {invoke: function() as Void
            __destruct_2 = to_rAnyN_AnyN_k_("hello", "world")
            a = __destruct_2.component1()
            b = __destruct_2.component2()
            assertEquals_AnyN_AnyN_StrN_k_("hello", a, invalid)
            assertEquals_AnyN_AnyN_StrN_k_("world", b, invalid)
        end function})
    end function})
end sub

function Config_create_Str_I_k_(name = "", value = 0) as Object
    this = {}
    this.__type = "Config"
    this.__proto = ["Config"]
    this.__id = __kotlin_nextObjectId()
    this.name = name
    this.value = value
    this.equals = Config_equals
    this.hashCode = Config_hashCode
    this.toString = Config_toString
    this.copy = Config_copy
    this.component1 = Config_component1
    this.component2 = Config_component2
    return this
end function

function Config_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "Config" then
        return false
    end if
    if m.name <> other.name then
        return false
    end if
    if m.value <> other.value then
        return false
    end if
    return true
end function

function Config_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.name)
    result = ((result * 31) + m.value)
    return result
end function

function Config_toString() as String
    return ((("Config(name=" + m.name) + ", value=") + m.value) + ")"
end function

function Config_copy(name = invalid, value = invalid) as Object
    if name = invalid then
        name = m.name
    end if
    if value = invalid then
        value = m.value
    end if
    return Config_create_Str_I_k_(name, value)
end function

function Config_component1() as String
    return m.name
end function

function Config_component2() as Integer
    return m.value
end function
