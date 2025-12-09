function RoByteArray_create_RoByteArray_k_() as Object
    this = {}
    this.__type = "RoByteArray"
    this.__proto = ["RoByteArray"]
    this.__id = __kotlin_nextObjectId()
    this.count_I_k_ = RoByteArray_count_I_k_
    this.capacity_I_k_ = RoByteArray_capacity_I_k_
    this.isEmpty_Z_k_ = RoByteArray_isEmpty_Z_k_
    this.clear = RoByteArray_clear
    this.get_I_I_k_ = RoByteArray_get_I_I_k_
    this.set_I_I_k_ = RoByteArray_set_I_I_k_
    this.push_I_k_ = RoByteArray_push_I_k_
    this.pop_IN_k_ = RoByteArray_pop_IN_k_
    this.peek_IN_k_ = RoByteArray_peek_IN_k_
    this.shift_IN_k_ = RoByteArray_shift_IN_k_
    this.unshift_I_k_ = RoByteArray_unshift_I_k_
    this.fromAsciiString_Str_k_ = RoByteArray_fromAsciiString_Str_k_
    this.toAsciiString_Str_k_ = RoByteArray_toAsciiString_Str_k_
    this.fromBase64String_Str_k_ = RoByteArray_fromBase64String_Str_k_
    this.toBase64String_Str_k_ = RoByteArray_toBase64String_Str_k_
    this.fromHexString_Str_k_ = RoByteArray_fromHexString_Str_k_
    this.toHexString_Str_k_ = RoByteArray_toHexString_Str_k_
    this.readFile_Str_Z_k_ = RoByteArray_readFile_Str_Z_k_
    this.writeFile_Str_Z_k_ = RoByteArray_writeFile_Str_Z_k_
    this.appendFile_Str_Z_k_ = RoByteArray_appendFile_Str_Z_k_
    this.getCRC32_I_k_ = RoByteArray_getCRC32_I_k_
    this.getMD5_Str_k_ = RoByteArray_getMD5_Str_k_
    this.getSHA1_Str_k_ = RoByteArray_getSHA1_Str_k_
    this.getSHA256_Str_k_ = RoByteArray_getSHA256_Str_k_
    this.getSHA512_Str_k_ = RoByteArray_getSHA512_Str_k_
    this.resize_I_I_k_ = RoByteArray_resize_I_I_k_
    this.append_RoByteArray_k_ = RoByteArray_append_RoByteArray_k_
    this.getNative_Dynamic_k_ = RoByteArray_getNative_Dynamic_k_
    this.get_native = RoByteArray_get_native_Dynamic_k_
    this.native = CreateObject("roByteArray")
    return this
end function

function RoByteArray_create_Dynamic_RoByteArray_k_(nativeArray as Object) as Object
    this = {}
    this.__type = "RoByteArray"
    this.__proto = ["RoByteArray"]
    this.__id = __kotlin_nextObjectId()
    this.count_I_k_ = RoByteArray_count_I_k_
    this.capacity_I_k_ = RoByteArray_capacity_I_k_
    this.isEmpty_Z_k_ = RoByteArray_isEmpty_Z_k_
    this.clear = RoByteArray_clear
    this.get_I_I_k_ = RoByteArray_get_I_I_k_
    this.set_I_I_k_ = RoByteArray_set_I_I_k_
    this.push_I_k_ = RoByteArray_push_I_k_
    this.pop_IN_k_ = RoByteArray_pop_IN_k_
    this.peek_IN_k_ = RoByteArray_peek_IN_k_
    this.shift_IN_k_ = RoByteArray_shift_IN_k_
    this.unshift_I_k_ = RoByteArray_unshift_I_k_
    this.fromAsciiString_Str_k_ = RoByteArray_fromAsciiString_Str_k_
    this.toAsciiString_Str_k_ = RoByteArray_toAsciiString_Str_k_
    this.fromBase64String_Str_k_ = RoByteArray_fromBase64String_Str_k_
    this.toBase64String_Str_k_ = RoByteArray_toBase64String_Str_k_
    this.fromHexString_Str_k_ = RoByteArray_fromHexString_Str_k_
    this.toHexString_Str_k_ = RoByteArray_toHexString_Str_k_
    this.readFile_Str_Z_k_ = RoByteArray_readFile_Str_Z_k_
    this.writeFile_Str_Z_k_ = RoByteArray_writeFile_Str_Z_k_
    this.appendFile_Str_Z_k_ = RoByteArray_appendFile_Str_Z_k_
    this.getCRC32_I_k_ = RoByteArray_getCRC32_I_k_
    this.getMD5_Str_k_ = RoByteArray_getMD5_Str_k_
    this.getSHA1_Str_k_ = RoByteArray_getSHA1_Str_k_
    this.getSHA256_Str_k_ = RoByteArray_getSHA256_Str_k_
    this.getSHA512_Str_k_ = RoByteArray_getSHA512_Str_k_
    this.resize_I_I_k_ = RoByteArray_resize_I_I_k_
    this.append_RoByteArray_k_ = RoByteArray_append_RoByteArray_k_
    this.getNative_Dynamic_k_ = RoByteArray_getNative_Dynamic_k_
    this.get_native = RoByteArray_get_native_Dynamic_k_
    this.native = nativeArray
    return this
