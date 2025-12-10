function RoDateTime_create_k_() as Object
    this = {}
    this.__type = "RoDateTime"
    this.__proto = ["RoDateTime"]
    this.__id = __kotlin_nextObjectId()
    this.mark_k_ = RoDateTime_mark_k_
    this.asSeconds_k_ = RoDateTime_asSeconds_k_
    this.fromSeconds_J_k_ = RoDateTime_fromSeconds_J_k_
    this.toISOString_k_ = RoDateTime_toISOString_k_
    this.fromISO8601String_Str_k_ = RoDateTime_fromISO8601String_Str_k_
    this.getYear_k_ = RoDateTime_getYear_k_
    this.getMonth_k_ = RoDateTime_getMonth_k_
    this.getDayOfMonth_k_ = RoDateTime_getDayOfMonth_k_
    this.getDayOfWeek_k_ = RoDateTime_getDayOfWeek_k_
    this.getHours_k_ = RoDateTime_getHours_k_
    this.getMinutes_k_ = RoDateTime_getMinutes_k_
    this.getSeconds_k_ = RoDateTime_getSeconds_k_
    this.getMilliseconds_k_ = RoDateTime_getMilliseconds_k_
    this.getTimeZoneOffset_k_ = RoDateTime_getTimeZoneOffset_k_
    this.getLocalYear_k_ = RoDateTime_getLocalYear_k_
    this.getLocalMonth_k_ = RoDateTime_getLocalMonth_k_
    this.getLocalDayOfMonth_k_ = RoDateTime_getLocalDayOfMonth_k_
    this.getLocalDayOfWeek_k_ = RoDateTime_getLocalDayOfWeek_k_
    this.getLocalHours_k_ = RoDateTime_getLocalHours_k_
    this.getLocalMinutes_k_ = RoDateTime_getLocalMinutes_k_
    this.getLocalSeconds_k_ = RoDateTime_getLocalSeconds_k_
    this.getWeekday_k_ = RoDateTime_getWeekday_k_
    this.getLastDayOfMonth_k_ = RoDateTime_getLastDayOfMonth_k_
    this.toString_k_ = RoDateTime_toString_k_
    this.toString = RoDateTime_toString_k_
    this.get_native = RoDateTime_get_native_k_
    this.native = CreateObject("roDateTime")
    return this
end function

sub RoDateTime_mark_k_()
    m.get_native().Mark()
end sub

function RoDateTime_asSeconds_k_() as LongInteger
    return m.get_native().AsSeconds()
end function

sub RoDateTime_fromSeconds_J_k_(seconds as LongInteger)
    m.get_native().FromSeconds(seconds)
end sub

function RoDateTime_toISOString_k_() as String
    return m.get_native().ToISOString()
end function

function RoDateTime_fromISO8601String_Str_k_(iso as String) as Boolean
    return m.get_native().FromISO8601String(iso)
end function

function RoDateTime_getYear_k_() as Integer
    return m.get_native().GetYear()
end function

function RoDateTime_getMonth_k_() as Integer
    return m.get_native().GetMonth()
end function

function RoDateTime_getDayOfMonth_k_() as Integer
    return m.get_native().GetDayOfMonth()
end function

function RoDateTime_getDayOfWeek_k_() as Integer
    return m.get_native().GetDayOfWeek()
end function

function RoDateTime_getHours_k_() as Integer
    return m.get_native().GetHours()
end function

function RoDateTime_getMinutes_k_() as Integer
    return m.get_native().GetMinutes()
end function

function RoDateTime_getSeconds_k_() as Integer
    return m.get_native().GetSeconds()
end function

function RoDateTime_getMilliseconds_k_() as Integer
    return m.get_native().GetMilliseconds()
end function

function RoDateTime_getTimeZoneOffset_k_() as Integer
    return m.get_native().GetTimeZoneOffset()
end function

function RoDateTime_getLocalYear_k_() as Integer
    return m.get_native().GetLocalYear()
end function

function RoDateTime_getLocalMonth_k_() as Integer
    return m.get_native().GetLocalMonth()
end function

function RoDateTime_getLocalDayOfMonth_k_() as Integer
    return m.get_native().GetLocalDayOfMonth()
end function

function RoDateTime_getLocalDayOfWeek_k_() as Integer
    return m.get_native().GetLocalDayOfWeek()
end function

function RoDateTime_getLocalHours_k_() as Integer
    return m.get_native().GetLocalHours()
end function

function RoDateTime_getLocalMinutes_k_() as Integer
    return m.get_native().GetLocalMinutes()
end function

function RoDateTime_getLocalSeconds_k_() as Integer
    return m.get_native().GetLocalSeconds()
end function

function RoDateTime_getWeekday_k_() as String
    return m.get_native().GetWeekday()
end function

function RoDateTime_getLastDayOfMonth_k_() as Integer
    return m.get_native().GetLastDayOfMonth()
end function

function RoDateTime_toString_k_() as String
    return m.toISOString_k_()
end function

function RoDateTime_get_native_k_() as Object
    return m.native
end function

function RoTimespan_create_k_() as Object
    this = {}
    this.__type = "RoTimespan"
    this.__proto = ["RoTimespan"]
    this.__id = __kotlin_nextObjectId()
    this.mark_k_ = RoTimespan_mark_k_
    this.totalMilliseconds_k_ = RoTimespan_totalMilliseconds_k_
    this.totalSeconds_k_ = RoTimespan_totalSeconds_k_
    this.getSecondsToHere_k_ = RoTimespan_getSecondsToHere_k_
    this.get_native = RoTimespan_get_native_k_
    this.native = CreateObject("roTimespan")
    return this
end function

sub RoTimespan_mark_k_()
    m.get_native().Mark()
end sub

function RoTimespan_totalMilliseconds_k_() as Integer
    return m.get_native().TotalMilliseconds()
end function

function RoTimespan_totalSeconds_k_() as Integer
    return m.get_native().TotalSeconds()
end function

function RoTimespan_getSecondsToHere_k_() as Object
    total = m.totalMilliseconds_k_()
    return Pair_create_AnyN_AnyN_k_(total / 1000, total mod 1000)
end function

function RoTimespan_get_native_k_() as Object
    return m.native
end function
