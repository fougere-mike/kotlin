function SGNodeDsl_create_k_() as Object
    this = {}
    this.__type = "SGNodeDsl"
    this.__proto = ["SGNodeDsl", "Annotation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    return this
end function

function SGLayout_create_k_() as Object
    this = {}
    this.__type = "SGLayout"
    this.__proto = ["SGLayout", "Annotation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    return this
end function

function NodeEntry___get_id_k_() as String
end function

function NodeEntry___get_nodeType_k_() as String
end function

function Vector2D_create_F_F_k_(x as Float, y as Float) as Object
    this = {}
    this.__type = "Vector2D"
    this.__proto = ["Vector2D"]
    this.__id = __kotlin_nextObjectId()
    this.x = x
    this.y = y
    this.toXmlValue_k_ = Vector2D_toXmlValue_k_
    this.equals = Vector2D_equals
    this.hashCode = Vector2D_hashCode
    this.toString = Vector2D_toString
    this.copy = Vector2D_copy
    this.component1 = Vector2D_component1
    this.component2 = Vector2D_component2
    return this
end function

function Vector2D_create_I_I_k_(x as Integer, y as Integer) as Object
    this = {}
    this.__type = "Vector2D"
    this.__proto = ["Vector2D"]
    this.__id = __kotlin_nextObjectId()
    this.x = x
    this.y = y
    this.toXmlValue_k_ = Vector2D_toXmlValue_k_
    this.equals = Vector2D_equals
    this.hashCode = Vector2D_hashCode
    this.toString = Vector2D_toString
    this.copy = Vector2D_copy
    this.component1 = Vector2D_component1
    this.component2 = Vector2D_component2
    return this
end function

function Vector2D_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "Vector2D" then
        return false
    end if
    if m.x <> other.x then
        return false
    end if
    if m.y <> other.y then
        return false
    end if
    return true
end function

function Vector2D_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.x)
    result = ((result * 31) + m.y)
    return result
end function

function Vector2D_toString() as String
    return ((("Vector2D(x=" + m.x) + ", y=") + m.y) + ")"
end function

function Vector2D_copy(x = invalid, y = invalid) as Object
    if x = invalid then
        x = m.x
    end if
    if y = invalid then
        y = m.y
    end if
    return Vector2D_create_F_F_k_(x, y)
end function

function Vector2D_component1() as Float
    return m.x
end function

function Vector2D_component2() as Float
    return m.y
end function

function Vector2D_toXmlValue_k_() as String
    return ((("[" + __kotlin_numToStr_F_k_(m.x)) + ", ") + __kotlin_numToStr_F_k_(m.y)) + "]"
end function

function Vector4D_create_F_F_F_F_k_(x as Float, y as Float, width as Float, height as Float) as Object
    this = {}
    this.__type = "Vector4D"
    this.__proto = ["Vector4D"]
    this.__id = __kotlin_nextObjectId()
    this.x = x
    this.y = y
    this.width = width
    this.height = height
    this.toXmlValue_k_ = Vector4D_toXmlValue_k_
    this.equals = Vector4D_equals
    this.hashCode = Vector4D_hashCode
    this.toString = Vector4D_toString
    this.copy = Vector4D_copy
    this.component1 = Vector4D_component1
    this.component2 = Vector4D_component2
    this.component3 = Vector4D_component3
    this.component4 = Vector4D_component4
    return this
end function

function Vector4D_create_I_I_I_I_k_(x as Integer, y as Integer, width as Integer, height as Integer) as Object
    this = {}
    this.__type = "Vector4D"
    this.__proto = ["Vector4D"]
    this.__id = __kotlin_nextObjectId()
    this.x = x
    this.y = y
    this.width = width
    this.height = height
    this.toXmlValue_k_ = Vector4D_toXmlValue_k_
    this.equals = Vector4D_equals
    this.hashCode = Vector4D_hashCode
    this.toString = Vector4D_toString
    this.copy = Vector4D_copy
    this.component1 = Vector4D_component1
    this.component2 = Vector4D_component2
    this.component3 = Vector4D_component3
    this.component4 = Vector4D_component4
    return this
end function

function Vector4D_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "Vector4D" then
        return false
    end if
    if m.x <> other.x then
        return false
    end if
    if m.y <> other.y then
        return false
    end if
    if m.width <> other.width then
        return false
    end if
    if m.height <> other.height then
        return false
    end if
    return true
end function

function Vector4D_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.x)
    result = ((result * 31) + m.y)
    result = ((result * 31) + m.width)
    result = ((result * 31) + m.height)
    return result
