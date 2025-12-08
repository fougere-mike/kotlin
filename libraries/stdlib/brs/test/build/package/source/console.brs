sub println()
    print("")
end sub

sub println_AnyN_k_(message as Dynamic)
    print((function(Str, message)
        if message = invalid then return "null" else return (function(Str, message)
            if (Type(message) = "String") or (Type(message) = "roString") then return message else return (function(Str, message)
                if ((((((Type(message) = "Integer") or (Type(message) = "LongInteger")) or (Type(message) = "Float")) or (Type(message) = "Double")) or (Type(message) = "roInt")) or (Type(message) = "roFloat")) or (Type(message) = "roDouble") then return Str(message) else return (function(message)
                    if (Type(message) = "Boolean") or (Type(message) = "roBoolean") then return (function(message)
                        if message then return "true" else return "false"
                    end function)(message) else return message.toString()
                end function)(message)
            end function)(Str, message)
        end function)(Str, message)
    end function)(Str, message))
end sub

sub print_AnyN_k_(message as Dynamic)
    print (function(Str, message)
        if message = invalid then return "null" else return (function(Str, message)
            if (Type(message) = "String") or (Type(message) = "roString") then return message else return (function(Str, message)
                if ((((((Type(message) = "Integer") or (Type(message) = "LongInteger")) or (Type(message) = "Float")) or (Type(message) = "Double")) or (Type(message) = "roInt")) or (Type(message) = "roFloat")) or (Type(message) = "roDouble") then return Str(message) else return (function(message)
                    if (Type(message) = "Boolean") or (Type(message) = "roBoolean") then return (function(message)
                        if message then return "true" else return "false"
                    end function)(message) else return message.toString()
                end function)(message)
            end function)(Str, message)
        end function)(Str, message)
    end function)(Str, message);
end sub

function readln_Str_k_() as String
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("readln is not supported in BrightScript")
end function

function readlnOrNull_StrN_k_() as Dynamic
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("readlnOrNull is not supported in BrightScript")
end function
