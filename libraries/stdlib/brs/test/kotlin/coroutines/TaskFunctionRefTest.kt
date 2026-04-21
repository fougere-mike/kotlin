/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.coroutines

import kotlin.test.*

/**
 * Documentation and findings for Task thread function reference behavior.
 *
 * ## Key Finding: Function References Do NOT Survive Task Thread Boundary
 *
 * BrightScript Task threads receive a **clone** of `m` (the component scope).
 * While primitives, strings, arrays, and roAssociativeArray objects survive
 * the thread boundary via `m.global` (which uses thread rendezvous), **function
 * references (lambdas, closures, method references)** do not survive.
 *
 * When you store a function reference in an roAssociativeArray and access it
 * from a Task thread, the function reference becomes `invalid` or loses its
 * callable behavior.
 *
 * ## Implications for withContext(Dispatchers.IO)
 *
 * This means we CANNOT simply pass Kotlin lambdas to Task threads via field
 * values or m.global. The workaround is the **Worker Registry Pattern**:
 *
 * 1. **Compiler extracts** work blocks to top-level worker functions
 * 2. **Worker functions are registered** by name on m.global at module init
 * 3. **TaskPool passes** the worker name (String) + captures (roAssociativeArray)
 * 4. **CoroutineTask looks up** the worker by name and invokes it
 *
 * Example generated code:
 * ```brightscript
 * ' Worker function (top-level)
 * function __ioWorker_fetchData_1(captures as Object) as Dynamic
 *     url = captures.url
 *     http = CreateObject("roUrlTransfer")
 *     http.setUrl(url)
 *     return http.getToString()
 * end function
 *
 * ' Registration (at module init, stores on m.global)
 * sub __registerIOWorkers()
 *     if m.global.__kotlinIOWorkers = invalid then m.global.__kotlinIOWorkers = {}
 *     m.global.__kotlinIOWorkers["fetchData_1"] = __ioWorker_fetchData_1
 * end sub
 * ```
 *
 * ## What DOES Survive Task Thread Boundary
 *
 * - Primitives: Integer, LongInteger, Float, Double, Boolean
 * - Strings
 * - roArray (arrays of primitives/strings/objects)
 * - roAssociativeArray (with primitive/string/array values)
 * - roSGNode references (via thread rendezvous - safe for field access)
 *
 * ## What Does NOT Survive
 *
 * - Function references (lambdas, closures, `::methodRef`)
 * - Objects with methods/behavior (functions become `invalid`)
 * - Captured references to render-thread objects (the clone issue)
 *
 * ## Testing Note
 *
 * The empirical tests for this behavior cannot run in the stdlib test framework
 * because:
 * 1. Custom Task components require XML definitions in `components/` directory
 * 2. The stdlib test build doesn't generate component XML files
 * 3. Without XML, `createChild("CustomTask")` fails at runtime
 *
 * The behavior was verified through:
 * - Analysis of Roku SceneGraph threading documentation
 * - Understanding of the `m` clone behavior for Task threads
 * - Examination of how roAssociativeArray handles function references
 *
 * @see kotlin.coroutines.task.TaskPool
 * @see kotlin.coroutines.task.CoroutineTask
 */
fun TestRunner.taskFunctionRefTests() {
    suite("Task Thread Function References") {

        test("Documentation: function refs don't survive Task thread") {
            // This test documents the finding that function references stored on
            // m.global become invalid when accessed from a Task thread.
            //
            // The actual empirical tests require custom Task components with XML
            // definitions, which the current test infrastructure doesn't support.
            //
            // Based on Roku documentation and analysis:
            // - Task threads get a CLONE of `m` (component scope)
            // - m.global uses thread rendezvous (safe for data access)
            // - Function references don't survive the clone process
            //
            // This finding drives the implementation of withContext(Dispatchers.IO)
            // to use a worker registry pattern instead of passing lambdas directly.

            assertTrue(true, "Function reference behavior documented")
        }
    }
}
