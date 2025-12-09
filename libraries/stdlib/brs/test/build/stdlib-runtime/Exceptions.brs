function __kotlin_isInstanceOf(obj as Object, typeName as String) as Boolean
    if obj = invalid then
        return false
    end if
    if Type(obj) <> "roAssociativeArray" then
        return false
    end if
    proto = obj.__proto
    if proto = invalid then
        return false
    end if
    for each t in proto
        if t = typeName then
            return true
        end if
    end for
    return false
end function

function Error_create_Error_k_() as Object
    this = Throwable_create_Throwable_k_()
    this._super = {}
    this.__proto = ["Error", this.__proto]
    this.__type = "Error"
    return this
end function

function Error_create_StrN_Error_k_(message as Dynamic) as Object
    this = Throwable_create_StrN_Throwable_k_(message)
    this._super = {}
    this.__proto = ["Error", this.__proto]
    this.__type = "Error"
    return this
end function

function Error_create_StrN_ThrowableN_Error_k_(message as Dynamic, cause as Dynamic) as Object
    this = Throwable_create_StrN_ThrowableN_Throwable_k_(message, cause)
    this._super = {}
    this.__proto = ["Error", this.__proto]
    this.__type = "Error"
    return this
end function

function Error_create_ThrowableN_Error_k_(cause as Dynamic) as Object
    this = Throwable_create_ThrowableN_Throwable_k_(cause)
    this._super = {}
    this.__proto = ["Error", this.__proto]
    this.__type = "Error"
    return this
end function

function Exception_create_Exception_k_() as Object
    this = Throwable_create_Throwable_k_()
    this._super = {}
    this.__proto = ["Exception", this.__proto]
    this.__type = "Exception"
    return this
end function

function Exception_create_StrN_Exception_k_(message as Dynamic) as Object
    this = Throwable_create_StrN_Throwable_k_(message)
    this._super = {}
    this.__proto = ["Exception", this.__proto]
    this.__type = "Exception"
    return this
end function

function Exception_create_StrN_ThrowableN_Exception_k_(message as Dynamic, cause as Dynamic) as Object
    this = Throwable_create_StrN_ThrowableN_Throwable_k_(message, cause)
    this._super = {}
    this.__proto = ["Exception", this.__proto]
    this.__type = "Exception"
    return this
end function

function Exception_create_ThrowableN_Exception_k_(cause as Dynamic) as Object
    this = Throwable_create_ThrowableN_Throwable_k_(cause)
    this._super = {}
    this.__proto = ["Exception", this.__proto]
    this.__type = "Exception"
    return this
end function

function RuntimeException_create_RuntimeException_k_() as Object
    this = Exception_create_Exception_k_()
    this._super = {}
    this.__proto = ["RuntimeException", this.__proto]
    this.__type = "RuntimeException"
    return this
end function

function RuntimeException_create_StrN_RuntimeException_k_(message as Dynamic) as Object
    this = Exception_create_StrN_Exception_k_(message)
    this._super = {}
    this.__proto = ["RuntimeException", this.__proto]
    this.__type = "RuntimeException"
    return this
end function

function RuntimeException_create_StrN_ThrowableN_RuntimeException_k_(message as Dynamic, cause as Dynamic) as Object
    this = Exception_create_StrN_ThrowableN_Exception_k_(message, cause)
    this._super = {}
    this.__proto = ["RuntimeException", this.__proto]
    this.__type = "RuntimeException"
    return this
end function

function RuntimeException_create_ThrowableN_RuntimeException_k_(cause as Dynamic) as Object
    this = Exception_create_ThrowableN_Exception_k_(cause)
    this._super = {}
    this.__proto = ["RuntimeException", this.__proto]
    this.__type = "RuntimeException"
    return this
end function

function IllegalArgumentException_create_IllegalArgumentException_k_() as Object
    this = RuntimeException_create_RuntimeException_k_()
    this._super = {}
    this.__proto = ["IllegalArgumentException", this.__proto]
    this.__type = "IllegalArgumentException"
    return this
