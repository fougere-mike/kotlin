function FunctionAdapter_create_FunctionAdapter_k_() as Object
    this = {}
    this.__type = "FunctionAdapter"
    this.__proto = ["FunctionAdapter"]
    this.invoke_Arr_AnyN_k_ = FunctionAdapter_invoke_Arr_AnyN_k_
    return this
end function

function FunctionAdapter_invoke_Arr_AnyN_k_(args as Object) as Dynamic
end function

function Function0Adapter_create_Function0AdapterAnyN_k_() as Object
    this = FunctionAdapter_create_FunctionAdapter_k_()
    this._super = {}
    this._super.invoke_Arr_AnyN_k_ = this.invoke_Arr_AnyN_k_
    this.__proto = ["Function0Adapter", this.__proto]
    this.__type = "Function0Adapter"
    this.invoke_AnyN_k_ = Function0Adapter_invoke_AnyN_k_
    this.invoke_Arr_AnyN_k_ = Function0Adapter_invoke_Arr_AnyN_k_
    return this
end function

function Function0Adapter_invoke_AnyN_k_() as Dynamic
end function

function Function0Adapter_invoke_Arr_AnyN_k_(args as Object) as Dynamic
    return m.invoke_AnyN_k_()
end function

function Function1Adapter_create_Function1AdapterAnyNAnyN_k_() as Object
    this = FunctionAdapter_create_FunctionAdapter_k_()
    this._super = {}
    this._super.invoke_Arr_AnyN_k_ = this.invoke_Arr_AnyN_k_
    this.__proto = ["Function1Adapter", this.__proto]
    this.__type = "Function1Adapter"
    this.invoke_AnyN_AnyN_k_ = Function1Adapter_invoke_AnyN_AnyN_k_
    this.invoke_Arr_AnyN_k_ = Function1Adapter_invoke_Arr_AnyN_k_
    return this
end function

function Function1Adapter_invoke_AnyN_AnyN_k_(p1 as Dynamic) as Dynamic
end function

function Function1Adapter_invoke_Arr_AnyN_k_(args as Object) as Dynamic
    return m.invoke_AnyN_AnyN_k_(args[0])
end function

function Function2Adapter_create_Function2AdapterAnyNAnyNAnyN_k_() as Object
    this = FunctionAdapter_create_FunctionAdapter_k_()
    this._super = {}
    this._super.invoke_Arr_AnyN_k_ = this.invoke_Arr_AnyN_k_
    this.__proto = ["Function2Adapter", this.__proto]
    this.__type = "Function2Adapter"
    this.invoke_AnyN_AnyN_AnyN_k_ = Function2Adapter_invoke_AnyN_AnyN_AnyN_k_
    this.invoke_Arr_AnyN_k_ = Function2Adapter_invoke_Arr_AnyN_k_
    return this
end function

function Function2Adapter_invoke_AnyN_AnyN_AnyN_k_(p1 as Dynamic, p2 as Dynamic) as Dynamic
end function

function Function2Adapter_invoke_Arr_AnyN_k_(args as Object) as Dynamic
    return m.invoke_AnyN_AnyN_AnyN_k_(args[0], args[1])
end function

function Function3Adapter_create_Function3AdapterAnyNAnyNAnyNAnyN_k_() as Object
    this = FunctionAdapter_create_FunctionAdapter_k_()
    this._super = {}
    this._super.invoke_Arr_AnyN_k_ = this.invoke_Arr_AnyN_k_
    this.__proto = ["Function3Adapter", this.__proto]
    this.__type = "Function3Adapter"
    this.invoke_AnyN_AnyN_AnyN_AnyN_k_ = Function3Adapter_invoke_AnyN_AnyN_AnyN_AnyN_k_
    this.invoke_Arr_AnyN_k_ = Function3Adapter_invoke_Arr_AnyN_k_
    return this
end function

function Function3Adapter_invoke_AnyN_AnyN_AnyN_AnyN_k_(p1 as Dynamic, p2 as Dynamic, p3 as Dynamic) as Dynamic
end function

function Function3Adapter_invoke_Arr_AnyN_k_(args as Object) as Dynamic
    return m.invoke_AnyN_AnyN_AnyN_AnyN_k_(args[0], args[1], args[2])
end function
