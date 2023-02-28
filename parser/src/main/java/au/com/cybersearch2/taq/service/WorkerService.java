/** Copyright 2022 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.taq.service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Maintains a pool of work threads scaled according to available processors 
 */
public class WorkerService {

	private static int MAX_THREADS = Runtime.getRuntime().availableProcessors() + 2;
	
    /** Counts number of providers that are active. Used to trigger activation and shutdown of worker service */
    private final AtomicInteger workerClientCount;
    /** Semaphore to throttle work submissions */
    private final Semaphore semaphore;
	/** Execution service */
    private ExecutorService executorService;

	public WorkerService() {
		semaphore = new Semaphore(MAX_THREADS);
		workerClientCount = new AtomicInteger();
	}

	public void addClient() {
		if (workerClientCount.getAndIncrement() == 0)
            activate();
	}

	public void removeClient() {
		if (workerClientCount.decrementAndGet() == 0)
    		shutdownAndAwaitTermination(10L);
	}
	
    public synchronized void activate() {
    	if ((executorService == null) || executorService.isShutdown())
		    executorService = Executors.newFixedThreadPool(MAX_THREADS, Executors.defaultThreadFactory());	
    }

    public <T> T submitWork(Callable<T> worker, Class<T> clazz) throws InterruptedException, ExecutionException {
    	try {
    		semaphore.acquireUninterruptibly();
    	    return executorService.submit(worker).get();
    	} finally {
    		semaphore.release();
    	}
    }
    
    public void shutdownAndAwaitTermination(long timeout) {
    	if (timeout < 2L)
    		timeout = 20L;
    	executorService.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
        	    executorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
             }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
         }
     }

	public void await() throws InterruptedException {
		semaphore.acquire(MAX_THREADS);
	}
 }
