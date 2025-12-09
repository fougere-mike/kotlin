function ExperimentalContracts_create_ExperimentalContracts_k_() as Object
    this = {}
    this.__type = "ExperimentalContracts"
    this.__proto = ["ExperimentalContracts"]
    return this
end function

sub contract_Function1ContractBuilderV_k_(builder as Object)
end sub

function ContractBuilder_returns_Returns_k_() as Object
end function

function ContractBuilder_returns_AnyN_Returns_k_(value as Dynamic) as Object
end function

function ContractBuilder_returnsNotNull_ReturnsNotNull_k_() as Object
end function

function ContractBuilder_callsInPlace_FunctionAnyN_InvocationKind_CallsInPlace_k_(lambda as Object, kind = InvocationKind_UNKNOWN) as Object
end function

function InvocationKind_create_InvocationKind_k_(__name as String, __ordinal as Integer) as Object
    this = {}
    this.__type = "InvocationKind"
    this.name = __name
    this.ordinal = __ordinal
    this.values_Arr_k_ = InvocationKind_values_Arr_k_
    this.valueOf_Str_InvocationKind_k_ = InvocationKind_valueOf_Str_InvocationKind_k_
    this.get_entries = InvocationKind_get_entries_EnumEntries_k_
    return this
end function

sub InvocationKind_initEntries()
    if m.InvocationKind_entriesInitialized then
        return
    end if
    m.InvocationKind_entriesInitialized = true
    m.InvocationKind_AT_MOST_ONCE = InvocationKind_create_InvocationKind_k_("AT_MOST_ONCE", 0)
    m.InvocationKind_EXACTLY_ONCE = InvocationKind_create_InvocationKind_k_("EXACTLY_ONCE", 1)
    m.InvocationKind_AT_LEAST_ONCE = InvocationKind_create_InvocationKind_k_("AT_LEAST_ONCE", 2)
    m.InvocationKind_UNKNOWN = InvocationKind_create_InvocationKind_k_("UNKNOWN", 3)
end sub

function InvocationKind_values() as Object
    InvocationKind_initEntries()
    return [m.InvocationKind_AT_MOST_ONCE, m.InvocationKind_EXACTLY_ONCE, m.InvocationKind_AT_LEAST_ONCE, m.InvocationKind_UNKNOWN]
end function

function InvocationKind_valueOf(name as String) as Object
    InvocationKind_initEntries()
    if name = "AT_MOST_ONCE" then
        return m.InvocationKind_AT_MOST_ONCE
    else if name = "EXACTLY_ONCE" then
        return m.InvocationKind_EXACTLY_ONCE
    else if name = "AT_LEAST_ONCE" then
        return m.InvocationKind_AT_LEAST_ONCE
    else if name = "UNKNOWN" then
        return m.InvocationKind_UNKNOWN
    else
        return invalid
    end if
end function

function SimpleEffect_implies_Z_ConditionalEffect_k_(booleanExpression as Boolean) as Object
end function
