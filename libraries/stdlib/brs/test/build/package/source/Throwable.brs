function Throwable_create_StrN_ThrowableN_k_(message as Dynamic, cause as Dynamic) as Object
    this = {}
    this.__type = "Throwable"
    this.__proto = ["Throwable"]
    this.__id = __kotlin_nextObjectId()
    this.captureStack_k_ = Throwable_captureStack_k_
    this.getStack_k_ = Throwable_getStack_k_
    this.setStack_StrN_k_ = Throwable_setStack_StrN_k_
    this.toString_k_ = Throwable_toString_k_
    this.toString = Throwable_toString_k_
    this.get_message = Throwable_get_message_k_
    this.get_cause = Throwable_get_cause_k_
    this.get__stack = Throwable_get__stack_k_
    this.set__stack = Throwable_set__stack_StrN_k_
    this.message = message
    this.cause = cause
    this._stack = invalid
    return this
end function

function Throwable_create_StrN_k_(message as Dynamic) as Object
    return Throwable_create_StrN_ThrowableN_k_(message, invalid)
end function

function Throwable_create_ThrowableN_k_(cause as Dynamic) as Object
    tmp0_safe_receiver = cause
    __when_tmp0 = invalid
    if tmp0_safe_receiver = invalid then
        __when_tmp0 = invalid
    else if true then
        __when_tmp0 = toString_AnyN_k_(tmp0_safe_receiver)
    end if
    return Throwable_create_StrN_ThrowableN_k_(__when_tmp0, cause)
end function

function Throwable_create_k_() as Object
    return Throwable_create_StrN_ThrowableN_k_(invalid, invalid)
end function

sub Throwable_captureStack_k_()
    m.set__stack(invalid)
end sub

function Throwable_getStack_k_() as Dynamic
    return m.get__stack()
end function

sub Throwable_setStack_StrN_k_(stack as Dynamic)
    m.set__stack(stack)
end sub

function Throwable_toString_k_() as String
    tmp0_elvis_lhs = "/* Unsupported: IrGetClassImpl */".get_simpleName()
    __when_tmp1 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp1 = "Throwable"
    else if true then
        __when_tmp1 = tmp0_elvis_lhs
    end if
    className = __when_tmp1

    __when_tmp2 = invalid
    if m.get_message() <> invalid then
        __when_tmp2 = ((className + ": ") + m.get_message())
    else if true then
        __when_tmp2 = className
    end if
    return __when_tmp2

end function

function Throwable_get_message_k_() as Dynamic
    return m.message
end function

function Throwable_get_cause_k_() as Dynamic
    return m.cause
end function

function Throwable_get__stack_k_() as Dynamic
    return m._stack
end function

sub Throwable_set__stack_StrN_k_(value as Dynamic)
    m._stack = value
end sub
