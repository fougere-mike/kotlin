function CoroutineSingletons_create_k_(__name as String, __ordinal as Integer) as Object
    this = {}
    this.__type = "CoroutineSingletons"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["CoroutineSingletons", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

sub CoroutineSingletons_initEntries()
    if m.CoroutineSingletons_entriesInitialized = true then
        return
    end if
    m.CoroutineSingletons_entriesInitialized = true
    m.CoroutineSingletons_COROUTINE_SUSPENDED = CoroutineSingletons_create_k_("COROUTINE_SUSPENDED", 0)
    m.CoroutineSingletons_UNDECIDED = CoroutineSingletons_create_k_("UNDECIDED", 1)
    m.CoroutineSingletons_RESUMED = CoroutineSingletons_create_k_("RESUMED", 2)
end sub

function CoroutineSingletons_values() as Object
    CoroutineSingletons_initEntries()
    return [m.CoroutineSingletons_COROUTINE_SUSPENDED, m.CoroutineSingletons_UNDECIDED, m.CoroutineSingletons_RESUMED]
end function

function CoroutineSingletons_valueOf(name as String) as Object
    CoroutineSingletons_initEntries()
    if name = "COROUTINE_SUSPENDED" then
        return m.CoroutineSingletons_COROUTINE_SUSPENDED
    else if name = "UNDECIDED" then
        return m.CoroutineSingletons_UNDECIDED
    else if name = "RESUMED" then
        return m.CoroutineSingletons_RESUMED
    else
        return invalid
    end if
end function

function __get_COROUTINE_SUSPENDED_k_() as Object
    return [CoroutineSingletons_initEntries(), m.CoroutineSingletons_COROUTINE_SUSPENDED][1]
end function
