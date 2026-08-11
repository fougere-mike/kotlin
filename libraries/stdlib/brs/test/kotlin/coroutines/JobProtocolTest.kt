package test.coroutines

import kotlin.test.*
import kotlin.coroutines.*
import kotlin.coroutines.cancellation.CancellationException

fun TestRunner.jobProtocolTests() {
    suite("Job completion protocol") {

        test("invokeOnCompletion fires on complete with null cause") {
            val job = Job()
            var fired = 0
            var seenCause: Throwable? = IllegalStateException("sentinel")
            job.invokeOnCompletion { cause ->
                fired = fired + 1
                seenCause = cause
            }
            assertTrue(job.complete())
            assertEquals(1, fired)
            assertNull(seenCause)
        }

        test("invokeOnCompletion fires with exception on completeExceptionally") {
            val job = Job()
            var seenCause: Throwable? = null
            job.invokeOnCompletion { cause -> seenCause = cause }
            val boom = IllegalStateException("boom")
            assertTrue(job.completeExceptionally(boom))
            assertEquals(boom, seenCause)
        }

        test("invokeOnCompletion fires with CancellationException on cancel") {
            val job = Job()
            var seenCause: Throwable? = null
            job.invokeOnCompletion { cause -> seenCause = cause }
            job.cancel()
            assertTrue(seenCause is CancellationException)
        }

        test("invokeOnCompletion on already-terminal job fires immediately") {
            val job = Job()
            job.complete()
            var fired = false
            job.invokeOnCompletion { fired = true }
            assertTrue(fired)
        }

        test("handler fires exactly once and dispose prevents firing") {
            val job = Job()
            var fired = 0
            val handle = job.invokeOnCompletion { fired = fired + 1 }
            handle.dispose()
            job.complete()
            assertEquals(0, fired)
        }

        test("throwing handler does not break other handlers") {
            val job = Job()
            var secondFired = false
            job.invokeOnCompletion { throw IllegalStateException("handler boom") }
            job.invokeOnCompletion { secondFired = true }
            job.complete()
            assertTrue(secondFired)
        }

        test("state flags across lifecycle") {
            val job = Job()
            assertTrue(job.isActive)
            assertFalse(job.isCompleted)
            assertFalse(job.isCancelled)
            job.complete()
            assertFalse(job.isActive)
            assertTrue(job.isCompleted)
            assertFalse(job.isCancelled)
        }

        test("cancelled job is terminal for a plain Job") {
            val job = Job()
            job.cancel()
            assertTrue(job.isCancelled)
            assertTrue(job.isCompleted)
            assertFalse(job.isActive)
        }

        test("complete after cancel returns false and does not overwrite outcome") {
            val job = Job()
            job.cancel()
            assertFalse(job.complete())
            assertTrue(job.isCancelled)
        }

        test("CancellationException in completeExceptionally is quiet cancellation") {
            val job = Job()
            var seenCause: Throwable? = null
            job.invokeOnCompletion { cause -> seenCause = cause }
            job.completeExceptionally(CancellationException("stop"))
            assertTrue(job.isCancelled)
            assertTrue(seenCause is CancellationException)
        }

        test("deferred completion protocol delivers value then handlers") {
            val d = CompletableDeferred<String>()
            var seen: Throwable? = IllegalStateException("sentinel")
            d.invokeOnCompletion { cause -> seen = cause }
            assertTrue(d.complete("v"))
            assertNull(seen)
            assertEquals("v", d.getCompleted())
            assertNull(d.getCompletionExceptionOrNull())
        }

        test("deferred getCompletionExceptionOrNull surfaces failure") {
            val d = CompletableDeferred<String>()
            val boom = IllegalStateException("boom")
            d.completeExceptionally(boom)
            assertEquals(boom, d.getCompletionExceptionOrNull())
        }

        test("complete(value) after cancel records completion without storing the value") {
            val d = CompletableDeferred<String>()
            d.cancel()
            assertFalse(d.complete("late"))
            assertTrue(d.isCompleted)
            var thrown: Throwable? = null
            try {
                d.getCompleted()
            } catch (e: Throwable) {
                thrown = e
            }
            assertTrue(thrown is CancellationException)
        }

        test("cancelled deferred getCompleted throws CancellationException") {
            val d = CompletableDeferred<String>()
            d.cancel()
            var thrown: Throwable? = null
            try {
                d.getCompleted()
            } catch (e: Throwable) {
                thrown = e
            }
            assertTrue(thrown is CancellationException)
        }
    }
}