end function

function RoByteArray_count_I_k_() as Integer
    return m.get_native().Count()
end function

function RoByteArray_capacity_I_k_() as Integer
    return m.get_native().Capacity()
end function

function RoByteArray_isEmpty_Z_k_() as Boolean
    return m.get_native().IsEmpty()
end function

sub RoByteArray_clear()
    m.get_native().Clear()
end sub

function RoByteArray_get_I_I_k_(index as Integer) as Integer
    return m.get_native()[index]
end function

sub RoByteArray_set_I_I_k_(index as Integer, value as Integer)
    m.get_native()[index] = value
end sub

sub RoByteArray_push_I_k_(value as Integer)
    m.get_native().Push(value)
end sub

function RoByteArray_pop_IN_k_() as Dynamic
    return m.get_native().Pop()
end function

function RoByteArray_peek_IN_k_() as Dynamic
    return m.get_native().Peek()
end function

function RoByteArray_shift_IN_k_() as Dynamic
    return m.get_native().Shift()
end function

sub RoByteArray_unshift_I_k_(value as Integer)
    m.get_native().Unshift(value)
end sub

sub RoByteArray_fromAsciiString_Str_k_(str as String)
    m.get_native().FromAsciiString(str)
end sub

function RoByteArray_toAsciiString_Str_k_() as String
    return m.get_native().ToAsciiString()
end function

sub RoByteArray_fromBase64String_Str_k_(base64 as String)
    m.get_native().FromBase64String(base64)
end sub

function RoByteArray_toBase64String_Str_k_() as String
    return m.get_native().ToBase64String()
end function

sub RoByteArray_fromHexString_Str_k_(hex as String)
    m.get_native().FromHexString(hex)
end sub

function RoByteArray_toHexString_Str_k_() as String
    return m.get_native().ToHexString()
end function

function RoByteArray_readFile_Str_Z_k_(path as String) as Boolean
    return m.get_native().ReadFile(path)
end function

function RoByteArray_writeFile_Str_Z_k_(path as String) as Boolean
    return m.get_native().WriteFile(path)
end function

function RoByteArray_appendFile_Str_Z_k_(path as String) as Boolean
    return m.get_native().AppendFile(path)
end function

function RoByteArray_getCRC32_I_k_() as Integer
    return m.get_native().GetCRC32()
end function

function RoByteArray_getMD5_Str_k_() as String
    return m.get_native().GetMD5()
end function

function RoByteArray_getSHA1_Str_k_() as String
    return m.get_native().GetSHA1()
end function

function RoByteArray_getSHA256_Str_k_() as String
    return m.get_native().GetSHA256()
end function

function RoByteArray_getSHA512_Str_k_() as String
    return m.get_native().GetSHA512()
end function

sub RoByteArray_resize_I_I_k_(newSize as Integer, fillValue = 0)
    m.get_native().Resize(newSize, fillValue)
end sub

sub RoByteArray_append_RoByteArray_k_(other as Object)
    m.get_native().Append(other.get_native())
end sub

function RoByteArray_getNative_Dynamic_k_() as Object
    return m.get_native()
end function

function RoByteArray_get_native_Dynamic_k_() as Object
    return m.native
end function
