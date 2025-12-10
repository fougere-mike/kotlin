function RoRegistry_Companion_create_k_() as Object
    this = {}
    this.__type = "RoRegistry_Companion"
    this.__proto = ["RoRegistry_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoRegistry_Companion_getInstance() as Object
    if m.RoRegistry_Companion_instance = invalid then
        m.RoRegistry_Companion_instance = RoRegistry_Companion_create_k_()
    end if
    return m.RoRegistry_Companion_instance
end function

function RoRegistrySection_Companion_create_k_() as Object
    this = {}
    this.__type = "RoRegistrySection_Companion"
    this.__proto = ["RoRegistrySection_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoRegistrySection_Companion_getInstance() as Object
    if m.RoRegistrySection_Companion_instance = invalid then
        m.RoRegistrySection_Companion_instance = RoRegistrySection_Companion_create_k_()
    end if
    return m.RoRegistrySection_Companion_instance
end function

function RoPath_Companion_create_k_() as Object
    this = {}
    this.__type = "RoPath_Companion"
    this.__proto = ["RoPath_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoPath_Companion_getInstance() as Object
    if m.RoPath_Companion_instance = invalid then
        m.RoPath_Companion_instance = RoPath_Companion_create_k_()
    end if
    return m.RoPath_Companion_instance
end function

function RoFileSystem_Companion_create_k_() as Object
    this = {}
    this.__type = "RoFileSystem_Companion"
    this.__proto = ["RoFileSystem_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoFileSystem_Companion_getInstance() as Object
    if m.RoFileSystem_Companion_instance = invalid then
        m.RoFileSystem_Companion_instance = RoFileSystem_Companion_create_k_()
    end if
    return m.RoFileSystem_Companion_instance
end function
