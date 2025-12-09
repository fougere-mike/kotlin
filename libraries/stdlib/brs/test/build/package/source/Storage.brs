function RoRegistry_create_RoRegistry_k_() as Object
    this = {}
    this.__type = "RoRegistry"
    this.__proto = ["RoRegistry"]
    this.__id = __kotlin_nextObjectId()
    this.getSectionList_Dynamic_k_ = RoRegistry_getSectionList_Dynamic_k_
    this.delete_Str_Z_k_ = RoRegistry_delete_Str_Z_k_
    this.flush_Z_k_ = RoRegistry_flush_Z_k_
    this.get_native = RoRegistry_get_native_Dynamic_k_
    this.native = CreateObject("roRegistry")
    return this
end function

function RoRegistry_getSectionList_Dynamic_k_() as Object
    return m.get_native().GetSectionList()
end function

function RoRegistry_delete_Str_Z_k_(section as String) as Boolean
    return m.get_native().Delete(section)
end function

function RoRegistry_flush_Z_k_() as Boolean
    return m.get_native().Flush()
end function

function RoRegistry_get_native_Dynamic_k_() as Object
    return m.native
end function

function RoRegistrySection_create_Str_RoRegistrySection_k_(section as String) as Object
    this = {}
    this.__type = "RoRegistrySection"
    this.__proto = ["RoRegistrySection"]
    this.__id = __kotlin_nextObjectId()
    this.read_Str_Str_k_ = RoRegistrySection_read_Str_Str_k_
    this.write_Str_Str_Z_k_ = RoRegistrySection_write_Str_Str_Z_k_
    this.delete_Str_Z_k_ = RoRegistrySection_delete_Str_Z_k_
    this.exists_Str_Z_k_ = RoRegistrySection_exists_Str_Z_k_
    this.flush_Z_k_ = RoRegistrySection_flush_Z_k_
    this.getKeyList_Dynamic_k_ = RoRegistrySection_getKeyList_Dynamic_k_
    this.readMulti_Dynamic_Dynamic_k_ = RoRegistrySection_readMulti_Dynamic_Dynamic_k_
    this.writeMulti_Dynamic_Z_k_ = RoRegistrySection_writeMulti_Dynamic_Z_k_
    this.get_native = RoRegistrySection_get_native_Dynamic_k_
    this.native = CreateObject("roRegistrySection", section)
    return this
end function

function RoRegistrySection_read_Str_Str_k_(key as String) as String
    return m.get_native().Read(key)
end function

function RoRegistrySection_write_Str_Str_Z_k_(key as String, value as String) as Boolean
    return m.get_native().Write(key, value)
end function

function RoRegistrySection_delete_Str_Z_k_(key as String) as Boolean
    return m.get_native().Delete(key)
end function

function RoRegistrySection_exists_Str_Z_k_(key as String) as Boolean
    return m.get_native().Exists(key)
end function

function RoRegistrySection_flush_Z_k_() as Boolean
    return m.get_native().Flush()
end function

function RoRegistrySection_getKeyList_Dynamic_k_() as Object
    return m.get_native().GetKeyList()
end function

function RoRegistrySection_readMulti_Dynamic_Dynamic_k_(keys as Object) as Object
    return m.get_native().ReadMulti(keys)
end function

function RoRegistrySection_writeMulti_Dynamic_Z_k_(keyValues as Object) as Boolean
    return m.get_native().WriteMulti(keyValues)
end function

function RoRegistrySection_get_native_Dynamic_k_() as Object
    return m.native
end function

function RoPath_create_Str_RoPath_k_(path as String) as Object
    this = {}
    this.__type = "RoPath"
    this.__proto = ["RoPath"]
    this.__id = __kotlin_nextObjectId()
    this.isValid_Z_k_ = RoPath_isValid_Z_k_
    this.change_Str_Z_k_ = RoPath_change_Str_Z_k_
    this.getString_Str_k_ = RoPath_getString_Str_k_
    this.getFilename_Str_k_ = RoPath_getFilename_Str_k_
    this.getParent_Str_k_ = RoPath_getParent_Str_k_
    this.getExtension_Str_k_ = RoPath_getExtension_Str_k_
    this.split_Dynamic_k_ = RoPath_split_Dynamic_k_
    this.toString_Str_k_ = RoPath_toString_Str_k_
    this.toString = RoPath_toString_Str_k_
    this.get_native = RoPath_get_native_Dynamic_k_
    this.native = CreateObject("roPath", path)
    return this
