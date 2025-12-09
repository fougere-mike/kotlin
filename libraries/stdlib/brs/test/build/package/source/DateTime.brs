function RoDateTime_create_RoDateTime_k_() as Object
    this = {}
    this.__type = "RoDateTime"
    this.__proto = ["RoDateTime"]
    this.__id = __kotlin_nextObjectId()
    this.mark = RoDateTime_mark
    this.asSeconds_J_k_ = RoDateTime_asSeconds_J_k_
    this.fromSeconds_J_k_ = RoDateTime_fromSeconds_J_k_
    this.toISOString_Str_k_ = RoDateTime_toISOString_Str_k_
    this.fromISO8601String_Str_Z_k_ = RoDateTime_fromISO8601String_Str_Z_k_
    this.getYear_I_k_ = RoDateTime_getYear_I_k_
    this.getMonth_I_k_ = RoDateTime_getMonth_I_k_
    this.getDayOfMonth_I_k_ = RoDateTime_getDayOfMonth_I_k_
    this.getDayOfWeek_I_k_ = RoDateTime_getDayOfWeek_I_k_
    this.getHours_I_k_ = RoDateTime_getHours_I_k_
    this.getMinutes_I_k_ = RoDateTime_getMinutes_I_k_
    this.getSeconds_I_k_ = RoDateTime_getSeconds_I_k_
    this.getMilliseconds_I_k_ = RoDateTime_getMilliseconds_I_k_
    this.getTimeZoneOffset_I_k_ = RoDateTime_getTimeZoneOffset_I_k_
    this.getLocalYear_I_k_ = RoDateTime_getLocalYear_I_k_
    this.getLocalMonth_I_k_ = RoDateTime_getLocalMonth_I_k_
    this.getLocalDayOfMonth_I_k_ = RoDateTime_getLocalDayOfMonth_I_k_
    this.getLocalDayOfWeek_I_k_ = RoDateTime_getLocalDayOfWeek_I_k_
    this.getLocalHours_I_k_ = RoDateTime_getLocalHours_I_k_
    this.getLocalMinutes_I_k_ = RoDateTime_getLocalMinutes_I_k_
    this.getLocalSeconds_I_k_ = RoDateTime_getLocalSeconds_I_k_
    this.getWeekday_Str_k_ = RoDateTime_getWeekday_Str_k_
    this.getLastDayOfMonth_I_k_ = RoDateTime_getLastDayOfMonth_I_k_
    this.toString_Str_k_ = RoDateTime_toString_Str_k_
    this.toString = RoDateTime_toString_Str_k_
    this.get_native = RoDateTime_get_native_Dynamic_k_
    this.native = CreateObject("roDateTime")
    return this
end function

sub RoDateTime_mark()
    m.get_native().Mark()
end sub

function RoDateTime_asSeconds_J_k_() as LongInteger
    return m.get_native().AsSeconds()
end function

sub RoDateTime_fromSeconds_J_k_(seconds as LongInteger)
    m.get_native().FromSeconds(seconds)
end sub

function RoDateTime_toISOString_Str_k_() as String
    return m.get_native().ToISOString()
end function

function RoDateTime_fromISO8601String_Str_Z_k_(iso as String) as Boolean
    return m.get_native().FromISO8601String(iso)
end function

function RoDateTime_getYear_I_k_() as Integer
    return m.get_native().GetYear()
end function

function RoDateTime_getMonth_I_k_() as Integer
    return m.get_native().GetMonth()
end function

function RoDateTime_getDayOfMonth_I_k_() as Integer
    return m.get_native().GetDayOfMonth()
end function

function RoDateTime_getDayOfWeek_I_k_() as Integer
    return m.get_native().GetDayOfWeek()
end function

function RoDateTime_getHours_I_k_() as Integer
    return m.get_native().GetHours()
end function

function RoDateTime_getMinutes_I_k_() as Integer
    return m.get_native().GetMinutes()
end function

function RoDateTime_getSeconds_I_k_() as Integer
    return m.get_native().GetSeconds()
end function

function RoDateTime_getMilliseconds_I_k_() as Integer
    return m.get_native().GetMilliseconds()
end function

function RoDateTime_getTimeZoneOffset_I_k_() as Integer
    return m.get_native().GetTimeZoneOffset()
end function

function RoDateTime_getLocalYear_I_k_() as Integer
    return m.get_native().GetLocalYear()
end function

function RoDateTime_getLocalMonth_I_k_() as Integer
    return m.get_native().GetLocalMonth()
end function

function RoDateTime_getLocalDayOfMonth_I_k_() as Integer
    return m.get_native().GetLocalDayOfMonth()
end function

function RoDateTime_getLocalDayOfWeek_I_k_() as Integer
    return m.get_native().GetLocalDayOfWeek()
end function

function RoDateTime_getLocalHours_I_k_() as Integer
    return m.get_native().GetLocalHours()
end function

function RoDateTime_getLocalMinutes_I_k_() as Integer
    return m.get_native().GetLocalMinutes()
end function

function RoDateTime_getLocalSeconds_I_k_() as Integer
    return m.get_native().GetLocalSeconds()
end function

function RoDateTime_getWeekday_Str_k_() as String
    return m.get_native().GetWeekday()
end function

function RoDateTime_getLastDayOfMonth_I_k_() as Integer
    return m.get_native().GetLastDayOfMonth()
end function

function RoDateTime_toString_Str_k_() as String
    return m.toISOString_Str_k_()
end function

function RoDateTime_get_native_Dynamic_k_() as Object
    return m.native
end function

function RoTimespan_create_RoTimespan_k_() as Object
    this = {}
    this.__type = "RoTimespan"
    this.__proto = ["RoTimespan"]
    this.__id = __kotlin_nextObjectId()
    this.mark = RoTimespan_mark
    this.totalMilliseconds_I_k_ = RoTimespan_totalMilliseconds_I_k_
    this.totalSeconds_I_k_ = RoTimespan_totalSeconds_I_k_
    this.getSecondsToHere_PairII_k_ = RoTimespan_getSecondsToHere_PairII_k_
    this.get_native = RoTimespan_get_native_Dynamic_k_
    this.native = CreateObject("roTimespan")
    return this
end function

sub RoTimespan_mark()
    m.get_native().Mark()
end sub

function RoTimespan_totalMilliseconds_I_k_() as Integer
    return m.get_native().TotalMilliseconds()
end function

function RoTimespan_totalSeconds_I_k_() as Integer
    return m.get_native().TotalSeconds()
end function

function RoTimespan_getSecondsToHere_PairII_k_() as Object
    total = m.totalMilliseconds_I_k_()
    return Pair_create_AnyN_AnyN_PairAnyNAnyN_k_(total / 1000, total mod 1000)
end function

function RoTimespan_get_native_Dynamic_k_() as Object
    return m.native
end function
