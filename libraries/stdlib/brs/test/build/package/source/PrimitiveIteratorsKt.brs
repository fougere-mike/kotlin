function ByteIterator_create_k_() as Object
    this = {}
    this.__type = "ByteIterator"
    this.__proto = ["ByteIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.next_k_ = ByteIterator_next_k_
    this.nextByte_k_ = ByteIterator_nextByte_k_
    return this
end function

function ByteIterator_next_k_() as Integer
    return m.nextByte_k_()
end function

function ByteIterator_nextByte_k_() as Integer
end function

function CharIterator_create_k_() as Object
    this = {}
    this.__type = "CharIterator"
    this.__proto = ["CharIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.next_k_ = CharIterator_next_k_
    this.nextChar_k_ = CharIterator_nextChar_k_
    return this
end function

function CharIterator_next_k_() as Object
    return m.nextChar_k_()
end function

function CharIterator_nextChar_k_() as Object
end function

function ShortIterator_create_k_() as Object
    this = {}
    this.__type = "ShortIterator"
    this.__proto = ["ShortIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.next_k_ = ShortIterator_next_k_
    this.nextShort_k_ = ShortIterator_nextShort_k_
    return this
end function

function ShortIterator_next_k_() as Integer
    return m.nextShort_k_()
end function

function ShortIterator_nextShort_k_() as Integer
end function

function IntIterator_create_k_() as Object
    this = {}
    this.__type = "IntIterator"
    this.__proto = ["IntIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.next_k_ = IntIterator_next_k_
    this.nextInt_k_ = IntIterator_nextInt_k_
    return this
end function

function IntIterator_next_k_() as Integer
    return m.nextInt_k_()
end function

function IntIterator_nextInt_k_() as Integer
end function

function LongIterator_create_k_() as Object
    this = {}
    this.__type = "LongIterator"
    this.__proto = ["LongIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.next_k_ = LongIterator_next_k_
    this.nextLong_k_ = LongIterator_nextLong_k_
    return this
end function

function LongIterator_next_k_() as LongInteger
    return m.nextLong_k_()
end function

function LongIterator_nextLong_k_() as LongInteger
end function

function FloatIterator_create_k_() as Object
    this = {}
    this.__type = "FloatIterator"
    this.__proto = ["FloatIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.next_k_ = FloatIterator_next_k_
    this.nextFloat_k_ = FloatIterator_nextFloat_k_
    return this
end function

function FloatIterator_next_k_() as Float
    return m.nextFloat_k_()
end function

function FloatIterator_nextFloat_k_() as Float
end function

function DoubleIterator_create_k_() as Object
    this = {}
    this.__type = "DoubleIterator"
    this.__proto = ["DoubleIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.next_k_ = DoubleIterator_next_k_
    this.nextDouble_k_ = DoubleIterator_nextDouble_k_
    return this
end function

function DoubleIterator_next_k_() as Double
    return m.nextDouble_k_()
end function

function DoubleIterator_nextDouble_k_() as Double
end function

function BooleanIterator_create_k_() as Object
    this = {}
    this.__type = "BooleanIterator"
    this.__proto = ["BooleanIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.next_k_ = BooleanIterator_next_k_
    this.nextBoolean_k_ = BooleanIterator_nextBoolean_k_
    return this
end function

function BooleanIterator_next_k_() as Boolean
    return m.nextBoolean_k_()
end function

function BooleanIterator_nextBoolean_k_() as Boolean
end function