end function

function RoPath_isValid_Z_k_() as Boolean
    return m.get_native().IsValid()
end function

function RoPath_change_Str_Z_k_(path as String) as Boolean
    return m.get_native().Change(path)
end function

function RoPath_getString_Str_k_() as String
    return m.get_native().GetString()
end function

function RoPath_getFilename_Str_k_() as String
    return m.get_native().GetFilename()
end function

function RoPath_getParent_Str_k_() as String
    return m.get_native().GetParent()
end function

function RoPath_getExtension_Str_k_() as String
    return m.get_native().GetExtension()
end function

function RoPath_split_Dynamic_k_() as Object
    return m.get_native().Split()
end function

function RoPath_toString_Str_k_() as String
    return m.getString_Str_k_()
end function

function RoPath_get_native_Dynamic_k_() as Object
    return m.native
end function

function RoFileSystem_create_RoFileSystem_k_() as Object
    this = {}
    this.__type = "RoFileSystem"
    this.__proto = ["RoFileSystem"]
    this.__id = __kotlin_nextObjectId()
    this.exists_Str_Z_k_ = RoFileSystem_exists_Str_Z_k_
    this.stat_Str_Dynamic_k_ = RoFileSystem_stat_Str_Dynamic_k_
    this.getVolumeList_Dynamic_k_ = RoFileSystem_getVolumeList_Dynamic_k_
    this.getVolumeInfo_Str_Dynamic_k_ = RoFileSystem_getVolumeInfo_Str_Dynamic_k_
    this.getDirectoryListing_Str_Dynamic_k_ = RoFileSystem_getDirectoryListing_Str_Dynamic_k_
    this.createDirectory_Str_Z_k_ = RoFileSystem_createDirectory_Str_Z_k_
    this.delete_Str_Z_k_ = RoFileSystem_delete_Str_Z_k_
    this.copyFile_Str_Str_Z_k_ = RoFileSystem_copyFile_Str_Str_Z_k_
    this.moveFile_Str_Str_Z_k_ = RoFileSystem_moveFile_Str_Str_Z_k_
    this.match_Str_Str_Dynamic_k_ = RoFileSystem_match_Str_Str_Dynamic_k_
    this.find_Str_Str_Dynamic_k_ = RoFileSystem_find_Str_Str_Dynamic_k_
    this.findRecurse_Str_Str_I_Dynamic_k_ = RoFileSystem_findRecurse_Str_Str_I_Dynamic_k_
    this.get_native = RoFileSystem_get_native_Dynamic_k_
    this.native = CreateObject("roFileSystem")
    return this
end function

function RoFileSystem_exists_Str_Z_k_(path as String) as Boolean
    return m.get_native().Exists(path)
end function

function RoFileSystem_stat_Str_Dynamic_k_(path as String) as Object
    return m.get_native().Stat(path)
end function

function RoFileSystem_getVolumeList_Dynamic_k_() as Object
    return m.get_native().GetVolumeList()
end function

function RoFileSystem_getVolumeInfo_Str_Dynamic_k_(volume as String) as Object
    return m.get_native().GetVolumeInfo(volume)
end function

function RoFileSystem_getDirectoryListing_Str_Dynamic_k_(path as String) as Object
    return m.get_native().GetDirectoryListing(path)
end function

function RoFileSystem_createDirectory_Str_Z_k_(path as String) as Boolean
    return m.get_native().CreateDirectory(path)
end function

function RoFileSystem_delete_Str_Z_k_(path as String) as Boolean
    return m.get_native().Delete(path)
end function

function RoFileSystem_copyFile_Str_Str_Z_k_(source as String, destination as String) as Boolean
    return m.get_native().CopyFile(source, destination)
end function

function RoFileSystem_moveFile_Str_Str_Z_k_(source as String, destination as String) as Boolean
    return m.get_native().MoveFile(source, destination)
end function

function RoFileSystem_match_Str_Str_Dynamic_k_(path as String, pattern as String) as Object
    return m.get_native().Match(path, pattern)
end function

function RoFileSystem_find_Str_Str_Dynamic_k_(path as String, regex as String) as Object
    return m.get_native().Find(path, regex)
end function

function RoFileSystem_findRecurse_Str_Str_I_Dynamic_k_(path as String, regex as String, maxDepth = 10) as Object
    return m.get_native().FindRecurse(path, regex, maxDepth)
end function

function RoFileSystem_get_native_Dynamic_k_() as Object
    return m.native
end function
