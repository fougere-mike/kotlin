function SGNodeDsl_create_k_() as Object
    this = {}
    this.__type = "SGNodeDsl"
    this.__proto = ["SGNodeDsl", "Annotation"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function SGLayout_create_k_() as Object
    this = {}
    this.__type = "SGLayout"
    this.__proto = ["SGLayout", "Annotation"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function NodeEntry_get_id_k_() as String
end function

function NodeEntry_get_nodeType_k_() as String
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

function Color_create_Str_k_(hex as String) as Object
    this = {}
    this.__type = "Color"
    this.__proto = ["Color"]
    this.__id = __kotlin_nextObjectId()
    this.toXmlValue_k_ = Color_toXmlValue_k_
    this.toString_k_ = Color_toString_k_
    this.toString = Color_toString_k_
    this.hashCode_k_ = Color_hashCode_k_
    this.hashCode = Color_hashCode_k_
    this.equals_AnyN_k_ = Color_equals_AnyN_k_
    this.equals = Color_equals_AnyN_k_
    this.get_hex = Color_get_hex_k_
    this.hex = hex
    return this
end function

function Color_toXmlValue_k_() as String
    return m.get_hex()
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

function Color_get_hex_k_() as String
    return m.hex
end function

function Color_Companion_create_k_() as Object
    this = {}
    this.__type = "Color_Companion"
    this.__proto = ["Color_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.get_White = Color_Companion_get_White_k_
    this.get_Black = Color_Companion_get_Black_k_
    this.get_Red = Color_Companion_get_Red_k_
    this.get_Green = Color_Companion_get_Green_k_
    this.get_Blue = Color_Companion_get_Blue_k_
    this.get_Transparent = Color_Companion_get_Transparent_k_
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

function Color_Companion_get_White_k_() as Object
    return m.White
end function

function Color_Companion_get_Black_k_() as Object
    return m.Black
end function

function Color_Companion_get_Red_k_() as Object
    return m.Red
end function

function Color_Companion_get_Green_k_() as Object
    return m.Green
end function

function Color_Companion_get_Blue_k_() as Object
    return m.Blue
end function

function Color_Companion_get_Transparent_k_() as Object
    return m.Transparent
end function
