function Test_create_Test_k_() as Object
    this = {}
    this.__type = "Test"
    this.__proto = ["Test"]
    return this
end function

function Ignore_create_Str_Ignore_k_(reason = "") as Object
    this = {}
    this.__type = "Ignore"
    this.__proto = ["Ignore"]
    this.reason = reason
    this.get_reason = Ignore_get_reason_Str_k_
    return this
end function

function Ignore_get_reason_Str_k_() as String
    return m.reason
end function

function BeforeTest_create_BeforeTest_k_() as Object
    this = {}
    this.__type = "BeforeTest"
    this.__proto = ["BeforeTest"]
    return this
end function

function AfterTest_create_AfterTest_k_() as Object
    this = {}
    this.__type = "AfterTest"
    this.__proto = ["AfterTest"]
    return this
end function