end function

function IllegalArgumentException_create_StrN_IllegalArgumentException_k_(message as Dynamic) as Object
    this = RuntimeException_create_StrN_RuntimeException_k_(message)
    this._super = {}
    this.__proto = ["IllegalArgumentException", this.__proto]
    this.__type = "IllegalArgumentException"
    return this
end function

function IllegalArgumentException_create_StrN_ThrowableN_IllegalArgumentException_k_(message as Dynamic, cause as Dynamic) as Object
    this = RuntimeException_create_StrN_ThrowableN_RuntimeException_k_(message, cause)
    this._super = {}
    this.__proto = ["IllegalArgumentException", this.__proto]
    this.__type = "IllegalArgumentException"
    return this
end function

function IllegalArgumentException_create_ThrowableN_IllegalArgumentException_k_(cause as Dynamic) as Object
    this = RuntimeException_create_ThrowableN_RuntimeException_k_(cause)
    this._super = {}
    this.__proto = ["IllegalArgumentException", this.__proto]
    this.__type = "IllegalArgumentException"
    return this
end function

function IllegalStateException_create_IllegalStateException_k_() as Object
    this = RuntimeException_create_RuntimeException_k_()
    this._super = {}
    this.__proto = ["IllegalStateException", this.__proto]
    this.__type = "IllegalStateException"
    return this
end function

function IllegalStateException_create_StrN_IllegalStateException_k_(message as Dynamic) as Object
    this = RuntimeException_create_StrN_RuntimeException_k_(message)
    this._super = {}
    this.__proto = ["IllegalStateException", this.__proto]
    this.__type = "IllegalStateException"
    return this
end function

function IllegalStateException_create_StrN_ThrowableN_IllegalStateException_k_(message as Dynamic, cause as Dynamic) as Object
    this = RuntimeException_create_StrN_ThrowableN_RuntimeException_k_(message, cause)
    this._super = {}
    this.__proto = ["IllegalStateException", this.__proto]
    this.__type = "IllegalStateException"
    return this
end function

function IllegalStateException_create_ThrowableN_IllegalStateException_k_(cause as Dynamic) as Object
    this = RuntimeException_create_ThrowableN_RuntimeException_k_(cause)
    this._super = {}
    this.__proto = ["IllegalStateException", this.__proto]
    this.__type = "IllegalStateException"
    return this
end function

function IndexOutOfBoundsException_create_IndexOutOfBoundsException_k_() as Object
    this = RuntimeException_create_RuntimeException_k_()
    this._super = {}
    this.__proto = ["IndexOutOfBoundsException", this.__proto]
    this.__type = "IndexOutOfBoundsException"
    return this
end function

function IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_(message as Dynamic) as Object
    this = RuntimeException_create_StrN_RuntimeException_k_(message)
    this._super = {}
    this.__proto = ["IndexOutOfBoundsException", this.__proto]
    this.__type = "IndexOutOfBoundsException"
    return this
end function

function ConcurrentModificationException_create_ConcurrentModificationException_k_() as Object
    this = RuntimeException_create_RuntimeException_k_()
    this._super = {}
    this.__proto = ["ConcurrentModificationException", this.__proto]
    this.__type = "ConcurrentModificationException"
    return this
end function

function ConcurrentModificationException_create_StrN_ConcurrentModificationException_k_(message as Dynamic) as Object
    this = RuntimeException_create_StrN_RuntimeException_k_(message)
    this._super = {}
    this.__proto = ["ConcurrentModificationException", this.__proto]
    this.__type = "ConcurrentModificationException"
    return this
end function

function ConcurrentModificationException_create_StrN_ThrowableN_ConcurrentModificationException_k_(message as Dynamic, cause as Dynamic) as Object
    this = RuntimeException_create_StrN_ThrowableN_RuntimeException_k_(message, cause)
    this._super = {}
    this.__proto = ["ConcurrentModificationException", this.__proto]
    this.__type = "ConcurrentModificationException"
    return this
