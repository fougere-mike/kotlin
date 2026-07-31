sub init()
    m.top.taskIn = ""
    m.top.taskOut = ""
    m.taskMain_k_ = SpikeTask_taskMain_k_
    m.__get_taskIn_k_ = SpikeTask___get_taskIn_k_
    m.__set_taskIn_Str_k_ = SpikeTask___set_taskIn_Str_k_
    m.__get_taskOut_k_ = SpikeTask___get_taskOut_k_
    m.__set_taskOut_Str_k_ = SpikeTask___set_taskOut_Str_k_
    m.top.setField("functionName", "SpikeTask_taskMain_k_")
end sub

sub SpikeTask_taskMain_k_()
    v = toString_AnyN_k_(m.top.getField("taskIn"))
    println_AnyN_k_("SpikeTask: taskMain running (task thread), taskIn=" + v)
    m.top.setField("taskOut", "task-wrote:" + v)
end sub

function SpikeTask___get_taskIn_k_() as String
    return m.taskIn
end function

sub SpikeTask___set_taskIn_Str_k_(value as String)
    m.taskIn = value
end sub

function SpikeTask___get_taskOut_k_() as String
    return m.taskOut
end function

sub SpikeTask___set_taskOut_Str_k_(value as String)
    m.taskOut = value
end sub