end function

function Vector4D_toString() as String
    return ((((((("Vector4D(x=" + m.x) + ", y=") + m.y) + ", width=") + m.width) + ", height=") + m.height) + ")"
end function

function Vector4D_copy(x = invalid, y = invalid, width = invalid, height = invalid) as Object
    if x = invalid then
        x = m.x
    end if
    if y = invalid then
        y = m.y
    end if
    if width = invalid then
        width = m.width
    end if
    if height = invalid then
        height = m.height
    end if
    return Vector4D_create_F_F_F_F_k_(x, y, width, height)
end function

function Vector4D_component1() as Float
    return m.x
end function

function Vector4D_component2() as Float
    return m.y
end function

function Vector4D_component3() as Float
    return m.width
end function

function Vector4D_component4() as Float
    return m.height
end function

function Vector4D_toXmlValue_k_() as String
    return ((((((("[" + __kotlin_numToStr_F_k_(m.x)) + ", ") + __kotlin_numToStr_F_k_(m.y)) + ", ") + __kotlin_numToStr_F_k_(m.width)) + ", ") + __kotlin_numToStr_F_k_(m.height)) + "]"
end function

function InterfaceFieldType_create_Str_k_(__name as String, __ordinal as Integer, brsType as String) as Object
    this = {}
    this.__type = "InterfaceFieldType"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["InterfaceFieldType", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.brsType = brsType
    this.__get_brsType = InterfaceFieldType___get_brsType_k_
    return this
end function

sub InterfaceFieldType_initEntries()
    if m.InterfaceFieldType_entriesInitialized = true then
        return
    end if
    m.InterfaceFieldType_entriesInitialized = true
    m.InterfaceFieldType_STRING = InterfaceFieldType_create_Str_k_("STRING", 0, "string")
    m.InterfaceFieldType_INTEGER = InterfaceFieldType_create_Str_k_("INTEGER", 1, "integer")
    m.InterfaceFieldType_LONG_INTEGER = InterfaceFieldType_create_Str_k_("LONG_INTEGER", 2, "longinteger")
    m.InterfaceFieldType_FLOAT = InterfaceFieldType_create_Str_k_("FLOAT", 3, "float")
    m.InterfaceFieldType_DOUBLE = InterfaceFieldType_create_Str_k_("DOUBLE", 4, "double")
    m.InterfaceFieldType_BOOLEAN = InterfaceFieldType_create_Str_k_("BOOLEAN", 5, "boolean")
    m.InterfaceFieldType_COLOR = InterfaceFieldType_create_Str_k_("COLOR", 6, "color")
    m.InterfaceFieldType_TIME = InterfaceFieldType_create_Str_k_("TIME", 7, "time")
    m.InterfaceFieldType_URI = InterfaceFieldType_create_Str_k_("URI", 8, "uri")
    m.InterfaceFieldType_NODE = InterfaceFieldType_create_Str_k_("NODE", 9, "node")
    m.InterfaceFieldType_VECTOR_2D = InterfaceFieldType_create_Str_k_("VECTOR_2D", 10, "vector2d")
    m.InterfaceFieldType_RECT_2D = InterfaceFieldType_create_Str_k_("RECT_2D", 11, "rect2D")
    m.InterfaceFieldType_ASSOC_ARRAY = InterfaceFieldType_create_Str_k_("ASSOC_ARRAY", 12, "assocarray")
    m.InterfaceFieldType_ARRAY = InterfaceFieldType_create_Str_k_("ARRAY", 13, "array")
    m.InterfaceFieldType_INT_ARRAY = InterfaceFieldType_create_Str_k_("INT_ARRAY", 14, "intarray")
    m.InterfaceFieldType_FLOAT_ARRAY = InterfaceFieldType_create_Str_k_("FLOAT_ARRAY", 15, "floatarray")
    m.InterfaceFieldType_BOOL_ARRAY = InterfaceFieldType_create_Str_k_("BOOL_ARRAY", 16, "boolarray")
    m.InterfaceFieldType_STRING_ARRAY = InterfaceFieldType_create_Str_k_("STRING_ARRAY", 17, "stringarray")
    m.InterfaceFieldType_COLOR_ARRAY = InterfaceFieldType_create_Str_k_("COLOR_ARRAY", 18, "colorarray")
    m.InterfaceFieldType_TIME_ARRAY = InterfaceFieldType_create_Str_k_("TIME_ARRAY", 19, "timearray")
    m.InterfaceFieldType_VECTOR_2D_ARRAY = InterfaceFieldType_create_Str_k_("VECTOR_2D_ARRAY", 20, "vector2darray")
    m.InterfaceFieldType_RECT_2D_ARRAY = InterfaceFieldType_create_Str_k_("RECT_2D_ARRAY", 21, "rect2DArray")
    m.InterfaceFieldType_NODE_ARRAY = InterfaceFieldType_create_Str_k_("NODE_ARRAY", 22, "nodearray")
end sub

function InterfaceFieldType_values() as Object
    InterfaceFieldType_initEntries()
    return [m.InterfaceFieldType_STRING, m.InterfaceFieldType_INTEGER, m.InterfaceFieldType_LONG_INTEGER, m.InterfaceFieldType_FLOAT, m.InterfaceFieldType_DOUBLE, m.InterfaceFieldType_BOOLEAN, m.InterfaceFieldType_COLOR, m.InterfaceFieldType_TIME, m.InterfaceFieldType_URI, m.InterfaceFieldType_NODE, m.InterfaceFieldType_VECTOR_2D, m.InterfaceFieldType_RECT_2D, m.InterfaceFieldType_ASSOC_ARRAY, m.InterfaceFieldType_ARRAY, m.InterfaceFieldType_INT_ARRAY, m.InterfaceFieldType_FLOAT_ARRAY, m.InterfaceFieldType_BOOL_ARRAY, m.InterfaceFieldType_STRING_ARRAY, m.InterfaceFieldType_COLOR_ARRAY, m.InterfaceFieldType_TIME_ARRAY, m.InterfaceFieldType_VECTOR_2D_ARRAY, m.InterfaceFieldType_RECT_2D_ARRAY, m.InterfaceFieldType_NODE_ARRAY]
end function

function InterfaceFieldType_valueOf(name as String) as Object
    InterfaceFieldType_initEntries()
    if name = "STRING" then
        return m.InterfaceFieldType_STRING
    else if name = "INTEGER" then
        return m.InterfaceFieldType_INTEGER
    else if name = "LONG_INTEGER" then
        return m.InterfaceFieldType_LONG_INTEGER
    else if name = "FLOAT" then
        return m.InterfaceFieldType_FLOAT
    else if name = "DOUBLE" then
        return m.InterfaceFieldType_DOUBLE
    else if name = "BOOLEAN" then
        return m.InterfaceFieldType_BOOLEAN
    else if name = "COLOR" then
        return m.InterfaceFieldType_COLOR
    else if name = "TIME" then
        return m.InterfaceFieldType_TIME
    else if name = "URI" then
        return m.InterfaceFieldType_URI
    else if name = "NODE" then
        return m.InterfaceFieldType_NODE
    else if name = "VECTOR_2D" then
        return m.InterfaceFieldType_VECTOR_2D
    else if name = "RECT_2D" then
        return m.InterfaceFieldType_RECT_2D
    else if name = "ASSOC_ARRAY" then
        return m.InterfaceFieldType_ASSOC_ARRAY
    else if name = "ARRAY" then
        return m.InterfaceFieldType_ARRAY
    else if name = "INT_ARRAY" then
        return m.InterfaceFieldType_INT_ARRAY
    else if name = "FLOAT_ARRAY" then
        return m.InterfaceFieldType_FLOAT_ARRAY
    else if name = "BOOL_ARRAY" then
        return m.InterfaceFieldType_BOOL_ARRAY
    else if name = "STRING_ARRAY" then
        return m.InterfaceFieldType_STRING_ARRAY
    else if name = "COLOR_ARRAY" then
        return m.InterfaceFieldType_COLOR_ARRAY
    else if name = "TIME_ARRAY" then
        return m.InterfaceFieldType_TIME_ARRAY
    else if name = "VECTOR_2D_ARRAY" then
        return m.InterfaceFieldType_VECTOR_2D_ARRAY
    else if name = "RECT_2D_ARRAY" then
        return m.InterfaceFieldType_RECT_2D_ARRAY
    else if name = "NODE_ARRAY" then
        return m.InterfaceFieldType_NODE_ARRAY
    else
        return invalid
    end if
end function

function InterfaceFieldEntry_create_Str_StrN_InterfaceFieldType_StrN_StrN_Z_k_(name as String, alias = invalid, type_ = [InterfaceFieldType_initEntries(), m.InterfaceFieldType_NODE][1], value = invalid, onChange = invalid, alwaysNotify = false) as Object
    this = {}
    this.__type = "InterfaceFieldEntry"
    this.__proto = ["InterfaceFieldEntry"]
    this.__id = __kotlin_nextObjectId()
    this.name = name
    this.alias = alias
    this.type = type_
    this.value = value
    this.onChange = onChange
    this.alwaysNotify = alwaysNotify
    this.equals = InterfaceFieldEntry_equals
    this.hashCode = InterfaceFieldEntry_hashCode
    this.toString = InterfaceFieldEntry_toString
    this.copy = InterfaceFieldEntry_copy
    this.component1 = InterfaceFieldEntry_component1
    this.component2 = InterfaceFieldEntry_component2
    this.component3 = InterfaceFieldEntry_component3
    this.component4 = InterfaceFieldEntry_component4
    this.component5 = InterfaceFieldEntry_component5
    this.component6 = InterfaceFieldEntry_component6
    return this
end function

function InterfaceFieldEntry_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "InterfaceFieldEntry" then
        return false
    end if
    if m.name <> other.name then
        return false
    end if
    if m.alias <> other.alias then
        return false
    end if
    if m.type <> other.type then
        return false
    end if
    if m.value <> other.value then
        return false
    end if
    if m.onChange <> other.onChange then
        return false
    end if
    if m.alwaysNotify <> other.alwaysNotify then
        return false
    end if
    return true
end function

function InterfaceFieldEntry_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.name)
    result = ((result * 31) + m.alias)
    result = ((result * 31) + m.type)
    result = ((result * 31) + m.value)
    result = ((result * 31) + m.onChange)
    result = ((result * 31) + m.alwaysNotify)
    return result
