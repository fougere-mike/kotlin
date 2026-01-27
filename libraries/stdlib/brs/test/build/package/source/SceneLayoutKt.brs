function SceneLayoutBase_create_RoSGNode_k_(top_ as Object) as Object
    this = {}
    this.__type = "SceneLayoutBase"
    this.__proto = ["SceneLayoutBase"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.get_Str_k_ = SceneLayoutBase_get_Str_k_
    this.__get_top = SceneLayoutBase___get_top_k_
    this.top = top_
    return this
end function

function SceneLayoutBase_get_Str_k_(id as String) as Dynamic
    return m.__get_top().findNode(id)
end function

function SceneLayoutBase___get_top_k_() as Object
    return m.top
end function

function SceneLayoutDefinition_create_ListNodeEntry_ListInterfaceFieldEntry_k_(nodes as Object, interfaceFields = emptyList_k_()) as Object
    this = {}
    this.__type = "SceneLayoutDefinition"
    this.__proto = ["SceneLayoutDefinition"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_nodes = SceneLayoutDefinition___get_nodes_k_
    this.__get_interfaceFields = SceneLayoutDefinition___get_interfaceFields_k_
    this.nodes = nodes
    this.interfaceFields = interfaceFields
    return this
end function

function SceneLayoutDefinition___get_nodes_k_() as Object
    return m.nodes
end function

function SceneLayoutDefinition___get_interfaceFields_k_() as Object
    return m.interfaceFields
end function

function sceneLayout_Function1LayoutBuilderV_k_(init as Object) as Object
    builder = LayoutBuilder_create_k_()
    init.invoke_AnyN_k_(builder)
    return SceneLayoutDefinition_create_ListNodeEntry_ListInterfaceFieldEntry_k_(builder.build_k_(), builder.buildInterfaceFields_k_())
end function