end function

function ConcurrentModificationException_create_ThrowableN_ConcurrentModificationException_k_(cause as Dynamic) as Object
    this = RuntimeException_create_ThrowableN_RuntimeException_k_(cause)
    this._super = {}
    this.__proto = ["ConcurrentModificationException", this.__proto]
    this.__type = "ConcurrentModificationException"
    return this
end function

function UnsupportedOperationException_create_UnsupportedOperationException_k_() as Object
    this = RuntimeException_create_RuntimeException_k_()
    this._super = {}
    this.__proto = ["UnsupportedOperationException", this.__proto]
    this.__type = "UnsupportedOperationException"
    return this
end function

function UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_(message as Dynamic) as Object
    this = RuntimeException_create_StrN_RuntimeException_k_(message)
    this._super = {}
    this.__proto = ["UnsupportedOperationException", this.__proto]
    this.__type = "UnsupportedOperationException"
    return this
end function

function UnsupportedOperationException_create_StrN_ThrowableN_UnsupportedOperationException_k_(message as Dynamic, cause as Dynamic) as Object
    this = RuntimeException_create_StrN_ThrowableN_RuntimeException_k_(message, cause)
    this._super = {}
    this.__proto = ["UnsupportedOperationException", this.__proto]
    this.__type = "UnsupportedOperationException"
    return this
end function

function UnsupportedOperationException_create_ThrowableN_UnsupportedOperationException_k_(cause as Dynamic) as Object
    this = RuntimeException_create_ThrowableN_RuntimeException_k_(cause)
    this._super = {}
    this.__proto = ["UnsupportedOperationException", this.__proto]
    this.__type = "UnsupportedOperationException"
    return this
end function

function NoSuchElementException_create_NoSuchElementException_k_() as Object
    this = RuntimeException_create_RuntimeException_k_()
    this._super = {}
    this.__proto = ["NoSuchElementException", this.__proto]
    this.__type = "NoSuchElementException"
    return this
end function

function NoSuchElementException_create_StrN_NoSuchElementException_k_(message as Dynamic) as Object
    this = RuntimeException_create_StrN_RuntimeException_k_(message)
    this._super = {}
    this.__proto = ["NoSuchElementException", this.__proto]
    this.__type = "NoSuchElementException"
    return this
end function

function NumberFormatException_create_NumberFormatException_k_() as Object
    this = IllegalArgumentException_create_IllegalArgumentException_k_()
    this._super = {}
    this.__proto = ["NumberFormatException", this.__proto]
    this.__type = "NumberFormatException"
    return this
end function

function NumberFormatException_create_StrN_NumberFormatException_k_(message as Dynamic) as Object
    this = IllegalArgumentException_create_StrN_IllegalArgumentException_k_(message)
    this._super = {}
    this.__proto = ["NumberFormatException", this.__proto]
    this.__type = "NumberFormatException"
    return this
end function

function NullPointerException_create_NullPointerException_k_() as Object
    this = RuntimeException_create_RuntimeException_k_()
    this._super = {}
    this.__proto = ["NullPointerException", this.__proto]
    this.__type = "NullPointerException"
    return this
end function

function NullPointerException_create_StrN_NullPointerException_k_(message as Dynamic) as Object
    this = RuntimeException_create_StrN_RuntimeException_k_(message)
    this._super = {}
    this.__proto = ["NullPointerException", this.__proto]
    this.__type = "NullPointerException"
    return this
end function

function ClassCastException_create_ClassCastException_k_() as Object
    this = RuntimeException_create_RuntimeException_k_()
    this._super = {}
    this.__proto = ["ClassCastException", this.__proto]
    this.__type = "ClassCastException"
    return this
end function

function ClassCastException_create_StrN_ClassCastException_k_(message as Dynamic) as Object
    this = RuntimeException_create_StrN_RuntimeException_k_(message)
    this._super = {}
    this.__proto = ["ClassCastException", this.__proto]
    this.__type = "ClassCastException"
    return this
end function