end function

function InterfaceFieldEntry_toString() as String
    return ((((((((((("InterfaceFieldEntry(name=" + m.name) + ", alias=") + m.alias) + ", type=") + m.type) + ", value=") + m.value) + ", onChange=") + m.onChange) + ", alwaysNotify=") + m.alwaysNotify) + ")"
end function

function InterfaceFieldEntry_copy(name = invalid, alias = invalid, type_ = invalid, value = invalid, onChange = invalid, alwaysNotify = invalid) as Object
    if name = invalid then
        name = m.name
    end if
    if alias = invalid then
        alias = m.alias
    end if
    if type_ = invalid then
        type_ = m.type
    end if
    if value = invalid then
        value = m.value
    end if
    if onChange = invalid then
        onChange = m.onChange
    end if
    if alwaysNotify = invalid then
        alwaysNotify = m.alwaysNotify
    end if
    return InterfaceFieldEntry_create_Str_StrN_InterfaceFieldType_StrN_StrN_Z_k_(name, alias, type_, value, onChange, alwaysNotify)
end function

function InterfaceFieldEntry_component1() as String
    return m.name
end function

function InterfaceFieldEntry_component2() as Dynamic
    return m.alias
end function

function InterfaceFieldEntry_component3() as Object
    return m.type
end function

