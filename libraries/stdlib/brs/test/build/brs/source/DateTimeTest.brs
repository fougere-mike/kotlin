sub dateTimeTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("DateTime", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("RoDateTime creation", {invoke: function() as Void
            dt = RoDateTime_create_k_()
            assertTrue_Z_StrN_k_(true)
        end function})
        m.test_Str_Function0V_k_("RoDateTime mark", {invoke: function() as Void
            dt = RoDateTime_create_k_()
            dt.mark_k_()
            seconds = dt.asSeconds_k_()
            assertTrue_Z_StrN_k_(seconds > 946684800)
        end function})
        m.test_Str_Function0V_k_("RoDateTime components", {invoke: function() as Void
            dt = RoDateTime_create_k_()
            dt.mark_k_()
            year = dt.getYear_k_()
            month = dt.getMonth_k_()
            day = dt.getDayOfMonth_k_()
            assertTrue_Z_StrN_k_(year >= 2024)
            assertTrue_Z_StrN_k_(rangeTo_rI_I_k_(1, 12).contains_I_k_(month))
            assertTrue_Z_StrN_k_(rangeTo_rI_I_k_(1, 31).contains_I_k_(day))
        end function})
        m.test_Str_Function0V_k_("RoDateTime time components", {invoke: function() as Void
            dt = RoDateTime_create_k_()
            dt.mark_k_()
            hours = dt.getHours_k_()
            minutes = dt.getMinutes_k_()
            seconds = dt.getSeconds_k_()
            assertTrue_Z_StrN_k_(rangeTo_rI_I_k_(0, 23).contains_I_k_(hours))
            assertTrue_Z_StrN_k_(rangeTo_rI_I_k_(0, 59).contains_I_k_(minutes))
            assertTrue_Z_StrN_k_(rangeTo_rI_I_k_(0, 59).contains_I_k_(seconds))
        end function})
        m.test_Str_Function0V_k_("RoDateTime milliseconds", {invoke: function() as Void
            dt = RoDateTime_create_k_()
            dt.mark_k_()
            ms = dt.getMilliseconds_k_()
            assertTrue_Z_StrN_k_(rangeTo_rI_I_k_(0, 999).contains_I_k_(ms))
        end function})
        m.test_Str_Function0V_k_("RoDateTime day of week", {invoke: function() as Void
            dt = RoDateTime_create_k_()
            dt.mark_k_()
            dow = dt.getDayOfWeek_k_()
            assertTrue_Z_StrN_k_(rangeTo_rI_I_k_(0, 6).contains_I_k_(dow))
        end function})
        m.test_Str_Function0V_k_("RoTimespan creation", {invoke: function() as Void
            ts = RoTimespan_create_k_()
            assertTrue_Z_StrN_k_(true)
        end function})
        m.test_Str_Function0V_k_("RoTimespan mark and measure", {invoke: function() as Void
            ts = RoTimespan_create_k_()
            ts.mark_k_()
            sum = 0
            inductionVariable = 1
            if inductionVariable <= 1000 then
                                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                sum = (sum + i)


                while inductionVariable <= 1000
                    i = inductionVariable
                    inductionVariable = (inductionVariable + 1)

                    sum = (sum + i)

                end while

            end if

            elapsed = ts.totalMilliseconds_k_()
            assertTrue_Z_StrN_k_(elapsed >= 0)
        end function})
        m.test_Str_Function0V_k_("RoTimespan totalSeconds", {invoke: function() as Void
            ts = RoTimespan_create_k_()
            ts.mark_k_()
            seconds = ts.totalSeconds_k_()
            assertTrue_Z_StrN_k_(seconds >= 0)
        end function})
        m.test_Str_Function0V_k_("multiple timespans", {invoke: function() as Void
            ts1 = RoTimespan_create_k_()
            ts2 = RoTimespan_create_k_()
            ts1.mark_k_()
            sum = 0
            inductionVariable = 1
            if inductionVariable <= 1000 then
                                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                sum = (sum + i)


                while inductionVariable <= 1000
                    i = inductionVariable
                    inductionVariable = (inductionVariable + 1)

                    sum = (sum + i)

                end while

            end if

            ts2.mark_k_()
            elapsed1 = ts1.totalMilliseconds_k_()
            elapsed2 = ts2.totalMilliseconds_k_()
            assertTrue_Z_StrN_k_(elapsed1 >= elapsed2)
        end function})
    end function})
end sub
