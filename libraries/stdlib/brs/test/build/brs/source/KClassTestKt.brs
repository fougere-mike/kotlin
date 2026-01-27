function SimpleClass_create_k_() as Object
    this = {}
    this.__type = "SimpleClass"
    this.__proto = ["SimpleClass"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    return this
end function

function Animal_create_Str_k_(name as String) as Object
    this = {}
    this.__type = "Animal"
    this.__proto = ["Animal"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_name = Animal___get_name_k_
    this.name = name
    return this
end function

function Animal___get_name_k_() as String
    return m.name
end function

function Dog_create_Str_k_(name as String) as Object
    this = Animal_create_Str_k_(name)
    this._super = {}
    this.__proto = ["Dog", this.__proto]
    this.__type = "Dog"
    return this
end function

function Cat_create_Str_k_(name as String) as Object
    this = Animal_create_Str_k_(name)
    this._super = {}
    this.__proto = ["Cat", this.__proto]
    this.__type = "Cat"
    return this
end function

sub kclassTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("KClass", kclassTests_lambda_create_k_())
    m.suite_Str_Function1TestRunnerV_k_("KClass Native Types", kclassTests_lambda_1_create_k_())
end sub

function kclassTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda"
    this.__proto = ["kclassTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_("SimpleClass", __kotlin_KClass_create("SimpleClass").__get_simpleName(), invalid)
end sub

function kclassTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_1"
    this.__proto = ["kclassTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_1_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_1_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_("Animal", __kotlin_KClass_create("Animal").__get_simpleName(), invalid)
end sub

function kclassTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_2"
    this.__proto = ["kclassTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_2_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_2_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_("Dog", __kotlin_KClass_create("Dog").__get_simpleName(), invalid)
end sub

function kclassTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_3"
    this.__proto = ["kclassTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_3_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_3_invoke_k_()
    assertTrue_Z_StrN_k_(brsStructuralEquals_AnyN_AnyN_k_(__kotlin_KClass_create("SimpleClass"), __kotlin_KClass_create("SimpleClass")), invalid)
end sub

function kclassTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_4"
    this.__proto = ["kclassTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_4_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_4_invoke_k_()
    assertFalse_Z_StrN_k_(brsStructuralEquals_AnyN_AnyN_k_(__kotlin_KClass_create("Dog"), __kotlin_KClass_create("Cat")), invalid)
end sub

function kclassTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_5"
    this.__proto = ["kclassTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_5_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_5_invoke_k_()
    assertFalse_Z_StrN_k_(brsStructuralEquals_AnyN_AnyN_k_(__kotlin_KClass_create("Animal"), __kotlin_KClass_create("Dog")), invalid)
end sub

function kclassTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_6"
    this.__proto = ["kclassTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_6_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_6_invoke_k_()
    dog = Dog_create_Str_k_("Buddy")
    assertEquals_AnyN_AnyN_StrN_k_("Dog", __kotlin_getClass(dog).__get_simpleName(), invalid)
end sub

function kclassTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_7"
    this.__proto = ["kclassTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_7_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_7_invoke_k_()
    animal = Dog_create_Str_k_("Rex")
    assertEquals_AnyN_AnyN_StrN_k_("Dog", __kotlin_getClass(animal).__get_simpleName(), invalid)
end sub

function kclassTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_8"
    this.__proto = ["kclassTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_8_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_8_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(__kotlin_KClass_create("Dog").hashCode(), __kotlin_KClass_create("Dog").hashCode(), invalid)
end sub

function kclassTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_9"
    this.__proto = ["kclassTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_9_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_9_invoke_k_()
    assertNotEquals_AnyN_AnyN_StrN_k_(__kotlin_KClass_create("Dog").hashCode(), __kotlin_KClass_create("Cat").hashCode(), invalid)
end sub

function kclassTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_10"
    this.__proto = ["kclassTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_10_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_10_invoke_k_()
    assertTrue_Z_StrN_k_(__kotlin_KClass_create("Dog").isInstance_AnyN_k_(Dog_create_Str_k_("Buddy")), invalid)
end sub

function kclassTests_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_11"
    this.__proto = ["kclassTests_lambda_lambda_11", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_11_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_11_invoke_k_()
    assertTrue_Z_StrN_k_(__kotlin_KClass_create("Animal").isInstance_AnyN_k_(Dog_create_Str_k_("Buddy")), invalid)
end sub

function kclassTests_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_12"
    this.__proto = ["kclassTests_lambda_lambda_12", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_12_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_12_invoke_k_()
    assertFalse_Z_StrN_k_(__kotlin_KClass_create("Dog").isInstance_AnyN_k_(Cat_create_Str_k_("Whiskers")), invalid)
end sub

function kclassTests_lambda_lambda_13_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_13"
    this.__proto = ["kclassTests_lambda_lambda_13", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_13_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_13_invoke_k_()
    assertFalse_Z_StrN_k_(__kotlin_KClass_create("Dog").isInstance_AnyN_k_(invalid), invalid)
end sub

function kclassTests_lambda_lambda_14_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_14"
    this.__proto = ["kclassTests_lambda_lambda_14", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_14_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_14_invoke_k_()
    map = mutableMapOf_k_()
    set_rMutableMap_AnyN_AnyN_k_(map, __kotlin_KClass_create("Dog"), "dog handler")
    set_rMutableMap_AnyN_AnyN_k_(map, __kotlin_KClass_create("Cat"), "cat handler")
    assertEquals_AnyN_AnyN_StrN_k_("dog handler", map.get_AnyN_k_(__kotlin_KClass_create("Dog")), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("cat handler", map.get_AnyN_k_(__kotlin_KClass_create("Cat")), invalid)
end sub

function kclassTests_lambda_lambda_15_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_15"
    this.__proto = ["kclassTests_lambda_lambda_15", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_15_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_15_invoke_k_()
    map = mutableMapOf_k_()
    set_rMutableMap_AnyN_AnyN_k_(map, __kotlin_KClass_create("Dog"), "dog handler")
    set_rMutableMap_AnyN_AnyN_k_(map, __kotlin_KClass_create("Cat"), "cat handler")
    dog = Dog_create_Str_k_("Buddy")
    assertEquals_AnyN_AnyN_StrN_k_("dog handler", map.get_AnyN_k_(__kotlin_getClass(dog)), invalid)
    cat = Cat_create_Str_k_("Whiskers")
    assertEquals_AnyN_AnyN_StrN_k_("cat handler", map.get_AnyN_k_(__kotlin_getClass(cat)), invalid)
end sub

function kclassTests_lambda_lambda_16_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_16"
    this.__proto = ["kclassTests_lambda_lambda_16", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_16_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_16_invoke_k_()
    assertNull_AnyN_StrN_k_(__kotlin_KClass_create("SimpleClass").__get_qualifiedName(), invalid)
end sub

function kclassTests_lambda_lambda_17_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_17"
    this.__proto = ["kclassTests_lambda_lambda_17", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_17_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_17_invoke_k_()
    str = toString_AnyN_k_(__kotlin_KClass_create("Dog"))
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(str, "Dog", invalid), invalid)
end sub

function kclassTests_lambda_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda"
    this.__proto = ["kclassTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = kclassTests_lambda_invoke_AnyN_k_
    return this
end function

sub kclassTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("simpleName returns class name", kclassTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("simpleName for Animal", kclassTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("simpleName for Dog", kclassTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("same class literals are equal", kclassTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("different class literals are not equal", kclassTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("parent and child class literals are not equal", kclassTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("get class from instance", kclassTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("get class from polymorphic reference", kclassTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("hashCode is consistent with equals", kclassTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("different classes have different hashCodes", kclassTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("isInstance returns true for matching type", kclassTests_lambda_lambda_10_create_k_())
    _this_suite.test_Str_Function0V_k_("isInstance returns true for subclass", kclassTests_lambda_lambda_11_create_k_())
    _this_suite.test_Str_Function0V_k_("isInstance returns false for non-matching type", kclassTests_lambda_lambda_12_create_k_())
    _this_suite.test_Str_Function0V_k_("isInstance returns false for null", kclassTests_lambda_lambda_13_create_k_())
    _this_suite.test_Str_Function0V_k_("KClass can be used as map key", kclassTests_lambda_lambda_14_create_k_())
    _this_suite.test_Str_Function0V_k_("KClass map lookup with instance class", kclassTests_lambda_lambda_15_create_k_())
    _this_suite.test_Str_Function0V_k_("qualifiedName returns null", kclassTests_lambda_lambda_16_create_k_())
    _this_suite.test_Str_Function0V_k_("toString returns class prefix with name", kclassTests_lambda_lambda_17_create_k_())
end sub

function kclassTests_lambda_lambda_18_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_18"
    this.__proto = ["kclassTests_lambda_lambda_18", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_18_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_18_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_("roArray", __kotlin_KClass_create("roArray").__get_simpleName(), invalid)
end sub

function kclassTests_lambda_lambda_19_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_19"
    this.__proto = ["kclassTests_lambda_lambda_19", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_19_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_19_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_("roAssociativeArray", __kotlin_KClass_create("roAssociativeArray").__get_simpleName(), invalid)
end sub

function kclassTests_lambda_lambda_20_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_20"
    this.__proto = ["kclassTests_lambda_lambda_20", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_20_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_20_invoke_k_()
    assertTrue_Z_StrN_k_(brsStructuralEquals_AnyN_AnyN_k_(__kotlin_KClass_create("roArray"), __kotlin_KClass_create("roArray")), invalid)
end sub

function kclassTests_lambda_lambda_21_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_21"
    this.__proto = ["kclassTests_lambda_lambda_21", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_21_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_21_invoke_k_()
    assertFalse_Z_StrN_k_(brsStructuralEquals_AnyN_AnyN_k_(__kotlin_KClass_create("roArray"), __kotlin_KClass_create("roAssociativeArray")), invalid)
end sub

function kclassTests_lambda_lambda_22_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_22"
    this.__proto = ["kclassTests_lambda_lambda_22", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_22_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_22_invoke_k_()
    map = mutableMapOf_k_()
    set_rMutableMap_AnyN_AnyN_k_(map, __kotlin_KClass_create("roArray"), "array handler")
    set_rMutableMap_AnyN_AnyN_k_(map, __kotlin_KClass_create("roAssociativeArray"), "aa handler")
    assertEquals_AnyN_AnyN_StrN_k_("array handler", map.get_AnyN_k_(__kotlin_KClass_create("roArray")), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("aa handler", map.get_AnyN_k_(__kotlin_KClass_create("roAssociativeArray")), invalid)
end sub

function kclassTests_lambda_lambda_23_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_23"
    this.__proto = ["kclassTests_lambda_lambda_23", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_23_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_23_invoke_k_()
    arr = CreateObject("roArray", 0, true)
    assertEquals_AnyN_AnyN_StrN_k_("roArray", __kotlin_getClass(arr).__get_simpleName(), invalid)
end sub

function kclassTests_lambda_lambda_24_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_24"
    this.__proto = ["kclassTests_lambda_lambda_24", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_24_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_24_invoke_k_()
    arr = CreateObject("roArray", 0, true)
    assertTrue_Z_StrN_k_(brsStructuralEquals_AnyN_AnyN_k_(__kotlin_getClass(arr), __kotlin_KClass_create("roArray")), invalid)
end sub

function kclassTests_lambda_lambda_25_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_lambda_25"
    this.__proto = ["kclassTests_lambda_lambda_25", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = kclassTests_lambda_lambda_25_invoke_k_
    return this
end function

sub kclassTests_lambda_lambda_25_invoke_k_()
    map = mutableMapOf_k_()
    set_rMutableMap_AnyN_AnyN_k_(map, __kotlin_KClass_create("roArray"), "array handler")
    arr = CreateObject("roArray", 0, true)
    assertEquals_AnyN_AnyN_StrN_k_("array handler", map.get_AnyN_k_(__kotlin_getClass(arr)), invalid)
end sub

function kclassTests_lambda_1_create_k_() as Object
    this = {}
    this.__type = "kclassTests_lambda_1"
    this.__proto = ["kclassTests_lambda_1", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = kclassTests_lambda_1_invoke_AnyN_k_
    return this
end function

sub kclassTests_lambda_1_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("RoArray class literal simpleName", kclassTests_lambda_lambda_18_create_k_())
    _this_suite.test_Str_Function0V_k_("RoAssociativeArray class literal simpleName", kclassTests_lambda_lambda_19_create_k_())
    _this_suite.test_Str_Function0V_k_("native type class literals are equal", kclassTests_lambda_lambda_20_create_k_())
    _this_suite.test_Str_Function0V_k_("different native type class literals are not equal", kclassTests_lambda_lambda_21_create_k_())
    _this_suite.test_Str_Function0V_k_("native type KClass as map key", kclassTests_lambda_lambda_22_create_k_())
    _this_suite.test_Str_Function0V_k_("get class from native RoArray instance", kclassTests_lambda_lambda_23_create_k_())
    _this_suite.test_Str_Function0V_k_("native instance class matches class literal", kclassTests_lambda_lambda_24_create_k_())
    _this_suite.test_Str_Function0V_k_("native type KClass map lookup with instance", kclassTests_lambda_lambda_25_create_k_())
end sub
