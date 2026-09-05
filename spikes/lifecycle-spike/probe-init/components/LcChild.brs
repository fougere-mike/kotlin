' LcChild — logs, FROM INSIDE init(), whether getParent()/getScene() are valid
' and what its `tag` field reads (Q2/Q3/Q4/Q5). Exposes lcPing/lcSelfPing for
' the callFunc probes (Q6).
sub init()
    m.who = "LcChild"
    parentValid = (m.top.getParent() <> invalid)
    sceneValid = (m.top.getScene() <> invalid)
    lcLog("child-init tag=" + m.top.tag + " parent=" + lcBool(parentValid) + " scene=" + lcBool(sceneValid))
end sub

' Q6a target: proves callFunc ran with THIS component's m (m.who set in init).
function lcPing() as String
    return "pong:" + m.who
end function

' Q6c target: a component calling callFunc on ITS OWN node.
function lcSelfPing() as String
    return "self:" + lcStr(m.top.callFunc("lcPing"))
end function
