sub dateTimeTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("DateTime", dateTimeTests_lambda_create_k_())
end sub

function dateTimeTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "dateTimeTests_lambda_lambda"
    this.__proto = ["dateTimeTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dateTimeTests_lambda_lambda_invoke_k_
    return this
end function

sub dateTimeTests_lambda_lambda_invoke_k_()
    dt = CreateObject("roDateTime")
    assertTrue_Z_StrN_k_(true, invalid)
end sub

function dateTimeTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "dateTimeTests_lambda_lambda_1"
    this.__proto = ["dateTimeTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dateTimeTests_lambda_lambda_1_invoke_k_
    return this
end function

sub dateTimeTests_lambda_lambda_1_invoke_k_()
    dt = CreateObject("roDateTime")
    dt.mark()
    seconds = dt.asSeconds()
    assertTrue_Z_StrN_k_(seconds > 946684800, invalid)
end sub

function dateTimeTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "dateTimeTests_lambda_lambda_2"
    this.__proto = ["dateTimeTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dateTimeTests_lambda_lambda_2_invoke_k_
    return this
end function

sub dateTimeTests_lambda_lambda_2_invoke_k_()
    dt = CreateObject("roDateTime")
    dt.mark()
    year = dt.getYear()
    month = dt.getMonth()
    day = dt.getDayOfMonth()
    assertTrue_Z_StrN_k_(year >= 2024, invalid)
    assertTrue_Z_StrN_k_(rangeTo_rI_I_k_(1, 12).contains_Any_k_(month), invalid)
    assertTrue_Z_StrN_k_(rangeTo_rI_I_k_(1, 31).contains_Any_k_(day), invalid)
end sub

function dateTimeTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "dateTimeTests_lambda_lambda_3"
    this.__proto = ["dateTimeTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dateTimeTests_lambda_lambda_3_invoke_k_
    return this
end function

sub dateTimeTests_lambda_lambda_3_invoke_k_()
    dt = CreateObject("roDateTime")
    dt.mark()
    hours = dt.getHours()
    minutes = dt.getMinutes()
    seconds = dt.getSeconds()
    assertTrue_Z_StrN_k_(rangeTo_rI_I_k_(0, 23).contains_Any_k_(hours), invalid)
    assertTrue_Z_StrN_k_(rangeTo_rI_I_k_(0, 59).contains_Any_k_(minutes), invalid)
    assertTrue_Z_StrN_k_(rangeTo_rI_I_k_(0, 59).contains_Any_k_(seconds), invalid)
end sub

function dateTimeTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "dateTimeTests_lambda_lambda_4"
    this.__proto = ["dateTimeTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dateTimeTests_lambda_lambda_4_invoke_k_
    return this
end function

sub dateTimeTests_lambda_lambda_4_invoke_k_()
    dt = CreateObject("roDateTime")
    dt.mark()
    ms = dt.getMilliseconds()
    assertTrue_Z_StrN_k_(rangeTo_rI_I_k_(0, 999).contains_Any_k_(ms), invalid)
end sub

function dateTimeTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "dateTimeTests_lambda_lambda_5"
    this.__proto = ["dateTimeTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dateTimeTests_lambda_lambda_5_invoke_k_
    return this
end function

sub dateTimeTests_lambda_lambda_5_invoke_k_()
    dt = CreateObject("roDateTime")
    dt.mark()
    dow = dt.getDayOfWeek()
    assertTrue_Z_StrN_k_(rangeTo_rI_I_k_(0, 6).contains_Any_k_(dow), invalid)
end sub

function dateTimeTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "dateTimeTests_lambda_lambda_6"
    this.__proto = ["dateTimeTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dateTimeTests_lambda_lambda_6_invoke_k_
    return this
end function

sub dateTimeTests_lambda_lambda_6_invoke_k_()
    ts = CreateObject("roTimespan")
    assertTrue_Z_StrN_k_(true, invalid)
end sub

function dateTimeTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "dateTimeTests_lambda_lambda_7"
    this.__proto = ["dateTimeTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dateTimeTests_lambda_lambda_7_invoke_k_
    return this
end function

sub dateTimeTests_lambda_lambda_7_invoke_k_()
    ts = CreateObject("roTimespan")
    ts.mark()
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

    elapsed = ts.totalMilliseconds()
    assertTrue_Z_StrN_k_(elapsed >= 0, invalid)
end sub

function dateTimeTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "dateTimeTests_lambda_lambda_8"
    this.__proto = ["dateTimeTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dateTimeTests_lambda_lambda_8_invoke_k_
    return this
end function

sub dateTimeTests_lambda_lambda_8_invoke_k_()
    ts = CreateObject("roTimespan")
    ts.mark()
    seconds = ts.totalSeconds()
    assertTrue_Z_StrN_k_(seconds >= 0, invalid)
end sub

function dateTimeTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "dateTimeTests_lambda_lambda_9"
    this.__proto = ["dateTimeTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dateTimeTests_lambda_lambda_9_invoke_k_
    return this
end function

sub dateTimeTests_lambda_lambda_9_invoke_k_()
    ts1 = CreateObject("roTimespan")
    ts2 = CreateObject("roTimespan")
    ts1.mark()
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

    ts2.mark()
    elapsed1 = ts1.totalMilliseconds()
    elapsed2 = ts2.totalMilliseconds()
    assertTrue_Z_StrN_k_(elapsed1 >= elapsed2, invalid)
end sub

function dateTimeTests_lambda_create_k_() as Object
    this = {}
    this.__type = "dateTimeTests_lambda"
    this.__proto = ["dateTimeTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = dateTimeTests_lambda_invoke_AnyN_k_
    return this
end function

sub dateTimeTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("RoDateTime creation", dateTimeTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("RoDateTime mark", dateTimeTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("RoDateTime components", dateTimeTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("RoDateTime time components", dateTimeTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("RoDateTime milliseconds", dateTimeTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("RoDateTime day of week", dateTimeTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("RoTimespan creation", dateTimeTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("RoTimespan mark and measure", dateTimeTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("RoTimespan totalSeconds", dateTimeTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("multiple timespans", dateTimeTests_lambda_lambda_9_create_k_())
end sub
