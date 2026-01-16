sub lazyDelegateTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Lazy Delegates", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("lazy isInitialized check", {invoke: function() as Void
            initCount = {value: 0}
            lazyInstance = lazy_Function0_k_({initCount: initCount, invoke: function() as String
                m.initCount.value = (m.initCount.value + 1)
                return "value"
            end function})
            assertFalse_Z_StrN_k_(lazyInstance.isInitialized_k_(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(0, initCount.value, invalid)
            assertEquals_AnyN_AnyN_StrN_k_("value", lazyInstance.get_value(), invalid)
            assertTrue_Z_StrN_k_(lazyInstance.isInitialized_k_(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(1, initCount.value, invalid)
            assertEquals_AnyN_AnyN_StrN_k_("value", lazyInstance.get_value(), invalid)
            assertTrue_Z_StrN_k_(lazyInstance.isInitialized_k_(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(1, initCount.value, invalid)
        end function})
        m.test_Str_Function0V_k_("lazy toString before and after initialization", {invoke: function() as Void
            lazyInstance = lazy_Function0_k_({invoke: function() as String
                return "hello"
            end function})
            assertEquals_AnyN_AnyN_StrN_k_("Lazy value not initialized yet.", toString_AnyN_k_(lazyInstance), invalid)
            lazyInstance.get_value()
            assertEquals_AnyN_AnyN_StrN_k_("hello", toString_AnyN_k_(lazyInstance), invalid)
        end function})
    end function})
end sub
