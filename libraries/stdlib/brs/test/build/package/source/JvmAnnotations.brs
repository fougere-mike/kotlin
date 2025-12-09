function JvmName_create_Str_JvmName_k_(name as String) as Object
    this = {}
    this.__type = "JvmName"
    this.__proto = ["JvmName", "Annotation"]
    this.__id = __kotlin_nextObjectId()
    this.get_name = JvmName_get_name_Str_k_
    this.name = name
    return this
end function

function JvmName_get_name_Str_k_() as String
    return m.name
end function

function JvmField_create_JvmField_k_() as Object
    this = {}
    this.__type = "JvmField"
    this.__proto = ["JvmField", "Annotation"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function JvmSynthetic_create_JvmSynthetic_k_() as Object
    this = {}
    this.__type = "JvmSynthetic"
    this.__proto = ["JvmSynthetic", "Annotation"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function JvmMultifileClass_create_JvmMultifileClass_k_() as Object
    this = {}
    this.__type = "JvmMultifileClass"
    this.__proto = ["JvmMultifileClass", "Annotation"]
    this.__id = __kotlin_nextObjectId()
    return this
end function
