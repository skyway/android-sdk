package com.ntt.skyway.core.network

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

class JobManagerTest {
    private val jobManager = JobManager(CoroutineScope(Dispatchers.IO + SupervisorJob()))

    @Test
    fun finishJobsWaitsForAllJobs() = runBlocking {
        var job1Completed = false
        var job2Completed = false
        var job3Completed = false

        val job1 = jobManager.launchJob {
            delay(100)
            job1Completed = true
            println("Job 1 completed")
        }

        val job2 = jobManager.launchJob {
            delay(200)
            job2Completed = true
            println("Job 2 completed")
        }

        val job3 = jobManager.launchJob {
            delay(300)
            job3Completed = true
            println("Job 3 completed")
        }

        // Call finishJobs and wait for all jobs to complete
        jobManager.terminateAllJobs()

        // Log the completion states
        println("Job 1 completed: $job1Completed")
        println("Job 2 completed: $job2Completed")
        println("Job 3 completed: $job3Completed")

        // Assert that all jobs were completed before finishJobs returned
        assertTrue(job1Completed)
        assertTrue(job2Completed)
        assertTrue(job3Completed)
    }

    @Test
    fun jobIsRemovedFromListAfterCompletion() = runBlocking {
        val job1 = jobManager.launchJob {
            delay(100)
            println("Job 1 completed")
        }

        val job2 = jobManager.launchJob {
            delay(200)
            println("Job 2 completed")
        }

        val job3 = jobManager.launchJob {
            delay(300)
            println("Job 3 completed")
        }

        val job4 = jobManager.launchJob {
            delay(300)
            println("Job 3 completed")
        }

        job1?.join()
        job2?.join()
        job3?.join()
        job4?.join()

        delay(100)

        assertEquals(0, jobManager.getJobListSize())

    }

    @Test
    fun launchJobFromDifferentThreads() = runBlocking {
        val threadCount = 10
        val jobCompletedFlags = BooleanArray(threadCount)

        // Launch jobs from different threads
        val threads = List(threadCount) { index ->
            Thread {
                val job = jobManager.launchJob {
                    delay((index + 1) * 100L)
                    jobCompletedFlags[index] = true
                }
            }
        }

        // Start all threads
        threads.forEach { it.start() }

        // Wait for all threads to finish
        threads.forEach { it.join() }

        // Call finishJobs and wait for all jobs to complete
        jobManager.terminateAllJobs()

        // Assert that all jobs were completed before finishJobs returned
        jobCompletedFlags.forEachIndexed { index, completed ->
            assertTrue("Job $index did not complete", completed)
        }

        // Ensure the jobs list is empty after finishJobs
        assertEquals(0, jobManager.getJobListSize())
    }



    @Test
    fun cancelledJobsAreRemovedFromList() = runBlocking {
        val job = jobManager.launchJob {
            delay(1000)  // Simulate long-running job
        }

        // Cancel the job before it completes
        job?.cancel()

        // Wait for the cancellation to propagate
        delay(100)

        // Ensure that the job was removed from the list after cancellation
        jobManager.terminateAllJobs()
        assertEquals(0, jobManager.getJobListSize())
    }

    @Test
    fun finishJobsClearsAllJobs() = runBlocking {
        val job1 = jobManager.launchJob {
            delay(100)
        }

        // Wait for job1 to complete
        job1?.join()

        // Finish any remaining jobs (should be none)
        jobManager.terminateAllJobs()

        // Ensure the jobs list is empty
        assertEquals(0, jobManager.getJobListSize())
    }

    @Test
    fun largeScaleParallelJobExecution() = runBlocking {
        val largeJobCount = 10000
        val completionFlags = BooleanArray(largeJobCount) { false }

        repeat(largeJobCount) { index ->
            jobManager.launchJob {
                delay(10)
                completionFlags[index] = true
            }
        }

        jobManager.terminateAllJobs()

        // Ensure that all jobs completed
        assertTrue(completionFlags.all { it })
    }

    @Test
    fun emptyJobAreHandledCorrectly() = runBlocking {
        val job = jobManager.launchJob {
            // Empty block
        }

        job?.join()  // Wait for the job to complete

        // Ensure the job was removed from the list after completion
        jobManager.terminateAllJobs()
        assertEquals(0, jobManager.getJobListSize())
    }

    @Test
    fun SupervisorJobAllowsIndependentFailure() = runBlocking {

        var job1Exception: Throwable? = null

        // Launch job1, which will throw an exception
        val job1 = jobManager.launchJob {
            try {
                delay(100)
                throw RuntimeException("Job 1 failed")
            } catch (e: Throwable) {
                job1Exception = e
            }
        }

        // Launch job2, which should complete successfully
        val job2 = jobManager.launchJob {
            delay(200)
        }

        val job3 = jobManager.launchJob {
            delay(300)
        }

        // Wait for both jobs to complete
        job1?.join()
        job2?.join()
        job3?.join()

        // Assert that job2 completed successfully
        job2?.isCompleted?.let { assertTrue(it) }
        job3?.isCompleted?.let { assertTrue(it) }

        // Assert that job1 threw an exception
        assertNotNull(job1Exception)
        assertTrue(job1Exception is RuntimeException)
    }
}