function InterfaceFieldEntry_component4() as Dynamic
    return m.value
end function

function InterfaceFieldEntry_component5() as Dynamic
    return m.onChange
end function

function InterfaceFieldEntry_component6() as Boolean
    return m.alwaysNotify
end function

function Color_create_Str_k_(hex as String) as Object
    this = {}
    this.__type = "Color"
    this.__proto = ["Color"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.toXmlValue_k_ = Color_toXmlValue_k_
    this.toString_k_ = Color_toString_k_
    this.toString = Color_toString_k_
    this.hashCode_k_ = Color_hashCode_k_
    this.hashCode = Color_hashCode_k_
    this.equals_AnyN_k_ = Color_equals_AnyN_k_
    this.equals = Color_equals_AnyN_k_
    this.__get_hex = Color___get_hex_k_
    this.hex = hex
    return this
end function

function Color_toXmlValue_k_() as String
    return m.__get_hex()
end function

function Color_toString_k_() as String
    return (("Color(" + "hex=") + m.hex) + ")"
end function

function Color_hashCode_k_() as Integer
    return __kotlin_stringHashCode(m.hex)
end function

function Color_equals_AnyN_k_(other as Dynamic) as Boolean
    if not __kotlin_isInstanceOf(other, "Color") then
        return false
    end if
    tmp0_other_with_cast = other
    if m.hex <> tmp0_other_with_cast.hex then
        return false
    end if
    return true
end function

function Color___get_hex_k_() as String
    return m.hex
end function

function Color_Companion_create_k_() as Object
    this = {}
    this.__type = "Color_Companion"
    this.__proto = ["Color_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_White = Color_Companion___get_White_k_
    this.__get_Black = Color_Companion___get_Black_k_
    this.__get_Red = Color_Companion___get_Red_k_
    this.__get_Green = Color_Companion___get_Green_k_
    this.__get_Blue = Color_Companion___get_Blue_k_
    this.__get_Transparent = Color_Companion___get_Transparent_k_
    this.White = Color_create_Str_k_("0xFFFFFFFF")
    this.Black = Color_create_Str_k_("0x000000FF")
    this.Red = Color_create_Str_k_("0xFF0000FF")
    this.Green = Color_create_Str_k_("0x00FF00FF")
    this.Blue = Color_create_Str_k_("0x0000FFFF")
    this.Transparent = Color_create_Str_k_("0x00000000")
    return this
end function

function Color_Companion_getInstance() as Object
    if GetGlobalAA().Color_Companion_instance = invalid then
        GetGlobalAA().Color_Companion_instance = Color_Companion_create_k_()
    end if
    return GetGlobalAA().Color_Companion_instance
end function

function Color_Companion___get_White_k_() as Object
    return m.White
end function

function Color_Companion___get_Black_k_() as Object
    return m.Black
end function

function Color_Companion___get_Red_k_() as Object
    return m.Red
end function

function Color_Companion___get_Green_k_() as Object
    return m.Green
end function

function Color_Companion___get_Blue_k_() as Object
    return m.Blue
end function

function Color_Companion___get_Transparent_k_() as Object
    return m.Transparent
end function