function AssertionError_create_AssertionError_k_() as Object
    this = Error_create_Error_k_()
    this._super = {}
    this.__proto = ["AssertionError", this.__proto]
    this.__type = "AssertionError"
    return this
end function

function AssertionError_create_AnyN_AssertionError_k_(message as Dynamic) as Object
    tmp0_safe_receiver = message
    __when_tmp0 = invalid
    if tmp0_safe_receiver = invalid then
        __when_tmp0 = invalid
    else if true then
        __when_tmp0 = ((function(Str, tmp0_safe_receiver)
            if tmp0_safe_receiver = invalid then return "null" else return (function(Str, tmp0_safe_receiver)
                if (Type(tmp0_safe_receiver) = "String") or (Type(tmp0_safe_receiver) = "roString") then return tmp0_safe_receiver else return (function(Str, tmp0_safe_receiver)
                    if ((((((Type(tmp0_safe_receiver) = "Integer") or (Type(tmp0_safe_receiver) = "LongInteger")) or (Type(tmp0_safe_receiver) = "Float")) or (Type(tmp0_safe_receiver) = "Double")) or (Type(tmp0_safe_receiver) = "roInt")) or (Type(tmp0_safe_receiver) = "roFloat")) or (Type(tmp0_safe_receiver) = "roDouble") then return Str(tmp0_safe_receiver) else return (function(tmp0_safe_receiver)
                        if (Type(tmp0_safe_receiver) = "Boolean") or (Type(tmp0_safe_receiver) = "roBoolean") then return (function(tmp0_safe_receiver)
                            if tmp0_safe_receiver then return "true" else return "false"
                        end function)(tmp0_safe_receiver) else return tmp0_safe_receiver.toString()
                    end function)(tmp0_safe_receiver)
                end function)(Str, tmp0_safe_receiver)
            end function)(Str, tmp0_safe_receiver)
        end function)(Str, tmp0_safe_receiver))
    end if
    this = Error_create_StrN_Error_k_(__when_tmp0)
    this._super = {}
    this.__proto = ["AssertionError", this.__proto]
    this.__type = "AssertionError"
    return this
end function

function AssertionError_create_StrN_ThrowableN_AssertionError_k_(message as Dynamic, cause as Dynamic) as Object
    this = Error_create_StrN_ThrowableN_Error_k_(message, cause)
    this._super = {}
    this.__proto = ["AssertionError", this.__proto]
    this.__type = "AssertionError"
    return this
end function

function ArithmeticException_create_ArithmeticException_k_() as Object
    this = RuntimeException_create_RuntimeException_k_()
    this._super = {}
    this.__proto = ["ArithmeticException", this.__proto]
    this.__type = "ArithmeticException"
    return this
end function

function ArithmeticException_create_StrN_ArithmeticException_k_(message as Dynamic) as Object
    this = RuntimeException_create_StrN_RuntimeException_k_(message)
    this._super = {}
    this.__proto = ["ArithmeticException", this.__proto]
    this.__type = "ArithmeticException"
    return this
end function

function NoWhenBranchMatchedException_create_NoWhenBranchMatchedException_k_() as Object
    this = RuntimeException_create_RuntimeException_k_()
    this._super = {}
    this.__proto = ["NoWhenBranchMatchedException", this.__proto]
    this.__type = "NoWhenBranchMatchedException"
    return this
end function

function NoWhenBranchMatchedException_create_StrN_NoWhenBranchMatchedException_k_(message as Dynamic) as Object
    this = RuntimeException_create_StrN_RuntimeException_k_(message)
    this._super = {}
    this.__proto = ["NoWhenBranchMatchedException", this.__proto]
    this.__type = "NoWhenBranchMatchedException"
    return this
end function

function NoWhenBranchMatchedException_create_StrN_ThrowableN_NoWhenBranchMatchedException_k_(message as Dynamic, cause as Dynamic) as Object
    this = RuntimeException_create_StrN_ThrowableN_RuntimeException_k_(message, cause)
    this._super = {}
    this.__proto = ["NoWhenBranchMatchedException", this.__proto]
    this.__type = "NoWhenBranchMatchedException"
    return this
