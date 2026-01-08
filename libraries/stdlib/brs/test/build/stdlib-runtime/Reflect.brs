function KType_get_classifier_k_() as Dynamic
end function

function KClass_get_simpleName_k_() as Dynamic
end function

function typeOf_k_() as Object
    return Anon_4a61a664_create_k_()
end function

function Anon_4a61a664_create_k_() as Object
    this = {}
    this.__type = "Anon_4a61a664"
    this.__proto = ["Anon_4a61a664", "KType"]
    this.__id = __kotlin_nextObjectId()
    this.get_classifier = Anon_4a61a664_get_classifier_k_
    this.classifier = invalid
    return this
end function

function Anon_4a61a664_get_classifier_k_() as Dynamic
    return m.classifier
end function
