function DelayTracker_create_k_() as Object
    this = {}
    this.__type = "DelayTracker"
    this.__proto = ["DelayTracker"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.currentTimeMs_k_ = DelayTracker_currentTimeMs_k_
    this.register_J_Function0V_k_ = DelayTracker_register_J_Function0V_k_
    this.tick_k_ = DelayTracker_tick_k_
    this.hasPendingDelays_k_ = DelayTracker_hasPendingDelays_k_
    this.pendingCount_k_ = DelayTracker_pendingCount_k_
    this.__get_timespan = DelayTracker___get_timespan_k_
    this.__get_pendingDelays = DelayTracker___get_pendingDelays_k_
    this.timespan = CreateObject("roTimespan")
    this.pendingDelays = mutableListOf_k_()
    return this
end function

function DelayTracker_currentTimeMs_k_() as Integer
    return m.__get_timespan().totalMilliseconds()
end function

sub DelayTracker_register_J_Function0V_k_(delayMs as LongInteger, callback as Object)
    deadline = m.currentTimeMs_k_() + delayMs
    m.__get_pendingDelays().add_AnyN_k_(DelayTracker_PendingDelay_create_I_Function0V_k_(deadline, callback))
end sub

function DelayTracker_tick_k_() as Integer
    if m.__get_pendingDelays().isEmpty_k_() then
        return 0
    end if
    now = m.currentTimeMs_k_()
    firedCount = 0
    i = 0
    while i < m.__get_pendingDelays().__get_size()
        delay = m.__get_pendingDelays().get_I_k_(i)
        if now >= delay.__get_deadlineMs() then
            m.__get_pendingDelays().removeAt_I_k_(i)
            delay.__get_callback().invoke_k_()
            firedCount = (firedCount + 1)
        else if true then
            i = (i + 1)
        end if
    end while
    return firedCount
end function

function DelayTracker_hasPendingDelays_k_() as Boolean
    tmp0 = m.__get_pendingDelays()
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = not this.isEmpty_k_()
        exit while
    end while
    return tmp_ret_0

end function

function DelayTracker_pendingCount_k_() as Integer
    return m.__get_pendingDelays().__get_size()
end function

function DelayTracker___get_timespan_k_() as Object
    return m.timespan
end function

function DelayTracker___get_pendingDelays_k_() as Object
    return m.pendingDelays
end function

function DelayTracker_PendingDelay_create_I_Function0V_k_(deadlineMs as Integer, callback as Object) as Object
    this = {}
    this.__type = "DelayTracker_PendingDelay"
    this.__proto = ["DelayTracker_PendingDelay"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_deadlineMs = DelayTracker_PendingDelay___get_deadlineMs_k_
    this.__get_callback = DelayTracker_PendingDelay___get_callback_k_
    this.deadlineMs = deadlineMs
    this.callback = callback
    return this
end function

function DelayTracker_PendingDelay___get_deadlineMs_k_() as Integer
    return m.deadlineMs
end function

function DelayTracker_PendingDelay___get_callback_k_() as Object
    return m.callback
end function

function DelayTracker_Companion_create_k_() as Object
    this = {}
    this.__type = "DelayTracker_Companion"
    this.__proto = ["DelayTracker_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get__instance = DelayTracker_Companion___get__instance_k_
    this.__set__instance = DelayTracker_Companion___set__instance_DelayTrackerN_k_
    this.__get_current = DelayTracker_Companion___get_current_k_
    this._instance = invalid
    return this
end function

function DelayTracker_Companion_getInstance() as Object
    if GetGlobalAA().DelayTracker_Companion_instance = invalid then
        GetGlobalAA().DelayTracker_Companion_instance = DelayTracker_Companion_create_k_()
    end if
    return GetGlobalAA().DelayTracker_Companion_instance
end function

function DelayTracker_Companion___get__instance_k_() as Dynamic
    return m._instance
end function

sub DelayTracker_Companion___set__instance_DelayTrackerN_k_(value as Dynamic)
    m._instance = value
end sub

function DelayTracker_Companion___get_current_k_() as Object
    if DelayTracker_Companion_getInstance().__get__instance() = invalid then
        DelayTracker_Companion_getInstance().__set__instance(DelayTracker_create_k_())
    end if
    return DelayTracker_Companion_getInstance().__get__instance()
end function
