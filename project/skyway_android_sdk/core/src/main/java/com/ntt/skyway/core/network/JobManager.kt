package com.ntt.skyway.core.network

import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class JobManager(private val scope: CoroutineScope) {
    // Use SupervisorJob to ensure that ChildJobs do not affect each other.
    private val jobsMutex = Mutex()
    @Volatile private var isTerminated = false
    private val jobs = mutableListOf<Job>()

    fun launchJob(block: suspend () -> Unit): Job? {
        if (isTerminated) {
            return null
        }
        // The invocation of block() will be executed in parallel.
        val job = scope.launch {
            block()
        }
        // Remove the job from the list once it is completed.
        job.invokeOnCompletion {
            scope.launch {
                jobsMutex.withLock {
                    jobs.remove(job)
                }
            }
        }
        // Use runBlocking to ensure that the jobs are added to the list when finishJobs is called.
        runBlocking {
            jobsMutex.withLock {
                jobs.add(job)
            }
        }
        return job
    }

    suspend fun terminateAllJobs() {
        isTerminated = true
        jobsMutex.withLock {
            Logger.log(Logger.LogLevel.DEBUG,"finishJobs called, jobs size: ${jobs.size}", "JobManager")
            jobs.joinAll()
            jobs.clear()
            Logger.log(Logger.LogLevel.DEBUG,"finishJobs completed, jobs cleared.", "JobManager")
        }
    }

    fun getJobListSize(): Int {
        return jobs.size
    }
}
