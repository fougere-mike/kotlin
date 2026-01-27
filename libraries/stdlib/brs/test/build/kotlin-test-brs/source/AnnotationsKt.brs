function Test_create_k_() as Object
    this = {}
    this.__type = "Test"
    this.__proto = ["Test", "Annotation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    return this
end function

function Ignore_create_Str_k_(reason = "") as Object
    this = {}
    this.__type = "Ignore"
    this.__proto = ["Ignore", "Annotation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_reason = Ignore___get_reason_k_
    this.reason = reason
    return this
end function

function Ignore___get_reason_k_() as String
    return m.reason
end function

function BeforeTest_create_k_() as Object
    this = {}
    this.__type = "BeforeTest"
    this.__proto = ["BeforeTest", "Annotation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    return this
end function

function AfterTest_create_k_() as Object
    this = {}
    this.__type = "AfterTest"
    this.__proto = ["AfterTest", "Annotation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    return this
end function
