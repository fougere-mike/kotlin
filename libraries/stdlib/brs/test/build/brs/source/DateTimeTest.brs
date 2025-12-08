sub dateTimeTests_rTestRunner_k_(m as Object)
    m.suite("DateTime", {_this_suite: _this_suite, invoke: function() as Void
        m._this_suite.test("RoDateTime creation", {invoke: function() as Void
            dt = RoDateTime_create_RoDateTime_k_()
            assertTrue_Z_StrN_k_(true)
        end function})
        m._this_suite.test("RoDateTime mark", {invoke: function() as Void
            dt = RoDateTime_create_RoDateTime_k_()
            dt.mark()
            seconds = dt.asSeconds()
            assertTrue_Z_StrN_k_(seconds > 946684800)
        end function})
        m._this_suite.test("RoDateTime components", {invoke: function() as Void
            dt = RoDateTime_create_RoDateTime_k_()
            dt.mark()
            year = dt.getYear()
            month = dt.getMonth()
            day = dt.getDayOfMonth()
            assertTrue_Z_StrN_k_(year >= 2024)
            assertTrue_Z_StrN_k_(1.rangeTo(12).contains(month))
            assertTrue_Z_StrN_k_(1.rangeTo(31).contains(day))
        end function})
        m._this_suite.test("RoDateTime time components", {invoke: function() as Void
            dt = RoDateTime_create_RoDateTime_k_()
            dt.mark()
            hours = dt.getHours()
            minutes = dt.getMinutes()
            seconds = dt.getSeconds()
            assertTrue_Z_StrN_k_(0.rangeTo(23).contains(hours))
            assertTrue_Z_StrN_k_(0.rangeTo(59).contains(minutes))
            assertTrue_Z_StrN_k_(0.rangeTo(59).contains(seconds))
        end function})
        m._this_suite.test("RoDateTime milliseconds", {invoke: function() as Void
            dt = RoDateTime_create_RoDateTime_k_()
            dt.mark()
            ms = dt.getMilliseconds()
            assertTrue_Z_StrN_k_(0.rangeTo(999).contains(ms))
        end function})
        m._this_suite.test("RoDateTime day of week", {invoke: function() as Void
            dt = RoDateTime_create_RoDateTime_k_()
            dt.mark()
            dow = dt.getDayOfWeek()
            assertTrue_Z_StrN_k_(0.rangeTo(6).contains(dow))
        end function})
        m._this_suite.test("RoTimespan creation", {invoke: function() as Void
            ts = RoTimespan_create_RoTimespan_k_()
            assertTrue_Z_StrN_k_(true)
        end function})
        m._this_suite.test("RoTimespan mark and measure", {invoke: function() as Void
            ts = RoTimespan_create_RoTimespan_k_()
            ts.mark()
            sum = 0
            inductionVariable = 1
            if lessOrEqual_I_I_Z_k_(inductionVariable, 1000) then
                                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                sum = (sum + i)


                while lessOrEqual_I_I_Z_k_(inductionVariable, 1000)
                    i = inductionVariable
                    inductionVariable = (inductionVariable + 1)

                    sum = (sum + i)

                end while

            end if

            elapsed = ts.totalMilliseconds()
            assertTrue_Z_StrN_k_(elapsed >= 0)
        end function})
        m._this_suite.test("RoTimespan totalSeconds", {invoke: function() as Void
            ts = RoTimespan_create_RoTimespan_k_()
            ts.mark()
            seconds = ts.totalSeconds()
            assertTrue_Z_StrN_k_(seconds >= 0)
        end function})
        m._this_suite.test("multiple timespans", {invoke: function() as Void
            ts1 = RoTimespan_create_RoTimespan_k_()
            ts2 = RoTimespan_create_RoTimespan_k_()
            ts1.mark()
            sum = 0
            inductionVariable = 1
            if lessOrEqual_I_I_Z_k_(inductionVariable, 1000) then
                                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                sum = (sum + i)


                while lessOrEqual_I_I_Z_k_(inductionVariable, 1000)
                    i = inductionVariable
                    inductionVariable = (inductionVariable + 1)

                    sum = (sum + i)

                end while

            end if

            ts2.mark()
            elapsed1 = ts1.totalMilliseconds()
            elapsed2 = ts2.totalMilliseconds()
            assertTrue_Z_StrN_k_(elapsed1 >= elapsed2)
        end function})
    end function})
end sub
