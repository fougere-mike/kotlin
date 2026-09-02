// Multi-catch try INSIDE a lambda (task 3b latent defect): the merged
// `__caught` variable BrsMultiCatchLowering synthesizes had no parent
// assigned, and BrsCallableReferenceLowering (which re-parents lambda bodies)
// crashed the whole compile with "Parent of element (VAR CATCH_PARAMETER
// name:__caught ...) is not initialized". Top-level multi-catch never hit the
// parent query — only lambda-hosted ones did. Pins the merged is-dispatch
// emitted inside a lowered lambda body.

fun runBlock(block: () -> String): String {
    return block()
}

fun classifyInLambda(input: String): String {
    return runBlock {
        try {
            if (input == "x") {
                throw IllegalStateException("boom")
            }
            "ok"
        } catch (e: IllegalStateException) {
            "ise"
        } catch (e: IllegalArgumentException) {
            "iae"
        }
    }
}
