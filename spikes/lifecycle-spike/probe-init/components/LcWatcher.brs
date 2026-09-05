' LcWatcher — arms an observer on its PARENT's `change` field (scoped or plain
' form) and logs every fire with the Operation and whether it is still
' attached. Expected NEGATIVE per flow-spike Probe A4 (a removed component's
' observers die in the same stack as removeChild); a fire is a FINDING that
' would permit a detach backstop.
sub init()
    m.who = "LcWatcher"
    m.top.observeField("armScoped", "onArmScoped")
    m.top.observeField("armPlain", "onArmPlain")
end sub

sub onArmScoped()
    p = m.top.getParent()
    if p = invalid then
        lcWatch("armScoped:no-parent")
        return
    end if
    p.observeFieldScoped("change", "onParentChangeScoped")
    lcWatch("armScoped:armed")
end sub

sub onArmPlain()
    p = m.top.getParent()
    if p = invalid then
        lcWatch("armPlain:no-parent")
        return
    end if
    p.observeField("change", "onParentChangePlain")
    lcWatch("armPlain:armed")
end sub

sub onParentChangeScoped(event as Object)
    recordChange("scoped", event)
end sub

sub onParentChangePlain(event as Object)
    recordChange("plain", event)
end sub

' try/catch: m.top on a half-removed node may crash (Probe A4 note); the
' record must survive to the global log either way.
sub recordChange(form as String, event as Object)
    try
        c = event.getData()
        attached = (m.top.getParent() <> invalid)
        lcWatch(form + ":" + lcStr(c.Operation) + ":" + lcBool(attached))
    catch e
        lcWatch(form + ":handler-crash:" + e.message)
    end try
end sub
