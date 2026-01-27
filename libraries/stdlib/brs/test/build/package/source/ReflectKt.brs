function KType___get_classifier_k_() as Dynamic
end function

function KClass_isInstance_AnyN_k_(value as Dynamic) as Boolean
end function

function KClass_equals_AnyN_k_(other as Dynamic) as Boolean
end function

function KClass_hashCode_k_() as Integer
end function

function KClass___get_simpleName_k_() as Dynamic
end function

function KClass___get_qualifiedName_k_() as Dynamic
    return invalid
end function

function typeOf_k_() as Object
    return Anon_68d1aa0e_create_k_()
end function

function Anon_68d1aa0e_create_k_() as Object
    this = {}
    this.__type = "Anon_68d1aa0e"
    this.__proto = ["Anon_68d1aa0e", "KType"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_classifier = Anon_68d1aa0e___get_classifier_k_
    this.classifier = invalid
    return this
end function

function Anon_68d1aa0e___get_classifier_k_() as Dynamic
    return m.classifier
end function
