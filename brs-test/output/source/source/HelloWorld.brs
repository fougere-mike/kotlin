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
    if m.AppConfig_instance = invalid then
        m.AppConfig_instance = AppConfig_create()
    end if
    return m.AppConfig_instance
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

sub Color_initEntries()
    if m.Color_entriesInitialized then
        return
    end if
    m.Color_entriesInitialized = true
    m.Color_RED = Color_create("RED", 0, 16711680)
    m.Color_GREEN = Color_create("GREEN", 1, 65280)
    m.Color_BLUE = Color_create("BLUE", 2, 255)
end sub

function Color_values() as Object
    Color_initEntries()
    return [m.Color_RED, m.Color_GREEN, m.Color_BLUE]
end function

function Color_valueOf(name as String) as Object
    Color_initEntries()
    if name = "RED" then
        return m.Color_RED
    else if name = "GREEN" then
        return m.Color_GREEN
    else if name = "BLUE" then
        return m.Color_BLUE
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

function Person_copy(name = invalid, age = invalid) as Object
    if name = invalid then
        name = m.name
    end if
    if age = invalid then
        age = m.age
    end if
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
    inner = {captured: {value: captured}, invoke: function(y as Integer) as Integer
        return m.captured.value + y
    end function}
    return inner.invoke(10)
end function

function lambdaExample(multiplier as Integer) as Function
    return {multiplier: multiplier, invoke: function(value as Integer) as Integer
        return value * m.multiplier
    end function}
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
    ref = {invoke: function(x as Integer) as Integer
        return double(x)
    end function}
    result = ref.invoke(5)
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

function testMutableCapture() as Integer
    counter = 0
    increment = {counter: {value: counter}, invoke: function() as Void
        m.counter.value = (m.counter.value + 1)
    end function}
    increment.invoke()
    increment.invoke()
    return counter
end function

function testReadOnlyCapture() as Integer
    multiplier = 5
    multiply = {multiplier: multiplier, invoke: function(x as Integer) as Integer
        return x * m.multiplier
    end function}
    return multiply.invoke(10)
end function

function testMultipleCaptures() as Integer
    a = 10
    b = 20
    compute = {a: {value: a}, b: b, invoke: function() as Integer
        m.a.value = (m.a.value + m.b)
        return m.a.value
    end function}
    compute.invoke()
    return a
end function

function makeCounter() as Function
    count = 0
    return {count: {value: count}, invoke: function() as Integer
        m.count.value = (m.count.value + 1)
        return m.count.value
    end function}
end function

function testReturnedClosure() as Integer
    counter = makeCounter()
    counter.invoke()
    counter.invoke()
    return counter.invoke()
end function

function testClosureWithParams() as Integer
    base = 100
    addToBase = {base: {value: base}, invoke: function(x as Integer, y as Integer) as Integer
        m.base.value = ((m.base.value + x) + y)
        return m.base.value
    end function}
    return addToBase.invoke(5, 10)
end function
