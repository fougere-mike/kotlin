sub globalFunctionsTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Core Utility Functions", globalFunctionsTests_lambda_create_k_())
    m.suite_Str_Function1TestRunnerV_k_("JSON Functions", globalFunctionsTests_lambda_1_create_k_())
    m.suite_Str_Function1TestRunnerV_k_("File System Functions", globalFunctionsTests_lambda_2_create_k_())
    m.suite_Str_Function1TestRunnerV_k_("String Conversion Functions", globalFunctionsTests_lambda_3_create_k_())
    m.suite_Str_Function1TestRunnerV_k_("Localization Functions", globalFunctionsTests_lambda_4_create_k_())
end sub

function globalFunctionsTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_lambda"
    this.__proto = ["globalFunctionsTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = globalFunctionsTests_lambda_lambda_invoke_k_
    return this
end function

sub globalFunctionsTests_lambda_lambda_invoke_k_()
    start = UpTime(0)
    Sleep(100)
    elapsed = UpTime(0) - start
    assertTrue_Z_StrN_k_(elapsed >= 0.05!, "Sleep should pause for at least 50ms, got " + __kotlin_numToStr_F_k_(elapsed))
end sub

function globalFunctionsTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_lambda_1"
    this.__proto = ["globalFunctionsTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = globalFunctionsTests_lambda_lambda_1_invoke_k_
    return this
end function

sub globalFunctionsTests_lambda_lambda_1_invoke_k_()
    time = UpTime(0)
    assertTrue_Z_StrN_k_(time > 0.0!, "UpTime should return positive value")
end sub

function globalFunctionsTests_lambda_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda"
    this.__proto = ["globalFunctionsTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = globalFunctionsTests_lambda_invoke_AnyN_k_
    return this
end function

sub globalFunctionsTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("sleep pauses execution", globalFunctionsTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("upTime returns positive value", globalFunctionsTests_lambda_lambda_1_create_k_())
end sub

function globalFunctionsTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_lambda_2"
    this.__proto = ["globalFunctionsTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = globalFunctionsTests_lambda_lambda_2_invoke_k_
    return this
end function

sub globalFunctionsTests_lambda_lambda_2_invoke_k_()
    result = ParseJson("{""name"": ""test"", ""value"": 42}")
    assertNotNull_AnyN_StrN_k_(result, invalid)
end sub

function globalFunctionsTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_lambda_3"
    this.__proto = ["globalFunctionsTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = globalFunctionsTests_lambda_lambda_3_invoke_k_
    return this
end function

sub globalFunctionsTests_lambda_lambda_3_invoke_k_()
    result = ParseJson("not valid json {")
    assertNull_AnyN_StrN_k_(result, "Invalid JSON should return null")
end sub

function globalFunctionsTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_lambda_4"
    this.__proto = ["globalFunctionsTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = globalFunctionsTests_lambda_lambda_4_invoke_k_
    return this
end function

sub globalFunctionsTests_lambda_lambda_4_invoke_k_()
    obj = CreateObject("roAssociativeArray")
    obj.addReplace("key", "value")
    json = FormatJson(obj)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "key", invalid), "JSON should contain key")
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "value", invalid), "JSON should contain value")
end sub

function globalFunctionsTests_lambda_1_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_1"
    this.__proto = ["globalFunctionsTests_lambda_1", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = globalFunctionsTests_lambda_1_invoke_AnyN_k_
    return this
end function

sub globalFunctionsTests_lambda_1_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("parseJson parses valid JSON", globalFunctionsTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("parseJson returns null for invalid JSON", globalFunctionsTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("formatJson formats object", globalFunctionsTests_lambda_lambda_4_create_k_())
end sub

function globalFunctionsTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_lambda_5"
    this.__proto = ["globalFunctionsTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = globalFunctionsTests_lambda_lambda_5_invoke_k_
    return this
end function

sub globalFunctionsTests_lambda_lambda_5_invoke_k_()
    files = ListDir("pkg:/")
    assertTrue_Z_StrN_k_(files.count() >= 0, "ListDir should return an array")
end sub

function globalFunctionsTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_lambda_6"
    this.__proto = ["globalFunctionsTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = globalFunctionsTests_lambda_lambda_6_invoke_k_
    return this
end function

sub globalFunctionsTests_lambda_lambda_6_invoke_k_()
    testPath = "tmp:/global_funcs_test.txt"
    testContent = "Hello, Roku!"
    written = WriteAsciiFile(testPath, testContent)
    assertTrue_Z_StrN_k_(written, "Write should succeed")
    read = ReadAsciiFile(testPath)
    assertEquals_AnyN_AnyN_StrN_k_(testContent, read, "Content should match")
    deleted = DeleteFile(testPath)
    assertTrue_Z_StrN_k_(deleted, "Delete should succeed")
end sub

function globalFunctionsTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_lambda_7"
    this.__proto = ["globalFunctionsTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = globalFunctionsTests_lambda_lambda_7_invoke_k_
    return this
end function

sub globalFunctionsTests_lambda_lambda_7_invoke_k_()
    testDir = "tmp:/global_funcs_test_dir"
    created = CreateDirectory(testDir)
    assertTrue_Z_StrN_k_(created, "Create should succeed")
    deleted = DeleteDirectory(testDir)
    assertTrue_Z_StrN_k_(deleted, "Delete should succeed")
end sub

function globalFunctionsTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_lambda_8"
    this.__proto = ["globalFunctionsTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = globalFunctionsTests_lambda_lambda_8_invoke_k_
    return this
end function

sub globalFunctionsTests_lambda_lambda_8_invoke_k_()
    matches = MatchFiles("pkg:/", "*.brs")
    assertTrue_Z_StrN_k_(matches.count() >= 0, invalid)
end sub

function globalFunctionsTests_lambda_2_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_2"
    this.__proto = ["globalFunctionsTests_lambda_2", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = globalFunctionsTests_lambda_2_invoke_AnyN_k_
    return this
end function

sub globalFunctionsTests_lambda_2_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("listDir returns array", globalFunctionsTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("read and write ascii file", globalFunctionsTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("create and delete directory", globalFunctionsTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("matchFiles searches for patterns", globalFunctionsTests_lambda_lambda_8_create_k_())
end sub

function globalFunctionsTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_lambda_9"
    this.__proto = ["globalFunctionsTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = globalFunctionsTests_lambda_lambda_9_invoke_k_
    return this
end function

sub globalFunctionsTests_lambda_lambda_9_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(42, StrToI("42"), invalid)
end sub

function globalFunctionsTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_lambda_10"
    this.__proto = ["globalFunctionsTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = globalFunctionsTests_lambda_lambda_10_invoke_k_
    return this
end function

sub globalFunctionsTests_lambda_lambda_10_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(-17, StrToI("-17"), invalid)
end sub

function globalFunctionsTests_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_lambda_11"
    this.__proto = ["globalFunctionsTests_lambda_lambda_11", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = globalFunctionsTests_lambda_lambda_11_invoke_k_
    return this
end function

sub globalFunctionsTests_lambda_lambda_11_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0, StrToI("not a number"), invalid)
end sub

function globalFunctionsTests_lambda_3_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_3"
    this.__proto = ["globalFunctionsTests_lambda_3", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = globalFunctionsTests_lambda_3_invoke_AnyN_k_
    return this
end function

sub globalFunctionsTests_lambda_3_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("strToI converts valid integer string", globalFunctionsTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("strToI converts negative integer string", globalFunctionsTests_lambda_lambda_10_create_k_())
    _this_suite.test_Str_Function0V_k_("strToI returns 0 for invalid string", globalFunctionsTests_lambda_lambda_11_create_k_())
end sub

function globalFunctionsTests_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_lambda_12"
    this.__proto = ["globalFunctionsTests_lambda_lambda_12", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = globalFunctionsTests_lambda_lambda_12_invoke_k_
    return this
end function

sub globalFunctionsTests_lambda_lambda_12_invoke_k_()
    result = Tr("Hello")
    assertEquals_AnyN_AnyN_StrN_k_("Hello", result, invalid)
end sub

function globalFunctionsTests_lambda_4_create_k_() as Object
    this = {}
    this.__type = "globalFunctionsTests_lambda_4"
    this.__proto = ["globalFunctionsTests_lambda_4", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = globalFunctionsTests_lambda_4_invoke_AnyN_k_
    return this
end function

sub globalFunctionsTests_lambda_4_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("tr returns original string without translations", globalFunctionsTests_lambda_lambda_12_create_k_())
end sub
