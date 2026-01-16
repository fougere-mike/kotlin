function Number_create_k_() as Object
    this = {}
    this.__type = "Number"
    this.__proto = ["Number"]
    this.__id = __kotlin_nextObjectId()
    this.toDouble_k_ = Number_toDouble_k_
    this.toFloat_k_ = Number_toFloat_k_
    this.toLong_k_ = Number_toLong_k_
    this.toInt_k_ = Number_toInt_k_
    this.toChar_k_ = Number_toChar_k_
    this.toShort_k_ = Number_toShort_k_
    this.toByte_k_ = Number_toByte_k_
    return this
end function

function Number_toDouble_k_() as Double
end function

function Number_toFloat_k_() as Float
end function

function Number_toLong_k_() as LongInteger
end function

function Number_toInt_k_() as Integer
end function

function Number_toChar_k_() as Object
    return Chr(m)
end function

function Number_toShort_k_() as Integer
end function

function Number_toByte_k_() as Integer
end function
