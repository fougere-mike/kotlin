' LcParent — its init() runs AFTER its XML child's init() per the Roku
' "Component initialization order" page; the log order pins it (Q1).
sub init()
    m.who = "LcParent"
    child = m.top.findNode("xmlChild")
    childTag = "none"
    if child <> invalid then childTag = child.tag
    lcLog("parent-init childFound=" + lcBool(child <> invalid) + " childTag=" + childTag)
end sub
