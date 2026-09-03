// The spec-§9 "StateFlow emit/collect codegen" golden: pins the
// BrsFlowAccessLowering rewrites in a component + VM pair —
// (1) `vm.screenState.value` GET → stateFlowGetValue,
// (2) `MutableStateFlow.value` SET → stateFlowSetValue,
// (3) interface-typed member `collect(collector)` → flowCollectDispatch —
// plus the include-closure consequence: DoorbellsKt.brs (the doorbell
// registry + statics + onKotlinFlowDoorbell) lands in deps.json, both via the
// rewritten statics and transitively through the collect{} extension
// (TerminalsKt → DoorbellsKt).
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.coroutines.flow.*

class ScreenVm {
    private val _screenState = MutableStateFlow("loading")
    val screenState: StateFlow<String> = _screenState.asStateFlow()

    fun push(next: String) {
        _screenState.value = next
    }
}

class TitleCollector : FlowCollector<String> {
    var last: String = ""

    override suspend fun emit(value: String) {
        last = value
    }
}

class StateFlowAccess : GroupComponent() {
    @SGStringField
    var rendered: String = ""

    private val vm = ScreenVm()

    init {
        rendered = vm.screenState.value
        launch {
            vm.screenState.collect { v -> rendered = v }
        }
        launch {
            vm.screenState.collect(TitleCollector())
        }
        vm.push("ready")
    }
}
