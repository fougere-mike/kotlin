function isInstanceOf(obj as Object, typeName as String) as Boolean
    if obj = invalid then
        return false
    end if
    if Type(obj) <> "roAssociativeArray" then
        return false
    end if
    proto = obj.__proto
    if proto = invalid then
        return false
    end if
    for each t in proto
        if t = typeName then
            return true
        end if
    end for
    return false
end function

function add(a as Integer, b as Integer) as Integer
    return a + b
end function

function greet(name as String) as String
    return "Hello, " + name
end function

sub main()
    result = add(1, 2)
    message = greet("Roku")
    config = AppConfig_get_setting()
    myColor = Color_RED
end sub

function AppConfig_create() as Object
    this = {}
    this.__type = "AppConfig"
    this.__proto = ["AppConfig"]
    this.setting = "default"
    return this
end function

function AppConfig_getInstance() as Object
    if AppConfig_instance = invalid then
        AppConfig_instance = AppConfig_create()
    end if
    return AppConfig_instance
end function

function AppConfig_get_setting() as String
    return m.setting
end function

function Color_create(__name as String, __ordinal as Integer, rgb as Integer) as Object
    this = {}
    this.__type = "Color"
    this.name = __name
    this.ordinal = __ordinal
    this.rgb = rgb
    return this
end function

function Color_initEntries()
    if Color_entriesInitialized then
        return
    end if
    Color_entriesInitialized = true
    Color_RED = Color_create("RED", 0, 16711680)
    Color_GREEN = Color_create("GREEN", 1, 65280)
    Color_BLUE = Color_create("BLUE", 2, 255)
end function

function Color_values() as Object
    Color_initEntries()
    return [Color_RED, Color_GREEN, Color_BLUE]
end function

function Color_valueOf(name as String) as Object
    Color_initEntries()
    if name = "RED" then
        return Color_RED
    else if name = "GREEN" then
        return Color_GREEN
    else if name = "BLUE" then
        return Color_BLUE
    else
        return invalid
    end if
end function

function Person_create(name as String, age as Integer) as Object
    this = {}
    this.__type = "Person"
    this.__proto = ["Person"]
    this.name = name
    this.age = age
    return this
end function

function Person_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "Person" then
        return false
    end if
    if m.name <> other.name then
        return false
    end if
    if m.age <> other.age then
        return false
    end if
    return true
end function

function Person_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.name)
    result = ((result * 31) + m.age)
    return result
end function

function Person_toString() as String
    return ((("Person(name=" + m.name) + ", age=") + m.age) + ")"
end function

function Person_copy(name as String = m.name, age as Integer = m.age) as Object
    return Person_create(name, age)
end function

function Person_component1() as String
    return m.name
end function

function Person_component2() as Integer
    return m.age
end function

function outerFunction(x as Integer) as Integer
    captured = x
    inner = function(y as Integer) as Integer
        return captured + y
    end function
    return inner(10)
end function

function lambdaExample(multiplier as Integer) as Function
    return function(value as Integer) as Integer
        return value * multiplier
    end function
end function

function Outer_create(value as Integer) as Object
    this = {}
    this.__type = "Outer"
    this.__proto = ["Outer"]
    this.value = value
    this.createInner = Outer_createInner
    return this
end function

function Outer_createInner(offset as Integer) as Object
    return Outer_Inner_create(m, offset)
end function

function Outer_get_value() as Integer
    return m.value
end function

function Outer_Inner_create($outer as Object, offset as Integer) as Object
    this = {}
    this.__type = "Outer_Inner"
    this.__proto = ["Outer_Inner"]
    this.$outer = $outer
    this.offset = offset
    this.compute = Outer_Inner_compute
    return this
end function

function Outer_Inner_compute() as Integer
    return m.$outer.value + m.get_offset()
end function

function Outer_Inner_get_offset() as Integer
    return m.offset
end function

function double(x as Integer) as Integer
    return x * 2
end function

sub testFunctionReference()
    ref = function(x as Integer) as Integer
        return double(x)
    end function
    result = ref(5)
end sub

sub testTryCatch()
    try
        result = 10 / 0
    catch e
        errorMsg = "Error occurred"
    end try
end sub

function Counter_create() as Object
    this = {}
    this.__type = "Counter"
    this.__proto = ["Counter"]
    this._count = 0
    return this
end function

function Counter_get__count() as Integer
    return m._count
end function

sub Counter_set__count(value as Integer)
    m._count = value
end sub

function Counter_get_count() as Integer
    return m.get__count()
end function

sub Counter_set_count(value as Integer)
    if value >= 0 then
        m.set__count(value)
    end if
end sub

function testComparison(x as Integer) as Boolean
    return x >= 0
end function

AppConfig_instance = invalid
Color_RED = invalid
Color_GREEN = invalid
Color_BLUE = invalid
Color_entriesInitialized = false
Color_initEntries()
