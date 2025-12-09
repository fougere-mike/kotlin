sub println()
    print("")
end sub

sub println_AnyN_k_(message as Dynamic)
    print(toString_AnyN_Str_k_(message))
end sub

sub print_AnyN_k_(message as Dynamic)
    print toString_AnyN_Str_k_(message);
end sub

function readln_Str_k_() as String
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("readln is not supported in BrightScript")
end function

function readlnOrNull_StrN_k_() as Dynamic
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("readlnOrNull is not supported in BrightScript")
end function
