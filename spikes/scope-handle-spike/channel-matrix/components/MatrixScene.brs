' MatrixScene — creates ProbeA (the "owner" analog) and ProbeB (the "child"),
' hands B a node reference to A via a declared node field, then fires the matrix.
' ProbeA arms ALL its observers/handlers in its own init, which runs inside
' createObject below — before anything here is wired (arming-order law).

sub init()
    di = CreateObject("roDeviceInfo")
    osv = di.GetOSVersion()
    print "[SPIKE] BEGIN model=" + di.GetModel() + " os=" + osv.major + "." + osv.minor + "." + osv.revision + " build=" + osv.build

    m.probeA = createObject("roSGNode", "ProbeA")
    m.probeA.id = "probeA"
    m.top.appendChild(m.probeA)

    m.probeB = createObject("roSGNode", "ProbeB")
    m.probeB.id = "probeB"
    m.top.appendChild(m.probeB)

    m.probeB.aNode = m.probeA
    m.probeB.runTests = true
end sub
