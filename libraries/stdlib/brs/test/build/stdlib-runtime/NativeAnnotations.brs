function NativeName_create_Str_NativeName_k_(name as String) as Object
    this = {}
    this.__type = "NativeName"
    this.__proto = ["NativeName"]
    this.name = name
    this.get_name = NativeName_get_name_Str_k_
    return this
end function

function NativeName_get_name_Str_k_() as String
    return m.name
end function
