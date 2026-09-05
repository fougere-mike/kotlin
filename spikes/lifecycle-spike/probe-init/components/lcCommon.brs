' Shared helpers for the lifecycle spike components. Included via <script> by
' each component so they resolve in that component's scope.

' Appends one entry to the global init log (a string csv the scene reads).
sub lcLog(entry as String)
    if m.global <> invalid then
        if m.global.hasField("__lcLog") then m.global.__lcLog = m.global.__lcLog + entry + ";"
    end if
end sub

' Appends one entry to the global detach-watch log.
sub lcWatch(entry as String)
    if m.global <> invalid then
        if m.global.hasField("__lcWatch") then m.global.__lcWatch = m.global.__lcWatch + entry + ";"
    end if
end sub

function lcBool(b as Boolean) as String
    if b then return "valid"
    return "invalid"
end function

function lcStr(v as Dynamic) as String
    if v = invalid then return "invalid"
    if type(v) = "roString" or type(v) = "String" then return v
    if type(v) = "roBoolean" or type(v) = "Boolean" then
        if v then return "true" else return "false"
    end if
    return FormatJson(v)
end function
