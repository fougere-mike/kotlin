function Throwable_create_StrN_ThrowableN_Throwable_k_(message as Dynamic, cause as Dynamic) as Object
    this = {}
    this.__type = "Throwable"
    this.__proto = ["Throwable"]
    this.message = message
    this.cause = cause
    this._stack = invalid
    this.captureStack = Throwable_captureStack
    this.getStack_StrN_k_ = Throwable_getStack_StrN_k_
    this.setStack_StrN_k_ = Throwable_setStack_StrN_k_
    this.toString_Str_k_ = Throwable_toString_Str_k_
    this.get_message = Throwable_get_message_StrN_k_
    this.get_cause = Throwable_get_cause_ThrowableN_k_
    this.get__stack = Throwable_get__stack_StrN_k_
    this.set__stack = Throwable_set__stack_StrN_k_
    return this
end function

function Throwable_create_StrN_Throwable_k_(message as Dynamic) as Object
    this = {}
    this.__type = "Throwable"
    this.__proto = ["Throwable"]
    this.message = message
    this.cause = cause
    this._stack = invalid
    this.captureStack = Throwable_captureStack
    this.getStack_StrN_k_ = Throwable_getStack_StrN_k_
    this.setStack_StrN_k_ = Throwable_setStack_StrN_k_
    this.toString_Str_k_ = Throwable_toString_Str_k_
    this.get_message = Throwable_get_message_StrN_k_
    this.get_cause = Throwable_get_cause_ThrowableN_k_
    this.get__stack = Throwable_get__stack_StrN_k_
    this.set__stack = Throwable_set__stack_StrN_k_
    return this
end function

function Throwable_create_ThrowableN_Throwable_k_(cause as Dynamic) as Object
    this = {}
    this.__type = "Throwable"
    this.__proto = ["Throwable"]
    this.message = message
    this.cause = cause
    this._stack = invalid
    this.captureStack = Throwable_captureStack
    this.getStack_StrN_k_ = Throwable_getStack_StrN_k_
    this.setStack_StrN_k_ = Throwable_setStack_StrN_k_
    this.toString_Str_k_ = Throwable_toString_Str_k_
    this.get_message = Throwable_get_message_StrN_k_
    this.get_cause = Throwable_get_cause_ThrowableN_k_
    this.get__stack = Throwable_get__stack_StrN_k_
    this.set__stack = Throwable_set__stack_StrN_k_
    return this
end function

function Throwable_create_Throwable_k_() as Object
    this = {}
    this.__type = "Throwable"
    this.__proto = ["Throwable"]
    this.message = message
    this.cause = cause
    this._stack = invalid
    this.captureStack = Throwable_captureStack
    this.getStack_StrN_k_ = Throwable_getStack_StrN_k_
    this.setStack_StrN_k_ = Throwable_setStack_StrN_k_
    this.toString_Str_k_ = Throwable_toString_Str_k_
    this.get_message = Throwable_get_message_StrN_k_
    this.get_cause = Throwable_get_cause_ThrowableN_k_
    this.get__stack = Throwable_get__stack_StrN_k_
    this.set__stack = Throwable_set__stack_StrN_k_
    return this
end function

sub Throwable_captureStack()
    m._stack = invalid
end sub

function Throwable_getStack_StrN_k_() as Dynamic
    return m._stack
end function

sub Throwable_setStack_StrN_k_(stack as Dynamic)
    m._stack = stack
end sub

function Throwable_toString_Str_k_() as String
    tmp0_elvis_lhs = "/* Unsupported: IrGetClassImpl */".simpleName
    __when_tmp1 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp1 = "Throwable"
    else if true then
        __when_tmp1 = tmp0_elvis_lhs
    end if
    className = __when_tmp1

    __when_tmp2 = invalid
    if m.message <> invalid then
        __when_tmp2 = ((className + ": ") + m.message)
    else if true then
        __when_tmp2 = className
    end if
    return __when_tmp2

end function

function Throwable_get_message_StrN_k_() as Dynamic
    return m.message
end function

function Throwable_get_cause_ThrowableN_k_() as Dynamic
    return m.cause
end function

function Throwable_get__stack_StrN_k_() as Dynamic
    return m._stack
end function

sub Throwable_set__stack_StrN_k_(value as Dynamic)
    m._stack = value
end sub
