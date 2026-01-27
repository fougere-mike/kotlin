sub main()
    runTests_Function1TestRunnerV_k_(main_lambda_create_k_())
end sub

function main_lambda_create_k_() as Object
    this = {}
    this.__type = "main_lambda"
    this.__proto = ["main_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = main_lambda_invoke_AnyN_k_
    return this
end function

sub main_lambda_invoke_AnyN_k_(_this_runTests as Object)
    arrayListTests_rTestRunner_k_(_this_runTests)
    hashMapTests_rTestRunner_k_(_this_runTests)
    hashSetTests_rTestRunner_k_(_this_runTests)
    linkedHashMapTests_rTestRunner_k_(_this_runTests)
    linkedHashSetTests_rTestRunner_k_(_this_runTests)
    collectionExtensionsTests_rTestRunner_k_(_this_runTests)
    sortingTests_rTestRunner_k_(_this_runTests)
    sequenceTests_rTestRunner_k_(_this_runTests)
    unsignedTests_rTestRunner_k_(_this_runTests)
    mathTests_rTestRunner_k_(_this_runTests)
    stringBuilderTests_rTestRunner_k_(_this_runTests)
    stringExtensionsTests_rTestRunner_k_(_this_runTests)
    standardFunctionsTests_rTestRunner_k_(_this_runTests)
    lazyDelegateTests_rTestRunner_k_(_this_runTests)
    dateTimeTests_rTestRunner_k_(_this_runTests)
    jsonTests_rTestRunner_k_(_this_runTests)
    globalFunctionsTests_rTestRunner_k_(_this_runTests)
    kclassTests_rTestRunner_k_(_this_runTests)
    coroutineTests_rTestRunner_k_(_this_runTests)
    suspendFunctionTests_rTestRunner_k_(_this_runTests)
    dispatcherTests_rTestRunner_k_(_this_runTests)
    delayTrackerTests_rTestRunner_k_(_this_runTests)
    delayFunctionTests_rTestRunner_k_(_this_runTests)
    yieldFunctionTests_rTestRunner_k_(_this_runTests)
    coroutineQueueTests_rTestRunner_k_(_this_runTests)
    runBlockingTests_rTestRunner_k_(_this_runTests)
end sub
