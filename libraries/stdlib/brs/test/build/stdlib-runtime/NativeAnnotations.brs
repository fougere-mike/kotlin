function NativeName_create_Str_k_(name as String) as Object
    this = {}
    this.__type = "NativeName"
    this.__proto = ["NativeName", "Annotation"]
    this.__id = __kotlin_nextObjectId()
    this.get_name = NativeName_get_name_k_
    this.name = name
    return this
end function

function NativeName_get_name_k_() as String
    return m.name
end function
