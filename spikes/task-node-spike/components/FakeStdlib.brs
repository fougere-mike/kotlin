' FakeStdlib.brs — stands in for the future stdlib TaskRunner script file.
' The observer callbacks live HERE, in a separate <script> include from the
' scene logic, to prove that a callback passed by name to observeFieldScoped
' resolves from an included script (the load-bearing check for TaskRunner).

sub FakeStdlib_onTaskState(event as object)
    handleTaskState(event.getRoSGNode(), event.getData())
end sub

sub FakeStdlib_onEmitSeq(event as object)
    onEmitFire()
end sub

sub FakeStdlib_onWatchdog(event as object)
    handleWatchdog()
end sub
