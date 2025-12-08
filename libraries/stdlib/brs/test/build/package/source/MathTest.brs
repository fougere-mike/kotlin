sub mathTests_rTestRunner_k_(m as Object)
    m.suite("Math", {_this_suite: _this_suite, invoke: function() as Void
        m._this_suite.test("PI", {invoke: function() as Void
            assertTrue_Z_StrN_k_(3.141592653589793# > 3.14#)
            assertTrue_Z_StrN_k_(3.141592653589793# < 3.15#)
        end function})
        m._this_suite.test("E", {invoke: function() as Void
            assertTrue_Z_StrN_k_(2.718281828459045# > 2.71#)
            assertTrue_Z_StrN_k_(2.718281828459045# < 2.72#)
        end function})
        m._this_suite.test("abs Int", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(5, abs_I_I_k_(-5))
            assertEquals_AnyN_AnyN_StrN_k_(5, abs_I_I_k_(5))
            assertEquals_AnyN_AnyN_StrN_k_(0, abs_I_I_k_(0))
        end function})
        m._this_suite.test("abs Double", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(5.5#, abs_D_D_k_(-5.5#))
            assertEquals_AnyN_AnyN_StrN_k_(5.5#, abs_D_D_k_(5.5#))
        end function})
        m._this_suite.test("min", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(1, min_I_I_I_k_(1, 2))
            assertEquals_AnyN_AnyN_StrN_k_(-5, min_I_I_I_k_(-5, 3))
            assertEquals_AnyN_AnyN_StrN_k_(1.5#, min_D_D_D_k_(1.5#, 2.5#))
        end function})
        m._this_suite.test("max", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(2, max_I_I_I_k_(1, 2))
            assertEquals_AnyN_AnyN_StrN_k_(3, max_I_I_I_k_(-5, 3))
            assertEquals_AnyN_AnyN_StrN_k_(2.5#, max_D_D_D_k_(1.5#, 2.5#))
        end function})
        m._this_suite.test("floor", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(3.0#, floor_D_D_k_(3.7#))
            assertEquals_AnyN_AnyN_StrN_k_(-4.0#, floor_D_D_k_(-3.3#))
        end function})
        m._this_suite.test("ceil", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(4.0#, ceil_D_D_k_(3.3#))
            assertEquals_AnyN_AnyN_StrN_k_(-3.0#, ceil_D_D_k_(-3.7#))
        end function})
        m._this_suite.test("round", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(4.0#, round_D_D_k_(3.7#))
            assertEquals_AnyN_AnyN_StrN_k_(3.0#, round_D_D_k_(3.3#))
            assertEquals_AnyN_AnyN_StrN_k_(4.0#, round_D_D_k_(3.5#))
        end function})
        m._this_suite.test("truncate", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(3.0#, truncate_D_D_k_(3.7#))
            assertEquals_AnyN_AnyN_StrN_k_(-3.0#, truncate_D_D_k_(-3.7#))
        end function})
        m._this_suite.test("roundToInt", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(4, roundToInt_rD_I_k_(3.7#))
            assertEquals_AnyN_AnyN_StrN_k_(3, roundToInt_rD_I_k_(3.3#))
        end function})
        m._this_suite.test("roundToLong", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(4&, roundToLong_rD_J_k_(3.7#))
            assertEquals_AnyN_AnyN_StrN_k_(3&, roundToLong_rD_J_k_(3.3#))
        end function})
        m._this_suite.test("sqrt", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(3.0#, sqrt_D_D_k_(9.0#))
            assertEquals_AnyN_AnyN_StrN_k_(2.0#, sqrt_D_D_k_(4.0#))
            assertTrue_Z_StrN_k_((sqrt_D_D_k_(2.0#) > 1.41#) and (sqrt_D_D_k_(2.0#) < 1.42#))
        end function})
        m._this_suite.test("pow", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(8.0#, pow_rD_D_D_k_(2.0#, 3.0#))
            assertEquals_AnyN_AnyN_StrN_k_(1.0#, pow_rD_D_D_k_(5.0#, 0.0#))
            assertEquals_AnyN_AnyN_StrN_k_(0.25#, pow_rD_D_D_k_(2.0#, -2.0#))
        end function})
        m._this_suite.test("pow Int", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(8.0#, pow_rD_I_D_k_(2.0#, 3))
            assertEquals_AnyN_AnyN_StrN_k_(1.0#, pow_rD_I_D_k_(5.0#, 0))
        end function})
        m._this_suite.test("exp", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(1.0#, exp_D_D_k_(0.0#))
            assertTrue_Z_StrN_k_((exp_D_D_k_(1.0#) > 2.71#) and (exp_D_D_k_(1.0#) < 2.72#))
        end function})
        m._this_suite.test("ln", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(0.0#, ln_D_D_k_(1.0#))
            assertEquals_AnyN_AnyN_StrN_k_(1.0#, ln_D_D_k_(2.718281828459045#))
        end function})
        m._this_suite.test("log10", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(0.0#, log10_D_D_k_(1.0#))
            assertEquals_AnyN_AnyN_StrN_k_(1.0#, log10_D_D_k_(10.0#))
            assertEquals_AnyN_AnyN_StrN_k_(2.0#, log10_D_D_k_(100.0#))
        end function})
        m._this_suite.test("log2", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(0.0#, log2_D_D_k_(1.0#))
            assertEquals_AnyN_AnyN_StrN_k_(1.0#, log2_D_D_k_(2.0#))
            assertEquals_AnyN_AnyN_StrN_k_(3.0#, log2_D_D_k_(8.0#))
        end function})
        m._this_suite.test("log with base", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(2.0#, log_D_D_D_k_(4.0#, 2.0#))
            assertEquals_AnyN_AnyN_StrN_k_(3.0#, log_D_D_D_k_(8.0#, 2.0#))
        end function})
        m._this_suite.test("sin", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(0.0#, sin_D_D_k_(0.0#))
            assertTrue_Z_StrN_k_(abs_D_D_k_(sin_D_D_k_(3.141592653589793# / 2) - 1.0#) < 1.0E-4#)
        end function})
        m._this_suite.test("cos", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(1.0#, cos_D_D_k_(0.0#))
            assertTrue_Z_StrN_k_((abs_D_D_k_(cos_D_D_k_(3.141592653589793#)) - -1.0#) < 1.0E-4#)
        end function})
        m._this_suite.test("tan", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(0.0#, tan_D_D_k_(0.0#))
            assertTrue_Z_StrN_k_(abs_D_D_k_(tan_D_D_k_(3.141592653589793# / 4) - 1.0#) < 1.0E-4#)
        end function})
        m._this_suite.test("asin", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(0.0#, asin_D_D_k_(0.0#))
            assertTrue_Z_StrN_k_(abs_D_D_k_(asin_D_D_k_(1.0#) - (3.141592653589793# / 2)) < 1.0E-4#)
        end function})
        m._this_suite.test("acos", {invoke: function() as Void
            assertTrue_Z_StrN_k_(abs_D_D_k_(acos_D_D_k_(1.0#)) < 1.0E-4#)
            assertTrue_Z_StrN_k_(abs_D_D_k_(acos_D_D_k_(0.0#) - (3.141592653589793# / 2)) < 1.0E-4#)
        end function})
        m._this_suite.test("atan", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(0.0#, atan_D_D_k_(0.0#))
            assertTrue_Z_StrN_k_(abs_D_D_k_(atan_D_D_k_(1.0#) - (3.141592653589793# / 4)) < 1.0E-4#)
        end function})
        m._this_suite.test("atan2", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(0.0#, atan2_D_D_D_k_(0.0#, 1.0#))
            assertTrue_Z_StrN_k_(abs_D_D_k_(atan2_D_D_D_k_(1.0#, 1.0#) - (3.141592653589793# / 4)) < 1.0E-4#)
        end function})
        m._this_suite.test("sinh", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(0.0#, sinh_D_D_k_(0.0#))
        end function})
        m._this_suite.test("cosh", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(1.0#, cosh_D_D_k_(0.0#))
        end function})
        m._this_suite.test("tanh", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(0.0#, tanh_D_D_k_(0.0#))
        end function})
        m._this_suite.test("hypot", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(5.0#, hypot_D_D_D_k_(3.0#, 4.0#))
            assertEquals_AnyN_AnyN_StrN_k_(13.0#, hypot_D_D_D_k_(5.0#, 12.0#))
        end function})
        m._this_suite.test("sign", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(1.0#, sign_D_D_k_(42.0#))
            assertEquals_AnyN_AnyN_StrN_k_(-1.0#, sign_D_D_k_(-42.0#))
            assertEquals_AnyN_AnyN_StrN_k_(0.0#, sign_D_D_k_(0.0#))
        end function})
        m._this_suite.test("sign Int", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(1, get_sign_rI_I_k_(42))
            assertEquals_AnyN_AnyN_StrN_k_(-1, get_sign_rI_I_k_(-42))
            assertEquals_AnyN_AnyN_StrN_k_(0, get_sign_rI_I_k_(0))
        end function})
        m._this_suite.test("isNaN", {invoke: function() as Void
            assertTrue_Z_StrN_k_(isNaN_rD_Z_k_((0.0# / 0.0#)))
            assertFalse_Z_StrN_k_(isNaN_rD_Z_k_(1.0#))
        end function})
        m._this_suite.test("isInfinite", {invoke: function() as Void
            assertTrue_Z_StrN_k_(isInfinite_rD_Z_k_((1.0E+309#)))
            assertTrue_Z_StrN_k_(isInfinite_rD_Z_k_((-1.0E+309#)))
            assertFalse_Z_StrN_k_(isInfinite_rD_Z_k_(1.0#))
        end function})
        m._this_suite.test("isFinite", {invoke: function() as Void
            assertTrue_Z_StrN_k_(isFinite_rD_Z_k_(1.0#))
            assertFalse_Z_StrN_k_(isFinite_rD_Z_k_((0.0# / 0.0#)))
            assertFalse_Z_StrN_k_(isFinite_rD_Z_k_((1.0E+309#)))
        end function})
    end function})
end sub
