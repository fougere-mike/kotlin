function Lazy_isInitialized_Z_k_() as Boolean
end function

function Lazy_get_value_AnyN_k_() as Dynamic
end function

function LazyThreadSafetyMode_create_LazyThreadSafetyMode_k_(__name as String, __ordinal as Integer) as Object
    this = {}
    this.__type = "LazyThreadSafetyMode"
    this.name = __name
    this.ordinal = __ordinal
    this.values_Arr_k_ = LazyThreadSafetyMode_values_Arr_k_
    this.valueOf_Str_LazyThreadSafetyMode_k_ = LazyThreadSafetyMode_valueOf_Str_LazyThreadSafetyMode_k_
    this.get_entries = LazyThreadSafetyMode_get_entries_EnumEntries_k_
    return this
end function

sub LazyThreadSafetyMode_initEntries()
    if m.LazyThreadSafetyMode_entriesInitialized then
        return
    end if
    m.LazyThreadSafetyMode_entriesInitialized = true
    m.LazyThreadSafetyMode_SYNCHRONIZED = LazyThreadSafetyMode_create_LazyThreadSafetyMode_k_("SYNCHRONIZED", 0)
    m.LazyThreadSafetyMode_PUBLICATION = LazyThreadSafetyMode_create_LazyThreadSafetyMode_k_("PUBLICATION", 1)
    m.LazyThreadSafetyMode_NONE = LazyThreadSafetyMode_create_LazyThreadSafetyMode_k_("NONE", 2)
end sub

function LazyThreadSafetyMode_values() as Object
    LazyThreadSafetyMode_initEntries()
    return [m.LazyThreadSafetyMode_SYNCHRONIZED, m.LazyThreadSafetyMode_PUBLICATION, m.LazyThreadSafetyMode_NONE]
end function

function LazyThreadSafetyMode_valueOf(name as String) as Object
    LazyThreadSafetyMode_initEntries()
    if name = "SYNCHRONIZED" then
        return m.LazyThreadSafetyMode_SYNCHRONIZED
    else if name = "PUBLICATION" then
        return m.LazyThreadSafetyMode_PUBLICATION
    else if name = "NONE" then
        return m.LazyThreadSafetyMode_NONE
    else
        return invalid
    end if
end function
