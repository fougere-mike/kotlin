sub init()
    m.top.functionName = "__kotlinTaskMain"
    m.run_k_ = RtqSpikeTask_run_k_
end sub

sub RtqSpikeTask_run_k_()
    t = Type(CreateObject("roRenderThreadQueue"))
    println_AnyN_k_("SPIKE|INFO|task_rtq_create|type=" + t)
    q = CreateObject("roRenderThreadQueue")
    if q <> invalid then
        payload = CreateObject("roAssociativeArray")
        payload.addReplace("src", "task")
        payload.addReplace("num", 42)
        q.PostMessage("spike.pump", payload)
        println_AnyN_k_("SPIKE|INFO|task_posted|channel=spike.pump")
    else
        println_AnyN_k_("SPIKE|INFO|task_posted|skipped-no-rtq-on-task-thread")
    end if
end sub

sub __kotlinTaskMain()
    try
        RtqSpikeTask_run_k_()
        m.top.kotlinTaskState = "done"
    catch e
        errInfo = {message: "", number: 0}
        if Type(e) = "roAssociativeArray" then
            if e.message <> invalid then
                errInfo.message = e.message
            end if
            if e.number <> invalid then
                errInfo.number = e.number
            end if
            if e.backtrace <> invalid then
                errInfo.backtrace = e.backtrace
            end if
        end if
        m.top.kotlinTaskError = errInfo
        m.top.kotlinTaskState = "error"
    end try
end sub
