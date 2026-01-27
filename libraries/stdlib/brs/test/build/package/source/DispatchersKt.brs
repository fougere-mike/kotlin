function Dispatchers_create_k_() as Object
    this = {}
    this.__type = "Dispatchers"
    this.__proto = ["Dispatchers"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_Default = Dispatchers___get_Default_k_
    this.__get_Main = Dispatchers___get_Main_k_
    this.__get_Unconfined = Dispatchers___get_Unconfined_k_
    this.__get_IO = Dispatchers___get_IO_k_
    this.Default = DefaultDispatcher_getInstance()
    this.Main = DefaultDispatcher_getInstance()
    this.Unconfined = UnconfinedDispatcher_getInstance()
    this.IO = DefaultDispatcher_getInstance()
    return this
end function

function Dispatchers_getInstance() as Object
    if GetGlobalAA().Dispatchers_instance = invalid then
        GetGlobalAA().Dispatchers_instance = Dispatchers_create_k_()
    end if
    return GetGlobalAA().Dispatchers_instance
end function

function Dispatchers___get_Default_k_() as Object
    return m.Default
end function

function Dispatchers___get_Main_k_() as Object
    return m.Main
end function

function Dispatchers___get_Unconfined_k_() as Object
    return m.Unconfined
end function

function Dispatchers___get_IO_k_() as Object
    return m.IO
end function