end function

function NoWhenBranchMatchedException_create_ThrowableN_NoWhenBranchMatchedException_k_(cause as Dynamic) as Object
    this = RuntimeException_create_ThrowableN_RuntimeException_k_(cause)
    this._super = {}
    this.__proto = ["NoWhenBranchMatchedException", this.__proto]
    this.__type = "NoWhenBranchMatchedException"
    return this
end function

function UninitializedPropertyAccessException_create_UninitializedPropertyAccessException_k_() as Object
    this = RuntimeException_create_RuntimeException_k_()
    this._super = {}
    this.__proto = ["UninitializedPropertyAccessException", this.__proto]
    this.__type = "UninitializedPropertyAccessException"
    return this
end function

function UninitializedPropertyAccessException_create_StrN_UninitializedPropertyAccessException_k_(message as Dynamic) as Object
    this = RuntimeException_create_StrN_RuntimeException_k_(message)
    this._super = {}
    this.__proto = ["UninitializedPropertyAccessException", this.__proto]
    this.__type = "UninitializedPropertyAccessException"
    return this
end function

function UninitializedPropertyAccessException_create_StrN_ThrowableN_UninitializedPropertyAccessException_k_(message as Dynamic, cause as Dynamic) as Object
    this = RuntimeException_create_StrN_ThrowableN_RuntimeException_k_(message, cause)
    this._super = {}
    this.__proto = ["UninitializedPropertyAccessException", this.__proto]
    this.__type = "UninitializedPropertyAccessException"
    return this
end function

function UninitializedPropertyAccessException_create_ThrowableN_UninitializedPropertyAccessException_k_(cause as Dynamic) as Object
    this = RuntimeException_create_ThrowableN_RuntimeException_k_(cause)
    this._super = {}
    this.__proto = ["UninitializedPropertyAccessException", this.__proto]
    this.__type = "UninitializedPropertyAccessException"
    return this
end function

function stackTraceToString_rThrowable_Str_k_(m as Object) as String
    sb = StringBuilder_create_StringBuilder_k_()
    sb.append_StrN_StringBuilder_k_(m.toString())
    sb.append_StrN_StringBuilder_k_(chr(10))
    tmp0_safe_receiver = (function(__kotlin_isInstanceOf)
        if __kotlin_isInstanceOf(m, "Throwable") then return m else return invalid
    end function)(__kotlin_isInstanceOf)
    __when_tmp1 = invalid
    if tmp0_safe_receiver = invalid then
        __when_tmp1 = invalid
    else if true then
        __when_tmp1 = tmp0_safe_receiver.getStack_StrN_k_()
    end if
    stack = __when_tmp1

    if stack <> invalid then
        sb.append_StrN_StringBuilder_k_(stack)
    end if
    suppressed = get_suppressedExceptions_rThrowable_Arr_k_(m)
    if suppressed.count() > 0 then
        i = 0
        while i < suppressed.count()
            sb.append_StrN_StringBuilder_k_(chr(10) + "Suppressed: ")
            sb.append_StrN_StringBuilder_k_(stackTraceToString_rThrowable_Str_k_(suppressed[i]))
            i = (i + 1)
        end while
    end if
    cause = m.get_cause()
    if cause <> invalid then
        sb.append_StrN_StringBuilder_k_(chr(10) + "Caused by: ")
        sb.append_StrN_StringBuilder_k_(stackTraceToString_rThrowable_Str_k_(cause))
    end if
    return sb.toString()
end function

sub printStackTrace_rThrowable_k_(m as Object)
    println_AnyN_k_(stackTraceToString_rThrowable_Str_k_(m))
end sub

sub addSuppressed_rThrowable_Throwable_k_(m as Object, exception as Object)
end sub

function get_suppressedExceptions_rThrowable_Arr_k_(m as Object) as Object
    return arrayOf_Arr_Arr_k_()
end function
