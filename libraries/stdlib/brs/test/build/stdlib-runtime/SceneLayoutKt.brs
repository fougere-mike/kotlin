function SceneLayoutBase_create_RoSGNode_k_(top_ as Object) as Object
    this = {}
    this.__type = "SceneLayoutBase"
    this.__proto = ["SceneLayoutBase"]
    this.__id = __kotlin_nextObjectId()
    this.get_Str_k_ = SceneLayoutBase_get_Str_k_
    this.get_top = SceneLayoutBase_get_top_k_
    this.top = top_
    return this
end function

function SceneLayoutBase_get_Str_k_(id as String) as Dynamic
    return m.get_top().findNode(id)
end function

function SceneLayoutBase_get_top_k_() as Object
    return m.top
end function

function SceneLayoutDefinition_create_ListNodeEntry_k_(nodes as Object) as Object
    this = {}
    this.__type = "SceneLayoutDefinition"
    this.__proto = ["SceneLayoutDefinition"]
    this.__id = __kotlin_nextObjectId()
    this.get_nodes = SceneLayoutDefinition_get_nodes_k_
    this.nodes = nodes
    return this
end function

function SceneLayoutDefinition_get_nodes_k_() as Object
    return m.nodes
end function

function sceneLayout_Function1LayoutBuilderV_k_(init as Object) as Object
    builder = LayoutBuilder_create_k_()
    init.invoke(builder)
    return SceneLayoutDefinition_create_ListNodeEntry_k_(builder.build_k_())
end function
